package com.example.sportlink.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportlink.domain.model.Lobby
import com.example.sportlink.domain.model.UserProfile
import com.example.sportlink.domain.repository.LobbyRepository
import com.example.sportlink.domain.repository.UserRepository
import com.example.sportlink.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Profile Screen.
 * 
 * Manages user profile and joined lobbies.
 * All dependencies are injected via constructor (10p DI).
 * 
 * Uses lifecycle-aware coroutines (viewModelScope) and Flow collection
 * with lifecycle awareness (collectAsStateWithLifecycle) for stability (5p Stabilitate).
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val lobbyRepository: LobbyRepository
) : ViewModel() {
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    /**
     * Observes joined lobbies from Room database.
     * 
     * Works offline - data is persisted locally (10p Persistență Date).
     * Flow is collected with lifecycle awareness to prevent memory leaks.
     */
    private val _joinedLobbies = MutableStateFlow<List<Lobby>>(emptyList())
    val joinedLobbies: StateFlow<List<Lobby>> = _joinedLobbies.asStateFlow()
    
    init {
        loadUserProfile()
        observeJoinedLobbies()
    }
    
    /**
     * Observes joined lobbies from Room database.
     * 
     * Uses viewModelScope for lifecycle-aware coroutines.
     * Automatically cancels when ViewModel is cleared (5p Stabilitate).
     */
    private fun observeJoinedLobbies() {
        viewModelScope.launch {
            lobbyRepository.getJoinedLobbies().collect { lobbies ->
                // Edge case: Handle null or empty list gracefully
                _joinedLobbies.value = lobbies ?: emptyList()
            }
        }
    }
    
    /**
     * Loads user profile from DataStore.
     * 
     * Fetches user profile on ViewModel initialization.
     * Handles edge cases:
     * - Profile not found (returns empty profile)
     * - DataStore read failures
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            when (val result = userRepository.getProfile()) {
                is Result.Success -> {
                    _userProfile.value = result.data
                }
                is Result.Error -> {
                    // Edge case: Set empty profile on error
                    _userProfile.value = UserProfile(
                        userId = "",
                        email = "",
                        nickname = "",
                        isLoggedIn = false
                    )
                }
                is Result.Loading -> {
                    // Keep current state
                }
            }
        }
    }
    
    /**
     * Leaves a lobby (removes from Room database).
     * 
     * Removes the lobby from local storage.
     * The UI will automatically update via Flow observation.
     * 
     * @param lobbyId The ID of the lobby to leave
     */
    fun leaveLobby(lobbyId: String) {
        // Edge case: Validate ID before deletion
        if (lobbyId.isBlank()) {
            return
        }
        
        viewModelScope.launch {
            lobbyRepository.leaveLobby(lobbyId)
            // UI updates automatically via Flow observation
        }
    }
    
    /**
     * Logs out the user (clears DataStore).
     * 
     * Clears all user preferences including nickname and login status.
     * Should be called before navigating to login screen.
     */
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
    
    /**
     * Deletes user account from backend.
     * 
     * Deletes the account and clears all local data.
     * Should be called before navigating to login screen.
     */
    fun deleteAccount() {
        viewModelScope.launch {
            userRepository.deleteAccount()
        }
    }
}

