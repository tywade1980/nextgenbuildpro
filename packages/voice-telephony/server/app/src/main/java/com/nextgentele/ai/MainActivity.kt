package com.nextgentele.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.nextgentele.ai.service.CallManagementService
import com.nextgentele.ai.service.IntegrationSocketService
import com.nextgentele.ai.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            initializeServices()
        } else {
            Toast.makeText(this, "Permissions required for AI call handling", Toast.LENGTH_LONG).show()
        }
    }
    
    private val requiredPermissions = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        checkPermissions()
    }
    
    private fun setupUI() {
        binding.apply {
            // AI Call Handler Toggle
            aiCallHandlerSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    startCallManagementService()
                } else {
                    stopCallManagementService()
                }
            }
            
            // Integration Service Toggle
            integrationServiceSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    startIntegrationService()
                } else {
                    stopIntegrationService()
                }
            }
            
            // Settings Button
            settingsButton.setOnClickListener {
                // TODO: Open settings activity
                Toast.makeText(this@MainActivity, "Settings coming soon", Toast.LENGTH_SHORT).show()
            }
            
            // Test Call Button
            testCallButton.setOnClickListener {
                // TODO: Implement test call functionality
                Toast.makeText(this@MainActivity, "Test call functionality", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun checkPermissions() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            initializeServices()
        }
    }
    
    private fun initializeServices() {
        // Check if we can be the default dialer app
        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        val defaultDialerPackage = telecomManager.defaultDialerPackage
        
        if (defaultDialerPackage != packageName) {
            Toast.makeText(
                this,
                "For full functionality, set NextGenTele as default dialer",
                Toast.LENGTH_LONG
            ).show()
        }
        
        updateServiceStatus()
    }
    
    private fun startCallManagementService() {
        val intent = Intent(this, CallManagementService::class.java)
        startForegroundService(intent)
        updateServiceStatus()
    }
    
    private fun stopCallManagementService() {
        val intent = Intent(this, CallManagementService::class.java)
        stopService(intent)
        updateServiceStatus()
    }
    
    private fun startIntegrationService() {
        val intent = Intent(this, IntegrationSocketService::class.java)
        startService(intent)
        updateServiceStatus()
    }
    
    private fun stopIntegrationService() {
        val intent = Intent(this, IntegrationSocketService::class.java)
        stopService(intent)
        updateServiceStatus()
    }
    
    private fun updateServiceStatus() {
        // Update UI to reflect current service status
        binding.apply {
            serviceStatusText.text = when {
                isServiceRunning(CallManagementService::class.java) -> "AI Call Handler: Active"
                else -> "AI Call Handler: Inactive"
            }
        }
    }
    
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}