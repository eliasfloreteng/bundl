package se.floreteng.bundl.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class BundledNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appPackage: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val subText: String?,
    val timestamp: Long,
    val isDelivered: Boolean = false,
    val deliveredAt: Long? = null,
    val bundleId: String? = null,
    val category: String? = null,
    val extras: String? = null // JSON string for additional data
)
