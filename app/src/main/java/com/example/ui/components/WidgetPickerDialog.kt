package com.example.ui.components

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetPickerDialog(
    onDismiss: () -> Unit,
    onWidgetSelected: (AppWidgetProviderInfo) -> Unit
) {
    val context = LocalContext.current
    var providers by remember { mutableStateOf<List<AppWidgetProviderInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            providers = appWidgetManager.installedProviders.sortedBy { it.loadLabel(context.packageManager) }
            isLoading = false
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Select Widget",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                Text("Loading widgets...", modifier = Modifier.padding(16.dp))
            } else if (providers.isEmpty()) {
                Text("No widgets found.", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(providers, key = { it.provider.flattenToString() }) { providerInfo ->
                        WidgetProviderItem(
                            info = providerInfo,
                            context = context,
                            onClick = { onWidgetSelected(providerInfo) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetProviderItem(
    info: AppWidgetProviderInfo,
    context: Context,
    onClick: () -> Unit
) {
    var iconDrawable by remember { mutableStateOf<Drawable?>(null) }
    var previewDrawable by remember { mutableStateOf<Drawable?>(null) }

    LaunchedEffect(info) {
        withContext(Dispatchers.IO) {
            iconDrawable = info.loadIcon(context, context.resources.displayMetrics.densityDpi)
            previewDrawable = info.loadPreviewImage(context, context.resources.displayMetrics.densityDpi)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconDrawable != null) {
                val bitmap = remember(iconDrawable) {
                    try {
                        iconDrawable!!.toBitmap(config = android.graphics.Bitmap.Config.ARGB_8888).asImageBitmap()
                    } catch (e: Exception) {
                        null
                    }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Widgets,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Outlined.Widgets,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = info.loadLabel(context.packageManager),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${info.provider.packageName} • ${info.minWidth}x${info.minHeight} dp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (previewDrawable != null) {
            val previewBitmap = remember(previewDrawable) {
                try {
                    previewDrawable!!.toBitmap(config = android.graphics.Bitmap.Config.ARGB_8888).asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }
            if (previewBitmap != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    bitmap = previewBitmap,
                    contentDescription = "Widget Preview",
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    alignment = Alignment.Center
                )
            }
        }
    }
}
