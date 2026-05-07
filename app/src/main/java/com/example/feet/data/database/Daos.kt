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

@Dao
interface RunDao {
    @Query("SELECT * FROM run_records ORDER BY timestamp DESC")
    fun getAllRuns(): Flow<List<RunRecordEntity>>

    @Query("SELECT * FROM run_records WHERE timestamp >= :cutoffTimestamp ORDER BY timestamp DESC")
    fun getRunsSince(cutoffTimestamp: Long): Flow<List<RunRecordEntity>>

    @Insert
    suspend fun insertRun(runRecord: RunRecordEntity): Long

    @Delete
    suspend fun deleteRun(runRecord: RunRecordEntity)

    @Query("DELETE FROM run_records WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOldRuns(cutoffTimestamp: Long)
}

@Dao
interface TimerDao {
    @Query("SELECT * FROM timer_records ORDER BY id ASC")
    fun getAllTimers(): Flow<List<TimerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: TimerEntity)

    @Update
    suspend fun updateTimer(timer: TimerEntity)

    @Delete
    suspend fun deleteTimer(timer: TimerEntity)

    @Query("DELETE FROM timer_records WHERE id = :id")
    suspend fun deleteTimerById(id: Long)
}

@Dao
interface StopwatchDao {
    @Query("SELECT * FROM stopwatch_records WHERE id = 1")
    fun getStopwatchState(): Flow<StopwatchEntity?>

    @Query("SELECT * FROM stopwatch_records WHERE id = 1")
    suspend fun getStopwatchOnce(): StopwatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopwatch(stopwatch: StopwatchEntity)

    @Query("SELECT * FROM lap_records ORDER BY timestamp ASC")
    fun getAllLaps(): Flow<List<LapRecordEntity>>

    @Insert
    suspend fun insertLap(lap: LapRecordEntity)

    @Query("DELETE FROM lap_records")
    suspend fun deleteAllLaps()
}