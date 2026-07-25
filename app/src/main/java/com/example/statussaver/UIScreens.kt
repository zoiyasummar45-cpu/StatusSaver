package com.example.statussaver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.delay

val NeonGreen = Color(0xFF00E676)
val DarkBg = Color(0xFF121212)
val CardBg = Color(0xFF262626)
val TextGray = Color(0xFF9E9E9E)

fun showStatusNotification(context: Context) {
    val channelId = "status_saver_notifications"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Status Saver Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for new WhatsApp statuses"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.splash_logo)
        .setContentTitle("New Statuses Available! 📲")
        .setContentText("Open Status Saver to view and save new WhatsApp statuses.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(1001, notification)
}

fun getSavedLanguage(context: Context): String {
    val prefs = context.getSharedPreferences("StatusSaverPrefs", Context.MODE_PRIVATE)
    return prefs.getString("SELECTED_LANGUAGE", "English") ?: "English"
}

fun saveLanguage(context: Context, language: String) {
    val prefs = context.getSharedPreferences("StatusSaverPrefs", Context.MODE_PRIVATE)
    prefs.edit().putString("SELECTED_LANGUAGE", language).apply()
}

@Composable
fun SplashLoadingScreenUI(onTimeout: () -> Unit) {
    val context = LocalContext.current
    val lang = remember { getSavedLanguage(context) }

    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(NeonGreen),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = Strings.get("app_title", lang),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Strings.get("splash_subtitle", lang),
                color = TextGray,
                fontSize = 14.sp
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
        ) {
            LinearProgressIndicator(
                color = NeonGreen,
                trackColor = Color(0xFF2E2E2E),
                modifier = Modifier
                    .width(160.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
            )
        }
    }
}

