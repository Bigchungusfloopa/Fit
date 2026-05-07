package com.example.feet.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WaterRecord::class,
        StepRecord::class,
        WorkoutEntity::class,
        UserPreferences::class,
        RunRecordEntity::class,
        TimerEntity::class,
        StopwatchEntity::class,
        LapRecordEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FitnessDatabase : RoomDatabase() {

    abstract fun waterDao(): WaterDao
    abstract fun stepDao(): StepDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun preferencesDao(): PreferencesDao
    abstract fun runDao(): RunDao
    abstract fun timerDao(): TimerDao
    abstract fun stopwatchDao(): StopwatchDao

    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        fun getDatabase(context: Context): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}