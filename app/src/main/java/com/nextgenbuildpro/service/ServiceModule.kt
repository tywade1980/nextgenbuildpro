package com.nextgenbuildpro.service

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nextgenbuildpro.CrmAgent
import com.nextgenbuildpro.LocationService
import com.nextgenbuildpro.PermissionManager
import com.nextgenbuildpro.timeclock.data.repository.TimeClockRepository
import com.nextgenbuildpro.timeclock.service.TimeClockService

/**
 * Service Module for NextGenBuildPro
 * 
 * This module manages common services used throughout the application:
 * - Location services
 * - Permission management
 * - CRM agent
 */
object ServiceModule {
    private var initialized = false
    private lateinit var locationService: LocationService
    private lateinit var permissionManager: PermissionManager
    private lateinit var crmAgent: CrmAgent
    private lateinit var timeClockRepository: TimeClockRepository
    private lateinit var timeClockService: TimeClockService

    /**
     * Check if the module is already initialized
     */
    fun isInitialized(): Boolean {
        return initialized
    }

    /**
     * Initialize the Service module
     */
    fun initialize(context: Context) {
        if (initialized) return

        // Initialize services
        locationService = LocationService(context)
        permissionManager = PermissionManager(context)
        crmAgent = CrmAgent(context)

        // Initialize time clock services
        timeClockRepository = TimeClockRepository(context)
        timeClockService = TimeClockService.create(context, locationService, timeClockRepository)

        initialized = true
    }

    /**
     * Get the location service
     */
    fun getLocationService(): LocationService {
        checkInitialized()
        return locationService
    }

    /**
     * Get the permission manager
     */
    fun getPermissionManager(): PermissionManager {
        checkInitialized()
        return permissionManager
    }

    /**
     * Get the CRM agent
     */
    fun getCrmAgent(): CrmAgent {
        checkInitialized()
        return crmAgent
    }

    /**
     * Get the time clock repository
     */
    fun getTimeClockRepository(): TimeClockRepository {
        checkInitialized()
        return timeClockRepository
    }

    /**
     * Get the time clock service
     */
    fun getTimeClockService(): TimeClockService {
        checkInitialized()
        return timeClockService
    }

    /**
     * Check if the module is initialized
     */
    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("Service Module is not initialized. Call initialize() first.")
        }
    }
}

/**
 * Composable function to remember service components
 */
@Composable
fun rememberServiceComponents(): ServiceComponents {
    val context = LocalContext.current

    // Initialize module if needed
    if (!ServiceModule.isInitialized()) {
        ServiceModule.initialize(context)
    }

    return ServiceComponents(
        locationService = ServiceModule.getLocationService(),
        permissionManager = ServiceModule.getPermissionManager(),
        crmAgent = ServiceModule.getCrmAgent(),
        timeClockService = ServiceModule.getTimeClockService()
    )
}

/**
 * Data class to hold service components
 */
data class ServiceComponents(
    val locationService: LocationService,
    val permissionManager: PermissionManager,
    val crmAgent: CrmAgent,
    val timeClockService: TimeClockService
)
