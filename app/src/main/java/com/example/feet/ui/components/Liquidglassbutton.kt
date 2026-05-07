package com.example.feet.ui.components // Or wherever your components are

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Enums to match what EnhancedWaterScreen.kt uses
enum class ButtonVariant { PRIMARY, SECONDARY }
enum class ButtonSize { SMALL, REGULAR }

@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Button",
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    size: ButtonSize = ButtonSize.REGULAR
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shape = RoundedCornerShape(14.dp)
    val accent = LocalGlassAccentColors.current

    // Glass background — primary gets a brighter white tint
    val bgAlpha = when {
        !enabled -> 0.04f
        isPressed && variant == ButtonVariant.PRIMARY -> 0.28f
        isPressed -> 0.18f
        variant == ButtonVariant.PRIMARY -> 0.20f
        else -> 0.10f
    }

    val borderColor = animateColorAsState(
        targetValue = when {
            !enabled -> Color.White.copy(alpha = 0.08f)
            variant == ButtonVariant.PRIMARY -> accent.border.copy(alpha = if (isPressed) 0.55f else 0.40f)
            else -> accent.border.copy(alpha = if (isPressed) 0.35f else 0.20f)
        },
        animationSpec = tween(150),
        label = "border"
    )

    val contentPadding = if (size == ButtonSize.SMALL)
        PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    else
        PaddingValues(horizontal = 24.dp, vertical = 12.dp)

    val textColor = if (enabled) Color.White.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = bgAlpha + 0.04f),
                        accent.tint.copy(alpha = bgAlpha)
                    )
                )
            )
            .background(Color(0xFF0A0A0F).copy(alpha = if (variant == ButtonVariant.PRIMARY) 0.30f else 0.45f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor.value,
                        borderColor.value.copy(alpha = borderColor.value.alpha * 0.4f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = shape
            )
            .bouncyClickable(enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = if (size == ButtonSize.SMALL) 14.sp else 16.sp,
            fontWeight = if (variant == ButtonVariant.PRIMARY) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
