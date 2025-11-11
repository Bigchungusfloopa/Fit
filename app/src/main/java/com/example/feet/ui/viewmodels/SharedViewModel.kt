package com.example.feet.ui.viewmodels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.feet.services.MediaNotificationListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// --- IMPORTANT: Change from ViewModel to AndroidViewModel ---
class SharedViewModel(application: Application) : AndroidViewModel(application) {

    // Data class to hold historical water data
    data class WaterData(
        val date: String, // "YYYY-MM-DD" format
        val totalMl: Int
    )

    private val _dailyGoalMl = MutableStateFlow(4000) // 4 liters
    val dailyGoalMl: StateFlow<Int> = _dailyGoalMl

    var glassSizeMl by mutableStateOf(250f)

    private val _todayWater = MutableStateFlow(0)
    val todayWater: StateFlow<Int> = _todayWater

    private val _waterHistory = MutableStateFlow<List<WaterData>>(emptyList())
    val waterHistory: StateFlow<List<WaterData>> = _waterHistory

    // --- NEW MEDIA TRACKING ---
    private val _currentTrack = MutableStateFlow<String?>(null)
    val currentTrack: StateFlow<String?> = _currentTrack

    private val _currentArtist = MutableStateFlow<String?>(null)
    val currentArtist: StateFlow<String?> = _currentArtist

