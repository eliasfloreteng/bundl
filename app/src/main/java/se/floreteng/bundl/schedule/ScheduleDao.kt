package se.floreteng.bundl.schedule

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Upsert
    suspend fun upsertSchedule(schedule: Schedule)

    @Delete
    suspend fun deleteSchedule(schedule: Schedule)

    @Query("SELECT * FROM Schedule ORDER BY hour ASC, minute ASC")
    fun getSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM Schedule WHERE id = :id")
    suspend fun getScheduleById(id: Long): Schedule?

    @Query("UPDATE Schedule SET enabled = :enabled WHERE id = :id")
    suspend fun updateScheduleEnabled(id: Long, enabled: Boolean)
}
