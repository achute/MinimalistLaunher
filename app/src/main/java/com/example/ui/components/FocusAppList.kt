package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockFont
import com.example.model.AppInfoItem
import com.example.model.FocusProfile
import com.example.ui.theme.getFontFamily

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusAppList(
    focusApps: List<AppInfoItem>,
    clockFont: ClockFont,
    isPrivateSpaceLocked: Boolean = false,
    onAppClick: (AppInfoItem) -> Unit,
    onAppLongClick: (AppInfoItem) -> Unit,
    onAddSlotClick: (slotIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val fontFamily = androidx.compose.runtime.remember(clockFont) {
        com.example.util.FontManager.resolveFontFamily(context, clockFont)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .testTag("focus_app_list_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        for (i in 0 until 5) {
            val appItem = focusApps.getOrNull(i)
            if (appItem != null) {
                // Focus App Text Item
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onAppClick(appItem) },
                            onLongClick = { onAppLongClick(appItem) }
                        )
                        .padding(vertical = 8.dp)
                        .testTag("focus_app_item_${appItem.packageName}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "0${i + 1}.",
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.width(32.dp)
                            )

                            Text(
                                text = appItem.displayLabel,
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }

                        // Indicators (Limit active, Private Space locked, etc.)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (appItem.isPrivateProfile && isPrivateSpaceLocked) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Private Space Locked",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            if (appItem.dailyLimitMinutes != null && appItem.dailyLimitMinutes > 0) {
                                Icon(
                                    imageVector = Icons.Outlined.HourglassBottom,
                                    contentDescription = "Screen Limit Active",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${appItem.dailyLimitMinutes}m",
                                    style = TextStyle(
                                        fontFamily = fontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // Empty Focus App Slot
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onAddSlotClick(i) },
                            onLongClick = {}
                        )
                        .padding(vertical = 8.dp)
                        .testTag("empty_focus_slot_$i")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "0${i + 1}.",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.width(32.dp)
                        )
                        Text(
                            text = "+ Assign Slot",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
    }
}
