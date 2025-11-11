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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border // Make sure this import is here
import androidx.compose.ui.Modifier
// import androidx.compose.ui.draw.blur // This import is no longer needed
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog // Import the standard Dialog
import com.example.feet.ui.components.* // Assuming LiquidGlassHeader and others are here
import com.example.feet.ui.theme.LiquidGlassColors
import com.example.feet.ui.viewmodels.SharedViewModel
import kotlin.math.sin


@Composable
fun EnhancedWaterScreen(viewModel: SharedViewModel) {
    val waterMl by viewModel.todayWater.collectAsState()
    val progress = viewModel.getWaterProgress()
    val glassesConsumed = viewModel.getGlassesConsumed()
    val glassesGoal = viewModel.getGlassesGoal()
    val glassSize = remember { viewModel.getGlassSize() }
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Added for the header
    ) {
        // --- 1. HEADER UPDATED ---
        Text(
            text = "Hydration Tracker",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp) // Added padding
        )
        // --- END HEADER UPDATE ---

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
                enabled = waterMl < dailyGoalMl // Changed from 4000
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
        // --- END ADDED ---

    } // End of main Column

    // --- UPDATED GLASS SIZE DIALOG ---
    if (showGlassSizeDialog) {
        // Use the standard Compose Dialog
        Dialog(
            onDismissRequest = { showGlassSizeDialog = false }
        ) {
            // Apply the "frosted black" style with higher opacity
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.6f)) // More opaque
                    // --- BLUR REMOVED HERE ---
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Set Glass Size",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )

                    TextField(
                        value = customGlassSize,
                        onValueChange = { customGlassSize = it },
                        label = { Text("Size in ml") },
                        modifier = Modifier.fillMaxWidth(),
                        // Text field colors to match the dark theme
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                            focusedTextColor = Color.White.copy(alpha = 0.9f),
                            unfocusedTextColor = Color.White.copy(alpha = 0.7f),
                            focusedLabelColor = Color.White.copy(alpha = 0.7f),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            focusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.White.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    // Quick preset buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetButton("200", { customGlassSize = "200" }, modifier = Modifier.weight(1f))
                        PresetButton("250", { customGlassSize = "250" }, modifier = Modifier.weight(1f))
                        PresetButton("350", { customGlassSize = "350" }, modifier = Modifier.weight(1f))
                        PresetButton("500", { customGlassSize = "500" }, modifier = Modifier.weight(1f))
                    }

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
                                if (newSize > 0) {
                                    viewModel.setGlassSize(newSize)
                                }
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
    }
    // --- END OF DIALOG UPDATE ---

    // --- NEW DIALOG FOR GOAL ---
    if (showGoalDialog) {
        Dialog(
            onDismissRequest = { showGoalDialog = false }
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Set Daily Goal",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )

                    TextField(
                        value = customGoalLiters,
                        onValueChange = { customGoalLiters = it },
                        label = { Text("Goal in Liters") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                            focusedTextColor = Color.White.copy(alpha = 0.9f),
                            unfocusedTextColor = Color.White.copy(alpha = 0.7f),
                            focusedLabelColor = Color.White.copy(alpha = 0.7f),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            focusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.White.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        )
                    )

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
                                if (newGoal > 0) {
                                    viewModel.setDailyGoal(newGoal)
                                }
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
    // --- END NEW DIALOG ---
}

@Composable
fun LiquidWaterFill(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "water_fill")

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Canvas(modifier = modifier) {
        val fillHeight = size.height * (1f - progress)

        val wavePath = Path().apply {
            moveTo(0f, fillHeight)

            for (x in 0..size.width.toInt() step 10) {
                val waveHeight = sin((x * 0.02f + waveOffset * 0.017f)) * 15f
                lineTo(x.toFloat(), fillHeight + waveHeight)
            }

            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        // --- 3. GRAPH COLOR UPDATED TO WHITE ---
        drawPath(
            path = wavePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.5f),
                    Color.White.copy(alpha = 0.3f)
                ),
                startY = fillHeight,
                endY = size.height
            )
        )

        // Add bubbles
        for (i in 0..5) {
            val bubbleY = fillHeight + (size.height - fillHeight) * (i / 5f)
            val bubbleX = size.width * (0.2f + i * 0.15f)
            val bubbleRadius = 5f + i * 2f

            drawCircle(
                color = Color.White.copy(alpha = 0.7f), // Brighter bubbles
                radius = bubbleRadius,
                center = Offset(
                    bubbleX + sin(waveOffset * 0.01f + i) * 20f,
                    bubbleY + sin(waveOffset * 0.02f + i) * 10f
                )
            )
        }
        // --- END OF GRAPH COLOR UPDATE ---
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
            .clickable(onClick = onClick)
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
// --- END OF NEW COMPOSABLES ---