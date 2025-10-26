package se.floreteng.bundl.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import se.floreteng.bundl.MainActivity
import se.floreteng.bundl.R
import se.floreteng.bundl.data.BundlDatabase

class BundleDeliveryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = BundlDatabase.getDatabase(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "bundl_delivery_channel"
        const val CHANNEL_NAME = "Bundled Notifications"
        private const val BASE_NOTIFICATION_ID = 10000
    }

    override suspend fun doWork(): Result {
        return try {
            createNotificationChannel()
            deliverBundles()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun deliverBundles() {
        // Get all apps with pending notifications
        val appsWithNotifications = database.notificationDao().getAppsWithPendingNotifications()

        for ((index, appPackage) in appsWithNotifications.withIndex()) {
            val pendingNotifications = database.notificationDao()
                .getPendingNotificationsByApp(appPackage)

            if (pendingNotifications.isNotEmpty()) {
                // Create and show bundle notification for this app
                showBundleNotification(
                    appPackage = appPackage,
                    appName = pendingNotifications.first().appName,
                    count = pendingNotifications.size,
                    notificationId = BASE_NOTIFICATION_ID + index
                )

                // Mark notifications as delivered
                val notificationIds = pendingNotifications.map { it.id }
                database.notificationDao().markAsDelivered(
                    notificationIds = notificationIds,
                    deliveredAt = System.currentTimeMillis()
                )
            }
        }
    }

    private fun showBundleNotification(
        appPackage: String,
        appName: String,
        count: Int,
        notificationId: Int
    ) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("app_package", appPackage)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = if (count == 1) {
            "You have 1 notification from $appName"
        } else {
            "You have $count notifications from $appName"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Bundl")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Delivers bundled notifications from social media apps"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
