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

class WaterWidget : AppWidgetProvider() {

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
                addWater(context, 250) // Add one glass (250ml)
                updateAllWidgets(context)
            }
            ACTION_REMOVE_WATER -> {
                removeWater(context, 250)
                updateAllWidgets(context)
            }
        }
    }

    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, WaterWidget::class.java)
        )
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun addWater(context: Context, ml: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentWater = prefs.getInt(KEY_WATER, 0)
        val goal = prefs.getInt(KEY_GOAL, 4000)
        val newWater = (currentWater + ml).coerceAtMost(goal)
        prefs.edit().putInt(KEY_WATER, newWater).apply()
    }

    private fun removeWater(context: Context, ml: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentWater = prefs.getInt(KEY_WATER, 0)
        val newWater = (currentWater - ml).coerceAtLeast(0)
        prefs.edit().putInt(KEY_WATER, newWater).apply()
    }

    companion object {
        private const val PREFS_NAME = "WaterWidgetPrefs"
        private const val KEY_WATER = "water_ml"
        private const val KEY_GOAL = "water_goal"
        const val ACTION_ADD_WATER = "com.example.feet.ACTION_ADD_WATER"
        const val ACTION_REMOVE_WATER = "com.example.feet.ACTION_REMOVE_WATER"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val waterMl = prefs.getInt(KEY_WATER, 0)
            val goal = prefs.getInt(KEY_GOAL, 4000)
            val waterLiters = waterMl / 1000f
            val glasses = waterMl / 250
            val progress = ((waterMl.toFloat() / goal) * 100).toInt().coerceIn(0, 100)

            val views = RemoteViews(context.packageName, R.layout.widget_water)

            // Update water count
            views.setTextViewText(R.id.widget_water_count, String.format("%.1fL", waterLiters))
            views.setTextViewText(R.id.widget_water_glasses, "$glasses glasses")
            views.setProgressBar(R.id.widget_water_progress, 100, progress, false)

            // Add button click
            val addIntent = Intent(context, WaterWidget::class.java).apply {
                action = ACTION_ADD_WATER
            }
            val addPendingIntent = PendingIntent.getBroadcast(
                context, 0, addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_water_add, addPendingIntent)

            // Remove button click
            val removeIntent = Intent(context, WaterWidget::class.java).apply {
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
}