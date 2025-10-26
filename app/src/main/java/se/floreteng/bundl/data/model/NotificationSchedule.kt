package se.floreteng.bundl.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class NotificationSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,           // 0-23
    val minute: Int,         // 0-59
    val daysOfWeek: String,  // JSON array: [0-6] where 0 = Sunday
    val isEnabled: Boolean = true
)
