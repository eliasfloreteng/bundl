package se.floreteng.bundl.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import se.floreteng.bundl.data.BundlDatabase

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_READ = "se.floreteng.bundl.ACTION_MARK_READ"
        const val EXTRA_APP_PACKAGE = "app_package"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_MARK_READ) {
            val appPackage = intent.getStringExtra(EXTRA_APP_PACKAGE) ?: return
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

            CoroutineScope(Dispatchers.IO).launch {
                val database = BundlDatabase.getDatabase(context)

                // Mark all pending notifications for this app as delivered
                val pendingNotifications = database.notificationDao()
                    .getPendingNotificationsByApp(appPackage)

                if (pendingNotifications.isNotEmpty()) {
                    val notificationIds = pendingNotifications.map { it.id }
                    database.notificationDao().markAsDelivered(
                        notificationIds = notificationIds,
                        deliveredAt = System.currentTimeMillis()
                    )
                }

                // Cancel the notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)
            }
        }
    }
}
