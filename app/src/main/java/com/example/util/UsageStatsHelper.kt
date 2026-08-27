package com.example.util

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import com.example.model.DailyAppUsage
import java.util.Calendar

object UsageStatsHelper {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getTodayUsageMap(context: Context): Map<String, Long> {
        if (!hasUsageStatsPermission(context)) return emptyMap()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyMap()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
        } catch (e: Exception) {
            emptyList<UsageStats>()
        }

        val usageMap = mutableMapOf<String, Long>()
        for (stat in stats) {
            val totalTime = stat.totalTimeInForeground
            if (totalTime > 0) {
                usageMap[stat.packageName] = (usageMap[stat.packageName] ?: 0L) + totalTime
            }
        }
        return usageMap
    }

    fun getTodayTopApps(context: Context, appLabelMap: Map<String, String>, limit: Int = 5): List<DailyAppUsage> {
        val usageMap = getTodayUsageMap(context)
        if (usageMap.isEmpty()) return emptyList()

        val totalUsageMillis = usageMap.values.sum().coerceAtLeast(1L)

        return usageMap.entries
            .filter { it.value > 60_000L } // filter out < 1 min for cleaner dashboard
            .sortedByDescending { it.value }
            .take(limit)
            .map { entry ->
                val label = appLabelMap[entry.key] ?: entry.key.substringAfterLast('.')
                DailyAppUsage(
                    packageName = entry.key,
                    appLabel = label,
                    usageMillis = entry.value,
                    percentageOfTotal = (entry.value.toFloat() / totalUsageMillis).coerceIn(0f, 1f)
                )
            }
    }

    fun getRecentApps(context: Context, limit: Int = 5): List<String> {
        if (!hasUsageStatsPermission(context)) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000L * 60 * 60 * 24 // last 24 hours

        val stats = try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
        } catch (e: Exception) {
            emptyList<UsageStats>()
        }

        val myPackage = context.packageName
        return stats
            .filter { it.lastTimeUsed > 0 && it.packageName != myPackage }
            .sortedByDescending { it.lastTimeUsed }
            .map { it.packageName }
            .distinct()
            .take(limit)
    }

    fun formatDuration(millis: Long): String {
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
}
