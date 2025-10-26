package se.floreteng.bundl.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey
    val appPackage: String,
    val appName: String,
    val isEnabled: Boolean = true
)
