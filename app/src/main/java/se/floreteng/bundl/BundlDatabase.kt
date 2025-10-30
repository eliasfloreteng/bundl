package se.floreteng.bundl

import androidx.room.Database
import androidx.room.RoomDatabase
import se.floreteng.bundl.apprule.AppRule
import se.floreteng.bundl.apprule.AppRuleDao
import se.floreteng.bundl.notifications.Notification
import se.floreteng.bundl.notifications.NotificationDao

@Database(
    entities = [AppRule::class, Notification::class],
    version = 2,
    exportSchema = false
)
abstract class BundlDatabase : RoomDatabase() {
    abstract val appRuleDao: AppRuleDao
    abstract val notificationDao: NotificationDao
}
