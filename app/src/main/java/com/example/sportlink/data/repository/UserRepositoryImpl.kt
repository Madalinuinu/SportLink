package com.example.sportlink.data.repository

import com.example.sportlink.data.preferences.PreferencesManager
import com.example.sportlink.domain.model.UserProfile
import com.example.sportlink.domain.repository.UserRepository
import com.example.sportlink.domain.util.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserRepository.
 * Handles user profile operations using DataStore.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : UserRepository {
    
    /**
     * Saves user profile (nickname) to DataStore.
     */
    override suspend fun saveProfile(nickname: String): Result<Unit> {
        return try {
            preferencesManager.saveNickname(nickname)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Gets user profile from DataStore.
     */
    override suspend fun getProfile(): Result<UserProfile> {
        return try {
            val nickname = preferencesManager.nickname.first()
            val isLoggedIn = preferencesManager.isLoggedIn.first()
            Result.Success(
                UserProfile(
                    nickname = nickname ?: "",
                    isLoggedIn = isLoggedIn
                )
            )
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Logs out the user by clearing DataStore.
     */
    override suspend fun logout(): Result<Unit> {
        return try {
            preferencesManager.clearPreferences()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

