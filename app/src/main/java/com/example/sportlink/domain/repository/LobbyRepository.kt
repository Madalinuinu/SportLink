package com.example.sportlink.domain.repository

import com.example.sportlink.domain.model.Lobby
import com.example.sportlink.domain.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for lobby operations.
 * This is the contract defined in the domain layer.
 * Implementation will be in the data layer.
 */
interface LobbyRepository {
    /**
     * Fetches all lobbies from the API.
     * Used for the Home Screen.
     */
    suspend fun getAllLobbies(): Result<List<Lobby>>
    
    /**
     * Fetches a specific lobby by ID.
     */
    suspend fun getLobbyById(id: String): Result<Lobby>
    
    /**
     * Creates a new lobby via POST request.
     */
    suspend fun createLobby(lobby: Lobby): Result<Lobby>
    
    /**
     * Gets all joined lobbies from local Room database.
     * Returns a Flow for reactive updates.
     * Used for "My Matches" screen (works offline).
     */
    fun getJoinedLobbies(): Flow<List<Lobby>>
    
    /**
     * Saves a lobby to Room database (user joins the lobby).
     */
    suspend fun joinLobby(lobby: Lobby): Result<Unit>
    
    /**
     * Removes a lobby from Room database (user leaves the lobby).
     */
    suspend fun leaveLobby(lobbyId: String): Result<Unit>
}

