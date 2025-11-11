package com.example.feet.ui.components // Or wherever your components are

import androidx.compose.foundation.background
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

    // Style based on new TranslucentBox
    // Primary buttons are slightly darker (more opaque)
    val baseAlpha = if (variant == ButtonVariant.PRIMARY) 0.25f else 0.15f
    val bgAlpha = if (isPressed) baseAlpha + 0.1f else baseAlpha
    val backgroundColor = Color.Black.copy(alpha = bgAlpha)

    val contentPadding = if (size == ButtonSize.SMALL)
        PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    else
        PaddingValues(horizontal = 24.dp, vertical = 12.dp)

    val textColor = if (enabled) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null // No ripple effect
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = if (size == ButtonSize.SMALL) 14.sp else 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}