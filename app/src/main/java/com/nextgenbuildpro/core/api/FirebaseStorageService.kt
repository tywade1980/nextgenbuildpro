package com.nextgenbuildpro.core.api

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

/**
 * Interface for Firebase Storage service operations.
 * 
 * This is a delicate API that abstracts Firebase Storage operations.
 * It provides a layer of abstraction to make it easier to:
 * 1. Swap implementations if the underlying API changes
 * 2. Mock for testing
 * 3. Add version checking and feature flagging
 * 4. Implement robust error handling
 */
interface FirebaseStorageService {
    /**
     * Get the Firebase Storage instance
     * @return The Firebase Storage instance
     */
    fun getStorageInstance(): FirebaseStorage
    
    /**
     * Get a reference to a file in Firebase Storage
     * @param path The path to the file
     * @return A StorageReference pointing to the file
     */
    fun getReference(path: String): StorageReference
    
    /**
     * Check if the service is available and properly configured
     * @return True if the service is available, false otherwise
     */
    fun isServiceAvailable(): Boolean
    
    /**
     * Get the current version of the Firebase Storage SDK
     * @return The version string
     */
    fun getServiceVersion(): String
    
    /**
     * Check if a specific feature is enabled
     * @param featureName The name of the feature to check
     * @return True if the feature is enabled, false otherwise
     */
    fun isFeatureEnabled(featureName: String): Boolean
    
    /**
     * Get the Firebase Storage bucket URL
     * @return The bucket URL
     */
    fun getBucketUrl(): String
    
    companion object {
        /**
         * Minimum supported version of Firebase Storage SDK
         */
        const val MIN_SUPPORTED_VERSION = "24.0.0"
        
        /**
         * Feature flag for large file uploads
         */
        const val FEATURE_LARGE_FILE_UPLOADS = "large_file_uploads"
        
        /**
         * Feature flag for file metadata
         */
        const val FEATURE_FILE_METADATA = "file_metadata"
    }
}