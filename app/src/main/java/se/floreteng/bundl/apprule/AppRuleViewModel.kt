package se.floreteng.bundl.apprule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppRuleViewModel(
    private val repository: AppRuleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppRuleState())
    private val _appRules = repository.getAppRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val state = combine(_state, _appRules) { state, appRules ->
        state.copy(appRules = appRules)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppRuleState())

    fun onEvent(event: AppRuleEvent) {
        when (event) {
            is AppRuleEvent.SetPackageName -> {
                _state.update { it.copy(packageName = event.packageName) }
            }
            is AppRuleEvent.SetMode -> {
                _state.update { it.copy(mode = event.mode) }
            }
            is AppRuleEvent.SetFilterString -> {
                _state.update { it.copy(filterString = event.filterString) }
            }
            is AppRuleEvent.SaveAppRule -> {
                viewModelScope.launch {
                    val currentState = _state.value
                    if (currentState.packageName.isBlank()) {
                        return@launch
                    }

                    val appRule = AppRule(
                        id = currentState.editingAppRule?.id,
                        packageName = currentState.packageName,
                        mode = currentState.mode,
                        filterString = currentState.filterString.ifBlank { null }
                    )
                    repository.upsertAppRule(appRule)

                    // Reset form
                    _state.update {
                        it.copy(
                            packageName = "",
                            mode = AppRuleMode.BLACKLIST,
                            filterString = "",
                            isDialogVisible = false,
                            editingAppRule = null
                        )
                    }
                }
            }
            is AppRuleEvent.DeleteAppRule -> {
                viewModelScope.launch {
                    repository.deleteAppRule(event.appRule)
                }
            }
            is AppRuleEvent.EditAppRule -> {
                _state.update {
                    it.copy(
                        packageName = event.appRule.packageName,
                        mode = event.appRule.mode,
                        filterString = event.appRule.filterString ?: "",
                        isDialogVisible = true,
                        editingAppRule = event.appRule
                    )
                }
            }
            AppRuleEvent.ShowDialog -> {
                _state.update {
                    it.copy(
                        isDialogVisible = true,
                        editingAppRule = null,
                        packageName = "",
                        mode = AppRuleMode.BLACKLIST,
                        filterString = ""
                    )
                }
            }
            AppRuleEvent.HideDialog -> {
                _state.update {
                    it.copy(
                        isDialogVisible = false,
                        editingAppRule = null,
                        packageName = "",
                        mode = AppRuleMode.BLACKLIST,
                        filterString = ""
                    )
                }
            }
        }
    }
}
