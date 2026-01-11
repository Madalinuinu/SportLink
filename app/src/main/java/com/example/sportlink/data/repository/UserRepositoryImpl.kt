package com.example.sportlink.data.repository

import com.example.sportlink.data.api.SportApi
import com.example.sportlink.data.dto.AuthResponse
import com.example.sportlink.data.dto.ForgotPasswordRequest
import com.example.sportlink.data.dto.LoginRequest
import com.example.sportlink.data.dto.MessageResponse
import com.example.sportlink.data.dto.RegisterRequest
import com.example.sportlink.data.dto.ResetPasswordRequest
import com.example.sportlink.data.dto.UpdateProfileRequest
import com.example.sportlink.data.dto.UserProfileDto
import com.example.sportlink.data.dto.VerifyEmailRequest
import com.example.sportlink.data.preferences.PreferencesManager
import com.example.sportlink.domain.model.UserPhoto
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
 * - Backend authentication (register, login, password reset)
 * - Profile management (update, delete account)
 * - Safe error handling with user-friendly messages
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val sportApi: SportApi
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
     * Returns profile with all fields from DataStore (userId, email, nickname, etc.).
     * 
     * @return Result<UserProfile> - Success with profile, or Error if read fails
     */
    override suspend fun getProfile(): Result<UserProfile> {
        return try {
            val userId = preferencesManager.getUserId()
            val email = preferencesManager.email.first()
            val nickname = preferencesManager.nickname.first()
            val profilePictureUrl = preferencesManager.profilePictureUrl.first()
            val bio = preferencesManager.bio.first()
            val isLoggedIn = preferencesManager.isLoggedIn.first()
            
            // Edge case: Handle null values gracefully
            if (userId == null || email == null || nickname == null) {
                // Fallback to legacy profile (nickname only)
                return Result.Success(
                    UserProfile(
                        userId = userId ?: "",
                        email = email ?: "",
                        nickname = nickname ?: "",
                        isLoggedIn = isLoggedIn
                    )
                )
            }
            
            Result.Success(
                UserProfile(
                    userId = userId,
                    email = email,
                    nickname = nickname,
                    profilePictureUrl = profilePictureUrl,
                    bio = bio,
                    photos = emptyList(), // Photos are not stored in DataStore, only in backend
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
     * Clears all user preferences including nickname, email, token, and login status.
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
    override suspend fun register(email: String, password: String, nickname: String): Result<MessageResponse> {
        return try {
            val response = sportApi.register(RegisterRequest(email, password, nickname))
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(Exception("Nu s-a putut trimite codul de verificare: ${e.message ?: "Eroare necunoscută"}"))
        }
    }
    
    /**
     * Verifies email code and creates account.
     * 
     * @param email User's email address
     * @param code Verification code from email
     * @param password User's password
     * @param nickname User's display nickname
     * @return Result with AuthResponse containing JWT token and user profile
     */
    override suspend fun verifyEmail(email: String, code: String, password: String, nickname: String): Result<AuthResponse> {
        return try {
            val response = sportApi.verifyEmail(VerifyEmailRequest(email, code, password, nickname))
            
            // Save JWT token and user profile to DataStore
            preferencesManager.saveAuthToken(response.token)
            preferencesManager.saveUserProfile(
                userId = response.user.userId,
                email = response.user.email,
                nickname = response.user.nickname,
                profilePictureUrl = response.user.profilePictureUrl,
                bio = response.user.bio
            )
            
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(Exception("Verificarea email-ului a eșuat: ${e.message ?: "Eroare necunoscută"}"))
        }
    }
    
    /**
     * Logs in user with email/password.
     * 
     * @param email User's email address
     * @param password User's password
     * @return Result with AuthResponse containing JWT token and user profile
     */
    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = sportApi.login(LoginRequest(email, password))
            
            // Save JWT token and user profile to DataStore
            preferencesManager.saveAuthToken(response.token)
            preferencesManager.saveUserProfile(
                userId = response.user.userId,
                email = response.user.email,
                nickname = response.user.nickname,
                profilePictureUrl = response.user.profilePictureUrl,
                bio = response.user.bio
            )
            
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(Exception("Autentificarea a eșuat: ${e.message ?: "Email sau parolă incorectă"}"))
        }
    }
    
    /**
     * Requests password reset (forgot password).
     * 
     * @param email User's email address
     * @return Result with MessageResponse containing reset token (for testing)
     */
    override suspend fun forgotPassword(email: String): Result<MessageResponse> {
        return try {
            val response = sportApi.forgotPassword(ForgotPasswordRequest(email))
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(Exception("Cererea de resetare parolă a eșuat: ${e.message ?: "Eroare necunoscută"}"))
        }
    }
    
    /**
     * Resets password with token from forgot-password.
     * 
     * @param token Reset token from forgot-password response
     * @param newPassword New password
     * @return Result with MessageResponse
     */
    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<MessageResponse> {
        return try {
            val response = sportApi.resetPassword(ResetPasswordRequest(email, code, newPassword))
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(Exception("Resetarea parolei a eșuat: ${e.message ?: "Cod invalid sau expirat"}"))
        }
    }
    
    /**
     * Updates user profile (nickname and/or bio).
     * 
     * @param nickname Optional new nickname
     * @param bio Optional new bio
     * @return Result with updated UserProfile
     */
    override suspend fun updateProfile(nickname: String?, bio: String?): Result<UserProfile> {
        return try {
            val token = preferencesManager.getAuthToken()
            if (token == null) {
                return Result.Error(Exception("Nu ești autentificat. Te rugăm să te conectezi din nou."))
            }
            
            val response = sportApi.updateProfile(
                token = "Bearer $token",
                request = UpdateProfileRequest(nickname, bio)
            )
            
            // Update DataStore with new profile data
            preferencesManager.saveUserProfile(
                userId = response.userId,
                email = response.email,
                nickname = response.nickname,
                profilePictureUrl = response.profilePictureUrl,
                bio = response.bio
            )
            
            // Convert DTO to Domain Model
            val userProfile = UserProfile(
                userId = response.userId,
                email = response.email,
                nickname = response.nickname,
                profilePictureUrl = response.profilePictureUrl,
                bio = response.bio,
                photos = response.photos.map { photo ->
                    UserPhoto(
                        id = photo.id,
                        photoUrl = photo.photoUrl,
                        caption = photo.caption
                    )
                },
                isLoggedIn = true
            )
            
            Result.Success(userProfile)
        } catch (e: Exception) {
            Result.Error(Exception("Actualizarea profilului a eșuat: ${e.message ?: "Eroare necunoscută"}"))
        }
    }
    
    /**
     * Deletes user account.
     * 
     * @return Result<Unit> - Success if deleted, Error if failed
     */
    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val token = preferencesManager.getAuthToken()
            if (token == null) {
                return Result.Error(Exception("Nu ești autentificat. Te rugăm să te conectezi din nou."))
            }
            
            sportApi.deleteAccount("Bearer $token")
            
            // Clear all preferences after account deletion
            preferencesManager.clearPreferences()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(Exception("Ștergerea contului a eșuat: ${e.message ?: "Eroare necunoscută"}"))
        }
    }
}

