package com.example.feet.ui.screens

import android.content.Context
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.toArgb
import com.example.feet.R
import com.example.feet.ui.components.*
import com.example.feet.ui.theme.LiquidGlassColors
import com.example.feet.ui.theme.LiquidGradients
import com.example.feet.ui.viewmodels.SharedViewModel
import com.example.feet.ui.screens.EnhancedWaterScreen
import com.example.feet.ui.screens.StepsScreen
import com.example.feet.ui.screens.EnhancedWorkoutScreen
import kotlin.math.cos
import kotlin.math.sin

enum class BackgroundStyle { ANIMATED, BLACK }

private const val DefaultGlassBorderAccent = 0xFFFFFFFF.toInt()
private const val DefaultGlassTintAccent = 0xFFFFFFFF.toInt()

sealed class Screen(val route: String, val title: String) {
    object Water : Screen("water", "Hydration")
    object Steps : Screen("steps", "Feet")
    object Time : Screen("time", "Time")
    object Workout : Screen("workout", "Workout")
}

@Composable
fun MainScreen(viewModel: SharedViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(Screen.Water, Screen.Steps, Screen.Time, Screen.Workout)
    var showSettings by remember { mutableStateOf(false) }

    // ── Persist background style across process kills ──────────────────────────
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    var backgroundStyle by remember {
        mutableStateOf(
            BackgroundStyle.valueOf(
                prefs.getString("background_style", BackgroundStyle.ANIMATED.name)
                    ?: BackgroundStyle.ANIMATED.name
            )
        )
    }
    var glassBorderAccent by remember {
        mutableStateOf(Color(prefs.getInt("glass_border_accent", DefaultGlassBorderAccent)))
    }
    var glassTintAccent by remember {
        mutableStateOf(Color(prefs.getInt("glass_tint_accent", DefaultGlassTintAccent)))
    }
    LaunchedEffect(backgroundStyle) {
        prefs.edit().putString("background_style", backgroundStyle.name).apply()
    }
    LaunchedEffect(glassBorderAccent) {
        prefs.edit().putInt("glass_border_accent", glassBorderAccent.toArgb()).apply()
    }
    LaunchedEffect(glassTintAccent) {
        prefs.edit().putInt("glass_tint_accent", glassTintAccent.toArgb()).apply()
    }

    CompositionLocalProvider(
        LocalGlassAccentColors provides GlassAccentColors(
            border = glassBorderAccent,
            tint = glassTintAccent
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

        // ── Background ─────────────────────────────────────────────────────
        when (backgroundStyle) {
            BackgroundStyle.BLACK -> Box(
                modifier = Modifier.fillMaxSize().background(Color.Black)
            )
            BackgroundStyle.ANIMATED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ColorBendsBackground(
                        colors = listOf(
                            Color.Red,
                            Color(0xFFFF7F00),
                            Color.Yellow,
                            Color.Green,
                            Color.Blue,
                            Color(0xFF8B00FF)
                        ),
                        scale = 1.0f,
                        speed = 0.3f,
                        warpStrength = 1.0f,
                        mouseInfluence = 0.8f
                    )
                } else {
                    LiquidBackground()
                }
            }
        }

        // ── Main content ────────────────────────────────────────────────────
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                LiquidGlassNavigationBar(
                    selectedTab = selectedTab,
                    tabs = tabs,
                    onTabSelected = { selectedTab = it }
                )
            }
        ) { paddingValues ->
            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(
                            initialOffsetX = { width -> width },
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        ) + fadeIn() togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { width -> -width },
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                ) + fadeOut()
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { width -> -width },
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        ) + fadeIn() togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { width -> width },
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                ) + fadeOut()
                    }
                },
                label = "screen_transition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> EnhancedWaterScreen(viewModel)
                    1 -> StepsScreen(viewModel)
                    2 -> TimeScreen(viewModel)
                    3 -> EnhancedWorkoutScreen(viewModel)
                }
            }
        }

        // ── Settings icon button (top-right) ────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 12.dp, top = 4.dp)
        ) {
            val glassShape = RoundedCornerShape(12.dp)
            val accent = LocalGlassAccentColors.current
            IconButton(
                onClick = { showSettings = true },
                modifier = Modifier
                    .size(40.dp)
                    .clip(glassShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.18f),
                                accent.tint.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .background(Color(0xFF0A0A0F).copy(alpha = 0.65f))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                accent.border.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.10f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        ),
                        shape = glassShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = Color.White.copy(alpha = 0.90f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── Settings screen slide-in ─────────────────────────────────────────
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(350)),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(300))
        ) {
            // Overlay the same background so settings screen feels consistent
            Box(modifier = Modifier.fillMaxSize()) {
                when (backgroundStyle) {
                    BackgroundStyle.BLACK -> Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black)
                    )
                    BackgroundStyle.ANIMATED -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ColorBendsBackground(
                                colors = listOf(
                                    Color.Red, Color(0xFFFF7F00), Color.Yellow,
                                    Color.Green, Color.Blue, Color(0xFF8B00FF)
                                ),
                                scale = 1.0f, speed = 0.3f,
                                warpStrength = 1.0f, mouseInfluence = 0.8f
                            )
                        } else {
                            LiquidBackground()
                        }
                    }
                }
                SettingsScreen(
                    backgroundStyle = backgroundStyle,
                    onBackgroundStyleChange = { backgroundStyle = it },
                    glassBorderAccent = glassBorderAccent,
                    onGlassBorderAccentChange = { glassBorderAccent = it },
                    glassTintAccent = glassTintAccent,
                    onGlassTintAccentChange = { glassTintAccent = it },
                    onDismiss = { showSettings = false }
                )
            }
        }
    }
    }
}

