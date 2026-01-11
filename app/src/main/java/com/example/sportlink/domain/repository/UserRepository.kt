package com.example.sportlink.domain.repository

import com.example.sportlink.domain.model.UserProfile
import com.example.sportlink.domain.util.Result

/**
 * Repository interface for user operations.
 * Handles user profile and authentication state.
 */
interface UserRepository {
    /**
     * Saves user profile (nickname) to DataStore.
     */
    suspend fun saveProfile(nickname: String): Result<Unit>
    
    /**
     * Gets user profile from DataStore.
     */
    suspend fun getProfile(): Result<UserProfile>
    
    /**
     * Logs out the user by clearing DataStore.
     */
    suspend fun logout(): Result<Unit>
}

