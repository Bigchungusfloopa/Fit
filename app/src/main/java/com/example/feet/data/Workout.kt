package com.example.feet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val duration: Int, // minutes
    val calories: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val completed: Boolean = false
)