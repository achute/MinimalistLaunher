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
import com.example.ui.components.WidgetPickerDialog
import com.example.ui.theme.MinimalistLauncherTheme
import com.example.ui.viewmodel.LauncherViewModel
import com.example.util.AppManager
import com.example.util.BiometricAuthHelper
import com.example.widget.AppWidgetHostManager
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    companion object {
        private const val REQUEST_BIND_WIDGET = 1001
        private const val REQUEST_CONFIG_WIDGET = 1002
    }

    private val viewModel: LauncherViewModel by viewModels()

    private var pendingWidgetSlotKey: String? = null
    private var pendingAppWidgetId: Int = -1

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

            var showWidgetPicker by remember { mutableStateOf(false) }

            MinimalistLauncherTheme(theme = settings.theme, fontFamily = activeFontFamily) {
                LauncherRootScreen(
                    viewModel = viewModel,
                    onStartWidgetPick = { slotKey ->
                        pendingWidgetSlotKey = slotKey
                        showWidgetPicker = true
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

                if (showWidgetPicker) {
                    WidgetPickerDialog(
                        onDismiss = {
                            showWidgetPicker = false
                            pendingWidgetSlotKey = null
                        },
                        onWidgetSelected = { providerInfo ->
                            showWidgetPicker = false
                            val allocatedId = AppWidgetHostManager.allocateAppWidgetId(appWidgetHost)
                            if (allocatedId != -1) {
                                com.example.widget.WidgetDiagnosticLogger.logBindAttempt(allocatedId, providerInfo)
                                val bound = try {
                                    appWidgetManager.bindAppWidgetIdIfAllowed(allocatedId, providerInfo.provider)
                                } catch (e: Exception) {
                                    android.util.Log.e("WidgetDiagnostic", "Exception during bindAppWidgetIdIfAllowed", e)
                                    false
                                }
                                com.example.widget.WidgetDiagnosticLogger.logBindResult(bound, allocatedId, providerInfo)
                                
                                if (bound) {
                                    pendingAppWidgetId = allocatedId
                                    AppWidgetHostManager.handlePickResult(
                                        context = this@MainActivity,
                                        appWidgetId = allocatedId,
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
                                            AppWidgetHostManager.deleteAppWidgetId(appWidgetHost, allocatedId)
                                            pendingWidgetSlotKey = null
                                        }
                                    )
                                } else {
                                    // Request permission to bind the widget
                                    pendingAppWidgetId = allocatedId
                                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, allocatedId)
                                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider)
                                    }
                                    try {
                                        startActivityForResult(intent, REQUEST_BIND_WIDGET)
                                    } catch (e: Exception) {
                                        com.example.widget.WidgetDiagnosticLogger.logBindIntentLaunchFailure(e)
                                        AppWidgetHostManager.deleteAppWidgetId(appWidgetHost, allocatedId)
                                        Toast.makeText(this@MainActivity, "Failed to bind widget: ${e.message}", Toast.LENGTH_LONG).show()
                                        pendingWidgetSlotKey = null
                                    }
                                }
                            } else {
                                Toast.makeText(this@MainActivity, "Failed to allocate Widget ID", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
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
                            onPickWidget = { slotKey ->
                                onStartWidgetPick(slotKey)
                            },
                            onRemoveWidget = { slotKey, appWidgetId ->
                                viewModel.removeWidgetSlot(slotKey, appWidgetId)
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

                        val executeGestureAction: (String, String) -> Unit = { action, customPkg ->
                            when (action.lowercase().trim()) {
                                "apps", "drawer" -> coroutineScope.launch { pagerState.animateScrollToPage(2) }
                                "utility" -> coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                "home" -> coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                "browser" -> AppManager.launchBrowser(context, preferredPackage = customPkg.ifBlank { settings.searchEnginePackage })
                                "search" -> AppManager.launchSearch(context, preferredPackage = settings.searchEnginePackage)
                                "camera" -> AppManager.launchDefaultAction(context, "camera")
                                "phone", "dialer" -> AppManager.launchDefaultAction(context, "phone")
                                "messages", "sms" -> AppManager.launchDefaultAction(context, "messages")
                                "notifications", "notification" -> AppManager.expandNotificationShade(context)
                                "settings" -> isSettingsOpen = true
                                "custom" -> {
                                    if (customPkg.isNotBlank()) {
                                        AppManager.launchApp(context, customPkg)
                                    } else {
                                        AppManager.launchBrowser(context)
                                    }
                                }
                                "none" -> {}
                                else -> {
                                    if (customPkg.isNotBlank()) {
                                        AppManager.launchApp(context, customPkg)
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(
                                    settings.swipeUpAction,
                                    settings.swipeUpPackage,
                                    settings.swipeDownAction,
                                    settings.swipeDownPackage,
                                    settings.swipeRightAction,
                                    settings.swipeRightPackage,
                                    settings.swipeLeftAction,
                                    settings.swipeLeftPackage
                                ) {
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
                                                    // Swipe Right
                                                    executeGestureAction(settings.swipeRightAction, settings.swipeRightPackage)
                                                } else {
                                                    // Swipe Left
                                                    executeGestureAction(settings.swipeLeftAction, settings.swipeLeftPackage)
                                                }
                                            } else if (absY > absX && absY > 40f) {
                                                if (totalDragY < 0) {
                                                    // Swipe Up
                                                    executeGestureAction(settings.swipeUpAction, settings.swipeUpPackage)
                                                } else {
                                                    // Swipe Down
                                                    executeGestureAction(settings.swipeDownAction, settings.swipeDownPackage)
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
                                isPrivateSpaceLocked = isOsPrivateSpaceLocked,
                                widgetSlots = widgetSlots,
                                appWidgetHost = app.appWidgetHost,
                                onSelectProfile = { viewModel.selectFocusProfile(it) },
                                onManageProfiles = { isSettingsOpen = true },
                                onPickWidget = { slotKey ->
                                    onStartWidgetPick(slotKey)
                                },
                                onRemoveWidget = { slotKey, appWidgetId ->
                                    viewModel.removeWidgetSlot(slotKey, appWidgetId)
                                }
                            )

                            Spacer(modifier = Modifier.weight(0.5f))

                            // Center: 5 Focus Mode Text Apps
                            FocusAppList(
                                focusApps = activeFocusApps,
                                clockFont = settings.clockFont,
                                isPrivateSpaceLocked = isOsPrivateSpaceLocked,
                                onAppClick = { appItem ->
                                    if (appItem.isPrivateProfile && isOsPrivateSpaceLocked) {
                                        val handle = osPrivateProfileHandle ?: appItem.userHandle
                                        if (handle != null) {
                                            viewModel.toggleOsPrivateSpace(context, handle)
                                        } else {
                                            AppManager.launchApp(context, appItem.packageName, appItem.userHandle)
                                        }
                                    } else {
                                        AppManager.launchApp(context, appItem.packageName, appItem.userHandle)
                                    }
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
                        val pkgToStore = if (targetApp.isPrivateProfile) "pvt:${targetApp.packageName}" else targetApp.packageName
                        val updated = when (slotIndex) {
                            0 -> activeProfile.copy(appPackage1 = pkgToStore)
                            1 -> activeProfile.copy(appPackage2 = pkgToStore)
                            2 -> activeProfile.copy(appPackage3 = pkgToStore)
                            3 -> activeProfile.copy(appPackage4 = pkgToStore)
                            else -> activeProfile.copy(appPackage5 = pkgToStore)
                        }.let {
                            if (targetApp.isPrivateProfile) it.copy(requiresPrivateSpace = true) else it
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
                        val pkgToStore = if (chosenApp.isPrivateProfile) "pvt:${chosenApp.packageName}" else chosenApp.packageName
                        val updated = when (appPickerSlotIndex) {
                            0 -> activeProfile.copy(appPackage1 = pkgToStore)
                            1 -> activeProfile.copy(appPackage2 = pkgToStore)
                            2 -> activeProfile.copy(appPackage3 = pkgToStore)
                            3 -> activeProfile.copy(appPackage4 = pkgToStore)
                            else -> activeProfile.copy(appPackage5 = pkgToStore)
                        }.let {
                            if (chosenApp.isPrivateProfile) it.copy(requiresPrivateSpace = true) else it
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
                onToggleShowHomeWidgets = { viewModel.setShowHomeWidgets(it) },
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
                onSelectSwipeUpAction = { viewModel.setSwipeUpAction(it) },
                onSelectSwipeUpPackage = { viewModel.setSwipeUpPackage(it) },
                onSelectSwipeDownAction = { viewModel.setSwipeDownAction(it) },
                onSelectSwipeDownPackage = { viewModel.setSwipeDownPackage(it) },
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
