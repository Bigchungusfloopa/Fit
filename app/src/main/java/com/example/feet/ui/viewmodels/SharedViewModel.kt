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
import kotlinx.coroutines.delay
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

        // Carry over workouts from the previous available day if today has none
        val todayWorkoutsList = repository.getWorkoutsByDate(today).first()
        if (todayWorkoutsList.isEmpty()) {
            val allWorkouts = repository.getAllWorkouts().first()
            if (allWorkouts.isNotEmpty()) {
                val mostRecentDate = allWorkouts.maxByOrNull { it.date }?.date
                if (mostRecentDate != null && mostRecentDate != today) {
                    val recentWorkouts = allWorkouts.filter { it.date == mostRecentDate }
                    recentWorkouts.forEach { workout ->
                        repository.insertWorkout(
                            date = today,
                            name = workout.name,
                            duration = workout.duration,
                            goalValue = workout.goalValue,
                            goalType = workout.goalType,
                            completed = false
                        )
                    }
                }
            }
        }

        // Load water data
        val waterRecord = repository.getWaterByDate(today)
        if (waterRecord != null) {
            _todayWater.value = waterRecord.totalMl
            _glassSizeMl.value = waterRecord.glassSize
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
        _glassSizeMl.value = prefs.glassSize
        _weightKg.value = prefs.weightKg
        _heightCm.value = prefs.heightCm
        _targetWeightKg.value = prefs.targetWeightKg

        // Load Timers with background calculation
        val savedTimers = repository.getAllTimers().first()
        val now = System.currentTimeMillis()
        _timers.value = savedTimers.map { timer ->
            if (timer.isRunning) {
                val elapsedSinceSave = now - timer.lastUpdated
                val newRemaining = (timer.remainingMillis - elapsedSinceSave).coerceAtLeast(0L)
                timer.copy(remainingMillis = newRemaining, isRunning = newRemaining > 0L, lastUpdated = now)
            } else {
                timer.copy(lastUpdated = now)
            }
        }

        // Load Stopwatch with background calculation
        val savedStopwatch = repository.getStopwatchOnce()
        if (savedStopwatch != null) {
            _stopwatchRunning.value = savedStopwatch.isRunning
            if (savedStopwatch.isRunning) {
                val elapsedSinceSave = now - savedStopwatch.lastUpdated
                _stopwatchAccumulated.value = savedStopwatch.accumulatedMillis + elapsedSinceSave
            } else {
                _stopwatchAccumulated.value = savedStopwatch.accumulatedMillis
            }
        }

        // Load Laps
        _laps.value = repository.getAllLaps().first()
        
        startTicking()
    }

    private fun startTicking() {
        viewModelScope.launch {
            var lastTick = System.currentTimeMillis()
            while (true) {
                delay(16L)
                val now = System.currentTimeMillis()
                val delta = now - lastTick
                lastTick = now

                // Update Timers
                _timers.value = _timers.value.map { timer ->
                    if (timer.isRunning) {
                        val remaining = (timer.remainingMillis - delta).coerceAtLeast(0L)
                        timer.copy(remainingMillis = remaining, isRunning = remaining > 0L, lastUpdated = now)
                    } else {
                        timer
                    }
                }

                // Update Stopwatch
                if (_stopwatchRunning.value) {
                    _stopwatchAccumulated.value += delta
                }
            }
        }
    }

    // Water tracking
    data class WaterData(val date: String, val totalMl: Int)

    private val _dailyGoalMl = MutableStateFlow(4000)
    val dailyGoalMl: StateFlow<Int> = _dailyGoalMl

    private val _glassSizeMl = MutableStateFlow(250f)
    val glassSizeMl: StateFlow<Float> = _glassSizeMl

    // Body metrics (BMI)
    private val _weightKg = MutableStateFlow(0f)
    val weightKg: StateFlow<Float> = _weightKg

    private val _heightCm = MutableStateFlow(0f)
    val heightCm: StateFlow<Float> = _heightCm

    private val _targetWeightKg = MutableStateFlow(0f)
    val targetWeightKg: StateFlow<Float> = _targetWeightKg

    fun getBmi(): Float {
        val h = _heightCm.value / 100f
        return if (h > 0f) _weightKg.value / (h * h) else 0f
    }

    fun getBmiCategory(): String = when {
        getBmi() <= 0f    -> ""
        getBmi() < 18.5f  -> "Underweight"
        getBmi() < 25f    -> "Normal"
        getBmi() < 30f    -> "Overweight"
        else              -> "Obese"
    }

    fun saveBodyMetrics(weightKg: Float, heightCm: Float, targetWeightKg: Float) {
        _weightKg.value = weightKg
        _heightCm.value = heightCm
        _targetWeightKg.value = targetWeightKg
        viewModelScope.launch {
            repository.updateBodyMetrics(weightKg, heightCm, targetWeightKg)
        }
    }

    private val _todayWater = MutableStateFlow(0)
    val todayWater: StateFlow<Int> = _todayWater

    val waterHistory: StateFlow<List<WaterData>> = repository.getAllWaterRecords()
        .map { records -> records.map { WaterData(it.date, it.totalMl) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGlass() {
        _todayWater.value += _glassSizeMl.value.toInt()
        saveTodayWater()
    }

    fun removeGlass() {
        val currentMl = _todayWater.value
        val glassSize = _glassSizeMl.value.toInt()
        if (currentMl >= glassSize) {
            _todayWater.value -= glassSize
        } else if (currentMl > 0) {
            _todayWater.value = 0
        }
        saveTodayWater()
    }

    private fun saveTodayWater() {
        viewModelScope.launch {
            repository.insertOrUpdateWater(getTodayDate(), _todayWater.value, _glassSizeMl.value)

            // Notify widget to update
            WaterWidgetSynced.notifyDataChanged(getApplication())
        }
    }

    fun setGlassSize(sizeMl: Float) {
        _glassSizeMl.value = sizeMl
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

    fun getGlassSize(): Float = _glassSizeMl.value

    fun getGlassesConsumed(): Int {
        return if (_glassSizeMl.value > 0) {
            (_todayWater.value / _glassSizeMl.value).toInt().coerceAtLeast(0)
        } else 0
    }

    fun getGlassesGoal(): Int {
        return if (_glassSizeMl.value > 0) {
            (_dailyGoalMl.value / _glassSizeMl.value).toInt()
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

    enum class GoalType { REPS, DURATION }

    val todayWorkouts: StateFlow<List<Workout>> = repository.getWorkoutsByDate(getTodayDate())
        .map { entities ->
            entities.map { entity ->
                Workout(
                    id = entity.id,
                    name = entity.name,
                    duration = entity.duration,
                    goalValue = entity.goalValue,
                    goalType = if (entity.goalType == "REPS") GoalType.REPS else GoalType.DURATION,
                    completed = entity.completed
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class WorkoutDaySummary(val date: String, val completionPercentage: Int)

    val workoutHistory: StateFlow<List<WorkoutDaySummary>> = repository.getAllWorkouts()
        .map { workouts ->
            workouts.groupBy { it.date }
                .map { (date, dailyWorkouts) ->
                    val completedCount = dailyWorkouts.count { it.completed }
                    val totalCount = dailyWorkouts.size
                    val percentage = if (totalCount > 0) (completedCount * 100) / totalCount else 0
                    WorkoutDaySummary(date, percentage)
                }
                .sortedBy { it.date }
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

    fun editWorkout(id: Long, newName: String, newGoalValue: Int, newGoalType: GoalType) {
        viewModelScope.launch {
            val workouts = todayWorkouts.value
            val workout = workouts.find { it.id == id } ?: return@launch

            val entity = WorkoutEntity(
                id = workout.id,
                date = getTodayDate(),
                name = newName,
                duration = workout.duration,
                goalValue = newGoalValue,
                goalType = newGoalType.name,
                completed = workout.completed
            )
            repository.updateWorkout(entity)
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

    // --- Timer & Stopwatch Logic ---
    private val _timers = MutableStateFlow<List<TimerEntity>>(emptyList())
    val timers: StateFlow<List<TimerEntity>> = _timers

    private val _stopwatchAccumulated = MutableStateFlow(0L)
    val stopwatchAccumulated: StateFlow<Long> = _stopwatchAccumulated

    private val _stopwatchRunning = MutableStateFlow(false)
    val stopwatchRunning: StateFlow<Boolean> = _stopwatchRunning

    private val _laps = MutableStateFlow<List<LapRecordEntity>>(emptyList())
    val laps: StateFlow<List<LapRecordEntity>> = _laps

    fun addTimer(durationMillis: Long) {
        viewModelScope.launch {
            val newTimer = TimerEntity(
                durationMillis = durationMillis,
                remainingMillis = durationMillis,
                isRunning = false
            )
            repository.insertTimer(newTimer)
            // Reload from DB to get the ID
            _timers.value = repository.getAllTimers().first()
        }
    }

    fun deleteTimer(timerId: Long) {
        viewModelScope.launch {
            repository.deleteTimer(timerId)
            _timers.value = _timers.value.filter { it.id != timerId }
        }
    }

    fun toggleTimer(timerId: Long) {
        val timer = _timers.value.find { it.id == timerId } ?: return
        val newState = !timer.isRunning
        _timers.value = _timers.value.map {
            if (it.id == timerId) it.copy(isRunning = newState, lastUpdated = System.currentTimeMillis()) else it
        }
        saveTimerToDb(timerId)
    }

    fun resetTimer(timerId: Long) {
        _timers.value = _timers.value.map {
            if (it.id == timerId) it.copy(
                remainingMillis = it.durationMillis,
                isRunning = false,
                lastUpdated = System.currentTimeMillis()
            ) else it
        }
        saveTimerToDb(timerId)
    }

    fun updateTimerDuration(timerId: Long, newDuration: Long) {
        _timers.value = _timers.value.map {
            if (it.id == timerId) it.copy(
                durationMillis = newDuration,
                remainingMillis = newDuration,
                isRunning = false,
                lastUpdated = System.currentTimeMillis()
            ) else it
        }
        saveTimerToDb(timerId)
    }

    private fun saveTimerToDb(timerId: Long) {
        viewModelScope.launch {
            val timer = _timers.value.find { it.id == timerId } ?: return@launch
            repository.updateTimer(timer)
        }
    }

    fun toggleStopwatch() {
        val newState = !_stopwatchRunning.value
        _stopwatchRunning.value = newState
        saveStopwatchToDb()
    }

    fun resetStopwatch() {
        _stopwatchRunning.value = false
        _stopwatchAccumulated.value = 0L
        viewModelScope.launch {
            repository.clearLaps()
            _laps.value = emptyList()
            saveStopwatchToDb()
        }
    }

    fun addLap() {
        if (_stopwatchAccumulated.value > 0) {
            viewModelScope.launch {
                repository.addLap(_stopwatchAccumulated.value)
                _laps.value = repository.getAllLaps().first()
            }
        }
    }

    private fun saveStopwatchToDb() {
        viewModelScope.launch {
            repository.updateStopwatch(
                StopwatchEntity(
                    accumulatedMillis = _stopwatchAccumulated.value,
                    isRunning = _stopwatchRunning.value,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(mediaUpdateReceiver)
    }
}