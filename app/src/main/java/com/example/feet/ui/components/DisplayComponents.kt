package com.example.feet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Glassmorphism card — frosted glass look with gradient border and inner highlight.
 */
@Composable
fun TranslucentBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val accent = LocalGlassAccentColors.current
    Box(
        modifier = modifier
            .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.10f),
                        accent.tint.copy(alpha = 0.08f)
                    )
                )
            )
            .background(Color(0xFF0A0A0F).copy(alpha = 0.55f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accent.border.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.10f),
                        accent.border.copy(alpha = 0.10f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = shape
            )
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        endY = size.height * 0.35f
                    )
                )
            }
            .padding(16.dp),
        content = content
    )
}

/**
 * A high-res display card with a purple accent glow.
 */
@Composable
fun GraphDisplayCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val accent = LocalGlassAccentColors.current
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.10f),
                        accent.tint.copy(alpha = 0.10f)
                    )
                )
            )
            .background(Color(0xFF0A0A0F).copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accent.border.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.15f),
                        accent.border.copy(alpha = 0.30f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = shape
            ),
        content = content
    )
}

/**
 * Glassmorphism dialog container — used by all dialog popups across screens.
 */
@Composable
fun GlassDialogBox(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val accent = LocalGlassAccentColors.current
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        accent.tint.copy(alpha = 0.10f)
                    )
                )
            )
            .background(Color(0xFF080810).copy(alpha = 0.82f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accent.border.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.12f),
                        accent.border.copy(alpha = 0.08f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = shape
            )
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.07f), Color.Transparent),
                        endY = size.height * 0.30f
                    )
                )
            }
            .padding(24.dp),
        content = content
    )
}

/**
 * Shared TextField colors for all glass dialogs across the app.
 */
@Composable
fun glassTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White.copy(alpha = 0.08f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
    focusedTextColor = Color.White.copy(alpha = 0.95f),
    unfocusedTextColor = Color.White.copy(alpha = 0.75f),
    focusedLabelColor = Color.White.copy(alpha = 0.70f),
    unfocusedLabelColor = Color.White.copy(alpha = 0.50f),
    focusedPlaceholderColor = Color.White.copy(alpha = 0.50f),
    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.35f),
    focusedIndicatorColor = Color.White.copy(alpha = 0.40f),
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = Color.White.copy(alpha = 0.95f)
)

/**
 * Extension for natural bouncy click effect using spring physics.
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bouncy_scale"
    )

    this
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            enabled = enabled,
            onClick = onClick
        )
}
