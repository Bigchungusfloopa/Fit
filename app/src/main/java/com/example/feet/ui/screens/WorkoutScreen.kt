package com.example.feet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.feet.ui.GlassButton
import com.example.feet.ui.GlassCard
import com.example.feet.ui.viewmodels.SharedViewModel

@Composable
fun WorkoutScreen(viewModel: SharedViewModel) {
    val workouts by viewModel.todayWorkouts.collectAsState()
    val completedWorkouts = viewModel.getCompletedWorkoutsCount()

    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var workoutName by remember { mutableStateOf("") }
    var workoutDuration by remember { mutableStateOf("") }
    var workoutGoalValue by remember { mutableStateOf("") }
    var selectedGoalType by remember { mutableStateOf(SharedViewModel.GoalType.REPS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Workouts",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Simplified Summary - Only Completed Workouts
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$completedWorkouts Workouts Completed Today",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Add Workout Button
        GlassButton(
            onClick = {
                workoutName = ""
                workoutDuration = ""
                workoutGoalValue = ""
                selectedGoalType = SharedViewModel.GoalType.REPS
                showAddWorkoutDialog = true
            },
            text = "➕ Add Custom Workout",
            modifier = Modifier.fillMaxWidth()
        )

        // Workout List
        Text(
            text = "Today's Workouts",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(start = 8.dp)
        )

        if (workouts.isEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No workouts today\nAdd your first custom workout!",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(workouts) { workout ->
                    WorkoutItem(
                        workout = workout,
                        onToggleComplete = {
                            viewModel.toggleWorkout(workout.id)
                        },
                        onDelete = {
                            viewModel.deleteWorkout(workout.id)
                        }
                    )
                }
            }
        }

        // Add Custom Workout Dialog
        if (showAddWorkoutDialog) {
            AddWorkoutDialog(
                name = workoutName,
                onNameChange = { workoutName = it },
                duration = workoutDuration,
                onDurationChange = { workoutDuration = it },
                goalValue = workoutGoalValue,
                onGoalValueChange = { workoutGoalValue = it },
                goalType = selectedGoalType,
                onGoalTypeChange = { selectedGoalType = it },
                onConfirm = {
                    val duration = workoutDuration.toIntOrNull()
                    val goalValue = workoutGoalValue.toIntOrNull() ?: 0

                    // Only require name and goal value (duration is optional)
                    if (workoutName.isNotBlank() && goalValue > 0) {
                        viewModel.addCustomWorkout(workoutName, duration, goalValue, selectedGoalType)
                        showAddWorkoutDialog = false
                    }
                },
                onDismiss = { showAddWorkoutDialog = false }
            )
        }
    }
}

@Composable
fun WorkoutItem(
    workout: SharedViewModel.Workout,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row with name and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workout.name,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Delete button
                GlassButton(
                    onClick = onDelete,
                    text = "🗑️",
                    modifier = Modifier.width(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Workout details (Duration optional, Goal required)
            val durationText = workout.duration?.let { "$it min" } ?: ""
            val goalText = when (workout.goalType) {
                SharedViewModel.GoalType.REPS -> "${workout.goalValue} reps"
                SharedViewModel.GoalType.KM -> "${workout.goalValue} km"
            }

            Text(
                text = if (durationText.isNotEmpty()) {
                    "$durationText • $goalText"
                } else {
                    goalText
                },
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Complete/Undo button
            if (workout.completed) {
                // Show both Undo and Delete buttons for completed workouts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        onClick = onToggleComplete,
                        text = "↶ Undo",
                        modifier = Modifier.weight(1f)
                    )

                    GlassButton(
                        onClick = onDelete,
                        text = "🗑️ Delete",
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Show only Complete button for incomplete workouts
                GlassButton(
                    onClick = onToggleComplete,
                    text = "Mark as Complete",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AddWorkoutDialog(
    name: String,
    onNameChange: (String) -> Unit,
    duration: String,
    onDurationChange: (String) -> Unit,
    goalValue: String,
    onGoalValueChange: (String) -> Unit,
    goalType: SharedViewModel.GoalType,
    onGoalTypeChange: (SharedViewModel.GoalType) -> Unit,
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
                    text = "Add Custom Workout",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // Workout Name
                androidx.compose.material3.TextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Workout name (e.g., Running, Push-ups)",
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    label = { Text("Workout Name", color = Color.White) },
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.White.copy(alpha = 0.6f),
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                // Duration (Optional)
                androidx.compose.material3.TextField(
                    value = duration,
                    onValueChange = onDurationChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Duration in minutes (optional)",
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    label = { Text("Duration (Optional)", color = Color.White) },
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.White.copy(alpha = 0.6f),
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                // Goal Type Selection
                Text(
                    text = "Goal Type",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reps Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .background(
                                color = if (goalType == SharedViewModel.GoalType.REPS)
                                    Color.White.copy(alpha = 0.3f)
                                else
                                    Color.White.copy(alpha = 0.1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (goalType == SharedViewModel.GoalType.REPS)
                                    Color.White.copy(alpha = 0.6f)
                                else
                                    Color.White.copy(alpha = 0.3f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            .clickable { onGoalTypeChange(SharedViewModel.GoalType.REPS) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Repetitions",
                            color = if (goalType == SharedViewModel.GoalType.REPS)
                                Color.White
                            else
                                Color.White.copy(alpha = 0.7f),
                            fontWeight = if (goalType == SharedViewModel.GoalType.REPS)
                                FontWeight.Bold
                            else
                                FontWeight.Normal
                        )
                    }

                    // Distance Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .background(
                                color = if (goalType == SharedViewModel.GoalType.KM)
                                    Color.White.copy(alpha = 0.3f)
                                else
                                    Color.White.copy(alpha = 0.1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (goalType == SharedViewModel.GoalType.KM)
                                    Color.White.copy(alpha = 0.6f)
                                else
                                    Color.White.copy(alpha = 0.3f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            .clickable { onGoalTypeChange(SharedViewModel.GoalType.KM) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Distance (km)",
                            color = if (goalType == SharedViewModel.GoalType.KM)
                                Color.White
                            else
                                Color.White.copy(alpha = 0.7f),
                            fontWeight = if (goalType == SharedViewModel.GoalType.KM)
                                FontWeight.Bold
                            else
                                FontWeight.Normal
                        )
                    }
                }

                // Goal Value
                androidx.compose.material3.TextField(
                    value = goalValue,
                    onValueChange = onGoalValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            when (goalType) {
                                SharedViewModel.GoalType.REPS -> "Number of repetitions"
                                SharedViewModel.GoalType.KM -> "Distance in kilometers"
                            },
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    label = {
                        Text(
                            when (goalType) {
                                SharedViewModel.GoalType.REPS -> "Repetitions"
                                SharedViewModel.GoalType.KM -> "Distance (km)"
                            },
                            color = Color.White
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
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                // Quick preset examples
                Text(
                    text = when (goalType) {
                        SharedViewModel.GoalType.REPS -> "Examples: Push-ups (50 reps), Squats (100 reps)"
                        SharedViewModel.GoalType.KM -> "Examples: Running (5 km), Cycling (10 km)"
                    },
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Action buttons
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
                        text = "Add Workout",
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && goalValue.toIntOrNull()?.let { it > 0 } == true
                    )
                }
            }
        }
    }
}