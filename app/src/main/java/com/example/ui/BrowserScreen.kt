package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BookmarksDialog
import com.example.ui.components.FindInPageBar
import com.example.ui.components.HistoryDialog
import com.example.ui.components.LeftSidebar
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StartPageView
import com.example.ui.components.UserscriptManagerDialog
import com.example.ui.components.WebViewContainer
import com.example.viewmodel.BrowserViewModel

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val selectedTabId by viewModel.selectedTabId.collectAsStateWithLifecycle()
    val isSidebarExpanded by viewModel.isSidebarExpanded.collectAsStateWithLifecycle()
    val isSidebarHidden by viewModel.isSidebarHidden.collectAsStateWithLifecycle()
    val userscripts by viewModel.userscripts.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val searchEngine by viewModel.searchEngine.collectAsStateWithLifecycle()
    val isIncognito by viewModel.isIncognito.collectAsStateWithLifecycle()

    val showUserscriptDialog by viewModel.showUserscriptDialog.collectAsStateWithLifecycle()
    val showBookmarksDialog by viewModel.showBookmarksDialog.collectAsStateWithLifecycle()
    val showHistoryDialog by viewModel.showHistoryDialog.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val showFindInPage by viewModel.showFindInPage.collectAsStateWithLifecycle()
    val findQuery by viewModel.findQuery.collectAsStateWithLifecycle()
    val findMatches by viewModel.findMatches.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val activeTab = remember(tabs, selectedTabId) {
        tabs.find { it.id == selectedTabId } ?: tabs.firstOrNull()
    }

    // Android Hardware Back button handling
    BackHandler(enabled = activeTab?.canGoBack == true) {
        viewModel.goBack()
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Vertical Sidebar (Zero top/bottom obstruction!)
            AnimatedVisibility(
                visible = !isSidebarHidden,
                enter = slideInHorizontally { -it } + fadeIn(),
                exit = slideOutHorizontally { -it } + fadeOut()
            ) {
                LeftSidebar(
                    tabs = tabs,
                    activeTab = activeTab,
                    isExpanded = isSidebarExpanded,
                    userscripts = userscripts,
                    searchEngine = searchEngine,
                    isIncognito = isIncognito,
                    onSelectTab = { viewModel.selectTab(it) },
                    onCloseTab = { viewModel.closeTab(it) },
                    onCloseOtherTabs = { viewModel.closeOtherTabs(it) },
                    onNewTab = { viewModel.openNewTab() },
                    onNavigate = { viewModel.loadUrl(it) },
                    onGoBack = { viewModel.goBack() },
                    onGoForward = { viewModel.goForward() },
                    onReloadOrStop = { viewModel.reloadOrStop() },
                    onGoHome = { viewModel.goHome() },
                    onToggleDesktopMode = { viewModel.toggleDesktopMode(it) },
                    onToggleExpand = { viewModel.toggleSidebarExpanded() },
                    onToggleHide = { viewModel.toggleSidebarHidden() },
                    onBookmarkCurrent = { viewModel.toggleBookmarkCurrent() },
                    onOpenUserscripts = { viewModel.setUserscriptDialogVisible(true) },
                    onOpenBookmarks = { viewModel.setBookmarksDialogVisible(true) },
                    onOpenHistory = { viewModel.setHistoryDialogVisible(true) },
                    onOpenFindInPage = { viewModel.setFindInPageVisible(true) },
                    onOpenSettings = { viewModel.setSettingsDialogVisible(true) }
                )
            }

            // Main Web View Area (Occupies full vertical height from top to bottom)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (activeTab != null && (activeTab.url == "about:blank" || activeTab.url.isBlank())) {
                    StartPageView(
                        searchEngine = searchEngine,
                        onNavigate = { viewModel.loadUrl(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    WebViewContainer(
                        tabs = tabs,
                        activeTab = activeTab,
                        userscripts = userscripts,
                        webCommands = viewModel.webCommands,
                        onTabUrlChanged = { id, url -> viewModel.updateTabUrl(id, url) },
                        onTabTitleChanged = { id, title -> viewModel.updateTabTitle(id, title) },
                        onTabLoadingChanged = { id, loading, prog -> viewModel.updateTabLoading(id, loading, prog) },
                        onTabNavigationChanged = { id, back, fwd -> viewModel.updateTabNavigation(id, back, fwd) },
                        onTabFaviconChanged = { id, icon -> viewModel.updateTabFavicon(id, icon) },
                        onFindMatchesFound = { cur, tot -> viewModel.setFindMatches(cur, tot) },
                        onScriptInjected = { viewModel.onScriptInjected(it) },
                        onRecordHistory = { title, url -> viewModel.recordHistory(title, url) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Find In Page floating bar
                if (showFindInPage) {
                    FindInPageBar(
                        query = findQuery,
                        matchCount = findMatches,
                        onQueryChange = { viewModel.onFindQueryChanged(it) },
                        onFindNext = { viewModel.findNext() },
                        onFindPrevious = { viewModel.findPrevious() },
                        onClose = { viewModel.setFindInPageVisible(false) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }

        // Floating button on left edge when sidebar is hidden in full screen mode
        if (isSidebarHidden) {
            Surface(
                onClick = { viewModel.toggleSidebarHidden() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp),
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                tonalElevation = 6.dp
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "唤出侧边栏",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 20.dp)
                        .size(24.dp)
                )
            }
        }
    }

    // Modal Dialogs
    if (showUserscriptDialog) {
        UserscriptManagerDialog(
            scripts = userscripts,
            onToggleScript = { id, enabled -> viewModel.toggleScript(id, enabled) },
            onSaveScript = { viewModel.saveScript(it) },
            onDeleteScript = { viewModel.deleteScript(it) },
            onDismiss = { viewModel.setUserscriptDialogVisible(false) }
        )
    }

    if (showBookmarksDialog) {
        BookmarksDialog(
            bookmarks = bookmarks,
            onOpenUrl = { viewModel.loadUrl(it) },
            onDeleteBookmark = { viewModel.removeBookmark(it) },
            onDismiss = { viewModel.setBookmarksDialogVisible(false) }
        )
    }

    if (showHistoryDialog) {
        HistoryDialog(
            historyList = history,
            onOpenUrl = { viewModel.loadUrl(it) },
            onDeleteHistory = { viewModel.deleteHistory(it) },
            onClearAll = { viewModel.clearAllHistory() },
            onDismiss = { viewModel.setHistoryDialogVisible(false) }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentSearchEngine = searchEngine,
            isIncognito = isIncognito,
            onSelectSearchEngine = { viewModel.setSearchEngine(it) },
            onToggleIncognito = { viewModel.toggleIncognito() },
            onClearCacheAndCookies = { viewModel.clearCacheAndCookies() },
            onDismiss = { viewModel.setSettingsDialogVisible(false) }
        )
    }
}
