package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.UserscriptEntity
import com.example.model.BrowserTab
import com.example.userscript.UserscriptEngine
import com.example.viewmodel.WebCommand
import kotlinx.coroutines.flow.SharedFlow

/**
 * Dynamically derives Desktop User Agent from the Android system's real WebView User Agent.
 * This preserves the real system WebView / Chrome version (e.g., Chrome 133, 134) instead of
 * hardcoding an obsolete version like 128.0.
 */
private fun getDesktopUserAgent(context: Context): String {
    val defaultUA = try {
        WebSettings.getDefaultUserAgent(context)
    } catch (e: Exception) {
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Safari/537.36"
    }
    return defaultUA
        .replace("Mobile Safari", "Safari")
        .replace("Mobile", "")
        .replace(Regex("\\(Linux;.*Android[^;)]*\\)"), "(X11; Linux x86_64)")
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    tabs: List<BrowserTab>,
    activeTab: BrowserTab?,
    userscripts: List<UserscriptEntity>,
    webCommands: SharedFlow<WebCommand>,
    onTabUrlChanged: (String, String) -> Unit,
    onTabTitleChanged: (String, String) -> Unit,
    onTabLoadingChanged: (String, Boolean, Int) -> Unit,
    onTabNavigationChanged: (String, Boolean, Boolean) -> Unit,
    onTabFaviconChanged: (String, Bitmap?) -> Unit,
    onFindMatchesFound: (Int, Int) -> Unit,
    onScriptInjected: (UserscriptEntity) -> Unit,
    onRecordHistory: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val webViewPool = remember { mutableStateMapOf<String, WebView>() }
    var customVideoView by remember { mutableStateOf<View?>(null) }
    var customVideoCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    // Clean up closed tabs from pool and view hierarchy
    val currentTabIds = remember(tabs) { tabs.map { it.id }.toSet() }
    LaunchedEffect(currentTabIds) {
        val deadTabIds = webViewPool.keys.filter { !currentTabIds.contains(it) }
        for (deadId in deadTabIds) {
            webViewPool.remove(deadId)?.apply {
                (parent as? ViewGroup)?.removeView(this)
                stopLoading()
                clearHistory()
                loadUrl("about:blank")
                destroy()
            }
        }
    }

    // Helper to configure a WebView with the device's native system WebView engine
    fun getOrCreateWebView(tab: BrowserTab): WebView {
        return webViewPool.getOrPut(tab.id) {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // CRITICAL FIX FOR BLACK SCREEN:
                // Explicitly set background color to WHITE so WebView hardware canvas never renders black while loading
                setBackgroundColor(android.graphics.Color.WHITE)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = !tab.isIncognito
                    databaseEnabled = !tab.isIncognito
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    allowFileAccess = !tab.isIncognito
                    allowContentAccess = true
                    mediaPlaybackRequiresUserGesture = false

                    // If incognito mode is active: do not cache or save forms
                    if (tab.isIncognito) {
                        cacheMode = WebSettings.LOAD_NO_CACHE
                        saveFormData = false
                        setGeolocationEnabled(false)
                    } else {
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    // ACCURATE SYSTEM WEBVIEW KERNEL:
                    // When isDesktopMode is false: userAgentString = null directly uses the system's exact WebView UA
                    // When isDesktopMode is true: derives desktop UA from the system default UA preserving real Chrome version
                    userAgentString = if (tab.isDesktopMode) getDesktopUserAgent(context) else null
                }

                try {
                    val cookieManager = CookieManager.getInstance()
                    if (tab.isIncognito) {
                        cookieManager.setAcceptThirdPartyCookies(this, false)
                    } else {
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)
                    }
                } catch (e: Exception) {
                    // Ignore cookie initialization exception
                }

                setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
                    onFindMatchesFound(activeMatchOrdinal, numberOfMatches)
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        url?.let {
                            onTabUrlChanged(tab.id, it)
                            onTabLoadingChanged(tab.id, true, 15)
                            // Inject document-start scripts
                            if (view != null) {
                                UserscriptEngine.injectScripts(
                                    webView = view,
                                    url = it,
                                    scripts = userscripts,
                                    runAt = "document-start",
                                    onScriptInjected = onScriptInjected
                                )
                            }
                        }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        url?.let {
                            onTabUrlChanged(tab.id, it)
                            onTabLoadingChanged(tab.id, false, 100)
                            onTabNavigationChanged(tab.id, canGoBack(), canGoForward())
                            val pageTitle = title ?: it
                            onTabTitleChanged(tab.id, pageTitle)
                            if (!tab.isIncognito) {
                                onRecordHistory(pageTitle, it)
                            }

                            // Inject document-end scripts
                            if (view != null) {
                                UserscriptEngine.injectScripts(
                                    webView = view,
                                    url = it,
                                    scripts = userscripts,
                                    runAt = "document-end",
                                    onScriptInjected = onScriptInjected
                                )
                            }
                        }
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val requestUrl = request?.url?.toString() ?: return false
                        // Keep http/https in WebView
                        return if (requestUrl.startsWith("http://") || requestUrl.startsWith("https://")) {
                            false
                        } else {
                            try {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    Uri.parse(requestUrl)
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Ignore unhandled schemes
                            }
                            true
                        }
                    }

                    override fun onRenderProcessGone(
                        view: WebView?,
                        detail: RenderProcessGoneDetail?
                    ): Boolean {
                        view?.let {
                            webViewPool.remove(tab.id)
                            (it.parent as? ViewGroup)?.removeView(it)
                            try {
                                it.stopLoading()
                                it.destroy()
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }
                        return true
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        onTabLoadingChanged(tab.id, newProgress < 100, newProgress)
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        title?.let { onTabTitleChanged(tab.id, it) }
                    }

                    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                        super.onReceivedIcon(view, icon)
                        onTabFaviconChanged(tab.id, icon)
                    }

                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        customVideoView = view
                        customVideoCallback = callback
                    }

                    override fun onHideCustomView() {
                        customVideoView = null
                        customVideoCallback?.onCustomViewHidden()
                        customVideoCallback = null
                    }
                }

                // Initial load
                if (tab.url.isNotBlank() && tab.url != "about:blank") {
                    loadUrl(tab.url)
                }
            }
        }
    }

    // Handle incoming WebCommands from ViewModel
    LaunchedEffect(activeTab?.id) {
        webCommands.collect { cmd ->
            val targetTabId = when (cmd) {
                is WebCommand.LoadUrl -> cmd.tabId
                is WebCommand.GoBack -> cmd.tabId
                is WebCommand.GoForward -> cmd.tabId
                is WebCommand.Reload -> cmd.tabId
                is WebCommand.StopLoading -> cmd.tabId
                is WebCommand.TranslatePage -> cmd.tabId
                is WebCommand.RestoreOriginalPage -> cmd.tabId
                else -> activeTab?.id
            }

            val webView = targetTabId?.let { webViewPool[it] } ?: return@collect

            when (cmd) {
                is WebCommand.LoadUrl -> webView.loadUrl(cmd.url)
                is WebCommand.GoBack -> if (webView.canGoBack()) webView.goBack()
                is WebCommand.GoForward -> if (webView.canGoForward()) webView.goForward()
                is WebCommand.Reload -> webView.reload()
                is WebCommand.StopLoading -> webView.stopLoading()
                is WebCommand.FindInPage -> {
                    if (cmd.query.isNotBlank()) {
                        webView.findAllAsync(cmd.query)
                        webView.findNext(cmd.forward)
                    }
                }
                is WebCommand.ClearFindMatches -> webView.clearMatches()
                is WebCommand.ClearCacheAndCookies -> {
                    webView.clearCache(true)
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                }
                is WebCommand.TranslatePage -> {
                    // Inject Google Translate element script directly into webpage
                    val script = """
                        javascript:(function() {
                            if (window.__lefttab_translate_loaded) {
                                var combo = document.querySelector('.goog-te-combo');
                                if (combo) {
                                    combo.value = '${cmd.targetLanguage}';
                                    combo.dispatchEvent(new Event('change'));
                                }
                                return;
                            }
                            window.__lefttab_translate_loaded = true;
                            window.googleTranslateElementInit = function() {
                                try {
                                    new google.translate.TranslateElement({
                                        pageLanguage: 'auto',
                                        includedLanguages: 'zh-CN,en,ja,ko,fr,de,es,ru,it',
                                        layout: google.translate.TranslateElement.InlineLayout.SIMPLE,
                                        autoDisplay: false
                                    }, 'google_translate_float_box');
                                    setTimeout(function() {
                                        var combo = document.querySelector('.goog-te-combo');
                                        if (combo) {
                                            combo.value = '${cmd.targetLanguage}';
                                            combo.dispatchEvent(new Event('change'));
                                        }
                                    }, 600);
                                } catch(e){}
                            };
                            var box = document.getElementById('google_translate_float_box');
                            if (!box) {
                                box = document.createElement('div');
                                box.id = 'google_translate_float_box';
                                box.style.cssText = 'position:fixed;bottom:20px;right:20px;z-index:999999;background:#fff;border:1px solid #1a73e8;border-radius:12px;padding:6px 12px;box-shadow:0 4px 16px rgba(0,0,0,0.2);';
                                document.body.appendChild(box);
                            }
                            var s = document.createElement('script');
                            s.src = 'https://translate.google.com/translate_a/element.js?cb=googleTranslateElementInit';
                            document.head.appendChild(s);
                        })();
                    """.trimIndent()
                    webView.loadUrl(script)
                }
                is WebCommand.RestoreOriginalPage -> {
                    val script = """
                        javascript:(function() {
                            var combo = document.querySelector('.goog-te-combo');
                            if (combo) {
                                combo.value = '';
                                combo.dispatchEvent(new Event('change'));
                            }
                            window.location.reload();
                        })();
                    """.trimIndent()
                    webView.loadUrl(script)
                }
            }
        }
    }

    // Update UA if isDesktopMode changes on activeTab
    LaunchedEffect(activeTab?.isDesktopMode) {
        val tab = activeTab ?: return@LaunchedEffect
        val webView = webViewPool[tab.id] ?: return@LaunchedEffect
        val targetUA = if (tab.isDesktopMode) getDesktopUserAgent(context) else null
        if (webView.settings.userAgentString != targetUA) {
            webView.settings.userAgentString = targetUA
        }
    }

    // Fullscreen custom view (for video playback fullscreen)
    if (customVideoView != null) {
        AndroidView(
            factory = {
                FrameLayout(context).apply {
                    addView(
                        customVideoView,
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    // PERSISTENT CONTAINER HOSTING ALL ACTIVE TABS:
    // Avoid tearing down AndroidView on tab switch. This prevents the EGL surface recreation / render node
    // teardown and completely eliminates the brief black screen!
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AndroidView(
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.WHITE)
                }
            },
            update = { container ->
                if (activeTab != null) {
                    val activeWv = getOrCreateWebView(activeTab)

                    // Attach to container if not already attached
                    if (activeWv.parent != container) {
                        (activeWv.parent as? ViewGroup)?.removeView(activeWv)
                        container.addView(activeWv)
                    }

                    // Set active WebView visible and all other tab WebViews gone
                    for (tab in tabs) {
                        val wv = webViewPool[tab.id] ?: continue
                        if (tab.id == activeTab.id) {
                            if (wv.visibility != View.VISIBLE) {
                                wv.visibility = View.VISIBLE
                            }
                            wv.bringToFront()
                        } else {
                            if (wv.visibility != View.GONE) {
                                wv.visibility = View.GONE
                            }
                        }
                    }

                    // Initial or pending navigation
                    if (activeTab.url.isNotBlank() &&
                        activeTab.url != "about:blank" &&
                        activeWv.url != activeTab.url &&
                        !activeTab.isLoading &&
                        activeWv.url == null
                    ) {
                        activeWv.loadUrl(activeTab.url)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Ultra-slim (2.5dp) top edge loading progress line (Zero content obstruction!)
        if (activeTab != null) {
            AnimatedVisibility(
                visible = activeTab.isLoading && activeTab.progress < 100,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    progress = { activeTab.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewPool.values.forEach {
                it.stopLoading()
                it.destroy()
            }
            webViewPool.clear()
        }
    }
}
