package com.nextgenbuildpro.timeclock.data.repository

import android.content.Context
import android.location.Location
import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.timeclock.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Repository for managing time clock data
 */
class TimeClockRepository(private val context: Context) {
    // Work locations
    private val _workLocations = MutableStateFlow<List<WorkLocation>>(emptyList())
    val workLocations: StateFlow<List<WorkLocation>> = _workLocations.asStateFlow()
    
    // Time clock entries
    private val _timeClockEntries = MutableStateFlow<List<TimeClockEntry>>(emptyList())
    val timeClockEntries: StateFlow<List<TimeClockEntry>> = _timeClockEntries.asStateFlow()
    
    // Time clock sessions
    private val _timeClockSessions = MutableStateFlow<List<TimeClockSession>>(emptyList())
    val timeClockSessions: StateFlow<List<TimeClockSession>> = _timeClockSessions.asStateFlow()
    
    // Current time clock status
    private val _timeClockStatus = MutableStateFlow(TimeClockStatus())
    val timeClockStatus: StateFlow<TimeClockStatus> = _timeClockStatus.asStateFlow()
    
    init {
        // Load sample data for demonstration
        loadSampleData()
    }
    
    /**
     * Add a new work location
     */
    suspend fun addWorkLocation(workLocation: WorkLocation): Boolean {
        try {
            _workLocations.value = _workLocations.value + workLocation
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Update an existing work location
     */
    suspend fun updateWorkLocation(workLocation: WorkLocation): Boolean {
        try {
            _workLocations.value = _workLocations.value.map { 
                if (it.id == workLocation.id) workLocation else it 
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Delete a work location
     */
    suspend fun deleteWorkLocation(workLocationId: String): Boolean {
        try {
            _workLocations.value = _workLocations.value.filter { it.id != workLocationId }
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Get all work locations
     */
    suspend fun getAllWorkLocations(): List<WorkLocation> {
        return _workLocations.value
    }
    
    /**
     * Get a work location by ID
     */
    suspend fun getWorkLocationById(workLocationId: String): WorkLocation? {
        return _workLocations.value.find { it.id == workLocationId }
    }
    
    /**
     * Find a work location that contains the given location
     */
    suspend fun findWorkLocationForLocation(location: Location): WorkLocation? {
        return _workLocations.value.find { workLocation ->
            workLocation.isActive && workLocation.isLocationWithinRadius(location)
        }
    }
    
    /**
     * Clock in at a work location
     */
    suspend fun clockIn(
        workLocationId: String,
        latitude: Double,
        longitude: Double,
        userId: String,
        projectId: String? = null,
        notes: String = "",
        isAutomatic: Boolean = true
    ): TimeClockEntry? {
        try {
            // Get the work location
            val workLocation = getWorkLocationById(workLocationId) ?: return null
            
            // Create a new time clock entry
            val timeClockEntry = TimeClockEntry(
                userId = userId,
                type = TimeClockEntryType.CLOCK_IN,
                workLocationId = workLocationId,
                workLocationName = workLocation.name,
                latitude = latitude,
                longitude = longitude,
                projectId = projectId ?: workLocation.projectId,
                notes = notes,
                isAutomatic = isAutomatic
            )
            
            // Add the entry to the list
            _timeClockEntries.value = _timeClockEntries.value + timeClockEntry
            
            // Create a new session
            val timeClockSession = TimeClockSession(
                userId = userId,
                clockInEntry = timeClockEntry,
                workLocationId = workLocationId,
                workLocationName = workLocation.name,
                projectId = projectId ?: workLocation.projectId,
                notes = notes
            )
            
            // Add the session to the list
            _timeClockSessions.value = _timeClockSessions.value + timeClockSession
            
            // Update the time clock status
            _timeClockStatus.value = TimeClockStatus(
                isClockedIn = true,
                currentWorkLocationId = workLocationId,
                currentWorkLocationName = workLocation.name,
                currentProjectId = projectId ?: workLocation.projectId,
                lastClockInTime = timeClockEntry.timestamp,
                currentSessionId = timeClockSession.id
            )
            
            return timeClockEntry
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * Clock out from a work location
     */
    suspend fun clockOut(
        latitude: Double,
        longitude: Double,
        userId: String,
        notes: String = "",
        isAutomatic: Boolean = true
    ): TimeClockEntry? {
        try {
            // Check if the user is clocked in
            if (!_timeClockStatus.value.isClockedIn) {
                return null
            }
            
            val workLocationId = _timeClockStatus.value.currentWorkLocationId ?: return null
            val workLocation = getWorkLocationById(workLocationId) ?: return null
            val sessionId = _timeClockStatus.value.currentSessionId ?: return null
            
            // Create a new time clock entry
            val timeClockEntry = TimeClockEntry(
                userId = userId,
                type = TimeClockEntryType.CLOCK_OUT,
                workLocationId = workLocationId,
                workLocationName = workLocation.name,
                latitude = latitude,
                longitude = longitude,
                projectId = _timeClockStatus.value.currentProjectId,
                notes = notes,
                isAutomatic = isAutomatic
            )
            
            // Add the entry to the list
            _timeClockEntries.value = _timeClockEntries.value + timeClockEntry
            
            // Update the session
            val session = _timeClockSessions.value.find { it.id == sessionId }
            if (session != null) {
                val clockInTime = session.clockInEntry.timestamp.time
                val clockOutTime = timeClockEntry.timestamp.time
                val duration = clockOutTime - clockInTime
                
                val updatedSession = session.copy(
                    clockOutEntry = timeClockEntry,
                    duration = duration,
                    notes = if (notes.isNotEmpty()) "${session.notes}\n$notes" else session.notes
                )
                
                _timeClockSessions.value = _timeClockSessions.value.map { 
                    if (it.id == sessionId) updatedSession else it 
                }
            }
            
            // Update the time clock status
            _timeClockStatus.value = TimeClockStatus()
            
            return timeClockEntry
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * Handle a location change event
     */
    suspend fun handleLocationChange(location: Location, userId: String): TimeClockEvent? {
        try {
            // Check if the user is at a work location
            val workLocation = findWorkLocationForLocation(location)
            
            // If the user is clocked in
            if (_timeClockStatus.value.isClockedIn) {
                val currentWorkLocationId = _timeClockStatus.value.currentWorkLocationId
                
                // If the user has left the current work location
                if (currentWorkLocationId != null && workLocation?.id != currentWorkLocationId) {
                    // Clock out
                    clockOut(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        userId = userId,
                        isAutomatic = true
                    )
                    return TimeClockEvent.LEFT_WORK_LOCATION
                }
            } 
            // If the user is not clocked in and has entered a work location
            else if (workLocation != null) {
                // Clock in
                clockIn(
                    workLocationId = workLocation.id,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    userId = userId,
                    projectId = workLocation.projectId,
                    isAutomatic = true
                )
                return TimeClockEvent.ENTERED_WORK_LOCATION
            }
            
            return null
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * Get time clock entries for a specific user
     */
    suspend fun getTimeClockEntriesForUser(userId: String): List<TimeClockEntry> {
        return _timeClockEntries.value.filter { it.userId == userId }
    }
    
    /**
     * Get time clock sessions for a specific user
     */
    suspend fun getTimeClockSessionsForUser(userId: String): List<TimeClockSession> {
        return _timeClockSessions.value.filter { it.userId == userId }
    }
    
    /**
     * Get time clock sessions for a specific date range
     */
    suspend fun getTimeClockSessionsForDateRange(
        userId: String,
        startDate: Date,
        endDate: Date
    ): List<TimeClockSession> {
        return _timeClockSessions.value.filter { session ->
            session.userId == userId && 
            session.date.time >= startDate.time && 
            session.date.time <= endDate.time
        }
    }
    
    /**
     * Calculate total work hours for a specific date range
     */
    suspend fun calculateTotalWorkHours(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Double {
        val sessions = getTimeClockSessionsForDateRange(userId, startDate, endDate)
        val totalMilliseconds = sessions.sumOf { it.duration ?: 0 }
        return totalMilliseconds / (1000.0 * 60 * 60) // Convert to hours
    }
    
    /**
     * Load sample data for demonstration
     */
    private fun loadSampleData() {
        // Sample work locations
        val sampleWorkLocations = listOf(
            WorkLocation(
                id = "location_1",
                name = "Main Office",
                address = "123 Main St, San Francisco, CA",
                latitude = 37.7749,
                longitude = -122.4194,
                radius = 100f
            ),
            WorkLocation(
                id = "location_2",
                name = "Project Site - Johnson Residence",
                address = "456 Oak Ave, San Francisco, CA",
                latitude = 37.7833,
                longitude = -122.4167,
                radius = 50f,
                projectId = "project_2"
            ),
            WorkLocation(
                id = "location_3",
                name = "Warehouse",
                address = "789 Industrial Blvd, Oakland, CA",
                latitude = 37.8044,
                longitude = -122.2711,
                radius = 150f
            )
        )
        
        _workLocations.value = sampleWorkLocations
    }
}