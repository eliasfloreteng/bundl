package se.floreteng.bundl.notifications

import kotlinx.coroutines.flow.Flow

class NotificationRepository(
    private val dao: NotificationDao
) {
    fun getAllNotifications(): Flow<List<Notification>> {
        return dao.getAllNotifications()
    }

    suspend fun getNotificationByKey(key: String): Notification? {
        return dao.getNotificationByKey(key)
    }

    suspend fun insertNotification(notification: Notification) {
        dao.insertNotification(notification)
    }

    suspend fun deleteNotification(notification: Notification) {
        dao.deleteNotification(notification)
    }

    suspend fun deleteAllNotifications() {
        dao.deleteAllNotifications()
    }

    fun getNotificationsByPackage(packageName: String): Flow<List<Notification>> {
        return dao.getNotificationsByPackage(packageName)
    }
}
