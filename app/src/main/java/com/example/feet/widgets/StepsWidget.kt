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

class StepsWidget : AppWidgetProvider() {

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
                addSteps(context, 100)
                updateAllWidgets(context)
            }
            ACTION_RESET_STEPS -> {
                resetSteps(context)
                updateAllWidgets(context)
            }
        }
    }

    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, StepsWidget::class.java)
        )
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun addSteps(context: Context, steps: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentSteps = prefs.getInt(KEY_STEPS, 0)
        prefs.edit().putInt(KEY_STEPS, currentSteps + steps).apply()
    }

    private fun resetSteps(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_STEPS, 0).apply()
    }

    companion object {
        private const val PREFS_NAME = "StepsWidgetPrefs"
        private const val KEY_STEPS = "steps"
        const val ACTION_ADD_STEPS = "com.example.feet.ACTION_ADD_STEPS"
        const val ACTION_RESET_STEPS = "com.example.feet.ACTION_RESET_STEPS"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val steps = prefs.getInt(KEY_STEPS, 0)
            val goal = 10000
            val progress = ((steps.toFloat() / goal) * 100).toInt().coerceIn(0, 100)

            val views = RemoteViews(context.packageName, R.layout.widget_steps)

            // Update steps count
            views.setTextViewText(R.id.widget_steps_count, steps.toString())
            views.setTextViewText(R.id.widget_steps_goal, "/ $goal")
            views.setProgressBar(R.id.widget_steps_progress, 100, progress, false)

            // Add button click - adds 100 steps
            val addIntent = Intent(context, StepsWidget::class.java).apply {
                action = ACTION_ADD_STEPS
            }
            val addPendingIntent = PendingIntent.getBroadcast(
                context, 0, addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_steps_add, addPendingIntent)

            // Reset button click
            val resetIntent = Intent(context, StepsWidget::class.java).apply {
                action = ACTION_RESET_STEPS
            }
            val resetPendingIntent = PendingIntent.getBroadcast(
                context, 1, resetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_steps_reset, resetPendingIntent)

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
}