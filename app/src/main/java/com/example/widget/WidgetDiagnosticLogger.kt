package com.example.widget

import android.appwidget.AppWidgetProviderInfo
import android.util.Log

object WidgetDiagnosticLogger {
    private const val TAG = "WidgetDiagnostic"

    fun logAllocationAttempt() {
        Log.d(TAG, "Attempting to allocate AppWidgetId...")
    }

    fun logAllocationSuccess(appWidgetId: Int) {
        Log.d(TAG, "Successfully allocated AppWidgetId: $appWidgetId")
    }

    fun logAllocationFailure(e: Exception) {
        Log.e(TAG, "CRITICAL: Failed to allocate AppWidgetId. The AppWidgetHost might be in an invalid state or missing permissions.", e)
    }

    fun logBindAttempt(appWidgetId: Int, providerInfo: AppWidgetProviderInfo) {
        Log.d(TAG, "Attempting to bind AppWidgetId $appWidgetId to provider ${providerInfo.provider.flattenToShortString()}")
    }

    fun logBindResult(success: Boolean, appWidgetId: Int, providerInfo: AppWidgetProviderInfo) {
        if (success) {
            Log.d(TAG, "SUCCESS: Bound AppWidgetId $appWidgetId to provider ${providerInfo.provider.flattenToShortString()}")
        } else {
            Log.w(TAG, "WARNING: Failed to bind silently. The app lacks android.permission.BIND_APPWIDGET or user hasn't granted it. Will request via ACTION_APPWIDGET_BIND intent.")
        }
    }

    fun logBindIntentLaunchFailure(e: Exception) {
        Log.e(TAG, "CRITICAL: Failed to launch ACTION_APPWIDGET_BIND intent. The system might have rejected the intent due to missing manifest permissions, or the ActivityNotFoundException was thrown.", e)
    }

    fun logDeletionAttempt(appWidgetId: Int) {
        Log.d(TAG, "Attempting to delete AppWidgetId: $appWidgetId")
    }

    fun logDeletionFailure(appWidgetId: Int, e: Exception) {
        Log.e(TAG, "Failed to delete AppWidgetId: $appWidgetId", e)
    }
}
