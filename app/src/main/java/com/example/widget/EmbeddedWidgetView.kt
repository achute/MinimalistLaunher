package com.example.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Context
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun EmbeddedWidgetView(
    appWidgetId: Int,
    appWidgetHost: AppWidgetHost,
    slotTitle: String = "External Widget",
    minHeight: Dp = 100.dp,
    onPickWidget: () -> Unit,
    onRemoveWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appWidgetManager = remember(context) { AppWidgetManager.getInstance(context) }
    val appWidgetInfo = remember(appWidgetId) {
        if (appWidgetId != -1) {
            try {
                appWidgetManager.getAppWidgetInfo(appWidgetId)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    if (appWidgetId != -1 && appWidgetInfo != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .testTag("embedded_widget_container")
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(minHeight),
                factory = { ctx ->
                    try {
                        val hostView = appWidgetHost.createView(ctx, appWidgetId, appWidgetInfo)
                        hostView.setAppWidget(appWidgetId, appWidgetInfo)
                        hostView
                    } catch (e: Exception) {
                        AppWidgetHostView(ctx)
                    }
                },
                update = { hostView ->
                    try {
                        hostView.setAppWidget(appWidgetId, appWidgetInfo)
                    } catch (e: Exception) {
                        // Ignore update errors
                    }
                }
            )

            // Small subtle remove widget button on top-right
            IconButton(
                onClick = onRemoveWidget,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
                    .padding(4.dp)
                    .testTag("remove_widget_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Remove Widget",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    } else {
        // Empty slot placeholder
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onPickWidget() }
                .padding(vertical = 12.dp, horizontal = 14.dp)
                .testTag("pick_widget_placeholder"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+ Attach $slotTitle",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
