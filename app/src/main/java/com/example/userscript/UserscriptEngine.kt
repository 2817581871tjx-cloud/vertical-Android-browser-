package com.example.userscript

import android.webkit.WebView
import com.example.data.local.UserscriptEntity
import java.util.regex.Pattern

object UserscriptEngine {

    // Checks if a URL matches any of the patterns defined in the Userscript.
    fun matchesUrl(patternsStr: String, url: String): Boolean {
        if (url.isBlank() || url.startsWith("about:") || url.startsWith("chrome:") || url.startsWith("data:")) {
            return false
        }

        val patterns = patternsStr.split(',', '\n').map { it.trim() }.filter { it.isNotEmpty() }
        if (patterns.isEmpty()) return true

        for (pattern in patterns) {
            if (pattern == "<all_urls>" || pattern == "*" || pattern == "*://*/*") {
                return true
            }

            try {
                val regexPattern = patternToRegex(pattern)
                val matcher = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE).matcher(url)
                if (matcher.matches()) {
                    return true
                }
            } catch (e: Exception) {
                // If regex parsing fails, fallback to simple contains check
                val cleanPattern = pattern.replace("*", "")
                if (cleanPattern.isNotEmpty() && url.contains(cleanPattern)) {
                    return true
                }
            }
        }
        return false
    }

    private fun patternToRegex(pattern: String): String {
        var p = pattern
        // Scheme conversion: *:// -> https?://
        p = if (p.startsWith("*://")) {
            "^https?://" + p.substring(4)
        } else if (p.startsWith("http://") || p.startsWith("https://")) {
            "^" + p
        } else {
            "^" + p
        }

        // Escape regex special chars except *
        val escaped = StringBuilder()
        for (ch in p) {
            when (ch) {
                '.' -> escaped.append("\\.")
                '?' -> escaped.append("\\?")
                '+' -> escaped.append("\\+")
                '[' -> escaped.append("\\[")
                ']' -> escaped.append("\\]")
                '(' -> escaped.append("\\(")
                ')' -> escaped.append("\\)")
                '{' -> escaped.append("\\{")
                '}' -> escaped.append("\\}")
                '^' -> escaped.append("^")
                '$' -> escaped.append("\\$")
                '|' -> escaped.append("\\|")
                '*' -> escaped.append(".*")
                else -> escaped.append(ch)
            }
        }
        escaped.append("$")
        return escaped.toString()
    }

    // Injects matching enabled scripts into the WebView.
    fun injectScripts(
        webView: WebView,
        url: String,
        scripts: List<UserscriptEntity>,
        runAt: String,
        onScriptInjected: ((UserscriptEntity) -> Unit)? = null
    ) {
        val matchingScripts = scripts.filter {
            it.isEnabled && it.runAt.equals(runAt, ignoreCase = true) && matchesUrl(it.matchPatterns, url)
        }

        for (script in matchingScripts) {
            val safeJs = "(function() { try { " + script.code + " } catch(err) { console.error(err); } })();"

            webView.evaluateJavascript(safeJs) {
                onScriptInjected?.invoke(script)
            }
        }
    }
}
