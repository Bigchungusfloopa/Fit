package com.example.feet.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDataDao {
    @Query("SELECT * FROM step_data WHERE date = :date")
    fun getByDate(date: String): Flow<StepData?>

    @Upsert
    suspend fun upsert(stepData: StepData)
}