@Composable
fun LiquidBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_bg")

    val wave1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )

    val wave2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .blur(radius = 2.dp)
    ) {
        drawLiquidWaves(wave1Offset, wave2Offset)
    }
}

fun DrawScope.drawLiquidWaves(wave1Offset: Float, wave2Offset: Float) {
    val width = size.width
    val height = size.height

    val wavePath1 = Path().apply {
        moveTo(0f, height * 0.3f)
        for (x in 0..width.toInt() step 5) {
            val y = height * 0.3f +
                    sin((x * 0.01f + wave1Offset * 0.017f)) * 50f +
                    cos((x * 0.005f + wave1Offset * 0.01f)) * 30f
            lineTo(x.toFloat(), y)
        }
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }

    drawPath(
        path = wavePath1,
        brush = Brush.verticalGradient(
            colors = listOf(
                LiquidGlassColors.DarkTeal.copy(alpha = 0.3f),
                LiquidGlassColors.SkyBlue.copy(alpha = 0.1f)
            )
        )
    )

    val wavePath2 = Path().apply {
        moveTo(0f, height * 0.4f)
        for (x in 0..width.toInt() step 5) {
            val y = height * 0.4f +
                    sin((x * 0.008f + wave2Offset * 0.02f)) * 40f +
                    cos((x * 0.012f + wave2Offset * 0.015f)) * 25f
            lineTo(x.toFloat(), y)
        }
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }

    drawPath(
        path = wavePath2,
        brush = Brush.verticalGradient(
            colors = listOf(
                LiquidGlassColors.SkyBlue.copy(alpha = 0.2f),
                LiquidGlassColors.LiquidBlue.copy(alpha = 0.05f)
            )
        )
    )
}

@Composable
fun FloatingGlassOrbs() {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")

    val orb1Y by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "orb1_y"
    )
    val orb2Y by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "orb2_y"
    )
    val orb3Y by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "orb3_y"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LiquidGlassColors.LiquidPurple.copy(alpha = 0.3f),
                    LiquidGlassColors.LiquidPurple.copy(alpha = 0.1f),
                    Color.Transparent
                )
            ),
            radius = 100f,
            center = Offset(100f, 200f + orb1Y)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LiquidGlassColors.LiquidCyan.copy(alpha = 0.25f),
                    LiquidGlassColors.LiquidCyan.copy(alpha = 0.08f),
                    Color.Transparent
                )
            ),
            radius = 80f,
            center = Offset(size.width - 120f, 250f + orb2Y)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LiquidGlassColors.LiquidBlue.copy(alpha = 0.2f),
                    LiquidGlassColors.LiquidBlue.copy(alpha = 0.05f),
                    Color.Transparent
                )
            ),
            radius = 120f,
            center = Offset(size.width / 2, size.height - 300f + orb3Y)
        )
    }
}

