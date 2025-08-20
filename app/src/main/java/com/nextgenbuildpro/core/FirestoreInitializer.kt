package com.nextgenbuildpro.core

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Utility class to initialize Firestore with the correct settings
 */
object FirestoreInitializer {
    private const val TAG = "FirestoreInitializer"

    /**
     * Initialize Firestore with test mode settings
     * This should be called early in the application lifecycle, such as in Application.onCreate()
     * @return The initialized FirebaseFirestore instance
     */
    fun initialize(): FirebaseFirestore {
        try {
            // Get the default Firestore instance
            val firestore = FirebaseFirestore.getInstance()

            // Configure Firestore settings for test mode
            // Create persistent cache settings (replaces deprecated setPersistenceEnabled)
            val persistentCacheSettings = PersistentCacheSettings.newBuilder()
                .setSizeBytes(104857600) // 100MB cache size (default)
                .build()

            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(persistentCacheSettings)  // Enable offline persistence
                .build()

            // Apply the settings
            firestore.firestoreSettings = settings

            Log.d(TAG, "Firestore initialized in test mode")
            return firestore
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firestore", e)
            throw e
        }
    }
}
