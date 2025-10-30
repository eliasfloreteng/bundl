package se.floreteng.bundl

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.floreteng.bundl.notifications.NotificationRepository
import se.floreteng.bundl.preferences.PreferencesManager
import se.floreteng.bundl.utils.NotificationAccessUtil
import se.floreteng.bundl.utils.NotificationDeliveryUtil

class HomeViewModel(
    private val preferencesManager: PreferencesManager,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val isBundlingEnabled: StateFlow<Boolean> = preferencesManager.isBundlingEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _shouldShowPermissionDialog = MutableStateFlow(false)
    val shouldShowPermissionDialog: StateFlow<Boolean> = _shouldShowPermissionDialog.asStateFlow()

    private val _shouldShowNotificationPermissionDialog = MutableStateFlow(false)
    val shouldShowNotificationPermissionDialog: StateFlow<Boolean> = _shouldShowNotificationPermissionDialog.asStateFlow()

    fun onBundlingToggled(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            if (enabled) {
                // Check if we need to request permission
                val hasRequestedBefore = preferencesManager.hasRequestedPermission.first()
                val hasAccess = NotificationAccessUtil.hasNotificationAccess(context)

                if (!hasRequestedBefore && !hasAccess) {
                    // First time enabling and no permission - show dialog
                    _shouldShowPermissionDialog.value = true
                } else if (hasAccess) {
                    // Has permission, just enable
                    preferencesManager.setBundlingEnabled(true)
                } else {
                    // Not first time but still no permission - show dialog
                    _shouldShowPermissionDialog.value = true
                }
            } else {
                // Disabling, just update the preference
                preferencesManager.setBundlingEnabled(false)
            }
        }
    }

    fun onPermissionDialogConfirmed(context: Context) {
        viewModelScope.launch {
            // Mark that we've requested permission
            preferencesManager.setHasRequestedPermission(true)
            // Open settings
            NotificationAccessUtil.openNotificationAccessSettings(context)
            // Hide dialog
            _shouldShowPermissionDialog.value = false
        }
    }

    fun onPermissionDialogDismissed() {
        _shouldShowPermissionDialog.value = false
    }

    fun checkAndUpdateBundlingState(context: Context) {
        viewModelScope.launch {
            // Check if bundling should be enabled based on actual permission
            val hasAccess = NotificationAccessUtil.hasNotificationAccess(context)

            if (!hasAccess) {
                preferencesManager.setHasRequestedPermission(false)

                val isEnabled = isBundlingEnabled.value
                if (isEnabled) {
                    // Was enabled but permission was revoked
                    preferencesManager.setBundlingEnabled(false)
                }
            }

        }
    }

    fun onDeliverNotificationsClicked(context: Context) {
        viewModelScope.launch {
            // Check if we have permission to post notifications
            val hasPermission = NotificationAccessUtil.hasNotificationPermission(context)

            if (!hasPermission) {
                // Show permission dialog
                _shouldShowNotificationPermissionDialog.value = true
            } else {
                // Has permission, deliver notifications
                deliverAllNotifications(context)
            }
        }
    }

    fun onNotificationPermissionDialogDismissed() {
        _shouldShowNotificationPermissionDialog.value = false
    }

    fun deliverAllNotifications(context: Context) {
        viewModelScope.launch {
            // Get all notifications from database
            val allNotifications = notificationRepository.getAllNotifications().first()

            if (allNotifications.isEmpty()) {
                return@launch
            }

            // Group notifications by package name
            val notificationsByApp = allNotifications.groupBy { it.packageName }

            // Deliver bundled notifications
            NotificationDeliveryUtil.deliverBundledNotifications(context, notificationsByApp)

            // Clear delivered notifications from database
            notificationRepository.deleteAllNotifications()
        }
    }
}
