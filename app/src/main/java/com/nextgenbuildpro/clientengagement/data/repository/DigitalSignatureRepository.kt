package com.nextgenbuildpro.clientengagement.data.repository

import android.content.Context
import com.nextgenbuildpro.clientengagement.data.model.DigitalSignature
import com.nextgenbuildpro.clientengagement.data.model.DocumentType
import com.nextgenbuildpro.clientengagement.data.model.SignableDocument
import com.nextgenbuildpro.clientengagement.data.model.SignatureField
import com.nextgenbuildpro.clientengagement.data.model.SignatureFieldType
import com.nextgenbuildpro.clientengagement.data.model.SignatureRequest
import com.nextgenbuildpro.clientengagement.data.model.SignatureRequestStatus
import com.nextgenbuildpro.core.Repository
import java.util.Calendar
import java.util.UUID

/**
 * Repository for managing digital signatures
 */
class DigitalSignatureRepository(private val context: Context) : Repository<DigitalSignature> {
    
    // In-memory storage for demo purposes
    private val signatures = mutableListOf<DigitalSignature>()
    private val signatureRequests = mutableListOf<SignatureRequest>()
    private val signableDocuments = mutableListOf<SignableDocument>()
    private val signatureFields = mutableListOf<SignatureField>()
    
    /**
     * Get all digital signatures
     */
    override suspend fun getAll(): List<DigitalSignature> {
        return signatures
    }
    
    /**
     * Get a digital signature by ID
     */
    override suspend fun getById(id: String): DigitalSignature? {
        return signatures.find { it.id == id }
    }
    
