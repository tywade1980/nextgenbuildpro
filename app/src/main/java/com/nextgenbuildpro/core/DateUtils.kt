package com.nextgenbuildpro.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class for date and time operations
 */
object DateUtils {
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    /**
     * Get current timestamp as formatted string
     */
    fun getCurrentTimestamp(): String {
        return dateTimeFormat.format(Date())
    }

    /**
     * Format a timestamp (in milliseconds) to a readable date/time string
     */
    fun formatTimestamp(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    /**
     * Format a timestamp (in milliseconds) to a readable date string
     */
    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Format a Date object to a readable date string
     */
    fun formatDate(date: Date, pattern: String = "MM/dd/yyyy"): String {
        return SimpleDateFormat(pattern, Locale.US).format(date)
    }

    /**
     * Format a timestamp (in milliseconds) to a readable time string
     */
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    /**
     * Parse a date/time string to a timestamp (in milliseconds)
     */
    fun parseTimestamp(dateTimeString: String): Long {
        return try {
            dateTimeFormat.parse(dateTimeString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
