package com.example.feet.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feet.ui.components.LiquidGlassButton
import com.example.feet.ui.components.ButtonVariant
import com.example.feet.ui.components.bouncyClickable
import com.example.feet.ui.viewmodels.RunningViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.example.feet.data.database.RunRecordEntity
import kotlin.math.max

// Dark map style JSON
private const val darkMapStyle = """
[
  {"elementType":"geometry","stylers":[{"color":"#0a0a0f"}]},
  {"elementType":"labels.text.fill","stylers":[{"color":"#746855"}]},
  {"elementType":"labels.text.stroke","stylers":[{"color":"#0a0a0f"}]},
  {"featureType":"road","elementType":"geometry","stylers":[{"color":"#1a1a2e"}]},
  {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#1a1a2e"}]},
  {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#1e1e3f"}]},
  {"featureType":"road.highway","elementType":"labels.icon","stylers":[{"visibility":"off"}]},
  {"featureType":"transit","elementType":"geometry","stylers":[{"color":"#0a0a0f"}]},
  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#0d1117"}]},
  {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#515c6d"}]},
  {"featureType":"poi","stylers":[{"visibility":"off"}]},
  {"featureType":"administrative.locality","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]}
]
"""

fun Modifier.fadingEdges(
    length: androidx.compose.ui.unit.Dp = 24.dp
) = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        val lengthPx = length.toPx()
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                (lengthPx / size.height).coerceAtMost(0.5f) to Color.Black,
                (1f - (lengthPx / size.height)).coerceAtLeast(0.5f) to Color.Black,
                1f to Color.Transparent
            ),
            blendMode = BlendMode.DstIn
        )
    }

@Composable
fun RunningSection(
    runningViewModel: RunningViewModel = viewModel()
) {
    val context = LocalContext.current
    val isRunning by runningViewModel.isRunning.collectAsState()
    val speedKmh by runningViewModel.speedKmh.collectAsState()
    val distanceKm by runningViewModel.distanceKm.collectAsState()
    val elapsedSeconds by runningViewModel.elapsedSeconds.collectAsState()
    val routePoints by runningViewModel.routePoints.collectAsState()
    val currentPace by runningViewModel.currentPace.collectAsState()
    val elevationGain by runningViewModel.elevationGain.collectAsState()
    val pastRuns by runningViewModel.pastRuns.collectAsState()
    var selectedRun by remember { mutableStateOf<RunRecordEntity?>(null) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) runningViewModel.startRun()
    }

    fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // Camera — auto-follow while running
    val cameraPositionState = rememberCameraPositionState()

    // Snap camera to current location on first GPS fix
    LaunchedEffect(routePoints) {
        val last = routePoints.lastOrNull() ?: return@LaunchedEffect
        cameraPositionState.animate(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(last)
                    .zoom(17f)
                    .tilt(if (isRunning) 40f else 0f)
                    .build()
            ),
            durationMs = if (isRunning) 800 else 300
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Section header ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Running",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold
            )
            if (isRunning) {
                PulsingDot(color = Color(0xFF00E676), modifier = Modifier.size(10.dp))
            }
        }

        // ── Stats glass card ─────────────────────────────────────────────────
        val cardShape = RoundedCornerShape(20.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.06f))
                    )
                )
                .background(Color(0xFF0A0A0F).copy(alpha = 0.55f))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.08f)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    ),
                    shape = cardShape
                )
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.06f), Color.Transparent),
                            endY = size.height * 0.35f
                        )
                    )
                }
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RunStat(
                        value = runningViewModel.formatPace(currentPace),
                        label = "pace",
                        accent = Color(0xFF7B61FF)
                    )
                    RunStatDivider()
                    RunStat(
                        value = "%.2f".format(distanceKm),
                        label = "km",
                        accent = Color(0xFF00BCD4)
                    )
                    RunStatDivider()
                    RunStat(
                        value = runningViewModel.formatElapsed(elapsedSeconds),
                        label = "time",
                        accent = Color(0xFF00E676)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RunStat(
                        value = "%.1f".format(speedKmh),
                        label = "km/h",
                        accent = Color(0xFFE91E63)
                    )
                    RunStatDivider()
                    RunStat(
                        value = "%.1f".format(elevationGain),
                        label = "m gain",
                        accent = Color(0xFFFFC107)
                    )
                }
            }
        }

        // ── Google Map ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.06f)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission(),
                    mapStyleOptions = MapStyleOptions(darkMapStyle)
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = false
                )
            ) {
                // Glow polyline (thicker, more transparent)
                if (routePoints.size >= 2) {
                    Polyline(
                        points = routePoints,
                        color = Color(0x557B61FF),
                        width = 22f,
                        jointType = JointType.ROUND
                    )
                    // Main route polyline
                    Polyline(
                        points = routePoints,
                        color = Color(0xFF7B61FF),
                        width = 10f,
                        jointType = JointType.ROUND
                    )
                }
            }

            // ── +/− zoom buttons (top-right corner) ───────────────────────────
            val coroutineScope = rememberCoroutineScope()
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ZoomButton("+") {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 200)
                    }
                }
                ZoomButton("-") {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 200)
                    }
                }
            }

            if (!isRunning && routePoints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(
                            color = Color(0xFF080810).copy(alpha = 0.72f),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "🏃 Tap Start Run to trace your path",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // ── Start / Stop button ──────────────────────────────────────────────
        LiquidGlassButton(
            onClick = {
                if (isRunning) {
                    runningViewModel.stopRun()
                } else {
                    if (hasLocationPermission()) {
                        runningViewModel.startRun()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
            },
            text = if (isRunning) "Stop Run" else "Start Run",
            modifier = Modifier.fillMaxWidth(),
            variant = if (isRunning) ButtonVariant.SECONDARY else ButtonVariant.PRIMARY
        )

        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Recent Runs",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        if (pastRuns.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pastRuns.forEach { run ->
                    PastRunItem(
                        run = run,
                        viewModel = runningViewModel,
                        onClick = { selectedRun = run }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recent runs yet.\nStart a run to see it here!",
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    selectedRun?.let { run ->
        RunDetailDialog(
            run = run,
            viewModel = runningViewModel,
            onDismiss = { selectedRun = null }
        )
    }
}

// ── Sub-composables ────────────────────────────────────────────────────────────

@Composable
private fun RunStat(value: String, label: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.95f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = accent.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun RunStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(Color.White.copy(alpha = 0.12f))
    )
}

@Composable
private fun ZoomButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = Color(0xFF0A0A0F).copy(alpha = 0.78f),
                shape = androidx.compose.foundation.shape.CircleShape
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.30f),
                shape = androidx.compose.foundation.shape.CircleShape
            )
            .bouncyClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.90f),
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
        )
    }
}

