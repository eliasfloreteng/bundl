package se.floreteng.bundl.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import se.floreteng.bundl.data.dao.*
import se.floreteng.bundl.data.model.*

@Database(
    entities = [
        BundledNotification::class,
        NotificationSchedule::class,
        AppConfig::class,
        ExemptionRule::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BundlDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun appConfigDao(): AppConfigDao
    abstract fun exemptionRuleDao(): ExemptionRuleDao

    companion object {
        @Volatile
        private var INSTANCE: BundlDatabase? = null

        fun getDatabase(context: Context): BundlDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BundlDatabase::class.java,
                    "bundl_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
