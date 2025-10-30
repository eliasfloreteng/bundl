package se.floreteng.bundl.apprule

sealed interface AppRuleEvent {
    data class SetPackageName(val packageName: String) : AppRuleEvent
    data class SetMode(val mode: AppRuleMode) : AppRuleEvent
    data class SetFilterString(val filterString: String) : AppRuleEvent
    data object SaveAppRule : AppRuleEvent
    data class DeleteAppRule(val appRule: AppRule) : AppRuleEvent
    data class EditAppRule(val appRule: AppRule) : AppRuleEvent
    data object ShowDialog : AppRuleEvent
    data object HideDialog : AppRuleEvent
}
