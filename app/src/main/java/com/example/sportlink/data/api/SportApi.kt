package com.example.sportlink.data.api

import com.example.sportlink.data.dto.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Retrofit API interface for SportLink.
 * Updated to support backend PostgreSQL with email/password authentication.
 * All functions are suspend functions to work with Coroutines (10p Management Asincron).
 */
interface SportApi {
    // ============================================
    // Auth Endpoints
    // ============================================
    
    /**
     * Register new user - sends verification code to email.
     * POST /api/auth/register
     * Does NOT create account - waits for email verification.
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): MessageResponse
    
    /**
     * Verify email code and create account.
     * POST /api/auth/verify-email
     */
    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): AuthResponse
    
    /**
     * Login user with email/password.
     * POST /api/auth/login
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    /**
     * Request password reset (forgot password).
     * POST /api/auth/forgot-password
     */
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): MessageResponse
    
    /**
     * Reset password with token.
     * POST /api/auth/reset-password
     */
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): MessageResponse
    
    // ============================================
    // User Endpoints (require JWT token)
    // ============================================
    
    /**
     * Get current user profile.
     * GET /api/users/profile
     * 
     * @param token JWT token in format "Bearer {token}"
     */
    @GET("users/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): UserProfileDto
    
    /**
     * Update user profile.
     * PUT /api/users/profile
     * 
     * @param token JWT token in format "Bearer {token}"
     */
    @PUT("users/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): UserProfileDto
    
    /**
     * Delete user account.
     * DELETE /api/users/account
     * 
     * @param token JWT token in format "Bearer {token}"
     */
    @DELETE("users/account")
    suspend fun deleteAccount(@Header("Authorization") token: String): MessageResponse
    
    // ============================================
    // Lobby Endpoints (existing - unchanged)
    // ============================================
    
    /**
     * Fetches all lobbies from the API.
     * GET /lobbies
     */
    @GET("lobbies")
    suspend fun getAllLobbies(): List<LobbyDto>
    
    /**
     * Fetches a specific lobby by ID.
     * GET /lobbies/{id}
     */
    @GET("lobbies/{id}")
    suspend fun getLobbyById(@Path("id") id: String): LobbyDto
    
    /**
     * Creates a new lobby.
     * POST /lobbies
     */
    @POST("lobbies")
    suspend fun createLobby(@Body request: CreateLobbyRequest): LobbyDto
}

