package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ClockFont
import com.example.data.LauncherSettings
import com.example.data.LauncherTheme
import com.example.model.AppInfoItem
import com.example.model.BottomSlot
import com.example.model.FocusProfile
import com.example.model.OpenSourceLibrariesData
import com.example.model.OpenSourceLibrary
import com.example.ui.theme.getFontFamily
import com.example.util.AppManager
import com.example.util.FontManager

// 1. App Options / Long Press Action Dialog
@Composable
fun AppActionDialog(
    appItem: AppInfoItem,
    clockFont: ClockFont,
    onPinToFocus: (slotIndex: Int) -> Unit,
    onSetAsBottomSlot: (slotIndex: Int) -> Unit,
    onOpenRename: () -> Unit,
    onOpenLimit: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val fontFamily = remember(clockFont) { FontManager.resolveFontFamily(context, clockFont) }
    var showPinSubmenu by remember { mutableStateOf(false) }
    var showBottomSubmenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().testTag("app_action_dialog")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appItem.displayLabel,
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = appItem.packageName,
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Pin to Focus Profile
                ActionRowItem(
                    icon = Icons.Outlined.PushPin,
                    title = "Pin to Focus Slot",
                    subtitle = "Assign to one of 5 home focus app slots",
                    fontFamily = fontFamily,
                    onClick = { showPinSubmenu = !showPinSubmenu }
                )

                if (showPinSubmenu) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (1..5).forEach { slotNum ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        onPinToFocus(slotNum - 1)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "0$slotNum",
                                    style = TextStyle(
                                        fontFamily = fontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }

                // Set as Bottom Shortcut
                ActionRowItem(
                    icon = Icons.Outlined.Tune,
                    title = "Assign to Bottom Action Bar",
                    subtitle = "Replace Phone, Messages, Camera, or Settings slot",
                    fontFamily = fontFamily,
                    onClick = { showBottomSubmenu = !showBottomSubmenu }
                )

                if (showBottomSubmenu) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("SLOT 1", "SLOT 2", "SLOT 3", "SLOT 4").forEachIndexed { idx, label ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        onSetAsBottomSlot(idx)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        fontFamily = fontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }

                // Rename
                ActionRowItem(
                    icon = Icons.Outlined.Edit,
                    title = "Rename App Label",
                    subtitle = "Set custom minimalist alias",
                    fontFamily = fontFamily,
                    onClick = {
                        onDismiss()
                        onOpenRename()
                    }
                )

                // Limit
                ActionRowItem(
                    icon = Icons.Outlined.HourglassBottom,
                    title = "Set Daily Screen Time Limit",
                    subtitle = if (appItem.dailyLimitMinutes != null && appItem.dailyLimitMinutes > 0) "Current: ${appItem.dailyLimitMinutes} min" else "No quota set",
                    fontFamily = fontFamily,
                    onClick = {
                        onDismiss()
                        onOpenLimit()
                    }
                )

                // App Info
                ActionRowItem(
                    icon = Icons.Outlined.Info,
                    title = "System App Settings",
                    subtitle = "Manage permissions, storage & notifications",
                    fontFamily = fontFamily,
                    onClick = {
                        onDismiss()
                        onOpenAppInfo()
                    }
                )

                // Uninstall
                ActionRowItem(
                    icon = Icons.Outlined.Delete,
                    title = "Uninstall Application",
                    subtitle = "Remove app completely from device",
                    fontFamily = fontFamily,
                    isDestructive = true,
                    onClick = {
                        onDismiss()
                        onUninstall()
                    }
                )
            }
        }
    }
}

@Composable
private fun ActionRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    fontFamily: FontFamily,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) Color(0xFFFF5555) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) Color(0xFFFF5555) else MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )
        }
    }
}

