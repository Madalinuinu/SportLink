package com.example.sportlink.ui.screens.forgotpassword

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
 * Sealed class representing UI states for Forgot Password Screen.
 */
sealed class ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState()
    object Loading : ForgotPasswordUiState()
    data class Success(val email: String) : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
}

/**
 * ViewModel for Forgot Password Screen.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()
    
    /**
     * Requests password reset for the given email.
     */
    fun requestPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = ForgotPasswordUiState.Error("Email-ul nu poate fi gol")
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = ForgotPasswordUiState.Error("Email-ul nu este valid")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            
            when (val result = userRepository.forgotPassword(email)) {
                is Result.Success -> {
                    // Code was sent successfully - navigate to reset password screen
                    _uiState.value = ForgotPasswordUiState.Success(email)
                }
                is Result.Error -> {
                    _uiState.value = ForgotPasswordUiState.Error(
                        result.exception.message ?: "Cererea de resetare parolă a eșuat"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = ForgotPasswordUiState.Loading
                }
            }
        }
    }
}

