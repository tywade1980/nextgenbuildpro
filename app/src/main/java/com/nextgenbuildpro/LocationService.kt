package com.nextgenbuildpro

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Enhanced Location Service for NextGenBuildPro
 * 
 * Features:
 * - Trigger-based location tracking (not continuous)
 * - High precision location tracking when needed
 * - Location history
 * - Battery-efficient location tracking
 */
class LocationService(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val _currentLocation = mutableStateOf<Location?>(null)
    private val _locationHistory = mutableStateOf<List<Location>>(emptyList())
    private val _isTracking = mutableStateOf(false)
    private var handlerThread: android.os.HandlerThread? = null

    // Maximum number of location history items to keep
    private val MAX_HISTORY_SIZE = 100

    val currentLocation: State<Location?> = _currentLocation
    val locationHistory: State<List<Location>> = _locationHistory
    val isTracking: State<Boolean> = _isTracking

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _currentLocation.value = location

            // Add new location to history and limit the size
            val updatedHistory = _locationHistory.value + location
            if (updatedHistory.size > MAX_HISTORY_SIZE) {
                _locationHistory.value = updatedHistory.takeLast(MAX_HISTORY_SIZE)
            } else {
                _locationHistory.value = updatedHistory
            }

            // Stop tracking immediately after receiving a location update
            // This makes the location service trigger-based instead of continuous
            stopTracking()
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    /**
     * Gets a single location update and then stops tracking
     * This is the trigger-based approach to location tracking
     */
    fun startTracking() {
        if (hasLocationPermission()) {
            try {
                // Stop any existing tracking first
                stopTracking()

                // Create a background thread for location processing
                handlerThread = android.os.HandlerThread("LocationThread")
                handlerThread?.start()
                val looper = handlerThread?.looper ?: return

                // Request location updates from both providers
                // We'll use requestLocationUpdates instead of the deprecated requestSingleUpdate
                // The listener will stop tracking after the first update
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0, // minTime - 0 means as soon as possible
                    0f, // minDistance - 0 means any distance change
                    locationListener,
                    looper
                )

                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0, // minTime - 0 means as soon as possible
                    0f, // minDistance - 0 means any distance change
                    locationListener,
                    looper
                )

                _isTracking.value = true
                android.util.Log.d("LocationService", "Trigger-based location tracking started")
            } catch (e: SecurityException) {
                // Handle permission exception
                android.util.Log.e("LocationService", "Security exception when starting location tracking: ${e.message}")
                _isTracking.value = false
                handlerThread = null
            } catch (e: Exception) {
                // Handle other exceptions
                android.util.Log.e("LocationService", "Error starting location tracking: ${e.message}")
                _isTracking.value = false
                handlerThread = null
            }
        } else {
            android.util.Log.w("LocationService", "Cannot start tracking: Location permission not granted")
        }
    }

    /**
     * Stops tracking location
     */
    fun stopTracking() {
        try {
            locationManager.removeUpdates(locationListener)

            // Clean up the handler thread
            handlerThread?.quitSafely()
            handlerThread = null

            android.util.Log.d("LocationService", "Location tracking stopped")
        } catch (e: Exception) {
            android.util.Log.e("LocationService", "Error stopping location tracking: ${e.message}")
        } finally {
            _isTracking.value = false
        }
    }

    /**
     * Checks if the app has the necessary location permissions
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests location permissions
     * On Android 10+, background location permission must be requested separately
     */
    fun requestLocationPermission(activity: Activity) {
        // First request foreground location permissions
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )

        // Background location permission should be requested separately
        // after foreground permissions are granted
        android.util.Log.i("LocationService", "Requested foreground location permissions")
    }

    /**
     * Requests background location permission
     * This should be called only after foreground location permissions are granted
     */
    fun requestBackgroundLocationPermission(activity: Activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Check if foreground location permissions are granted
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == 
                    PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == 
                    PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                )
                android.util.Log.i("LocationService", "Requested background location permission")
            } else {
                android.util.Log.w("LocationService", "Cannot request background location: Foreground location permissions not granted")
            }
        }
    }

    /**
     * Gets the last known location
     */
    fun getLastKnownLocation(): Location? {
        if (hasLocationPermission()) {
            try {
                // Try to get location from GPS provider first
                var location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                // If GPS location is not available, try network provider
                if (location == null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                return location
            } catch (e: SecurityException) {
                // Handle permission exception
                android.util.Log.e("LocationService", "Security exception when getting last known location: ${e.message}")
            } catch (e: Exception) {
                // Handle other exceptions
                android.util.Log.e("LocationService", "Error getting last known location: ${e.message}")
            }
        } else {
            android.util.Log.w("LocationService", "Cannot get last known location: Location permission not granted")
        }
        return null
    }

    /**
     * Calculates distance between two locations
     */
    fun calculateDistance(location1: Location, location2: Location): Float {
        return location1.distanceTo(location2)
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002
    }
}

/**
 * Composable function to use location service in Compose UI
 */
@Composable
fun rememberLocationService(): LocationService {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }

    DisposableEffect(locationService) {
        onDispose {
            locationService.stopTracking()
        }
    }

    return locationService
}
