package com.example.ui.components

import android.appwidget.AppWidgetHost
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockFont
import com.example.data.LauncherSettings
import com.example.model.FocusProfile
import com.example.model.WidgetSlot
import com.example.ui.theme.getFontFamily
import com.example.widget.AppWidgetHostManager
import com.example.widget.EmbeddedWidgetView
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClockHeader(
    settings: LauncherSettings,
    focusProfiles: List<FocusProfile>,
    activeProfileId: Int,
    widgetSlots: List<WidgetSlot>,
    appWidgetHost: AppWidgetHost,
    onSelectProfile: (Int) -> Unit,
    onManageProfiles: () -> Unit,
    onPickWeatherWidget: () -> Unit,
    onRemoveWeatherWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Date()) }

    LaunchedEffect(settings.showSeconds) {
        while (true) {
            currentTime = Date()
            delay(if (settings.showSeconds) 1000L else 5000L)
        }
    }

    val timePattern = when {
        settings.is24Hour && settings.showSeconds -> "HH:mm:ss"
        settings.is24Hour -> "HH:mm"
        settings.showSeconds -> "hh:mm:ss a"
        else -> "hh:mm a"
    }

    val timeString = remember(currentTime, timePattern) {
        SimpleDateFormat(timePattern, Locale.getDefault()).format(currentTime).uppercase()
    }

    val dateString = remember(currentTime) {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(currentTime).uppercase()
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val clockFontFamily = remember(settings.clockFont) {
        com.example.util.FontManager.resolveFontFamily(context, settings.clockFont)
    }
    val activeProfile = focusProfiles.find { it.id == activeProfileId } ?: focusProfiles.firstOrNull()
    val weatherSlot = widgetSlots.find { it.slotKey == AppWidgetHostManager.SLOT_HEADER_WEATHER }
    var profileMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag("clock_header_section"),
        horizontalAlignment = Alignment.Start
    ) {
        // Top row: Date & Active Focus Profile Chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (settings.showDate) {
                Text(
                    text = dateString,
                    style = TextStyle(
                        fontFamily = clockFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("current_date_text")
                )
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Focus Mode Chip
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(
                            0.5.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { profileMenuExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("focus_profile_selector"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeProfile?.name?.uppercase() ?: "FOCUS",
                        style = TextStyle(
                            fontFamily = clockFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = profileMenuExpanded,
                    onDismissRequest = { profileMenuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    focusProfiles.forEach { profile ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = profile.name,
                                    style = TextStyle(
                                        fontFamily = clockFontFamily,
                                        fontWeight = if (profile.id == activeProfileId) FontWeight.Bold else FontWeight.Normal,
                                        color = if (profile.id == activeProfileId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            },
                            onClick = {
                                onSelectProfile(profile.id)
                                profileMenuExpanded = false
                            },
                            modifier = Modifier.testTag("profile_item_${profile.id}")
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Manage Profiles...",
                                    style = TextStyle(
                                        fontFamily = clockFontFamily,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        },
                        onClick = {
                            profileMenuExpanded = false
                            onManageProfiles()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Digital Clock
        Text(
            text = timeString,
            style = TextStyle(
                fontFamily = clockFontFamily,
                fontSize = if (settings.showSeconds) 42.sp else 50.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.testTag("digital_clock_text")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Weather Slot / Embedded 3rd Party Widget
        if (weatherSlot != null && weatherSlot.appWidgetId != -1) {
            EmbeddedWidgetView(
                appWidgetId = weatherSlot.appWidgetId,
                appWidgetHost = appWidgetHost,
                slotTitle = "Weather Widget",
                minHeight = 84.dp,
                onPickWidget = onPickWeatherWidget,
                onRemoveWidget = onRemoveWeatherWidget,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            // Optional minimal weather or widget attach prompt
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onPickWeatherWidget() }
                    .padding(vertical = 10.dp, horizontal = 14.dp)
                    .testTag("weather_widget_slot_prompt"),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "WEATHER: OFFLINE NATIVE WIDGET",
                        style = TextStyle(
                            fontFamily = clockFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = "[+ ATTACH]",
                        style = TextStyle(
                            fontFamily = clockFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}
