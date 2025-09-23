package com.nextgenbuildpro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nextgenbuildpro.core.ModuleManager
import com.nextgenbuildpro.debug.AutomationDebugger
import com.nextgenbuildpro.receptionist.service.registerDialerRoleLauncher
import com.nextgenbuildpro.receptionist.service.requestDialerRole
import com.nextgenbuildpro.ui.NextGenBuildProApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the ActivityResultLauncher for dialer role request
        dialerRoleLauncher = registerDialerRoleLauncher(this)

        // Initialize the ModuleManager with automation services
        ModuleManager.initialize(applicationContext)
        
        // Initialize automation debugger
        automationDebugger = AutomationDebugger(applicationContext)
        
        // Initialize automation system in background
        initializeAutomationSystem()

        // Request dialer role if needed
        // requestDialerRoleIfNeeded()

        // Get required services from the ServiceModule
        val serviceModule = ModuleManager.getServiceModule()
        val locationService = serviceModule.getLocationService()
        val permissionManager = serviceModule.getPermissionManager()
        val crmAgent = serviceModule.getCrmAgent()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Use the NextGenBuildProApp composable from MainApp.kt
                    NextGenBuildProApp(
                        locationService = locationService,
                        permissionManager = permissionManager,
                        crmAgent = crmAgent
                    )
                }
            }
        }
    }
    
    /**
     * Initialize the automation system in the background
     */
    private fun initializeAutomationSystem() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "Initializing automation system...")
                
                // Run automation tests to ensure everything is working
                try {
                    automationDebugger.runAutomationTests()
                    
                    // Generate and log test report
                    val report = automationDebugger.generateTestReport()
                    Log.i(TAG, "Automation Test Report:\n$report")
                } catch (e: Exception) {
                    Log.w(TAG, "Automation tests failed, but continuing with initialization", e)
                }
                
                Log.i(TAG, "Automation system initialized successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize automation system", e)
            }
        }
    }

    /**
     * Request the dialer role if needed
     * 
     * This method uses the ActivityResultLauncher to request the dialer role,
     * which is the recommended approach for Android 11+ (API level 30+)
     */
    fun requestDialerRoleIfNeeded() {
        requestDialerRole(applicationContext, this, dialerRoleLauncher)
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
}
