package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "userscripts")
data class UserscriptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val version: String = "1.0",
    val author: String = "User",
    val matchPatterns: String = "*://*/*", // Comma- or newline-separated match patterns
    val runAt: String = "document-end", // "document-start" or "document-end"
    val code: String,
    val isEnabled: Boolean = true,
    val isBuiltIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
