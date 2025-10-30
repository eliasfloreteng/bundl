package se.floreteng.bundl.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import se.floreteng.bundl.NotificationActionReceiver
import se.floreteng.bundl.R
import se.floreteng.bundl.notifications.Notification

object NotificationDeliveryUtil {

    private const val CHANNEL_ID = "bundl_delivery_channel"
    private const val CHANNEL_NAME = "Bundled Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications delivered by Bundl"

    /**
     * Create notification channel for delivered notifications
     */
    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Deliver bundled notifications grouped by app
     */
    fun deliverBundledNotifications(
        context: Context,
        notificationsByApp: Map<String, List<Notification>>
    ) {
        createNotificationChannel(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationsByApp.forEach { (packageName, notifications) ->
            val appName = getAppName(context, packageName)
            val notificationId = packageName.hashCode()

            // Create intent to open the app
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            val contentPendingIntent = if (launchIntent != null) {
                PendingIntent.getActivity(
                    context,
                    notificationId,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                null
            }

            // Create intent for "Mark as Read" action
            val markAsReadIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_AS_READ
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val markAsReadPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                markAsReadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Build summary notification
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("$appName (${notifications.size})")
                .setContentText(buildSummaryText(notifications))
                .setStyle(buildInboxStyle(notifications, appName))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false) // Don't dismiss on tap
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Mark as Read",
                    markAsReadPendingIntent
                )
                .apply {
                    if (contentPendingIntent != null) {
                        setContentIntent(contentPendingIntent)
                    }
                }
                .build()

            notificationManager.notify(notificationId, notification)
        }
    }

    /**
     * Build a short summary text for the notification
     */
    private fun buildSummaryText(notifications: List<Notification>): String {
        val first = notifications.firstOrNull() ?: return "No notifications"

        return when {
            notifications.size == 1 -> {
                first.title ?: first.text ?: "New notification"
            }
            notifications.size == 2 -> {
                val second = notifications[1]
                "${first.title ?: "Notification"}, ${second.title ?: "Notification"}"
            }
            else -> {
                "${first.title ?: "Notification"} and ${notifications.size - 1} more"
            }
        }
    }

    /**
     * Build inbox style for detailed view
     */
    private fun buildInboxStyle(
        notifications: List<Notification>,
        appName: String
    ): NotificationCompat.InboxStyle {
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("$appName (${notifications.size} notifications)")

        // Add up to 5 most recent notifications
        notifications.take(5).forEach { notification ->
            val title = notification.title ?: "Notification"
            val text = notification.text?.take(50) ?: ""
            val line = if (text.isNotEmpty()) {
                "$title: $text"
            } else {
                title
            }
            inboxStyle.addLine(line)
        }

        if (notifications.size > 5) {
            inboxStyle.setSummaryText("+ ${notifications.size - 5} more")
        }

        return inboxStyle
    }

    /**
     * Get app name from package name
     */
    private fun getAppName(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}
