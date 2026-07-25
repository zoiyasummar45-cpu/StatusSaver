package com.example.statussaver.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Ek class banayenge jo hamare custom colors hold karegi
data class AppColors(
    val background: Color,
    val surface: Color,
    val text: Color,
    val subText: Color,
    val accent: Color
)

val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No colors provided")
}

@Composable
fun StatusSaverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Yahan se system theme detect hoti hai
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        AppColors(
            background = DarkBackground,
            surface = DarkSurface,
            text = DarkText,
            subText = DarkSubText,
            accent = AppAccentGreen
        )
    } else {
        AppColors(
            background = LightBackground,
            surface = LightSurface,
            text = LightText,
            subText = LightSubText,
            accent = AppAccentGreen
        )
    }

    CompositionLocalProvider(LocalAppColors provides colors) {
        content()
    }
}