package se.floreteng.bundl.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
        // Create intent to launch the source app
        val launchIntent = applicationContext.packageManager.getLaunchIntentForPackage(appPackage)

        val pendingIntent = if (launchIntent != null) {
            // Launch the source app directly
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            PendingIntent.getActivity(
                applicationContext,
                notificationId,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            // Fallback: open Bundl app if we can't launch the source app
            try {
                val mainActivityClass = applicationContext.javaClass.classLoader?.loadClass("se.floreteng.bundl.MainActivity")
                val fallbackIntent = Intent(applicationContext, mainActivityClass).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("app_package", appPackage)
                }
                PendingIntent.getActivity(
                    applicationContext,
                    notificationId,
                    fallbackIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } catch (e: Exception) {
                // If all else fails, create a null pending intent
                null
            }
        }

        val contentText = if (count == 1) {
            "You have 1 notification from $appName"
        } else {
            "You have $count notifications from $appName"
        }

        // Get the app's icon
        val appIcon = getAppIcon(appPackage)

        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(appName)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Set large icon if available
        if (appIcon != null) {
            notificationBuilder.setLargeIcon(appIcon)
        }

        // Set content intent if available
        if (pendingIntent != null) {
            notificationBuilder.setContentIntent(pendingIntent)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun getAppIcon(packageName: String): Bitmap? {
        return try {
            val packageManager = applicationContext.packageManager
            val appIcon: Drawable = packageManager.getApplicationIcon(packageName)
            drawableToBitmap(appIcon)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
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
