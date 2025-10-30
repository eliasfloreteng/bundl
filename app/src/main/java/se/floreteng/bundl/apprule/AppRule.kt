package se.floreteng.bundl.apprule

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppRule(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val packageName: String,
    val mode: AppRuleMode,
    val filterString: String?
)