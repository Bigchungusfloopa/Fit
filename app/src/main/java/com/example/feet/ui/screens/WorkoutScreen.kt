package com.example.feet.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.feet.ui.components.ButtonSize
import com.example.feet.ui.components.ButtonVariant
import com.example.feet.ui.components.LiquidGlassButton
import com.example.feet.ui.components.TranslucentBox
import com.example.feet.ui.viewmodels.SharedViewModel

@Composable
fun EnhancedWorkoutScreen(viewModel: SharedViewModel) {
    val workouts by viewModel.todayWorkouts.collectAsState()
    val completedWorkouts = viewModel.getCompletedWorkoutsCount()

    // --- NEW MEDIA STATE ---
    val currentTrack by viewModel.currentTrack.collectAsState()
    val currentArtist by viewModel.currentArtist.collectAsState()
    var hasNotificationPermission by remember { mutableStateOf(viewModel.isNotificationListenerEnabled()) }
    // --- END NEW STATE ---

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
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
        )

        // Battery-style Progress Bar
        TranslucentBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Today's Progress",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                WorkoutBatteryIndicator(
                    completedWorkouts = completedWorkouts,
                    totalWorkouts = workouts.size
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$completedWorkouts of ${workouts.size} workouts completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // Add Workout Button
        LiquidGlassButton(
            onClick = {
                workoutName = ""
                workoutDuration = ""
                workoutGoalValue = ""
                selectedGoalType = SharedViewModel.GoalType.REPS
                showAddWorkoutDialog = true
            },
            text = "Add Custom Workout",
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.PRIMARY
        )

        // Workout List
        Text(
            text = "Today's Workouts",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(start = 8.dp)
        )

        if (workouts.isEmpty()) {
            TranslucentBox(
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
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Use weight modifier if list is not empty
        LazyColumn(
            modifier = Modifier.weight(if (workouts.isEmpty()) 1f else 1f),
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

        // Add a spacer to push it to the bottom if the list is empty
        if (workouts.isEmpty()) {
            Spacer(modifier = Modifier.weight(1f))
        }

        // --- NOW PLAYING CARD ---
        NowPlayingCard(
            artist = currentArtist,
            track = currentTrack,
            hasPermission = hasNotificationPermission,
            onRequestPermission = {
                viewModel.requestNotificationPermission()
            }
        )
        // --- END NOW PLAYING CARD ---

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
fun NowPlayingCard(
    artist: String?,
    track: String?,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    TranslucentBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        if (!hasPermission) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Enable notification access to see music",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                LiquidGlassButton(
                    onClick = onRequestPermission,
                    text = "Enable",
                    size = ButtonSize.SMALL,
                    variant = ButtonVariant.PRIMARY
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (!track.isNullOrBlank()) track else "No music playing",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                if (!artist.isNullOrBlank()) {
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutBatteryIndicator(
    completedWorkouts: Int,
    totalWorkouts: Int
) {
    val progress = if (totalWorkouts > 0) {
        completedWorkouts.toFloat() / totalWorkouts.toFloat()
    } else {
        0f
    }

    // Animate the progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "progress_animation"
    )

    // Progress bar color based on completion
    val progressColor = when {
        progress >= 1f -> Color(0xFF00E676) // Bright green when complete
        progress >= 0.5f -> Color(0xFF69F0AE) // Light green
        progress >= 0.25f -> Color(0xFFFFD54F) // Yellow
        else -> Color.White.copy(alpha = 0.3f) // Dim white
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rounded rectangle progress bar
        Box(
            modifier = Modifier
                .width(250.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(25.dp)
                )
        ) {
            // Filled portion with animation
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(25.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                progressColor.copy(alpha = 0.6f),
                                progressColor
                            )
                        )
                    )
            )

            // Percentage text overlay
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WorkoutItem(
    workout: SharedViewModel.Workout,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    TranslucentBox(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header row with name and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Delete button
                LiquidGlassButton(
                    onClick = onDelete,
                    text = "Delete",
                    size = ButtonSize.SMALL,
                    variant = ButtonVariant.SECONDARY
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
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Complete/Undo button
            LiquidGlassButton(
                onClick = onToggleComplete,
                text = if (workout.completed) "Undo" else "Mark as Complete",
                modifier = Modifier.fillMaxWidth(),
                variant = if (workout.completed) ButtonVariant.SECONDARY else ButtonVariant.PRIMARY
            )
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
    Dialog(
        onDismissRequest = onDismiss
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
                    text = "Add Custom Workout",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )

                TextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Workout Name") },
                    placeholder = { Text("e.g., Running, Push-ups") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        focusedTextColor = Color.White.copy(alpha = 0.9f),
                        unfocusedTextColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color.White.copy(alpha = 0.7f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                TextField(
                    value = duration,
                    onValueChange = onDurationChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Duration (Optional)") },
                    placeholder = { Text("Duration in minutes") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        focusedTextColor = Color.White.copy(alpha = 0.9f),
                        unfocusedTextColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color.White.copy(alpha = 0.7f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true
                )

                // Goal Type Selection
                Text(
                    text = "Goal Type",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reps Button
                    LiquidGlassButton(
                        onClick = { onGoalTypeChange(SharedViewModel.GoalType.REPS) },
                        text = "Repetitions",
                        modifier = Modifier.weight(1f),
                        variant = if (goalType == SharedViewModel.GoalType.REPS) ButtonVariant.PRIMARY else ButtonVariant.SECONDARY
                    )

                    // Distance Button
                    LiquidGlassButton(
                        onClick = { onGoalTypeChange(SharedViewModel.GoalType.KM) },
                        text = "Distance (km)",
                        modifier = Modifier.weight(1f),
                        variant = if (goalType == SharedViewModel.GoalType.KM) ButtonVariant.PRIMARY else ButtonVariant.SECONDARY
                    )
                }

                TextField(
                    value = goalValue,
                    onValueChange = onGoalValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            when (goalType) {
                                SharedViewModel.GoalType.REPS -> "Repetitions"
                                SharedViewModel.GoalType.KM -> "Distance (km)"
                            }
                        )
                    },
                    placeholder = {
                        Text(
                            when (goalType) {
                                SharedViewModel.GoalType.REPS -> "e.g., 50"
                                SharedViewModel.GoalType.KM -> "e.g., 5"
                            }
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        focusedTextColor = Color.White.copy(alpha = 0.9f),
                        unfocusedTextColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color.White.copy(alpha = 0.7f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true
                )

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LiquidGlassButton(
                        onClick = onDismiss,
                        text = "Cancel",
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.SECONDARY
                    )

                    LiquidGlassButton(
                        onClick = onConfirm,
                        text = "Add Workout",
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && goalValue.toIntOrNull()?.let { it > 0 } == true,
                        variant = ButtonVariant.PRIMARY
                    )
                }
            }
        }
    }
}