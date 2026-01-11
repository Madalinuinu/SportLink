package com.example.sportlink.ui.screens.verifyemail

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
 * Sealed class representing UI states for Verify Email Screen.
 */
sealed class VerifyEmailUiState {
    /** Initial state when screen is first loaded */
    object Idle : VerifyEmailUiState()
    
    /** State when verification is in progress */
    object Loading : VerifyEmailUiState()
    
    /** State when verification is successful */
    object Success : VerifyEmailUiState()
    
    /**
     * State when an error occurs during verification.
     * 
     * @param message User-friendly error message
     */
    data class Error(val message: String) : VerifyEmailUiState()
}

/**
 * ViewModel for Verify Email Screen.
 * Manages email verification code input and verification.
 * 
 * All dependencies are injected via constructor (10p DI).
 */
@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    // Private mutable state flow
    private val _uiState = MutableStateFlow<VerifyEmailUiState>(VerifyEmailUiState.Idle)
    
    // Public immutable state flow
    val uiState: StateFlow<VerifyEmailUiState> = _uiState.asStateFlow()
    
    /**
     * Verifies email code and creates account.
     * 
     * @param email User's email address
     * @param code Verification code from email
     * @param password User's password
     * @param nickname User's display nickname
     */
    fun verifyEmail(email: String, code: String, password: String, nickname: String) {
        // Validation
        if (code.length != 6) {
            _uiState.value = VerifyEmailUiState.Error("Codul trebuie să aibă 6 cifre")
            return
        }
        
        if (!code.all { it.isDigit() }) {
            _uiState.value = VerifyEmailUiState.Error("Codul trebuie să conțină doar cifre")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = VerifyEmailUiState.Loading
            
            when (val result = userRepository.verifyEmail(email, code, password, nickname)) {
                is Result.Success -> {
                    _uiState.value = VerifyEmailUiState.Success
                }
                is Result.Error -> {
                    _uiState.value = VerifyEmailUiState.Error(
                        result.exception.message ?: "Verificarea email-ului a eșuat"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = VerifyEmailUiState.Loading
                }
            }
        }
    }
}

