package se.floreteng.bundl.service

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import se.floreteng.bundl.data.BundlDatabase
import se.floreteng.bundl.data.model.BundledNotification
import se.floreteng.bundl.util.ExemptionChecker
import se.floreteng.bundl.util.PreferenceManager

class BundlNotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: BundlDatabase
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var exemptionChecker: ExemptionChecker

    companion object {
        private const val TAG = "BundlNotificationListener"
    }

    override fun onCreate() {
        super.onCreate()
        database = BundlDatabase.getDatabase(applicationContext)
        preferenceManager = PreferenceManager(applicationContext)
        exemptionChecker = ExemptionChecker(database)
        Log.d(TAG, "Service created")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        // Check if bundling is enabled
        if (!preferenceManager.isBundlingEnabled()) {
            Log.d(TAG, "Bundling is disabled, ignoring notification")
            return
        }

        val packageName = sbn.packageName
        Log.d(TAG, "Notification posted from: $packageName")

        scope.launch {
            try {
                // Check if this app is configured for bundling
                val appConfig = database.appConfigDao().getAppConfig(packageName)
                if (appConfig == null || !appConfig.isEnabled) {
                    Log.d(TAG, "App not configured for bundling: $packageName")
                    return@launch
                }

                val notification = sbn.notification
                val extras = notification.extras

                // Extract notification details
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
                val category = notification.category

                // Check if this notification is exempt from bundling
                val isExempt = exemptionChecker.isExempt(
                    packageName = packageName,
                    category = category,
                    title = title,
                    text = text
                )

                if (isExempt) {
                    Log.d(TAG, "Notification is exempt, allowing through: $packageName")
                    return@launch
                }

                // Create bundled notification record
                val bundledNotification = BundledNotification(
                    appPackage = packageName,
                    appName = appConfig.appName,
                    title = title,
                    text = text,
                    subText = subText,
                    timestamp = System.currentTimeMillis(),
                    category = category,
                    extras = extrasToJson(extras)
                )

                // Save to database
                database.notificationDao().insert(bundledNotification)
                Log.d(TAG, "Notification bundled: $packageName - $title")

                // Cancel the original notification
                cancelNotification(sbn.key)
                Log.d(TAG, "Original notification cancelled")

            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        Log.d(TAG, "Notification removed: ${sbn.packageName}")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Listener disconnected")

        // Request rebind
        requestRebind(Intent(this, BundlNotificationListenerService::class.java).component)
    }

    private fun extrasToJson(extras: Bundle): String {
        val map = mutableMapOf<String, Any?>()
        for (key in extras.keySet()) {
            try {
                val value = extras.get(key)
                if (value is String || value is Number || value is Boolean) {
                    map[key] = value
                }
            } catch (e: Exception) {
                // Ignore non-serializable values
            }
        }
        return com.google.gson.Gson().toJson(map)
    }
}
