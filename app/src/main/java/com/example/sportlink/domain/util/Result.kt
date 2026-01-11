package com.example.sportlink.domain.util

/**
 * Sealed class for handling operation results.
 * Used throughout the app for error handling and state management.
 * 
 * @param T The type of data returned on success
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with data.
     */
    data class Success<T>(val data: T) : Result<T>()
    
    /**
     * Represents a failed operation with an exception.
     */
    data class Error(val exception: Throwable) : Result<Nothing>()
    
    /**
     * Represents an operation in progress.
     */
    object Loading : Result<Nothing>()
}

/**
 * Extension function to handle success case.
 */
inline fun <T> Result<T>.onSuccess(action: (value: T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Extension function to handle error case.
 */
inline fun <T> Result<T>.onError(action: (exception: Throwable) -> Unit): Result<T> {
    if (this is Result.Error) action(exception)
    return this
}

