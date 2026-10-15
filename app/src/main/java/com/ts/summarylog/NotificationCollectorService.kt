package com.ts.summarylog

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

    override fun onListenerConnected() {
        super.onListenerConnected()
        val prefs = applicationContext.getSharedPreferences("summarylog_settings", Context.MODE_PRIVATE)
        val isPersistentEnabled = prefs.getBoolean(PREF_PERSISTENT_NOTIFICATION, false)
        if (isPersistentEnabled) {
            updateOngoingNotification(applicationContext, true)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                requestRebind(ComponentName(this, NotificationCollectorService::class.java))
            } catch (_: Exception) {}
        }
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
            try {
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
            } catch (_: Exception) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val PREF_PERSISTENT_NOTIFICATION = "persistent_notification"
        const val CHANNEL_ID = "summarylog_monitor_channel"
        const val NOTIFICATION_ID = 2001

        fun updateOngoingNotification(context: Context, isEnabled: Boolean) {
            val notificationManager = NotificationManagerCompat.from(context)

            if (!isEnabled) {
                try {
                    notificationManager.cancel(NOTIFICATION_ID)
                } catch (_: Exception) {}
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Notification Monitoring Status",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Displays when SummaryLog is actively monitoring notifications in background"
                    setShowBadge(false)
                }
                val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                systemNotificationManager?.createNotificationChannel(channel)
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("SummaryLog Active")
                .setContentText("Monitoring notifications in background")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .build()

            try {
                notificationManager.notify(NOTIFICATION_ID, notification)
            } catch (_: SecurityException) {
                // On Android 13+, POST_NOTIFICATIONS may need to be granted first
            } catch (_: Exception) {}
        }

        fun forceRebind(context: Context) {
            val componentName = ComponentName(context, NotificationCollectorService::class.java)
            val pm = context.packageManager
            try {
                pm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    NotificationListenerService.requestRebind(componentName)
                }
            } catch (_: Exception) {}
        }
    }
}
