package com.example.sportlink.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Map
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*
import com.example.sportlink.util.LocationData
import com.example.sportlink.util.SportLocation

/**
 * Create Lobby Screen composable.
 * Modern design with sport selector, location search, date/time pickers, and max players slider.
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
    val context = LocalContext.current
    
    // Format selected date and time for display
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale("ro", "RO")) }
    
    val selectedDateText = remember(viewModel.selectedDate) {
        if (viewModel.selectedDate != null) {
            dateFormatter.format(Date(viewModel.selectedDate!!))
        } else {
            "Selectează data"
        }
    }
    
    val selectedTimeText = remember(viewModel.selectedTime) {
        if (viewModel.selectedTime != null) {
            val (hour, minute) = viewModel.selectedTime!!
            String.format("%02d:%02d", hour, minute)
        } else {
            "Selectează ora"
        }
    }
    
    // Date picker dialog
    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(year, month, dayOfMonth)
                viewModel.updateDate(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
    
    // Time picker dialog
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                viewModel.updateTime(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
    }
    
    // Navigate back on success
    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateLobbyUiState.Success -> {
                snackbarHostState.showSnackbar("Lobby creat cu succes!")
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
                title = { Text("Creează Lobby", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Înapoi")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sport Selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Sport",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val sports = listOf("Fotbal", "Tenis", "Baschet")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sports.forEach { sport ->
                        FilterChip(
                            selected = viewModel.sportName == sport,
                            onClick = { viewModel.updateSportName(sport) },
                            label = { 
                                Text(
                                    text = sport,
                                    style = MaterialTheme.typography.bodyLarge
                                ) 
                            },
                            enabled = uiState !is CreateLobbyUiState.Loading,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Location Selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Locație",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Get predefined locations for selected sport
                val availableLocations = remember(viewModel.sportName) {
                    if (viewModel.sportName.isNotBlank()) {
                        LocationData.getLocationsForSport(viewModel.sportName)
                    } else {
                        emptyList<SportLocation>()
                    }
                }
                
                var expanded by remember { mutableStateOf(false) }
                
                    if (availableLocations.isEmpty()) {
                    // Show message if no sport selected
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { 
                            Text(
                                text = "Selectează mai întâi un sport",
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                        },
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    // Dropdown for location selection
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = viewModel.location,
                            onValueChange = {},
                            readOnly = true,
                            label = { 
                                Text(
                                    text = "Selectează locația",
                                    style = MaterialTheme.typography.bodyMedium
                                ) 
                            },
                            placeholder = { 
                                Text(
                                    text = "Alege o locație...",
                                    style = MaterialTheme.typography.bodyMedium
                                ) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            enabled = uiState !is CreateLobbyUiState.Loading && viewModel.sportName.isNotBlank(),
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null)
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            availableLocations.forEach { location ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = location.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = location.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.updateLocation(
                                            location.name,
                                            location.latitude,
                                            location.longitude
                                        )
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // "Vezi pe hartă" button (only if location is selected)
                    if (viewModel.location.isNotBlank() && viewModel.locationLat != null && viewModel.locationLng != null) {
                        OutlinedButton(
                            onClick = {
                                // Open Google Maps with selected location
                                val gmmIntentUri = Uri.parse("geo:${viewModel.locationLat},${viewModel.locationLng}?q=${Uri.encode(viewModel.location)}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                
                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(mapIntent)
                                } else {
                                    // Fallback to web browser if Maps app is not installed
                                    val webIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${viewModel.locationLat},${viewModel.locationLng}")
                                    )
                                    context.startActivity(webIntent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState !is CreateLobbyUiState.Loading
                        ) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Vezi pe hartă",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            
            // Date and Time Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Picker
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is CreateLobbyUiState.Loading
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = selectedDateText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                // Time Picker
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ora",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedButton(
                        onClick = { timePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is CreateLobbyUiState.Loading
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = selectedTimeText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Max Players Slider
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jucători maximi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${viewModel.maxPlayers}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = viewModel.maxPlayers.toFloat(),
                    onValueChange = { viewModel.updateMaxPlayers(it.toInt()) },
                    valueRange = 2f..50f,
                    steps = 47, // 2, 3, 4, ..., 50
                    enabled = uiState !is CreateLobbyUiState.Loading,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("2", style = MaterialTheme.typography.bodySmall)
                    Text("50", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            // Description
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Descriere (opțional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { 
                        Text(
                            text = "Adaugă detalii despre meci",
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    placeholder = { 
                        Text(
                            text = "ex: Meci amical, nivel intermediar...",
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is CreateLobbyUiState.Loading,
                    minLines = 3,
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Create Button
            Button(
                onClick = { viewModel.createLobby() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState !is CreateLobbyUiState.Loading && 
                         viewModel.sportName.isNotBlank() &&
                         viewModel.location.isNotBlank() &&
                         viewModel.selectedDate != null &&
                         viewModel.selectedTime != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (uiState is CreateLobbyUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 12.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }
                Text(
                    text = "Creează Lobby",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
