package com.nextgenbuildpro.core.api.impl

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.nextgenbuildpro.core.api.ApiUsageRegistry
import com.nextgenbuildpro.core.api.ConfigurationService
import com.nextgenbuildpro.core.api.FirestoreService

/**
 * Implementation of FirestoreService interface.
 * 
 * This class encapsulates all Firestore operations and provides:
 * 1. Version checking to ensure compatibility
 * 2. Feature flagging to control feature availability
 * 3. Error handling for Firestore operations
 * 4. Usage tracking via ApiUsageRegistry
 */
class FirestoreServiceImpl(
    private val configService: ConfigurationService,
    private val apiUsageRegistry: ApiUsageRegistry
) : FirestoreService {
    
    private val TAG = "FirestoreService"
    private var firestoreInstance: FirebaseFirestore? = null
    private var offlinePersistenceEnabled = true
    
    /**
     * Get the Firestore instance
     * @return The Firestore instance
     */
    override fun getFirestoreInstance(): FirebaseFirestore {
        // Register API call
        registerApiCall("getFirestoreInstance")
        
        // Check if service is available
        if (!isServiceAvailable()) {
            Log.e(TAG, "Firestore service is not available or not properly configured")
            throw IllegalStateException("Firestore service is not available")
        }
        
        // Initialize if not already initialized
        if (firestoreInstance == null) {
            try {
                // Get the default Firestore instance
                firestoreInstance = FirebaseFirestore.getInstance()
                
                // Configure Firestore settings
                val persistentCacheSettings = PersistentCacheSettings.newBuilder()
                    .setSizeBytes(104857600) // 100MB cache size (default)
                    .build()
                
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(persistentCacheSettings)  // Enable offline persistence
                    .build()
                
                // Apply the settings
                firestoreInstance!!.firestoreSettings = settings
                
                offlinePersistenceEnabled = true
                Log.d(TAG, "Firestore initialized with offline persistence enabled")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Firestore", e)
                throw IllegalStateException("Failed to initialize Firestore", e)
            }
        }
        
        return firestoreInstance!!
    }
    
    /**
     * Get a reference to a collection in Firestore
     * @param collectionPath The path to the collection
     * @return A CollectionReference pointing to the collection
     */
    override fun getCollection(collectionPath: String): CollectionReference {
        // Register API call
        registerApiCall("getCollection")
        
        try {
            return getFirestoreInstance().collection(collectionPath)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting collection for path: $collectionPath", e)
            throw IllegalStateException("Failed to get collection for path: $collectionPath", e)
        }
    }
    
    /**
     * Get a reference to a document in Firestore
     * @param documentPath The path to the document
     * @return A DocumentReference pointing to the document
     */
    override fun getDocument(documentPath: String): DocumentReference {
        // Register API call
        registerApiCall("getDocument")
        
        try {
            return getFirestoreInstance().document(documentPath)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting document for path: $documentPath", e)
            throw IllegalStateException("Failed to get document for path: $documentPath", e)
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
            Log.w(TAG, "Firestore version is not supported")
            return false
        }
        
        // Additional checks can be added here
        return true
    }
    
    /**
     * Get the current version of the Firestore SDK
     * @return The version string
     */
    override fun getServiceVersion(): String {
        // Register API call
        registerApiCall("getServiceVersion")
        
        // In a real implementation, this would get the actual Firestore SDK version
        // For now, we'll return a placeholder version
        return configService.getCurrentVersion(ConfigurationService.SERVICE_FIRESTORE)
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
     * Check if offline persistence is enabled
     * @return True if offline persistence is enabled, false otherwise
     */
    override fun isOfflinePersistenceEnabled(): Boolean {
        // Register API call
        registerApiCall("isOfflinePersistenceEnabled")
        
        return offlinePersistenceEnabled
    }
    
    /**
     * Check if the current version is supported
     * @return True if the version is supported, false otherwise
     */
    private fun isVersionSupported(): Boolean {
        val currentVersion = getServiceVersion()
        val minVersion = FirestoreService.MIN_SUPPORTED_VERSION
        
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
                serviceName = ConfigurationService.SERVICE_FIRESTORE,
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