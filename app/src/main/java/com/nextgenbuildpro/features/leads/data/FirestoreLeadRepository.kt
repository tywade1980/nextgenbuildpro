package com.nextgenbuildpro.features.leads.data

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nextgenbuildpro.core.firestore.BaseFirestoreRepository
import com.nextgenbuildpro.core.firestore.FirebaseFirestoreInitializer
import com.nextgenbuildpro.core.firestore.FirestoreCollectionNames
import com.nextgenbuildpro.core.firestore.FirestoreDocumentConverter
import com.nextgenbuildpro.core.storage.FirebaseStorageRepository
import com.nextgenbuildpro.features.leads.domain.Lead
import com.nextgenbuildpro.features.leads.domain.LeadRepository
import com.nextgenbuildpro.features.leads.domain.LeadStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

/**
 * Implementation of LeadRepository that uses Firestore for storage
 */
class FirestoreLeadRepository(
    firestore: FirebaseFirestore = FirebaseFirestoreInitializer.firestore,
    private val storageRepository: FirebaseStorageRepository = FirebaseStorageRepository()
) : BaseFirestoreRepository<Lead, String>(firestore), LeadRepository {
    
    companion object {
        private const val TAG = "FirestoreLeadRepo"
    }
    
    /**
     * Get the collection reference for leads
     */
    override fun getCollectionReference(): CollectionReference {
        return firestore.collection(FirestoreCollectionNames.LEADS)
    }
    
    /**
     * Get the default query for leads
     * Orders leads by updatedAt date (descending)
     */
    override fun getDefaultQuery(): Query {
        return getCollectionReference().orderBy("updatedAt", Query.Direction.DESCENDING)
    }
    
    /**
     * Convert a string to a lead ID
     */
    override fun stringToId(idString: String): String {
        return idString
    }
    
    /**
     * Convert a document snapshot to a Lead model
     */
    override fun fromDocument(document: DocumentSnapshot): Lead {
        return Lead(
            id = document.id,
            name = FirestoreDocumentConverter.getString(document, "name"),
            phone = FirestoreDocumentConverter.getString(document, "phone"),
            email = FirestoreDocumentConverter.getString(document, "email"),
            address = FirestoreDocumentConverter.getString(document, "address"),
            notes = FirestoreDocumentConverter.getString(document, "notes"),
            status = LeadStatus.valueOf(
                FirestoreDocumentConverter.getString(document, "status", LeadStatus.NEW.name)
            ),
            createdAt = FirestoreDocumentConverter.getDate(document, "createdAt", Date()),
            updatedAt = FirestoreDocumentConverter.getDate(document, "updatedAt", Date()),
            photoUrls = FirestoreDocumentConverter.getStringList(document, "photoUrls"),
            source = FirestoreDocumentConverter.getString(document, "source"),
            assignedTo = FirestoreDocumentConverter.getString(document, "assignedTo")
        )
    }
    
    /**
     * Convert a Lead model to a map for Firestore
     */
    override fun toMap(item: Lead): Map<String, Any?> {
        return mapOf(
            "id" to item.id,
            "name" to item.name,
            "phone" to item.phone,
            "email" to item.email,
            "address" to item.address,
            "notes" to item.notes,
            "status" to item.status.name,
            "createdAt" to FirestoreDocumentConverter.dateToTimestamp(item.createdAt),
            "updatedAt" to FirestoreDocumentConverter.dateToTimestamp(item.updatedAt ?: Date()),
            "photoUrls" to item.photoUrls,
            "source" to item.source,
            "assignedTo" to item.assignedTo
        )
    }
    
    /**
     * Get all leads
     */
    override fun getLeads(): Flow<List<Lead>> {
        return getAll()
    }
    
    /**
     * Get a lead by ID
     */
    override fun getLead(id: String): Flow<Lead?> {
        return getById(id)
    }
    
    /**
     * Get leads by status
     */
    override fun getLeadsByStatus(status: LeadStatus): Flow<List<Lead>> = callbackFlow {
        val listenerRegistration = getCollectionReference()
            .whereEqualTo("status", status.name)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting leads by status", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val leads = fromQuerySnapshot(snapshot)
                    trySend(leads)
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    /**
     * Save a lead
     */
    override suspend fun saveLead(lead: Lead): String {
        val leadToSave = lead.copy(updatedAt = Date())
        return if (lead.id.isBlank()) {
            // Create new lead with a generated ID
            val newLead = leadToSave.copy(
                id = UUID.randomUUID().toString(),
                createdAt = Date()
            )
            add(newLead)
        } else {
            // Update existing lead
            update(lead.id, leadToSave)
            lead.id
        }
    }
    
    /**
     * Delete a lead
     */
    override suspend fun deleteLead(id: String) {
        delete(id)
    }
    
    /**
     * Upload a photo for a lead
     */
    override suspend fun uploadLeadPhoto(leadId: String, photoUri: Uri): String {
        return storageRepository.uploadFile(
            uri = photoUri,
            module = FirestoreCollectionNames.LEADS,
            id = leadId,
            contentType = "image/jpeg"
        )
    }
    
    /**
     * Update lead status
     */
    override suspend fun updateLeadStatus(id: String, status: LeadStatus) {
        val docRef = getCollectionReference().document(id)
        docRef.update(
            mapOf(
                "status" to status.name,
                "updatedAt" to FirestoreDocumentConverter.dateToTimestamp(Date())
            )
        ).await()
    }
}