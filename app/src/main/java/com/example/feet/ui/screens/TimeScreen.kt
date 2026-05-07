package com.example.feet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.feet.ui.components.glassTextFieldColors
import com.example.feet.ui.components.LocalGlassAccentColors
import com.example.feet.ui.components.TranslucentBox
import com.example.feet.ui.components.GlassDialogBox
import com.example.feet.ui.components.LiquidGlassButton
import com.example.feet.ui.components.ButtonVariant
import kotlinx.coroutines.delay

import androidx.compose.runtime.collectAsState
import com.example.feet.ui.viewmodels.SharedViewModel
import com.example.feet.data.database.TimerEntity
import com.example.feet.ui.components.bouncyClickable
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

private enum class TimeMode {
    TIMER,
    STOPWATCH
}

@Composable
fun TimeScreen(viewModel: SharedViewModel) {
    val stopwatchMillis by viewModel.stopwatchAccumulated.collectAsState()
    val stopwatchRunning by viewModel.stopwatchRunning.collectAsState()
    val lapEntities by viewModel.laps.collectAsState()
    val laps = lapEntities.map { it.lapMillis }
    
    val timers by viewModel.timers.collectAsState()
    var mode by remember { mutableStateOf(TimeMode.TIMER) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        TimeGlassPanel(
            mode = mode,
            onModeChange = { mode = it },
            stopwatchElapsed = formatStopwatchElapsed(stopwatchMillis),
            laps = laps,
            isStopwatchRunning = stopwatchRunning,
            onStopwatchPrimaryClick = { viewModel.toggleStopwatch() },
            onStopwatchResetClick = { viewModel.resetStopwatch() },
            onLapClick = { viewModel.addLap() },
            timers = timers,
            onTimerAdd = { viewModel.addTimer(it) },
            onTimerPlayPause = { viewModel.toggleTimer(it) },
            onTimerReset = { viewModel.resetTimer(it) },
            onTimerDurationChange = { id, newDuration -> viewModel.updateTimerDuration(id, newDuration) },
            onTimerDelete = { viewModel.deleteTimer(it) }
        )
    }
}

