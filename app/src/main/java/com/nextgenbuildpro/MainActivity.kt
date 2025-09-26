package com.nextgenbuildpro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import com.nextgenbuildpro.core.ModuleManager
import com.nextgenbuildpro.debug.AutomationDebugger
import com.nextgenbuildpro.debug.DeadCodeDetector
import com.nextgenbuildpro.debug.WorkflowAnalyzer
import com.nextgenbuildpro.navigation.NavigationStackTracker
import com.nextgenbuildpro.receptionist.service.registerDialerRoleLauncher
import com.nextgenbuildpro.receptionist.service.requestDialerRole
import com.nextgenbuildpro.ui.FeatureCompletionTracker
import com.nextgenbuildpro.ui.NextGenBuildProApp
import com.nextgenbuildpro.util.ErrorLogger
import com.nextgenbuildpro.LocationService
import com.nextgenbuildpro.PermissionManager
import com.nextgenbuildpro.CrmAgent
import kotlinx.coroutines.*

private const val TAG = "MainActivity"

/**
 * Main Activity for NextGenBuildPro
 * 
 * This activity initializes the ModuleManager and sets up the UI.
 * It uses a modular framework to manage different parts of the application.
 * Now includes automation system initialization and testing.
 */
class MainActivity : ComponentActivity() {
    // ActivityResultLauncher for dialer role request
    private lateinit var dialerRoleLauncher: ActivityResultLauncher<Intent>
    
    // Automation debugger
    private lateinit var automationDebugger: AutomationDebugger

    // Navigation stack tracker
    private lateinit var navigationStackTracker: NavigationStackTracker

    // Services and managers
    private lateinit var locationService: LocationService
    private lateinit var permissionManager: PermissionManager
    private lateinit var crmAgent: CrmAgent

    // Flag to determine if we're in debug mode
    private val isDebugBuild = true // Hardcoded for now instead of using BuildConfig.DEBUG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the ActivityResultLauncher for dialer role request
        dialerRoleLauncher = registerDialerRoleLauncher(this)

        // Initialize the ModuleManager with automation services
        ModuleManager.initialize(applicationContext)
        
        // Initialize required services and managers
        locationService = LocationService(this)
        permissionManager = PermissionManager(this)
        crmAgent = CrmAgent(this)

        // Initialize automation debugger
        automationDebugger = AutomationDebugger(applicationContext)
        
        // Initialize automation system in background
        initializeAutomationSystem()

        // Request dialer role if needed
        requestDialerRole(applicationContext, this, dialerRoleLauncher)

        // Initialize error logging system
        initializeErrorLogging()

        // Enable dead code detection in debug builds
        if (isDebugBuild) {
            DeadCodeDetector.setEnabled(true)
            Log.d(TAG, "Dead code detection enabled")
            WorkflowAnalyzer.reset()
            FeatureCompletionTracker.reset()
        }

        setContent {
            NextGenBuildProApp(
                locationService = locationService,
                permissionManager = permissionManager,
                crmAgent = crmAgent
            )
        }
    }
    
    /**
     * Initialize the automation system in a background thread
     */
    private fun initializeAutomationSystem() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Initializing automation system")
                withContext(Dispatchers.Main) {
                    automationDebugger.initialize()
                }
                Log.d(TAG, "Automation system initialized successfully")
            } catch (e: Exception) {
                ErrorLogger.logError(TAG, "Failed to initialize automation system", e)
            }
        }
    }

    /**
     * Initialize the error logging system
     */
    private fun initializeErrorLogging() {
        try {
            // Log startup info
            val deviceInfo = mapOf(
                "device" to android.os.Build.MODEL,
                "os" to "Android ${android.os.Build.VERSION.RELEASE}",
                "appVersion" to "1.0.0" // Hardcoded for now instead of using BuildConfig.VERSION_NAME
            )

            ErrorLogger.logError(TAG, "Application started", null, deviceInfo)
            Log.d(TAG, "Error logging system initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize error logging", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::navigationStackTracker.isInitialized) {
            navigationStackTracker.stopTracking(this)
        }

        // Log navigation and button statistics before shutdown
        if (isDebugBuild) {
            DeadCodeDetector.logReport()
            Log.d(TAG, navigationStackTracker.getNavigationReport())
            FeatureCompletionTracker.logReport()
            WorkflowAnalyzer.logReport()
        }
    }
}
