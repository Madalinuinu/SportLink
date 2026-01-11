package com.example.sportlink.util

import com.example.sportlink.domain.util.Result
import kotlinx.coroutines.delay
import kotlin.math.pow

/**
 * Helper object for implementing exponential backoff retry mechanism.
 * 
 * This utility provides a retry function with exponential backoff,
 * which increases the delay between retries exponentially.
 * Used for improving stability when handling network errors (5p Stabilitate).
 */
object RetryHelper {
    
    /**
     * Executes a suspend function with exponential backoff retry logic.
     * 
     * Implements exponential backoff: delays increase as 2^attempt * initialDelayMs,
     * capped at maxDelayMs. This helps handle transient network errors gracefully.
     * 
     * @param maxRetries Maximum number of retry attempts (default: 3)
     * @param initialDelayMs Initial delay before first retry in milliseconds (default: 1000ms)
     * @param maxDelayMs Maximum delay between retries in milliseconds (default: 10000ms)
     * @param multiplier Exponential multiplier for delay calculation (default: 2.0)
     * @param block The suspend function to execute that returns domain Result<T>
     * @return Result<T> - Success if any attempt succeeds, Error if all attempts fail
     * 
     * Example usage:
     * ```
     * val result = RetryHelper.retryWithExponentialBackoff {
     *     repository.getAllLobbies()
     * }
     * ```
     */
    suspend fun <T> retryWithExponentialBackoff(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 10000,
        multiplier: Double = 2.0,
        block: suspend () -> Result<T>
    ): Result<T> {
        var lastException: Throwable? = null
        
        repeat(maxRetries) { attempt ->
            val result = block()
            
            when (result) {
                is Result.Success -> return result
                is Result.Error -> {
                    if (attempt < maxRetries - 1) {
                        // Calculate exponential backoff delay: 2^attempt * initialDelayMs
                        val delayMs = (initialDelayMs * multiplier.pow(attempt)).toLong()
                            .coerceAtMost(maxDelayMs)
                        
                        delay(delayMs)
                        lastException = result.exception
                    } else {
                        lastException = result.exception
                    }
                }
                is Result.Loading -> {
                    // If still loading, wait a bit and retry
                    delay(500)
                }
            }
        }
        
        return Result.Error(
            lastException ?: Exception("Failed after $maxRetries retry attempts")
        )
    }
}

