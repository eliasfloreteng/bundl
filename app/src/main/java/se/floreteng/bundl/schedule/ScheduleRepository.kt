package se.floreteng.bundl.schedule

import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val dao: ScheduleDao) {

    fun getSchedules(): Flow<List<Schedule>> {
        return dao.getSchedules()
    }

    suspend fun upsertSchedule(schedule: Schedule) {
        dao.upsertSchedule(schedule)
    }

    suspend fun deleteSchedule(schedule: Schedule) {
        dao.deleteSchedule(schedule)
    }

    suspend fun getScheduleById(id: Long): Schedule? {
        return dao.getScheduleById(id)
    }

    suspend fun updateScheduleEnabled(id: Long, enabled: Boolean) {
        dao.updateScheduleEnabled(id, enabled)
    }
}
