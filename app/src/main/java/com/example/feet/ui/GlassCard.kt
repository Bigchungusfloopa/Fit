package com.example.feet.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp)) // Clip the content and blur
            // --- CHANGE HERE ---
            .background(Color.Black.copy(alpha = 0.3f)) // Blackish translucent background
            // --- END CHANGE ---
            .blur(radius = 16.dp) // Frosted glass blur effect
            .border( // Keep a subtle white border
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        // We still add padding so your content isn't on the edges
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}