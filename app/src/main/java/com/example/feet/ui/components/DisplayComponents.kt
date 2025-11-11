package com.example.feet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A simple translucent box for text and info.
 */
@Composable
fun TranslucentBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            // --- CHANGE HERE ---
            // Increased opacity from 0.25f to 0.4f
            .background(Color.Black.copy(alpha = 0.4f))
            // --- END CHANGE ---
            .padding(16.dp),
        content = content
    )
}

/**
 * A "high-res" card with a purple accent glow, used for main displays.
 */
@Composable
fun GraphDisplayCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow( // Add a purple "glow"
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFF8B00FF) // Purple Accent
            )
            .clip(RoundedCornerShape(20.dp))
            .background( // Dark, high-res gradient
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.5f),
                        Color.Black.copy(alpha = 0.3f)
                    )
                )
            )
            .border( // A sharp, thin border
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            ),
        content = content
    )
}