package se.floreteng.bundl.schedule

sealed interface ScheduleEvent {
    data object ShowDialog : ScheduleEvent
    data object HideDialog : ScheduleEvent
    data class SetHour(val hour: Int) : ScheduleEvent
    data class SetMinute(val minute: Int) : ScheduleEvent
    data object SaveSchedule : ScheduleEvent
    data class DeleteSchedule(val schedule: Schedule) : ScheduleEvent
    data class ToggleSchedule(val schedule: Schedule, val enabled: Boolean) : ScheduleEvent
}
