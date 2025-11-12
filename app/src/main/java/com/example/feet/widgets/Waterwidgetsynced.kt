package com.example.feet.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.feet.R
import com.example.feet.MainActivity
import com.example.feet.data.database.FitnessDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WaterWidgetSynced : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_ADD_WATER -> {
                widgetScope.launch {
                    addWater(context, 250)
                    updateAllWidgets(context)
                }
            }
            ACTION_REMOVE_WATER -> {
                widgetScope.launch {
                    removeWater(context, 250)
                    updateAllWidgets(context)
                }
            }
            ACTION_REFRESH -> {
                updateAllWidgets(context)
            }
        }
    }

    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, WaterWidgetSynced::class.java)
        )
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private suspend fun addWater(context: Context, ml: Int) {
        withContext(Dispatchers.IO) {
            try {
                val database = FitnessDatabase.getDatabase(context)
                val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

                // Get current water from database
                val currentRecord = database.waterDao().getWaterByDate(today)
                val currentWater = currentRecord?.totalMl ?: 0
                val glassSize = currentRecord?.glassSize ?: 250f

                // Get goal from preferences
                val prefs = database.preferencesDao().getPreferencesOnce()
                val goal = prefs?.dailyWaterGoalMl ?: 4000

                // Add water (don't exceed goal)
                val newWater = (currentWater + ml).coerceAtMost(goal)

                database.waterDao().insertWater(
                    com.example.feet.data.database.WaterRecord(
                        date = today,
                        totalMl = newWater,
                        glassSize = glassSize
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun removeWater(context: Context, ml: Int) {
        withContext(Dispatchers.IO) {
            try {
                val database = FitnessDatabase.getDatabase(context)
                val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

                // Get current water from database
                val currentRecord = database.waterDao().getWaterByDate(today)
                val currentWater = currentRecord?.totalMl ?: 0
                val glassSize = currentRecord?.glassSize ?: 250f

                // Remove water (don't go below 0)
                val newWater = (currentWater - ml).coerceAtLeast(0)

                database.waterDao().insertWater(
                    com.example.feet.data.database.WaterRecord(
                        date = today,
                        totalMl = newWater,
                        glassSize = glassSize
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val ACTION_ADD_WATER = "com.example.feet.ACTION_ADD_WATER_SYNCED"
        const val ACTION_REMOVE_WATER = "com.example.feet.ACTION_REMOVE_WATER_SYNCED"
        const val ACTION_REFRESH = "com.example.feet.ACTION_REFRESH_WATER_WIDGET"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                val (waterMl, goal, glassSize) = withContext(Dispatchers.IO) {
                    try {
                        val database = FitnessDatabase.getDatabase(context)
                        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                        val record = database.waterDao().getWaterByDate(today)

                        // Get goal from preferences
                        val prefs = database.preferencesDao().getPreferencesOnce()
                        val goalMl = prefs?.dailyWaterGoalMl ?: 4000
                        val size = record?.glassSize ?: 250f

                        Triple(record?.totalMl ?: 0, goalMl, size)
                    } catch (e: Exception) {
                        Triple(0, 4000, 250f)
                    }
                }

                val waterLiters = waterMl / 1000f
                val glasses = waterMl / glassSize.toInt()
                val progress = ((waterMl.toFloat() / goal) * 100).toInt().coerceIn(0, 100)

                val views = RemoteViews(context.packageName, R.layout.widget_water)

                // Update water count
                views.setTextViewText(R.id.widget_water_count, String.format("%.1fL", waterLiters))
                views.setTextViewText(R.id.widget_water_glasses, "$glasses glasses")
                views.setProgressBar(R.id.widget_water_progress, 100, progress, false)

                // Add button click
                val addIntent = Intent(context, WaterWidgetSynced::class.java).apply {
                    action = ACTION_ADD_WATER
                }
                val addPendingIntent = PendingIntent.getBroadcast(
                    context, 0, addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_water_add, addPendingIntent)

                // Remove button click
                val removeIntent = Intent(context, WaterWidgetSynced::class.java).apply {
                    action = ACTION_REMOVE_WATER
                }
                val removePendingIntent = PendingIntent.getBroadcast(
                    context, 1, removeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_water_remove, removePendingIntent)

                // Widget click - opens app
                val mainIntent = Intent(context, MainActivity::class.java)
                val mainPendingIntent = PendingIntent.getActivity(
                    context, 2, mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_water_container, mainPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        // Call this from your app when water is updated
        fun notifyDataChanged(context: Context) {
            val intent = Intent(context, WaterWidgetSynced::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}