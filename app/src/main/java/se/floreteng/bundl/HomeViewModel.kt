package se.floreteng.bundl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.floreteng.bundl.preferences.PreferencesManager

class HomeViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val isBundlingEnabled: StateFlow<Boolean> = preferencesManager.isBundlingEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setBundlingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBundlingEnabled(enabled)
        }
    }
}
