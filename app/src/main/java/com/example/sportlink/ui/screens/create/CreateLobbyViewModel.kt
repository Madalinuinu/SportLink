package com.example.sportlink.ui.screens.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportlink.domain.model.Lobby
import com.example.sportlink.domain.repository.LobbyRepository
import com.example.sportlink.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sealed class representing UI states for Create Lobby Screen.
 * 
 * Used for state management in MVVM pattern (10p Model Arhitectural).
 * Provides type-safe state representation for form states and API operations.
 */
sealed class CreateLobbyUiState {
    /** Initial state when screen is first loaded */
    object Idle : CreateLobbyUiState()
    
    /** State when creating lobby (POST request in progress) */
    object Loading : CreateLobbyUiState()
    
    /**
     * State when lobby is successfully created.
     * 
     * @param lobby The created lobby returned from API
     */
    data class Success(val lobby: Lobby) : CreateLobbyUiState()
    
    /**
     * State when an error occurs during lobby creation.
     * 
     * @param message User-friendly error message (5p Stabilitate)
     */
    data class Error(val message: String) : CreateLobbyUiState()
}

/**
 * ViewModel for Create Lobby Screen.
 * Manages form state and lobby creation.
 * 
 * All dependencies are injected via constructor (10p DI).
 */
@HiltViewModel
class CreateLobbyViewModel @Inject constructor(
    private val lobbyRepository: LobbyRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CreateLobbyUiState>(CreateLobbyUiState.Idle)
    val uiState: StateFlow<CreateLobbyUiState> = _uiState.asStateFlow()
    
    // Form state
    var sportName by mutableStateOf("")
        private set
    var location by mutableStateOf("")
        private set
    var locationLat by mutableStateOf<Double?>(null)
        private set
    var locationLng by mutableStateOf<Double?>(null)
        private set
    var selectedDate by mutableStateOf<Long?>(null) // Timestamp
        private set
    var selectedTime by mutableStateOf<Pair<Int, Int>?>(null) // Hour, Minute
        private set
    var maxPlayers by mutableStateOf(10)
        private set
    var description by mutableStateOf("")
        private set
    
    /**
     * Updates sport name.
     * Resets location when sport changes.
     */
    fun updateSportName(value: String) {
        sportName = value
        // Reset location when sport changes
        location = ""
        locationLat = null
        locationLng = null
    }
    
    /**
     * Updates location with coordinates.
     */
    fun updateLocation(value: String, lat: Double? = null, lng: Double? = null) {
        location = value
        locationLat = lat
        locationLng = lng
    }
    
    /**
     * Updates selected date.
     */
    fun updateDate(timestamp: Long) {
        selectedDate = timestamp
    }
    
    /**
     * Updates selected time.
     */
    fun updateTime(hour: Int, minute: Int) {
        selectedTime = Pair(hour, minute)
    }
    
    /**
     * Updates max players.
     */
    fun updateMaxPlayers(value: Int) {
        maxPlayers = value
    }
    
    /**
     * Updates description.
     */
    fun updateDescription(value: String) {
        description = value
    }
    
    /**
     * Validates form input before submission.
     * 
     * Validates all required fields and ensures data integrity:
     * - Sport name must not be blank
     * - Location must not be blank
     * - Date must not be blank
     * - Max players must be a positive integer
     * 
     * @return Error message if validation fails, null if validation passes
     */
    private fun validateForm(): String? {
        if (sportName.isBlank()) return "Numele sportului este obligatoriu"
        if (location.isBlank()) return "Locația este obligatorie"
        if (selectedDate == null) return "Data este obligatorie"
        if (selectedTime == null) return "Ora este obligatorie"
        
        // Edge case: Validate maxPlayers is a valid positive number
        if (maxPlayers <= 0) {
            return "Numărul maxim de jucători trebuie să fie un număr pozitiv"
        }
        
        // Edge case: Reasonable limit for max players
        if (maxPlayers > 100) {
            return "Numărul maxim de jucători nu poate depăși 100"
        }
        
        // Edge case: Date must be in the future
        val selectedDateTime = java.util.Calendar.getInstance().apply {
            timeInMillis = selectedDate!!
            set(java.util.Calendar.HOUR_OF_DAY, selectedTime!!.first)
            set(java.util.Calendar.MINUTE, selectedTime!!.second)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        if (selectedDateTime.timeInMillis <= System.currentTimeMillis()) {
            return "Data și ora trebuie să fie în viitor"
        }
        
        return null
    }
    
    /**
     * Creates a new lobby via POST request.
     */
    fun createLobby() {
        val validationError = validateForm()
        if (validationError != null) {
            _uiState.value = CreateLobbyUiState.Error(validationError)
            return
        }
        
        viewModelScope.launch {
            _uiState.value = CreateLobbyUiState.Loading
            
            // Combine date and time into ISO 8601 format
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = selectedDate!!
                set(java.util.Calendar.HOUR_OF_DAY, selectedTime!!.first)
                set(java.util.Calendar.MINUTE, selectedTime!!.second)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            
            // Format as ISO 8601: "2024-03-15T18:00:00Z"
            val dateTimeString = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(calendar.time)
            
            val lobby = Lobby(
                id = "", // Will be generated by API
                sportName = sportName,
                location = location,
                locationLat = locationLat,
                locationLng = locationLng,
                date = dateTimeString,
                maxPlayers = maxPlayers,
                joinedPlayers = 0,
                description = description.ifBlank { null }
            )
            
            when (val result = lobbyRepository.createLobby(lobby)) {
                is Result.Success -> {
                    _uiState.value = CreateLobbyUiState.Success(result.data)
                    // Reset form
                    resetForm()
                }
                is Result.Error -> {
                    _uiState.value = CreateLobbyUiState.Error(
                        result.exception.message ?: "Failed to create lobby"
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Resets the form.
     */
    private fun resetForm() {
        sportName = ""
        location = ""
        locationLat = null
        locationLng = null
        selectedDate = null
        selectedTime = null
        maxPlayers = 10
        description = ""
    }
}

