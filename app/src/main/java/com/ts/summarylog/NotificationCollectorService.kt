package com.ts.summarylog

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.ts.summarylog.data.AppDatabase
import com.ts.summarylog.data.NotificationEntity
import com.ts.summarylog.util.NotificationImageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NotificationCollectorService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val packageName = sbn.packageName
        if (packageName == applicationContext.packageName) return // Ignore self

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE)?.trim() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""

        // Discard empty or noise-only alerts
        if (title.isBlank() && text.isBlank()) return

        serviceScope.launch {
            // Check whitelist condition
            val monitoredCount = database.monitoredAppDao().getMonitoredAppsCount()
            if (monitoredCount > 0) {
                val isMonitored = database.monitoredAppDao().isAppMonitored(packageName)
                if (!isMonitored) return@launch
            }

            val appName = try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                packageName.substringAfterLast('.')
            }

            val savedImagePath = NotificationImageHelper.extractAndSaveImage(applicationContext, extras)

            database.notificationDao().insert(
                NotificationEntity(
                    packageName = packageName,
                    appName = appName,
                    title = title.ifBlank { appName },
                    text = text,
                    imagePath = savedImagePath,
                    isRead = false,
                    timestamp = sbn.postTime
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