@Composable
private fun TimeGlassPanel(
    mode: TimeMode,
    onModeChange: (TimeMode) -> Unit,
    stopwatchElapsed: String,
    laps: List<Long>,
    isStopwatchRunning: Boolean,
    onStopwatchPrimaryClick: () -> Unit,
    onStopwatchResetClick: () -> Unit,
    onLapClick: () -> Unit,
    timers: List<TimerEntity>,
    onTimerAdd: (Long) -> Unit,
    onTimerPlayPause: (Long) -> Unit,
    onTimerReset: (Long) -> Unit,
    onTimerDurationChange: (Long, Long) -> Unit,
    onTimerDelete: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimeModeSwitch(
            mode = mode,
            onModeChange = onModeChange
        )

        when (mode) {
            TimeMode.TIMER -> TimerContent(
                timers = timers,
                onAdd = onTimerAdd,
                onPlayPause = onTimerPlayPause,
                onReset = onTimerReset,
                onDurationChange = onTimerDurationChange,
                onDelete = onTimerDelete,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            TimeMode.STOPWATCH -> StopwatchContent(
                elapsed = stopwatchElapsed,
                laps = laps,
                isRunning = isStopwatchRunning,
                onPrimaryClick = onStopwatchPrimaryClick,
                onResetClick = onStopwatchResetClick,
                onLapClick = onLapClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TimeModeSwitch(
    mode: TimeMode,
    onModeChange: (TimeMode) -> Unit
) {
    val accent = LocalGlassAccentColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // FIX: Use TimeMode.entries instead of deprecated TimeMode.values()
        TimeMode.entries.forEach { item ->
            val selected = item == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) accent.tint.copy(alpha = 0.22f) else Color.Transparent)
                    .border(
                        width = if (selected) 1.dp else 0.dp,
                        color = if (selected) accent.border.copy(alpha = 0.42f) else Color.Transparent,
                        shape = RoundedCornerShape(50)
                    )
                    .bouncyClickable { onModeChange(item) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = if (item == TimeMode.TIMER) "TIMER" else "STOPWATCH",
                    color = Color.White.copy(alpha = if (selected) 0.94f else 0.48f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun TimerContent(
    timers: List<TimerEntity>,
    onAdd: (Long) -> Unit,
    onPlayPause: (Long) -> Unit,
    onReset: (Long) -> Unit,
    onDurationChange: (Long, Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddTimer by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .fadingEdges(24.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                timers.forEachIndexed { index, timer ->
                    TimerRow(
                        index = index,
                        timer = timer,
                        onPlayPause = { onPlayPause(timer.id) },
                        onReset = { onReset(timer.id) },
                        onDelete = { onDelete(timer.id) },
                        onDurationChange = { duration -> onDurationChange(timer.id, duration) }
                    )
                }
            }
        }

        TimerAddButton(
            onClick = { showAddTimer = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 88.dp)
        )
    }

    if (showAddTimer) {
        TimerEditDialog(
            title = "Add Timer",
            initialValue = "00:05:00",
            onDismiss = { showAddTimer = false },
            onSave = { value ->
                parseTimerInput(value)?.let(onAdd)
                showAddTimer = false
            }
        )
    }
}

@Composable
private fun TimerAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = LocalGlassAccentColors.current
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, accent.border.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .bouncyClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "+",
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TimerRow(
    index: Int,
    timer: TimerEntity,
    onPlayPause: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit,
    onDurationChange: (Long) -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }

    // FIX: TranslucentBox and Column are properly closed before the Dialog
    TranslucentBox(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Text(
                    text = "TIMER %02d".format(index + 1),
                    color = Color.White.copy(alpha = 0.42f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                androidx.compose.material3.Text(
                    text = "Delete",
                    color = Color.White.copy(alpha = 0.44f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .bouncyClickable(onClick = onDelete)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            androidx.compose.material3.Text(
                text = formatCountdownElapsed(timer.remainingMillis),
                color = Color.White.copy(alpha = 0.96f),
                fontSize = 48.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .bouncyClickable { showEditor = true }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )

            androidx.compose.material3.Text(
                text = "set ${formatCountdownElapsed(timer.durationMillis)}",
                color = Color.White.copy(alpha = 0.46f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallTimeControlButton(onClick = onReset) {
                    ResetGlyph(LocalGlassAccentColors.current.border.copy(alpha = 0.92f))
                }
                SmallTimeControlButton(onClick = onPlayPause) {
                    if (timer.isRunning) {
                        PauseGlyph(Color.White.copy(alpha = 0.92f))
                    } else {
                        PlayGlyph(Color.White.copy(alpha = 1f))
                    }
                }
            }
        } // closes Column
    } // closes TranslucentBox

    // FIX: Dialog is now outside TranslucentBox, at the correct composable scope
    if (showEditor) {
        TimerEditDialog(
            title = "Edit Timer",
            initialValue = formatCountdownElapsed(timer.durationMillis),
            onDismiss = { showEditor = false },
            onSave = { value ->
                parseTimerInput(value)?.let(onDurationChange)
                showEditor = false
            }
        )
    }
}

@Composable
private fun StopwatchContent(
    elapsed: String,
    laps: List<Long>,
    isRunning: Boolean,
    onPrimaryClick: () -> Unit,
    onResetClick: () -> Unit,
    onLapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(58.dp))

        MetricReadout(
            label = null,
            value = elapsed,
            valueSize = 58.sp,
            unit = null
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeControlButton(onClick = onPrimaryClick, isPrimary = true) {
                if (isRunning) StopGlyph(Color.White) else PlayGlyph(Color.White)
            }

            TimeControlButton(onClick = onLapClick) {
                LapGlyph(LocalGlassAccentColors.current.border.copy(alpha = 0.92f))
            }
            
            TimeControlButton(onClick = onResetClick) {
                ResetGlyph(Color.White.copy(alpha = 0.9f))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LapList(
            laps = laps,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun TimerEditDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }

    Dialog(onDismissRequest = onDismiss) {
        GlassDialogBox(
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.Text(
                text = title,
                color = Color.White.copy(alpha = 0.94f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = value,
                onValueChange = { value = it.filter { char -> char.isDigit() || char == ':' } },
                label = { androidx.compose.material3.Text("HH:MM:SS") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = glassTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassButton(
                    onClick = onDismiss,
                    text = "Cancel",
                    modifier = Modifier.weight(1f),
                    variant = ButtonVariant.SECONDARY
                )
                LiquidGlassButton(
                    onClick = { onSave(value) },
                    text = "Save",
                    modifier = Modifier.weight(1f),
                    variant = ButtonVariant.PRIMARY
                )
            }
        }
    }
}

@Composable
private fun MetricReadout(
    label: String?,
    value: String,
    valueSize: androidx.compose.ui.unit.TextUnit,
    unit: String?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (label != null) {
            androidx.compose.material3.Text(
                text = label,
                color = Color.White.copy(alpha = 0.56f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }
        androidx.compose.material3.Text(
            text = value,
            color = Color.White.copy(alpha = 0.98f),
            fontSize = valueSize,
            lineHeight = valueSize,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        if (unit != null) {
            androidx.compose.material3.Text(
                text = unit,
                color = Color.White.copy(alpha = 0.46f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
private fun LapList(
    laps: List<Long>,
    modifier: Modifier = Modifier
) {
    if (laps.isEmpty()) {
        Box(
            modifier = modifier.padding(top = 18.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            androidx.compose.material3.Text(
                text = "LAPS",
                color = Color.White.copy(alpha = 0.32f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }
        return
    }

    Column(
        modifier = modifier
            .padding(top = 18.dp)
            .fadingEdges(24.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        laps.asReversed().forEachIndexed { index, lapTime ->
            val lapNumber = laps.size - index
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Text(
                    text = "LAP %02d".format(lapNumber),
                    color = Color.White.copy(alpha = 0.46f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
                androidx.compose.material3.Text(
                    text = formatStopwatchElapsed(lapTime),
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SmallTimeControlButton(
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val accent = LocalGlassAccentColors.current
    val background = if (isPrimary) {
        Brush.verticalGradient(
            colors = listOf(
                accent.tint.copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.08f),
                accent.tint.copy(alpha = 0.05f)
            )
        )
    }

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(background)
            .background(Color(0xFF0A0A0F).copy(alpha = if (isPrimary) 0.22f else 0.46f))
            .border(
                width = 1.dp,
                color = accent.border.copy(alpha = if (!enabled) 0.10f else if (isPrimary) 0.42f else 0.20f),
                shape = CircleShape
            )
            .bouncyClickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun TimeControlButton(
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val accent = LocalGlassAccentColors.current
    val background = if (isPrimary) {
        Brush.verticalGradient(
            colors = listOf(
                accent.tint.copy(alpha = 0.34f),
                Color.White.copy(alpha = 0.10f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.10f),
                accent.tint.copy(alpha = 0.08f)
            )
        )
    }

    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(background)
            .background(Color(0xFF0A0A0F).copy(alpha = if (isPrimary) 0.28f else 0.54f))
            .border(
                width = 1.dp,
                color = accent.border.copy(alpha = if (!enabled) 0.12f else if (isPrimary) 0.52f else 0.26f),
                shape = CircleShape
            )
            .bouncyClickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun StopGlyph(color: Color) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(color)
    )
}

@Composable
private fun PauseGlyph(color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(width = 7.dp, height = 24.dp)
                .background(color)
        )
        Box(
            modifier = Modifier
                .size(width = 7.dp, height = 24.dp)
                .background(color)
        )
    }
}

@Composable
private fun ResetGlyph(color: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val stroke = Stroke(width = 2.4.dp.toPx())
        val arrow = Path().apply {
            moveTo(size.width * 0.30f, size.height * 0.30f)
            lineTo(size.width * 0.30f, size.height * 0.10f)
            lineTo(size.width * 0.10f, size.height * 0.10f)
        }
        drawPath(arrow, color, style = stroke)
        drawArc(
            color = color,
            startAngle = 218f,
            sweepAngle = 285f,
            useCenter = false,
            topLeft = Offset(size.width * 0.14f, size.height * 0.14f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.72f, size.height * 0.72f),
            style = stroke
        )
    }
}

@Composable
private fun PlayGlyph(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.30f, size.height * 0.18f)
            lineTo(size.width * 0.78f, size.height * 0.50f)
            lineTo(size.width * 0.30f, size.height * 0.82f)
            close()
        }
        drawPath(path, color)
    }
}

@Composable
private fun LapGlyph(color: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val stroke = Stroke(width = 2.3.dp.toPx())
        drawCircle(
            color = color,
            radius = size.minDimension * 0.36f,
            center = center,
            style = stroke
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.50f, size.height * 0.22f),
            end = Offset(size.width * 0.50f, size.height * 0.50f),
            strokeWidth = 2.3.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.50f, size.height * 0.50f),
            end = Offset(size.width * 0.68f, size.height * 0.60f),
            strokeWidth = 2.3.dp.toPx()
        )
        drawCircle(
            color = color,
            radius = 2.2.dp.toPx(),
            center = Offset(size.width * 0.76f, size.height * 0.24f)
        )
    }
}

private fun formatStopwatchElapsed(milliseconds: Long): String {
    val hours = milliseconds / 3_600_000L
    val minutes = (milliseconds % 3_600_000L) / 60_000L
    val seconds = (milliseconds % 60_000L) / 1_000L
    val millis = milliseconds % 1_000L
    return "%02d:%02d:%02d.%03d".format(hours, minutes, seconds, millis)
}

private fun formatCountdownElapsed(milliseconds: Long): String {
    val totalSeconds = (milliseconds + 999L) / 1_000L
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

private fun parseTimerInput(input: String): Long? {
    val parts = input.split(":").mapNotNull { it.toLongOrNull() }
    if (parts.isEmpty()) return null
    val seconds = when (parts.size) {
        1 -> parts[0]
        2 -> parts[0] * 60L + parts[1]
        else -> parts.takeLast(3).let { it[0] * 3600L + it[1] * 60L + it[2] }
    }
    return (seconds * 1_000L).takeIf { it > 0L }
}