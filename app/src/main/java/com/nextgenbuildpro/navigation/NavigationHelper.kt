package com.nextgenbuildpro.navigation

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions

/**
 * Helper class for navigation with safety checks
 * 
 * This class provides methods to safely navigate to destinations,
 * with error handling for unimplemented screens.
 */
class NavigationHelper(private val context: Context) {

    // Set of implemented destinations
    private val implementedDestinations = setOf(
        NavDestinations.HOME,
        NavDestinations.LEADS,
        NavDestinations.LEAD_DETAIL,
        NavDestinations.LEAD_EDITOR,  // Add this line
        NavDestinations.NOTE_EDITOR,  // Add this line
        NavDestinations.ESTIMATES,
        NavDestinations.ESTIMATE_DETAIL,
        NavDestinations.CALENDAR,
        NavDestinations.CALENDAR_EVENT_EDITOR, // Moved from placeholders
        NavDestinations.CALENDAR_TIMELINE, // Moved from placeholders
        NavDestinations.MESSAGES, // Moved from placeholders
        NavDestinations.MORE, // Moved from placeholders
        NavDestinations.NOTIFICATIONS, // Moved from placeholders
        NavDestinations.AR_VISUALIZATION,
        NavDestinations.VOICE_TO_TEXT,
        NavDestinations.OFFLINE_MODE,
        NavDestinations.TIME_CLOCK,
        NavDestinations.CLIENT_PORTAL,
        NavDestinations.PROGRESS_UPDATES,
        NavDestinations.DIGITAL_SIGNATURE,
        "tasks" // Direct route for tasks screen
    )

    // Set of partially implemented destinations (have a placeholder)
    private val placeholderDestinations = emptySet<String>()
    // All destinations have been moved to implementedDestinations
    // NavDestinations.PROJECTS // Moved to implemented destinations
    // NavDestinations.MESSAGES, // Moved to implemented destinations
    // NavDestinations.MORE, // Moved to implemented destinations
    // NavDestinations.CALENDAR_EVENT_EDITOR, // Moved to implemented destinations
    // NavDestinations.CALENDAR_TIMELINE, // Moved to implemented destinations
    // NavDestinations.NOTIFICATIONS // Moved to implemented destinations

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
            // Check if the destination is implemented
            if (isDestinationImplemented(destination)) {
                navController.navigate(destination)
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

            // Log the error for debugging
            android.util.Log.e("NavigationHelper", "Navigation failed to '$destination'", e)

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
        return navigateSafely(navController, "$destination/$args", showToast)
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
        return implementedDestinations.contains(destination) ||
                implementedDestinations.any { 
                    destination.startsWith("$it/") 
                }
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

    /**
     * Navigate to a destination and register for a result
     * 
     * @param navController The NavController to use for navigation
     * @param destination The destination route
     * @param resultKey The key to use for the result
     * @param onResult Callback to be invoked when returning from the destination
     * @return True if navigation was successful, false otherwise
     */
    fun navigateForResult(
        navController: NavController,
        destination: String,
        resultKey: String,
        onResult: (Any?) -> Unit
    ): Boolean {
        // First check if the destination is implemented
        if (!isDestinationImplemented(destination) && !isDestinationPlaceholder(destination)) {
            Toast.makeText(
                context,
                "Navigation failed: The screen '$destination' is not yet implemented",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        try {
            // Set up the previous backstack entry to receive the result
            val currentEntry = navController.currentBackStackEntry
            if (currentEntry != null) {
                // Set up the result listener on the current entry
                // Observe the result with a one-time observer
                currentEntry.savedStateHandle.getLiveData<Any>(resultKey).observe(currentEntry) { result ->
                    // Call the result callback
                    onResult(result)

                    // Clear the result to prevent it from being delivered again
                    currentEntry.savedStateHandle.remove<Any>(resultKey)
                }
            }

            // Navigate to the destination
            navController.navigate(destination)
            return true
        } catch (e: Exception) {
            // Handle navigation exceptions
            Toast.makeText(
                context,
                "Navigation error: ${e.message ?: "Unknown error"}",
                Toast.LENGTH_LONG
            ).show()

            // Log the error for debugging
            android.util.Log.e("NavigationHelper", "Navigation for result failed to '$destination'", e)

            return false
        }
    }

    /**
     * Set a navigation result to be returned to the previous screen
     * 
     * @param navController The NavController to use for navigation
     * @param resultKey The key to use for the result
     * @param result The result to return
     */
    fun setNavigationResult(
        navController: NavController,
        resultKey: String,
        result: Any
    ) {
        // Get the previous backstack entry
        val previousEntry = navController.previousBackStackEntry
        if (previousEntry != null) {
            // Set the result on the previous entry's saved state handle
            previousEntry.savedStateHandle[resultKey] = result
        }
    }

    /**
     * Navigate to a destination with arguments and register for a result
     * 
     * @param navController The NavController to use for navigation
     * @param destination The base destination route
     * @param args The arguments to append to the route
     * @param resultKey The key to use for the result
     * @param onResult Callback to be invoked when returning from the destination
     * @return True if navigation was successful, false otherwise
     */
    fun navigateWithArgsForResult(
        navController: NavController,
        destination: String,
        args: String,
        resultKey: String,
        onResult: (Any?) -> Unit
    ): Boolean {
        return navigateForResult(navController, "$destination/$args", resultKey, onResult)
    }

    companion object {
        /**
         * Create a NavigationHelper instance
         * 
         * @param context The context to use for toast messages
         * @return A NavigationHelper instance
         */
        fun create(context: Context): NavigationHelper {
            return NavigationHelper(context)
        }
    }
}
