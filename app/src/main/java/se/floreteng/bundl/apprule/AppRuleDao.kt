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

    @Query("SELECT * FROM apprule ORDER BY packageName ASC")
    fun getAppRules(): Flow<List<AppRule>>
}