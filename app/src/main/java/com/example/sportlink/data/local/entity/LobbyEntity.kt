package com.example.sportlink.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sportlink.domain.model.Lobby

/**
 * Room Entity for storing joined lobbies locally.
 * This represents the database table structure.
 */
@Entity(tableName = "joined_lobbies")
data class LobbyEntity(
    @PrimaryKey
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
 * Extension function to convert Entity to Domain model.
 */
fun LobbyEntity.toDomainModel(): Lobby {
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

/**
 * Extension function to convert Domain model to Entity.
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

