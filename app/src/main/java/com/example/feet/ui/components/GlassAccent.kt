package com.example.feet.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class GlassAccentColors(
    val border: Color = Color.White,
    val tint: Color = Color.White
)

val LocalGlassAccentColors = staticCompositionLocalOf { GlassAccentColors() }
