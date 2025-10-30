package se.floreteng.bundl.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import se.floreteng.bundl.ScheduledDeliveryReceiver
import se.floreteng.bundl.schedule.Schedule
import java.util.Calendar

object ScheduleAlarmUtil {

    private const val TAG = "ScheduleAlarmUtil"

    /**
     * Schedule an alarm for a specific delivery time
     */
    fun scheduleAlarm(context: Context, schedule: Schedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ScheduledDeliveryReceiver::class.java).apply {
            putExtra(ScheduledDeliveryReceiver.EXTRA_SCHEDULE_ID, schedule.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate next occurrence of the scheduled time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Schedule repeating alarm
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Log.d(TAG, "Scheduled alarm for ${schedule.hour}:${schedule.minute} at ${calendar.time}")
    }

    /**
     * Cancel an alarm for a specific schedule
     */
    fun cancelAlarm(context: Context, schedule: Schedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ScheduledDeliveryReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        Log.d(TAG, "Cancelled alarm for ${schedule.hour}:${schedule.minute}")
    }

    /**
     * Reschedule all enabled schedules
     */
    fun rescheduleAllAlarms(context: Context, schedules: List<Schedule>) {
        schedules.forEach { schedule ->
            if (schedule.enabled) {
                scheduleAlarm(context, schedule)
            } else {
                cancelAlarm(context, schedule)
            }
        }
    }
}
