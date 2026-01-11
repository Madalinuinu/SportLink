package com.example.sportlink.ui.viewmodel

import androidx.lifecycle.ViewModel

/**
 * Base ViewModel class for common functionality across all ViewModels.
 * 
 * This abstract class can be extended by other ViewModels to share common behavior.
 * Currently serves as a placeholder for future common functionality such as:
 * - Common error handling
 * - Logging utilities
 * - Shared state management patterns
 * 
 * All ViewModels use viewModelScope for coroutines, which is automatically
 * cancelled when the ViewModel is cleared (lifecycle-aware - 5p Stabilitate).
 */
abstract class BaseViewModel : ViewModel() {
    // Common ViewModel functionality can be added here in the future
}

