package se.floreteng.bundl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.floreteng.bundl.notifications.NotificationRepository
import se.floreteng.bundl.utils.NotificationAccessUtil
import se.floreteng.bundl.utils.NotificationDeliveryUtil

class ScheduledDeliveryReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        private const val TAG = "ScheduledDeliveryReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
        Log.d(TAG, "Received scheduled delivery trigger for schedule ID: $scheduleId")

        // Check if we have permission to post notifications
        if (!NotificationAccessUtil.hasNotificationPermission(context)) {
            Log.w(TAG, "No notification permission, skipping scheduled delivery")
            return
        }

        // Use goAsync to handle coroutine work
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                deliverScheduledNotifications(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error delivering scheduled notifications", e)
            } finally {
                pendingResult.finish()
                scope.cancel()
            }
        }
    }

    private suspend fun deliverScheduledNotifications(context: Context) {
        val database = Room.databaseBuilder(
            context,
            BundlDatabase::class.java,
            "bundl_database"
        ).build()

        val notificationRepository = NotificationRepository(database.notificationDao)

        // Get all notifications from database
        val allNotifications = notificationRepository.getAllNotifications().first()

        if (allNotifications.isEmpty()) {
            Log.d(TAG, "No notifications to deliver")
            return
        }

        Log.d(TAG, "Delivering ${allNotifications.size} notifications")

        // Group notifications by package name
        val notificationsByApp = allNotifications.groupBy { it.packageName }

        // Deliver bundled notifications
        NotificationDeliveryUtil.deliverBundledNotifications(context, notificationsByApp)

        // Clear delivered notifications from database
        notificationRepository.deleteAllNotifications()

        Log.d(TAG, "Scheduled delivery completed")
    }
}
