package com.example.sportlink.ui.screens.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Create Lobby Screen composable.
 * Allows users to create a new lobby.
 * 
 * @param onNavigateBack Callback to navigate back
 * @param viewModel The ViewModel for this screen (injected via Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLobbyScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateLobbyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Navigate back on success
    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateLobbyUiState.Success -> {
                onNavigateBack()
            }
            is CreateLobbyUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as CreateLobbyUiState.Error).message)
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Lobby") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sport Name - Simple selection with chips
            Text(
                text = "Select Sport:",
                style = MaterialTheme.typography.titleMedium
            )
            val sports = listOf("Fotbal", "Tenis", "Baschet", "Volei", "Basket")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sports.forEach { sport ->
                    FilterChip(
                        selected = viewModel.sportName == sport,
                        onClick = { viewModel.updateSportName(sport) },
                        label = { Text(sport) },
                        enabled = uiState !is CreateLobbyUiState.Loading
                    )
                }
            }
            
            // Display selected sport
            if (viewModel.sportName.isNotBlank()) {
                Text(
                    text = "Selected: ${viewModel.sportName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Location
            OutlinedTextField(
                value = viewModel.location,
                onValueChange = { viewModel.updateLocation(it) },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateLobbyUiState.Loading,
                singleLine = true
            )
            
            // Date (simplified - can be improved with DatePicker)
            OutlinedTextField(
                value = viewModel.date,
                onValueChange = { viewModel.updateDate(it) },
                label = { Text("Date (YYYY-MM-DD HH:MM)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateLobbyUiState.Loading,
                singleLine = true,
                placeholder = { Text("2024-03-15 18:00") }
            )
            
            // Max Players
            OutlinedTextField(
                value = viewModel.maxPlayers,
                onValueChange = { viewModel.updateMaxPlayers(it) },
                label = { Text("Max Players") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateLobbyUiState.Loading,
                singleLine = true
            )
            
            // Description
            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateLobbyUiState.Loading,
                minLines = 3,
                maxLines = 5
            )
            
            // Create Button
            Button(
                onClick = { viewModel.createLobby() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateLobbyUiState.Loading
            ) {
                if (uiState is CreateLobbyUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text("CREATE")
            }
        }
    }
}