@Composable
fun SplashScreenUI(onStartClick: () -> Unit) {
    val context = LocalContext.current
    val lang = remember { getSavedLanguage(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(NeonGreen),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = Strings.get("app_title", lang),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Strings.get("splash_subtitle", lang),
                color = TextGray,
                fontSize = 14.sp
            )
        }

        // Bottom "Click to Start" Button
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = Strings.get("click_to_start", lang),
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = Strings.get("terms_agree", lang),
                color = TextGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LanguageScreenUI(onDone: () -> Unit) {
    val context = LocalContext.current
    val languages = listOf("English", "العربية", "فارسی", "Deutsch", "Espanol", "Indonesi", "Urdu")
    var selectedLanguage by remember { mutableStateOf(getSavedLanguage(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Strings.get("language", selectedLanguage),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            // Green Checkmark Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NeonGreen)
                    .clickable {
                        saveLanguage(context, selectedLanguage)
                        onDone()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = Strings.get("lang_subtitle", selectedLanguage),
            color = TextGray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(languages) { lang ->
                val isSelected = selectedLanguage == lang

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(29.dp))
                        .background(if (isSelected) NeonGreen else CardBg)
                        .clickable { selectedLanguage = lang }
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lang,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 17.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )

                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedLanguage = lang },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.Black,
                            unselectedColor = TextGray
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionScreenUI(
    onAllowClick: () -> Unit,
    onNotNowClick: () -> Unit
) {
    val context = LocalContext.current
    val lang = remember { getSavedLanguage(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lock Folder Icon
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(2.dp, NeonGreen, RoundedCornerShape(28.dp))
                    .background(Color(0xFF1A2B22)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Folder Lock",
                    tint = NeonGreen,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = Strings.get("allow_storage_access", lang),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = Strings.get("allow_access_desc", lang),
                color = TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onAllowClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = Strings.get("allow_access_btn", lang),
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNotNowClick) {
                Text(
                    text = Strings.get("not_now", lang),
                    color = TextGray,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun SettingsScreenUI(onOpenLanguage: () -> Unit, onOpenPermission: () -> Unit) {
    val context = LocalContext.current
    val currentLang = remember { getSavedLanguage(context) }
    val prefs = remember { context.getSharedPreferences("StatusSaverPrefs", Context.MODE_PRIVATE) }

    var notificationEnabled by remember { mutableStateOf(prefs.getBoolean("NOTIF_ENABLED", true)) }
    var autoSaveEnabled by remember { mutableStateOf(prefs.getBoolean("AUTO_SAVE_ENABLED", false)) }
    var showRateUsDialog by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showStatusNotification(context)
            Toast.makeText(context, "Notification Sent!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // PRO Ad-Free Banner (Matching Mockup)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(Color(0xFFFF9800), Color(0xFFFF5722))),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3E2800)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = "PRO",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Remove All Ads",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Enjoy Status Saver without Ads",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Status Saver PRO Version Activated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Go PRO",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Group 1 (Language, Notification, Auto Save, Save Folder)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                // 1. Language
                SettingItemRow(
                    title = "Language",
                    subtitle = "Change your language",
                    rightContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = currentLang, color = NeonGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Arrow", tint = NeonGreen, modifier = Modifier.size(18.dp))
                        }
                    },
                    onClick = onOpenLanguage
                )

                HorizontalDivider(color = Color(0xFF2C2C2E))

                // 2. Notification
                SettingItemRow(
                    title = "Notification",
                    subtitle = "Get notified when new statuses available",
                    rightContent = {
                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = { isChecked ->
                                notificationEnabled = isChecked
                                prefs.edit().putBoolean("NOTIF_ENABLED", isChecked).apply()
                                if (isChecked) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        showStatusNotification(context)
                                        Toast.makeText(context, "Notification Alert Sent!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonGreen,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = CardBg
                            )
                        )
                    }
                )

                HorizontalDivider(color = Color(0xFF2C2C2E))

                // 3. Auto Save
                SettingItemRow(
                    title = "Auto Save",
                    subtitle = "Automatically Save all New Statuses",
                    rightContent = {
                        Switch(
                            checked = autoSaveEnabled,
                            onCheckedChange = { isChecked ->
                                autoSaveEnabled = isChecked
                                prefs.edit().putBoolean("AUTO_SAVE_ENABLED", isChecked).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonGreen,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = CardBg
                            )
                        )
                    }
                )

                HorizontalDivider(color = Color(0xFF2C2C2E))

                // 4. Save Statuses in Folder
                SettingItemRow(
                    title = "Save Statuses in Folder",
                    subtitle = "/storage/emulated/0/Download/StatusSaver",
                    onClick = onOpenPermission
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Group 2 (Privacy Policy, Share, Rate Us, About)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                // 5. Privacy Policy
                SettingItemRow(
                    title = "Privacy Policy",
                    subtitle = "Our Terms and conditions",
                    onClick = {
                        Toast.makeText(context, "Privacy Policy & Terms", Toast.LENGTH_SHORT).show()
                    }
                )

                HorizontalDivider(color = Color(0xFF2C2C2E))

                // 6. Share with others
                SettingItemRow(
                    title = "Share with others",
                    subtitle = "Share this app with your beloved friends",
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Download WhatsApp Status Saver App: https://play.google.com/store/apps/details?id=${context.packageName}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share App"))
                    }
                )

                HorizontalDivider(color = Color(0xFF2C2C2E))

                // 7. Rate Us (Triggers RateUsDialog matching Image 1)
                SettingItemRow(
                    title = "Rate Us",
                    subtitle = "Please support our work by your rating",
                    onClick = { showRateUsDialog = true }
                )

                HorizontalDivider(color = Color(0xFF2C2C2E))

                // 8. About
                SettingItemRow(
                    title = "About",
                    subtitle = "Version: 1.1.0"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Banner Ad Container
        BannerAdView()
    }

    if (showRateUsDialog) {
        RateUsDialog(onDismiss = { showRateUsDialog = false })
    }
}

@Composable
fun RateUsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var ratingState by remember { mutableIntStateOf(5) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2E)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Rate our App",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Please Rate our work by your feedback to keep us Inspired!",
                    color = Color(0xFFD0D0D0),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 5 Interactive Stars Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        val isFilled = i <= ratingState
                        Icon(
                            imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Star $i",
                            tint = if (isFilled) Color(0xFFFFC107) else Color(0xFF888888),
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { ratingState = i }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Bottom Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Left "NOT NOW" Button
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.5.dp, NeonGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text(
                            text = "NOT NOW",
                            color = NeonGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Right "RATE US" Button
                    Button(
                        onClick = {
                            onDismiss()
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Thank you for rating us $ratingState stars!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text(
                            text = "RATE US",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BannerAdView() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF333336), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF9800)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ad", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Status Saver Premium — Enjoy Ad-Free Experience",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun SettingItemRow(
    title: String,
    subtitle: String,
    rightContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 12.sp
                )
            }

            if (rightContent != null) {
                Spacer(modifier = Modifier.width(12.dp))
                rightContent()
            }
        }
    }
}