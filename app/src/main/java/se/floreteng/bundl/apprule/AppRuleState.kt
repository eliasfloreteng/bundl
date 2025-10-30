package se.floreteng.bundl.apprule

data class AppRuleState(
    val appRules: List<AppRule> = emptyList(),
    val packageName: String = "",
    val mode: AppRuleMode = AppRuleMode.BLACKLIST,
    val filterString: String = "",
    val isDialogVisible: Boolean = false,
    val editingAppRule: AppRule? = null
)
