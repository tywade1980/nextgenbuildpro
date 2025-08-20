package com.nextgenbuildpro.core.firestore

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Interface for Firestore repositories
 * Defines common operations that all Firestore repositories should implement
 * @param T The type of model this repository handles
 * @param ID The type of ID used for the model (usually String)
 */
interface FirestoreRepository<T, ID> {
    
    /**
     * Get all documents from the collection
     * @return Flow of list of models
     */
    fun getAll(): Flow<List<T>>
    
    /**
     * Get a document by ID
     * @param id The ID of the document
     * @return Flow of the model or null if not found
     */
    fun getById(id: ID): Flow<T?>
    
    /**
     * Add a new document to the collection
     * @param item The model to add
     * @return The ID of the new document
     */
    suspend fun add(item: T): ID
    
    /**
     * Update an existing document
     * @param id The ID of the document to update
     * @param item The updated model
     */
    suspend fun update(id: ID, item: T)
    
    /**
     * Delete a document by ID
     * @param id The ID of the document to delete
     */
    suspend fun delete(id: ID)
    
    /**
     * Convert a document snapshot to a model
     * @param document The document snapshot
     * @return The model
     */
    fun fromDocument(document: DocumentSnapshot): T
    
    /**
     * Convert a model to a map for Firestore
     * @param item The model
     * @return Map of field names to values
     */
    fun toMap(item: T): Map<String, Any?>
    
    /**
     * Convert a query snapshot to a list of models
     * @param snapshot The query snapshot
     * @return List of models
     */
    fun fromQuerySnapshot(snapshot: QuerySnapshot): List<T> {
        return snapshot.documents.mapNotNull { doc ->
            try {
                fromDocument(doc)
            } catch (e: Exception) {
                null
            }
        }
    }
}