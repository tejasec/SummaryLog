package com.ts.summarylog

data class NotificationItem(
    val id: Long = System.currentTimeMillis(),
    val packageName: String = "",
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
