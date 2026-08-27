package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.UserManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MinimalistApp
import com.example.data.ClockFont
import com.example.data.LauncherSettings
import com.example.data.LauncherTheme
import com.example.model.AppInfoItem
import com.example.model.AppLimitRule
import com.example.model.BottomSlot
import com.example.model.CustomAppLabel
import com.example.model.DailyAppUsage
import com.example.model.FocusProfile
import com.example.model.HiddenApp
import com.example.model.TaskItem
import com.example.model.WidgetSlot
import com.example.util.AppManager
import com.example.util.BatteryHelper
import com.example.util.BatteryStatus
import com.example.util.CryptoUtil
import com.example.util.FontManager
import com.example.util.UsageStatsHelper
import com.example.widget.AppWidgetHostManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MinimalistApp
    private val dao = app.database.launcherDao()
    private val prefManager = app.preferencesManager

    val settings: StateFlow<LauncherSettings> = prefManager.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LauncherSettings())

    val focusProfiles: StateFlow<List<FocusProfile>> = dao.getAllFocusProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val taskItems: StateFlow<List<TaskItem>> = dao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Decrypted Hidden Apps Flow (packages decrypted on the fly from encrypted database rows)
    val hiddenApps: StateFlow<List<HiddenApp>> = dao.getAllHiddenApps()
        .map { encryptedList ->
            encryptedList.map { item ->
                val decryptedPkg = CryptoUtil.decrypt(item.packageName)
                HiddenApp(packageName = decryptedPkg, addedTimestamp = item.addedTimestamp)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customLabels: StateFlow<List<CustomAppLabel>> = dao.getAllCustomLabels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bottomSlots: StateFlow<List<BottomSlot>> = dao.getAllBottomSlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val limitRules: StateFlow<List<AppLimitRule>> = dao.getAllLimitRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val widgetSlots: StateFlow<List<WidgetSlot>> = dao.getAllWidgetSlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _rawInstalledApps = MutableStateFlow<List<AppInfoItem>>(emptyList())
    val rawInstalledApps = _rawInstalledApps.asStateFlow()

    private val _topUsageApps = MutableStateFlow<List<DailyAppUsage>>(emptyList())
    val topUsageApps = _topUsageApps.asStateFlow()

    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission = _hasUsagePermission.asStateFlow()

    private val _isPrivateSpaceUnlocked = MutableStateFlow(false)
    val isPrivateSpaceUnlocked = _isPrivateSpaceUnlocked.asStateFlow()

    val batteryStatus: StateFlow<BatteryStatus> = BatteryHelper.getBatteryStatusFlow(application)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BatteryStatus())

    // Enriched Installed Apps with custom labels, hidden status, limit rules
    val enrichedApps: StateFlow<List<AppInfoItem>> = combine(
        _rawInstalledApps,
        hiddenApps,
        customLabels,
        limitRules
    ) { apps, hidden, labels, limits ->
        val hiddenSet = hidden.map { it.packageName }.toSet()
        val labelMap = labels.associate { it.packageName to it.customLabel }
        val limitMap = limits.associate { it.packageName to it.dailyLimitMinutes }

        apps.map { appItem ->
            appItem.copy(
                customLabel = labelMap[appItem.packageName],
                isHidden = hiddenSet.contains(appItem.packageName),
                dailyLimitMinutes = limitMap[appItem.packageName]
            )
        }.sortedBy { it.displayLabel.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Focus Profile Apps (Max 5)
    val activeFocusApps: StateFlow<List<AppInfoItem>> = combine(
        enrichedApps,
        focusProfiles,
        settings
    ) { apps, profiles, currentSettings ->
        val activeProfile = profiles.find { it.id == currentSettings.activeProfileId }
            ?: profiles.firstOrNull()

        if (activeProfile == null) return@combine emptyList()

        val activePkgs = activeProfile.getPackages()
        val appMap = apps.associateBy { it.packageName }

        activePkgs.mapNotNull { pkg ->
            appMap[pkg] ?: AppInfoItem(packageName = pkg, label = pkg.substringAfterLast('.'))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recent Apps (5 most recently used apps)
    val recentApps: StateFlow<List<AppInfoItem>> = combine(
        enrichedApps,
        _rawInstalledApps
    ) { allApps, _ ->
        val recentPackages = UsageStatsHelper.getRecentApps(getApplication(), limit = 5)
        val appMap = allApps.associateBy { it.packageName }
        recentPackages.mapNotNull { pkg -> appMap[pkg] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ensureDefaultData()
            } catch (e: Exception) {
                // Safe ignore initialization errors
            }
            refreshInstalledApps()
            refreshUsageStats()
        }
    }

    private suspend fun ensureDefaultData() {
        val profiles = dao.getAllFocusProfilesDirect()
        if (profiles.isEmpty()) {
            dao.insertFocusProfile(
                FocusProfile(
                    id = 1,
                    name = "Deep Work",
                    isDndLinked = false,
                    appPackage1 = "com.google.android.gm",
                    appPackage2 = "com.google.android.keep",
                    appPackage3 = "com.google.android.calendar",
                    appPackage4 = "com.android.chrome",
                    appPackage5 = "com.google.android.apps.docs"
                )
            )
            dao.insertFocusProfile(
                FocusProfile(
                    id = 2,
                    name = "Minimal",
                    isDndLinked = false,
                    appPackage1 = "com.google.android.dialer",
                    appPackage2 = "com.google.android.apps.messaging",
                    appPackage3 = "com.google.android.keep",
                    appPackage4 = "",
                    appPackage5 = ""
                )
            )
            dao.insertFocusProfile(
                FocusProfile(
                    id = 3,
                    name = "Wind Down",
                    isDndLinked = true,
                    appPackage1 = "com.google.android.apps.books",
                    appPackage2 = "com.google.android.apps.podcasts",
                    appPackage3 = "com.google.android.deskclock",
                    appPackage4 = "",
                    appPackage5 = ""
                )
            )
        }

        val slots = dao.getAllBottomSlotsDirect()
        if (slots.isEmpty()) {
            dao.insertBottomSlot(BottomSlot(slotIndex = 0, packageName = "", customLabel = "Phone", defaultType = "phone"))
            dao.insertBottomSlot(BottomSlot(slotIndex = 1, packageName = "", customLabel = "Messages", defaultType = "messages"))
            dao.insertBottomSlot(BottomSlot(slotIndex = 2, packageName = "", customLabel = "Camera", defaultType = "camera"))
        }

        val wSlots = dao.getAllWidgetSlotsDirect()
        if (wSlots.isEmpty()) {
            dao.insertWidgetSlot(WidgetSlot(slotKey = "HEADER_WEATHER", appWidgetId = -1, isCustomWidgetEnabled = false))
            dao.insertWidgetSlot(WidgetSlot(slotKey = "UTILITY_BATTERY", appWidgetId = -1, isCustomWidgetEnabled = false))
        }

        val tasks = dao.getAllTasksDirect()
        if (tasks.isEmpty()) {
            dao.insertTask(TaskItem(title = "Welcome to Minimalist Launcher", isDone = false, timestamp = System.currentTimeMillis()))
            dao.insertTask(TaskItem(title = "Swipe right or tap Utility for Battery & Tasks", isDone = false, timestamp = System.currentTimeMillis() - 1000))
            dao.insertTask(TaskItem(title = "Swipe up for all apps", isDone = false, timestamp = System.currentTimeMillis() - 2000))
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = AppManager.getInstalledApps(getApplication())
            _rawInstalledApps.value = apps
            refreshUsageStats()
        }
    }

    fun refreshUsageStats() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasPerm = UsageStatsHelper.hasUsageStatsPermission(getApplication())
            _hasUsagePermission.value = hasPerm
            if (hasPerm) {
                val labelMap = _rawInstalledApps.value.associate { it.packageName to it.label }
                val topApps = UsageStatsHelper.getTodayTopApps(getApplication(), labelMap, limit = 5)
                _topUsageApps.value = topApps
            } else {
                _topUsageApps.value = emptyList()
            }
        }
    }

    // Focus Profile Management
    fun selectFocusProfile(profileId: Int) {
        viewModelScope.launch {
            prefManager.updateActiveProfileId(profileId)
            val profile = focusProfiles.value.find { it.id == profileId }
            if (profile?.lockPrivateSpace != false) {
                // Auto-lock private space when focus profile becomes active
                _isPrivateSpaceUnlocked.value = false
            }
        }
    }

    fun saveFocusProfile(profile: FocusProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            if (profile.id == 0) {
                val newId = dao.insertFocusProfile(profile)
                prefManager.updateActiveProfileId(newId.toInt())
            } else {
                dao.updateFocusProfile(profile)
            }
        }
    }

    fun deleteFocusProfile(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteFocusProfile(id)
            val current = settings.value.activeProfileId
            if (current == id) {
                prefManager.updateActiveProfileId(1)
            }
        }
    }

    // To-Do list operations
    fun addTask(title: String, priority: Int = 0) {
        if (title.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertTask(
                TaskItem(
                    title = title.trim(),
                    isDone = false,
                    timestamp = System.currentTimeMillis(),
                    priority = priority
                )
            )
        }
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateTask(task.copy(isDone = !task.isDone))
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteTask(taskId)
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearCompletedTasks()
        }
    }

    // Hidden Apps / Private Space with AES-GCM Encrypted Storage
    fun setAppHidden(packageName: String, isHidden: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val allStored = dao.getAllHiddenAppsDirect()
            val existing = allStored.find { CryptoUtil.decrypt(it.packageName) == packageName }

            if (isHidden) {
                if (existing == null) {
                    val encryptedPackageName = CryptoUtil.encrypt(packageName)
                    dao.insertHiddenApp(HiddenApp(packageName = encryptedPackageName))
                }
            } else {
                if (existing != null) {
                    dao.deleteHiddenApp(existing.packageName)
                }
                // Also clean up any unencrypted legacy matches
                dao.deleteHiddenApp(packageName)
            }
        }
    }

    fun setPrivateSpaceUnlocked(unlocked: Boolean) {
        _isPrivateSpaceUnlocked.value = unlocked
    }

    // Custom Label / Rename
    fun setCustomAppLabel(packageName: String, label: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (label.isBlank()) {
                dao.deleteCustomLabel(packageName)
            } else {
                dao.insertCustomLabel(CustomAppLabel(packageName = packageName, customLabel = label.trim()))
            }
        }
    }

    // App Limit Rules
    fun setAppLimit(packageName: String, minutes: Int, isEnabled: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            if (minutes <= 0) {
                dao.deleteLimitRule(packageName)
            } else {
                dao.insertLimitRule(AppLimitRule(packageName = packageName, dailyLimitMinutes = minutes, isEnabled = isEnabled))
            }
        }
    }

    fun removeAppLimit(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteLimitRule(packageName)
        }
    }

    // Bottom Slot Configuration
    fun setBottomSlot(slotIndex: Int, packageName: String, customLabel: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertBottomSlot(
                BottomSlot(
                    slotIndex = slotIndex,
                    packageName = packageName,
                    customLabel = customLabel
                )
            )
        }
    }

    // Widget Slot Configuration
    fun setWidgetSlot(slotKey: String, appWidgetId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertWidgetSlot(
                WidgetSlot(
                    slotKey = slotKey,
                    appWidgetId = appWidgetId,
                    isCustomWidgetEnabled = true
                )
            )
        }
    }

    fun removeWidgetSlot(slotKey: String, appWidgetId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            AppWidgetHostManager.deleteAppWidgetId(app.appWidgetHost, appWidgetId)
            dao.deleteWidgetSlot(slotKey)
        }
    }

    // Theme & Font Preferences
    fun setTheme(theme: LauncherTheme) {
        viewModelScope.launch { prefManager.updateTheme(theme) }
    }

    fun setClockFont(font: ClockFont) {
        viewModelScope.launch { prefManager.updateClockFont(font) }
    }

    fun setCustomFontDisplayName(name: String) {
        viewModelScope.launch { prefManager.updateCustomFontDisplayName(name) }
    }

    fun setApplyFontToAllUI(applyToAll: Boolean) {
        viewModelScope.launch { prefManager.updateApplyFontToAllUI(applyToAll) }
    }

    fun importCustomFont(uri: Uri): Result<String> {
        val result = FontManager.importFontFromUri(getApplication(), uri)
        if (result.isSuccess) {
            val displayName = result.getOrNull() ?: "Custom TTF/OTF Font"
            viewModelScope.launch {
                prefManager.updateCustomFontDisplayName(displayName)
                prefManager.updateClockFont(ClockFont.CUSTOM_FILE)
            }
        }
        return result
    }

    fun clearCustomFont() {
        FontManager.clearCustomFont(getApplication())
        viewModelScope.launch {
            prefManager.updateCustomFontDisplayName("")
            if (settings.value.clockFont == ClockFont.CUSTOM_FILE) {
                prefManager.updateClockFont(ClockFont.RETRO_MONO)
            }
        }
    }

    fun set24Hour(is24: Boolean) {
        viewModelScope.launch { prefManager.update24Hour(is24) }
    }

    fun setShowSeconds(show: Boolean) {
        viewModelScope.launch { prefManager.updateShowSeconds(show) }
    }

    fun setShowDate(show: Boolean) {
        viewModelScope.launch { prefManager.updateShowDate(show) }
    }

    fun setWallpaperDim(dim: Float) {
        viewModelScope.launch { prefManager.updateWallpaperDim(dim) }
    }

    fun setWallpaperEnabled(enabled: Boolean) {
        viewModelScope.launch { prefManager.updateWallpaperEnabled(enabled) }
    }

    fun setLockPrivateSpaceWithFocus(lock: Boolean) {
        viewModelScope.launch { prefManager.updateLockPrivateSpaceWithFocus(lock) }
    }

    fun setUseBatteryWidget(useWidget: Boolean) {
        viewModelScope.launch { prefManager.updateUseBatteryWidget(useWidget) }
    }

    fun setSwipeRightAction(action: String) {
        viewModelScope.launch { prefManager.updateSwipeRightAction(action) }
    }

    fun setSwipeLeftAction(action: String) {
        viewModelScope.launch { prefManager.updateSwipeLeftAction(action) }
    }

    fun setSwipeRightPackage(pkg: String) {
        viewModelScope.launch { prefManager.updateSwipeRightPackage(pkg) }
    }

    fun setSwipeLeftPackage(pkg: String) {
        viewModelScope.launch { prefManager.updateSwipeLeftPackage(pkg) }
    }

    fun setSearchEnginePackage(pkg: String) {
        viewModelScope.launch { prefManager.updateSearchEnginePackage(pkg) }
    }
}
