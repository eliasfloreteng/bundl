package se.floreteng.bundl.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import se.floreteng.bundl.data.model.BundledNotification

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: BundledNotification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<BundledNotification>)

    @Update
    suspend fun update(notification: BundledNotification)

    @Delete
    suspend fun delete(notification: BundledNotification)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<BundledNotification>>

    @Query("SELECT * FROM notifications WHERE isDelivered = 0 ORDER BY timestamp DESC")
    fun getPendingNotifications(): Flow<List<BundledNotification>>

    @Query("SELECT * FROM notifications WHERE isDelivered = 0 AND appPackage = :appPackage ORDER BY timestamp DESC")
    suspend fun getPendingNotificationsByApp(appPackage: String): List<BundledNotification>

    @Query("SELECT * FROM notifications WHERE appPackage = :appPackage ORDER BY timestamp DESC")
    fun getNotificationsByApp(appPackage: String): Flow<List<BundledNotification>>

    @Query("SELECT DISTINCT appPackage FROM notifications WHERE isDelivered = 0")
    suspend fun getAppsWithPendingNotifications(): List<String>

    @Query("SELECT COUNT(*) FROM notifications WHERE isDelivered = 0 AND appPackage = :appPackage")
    fun getPendingCountByApp(appPackage: String): Flow<Int>

    @Query("UPDATE notifications SET isDelivered = 1, deliveredAt = :deliveredAt WHERE id IN (:notificationIds)")
    suspend fun markAsDelivered(notificationIds: List<Long>, deliveredAt: Long)

    @Query("DELETE FROM notifications WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getNotificationById(id: Long): BundledNotification?
}
