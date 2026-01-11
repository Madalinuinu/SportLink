package com.example.sportlink.data.dto

/**
 * Data Transfer Object for creating a new lobby (POST request body).
 */
data class CreateLobbyRequest(
    val sportName: String,
    val location: String,
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val date: String, // ISO 8601 format: "2024-03-15T18:00:00Z"
    val maxPlayers: Int,
    val description: String? = null
)

