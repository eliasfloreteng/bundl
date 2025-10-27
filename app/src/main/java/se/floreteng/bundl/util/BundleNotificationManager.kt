package se.floreteng.bundl.util

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.app.NotificationCompat
import se.floreteng.bundl.R
import se.floreteng.bundl.data.BundlDatabase

class BundleNotificationManager(private val context: Context) {

    private val database = BundlDatabase.getDatabase(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "bundl_delivery_channel"
        private const val BASE_NOTIFICATION_ID = 10000

        // Map to track which app packages are associated with which notification IDs
        private val packageToNotificationId = mutableMapOf<String, Int>()
        private var nextNotificationId = BASE_NOTIFICATION_ID
    }

    suspend fun updateOrCancelBundledNotification(appPackage: String) {
        // Get pending notifications for this app
        val pendingNotifications = database.notificationDao()
            .getPendingNotificationsByApp(appPackage)

        val notificationId = packageToNotificationId[appPackage] ?: return

        when {
            pendingNotifications.isEmpty() -> {
                // No more pending notifications, cancel the bundled notification
                notificationManager.cancel(notificationId)
                packageToNotificationId.remove(appPackage)
            }
            else -> {
                // Update the notification with new count
                showOrUpdateBundleNotification(
                    appPackage = appPackage,
                    appName = pendingNotifications.first().appName,
                    count = pendingNotifications.size,
                    notificationId = notificationId
                )
            }
        }
    }

    fun showOrUpdateBundleNotification(
        appPackage: String,
        appName: String,
        count: Int,
        notificationId: Int? = null
    ) {
        val id = notificationId ?: packageToNotificationId.getOrPut(appPackage) {
            nextNotificationId++
        }

        packageToNotificationId[appPackage] = id

        // Create intent to launch the source app
        val launchIntent = context.packageManager.getLaunchIntentForPackage(appPackage)

        val pendingIntent = if (launchIntent != null) {
            launchIntent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            android.app.PendingIntent.getActivity(
                context,
                id,
                launchIntent,
                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            null
        }

        // Create intent for "Mark as Read" action
        val markReadIntent = android.content.Intent(context, se.floreteng.bundl.receiver.NotificationActionReceiver::class.java).apply {
            action = se.floreteng.bundl.receiver.NotificationActionReceiver.ACTION_MARK_READ
            putExtra(se.floreteng.bundl.receiver.NotificationActionReceiver.EXTRA_APP_PACKAGE, appPackage)
            putExtra(se.floreteng.bundl.receiver.NotificationActionReceiver.EXTRA_NOTIFICATION_ID, id)
        }

        val markReadPendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            id + 50000, // Offset to avoid collision
            markReadIntent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = if (count == 1) {
            "You have 1 notification from $appName"
        } else {
            "You have $count notifications from $appName"
        }

        val appIcon = getAppIcon(appPackage)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(appName)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(false)  // Don't auto-cancel, let user explicitly mark as read
            .setNumber(0)
            .setOnlyAlertOnce(true)
            .addAction(0, "Mark as Read", markReadPendingIntent)

        if (appIcon != null) {
            notificationBuilder.setLargeIcon(appIcon)
        }

        if (pendingIntent != null) {
            notificationBuilder.setContentIntent(pendingIntent)
        }

        notificationManager.notify(id, notificationBuilder.build())
    }

    private fun getAppIcon(packageName: String): Bitmap? {
        return try {
            val packageManager = context.packageManager
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
}
