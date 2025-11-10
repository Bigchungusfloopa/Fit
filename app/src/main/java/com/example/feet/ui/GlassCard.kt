package com.example.feet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFD8E3E7).copy(alpha = 0.2f),
                            Color(0xFFC4D3D9).copy(alpha = 0.1f)
                        )
                    )
                )
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.1f),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        )
                    }
                }
                .blur(radius = 0.8.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawWithCache {
                        onDrawBehind {
                            // Draw border with simple rectangle instead of round rect
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.1f),
                                        Color.White.copy(alpha = 0.3f)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, size.height)
                                ),
                                topLeft = Offset(0f, 0f),
                                size = size,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                            )
                        }
                    }
            )

            Box(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}