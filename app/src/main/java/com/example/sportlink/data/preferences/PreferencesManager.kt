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
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NICKNAME_KEY = stringPreferencesKey("user_nickname")
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token") // JWT token
        private val PROFILE_PICTURE_URL_KEY = stringPreferencesKey("profile_picture_url")
        private val USER_BIO_KEY = stringPreferencesKey("user_bio")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    }
    
    /**
     * Saves user nickname to DataStore.
     * Legacy method for simple nickname-based login.
     */
    suspend fun saveNickname(nickname: String) {
        dataStore.edit { preferences ->
            preferences[USER_NICKNAME_KEY] = nickname
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }
    
    /**
     * Saves JWT authentication token from backend.
     * Used for API calls that require authentication.
     */
    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }
    
    /**
     * Gets JWT authentication token.
     */
    val authToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN_KEY]
    }
    
    /**
     * Gets JWT authentication token synchronously (suspend function).
     */
    suspend fun getAuthToken(): String? {
        return dataStore.data.first()[AUTH_TOKEN_KEY]
    }
    
    /**
     * Saves user profile from backend (after login/register).
     * Includes all profile data: userId, email, nickname, profilePictureUrl, bio.
     */
    suspend fun saveUserProfile(
        userId: String,
        email: String,
        nickname: String,
        profilePictureUrl: String? = null,
        bio: String? = null
    ) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_NICKNAME_KEY] = nickname
            preferences[IS_LOGGED_IN_KEY] = true
            profilePictureUrl?.let { preferences[PROFILE_PICTURE_URL_KEY] = it }
            bio?.let { preferences[USER_BIO_KEY] = it }
        }
    }
    
    /**
     * Gets user ID from DataStore.
     */
    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }
    
    /**
     * Gets user ID synchronously (suspend function).
     */
    suspend fun getUserId(): String? {
        return dataStore.data.first()[USER_ID_KEY]
    }
    
    /**
     * Gets user email from DataStore.
     */
    val email: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }
    
    /**
     * Gets profile picture URL from DataStore.
     */
    val profilePictureUrl: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PROFILE_PICTURE_URL_KEY]
    }
    
    /**
     * Gets user bio from DataStore.
     */
    val bio: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_BIO_KEY]
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

