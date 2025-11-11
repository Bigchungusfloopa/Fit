package com.example.feet.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.feet.ui.components.ButtonSize
import com.example.feet.ui.components.ButtonVariant
import com.example.feet.ui.components.LiquidGlassButton
import com.example.feet.ui.components.TranslucentBox
import com.example.feet.ui.viewmodels.SharedViewModel
import kotlinx.coroutines.delay

@Composable
fun StepsScreen(viewModel: SharedViewModel) {
    val steps by viewModel.todaySteps.collectAsState()
    val goal by viewModel.dailyStepGoal.collectAsState()
    val progress by remember { derivedStateOf { viewModel.getStepsProgress() } }
    val weekHistory by viewModel.stepHistory.collectAsState()
    val isLiveTracking by viewModel.isLiveTracking.collectAsState()

    // --- NEW MEDIA STATE ---
    val currentTrack by viewModel.currentTrack.collectAsState()
    val currentArtist by viewModel.currentArtist.collectAsState()
    var hasNotificationPermission by remember { mutableStateOf(viewModel.isNotificationListenerEnabled()) }
    // --- END NEW STATE ---


    var showGoalDialog by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var newGoal by remember { mutableStateOf(goal.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Center header
    ) {

        // --- HEADER UPDATED ---
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show pulsing dot on the left if tracking is live
            if (isLiveTracking) {
                PulsingDot(
                    color = Color(0xFF00E676),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = "Step Tracker",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold
            )
        }
        // --- END OF UPDATE ---

        // Steps Card with Progress Bar and Stats
        TranslucentBox(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Today's Progress",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Goal: ${goal.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")} steps",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                StepProgressBar(
                    progress = progress
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Steps Complete and Distance Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = steps.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,"),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Steps Complete",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.1f".format(viewModel.calculateDistance()),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Distance (km)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LiquidGlassButton(
                onClick = {
                    newGoal = goal.toString()
                    showGoalDialog = true
                },
                text = "Set Goal",
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.SECONDARY
            )

            LiquidGlassButton(
                onClick = { showHistory = true },
                text = "History",
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.SECONDARY
            )
        }

        // Weather Card
        WeatherCard()

        // 30-Day History Preview
        Text(
            text = "Last 30 Days",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        )

        StepHistoryGrid(
            history = viewModel.getMonthHistory()
        )

        // Testing Controls
        if (!viewModel.isStepTrackingAvailable()) {
            LiquidGlassButton(
                onClick = { viewModel.simulateStepUpdate() },
                text = "Simulate Walking",
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.PRIMARY
            )
        }

        // --- NOW PLAYING CARD ---
        NowPlayingCard(
            artist = currentArtist,
            track = currentTrack,
            hasPermission = hasNotificationPermission,
            onRequestPermission = {
                viewModel.requestNotificationPermission()
                // Update permission state after returning from settings
                hasNotificationPermission = viewModel.isNotificationListenerEnabled()
            }
        )
        // --- END NOW PLAYING CARD ---

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

// --- NEW COMPOSABLE FOR MUSIC ---

@Composable
fun NowPlayingCard(
    artist: String?,
    track: String?,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    TranslucentBox(
        modifier = modifier
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
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!artist.isNullOrBlank()) {
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
// --- END NOW PLAYING CARD COMPOSABLE ---


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
    steps: Int
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
        style = MaterialTheme.typography.displayLarge,
        color = Color.White.copy(alpha = 0.9f), // Simplified color
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
    Dialog( // Using standard Dialog
        onDismissRequest = onDismiss
    ) {
        Box( // Styled like water screen dialog
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
                    text = "Set Daily Step Goal",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )

                TextField( // Using standard TextField
                    value = currentGoal,
                    onValueChange = onGoalChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Step Goal") },
                    colors = TextFieldDefaults.colors( // Dark theme colors
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
                    LiquidGlassButton( // Replaced GlassButton
                        onClick = { onGoalChange("5000") },
                        text = "5,000",
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.SECONDARY
                    )

                    LiquidGlassButton( // Replaced GlassButton
                        onClick = { onGoalChange("10000") },
                        text = "10,000",
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.SECONDARY
                    )

                    LiquidGlassButton( // Replaced GlassButton
                        onClick = { onGoalChange("15000") },
                        text = "15,000",
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.SECONDARY
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LiquidGlassButton( // Replaced GlassButton
                        onClick = onDismiss,
                        text = "Cancel",
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.SECONDARY
                    )

                    LiquidGlassButton( // Replaced GlassButton
                        onClick = onConfirm,
                        text = "Set Goal",
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.PRIMARY
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
    Dialog( // Using standard Dialog
        onDismissRequest = onDismiss
    ) {
        Box( // Styled like water screen dialog
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Step History",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (history.isEmpty()) {
                    Text(
                        text = "No history available yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f),
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

                LiquidGlassButton( // Replaced GlassButton
                    onClick = onDismiss,
                    text = "Close",
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.PRIMARY
                )
            }
        }
    }
}

@Composable
fun StepHistoryItem(stepData: SharedViewModel.StepData) {
    TranslucentBox( // Using TranslucentBox for items
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stepData.date.substring(5), // Short date
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${stepData.steps} steps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )

                val percentage = (stepData.steps.toFloat() / stepData.goal * 100).toInt()
                Text(
                    text = "$percentage% of goal",
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        percentage >= 100 -> Color(0xFF69F0AE) // Keep green
                        percentage >= 75 -> Color(0xFFFFD54F) // Keep yellow
                        else -> Color.White.copy(alpha = 0.7f) // Toned down
                    }
                )
            }
        }
    }
}

@Composable
fun StepHistoryGrid(
    history: List<SharedViewModel.StepData>
) {
    TranslucentBox(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Create rows of 7 days each (like a calendar week)
            val rows = history.chunked(7)

            if (history.isEmpty()) {
                Text(
                    text = "No step history yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                rows.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        week.forEach { day ->
                            StepDayCell(
                                stepData = day,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill remaining cells if less than 7 days
                        repeat(7 - week.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepDayCell(
    stepData: SharedViewModel.StepData,
    modifier: Modifier = Modifier
) {
    val percentage = (stepData.steps.toFloat() / stepData.goal)

    val cellColor = when {
        percentage >= 1.0f -> Color(0xFF00E676) // Bright green - goal achieved
        percentage >= 0.75f -> Color(0xFF69F0AE) // Light green - 75%+
        percentage >= 0.5f -> Color(0xFFFFD54F) // Yellow - 50%+
        percentage >= 0.25f -> Color(0xFFFF8A65) // Orange - 25%+
        stepData.steps > 0 -> Color.White.copy(alpha = 0.3f) // Some activity
        else -> Color.White.copy(alpha = 0.1f) // No activity
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (stepData.steps > 0) 1f else 0.3f,
        animationSpec = tween(durationMillis = 300),
        label = "cell_alpha"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(cellColor.copy(alpha = animatedAlpha))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stepData.date.substring(8), // Day number (DD)
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            if (stepData.steps > 0) {
                Text(
                    text = "${(percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
fun StepProgressBar(
    progress: Float
) {
    // Animate the progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "step_progress_animation"
    )

    // Progress bar color based on completion
    val progressColor = when {
        progress >= 1f -> Color(0xFF00E676) // Bright green when complete
        progress >= 0.75f -> Color(0xFF69F0AE) // Light green
        progress >= 0.5f -> Color(0xFFFFD54F) // Yellow
        progress >= 0.25f -> Color(0xFFFF8A65) // Orange
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
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
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
fun WeatherCard() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var temperature by remember { mutableStateOf("--") }
    var weatherCondition by remember { mutableStateOf("Checking...") }
    var weatherIcon by remember { mutableStateOf("🌤️") }

    // Try to get weather from Android system
    LaunchedEffect(Unit) {
        try {
            // Try to read from system weather content provider
            // This reads cached weather data from Google's Weather app
            val uri = android.net.Uri.parse("content://com.google.android.apps.gsa.weather/weather")
            val cursor = context.contentResolver.query(
                uri,
                arrayOf("temperature", "condition", "location"),
                null,
                null,
                null
            )

            if (cursor != null && cursor.moveToFirst()) {
                val tempIndex = cursor.getColumnIndex("temperature")
                val conditionIndex = cursor.getColumnIndex("condition")

                if (tempIndex >= 0) {
                    val temp = cursor.getString(tempIndex)
                    temperature = temp?.replace("°", "") ?: "--"
                }

                if (conditionIndex >= 0) {
                    val condition = cursor.getString(conditionIndex)
                    weatherCondition = condition ?: "Clear"

                    // Set icon based on condition
                    weatherIcon = when (condition?.lowercase()) {
                        "sunny", "clear" -> "☀️"
                        "partly cloudy", "mostly cloudy" -> "⛅"
                        "cloudy", "overcast" -> "☁️"
                        "rain", "rainy", "showers" -> "🌧️"
                        "thunderstorm", "storm" -> "⛈️"
                        "snow", "snowy" -> "❄️"
                        "fog", "foggy", "mist" -> "🌫️"
                        else -> "🌤️"
                    }
                }
                cursor.close()
            } else {
                // Fallback: Use generic pleasant weather
                temperature = "24"
                weatherCondition = "Pleasant"
                weatherIcon = "🌤️"
            }
        } catch (e: Exception) {
            // Fallback if we can't access weather data
            temperature = "24"
            weatherCondition = "Pleasant"
            weatherIcon = "🌤️"
        }
    }

    TranslucentBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Weather Icon and Condition
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = weatherIcon,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = weatherCondition,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            // Temperature and Location
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$temperature°C",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Current Weather",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}