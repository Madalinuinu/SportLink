package com.example.sportlink.data.dto

import com.example.sportlink.domain.model.Lobby
import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for Lobby from API response.
 * Maps JSON fields to Kotlin properties using @SerializedName.
 */
data class LobbyDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("sportName")
    val sportName: String,
    
    @SerializedName("location")
    val location: String,
    
    @SerializedName("locationLat")
    val locationLat: Double? = null,
    
    @SerializedName("locationLng")
    val locationLng: Double? = null,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("maxPlayers")
    val maxPlayers: Int,
    
    @SerializedName("joinedPlayers")
    val joinedPlayers: Int,
    
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("creatorNickname")
    val creatorNickname: String? = null,
    
    @SerializedName("creatorEmail")
    val creatorEmail: String? = null,
    
    @SerializedName("participants")
    val participants: List<LobbyParticipantDto>? = null
)

/**
 * Data Transfer Object for Lobby Participant.
 */
data class LobbyParticipantDto(
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("nickname")
    val nickname: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("joinedAt")
    val joinedAt: String
)

/**
 * Extension function to convert DTO to Domain model.
 */
fun LobbyDto.toDomainModel(): Lobby {
    return Lobby(
        id = id,
        sportName = sportName,
        location = location,
        locationLat = locationLat,
        locationLng = locationLng,
        date = date,
        maxPlayers = maxPlayers,
        joinedPlayers = joinedPlayers,
        imageUrl = imageUrl,
        description = description,
        createdAt = createdAt,
        creatorNickname = creatorNickname,
        creatorEmail = creatorEmail,
        participants = participants?.map { 
            com.example.sportlink.domain.model.LobbyParticipant(
                userId = it.userId,
                nickname = it.nickname,
                email = it.email,
                joinedAt = it.joinedAt
            )
        } ?: emptyList()
    )
}

