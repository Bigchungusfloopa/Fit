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
    val id: Int = 1, // Single row table
    val dailyWaterGoalMl: Int = 4000,
    val dailyStepGoal: Int = 10000,
    val glassSize: Float = 250f,
    val timestamp: Long = System.currentTimeMillis()
)