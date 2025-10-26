package se.floreteng.bundl.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exemption_rules")
data class ExemptionRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appPackage: String,
    val ruleType: String,    // e.g., "MESSAGE", "CALL", "MENTION"
    val keywords: String?,   // JSON array of keywords to match
    val categoryFilter: String?,  // Notification category filter
    val isEnabled: Boolean = true
)
