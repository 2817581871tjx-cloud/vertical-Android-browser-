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
    val isDesktopMode: Boolean = true, // Default to Desktop Mode for tablets for rich desktop websites!
    val favicon: Bitmap? = null
)
