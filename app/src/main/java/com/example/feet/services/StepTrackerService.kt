package com.example.feet.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.feet.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StepTrackerService : Service(), SensorEventListener {

    private val binder = StepTrackerBinder()
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    private var initialSteps = 0
    private var hasInitialValue = false

    inner class StepTrackerBinder : Binder() {
        fun getService(): StepTrackerService = this@StepTrackerService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        setupStepSensor()
        startForegroundService()
    }

    private fun setupStepSensor() {
        // Try to get step counter sensor (most accurate)
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            // Fallback to step detector (less accurate but works on more devices)
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            Log.d("StepTracker", "Using STEP_DETECTOR sensor")
        } else {
            Log.d("StepTracker", "Using STEP_COUNTER sensor")
        }

        stepSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: run {
            Log.e("StepTracker", "No step sensor available on this device")
        }
    }

    override fun onSensorEvent(event: SensorEvent?) {
        event?.let {
            when (event.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    // STEP_COUNTER gives total steps since last reboot
                    val totalSteps = event.values[0].toInt()

                    if (!hasInitialValue) {
                        initialSteps = totalSteps
                        hasInitialValue = true
                    }

                    val stepsSinceStart = totalSteps - initialSteps
                    _stepCount.value = stepsSinceStart
                    Log.d("StepTracker", "Step Counter: $stepsSinceStart steps")
                }

                Sensor.TYPE_STEP_DETECTOR -> {
                    // STEP_DETECTOR gives 1.0 for each step detected
                    if (event.values[0] == 1.0f) {
                        _stepCount.value += 1
                        Log.d("StepTracker", "Step Detected: ${_stepCount.value} steps")
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    private fun startForegroundService() {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "step_tracker_channel")
            .setContentTitle("Feet - Step Tracking")
            .setContentText("Tracking your steps in real-time")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "step_tracker_channel",
                "Step Tracker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks your steps in the background"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    // Method to reset steps (call this at midnight)
    fun resetSteps() {
        if (stepSensor?.type == Sensor.TYPE_STEP_COUNTER) {
            hasInitialValue = false
        }
        _stepCount.value = 0
    }

    // Method to get current steps
    fun getCurrentSteps(): Int = _stepCount.value
}