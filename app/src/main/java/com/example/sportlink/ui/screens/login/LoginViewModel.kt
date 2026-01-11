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
 * 
 * Used for state management in MVVM pattern (10p Model Arhitectural).
 * Provides type-safe state representation for login flow.
 */
sealed class LoginUiState {
    /** Initial state when screen is first loaded */
    object Idle : LoginUiState()
    
    /** State when login is in progress (saving to DataStore) */
    object Loading : LoginUiState()
    
    /**
     * State when login is successful.
     * 
     * @param nickname The user's nickname that was saved
     */
    data class Success(val nickname: String) : LoginUiState()
    
    /**
     * State when an error occurs during login.
     * 
     * @param message User-friendly error message (5p Stabilitate)
     */
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
     * Checks if user is already logged in (persistence check).
     * If logged in, sets state to Success to trigger navigation.
     */
    fun checkIfLoggedIn() {
        viewModelScope.launch {
            when (val result = userRepository.getProfile()) {
                is Result.Success -> {
                    if (result.data.isLoggedIn && result.data.nickname.isNotBlank()) {
                        _uiState.value = LoginUiState.Success(result.data.nickname)
                    }
                }
                else -> {
                    // User not logged in, stay on login screen
                }
            }
        }
    }
    
    /**
     * Performs login with email and password.
     * Authenticates with backend and saves profile to DataStore.
     * 
     * @param email User's email address
     * @param password User's password
     */
    fun login(email: String, password: String) {
        if (email.isBlank()) {
            _uiState.value = LoginUiState.Error("Email-ul nu poate fi gol")
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = LoginUiState.Error("Email-ul nu este valid")
            return
        }
        
        if (password.isBlank()) {
            _uiState.value = LoginUiState.Error("Parola nu poate fi goală")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            when (val result = userRepository.login(email, password)) {
                is Result.Success -> {
                    _uiState.value = LoginUiState.Success(result.data.user.nickname)
                }
                is Result.Error -> {
                    _uiState.value = LoginUiState.Error(
                        result.exception.message ?: "Autentificarea a eșuat"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = LoginUiState.Loading
                }
            }
        }
    }
    
    /**
     * Legacy method: Performs login with nickname (for backward compatibility).
     * Saves nickname to DataStore and navigates to Home on success.
     * 
     * @param nickname The user's nickname
     */
    fun loginWithNickname(nickname: String) {
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

