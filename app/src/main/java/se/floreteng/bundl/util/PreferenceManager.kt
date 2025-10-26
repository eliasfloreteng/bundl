package se.floreteng.bundl.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "bundl_preferences"
        private const val KEY_BUNDLING_ENABLED = "bundling_enabled"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }

    fun isBundlingEnabled(): Boolean {
        return prefs.getBoolean(KEY_BUNDLING_ENABLED, true)
    }

    fun setBundlingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BUNDLING_ENABLED, enabled).apply()
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunch(isFirst: Boolean) {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, isFirst).apply()
    }
}
