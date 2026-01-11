package com.example.sportlink.ui.screens.register

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Register Screen composable.
 * Allows users to create a new account with email, password, and nickname.
 * 
 * @param onRegisterSuccess Callback when registration is successful
 * @param onNavigateToLogin Callback to navigate to login screen
 * @param viewModel The ViewModel for this screen (injected via Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (email: String, password: String, nickname: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Navigate to Home when registration is successful
        LaunchedEffect(uiState) {
            when (uiState) {
                is RegisterUiState.CodeSent -> {
                    val state = uiState as RegisterUiState.CodeSent
                    snackbarHostState.showSnackbar("Cod de verificare trimis pe email!")
                    onRegisterSuccess(state.email, state.password, state.nickname)
                }
                is RegisterUiState.Error -> {
                    snackbarHostState.showSnackbar((uiState as RegisterUiState.Error).message)
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Creează Cont",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    enabled = uiState !is RegisterUiState.Loading,
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Parolă") },
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    enabled = uiState !is RegisterUiState.Loading,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmă Parola") },
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    enabled = uiState !is RegisterUiState.Loading,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname") },
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    enabled = uiState !is RegisterUiState.Loading,
                    singleLine = true
                )
                
                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Parolele nu se potrivesc")
                            }
                        } else {
                            viewModel.register(email, password, nickname)
                        }
                    },
                    enabled = uiState !is RegisterUiState.Loading &&
                            email.isNotBlank() &&
                            password.isNotBlank() &&
                            confirmPassword.isNotBlank() &&
                            nickname.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 8.dp)
                ) {
                    if (uiState is RegisterUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text("Înregistrează-te")
                }
                
                TextButton(onClick = onNavigateToLogin) {
                    Text("Ai deja cont? Conectează-te")
                }
            }
        }
    }
}

