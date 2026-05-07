package com.example.feet.ui.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.feet.data.database.FitnessDatabase
import com.example.feet.data.repository.FitnessRepository
import com.example.feet.data.database.RunRecordEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RunningViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val database = FitnessDatabase.getDatabase(application)
    private val repository = FitnessRepository(database)

    // ── State ──────────────────────────────────────────────────────────────────
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _speedKmh = MutableStateFlow(0f)
    val speedKmh: StateFlow<Float> = _speedKmh.asStateFlow()

    private val _distanceKm = MutableStateFlow(0f)
    val distanceKm: StateFlow<Float> = _distanceKm.asStateFlow()

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints.asStateFlow()

    private val _elevationGain = MutableStateFlow(0f)
    val elevationGain: StateFlow<Float> = _elevationGain.asStateFlow()

    private val _currentPace = MutableStateFlow(0f)
    val currentPace: StateFlow<Float> = _currentPace.asStateFlow()

    val pastRuns: StateFlow<List<RunRecordEntity>> = repository.getRunsSince(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var lastLocation: Location? = null
    private var timerJob: Job? = null

    // ── High-accuracy location request (1-second GPS intervals) ───────────────
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 1_000L
    ).setMinUpdateDistanceMeters(1f).build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return

            // Speed: m/s → km/h
            val currentSpeedKmh = if (loc.hasSpeed()) loc.speed * 3.6f else _speedKmh.value
            _speedKmh.value = currentSpeedKmh

            // Pace: min/km
            _currentPace.value = if (currentSpeedKmh > 0) 60f / currentSpeedKmh else 0f

            // Distance: accumulate from last known point
            lastLocation?.let { prev ->
                val delta = prev.distanceTo(loc) / 1000f // metres → km
                if (delta > 0.002f) {               // ignore GPS jitter < 2m
                    _distanceKm.value += delta
                }

                // Incline / Elevation Gain
                if (loc.hasAltitude() && prev.hasAltitude()) {
                    val altDelta = loc.altitude - prev.altitude
                    if (altDelta > 0) { // Only track positive elevation gain
                        _elevationGain.value += altDelta.toFloat()
                    }
                }
            }
            lastLocation = loc

            // Append to polyline
            _routePoints.value = _routePoints.value + LatLng(loc.latitude, loc.longitude)
        }
    }

    // ── Public API ─────────────────────────────────────────────────────────────
    @SuppressLint("MissingPermission")
    fun startRun() {
        if (_isRunning.value) return
        _isRunning.value = true

        // Reset session
        _elapsedSeconds.value = 0L
        _speedKmh.value = 0f
        _currentPace.value = 0f
        _distanceKm.value = 0f
        _elevationGain.value = 0f
        _routePoints.value = emptyList()
        lastLocation = null

        // Start timer
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _elapsedSeconds.value++
            }
        }

        // Start GPS updates
        fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopRun() {
        if (!_isRunning.value) return
        _isRunning.value = false
        timerJob?.cancel()
        fusedClient.removeLocationUpdates(locationCallback)

        // Save run if there's distance
        if (_distanceKm.value > 0.01f) {
            val routeString = _routePoints.value.joinToString(";") { "${it.latitude},${it.longitude}" }
            val avgPace = if (_distanceKm.value > 0) (_elapsedSeconds.value / 60f) / _distanceKm.value else 0f
            val runRecord = RunRecordEntity(
                date = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                distanceKm = _distanceKm.value,
                durationSeconds = _elapsedSeconds.value,
                avgPace = avgPace,
                elevationGain = _elevationGain.value,
                routePointsString = routeString
            )
            viewModelScope.launch {
                repository.insertRun(runRecord)
            }
        }

        _speedKmh.value = 0f
        _currentPace.value = 0f
    }

    override fun onCleared() {
        super.onCleared()
        fusedClient.removeLocationUpdates(locationCallback)
        timerJob?.cancel()
    }

    // ── Formatting helpers ─────────────────────────────────────────────────────
    fun formatElapsed(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    fun formatPace(pace: Float): String {
        if (pace <= 0f || pace > 60f) return "--:--"
        val minutes = pace.toInt()
        val seconds = ((pace - minutes) * 60).toInt()
        return "%d:%02d".format(minutes, seconds)
    }
}
