package com.example.feet.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.feet.ui.components.*
import com.example.feet.ui.theme.LiquidGlassColors
import com.example.feet.ui.viewmodels.SharedViewModel
import kotlin.math.sin



@Composable
fun EnhancedWaterScreen(viewModel: SharedViewModel) {
    val waterMl by viewModel.todayWater.collectAsState()
    val progress = viewModel.getWaterProgress()
    val glassesConsumed = viewModel.getGlassesConsumed()
    val glassesGoal = viewModel.getGlassesGoal()
    val glassSize by viewModel.glassSizeMl.collectAsState()
    val weightKg by viewModel.weightKg.collectAsState()
    val heightCm by viewModel.heightCm.collectAsState()
    val targetWeightKg by viewModel.targetWeightKg.collectAsState()
    val waterHistory by viewModel.waterHistory.collectAsState()

    // --- NEW STATE ---
    val dailyGoalMl by viewModel.dailyGoalMl.collectAsState()
    var showGoalDialog by remember { mutableStateOf(false) }
    var customGoalLiters by remember { mutableStateOf((dailyGoalMl / 1000f).toString()) }
    // --- END NEW STATE ---

    var showGlassSizeDialog by remember { mutableStateOf(false) }
    var customGlassSize by remember { mutableStateOf(glassSize.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Added for the header
    ) {


        Spacer(modifier = Modifier.height(8.dp))

        // --- UPDATED MAIN CARD ---
        // Removed GraphDisplayCard, using a simple clipped Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(20.dp)), // Clean, rounded edges
            contentAlignment = Alignment.Center
        ) {
            // Liquid fill animation
            LiquidWaterFill(
                progress = progress,
                modifier = Modifier.fillMaxSize()
            )

            // Column for text, etc.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // --- CHANGE HERE: Replaced AnimatedCounter with Text ---
                Text(
                    text = "${String.format("%.1f", waterMl.toFloat() / 1000f)}L",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )
                )
                // --- END CHANGE ---

                Text(
                    text = "$glassesConsumed / $glassesGoal glasses",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )

                // --- 2. REMOVED SLIDER ---
                // LiquidProgressIndicator(...) was here and is now removed.
                // --- END REMOVAL ---
            }
        }
        // --- END OF UPDATE ---

        // Quick add buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LiquidGlassButton(
                onClick = { viewModel.removeGlass() },
                text = "Remove",
                variant = ButtonVariant.SECONDARY,
                modifier = Modifier.weight(1f),
                enabled = waterMl > 0
            )

            LiquidGlassButton(
                onClick = { viewModel.addGlass() },
                text = "Add Glass",
                variant = ButtonVariant.PRIMARY,
                modifier = Modifier.weight(1f),
                enabled = glassesConsumed < glassesGoal // Fixed: Check glasses count, not ml
            )
        }

        // --- UPDATED GLASS SIZE CARD ---
        // Using TranslucentBox
        TranslucentBox(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Glass Size",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "${glassSize.toInt()} ml per glass",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                LiquidGlassButton(
                    onClick = {
                        customGlassSize = glassSize.toString()
                        showGlassSizeDialog = true
                    },
                    text = "Change",
                    size = ButtonSize.SMALL,
                    variant = ButtonVariant.SECONDARY
                )
            }
        }

        // --- NEW DAILY GOAL CARD ---
        TranslucentBox(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Goal",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "${String.format("%.1f", dailyGoalMl / 1000f)} Liters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                LiquidGlassButton(
                    onClick = {
                        customGoalLiters = (dailyGoalMl / 1000f).toString()
                        showGoalDialog = true
                    },
                    text = "Set Goal",
                    size = ButtonSize.SMALL,
                    variant = ButtonVariant.SECONDARY
                )
            }
        }
        // --- END NEW CARD ---


        // --- UPDATED STATS CARDS ---
        // Using TranslucentBox
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // "Remaining" stat box
            TranslucentBox(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // --- CHANGE HERE ---
                        val remainingLiters = viewModel.getRemainingWaterMl().toFloat() / 1000f
                        val goalLiters = dailyGoalMl / 1000f
                        Text(
                            text = String.format("%.1f", remainingLiters),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "L / ${String.format("%.1f", goalLiters)} L", // Updated Unit
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        // --- END CHANGE ---
                    }
                }
            }
            // "Hydration" stat box
            TranslucentBox(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = "Hydration",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }
                }
            }
        }

        // --- NEW SCROLLABLE BOX ADDED ---
        WaterHistorySection(history = waterHistory)

        // ── BMI & Weight Goal Card ───────────────────────────────────────────
        BmiCard(
            savedWeightKg = weightKg,
            savedHeightCm = heightCm,
            savedTargetKg = targetWeightKg,
            bmi = viewModel.getBmi(),
            bmiCategory = viewModel.getBmiCategory(),
            onSave = { w, h, t -> viewModel.saveBodyMetrics(w, h, t) }
        )

    } // End of main Column

    if (showGlassSizeDialog) {
        Dialog(onDismissRequest = { showGlassSizeDialog = false }) {
            GlassDialogBox {
                Text(
                    text = "Set Glass Size",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = customGlassSize,
                    onValueChange = { customGlassSize = it },
                    label = { Text("Size in ml") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = glassTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetButton("200", { customGlassSize = "200" }, modifier = Modifier.weight(1f))
                    PresetButton("250", { customGlassSize = "250" }, modifier = Modifier.weight(1f))
                    PresetButton("350", { customGlassSize = "350" }, modifier = Modifier.weight(1f))
                    PresetButton("500", { customGlassSize = "500" }, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiquidGlassButton(
                        onClick = { showGlassSizeDialog = false },
                        text = "Cancel",
                        variant = ButtonVariant.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )
                    LiquidGlassButton(
                        onClick = {
                            val newSize = customGlassSize.toFloatOrNull() ?: 250f
                            if (newSize > 0) viewModel.setGlassSize(newSize)
                            showGlassSizeDialog = false
                        },
                        text = "Save",
                        variant = ButtonVariant.PRIMARY,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }


    if (showGoalDialog) {
        Dialog(onDismissRequest = { showGoalDialog = false }) {
            GlassDialogBox {
                Text(
                    text = "Set Daily Goal",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = customGoalLiters,
                    onValueChange = { customGoalLiters = it },
                    label = { Text("Goal in Liters") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = glassTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiquidGlassButton(
                        onClick = { showGoalDialog = false },
                        text = "Cancel",
                        variant = ButtonVariant.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )
                    LiquidGlassButton(
                        onClick = {
                            val newGoal = customGoalLiters.toFloatOrNull() ?: 4f
                            if (newGoal > 0) viewModel.setDailyGoal(newGoal)
                            showGoalDialog = false
                        },
                        text = "Save",
                        variant = ButtonVariant.PRIMARY,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


@Composable
fun LiquidWaterFill(
    progress: Float,
    modifier: Modifier = Modifier
) {
    // Animated progress fill
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600, easing = EaseInOutCubic),
        label = "water_progress"
    )

    // Wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "wave_animation")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    Canvas(modifier = modifier) {
        val fillHeight = size.height * (1f - animatedProgress)

        // Create wave path - minimalistic wave
        val wavePath = Path().apply {
            moveTo(0f, fillHeight)

            // Create smooth wave with smaller amplitude
            val waveLength = size.width / 2f
            val amplitude = 8f // Subtle wave height

            var x = 0f
            while (x <= size.width) {
                val angle = ((x / waveLength) * 360f + waveOffset) * (Math.PI / 180f).toFloat()
                val y = fillHeight + sin(angle) * amplitude
                lineTo(x, y)
                x += 5f
            }

            // Complete the path
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        // Draw the water with gradient
        drawPath(
            path = wavePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.35f)
                ),
                startY = fillHeight,
                endY = size.height
            )
        )

        // Add a subtle glow line at the wave top
        drawPath(
            path = Path().apply {
                moveTo(0f, fillHeight)
                var x = 0f
                while (x <= size.width) {
                    val angle = ((x / (size.width / 2f)) * 360f + waveOffset) * (Math.PI / 180f).toFloat()
                    val y = fillHeight + sin(angle) * 8f
                    lineTo(x, y)
                    x += 5f
                }
            },
            color = Color.White.copy(alpha = 0.5f),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

// --- UPDATED PRESET BUTTON ---
@Composable
fun PresetButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth() // Make it fill the weight from the Row
            .clip(RoundedCornerShape(12.dp))
            .background(color = Color.Black.copy(alpha = 0.15f)) // Use secondary variant alpha
            .bouncyClickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    )
    {
        Text(
            text = "${text}ml",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// --- NEW COMPOSABLES ADDED FOR HISTORY ---

@Composable
fun WaterHistorySection(history: List<SharedViewModel.WaterData>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Past 10 Days",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (history.isEmpty()) {
            TranslucentBox(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No history yet. Drink some water!",
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                // Get the last 10 days and reverse them to show most recent first
                items(history.takeLast(10).reversed()) { item ->
                    HistoryItem(item = item)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(item: SharedViewModel.WaterData) {
    TranslucentBox(
        modifier = Modifier.width(100.dp) // Give each item a fixed width
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(), // Center content
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.totalMl.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = "ml",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                // Format date from "YYYY-MM-DD" to "MM-DD"
                text = item.date.substring(5),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

// ── BMI & Weight Goal Card ────────────────────────────────────────────────────
@Composable
fun BmiCard(
    savedWeightKg: Float,
    savedHeightCm: Float,
    savedTargetKg: Float,
    bmi: Float,
    bmiCategory: String,
    onSave: (weight: Float, height: Float, target: Float) -> Unit
) {
    var weight by remember(savedWeightKg) { mutableStateOf(if (savedWeightKg > 0f) savedWeightKg.toString() else "") }
    var height by remember(savedHeightCm) { mutableStateOf(if (savedHeightCm > 0f) savedHeightCm.toString() else "") }
    var target by remember(savedTargetKg) { mutableStateOf(if (savedTargetKg > 0f) savedTargetKg.toString() else "") }

    // Recompute live BMI from local inputs
    val liveBmi: Float = run {
        val w = weight.toFloatOrNull() ?: 0f
        val h = (height.toFloatOrNull() ?: 0f) / 100f
        if (h > 0f) w / (h * h) else 0f
    }
    val liveCategory = when {
        liveBmi <= 0f   -> ""
        liveBmi < 18.5f -> "Underweight"
        liveBmi < 25f   -> "Normal"
        liveBmi < 30f   -> "Overweight"
        else            -> "Obese"
    }
    val categoryColor = when (liveCategory) {
        "Normal"      -> Color(0xFF00C853)
        "Underweight" -> Color(0xFF40C4FF)
        "Overweight"  -> Color(0xFFFFAB00)
        "Obese"       -> Color(0xFFFF5252)
        else          -> Color.White.copy(alpha = 0.4f)
    }

    TranslucentBox(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "BMI & Weight",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold
            )

            // ── Inputs ──────────────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.weight(1f),
                    colors = glassTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                TextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height (cm)") },
                    modifier = Modifier.weight(1f),
                    colors = glassTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            TextField(
                value = target,
                onValueChange = { target = it },
                label = { Text("Target Weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                colors = glassTextFieldColors(),
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // ── BMI Result ──────────────────────────────────────────────────────
            if (liveBmi > 0f) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BMI",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                        Text(
                            text = "%.1f".format(liveBmi),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White.copy(alpha = 0.95f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Category badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = categoryColor.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(1.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = liveCategory,
                            color = categoryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                // ── Weight-to-goal indicator ─────────────────────────────────────
                val targetW = target.toFloatOrNull() ?: 0f
                val currentW = weight.toFloatOrNull() ?: 0f
                if (targetW > 0f && currentW > 0f) {
                    val diff = currentW - targetW
                    Text(
                        text = when {
                            diff > 0f  -> "%.1f kg to lose to reach goal".format(diff)
                            diff < 0f  -> "%.1f kg to gain to reach goal".format(-diff)
                            else       -> "🎉 Goal reached!"
                        },
                        color = if (diff == 0f) Color(0xFF00C853) else Color.White.copy(alpha = 0.65f),
                        fontSize = 13.sp
                    )
                }
            }

            // ── Save ────────────────────────────────────────────────────────────
            LiquidGlassButton(
                onClick = {
                    val w = weight.toFloatOrNull() ?: return@LiquidGlassButton
                    val h = height.toFloatOrNull() ?: return@LiquidGlassButton
                    val t = target.toFloatOrNull() ?: 0f
                    onSave(w, h, t)
                },
                text = "Save",
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.PRIMARY,
                enabled = weight.toFloatOrNull() != null && height.toFloatOrNull() != null
            )
        }
    }
}