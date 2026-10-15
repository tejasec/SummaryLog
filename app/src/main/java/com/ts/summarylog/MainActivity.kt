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

import android.app.ActivityManager
import android.content.ComponentName
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.text.TextUtils

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
    val fromCompat = try {
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    } catch (_: Exception) {
        false
    }
    if (fromCompat) return true

    return try {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            flat.split(":").any { item ->
                val component = ComponentName.unflattenFromString(item)
                component?.packageName == context.packageName
            }
        } else {
            false
        }
    } catch (_: Exception) {
        false
    }
}

fun openNotificationAccessSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Fallback for Android Go or stripped ROMs where listener settings activity doesn't exist
        openAppDetailsSettings(context)
    }
}

fun openAppDetailsSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val genericSettings = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(genericSettings)
        } catch (_: Exception) {}
    }
}

fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } catch (_: Exception) {
            false
        }
    } else {
        true
    }
}

fun requestIgnoreBatteryOptimizations(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (_: Exception) {
                openAppDetailsSettings(context)
            }
        }
    }
}

fun openOemAutoStartSettings(context: Context): Boolean {
    val oemIntents = listOf(
        Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
        Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings")),
        Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
        Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
        Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
        Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
        Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
        Intent().setComponent(ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity"))
    )

    for (intent in oemIntents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (context.packageManager.resolveActivity(intent, 0) != null) {
                context.startActivity(intent)
                return true
            }
        } catch (_: Exception) {}
    }
    openAppDetailsSettings(context)
    return false
}

fun isLowRamDevice(context: Context): Boolean {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    return am?.isLowRamDevice ?: false
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
