package com.example.sportlink.data.repository

import com.example.sportlink.data.api.SportApi
import com.example.sportlink.data.dto.toDomainModel
import com.example.sportlink.data.local.dao.LobbyDao
import com.example.sportlink.data.local.entity.toDomainModel
import com.example.sportlink.domain.model.Lobby
import com.example.sportlink.domain.model.toEntity
import com.example.sportlink.domain.repository.LobbyRepository
import com.example.sportlink.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LobbyRepository.
 * This is the "single source of truth" for lobby data.
 * Handles API calls and local database operations.
 * 
 * All dependencies are injected via constructor (10p DI - not created manually).
 */
@Singleton
class LobbyRepositoryImpl @Inject constructor(
    private val sportApi: SportApi,
    private val lobbyDao: LobbyDao
) : LobbyRepository {
    
    /**
     * Fetches all lobbies from API.
     * Used for Home Screen.
     */
    override suspend fun getAllLobbies(): Result<List<Lobby>> {
        return try {
            val dtos = sportApi.getAllLobbies()
            val lobbies = dtos.map { it.toDomainModel() }
            Result.Success(lobbies)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Fetches a specific lobby by ID from API.
     */
    override suspend fun getLobbyById(id: String): Result<Lobby> {
        return try {
            val dto = sportApi.getLobbyById(id)
            Result.Success(dto.toDomainModel())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Creates a new lobby via POST request.
     */
    override suspend fun createLobby(lobby: Lobby): Result<Lobby> {
        return try {
            val request = com.example.sportlink.data.dto.CreateLobbyRequest(
                sportName = lobby.sportName,
                location = lobby.location,
                date = lobby.date,
                maxPlayers = lobby.maxPlayers,
                description = lobby.description
            )
            val dto = sportApi.createLobby(request)
            val createdLobby = dto.toDomainModel()
            Result.Success(createdLobby)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Gets all joined lobbies from Room database.
     * Returns Flow for reactive updates (works offline).
     */
    override fun getJoinedLobbies(): Flow<List<Lobby>> {
        return lobbyDao.getAllJoinedLobbies().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    /**
     * Saves a lobby to Room database (user joins the lobby).
     */
    override suspend fun joinLobby(lobby: Lobby): Result<Unit> {
        return try {
            lobbyDao.insertLobby(lobby.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Removes a lobby from Room database (user leaves the lobby).
     */
    override suspend fun leaveLobby(lobbyId: String): Result<Unit> {
        return try {
            lobbyDao.deleteLobbyById(lobbyId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

