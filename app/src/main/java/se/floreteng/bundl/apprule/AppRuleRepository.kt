package se.floreteng.bundl.apprule

import kotlinx.coroutines.flow.Flow

class AppRuleRepository(
    private val dao: AppRuleDao
) {
    fun getAppRules(): Flow<List<AppRule>> {
        return dao.getAppRules()
    }

    suspend fun getAppRuleById(id: Int): AppRule? {
        return dao.getAppRuleById(id)
    }

    suspend fun upsertAppRule(appRule: AppRule) {
        dao.upsertAppRule(appRule)
    }

    suspend fun deleteAppRule(appRule: AppRule) {
        dao.deleteAppRule(appRule)
    }
}
