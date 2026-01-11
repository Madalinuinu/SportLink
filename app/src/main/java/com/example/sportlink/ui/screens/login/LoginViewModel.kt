package com.example.sportlink.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportlink.domain.repository.UserRepository
import com.example.sportlink.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sealed class representing UI states for Login Screen.
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val nickname: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

/**
 * ViewModel for Login Screen.
 * Manages login form state and user authentication.
 * 
 * All dependencies are injected via constructor (10p DI).
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    // Private mutable state flow
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    
    // Public immutable state flow
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    /**
     * Performs login with the given nickname.
     * Saves nickname to DataStore and navigates to Home on success.
     * 
     * @param nickname The user's nickname
     */
    fun login(nickname: String) {
        if (nickname.isBlank()) {
            _uiState.value = LoginUiState.Error("Nickname cannot be empty")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            when (val result = userRepository.saveProfile(nickname)) {
                is Result.Success -> {
                    _uiState.value = LoginUiState.Success(nickname)
                }
                is Result.Error -> {
                    _uiState.value = LoginUiState.Error(
                        result.exception.message ?: "Failed to save profile"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = LoginUiState.Loading
                }
            }
        }
    }
}

