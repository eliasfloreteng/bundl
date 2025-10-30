package se.floreteng.bundl.apprule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppRuleViewModel(
    private val appRuleDao: AppRuleDao
) : ViewModel() {
    private val _state = MutableStateFlow(AppRuleState())

    private val _appRules = appRuleDao.getAppRules()

    val state = combine(_state, _appRules) { state, appRules ->
        state.copy(appRules = appRules)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppRuleState()
    )

    fun onEvent(event: AppRuleEvent) {
        when (event) {
            is AppRuleEvent.DeleteAppRule -> {
                viewModelScope.launch {
                    appRuleDao.deleteAppRule(event.appRule)
                }
            }

            AppRuleEvent.HideDialog -> {
                _state.update {
                    it.copy(isAddingAppRule = false)
                }
            }

            AppRuleEvent.SaveAppRule -> {
                val packageName = state.value.packageName
                val filterString = state.value.filterString
                val mode = state.value.mode

                if (packageName.isBlank() || (filterString != null && filterString.isBlank())) {
                    return
                }

                val appRule = AppRule(
                    packageName = packageName,
                    filterString = filterString,
                    mode = mode
                )
                viewModelScope.launch {
                    appRuleDao.upsertAppRule(appRule)
                }

                _state.update {
                    it.copy(
                        isAddingAppRule = false,
                        packageName = "",
                        filterString = ""
                    )
                }
            }

            is AppRuleEvent.SetFilterString -> {
                _state.update {
                    it.copy(filterString = event.filterString)
                }
            }

            is AppRuleEvent.SetMode -> {
                _state.update {
                    it.copy(mode = event.mode)
                }
            }

            is AppRuleEvent.SetPackageName -> {
                _state.update {
                    it.copy(packageName = event.packageName)
                }
            }

            AppRuleEvent.ShowDialog -> {
                _state.update {
                    it.copy(isAddingAppRule = true)
                }
            }
        }
    }
}