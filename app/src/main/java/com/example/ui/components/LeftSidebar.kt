package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserscriptEntity
import com.example.model.BrowserTab
import com.example.model.SearchEngine

@Composable
fun LeftSidebar(
    tabs: List<BrowserTab>,
    activeTab: BrowserTab?,
    isExpanded: Boolean,
    userscripts: List<UserscriptEntity>,
    searchEngine: SearchEngine,
    isIncognito: Boolean,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onCloseOtherTabs: (String) -> Unit,
    onNewTab: () -> Unit,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onReloadOrStop: () -> Unit,
    onGoHome: () -> Unit,
    onToggleDesktopMode: (String) -> Unit,
    onToggleExpand: () -> Unit,
    onToggleHide: () -> Unit,
    onBookmarkCurrent: () -> Unit,
    onToggleIncognito: () -> Unit,
    onTranslatePage: () -> Unit,
    onOpenUserscripts: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFindInPage: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabledScriptsCount = remember(userscripts) { userscripts.count { it.isEnabled } }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(if (isExpanded) 280.dp else 64.dp)
            .animateContentSize(),
        color = if (isIncognito) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f) else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isIncognito) 6.dp else 3.dp,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            if (isExpanded) {
                ExpandedSidebarContent(
                    tabs = tabs,
                    activeTab = activeTab,
                    enabledScriptsCount = enabledScriptsCount,
                    searchEngine = searchEngine,
                    isIncognito = isIncognito,
                    onSelectTab = onSelectTab,
                    onCloseTab = onCloseTab,
                    onCloseOtherTabs = onCloseOtherTabs,
                    onNewTab = onNewTab,
                    onNavigate = onNavigate,
                    onGoBack = onGoBack,
                    onGoForward = onGoForward,
                    onReloadOrStop = onReloadOrStop,
                    onGoHome = onGoHome,
                    onToggleDesktopMode = onToggleDesktopMode,
                    onToggleExpand = onToggleExpand,
                    onToggleHide = onToggleHide,
                    onBookmarkCurrent = onBookmarkCurrent,
                    onToggleIncognito = onToggleIncognito,
                    onTranslatePage = onTranslatePage,
                    onOpenUserscripts = onOpenUserscripts,
                    onOpenBookmarks = onOpenBookmarks,
                    onOpenHistory = onOpenHistory,
                    onOpenFindInPage = onOpenFindInPage,
                    onOpenSettings = onOpenSettings
                )
            } else {
                SlimSidebarContent(
                    tabs = tabs,
                    activeTab = activeTab,
                    enabledScriptsCount = enabledScriptsCount,
                    isIncognito = isIncognito,
                    onNewTab = onNewTab,
                    onGoBack = onGoBack,
                    onGoForward = onGoForward,
                    onReloadOrStop = onReloadOrStop,
                    onGoHome = onGoHome,
                    onToggleDesktopMode = onToggleDesktopMode,
                    onToggleExpand = onToggleExpand,
                    onToggleHide = onToggleHide,
                    onToggleIncognito = onToggleIncognito,
                    onTranslatePage = onTranslatePage,
                    onOpenUserscripts = onOpenUserscripts,
                    onOpenBookmarks = onOpenBookmarks,
                    onOpenHistory = onOpenHistory,
                    onOpenSettings = onOpenSettings
                )
            }
        }
    }
}

