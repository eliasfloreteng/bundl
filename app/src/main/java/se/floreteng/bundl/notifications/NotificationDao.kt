package se.floreteng.bundl.notifications

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Delete
    suspend fun deleteNotification(notification: Notification)

    @Query("SELECT * FROM Notification ORDER BY notificationTime DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Query("SELECT * FROM Notification WHERE key = :key")
    suspend fun getNotificationByKey(key: String): Notification?

    @Query("DELETE FROM Notification")
    suspend fun deleteAllNotifications()

    @Query("SELECT * FROM Notification WHERE packageName = :packageName ORDER BY notificationTime DESC")
    fun getNotificationsByPackage(packageName: String): Flow<List<Notification>>
}
