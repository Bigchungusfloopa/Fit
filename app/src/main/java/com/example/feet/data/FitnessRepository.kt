package com.example.feet.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.*

class FitnessRepository(private val database: AppDatabase) {

    // Water functions
    suspend fun addWater(amountMl: Int) {
        database.waterIntakeDao().insert(WaterIntake(amountMl = amountMl))
    }

    fun getTodayWater(): Flow<Int> {
        val (start, end) = getTodayRange()
        return database.waterIntakeDao().getTotalMlBetween(start, end)
            .map { it ?: 0 }
    }

    // Steps functions
    suspend fun setTodaySteps(steps: Int) {
        val today = LocalDate.now().toString()
        val calories = (steps * 0.04).toInt()
        val distance = steps * 0.000762f
        database.stepDataDao().upsert(StepData(today, steps, calories, distance))
    }

    fun getTodaySteps(): Flow<StepData?> {
        val today = LocalDate.now().toString()
        return database.stepDataDao().getByDate(today)
    }

    // Workout functions
    suspend fun addWorkout(name: String, duration: Int, calories: Int) {
        database.workoutDao().insert(Workout(
            name = name,
            duration = duration,
            calories = calories
        ))
    }

    suspend fun toggleWorkout(id: Long) {
        val workout = database.workoutDao().getById(id)
        workout?.let {
            database.workoutDao().update(it.copy(completed = !it.completed))
        }
    }

    fun getTodayWorkouts(): Flow<List<Workout>> {
        val (start, end) = getTodayRange()
        return database.workoutDao().getWorkoutsBetween(start, end)
    }

    // Helper functions
    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val end = calendar.timeInMillis
        return start to end
    }
}