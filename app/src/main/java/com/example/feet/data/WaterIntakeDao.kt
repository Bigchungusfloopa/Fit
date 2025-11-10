package com.example.feet.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterIntakeDao {
    @Query("SELECT * FROM water_intake WHERE timestamp >= :start AND timestamp < :end")
    fun getWaterBetween(start: Long, end: Long): Flow<List<WaterIntake>>

    @Insert
    suspend fun insert(waterIntake: WaterIntake)

    @Query("SELECT SUM(amountMl) FROM water_intake WHERE timestamp >= :start AND timestamp < :end")
    fun getTotalMlBetween(start: Long, end: Long): Flow<Int?>
}