package se.floreteng.bundl.schedule

data class ScheduleState(
    val schedules: List<Schedule> = emptyList(),
    val isAddingSchedule: Boolean = false,
    val selectedHour: Int = 12,
    val selectedMinute: Int = 0
)