@Composable
private fun ExpandedSidebarContent(
    tabs: List<BrowserTab>,
    activeTab: BrowserTab?,
    enabledScriptsCount: Int,
    searchEngine: SearchEngine,
    isIncognito: Boolean,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onCloseOtherTabs: (String) -> Unit,
    onNewTab: () -> Unit,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onReloadOrStop: () -> Unit,
    onGoHome: () -> Unit,
    onToggleDesktopMode: (String) -> Unit,
    onToggleExpand: () -> Unit,
    onToggleHide: () -> Unit,
    onBookmarkCurrent: () -> Unit,
    onToggleIncognito: () -> Unit,
    onTranslatePage: () -> Unit,
    onOpenUserscripts: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFindInPage: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var urlInput by remember { mutableStateOf(activeTab?.url ?: "") }

    LaunchedEffect(activeTab?.url) {
        if (activeTab != null && activeTab.url != "about:blank") {
            urlInput = activeTab.url
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        // Top Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "LeftTab",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    onClick = onToggleIncognito,
                    shape = RoundedCornerShape(12.dp),
                    color = if (isIncognito) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "切换无痕模式",
                            tint = if (isIncognito) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isIncognito) "无痕" else "常规",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isIncognito) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = onToggleExpand, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                        contentDescription = "收起侧栏",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onToggleHide, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "全屏沉浸",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // URL / Search Input in Left Sidebar
        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            singleLine = true,
            placeholder = { Text("搜索或输入网址...", fontSize = 12.sp) },
            leadingIcon = {
                val isHttps = activeTab?.url?.startsWith("https://") == true
                Icon(
                    imageVector = if (isHttps) Icons.Default.Lock else Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isHttps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (urlInput.isNotBlank()) {
                    IconButton(
                        onClick = { urlInput = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清空",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(
                onGo = {
                    focusManager.clearFocus()
                    onNavigate(urlInput)
                }
            ),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
        )

        // Navigation Actions Row (Back, Forward, Refresh, Home, Star)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onGoBack,
                enabled = activeTab?.canGoBack == true,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "后退",
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onGoForward,
                enabled = activeTab?.canGoForward == true,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "前进",
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onReloadOrStop,
                modifier = Modifier.size(36.dp)
            ) {
                if (activeTab?.isLoading == true) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "停止",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            IconButton(
                onClick = onGoHome,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "主页",
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onTranslatePage,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "网页翻译",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onBookmarkCurrent,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "收藏本页",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = DividerDefaults.color.copy(alpha = 0.5f)
        )

        // Vertical Tabs Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "标签页 (${tabs.size})",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            IconButton(
                onClick = onNewTab,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "新建标签页",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Vertical Tabs List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(tabs, key = { it.id }) { tab ->
                val isActive = tab.id == activeTab?.id
                VerticalTabItem(
                    tab = tab,
                    isActive = isActive,
                    onClick = { onSelectTab(tab.id) },
                    onClose = { onCloseTab(tab.id) },
                    onCloseOthers = { onCloseOtherTabs(tab.id) }
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = DividerDefaults.color.copy(alpha = 0.5f)
        )

        // Bottom Tools & Quick Actions in Left Sidebar
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Userscript / 油猴插件 Button with badge
            SidebarToolRow(
                icon = Icons.Default.Extension,
                label = "油猴插件",
                badgeText = if (enabledScriptsCount > 0) "$enabledScriptsCount 开启" else null,
                badgeColor = MaterialTheme.colorScheme.primary,
                onClick = onOpenUserscripts
            )

            // Desktop Mode Toggle
            val isDesktop = activeTab?.isDesktopMode == true
            SidebarToolRow(
                icon = if (isDesktop) Icons.Default.DesktopWindows else Icons.Default.Smartphone,
                label = if (isDesktop) "电脑版排版 (开)" else "移动端排版",
                badgeText = if (isDesktop) "PC" else "Mobile",
                badgeColor = if (isDesktop) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                onClick = {
                    if (activeTab != null) {
                        onToggleDesktopMode(activeTab.id)
                    }
                }
            )

            // Bookmarks & History
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenBookmarks),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "书签", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenHistory),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "历史", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Find in Page & Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenFindInPage),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "页内查找", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenSettings),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "设置", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SlimSidebarContent(
    tabs: List<BrowserTab>,
    activeTab: BrowserTab?,
    enabledScriptsCount: Int,
    isIncognito: Boolean,
    onNewTab: () -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onReloadOrStop: () -> Unit,
    onGoHome: () -> Unit,
    onToggleDesktopMode: (String) -> Unit,
    onToggleExpand: () -> Unit,
    onToggleHide: () -> Unit,
    onToggleIncognito: () -> Unit,
    onTranslatePage: () -> Unit,
    onOpenUserscripts: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onToggleExpand, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "展开侧边栏",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            IconButton(
                onClick = onGoBack,
                enabled = activeTab?.canGoBack == true,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "后退",
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onGoForward,
                enabled = activeTab?.canGoForward == true,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "前进",
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onReloadOrStop, modifier = Modifier.size(40.dp)) {
                if (activeTab?.isLoading == true) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "停止",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            IconButton(onClick = onGoHome, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "主页",
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onTranslatePage, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "网页翻译",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.width(36.dp))
            Spacer(modifier = Modifier.height(8.dp))

            // Tabs button with badge
            BadgedBox(
                badge = {
                    Badge { Text(text = "${tabs.size}") }
                }
            ) {
                IconButton(onClick = onToggleExpand, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Tab,
                        contentDescription = "标签页",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onNewTab, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "新建标签页",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Bottom section
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Incognito Mode Toggle Button
            IconButton(onClick = onToggleIncognito, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = "无痕模式",
                    tint = if (isIncognito) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Userscript button
            BadgedBox(
                badge = {
                    if (enabledScriptsCount > 0) {
                        Badge { Text(text = "$enabledScriptsCount") }
                    }
                }
            ) {
                IconButton(onClick = onOpenUserscripts, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = "油猴插件",
                        tint = if (enabledScriptsCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Desktop mode toggle
            val isDesktop = activeTab?.isDesktopMode == true
            IconButton(
                onClick = {
                    if (activeTab != null) onToggleDesktopMode(activeTab.id)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isDesktop) Icons.Default.DesktopWindows else Icons.Default.Smartphone,
                    contentDescription = "切换桌面排版",
                    tint = if (isDesktop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onOpenBookmarks, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "书签",
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onOpenHistory, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "历史",
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onOpenSettings, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun VerticalTabItem(
    tab: BrowserTab,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onCloseOthers: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            }
        ),
        border = if (isActive) {
            CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favicon or Domain Initial
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (tab.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (tab.favicon != null) {
                    Image(
                        bitmap = tab.favicon.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    val initial = tab.title.trim().firstOrNull()?.toString() ?: "页"
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Title & Host
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val host = try {
                    if (tab.url.startsWith("http")) java.net.URI(tab.url).host ?: tab.url else tab.url
                } catch (e: Exception) {
                    tab.url
                }
                Text(
                    text = host,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Close Tab Button
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭标签",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SidebarToolRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    badgeText: String? = null,
    badgeColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (badgeText != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
