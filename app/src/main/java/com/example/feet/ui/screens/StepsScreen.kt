package com.example.feet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.feet.ui.GlassButton
import com.example.feet.ui.GlassCard
import com.example.feet.ui.LiquidProgressBar
import com.example.feet.ui.viewmodels.SharedViewModel

@Composable
fun StepsScreen(viewModel: SharedViewModel) {
    val steps by viewModel.todaySteps.collectAsState()
    val goal by viewModel.dailyStepGoal.collectAsState()
    val progress by remember { derivedStateOf { viewModel.getStepsProgress() } }
    val weekHistory by viewModel.stepHistory.collectAsState()

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

        // Live Steps Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Today's Steps",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$steps",
                    style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
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
                    color = Color(0xFF4CAF50),
                    waveColor = Color(0xFF81C784)
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

        // Simulate Steps Button (for testing)
        GlassButton(
            onClick = { viewModel.simulateStepUpdate() },
            text = "👟 Simulate Walking",
            modifier = Modifier.fillMaxWidth()
        )

        // Live Tracking Status
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (viewModel.isStepTrackingAvailable()) "✅ Step Tracking Ready" else "❌ Step Tracking Unavailable",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (viewModel.isStepTrackingAvailable()) {
                        "Step tracking is ready. Use 'Simulate Walking' to test."
                    } else {
                        "Step tracking is not available on this device."
                    },
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
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

// ... rest of the StepsScreen.kt code remains the same (SetGoalDialog, StepHistoryDialog, StepHistoryItem)