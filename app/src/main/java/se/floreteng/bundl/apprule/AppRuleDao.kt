package se.floreteng.bundl.apprule

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRuleDao {
    @Upsert
    suspend fun upsertAppRule(appRule: AppRule)

    @Delete
    suspend fun deleteAppRule(appRule: AppRule)

    @Query("SELECT * FROM AppRule ORDER BY packageName ASC")
    fun getAppRules(): Flow<List<AppRule>>

    @Query("SELECT * FROM AppRule WHERE id = :id")
    suspend fun getAppRuleById(id: Int): AppRule?
}
