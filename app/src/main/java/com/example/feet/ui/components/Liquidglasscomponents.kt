package com.example.feet.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate as drawRotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.feet.ui.theme.LiquidGlassColors

// Animated counter for smooth number transitions
@Composable
fun AnimatedCounter(
    value: Int,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    style: TextStyle = MaterialTheme.typography.headlineLarge
) {
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "counter"
    )

    Text(
        text = "$prefix$animatedValue$suffix",
        style = style,
        modifier = modifier
    )
}

// Liquid progress indicator
@Composable
fun LiquidProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    primaryColor: Color = LiquidGlassColors.LiquidBlue,
    secondaryColor: Color = LiquidGlassColors.LiquidCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "progress")

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(LiquidGlassColors.GlassLight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(RoundedCornerShape(height / 2))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(primaryColor, secondaryColor, primaryColor)
                    )
                )
                .drawBehind {
                    val shimmerWidth = size.width * 0.3f
                    val offset = size.width * shimmerOffset

                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            startX = offset - shimmerWidth / 2,
                            endX = offset + shimmerWidth / 2
                        )
                    )
                }
        )
    }
}

// Glass header with animated gradient
@Composable
fun LiquidGlassHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header")

    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        LiquidGlassColors.TextPrimary,
                        LiquidGlassColors.LiquidCyan,
                        LiquidGlassColors.TextPrimary
                    ),
                    startX = 0f,
                    endX = 1000f * gradientOffset
                ),
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = LiquidGlassColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Stat card with glass effect
@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier,
        glassLevel = GlassLevel.LIGHT,
        enableShimmer = false
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = LiquidGlassColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LiquidGlassColors.TextSecondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = LiquidGlassColors.TextTertiary
            )
        }
    }
}

// Liquid glass dialog
@Composable
fun LiquidGlassDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            glassLevel = GlassLevel.HEAVY,
            enableLiquidEffect = true
        ) {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = LiquidGlassColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                HorizontalDivider(
                    color = LiquidGlassColors.BorderLight,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                content()
            }
        }
    }
}

// Circular progress with liquid effect
@Composable
fun LiquidCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 12.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circular")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background circle
            drawCircle(
                color = LiquidGlassColors.GlassLight,
                radius = (size / 2).toPx() - strokeWidth.toPx() / 2,
                style = Stroke(width = strokeWidth.toPx())
            )

            // Progress arc
            drawRotate(rotation) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            LiquidGlassColors.LiquidBlue,
                            LiquidGlassColors.LiquidCyan,
                            LiquidGlassColors.LiquidPurple,
                            LiquidGlassColors.LiquidBlue
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            // Glow effect
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LiquidGlassColors.LiquidCyan.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                radius = (size / 2).toPx()
            )
        }
    }
}

// Animated floating icon
@Composable
fun FloatingIcon(
    icon: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Text(
        text = icon,
        fontSize = 48.sp,
        modifier = modifier
            .offset(y = offsetY.dp)
            .rotate(rotation)
    )
}

// Ripple effect button wrapper
@Composable
fun RippleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var ripplePosition by remember { mutableStateOf(Offset.Zero) }
    var showRipple by remember { mutableStateOf(false) }

    val rippleAlpha by animateFloatAsState(
        targetValue = if (showRipple) 0f else 0.3f,
        animationSpec = tween(600),
        finishedListener = { showRipple = false },
        label = "ripple"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                showRipple = true
                onClick()
            }
            .drawBehind {
                if (showRipple) {
                    drawCircle(
                        color = Color.White.copy(alpha = rippleAlpha),
                        radius = size.maxDimension,
                        center = ripplePosition
                    )
                }
            }
    ) {
        content()
    }
}