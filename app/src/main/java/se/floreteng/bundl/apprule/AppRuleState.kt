package se.floreteng.bundl.apprule

data class AppRuleState(
    val appRules: List<AppRule> = emptyList(),

    val packageName: String = "",
    val mode: AppRuleMode = AppRuleMode.WHITELIST,
    val filterString: String? = "",

    val isAddingAppRule: Boolean = false,
)