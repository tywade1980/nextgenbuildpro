package com.nextgenbuildpro.util

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ErrorLogger - Enhanced error logging utility for NextGenBuildPro
 *
 * Provides comprehensive logging for errors, including stack traces, timestamps,
 * and structured logging for analytics.
 */
object ErrorLogger {
    // Constants
    private const val DEFAULT_TAG = "NextGenBuildPro"
    private const val MAX_LOG_LENGTH = 4000

    // Format for timestamps
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    /**
     * Log an error with enhanced details
     *
     * @param tag The log tag to use
     * @param message The error message
     * @param error The exception or error
     * @param additionalInfo Additional context information (optional)
     */
    fun logError(tag: String, message: String, error: Throwable?, additionalInfo: Map<String, Any>? = null) {
        val timestamp = dateFormat.format(Date())
        val stackTrace = error?.let { getStackTraceString(it) } ?: ""

        val stringBuilder = StringBuilder()
        stringBuilder.append("⚠️ ERROR [$timestamp] - $message\n")

        // Add additional context if provided
        additionalInfo?.let {
            stringBuilder.append("📋 Context: ")
            stringBuilder.append(it.entries.joinToString(", ") { entry -> "${entry.key}=${entry.value}" })
            stringBuilder.append("\n")
        }

        // Add stack trace if available
        if (stackTrace.isNotEmpty()) {
            stringBuilder.append("🔍 Stack Trace:\n$stackTrace")
        }

        // Log the complete error message in chunks if necessary
        val errorLog = stringBuilder.toString()
        if (errorLog.length > MAX_LOG_LENGTH) {
            var i = 0
            while (i < errorLog.length) {
                val end = (i + MAX_LOG_LENGTH).coerceAtMost(errorLog.length)
                Log.e(tag, errorLog.substring(i, end))
                i = end
            }
        } else {
            Log.e(tag, errorLog)
        }
    }

    /**
     * Log an error with default tag
     *
     * @param message The error message
     * @param error The exception or error
     * @param additionalInfo Additional context information (optional)
     */
    fun logError(message: String, error: Throwable? = null, additionalInfo: Map<String, Any>? = null) {
        logError(DEFAULT_TAG, message, error, additionalInfo)
    }

    /**
     * Log navigation errors
     *
     * @param destination The navigation destination
     * @param error The exception or error
     * @param additionalInfo Additional context information (optional)
     */
    fun logNavigationError(destination: String, error: Throwable? = null, additionalInfo: Map<String, Any>? = null) {
        val info = additionalInfo?.toMutableMap() ?: mutableMapOf()
        info["destination"] = destination

        logError("Navigation", "Navigation failed to: $destination", error, info)
    }

    /**
     * Log button click errors
     *
     * @param buttonId The identifier for the button
     * @param screenName The name of the screen where the button is located
     * @param error The exception or error
     * @param additionalInfo Additional context information (optional)
     */
    fun logButtonError(buttonId: String, screenName: String, error: Throwable? = null, additionalInfo: Map<String, Any>? = null) {
        val info = additionalInfo?.toMutableMap() ?: mutableMapOf()
        info["buttonId"] = buttonId
        info["screenName"] = screenName

        logError("UserInterface", "Button click error: $buttonId on $screenName", error, info)
    }

    /**
     * Get a formatted stack trace string from an exception
     *
     * @param throwable The exception to get a stack trace from
     * @return A string containing the formatted stack trace
     */
    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }
}
