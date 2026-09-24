package com.ts.summarylog.ui.conversations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.ts.summarylog.data.AppDatabase
import com.ts.summarylog.data.ConversationMetaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ConversationThreadsContainerScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val conversations by database.notificationDao().getConversationThreads().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    var selectedThread by remember { mutableStateOf<Pair<String, String>?>(null) }

    val currentSelection = selectedThread
    if (currentSelection != null) {
        ConversationDetailScreen(
            packageName = currentSelection.first,
            title = currentSelection.second,
            onNavigateBack = { selectedThread = null }
        )
    } else {
        ConversationListScreen(
            conversations = conversations,
            onSelectThread = { pkg, title ->
                selectedThread = Pair(pkg, title)
            },
            onTogglePin = { thread ->
                coroutineScope.launch(Dispatchers.IO) {
                    val id = "${thread.packageName}_${thread.title}"
                    database.notificationDao().setConversationMeta(
                        ConversationMetaEntity(id = id, isPinned = !thread.isPinned)
                    )
                }
            },
            onDeleteConversation = { thread ->
                coroutineScope.launch(Dispatchers.IO) {
                    database.notificationDao().deleteConversation(thread.packageName, thread.title)
                }
            }
        )
    }
}
