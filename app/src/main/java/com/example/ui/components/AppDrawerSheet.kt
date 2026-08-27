package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockFont
import com.example.model.AppInfoItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerSheet(
    mainApps: List<AppInfoItem>,
    privateApps: List<AppInfoItem>,
    recentApps: List<AppInfoItem>,
    clockFont: ClockFont,
    isPrivateSpaceLocked: Boolean,
    osPrivateProfileHandle: android.os.UserHandle?,
    onAppClick: (AppInfoItem) -> Unit,
    onAppLongClick: (AppInfoItem) -> Unit,
    onTogglePrivateSpace: (android.os.UserHandle) -> Unit,
    onCloseDrawer: () -> Unit,
    onSearchWeb: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val fontFamily = remember(clockFont) {
        com.example.util.FontManager.resolveFontFamily(context, clockFont)
    }
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val visibleMainApps = remember(mainApps, searchQuery) {
        if (searchQuery.isBlank()) mainApps
        else mainApps.filter { it.displayLabel.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }
    }

    val visiblePrivateApps = remember(privateApps, searchQuery) {
        if (searchQuery.isBlank()) privateApps
        else privateApps.filter { it.displayLabel.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }
    }

    val groupedApps = remember(visibleMainApps) {
        visibleMainApps.groupBy {
            val firstChar = it.displayLabel.firstOrNull()?.uppercaseChar() ?: '#'
            if (firstChar in 'A'..'Z') firstChar else '#'
        }
    }

    val alphabetList = remember { ('A'..'Z').toList() + listOf('#') }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 12.dp)
            .testTag("app_drawer_container")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "APPLICATIONS",
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            IconButton(
                onClick = onCloseDrawer,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("close_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close Drawer",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Type to search apps...",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear Search",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("drawer_search_input"),
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = fontFamily,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchQuery.isNotBlank() && visibleMainApps.isEmpty()) {
                        onSearchWeb(searchQuery)
                    } else if (visibleMainApps.isNotEmpty()) {
                        onAppClick(visibleMainApps.first())
                    }
                }
            ),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp, end = 8.dp)
                    .testTag("app_drawer_list")
            ) {
                if (searchQuery.isBlank() && recentApps.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RECENT APPS",
                                    style = TextStyle(
                                        fontFamily = fontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                )
                            }
                            recentApps.forEach { appItem ->
                                AppDrawerItem(
                                    appItem = appItem,
                                    fontFamily = fontFamily,
                                    onClick = { onAppClick(appItem) },
                                    onLongClick = { onAppLongClick(appItem) }
                                )
                            }
                        }
                    }
                }

                if (visibleMainApps.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No apps matching \"$searchQuery\"",
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { onSearchWeb(searchQuery) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .testTag("search_web_button")
                            ) {
                                Text(
                                    text = "Search Web for \"$searchQuery\"",
                                    style = TextStyle(
                                        fontFamily = fontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                } else {
                    groupedApps.forEach { (charHeader, appsUnderChar) ->
                        item(key = "header_$charHeader") {
                            Text(
                                text = charHeader.toString(),
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                            )
                        }

                        items(appsUnderChar, key = { "${it.packageName}_${it.userHandle?.hashCode() ?: 0}" }) { appItem ->
                            AppDrawerItem(
                                appItem = appItem,
                                fontFamily = fontFamily,
                                onClick = { onAppClick(appItem) },
                                onLongClick = { onAppLongClick(appItem) }
                            )
                        }
                    }
                }

                // Private Space pill at the bottom
                if (osPrivateProfileHandle != null) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { onTogglePrivateSpace(osPrivateProfileHandle) }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isPrivateSpaceLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                                        contentDescription = "Private Space",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Private Space",
                                        style = TextStyle(
                                            fontFamily = fontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                if (isPrivateSpaceLocked) {
                                    Text(
                                        text = "Tap to unlock",
                                        style = TextStyle(
                                            fontFamily = fontFamily,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                            .clickable { onTogglePrivateSpace(osPrivateProfileHandle) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = "Lock Private Space",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Lock",
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (!isPrivateSpaceLocked && visiblePrivateApps.isNotEmpty()) {
                        items(visiblePrivateApps, key = { "${it.packageName}_${it.userHandle?.hashCode() ?: 0}" }) { appItem ->
                            AppDrawerItem(
                                appItem = appItem,
                                fontFamily = fontFamily,
                                onClick = { onAppClick(appItem) },
                                onLongClick = { onAppLongClick(appItem) }
                            )
                        }
                    } else if (!isPrivateSpaceLocked) {
                        item {
                            Text(
                                text = "No apps in Private Space matching search.",
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 16.dp)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }

            if (searchQuery.isBlank()) {
                Column(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxHeight()
                        .padding(end = 4.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    alphabetList.forEach { letter ->
                        Text(
                            text = letter.toString(),
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    val index = visibleMainApps.indexOfFirst {
                                        val firstChar = it.displayLabel.firstOrNull()?.uppercaseChar() ?: '#'
                                        val targetChar = if (firstChar in 'A'..'Z') firstChar else '#'
                                        targetChar == letter
                                    }
                                    if (index >= 0) {
                                        coroutineScope.launch {
                                            // Approximation
                                            listState.scrollToItem(index)
                                        }
                                    }
                                }
                                .padding(vertical = 1.dp, horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppDrawerItem(
    appItem: AppInfoItem,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .testTag("app_item_${appItem.packageName}_${appItem.userHandle?.hashCode() ?: 0}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = appItem.displayLabel,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (appItem.dailyLimitMinutes != null && appItem.dailyLimitMinutes > 0) {
                    Icon(
                        imageVector = Icons.Outlined.HourglassBottom,
                        contentDescription = "Screen Limit",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
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
}
