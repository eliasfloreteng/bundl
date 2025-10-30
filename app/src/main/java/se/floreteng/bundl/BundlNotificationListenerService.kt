package se.floreteng.bundl

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BundlNotificationListenerService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: BundlDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            BundlDatabase::class.java,
            "bundl_database"
        ).build()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        // TODO("Check if bundling is enabled on the main screen before proceeding")

        val notification = sbn.notification
        val extras = notification.extras

        val key = sbn.key
        val tag = sbn.tag
        val postTime = sbn.postTime
        val packageName = sbn.packageName
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val category = notification.category ?: "unknown"
        val notificationTime = notification.`when`

        Log.d(
            "BundlNotificationListener",
            "Notification details:" +
                    "key=$key," +
                    "tag=$tag," +
                    "postTime=$postTime," +
                    "packageName=$packageName," +
                    "title=$title," +
                    "text=$text," +
                    "subText=$subText," +
                    "category=$category," +
                    "notificationTime=$notificationTime"
        )

        // Save notification to database
        val notificationEntity = se.floreteng.bundl.notifications.Notification(
            key = key,
            tag = tag,
            postTime = postTime,
            packageName = packageName,
            title = title,
            text = text,
            subText = subText,
            category = category,
            notificationTime = notificationTime
        )

        serviceScope.launch {
            try {
                database.notificationDao.insertNotification(notificationEntity)
                Log.d("BundlNotificationListener", "Notification saved to database")
            } catch (e: Exception) {
                Log.e("BundlNotificationListener", "Error saving notification to database", e)
            }
        }

        // TODO("Implement actual check against app rules from database")
        // if (...) {
        //     cancelNotification(sbn.key)
        //     Log.d("BundlNotificationListener", "Notification canceled based on app rules")
        // }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("BundlNotificationListener", "Listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("BundlNotificationListener", "Listener disconnected")

        // Request rebind
        requestRebind(
            Intent(
                this,
                se.floreteng.bundl.BundlNotificationListenerService::class.java
            ).component
        )
    }
}