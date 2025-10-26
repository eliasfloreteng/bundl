package se.floreteng.bundl.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import se.floreteng.bundl.util.ScheduleManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all delivery times after device reboot
            val scheduleManager = ScheduleManager(context)

            CoroutineScope(Dispatchers.IO).launch {
                scheduleManager.scheduleAll()
            }
        }
    }
}
