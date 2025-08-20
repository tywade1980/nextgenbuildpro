package com.nextgenbuildpro.core.api

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query

/**
 * Interface for Firestore service operations.
 * 
 * This is a delicate API that abstracts Firestore operations.
 * It provides a layer of abstraction to make it easier to:
 * 1. Swap implementations if the underlying API changes
 * 2. Mock for testing
 * 3. Add version checking and feature flagging
 * 4. Implement robust error handling
 */
interface FirestoreService {
    /**
     * Get the Firestore instance
     * @return The Firestore instance
     */
    fun getFirestoreInstance(): FirebaseFirestore
    
    /**
     * Get a reference to a collection in Firestore
     * @param collectionPath The path to the collection
     * @return A CollectionReference pointing to the collection
     */
    fun getCollection(collectionPath: String): CollectionReference
    
    /**
     * Get a reference to a document in Firestore
     * @param documentPath The path to the document
     * @return A DocumentReference pointing to the document
     */
    fun getDocument(documentPath: String): DocumentReference
    
    /**
     * Check if the service is available and properly configured
     * @return True if the service is available, false otherwise
     */
    fun isServiceAvailable(): Boolean
    
    /**
     * Get the current version of the Firestore SDK
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
     * Check if offline persistence is enabled
     * @return True if offline persistence is enabled, false otherwise
     */
    fun isOfflinePersistenceEnabled(): Boolean
    
    companion object {
        /**
         * Minimum supported version of Firestore SDK
         */
        const val MIN_SUPPORTED_VERSION = "24.0.0"
        
        /**
         * Feature flag for transactions
         */
        const val FEATURE_TRANSACTIONS = "transactions"
        
        /**
         * Feature flag for batch operations
         */
        const val FEATURE_BATCH_OPERATIONS = "batch_operations"
        
        /**
         * Feature flag for real-time updates
         */
        const val FEATURE_REAL_TIME_UPDATES = "real_time_updates"
    }
}