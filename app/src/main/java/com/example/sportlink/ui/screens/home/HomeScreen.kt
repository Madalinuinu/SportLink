package com.example.sportlink.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Home Screen composable.
 * Displays the list of available lobbies.
 * 
 * @param onNavigateToDetails Callback when a lobby is clicked
 * @param onNavigateToCreate Callback when create button is clicked
 * @param onNavigateToProfile Callback when profile button is clicked
 * @param viewModel The ViewModel for this screen (injected via Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SportLink") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HomeUiState.Success -> {
                    if (state.lobbies.isEmpty()) {
                        Text(
                            text = "No lobbies available",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Text(
                            text = "Found ${state.lobbies.size} lobbies",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                        // Lobby list will be implemented in ETAPA 4
                    }
                }
                is HomeUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

