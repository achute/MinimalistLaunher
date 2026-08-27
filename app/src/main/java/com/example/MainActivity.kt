package com.example

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppInfoItem
import com.example.model.BottomSlot
import com.example.model.FocusProfile
import com.example.ui.components.AppActionDialog
import com.example.ui.components.AppDrawerSheet
import com.example.ui.components.AppPickerDialog
import com.example.ui.components.BottomActionBar
import com.example.ui.components.BottomSlotCustomizationDialog
import com.example.ui.components.ClockHeader
import com.example.ui.components.FocusAppList
import com.example.ui.components.RenameAppDialog
import com.example.ui.components.SetLimitDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.UtilityDashboard
import com.example.ui.theme.MinimalistLauncherTheme
import com.example.ui.viewmodel.LauncherViewModel
import com.example.util.AppManager
import com.example.util.BiometricAuthHelper
import com.example.widget.AppWidgetHostManager
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    private var pendingWidgetSlotKey: String? = null
    private var pendingAppWidgetId: Int = -1

    private lateinit var widgetPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var widgetConfigLauncher: ActivityResultLauncher<Intent>

    private val launcherAppsCallback = object : android.content.pm.LauncherApps.Callback() {
        override fun onPackageAdded(packageName: String, user: android.os.UserHandle) { viewModel.refreshInstalledApps() }
        override fun onPackageChanged(packageName: String, user: android.os.UserHandle) { viewModel.refreshInstalledApps() }
        override fun onPackageRemoved(packageName: String, user: android.os.UserHandle) { viewModel.refreshInstalledApps() }
        override fun onPackagesAvailable(packageNames: Array<out String>, user: android.os.UserHandle, replacing: Boolean) { viewModel.refreshInstalledApps() }
        override fun onPackagesUnavailable(packageNames: Array<out String>, user: android.os.UserHandle, replacing: Boolean) { viewModel.refreshInstalledApps() }
        override fun onPackagesSuspended(packageNames: Array<out String>, user: android.os.UserHandle) { viewModel.refreshInstalledApps() }
        override fun onPackagesUnsuspended(packageNames: Array<out String>, user: android.os.UserHandle) { viewModel.refreshInstalledApps() }
    }

    private val profileReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.refreshInstalledApps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MinimalistApp
        val appWidgetHost = app.appWidgetHost
        val appWidgetManager = AppWidgetManager.getInstance(this)

        // Register Widget Pick Activity Result Launcher
        widgetPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val appWidgetId = result.data?.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    pendingAppWidgetId
                ) ?: pendingAppWidgetId

                if (appWidgetId != -1) {
                    AppWidgetHostManager.handlePickResult(
                        context = this,
                        appWidgetId = appWidgetId,
                        appWidgetManager = appWidgetManager,
                        configLauncher = widgetConfigLauncher,
                        onSuccess = { configuredId ->
                            pendingWidgetSlotKey?.let { slotKey ->
                                viewModel.setWidgetSlot(slotKey, configuredId)
                            }
                        },
                        onError = {
                            AppWidgetHostManager.deleteAppWidgetId(appWidgetHost, appWidgetId)
                        }
                    )
                }
            } else {
                // User cancelled widget pick
                if (pendingAppWidgetId != -1) {
                    AppWidgetHostManager.deleteAppWidgetId(appWidgetHost, pendingAppWidgetId)
                    pendingAppWidgetId = -1
                }
            }
        }

        // Register Widget Configuration Activity Result Launcher
        widgetConfigLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val appWidgetId = result.data?.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    pendingAppWidgetId
                ) ?: pendingAppWidgetId

                if (appWidgetId != -1) {
                    pendingWidgetSlotKey?.let { slotKey ->
                        viewModel.setWidgetSlot(slotKey, appWidgetId)
                    }
                }
            } else {
                // Config cancelled
                if (pendingAppWidgetId != -1) {
                    AppWidgetHostManager.deleteAppWidgetId(appWidgetHost, pendingAppWidgetId)
                    pendingAppWidgetId = -1
                }
            }
        }

        val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as android.content.pm.LauncherApps
        launcherApps.registerCallback(launcherAppsCallback)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val activeFontFamily = remember(settings.clockFont, settings.applyFontToAllUI) {
                if (settings.applyFontToAllUI) {
                    com.example.util.FontManager.resolveFontFamily(this@MainActivity, settings.clockFont)
                } else {
                    androidx.compose.ui.text.font.FontFamily.Monospace
                }
            }

            MinimalistLauncherTheme(theme = settings.theme, fontFamily = activeFontFamily) {
                LauncherRootScreen(
                    viewModel = viewModel,
                    onStartWidgetPick = { slotKey ->
                        pendingWidgetSlotKey = slotKey
                        AppWidgetHostManager.startWidgetPicker(
                            host = appWidgetHost,
                            pickerLauncher = widgetPickerLauncher,
                            onAllocated = { allocatedId ->
                                pendingAppWidgetId = allocatedId
                            }
                        )
                    },
                    onTriggerBiometric = {
                        BiometricAuthHelper.authenticate(
                            activity = this,
                            title = "Unlock Private Space",
                            subtitle = "Use biometrics or PIN to view hidden apps",
                            onSuccess = {
                                viewModel.setPrivateSpaceUnlocked(true)
                            },
                            onError = { errMsg ->
                                Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            val app = application as MinimalistApp
            app.appWidgetHost.startListening()
        } catch (e: Exception) {
            // Ignore listening errors
        }
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_MANAGED_PROFILE_UNLOCKED)
                addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
                addAction("android.intent.action.PROFILE_ACCESSIBLE")
                addAction("android.intent.action.PROFILE_INACCESSIBLE")
            }
            registerReceiver(profileReceiver, filter)
        } catch (e: Exception) {}
        viewModel.refreshInstalledApps()
        viewModel.refreshUsageStats()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshInstalledApps()
    }

    override fun onStop() {
        super.onStop()
        try {
            val app = application as MinimalistApp
            app.appWidgetHost.stopListening()
        } catch (e: Exception) {
            // Ignore stop errors
        }
        try {
            unregisterReceiver(profileReceiver)
        } catch (e: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as android.content.pm.LauncherApps
        launcherApps.unregisterCallback(launcherAppsCallback)
    }
}

