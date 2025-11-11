package com.example.feet.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Button",
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val buttonAlpha = if (isPressed) 0.3f else 0.2f
    val borderAlpha = if (isPressed) 0.4f else 0.3f

    Button(
        onClick = onClick,
        modifier = modifier,
        interactionSource = interactionSource,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke( // Keep the white border for contrast
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = borderAlpha),
                    Color.White.copy(alpha = borderAlpha * 0.5f)
                )
            )
        ),
        colors = ButtonDefaults.buttonColors(
            // --- CHANGE HERE ---
            containerColor = Color.Black.copy(alpha = buttonAlpha), // Blackish container
            contentColor = Color.White.copy(alpha = 0.9f), // Toned-down text
            disabledContainerColor = Color.Black.copy(alpha = 0.1f), // Darker disabled
            // --- END CHANGE ---
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(text = text)
    }
}