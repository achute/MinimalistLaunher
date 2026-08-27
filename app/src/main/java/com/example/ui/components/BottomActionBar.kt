package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockFont
import com.example.model.BottomSlot

fun resolveBottomSlotIcon(iconName: String, defaultType: String = ""): ImageVector {
    val key = (if (iconName.isNotBlank()) iconName else defaultType).lowercase().trim()
    return when (key) {
        "phone", "call", "dialer" -> Icons.Outlined.Call
        "messages", "message", "sms", "chat" -> Icons.Outlined.Chat
        "camera", "photo", "photos" -> Icons.Outlined.PhotoCamera
        "settings", "gear", "config", "preferences" -> Icons.Outlined.Settings
        "browser", "globe", "web", "internet" -> Icons.Outlined.Language
        "search", "find", "google" -> Icons.Outlined.Search
        "email", "mail", "inbox" -> Icons.Outlined.Email
        "music", "audio", "song" -> Icons.Outlined.MusicNote
        "star", "favorite_border" -> Icons.Outlined.Star
        "heart", "favorite" -> Icons.Outlined.Favorite
        "folder", "files", "docs" -> Icons.Outlined.Folder
        "terminal", "code", "dev" -> Icons.Outlined.Terminal
        "apps", "grid", "all_apps" -> Icons.Outlined.Apps
        "clock", "time", "alarm" -> Icons.Outlined.Schedule
        "map", "navigation", "gps" -> Icons.Outlined.Navigation
        "bookmark", "saved" -> Icons.Outlined.Bookmark
        else -> Icons.Outlined.Apps
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomActionBar(
    bottomSlots: List<BottomSlot>,
    clockFont: ClockFont,
    onSlotClick: (BottomSlot) -> Unit,
    onSlotLongClick: (BottomSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val fontFamily = androidx.compose.runtime.remember(clockFont) {
        com.example.util.FontManager.resolveFontFamily(context, clockFont)
    }

    // Fixed 4 icons by default: Phone, Messages, Camera, Settings
    val slot0 = bottomSlots.find { it.slotIndex == 0 } ?: BottomSlot(0, "", "Phone", "phone", "phone")
    val slot1 = bottomSlots.find { it.slotIndex == 1 } ?: BottomSlot(1, "", "Messages", "messages", "messages")
    val slot2 = bottomSlots.find { it.slotIndex == 2 } ?: BottomSlot(2, "", "Camera", "camera", "camera")
    val slot3 = bottomSlots.find { it.slotIndex == 3 } ?: BottomSlot(3, "", "Settings", "settings", "settings")
    val slots = listOf(slot0, slot1, slot2, slot3)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("bottom_action_bar_section"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        slots.forEach { slot ->
            val iconVector = resolveBottomSlotIcon(slot.iconName, slot.defaultType)
            val title = if (slot.customLabel.isNotBlank()) {
                slot.customLabel
            } else if (slot.packageName.isNotBlank()) {
                slot.packageName.substringAfterLast('.')
            } else {
                when (slot.defaultType.lowercase()) {
                    "phone" -> "Phone"
                    "messages" -> "Messages"
                    "camera" -> "Camera"
                    "settings" -> "Settings"
                    "browser" -> "Browser"
                    "search" -> "Search"
                    else -> "App"
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        RoundedCornerShape(12.dp)
                    )
                    .combinedClickable(
                        onClick = { onSlotClick(slot) },
                        onLongClick = { onSlotLongClick(slot) }
                    )
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .testTag("bottom_slot_${slot.slotIndex}"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = title,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title.uppercase(),
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
