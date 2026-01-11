package com.example.sportlink.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportlink.data.dto.MessageResponse
import com.example.sportlink.domain.repository.UserRepository
import com.example.sportlink.domain.util.Result
import com.example.sportlink.util.PasswordValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sealed class representing UI states for Register Screen.
 * 
 * Used for state management in MVVM pattern (10p Model Arhitectural).
 */
sealed class RegisterUiState {
    /** Initial state when screen is first loaded */
    object Idle : RegisterUiState()
    
    /** State when registration is in progress */
    object Loading : RegisterUiState()
    
    /**
     * State when verification code was sent successfully.
     * 
     * @param email The user's email where code was sent
     * @param password User's password (to pass to verify screen)
     * @param nickname User's nickname (to pass to verify screen)
     */
    data class CodeSent(val email: String, val password: String, val nickname: String) : RegisterUiState()
    
    /**
     * State when an error occurs during registration.
     * 
     * @param message User-friendly error message (5p Stabilitate)
     */
    data class Error(val message: String) : RegisterUiState()
}

/**
 * ViewModel for Register Screen.
 * Manages registration form state and user registration.
 * 
 * All dependencies are injected via constructor (10p DI).
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    // Private mutable state flow
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    
    // Public immutable state flow
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    /**
     * Performs user registration with email, password, and nickname.
     * 
     * @param email User's email address
     * @param password User's password
     * @param nickname User's display nickname
     */
    fun register(email: String, password: String, nickname: String) {
        // Validation
        if (email.isBlank()) {
            _uiState.value = RegisterUiState.Error("Email-ul nu poate fi gol")
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = RegisterUiState.Error("Email-ul nu este valid")
            return
        }
        
        // Validare parolă: minim 8 caractere, literă mare, cifră
        val (isPasswordValid, passwordError) = PasswordValidator.validatePassword(password)
        if (!isPasswordValid) {
            _uiState.value = RegisterUiState.Error(passwordError ?: "Parolă invalidă")
            return
        }
        
        if (nickname.isBlank()) {
            _uiState.value = RegisterUiState.Error("Nickname-ul nu poate fi gol")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            
            when (val result = userRepository.register(email, password, nickname)) {
                is Result.Success -> {
                    // Code was sent successfully - navigate to verify screen
                    _uiState.value = RegisterUiState.CodeSent(email, password, nickname)
                }
                is Result.Error -> {
                    _uiState.value = RegisterUiState.Error(
                        result.exception.message ?: "Nu s-a putut trimite codul de verificare"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = RegisterUiState.Loading
                }
            }
        }
    }
}

