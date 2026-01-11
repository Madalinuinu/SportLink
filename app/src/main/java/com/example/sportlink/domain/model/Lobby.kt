package com.example.sportlink.domain.model

import com.example.sportlink.data.local.entity.LobbyEntity

/**
 * Domain model for a Lobby (sport match/event).
 * This is a pure Kotlin data class with no Android dependencies.
 * Represents a lobby in the domain layer of Clean Architecture.
 */
data class Lobby(
    val id: String,
    val sportName: String,
    val location: String,
    val date: String,
    val maxPlayers: Int,
    val joinedPlayers: Int,
    val imageUrl: String? = null,
    val description: String? = null,
    val createdAt: String? = null
)

/**
 * Extension function to convert Domain model to Entity.
 * This allows the domain layer to be converted to data layer entities.
 */
fun Lobby.toEntity(): LobbyEntity {
    return LobbyEntity(
        id = id,
        sportName = sportName,
        location = location,
        date = date,
        maxPlayers = maxPlayers,
        joinedPlayers = joinedPlayers,
        imageUrl = imageUrl,
        description = description,
        createdAt = createdAt
    )
}

