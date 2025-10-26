package se.floreteng.bundl.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import se.floreteng.bundl.data.model.NotificationSchedule

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: NotificationSchedule): Long

    @Update
    suspend fun update(schedule: NotificationSchedule)

    @Delete
    suspend fun delete(schedule: NotificationSchedule)

    @Query("SELECT * FROM schedules ORDER BY hour, minute")
    fun getAllSchedules(): Flow<List<NotificationSchedule>>

    @Query("SELECT * FROM schedules WHERE isEnabled = 1 ORDER BY hour, minute")
    fun getEnabledSchedules(): Flow<List<NotificationSchedule>>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): NotificationSchedule?

    @Query("DELETE FROM schedules")
    suspend fun deleteAll()
}
