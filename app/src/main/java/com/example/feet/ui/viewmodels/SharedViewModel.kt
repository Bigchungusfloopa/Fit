package com.example.feet.ui.viewmodels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import com.example.feet.widgets.StepsWidgetSynced
import com.example.feet.widgets.WaterWidgetSynced
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.feet.data.database.*
import com.example.feet.data.repository.FitnessRepository
import com.example.feet.services.MediaNotificationListener
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize database and repository
    private val database = FitnessDatabase.getDatabase(application)
    private val repository = FitnessRepository(database)

    // Media tracking - DEFINED BEFORE init BLOCK
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

    // Initialize preferences on startup
    init {
        viewModelScope.launch {
            repository.initializePreferences()
            loadTodayData()
        }

        // Register media update receiver
        LocalBroadcastManager.getInstance(application).registerReceiver(
            mediaUpdateReceiver,
            IntentFilter(MediaNotificationListener.ACTION_MEDIA_UPDATE)
        )
    }

    private fun getTodayDate(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    }

    private suspend fun loadTodayData() {
        val today = getTodayDate()

        // Load water data
        val waterRecord = repository.getWaterByDate(today)
        if (waterRecord != null) {
            _todayWater.value = waterRecord.totalMl
            glassSizeMl = waterRecord.glassSize
        }

        // Load step data
        val stepRecord = repository.getStepsByDate(today)
        if (stepRecord != null) {
            _todaySteps.value = stepRecord.steps
            _dailyStepGoal.value = stepRecord.goal
        }

        // Load preferences
        val prefs = repository.getPreferencesOnce()
        _dailyGoalMl.value = prefs.dailyWaterGoalMl
        _dailyStepGoal.value = prefs.dailyStepGoal
        glassSizeMl = prefs.glassSize
    }

    // Water tracking
    data class WaterData(val date: String, val totalMl: Int)

    private val _dailyGoalMl = MutableStateFlow(4000)
    val dailyGoalMl: StateFlow<Int> = _dailyGoalMl

    var glassSizeMl = 250f
        private set

    private val _todayWater = MutableStateFlow(0)
    val todayWater: StateFlow<Int> = _todayWater

    val waterHistory: StateFlow<List<WaterData>> = repository.getAllWaterRecords()
        .map { records -> records.map { WaterData(it.date, it.totalMl) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGlass() {
        _todayWater.value += glassSizeMl.toInt()
        saveTodayWater()
    }

    fun removeGlass() {
        val currentMl = _todayWater.value
        val glassSize = glassSizeMl.toInt()
        if (currentMl >= glassSize) {
            _todayWater.value -= glassSize
        } else if (currentMl > 0) {
            _todayWater.value = 0
        }
        saveTodayWater()
    }

    private fun saveTodayWater() {
        viewModelScope.launch {
            repository.insertOrUpdateWater(getTodayDate(), _todayWater.value, glassSizeMl)

            // Notify widget to update
            WaterWidgetSynced.notifyDataChanged(getApplication())
        }
    }

    fun setGlassSize(sizeMl: Float) {
        glassSizeMl = sizeMl
        viewModelScope.launch {
            repository.updateGlassSize(sizeMl)
        }
        saveTodayWater()
    }

    fun setDailyGoal(goalLiters: Float) {
        if (goalLiters > 0) {
            val goalMl = (goalLiters * 1000).toInt()
            _dailyGoalMl.value = goalMl
            viewModelScope.launch {
                repository.updateDailyWaterGoal(goalMl)
            }
        }
    }

    fun getGlassSize(): Float = glassSizeMl

    fun getGlassesConsumed(): Int {
        return if (glassSizeMl > 0) {
            (_todayWater.value / glassSizeMl).toInt().coerceAtLeast(0)
        } else 0
    }

    fun getGlassesGoal(): Int {
        return if (glassSizeMl > 0) {
            (_dailyGoalMl.value / glassSizeMl).toInt()
        } else 16
    }

    fun getWaterProgress(): Float {
        if (_dailyGoalMl.value == 0) return 0f
        return (_todayWater.value.toFloat() / _dailyGoalMl.value).coerceIn(0f, 1f)
    }

    fun getRemainingGlasses(): Int {
        return (getGlassesGoal() - getGlassesConsumed()).coerceAtLeast(0)
    }

    fun getRemainingWaterMl(): Int {
        return (_dailyGoalMl.value - _todayWater.value).coerceAtLeast(0)
    }

    // Steps tracking
    data class StepData(val date: String, val steps: Int, val goal: Int)

    private val _dailyStepGoal = MutableStateFlow(10000)
    val dailyStepGoal: StateFlow<Int> = _dailyStepGoal

    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps

    val stepHistory: StateFlow<List<StepData>> = repository.getAllStepRecords()
        .map { records -> records.map { StepData(it.date, it.steps, it.goal) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
            viewModelScope.launch {
                repository.updateDailyStepGoal(goal)
            }
            saveTodaySteps()
        }
    }

    fun simulateStepUpdate() {
        val randomSteps = if (!_isLiveTracking.value) (50..200).random() else (10..50).random()
        _todaySteps.value += randomSteps
        saveTodaySteps()
    }

    private fun saveTodaySteps() {
        viewModelScope.launch {
            repository.insertOrUpdateSteps(getTodayDate(), _todaySteps.value, _dailyStepGoal.value)

            // Notify widget to update
            StepsWidgetSynced.notifyDataChanged(getApplication())
        }
    }

    fun getStepsProgress(): Float {
        val goal = _dailyStepGoal.value
        return if (goal > 0) {
            (_todaySteps.value.toFloat() / goal).coerceAtMost(1.0f)
        } else 0f
    }

    fun getWeekHistory(): List<StepData> {
        return stepHistory.value.takeLast(7).reversed()
    }

    fun getMonthHistory(): List<StepData> {
        return stepHistory.value.takeLast(30).reversed()
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

    fun isStepTrackingAvailable(): Boolean = true

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

    enum class GoalType { REPS, KM }

    val todayWorkouts: StateFlow<List<Workout>> = repository.getWorkoutsByDate(getTodayDate())
        .map { entities ->
            entities.map { entity ->
                Workout(
                    id = entity.id,
                    name = entity.name,
                    duration = entity.duration,
                    goalValue = entity.goalValue,
                    goalType = if (entity.goalType == "REPS") GoalType.REPS else GoalType.KM,
                    completed = entity.completed
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomWorkout(name: String, duration: Int?, goalValue: Int, goalType: GoalType) {
        viewModelScope.launch {
            repository.insertWorkout(
                date = getTodayDate(),
                name = name,
                duration = duration,
                goalValue = goalValue,
                goalType = goalType.name,
                completed = false
            )
        }
    }

    fun deleteWorkout(workoutId: Long) {
        viewModelScope.launch {
            repository.deleteWorkout(workoutId)
        }
    }

    fun toggleWorkout(id: Long) {
        viewModelScope.launch {
            val workouts = todayWorkouts.value
            val workout = workouts.find { it.id == id } ?: return@launch

            val entity = WorkoutEntity(
                id = workout.id,
                date = getTodayDate(),
                name = workout.name,
                duration = workout.duration,
                goalValue = workout.goalValue,
                goalType = workout.goalType.name,
                completed = !workout.completed
            )
            repository.updateWorkout(entity)
        }
    }

    fun getCompletedWorkoutsCount(): Int {
        return todayWorkouts.value.count { it.completed }
    }

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
        val context = getApplication<Application>()
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(mediaUpdateReceiver)
    }
}