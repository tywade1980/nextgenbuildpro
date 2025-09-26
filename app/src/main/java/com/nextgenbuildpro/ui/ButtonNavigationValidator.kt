package com.nextgenbuildpro.ui

import android.util.Log
import androidx.navigation.NavController
import com.nextgenbuildpro.navigation.NavigationHelper
import com.nextgenbuildpro.util.ErrorLogger

/**
 * ButtonNavigationValidator - Utility to validate button navigation actions
 *
 * This class helps prevent improper navigation from buttons by validating
 * navigation targets and tracking button click events.
 */
object ButtonNavigationValidator {
    private const val TAG = "ButtonNavValidator"

    // Map to track button click counts
    private val buttonClickCounts = mutableMapOf<String, Int>()

    // Map to track navigation success rates for buttons
    private val navigationSuccessRates = mutableMapOf<String, Pair<Int, Int>>() // (success, attempts)

    /**
     * Validate and perform navigation from a button
     *
     * @param navController The NavController to use for navigation
     * @param destination The destination route
     * @param buttonId An identifier for the button
     * @param screenName The screen where the button is located
     * @return True if navigation was successful, false otherwise
     */
    fun validateAndNavigate(
        navController: NavController,
        destination: String,
        buttonId: String,
        screenName: String
    ): Boolean {
        try {
            // Track button click
            trackButtonClick(buttonId, screenName)

            Log.d(TAG, "Button '$buttonId' on '$screenName' attempting navigation to: $destination")

            // Create NavigationHelper to validate destination
            val navigationHelper = NavigationHelper.create(navController.context)

            // Check if destination is implemented before attempting navigation
            if (!navigationHelper.isDestinationImplemented(destination)) {
                // Log error for unimplemented destination
                val errorMessage = "Button '$buttonId' on screen '$screenName' attempted navigation to unimplemented destination: $destination"
                Log.e(TAG, errorMessage)

                ErrorLogger.logButtonError(
                    buttonId = buttonId,
                    screenName = screenName,
                    additionalInfo = mapOf(
                        "destination" to destination,
                        "error" to "Destination not implemented"
                    )
                )

                // Update success rate
                updateNavigationSuccessRate(buttonId, false)

                return false
            }

            // Perform navigation
            val success = navigationHelper.navigateSafely(navController, destination)

            // Update success rate
            updateNavigationSuccessRate(buttonId, success)

            return success
        } catch (e: Exception) {
            // Log any exceptions during navigation
            Log.e(TAG, "Navigation error from button: $buttonId on $screenName", e)

            ErrorLogger.logButtonError(
                buttonId = buttonId,
                screenName = screenName,
                error = e,
                additionalInfo = mapOf(
                    "destination" to destination
                )
            )

            // Update success rate
            updateNavigationSuccessRate(buttonId, false)

            return false
        }
    }

    /**
     * Track a button click
     *
     * @param buttonId An identifier for the button
     * @param screenName The screen where the button is located
     */
    fun trackButtonClick(buttonId: String, screenName: String) {
        val key = "$screenName:$buttonId"
        val count = buttonClickCounts.getOrDefault(key, 0) + 1
        buttonClickCounts[key] = count
    }

    /**
     * Update navigation success rate for a button
     *
     * @param buttonId An identifier for the button
     * @param success Whether navigation was successful
     */
    private fun updateNavigationSuccessRate(buttonId: String, success: Boolean) {
        val (successes, attempts) = navigationSuccessRates.getOrDefault(buttonId, Pair(0, 0))
        navigationSuccessRates[buttonId] = Pair(
            successes + if (success) 1 else 0,
            attempts + 1
        )
    }

    /**
     * Get button click statistics
     *
     * @return A report of button clicks and navigation success rates
     */
    fun getButtonStatistics(): String {
        if (buttonClickCounts.isEmpty()) {
            return "No button clicks tracked."
        }

        val stringBuilder = StringBuilder()
        stringBuilder.appendLine("Button Navigation Statistics")
        stringBuilder.appendLine("============================")

        stringBuilder.appendLine("Button click counts:")
        buttonClickCounts.entries
            .sortedByDescending { it.value }
            .forEach { (button, count) ->
                stringBuilder.appendLine("$button: $count clicks")
            }

        stringBuilder.appendLine()
        stringBuilder.appendLine("Navigation success rates:")
        navigationSuccessRates.entries.forEach { (button, stats) ->
            val (successes, attempts) = stats
            val rate = if (attempts > 0) (successes * 100.0 / attempts) else 0.0
            stringBuilder.appendLine("$button: ${rate.toInt()}% (${successes}/${attempts})")
        }

        return stringBuilder.toString()
    }

    /**
     * Reset tracking data
     */
    fun reset() {
        buttonClickCounts.clear()
        navigationSuccessRates.clear()
        Log.d(TAG, "Button navigation tracking data reset")
    }

    /**
     * Log button statistics
     */
    fun logStatistics() {
        val report = getButtonStatistics()

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