    /**
     * Save a new digital signature
     */
    override suspend fun save(item: DigitalSignature): Boolean {
        return try {
            signatures.add(item)
            
            // If this signature is for a signature request, update the request status
            val request = signatureRequests.find { it.documentId == item.documentId && it.requestedFrom == item.signedBy }
            if (request != null) {
                val updatedRequest = request.copy(
                    status = SignatureRequestStatus.COMPLETED,
                    completedAt = item.signedAt,
                    signatureId = item.id
                )
                
                val index = signatureRequests.indexOfFirst { it.id == request.id }
                if (index != -1) {
                    signatureRequests[index] = updatedRequest
                }
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Update an existing digital signature
     */
    override suspend fun update(item: DigitalSignature): Boolean {
        return try {
            val index = signatures.indexOfFirst { it.id == item.id }
            if (index != -1) {
                signatures[index] = item
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Delete a digital signature by ID
     */
    override suspend fun delete(id: String): Boolean {
        return try {
            signatures.removeIf { it.id == id }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get signatures for a document
     */
    suspend fun getSignaturesForDocument(documentId: String): List<DigitalSignature> {
        return signatures.filter { it.documentId == documentId }
    }
    
    /**
     * Get signatures by a specific signer
     */
    suspend fun getSignaturesBySigner(signedBy: String): List<DigitalSignature> {
        return signatures.filter { it.signedBy == signedBy }
    }
    
    /**
     * Verify a signature
     */
    suspend fun verifySignature(signatureId: String): Boolean {
        val signature = getById(signatureId) ?: return false
        
        // In a real implementation, this would verify the signature's authenticity
        // For now, we'll just mark it as valid
        if (!signature.isValid) {
            val validatedSignature = signature.copy(isValid = true)
            return update(validatedSignature)
        }
        
        return true
    }
    
    /**
     * Invalidate a signature
     */
    suspend fun invalidateSignature(signatureId: String, reason: String): Boolean {
        val signature = getById(signatureId) ?: return false
        
        val invalidatedSignature = signature.copy(isValid = false)
        return update(invalidatedSignature)
    }
    
    /**
     * Save a signature request
     */
    suspend fun saveSignatureRequest(request: SignatureRequest): Boolean {
        return try {
            // Set expiration date if not provided
            val finalRequest = if (request.expiresAt == null) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, 14) // Default to 14 days
                request.copy(expiresAt = calendar.timeInMillis)
            } else {
                request
            }
            
            signatureRequests.add(finalRequest)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get a signature request by ID
     */
    suspend fun getSignatureRequestById(id: String): SignatureRequest? {
        return signatureRequests.find { it.id == id }
    }
    
    /**
     * Get signature requests for a document
     */
    suspend fun getSignatureRequestsForDocument(documentId: String): List<SignatureRequest> {
        return signatureRequests.filter { it.documentId == documentId }
    }
    
    /**
     * Get signature requests for a recipient
     */
    suspend fun getSignatureRequestsForRecipient(recipientId: String): List<SignatureRequest> {
        return signatureRequests.filter { it.requestedFrom == recipientId }
    }
    
    /**
     * Get signature requests by status
     */
    suspend fun getSignatureRequestsByStatus(status: SignatureRequestStatus): List<SignatureRequest> {
        return signatureRequests.filter { it.status == status }
    }
    
    /**
     * Update a signature request
     */
    suspend fun updateSignatureRequest(request: SignatureRequest): Boolean {
        return try {
            val index = signatureRequests.indexOfFirst { it.id == request.id }
            if (index != -1) {
                signatureRequests[index] = request
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Cancel a signature request
     */
    suspend fun cancelSignatureRequest(requestId: String): Boolean {
        val request = getSignatureRequestById(requestId) ?: return false
        
        val cancelledRequest = request.copy(
            status = SignatureRequestStatus.CANCELLED,
            completedAt = System.currentTimeMillis()
        )
        
        return updateSignatureRequest(cancelledRequest)
    }
    
    /**
     * Send a reminder for a signature request
     */
    suspend fun sendReminder(requestId: String): Boolean {
        val request = getSignatureRequestById(requestId) ?: return false
        
        // Only send reminders for pending or viewed requests
        if (request.status != SignatureRequestStatus.PENDING && request.status != SignatureRequestStatus.VIEWED) {
            return false
        }
        
        val updatedRequest = request.copy(
            remindersSent = request.remindersSent + 1,
            lastReminderSent = System.currentTimeMillis()
        )
        
        return updateSignatureRequest(updatedRequest)
    }
    
    /**
     * Save a signable document
     */
    suspend fun saveSignableDocument(document: SignableDocument): Boolean {
        return try {
            signableDocuments.add(document)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get a signable document by ID
     */
    suspend fun getSignableDocumentById(id: String): SignableDocument? {
        return signableDocuments.find { it.id == id }
    }
    
    /**
     * Get signable documents by type
     */
    suspend fun getSignableDocumentsByType(documentType: DocumentType): List<SignableDocument> {
        return signableDocuments.filter { it.documentType == documentType }
    }
    
    /**
     * Get signable document templates
     */
    suspend fun getSignableDocumentTemplates(): List<SignableDocument> {
        return signableDocuments.filter { it.isTemplate }
    }
    
    /**
     * Update a signable document
     */
    suspend fun updateSignableDocument(document: SignableDocument): Boolean {
        return try {
            val index = signableDocuments.indexOfFirst { it.id == document.id }
            if (index != -1) {
                signableDocuments[index] = document
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Delete a signable document
     */
    suspend fun deleteSignableDocument(id: String): Boolean {
        return try {
            val removed = signableDocuments.removeIf { it.id == id }
            
            // Also remove related signature fields
            if (removed) {
                signatureFields.removeIf { it.documentId == id }
            }
            
            removed
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Add a signature field to a document
     */
    suspend fun addSignatureField(field: SignatureField): Boolean {
        return try {
            signatureFields.add(field)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get signature fields for a document
     */
    suspend fun getSignatureFields(documentId: String): List<SignatureField> {
        return signatureFields.filter { it.documentId == documentId }
    }
    
    /**
     * Update a signature field
     */
    suspend fun updateSignatureField(field: SignatureField): Boolean {
        return try {
            val index = signatureFields.indexOfFirst { it.id == field.id }
            if (index != -1) {
                signatureFields[index] = field
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Delete a signature field
     */
    suspend fun deleteSignatureField(id: String): Boolean {
        return try {
            signatureFields.removeIf { it.id == id }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Create a document from a template
     */
    suspend fun createDocumentFromTemplate(templateId: String, title: String, createdBy: String): String? {
        val template = getSignableDocumentById(templateId) ?: return null
        
        if (!template.isTemplate) {
            return null
        }
        
        // Create a new document based on the template
        val newDocumentId = UUID.randomUUID().toString()
        val newDocument = template.copy(
            id = newDocumentId,
            title = title,
            createdBy = createdBy,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isTemplate = false
        )
        
        // Save the new document
        if (!saveSignableDocument(newDocument)) {
            return null
        }
        
        // Copy signature fields from the template
        val templateFields = getSignatureFields(templateId)
        for (field in templateFields) {
            val newField = field.copy(
                id = UUID.randomUUID().toString(),
                documentId = newDocumentId
            )
            
            if (!addSignatureField(newField)) {
                // If we fail to add a field, delete the document and return null
                deleteSignableDocument(newDocumentId)
                return null
            }
        }
        
        return newDocumentId
    }
}