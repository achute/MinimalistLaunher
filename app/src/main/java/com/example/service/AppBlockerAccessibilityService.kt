package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.MinimalistApp
import com.example.ui.BlockScreenActivity
import com.example.util.UsageStatsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppBlockerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastBlockedPackage: String? = null
    private var lastBlockedTimestamp: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName ||
            packageName == "com.android.systemui" ||
            packageName == "com.android.settings") {
            return
        }

        serviceScope.launch {
            checkAndEnforceLimit(packageName)
        }
    }

    private suspend fun checkAndEnforceLimit(packageName: String) {
        val app = applicationContext as? MinimalistApp ?: return
        val dao = app.database.launcherDao()

        val rule = dao.getLimitRuleDirect(packageName) ?: return
        if (!rule.isEnabled || rule.dailyLimitMinutes <= 0) return

        // Check if snooze is active for this session
        if (BlockScreenActivity.isPackageSnoozed(packageName)) {
            return
        }

        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && (now - lastBlockedTimestamp) < 3000L) {
            return // Throttle duplicate events within 3 seconds
        }

        val usageMap = UsageStatsHelper.getTodayUsageMap(applicationContext)
        val usageMillis = usageMap[packageName] ?: 0L
        val limitMillis = rule.dailyLimitMinutes * 60 * 1000L

        if (usageMillis >= limitMillis) {
            lastBlockedPackage = packageName
            lastBlockedTimestamp = now

            withContext(Dispatchers.Main) {
                val intent = Intent(applicationContext, BlockScreenActivity::class.java).apply {
                    putExtra(BlockScreenActivity.EXTRA_PACKAGE_NAME, packageName)
                    putExtra(BlockScreenActivity.EXTRA_LIMIT_MINUTES, rule.dailyLimitMinutes)
                    putExtra(BlockScreenActivity.EXTRA_USAGE_MILLIS, usageMillis)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
