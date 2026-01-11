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
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val date: String, // ISO 8601 format
    val maxPlayers: Int,
    val joinedPlayers: Int,
    val imageUrl: String? = null,
    val description: String? = null,
    val createdAt: String? = null,
    val creatorNickname: String? = null,
    val creatorEmail: String? = null,
    val participants: List<LobbyParticipant> = emptyList()
)

/**
 * Domain model for a Lobby Participant.
 */
data class LobbyParticipant(
    val userId: String,
    val nickname: String,
    val email: String,
    val joinedAt: String
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

