package com.ts.summarylog.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.ts.summarylog.NotificationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class GeminiChatHelper(apiKey: String) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    fun streamQueryWithNotifications(
        userPrompt: String,
        notifications: List<NotificationItem>
    ): Flow<String> = flow {
        val notificationContext = if (notifications.isEmpty()) {
            "No notifications recorded."
        } else {
            notifications.take(50).joinToString("\n") { item ->
                "- [${item.appName.uppercase()}] ${item.title}: ${item.text}"
            }
        }

        val fullPrompt = """
            You are SummaryLog Assistant. Answer based on these notifications:
            
            $notificationContext
            
            Question: $userPrompt
            
            Provide a clean response using standard Markdown (bolding, bullet points, headers, inline code).
        """.trimIndent()

        generativeModel.generateContentStream(fullPrompt).collect { chunk ->
            chunk.text?.let { emit(it) }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun queryWithNotifications(
        userPrompt: String,
        notifications: List<NotificationItem>
    ): String = withContext(Dispatchers.IO) {
        try {
            val notificationContext = if (notifications.isEmpty()) {
                "No notifications recorded."
            } else {
                notifications.take(50).joinToString("\n") { item ->
                    "- [${item.appName.uppercase()}] ${item.title}: ${item.text}"
                }
            }

            val fullPrompt = """
                You are SummaryLog Assistant, a personal notification assistant.
                
                CURRENT NOTIFICATIONS:
                $notificationContext
                
                USER QUESTION:
                $userPrompt
                
                INSTRUCTIONS:
                Answer the user's question directly based on the notifications above. 
                If asked to summarize, group related notifications by urgency or topic.
                If looking for OTPs or codes, extract and highlight them.
                If the answer isn't in the notifications, state that clearly.
            """.trimIndent()

            val response = generativeModel.generateContent(fullPrompt)
            response.text ?: "I received an empty response from Gemini."
        } catch (e: Exception) {
            "Error contacting Gemini: ${e.localizedMessage ?: "Unknown error"}"
        }
    }
}
