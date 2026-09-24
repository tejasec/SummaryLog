package com.ts.summarylog.ui.conversations

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ts.summarylog.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConversationFeedScreen(
    database: AppDatabase,
    onSelectThread: (packageName: String, title: String) -> Unit,
) {
    val dao = database.notificationDao()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    val conversations by dao.getConversations(searchQuery).collectAsState(initial = emptyList())

    val selectedKeys = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedKeys.isNotEmpty()

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedKeys.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedKeys.clear() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            scope.launch(Dispatchers.IO) {
                                selectedKeys.forEach { key ->
                                    val parts = key.split("||")
                                    if (parts.size == 2) dao.deleteConversation(parts[0], parts[1])
                                }
                                selectedKeys.clear()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                        }
                    },
                )
            } else {
                TopAppBar(
                    title = { Text("Conversations") },
                    actions = {
                        IconButton(onClick = { scope.launch(Dispatchers.IO) { dao.markAllAsRead() } }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark all read")
                        }
                    },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search notifications...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(conversations, key = { "${it.packageName}||${it.title}" }) { thread ->
                    val itemKey = "${thread.packageName}||${thread.title}"
                    val isSelected = selectedKeys.contains(itemKey)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (isSelectionMode) {
                                        if (isSelected) selectedKeys.remove(itemKey) else selectedKeys.add(itemKey)
                                    } else {
                                        onSelectThread(thread.packageName, thread.title)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) selectedKeys.add(itemKey)
                                },
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isSelectionMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked == true) selectedKeys.add(itemKey) else selectedKeys.remove(itemKey)
                                },
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(thread.title, fontWeight = FontWeight.SemiBold)
                            Text(thread.lastMessage, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }

                        if (thread.unreadCount > 0 && !isSelectionMode) {
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text("${thread.unreadCount}")
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}
