import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# First, remove the bad onActivityResult from the end
end_pattern = r"\n    @Deprecated\(\"Deprecated in Java\"\)\n    override fun onActivityResult.*?\}\n\}\n"
# Actually I'll just find the last `@Deprecated("Deprecated in Java")` and cut from there.
idx = content.rfind('@Deprecated("Deprecated in Java")')
if idx != -1:
    content = content[:idx].rstrip() + "\n"
else:
    print("Could not find onActivityResult at end!")

# Now insert it before onDestroy
on_destroy_idx = content.find("    override fun onDestroy() {")

if on_destroy_idx != -1:
    to_insert = """
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

"""
    content = content[:on_destroy_idx] + to_insert + content[on_destroy_idx:]
else:
    print("Could not find onDestroy")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
