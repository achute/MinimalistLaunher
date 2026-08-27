package com.example.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

class LauncherAppWidgetHost(
    context: Context,
    hostId: Int = HOST_ID
) : AppWidgetHost(context, hostId) {

    companion object {
        const val HOST_ID = 1024
    }

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidgetInfo: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return super.onCreateView(context, appWidgetId, appWidgetInfo).apply {
            // Apply standard clean minimalist container style
            setPadding(0, 0, 0, 0)
        }
    }
}
