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
        WidgetDiagnosticLogger.logAllocationAttempt()
        return try {
            val id = host.allocateAppWidgetId()
            WidgetDiagnosticLogger.logAllocationSuccess(id)
            id
        } catch (e: Exception) {
            WidgetDiagnosticLogger.logAllocationFailure(e)
            -1
        }
    }

    fun deleteAppWidgetId(host: AppWidgetHost, appWidgetId: Int) {
        if (appWidgetId != -1) {
            WidgetDiagnosticLogger.logDeletionAttempt(appWidgetId)
            try {
                host.deleteAppWidgetId(appWidgetId)
            } catch (e: Exception) {
                WidgetDiagnosticLogger.logDeletionFailure(appWidgetId, e)
            }
        }
    }

    fun startWidgetPicker(
        context: Context,
        host: AppWidgetHost,
        pickerLauncher: ActivityResultLauncher<Intent>,
        onAllocated: (Int) -> Unit
    ) {
        val appWidgetId = allocateAppWidgetId(host)
        
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            // Add empty custom info/extras to prevent crashes on some OEM ROMs
            putParcelableArrayListExtra(
                AppWidgetManager.EXTRA_CUSTOM_INFO,
                ArrayList<android.appwidget.AppWidgetProviderInfo>()
            )
            putParcelableArrayListExtra(
                AppWidgetManager.EXTRA_CUSTOM_EXTRAS,
                ArrayList<android.os.Bundle>()
            )
        }
        try {
            pickerLauncher.launch(pickIntent)
            onAllocated(appWidgetId)
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Failed to open widget picker: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            deleteAppWidgetId(host, appWidgetId)
        }
    }

    fun handlePickResult(
        context: Context,
        appWidgetId: Int,
        appWidgetManager: AppWidgetManager,
        onLaunchConfig: (Intent) -> Unit,
        onSuccess: (Int) -> Unit,
        onError: () -> Unit
    ) {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
        if (info == null) {
            android.util.Log.e("WidgetDiagnostic", "handlePickResult: Failed to get AppWidgetProviderInfo for id $appWidgetId. The widget might not have bound correctly.")
            onError()
            return
        }

        if (info.configure != null) {
            android.util.Log.d("WidgetDiagnostic", "handlePickResult: Widget requires configuration via ${info.configure.flattenToShortString()}")
            // Widget requires configuration
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = info.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            try {
                onLaunchConfig(intent)
            } catch (e: Exception) {
                android.util.Log.e("WidgetDiagnostic", "handlePickResult: Failed to launch configuration activity.", e)
                e.printStackTrace()
                // If configuration fails to launch, we can't use the widget properly
                onError()
            }
        } else {
            android.util.Log.d("WidgetDiagnostic", "handlePickResult: Widget bound successfully without configuration.")
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
