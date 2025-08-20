package com.nextgenbuildpro.timeclock.data.model

import android.location.Location
import java.util.Date
import java.util.UUID

/**
 * Data models for the Time Clock feature
 */

/**
 * Represents a work location where clock-in/out can occur
 */
data class WorkLocation(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float = 100f, // Default radius in meters
    val projectId: String? = null,
    val isActive: Boolean = true
) {
    /**
     * Check if a location is within this work location's radius
     */
    fun isLocationWithinRadius(location: Location): Boolean {
        val workLocationPoint = Location("WorkLocation")
        workLocationPoint.latitude = latitude
        workLocationPoint.longitude = longitude
        
        return location.distanceTo(workLocationPoint) <= radius
    }
}

/**
 * Represents a time clock entry (clock-in or clock-out event)
 */
data class TimeClockEntry(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val timestamp: Date = Date(),
    val type: TimeClockEntryType,
    val workLocationId: String,
    val workLocationName: String,
    val latitude: Double,
    val longitude: Double,
    val projectId: String? = null,
    val notes: String = "",
    val isAutomatic: Boolean = true // Whether this was automatically generated
)

/**
 * Represents a time clock session (from clock-in to clock-out)
 */
data class TimeClockSession(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val clockInEntry: TimeClockEntry,
    val clockOutEntry: TimeClockEntry? = null,
    val duration: Long? = null, // Duration in milliseconds
    val projectId: String? = null,
    val workLocationId: String,
    val workLocationName: String,
    val date: Date = clockInEntry.timestamp,
    val notes: String = ""
)

/**
 * Represents the current time clock status
 */
data class TimeClockStatus(
    val isClockedIn: Boolean = false,
    val currentWorkLocationId: String? = null,
    val currentWorkLocationName: String? = null,
    val currentProjectId: String? = null,
    val lastClockInTime: Date? = null,
    val currentSessionId: String? = null,
    val currentSessionDuration: Long? = null // Duration in milliseconds
)

/**
 * Enum for time clock entry types
 */
enum class TimeClockEntryType {
    CLOCK_IN,
    CLOCK_OUT
}

/**
 * Enum for time clock events
 */
enum class TimeClockEvent {
    ENTERED_WORK_LOCATION,
    LEFT_WORK_LOCATION,
    MANUAL_CLOCK_IN,
    MANUAL_CLOCK_OUT
}