private fun parseRoutePoints(routePointsString: String): List<LatLng> {
    if (routePointsString.isEmpty()) return emptyList()
    return routePointsString.split(";").mapNotNull {
        val parts = it.split(",")
        if (parts.size == 2) {
            val lat = parts[0].toDoubleOrNull()
            val lng = parts[1].toDoubleOrNull()
            if (lat != null && lng != null) LatLng(lat, lng) else null
        } else null
    }
}

@Composable
private fun PastRunItem(
    run: RunRecordEntity,
    viewModel: RunningViewModel,
    onClick: () -> Unit
) {
    val routePoints = remember(run.routePointsString) { parseRoutePoints(run.routePointsString) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0A0A0F).copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .bouncyClickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = run.date,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    text = "${"%.2f".format(run.distanceKm)} km",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PastRunMiniStat("Time", viewModel.formatElapsed(run.durationSeconds))
                PastRunMiniStat("Pace", viewModel.formatPace(run.avgPace))
                PastRunMiniStat("Gain", "${"%.1f".format(run.elevationGain)} m")
            }

            if (routePoints.size >= 2) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    val cameraPositionState = rememberCameraPositionState()
                    LaunchedEffect(routePoints) {
                        val bounds = LatLngBounds.builder()
                        routePoints.forEach { bounds.include(it) }
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(bounds.build().center, 14f)
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(mapStyleOptions = MapStyleOptions(darkMapStyle)),
                        googleMapOptionsFactory = { com.google.android.gms.maps.GoogleMapOptions().liteMode(true) },
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            scrollGesturesEnabled = false,
                            zoomGesturesEnabled = false
                        )
                    ) {
                        Polyline(
                            points = routePoints,
                            color = Color(0xFF7B61FF),
                            width = 10f,
                            jointType = JointType.ROUND
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RunDetailDialog(
    run: RunRecordEntity,
    viewModel: RunningViewModel,
    onDismiss: () -> Unit
) {
    val routePoints = remember(run.routePointsString) { parseRoutePoints(run.routePointsString) }
    val speedSamples = remember(run.id, routePoints) {
        buildSpeedSamples(routePoints, run.distanceKm, run.durationSeconds)
    }
    val paceSamples = remember(speedSamples) {
        speedSamples.map { speed -> if (speed > 0f) 60f / speed else 0f }
    }
    val avgSpeed = if (run.durationSeconds > 0) run.distanceKm / (run.durationSeconds / 3600f) else 0f
    val caloriesEstimate = (run.distanceKm * 62f).toInt()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.14f),
                            Color.White.copy(alpha = 0.07f)
                        )
                    )
                )
                .background(Color(0xFF080810).copy(alpha = 0.94f))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.08f)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
                    .fadingEdges(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Run Details",
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = run.date,
                            color = Color.White.copy(alpha = 0.50f),
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "Close",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .bouncyClickable(onClick = onDismiss)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                if (routePoints.size >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    ) {
                        val cameraPositionState = rememberCameraPositionState()
                        LaunchedEffect(routePoints) {
                            val bounds = LatLngBounds.builder()
                            routePoints.forEach { bounds.include(it) }
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(bounds.build().center, 14f)
                        }

                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(mapStyleOptions = MapStyleOptions(darkMapStyle)),
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = false,
                                scrollGesturesEnabled = true,
                                zoomGesturesEnabled = true
                            )
                        ) {
                            Polyline(
                                points = routePoints,
                                color = Color(0xFF7B61FF),
                                width = 10f,
                                jointType = JointType.ROUND
                            )
                        }

                        // Zoom buttons
                        val coroutineScope = rememberCoroutineScope()
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ZoomButton("+") {
                                coroutineScope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 200)
                                }
                            }
                            ZoomButton("-") {
                                coroutineScope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 200)
                                }
                            }
                        }
                    }
                }

                RunMetricGrid(
                    metrics = listOf(
                        "Distance" to "${"%.2f".format(run.distanceKm)} km",
                        "Time" to viewModel.formatElapsed(run.durationSeconds),
                        "Avg pace" to viewModel.formatPace(run.avgPace),
                        "Avg speed" to "${"%.1f".format(avgSpeed)} km/h",
                        "Gain" to "${"%.1f".format(run.elevationGain)} m",
                        "Calories" to "$caloriesEstimate kcal"
                    )
                )

                RunLineChart(
                    title = "Speed",
                    unit = "km/h",
                    samples = speedSamples,
                    color = Color(0xFFE91E63),
                    valueFormatter = { "%.1f".format(it) }
                )

                RunLineChart(
                    title = "Pace",
                    unit = "min/km",
                    samples = paceSamples,
                    color = Color(0xFF7B61FF),
                    valueFormatter = { viewModel.formatPace(it) }
                )
            }
        }
    }
}

