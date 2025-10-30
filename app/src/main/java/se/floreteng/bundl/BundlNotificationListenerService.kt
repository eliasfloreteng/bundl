package se.floreteng.bundl

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.launch

class BundlNotificationListenerService : NotificationListenerService() {
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
        val category = notification.category
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

        // TODO("Implement actual check agains app rules from database")
        // if (...) {
        //     TODO("Add notification to database")
        //     cancelNotification(sbn.key)
        //     Log.d("BundlNotificationListener", "Notification canceled based on app rules")
        // }
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