// ── Bottom navigation bar ──────────────────────────────────────────────────────
@Composable
fun LiquidGlassNavigationBar(
    selectedTab: Int,
    tabs: List<Screen>,
    onTabSelected: (Int) -> Unit
) {
    val glassShape = RoundedCornerShape(50)
    val accent = LocalGlassAccentColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 12.dp)
            .height(64.dp)
            .clip(glassShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        accent.tint.copy(alpha = 0.20f)
                    )
                )
            )
            .background(Color(0xFF0A0A0F).copy(alpha = 0.65f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accent.border.copy(alpha = 0.24f),
                        Color.White.copy(alpha = 0.05f),
                        accent.border.copy(alpha = 0.035f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = glassShape
            )
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent),
                        endY = size.height * 0.4f
                    )
                )
            }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, screen ->
                NavigationItem(
                    screen = screen,
                    isSelected = selectedTab == index,
                    onClick = { onTabSelected(index) }
                )
            }
        }
    }
}

@Composable
fun NavigationItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_scale"
    )

    val color = if (isSelected) Color.White.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.50f)
    val accent = LocalGlassAccentColors.current

    Box(
        modifier = Modifier
            .size(width = 54.dp, height = 44.dp)
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) accent.tint.copy(alpha = 0.16f) else Color.Transparent)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) accent.border.copy(alpha = 0.35f) else Color.Transparent,
                shape = RoundedCornerShape(50)
            )
            .bouncyClickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        NavIcon(
            screen = screen,
            color = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun NavIcon(
    screen: Screen,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (screen == Screen.Steps) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = screen.title,
            colorFilter = ColorFilter.tint(color),
            modifier = modifier.scale(1.4f)
        )
        return
    }

    Canvas(modifier = modifier) {
        when (screen) {
            Screen.Water -> drawWaterIcon(color)
            Screen.Time -> drawTimeIcon(color)
            Screen.Workout -> drawWorkoutIcon(color)
            Screen.Steps -> Unit
        }
    }
}

private fun DrawScope.drawWaterIcon(color: Color) {
    val path = Path().apply {
        moveTo(size.width * 0.50f, size.height * 0.05f)
        cubicTo(size.width * 0.22f, size.height * 0.34f, size.width * 0.14f, size.height * 0.50f, size.width * 0.14f, size.height * 0.67f)
        cubicTo(size.width * 0.14f, size.height * 0.88f, size.width * 0.30f, size.height * 0.98f, size.width * 0.50f, size.height * 0.98f)
        cubicTo(size.width * 0.70f, size.height * 0.98f, size.width * 0.86f, size.height * 0.88f, size.width * 0.86f, size.height * 0.67f)
        cubicTo(size.width * 0.86f, size.height * 0.50f, size.width * 0.78f, size.height * 0.34f, size.width * 0.50f, size.height * 0.05f)
        close()
    }
    drawPath(path = path, color = color)
}

private fun DrawScope.drawTimeIcon(color: Color) {
    val stroke = Stroke(width = 2.4.dp.toPx())
    drawCircle(color = color, radius = size.minDimension * 0.42f, center = center, style = stroke)
    drawLine(color = color, start = center, end = Offset(center.x, size.height * 0.28f), strokeWidth = 2.4.dp.toPx())
    drawLine(color = color, start = center, end = Offset(size.width * 0.67f, size.height * 0.58f), strokeWidth = 2.4.dp.toPx())
}

private fun DrawScope.drawWorkoutIcon(color: Color) {
    val strokeWidth = 3.dp.toPx()
    drawLine(color = color, start = Offset(size.width * 0.24f, size.height * 0.50f), end = Offset(size.width * 0.76f, size.height * 0.50f), strokeWidth = strokeWidth)
    drawRoundRect(color = color, topLeft = Offset(size.width * 0.06f, size.height * 0.34f), size = Size(size.width * 0.16f, size.height * 0.32f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
    drawRoundRect(color = color, topLeft = Offset(size.width * 0.78f, size.height * 0.34f), size = Size(size.width * 0.16f, size.height * 0.32f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
}
