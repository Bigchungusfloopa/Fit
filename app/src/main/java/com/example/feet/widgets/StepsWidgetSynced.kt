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

class StepsWidgetSynced : AppWidgetProvider() {

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
            ACTION_ADD_STEPS -> {
                widgetScope.launch {
                    addSteps(context, 100)
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
            ComponentName(context, StepsWidgetSynced::class.java)
        )
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private suspend fun addSteps(context: Context, steps: Int) {
        withContext(Dispatchers.IO) {
            try {
                val database = FitnessDatabase.getDatabase(context)
                val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

                // Get current steps from database
                val currentRecord = database.stepDao().getStepsByDate(today)
                val currentSteps = currentRecord?.steps ?: 0
                val goal = currentRecord?.goal ?: 10000

                // Update steps
                val newSteps = currentSteps + steps
                database.stepDao().insertSteps(
                    com.example.feet.data.database.StepRecord(
                        date = today,
                        steps = newSteps,
                        goal = goal
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val ACTION_ADD_STEPS = "com.example.feet.ACTION_ADD_STEPS_SYNCED"
        const val ACTION_REFRESH = "com.example.feet.ACTION_REFRESH_STEPS_WIDGET"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                val (steps, goal) = withContext(Dispatchers.IO) {
                    try {
                        val database = FitnessDatabase.getDatabase(context)
                        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                        val record = database.stepDao().getStepsByDate(today)

                        Pair(record?.steps ?: 0, record?.goal ?: 10000)
                    } catch (e: Exception) {
                        Pair(0, 10000)
                    }
                }

                val progress = ((steps.toFloat() / goal) * 100).toInt().coerceIn(0, 100)

                val views = RemoteViews(context.packageName, R.layout.widget_steps)

                // Update steps count
                views.setTextViewText(R.id.widget_steps_count, steps.toString())
                views.setTextViewText(R.id.widget_steps_goal, "/ $goal")
                views.setProgressBar(R.id.widget_steps_progress, 100, progress, false)

                // Add button click
                val addIntent = Intent(context, StepsWidgetSynced::class.java).apply {
                    action = ACTION_ADD_STEPS
                }
                val addPendingIntent = PendingIntent.getBroadcast(
                    context, 0, addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_steps_add, addPendingIntent)

                // Refresh button (instead of reset)
                val refreshIntent = Intent(context, StepsWidgetSynced::class.java).apply {
                    action = ACTION_REFRESH
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context, 1, refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_steps_reset, refreshPendingIntent)

                // Widget click - opens app
                val mainIntent = Intent(context, MainActivity::class.java)
                val mainPendingIntent = PendingIntent.getActivity(
                    context, 2, mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_steps_container, mainPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        // Call this from your app when steps are updated
        fun notifyDataChanged(context: Context) {
            val intent = Intent(context, StepsWidgetSynced::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}