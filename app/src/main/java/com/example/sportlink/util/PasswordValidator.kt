package com.example.sportlink.util

/**
 * Utility object for password validation.
 * 
 * Validates passwords according to security requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one digit
 */
object PasswordValidator {
    /**
     * Validates a password according to security requirements.
     * 
     * @param password The password to validate
     * @return Pair<Boolean, String?> - (isValid, errorMessage)
     *         If isValid is true, errorMessage is null
     *         If isValid is false, errorMessage contains the validation error
     */
    fun validatePassword(password: String): Pair<Boolean, String?> {
        if (password.isBlank()) {
            return Pair(false, "Parola nu poate fi goală")
        }
        
        if (password.length < 8) {
            return Pair(false, "Parola trebuie să aibă cel puțin 8 caractere")
        }
        
        // Verifică dacă parola conține cel puțin o literă mare
        if (!password.any { it.isUpperCase() }) {
            return Pair(false, "Parola trebuie să conțină cel puțin o literă mare")
        }
        
        // Verifică dacă parola conține cel puțin o cifră
        if (!password.any { it.isDigit() }) {
            return Pair(false, "Parola trebuie să conțină cel puțin o cifră")
        }
        
        return Pair(true, null)
    }
}

