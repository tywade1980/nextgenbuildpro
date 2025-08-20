package com.nextgenbuildpro

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.nextgenbuildpro.core.ApiKeyManager
import com.nextgenbuildpro.core.FirebaseStorageInitializer
import com.nextgenbuildpro.core.FirestoreInitializer
import com.nextgenbuildpro.core.api.di.ApiModule

/**
 * Application class for NextGenBuildPro
 * Initializes Firebase components and other app-wide configurations
 */
class NextGenBuildProApplication : Application() {

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
}
