package com.nextgenbuildpro.util

import android.util.Log

/**
 * Application Logger Utility
 * 
 * This utility class provides centralized logging control for the application.
 * It allows selectively disabling debug logs while preserving error and warning logs.
 */
object AppLogger {
    // Set to false to disable debug logging
    private const val DEBUG_ENABLED = false
    
    /**
     * Log a debug message (disabled in production)
     */
    fun d(tag: String, message: String) {
        if (DEBUG_ENABLED) {
            Log.d(tag, message)
        }
    }
    
    /**
     * Log an error message (always enabled)
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
    
    /**
     * Log a warning message (always enabled)
     */
    fun w(tag: String, message: String) {
        Log.w(tag, message)
    }
    
    /**
     * Log an info message (always enabled)
     */
    fun i(tag: String, message: String) {
        Log.i(tag, message)
    }
}