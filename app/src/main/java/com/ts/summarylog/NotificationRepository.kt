package com.ts.summarylog

import android.content.Context
import com.ts.summarylog.data.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object NotificationRepository {
    fun getNotifications(context: Context): Flow<List<NotificationItem>> {
        val dao = AppDatabase.getDatabase(context).notificationDao()
        return dao.getAllNotifications().map { entities ->
            entities.map { entity ->
                NotificationItem(
                    id = entity.id,
                    packageName = entity.packageName,
                    appName = entity.appName,
                    title = entity.title,
                    text = entity.text,
                    timestamp = entity.timestamp
                )
            }
        }
    }
}
