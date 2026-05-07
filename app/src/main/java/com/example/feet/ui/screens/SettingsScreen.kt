package com.example.feet.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feet.ui.components.LocalGlassAccentColors
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SettingsScreen(
    backgroundStyle: BackgroundStyle,
    onBackgroundStyleChange: (BackgroundStyle) -> Unit,
    glassBorderAccent: Color,
    onGlassBorderAccentChange: (Color) -> Unit,
    glassTintAccent: Color,
    onGlassTintAccentChange: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Same background logic as MainScreen
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // ── Top bar ──────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Settings",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Section: Background ──────────────────────────────────────────
            Text(
                text = "BACKGROUND",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.40f),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            GlassSelectionCard {
                BackgroundStyleOption(
                    label = "Animated",
                    description = "Rainbow color-bends shader",
                    isSelected = backgroundStyle == BackgroundStyle.ANIMATED,
                    onClick = { onBackgroundStyleChange(BackgroundStyle.ANIMATED) }
                )
                GlassDivider()
                BackgroundStyleOption(
                    label = "Black",
                    description = "Pure black, battery-friendly",
                    isSelected = backgroundStyle == BackgroundStyle.BLACK,
                    onClick = { onBackgroundStyleChange(BackgroundStyle.BLACK) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "GLASS ACCENT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.40f),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            GlassSelectionCard {
                ColorWheelSetting(
                    label = "Border",
                    description = "Accent color applied only to glass edges",
                    selectedColor = glassBorderAccent,
                    onColorChange = onGlassBorderAccentChange
                )
                GlassDivider()
                ColorWheelSetting(
                    label = "Tint",
                    description = "Soft color wash inside glass panels",
                    selectedColor = glassTintAccent,
                    onColorChange = onGlassTintAccentChange
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Section: About ───────────────────────────────────────────────
            Text(
                text = "ABOUT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.40f),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )
            GlassSelectionCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Feet",
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "v1.0",
                        color = Color.White.copy(alpha = 0.40f),
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ── Reusable glass container for settings rows ────────────────────────────────
@Composable
private fun GlassSelectionCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    val accent = LocalGlassAccentColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                        accent.border.copy(alpha = 0.48f),
                        Color.White.copy(alpha = 0.08f)
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
                        colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent),
                        endY = size.height * 0.30f
                    )
                )
            }
            .padding(horizontal = 16.dp),
        content = content
    )
}

@Composable
private fun ColorWheelSetting(
    label: String,
    description: String,
    selectedColor: Color,
    onColorChange: (Color) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.90f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 13.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(selectedColor)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(10.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        ColorWheel(
            selectedColor = selectedColor,
            onColorChange = onColorChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
        )
    }
}

@Composable
private fun ColorWheel(
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val hsv = remember(selectedColor) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(selectedColor.toArgb(), it) }
    }

    fun updateColor(offset: Offset) {
        val radius = min(size.width, size.height) / 2f
        if (radius <= 0f) return
        val center = Offset(size.width / 2f, size.height / 2f)
        val dx = offset.x - center.x
        val dy = offset.y - center.y
        val distance = sqrt(dx * dx + dy * dy).coerceAtMost(radius)
        val hue = ((atan2(dy, dx) * 180f / PI.toFloat()) + 360f) % 360f
        val saturation = (distance / radius).coerceIn(0f, 1f)
        onColorChange(Color.hsv(hue = hue, saturation = saturation, value = 1f))
    }

    Canvas(
        modifier = modifier
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { updateColor(it) },
                    onDrag = { change, _ -> updateColor(change.position) }
                )
            }
    ) {
        val radius = min(size.width, size.height) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val hueColors = listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red
        )

        drawCircle(
            brush = Brush.sweepGradient(hueColors, center),
            radius = radius,
            center = center
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color.Transparent),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.18f),
            radius = radius,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        val angle = hsv[0] * PI.toFloat() / 180f
        val markerDistance = radius * hsv[1].coerceIn(0f, 1f)
        val marker = Offset(
            x = center.x + cos(angle) * markerDistance,
            y = center.y + sin(angle) * markerDistance
        )
        drawCircle(
            color = Color.White,
            radius = 9.dp.toPx(),
            center = marker,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        drawCircle(
            color = selectedColor,
            radius = 5.dp.toPx(),
            center = marker
        )
    }
}

@Composable
private fun GlassDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.08f))
    )
}

@Composable
private fun BackgroundStyleOption(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.90f),
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 13.sp
            )
        }
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(tween(200)) + scaleIn(tween(200)),
            exit = fadeOut(tween(150)) + scaleOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(
                        color = Color(0xFF7B61FF),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
