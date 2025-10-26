package se.floreteng.bundl.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import se.floreteng.bundl.data.model.ExemptionRule

@Dao
interface ExemptionRuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: ExemptionRule): Long

    @Update
    suspend fun update(rule: ExemptionRule)

    @Delete
    suspend fun delete(rule: ExemptionRule)

    @Query("SELECT * FROM exemption_rules WHERE appPackage = :appPackage AND isEnabled = 1")
    suspend fun getEnabledRulesForApp(appPackage: String): List<ExemptionRule>

    @Query("SELECT * FROM exemption_rules WHERE appPackage = :appPackage")
    fun getRulesForApp(appPackage: String): Flow<List<ExemptionRule>>

    @Query("SELECT * FROM exemption_rules ORDER BY appPackage")
    fun getAllRules(): Flow<List<ExemptionRule>>

    @Query("SELECT * FROM exemption_rules WHERE id = :id")
    suspend fun getRuleById(id: Long): ExemptionRule?
}
