package com.example.feet.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.feet.ui.components.ButtonSize
import com.example.feet.ui.components.ButtonVariant
import com.example.feet.ui.components.GlassDialogBox
import com.example.feet.ui.components.LiquidGlassButton
import com.example.feet.ui.components.TranslucentBox
import com.example.feet.ui.components.glassTextFieldColors
import com.example.feet.ui.viewmodels.SharedViewModel
@Composable
fun EnhancedWorkoutScreen(viewModel: SharedViewModel) {
    val workouts by viewModel.todayWorkouts.collectAsState()
    val completedWorkouts = viewModel.getCompletedWorkoutsCount()
    val workoutHistory by viewModel.workoutHistory.collectAsState()

    // --- NEW MEDIA STATE ---
    val currentTrack by viewModel.currentTrack.collectAsState()
    val currentArtist by viewModel.currentArtist.collectAsState()
    var hasNotificationPermission by remember { mutableStateOf(viewModel.isNotificationListenerEnabled()) }
    // --- END NEW STATE ---

    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var showAllTimeHistoryDialog by remember { mutableStateOf(false) }
    var editingWorkoutId by remember { mutableStateOf<Long?>(null) }
    var workoutName by remember { mutableStateOf("") }
    var workoutGoalValue by remember { mutableStateOf("") }
    var selectedGoalType by remember { mutableStateOf(SharedViewModel.GoalType.REPS) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NowPlayingCard(
                artist = currentArtist,
                track = currentTrack,
                hasPermission = hasNotificationPermission,
                onRequestPermission = {
                    viewModel.requestNotificationPermission()
                },
                modifier = Modifier.fillMaxWidth()
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
                    workoutGoalValue = ""
                    selectedGoalType = SharedViewModel.GoalType.REPS
                    editingWorkoutId = null
                    showAddWorkoutDialog = true
                },
                text = "Add Custom Workout",
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.PRIMARY
            )

            WorkoutHistorySection(
                history = workoutHistory,
                onViewAllClick = { showAllTimeHistoryDialog = true }
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

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fadingEdges(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 124.dp)
            ) {
                items(workouts) { workout ->
                    WorkoutItem(
                        workout = workout,
                        onToggleComplete = {
                            viewModel.toggleWorkout(workout.id)
                        },
                        onDelete = {
                            viewModel.deleteWorkout(workout.id)
                        },
                        onEdit = {
                            workoutName = workout.name
                            workoutGoalValue = workout.goalValue.toString()
                            selectedGoalType = workout.goalType
                            editingWorkoutId = workout.id
                            showAddWorkoutDialog = true
                        }
                    )
                }
            }
        }

        // Add Custom Workout Dialog
        if (showAddWorkoutDialog) {
            AddWorkoutDialog(
                isEdit = editingWorkoutId != null,
                name = workoutName,
                onNameChange = { workoutName = it },
                goalValue = workoutGoalValue,
                onGoalValueChange = { workoutGoalValue = it },
                goalType = selectedGoalType,
                onGoalTypeChange = { selectedGoalType = it },
                onConfirm = {
                    val goalValue = workoutGoalValue.toIntOrNull() ?: 0

                    if (workoutName.isNotBlank() && goalValue > 0) {
                        if (editingWorkoutId != null) {
                            viewModel.editWorkout(editingWorkoutId!!, workoutName, goalValue, selectedGoalType)
                        } else {
                            viewModel.addCustomWorkout(workoutName, null, goalValue, selectedGoalType)
                        }
                        showAddWorkoutDialog = false
                        editingWorkoutId = null
                    }
                },
                onDismiss = { 
                    showAddWorkoutDialog = false
                    editingWorkoutId = null
                }
            )
        }

        // All Time History Dialog
        if (showAllTimeHistoryDialog) {
            AllTimeHistoryDialog(
                history = workoutHistory,
                onDismiss = { showAllTimeHistoryDialog = false }
            )
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
    onDelete: () -> Unit,
    onEdit: () -> Unit
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

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Workout details (Goal required)
            val goalText = when (workout.goalType) {
                SharedViewModel.GoalType.REPS -> "${workout.goalValue} reps"
                SharedViewModel.GoalType.DURATION -> "${workout.goalValue} min"
            }

            Text(
                text = goalText,
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
    isEdit: Boolean = false,
    name: String,
    onNameChange: (String) -> Unit,
    goalValue: String,
    onGoalValueChange: (String) -> Unit,
    goalType: SharedViewModel.GoalType,
    onGoalTypeChange: (SharedViewModel.GoalType) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassDialogBox {
            Text(
                text = if (isEdit) "Edit Workout" else "Add Custom Workout",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Workout Name") },
                placeholder = { Text("e.g., Running, Push-ups") },
                colors = glassTextFieldColors(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Goal Type",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassButton(
                    onClick = { onGoalTypeChange(SharedViewModel.GoalType.REPS) },
                    text = "Repetitions",
                    modifier = Modifier.weight(1f),
                    variant = if (goalType == SharedViewModel.GoalType.REPS) ButtonVariant.PRIMARY else ButtonVariant.SECONDARY
                )
                LiquidGlassButton(
                    onClick = { onGoalTypeChange(SharedViewModel.GoalType.DURATION) },
                    text = "Duration (min)",
                    modifier = Modifier.weight(1f),
                    variant = if (goalType == SharedViewModel.GoalType.DURATION) ButtonVariant.PRIMARY else ButtonVariant.SECONDARY
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = goalValue,
                onValueChange = onGoalValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        when (goalType) {
                            SharedViewModel.GoalType.REPS -> "Repetitions"
                            SharedViewModel.GoalType.DURATION -> "Duration (min)"
                        }
                    )
                },
                placeholder = {
                    Text(
                        when (goalType) {
                            SharedViewModel.GoalType.REPS -> "e.g., 50"
                            SharedViewModel.GoalType.DURATION -> "e.g., 30"
                        }
                    )
                },
                colors = glassTextFieldColors(),
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                    text = if (isEdit) "Save" else "Add",
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank() && goalValue.toIntOrNull()?.let { it > 0 } == true,
                    variant = ButtonVariant.PRIMARY
                )
            }
        }
    }
}

@Composable
fun WorkoutHistorySection(
    history: List<SharedViewModel.WorkoutDaySummary>,
    onViewAllClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Recent Progress",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (history.isEmpty()) {
            TranslucentBox(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No history yet.",
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                contentPadding = PaddingValues(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(history.takeLast(3).reversed()) { item ->
                    WorkoutHistoryItem(item = item)
                }

                item {
                    // History Button
                    TranslucentBox(
                        modifier = Modifier
                            .height(100.dp)
                            .padding(start = 4.dp)
                            .clickable { onViewAllClick() }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "History",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutHistoryItem(item: SharedViewModel.WorkoutDaySummary) {
    TranslucentBox(
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${item.completionPercentage}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = "completed",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.date.substring(5),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AllTimeHistoryDialog(
    history: List<SharedViewModel.WorkoutDaySummary>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassDialogBox {
            Text(
                text = "All-Time History",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (history.isEmpty()) {
                Text(
                    text = "No history available.",
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history.reversed()) { item ->
                        TranslucentBox(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.date,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "${item.completionPercentage}% completed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            LiquidGlassButton(
                onClick = onDismiss,
                text = "Close",
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.SECONDARY
            )
        }
    }
}
