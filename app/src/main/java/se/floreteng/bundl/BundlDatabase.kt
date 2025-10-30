package se.floreteng.bundl

import androidx.room.Database
import androidx.room.RoomDatabase
import se.floreteng.bundl.apprule.AppRule
import se.floreteng.bundl.apprule.AppRuleDao
import se.floreteng.bundl.notifications.Notification
import se.floreteng.bundl.notifications.NotificationDao
import se.floreteng.bundl.schedule.Schedule
import se.floreteng.bundl.schedule.ScheduleDao

@Database(
    entities = [AppRule::class, Notification::class, Schedule::class],
    version = 3,
    exportSchema = false
)
abstract class BundlDatabase : RoomDatabase() {
    abstract val appRuleDao: AppRuleDao
    abstract val notificationDao: NotificationDao
    abstract val scheduleDao: ScheduleDao
}