@Composable
fun LauncherRootScreen(
    viewModel: LauncherViewModel,
    onStartWidgetPick: (slotKey: String) -> Unit,
    onTriggerBiometric: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = context.applicationContext as MinimalistApp

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val focusProfiles by viewModel.focusProfiles.collectAsStateWithLifecycle()
    val activeFocusApps by viewModel.activeFocusApps.collectAsStateWithLifecycle()
    val bottomSlots by viewModel.bottomSlots.collectAsStateWithLifecycle()
    val widgetSlots by viewModel.widgetSlots.collectAsStateWithLifecycle()
    val enrichedApps by viewModel.enrichedApps.collectAsStateWithLifecycle()
    val filteredApps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val privateSpaceApps by viewModel.privateSpaceApps.collectAsStateWithLifecycle()
    val isOsPrivateSpaceLocked by viewModel.isOsPrivateSpaceLocked.collectAsStateWithLifecycle()
    val osPrivateProfileHandle by viewModel.osPrivateProfileHandle.collectAsStateWithLifecycle()
    val recentApps by viewModel.recentApps.collectAsStateWithLifecycle()
    val taskList by viewModel.taskItems.collectAsStateWithLifecycle()
    val batteryStatus by viewModel.batteryStatus.collectAsStateWithLifecycle()
    val topUsageApps by viewModel.topUsageApps.collectAsStateWithLifecycle()
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsStateWithLifecycle()
    val isPrivateSpaceUnlocked by viewModel.isPrivateSpaceUnlocked.collectAsStateWithLifecycle()

    // Screen State
    var isDrawerOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    // Dialogs State
    var selectedAppForAction by remember { mutableStateOf<AppInfoItem?>(null) }
    var appToRename by remember { mutableStateOf<AppInfoItem?>(null) }
    var appToSetLimit by remember { mutableStateOf<AppInfoItem?>(null) }
    var appPickerSlotIndex by remember { mutableIntStateOf(-1) } // 0-4 for focus, 10-12 for bottom
    var bottomSlotToRemap by remember { mutableStateOf<BottomSlot?>(null) }

    // Horizontal Pager: Page 0 = Utility & Health, Page 1 = Main Home, Page 2 = All Apps
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    // Handle system back press
    BackHandler {
        if (isDrawerOpen) {
            isDrawerOpen = false
        } else if (isSettingsOpen) {
            isSettingsOpen = false
        } else if (pagerState.currentPage != 1) {
            coroutineScope.launch { pagerState.animateScrollToPage(1) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (settings.isWallpaperEnabled) {
                    MaterialTheme.colorScheme.background.copy(alpha = settings.wallpaperDim)
                } else {
                    MaterialTheme.colorScheme.background
                }
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("launcher_root_container")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Minimal Pager Indicator / Quick Page Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                // Page 0: Utility
                Row(
                    modifier = Modifier
                        .clickable { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("page_tab_utility"),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == 0) 6.dp else 4.dp)
                            .background(
                                color = if (pagerState.currentPage == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    androidx.compose.material3.Text(
                        text = "UTILITIES",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 10.sp,
                            fontWeight = if (pagerState.currentPage == 0) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                            color = if (pagerState.currentPage == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Page 1: Home
                Row(
                    modifier = Modifier
                        .clickable { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("page_tab_home"),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == 1) 6.dp else 4.dp)
                            .background(
                                color = if (pagerState.currentPage == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    androidx.compose.material3.Text(
                        text = "HOME",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 10.sp,
                            fontWeight = if (pagerState.currentPage == 1) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                            color = if (pagerState.currentPage == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Page 2: Apps
                Row(
                    modifier = Modifier
                        .clickable { coroutineScope.launch { pagerState.animateScrollToPage(2) } }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("page_tab_apps"),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == 2) 6.dp else 4.dp)
                            .background(
                                color = if (pagerState.currentPage == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    androidx.compose.material3.Text(
                        text = "APPS",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 10.sp,
                            fontWeight = if (pagerState.currentPage == 2) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                            color = if (pagerState.currentPage == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("launcher_horizontal_pager")
            ) { page ->
                when (page) {
                    0 -> {
                        // Left Panel: Utility & Health Dashboard
                        UtilityDashboard(
                            settings = settings,
                            batteryStatus = batteryStatus,
                            widgetSlots = widgetSlots,
                            appWidgetHost = app.appWidgetHost,
                            topUsageApps = topUsageApps,
                            hasUsagePermission = hasUsagePermission,
                            taskList = taskList,
                            onToggleBatteryWidgetMode = {
                                viewModel.setUseBatteryWidget(!settings.useBatteryWidget)
                            },
                            onPickBatteryWidget = {
                                onStartWidgetPick(AppWidgetHostManager.SLOT_UTILITY_BATTERY)
                            },
                            onRemoveBatteryWidget = {
                                val slot = widgetSlots.find { it.slotKey == AppWidgetHostManager.SLOT_UTILITY_BATTERY }
                                if (slot != null) {
                                    viewModel.removeWidgetSlot(AppWidgetHostManager.SLOT_UTILITY_BATTERY, slot.appWidgetId)
                                }
                            },
                            onRequestUsagePermission = {
                                AppManager.openUsageAccessSettings(context)
                            },
                            onAddTask = { viewModel.addTask(it) },
                            onToggleTask = { viewModel.toggleTask(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onClearCompletedTasks = { viewModel.clearCompletedTasks() },
                            onNavigateToHome = {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            }
                        )
                    }
                    1 -> {
                        // Center Main Home View
                        var totalDragX by remember { mutableFloatStateOf(0f) }
                        var totalDragY by remember { mutableFloatStateOf(0f) }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = {
                                            totalDragX = 0f
                                            totalDragY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            totalDragX += dragAmount.x
                                            totalDragY += dragAmount.y
                                            if (kotlin.math.abs(totalDragX) > 20f || kotlin.math.abs(totalDragY) > 20f) {
                                                change.consume()
                                            }
                                        },
                                        onDragEnd = {
                                            val absX = kotlin.math.abs(totalDragX)
                                            val absY = kotlin.math.abs(totalDragY)

                                            if (absX > absY && absX > 40f) {
                                                if (totalDragX > 0) {
                                                    // Swipe Right -> Go to Utilities (Page 0)
                                                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                                } else {
                                                    // Swipe Left -> Go to App List (Page 2)
                                                    coroutineScope.launch { pagerState.animateScrollToPage(2) }
                                                }
                                            } else if (absY > absX && absY > 40f) {
                                                if (totalDragY < 0) {
                                                    // Swipe Up -> Go to App List (Page 2)
                                                    coroutineScope.launch { pagerState.animateScrollToPage(2) }
                                                } else {
                                                    // Swipe Down -> Expand Notifications Shade
                                                    AppManager.expandNotificationShade(context)
                                                }
                                            }
                                            totalDragX = 0f
                                            totalDragY = 0f
                                        },
                                        onDragCancel = {
                                            totalDragX = 0f
                                            totalDragY = 0f
                                        }
                                    )
                                }
                        ) {
                            // Top Header (Clock, Date, Focus Mode Selector, Weather Widget)
                            ClockHeader(
                                settings = settings,
                                focusProfiles = focusProfiles,
                                activeProfileId = settings.activeProfileId,
                                widgetSlots = widgetSlots,
                                appWidgetHost = app.appWidgetHost,
                                onSelectProfile = { viewModel.selectFocusProfile(it) },
                                onManageProfiles = { isSettingsOpen = true },
                                onPickWeatherWidget = {
                                    onStartWidgetPick(AppWidgetHostManager.SLOT_HEADER_WEATHER)
                                },
                                onRemoveWeatherWidget = {
                                    val slot = widgetSlots.find { it.slotKey == AppWidgetHostManager.SLOT_HEADER_WEATHER }
                                    if (slot != null) {
                                        viewModel.removeWidgetSlot(AppWidgetHostManager.SLOT_HEADER_WEATHER, slot.appWidgetId)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.weight(0.5f))

                            // Center: 5 Focus Mode Text Apps
                            FocusAppList(
                                focusApps = activeFocusApps,
                                clockFont = settings.clockFont,
                                onAppClick = { appItem ->
                                    AppManager.launchApp(context, appItem.packageName, appItem.userHandle)
                                },
                                onAppLongClick = { appItem ->
                                    selectedAppForAction = appItem
                                },
                                onAddSlotClick = { slotIndex ->
                                    appPickerSlotIndex = slotIndex
                                }
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Bottom: 4 Fixed Icons (Phone, Messages, Camera, Settings by default - fully customizable)
                            BottomActionBar(
                                bottomSlots = bottomSlots,
                                clockFont = settings.clockFont,
                                onSlotClick = { slot ->
                                    if (slot.packageName.isNotBlank()) {
                                        AppManager.launchApp(context, slot.packageName)
                                    } else if (slot.defaultType.lowercase() == "settings" || slot.iconName.lowercase() == "settings") {
                                        isSettingsOpen = true
                                    } else {
                                        val success = AppManager.launchDefaultAction(context, slot.defaultType)
                                        if (!success) {
                                            bottomSlotToRemap = slot
                                        }
                                    }
                                },
                                onSlotLongClick = { slot ->
                                    bottomSlotToRemap = slot
                                }
                            )
                        }
                    }
                    2 -> {
                        // Right Panel: App List Drawer
                        AppDrawerSheet(
                            mainApps = filteredApps,
                            privateApps = privateSpaceApps,
                            recentApps = recentApps,
                            clockFont = settings.clockFont,
                            isPrivateSpaceLocked = isOsPrivateSpaceLocked,
                            osPrivateProfileHandle = osPrivateProfileHandle,
                            onAppClick = { appItem ->
                                AppManager.launchApp(context, appItem.packageName, appItem.userHandle)
                            },
                            onAppLongClick = { appItem ->
                                selectedAppForAction = appItem
                            },
                            onTogglePrivateSpace = { handle ->
                                viewModel.toggleOsPrivateSpace(context, handle)
                            },
                            onCloseDrawer = {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            onSearchWeb = { query ->
                                AppManager.launchSearch(context, query, settings.searchEnginePackage)
                            }
                        )
                    }
                }
            }
        }

        // Fullscreen Slide-Up App Drawer
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AppDrawerSheet(
                mainApps = filteredApps,
                privateApps = privateSpaceApps,
                recentApps = recentApps,
                clockFont = settings.clockFont,
                isPrivateSpaceLocked = isOsPrivateSpaceLocked,
                osPrivateProfileHandle = osPrivateProfileHandle,
                onAppClick = { appItem ->
                    isDrawerOpen = false
                    AppManager.launchApp(context, appItem.packageName, appItem.userHandle)
                },
                onAppLongClick = { appItem ->
                    selectedAppForAction = appItem
                },
                onTogglePrivateSpace = { handle ->
                    viewModel.toggleOsPrivateSpace(context, handle)
                },
                onCloseDrawer = { isDrawerOpen = false },
                onSearchWeb = { query ->
                    isDrawerOpen = false
                    AppManager.launchSearch(context, query, settings.searchEnginePackage)
                }
            )
        }

        // App Long Press Action Dialog
        if (selectedAppForAction != null) {
            val targetApp = selectedAppForAction!!
            AppActionDialog(
                appItem = targetApp,
                clockFont = settings.clockFont,
                onPinToFocus = { slotIndex ->
                    val activeProfile = focusProfiles.find { it.id == settings.activeProfileId }
                    if (activeProfile != null) {
                        val updated = when (slotIndex) {
                            0 -> activeProfile.copy(appPackage1 = targetApp.packageName)
                            1 -> activeProfile.copy(appPackage2 = targetApp.packageName)
                            2 -> activeProfile.copy(appPackage3 = targetApp.packageName)
                            3 -> activeProfile.copy(appPackage4 = targetApp.packageName)
                            else -> activeProfile.copy(appPackage5 = targetApp.packageName)
                        }
                        viewModel.saveFocusProfile(updated)
                    }
                },
                onSetAsBottomSlot = { slotIndex ->
                    viewModel.setBottomSlot(slotIndex, targetApp.packageName, targetApp.displayLabel)
                },
                onOpenRename = {
                    appToRename = targetApp
                },
                onOpenLimit = {
                    appToSetLimit = targetApp
                },
                onOpenAppInfo = {
                    AppManager.openAppSettings(context, targetApp.packageName, targetApp.userHandle)
                },
                onUninstall = {
                    AppManager.uninstallApp(context, targetApp.packageName)
                },
                onDismiss = { selectedAppForAction = null }
            )
        }

        // Rename App Dialog
        if (appToRename != null) {
            RenameAppDialog(
                appItem = appToRename!!,
                clockFont = settings.clockFont,
                onSave = { newName ->
                    viewModel.setCustomAppLabel(appToRename!!.packageName, newName)
                    appToRename = null
                },
                onDismiss = { appToRename = null }
            )
        }

        // Set Limit Dialog
        if (appToSetLimit != null) {
            SetLimitDialog(
                appItem = appToSetLimit!!,
                clockFont = settings.clockFont,
                onSaveLimit = { minutes ->
                    viewModel.setAppLimit(appToSetLimit!!.packageName, minutes)
                    appToSetLimit = null
                },
                onRemoveLimit = {
                    viewModel.removeAppLimit(appToSetLimit!!.packageName)
                    appToSetLimit = null
                },
                onDismiss = { appToSetLimit = null }
            )
        }

        // App Picker Dialog (for assigning focus slots 0-4)
        if (appPickerSlotIndex in 0..4) {
            AppPickerDialog(
                title = "Assign Focus Slot 0${appPickerSlotIndex + 1}",
                allApps = enrichedApps,
                clockFont = settings.clockFont,
                onAppSelected = { chosenApp ->
                    val activeProfile = focusProfiles.find { it.id == settings.activeProfileId }
                    if (activeProfile != null) {
                        val updated = when (appPickerSlotIndex) {
                            0 -> activeProfile.copy(appPackage1 = chosenApp.packageName)
                            1 -> activeProfile.copy(appPackage2 = chosenApp.packageName)
                            2 -> activeProfile.copy(appPackage3 = chosenApp.packageName)
                            3 -> activeProfile.copy(appPackage4 = chosenApp.packageName)
                            else -> activeProfile.copy(appPackage5 = chosenApp.packageName)
                        }
                        viewModel.saveFocusProfile(updated)
                    }
                    appPickerSlotIndex = -1
                },
                onDismiss = { appPickerSlotIndex = -1 }
            )
        }

        // Customize Bottom Slot Dialog
        if (bottomSlotToRemap != null) {
            val slot = bottomSlotToRemap!!
            BottomSlotCustomizationDialog(
                slot = slot,
                allApps = enrichedApps,
                clockFont = settings.clockFont,
                onSave = { pkg, label, defType, iconName ->
                    viewModel.setBottomSlot(slot.slotIndex, pkg, label, defType, iconName)
                    bottomSlotToRemap = null
                },
                onReset = {
                    viewModel.resetBottomSlot(slot.slotIndex)
                    bottomSlotToRemap = null
                },
                onDismiss = { bottomSlotToRemap = null }
            )
        }

        // Full Settings Dialog
        if (isSettingsOpen) {
            SettingsDialog(
                settings = settings,
                focusProfiles = focusProfiles,
                allApps = enrichedApps,
                hasUsagePermission = hasUsagePermission,
                onSelectTheme = { viewModel.setTheme(it) },
                onSelectClockFont = { viewModel.setClockFont(it) },
                onToggle24Hour = { viewModel.set24Hour(it) },
                onToggleShowSeconds = { viewModel.setShowSeconds(it) },
                onToggleShowDate = { viewModel.setShowDate(it) },
                onUpdateWallpaperDim = { viewModel.setWallpaperDim(it) },
                onToggleWallpaper = { viewModel.setWallpaperEnabled(it) },
                onOpenDefaultLauncherSettings = { AppManager.openDefaultLauncherSettings(context) },
                onOpenUsageAccessSettings = { AppManager.openUsageAccessSettings(context) },
                onOpenAccessibilitySettings = { AppManager.openAccessibilitySettings(context) },
                onSaveProfile = { viewModel.saveFocusProfile(it) },
                onDeleteProfile = { viewModel.deleteFocusProfile(it) },
                onPickWeatherWidget = {
                    isSettingsOpen = false
                    onStartWidgetPick(AppWidgetHostManager.SLOT_HEADER_WEATHER)
                },
                onPickBatteryWidget = {
                    isSettingsOpen = false
                    onStartWidgetPick(AppWidgetHostManager.SLOT_UTILITY_BATTERY)
                },
                onImportCustomFont = { uri -> viewModel.importCustomFont(uri) },
                onClearCustomFont = { viewModel.clearCustomFont() },
                onToggleApplyFontToAllUI = { viewModel.setApplyFontToAllUI(it) },
                onTriggerBiometricTest = {
                    (context as? FragmentActivity)?.let { activity ->
                        BiometricAuthHelper.authenticate(
                            activity = activity,
                            title = "Vault Biometric Check",
                            subtitle = "Hardware Keystore biometrics test",
                            onSuccess = {
                                Toast.makeText(context, "Biometrics Verified!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { errMsg ->
                                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                onSelectSwipeRightAction = { viewModel.setSwipeRightAction(it) },
                onSelectSwipeLeftAction = { viewModel.setSwipeLeftAction(it) },
                onSelectSwipeRightPackage = { viewModel.setSwipeRightPackage(it) },
                onSelectSwipeLeftPackage = { viewModel.setSwipeLeftPackage(it) },
                onSelectSearchEnginePackage = { viewModel.setSearchEnginePackage(it) },
                onDismiss = { isSettingsOpen = false }
            )
        }
    }
}
