package com.example.sportlink.domain.model

/**
 * Domain model for user profile.
 * Represents user information in the domain layer.
 */
data class UserProfile(
    val nickname: String,
    val isLoggedIn: Boolean = false
)

