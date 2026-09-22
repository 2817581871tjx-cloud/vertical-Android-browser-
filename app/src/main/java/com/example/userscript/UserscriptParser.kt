package com.example.userscript

import com.example.data.local.UserscriptEntity

data class ParsedMetadata(
    val name: String,
    val description: String,
    val version: String,
    val author: String,
    val matchPatterns: List<String>,
    val runAt: String,
    val rawCode: String
)

object UserscriptParser {
    fun parseScript(fullCode: String): ParsedMetadata {
        val metadataRegex = Regex("""//\s*==UserScript==([\s\S]*?)//\s*==/UserScript==""", RegexOption.IGNORE_CASE)
        val matchResult = metadataRegex.find(fullCode)

        var name = "未命名油猴脚本"
        var description = "自定义油猴脚本"
        var version = "1.0"
        var author = "User"
        val matches = mutableListOf<String>()
        var runAt = "document-end"

        if (matchResult != null) {
            val metaLines = matchResult.groupValues[1].lines()
            for (line in metaLines) {
                val trimmed = line.trim()
                if (!trimmed.startsWith("//")) continue
                val content = trimmed.removePrefix("//").trim()
                if (!content.startsWith("@")) continue

                val parts = content.split(Regex("\\s+"), limit = 2)
                val key = parts.getOrNull(0)?.lowercase() ?: ""
                val value = parts.getOrNull(1)?.trim() ?: ""

                when (key) {
                    "@name" -> if (value.isNotBlank()) name = value
                    "@description" -> if (value.isNotBlank()) description = value
                    "@version" -> if (value.isNotBlank()) version = value
                    "@author" -> if (value.isNotBlank()) author = value
                    "@match", "@include" -> if (value.isNotBlank()) matches.add(value)
                    "@run-at" -> {
                        if (value.contains("start", ignoreCase = true)) {
                            runAt = "document-start"
                        } else {
                            runAt = "document-end"
                        }
                    }
                }
            }
        }

        if (matches.isEmpty()) {
            matches.add("*://*/*")
        }

        return ParsedMetadata(
            name = name,
            description = description,
            version = version,
            author = author,
            matchPatterns = matches,
            runAt = runAt,
            rawCode = fullCode
        )
    }

    fun toEntity(metadata: ParsedMetadata): UserscriptEntity {
        return UserscriptEntity(
            name = metadata.name,
            description = metadata.description,
            version = metadata.version,
            author = metadata.author,
            matchPatterns = metadata.matchPatterns.joinToString(","),
            runAt = metadata.runAt,
            code = metadata.rawCode,
            isEnabled = true,
            isBuiltIn = false
        )
    }
}
