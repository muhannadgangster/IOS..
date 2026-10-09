package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = SpaceObsidian,
    primaryContainer = SpaceCardBg,
    onPrimaryContainer = NeonCyan,
    secondary = NeonViolet,
    onSecondary = Color.White,
    secondaryContainer = SpaceNavy,
    onSecondaryContainer = NeonViolet,
    tertiary = NeonPink,
    background = SpaceObsidian,
    onBackground = TextPrimary,
    surface = SpaceNavy,
    onSurface = TextPrimary,
    surfaceVariant = SpaceCardBg,
    onSurfaceVariant = TextSecondary,
    outline = SpaceCardBorder,
    outlineVariant = Color(0x1FFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EDFA),
    onPrimaryContainer = ElectricBlue,
    secondary = NeonViolet,
    onSecondary = Color.White,
    background = Color(0xFF0C1024), // Keep sleek dark glass ambient for launcher
    onBackground = TextPrimary,
    surface = Color(0xFF141A33),
    onSurface = TextPrimary,
    surfaceVariant = Color(0x33FFFFFF),
    onSurfaceVariant = TextSecondary,
    outline = Color(0x33FFFFFF)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // launcher is best with vibrant dark glass

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
