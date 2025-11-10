package com.example.feet.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SharedViewModel : ViewModel() {

    // Water settings
    private val dailyGoalMl = 4000 // 4 liters
    var glassSizeMl by mutableStateOf(250f) // Default glass size, can be customized

    private val _todayWater = MutableStateFlow(0)
    val todayWater: StateFlow<Int> = _todayWater

    // Add one glass with current glass size
    fun addGlass() {
        _todayWater.value += glassSizeMl.toInt()
    }

    // Remove one glass with current glass size - PREVENT NEGATIVE
    fun removeGlass() {
        val currentMl = _todayWater.value
        val glassSize = glassSizeMl.toInt()
        if (currentMl >= glassSize) {
            _todayWater.value -= glassSize
        }
        // If current water is less than glass size but greater than 0, set to 0
        else if (currentMl > 0) {
            _todayWater.value = 0
        }
    }

    // Set custom glass size
    fun setGlassSize(sizeMl: Float) {
        glassSizeMl = sizeMl
    }

    // Get current glass size in ml
    fun getGlassSize(): Float = glassSizeMl

    // Get number of glasses consumed - PREVENT NEGATIVE
    fun getGlassesConsumed(): Int {
        val glasses = if (glassSizeMl > 0) {
            (_todayWater.value / glassSizeMl).toInt()
        } else {
            0
        }
        return glasses.coerceAtLeast(0) // Ensure non-negative
    }

    // Get number of glasses needed to reach goal
    fun getGlassesGoal(): Int {
        return if (glassSizeMl > 0) {
            (dailyGoalMl / glassSizeMl).toInt()
        } else {
            16 // Default fallback
        }
    }

    // Get water progress (0.0 to 1.0) - PREVENT NEGATIVE
    fun getWaterProgress(): Float {
        val progress = (_todayWater.value.toFloat() / dailyGoalMl)
        return progress.coerceIn(0f, 1f) // Ensure between 0 and 1
    }

    // Get remaining glasses to goal - PREVENT NEGATIVE
    fun getRemainingGlasses(): Int {
        val remaining = getGlassesGoal() - getGlassesConsumed()
        return remaining.coerceAtLeast(0)
    }

    // Get remaining water in ml - PREVENT NEGATIVE
    fun getRemainingWaterMl(): Int {
        val remaining = dailyGoalMl - _todayWater.value
        return remaining.coerceAtLeast(0)
    }

    // Steps - Simulated tracking for now
    data class StepData(
        val date: String, // "2024-01-15" format
        val steps: Int,
        val goal: Int
    )

    private val _dailyStepGoal = MutableStateFlow(10000) // Default goal
    val dailyStepGoal: StateFlow<Int> = _dailyStepGoal

    private val _todaySteps = MutableStateFlow(0) // Simulated step count
    val todaySteps: StateFlow<Int> = _todaySteps

    private val _stepHistory = MutableStateFlow<List<StepData>>(emptyList())
    val stepHistory: StateFlow<List<StepData>> = _stepHistory

    // Set daily step goal
    fun setDailyStepGoal(goal: Int) {
        if (goal > 0) {
            _dailyStepGoal.value = goal
            saveTodaySteps() // Update history with new goal
        }
    }

    // Simulate step updates (in real app, this would come from sensor)
    fun simulateStepUpdate() {
        // Add random steps to simulate walking
        val randomSteps = (50..200).random()
        _todaySteps.value += randomSteps
        saveTodaySteps()
    }

    // Save today's steps to history
    private fun saveTodaySteps() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val existingIndex = _stepHistory.value.indexOfFirst { it.date == today }

        val newStepData = StepData(
            date = today,
            steps = _todaySteps.value,
            goal = _dailyStepGoal.value
        )

        if (existingIndex >= 0) {
            // Update existing entry
            val updatedHistory = _stepHistory.value.toMutableList()
            updatedHistory[existingIndex] = newStepData
            _stepHistory.value = updatedHistory
        } else {
            // Add new entry
            _stepHistory.value = _stepHistory.value + newStepData
        }
    }

    // Get steps progress (0.0 to 1.0)
    fun getStepsProgress(): Float {
        val goal = _dailyStepGoal.value
        return if (goal > 0) {
            (_todaySteps.value.toFloat() / goal).coerceAtMost(1.0f)
        } else {
            0f
        }
    }

    // Get week history (last 7 days)
    fun getWeekHistory(): List<StepData> {
        return _stepHistory.value.takeLast(7).reversed()
    }

    // Calculate calories from steps
    fun calculateCalories(): Int {
        return (_todaySteps.value * 0.04).toInt()
    }

    // Calculate distance from steps
    fun calculateDistance(): Float {
        return _todaySteps.value * 0.000762f
    }

    // Reset today's steps (for testing/midnight reset)
    fun resetTodaySteps() {
        _todaySteps.value = 0
        saveTodaySteps()
    }

    // Check if step tracking is available (simulated for now)
    fun isStepTrackingAvailable(): Boolean {
        return true // Simulated as available
    }

    // Workouts
    data class Workout(
        val id: Long,
        val name: String,
        val duration: Int? = null, // in minutes (optional)
        val goalValue: Int, // either reps or distance
        val goalType: GoalType, // REPS or KM
        val completed: Boolean = false
    )

    enum class GoalType {
        REPS, KM
    }

    private val _todayWorkouts = MutableStateFlow<List<Workout>>(emptyList())
    val todayWorkouts: StateFlow<List<Workout>> = _todayWorkouts

    // Add custom workout
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

    // Delete workout
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