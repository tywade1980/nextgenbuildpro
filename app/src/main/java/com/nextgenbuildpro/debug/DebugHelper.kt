package com.nextgenbuildpro.debug

import android.content.Context
import com.nextgenbuildpro.crm.data.repository.PhotoRepository

/**
 * Debug Helper for NextGenBuildPro
 * 
 * This class has been emptied to remove debugging functionality.
 */
class DebugHelper(private val context: Context) {
    /**
     * Log a debug message - does nothing
     */
    fun logDebug(message: String) {
        // Debugging disabled
    }

    /**
     * Show a toast message for debugging - does nothing
     */
    fun showDebugToast(message: String) {
        // Debugging disabled
    }

    /**
     * Restart all monitoring services - does nothing
     */
    fun restartAllServices() {
        // Debugging disabled
    }

    /**
     * Add a test project location for photo matching - does nothing
     */
    fun addTestProjectLocation(photoRepository: PhotoRepository, latitude: Double, longitude: Double) {
        // Debugging disabled
    }

    /**
     * Simulate a clock-out event to trigger the daily log - does nothing
     */
    fun simulateClockOut(projectId: String = "project_2", leadId: String = "lead_2") {
        // Debugging disabled
    }

    /**
     * Print the current state of all repositories to the log - does nothing
     */
    fun logRepositoryState(photoRepository: PhotoRepository) {
        // Debugging disabled
    }

    /**
     * Check if all required permissions are granted - returns empty list
     */
    fun checkPermissions(): List<String> {
        return emptyList()
    }

    /**
     * Set the emulator's mock location - does nothing
     */
    fun setMockLocation(latitude: Double, longitude: Double) {
        // Debugging disabled
    }

    companion object {
        /**
         * Create a debug helper instance
         */
        fun create(context: Context): DebugHelper {
            return DebugHelper(context)
        }
    }
}
