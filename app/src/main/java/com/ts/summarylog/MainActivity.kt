package com.ts.summarylog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import com.ts.summarylog.data.AppDatabase
import com.ts.summarylog.ui.conversations.ConversationDetailScreen
import com.ts.summarylog.ui.conversations.ConversationFeedScreen
import com.ts.summarylog.ui.insights.InsightsScreen
import com.ts.summarylog.ui.settings.AppWhitelistScreen
import com.ts.summarylog.ui.theme.SummaryLogTheme
import com.ts.summarylog.worker.NotificationPruneWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationPruneWorker.schedule(this)
        setContent {
            SummaryLogTheme {
                SummaryLogRootScreen()
            }
        }
    }
}

fun isNotificationAccessGranted(context: Context): Boolean {
    return NotificationManagerCompat.getEnabledListenerPackages(context)
        .contains(context.packageName)
}

fun openNotificationAccessSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
}

enum class AppTab(val title: String) {
    HOME("Home"),
    CONVERSATIONS("Chats"),
    INSIGHTS("Insights"),
    MONITORED_APPS("Track Apps"),
}

@Composable
fun SummaryLogRootScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }

    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var activeThread by remember { mutableStateOf<Pair<String, String>?>(null) }

    if (activeThread != null) {
        val (pkg, title) = activeThread!!
        ConversationDetailScreen(
            packageName = pkg,
            title = title,
            onNavigateBack = { activeThread = null },
        )
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.HOME,
                        onClick = { currentTab = AppTab.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(AppTab.HOME.title) },
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.CONVERSATIONS,
                        onClick = { currentTab = AppTab.CONVERSATIONS },
                        icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                        label = { Text(AppTab.CONVERSATIONS.title) },
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.INSIGHTS,
                        onClick = { currentTab = AppTab.INSIGHTS },
                        icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                        label = { Text(AppTab.INSIGHTS.title) },
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.MONITORED_APPS,
                        onClick = { currentTab = AppTab.MONITORED_APPS },
                        icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                        label = { Text(AppTab.MONITORED_APPS.title) },
                    )
                }
            },
        ) { padding ->
            Surface(modifier = Modifier.padding(padding)) {
                when (currentTab) {
                    AppTab.HOME -> SummaryLogMainScreen()
                    AppTab.CONVERSATIONS -> ConversationFeedScreen(
                        database = database,
                        onSelectThread = { pkg, title -> activeThread = Pair(pkg, title) },
                    )
                    AppTab.INSIGHTS -> InsightsScreen(database = database)
                    AppTab.MONITORED_APPS -> AppWhitelistScreen()
                }
            }
        }
    }
}

@Composable
fun SummaryLogApp() {
    SummaryLogRootScreen()
}
