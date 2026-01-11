package com.example.sportlink.domain.repository

import com.example.sportlink.data.dto.AuthResponse
import com.example.sportlink.data.dto.MessageResponse
import com.example.sportlink.domain.model.UserProfile
import com.example.sportlink.domain.util.Result

/**
 * Repository interface for user operations.
 * Extended with backend integration for PostgreSQL + email/password authentication.
 */
interface UserRepository {
    /**
     * Saves user profile (nickname) to DataStore.
     * Legacy method for simple nickname-based login.
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
    
    // ============================================
    // New methods for backend integration
    // ============================================
    
    /**
     * Registers a new user - sends verification code to email.
     * Does NOT create account - waits for email verification.
     * 
     * @param email User's email address
     * @param password User's password
     * @param nickname User's display nickname
     * @return Result with MessageResponse indicating code was sent
     */
    suspend fun register(email: String, password: String, nickname: String): Result<MessageResponse>
    
    /**
     * Verifies email code and creates account.
     * 
     * @param email User's email address
     * @param code Verification code from email
     * @param password User's password
     * @param nickname User's display nickname
     * @return Result with AuthResponse containing JWT token and user profile
     */
    suspend fun verifyEmail(email: String, code: String, password: String, nickname: String): Result<AuthResponse>
    
    /**
     * Logs in user with email/password.
     * 
     * @param email User's email address
     * @param password User's password
     * @return Result with AuthResponse containing JWT token and user profile
     */
    suspend fun login(email: String, password: String): Result<AuthResponse>
    
    /**
     * Requests password reset (forgot password).
     * 
     * @param email User's email address
     * @return Result with MessageResponse containing reset token (for testing)
     */
    suspend fun forgotPassword(email: String): Result<MessageResponse>
    
    /**
     * Resets password with code from forgot-password (similar to verify-email).
     * 
     * @param email User's email address
     * @param code Verification code from email
     * @param newPassword New password
     * @return Result with MessageResponse
     */
    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<MessageResponse>
    
    /**
     * Updates user profile (nickname and/or bio).
     * 
     * @param nickname Optional new nickname
     * @param bio Optional new bio
     * @return Result with updated UserProfile
     */
    suspend fun updateProfile(nickname: String?, bio: String?): Result<UserProfile>
    
    /**
     * Deletes user account.
     * 
     * @return Result<Unit> - Success if deleted, Error if failed
     */
    suspend fun deleteAccount(): Result<Unit>
}

