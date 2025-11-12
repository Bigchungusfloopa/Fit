package com.example.feet.data.repository

import com.example.feet.data.database.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FitnessRepository(private val database: FitnessDatabase) {

    private val waterDao = database.waterDao()
    private val stepDao = database.stepDao()
    private val workoutDao = database.workoutDao()
    private val preferencesDao = database.preferencesDao()

    // Water operations
    fun getAllWaterRecords(): Flow<List<WaterRecord>> = waterDao.getAllWaterRecords()

    suspend fun getWaterByDate(date: String): WaterRecord? = waterDao.getWaterByDate(date)

    suspend fun insertOrUpdateWater(date: String, totalMl: Int, glassSize: Float) {
        waterDao.insertWater(WaterRecord(date, totalMl, glassSize))
    }

    suspend fun deleteOldWaterRecords(daysToKeep: Int = 90) {
        val cutoffDate = LocalDate.now().minusDays(daysToKeep.toLong())
            .format(DateTimeFormatter.ISO_DATE)
        waterDao.deleteOldRecords(cutoffDate)
    }

    // Step operations
    fun getAllStepRecords(): Flow<List<StepRecord>> = stepDao.getAllStepRecords()

    suspend fun getStepsByDate(date: String): StepRecord? = stepDao.getStepsByDate(date)

    suspend fun insertOrUpdateSteps(date: String, steps: Int, goal: Int) {
        stepDao.insertSteps(StepRecord(date, steps, goal))
    }

    suspend fun deleteOldStepRecords(daysToKeep: Int = 90) {
        val cutoffDate = LocalDate.now().minusDays(daysToKeep.toLong())
            .format(DateTimeFormatter.ISO_DATE)
        stepDao.deleteOldRecords(cutoffDate)
    }

    // Workout operations
    fun getWorkoutsByDate(date: String): Flow<List<WorkoutEntity>> =
        workoutDao.getWorkoutsByDate(date)

    fun getAllWorkouts(): Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    suspend fun insertWorkout(
        date: String,
        name: String,
        duration: Int?,
        goalValue: Int,
        goalType: String,
        completed: Boolean
    ): Long {
        return workoutDao.insertWorkout(
            WorkoutEntity(
                date = date,
                name = name,
                duration = duration,
                goalValue = goalValue,
                goalType = goalType,
                completed = completed
            )
        )
    }

    suspend fun updateWorkout(workout: WorkoutEntity) {
        workoutDao.updateWorkout(workout)
    }

    suspend fun deleteWorkout(workoutId: Long) {
        workoutDao.deleteWorkoutById(workoutId)
    }

    suspend fun deleteOldWorkoutRecords(daysToKeep: Int = 90) {
        val cutoffDate = LocalDate.now().minusDays(daysToKeep.toLong())
            .format(DateTimeFormatter.ISO_DATE)
        workoutDao.deleteOldRecords(cutoffDate)
    }

    // Preferences operations
    fun getPreferences(): Flow<UserPreferences?> = preferencesDao.getPreferences()

    suspend fun getPreferencesOnce(): UserPreferences {
        return preferencesDao.getPreferencesOnce() ?: UserPreferences()
    }

    suspend fun updateDailyWaterGoal(goalMl: Int) {
        val current = getPreferencesOnce()
        preferencesDao.insertPreferences(current.copy(dailyWaterGoalMl = goalMl))
    }

    suspend fun updateDailyStepGoal(goal: Int) {
        val current = getPreferencesOnce()
        preferencesDao.insertPreferences(current.copy(dailyStepGoal = goal))
    }

    suspend fun updateGlassSize(sizeMl: Float) {
        val current = getPreferencesOnce()
        preferencesDao.insertPreferences(current.copy(glassSize = sizeMl))
    }

    suspend fun initializePreferences() {
        if (preferencesDao.getPreferencesOnce() == null) {
            preferencesDao.insertPreferences(UserPreferences())
        }
    }
}