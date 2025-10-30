package se.floreteng.bundl.notifications

data class NotificationState(
    val notifications: List<Notification> = emptyList(),
    val selectedNotification: Notification? = null,
    val isDetailDialogVisible: Boolean = false
)
