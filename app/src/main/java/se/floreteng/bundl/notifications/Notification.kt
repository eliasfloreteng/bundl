package se.floreteng.bundl.notifications

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Notification(
    @PrimaryKey
    val key: String,
    val tag: String?,
    val postTime: Long,
    val packageName: String,
    val title: String?,
    val text: String?,
    val subText: String?,
    val category: String,
    val notificationTime: Long,
)