@Composable
private fun RunMetricGrid(metrics: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowMetrics.forEach { (label, value) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.07f))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = value,
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = label,
                                color = Color.White.copy(alpha = 0.46f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                if (rowMetrics.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RunLineChart(
    title: String,
    unit: String,
    samples: List<Float>,
    color: Color,
    valueFormatter: (Float) -> String
) {
    val visibleSamples = if (samples.size >= 2) samples else listOf(0f, 0f)
    val maxValue = max(visibleSamples.maxOrNull() ?: 0f, 1f)
    val avgValue = visibleSamples.filter { it > 0f }.average().takeIf { !it.isNaN() }?.toFloat() ?: 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${valueFormatter(avgValue)} $unit",
                color = color.copy(alpha = 0.88f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val chartPadding = 8.dp.toPx()
            val chartWidth = size.width - chartPadding * 2
            val chartHeight = size.height - chartPadding * 2
            val stepX = chartWidth / (visibleSamples.lastIndex.coerceAtLeast(1))
            val path = Path()

            visibleSamples.forEachIndexed { index, value ->
                val x = chartPadding + stepX * index
                val normalized = (value / maxValue).coerceIn(0f, 1f)
                val y = chartPadding + chartHeight - normalized * chartHeight
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            repeat(4) { line ->
                val y = chartPadding + chartHeight * line / 3f
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(chartPadding, y),
                    end = Offset(size.width - chartPadding, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 3.dp.toPx())
            )
            visibleSamples.forEachIndexed { index, value ->
                val x = chartPadding + stepX * index
                val normalized = (value / maxValue).coerceIn(0f, 1f)
                val y = chartPadding + chartHeight - normalized * chartHeight
                drawCircle(color = color.copy(alpha = 0.9f), radius = 3.dp.toPx(), center = Offset(x, y))
            }
        }
    }
}

private fun buildSpeedSamples(
    routePoints: List<LatLng>,
    distanceKm: Float,
    durationSeconds: Long
): List<Float> {
    if (durationSeconds <= 0L) return emptyList()
    if (routePoints.size < 2) {
        val avgSpeed = if (durationSeconds > 0) distanceKm / (durationSeconds / 3600f) else 0f
        return listOf(avgSpeed, avgSpeed)
    }

    val segmentSeconds = durationSeconds.toFloat() / (routePoints.size - 1)
    return routePoints.zipWithNext().map { (start, end) ->
        val segmentDistanceKm = distanceBetweenKm(start, end)
        segmentDistanceKm / (segmentSeconds / 3600f)
    }
}

private fun distanceBetweenKm(start: LatLng, end: LatLng): Float {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(
        start.latitude,
        start.longitude,
        end.latitude,
        end.longitude,
        results
    )
    return results[0] / 1000f
}

@Composable
private fun PastRunMiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text(text = label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
    }
}
