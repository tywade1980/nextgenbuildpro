package com.nextgenbuildpro.core.api

/**
 * Interface for managing API configuration, including feature flags and version information.
 * 
 * This is a delicate API that abstracts configuration management.
 * It provides a layer of abstraction to make it easier to:
 * 1. Swap configuration sources (local, remote, etc.)
 * 2. Implement feature flagging
 * 3. Track configuration versions
 * 4. Audit configuration changes
 */
interface ConfigurationService {
    /**
     * Get a feature flag value
     * @param featureName The name of the feature
     * @param defaultValue The default value to return if the feature flag is not found
     * @return The feature flag value
     */
    fun isFeatureEnabled(featureName: String, defaultValue: Boolean = false): Boolean
    
    /**
     * Get a configuration value
     * @param key The configuration key
     * @param defaultValue The default value to return if the configuration is not found
     * @return The configuration value
     */
    fun getConfigValue(key: String, defaultValue: String? = null): String?
    
    /**
     * Get the minimum supported version for a service
     * @param serviceName The name of the service
     * @return The minimum supported version
     */
    fun getMinSupportedVersion(serviceName: String): String
    
    /**
     * Get the current version of a service
     * @param serviceName The name of the service
     * @return The current version
     */
    fun getCurrentVersion(serviceName: String): String
    
    /**
     * Check if a service version is supported
     * @param serviceName The name of the service
     * @param version The version to check
     * @return True if the version is supported, false otherwise
     */
    fun isVersionSupported(serviceName: String, version: String): Boolean
    
    /**
     * Refresh configuration from the source
     * @return True if the refresh was successful, false otherwise
     */
    fun refreshConfiguration(): Boolean
    
    /**
     * Log a configuration change for audit purposes
     * @param key The configuration key
     * @param oldValue The old value
     * @param newValue The new value
     * @param source The source of the change (user, system, etc.)
     */
    fun logConfigurationChange(key: String, oldValue: String?, newValue: String?, source: String)
    
    companion object {
        /**
         * Service name for Firebase Storage
         */
        const val SERVICE_FIREBASE_STORAGE = "firebase_storage"
        
        /**
         * Service name for Firestore
         */
        const val SERVICE_FIRESTORE = "firestore"
        
        /**
         * Service name for Google APIs
         */
        const val SERVICE_GOOGLE_APIS = "google_apis"
    }
}