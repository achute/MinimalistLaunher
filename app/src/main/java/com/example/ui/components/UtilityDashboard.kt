package com.example.ui.components

import android.appwidget.AppWidgetHost
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockFont
import com.example.data.LauncherSettings
import com.example.model.DailyAppUsage
import com.example.model.TaskItem
import com.example.model.WidgetSlot
import com.example.ui.theme.getFontFamily
import com.example.util.BatteryStatus
import com.example.util.UsageStatsHelper
import com.example.widget.AppWidgetHostManager
import com.example.widget.EmbeddedWidgetView

@Composable
fun UtilityDashboard(
    settings: LauncherSettings,
    batteryStatus: BatteryStatus,
    widgetSlots: List<WidgetSlot>,
    appWidgetHost: AppWidgetHost,
    topUsageApps: List<DailyAppUsage>,
    hasUsagePermission: Boolean,
    taskList: List<TaskItem>,
    onToggleBatteryWidgetMode: () -> Unit,
    onPickBatteryWidget: () -> Unit,
    onRemoveBatteryWidget: () -> Unit,
    onRequestUsagePermission: () -> Unit,
    onAddTask: (String) -> Unit,
    onToggleTask: (TaskItem) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onClearCompletedTasks: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val fontFamily = getFontFamily(settings.clockFont)
    var newTaskText by remember { mutableStateOf("") }
    val batterySlot = widgetSlots.find { it.slotKey == AppWidgetHostManager.SLOT_UTILITY_BATTERY }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("utility_dashboard_list"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section 1: Dashboard Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UTILITY & HEALTH",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onNavigateToHome() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("back_to_home_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SWIPE RIGHT → HOME",
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Section 2: Battery Component
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
                    .testTag("battery_section")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (batteryStatus.isCharging) Icons.Outlined.BatteryChargingFull else Icons.Outlined.BatteryFull,
                            contentDescription = "Battery",
                            tint = if (batteryStatus.level <= 20) Color(0xFFFF4444) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BATTERY TELEMETRY",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    // Mode switch (Text Monitor vs Widget)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onToggleBatteryWidgetMode() }
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz,
                            contentDescription = "Toggle Widget/Text Mode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (settings.useBatteryWidget) "WIDGET MODE" else "TEXT STATS",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (settings.useBatteryWidget) {
                    // Third-party Battery AppWidget via AppWidgetHost
                    if (batterySlot != null && batterySlot.appWidgetId != -1) {
                        EmbeddedWidgetView(
                            appWidgetId = batterySlot.appWidgetId,
                            appWidgetHost = appWidgetHost,
                            slotTitle = "Battery Widget",
                            minHeight = 80.dp,
                            onPickWidget = onPickBatteryWidget,
                            onRemoveWidget = onRemoveBatteryWidget
                        )
                    } else {
                        EmbeddedWidgetView(
                            appWidgetId = -1,
                            appWidgetHost = appWidgetHost,
                            slotTitle = "3rd-Party Battery Widget",
                            minHeight = 60.dp,
                            onPickWidget = onPickBatteryWidget,
                            onRemoveWidget = {}
                        )
                    }
                } else {
                    // Built-in Clean Minimalist Text Battery Monitor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${batteryStatus.level}%",
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (batteryStatus.level <= 20) Color(0xFFFF5555) else MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier.testTag("battery_percentage_text")
                            )
                            Text(
                                text = batteryStatus.chargingSource.uppercase(),
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TEMP: ${String.format("%.1f", batteryStatus.temperatureCelsius)}°C",
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            )
                            Text(
                                text = "HEALTH: ${batteryStatus.health.uppercase()}",
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            )
                            Text(
                                text = "VOLT: ${batteryStatus.voltageMv} mV",
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section 3: App Usage Breakdown (Digital Wellbeing)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
                    .testTag("screen_time_section")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.HourglassTop,
                            contentDescription = "Screen Time",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TODAY'S SCREEN TIME",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    if (hasUsagePermission) {
                        val totalDuration = topUsageApps.sumOf { it.usageMillis }
                        Text(
                            text = UsageStatsHelper.formatDuration(totalDuration),
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!hasUsagePermission) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Usage Access permission is required to calculate local on-device screen time breakdown.",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                lineHeight = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onRequestUsagePermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("grant_usage_permission_button")
                        ) {
                            Text(
                                text = "Grant Usage Access",
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                } else if (topUsageApps.isEmpty()) {
                    Text(
                        text = "No recorded app usage yet today. Open apps to see local screen-time telemetry.",
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        topUsageApps.forEach { usage ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = usage.appLabel,
                                        style = TextStyle(
                                            fontFamily = fontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onBackground
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = UsageStatsHelper.formatDuration(usage.usageMillis),
                                        style = TextStyle(
                                            fontFamily = fontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Minimal Progress bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = usage.percentageOfTotal)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: To-Do / Quick Tasks
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
                    .testTag("tasks_section")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OFFLINE FOCUS TASKS",
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    if (taskList.any { it.isDone }) {
                        Text(
                            text = "CLEAR DONE",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .clickable { onClearCompletedTasks() }
                                .padding(4.dp)
                                .testTag("clear_completed_tasks_button")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Inline Add Task Input
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    placeholder = {
                        Text(
                            text = "+ Quick task (Press Enter)",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_task_input"),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newTaskText.isNotBlank()) {
                                onAddTask(newTaskText)
                                newTaskText = ""
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (taskList.isEmpty()) {
                    Text(
                        text = "No pending tasks. Add something to focus on today.",
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        taskList.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onToggleTask(task) }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                                    .testTag("task_item_${task.id}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isDone,
                                    onCheckedChange = { onToggleTask(task) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.size(28.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = task.title,
                                    style = TextStyle(
                                        fontFamily = fontFamily,
                                        fontSize = 13.sp,
                                        color = if (task.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onBackground,
                                        textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = { onDeleteTask(task.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete Task",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
