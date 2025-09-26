package com.nextgenbuildpro.navigation

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController

private const val TAG = "NavigationHelper"

/**
 * Helper class for navigation with safety checks
 * 
 * This class provides methods to safely navigate to destinations,
 * with error handling for unimplemented screens.
 */
class NavigationHelper private constructor(private val context: Context) {

    companion object {
        @Volatile private var instance: NavigationHelper? = null

        /**
         * Create or return an existing instance of NavigationHelper
         *
         * @param context The application context
         * @return The NavigationHelper instance
         */
        fun create(context: Context): NavigationHelper {
            return instance ?: synchronized(this) {
                instance ?: NavigationHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    // Set of implemented destinations
    private val implementedDestinations = setOf(
        NavDestinations.HOME,
        NavDestinations.LEADS,
        NavDestinations.LEAD_DETAIL,
        NavDestinations.LEAD_EDITOR,
        NavDestinations.NOTE_EDITOR,
        NavDestinations.ESTIMATES,
        NavDestinations.ESTIMATE_DETAIL,
        NavDestinations.CALENDAR,
        NavDestinations.CALENDAR_EVENT_EDITOR,
        NavDestinations.CALENDAR_TIMELINE,
        NavDestinations.MESSAGES,
        NavDestinations.MORE,
        NavDestinations.NOTIFICATIONS,
        NavDestinations.AR_VISUALIZATION,
        NavDestinations.VOICE_TO_TEXT,
        NavDestinations.OFFLINE_MODE,
        NavDestinations.TIME_CLOCK,
        NavDestinations.CLIENT_PORTAL,
        NavDestinations.PROGRESS_UPDATES,
        NavDestinations.DIGITAL_SIGNATURE,
        NavDestinations.WORKFLOW_AUTOMATION,
        NavDestinations.PROJECTS,
        NavDestinations.ESTIMATE_EDITOR,
        NavDestinations.ESTIMATE_ITEM_EDITOR,
        "tasks" // Direct route for tasks screen
    )

    // Set of partially implemented destinations (have a placeholder)
    private val placeholderDestinations = setOf<String>(
        NavDestinations.ROOM_SCAN,
        NavDestinations.BMS,
        NavDestinations.BUILDING_DETAIL
    )

    /**
     * Safely navigate to a destination
     * 
     * @param navController The NavController to use for navigation
     * @param destination The destination route
     * @param showToast Whether to show a toast message for unimplemented screens
     * @return True if navigation was successful, false otherwise
     */
    fun navigateSafely(
        navController: NavController, 
        destination: String,
        showToast: Boolean = true
    ): Boolean {
        try {
            Log.d(TAG, "Attempting navigation to: $destination")

            // Check if the destination is implemented
            if (isDestinationImplemented(destination)) {
                // Get the current destination to avoid redundant navigation
                val currentDestination = navController.currentDestination?.route
                if (currentDestination == destination) {
                    Log.d(TAG, "Already at destination: $destination, skipping navigation")
                    return true
                }

                navController.navigate(destination)
                Log.d(TAG, "Successfully navigated to: $destination")
                return true
            }

            // Check if the destination has a placeholder
            if (isDestinationPlaceholder(destination)) {
                navController.navigate(destination)
                if (showToast) {
                    Toast.makeText(
                        context,
                        "This feature is coming soon!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.d(TAG, "Navigated to placeholder destination: $destination")
                return true
            }

            // Destination is not implemented
            if (showToast) {
                Toast.makeText(
                    context,
                    "Navigation failed: The screen '$destination' is not yet implemented",
                    Toast.LENGTH_LONG
                ).show()
            }
            Log.w(TAG, "Navigation failed: The destination '$destination' is not implemented")

            // Return false to indicate navigation was not successful
            return false
        } catch (e: Exception) {
            // Handle navigation exceptions
            if (showToast) {
                Toast.makeText(
                    context,
                    "Navigation error: ${e.message ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }

            // Log the error for debugging with stack trace
            Log.e(TAG, "Navigation error to '$destination'", e)
            return false
        }
    }

    /**
     * Safely navigate to a destination with arguments
     * 
     * @param navController The NavController to use for navigation
     * @param destination The base destination route
     * @param args The arguments to append to the route
     * @param showToast Whether to show a toast message for unimplemented screens
     * @return True if navigation was successful, false otherwise
     */
    fun navigateSafelyWithArgs(
        navController: NavController,
        destination: String,
        args: String,
        showToast: Boolean = true
    ): Boolean {
        val fullDestination = "$destination/$args"
        Log.d(TAG, "Attempting navigation to: $fullDestination")
        return navigateSafely(navController, fullDestination, showToast)
    }

    /**
     * Check if a destination is implemented
     * 
     * @param destination The destination route
     * @return True if the destination is implemented, false otherwise
     */
    fun isDestinationImplemented(destination: String): Boolean {
        // Check if the destination is in the implemented set
        // or if it starts with one of the implemented destinations followed by a slash
        val isImplemented = implementedDestinations.contains(destination) ||
                implementedDestinations.any {
                    destination.startsWith("$it/") 
                }

        if (!isImplemented) {
            Log.d(TAG, "Destination is not implemented: $destination")
        }

        return isImplemented
    }

    /**
     * Check if a destination has a placeholder
     * 
     * @param destination The destination route
     * @return True if the destination has a placeholder, false otherwise
     */
    fun isDestinationPlaceholder(destination: String): Boolean {
        // Check if the destination is in the placeholder set
        // or if it starts with one of the placeholder destinations followed by a slash
        return placeholderDestinations.contains(destination) ||
                placeholderDestinations.any { 
                    destination.startsWith("$it/") 
                }
    }
}
