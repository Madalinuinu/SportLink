package com.example.sportlink.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportlink.ui.components.LobbyItem
import com.example.sportlink.ui.components.LobbyItemSkeleton
import com.example.sportlink.ui.components.ModernEmptyState
import com.example.sportlink.ui.components.ModernErrorState
import com.example.sportlink.ui.components.ModernLoadingState

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
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "SportLink",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !isRefreshing
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { onNavigateToProfile() }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToCreate() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Lobby",
                    modifier = Modifier.size(28.dp)
                )
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
                        ModernLoadingState(
                            message = "Se încarcă lobby-urile...",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                is HomeUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // Modern search bar with gradient background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                label = { Text("Caută lobby-uri...") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { 
                                            viewModel.updateSearchQuery("")
                                        }) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                            
                        // Modern sport filter chips
                        val sports = remember { listOf(null, "Fotbal", "Tenis", "Baschet") }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(sports.size) { index ->
                                val sport = sports[index]
                                val isSelected = selectedSportFilter == sport
                                
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateSportFilter(sport) },
                                    enabled = true,
                                    label = { 
                                        Text(
                                            text = sport ?: "Toate",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    modifier = Modifier,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }
                            
                        // Results count with modern badge
                        if (filteredLobbies.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${filteredLobbies.size} lobby-uri găsite",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        
                        // Lobby list
                        if (filteredLobbies.isEmpty()) {
                            ModernEmptyState(
                                icon = Icons.Default.SportsSoccer,
                                title = if (state.lobbies.isEmpty()) {
                                    "Nu există lobby-uri"
                                } else {
                                    "Nu s-au găsit rezultate"
                                },
                                message = if (state.lobbies.isEmpty()) {
                                    "Fii primul care creează un lobby și găsește parteneri de joc!"
                                } else {
                                    "Încearcă să modifici filtrele sau termenii de căutare"
                                },
                                actionText = if (isEmptyState) "Creează primul lobby!" else null,
                                onAction = if (isEmptyState) { { onNavigateToCreate() } } else null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Animated list items with key() for optimization (Optimizare Compose)
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = filteredLobbies,
                                    key = { it.id } // Optimized: key() prevents unnecessary recompositions
                                ) { lobby ->
                                    // Optimized: Removed AnimatedVisibility wrapper for better performance
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
                is HomeUiState.Error -> {
                    ModernErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadLobbies() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

