package se.floreteng.bundl.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.floreteng.bundl.utils.ScheduleAlarmUtil

class ScheduleViewModel(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _schedules = repository.getSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _state = MutableStateFlow(ScheduleState())

    val state = combine(_state, _schedules) { state, schedules ->
        state.copy(schedules = schedules)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScheduleState())

    fun onEvent(event: ScheduleEvent) {
        when (event) {
            is ScheduleEvent.ShowDialog -> {
                _state.update { it.copy(
                    isAddingSchedule = true,
                    selectedHour = 12,
                    selectedMinute = 0
                ) }
            }
            is ScheduleEvent.HideDialog -> {
                _state.update { it.copy(isAddingSchedule = false) }
            }
            is ScheduleEvent.SetHour -> {
                _state.update { it.copy(selectedHour = event.hour) }
            }
            is ScheduleEvent.SetMinute -> {
                _state.update { it.copy(selectedMinute = event.minute) }
            }
            is ScheduleEvent.SaveSchedule -> {
                viewModelScope.launch {
                    val schedule = Schedule(
                        hour = _state.value.selectedHour,
                        minute = _state.value.selectedMinute,
                        enabled = true
                    )
                    repository.upsertSchedule(schedule)
                    _state.update { it.copy(isAddingSchedule = false) }
                }
            }
            is ScheduleEvent.DeleteSchedule -> {
                viewModelScope.launch {
                    repository.deleteSchedule(event.schedule)
                }
            }
            is ScheduleEvent.ToggleSchedule -> {
                viewModelScope.launch {
                    repository.updateScheduleEnabled(event.schedule.id, event.enabled)
                }
            }
        }
    }

    fun rescheduleAllAlarms(context: Context) {
        viewModelScope.launch {
            val schedules = _schedules.value
            schedules.forEach { schedule ->
                if (schedule.enabled) {
                    ScheduleAlarmUtil.scheduleAlarm(context, schedule)
                } else {
                    ScheduleAlarmUtil.cancelAlarm(context, schedule)
                }
            }
        }
    }
}
