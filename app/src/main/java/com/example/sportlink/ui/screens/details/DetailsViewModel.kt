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
 */
sealed class DetailsUiState {
    object Loading : DetailsUiState()
    data class Success(val lobby: Lobby, val isJoined: Boolean) : DetailsUiState()
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
     */
    fun loadLobbyDetails(id: String) {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            
            // Check if lobby is already joined
            val isJoined = checkIfJoined(id)
            
            when (val result = lobbyRepository.getLobbyById(id)) {
                is Result.Success -> {
                    currentLobby = result.data
                    _uiState.value = DetailsUiState.Success(result.data, isJoined)
                }
                is Result.Error -> {
                    _uiState.value = DetailsUiState.Error(
                        result.exception.message ?: "Failed to load lobby details"
                    )
                }
                else -> {}
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
     */
    fun joinLobby(lobby: Lobby) {
        viewModelScope.launch {
            when (val result = lobbyRepository.joinLobby(lobby)) {
                is Result.Success -> {
                    currentLobby = lobby
                    _uiState.value = DetailsUiState.Success(lobby, isJoined = true)
                }
                is Result.Error -> {
                    _uiState.value = DetailsUiState.Error(
                        result.exception.message ?: "Failed to join lobby"
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Leaves a lobby (removes from Room database).
     */
    fun leaveLobby(lobbyId: String) {
        viewModelScope.launch {
            when (val result = lobbyRepository.leaveLobby(lobbyId)) {
                is Result.Success -> {
                    currentLobby?.let { lobby ->
                        _uiState.value = DetailsUiState.Success(lobby, isJoined = false)
                    }
                }
                is Result.Error -> {
                    _uiState.value = DetailsUiState.Error(
                        result.exception.message ?: "Failed to leave lobby"
                    )
                }
                else -> {}
            }
        }
    }
}

