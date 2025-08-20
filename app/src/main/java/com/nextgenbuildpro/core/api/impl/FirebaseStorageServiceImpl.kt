package com.nextgenbuildpro.core.api.impl

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.nextgenbuildpro.core.ApiKeyManager
import com.nextgenbuildpro.core.api.ApiUsageRegistry
import com.nextgenbuildpro.core.api.ConfigurationService
import com.nextgenbuildpro.core.api.FirebaseStorageService

/**
 * Implementation of FirebaseStorageService interface.
 * 
 * This class encapsulates all Firebase Storage operations and provides:
 * 1. Version checking to ensure compatibility
 * 2. Feature flagging to control feature availability
 * 3. Error handling for Firebase Storage operations
 * 4. Usage tracking via ApiUsageRegistry
 */
class FirebaseStorageServiceImpl(
    private val configService: ConfigurationService,
    private val apiUsageRegistry: ApiUsageRegistry
) : FirebaseStorageService {
    
    private val TAG = "FirebaseStorageService"
    private var storageInstance: FirebaseStorage? = null
    private val DEFAULT_FIREBASE_STORAGE_URL = "gs://nextgenbuildpro.firebasestorage.app"
    
    /**
     * Get the Firebase Storage instance
     * @return The Firebase Storage instance
     */
    override fun getStorageInstance(): FirebaseStorage {
        // Register API call
        registerApiCall("getStorageInstance")
        
        // Check if service is available
        if (!isServiceAvailable()) {
            Log.e(TAG, "Firebase Storage service is not available or not properly configured")
            throw IllegalStateException("Firebase Storage service is not available")
        }
        
        // Initialize if not already initialized
        if (storageInstance == null) {
            try {
                // Get the storage bucket URL from ApiKeyManager or use the default
                val bucketUrl = ApiKeyManager.getFirebaseStorageUrl() ?: DEFAULT_FIREBASE_STORAGE_URL
                
                if (ApiKeyManager.getFirebaseStorageUrl() == null) {
                    Log.w(TAG, "Firebase Storage URL not found in ApiKeyManager, using default URL")
                }
                
                // Set the storage bucket URL
                storageInstance = FirebaseStorage.getInstance(bucketUrl)
                Log.d(TAG, "Firebase Storage initialized with bucket URL: $bucketUrl")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Firebase Storage", e)
                throw IllegalStateException("Failed to initialize Firebase Storage", e)
            }
        }
        
        return storageInstance!!
    }
    
    /**
     * Get a reference to a file in Firebase Storage
     * @param path The path to the file
     * @return A StorageReference pointing to the file
     */
    override fun getReference(path: String): StorageReference {
        // Register API call
        registerApiCall("getReference")
        
        try {
            return getStorageInstance().reference.child(path)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting reference for path: $path", e)
            throw IllegalStateException("Failed to get reference for path: $path", e)
        }
    }
    
    /**
     * Check if the service is available and properly configured
     * @return True if the service is available, false otherwise
     */
    override fun isServiceAvailable(): Boolean {
        // Register API call
        registerApiCall("isServiceAvailable")
        
        // Check if the current version is supported
        if (!isVersionSupported()) {
            Log.w(TAG, "Firebase Storage version is not supported")
            return false
        }
        
        // Additional checks can be added here
        return true
    }
    
    /**
     * Get the current version of the Firebase Storage SDK
     * @return The version string
     */
    override fun getServiceVersion(): String {
        // Register API call
        registerApiCall("getServiceVersion")
        
        // In a real implementation, this would get the actual Firebase Storage SDK version
        // For now, we'll return a placeholder version
        return configService.getCurrentVersion(ConfigurationService.SERVICE_FIREBASE_STORAGE)
    }
    
    /**
     * Check if a specific feature is enabled
     * @param featureName The name of the feature to check
     * @return True if the feature is enabled, false otherwise
     */
    override fun isFeatureEnabled(featureName: String): Boolean {
        // Register API call
        registerApiCall("isFeatureEnabled")
        
        return configService.isFeatureEnabled(featureName, false)
    }
    
    /**
     * Get the Firebase Storage bucket URL
     * @return The bucket URL
     */
    override fun getBucketUrl(): String {
        // Register API call
        registerApiCall("getBucketUrl")
        
        return ApiKeyManager.getFirebaseStorageUrl() ?: DEFAULT_FIREBASE_STORAGE_URL
    }
    
    /**
     * Check if the current version is supported
     * @return True if the version is supported, false otherwise
     */
    private fun isVersionSupported(): Boolean {
        val currentVersion = getServiceVersion()
        val minVersion = FirebaseStorageService.MIN_SUPPORTED_VERSION
        
        // In a real implementation, this would compare version strings properly
        // For now, we'll use a simple string comparison
        return currentVersion >= minVersion
    }
    
    /**
     * Register an API call with the ApiUsageRegistry
     * @param methodName The name of the method being called
     */
    private fun registerApiCall(methodName: String) {
        try {
            apiUsageRegistry.registerApiCall(
                serviceName = ConfigurationService.SERVICE_FIREBASE_STORAGE,
                methodName = methodName,
                callerClass = getCallerClassName(),
                callerMethod = getCallerMethodName()
            )
        } catch (e: Exception) {
            // Don't let registry errors affect the service operation
            Log.e(TAG, "Error registering API call", e)
        }
    }
    
    /**
     * Get the name of the calling class
     * @return The name of the calling class
     */
    private fun getCallerClassName(): String {
        val stackTrace = Thread.currentThread().stackTrace
        // Skip first few elements that represent this method and its callers within this class
        for (i in 3 until stackTrace.size) {
            val className = stackTrace[i].className
            if (!className.startsWith(this.javaClass.name)) {
                return className
            }
        }
        return "unknown"
    }
    
    /**
     * Get the name of the calling method
     * @return The name of the calling method
     */
    private fun getCallerMethodName(): String {
        val stackTrace = Thread.currentThread().stackTrace
        // Skip first few elements that represent this method and its callers within this class
        for (i in 3 until stackTrace.size) {
            val className = stackTrace[i].className
            if (!className.startsWith(this.javaClass.name)) {
                return stackTrace[i].methodName
            }
        }
        return "unknown"
    }
}