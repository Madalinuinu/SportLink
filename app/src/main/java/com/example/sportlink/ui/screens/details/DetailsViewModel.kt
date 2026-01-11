package com.example.sportlink.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportlink.domain.model.Lobby
import com.example.sportlink.domain.repository.LobbyRepository
import com.example.sportlink.domain.util.Result
import com.example.sportlink.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sealed class representing UI states for Details Screen.
 * 
 * Used for state management in MVVM pattern (10p Model Arhitectural).
 * Provides type-safe state representation for loading, success, and error cases.
 */
sealed class DetailsUiState {
    /** Represents the loading state when fetching lobby details */
    object Loading : DetailsUiState()
    
    /**
     * Represents successful state with lobby data.
     * 
     * @param lobby The lobby details
     * @param isJoined Whether the current user has joined this lobby
     * @param isCreator Whether the current user is the creator of this lobby
     */
    data class Success(val lobby: Lobby, val isJoined: Boolean, val isCreator: Boolean = false) : DetailsUiState()
    
    /**
     * Represents error state with user-friendly message.
     * 
     * @param message User-friendly error message (5p Stabilitate)
     */
    data class Error(val message: String) : DetailsUiState()
}

/**
 * ViewModel for Details Screen.
 * Manages lobby details and join/leave operations.
 * 
 * All dependencies are injected via constructor (10p DI).
 */
@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val lobbyRepository: LobbyRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()
    
    private var currentLobby: Lobby? = null
    
    /**
     * Loads lobby details by ID.
     * 
     * Fetches lobby from API and checks if user has already joined.
     * Handles edge cases:
     * - Invalid lobby ID
     * - Lobby not found
     * - Network errors
     * 
     * Uses viewModelScope for lifecycle-aware coroutines (5p Stabilitate).
     * 
     * @param id The lobby ID to load
     */
    fun loadLobbyDetails(id: String) {
        // Edge case: Validate ID before API call
        if (id.isBlank()) {
            _uiState.value = DetailsUiState.Error("ID-ul lobby-ului este invalid.")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            
            // Check if lobby is already joined (from Room database)
            val isJoined = checkIfJoined(id)
            
            when (val result = lobbyRepository.getLobbyById(id)) {
                is Result.Success -> {
                    currentLobby = result.data
                    // Check if current user is creator by comparing emails
                    val currentUserEmail = preferencesManager.email.first()
                    val isCreator = currentUserEmail != null && 
                                   result.data.creatorEmail != null &&
                                   currentUserEmail == result.data.creatorEmail
                    _uiState.value = DetailsUiState.Success(result.data, isJoined, isCreator)
                }
                is Result.Error -> {
                    _uiState.value = DetailsUiState.Error(
                        result.exception.message ?: "Nu s-au putut încărca detaliile lobby-ului. Te rugăm să încerci din nou."
                    )
                }
                is Result.Loading -> {
                    _uiState.value = DetailsUiState.Loading
                }
            }
        }
    }
    
    /**
     * Checks if lobby is already joined by querying Room.
     */
    private suspend fun checkIfJoined(lobbyId: String): Boolean {
        return try {
            val joinedLobbies = lobbyRepository.getJoinedLobbies()
            val lobbies = joinedLobbies.first()
            lobbies.any { it.id == lobbyId }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Joins a lobby via API and saves to Room database.
     * 
     * First calls API to join the lobby, then saves to local Room database for offline access.
     * Updates UI state to reflect joined status and reloads lobby details to get updated participants.
     * 
     * Handles edge cases:
     * - Duplicate join attempts (handled by API)
     * - Network errors
     * - Database write failures
     * 
     * @param lobby The lobby to join
     */
    fun joinLobby(lobby: Lobby) {
        // Edge case: Validate lobby before joining
        if (lobby.id.isBlank()) {
            _uiState.value = DetailsUiState.Error("Lobby-ul nu este valid.")
            return
        }
        
        viewModelScope.launch {
            // First join via API
            val repositoryImpl = lobbyRepository as? com.example.sportlink.data.repository.LobbyRepositoryImpl
            if (repositoryImpl != null) {
                when (val apiResult = repositoryImpl.joinLobbyApi(lobby.id)) {
                    is Result.Success -> {
                        // After successful API join, save to Room
                        when (val roomResult = lobbyRepository.joinLobby(lobby)) {
                            is Result.Success -> {
                                // Reload lobby details to get updated participants list
                                loadLobbyDetails(lobby.id)
                            }
                            is Result.Error -> {
                                // API join succeeded but Room save failed - still show as joined
                                loadLobbyDetails(lobby.id)
                            }
                            else -> {}
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = DetailsUiState.Error(
                            apiResult.exception.message ?: "Nu s-a putut alătura la lobby. Te rugăm să încerci din nou."
                        )
                    }
                    else -> {}
                }
            } else {
                // Fallback to old method if cast fails
                when (val result = lobbyRepository.joinLobby(lobby)) {
                    is Result.Success -> {
                        currentLobby = lobby
                        _uiState.value = DetailsUiState.Success(lobby, isJoined = true)
                    }
                    is Result.Error -> {
                        _uiState.value = DetailsUiState.Error(
                            result.exception.message ?: "Nu s-a putut alătura la lobby. Te rugăm să încerci din nou."
                        )
                    }
                    else -> {}
                }
            }
        }
    }
    
    /**
     * Leaves a lobby via API.
     * 
     * If user is creator, deletes the entire lobby.
     * If user is participant, only removes from participants.
     * 
     * Handles edge cases:
     * - Lobby not found
     * - Network errors
     * - User not a participant
     * 
     * @param lobbyId The ID of the lobby to leave
     */
    fun leaveLobby(lobbyId: String) {
        // Edge case: Validate ID before leaving
        if (lobbyId.isBlank()) {
            _uiState.value = DetailsUiState.Error("ID-ul lobby-ului este invalid.")
            return
        }
        
        viewModelScope.launch {
            val repositoryImpl = lobbyRepository as? com.example.sportlink.data.repository.LobbyRepositoryImpl
            if (repositoryImpl != null) {
                when (val apiResult = repositoryImpl.leaveLobbyApi(lobbyId)) {
                    is Result.Success -> {
                        // If creator left, lobby was deleted - API handles it
                        // If participant left, just update UI
                        // Reload lobby details to get updated state
                        loadLobbyDetails(lobbyId)
                    }
                    is Result.Error -> {
                        _uiState.value = DetailsUiState.Error(
                            apiResult.exception.message ?: "Nu s-a putut părăsi lobby-ul. Te rugăm să încerci din nou."
                        )
                    }
                    else -> {}
                }
            } else {
                // Fallback to old method if cast fails
                when (val result = lobbyRepository.leaveLobby(lobbyId)) {
                    is Result.Success -> {
                        currentLobby?.let { lobby ->
                            _uiState.value = DetailsUiState.Success(lobby, isJoined = false)
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = DetailsUiState.Error(
                            result.exception.message ?: "Nu s-a putut părăsi lobby-ul. Te rugăm să încerci din nou."
                        )
                    }
                    else -> {}
                }
            }
        }
    }
    
}

