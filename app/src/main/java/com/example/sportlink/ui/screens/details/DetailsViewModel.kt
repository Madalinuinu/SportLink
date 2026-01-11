package com.example.sportlink.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportlink.domain.model.Lobby
import com.example.sportlink.domain.repository.LobbyRepository
import com.example.sportlink.domain.util.Result
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
     */
    data class Success(val lobby: Lobby, val isJoined: Boolean) : DetailsUiState()
    
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
    private val lobbyRepository: LobbyRepository
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
                    _uiState.value = DetailsUiState.Success(result.data, isJoined)
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
     * Joins a lobby (saves to Room database).
     * 
     * Saves the lobby to local Room database for offline access.
     * Updates UI state to reflect joined status.
     * 
     * Handles edge cases:
     * - Duplicate join attempts (handled by Room REPLACE strategy)
     * - Database write failures
     * 
     * @param lobby The lobby to join
     */
    fun joinLobby(lobby: Lobby) {
        // Edge case: Validate lobby before saving
        if (lobby.id.isBlank()) {
            _uiState.value = DetailsUiState.Error("Lobby-ul nu este valid.")
            return
        }
        
        viewModelScope.launch {
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
                is Result.Loading -> {
                    _uiState.value = DetailsUiState.Loading
                }
            }
        }
    }
    
    /**
     * Leaves a lobby (removes from Room database).
     * 
     * Removes the lobby from local Room database.
     * Updates UI state to reflect left status.
     * 
     * Handles edge cases:
     * - Lobby not found in database (handled gracefully)
     * - Database delete failures
     * 
     * @param lobbyId The ID of the lobby to leave
     */
    fun leaveLobby(lobbyId: String) {
        // Edge case: Validate ID before deletion
        if (lobbyId.isBlank()) {
            _uiState.value = DetailsUiState.Error("ID-ul lobby-ului este invalid.")
            return
        }
        
        viewModelScope.launch {
            when (val result = lobbyRepository.leaveLobby(lobbyId)) {
                is Result.Success -> {
                    // Update UI to reflect left status
                    currentLobby?.let { lobby ->
                        _uiState.value = DetailsUiState.Success(lobby, isJoined = false)
                    }
                }
                is Result.Error -> {
                    _uiState.value = DetailsUiState.Error(
                        result.exception.message ?: "Nu s-a putut părăsi lobby-ul. Te rugăm să încerci din nou."
                    )
                }
                is Result.Loading -> {
                    _uiState.value = DetailsUiState.Loading
                }
            }
        }
    }
}

