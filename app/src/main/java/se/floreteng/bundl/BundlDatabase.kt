package se.floreteng.bundl

import androidx.room.Database
import androidx.room.RoomDatabase
import se.floreteng.bundl.apprule.AppRule
import se.floreteng.bundl.apprule.AppRuleDao

@Database(
    entities = [AppRule::class],
    version = 1,
    exportSchema = false
)
abstract class BundlDatabase : RoomDatabase() {
    abstract val appRuleDao: AppRuleDao
}
