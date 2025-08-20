package com.nextgenbuildpro.core.firestore

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nextgenbuildpro.core.FirestoreInitializer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Base implementation of FirestoreRepository
 * Provides common functionality for all Firestore repositories
 * @param T The type of model this repository handles
 * @param ID The type of ID used for the model (usually String)
 */
abstract class BaseFirestoreRepository<T, ID>(
    protected val firestore: FirebaseFirestore = FirebaseFirestoreInitializer.firestore
) : FirestoreRepository<T, ID> {

    companion object {
        private const val TAG = "BaseFirestoreRepo"
    }

    /**
     * Get the collection reference for this repository
     * @return The collection reference
     */
    protected abstract fun getCollectionReference(): CollectionReference

    /**
     * Get the default query for this repository
     * Override this method to provide a custom query (e.g., sorting, filtering)
     * @return The default query
     */
    protected open fun getDefaultQuery(): Query {
        return getCollectionReference()
    }

    /**
     * Get the ID field name for this repository
     * Override this method if the ID field name is not "id"
     * @return The ID field name
     */
    protected open fun getIdFieldName(): String {
        return "id"
    }

    /**
     * Convert an ID to a string
     * @param id The ID
     * @return The ID as a string
     */
    protected open fun idToString(id: ID): String {
        return id.toString()
    }

    /**
     * Convert a string to an ID
     * @param idString The ID as a string
     * @return The ID
     */
    protected abstract fun stringToId(idString: String): ID

    /**
     * Get all documents from the collection
     * @return Flow of list of models
     */
    override fun getAll(): Flow<List<T>> = callbackFlow {
        val listenerRegistration = getDefaultQuery().addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error getting documents", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val items = fromQuerySnapshot(snapshot)
                trySend(items)
            }
        }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Get a document by ID
     * @param id The ID of the document
     * @return Flow of the model or null if not found
     */
    override fun getById(id: ID): Flow<T?> = callbackFlow {
        val listenerRegistration = getCollectionReference().document(idToString(id))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting document", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val item = fromDocument(snapshot)
                        trySend(item)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document", e)
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Add a new document to the collection
     * @param item The model to add
     * @return The ID of the new document
     */
    override suspend fun add(item: T): ID {
        val docRef = getCollectionReference().document()
        val data = toMap(item).toMutableMap()

        // Add the document ID to the data if it's not already there
        if (!data.containsKey(getIdFieldName())) {
            data[getIdFieldName()] = docRef.id
        }

        docRef.set(data).await()
        return stringToId(docRef.id)
    }

    /**
     * Update an existing document
     * @param id The ID of the document to update
     * @param item The updated model
     */
    override suspend fun update(id: ID, item: T) {
        val docRef = getCollectionReference().document(idToString(id))
        val data = toMap(item)
        docRef.update(data).await()
    }

    /**
     * Delete a document by ID
     * @param id The ID of the document to delete
     */
    override suspend fun delete(id: ID) {
        val docRef = getCollectionReference().document(idToString(id))
        docRef.delete().await()
    }
}

/**
 * Firestore initializer object
 * Provides access to the Firestore instance
 */
object FirebaseFirestoreInitializer {
    val firestore: FirebaseFirestore by lazy {
        FirestoreInitializer.initialize()
    }
}
