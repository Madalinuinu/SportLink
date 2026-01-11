package com.example.sportlink.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for time formatting and relative time display.
 */

/**
 * Formats a timestamp to a relative time string (e.g., "acum 5 minute", "acum 2 ore").
 * 
 * @param timestamp ISO 8601 timestamp string (e.g., "2024-03-15T18:00:00Z")
 * @return Relative time string in Romanian
 */
fun getRelativeTimeString(timestamp: String?): String {
    if (timestamp == null || timestamp.isBlank()) {
        return "Data necunoscută"
    }
    
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = sdf.parse(timestamp)
        if (date == null) {
            return timestamp // Fallback to original string
        }
        
        val now = Date()
        val diff = now.time - date.time
        
        when {
            diff < 0 -> "În viitor"
            diff < 60 * 1000 -> "acum" // Less than 1 minute
            diff < 60 * 60 * 1000 -> {
                val minutes = (diff / (60 * 1000)).toInt()
                if (minutes == 1) "acum 1 minut" else "acum $minutes minute"
            }
            diff < 24 * 60 * 60 * 1000 -> {
                val hours = (diff / (60 * 60 * 1000)).toInt()
                if (hours == 1) "acum 1 oră" else "acum $hours ore"
            }
            diff < 7 * 24 * 60 * 60 * 1000 -> {
                val days = (diff / (24 * 60 * 60 * 1000)).toInt()
                if (days == 1) "acum 1 zi" else "acum $days zile"
            }
            else -> {
                // Format as date for older items
                val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale("ro", "RO"))
                dateFormatter.format(date)
            }
        }
    } catch (e: Exception) {
        timestamp // Fallback to original string on parse error
    }
}

/**
 * Formats a date/time string for display.
 * 
 * @param dateTime ISO 8601 timestamp string
 * @return Formatted date/time string in Romanian
 */
fun formatDateTime(dateTime: String?): String {
    if (dateTime == null || dateTime.isBlank()) {
        return "Data necunoscută"
    }
    
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = sdf.parse(dateTime)
        if (date == null) {
            return dateTime
        }
        
        val dateFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("ro", "RO"))
        dateFormatter.format(date)
    } catch (e: Exception) {
        dateTime
    }
}

/**
 * Checks if a lobby date is in the future (lobby is active).
 * 
 * @param dateTime ISO 8601 timestamp string
 * @return true if lobby date is in the future, false otherwise
 */
fun isLobbyDateInFuture(dateTime: String?): Boolean {
    if (dateTime == null || dateTime.isBlank()) {
        return false
    }
    
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val lobbyDate = sdf.parse(dateTime)
        if (lobbyDate == null) {
            false
        } else {
            val now = Date()
            lobbyDate.after(now) // Returns true if lobby date is after current time
        }
    } catch (e: Exception) {
        false
    }
}

