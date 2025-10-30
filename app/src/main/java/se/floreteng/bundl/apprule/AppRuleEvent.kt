package se.floreteng.bundl.apprule

sealed interface AppRuleEvent {
    object SaveAppRule : AppRuleEvent

    data class SetPackageName(val packageName: String) : AppRuleEvent
    data class SetMode(val mode: AppRuleMode) : AppRuleEvent
    data class SetFilterString(val filterString: String?) : AppRuleEvent


    object ShowDialog : AppRuleEvent
    object HideDialog : AppRuleEvent

    data class DeleteAppRule(
        val appRule: AppRule
    ) : AppRuleEvent
}