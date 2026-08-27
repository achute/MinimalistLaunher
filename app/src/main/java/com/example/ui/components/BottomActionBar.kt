package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockFont
import com.example.model.BottomSlot
import com.example.ui.theme.getFontFamily

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomActionBar(
    bottomSlots: List<BottomSlot>,
    clockFont: ClockFont,
    onSlotClick: (BottomSlot) -> Unit,
    onSlotLongClick: (BottomSlot) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val fontFamily = androidx.compose.runtime.remember(clockFont) {
        com.example.util.FontManager.resolveFontFamily(context, clockFont)
    }

    // Ensure 3 slots exist
    val slot0 = bottomSlots.find { it.slotIndex == 0 } ?: BottomSlot(0, "", "Phone", "phone")
    val slot1 = bottomSlots.find { it.slotIndex == 1 } ?: BottomSlot(1, "", "Messages", "messages")
    val slot2 = bottomSlots.find { it.slotIndex == 2 } ?: BottomSlot(2, "", "Camera", "camera")
    val slots = listOf(slot0, slot1, slot2)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("bottom_action_bar_section"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slots.forEach { slot ->
                val title = if (slot.customLabel.isNotBlank()) {
                    slot.customLabel
                } else if (slot.packageName.isNotBlank()) {
                    slot.packageName.substringAfterLast('.')
                } else {
                    slot.defaultType.replaceFirstChar { it.uppercase() }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(
                            0.5.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .combinedClickable(
                            onClick = { onSlotClick(slot) },
                            onLongClick = { onSlotLongClick(slot) }
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("bottom_slot_${slot.slotIndex}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.uppercase(),
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Settings Icon Button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .border(
                    0.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
                .testTag("launcher_settings_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Launcher Settings",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
