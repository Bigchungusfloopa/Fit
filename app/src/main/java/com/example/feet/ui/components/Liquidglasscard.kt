package com.example.feet.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.feet.ui.theme.LiquidGlassColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    glassLevel: GlassLevel = GlassLevel.MEDIUM,
    enableShimmer: Boolean = true,
    enableLiquidEffect: Boolean = true,
    borderGlow: Boolean = true,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glass_animation")

    // Shimmer animation
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // Liquid wave animation
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    // Border glow pulse
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        modifier = modifier
            .drawWithCache {
                onDrawBehind {
                    // Draw liquid effect background
                    if (enableLiquidEffect) {
                        drawLiquidBackground(waveOffset)
                    }

                    // Draw glass blur effect
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = when (glassLevel) {
                                GlassLevel.LIGHT -> listOf(
                                    Color(0x0DFFFFFF),
                                    Color(0x1AFFFFFF)
                                )
                                GlassLevel.MEDIUM -> listOf(
                                    Color(0x1AFFFFFF),
                                    Color(0x33FFFFFF)
                                )
                                GlassLevel.HEAVY -> listOf(
                                    Color(0x33FFFFFF),
                                    Color(0x4DFFFFFF)
                                )
                            }
                        )
                    )
                }
            },
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
                        colors = when (glassLevel) {
                            GlassLevel.LIGHT -> listOf(
                                LiquidGlassColors.GlassLight.copy(alpha = 0.3f),
                                LiquidGlassColors.GlassLight.copy(alpha = 0.1f)
                            )
                            GlassLevel.MEDIUM -> listOf(
                                LiquidGlassColors.GlassMedium.copy(alpha = 0.4f),
                                LiquidGlassColors.GlassMedium.copy(alpha = 0.2f)
                            )
                            GlassLevel.HEAVY -> listOf(
                                LiquidGlassColors.GlassHeavy.copy(alpha = 0.5f),
                                LiquidGlassColors.GlassHeavy.copy(alpha = 0.3f)
                            )
                        }
                    )
                )
                .blur(radius = 1.dp)
                .then(
                    if (borderGlow) {
                        Modifier.border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    LiquidGlassColors.BorderLight.copy(alpha = glowIntensity),
                                    LiquidGlassColors.BorderMedium.copy(alpha = glowIntensity * 0.7f),
                                    LiquidGlassColors.BorderHeavy.copy(alpha = glowIntensity)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    } else {
                        Modifier.border(
                            width = 1.dp,
                            color = LiquidGlassColors.BorderLight,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                )
        ) {
            // Shimmer overlay
            if (enableShimmer) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val shimmerWidth = size.width * 0.3f
                    val offset = size.width * shimmerOffset

                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x1AFFFFFF),
                                Color(0x33FFFFFF),
                                Color(0x1AFFFFFF),
                                Color.Transparent
                            ),
                            start = Offset(offset - shimmerWidth / 2, 0f),
                            end = Offset(offset + shimmerWidth / 2, size.height)
                        )
                    )
                }
            }

            Box(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

enum class GlassLevel {
    LIGHT,
    MEDIUM,
    HEAVY
}

// Extension function to draw liquid background
private fun DrawScope.drawLiquidBackground(waveOffset: Float) {
    val wavePath = Path().apply {
        moveTo(0f, size.height * 0.5f)

        for (x in 0..size.width.toInt() step 20) {
            val y = size.height * 0.5f +
                    sin((x * 0.02f + waveOffset * 0.01f)) * 20f +
                    cos((x * 0.03f + waveOffset * 0.02f)) * 10f
            lineTo(x.toFloat(), y)
        }

        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }

    drawPath(
        path = wavePath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0x1A64B5F6),
                Color(0x0D90CAF9)
            )
        )
    )
}