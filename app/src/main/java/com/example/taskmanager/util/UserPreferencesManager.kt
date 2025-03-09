package com.example.taskmanager.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesManager @Inject constructor(  // ✅ Add @Inject
    private val dataStore: DataStore<Preferences>  // Ensure DataStore is correctly injected
) {
    companion object {
        private val PRIMARY_COLOR_KEY = intPreferencesKey("primary_color")
    }

    val primaryColorFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PRIMARY_COLOR_KEY] ?: 0xFF6200EE.toInt() // Default Color
    }

    suspend fun savePrimaryColor(color: Int) {
        dataStore.edit { preferences ->
            preferences[PRIMARY_COLOR_KEY] = color
        }
    }
}