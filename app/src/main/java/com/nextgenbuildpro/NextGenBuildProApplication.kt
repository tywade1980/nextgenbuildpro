package com.nextgenbuildpro

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.nextgenbuildpro.core.ApiKeyManager
import com.nextgenbuildpro.core.FirebaseStorageInitializer
import com.nextgenbuildpro.core.FirestoreInitializer
import com.nextgenbuildpro.core.MainOrchestrator
import com.nextgenbuildpro.core.api.di.ApiModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class for NextGenBuildPro
 * Initializes Firebase components, API services, and the main orchestration system
 */
class NextGenBuildProApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var mainOrchestrator: MainOrchestrator

    companion object {
        private const val TAG = "NextGenBuildProApp"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize API Key Manager
        initializeApiKeyManager()

        // Initialize Firebase
        initializeFirebase()

        // Initialize API Services
        initializeApiServices()

        // Initialize Main Orchestrator
        initializeMainOrchestrator()
    }

    /**
     * Initialize API Key Manager
     */
    private fun initializeApiKeyManager() {
        try {
            val success = ApiKeyManager.initialize(this)
            if (success) {
                Log.d(TAG, "API Key Manager initialized successfully")
            } else {
                Log.w(TAG, "API Key Manager initialization failed, using default values")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing API Key Manager", e)
        }
    }

    /**
     * Initialize Firebase components
     */
    private fun initializeFirebase() {
        try {
            // Initialize Firebase App first
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase App initialized")

            // Initialize Firestore with test mode settings
            val firestore = FirestoreInitializer.initialize()
            Log.d(TAG, "Firestore initialized: ${firestore.app.name}")

            // Initialize Firebase Storage with the correct bucket URL
            val storage = FirebaseStorageInitializer.initialize()
            Log.d(TAG, "Firebase Storage initialized: ${storage.app.name}")

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
        }
    }

    /**
     * Initialize API Services
     * This sets up the abstraction layer for all API interactions
     */
    private fun initializeApiServices() {
        try {
            // Initialize all API services
            ApiModule.initialize(this)
            Log.d(TAG, "API Services initialized successfully")

            // Log initialization in the API usage registry
            val apiUsageRegistry = ApiModule.provideApiUsageRegistry(this)
            apiUsageRegistry.registerApiCall(
                serviceName = "application",
                methodName = "initializeApiServices",
                callerClass = this.javaClass.name,
                callerMethod = "onCreate"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing API Services", e)
        }
    }

    /**
     * Initialize Main Orchestrator
     * This initializes the AI orchestration system in the background
     */
    private fun initializeMainOrchestrator() {
        applicationScope.launch {
            try {
                Log.d(TAG, "Initializing Main Orchestrator...")
                mainOrchestrator = MainOrchestrator(this@NextGenBuildProApplication)
                val result = mainOrchestrator.initialize()
                
                if (result.isSuccess) {
                    Log.d(TAG, "Main Orchestrator initialized successfully")
                } else {
                    Log.w(TAG, "Main Orchestrator initialization failed: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Main Orchestrator", e)
            }
        }
    }

    /**
     * Get the MainOrchestrator instance
     */
    fun getMainOrchestrator(): MainOrchestrator? {
        return if (::mainOrchestrator.isInitialized) mainOrchestrator else null
    }
}
