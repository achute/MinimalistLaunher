package com.example.widget

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

object AppWidgetHostManager {

    const val SLOT_HEADER_WEATHER = "HEADER_WEATHER"
    const val SLOT_UTILITY_BATTERY = "UTILITY_BATTERY"

    fun allocateAppWidgetId(host: AppWidgetHost): Int {
        return host.allocateAppWidgetId()
    }

    fun deleteAppWidgetId(host: AppWidgetHost, appWidgetId: Int) {
        if (appWidgetId != -1) {
            try {
                host.deleteAppWidgetId(appWidgetId)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun startWidgetPicker(
        host: AppWidgetHost,
        pickerLauncher: ActivityResultLauncher<Intent>,
        onAllocated: (Int) -> Unit
    ) {
        val appWidgetId = allocateAppWidgetId(host)
        onAllocated(appWidgetId)

        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        pickerLauncher.launch(pickIntent)
    }

    fun handlePickResult(
        context: Context,
        appWidgetId: Int,
        appWidgetManager: AppWidgetManager,
        configLauncher: ActivityResultLauncher<Intent>,
        onSuccess: (Int) -> Unit,
        onError: () -> Unit
    ) {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
        if (info == null) {
            onError()
            return
        }

        if (info.configure != null) {
            // Widget requires configuration
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = info.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            configLauncher.launch(intent)
        } else {
            // No configuration needed
            onSuccess(appWidgetId)
        }
    }

    fun getAppWidgetInfo(context: Context, appWidgetId: Int): AppWidgetProviderInfo? {
        if (appWidgetId == -1) return null
        val manager = AppWidgetManager.getInstance(context)
        return try {
            manager.getAppWidgetInfo(appWidgetId)
        } catch (e: Exception) {
            null
        }
    }
}
