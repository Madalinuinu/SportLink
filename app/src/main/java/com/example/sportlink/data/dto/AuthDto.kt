package com.example.sportlink.data.dto

/**
 * Request DTOs for authentication.
 * Used for API requests to backend PostgreSQL.
 */

/**
 * Request for user registration with email/password.
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String
)

/**
 * Request for user login with email/password.
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Request for forgot password (request password reset).
 */
data class ForgotPasswordRequest(
    val email: String
)

/**
 * Request for reset password with code (similar to verify-email).
 */
data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

/**
 * Request for email verification.
 * Used after register to verify email code.
 */
data class VerifyEmailRequest(
    val email: String,
    val code: String,
    val password: String,
    val nickname: String
)

/**
 * Request for updating user profile.
 * All fields are optional - only provided fields will be updated.
 */
data class UpdateProfileRequest(
    val nickname: String? = null,
    val bio: String? = null
)

/**
 * Response DTOs for authentication.
 */

/**
 * Response from authentication endpoints (login/register).
 * Contains JWT token and user profile data.
 */
data class AuthResponse(
    val token: String,
    val user: UserProfileDto
)

/**
 * Response for forgot password.
 * Contains message and token (for testing - token should not be returned in production).
 */
data class MessageResponse(
    val message: String,
    val token: String? = null // Pentru testare - în producție nu returna token-ul!
)

/**
 * User profile DTO from backend.
 * Extended with profile picture, bio, and photos (Instagram-like).
 */
data class UserProfileDto(
    val userId: String,
    val email: String,
    val nickname: String,
    val profilePictureUrl: String?,
    val bio: String?,
    val photos: List<UserPhotoDto> = emptyList()
)

/**
 * User photo DTO from backend.
 * Represents a photo uploaded by the user.
 */
data class UserPhotoDto(
    val id: String,
    val photoUrl: String,
    val caption: String?
)