    private val mediaUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MediaNotificationListener.ACTION_MEDIA_UPDATE) {
                _currentTrack.value = intent.getStringExtra(MediaNotificationListener.EXTRA_TRACK)
                _currentArtist.value = intent.getStringExtra(MediaNotificationListener.EXTRA_ARTIST)
            }
        }
    }

    init {
        // Register the receiver
        LocalBroadcastManager.getInstance(application).registerReceiver(
            mediaUpdateReceiver,
            IntentFilter(MediaNotificationListener.ACTION_MEDIA_UPDATE)
        )
    }

    override fun onCleared() {
        super.onCleared()
        // Unregister the receiver
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(mediaUpdateReceiver)
    }

    // --- NEW PERMISSION FUNCTIONS ---
    fun isNotificationListenerEnabled(): Boolean {
        val context = getApplication<Application>()
        val enabledListeners = android.provider.Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        val componentName = ComponentName(context, MediaNotificationListener::class.java).flattenToString()
        return enabledListeners?.contains(componentName) == true
    }

    fun requestNotificationPermission() {
        // This intent takes the user to the setting screen
        val context = getApplication<Application>()
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    // --- END NEW FUNCTIONS ---

    // ... (rest of your water, steps, and workout functions are unchanged) ...

    // Add one glass
    fun addGlass() {
        _todayWater.value += glassSizeMl.toInt()
        saveTodayWater()
    }

    // Remove one glass
    fun removeGlass() {
        val currentMl = _todayWater.value
        val glassSize = glassSizeMl.toInt()
        if (currentMl >= glassSize) {
            _todayWater.value -= glassSize
            saveTodayWater()
        }
        else if (currentMl > 0) {
            _todayWater.value = 0
            saveTodayWater()
        }
    }

    // Save today's water
    private fun saveTodayWater() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val existingIndex = _waterHistory.value.indexOfFirst { it.date == today }

        val newWaterData = WaterData(
            date = today,
            totalMl = _todayWater.value
        )

        if (existingIndex >= 0) {
            val updatedHistory = _waterHistory.value.toMutableList()
            updatedHistory[existingIndex] = newWaterData
            _waterHistory.value = updatedHistory
        } else {
            _waterHistory.value = _waterHistory.value + newWaterData
        }
    }

    // Set custom glass size
    fun setGlassSize(sizeMl: Float) {
        glassSizeMl = sizeMl
    }

    // Set daily goal from Liters
    fun setDailyGoal(goalLiters: Float) {
        if (goalLiters > 0) {
            _dailyGoalMl.value = (goalLiters * 1000).toInt()
        }
    }


    // Get current glass size in ml
    fun getGlassSize(): Float = glassSizeMl

    // Get number of glasses consumed
    fun getGlassesConsumed(): Int {
        val glasses = if (glassSizeMl > 0) {
            (_todayWater.value / glassSizeMl).toInt()
        } else {
            0
        }
        return glasses.coerceAtLeast(0)
    }

    // Get number of glasses needed to reach goal
    fun getGlassesGoal(): Int {
        return if (glassSizeMl > 0) {
            (_dailyGoalMl.value / glassSizeMl).toInt()
        } else {
            16
        }
    }

    // Get water progress
    fun getWaterProgress(): Float {
        if (_dailyGoalMl.value == 0) return 0f
        val progress = (_todayWater.value.toFloat() / _dailyGoalMl.value)
        return progress.coerceIn(0f, 1f)
    }

    // Get remaining glasses
    fun getRemainingGlasses(): Int {
        val remaining = getGlassesGoal() - getGlassesConsumed()
        return remaining.coerceAtLeast(0)
    }

    // Get remaining water in ml
    fun getRemainingWaterMl(): Int {
        val remaining = _dailyGoalMl.value - _todayWater.value
        return remaining.coerceAtLeast(0)
    }

    // Steps
    data class StepData(
        val date: String,
        val steps: Int,
        val goal: Int
    )

    private val _dailyStepGoal = MutableStateFlow(10000)
    val dailyStepGoal: StateFlow<Int> = _dailyStepGoal

    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps

    private val _stepHistory = MutableStateFlow<List<StepData>>(emptyList())
    val stepHistory: StateFlow<List<StepData>> = _stepHistory

    private val _isLiveTracking = MutableStateFlow(false)
    val isLiveTracking: StateFlow<Boolean> = _isLiveTracking

    private val _hasStepSensor = MutableStateFlow(false)
    val hasStepSensor: StateFlow<Boolean> = _hasStepSensor

    fun setStepSensorAvailable(available: Boolean) {
        _hasStepSensor.value = available
    }

    fun updateLiveSteps(steps: Int) {
        _isLiveTracking.value = true
        _todaySteps.value = steps
        saveTodaySteps()
    }

    fun setDailyStepGoal(goal: Int) {
        if (goal > 0) {
            _dailyStepGoal.value = goal
            saveTodaySteps()
        }
    }

    fun simulateStepUpdate() {
        if (!_isLiveTracking.value) {
            val randomSteps = (50..200).random()
            _todaySteps.value += randomSteps
            saveTodaySteps()
        } else {
            val randomSteps = (10..50).random()
            _todaySteps.value += randomSteps
            saveTodaySteps()
        }
    }

    private fun saveTodaySteps() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val existingIndex = _stepHistory.value.indexOfFirst { it.date == today }

        val newStepData = StepData(
            date = today,
            steps = _todaySteps.value,
            goal = _dailyStepGoal.value
        )

        if (existingIndex >= 0) {
            val updatedHistory = _stepHistory.value.toMutableList()
            updatedHistory[existingIndex] = newStepData
            _stepHistory.value = updatedHistory
        } else {
            _stepHistory.value = _stepHistory.value + newStepData
        }
    }

    fun getStepsProgress(): Float {
        val goal = _dailyStepGoal.value
        return if (goal > 0) {
            (_todaySteps.value.toFloat() / goal).coerceAtMost(1.0f)
        } else {
            0f
        }
    }

    fun getWeekHistory(): List<StepData> {
        return _stepHistory.value.takeLast(7).reversed()
    }

    fun getMonthHistory(): List<StepData> {
        return _stepHistory.value.takeLast(30).reversed()
    }

    fun calculateCalories(): Int {
        return (_todaySteps.value * 0.04).toInt()
    }

    fun calculateDistance(): Float {
        return _todaySteps.value * 0.000762f
    }

    fun resetTodaySteps() {
        _todaySteps.value = 0
        _isLiveTracking.value = false
        saveTodaySteps()
    }

    fun isStepTrackingAvailable(): Boolean {
        return true
    }

    fun getTrackingStatus(): String {
        return when {
            _isLiveTracking.value -> "Live Tracking"
            _hasStepSensor.value -> "Sensor Ready"
            else -> "Simulation Mode"
        }
    }

    // Workouts
    data class Workout(
        val id: Long,
        val name: String,
        val duration: Int? = null,
        val goalValue: Int,
        val goalType: GoalType,
        val completed: Boolean = false
    )

    enum class GoalType {
        REPS, KM
    }

    private val _todayWorkouts = MutableStateFlow<List<Workout>>(emptyList())
    val todayWorkouts: StateFlow<List<Workout>> = _todayWorkouts

    fun addCustomWorkout(name: String, duration: Int?, goalValue: Int, goalType: GoalType) {
        val newId = (_todayWorkouts.value.maxOfOrNull { it.id } ?: 0) + 1
        val newWorkout = Workout(
            id = newId,
            name = name,
            duration = duration,
            goalValue = goalValue,
            goalType = goalType
        )
        _todayWorkouts.value = _todayWorkouts.value + newWorkout
    }

    fun deleteWorkout(workoutId: Long) {
        _todayWorkouts.value = _todayWorkouts.value.filter { it.id != workoutId }
    }

    fun toggleWorkout(id: Long) {
        _todayWorkouts.value = _todayWorkouts.value.map { workout ->
            if (workout.id == id) workout.copy(completed = !workout.completed)
            else workout
        }
    }

    fun getCompletedWorkoutsCount(): Int {
        return _todayWorkouts.value.count { it.completed }
    }
}