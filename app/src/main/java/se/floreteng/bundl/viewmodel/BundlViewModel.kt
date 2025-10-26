package se.floreteng.bundl.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import se.floreteng.bundl.data.BundlDatabase
import se.floreteng.bundl.data.model.*
import se.floreteng.bundl.util.PreferenceManager
import se.floreteng.bundl.util.ScheduleManager

class BundlViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BundlDatabase.getDatabase(application)
    private val preferenceManager = PreferenceManager(application)
    private val scheduleManager = ScheduleManager(application)

    // Flows
    val allNotifications = database.notificationDao().getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pendingNotifications = database.notificationDao().getPendingNotifications()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val appConfigs = database.appConfigDao().getAllAppConfigs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val schedules = database.scheduleDao().getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isBundlingEnabled = MutableStateFlow(preferenceManager.isBundlingEnabled())
    val isBundlingEnabled: StateFlow<Boolean> = _isBundlingEnabled.asStateFlow()

    // Pending notification counts by app
    fun getPendingCountForApp(appPackage: String): Flow<Int> {
        return database.notificationDao().getPendingCountByApp(appPackage)
    }

    // Toggle bundling
    fun toggleBundling() {
        val newState = !_isBundlingEnabled.value
        preferenceManager.setBundlingEnabled(newState)
        _isBundlingEnabled.value = newState
    }

    // App Config operations
    fun updateAppConfig(appConfig: AppConfig) {
        viewModelScope.launch {
            database.appConfigDao().update(appConfig)
        }
    }

    fun addAppConfig(appConfig: AppConfig) {
        viewModelScope.launch {
            database.appConfigDao().insert(appConfig)
        }
    }

    // Schedule operations
    fun addSchedule(schedule: NotificationSchedule) {
        viewModelScope.launch {
            database.scheduleDao().insert(schedule)
            scheduleManager.scheduleAll()
        }
    }

    fun updateSchedule(schedule: NotificationSchedule) {
        viewModelScope.launch {
            database.scheduleDao().update(schedule)
            scheduleManager.scheduleAll()
        }
    }

    fun deleteSchedule(schedule: NotificationSchedule) {
        viewModelScope.launch {
            database.scheduleDao().delete(schedule)
            scheduleManager.cancelSchedule(schedule.id)
            scheduleManager.scheduleAll()
        }
    }

    // Exemption rule operations
    fun getExemptionRulesForApp(appPackage: String): Flow<List<ExemptionRule>> {
        return database.exemptionRuleDao().getRulesForApp(appPackage)
    }

    fun addExemptionRule(rule: ExemptionRule) {
        viewModelScope.launch {
            database.exemptionRuleDao().insert(rule)
        }
    }

    fun updateExemptionRule(rule: ExemptionRule) {
        viewModelScope.launch {
            database.exemptionRuleDao().update(rule)
        }
    }

    fun deleteExemptionRule(rule: ExemptionRule) {
        viewModelScope.launch {
            database.exemptionRuleDao().delete(rule)
        }
    }

    // Trigger immediate delivery
    fun deliverNow() {
        viewModelScope.launch {
            scheduleManager.triggerImmediateDelivery()
        }
    }

    // Delete old notifications
    fun cleanOldNotifications(olderThanDays: Int = 30) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            database.notificationDao().deleteOlderThan(timestamp)
        }
    }
}
