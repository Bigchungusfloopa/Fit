package com.example.feet.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.feet.ui.GlassButton
import com.example.feet.ui.GlassCard
import com.example.feet.ui.LiquidProgressBar
import com.example.feet.ui.viewmodels.SharedViewModel
import kotlinx.coroutines.delay

@Composable
fun StepsScreen(viewModel: SharedViewModel) {
    val steps by viewModel.todaySteps.collectAsState()
    val goal by viewModel.dailyStepGoal.collectAsState()
    val progress by remember { derivedStateOf { viewModel.getStepsProgress() } }
    val weekHistory by viewModel.stepHistory.collectAsState()
    val isLiveTracking by viewModel.isLiveTracking.collectAsState()
    val hasStepSensor by viewModel.hasStepSensor.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var newGoal by remember { mutableStateOf(goal.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step Tracker",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Live Tracking Status Indicator
        TrackingStatusCard(
            status = viewModel.getTrackingStatus(),
            isLive = isLiveTracking,
            hasSensor = hasStepSensor
        )

        // Live Steps Card with Animation
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Today's Steps",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Animated step counter
                AnimatedStepCounter(
                    steps = steps,
                    isLive = isLiveTracking
                )

                Text(
                    text = "Goal: $goal steps",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LiquidProgressBar(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = if (isLiveTracking) Color(0xFF00E676) else Color(0xFF4CAF50),
                    waveColor = if (isLiveTracking) Color(0xFF69F0AE) else Color(0xFF81C784)
                )
            }
        }

        // Stats Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${viewModel.calculateCalories()}",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calories",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f".format(viewModel.calculateDistance()),
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Distance (km)",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Progress",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassButton(
                onClick = {
                    newGoal = goal.toString()
                    showGoalDialog = true
                },
                text = "🎯 Set Goal",
                modifier = Modifier.weight(1f)
            )

            GlassButton(
                onClick = { showHistory = true },
                text = "📊 History",
                modifier = Modifier.weight(1f)
            )
        }

        // Testing Controls - Show different text based on tracking mode
        if (!isLiveTracking || !hasStepSensor) {
            GlassButton(
                onClick = { viewModel.simulateStepUpdate() },
                text = if (isLiveTracking) "➕ Add Test Steps" else "👟 Simulate Walking",
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Set Goal Dialog
        if (showGoalDialog) {
            SetGoalDialog(
                currentGoal = newGoal,
                onGoalChange = { newGoal = it },
                onConfirm = {
                    val goalValue = newGoal.toIntOrNull() ?: 10000
                    viewModel.setDailyStepGoal(goalValue)
                    showGoalDialog = false
                },
                onDismiss = { showGoalDialog = false }
            )
        }

        // History Dialog
        if (showHistory) {
            StepHistoryDialog(
                history = viewModel.getWeekHistory(),
                onDismiss = { showHistory = false }
            )
        }
    }
}

@Composable
fun TrackingStatusCard(
    status: String,
    isLive: Boolean,
    hasSensor: Boolean
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing indicator for live tracking
            if (isLive) {
                PulsingDot(
                    color = Color(0xFF00E676),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = status,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = if (isLive) Color(0xFF69F0AE) else Color.White,
                fontWeight = FontWeight.Bold
            )

            if (!hasSensor) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(No sensor detected)",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun PulsingDot(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = alpha),
                shape = CircleShape
            )
    )
}

@Composable
fun AnimatedStepCounter(
    steps: Int,
    isLive: Boolean
) {
    val animatedSteps by animateIntAsState(
        targetValue = steps,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "steps"
    )

    Text(
        text = "$animatedSteps",
        style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
        color = if (isLive) Color(0xFF69F0AE) else Color.White,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SetGoalDialog(
    currentGoal: String,
    onGoalChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Set Daily Step Goal",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                androidx.compose.material3.TextField(
                    value = currentGoal,
                    onValueChange = onGoalChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Enter step goal",
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.White.copy(alpha = 0.6f),
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f)
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                // Quick preset buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        onClick = { onGoalChange("5000") },
                        text = "5,000",
                        modifier = Modifier.weight(1f)
                    )

                    GlassButton(
                        onClick = { onGoalChange("10000") },
                        text = "10,000",
                        modifier = Modifier.weight(1f)
                    )

                    GlassButton(
                        onClick = { onGoalChange("15000") },
                        text = "15,000",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        onClick = onDismiss,
                        text = "Cancel",
                        modifier = Modifier.weight(1f)
                    )

                    GlassButton(
                        onClick = onConfirm,
                        text = "Set Goal",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StepHistoryDialog(
    history: List<SharedViewModel.StepData>,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Step History",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (history.isEmpty()) {
                    Text(
                        text = "No history available yet",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history) { stepData ->
                            StepHistoryItem(stepData = stepData)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassButton(
                    onClick = onDismiss,
                    text = "Close",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StepHistoryItem(stepData: SharedViewModel.StepData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stepData.date,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${stepData.steps} steps",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            val percentage = (stepData.steps.toFloat() / stepData.goal * 100).toInt()
            Text(
                text = "$percentage% of goal",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = when {
                    percentage >= 100 -> Color(0xFF69F0AE)
                    percentage >= 75 -> Color(0xFFFFD54F)
                    else -> Color.White.copy(alpha = 0.6f)
                }
            )
        }
    }
}