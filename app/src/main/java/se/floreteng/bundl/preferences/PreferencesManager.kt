package se.floreteng.bundl.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bundl_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        private val BUNDLING_ENABLED_KEY = booleanPreferencesKey("bundling_enabled")
    }

    val isBundlingEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[BUNDLING_ENABLED_KEY] ?: false
        }

    suspend fun setBundlingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BUNDLING_ENABLED_KEY] = enabled
        }
    }
}
