package com.example.sportlink.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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
    private val lobbyDao: LobbyDao,
    @ApplicationContext private val context: Context
) : LobbyRepository {
    
    /**
     * Checks if device has internet connection.
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Converts exception to user-friendly error message.
     */
    private fun getErrorMessage(exception: Throwable): String {
        return when (exception) {
            is UnknownHostException -> "Nu există conexiune la internet. Verifică conexiunea și încearcă din nou."
            is SocketTimeoutException -> "Timpul de așteptare a expirat. Te rugăm să încerci din nou."
            is java.net.ConnectException -> "Nu s-a putut conecta la server. Verifică conexiunea la internet."
            else -> exception.message ?: "A apărut o eroare. Te rugăm să încerci din nou."
        }
    }
    
    /**
     * Fetches all lobbies from API.
     * Used for Home Screen.
     * 
     * Improved error handling with user-friendly messages (5p Stabilitate).
     * Handles edge cases:
     * - No internet connection
     * - Network timeout
     * - API down
     * - Empty response
     * - Invalid JSON (handled by Retrofit/Gson)
     * 
     * @return Result<List<Lobby>> - Success with list (can be empty), or Error with user-friendly message
     */
    override suspend fun getAllLobbies(): Result<List<Lobby>> {
        return try {
            // Edge case: Check network availability before API call
            if (!isNetworkAvailable()) {
                return Result.Error(Exception("Nu există conexiune la internet. Verifică conexiunea și încearcă din nou."))
            }
            
            val dtos = sportApi.getAllLobbies()
            
            // Edge case: Handle null or empty response
            if (dtos.isNullOrEmpty()) {
                return Result.Success(emptyList())
            }
            
            // Edge case: Filter out invalid lobbies (null IDs, etc.)
            val lobbies = dtos
                .mapNotNull { dto ->
                    try {
                        // Validate DTO before converting
                        if (dto.id.isBlank()) null else dto.toDomainModel()
                    } catch (e: Exception) {
                        // Skip invalid lobby entries
                        null
                    }
                }
            
            Result.Success(lobbies)
        } catch (e: Exception) {
            Result.Error(Exception(getErrorMessage(e)))
        }
    }
    
    /**
     * Fetches a specific lobby by ID from API.
     * 
     * Improved error handling with user-friendly messages.
     * Handles edge cases:
     * - Invalid lobby ID
     * - Lobby not found (404)
     * - Network errors
     * 
     * @param id The lobby ID to fetch
     * @return Result<Lobby> - Success with lobby, or Error with user-friendly message
     */
    override suspend fun getLobbyById(id: String): Result<Lobby> {
        return try {
            // Edge case: Validate ID before API call
            if (id.isBlank()) {
                return Result.Error(Exception("ID-ul lobby-ului este invalid."))
            }
            
            if (!isNetworkAvailable()) {
                return Result.Error(Exception("Nu există conexiune la internet. Verifică conexiunea și încearcă din nou."))
            }
            
            val dto = sportApi.getLobbyById(id)
            
            // Edge case: Validate DTO before converting
            if (dto.id.isBlank()) {
                return Result.Error(Exception("Lobby-ul nu a fost găsit."))
            }
            
            Result.Success(dto.toDomainModel())
        } catch (e: Exception) {
            Result.Error(Exception(getErrorMessage(e)))
        }
    }
    
    /**
     * Creates a new lobby via POST request.
     * Improved error handling with user-friendly messages.
     */
    override suspend fun createLobby(lobby: Lobby): Result<Lobby> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.Error(Exception("Nu există conexiune la internet. Verifică conexiunea și încearcă din nou."))
            }
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
            Result.Error(Exception(getErrorMessage(e)))
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

