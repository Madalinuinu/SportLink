package com.example.sportlink.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for user preferences using DataStore.
 * Handles saving and retrieving user profile data.
 */
@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val USER_NICKNAME_KEY = stringPreferencesKey("user_nickname")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    }
    
    /**
     * Saves user nickname to DataStore.
     */
    suspend fun saveNickname(nickname: String) {
        dataStore.edit { preferences ->
            preferences[USER_NICKNAME_KEY] = nickname
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }
    
    /**
     * Gets user nickname from DataStore.
     */
    val nickname: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_NICKNAME_KEY]
    }
    
    /**
     * Gets user nickname synchronously (suspend function).
     */
    suspend fun getNickname(): String? {
        return dataStore.data.first()[USER_NICKNAME_KEY]
    }
    
    /**
     * Sets logged in status.
     */
    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = isLoggedIn
        }
    }
    
    /**
     * Checks if user is logged in.
     */
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] ?: false
    }
    
    /**
     * Clears all preferences (logout).
     */
    suspend fun clearPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

