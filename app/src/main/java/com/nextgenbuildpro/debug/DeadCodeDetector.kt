package com.nextgenbuildpro.debug

import android.util.Log

/**
 * DeadCodeDetector - Utility to help identify unused code in the application
 *
 * This utility provides methods to track code execution and identify
 * potentially unused methods and classes.
 */
object DeadCodeDetector {
    private const val TAG = "DeadCodeDetector"

    // Map to track execution counts of methods
    private val executionCounts = mutableMapOf<String, Int>()

    // Set to track visited classes
    private val visitedClasses = mutableSetOf<String>()

    // Flag to enable/disable tracking
    private var isEnabled = false

    /**
     * Enable or disable the dead code detection
     *
     * @param enabled Whether to enable tracking
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        Log.d(TAG, "Dead code detection ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Track method execution
     *
     * @param className The name of the class containing the method
     * @param methodName The name of the method being executed
     */
    fun trackExecution(className: String, methodName: String) {
        if (!isEnabled) return

        val key = "$className#$methodName"
        val count = executionCounts.getOrDefault(key, 0) + 1
        executionCounts[key] = count

        // Add class to visited classes
        visitedClasses.add(className)

        if (count == 1) {
            Log.d(TAG, "First execution: $key")
        }
    }

    /**
     * Get the execution count for a method
     *
     * @param className The name of the class containing the method
     * @param methodName The name of the method
     * @return The number of times the method has been executed
     */
    fun getExecutionCount(className: String, methodName: String): Int {
        val key = "$className#$methodName"
        return executionCounts.getOrDefault(key, 0)
    }

    /**
     * Check if a class has been visited
     *
     * @param className The name of the class to check
     * @return Whether the class has been visited
     */
    fun isClassVisited(className: String): Boolean {
        return visitedClasses.contains(className)
    }

    /**
     * Reset all tracking data
     */
    fun reset() {
        executionCounts.clear()
        visitedClasses.clear()
        Log.d(TAG, "Dead code detection data reset")
    }

    /**
     * Get a report of tracked executions
     *
     * @return A string containing the report
     */
    fun getReport(): String {
        if (executionCounts.isEmpty()) {
            return "No methods tracked."
        }

        val stringBuilder = StringBuilder()
        stringBuilder.appendLine("Dead Code Detection Report")
        stringBuilder.appendLine("=========================")
        stringBuilder.appendLine("Tracked methods: ${executionCounts.size}")
        stringBuilder.appendLine("Visited classes: ${visitedClasses.size}")
        stringBuilder.appendLine()

        stringBuilder.appendLine("Method execution counts:")
        executionCounts.entries
            .sortedByDescending { it.value }
            .forEach { (method, count) ->
                stringBuilder.appendLine("$method: $count executions")
            }

        return stringBuilder.toString()
    }

    /**
     * Log the current execution report
     */
    fun logReport() {
        val report = getReport()

        // Split the report into chunks if necessary
        val maxLength = 4000
        var i = 0
        while (i < report.length) {
            val end = (i + maxLength).coerceAtMost(report.length)
            Log.d(TAG, report.substring(i, end))
            i = end
        }
    }
}
