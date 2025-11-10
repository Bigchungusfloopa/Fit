package com.example.feet.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun LiquidProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF64B5F6),
    waveColor: Color = Color(0xFF90CAF9)
) {
    val animatedProgress = remember { Animatable(0f) }
    val waveOffset = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        launch {
            animatedProgress.animateTo(
                targetValue = progress,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            waveOffset.animateTo(
                targetValue = 2f,
                animationSpec = tween(durationMillis = 2000)
            )
            waveOffset.snapTo(0f)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val width = size.width
        val height = size.height
        val progressHeight = height * animatedProgress.value

        // Draw background
        drawRect(
            color = color.copy(alpha = 0.2f),
            size = size
        )

        if (progressHeight > 0) {
            val path = Path().apply {
                moveTo(0f, height)

                val waveCount = 3
                val waveWidth = width / waveCount

                for (i in 0..waveCount) {
                    val x = i * waveWidth + waveOffset.value * 50
                    val y = height - progressHeight +
                            sin(x / 50f + waveOffset.value * 2f) * 5f

                    if (i == 0) {
                        lineTo(x, y)
                    } else {
                        quadraticBezierTo(
                            x1 = x - waveWidth / 2,
                            y1 = height - progressHeight + sin((x - waveWidth / 2) / 50f) * 8f,
                            x2 = x,
                            y2 = y
                        )
                    }
                }

                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            clipPath(path) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            waveColor,
                            color,
                            color.copy(alpha = 0.8f)
                        ),
                        startY = height - progressHeight,
                        endY = height
                    ),
                    size = Size(width, height)
                )

                // Add highlight
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.7f, height - progressHeight * 0.5f),
                        radius = progressHeight * 0.6f
                    )
                )
            }
        }

        // Draw border
        drawRect(
            color = color.copy(alpha = 0.3f),
            topLeft = Offset(0f, 0f),
            size = size,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
    }
}