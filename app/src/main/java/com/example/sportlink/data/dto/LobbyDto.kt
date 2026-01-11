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
    val createdAt: String? = null
)

/**
 * Extension function to convert DTO to Domain model.
 */
fun LobbyDto.toDomainModel(): Lobby {
    return Lobby(
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

