package se.floreteng.bundl.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationState())
    private val _notifications = repository.getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val state = combine(_state, _notifications) { state, notifications ->
        state.copy(notifications = notifications)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationState())

    fun showNotificationDetails(notification: Notification) {
        _state.update {
            it.copy(
                selectedNotification = notification,
                isDetailDialogVisible = true
            )
        }
    }

    fun hideNotificationDetails() {
        _state.update {
            it.copy(
                selectedNotification = null,
                isDetailDialogVisible = false
            )
        }
    }

    fun deleteNotification(notification: Notification) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            repository.deleteAllNotifications()
        }
    }
}
