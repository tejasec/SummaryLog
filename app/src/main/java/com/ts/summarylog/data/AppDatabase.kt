package com.ts.summarylog.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Entity(
    tableName = "notifications",
    indices = [
        androidx.room.Index(value = ["packageName", "title"]),
        androidx.room.Index(value = ["timestamp"]),
        androidx.room.Index(value = ["isRead"])
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val imagePath: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class AppStat(
    val packageName: String,
    val appName: String,
    val alertCount: Int
)

@Entity(tableName = "monitored_apps")
data class MonitoredAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isMonitored: Boolean = true
)

@Entity(tableName = "conversation_meta")
data class ConversationMetaEntity(
    @PrimaryKey val id: String,
    val isPinned: Boolean = false
)

data class ConversationSummary(
    val packageName: String,
    val appName: String,
    val title: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val messageCount: Int,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false
)

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(notification: NotificationEntity)

    @Query("""
        SELECT n.packageName AS packageName, 
               n.appName AS appName, 
               n.title AS title, 
               n.text AS lastMessage, 
               MAX(n.timestamp) AS lastTimestamp, 
               COUNT(*) AS messageCount,
               SUM(CASE WHEN n.isRead = 0 THEN 1 ELSE 0 END) AS unreadCount,
               COALESCE(m.isPinned, 0) AS isPinned
        FROM notifications n
        LEFT JOIN conversation_meta m ON m.id = (n.packageName || '_' || n.title)
        WHERE (:query = '' OR n.title LIKE '%' || :query || '%' OR n.text LIKE '%' || :query || '%' OR n.appName LIKE '%' || :query || '%')
        GROUP BY n.packageName, n.title
        ORDER BY isPinned DESC, lastTimestamp DESC
    """)
    fun getConversations(query: String): Flow<List<ConversationSummary>>

    @Query("""
        SELECT n.packageName AS packageName, 
               n.appName AS appName, 
               n.title AS title, 
               n.text AS lastMessage, 
               MAX(n.timestamp) AS lastTimestamp, 
               COUNT(*) AS messageCount,
               SUM(CASE WHEN n.isRead = 0 THEN 1 ELSE 0 END) AS unreadCount,
               COALESCE(m.isPinned, 0) AS isPinned
        FROM notifications n
        LEFT JOIN conversation_meta m ON m.id = (n.packageName || '_' || n.title)
        GROUP BY n.packageName, n.title
        ORDER BY isPinned DESC, lastTimestamp DESC
    """)
    fun getConversationThreads(): Flow<List<ConversationSummary>>

    @Query("""
        SELECT n.packageName AS packageName, 
               n.appName AS appName, 
               n.title AS title, 
               n.text AS lastMessage, 
               MAX(n.timestamp) AS lastTimestamp, 
               COUNT(*) AS messageCount,
               SUM(CASE WHEN n.isRead = 0 THEN 1 ELSE 0 END) AS unreadCount,
               COALESCE(m.isPinned, 0) AS isPinned
        FROM notifications n
        LEFT JOIN conversation_meta m ON m.id = (n.packageName || '_' || n.title)
        WHERE n.title LIKE '%' || :query || '%' 
           OR n.text LIKE '%' || :query || '%' 
           OR n.appName LIKE '%' || :query || '%'
        GROUP BY n.packageName, n.title
        ORDER BY isPinned DESC, lastTimestamp DESC
    """)
    fun searchConversationThreads(query: String): Flow<List<ConversationSummary>>

    @Query("""
        SELECT * FROM notifications 
        WHERE title LIKE '%' || :query || '%' 
           OR text LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchAllNotifications(query: String): Flow<List<NotificationEntity>>

    @Query("""
        SELECT * FROM notifications 
        WHERE packageName = :packageName AND title = :title 
        ORDER BY timestamp ASC
    """)
    fun getMessagesForConversation(packageName: String, title: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setConversationMeta(meta: ConversationMetaEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0")
    fun markAllAsRead(): Int

    @Query("UPDATE notifications SET isRead = 1 WHERE id IN (:ids)")
    fun markBatchAsRead(ids: List<Long>): Int

    @Query("DELETE FROM notifications WHERE id IN (:ids)")
    fun deleteBatch(ids: List<Long>): Int

    @Query("DELETE FROM notifications WHERE id = :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM notifications WHERE packageName = :packageName AND title = :title")
    fun deleteConversation(packageName: String, title: String)

    @Query("DELETE FROM notifications WHERE timestamp < :cutoffTimestamp")
    fun deleteOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM notifications")
    fun clearAll()

    @Query("SELECT COUNT(*) FROM notifications")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE timestamp >= :sinceTimestamp")
    fun getCountSince(sinceTimestamp: Long): Flow<Int>

    @Query("""
        SELECT packageName, appName, COUNT(*) AS alertCount 
        FROM notifications 
        GROUP BY packageName 
        ORDER BY alertCount DESC 
        LIMIT 5
    """)
    fun getTopApps(): Flow<List<AppStat>>
}

@Dao
interface MonitoredAppDao {
    @Query("SELECT * FROM monitored_apps")
    fun getAllMonitoredApps(): Flow<List<MonitoredAppEntity>>

    @Query("SELECT packageName FROM monitored_apps WHERE isMonitored = 1")
    fun getMonitoredPackageNames(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM monitored_apps WHERE packageName = :packageName AND isMonitored = 1)")
    fun isAppMonitored(packageName: String): Boolean

    @Query("SELECT COUNT(*) FROM monitored_apps WHERE isMonitored = 1")
    fun getMonitoredAppsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setAppMonitoring(app: MonitoredAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setAppMonitoringBatch(apps: List<MonitoredAppEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAppsIfAbsent(apps: List<MonitoredAppEntity>)
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: ChatMessageEntity)

    @Update
    fun updateMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    fun clearChat()
}

@Database(
    entities = [NotificationEntity::class, MonitoredAppEntity::class, ChatMessageEntity::class, ConversationMetaEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun monitoredAppDao(): MonitoredAppDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "summarylog_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
