package com.example.sportlink.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sportlink.ui.components.LobbyItem

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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredLobbies by viewModel.filteredLobbies.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedSportFilter by viewModel.selectedSportFilter.collectAsStateWithLifecycle()
    
    var searchText by remember { mutableStateOf("") }
    val isRefreshing = uiState is HomeUiState.Loading
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SportLink") },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !isRefreshing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, contentDescription = "Create Lobby")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    if (filteredLobbies.isEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                is HomeUiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                            // Search bar
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = {
                                    searchText = it
                                    viewModel.updateSearchQuery(it)
                                },
                                label = { Text("Search lobbies...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                singleLine = true
                            )
                            
                            // Sport filter chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                val sports = listOf(null, "Fotbal", "Tenis", "Baschet")
                                items(sports.size) { index ->
                                    val sport = sports[index]
                                    FilterChip(
                                        selected = selectedSportFilter == sport,
                                        onClick = { viewModel.updateSportFilter(sport) },
                                        label = { Text(sport ?: "Toate") }
                                    )
                                }
                            }
                            
                            // Results count (5p UX)
                            if (filteredLobbies.isNotEmpty()) {
                                Text(
                                    text = "${filteredLobbies.size} lobby-uri găsite",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            
                            // Lobby list
                            if (filteredLobbies.isEmpty()) {
                                // Empty state (5p Gestionarea Stărilor UI)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (state.lobbies.isEmpty()) {
                                            "Nu există lobby-uri disponibile"
                                        } else {
                                            "Nu s-au găsit lobby-uri care să corespundă criteriilor"
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredLobbies) { lobby ->
                                        LobbyItem(
                                            lobby = lobby,
                                            onClick = { onNavigateToDetails(lobby.id) }
                                        )
                                    }
                                }
                            }
                        }
                }
                is HomeUiState.Error -> {
                    // Error state with Retry button (5p Gestionarea Stărilor UI)
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.loadLobbies() }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

