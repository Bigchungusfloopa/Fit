package com.example.feet.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_records ORDER BY date DESC")
    fun getAllWaterRecords(): Flow<List<WaterRecord>>

    @Query("SELECT * FROM water_records WHERE date = :date")
    suspend fun getWaterByDate(date: String): WaterRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWater(record: WaterRecord)

    @Update
    suspend fun updateWater(record: WaterRecord)

    @Delete
    suspend fun deleteWater(record: WaterRecord)

    @Query("DELETE FROM water_records WHERE date < :cutoffDate")
    suspend fun deleteOldRecords(cutoffDate: String)
}

@Dao
interface StepDao {
    @Query("SELECT * FROM step_records ORDER BY date DESC")
    fun getAllStepRecords(): Flow<List<StepRecord>>

    @Query("SELECT * FROM step_records WHERE date = :date")
    suspend fun getStepsByDate(date: String): StepRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(record: StepRecord)

    @Update
    suspend fun updateSteps(record: StepRecord)

    @Delete
    suspend fun deleteSteps(record: StepRecord)

    @Query("DELETE FROM step_records WHERE date < :cutoffDate")
    suspend fun deleteOldRecords(cutoffDate: String)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_records WHERE date = :date ORDER BY timestamp DESC")
    fun getWorkoutsByDate(date: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout_records ORDER BY date DESC, timestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM workout_records WHERE id = :workoutId")
    suspend fun deleteWorkoutById(workoutId: Long)

    @Query("DELETE FROM workout_records WHERE date < :cutoffDate")
    suspend fun deleteOldRecords(cutoffDate: String)
}

@Dao
interface PreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getPreferences(): Flow<UserPreferences?>

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getPreferencesOnce(): UserPreferences?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: UserPreferences)

    @Update
    suspend fun updatePreferences(preferences: UserPreferences)
}