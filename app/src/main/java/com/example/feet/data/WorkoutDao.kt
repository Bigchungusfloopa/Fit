package com.example.feet.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp DESC")
    fun getWorkoutsBetween(start: Long, end: Long): Flow<List<Workout>>

    @Insert
    suspend fun insert(workout: Workout)

    @Update
    suspend fun update(workout: Workout)

    @Delete
    suspend fun delete(workout: Workout)

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: Long): Workout?
}