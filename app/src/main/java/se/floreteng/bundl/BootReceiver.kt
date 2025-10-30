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
import se.floreteng.bundl.schedule.ScheduleRepository
import se.floreteng.bundl.utils.ScheduleAlarmUtil

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted, rescheduling alarms")

            // Use goAsync to handle coroutine work
            val pendingResult = goAsync()
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

            scope.launch {
                try {
                    rescheduleAllAlarms(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling alarms after boot", e)
                } finally {
                    pendingResult.finish()
                    scope.cancel()
                }
            }
        }
    }

    private suspend fun rescheduleAllAlarms(context: Context) {
        val database = Room.databaseBuilder(
            context,
            BundlDatabase::class.java,
            "bundl_database"
        ).build()

        val scheduleRepository = ScheduleRepository(database.scheduleDao)

        // Get all schedules from database
        val schedules = scheduleRepository.getSchedules().first()

        Log.d(TAG, "Found ${schedules.size} schedules to reschedule")

        // Reschedule all enabled alarms
        ScheduleAlarmUtil.rescheduleAllAlarms(context, schedules)

        Log.d(TAG, "Alarms rescheduled successfully")
    }
}
