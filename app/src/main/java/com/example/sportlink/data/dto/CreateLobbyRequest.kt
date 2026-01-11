package com.example.sportlink.data.dto

/**
 * Data Transfer Object for creating a new lobby (POST request body).
 */
data class CreateLobbyRequest(
    val sportName: String,
    val location: String,
    val date: String,
    val maxPlayers: Int,
    val description: String? = null
)

