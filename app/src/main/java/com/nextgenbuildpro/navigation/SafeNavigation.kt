package com.nextgenbuildpro.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

private const val TAG = "SafeNavigation"

/**
 * Composable function to remember a NavigationHelper instance
 */
@Composable
fun rememberNavigationHelper(): NavigationHelper {
    val context = LocalContext.current
    return remember { NavigationHelper.create(context) }
}

/**
 * Extension function for NavController to safely navigate to a destination
 * 
 * @param destination The destination route
 * @param showToast Whether to show a toast message for unimplemented screens
 * @return True if navigation was successful, false otherwise
 */
fun NavController.navigateSafely(destination: String, showToast: Boolean = true): Boolean {
    try {
        val context = this.context
        val navigationHelper = NavigationHelper.create(context)
        Log.d(TAG, "Attempting to navigate to: $destination")
        return navigationHelper.navigateSafely(this, destination, showToast)
    } catch (e: Exception) {
        Log.e(TAG, "Critical navigation error for destination: $destination", e)
        return false
    }
}

/**
 * Extension function for NavController to safely navigate to a destination with arguments
 * 
 * @param destination The base destination route
 * @param args The arguments to append to the route
 * @param showToast Whether to show a toast message for unimplemented screens
 * @return True if navigation was successful, false otherwise
 */
fun NavController.navigateSafelyWithArgs(
    destination: String,
    args: String,
    showToast: Boolean = true
): Boolean {
    try {
        val context = this.context
        val navigationHelper = NavigationHelper.create(context)
        Log.d(TAG, "Attempting to navigate to: $destination with args: $args")
        return navigationHelper.navigateSafelyWithArgs(this, destination, args, showToast)
    } catch (e: Exception) {
        Log.e(TAG, "Critical navigation error for destination: $destination with args: $args", e)
        return false
    }
}

/**
 * Extension function for NavController to check if a destination is implemented
 * 
 * @param destination The destination route
 * @return True if the destination is implemented, false otherwise
 */
fun NavController.isDestinationImplemented(destination: String): Boolean {
    val context = this.context
    val navigationHelper = NavigationHelper.create(context)
    val isImplemented = navigationHelper.isDestinationImplemented(destination)
    if (!isImplemented) {
        Log.w(TAG, "Attempted to navigate to unimplemented destination: $destination")
    }
    return isImplemented
}

/**
 * Extension function for NavController to check if a destination has a placeholder
 * 
 * @param destination The destination route
 * @return True if the destination has a placeholder, false otherwise
 */
fun NavController.isDestinationPlaceholder(destination: String): Boolean {
    val context = this.context
    val navigationHelper = NavigationHelper.create(context)
    return navigationHelper.isDestinationPlaceholder(destination)
}