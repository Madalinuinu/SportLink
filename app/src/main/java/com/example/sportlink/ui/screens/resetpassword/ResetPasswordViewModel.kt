package com.example.sportlink.ui.screens.resetpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * Sealed class representing UI states for Reset Password Screen.
 */
sealed class ResetPasswordUiState {
    object Idle : ResetPasswordUiState()
    object Loading : ResetPasswordUiState()
    object Success : ResetPasswordUiState()
    data class Error(val message: String) : ResetPasswordUiState()
}

/**
 * ViewModel for Reset Password Screen.
 */
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ResetPasswordUiState>(ResetPasswordUiState.Idle)
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()
    
    /**
     * Resets password with the given email, code, and new password.
     */
    fun resetPassword(email: String, code: String, newPassword: String, confirmPassword: String) {
        // Validare cod
        if (code.length != 6) {
            _uiState.value = ResetPasswordUiState.Error("Codul trebuie să aibă 6 cifre")
            return
        }
        
        if (!code.all { it.isDigit() }) {
            _uiState.value = ResetPasswordUiState.Error("Codul trebuie să conțină doar cifre")
            return
        }
        
        // Validare parolă: minim 8 caractere, literă mare, cifră
        val (isPasswordValid, passwordError) = PasswordValidator.validatePassword(newPassword)
        if (!isPasswordValid) {
            _uiState.value = ResetPasswordUiState.Error(passwordError ?: "Parolă invalidă")
            return
        }
        
        if (newPassword != confirmPassword) {
            _uiState.value = ResetPasswordUiState.Error("Parolele nu se potrivesc")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = ResetPasswordUiState.Loading
            
            when (val result = userRepository.resetPassword(email, code, newPassword)) {
                is Result.Success -> {
                    _uiState.value = ResetPasswordUiState.Success
                }
                is Result.Error -> {
                    _uiState.value = ResetPasswordUiState.Error(
                        result.exception.message ?: "Resetarea parolei a eșuat"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = ResetPasswordUiState.Loading
                }
            }
        }
    }
}

