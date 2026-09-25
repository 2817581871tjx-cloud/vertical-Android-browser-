package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BrowserDatabase
import com.example.data.local.BrowserRepository
import com.example.data.local.UserscriptEntity
import com.example.model.BrowserTab
import com.example.model.SearchEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface WebCommand {
    data class LoadUrl(val tabId: String, val url: String) : WebCommand
    data class GoBack(val tabId: String) : WebCommand
    data class GoForward(val tabId: String) : WebCommand
    data class Reload(val tabId: String) : WebCommand
    data class StopLoading(val tabId: String) : WebCommand
    data class FindInPage(val query: String, val forward: Boolean = true) : WebCommand
    data object ClearFindMatches : WebCommand
    data object ClearCacheAndCookies : WebCommand
    data class TranslatePage(val tabId: String, val targetLanguage: String = "zh-CN") : WebCommand
    data class RestoreOriginalPage(val tabId: String) : WebCommand
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BrowserRepository(BrowserDatabase.getInstance(application))

    val userscripts: StateFlow<List<UserscriptEntity>> = repository.allScripts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Web command channel to communicate with WebViews
    private val _webCommands = MutableSharedFlow<WebCommand>(extraBufferCapacity = 16)
    val webCommands: SharedFlow<WebCommand> = _webCommands.asSharedFlow()

    // Tabs state
    private val initialTab = BrowserTab(
        title = "微软必应",
        url = "https://www.bing.com",
        isDesktopMode = false
    )
    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(initialTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _selectedTabId = MutableStateFlow(initialTab.id)
    val selectedTabId: StateFlow<String> = _selectedTabId.asStateFlow()

    // Sidebar states: expanded (full with text), collapsed (slim icon rail), or hidden (true fullscreen)
    private val _isSidebarExpanded = MutableStateFlow(true)
    val isSidebarExpanded: StateFlow<Boolean> = _isSidebarExpanded.asStateFlow()

    private val _isSidebarHidden = MutableStateFlow(false)
    val isSidebarHidden: StateFlow<Boolean> = _isSidebarHidden.asStateFlow()

    // Active Search Engine
    private val _searchEngine = MutableStateFlow(SearchEngine.BING)
    val searchEngine: StateFlow<SearchEngine> = _searchEngine.asStateFlow()

    // Incognito Mode
    private val _isIncognito = MutableStateFlow(false)
    val isIncognito: StateFlow<Boolean> = _isIncognito.asStateFlow()

    // Translation Bar & Controls
    private val _showTranslationBar = MutableStateFlow(false)
    val showTranslationBar: StateFlow<Boolean> = _showTranslationBar.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    // Dialog & Overlay controls
    private val _showUserscriptDialog = MutableStateFlow(false)
    val showUserscriptDialog: StateFlow<Boolean> = _showUserscriptDialog.asStateFlow()

    private val _showBookmarksDialog = MutableStateFlow(false)
    val showBookmarksDialog: StateFlow<Boolean> = _showBookmarksDialog.asStateFlow()

    private val _showHistoryDialog = MutableStateFlow(false)
    val showHistoryDialog: StateFlow<Boolean> = _showHistoryDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showFindInPage = MutableStateFlow(false)
    val showFindInPage: StateFlow<Boolean> = _showFindInPage.asStateFlow()

    private val _findQuery = MutableStateFlow("")
    val findQuery: StateFlow<String> = _findQuery.asStateFlow()

    private val _findMatches = MutableStateFlow("0/0")
    val findMatches: StateFlow<String> = _findMatches.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultScripts()
        }
    }

    val activeTab: BrowserTab?
        get() = _tabs.value.find { it.id == _selectedTabId.value } ?: _tabs.value.firstOrNull()

    fun selectTab(tabId: String) {
        if (_tabs.value.any { it.id == tabId }) {
            _selectedTabId.value = tabId
        }
    }

    fun openNewTab(
        url: String = "https://www.bing.com",
        title: String = "新标签页",
        isIncognito: Boolean = _isIncognito.value
    ) {
        val newTab = BrowserTab(
            title = title,
            url = url,
            isDesktopMode = false,
            isIncognito = isIncognito
        )
        _tabs.value = _tabs.value + newTab
        _selectedTabId.value = newTab.id
    }

    fun openNewIncognitoTab(url: String = "https://www.bing.com") {
        openNewTab(url = url, title = "无痕标签页", isIncognito = true)
        showToast("已打开全新无痕标签页")
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        if (currentList.size <= 1) {
            // If only one tab left, reset it to home instead of leaving empty screen
            val newSingleTab = BrowserTab(
                title = "微软必应",
                url = "https://www.bing.com",
                isDesktopMode = false,
                isIncognito = _isIncognito.value
            )
            _tabs.value = listOf(newSingleTab)
            _selectedTabId.value = newSingleTab.id
            _webCommands.tryEmit(WebCommand.LoadUrl(newSingleTab.id, newSingleTab.url))
            return
        }

        val closingIndex = currentList.indexOfFirst { it.id == tabId }
        val remaining = currentList.filter { it.id != tabId }
        _tabs.value = remaining

        if (_selectedTabId.value == tabId) {
            val nextIndex = (closingIndex.coerceAtMost(remaining.size - 1)).coerceAtLeast(0)
            _selectedTabId.value = remaining[nextIndex].id
        }
    }

    fun closeOtherTabs(keepTabId: String) {
        val keep = _tabs.value.find { it.id == keepTabId } ?: return
        _tabs.value = listOf(keep)
        _selectedTabId.value = keep.id
    }

    fun updateTabUrl(tabId: String, url: String) {
        _tabs.value = _tabs.value.map {
            if (it.id == tabId) it.copy(url = url) else it
        }
    }

    fun updateTabTitle(tabId: String, title: String) {
        _tabs.value = _tabs.value.map {
            if (it.id == tabId) it.copy(title = title.ifBlank { it.url }) else it
        }
    }

    fun updateTabLoading(tabId: String, isLoading: Boolean, progress: Int) {
        _tabs.value = _tabs.value.map {
            if (it.id == tabId) it.copy(isLoading = isLoading, progress = progress) else it
        }
    }

    fun updateTabNavigation(tabId: String, canGoBack: Boolean, canGoForward: Boolean) {
        _tabs.value = _tabs.value.map {
            if (it.id == tabId) it.copy(canGoBack = canGoBack, canGoForward = canGoForward) else it
        }
    }

    fun updateTabFavicon(tabId: String, favicon: Bitmap?) {
        _tabs.value = _tabs.value.map {
            if (it.id == tabId) it.copy(favicon = favicon) else it
        }
    }

    fun toggleDesktopMode(tabId: String) {
        val tab = _tabs.value.find { it.id == tabId } ?: return
        val newMode = !tab.isDesktopMode
        _tabs.value = _tabs.value.map {
            if (it.id == tabId) it.copy(isDesktopMode = newMode) else it
        }
        showToast(if (newMode) "已切换为桌面版 (PC) 浏览模式" else "已切换为移动版浏览模式")
        // Reload tab to apply new UA
        _webCommands.tryEmit(WebCommand.Reload(tabId))
    }

    fun toggleSidebarExpanded() {
        _isSidebarExpanded.value = !_isSidebarExpanded.value
    }

    fun toggleSidebarHidden() {
        _isSidebarHidden.value = !_isSidebarHidden.value
    }

    fun loadUrl(url: String) {
        val currentTab = activeTab ?: return
        val targetUrl = _searchEngine.value.buildQueryUrl(url)
        updateTabUrl(currentTab.id, targetUrl)
        _webCommands.tryEmit(WebCommand.LoadUrl(currentTab.id, targetUrl))
    }

    fun goBack() {
        val currentTab = activeTab ?: return
        if (currentTab.canGoBack) {
            _webCommands.tryEmit(WebCommand.GoBack(currentTab.id))
        }
    }

    fun goForward() {
        val currentTab = activeTab ?: return
        if (currentTab.canGoForward) {
            _webCommands.tryEmit(WebCommand.GoForward(currentTab.id))
        }
    }

    fun reloadOrStop() {
        val currentTab = activeTab ?: return
        if (currentTab.isLoading) {
            _webCommands.tryEmit(WebCommand.StopLoading(currentTab.id))
        } else {
            _webCommands.tryEmit(WebCommand.Reload(currentTab.id))
        }
    }

    fun goHome() {
        loadUrl(_searchEngine.value.homeUrl)
    }

    fun setSearchEngine(engine: SearchEngine) {
        _searchEngine.value = engine
        showToast("默认搜索引擎已设为: ${engine.displayName}")
    }

    fun toggleIncognito() {
        _isIncognito.value = !_isIncognito.value
        showToast(if (_isIncognito.value) "已开启无痕浏览模式 (不留历史与数据)" else "已关闭无痕浏览")
    }

    fun toggleTranslationBar() {
        _showTranslationBar.value = !_showTranslationBar.value
        if (_showTranslationBar.value) {
            translateActivePage("zh-CN")
        }
    }

    fun setTranslationBarVisible(visible: Boolean) {
        _showTranslationBar.value = visible
    }

    fun translateActivePage(targetLang: String = "zh-CN") {
        val tab = activeTab ?: return
        _isTranslating.value = true
        _webCommands.tryEmit(WebCommand.TranslatePage(tab.id, targetLang))
        showToast("已启动页面翻译: 转换为 ${if (targetLang == "zh-CN") "简体中文" else targetLang}")
    }

    fun restoreActivePageOriginal() {
        val tab = activeTab ?: return
        _isTranslating.value = false
        _webCommands.tryEmit(WebCommand.RestoreOriginalPage(tab.id))
        showToast("已恢复网页原文")
    }

    fun translateViaGoogleWeb() {
        val tab = activeTab ?: return
        if (tab.url.isNotBlank() && tab.url.startsWith("http")) {
            try {
                val encoded = java.net.URLEncoder.encode(tab.url, "UTF-8")
                val googleUrl = "https://translate.google.com/translate?sl=auto&tl=zh-CN&u=$encoded"
                openNewTab(url = googleUrl, title = "Google 网页翻译")
                showToast("已通过 Google 网页翻译代理打开")
            } catch (e: Exception) {
                showToast("翻译失败: ${e.message}")
            }
        } else {
            showToast("当前页面非有效网址，无法翻译")
        }
    }

    fun setFindInPageVisible(visible: Boolean) {
        _showFindInPage.value = visible
        if (!visible) {
            _webCommands.tryEmit(WebCommand.ClearFindMatches)
            _findQuery.value = ""
        }
    }

    fun onFindQueryChanged(query: String) {
        _findQuery.value = query
        _webCommands.tryEmit(WebCommand.FindInPage(query, forward = true))
    }

    fun findNext() {
        _webCommands.tryEmit(WebCommand.FindInPage(_findQuery.value, forward = true))
    }

    fun findPrevious() {
        _webCommands.tryEmit(WebCommand.FindInPage(_findQuery.value, forward = false))
    }

    fun setFindMatches(current: Int, total: Int) {
        _findMatches.value = if (total > 0) "${current + 1}/$total" else "0/0"
    }

    fun toggleBookmarkCurrent() {
        val tab = activeTab ?: return
        if (tab.url.isBlank() || tab.url.startsWith("about:")) return
        viewModelScope.launch {
            val isBookmarked = repository.isBookmarked(tab.url)
            if (isBookmarked) {
                repository.removeBookmark(tab.url)
                showToast("已移除书签")
            } else {
                repository.addBookmark(tab.title, tab.url)
                showToast("已加入书签: ${tab.title}")
            }
        }
    }

    fun removeBookmark(url: String) {
        viewModelScope.launch {
            repository.removeBookmark(url)
            showToast("书签已删除")
        }
    }

    fun recordHistory(title: String, url: String) {
        if (_isIncognito.value) return
        viewModelScope.launch {
            repository.recordHistory(title, url)
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showToast("历史记录已全部清空")
        }
    }

    fun clearCacheAndCookies() {
        viewModelScope.launch {
            _webCommands.tryEmit(WebCommand.ClearCacheAndCookies)
            showToast("缓存与 Cookies 已清除")
        }
    }

    // Userscript operations
    fun toggleScript(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleScript(id, isEnabled)
            showToast(if (isEnabled) "已启用油猴脚本" else "已停用油猴脚本")
        }
    }

    fun saveScript(script: UserscriptEntity) {
        viewModelScope.launch {
            repository.saveScript(script)
            showToast("油猴脚本保存成功: ${script.name}")
        }
    }

    fun deleteScript(id: Long) {
        viewModelScope.launch {
            repository.deleteScript(id)
            showToast("油猴脚本已删除")
        }
    }

    fun onScriptInjected(script: UserscriptEntity) {
        // Can optionally log or notify
    }

    fun setUserscriptDialogVisible(visible: Boolean) {
        _showUserscriptDialog.value = visible
    }

    fun setBookmarksDialogVisible(visible: Boolean) {
        _showBookmarksDialog.value = visible
    }

    fun setHistoryDialogVisible(visible: Boolean) {
        _showHistoryDialog.value = visible
    }

    fun setSettingsDialogVisible(visible: Boolean) {
        _showSettingsDialog.value = visible
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
