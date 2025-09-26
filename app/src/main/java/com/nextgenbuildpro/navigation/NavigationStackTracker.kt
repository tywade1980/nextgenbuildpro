package com.nextgenbuildpro.navigation

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.nextgenbuildpro.util.ErrorLogger
import java.util.Stack

/**
 * NavigationStackTracker - Monitors navigation stack and detects potential issues
 *
 * This class provides tracking of navigation events and can detect issues like:
 * - Circular navigation paths
 * - Dead-end screens
 * - Excessive navigation depth
 */
class NavigationStackTracker(private val navController: NavController) :
    NavController.OnDestinationChangedListener,
    LifecycleEventObserver {

    companion object {
        private const val TAG = "NavigationStackTracker"
        private const val MAX_STACK_DEPTH = 10
    }

    // Stack to track navigation history
    private val navigationStack = Stack<NavigationEvent>()

    // Map to track frequency of destinations
    private val destinationFrequency = mutableMapOf<String, Int>()

    // Flag to enable/disable tracking
    private var isEnabled = false

    /**
     * Start tracking navigation events
     *
     * @param lifecycleOwner The lifecycle owner to observe for lifecycle events
     */
    fun startTracking(lifecycleOwner: LifecycleOwner) {
        isEnabled = true
        lifecycleOwner.lifecycle.addObserver(this)
        navController.addOnDestinationChangedListener(this)
        Log.d(TAG, "Navigation stack tracking enabled")
    }

    /**
     * Stop tracking navigation events
     *
     * @param lifecycleOwner The lifecycle owner to remove observer from
     */
    fun stopTracking(lifecycleOwner: LifecycleOwner) {
        isEnabled = false
        lifecycleOwner.lifecycle.removeObserver(this)
        navController.removeOnDestinationChangedListener(this)
        Log.d(TAG, "Navigation stack tracking disabled")
    }

    /**
     * Reset the tracking data
     */
    fun reset() {
        navigationStack.clear()
        destinationFrequency.clear()
        Log.d(TAG, "Navigation stack tracking data reset")
    }

    /**
     * Called when the navigation destination changes
     */
    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: android.os.Bundle?
    ) {
        if (!isEnabled) return

        val destinationId = destination.route ?: destination.id.toString()
        val timestamp = System.currentTimeMillis()

        // Create navigation event
        val event = NavigationEvent(
            destinationId = destinationId,
            timestamp = timestamp,
            arguments = arguments
        )

        // Add to stack
        navigationStack.push(event)

        // Update frequency
        val count = destinationFrequency.getOrDefault(destinationId, 0) + 1
        destinationFrequency[destinationId] = count

        // Check for issues
        checkForNavigationIssues(event)

        Log.d(TAG, "Navigation: $destinationId, Stack depth: ${navigationStack.size}")
    }

    /**
     * Check for potential navigation issues
     *
     * @param event The current navigation event
     */
    private fun checkForNavigationIssues(event: NavigationEvent) {
        // Check for excessive stack depth
        if (navigationStack.size > MAX_STACK_DEPTH) {
            val warning = "Navigation stack depth exceeds $MAX_STACK_DEPTH. " +
                          "Consider simplifying navigation flow."
            Log.w(TAG, warning)
            ErrorLogger.logError(TAG, warning, null, mapOf(
                "stackDepth" to navigationStack.size,
                "currentDestination" to event.destinationId
            ))
        }

        // Check for circular navigation
        val circularPath = detectCircularNavigation()
        if (circularPath.isNotEmpty()) {
            val warning = "Detected circular navigation path: $circularPath"
            Log.w(TAG, warning)
            ErrorLogger.logError(TAG, warning, null, mapOf(
                "circularPath" to circularPath.joinToString(" -> ")
            ))
        }
    }

    /**
     * Detect circular navigation patterns
     *
     * @return A list of destinations that form a circular path, or empty if none found
     */
    private fun detectCircularNavigation(): List<String> {
        if (navigationStack.size < 3) return emptyList()

        // Get the last destination
        val lastDestination = navigationStack.peek().destinationId

        // Check if this destination has appeared at least 3 times in the stack
        if (destinationFrequency.getOrDefault(lastDestination, 0) < 3) return emptyList()

        // Find the path between the previous two occurrences
        val path = mutableListOf<String>()
        var foundFirst = false

        // Convert stack to list for easier iteration (most recent first)
        val stackAsList = navigationStack.toList().asReversed()

        for (i in 0 until stackAsList.size - 1) {
            val event = stackAsList[i]

            if (event.destinationId == lastDestination) {
                if (!foundFirst) {
                    foundFirst = true
                } else {
                    // Found second occurrence, return the path
                    return path
                }
            } else if (foundFirst) {
                path.add(event.destinationId)
            }
        }

        return emptyList()
    }

    /**
     * Lifecycle event handler
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                stopTracking(source)
            }
            else -> { /* Ignore other events */ }
        }
    }

    /**
     * Get a report of navigation history
     *
     * @return A string containing the report
     */
    fun getNavigationReport(): String {
        if (navigationStack.isEmpty()) {
            return "No navigation events tracked."
        }

        val stringBuilder = StringBuilder()
        stringBuilder.appendLine("Navigation Stack Report")
        stringBuilder.appendLine("======================")
        stringBuilder.appendLine("Stack depth: ${navigationStack.size}")
        stringBuilder.appendLine()

        stringBuilder.appendLine("Navigation history (most recent first):")
        navigationStack.toList().asReversed().take(20).forEach { event ->
            stringBuilder.appendLine("${event.destinationId} (${formatTimestamp(event.timestamp)})")
        }

        stringBuilder.appendLine()
        stringBuilder.appendLine("Destination frequencies:")
        destinationFrequency.entries
            .sortedByDescending { it.value }
            .forEach { (destination, count) ->
                stringBuilder.appendLine("$destination: $count visits")
            }

        return stringBuilder.toString()
    }

    /**
     * Format a timestamp as a human-readable string
     *
     * @param timestamp The timestamp in milliseconds
     * @return A formatted string
     */
    private fun formatTimestamp(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        return java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US).format(date)
    }

    /**
     * Data class representing a navigation event
     */
    data class NavigationEvent(
        val destinationId: String,
        val timestamp: Long,
        val arguments: android.os.Bundle?
    )
}
