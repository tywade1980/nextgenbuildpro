package com.nextgenbuildpro

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nextgenbuildpro.core.ModuleManager
import com.nextgenbuildpro.receptionist.service.registerDialerRoleLauncher
import com.nextgenbuildpro.receptionist.service.requestDialerRole
import com.nextgenbuildpro.ui.NextGenBuildProApp

/**
 * Main Activity for NextGenBuildPro
 * 
 * This activity initializes the ModuleManager and sets up the UI.
 * It uses a modular framework to manage different parts of the application.
 */
class MainActivity : ComponentActivity() {
    // ActivityResultLauncher for dialer role request
    private lateinit var dialerRoleLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the ActivityResultLauncher for dialer role request
        dialerRoleLauncher = registerDialerRoleLauncher(this)

        // Initialize the ModuleManager
        ModuleManager.initialize(applicationContext)

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
     * Request the dialer role if needed
     * 
     * This method uses the ActivityResultLauncher to request the dialer role,
     * which is the recommended approach for Android 11+ (API level 30+)
     */
    fun requestDialerRoleIfNeeded() {
        requestDialerRole(applicationContext, this, dialerRoleLauncher)
    }
}
