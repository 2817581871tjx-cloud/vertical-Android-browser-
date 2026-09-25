package com.example.model

import android.graphics.Bitmap
import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "新标签页",
    val url: String = "about:blank",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val isDesktopMode: Boolean = false, // Default to Native System WebView User Agent
    val isIncognito: Boolean = false,
    val favicon: Bitmap? = null
)
