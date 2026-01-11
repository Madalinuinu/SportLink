package com.example.sportlink.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportlink.domain.model.Lobby
import com.example.sportlink.domain.repository.LobbyRepository
import com.example.sportlink.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sealed class representing UI states for Home Screen.
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val lobbies: List<Lobby>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

/**
 * ViewModel for Home Screen.
 * Manages lobby data and UI state.
 * 
 * All dependencies are injected via constructor (10p DI).
 * Uses Coroutines for async operations (10p Management Asincron).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val lobbyRepository: LobbyRepository
) : ViewModel() {
    
    // Private mutable state flow
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    
    // Public immutable state flow
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        // Load lobbies when ViewModel is created
        loadLobbies()
    }
    
    /**
     * Loads all lobbies from the repository.
     * Uses viewModelScope.launch for async operations (10p Management Asincron).
     */
    fun loadLobbies() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            
            when (val result = lobbyRepository.getAllLobbies()) {
                is Result.Success -> {
                    _uiState.value = HomeUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = HomeUiState.Error(
                        result.exception.message ?: "Failed to load lobbies"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = HomeUiState.Loading
                }
            }
        }
    }
    
    /**
     * Refreshes the lobby list.
     */
    fun refresh() {
        loadLobbies()
    }
}

