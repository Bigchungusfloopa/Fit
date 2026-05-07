package com.example.feet.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_records")
data class WaterRecord(
    @PrimaryKey
    val date: String, // Format: "YYYY-MM-DD"
    val totalMl: Int,
    val glassSize: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "step_records")
data class StepRecord(
    @PrimaryKey
    val date: String, // Format: "YYYY-MM-DD"
    val steps: Int,
    val goal: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_records")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: "YYYY-MM-DD"
    val name: String,
    val duration: Int?,
    val goalValue: Int,
    val goalType: String, // "REPS" or "KM"
    val completed: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey
    val id: Int = 1,
    val dailyWaterGoalMl: Int = 4000,
    val dailyStepGoal: Int = 10000,
    val glassSize: Float = 250f,
    val weightKg: Float = 0f,
    val heightCm: Float = 0f,
    val targetWeightKg: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "run_records")
data class RunRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: "YYYY-MM-DD"
    val distanceKm: Float,
    val durationSeconds: Long,
    val avgPace: Float,
    val elevationGain: Float,
    val routePointsString: String, // Serialize List<LatLng> as String
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "timer_records")
data class TimerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationMillis: Long,
    val remainingMillis: Long,
    val isRunning: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "stopwatch_records")
data class StopwatchEntity(
    @PrimaryKey
    val id: Int = 1,
    val accumulatedMillis: Long,
    val isRunning: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "lap_records")
data class LapRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lapMillis: Long,
    val timestamp: Long = System.currentTimeMillis()
)