package se.floreteng.bundl.util

import android.content.Context
import androidx.work.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import se.floreteng.bundl.data.BundlDatabase
import se.floreteng.bundl.data.model.NotificationSchedule
import se.floreteng.bundl.worker.BundleDeliveryWorker
import java.util.*
import java.util.concurrent.TimeUnit

class ScheduleManager(private val context: Context) {

    private val database = BundlDatabase.getDatabase(context)
    private val workManager = WorkManager.getInstance(context)
    private val gson = Gson()

    companion object {
        private const val WORK_TAG_PREFIX = "bundle_delivery_"
    }

    suspend fun scheduleAll() {
        // Cancel all existing work
        workManager.cancelAllWorkByTag("bundle_delivery").await()

        // Get all enabled schedules
        val schedules = database.scheduleDao().getEnabledSchedules().first()

        // Schedule each one
        for (schedule in schedules) {
            scheduleDelivery(schedule)
        }
    }

    private fun scheduleDelivery(schedule: NotificationSchedule) {
        val daysOfWeek: List<Int> = try {
            gson.fromJson(schedule.daysOfWeek, object : TypeToken<List<Int>>() {}.type)
        } catch (e: Exception) {
            listOf(1, 2, 3, 4, 5, 6, 7) // Default to all days
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the time has already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Calculate initial delay
        val currentTime = System.currentTimeMillis()
        val initialDelay = calendar.timeInMillis - currentTime

        val workRequest = PeriodicWorkRequestBuilder<BundleDeliveryWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("bundle_delivery")
            .addTag("${WORK_TAG_PREFIX}${schedule.id}")
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "${WORK_TAG_PREFIX}${schedule.id}",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    suspend fun cancelSchedule(scheduleId: Long) {
        workManager.cancelAllWorkByTag("${WORK_TAG_PREFIX}$scheduleId").await()
    }

    suspend fun cancelAll() {
        workManager.cancelAllWorkByTag("bundle_delivery").await()
    }

    suspend fun triggerImmediateDelivery() {
        val workRequest = OneTimeWorkRequestBuilder<BundleDeliveryWorker>()
            .build()

        workManager.enqueue(workRequest).await()
    }
}
