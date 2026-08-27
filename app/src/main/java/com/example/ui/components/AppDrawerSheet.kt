package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.getFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerSheet(
    allApps: List<AppInfoItem>,
    recentApps: List<AppInfoItem>,
    clockFont: ClockFont,
    isPrivateSpaceUnlocked: Boolean,
    onAppClick: (AppInfoItem) -> Unit,
    onAppLongClick: (AppInfoItem) -> Unit,
    onUnlockPrivateSpace: () -> Unit,
    onLockPrivateSpace: () -> Unit,
    onCloseDrawer: () -> Unit,
    onSearchWeb: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val fontFamily = remember(clockFont) {
        com.example.util.FontManager.resolveFontFamily(context, clockFont)
    }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All Apps, 1: Private Space
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Filter visible vs hidden
    val visibleApps = remember(allApps, searchQuery) {
        val nonHidden = allApps.filter { !it.isHidden }
        if (searchQuery.isBlank()) {
            nonHidden
        } else {
            nonHidden.filter {
                it.displayLabel.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val hiddenAppsList = remember(allApps) {
        allApps.filter { it.isHidden }
    }

    // Group by first letter for A-Z indexing
    val groupedApps = remember(visibleApps) {
        visibleApps.groupBy {
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
        // Drawer Header & Close Button
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

        // Search Field
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
                    if (searchQuery.isNotBlank() && visibleApps.isEmpty()) {
                        onSearchWeb(searchQuery)
                    } else if (visibleApps.isNotEmpty()) {
                        onAppClick(visibleApps.first())
                    }
                }
            ),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        // Tabs: Standard Drawer vs Private Space
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 2.dp
                )
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "ALL APPS (${visibleApps.size})",
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                },
                modifier = Modifier.testTag("tab_all_apps")
            )

            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    if (!isPrivateSpaceUnlocked) {
                        onUnlockPrivateSpace()
                    }
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPrivateSpaceUnlocked) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isPrivateSpaceUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRIVATE SPACE (${hiddenAppsList.size})",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("tab_private_space")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedTab == 0) {
            // Standard App Drawer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Main App List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 20.dp, end = 8.dp)
                        .testTag("app_drawer_list")
                ) {
                    // Recent Apps section (only when not searching)
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

                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }

                    // Categorized Alphabetical List
                    if (visibleApps.isEmpty()) {
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

                            items(appsUnderChar, key = { it.packageName }) { appItem ->
                                AppDrawerItem(
                                    appItem = appItem,
                                    fontFamily = fontFamily,
                                    onClick = { onAppClick(appItem) },
                                    onLongClick = { onAppLongClick(appItem) }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }

                // A-Z Fast Scroll Rail
                if (searchQuery.isBlank()) {
                    Column(
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight()
                            .padding(end = 4.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        alphabetList.forEach { char ->
                            val isPresent = groupedApps.containsKey(char)
                            Text(
                                text = char.toString(),
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = if (isPresent) FontWeight.Bold else FontWeight.Light,
                                    color = if (isPresent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .clickable(enabled = isPresent) {
                                        // Scroll to header
                                        // approximate index calculation
                                        val headerKey = "header_$char"
                                        coroutineScope.launch {
                                            // Scroll to char position
                                            val index = groupedApps.keys.toList().indexOf(char)
                                            if (index >= 0) {
                                                listState.animateScrollToItem(index * 2)
                                            }
                                        }
                                    }
                                    .padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Private Space Section
            if (!isPrivateSpaceUnlocked) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = "Biometric Lock",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Private Space Locked",
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Hidden apps are protected by on-device biometric security or PIN.",
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onUnlockPrivateSpace() }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .testTag("unlock_biometric_button")
                    ) {
                        Text(
                            text = "Unlock with Biometrics / PIN",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            } else {
                // Unlocked Private Space App List
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HIDDEN PACKAGES",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        Text(
                            text = "[ LOCK NOW ]",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .clickable { onLockPrivateSpace() }
                                .padding(4.dp)
                                .testTag("lock_private_space_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (hiddenAppsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hidden apps yet.\nLong-press any app in All Apps to move it here.",
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                    } else {
                        LazyColumn {
                            items(hiddenAppsList, key = { it.packageName }) { appItem ->
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
            .testTag("app_item_${appItem.packageName}")
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

                if (appItem.isHidden) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.VisibilityOff,
                        contentDescription = "Hidden",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