// 2. Rename App Dialog
@Composable
fun RenameAppDialog(
    appItem: AppInfoItem,
    clockFont: ClockFont,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val fontFamily = remember(clockFont) { FontManager.resolveFontFamily(context, clockFont) }
    var aliasText by remember { mutableStateOf(appItem.customLabel ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rename App Label",
                style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Original: ${appItem.label}",
                    style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                )
                OutlinedTextField(
                    value = aliasText,
                    onValueChange = { aliasText = it },
                    placeholder = { Text(appItem.label, style = TextStyle(fontFamily = fontFamily)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("rename_app_input"),
                    textStyle = TextStyle(fontFamily = fontFamily, fontSize = 14.sp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(aliasText) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text("Save", style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = TextStyle(fontFamily = fontFamily))
            }
        }
    )
}

// 3. Set Daily Limit Dialog
@Composable
fun SetLimitDialog(
    appItem: AppInfoItem,
    clockFont: ClockFont,
    onSaveLimit: (minutes: Int) -> Unit,
    onRemoveLimit: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val fontFamily = remember(clockFont) { FontManager.resolveFontFamily(context, clockFont) }
    var minutesText by remember { mutableStateOf((appItem.dailyLimitMinutes ?: 30).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Daily Screen Time Limit",
                style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Set maximum minutes allowed per day for ${appItem.displayLabel}:",
                    style = TextStyle(fontFamily = fontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                )

                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) minutesText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("app_limit_input"),
                    trailingIcon = { Text("min", style = TextStyle(fontFamily = fontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)) },
                    textStyle = TextStyle(fontFamily = fontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { minutesText = mins.toString() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("${mins}m", style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mins = minutesText.toIntOrNull() ?: 0
                    onSaveLimit(mins)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text("Set Quota", style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            if (appItem.dailyLimitMinutes != null && appItem.dailyLimitMinutes > 0) {
                TextButton(onClick = onRemoveLimit) {
                    Text("Remove Quota", style = TextStyle(fontFamily = fontFamily, color = Color(0xFFFF5555)))
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", style = TextStyle(fontFamily = fontFamily))
                }
            }
        }
    )
}

// 4. Generic App Picker Dialog
@Composable
fun AppPickerDialog(
    title: String,
    allApps: List<AppInfoItem>,
    clockFont: ClockFont,
    onAppSelected: (AppInfoItem) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val fontFamily = remember(clockFont) { FontManager.resolveFontFamily(context, clockFont) }
    var query by remember { mutableStateOf("") }

    val filtered = remember(allApps, query) {
        if (query.isBlank()) allApps
        else allApps.filter { it.displayLabel.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).testTag("app_picker_dialog")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title.uppercase(),
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Filter apps...", style = TextStyle(fontFamily = fontFamily, fontSize = 12.sp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("app_picker_search"),
                    textStyle = TextStyle(fontFamily = fontFamily, fontSize = 13.sp),
                    shape = RoundedCornerShape(8.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered, key = { "${it.packageName}_${it.userHandle?.hashCode() ?: 0}" }) { app ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onAppSelected(app) }
                                .padding(vertical = 10.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = app.displayLabel,
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// 5. Full Settings Dialog with 5 Tabs: Theme & Fonts, Gestures & Shortcuts, Focus Profiles, Privacy & Biometrics, Open Source Licenses
@Composable
fun SettingsDialog(
    settings: LauncherSettings,
    focusProfiles: List<FocusProfile>,
    allApps: List<AppInfoItem>,
    hasUsagePermission: Boolean,
    onSelectTheme: (LauncherTheme) -> Unit,
    onSelectClockFont: (ClockFont) -> Unit,
    onToggle24Hour: (Boolean) -> Unit,
    onToggleShowSeconds: (Boolean) -> Unit,
    onToggleShowDate: (Boolean) -> Unit,
    onUpdateWallpaperDim: (Float) -> Unit,
    onToggleWallpaper: (Boolean) -> Unit,
    onOpenDefaultLauncherSettings: () -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onSaveProfile: (FocusProfile) -> Unit,
    onDeleteProfile: (Int) -> Unit,
    onPickWeatherWidget: () -> Unit,
    onPickBatteryWidget: () -> Unit,
    onImportCustomFont: (Uri) -> Result<String>,
    onClearCustomFont: () -> Unit,
    onToggleApplyFontToAllUI: (Boolean) -> Unit,
    onTriggerBiometricTest: () -> Unit,
    onSelectSwipeUpAction: (String) -> Unit = {},
    onSelectSwipeUpPackage: (String) -> Unit = {},
    onSelectSwipeDownAction: (String) -> Unit = {},
    onSelectSwipeDownPackage: (String) -> Unit = {},
    onSelectSwipeRightAction: (String) -> Unit = {},
    onSelectSwipeLeftAction: (String) -> Unit = {},
    onSelectSwipeRightPackage: (String) -> Unit = {},
    onSelectSwipeLeftPackage: (String) -> Unit = {},
    onSelectSearchEnginePackage: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val fontFamily = remember(settings.clockFont) { FontManager.resolveFontFamily(context, settings.clockFont) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Themes & Fonts, 1: Gestures, 2: Focus Profiles, 3: Privacy & Biometric, 4: 3rd Party Licenses
    var editingProfile by remember { mutableStateOf<FocusProfile?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("launcher_settings_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LAUNCHER PREFERENCES",
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary,
                                height = 2.dp
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("THEME & FONTS", style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("GESTURES", style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("FOCUS PROFILES", style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("PRIVACY & VAULT", style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)) }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text("3RD PARTY NOTICES", style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> AppearanceAndFontsTab(
                            settings = settings,
                            fontFamily = fontFamily,
                            onSelectTheme = onSelectTheme,
                            onSelectClockFont = onSelectClockFont,
                            onToggle24Hour = onToggle24Hour,
                            onToggleShowSeconds = onToggleShowSeconds,
                            onToggleShowDate = onToggleShowDate,
                            onUpdateWallpaperDim = onUpdateWallpaperDim,
                            onToggleWallpaper = onToggleWallpaper,
                            onImportCustomFont = onImportCustomFont,
                            onClearCustomFont = onClearCustomFont,
                            onToggleApplyFontToAllUI = onToggleApplyFontToAllUI
                        )
                        1 -> GesturesAndShortcutsTab(
                            settings = settings,
                            allApps = allApps,
                            fontFamily = fontFamily,
                            onSelectSwipeUpAction = onSelectSwipeUpAction,
                            onSelectSwipeUpPackage = onSelectSwipeUpPackage,
                            onSelectSwipeDownAction = onSelectSwipeDownAction,
                            onSelectSwipeDownPackage = onSelectSwipeDownPackage,
                            onSelectSwipeRightAction = onSelectSwipeRightAction,
                            onSelectSwipeLeftAction = onSelectSwipeLeftAction,
                            onSelectSwipeRightPackage = onSelectSwipeRightPackage,
                            onSelectSwipeLeftPackage = onSelectSwipeLeftPackage,
                            onSelectSearchEnginePackage = onSelectSearchEnginePackage
                        )
                        2 -> FocusProfilesTab(
                            profiles = focusProfiles,
                            activeProfileId = settings.activeProfileId,
                            allApps = allApps,
                            fontFamily = fontFamily,
                            onEditProfile = { editingProfile = it },
                            onDeleteProfile = onDeleteProfile,
                            onNewProfile = {
                                editingProfile = FocusProfile(id = 0, name = "New Focus", isDndLinked = false, lockPrivateSpace = true)
                            }
                        )
                        3 -> PrivacyAndVaultTab(
                            hasUsagePermission = hasUsagePermission,
                            fontFamily = fontFamily,
                            onOpenDefaultLauncherSettings = onOpenDefaultLauncherSettings,
                            onOpenUsageAccessSettings = onOpenUsageAccessSettings,
                            onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                            onTriggerBiometricTest = onTriggerBiometricTest
                        )
                        4 -> ThirdPartyLibrariesTab(
                            fontFamily = fontFamily
                        )
                    }
                }
            }
        }
    }

    if (editingProfile != null) {
        FocusProfileEditDialog(
            profile = editingProfile!!,
            allApps = allApps,
            fontFamily = fontFamily,
            onSave = {
                onSaveProfile(it)
                editingProfile = null
            },
            onDismiss = { editingProfile = null }
        )
    }
}

// 5.5 Gestures & Shortcuts Tab
@Composable
private fun GesturesAndShortcutsTab(
    settings: LauncherSettings,
    allApps: List<AppInfoItem>,
    fontFamily: FontFamily,
    onSelectSwipeUpAction: (String) -> Unit,
    onSelectSwipeUpPackage: (String) -> Unit,
    onSelectSwipeDownAction: (String) -> Unit,
    onSelectSwipeDownPackage: (String) -> Unit,
    onSelectSwipeRightAction: (String) -> Unit,
    onSelectSwipeLeftAction: (String) -> Unit,
    onSelectSwipeRightPackage: (String) -> Unit,
    onSelectSwipeLeftPackage: (String) -> Unit,
    onSelectSearchEnginePackage: (String) -> Unit
) {
    var pickingAppForGesture by remember { mutableStateOf<String?>(null) } // "up", "down", "right", "left", "search"

    val gestureOptions = listOf(
        Triple("apps", "All Applications", "Navigate directly to full applications drawer list"),
        Triple("browser", "Web Browser", "Instantly open Google Chrome or default web browser"),
        Triple("utility", "Utility & Health", "Quick battery diagnostics, task checklist & screen time"),
        Triple("search", "Web Search", "Launch web search or device assistant"),
        Triple("notifications", "Notification Shade", "Expand Android system notifications & quick settings"),
        Triple("camera", "Camera", "Launch camera for quick photo capture"),
        Triple("phone", "Phone / Dialer", "Open default phone dialer keypad"),
        Triple("messages", "Messages / SMS", "Open default messaging app"),
        Triple("settings", "Launcher Settings", "Open launcher configuration preferences"),
        Triple("custom", "Custom Installed App", "Select any installed app on your device"),
        Triple("none", "Disabled", "Do nothing on this swipe gesture")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "HOME SCREEN SWIPE GESTURES",
            style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
        )

        Text(
            text = "Customize the action performed when swiping in each direction on the main home screen.",
            style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        )

        // 1. Swipe Up Gesture (Configurable - default: Apps, or Browser/Search/etc.)
        GestureSectionCard(
            title = "↑  SWIPE UP GESTURE",
            currentAction = settings.swipeUpAction,
            customPackage = settings.swipeUpPackage,
            options = gestureOptions,
            fontFamily = fontFamily,
            onSelectAction = { action ->
                onSelectSwipeUpAction(action)
                if (action == "custom" && settings.swipeUpPackage.isBlank()) {
                    pickingAppForGesture = "up"
                }
            },
            onPickApp = { pickingAppForGesture = "up" }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // 2. Swipe Down Gesture (Configurable - default: Notification Shade, or Browser/Apps/etc.)
        GestureSectionCard(
            title = "↓  SWIPE DOWN GESTURE",
            currentAction = settings.swipeDownAction,
            customPackage = settings.swipeDownPackage,
            options = gestureOptions,
            fontFamily = fontFamily,
            onSelectAction = { action ->
                onSelectSwipeDownAction(action)
                if (action == "custom" && settings.swipeDownPackage.isBlank()) {
                    pickingAppForGesture = "down"
                }
            },
            onPickApp = { pickingAppForGesture = "down" }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // 3. Swipe Right Gesture (Configurable - default: Utility page 0, or Browser/etc.)
        GestureSectionCard(
            title = "→  SWIPE RIGHT GESTURE",
            currentAction = settings.swipeRightAction,
            customPackage = settings.swipeRightPackage,
            options = gestureOptions,
            fontFamily = fontFamily,
            onSelectAction = { action ->
                onSelectSwipeRightAction(action)
                if (action == "custom" && settings.swipeRightPackage.isBlank()) {
                    pickingAppForGesture = "right"
                }
            },
            onPickApp = { pickingAppForGesture = "right" }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // 4. Swipe Left Gesture (Configurable - default: Apps page 2, or Utility/Browser/etc.)
        GestureSectionCard(
            title = "←  SWIPE LEFT GESTURE",
            currentAction = settings.swipeLeftAction,
            customPackage = settings.swipeLeftPackage,
            options = gestureOptions,
            fontFamily = fontFamily,
            onSelectAction = { action ->
                onSelectSwipeLeftAction(action)
                if (action == "custom" && settings.swipeLeftPackage.isBlank()) {
                    pickingAppForGesture = "left"
                }
            },
            onPickApp = { pickingAppForGesture = "left" }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Default Web Browser / Engine Preference
        Text(
            text = "PREFERRED WEB BROWSER / SEARCH APP",
            style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (settings.searchEnginePackage.isBlank()) "System Default Browser" else settings.searchEnginePackage.substringAfterLast('.'),
                    style = TextStyle(fontFamily = fontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                )
                Text(
                    text = "Used for web browser and web search gesture actions",
                    style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                )
            }

            Text(
                text = if (settings.searchEnginePackage.isBlank()) "[SELECT]" else "[CHANGE]",
                style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .clickable { pickingAppForGesture = "search" }
                    .padding(6.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (pickingAppForGesture != null) {
        val title = when (pickingAppForGesture) {
            "up" -> "Select App for Swipe Up"
            "down" -> "Select App for Swipe Down"
            "right" -> "Select App for Swipe Right"
            "left" -> "Select App for Swipe Left"
            else -> "Select Preferred Browser / Search App"
        }
        AppPickerDialog(
            title = title,
            allApps = allApps,
            clockFont = settings.clockFont,
            onAppSelected = { app ->
                when (pickingAppForGesture) {
                    "up" -> {
                        onSelectSwipeUpAction("custom")
                        onSelectSwipeUpPackage(app.packageName)
                    }
                    "down" -> {
                        onSelectSwipeDownAction("custom")
                        onSelectSwipeDownPackage(app.packageName)
                    }
                    "right" -> {
                        onSelectSwipeRightAction("custom")
                        onSelectSwipeRightPackage(app.packageName)
                    }
                    "left" -> {
                        onSelectSwipeLeftAction("custom")
                        onSelectSwipeLeftPackage(app.packageName)
                    }
                    "search" -> onSelectSearchEnginePackage(app.packageName)
                }
                pickingAppForGesture = null
            },
            onDismiss = { pickingAppForGesture = null }
        )
    }
}

@Composable
private fun GestureSectionCard(
    title: String,
    currentAction: String,
    customPackage: String,
    options: List<Triple<String, String, String>>,
    fontFamily: FontFamily,
    onSelectAction: (String) -> Unit,
    onPickApp: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val currentOption = options.find { it.first.equals(currentAction, ignoreCase = true) } ?: options.first()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(fontFamily = fontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = MaterialTheme.colorScheme.primary)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Action: ${currentOption.second}",
                        style = TextStyle(fontFamily = fontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    )
                    if (currentAction == "custom" && customPackage.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[${customPackage.substringAfterLast('.')}]",
                            style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }

            Text(
                text = if (isExpanded) "[COLLAPSE]" else "[CHANGE]",
                style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { (actionKey, optTitle, desc) ->
                    val isSelected = currentAction.equals(actionKey, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                            .border(
                                0.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                onSelectAction(actionKey)
                            }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = optTitle,
                                    style = TextStyle(
                                        fontFamily = fontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                if (actionKey == "custom" && isSelected && customPackage.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "[${customPackage.substringAfterLast('.')}]",
                                        style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                            Text(
                                text = desc,
                                style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            )
                        }

                        if (actionKey == "custom" && isSelected) {
                            Text(
                                text = if (customPackage.isBlank()) "[CHOOSE]" else "[CHANGE]",
                                style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .clickable { onPickApp() }
                                    .padding(4.dp)
                            )
                        } else if (isSelected) {
                            Icon(imageVector = Icons.Outlined.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// 6. Appearance, Fonts & Wallpaper Tab
@Composable
private fun AppearanceAndFontsTab(
    settings: LauncherSettings,
    fontFamily: FontFamily,
    onSelectTheme: (LauncherTheme) -> Unit,
    onSelectClockFont: (ClockFont) -> Unit,
    onToggle24Hour: (Boolean) -> Unit,
    onToggleShowSeconds: (Boolean) -> Unit,
    onToggleShowDate: (Boolean) -> Unit,
    onUpdateWallpaperDim: (Float) -> Unit,
    onToggleWallpaper: (Boolean) -> Unit,
    onImportCustomFont: (Uri) -> Result<String>,
    onClearCustomFont: () -> Unit,
    onToggleApplyFontToAllUI: (Boolean) -> Unit
) {
    val context = LocalContext.current

    // SAF Document Picker for TTF / OTF
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = onImportCustomFont(uri)
            if (result.isSuccess) {
                Toast.makeText(context, "Loaded font: ${result.getOrNull()}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Font import failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Theme Selector
        Text(
            text = "MINIMAL PALETTE PRESETS",
            style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LauncherTheme.entries.forEach { theme ->
                val isSelected = theme == settings.theme
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(
                            0.5.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectTheme(theme) }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = theme.title,
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = theme.description,
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }

                    if (isSelected) {
                        Icon(imageVector = Icons.Outlined.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Typography Font Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TYPOGRAPHY & CLOCK FONTS",
                style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
            )

            Text(
                text = "AOSP System / TTF",
                style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            )
        }

        // Font Preset Chips Grid
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val presets = listOf(
                ClockFont.RETRO_MONO,
                ClockFont.CLEAN_SANS,
                ClockFont.ELEGANT_SERIF,
                ClockFont.DIGITAL_MATRIX,
                ClockFont.TERMINAL_VT100,
                ClockFont.RETRO_TYPEWRITER
            )

            presets.chunked(2).forEach { rowFonts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowFonts.forEach { font ->
                        val isSelected = font == settings.clockFont
                        val previewFontFamily = remember(font) { FontManager.resolveFontFamily(context, font) }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(
                                    0.5.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectClockFont(font) }
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = font.displayName,
                                    style = TextStyle(
                                        fontFamily = previewFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "12:45 PM • Mon, Oct 24",
                                    style = TextStyle(
                                        fontFamily = previewFontFamily,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Font (TTF/OTF) from Local Storage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (settings.clockFont == ClockFont.CUSTOM_FILE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(
                    1.dp,
                    if (settings.clockFont == ClockFont.CUSTOM_FILE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.UploadFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CUSTOM LOCAL FONT (TTF/OTF)",
                            style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    }

                    if (FontManager.hasCustomFont(context)) {
                        Text(
                            text = "[LOADED]",
                            style = TextStyle(fontFamily = fontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
                        )
                    }
                }

                Text(
                    text = if (settings.customFontDisplayName.isNotBlank()) {
                        "Active font file: ${settings.customFontDisplayName}"
                    } else {
                        "Load any license-compliant TrueType (.ttf) or OpenType (.otf) font file directly from your device storage."
                    },
                    style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                fontPickerLauncher.launch(
                                    arrayOf(
                                        "font/ttf",
                                        "font/otf",
                                        "font/*",
                                        "application/x-font-ttf",
                                        "application/x-font-otf",
                                        "application/font-sfnt",
                                        "application/octet-stream",
                                        "*/*"
                                    )
                                )
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (FontManager.hasCustomFont(context)) "Choose Different File" else "Browse TTF/OTF...",
                            style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        )
                    }

                    if (FontManager.hasCustomFont(context)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .clickable {
                                    if (settings.clockFont != ClockFont.CUSTOM_FILE) {
                                        onSelectClockFont(ClockFont.CUSTOM_FILE)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (settings.clockFont == ClockFont.CUSTOM_FILE) "Active" else "Use This",
                                style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }

                        IconButton(onClick = onClearCustomFont, modifier = Modifier.size(34.dp)) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Clear Font", tint = Color(0xFFFF5555), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Time & Display Options
        Text(
            text = "TIME & HEADER FORMAT",
            style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingSwitchRow("24-Hour Military Clock", settings.is24Hour, fontFamily, onToggle24Hour)
            SettingSwitchRow("Display Live Seconds Counter", settings.showSeconds, fontFamily, onToggleShowSeconds)
            SettingSwitchRow("Display Current Date Header", settings.showDate, fontFamily, onToggleShowDate)
            SettingSwitchRow("Apply Font to Entire Launcher UI", settings.applyFontToAllUI, fontFamily, onToggleApplyFontToAllUI)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Wallpaper Toggle & Controls
        Text(
            text = "WALLPAPER DISPLAY & CONTROLS",
            style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SettingSwitchRow(
                title = "Show System Device Wallpaper",
                checked = settings.isWallpaperEnabled,
                fontFamily = fontFamily,
                onCheckedChange = onToggleWallpaper
            )

            if (settings.isWallpaperEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Wallpaper Scrim Dimming",
                            style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(
                            text = "${(settings.wallpaperDim * 100).toInt()}%",
                            style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    }

                    Slider(
                        value = settings.wallpaperDim,
                        onValueChange = onUpdateWallpaperDim,
                        valueRange = 0.2f..0.98f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Text(
                        text = "Dimming ensures text readability against bright wallpaper backgrounds.",
                        style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    )
                }
            }

            // Button to open system wallpaper picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .clickable { AppManager.openWallpaperPicker(context) }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Wallpaper, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Change System Wallpaper",
                                style = TextStyle(fontFamily = fontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            )
                            Text(
                                text = "Launch default Android wallpaper selector",
                                style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            )
                        }
                    }

                    Text(
                        text = "[OPEN]",
                        style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    fontFamily: FontFamily,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(fontFamily = fontFamily, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

// 7. Focus Profiles Management Tab
@Composable
private fun FocusProfilesTab(
    profiles: List<FocusProfile>,
    activeProfileId: Int,
    allApps: List<AppInfoItem>,
    fontFamily: FontFamily,
    onEditProfile: (FocusProfile) -> Unit,
    onDeleteProfile: (Int) -> Unit,
    onNewProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CONFIGURED FOCUS PROFILES",
                style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
            )

            Text(
                text = "+ ADD PROFILE",
                style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.clickable { onNewProfile() }.padding(4.dp)
            )
        }

        profiles.forEach { profile ->
            val isCurrent = profile.id == activeProfileId
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(0.5.dp, if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name.uppercase(),
                            style = TextStyle(fontFamily = fontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        )
                        if (isCurrent) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "[ACTIVE]",
                                style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { onEditProfile(profile) }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        }
                        if (profiles.size > 1) {
                            IconButton(onClick = { onDeleteProfile(profile.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", tint = Color(0xFFFF5555), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                val pkgs = profile.getPackages()
                Text(
                    text = if (pkgs.isEmpty()) "No apps assigned yet" else "${pkgs.size} apps: " + pkgs.joinToString(", ") { it.substringAfterLast('.') },
                    style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                )

                if (profile.lockPrivateSpace) {
                    Text(
                        text = "• Private Space auto-locked during this mode",
                        style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}

// 8. Focus Profile Edit Dialog
@Composable
private fun FocusProfileEditDialog(
    profile: FocusProfile,
    allApps: List<AppInfoItem>,
    fontFamily: FontFamily,
    onSave: (FocusProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var isDndLinked by remember { mutableStateOf(profile.isDndLinked) }
    var lockPrivateSpace by remember { mutableStateOf(profile.lockPrivateSpace) }
    var app1 by remember { mutableStateOf(profile.appPackage1) }
    var app2 by remember { mutableStateOf(profile.appPackage2) }
    var app3 by remember { mutableStateOf(profile.appPackage3) }
    var app4 by remember { mutableStateOf(profile.appPackage4) }
    var app5 by remember { mutableStateOf(profile.appPackage5) }

    var pickingSlotIndex by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Focus Profile", style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name", style = TextStyle(fontFamily = fontFamily)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Link with System DND", style = TextStyle(fontFamily = fontFamily, fontSize = 12.sp))
                    Switch(checked = isDndLinked, onCheckedChange = { isDndLinked = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Auto-Lock Private Space", style = TextStyle(fontFamily = fontFamily, fontSize = 12.sp))
                    Switch(checked = lockPrivateSpace, onCheckedChange = { lockPrivateSpace = it })
                }

                Text("ASSIGN 5 FOCUS APPS:", style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))

                listOf(
                    Pair(1, app1) to { pkg: String -> app1 = pkg },
                    Pair(2, app2) to { pkg: String -> app2 = pkg },
                    Pair(3, app3) to { pkg: String -> app3 = pkg },
                    Pair(4, app4) to { pkg: String -> app4 = pkg },
                    Pair(5, app5) to { pkg: String -> app5 = pkg }
                ).forEach { (slotData, setter) ->
                    val (num, pkg) = slotData
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { pickingSlotIndex = num }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Slot 0$num: ${if (pkg.isBlank()) "[Tap to select]" else pkg.substringAfterLast('.')}",
                            style = TextStyle(fontFamily = fontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        )
                        if (pkg.isNotBlank()) {
                            IconButton(onClick = { setter("") }, modifier = Modifier.size(20.dp)) {
                                Icon(imageVector = Icons.Outlined.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        profile.copy(
                            name = name.ifBlank { "Focus Profile" },
                            isDndLinked = isDndLinked,
                            lockPrivateSpace = lockPrivateSpace,
                            appPackage1 = app1,
                            appPackage2 = app2,
                            appPackage3 = app3,
                            appPackage4 = app4,
                            appPackage5 = app5
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text("Save Profile", style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", style = TextStyle(fontFamily = fontFamily)) }
        }
    )

    if (pickingSlotIndex != null) {
        AppPickerDialog(
            title = "Assign Slot 0${pickingSlotIndex}",
            allApps = allApps,
            clockFont = ClockFont.RETRO_MONO,
            onAppSelected = { app ->
                when (pickingSlotIndex) {
                    1 -> app1 = app.packageName
                    2 -> app2 = app.packageName
                    3 -> app3 = app.packageName
                    4 -> app4 = app.packageName
                    5 -> app5 = app.packageName
                }
                pickingSlotIndex = null
            },
            onDismiss = { pickingSlotIndex = null }
        )
    }
}

// 9. Privacy, Biometrics & Encrypted Room Vault Tab
@Composable
private fun PrivacyAndVaultTab(
    hasUsagePermission: Boolean,
    fontFamily: FontFamily,
    onOpenDefaultLauncherSettings: () -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onTriggerBiometricTest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Zero-Network Audit Certificate
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF00240E))
                .border(1.dp, Color(0xFF00FF66), RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = Color(0xFF00FF66),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% ZERO-INTERNET AUDITED",
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FF66)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "android.permission.INTERNET is strictly omitted from AndroidManifest.xml. Zero network sockets, zero remote telemetry, zero analytics.",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 15.sp
                    )
                )
            }
        }

        // Biometrics & AES-256 GCM Storage Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENCRYPTED PRIVATE SPACE VAULT",
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Text(
                    text = "Hidden app package identifiers are encrypted with AES-256 GCM using keys stored in the Android KeyStore before being written to the Room Database. Physical database dumps cannot reveal your hidden app list.",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 15.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onTriggerBiometricTest() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Test Biometric Prompt (Fingerprint / PIN)",
                        style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    )
                }
            }
        }

        Text(
            text = "SYSTEM PERMISSIONS & SHORTCUTS",
            style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
        )

        // Default Launcher
        SystemActionCard(
            title = "Set as Default Home Launcher",
            subtitle = "Enable this launcher as your primary Android home screen",
            buttonLabel = "Change Default",
            fontFamily = fontFamily,
            onClick = onOpenDefaultLauncherSettings
        )

        // Usage Access
        SystemActionCard(
            title = "Usage Access Telemetry",
            subtitle = if (hasUsagePermission) "Granted — On-device screen time active" else "Not Granted — Required for screen time breakdown",
            buttonLabel = if (hasUsagePermission) "Manage Access" else "Grant Access",
            fontFamily = fontFamily,
            onClick = onOpenUsageAccessSettings
        )

        // Accessibility Service for App Blocker
        SystemActionCard(
            title = "Accessibility Service (App Blocker)",
            subtitle = "Required to detect app launches and enforce digital wellbeing screen limits",
            buttonLabel = "Open Accessibility",
            fontFamily = fontFamily,
            onClick = onOpenAccessibilitySettings
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// 10. Third Party Libraries & Open Source Licenses Tab
@Composable
private fun ThirdPartyLibrariesTab(
    fontFamily: FontFamily
) {
    val libraries = OpenSourceLibrariesData.libraries
    var expandedLibraryName by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OPEN-SOURCE LICENSES & 3RD PARTY NOTICES",
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        Text(
            text = "This launcher is built on standard open-source libraries. All font families and UI components comply strictly with open-source licensing without proprietary restrictions.",
            style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        )

        libraries.forEach { lib ->
            val isExpanded = expandedLibraryName == lib.name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .clickable { expandedLibraryName = if (isExpanded) null else lib.name }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lib.name,
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "${lib.author} • ${lib.licenseType}",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Text(
                        text = if (isExpanded) "[HIDE]" else "[VIEW]",
                        style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lib.description,
                    style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                )

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "VERSION: ${lib.version}\nURL: ${lib.projectUrl}",
                        style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = lib.licenseNotice,
                            style = TextStyle(fontFamily = fontFamily, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SystemActionCard(
    title: String,
    subtitle: String,
    buttonLabel: String,
    fontFamily: FontFamily,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(fontFamily = fontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            )
            Text(
                text = subtitle,
                style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onClick() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = buttonLabel,
                style = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            )
        }
    }
}

// 11. Bottom Slot Customization Dialog (Allow customizing 4 bottom icons, custom text & icons)
@Composable
fun BottomSlotCustomizationDialog(
    slot: BottomSlot,
    allApps: List<AppInfoItem>,
    clockFont: ClockFont,
    onSave: (packageName: String, customLabel: String, defaultType: String, iconName: String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val fontFamily = remember(clockFont) { FontManager.resolveFontFamily(context, clockFont) }

    var selectedDefaultType by remember { mutableStateOf(slot.defaultType) }
    var selectedPackageName by remember { mutableStateOf(slot.packageName) }
    var customLabelText by remember { mutableStateOf(slot.customLabel.ifBlank { slot.defaultType.replaceFirstChar { it.uppercase() } }) }
    var selectedIconName by remember { mutableStateOf(slot.iconName.ifBlank { slot.defaultType }) }
    var showAppPicker by remember { mutableStateOf(false) }

    val defaultActions = listOf(
        Triple("phone", "Phone", "phone"),
        Triple("messages", "Messages", "messages"),
        Triple("camera", "Camera", "camera"),
        Triple("settings", "Settings", "settings"),
        Triple("browser", "Browser", "browser"),
        Triple("search", "Search", "search")
    )

    val availableIcons = listOf(
        "phone", "messages", "camera", "settings", "browser", "search",
        "mail", "music", "star", "heart", "folder", "terminal",
        "apps", "clock", "navigation", "bookmark"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Customize Bottom Slot 0${slot.slotIndex + 1}",
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Preset Default Action Shortcuts
                Text(
                    text = "ACTION OR APP TARGET",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    defaultActions.forEach { (type, label, icon) ->
                        val isSelected = selectedDefaultType == type && selectedPackageName.isBlank()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(
                                    0.5.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedDefaultType = type
                                    selectedPackageName = ""
                                    customLabelText = label
                                    selectedIconName = icon
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = resolveBottomSlotIcon(icon),
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Default $label Action",
                                    style = TextStyle(
                                        fontFamily = fontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Choose Specific Installed App
                    val isCustomApp = selectedPackageName.isNotBlank()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCustomApp) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .border(
                                0.5.dp,
                                if (isCustomApp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { showAppPicker = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = resolveBottomSlotIcon(selectedIconName),
                                contentDescription = null,
                                tint = if (isCustomApp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isCustomApp) "App: ${selectedPackageName.substringAfterLast('.')}" else "Pick Installed Application...",
                                    style = TextStyle(
                                        fontFamily = fontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = if (isCustomApp) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCustomApp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                if (isCustomApp) {
                                    Text(
                                        text = selectedPackageName,
                                        style = TextStyle(
                                            fontFamily = fontFamily,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isCustomApp) "[CHANGE]" else "[BROWSE]",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Custom Text Label Input
                Text(
                    text = "CUSTOM DISPLAY LABEL",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = customLabelText,
                    onValueChange = { customLabelText = it },
                    label = { Text("Display Name / Accessibility Label", style = TextStyle(fontFamily = fontFamily)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = fontFamily, fontSize = 13.sp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Custom Icon Selector
                Text(
                    text = "SELECT ICON SYMBOL",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    availableIcons.chunked(4).forEach { rowIcons ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowIcons.forEach { iconKey ->
                                val isSelected = selectedIconName == iconKey
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(
                                            0.5.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedIconName = iconKey },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = resolveBottomSlotIcon(iconKey),
                                        contentDescription = iconKey,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        selectedPackageName,
                        customLabelText.trim(),
                        selectedDefaultType,
                        selectedIconName
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save", style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onReset) {
                    Text("Reset Default", style = TextStyle(fontFamily = fontFamily, color = Color(0xFFFF5555)))
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", style = TextStyle(fontFamily = fontFamily))
                }
            }
        }
    )

    if (showAppPicker) {
        AppPickerDialog(
            title = "Select App for Slot 0${slot.slotIndex + 1}",
            allApps = allApps,
            clockFont = clockFont,
            onAppSelected = { app ->
                selectedPackageName = app.packageName
                selectedDefaultType = "custom"
                customLabelText = app.displayLabel
                selectedIconName = "apps"
                showAppPicker = false
            },
            onDismiss = { showAppPicker = false }
        )
    }
}
