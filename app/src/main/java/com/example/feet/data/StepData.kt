package com.example.feet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_data")
data class StepData(
    @PrimaryKey
    val date: String, // "2024-01-15" format
    val steps: Int,
    val calories: Int,
    val distance: Float
)