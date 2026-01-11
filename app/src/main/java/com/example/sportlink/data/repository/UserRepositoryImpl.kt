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
 * 
 * Handles user profile operations using DataStore for persistence (10p Persistență Date).
 * All dependencies are injected via constructor (10p DI).
 * 
 * This implementation provides:
 * - User profile persistence using DataStore
 * - Login/logout functionality
 * - Safe error handling with user-friendly messages
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : UserRepository {
    
    /**
     * Saves user profile (nickname) to DataStore.
     * 
     * Handles edge cases:
     * - Empty or blank nickname (validated in ViewModel)
     * - DataStore write failures
     * 
     * @param nickname The user's nickname to save
     * @return Result<Unit> - Success if saved, Error with user-friendly message if failed
     */
    override suspend fun saveProfile(nickname: String): Result<Unit> {
        return try {
            // Edge case: Validate nickname before saving
            if (nickname.isBlank()) {
                return Result.Error(Exception("Nickname-ul nu poate fi gol."))
            }
            
            preferencesManager.saveNickname(nickname)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(Exception("Nu s-a putut salva profilul. Te rugăm să încerci din nou."))
        }
    }
    
    /**
     * Gets user profile from DataStore.
     * 
     * Returns profile with empty nickname if not set (edge case handling).
     * 
     * @return Result<UserProfile> - Success with profile, or Error if read fails
     */
    override suspend fun getProfile(): Result<UserProfile> {
        return try {
            val nickname = preferencesManager.nickname.first()
            val isLoggedIn = preferencesManager.isLoggedIn.first()
            
            // Edge case: Handle null nickname gracefully
            Result.Success(
                UserProfile(
                    nickname = nickname ?: "",
                    isLoggedIn = isLoggedIn
                )
            )
        } catch (e: Exception) {
            Result.Error(Exception("Nu s-a putut încărca profilul. Te rugăm să încerci din nou."))
        }
    }
    
    /**
     * Logs out the user by clearing DataStore.
     * 
     * Clears all user preferences including nickname and login status.
     * 
     * @return Result<Unit> - Success if cleared, Error if operation fails
     */
    override suspend fun logout(): Result<Unit> {
        return try {
            preferencesManager.clearPreferences()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(Exception("Nu s-a putut efectua logout-ul. Te rugăm să încerci din nou."))
        }
    }
}

