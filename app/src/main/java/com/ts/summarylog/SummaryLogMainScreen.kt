package com.ts.summarylog

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ts.summarylog.ai.GeminiChatHelper
import com.ts.summarylog.ai.PrototypeChatEngine
import com.ts.summarylog.data.AppDatabase
import com.ts.summarylog.data.ChatMessageEntity
import com.ts.summarylog.ui.ChatSectionHeader
import com.ts.summarylog.ui.RichMarkdownContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun SummaryLogMainScreen(
    context: Context = LocalContext.current
) {
    val database = remember(context) { AppDatabase.getDatabase(context) }
    val chatDao = database.chatDao()

    val chatMessages by chatDao.getAllMessages().collectAsState(initial = emptyList())
    val notifications by NotificationRepository.getNotifications(context).collectAsState(initial = emptyList())
    val hasPermission = remember { mutableStateOf(isNotificationAccessGranted(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission.value = isNotificationAccessGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var activeStreamingId by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val chatListState = rememberLazyListState()

    val apiKey = BuildConfig.GEMINI_API_KEY
    val geminiHelper = remember(apiKey) {
        if (apiKey.isNotBlank() && apiKey != "YOUR_GEMINI_API_KEY") {
            GeminiChatHelper(apiKey = apiKey)
        } else {
            null
        }
    }

    val onSendMessage: () -> Unit = {
        val prompt = inputText.trim()
        if (prompt.isNotBlank() && !isGenerating) {
            inputText = ""
            isGenerating = true

            coroutineScope.launch(Dispatchers.IO) {
                // 1. Insert user query into Room
                val userMsg = ChatMessageEntity(text = prompt, isUser = true)
                chatDao.insertMessage(userMsg)

                // 2. Insert placeholder assistant response into Room
                val assistantMsgId = UUID.randomUUID().toString()
                activeStreamingId = assistantMsgId
                var accumulatedText = ""
                chatDao.insertMessage(ChatMessageEntity(id = assistantMsgId, text = "", isUser = false))

                withContext(Dispatchers.Main) {
                    chatListState.animateScrollToItem((chatMessages.size).coerceAtLeast(0))
                }

                try {
                    if (geminiHelper != null) {
                        geminiHelper.streamQueryWithNotifications(prompt, notifications)
                            .collect { chunk ->
                                accumulatedText += chunk
                                chatDao.updateMessage(
                                    ChatMessageEntity(id = assistantMsgId, text = accumulatedText, isUser = false)
                                )
                                withContext(Dispatchers.Main) {
                                    chatListState.scrollToItem((chatMessages.size - 1).coerceAtLeast(0))
                                }
                            }
                    } else {
                        val reply = PrototypeChatEngine.generateReply(prompt, notifications)
                        chatDao.updateMessage(
                            ChatMessageEntity(id = assistantMsgId, text = reply, isUser = false)
                        )
                        withContext(Dispatchers.Main) {
                            chatListState.scrollToItem((chatMessages.size - 1).coerceAtLeast(0))
                        }
                    }
                } catch (e: Exception) {
                    chatDao.updateMessage(
                        ChatMessageEntity(
                            id = assistantMsgId,
                            text = "Error: ${e.localizedMessage ?: "Unknown error"}",
                            isUser = false
                        )
                    )
                } finally {
                    withContext(Dispatchers.Main) {
                        isGenerating = false
                        activeStreamingId = null
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .systemBarsPadding()
    ) {
        // --- TOP SECTION: Incoming Notifications Feed ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            var showRetentionDialog by remember { mutableStateOf(false) }
            val prefs = remember { context.getSharedPreferences("summarylog_settings", Context.MODE_PRIVATE) }
            var retentionDays by remember { mutableStateOf(prefs.getInt("retention_days", 30)) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Notifications (${notifications.size})",
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = { showRetentionDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Auto-delete retention settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showRetentionDialog) {
                AlertDialog(
                    onDismissRequest = { showRetentionDialog = false },
                    title = { Text("Auto-Delete Retention") },
                    text = {
                        Column {
                            Text(
                                "Automatically prune notifications older than the selected duration daily:",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            val options = listOf(
                                30 to "30 Days",
                                60 to "60 Days",
                                90 to "90 Days",
                                0 to "Never"
                            )
                            options.forEach { (days, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            retentionDays = days
                                            prefs.edit().putInt("retention_days", days).apply()
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (retentionDays == days),
                                        onClick = {
                                            retentionDays = days
                                            prefs.edit().putInt("retention_days", days).apply()
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(label, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showRetentionDialog = false }) {
                            Text("Done")
                        }
                    }
                )
            }

            var searchQuery by remember { mutableStateOf("") }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search notifications...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            val displayedNotifications = remember(notifications, searchQuery) {
                if (searchQuery.isBlank()) {
                    notifications
                } else {
                    notifications.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.text.contains(searchQuery, ignoreCase = true) ||
                        it.appName.contains(searchQuery, ignoreCase = true)
                    }
                }
            }

            if (!hasPermission.value) {
                Button(
                    onClick = {
                        openNotificationAccessSettings(context)
                        hasPermission.value = isNotificationAccessGranted(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text("Grant Notification Access")
                }
            }

            if (displayedNotifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No matching notifications" else "No notifications recorded yet",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(displayedNotifications, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(item.appName.uppercase(), style = MaterialTheme.typography.labelSmall)
                                Text(item.title, style = MaterialTheme.typography.titleSmall)
                                Text(item.text, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // --- BOTTOM SECTION: Resizable Summary Logs Window ---
        ResizableSummaryLogsWindow {
            Column(modifier = Modifier.fillMaxSize()) {
                ChatSectionHeader(
                    messageCount = chatMessages.size,
                    onClearChatConfirmed = {
                        coroutineScope.launch(Dispatchers.IO) {
                            chatDao.clearChat()
                        }
                    }
                )

                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatMessages, key = { it.id }) { message ->
                        ChatBubble(
                            message = message,
                            isStreaming = (message.id == activeStreamingId)
                        )
                    }
                }

                // Input bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask about your notifications...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !isGenerating,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onSendMessage,
                        enabled = !isGenerating && inputText.isNotBlank(),
                        modifier = Modifier.background(
                            color = if (isGenerating) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(50)
                        )
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.ResizableSummaryLogsWindow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val windowPrefs = remember { context.getSharedPreferences("summarylog_window_settings", Context.MODE_PRIVATE) }

    var widthFraction by rememberSaveable {
        mutableFloatStateOf(windowPrefs.getFloat("window_width_fraction", 0.80f).coerceIn(0.0f, 0.80f))
    }
    var heightWeight by rememberSaveable {
        mutableFloatStateOf(windowPrefs.getFloat("window_height_weight", 1.20f).coerceIn(0.50f, 2.50f))
    }
    var isResizing by remember { mutableStateOf(false) }

    fun saveWindowSettings() {
        windowPrefs.edit()
            .putFloat("window_width_fraction", widthFraction)
            .putFloat("window_height_weight", heightWeight)
            .apply()
    }

    Column(
        modifier = modifier
            .weight(heightWeight)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top vertical drag handle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(
                    if (isResizing) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { isResizing = true },
                        onDragEnd = {
                            isResizing = false
                            saveWindowSettings()
                        },
                        onDragCancel = {
                            isResizing = false
                            saveWindowSettings()
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        val deltaWeight = -dragAmount / 300f
                        heightWeight = (heightWeight + deltaWeight).coerceIn(0.50f, 2.50f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        color = if (isResizing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }

        // Summary Logs window covering screen till edges
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = if (isResizing) BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)) else null,
                tonalElevation = if (isResizing) 8.dp else 2.dp
            ) {
                content()
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessageEntity,
    isStreaming: Boolean = false
) {
    val isUser = message.isUser
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 1.dp,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                RichMarkdownContent(
                    content = message.text,
                    textColor = textColor,
                    isStreaming = isStreaming
                )

                // Message footer: copy action
                if (!isStreaming && message.text.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(message.text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy message",
                                tint = textColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
