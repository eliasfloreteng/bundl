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

//    override fun onBind(intent: Intent): IBinder {
//        TODO("Return the communication channel to the service.")
//    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

//        // Check if bundling is enabled
//        if (!preferenceManager.isBundlingEnabled()) {
//            Log.d("BundlNotificationListener", "Bundling is disabled, ignoring notification")
//            return
//        }

        val packageName = sbn.packageName
        Log.d("BundlNotificationListener", "Notification posted from: $packageName")

        val notification = sbn.notification
        val extras = notification.extras

        // Extract notification details
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val category = notification.category

        Log.d(
            "BundlNotificationListener",
            "Notification details: title=$title, text=$text, subText=$subText, category=$category"
        )

//        TODO("Change to 'sent you a Snap' and variable condition")
//        if (text != null && text.contains("sent a Chat")) {
//            cancelNotification(sbn.key)
//        }

//        cancelNotification(sbn.key)
    }

//    override fun onNotificationRemoved(sbn: StatusBarNotification) {
//        super.onNotificationRemoved(sbn)
//        val packageName = sbn.packageName
//        Log.d(TAG, "Notification removed: $packageName")
//
//        // When the source app clears its notifications, we should update our bundled notification
//        scope.launch {
//            try {
//                val appConfig = database.appConfigDao().getAppConfig(packageName)
//                if (appConfig != null && appConfig.isEnabled) {
//                    // This is a managed app, check if we should update the bundled notification
//                    // We'll assume notifications were handled/cleared and mark old pending ones as delivered
//                    val bundleManager =
//                        se.floreteng.bundl.util.BundleNotificationManager(applicationContext)
//                    bundleManager.updateOrCancelBundledNotification(packageName)
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error updating bundled notification", e)
//            }
//        }
//    }

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