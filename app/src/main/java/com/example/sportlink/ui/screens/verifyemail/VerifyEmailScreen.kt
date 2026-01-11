package com.example.sportlink.ui.screens.verifyemail

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Verify Email Screen composable.
 * Allows users to enter verification code received via email.
 * 
 * @param email User's email address
 * @param password User's password (from register)
 * @param nickname User's nickname (from register)
 * @param onVerificationSuccess Callback when verification is successful
 * @param onNavigateBack Callback to navigate back to register
 * @param viewModel The ViewModel for this screen (injected via Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    email: String,
    password: String,
    nickname: String,
    onVerificationSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: VerifyEmailViewModel = hiltViewModel()
) {
    var verificationCode by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is VerifyEmailUiState.Success -> {
                snackbarHostState.showSnackbar("Contul a fost creat cu succes!")
                kotlinx.coroutines.delay(1500)
                onVerificationSuccess()
            }
            is VerifyEmailUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as VerifyEmailUiState.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
            ) {
                Text(
                    text = "Verificare Email",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                Text(
                    text = "Am trimis un cod de verificare pe adresa:\n$email",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    enabled = uiState !is VerifyEmailUiState.Loading,
                    singleLine = true,
                    placeholder = { Text("000000") }
                )

                Button(
                    onClick = { 
                        viewModel.verifyEmail(email, verificationCode, password, nickname)
                    },
                    enabled = uiState !is VerifyEmailUiState.Loading && verificationCode.length == 6,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp)
                ) {
                    if (uiState is VerifyEmailUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text("Verifică Email")
                }

                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Înapoi la înregistrare")
                }
            }
        }
    }
}

