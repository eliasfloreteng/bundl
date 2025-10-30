package se.floreteng.bundl

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_AS_READ = "se.floreteng.bundl.MARK_AS_READ"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_MARK_AS_READ) {
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

            if (notificationId != -1) {
                // Dismiss the notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)

                Log.d("NotificationActionReceiver", "Marked notification $notificationId as read")
            }
        }
    }
}
