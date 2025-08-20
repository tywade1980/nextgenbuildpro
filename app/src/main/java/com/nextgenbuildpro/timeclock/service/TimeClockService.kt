package com.nextgenbuildpro.timeclock.service

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.nextgenbuildpro.LocationService
import com.nextgenbuildpro.timeclock.data.model.TimeClockEvent
import com.nextgenbuildpro.timeclock.data.model.TimeClockStatus
import com.nextgenbuildpro.timeclock.data.repository.TimeClockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Service for managing time clock operations with location-based automation
 */
class TimeClockService(
    private val context: Context,
    private val locationService: LocationService,
    private val timeClockRepository: TimeClockRepository
) {
    private val TAG = "TimeClockService"
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var locationMonitoringJob: Job? = null

    // Current user ID (would come from authentication in a real app)
    private val currentUserId = "user_1"

    // Last known location
    private val _lastKnownLocation = mutableStateOf<Location?>(null)
    val lastKnownLocation: State<Location?> = _lastKnownLocation

    // Time clock status
    private val _timeClockStatus = mutableStateOf<TimeClockStatus>(TimeClockStatus())
    val timeClockStatus: State<TimeClockStatus> = _timeClockStatus

    // Event listeners
    private val clockEventListeners = mutableListOf<(TimeClockEvent) -> Unit>()

    init {
        // Initialize the service
        initialize()
    }

    /**
     * Initialize the service
     */
    private fun initialize() {
        Log.d(TAG, "Initializing TimeClockService")

        // Start monitoring location changes
        startLocationMonitoring()

        // Observe time clock status changes
        observeTimeClockStatus()
    }

    /**
     * Start monitoring location changes - trigger-based approach
     * This method no longer continuously monitors location
     */
    private fun startLocationMonitoring() {
        // Cancel any existing job
        locationMonitoringJob?.cancel()
        locationMonitoringJob = null

        Log.d(TAG, "Location monitoring is now trigger-based. Use requestLocationUpdate() to get location.")
    }

    /**
     * Request a single location update
     * This is the trigger-based approach to location monitoring
     */
    fun requestLocationUpdate() {
        // Cancel any existing job first
        locationMonitoringJob?.cancel()

        // Create a new job to handle the location update
        locationMonitoringJob = serviceScope.launch {
            Log.d(TAG, "Requesting single location update")

            // Get current location before requesting an update
            val initialLocation = locationService.currentLocation.value

            // Request a location update
            locationService.startTracking()

            // Wait for the location update (with timeout)
            var attempts = 0
            var locationUpdated = false
            while (attempts < 10 && !locationUpdated) {
                kotlinx.coroutines.delay(1000) // Wait 1 second

                val newLocation = locationService.currentLocation.value
                if (newLocation != null && newLocation != initialLocation) {
                    handleLocationChange(newLocation)
                    locationUpdated = true
                }

                attempts++
            }

            if (!locationUpdated) {
                Log.d(TAG, "No new location received after timeout")
            }
        }
    }

    /**
     * Observe time clock status changes
     */
    private fun observeTimeClockStatus() {
        serviceScope.launch {
            timeClockRepository.timeClockStatus.collectLatest { status ->
                _timeClockStatus.value = status
            }
        }
    }

    /**
     * Handle location changes
     */
    private suspend fun handleLocationChange(location: Location) {
        // Update last known location
        _lastKnownLocation.value = location

        // Check if the location change should trigger a clock event
        val event = timeClockRepository.handleLocationChange(location, currentUserId)

        // If an event was triggered, notify listeners
        if (event != null) {
            Log.d(TAG, "Clock event triggered: $event")
            notifyClockEventListeners(event)
        }
    }

    /**
     * Manually clock in
     */
    fun clockIn(
        workLocationId: String,
        projectId: String? = null,
        notes: String = ""
    ) {
        serviceScope.launch {
            val location = _lastKnownLocation.value ?: locationService.getLastKnownLocation()

            if (location != null) {
                val entry = timeClockRepository.clockIn(
                    workLocationId = workLocationId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    userId = currentUserId,
                    projectId = projectId,
                    notes = notes,
                    isAutomatic = false
                )

                if (entry != null) {
                    Log.d(TAG, "Manually clocked in at $workLocationId")
                    notifyClockEventListeners(TimeClockEvent.MANUAL_CLOCK_IN)
                } else {
                    Log.e(TAG, "Failed to clock in")
                }
            } else {
                Log.e(TAG, "Cannot clock in: Location not available")
            }
        }
    }

    /**
     * Manually clock out
     */
    fun clockOut(notes: String = "") {
        serviceScope.launch {
            val location = _lastKnownLocation.value ?: locationService.getLastKnownLocation()

            if (location != null) {
                val entry = timeClockRepository.clockOut(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    userId = currentUserId,
                    notes = notes,
                    isAutomatic = false
                )

                if (entry != null) {
                    Log.d(TAG, "Manually clocked out")
                    notifyClockEventListeners(TimeClockEvent.MANUAL_CLOCK_OUT)
                } else {
                    Log.e(TAG, "Failed to clock out")
                }
            } else {
                Log.e(TAG, "Cannot clock out: Location not available")
            }
        }
    }

    /**
     * Add a clock event listener
     */
    fun addClockEventListener(listener: (TimeClockEvent) -> Unit) {
        clockEventListeners.add(listener)
    }

    /**
     * Remove a clock event listener
     */
    fun removeClockEventListener(listener: (TimeClockEvent) -> Unit) {
        clockEventListeners.remove(listener)
    }

    /**
     * Notify all clock event listeners
     */
    private fun notifyClockEventListeners(event: TimeClockEvent) {
        clockEventListeners.forEach { it(event) }
    }

    /**
     * Get all work locations
     */
    suspend fun getAllWorkLocations() = timeClockRepository.getAllWorkLocations()

    /**
     * Get time clock sessions for the current user
     */
    suspend fun getTimeClockSessions() = timeClockRepository.getTimeClockSessionsForUser(currentUserId)

    /**
     * Calculate total work hours for a date range
     */
    suspend fun calculateTotalWorkHours(startDate: java.util.Date, endDate: java.util.Date) = 
        timeClockRepository.calculateTotalWorkHours(currentUserId, startDate, endDate)

    /**
     * Process voice command for time clock
     * Implements a "wake word" system for time clock functionality
     */
    fun processVoiceCommand(command: String): Boolean {
        val lowerCommand = command.lowercase().trim()

        return when {
            lowerCommand.contains("clock in") -> {
                // Get the default work location (in a real app, this would be more sophisticated)
                serviceScope.launch {
                    val locations = getAllWorkLocations()
                    if (locations.isNotEmpty()) {
                        clockIn(locations.first().id, notes = "Voice command: $command")
                        Log.d(TAG, "Voice command processed: Clock In")
                    } else {
                        Log.e(TAG, "Cannot process 'clock in' command: No work locations available")
                    }
                }
                true
            }
            lowerCommand.contains("clock out") -> {
                clockOut(notes = "Voice command: $command")
                Log.d(TAG, "Voice command processed: Clock Out")
                true
            }
            else -> false
        }
    }

    /**
     * Stop the service
     */
    fun stop() {
        locationMonitoringJob?.cancel()
        locationMonitoringJob = null
    }

    companion object {
        /**
         * Create a TimeClockService instance
         */
        fun create(
            context: Context,
            locationService: LocationService,
            timeClockRepository: TimeClockRepository
        ): TimeClockService {
            return TimeClockService(context, locationService, timeClockRepository)
        }
    }
}
