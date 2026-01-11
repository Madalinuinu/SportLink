package com.example.sportlink.domain.model

/**
 * Domain model for user profile.
 * Extended with email and profile fields for backend integration.
 * Represents user information in the domain layer.
 */
data class UserProfile(
    val userId: String,
    val email: String,
    val nickname: String,
    val profilePictureUrl: String? = null,
    val bio: String? = null,
    val photos: List<UserPhoto> = emptyList(),
    val isLoggedIn: Boolean = false
)

/**
 * Domain model for user photo.
 * Represents a photo uploaded by the user (Instagram-like feature).
 */
data class UserPhoto(
    val id: String,
    val photoUrl: String,
    val caption: String? = null
)

