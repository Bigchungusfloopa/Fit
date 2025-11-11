package com.example.feet.ui.screens

import android.os.Build // <-- ADDED THIS IMPORT
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import com.example.feet.ui.components.*
import com.example.feet.ui.theme.LiquidGlassColors
import com.example.feet.ui.theme.LiquidGradients
import com.example.feet.ui.viewmodels.SharedViewModel
import com.example.feet.ui.screens.EnhancedWaterScreen
// Import the actual screens you provided
import com.example.feet.ui.screens.StepsScreen
import com.example.feet.ui.screens.EnhancedWorkoutScreen
import kotlin.math.cos
import kotlin.math.sin

// --- EMOJI REMOVED ---
// Navigation items with liquid glass icons
sealed class Screen(val route: String, val title: String) {
    object Water : Screen("water", "Hydration")
    object Steps : Screen("steps", "Steps")
    object Workout : Screen("workout", "Workout")
}

@Composable
fun MainScreen(viewModel: SharedViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(Screen.Water, Screen.Steps, Screen.Workout)

    Box(
        modifier = Modifier
            .fillMaxSize()
        //.background(LiquidGradients.oceanGradient)
    ) {

        // --- UPDATED BACKGROUND ---
        // Checks if the phone is Android 13 (SDK 33) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ColorBendsBackground(
                // Use bright RGB/rainbow colors instead:
                colors = listOf(
                    Color.Red,
                    Color(0xFFFF7F00), // Orange
                    Color.Yellow,
                    Color.Green,
                    Color.Blue,
                    Color(0xFF8B00FF)  // Violet
                ),
                scale = 1.0f,
                speed = 0.3f,
                warpStrength = 1.0f,
                mouseInfluence = 0.8f
            )
        } else {
            // Fallback for older phones (Android 12 and below)
            // This will show your original liquid background
            LiquidBackground()
        }

        // Note: I've commented out the orbs, as the new shader
        // background is very detailed. You can re-enable if you like.
        // FloatingGlassOrbs()
        // --- END OF UPDATE ---


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
                    .padding(paddingValues),
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
                    // Updated to call the correct function from StepsScreen.kt
                    1 -> StepsScreen(viewModel)
                    // Updated to call the correct function from WorkoutScreen.kt
                    2 -> EnhancedWorkoutScreen(viewModel)
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

    // First wave layer
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

    // Second wave layer
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

    // Multiple floating orbs with different animations
    val orb1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1_y"
    )

    val orb2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2_y"
    )

    val orb3Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb3_y"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // Orb 1 - Top left
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

        // Orb 2 - Top right
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

        // Orb 3 - Bottom center
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

@Composable
fun LiquidGlassNavigationBar(
    selectedTab: Int,
    tabs: List<Screen>,
    onTabSelected: (Int) -> Unit
) {
    // Using the existing LiquidGlassCard component with available parameters
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(72.dp)
            // --- CHANGE HERE ---
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.5f), // Darker "blackish" gradient
                        Color.Black.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
        // --- END CHANGE ---
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

// --- UPDATED NAVIGATION ITEM ---
@Composable
fun NavigationItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_scale"
    )

    // --- CHANGE HERE ---
    // Toned-down colors for readability
    val color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.6f)
    // --- END CHANGE ---

    Column(
        modifier = Modifier
            .scale(scale)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = screen.title, // Only show the title
            fontSize = 14.sp,
            color = color,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(vertical = 8.dp) // Add some padding
        )
    }
}

//
// --- Placeholder functions removed ---
//