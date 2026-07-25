package com.example.statussaver

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

enum class ScreenState {
    SPLASH,
    LANGUAGE,
    PERMISSION,
    DASHBOARD
}

val MEDIA_STARTING_URI: Uri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fmedia")

fun savePermissionUri(context: Context, uri: String) {
    val sharedPreferences = context.getSharedPreferences("StatusSaverPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("FOLDER_URI", uri).apply()
}

fun getSavedPermissionUri(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("StatusSaverPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("FOLDER_URI", null)
}

fun isFirstLaunch(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("StatusSaverPrefs", Context.MODE_PRIVATE)
    return !sharedPreferences.contains("SELECTED_LANGUAGE")
}

@Composable
fun MainAppNavigation() {
    val context = LocalContext.current

    var folderUri by remember {
        mutableStateOf<Uri?>(getSavedPermissionUri(context)?.let { Uri.parse(it) })
    }

    val initialScreen = remember {
        if (folderUri != null && !isFirstLaunch(context)) {
            ScreenState.DASHBOARD
        } else {
            ScreenState.SPLASH
        }
    }

    var currentScreen by remember { mutableStateOf(initialScreen) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)

            savePermissionUri(context, uri.toString())
            folderUri = uri
            currentScreen = ScreenState.SPLASH
            Toast.makeText(context, "Permission granted!", Toast.LENGTH_SHORT).show()
        }
    }

    when (currentScreen) {
        ScreenState.SPLASH -> {
            if (folderUri != null && !isFirstLaunch(context)) {
                SplashLoadingScreenUI(
                    onTimeout = {
                        currentScreen = ScreenState.DASHBOARD
                    }
                )
            } else {
                SplashScreenUI(
                    onStartClick = {
                        currentScreen = ScreenState.LANGUAGE
                    }
                )
            }
        }

        ScreenState.LANGUAGE -> {
            LanguageScreenUI(
                onDone = {
                    if (folderUri == null) {
                        currentScreen = ScreenState.PERMISSION
                    } else {
                        currentScreen = ScreenState.DASHBOARD
                    }
                }
            )
        }

        ScreenState.PERMISSION -> {
            PermissionScreenUI(
                onAllowClick = {
                    Toast.makeText(context, "Tap 'USE THIS FOLDER' to allow access", Toast.LENGTH_LONG).show()
                    folderPickerLauncher.launch(MEDIA_STARTING_URI)
                },
                onNotNowClick = {
                    currentScreen = ScreenState.DASHBOARD
                }
            )
        }

        ScreenState.DASHBOARD -> {
            DashboardScreen(
                grantedUriString = folderUri?.toString() ?: "",
                onOpenLanguage = { currentScreen = ScreenState.LANGUAGE },
                onOpenPermission = { currentScreen = ScreenState.PERMISSION }
            )
        }
    }
}

@Composable
fun WelcomeScreen() {
    MainAppNavigation()
}