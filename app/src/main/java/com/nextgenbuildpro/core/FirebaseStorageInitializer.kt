package com.nextgenbuildpro.core

import android.util.Log
import com.google.firebase.storage.FirebaseStorage

/**
 * Utility class to initialize Firebase Storage with the correct bucket URL
 */
object FirebaseStorageInitializer {
    private const val TAG = "FirebaseStorageInit"
    private const val DEFAULT_FIREBASE_STORAGE_URL = "gs://nextgenbuildpro.firebasestorage.app"

    /**
     * Initialize Firebase Storage with the correct bucket URL
     * This should be called early in the application lifecycle, such as in Application.onCreate()
     * @return The initialized FirebaseStorage instance
     */
    fun initialize(): FirebaseStorage {
        // Get the default instance first
        val storage = FirebaseStorage.getInstance()

        // Get the storage bucket URL from ApiKeyManager or use the default
        val bucketUrl = ApiKeyManager.getFirebaseStorageUrl() ?: DEFAULT_FIREBASE_STORAGE_URL

        if (ApiKeyManager.getFirebaseStorageUrl() == null) {
            Log.w(TAG, "Firebase Storage URL not found in ApiKeyManager, using default URL")
        }

        // Set the storage bucket URL
        try {
            // For custom URL, we can use the following approach
            // Note: In most cases, Firebase automatically uses the correct bucket from google-services.json
            // This is only needed if you want to explicitly set a different bucket
            return FirebaseStorage.getInstance(bucketUrl)
        } catch (e: Exception) {
            // If there's an error, log it and return the default instance
            Log.e(TAG, "Error initializing with custom URL: $bucketUrl", e)
            return storage
        }
    }

    /**
     * Get the Firebase Storage bucket URL
     * @return The Firebase Storage bucket URL from ApiKeyManager or the default URL
     */
    fun getBucketUrl(): String {
        return ApiKeyManager.getFirebaseStorageUrl() ?: DEFAULT_FIREBASE_STORAGE_URL
    }
}
