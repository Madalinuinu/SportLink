package com.example.sportlink.util

/**
 * Constants used throughout the SportLink application.
 * 
 * Centralizes all application-wide constants for easier maintenance.
 * All constants are defined here to avoid magic numbers/strings throughout the codebase.
 */
object Constants {
    /**
     * Base URL for REST API endpoints.
     * 
     * For development: Use localhost backend (http://10.0.2.2:3000/api/ pentru emulator)
     * For production: Use your deployed backend URL
     * 
     * IMPORTANT: Pentru emulator Android, folosește 10.0.2.2 în loc de localhost!
     * Pentru device fizic, folosește IP-ul PC-ului tău (ex: http://192.168.1.100:3000/api/)
     */
    const val BASE_URL = "http://10.0.2.2:3000/api/" // Pentru emulator Android
    // const val BASE_URL = "http://192.168.1.100:3000/api/" // Pentru device fizic (schimbă cu IP-ul tău)
    
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

