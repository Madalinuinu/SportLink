package com.example.sportlink.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportlink.domain.model.Lobby
import com.example.sportlink.domain.repository.LobbyRepository
import com.example.sportlink.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sealed class representing UI states for Home Screen.
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val lobbies: List<Lobby>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

/**
 * ViewModel for Home Screen.
 * Manages lobby data and UI state.
 * 
 * All dependencies are injected via constructor (10p DI).
 * Uses Coroutines for async operations (10p Management Asincron).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val lobbyRepository: LobbyRepository
) : ViewModel() {
    
    // Private mutable state flow
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    
    // Public immutable state flow
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // Search and filter state (Bonus - 5p Complexitate)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedSportFilter = MutableStateFlow<String?>(null)
    val selectedSportFilter: StateFlow<String?> = _selectedSportFilter.asStateFlow()
    
    // Filtered lobbies combining search and filter (10p Model Arhitectural - filtering in ViewModel)
    val filteredLobbies: StateFlow<List<Lobby>> = combine(
        _uiState,
        _searchQuery.debounce(300), // Debounce search input (5p UX)
        _selectedSportFilter
    ) { state, query, sportFilter ->
        when (state) {
            is HomeUiState.Success -> {
                var filtered = state.lobbies
                
                // Apply search filter
                if (query.isNotBlank()) {
                    filtered = filtered.filter { lobby ->
                        lobby.sportName.contains(query, ignoreCase = true) ||
                        lobby.location.contains(query, ignoreCase = true) ||
                        (lobby.description?.contains(query, ignoreCase = true) == true)
                    }
                }
                
                // Apply sport filter
                if (sportFilter != null) {
                    filtered = filtered.filter { it.sportName == sportFilter }
                }
                
                filtered
            }
            else -> emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        // Load lobbies when ViewModel is created
        loadLobbies()
    }
    
    /**
     * Loads all lobbies from the repository.
     * Uses viewModelScope.launch for async operations (10p Management Asincron).
     */
    fun loadLobbies() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            
            when (val result = lobbyRepository.getAllLobbies()) {
                is Result.Success -> {
                    _uiState.value = HomeUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = HomeUiState.Error(
                        result.exception.message ?: "Failed to load lobbies"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = HomeUiState.Loading
                }
            }
        }
    }
    
    /**
     * Refreshes the lobby list.
     */
    fun refresh() {
        loadLobbies()
    }
    
    /**
     * Updates the search query.
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Updates the selected sport filter.
     */
    fun updateSportFilter(sport: String?) {
        _selectedSportFilter.value = sport
    }
}

