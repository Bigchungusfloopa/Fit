package com.example.feet.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [WaterIntake::class, StepData::class, Workout::class],
    version = 1,
    exportSchema = false  // Add this to disable schema export
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterIntakeDao(): WaterIntakeDao
    abstract fun stepDataDao(): StepDataDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "feet.db"
                )
                    .fallbackToDestructiveMigration()  // Add this for safety
                    .build()
                    .also { instance = it }
            }
        }
    }
}