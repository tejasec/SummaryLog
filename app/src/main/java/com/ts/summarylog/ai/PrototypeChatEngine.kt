package com.ts.summarylog.ai

import com.ts.summarylog.NotificationItem

object PrototypeChatEngine {
    fun generateReply(prompt: String, currentNotifications: List<NotificationItem>): String {
        val query = prompt.lowercase().trim()

        return when {
            query.contains("summarize") -> {
                if (currentNotifications.isEmpty()) {
                    "No notifications found to summarize."
                } else {
                    "Summary of recent alerts:\n" + currentNotifications.take(5).joinToString("\n") {
                        "• [${it.appName.uppercase()}] ${it.title}: ${it.text}"
                    }
                }
            }
            query.contains("otp") || query.contains("code") -> {
                val matches = currentNotifications.filter {
                    it.text.contains(Regex("\\b\\d{4,6}\\b")) || it.text.contains("code", ignoreCase = true)
                }
                if (matches.isEmpty()) {
                    "No verification codes detected in recent notifications."
                } else {
                    "Found codes:\n" + matches.joinToString("\n") { "• ${it.title}: ${it.text}" }
                }
            }
            else -> {
                val matches = currentNotifications.filter {
                    it.text.contains(query, ignoreCase = true) || it.title.contains(query, ignoreCase = true)
                }
                if (matches.isNotEmpty()) {
                    "Found ${matches.size} relevant notification(s):\n" +
                        matches.take(3).joinToString("\n") { "• ${it.title}: ${it.text}" }
                } else {
                    "I couldn't find any notifications matching \"$prompt\". Try asking for a summary or searching for a sender's name."
                }
            }
        }
    }
}
