package com.example.data.local

import com.example.userscript.BuiltinScripts
import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val database: BrowserDatabase) {

    private val userscriptDao = database.userscriptDao()
    private val bookmarkDao = database.bookmarkDao()
    private val historyDao = database.historyDao()

    val allScripts: Flow<List<UserscriptEntity>> = userscriptDao.getAllScripts()
    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    suspend fun ensureDefaultScripts() {
        val count = userscriptDao.getScriptCount()
        if (count == 0) {
            userscriptDao.insertScripts(BuiltinScripts.getPreloadedScripts())
        }
    }

    suspend fun getEnabledScriptsDirect(): List<UserscriptEntity> {
        return userscriptDao.getEnabledScriptsDirect()
    }

    suspend fun saveScript(script: UserscriptEntity): Long {
        return userscriptDao.insertScript(script)
    }

    suspend fun updateScript(script: UserscriptEntity) {
        userscriptDao.updateScript(script)
    }

    suspend fun toggleScript(id: Long, isEnabled: Boolean) {
        userscriptDao.updateScriptEnabled(id, isEnabled)
    }

    suspend fun deleteScript(id: Long) {
        userscriptDao.deleteScriptById(id)
    }

    suspend fun addBookmark(title: String, url: String) {
        bookmarkDao.insertBookmark(BookmarkEntity(title = title.ifBlank { url }, url = url))
    }

    suspend fun removeBookmark(url: String) {
        bookmarkDao.deleteBookmarkByUrl(url)
    }

    suspend fun isBookmarked(url: String): Boolean {
        return bookmarkDao.isBookmarkedDirect(url)
    }

    suspend fun recordHistory(title: String, url: String) {
        if (url.isNotBlank() && !url.startsWith("about:") && !url.startsWith("chrome:")) {
            historyDao.insertHistory(HistoryEntity(title = title.ifBlank { url }, url = url))
        }
    }

    suspend fun deleteHistory(id: Long) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }
}
