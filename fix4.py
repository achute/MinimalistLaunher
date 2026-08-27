import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Find the start of the end block
start_str = "                onDismiss = { isSettingsOpen = false }"
start_idx = content.find(start_str)

end_content = """                onDismiss = { isSettingsOpen = false }
            )
        }
    }
}

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        val app = application as MinimalistApp
        val appWidgetHost = app.appWidgetHost
        val appWidgetManager = AppWidgetManager.getInstance(this)
        
        if (requestCode == REQUEST_BIND_WIDGET) {
            val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingAppWidgetId) ?: pendingAppWidgetId
            if (resultCode == android.app.Activity.RESULT_OK && appWidgetId != -1) {
                com.example.widget.AppWidgetHostManager.handlePickResult(
                    context = this,
                    appWidgetId = appWidgetId,
                    appWidgetManager = appWidgetManager,
                    onLaunchConfig = { configIntent ->
                        startActivityForResult(configIntent, REQUEST_CONFIG_WIDGET)
                    },
                    onSuccess = { configuredId ->
                        pendingWidgetSlotKey?.let { slotKey ->
                            viewModel.setWidgetSlot(slotKey, configuredId)
                        }
                        pendingWidgetSlotKey = null
                    },
                    onError = {
                        com.example.widget.AppWidgetHostManager.deleteAppWidgetId(appWidgetHost, appWidgetId)
                        pendingWidgetSlotKey = null
                    }
                )
            } else {
                if (pendingAppWidgetId != -1) {
                    com.example.widget.AppWidgetHostManager.deleteAppWidgetId(appWidgetHost, pendingAppWidgetId)
                    pendingAppWidgetId = -1
                }
                pendingWidgetSlotKey = null
            }
        } else if (requestCode == REQUEST_CONFIG_WIDGET) {
            val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingAppWidgetId) ?: pendingAppWidgetId
            if (resultCode == android.app.Activity.RESULT_OK && appWidgetId != -1) {
                pendingWidgetSlotKey?.let { slotKey ->
                    viewModel.setWidgetSlot(slotKey, appWidgetId)
                }
            } else {
                if (pendingAppWidgetId != -1) {
                    com.example.widget.AppWidgetHostManager.deleteAppWidgetId(appWidgetHost, pendingAppWidgetId)
                    pendingAppWidgetId = -1
                }
            }
            pendingWidgetSlotKey = null
        }
    }
}
"""

new_content = content[:start_idx] + end_content

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(new_content)
