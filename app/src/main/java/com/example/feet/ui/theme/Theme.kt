package com.example.feet.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Liquid Glass Color Palette
object LiquidGlassColors {
    // Primary glass colors
    val GlassLight = Color(0x1AFFFFFF)
    val GlassMedium = Color(0x33FFFFFF)
    val GlassHeavy = Color(0x4DFFFFFF)

    // Accent colors with transparency
    val LiquidBlue = Color(0x8064B5F6)
    val LiquidCyan = Color(0x8090CAF9)
    val LiquidPurple = Color(0x80BB86FC)
    val LiquidPink = Color(0x80FFB6C1)
    val LiquidGreen = Color(0x8069F0AE)
    val LiquidOrange = Color(0x80FFB74D)

    // Background gradients
    val DeepOcean = Color(0xFF051923)
    val MidnightBlue = Color(0xFF003554)
    val DarkTeal = Color(0xFF006494)
    val SkyBlue = Color(0xFF0582CA)

    // Text colors with proper contrast
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xCCFFFFFF)
    val TextTertiary = Color(0x99FFFFFF)
    val TextDisabled = Color(0x66FFFFFF)

    // Border colors
    val BorderLight = Color(0x4DFFFFFF)
    val BorderMedium = Color(0x66FFFFFF)
    val BorderHeavy = Color(0x99FFFFFF)
}

// Gradient Brushes for liquid effects
object LiquidGradients {
    val oceanGradient = Brush.verticalGradient(
        colors = listOf(
            LiquidGlassColors.DeepOcean,
            LiquidGlassColors.MidnightBlue,
            LiquidGlassColors.DarkTeal
        )
    )

    val glassGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0x1AFFFFFF),
            Color(0x0DFFFFFF),
            Color(0x1AFFFFFF)
        )
    )

    val liquidBlueGradient = Brush.radialGradient(
        colors = listOf(
            LiquidGlassColors.LiquidBlue,
            LiquidGlassColors.LiquidCyan,
            Color.Transparent
        )
    )

    val shimmerGradient = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color(0x33FFFFFF),
            Color.Transparent
        )
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = LiquidGlassColors.LiquidBlue,
    secondary = LiquidGlassColors.LiquidCyan,
    tertiary = LiquidGlassColors.LiquidPurple,
    background = LiquidGlassColors.DeepOcean,
    surface = LiquidGlassColors.MidnightBlue,
    onPrimary = LiquidGlassColors.TextPrimary,
    onSecondary = LiquidGlassColors.TextPrimary,
    onTertiary = LiquidGlassColors.TextPrimary,
    onBackground = LiquidGlassColors.TextPrimary,
    onSurface = LiquidGlassColors.TextSecondary,
    surfaceVariant = LiquidGlassColors.GlassMedium,
    onSurfaceVariant = LiquidGlassColors.TextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = LiquidGlassColors.LiquidBlue,
    secondary = LiquidGlassColors.LiquidCyan,
    tertiary = LiquidGlassColors.LiquidPurple,
    background = LiquidGlassColors.DeepOcean,
    surface = LiquidGlassColors.MidnightBlue,
    onPrimary = LiquidGlassColors.TextPrimary,
    onSecondary = LiquidGlassColors.TextPrimary,
    onTertiary = LiquidGlassColors.TextPrimary,
    onBackground = LiquidGlassColors.TextPrimary,
    onSurface = LiquidGlassColors.TextSecondary,
    surfaceVariant = LiquidGlassColors.GlassMedium,
    onSurfaceVariant = LiquidGlassColors.TextSecondary
)

@Composable
fun FeetTheme(
    darkTheme: Boolean = true, // Force dark theme for liquid glass effect
    dynamicColor: Boolean = false, // Disable dynamic colors for consistent glass look
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // Always use dark for liquid glass

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set transparent status bar for full glass effect
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

            // Enable edge-to-edge display
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}