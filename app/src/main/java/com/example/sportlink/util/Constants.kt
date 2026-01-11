package com.example.sportlink.util

/**
 * Constants used throughout the SportLink application.
 * 
 * Centralizes all application-wide constants for easier maintenance.
 * All constants are defined here to avoid magic numbers/strings throughout the codebase.
 */
object Constants {
    /**
     * MockAPI Base URL for REST API endpoints.
     * 
     * This is the base URL for all API calls to MockAPI.io.
     * All endpoints are relative to this base URL.
     */
    const val BASE_URL = "https://6963a0832d146d9f58d3ef80.mockapi.io/"
    
    /**
     * Room Database name.
     * Used when creating the Room database instance.
     */
    const val DATABASE_NAME = "sportlink_database"
    
    /**
     * Room Database version.
     * Increment this when making schema changes to trigger migrations.
     */
    const val DATABASE_VERSION = 1
    
    /**
     * DataStore preferences file name.
     * Used for storing user preferences (nickname, login status).
     */
    const val PREFERENCES_NAME = "user_preferences"
}

