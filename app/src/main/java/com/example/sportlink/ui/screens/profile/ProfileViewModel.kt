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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Profile Screen.
 * Manages user profile and joined lobbies.
 * 
 * All dependencies are injected via constructor (10p DI).
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val lobbyRepository: LobbyRepository
) : ViewModel() {
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    // Observe joined lobbies from Room (works offline - 10p Persistență Date)
    private val _joinedLobbies = MutableStateFlow<List<Lobby>>(emptyList())
    val joinedLobbies: StateFlow<List<Lobby>> = _joinedLobbies.asStateFlow()
    
    init {
        loadUserProfile()
        observeJoinedLobbies()
    }
    
    /**
     * Observes joined lobbies from Room database.
     */
    private fun observeJoinedLobbies() {
        viewModelScope.launch {
            lobbyRepository.getJoinedLobbies().collect { lobbies ->
                _joinedLobbies.value = lobbies
            }
        }
    }
    
    /**
     * Loads user profile from DataStore.
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            when (val result = userRepository.getProfile()) {
                is Result.Success -> {
                    _userProfile.value = result.data
                }
                else -> {}
            }
        }
    }
    
    /**
     * Leaves a lobby (removes from Room).
     */
    fun leaveLobby(lobbyId: String) {
        viewModelScope.launch {
            lobbyRepository.leaveLobby(lobbyId)
        }
    }
    
    /**
     * Logs out the user (clears DataStore).
     */
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}

