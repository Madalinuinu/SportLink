package com.example.sportlink.ui.screens.resetpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Reset Password Screen composable.
 * Allows users to reset their password with a verification code.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    email: String,
    onNavigateBack: () -> Unit,
    onPasswordResetSuccess: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    var verificationCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Navigate to Login when password reset is successful
    LaunchedEffect(uiState) {
        when (uiState) {
            is ResetPasswordUiState.Success -> {
                snackbarHostState.showSnackbar("Parola a fost resetată cu succes!")
                kotlinx.coroutines.delay(1500)
                onPasswordResetSuccess()
            }
            is ResetPasswordUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as ResetPasswordUiState.Error).message)
            }
            else -> {}
        }
    }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
            ) {
                Text(
                    text = "Resetează Parola",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Am trimis un cod de verificare pe adresa:\n$email",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { 
                        // Allow only digits and limit to 6 characters
                        if (it.all { it.isDigit() } && it.length <= 6) {
                            verificationCode = it
                        }
                    },
                    label = { Text("Cod de verificare (6 cifre)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    enabled = uiState !is ResetPasswordUiState.Loading,
                    singleLine = true,
                    placeholder = { Text("000000") }
                )
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Parolă nouă") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    enabled = uiState !is ResetPasswordUiState.Loading,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmă parola nouă") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    enabled = uiState !is ResetPasswordUiState.Loading,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                Button(
                    onClick = { 
                        viewModel.resetPassword(email, verificationCode, newPassword, confirmPassword)
                    },
                    enabled = uiState !is ResetPasswordUiState.Loading && 
                            verificationCode.length == 6 &&
                            newPassword.isNotBlank() && 
                            confirmPassword.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                ) {
                    if (uiState is ResetPasswordUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text("Resetează parola")
                }
            }
        }
    }
}
