package com.example.statussaver

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder

val PillBg = Color(0xFF262626)

fun repostToWhatsApp(context: Context, uri: Uri, isVideo: Boolean) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (isVideo) "video/*" else "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (isVideo) "video/*" else "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                setPackage("com.whatsapp.w4b")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e2: Exception) {
            Toast.makeText(context, "WhatsApp not found", Toast.LENGTH_SHORT).show()
        }
    }
}

fun shareMedia(context: Context, uri: Uri, isVideo: Boolean) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (isVideo) "video/*" else "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Status"))
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot share status", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun DashboardScreen(
    grantedUriString: String,
    onOpenLanguage: () -> Unit = {},
    onOpenPermission: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentLang = remember { getSavedLanguage(context) }

    // Bottom Nav State: 0 = Status, 1 = Saved, 2 = Setting
    var bottomNavIndex by remember { mutableIntStateOf(0) }

    // Segmented Toggle State: false = Simple WhatsApp ("Status"), true = WhatsApp Business ("B Status")
    var isBusinessSelected by remember { mutableStateOf(false) }

    // Media & Loading State
    var mediaList by remember { mutableStateOf(emptyList<StatusMedia>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Selected Items for Delete
    var selectedUris by remember { mutableStateOf(setOf<Uri>()) }

    // Full Screen Viewer State
    var viewerMedia by remember { mutableStateOf<StatusMedia?>(null) }
    var isViewingFromSavedTab by remember { mutableStateOf(false) }

    // Delete Confirmation Dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load Statuses
    LaunchedEffect(grantedUriString, isBusinessSelected, bottomNavIndex) {
        if (bottomNavIndex == 0) {
            isLoading = true
            mediaList = fetchStatuses(context, grantedUriString, isBusiness = isBusinessSelected)
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- Top App Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedUris.isNotEmpty()) "${selectedUris.size} Selected" else Strings.get("app_title", currentLang),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                if (selectedUris.isNotEmpty()) {
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Render Tab Screen Content
            when (bottomNavIndex) {
                0 -> StatusTabContent(
                    isBusinessSelected = isBusinessSelected,
                    onToggleBusiness = { isBusinessSelected = it },
                    mediaList = mediaList,
                    isLoading = isLoading,
                    selectedUris = selectedUris,
                    onItemClick = { media ->
                        if (selectedUris.isNotEmpty()) {
                            selectedUris = if (selectedUris.contains(media.uri)) {
                                selectedUris - media.uri
                            } else {
                                selectedUris + media.uri
                            }
                        } else {
                            viewerMedia = media
                            isViewingFromSavedTab = false
                        }
                    },
                    onRefresh = {
                        isLoading = true
                        mediaList = emptyList()
                    }
                )

                1 -> SavedTabContent(
                    selectedUris = selectedUris,
                    onItemClick = { media ->
                        if (selectedUris.isNotEmpty()) {
                            selectedUris = if (selectedUris.contains(media.uri)) {
                                selectedUris - media.uri
                            } else {
                                selectedUris + media.uri
                            }
                        } else {
                            viewerMedia = media
                            isViewingFromSavedTab = true
                        }
                    }
                )

                2 -> SettingsScreenUI(
                    onOpenLanguage = onOpenLanguage,
                    onOpenPermission = onOpenPermission
                )
            }
        }

        // --- Bottom Navigation Bar (3 Tabs: Status, Saved, Setting) ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF18181A)),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Status
            BottomNavItem(
                icon = Icons.Default.DonutLarge,
                label = Strings.get("status_tab", currentLang),
                isSelected = bottomNavIndex == 0,
                onClick = { bottomNavIndex = 0 }
            )

            // Tab 2: Saved
            BottomNavItem(
                icon = Icons.Default.Download,
                label = Strings.get("saved_tab", currentLang),
                isSelected = bottomNavIndex == 1,
                onClick = { bottomNavIndex = 1 }
            )

            // Tab 3: Setting
            BottomNavItem(
                icon = Icons.Default.Settings,
                label = Strings.get("setting_tab", currentLang),
                isSelected = bottomNavIndex == 2,
                onClick = { bottomNavIndex = 2 }
            )
        }
    }

    // Delete Confirmation Dialog matching Image 2
    if (showDeleteDialog) {
        DeleteStatusDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirmDelete = {
                val count = selectedUris.size
                selectedUris.forEach { uri ->
                    deleteSavedMedia(context, StatusMedia(uri, MediaType.IMAGE))
                }
                selectedUris = emptySet()
                showDeleteDialog = false
                Toast.makeText(context, "Deleted $count item(s)", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Full Screen Status & Video Viewer Dialog matching Mockups 1, 2, 3, 4
    if (viewerMedia != null) {
        FullMediaViewerDialog(
            media = viewerMedia!!,
            isFromSavedTab = isViewingFromSavedTab,
            onDismiss = { viewerMedia = null }
        )
    }
}

@Composable
fun DeleteStatusDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF242426)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                // Red Warning Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B1A1A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFFF3B30),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Delete status?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This action cannot be undone.",
                    color = TextGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(color = Color(0xFF38383A), thickness = 1.dp)

                // Split Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    VerticalDivider(color = Color(0xFF38383A), thickness = 1.dp)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onConfirmDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Delete",
                            color = Color(0xFFFF3B30),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTabContent(
    isBusinessSelected: Boolean,
    onToggleBusiness: (Boolean) -> Unit,
    mediaList: List<StatusMedia>,
    isLoading: Boolean,
    selectedUris: Set<Uri>,
    onItemClick: (StatusMedia) -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val currentLang = remember { getSavedLanguage(context) }
    var savedUris by remember { mutableStateOf(setOf<Uri>()) }

    Column(modifier = Modifier.fillMaxSize().padding(bottom = 64.dp)) {

        // Segmented Toggle Pill (Status | B Status)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(PillBg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (!isBusinessSelected) NeonGreen else Color.Transparent)
                    .clickable { onToggleBusiness(false) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.get("status_tab", currentLang),
                    color = if (!isBusinessSelected) Color.Black else TextGray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isBusinessSelected) NeonGreen else Color.Transparent)
                    .clickable { onToggleBusiness(true) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.get("b_status_tab", currentLang),
                    color = if (isBusinessSelected) Color.Black else TextGray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonGreen)
            }
        } else if (mediaList.isEmpty()) {
            EmptyStateUI(
                title = Strings.get("no_statuses_found", currentLang),
                subtitle = Strings.get("empty_subtitle", currentLang)
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(mediaList) { media ->
                        val isSelected = selectedUris.contains(media.uri)
                        val isSaved = savedUris.contains(media.uri)

                        StatusGridItem(
                            media = media,
                            isSelected = isSelected,
                            isSaved = isSaved,
                            onClick = { onItemClick(media) },
                            onDownloadClick = {
                                val success = saveMediaToGallery(context, media)
                                if (success) {
                                    savedUris = savedUris + media.uri
                                }
                            }
                        )
                    }
                }

                // Floating Action Button (Refresh 🔄)
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusGridItem(
    media: StatusMedia,
    isSelected: Boolean,
    isSaved: Boolean = false,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(3.dp)
            .aspectRatio(1f)
            .border(
                width = if (isSelected) 2.5.dp else 0.dp,
                color = if (isSelected) NeonGreen else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = media.uri,
                imageLoader = imageLoader,
                contentDescription = "Media",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Video Play Overlay
            if (media.mediaType == MediaType.VIDEO) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Audio Icon Overlay
            if (media.mediaType == MediaType.AUDIO) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AudioFile,
                        contentDescription = "Audio",
                        tint = NeonGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Selection Badge / Saved Checkmark Badge / Download Icon
            if (isSelected || isSaved) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Saved",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = { onDownloadClick() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(30.dp)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SavedTabContent(
    selectedUris: Set<Uri>,
    onItemClick: (StatusMedia) -> Unit
) {
    val context = LocalContext.current
    val currentLang = remember { getSavedLanguage(context) }
    var savedList by remember { mutableStateOf(emptyList<StatusMedia>()) }

    LaunchedEffect(Unit) {
        savedList = fetchSavedStatuses(context)
    }

    if (savedList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateUI(
                title = Strings.get("no_statuses_saved", currentLang),
                subtitle = Strings.get("empty_subtitle", currentLang)
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
        ) {
            items(savedList) { media ->
                val isSelected = selectedUris.contains(media.uri)
                StatusGridItem(
                    media = media,
                    isSelected = isSelected,
                    isSaved = true,
                    onClick = { onItemClick(media) },
                    onDownloadClick = {}
                )
            }
        }
    }
}

@Composable
fun EmptyStateUI(title: String, subtitle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.empty_status_illustration),
            contentDescription = "No Statuses",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            color = TextGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isSelected) NeonGreen.copy(alpha = 0.2f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) NeonGreen else TextGray,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = label,
            color = if (isSelected) NeonGreen else TextGray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun FullMediaViewerDialog(
    media: StatusMedia,
    isFromSavedTab: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isSavedState by remember { mutableStateOf(isFromSavedTab) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Video Player Control States
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isNightMode by remember { mutableStateOf(false) }
    var isControlsLocked by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var volumeLevel by remember { mutableFloatStateOf(0.7f) }
    var brightnessLevel by remember { mutableFloatStateOf(0.8f) }

    val exoPlayer = remember(media.uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(media.uri))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(media.uri) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(isMuted, playbackSpeed, volumeLevel) {
        exoPlayer.volume = if (isMuted) 0f else volumeLevel
        exoPlayer.setPlaybackSpeed(playbackSpeed)
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) exoPlayer.play() else exoPlayer.pause()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isNightMode) Color.Black else Color(0xFF0F0F11))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Media Content View
            if (media.mediaType == MediaType.VIDEO || media.mediaType == MediaType.AUDIO) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (media.mediaType == MediaType.AUDIO) {
                        Icon(
                            imageVector = Icons.Default.AudioFile,
                            contentDescription = "Audio Wave",
                            tint = NeonGreen,
                            modifier = Modifier.size(100.dp)
                        )
                    }

                    // Video Controls Layer (Mockup 4)
                    if (media.mediaType == MediaType.VIDEO && !isControlsLocked) {
                        // Top Circle Quick Actions Bar
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 56.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                            }

                            IconButton(onClick = { isMuted = !isMuted }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = "Mute",
                                    tint = if (isMuted) Color(0xFFFF3B30) else Color(0xFF2196F3),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .clickable {
                                        playbackSpeed = when (playbackSpeed) {
                                            1.0f -> 1.25f
                                            1.25f -> 1.5f
                                            1.5f -> 2.0f
                                            else -> 1.0f
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${playbackSpeed}x", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(onClick = { isNightMode = !isNightMode }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Nightlight, contentDescription = "Night", tint = Color.White, modifier = Modifier.size(18.dp))
                            }

                            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.CropFree, contentDescription = "Crop", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Left Side Green Volume Slider Overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Volume", tint = NeonGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(volumeLevel)
                                        .background(NeonGreen)
                                )
                            }
                        }

                        // Right Side Orange Brightness Slider Overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.WbSunny, contentDescription = "Brightness", tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(brightnessLevel)
                                        .background(Color(0xFFFF9800))
                                )
                            }
                        }

                        // Video Bottom Scrub & Playback Controls Row
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 80.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            // Progress bar
                            LinearProgressIndicator(
                                progress = { 0.3f },
                                color = NeonGreen,
                                trackColor = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Controls Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AspectRatio, contentDescription = "Aspect Ratio", tint = Color.White, modifier = Modifier.size(22.dp))
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(26.dp))
                                IconButton(onClick = { isPlaying = !isPlaying }) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "PlayPause",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(26.dp))
                                IconButton(onClick = { isControlsLocked = true }) {
                                    Icon(Icons.Default.LockOpen, contentDescription = "Lock", tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                // Image Full Screen View (Mockup 1, 2, 3)
                AsyncImage(
                    model = media.uri,
                    contentDescription = "Status Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top Header Bar (Back Arrow ← & Video Title)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                if (media.mediaType == MediaType.VIDEO) {
                    Text(
                        text = media.name.ifEmpty { "VID11000_11334" },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            val success = saveMediaToGallery(context, media)
                            if (success) {
                                isSavedState = true
                                Toast.makeText(context, "Status Saved!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Bottom Action Bar (Mockups 1, 2, 3: Repost | Share | Download/Saved/Delete)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(Color.Black.copy(alpha = 0.85f)),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button 1: Repost
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            repostToWhatsApp(context, media.uri, isVideo = media.mediaType == MediaType.VIDEO)
                        }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "Repost",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Repost", color = Color.White, fontSize = 12.sp)
                }

                // Button 2: Share
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            shareMedia(context, media.uri, isVideo = media.mediaType == MediaType.VIDEO)
                        }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Share", color = Color.White, fontSize = 12.sp)
                }

                // Button 3: Download / Saved / Delete
                if (isFromSavedTab) {
                    // Delete Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { showDeleteConfirmation = true }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Delete", color = Color(0xFFFF3B30), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isSavedState) {
                    // Saved Checkmark Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Saved",
                            tint = NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Saved", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Download Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                val success = saveMediaToGallery(context, media)
                                if (success) {
                                    isSavedState = true
                                    Toast.makeText(context, "Status Saved!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Download", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        DeleteStatusDialog(
            onDismiss = { showDeleteConfirmation = false },
            onConfirmDelete = {
                deleteSavedMedia(context, media)
                showDeleteConfirmation = false
                onDismiss()
                Toast.makeText(context, "Status deleted", Toast.LENGTH_SHORT).show()
            }
        )
    }
}