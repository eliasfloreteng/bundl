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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.floreteng.bundl.apprule.AppRule
import se.floreteng.bundl.apprule.AppRuleMode

class BundlNotificationListenerService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: BundlDatabase
    private lateinit var preferencesManager: se.floreteng.bundl.preferences.PreferencesManager

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            BundlDatabase::class.java,
            "bundl_database"
        ).build()
        preferencesManager = se.floreteng.bundl.preferences.PreferencesManager(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        serviceScope.launch {
            // Check if bundling is enabled
            val isBundlingEnabled = preferencesManager.isBundlingEnabled.first()
            if (!isBundlingEnabled) {
                Log.d("BundlNotificationListener", "Bundling is disabled, skipping notification")
                return@launch
            }

            processNotification(sbn)
        }
    }

    private suspend fun processNotification(sbn: StatusBarNotification) {
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
            "Notification received: key=$key, package=$packageName, title=$title"
        )

        // Load all app rules from database
        val appRules = database.appRuleDao.getAppRules().first()

        // Check if notification should be cancelled based on rules
        val shouldCancel = shouldCancelNotification(packageName, title, text, subText, appRules)

        if (shouldCancel) {
            // Cancel the notification
            cancelNotification(key)
            Log.d("BundlNotificationListener", "Notification cancelled based on app rules")

            // Save cancelled notification to database
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

            try {
                database.notificationDao.insertNotification(notificationEntity)
                Log.d("BundlNotificationListener", "Cancelled notification saved to database")
            } catch (e: Exception) {
                Log.e("BundlNotificationListener", "Error saving notification to database", e)
            }
        } else {
            Log.d("BundlNotificationListener", "Notification allowed through (no matching rules)")
        }
    }

    /**
     * Determines if a notification should be cancelled based on app rules
     *
     * BLACKLIST mode: Cancel if package matches AND (no filter OR filter matches)
     * WHITELIST mode: Allow ONLY if package matches AND (no filter OR filter matches)
     */
    private fun shouldCancelNotification(
        packageName: String,
        title: String?,
        text: String?,
        subText: String?,
        appRules: List<AppRule>
    ): Boolean {
        // Find rules for this package
        val matchingRules = appRules.filter { it.packageName == packageName }

        if (matchingRules.isEmpty()) {
            // No rules for this package
            return false
        }

        for (rule in matchingRules) {
            val filterMatches = if (rule.filterString.isNullOrBlank()) {
                // No filter means rule applies to all notifications from this package
                true
            } else {
                // Check if filter string appears in title, text, or subtext
                val filter = rule.filterString.lowercase()
                val titleLower = title?.lowercase() ?: ""
                val textLower = text?.lowercase() ?: ""
                val subTextLower = subText?.lowercase() ?: ""

                titleLower.contains(filter) ||
                        textLower.contains(filter) ||
                        subTextLower.contains(filter)
            }

            when (rule.mode) {
                AppRuleMode.BLACKLIST -> {
                    // Cancel if package matches and filter matches (or no filter)
                    if (filterMatches) {
                        Log.d(
                            "BundlNotificationListener",
                            "BLACKLIST rule matched: package=$packageName, filter=${rule.filterString}"
                        )
                        return true
                    }
                }
                AppRuleMode.WHITELIST -> {
                    // Allow if package matches and filter matches (or no filter)
                    if (filterMatches) {
                        Log.d(
                            "BundlNotificationListener",
                            "WHITELIST rule matched: package=$packageName, filter=${rule.filterString}"
                        )
                        return false // Don't cancel - whitelist allows it through
                    }
                }
            }
        }

        // If we have WHITELIST rules but none matched, cancel the notification
        val hasWhitelistRules = matchingRules.any { it.mode == AppRuleMode.WHITELIST }
        if (hasWhitelistRules) {
            Log.d(
                "BundlNotificationListener",
                "WHITELIST rules exist but didn't match: package=$packageName"
            )
            return true // Cancel because whitelist didn't match
        }

        return false
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