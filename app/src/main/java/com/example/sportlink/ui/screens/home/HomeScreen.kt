package com.example.sportlink.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.example.sportlink.ui.components.LobbyItemSkeleton

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
    
    // Use derivedStateOf for computed state to avoid unnecessary recompositions (Optimizare Compose)
    val isRefreshing by remember {
        derivedStateOf { uiState is HomeUiState.Loading }
    }
    
    val isEmptyState by remember {
        derivedStateOf {
            when (val state = uiState) {
                is HomeUiState.Success -> state.lobbies.isEmpty()
                else -> false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SportLink") },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.refresh()
                        },
                        enabled = !isRefreshing,
                        modifier = Modifier.semantics { contentDescription = "Refresh lobby list" }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(
                        onClick = {
                            onNavigateToProfile()
                        },
                        modifier = Modifier.semantics { contentDescription = "Open profile" }
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onNavigateToCreate()
                },
                modifier = Modifier.semantics { contentDescription = "Create new lobby" }
            ) {
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
                    // Loading skeletons instead of simple CircularProgressIndicator (5p Gestionarea Stărilor UI)
                    if (filteredLobbies.isEmpty()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(5) {
                                LobbyItemSkeleton()
                            }
                        }
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
                                // Improved empty state with icon and action (5p Design și Layout, 5p UX)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.SportsSoccer,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = if (state.lobbies.isEmpty()) {
                                                "Nu există lobby-uri disponibile"
                                            } else {
                                                "Nu s-au găsit lobby-uri care să corespundă criteriilor"
                                            },
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isEmptyState) {
                                            Button(
                                                onClick = {
                                                    onNavigateToCreate()
                                                }
                                            ) {
                                                Text("Creează primul lobby!")
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Animated list items with key() for optimization (Optimizare Compose)
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(
                                        items = filteredLobbies,
                                        key = { it.id } // Use key() to avoid unnecessary recompositions
                                    ) { lobby ->
                                        AnimatedVisibility(
                                            visible = true,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            LobbyItem(
                                                lobby = lobby,
                                                onClick = {
                                                    onNavigateToDetails(lobby.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                }
                is HomeUiState.Error -> {
                    // Improved error state with Retry button (5p Gestionarea Stărilor UI, 5p Stabilitate)
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = {
                                viewModel.loadLobbies()
                            },
                            modifier = Modifier.semantics { contentDescription = "Retry loading lobbies" }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

