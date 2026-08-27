package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_profiles")
data class FocusProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isDndLinked: Boolean = false,
    val lockPrivateSpace: Boolean = true,
    val appPackage1: String = "",
    val appPackage2: String = "",
    val appPackage3: String = "",
    val appPackage4: String = "",
    val appPackage5: String = ""
) {
    fun getPackages(): List<String> {
        return listOf(appPackage1, appPackage2, appPackage3, appPackage4, appPackage5)
            .filter { it.isNotBlank() }
    }
}

@Entity(tableName = "app_limit_rules")
data class AppLimitRule(
    @PrimaryKey val packageName: String,
    val dailyLimitMinutes: Int = 30,
    val isEnabled: Boolean = true
)

@Entity(tableName = "task_items")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isDone: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val priority: Int = 0 // 0: Normal, 1: High
)

@Entity(tableName = "hidden_apps")
data class HiddenApp(
    @PrimaryKey val packageName: String,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_app_labels")
data class CustomAppLabel(
    @PrimaryKey val packageName: String,
    val customLabel: String
)

@Entity(tableName = "bottom_slots")
data class BottomSlot(
    @PrimaryKey val slotIndex: Int, // 0: Phone, 1: Messages, 2: Camera, 3: Settings
    val packageName: String = "",
    val customLabel: String = "",
    val defaultType: String = "", // "phone", "messages", "camera", "settings", "browser", "search", "custom"
    val iconName: String = "" // "phone", "messages", "camera", "settings", "browser", "search", "email", "music", etc.
)

@Entity(tableName = "widget_slots")
data class WidgetSlot(
    @PrimaryKey val slotKey: String, // e.g. "HEADER_WEATHER", "UTILITY_BATTERY"
    val appWidgetId: Int = -1,
    val isCustomWidgetEnabled: Boolean = false
)

data class AppInfoItem(
    val packageName: String,
    val label: String,
    val customLabel: String? = null,
    val isHidden: Boolean = false,
    val isFavorite: Boolean = false,
    val lastUsedTimestamp: Long = 0L,
    val todayUsageMillis: Long = 0L,
    val dailyLimitMinutes: Int? = null,
    val userHandle: android.os.UserHandle? = null,
    val isPrivateProfile: Boolean = false
) {
    val displayLabel: String
        get() = customLabel?.takeIf { it.isNotBlank() } ?: label
}

data class DailyAppUsage(
    val packageName: String,
    val appLabel: String,
    val usageMillis: Long,
    val percentageOfTotal: Float = 0f
)
