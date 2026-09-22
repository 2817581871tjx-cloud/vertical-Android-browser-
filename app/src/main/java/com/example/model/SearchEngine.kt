package com.example.model

enum class SearchEngine(
    val displayName: String,
    val searchUrlPrefix: String,
    val homeUrl: String,
    val iconName: String
) {
    BING("必应 (Bing)", "https://www.bing.com/search?q=", "https://www.bing.com", "B"),
    BAIDU("百度 (Baidu)", "https://www.baidu.com/s?wd=", "https://www.baidu.com", "百"),
    GOOGLE("Google", "https://www.google.com/search?q=", "https://www.google.com", "G"),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=", "https://duckduckgo.com", "D");

    fun buildQueryUrl(query: String): String {
        val trimmed = query.trim()
        return if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("file://", ignoreCase = true)
        ) {
            trimmed
        } else if (trimmed.contains(".") && !trimmed.contains(" ") && trimmed.length > 3) {
            "https://$trimmed"
        } else {
            searchUrlPrefix + java.net.URLEncoder.encode(trimmed, "UTF-8")
        }
    }
}
