package com.nextgenbuildpro.clientengagement.service

import android.content.Context
import android.net.Uri
import android.util.Log
import com.nextgenbuildpro.clientengagement.data.model.DocumentType
import com.nextgenbuildpro.clientengagement.data.model.SignableDocument
import com.nextgenbuildpro.clientengagement.data.model.SignatureRequest
import com.nextgenbuildpro.clientengagement.data.repository.DigitalSignatureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Service for handling document uploads and signature requests
 */
class DocumentUploadService(
    private val context: Context,
    private val signatureRepository: DigitalSignatureRepository
) {
    private val TAG = "DocumentUploadService"
    
    /**
     * Upload a document and create a signable document
     * 
     * @param uri The URI of the document to upload
     * @param title The title of the document
     * @param description The description of the document
     * @param documentType The type of document
     * @param createdBy The ID of the user who created the document
     * @return The ID of the created signable document, or null if the upload failed
     */
    suspend fun uploadDocument(
        uri: Uri,
        title: String,
        description: String?,
        documentType: DocumentType,
        createdBy: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            // In a real app, we would upload the file to cloud storage
            // For this example, we'll copy it to the app's files directory
            val documentUrl = copyFileToAppStorage(uri, documentType.name.lowercase())
            
            if (documentUrl != null) {
                // Create a signable document
                val document = SignableDocument(
                    title = title,
                    description = description,
                    documentUrl = documentUrl,
                    documentType = documentType,
                    createdBy = createdBy,
                    signatureFields = emptyList()
                )
                
                // Save the document
                val success = signatureRepository.saveSignableDocument(document)
                
                if (success) {
                    Log.d(TAG, "Document uploaded successfully: ${document.id}")
                    return@withContext document.id
                }
            }
            
            Log.e(TAG, "Failed to upload document")
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading document", e)
            return@withContext null
        }
    }
    
    /**
     * Create a signature request for a document
     * 
     * @param documentId The ID of the document to request a signature for
     * @param documentType The type of document
     * @param requestedBy The ID of the user requesting the signature
     * @param requestedFrom The ID of the user who should sign the document
     * @param message An optional message to include with the request
     * @param expiresInDays The number of days until the request expires, or null for no expiration
     * @return The ID of the created signature request, or null if the creation failed
     */
    suspend fun createSignatureRequest(
        documentId: String,
        documentType: DocumentType,
        requestedBy: String,
        requestedFrom: String,
        message: String? = null,
        expiresInDays: Int? = 14
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Calculate expiration time
            val expiresAt = if (expiresInDays != null) {
                System.currentTimeMillis() + (expiresInDays * 24 * 60 * 60 * 1000L)
            } else {
                null
            }
            
            // Create a signature request
            val request = SignatureRequest(
                documentId = documentId,
                documentType = documentType,
                requestedBy = requestedBy,
                requestedFrom = requestedFrom,
                expiresAt = expiresAt,
                message = message
            )
            
            // Save the request
            val success = signatureRepository.saveSignatureRequest(request)
            
            if (success) {
                Log.d(TAG, "Signature request created successfully: ${request.id}")
                
                // In a real app, we would send a notification to the recipient
                sendSignatureRequestNotification(request)
                
                return@withContext request.id
            }
            
            Log.e(TAG, "Failed to create signature request")
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error creating signature request", e)
            return@withContext null
        }
    }
    
    /**
     * Send a notification for a signature request
     * 
     * @param request The signature request to send a notification for
     */
    private fun sendSignatureRequestNotification(request: SignatureRequest) {
        // In a real app, we would send a push notification, email, or SMS
        // For this example, we'll just log a message
        Log.d(TAG, "Sending notification for signature request: ${request.id}")
        Log.d(TAG, "To: ${request.requestedFrom}")
        Log.d(TAG, "From: ${request.requestedBy}")
        Log.d(TAG, "Document ID: ${request.documentId}")
        Log.d(TAG, "Document Type: ${request.documentType}")
        Log.d(TAG, "Message: ${request.message ?: "No message"}")
    }
    
    /**
     * Copy a file from a URI to the app's files directory
     * 
     * @param uri The URI of the file to copy
     * @param subdirectory The subdirectory to copy the file to
     * @return The path to the copied file, or null if the copy failed
     */
    private fun copyFileToAppStorage(uri: Uri, subdirectory: String): String? {
        try {
            // Create the subdirectory if it doesn't exist
            val directory = File(context.filesDir, subdirectory)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            // Generate a unique filename
            val filename = "${UUID.randomUUID()}.pdf"
            val destinationFile = File(directory, filename)
            
            // Copy the file
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Return the path to the copied file
            return destinationFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error copying file to app storage", e)
            return null
        }
    }
    
    /**
     * Get the file for a document URL
     * 
     * @param documentUrl The URL of the document
     * @return The file for the document, or null if the file doesn't exist
     */
    fun getDocumentFile(documentUrl: String): File? {
        val file = File(documentUrl)
        return if (file.exists()) file else null
    }
    
    companion object {
        /**
         * Create a DocumentUploadService
         * 
         * @param context The context to use
         * @param signatureRepository The repository to use for storing documents and signature requests
         * @return A new DocumentUploadService
         */
        fun create(
            context: Context,
            signatureRepository: DigitalSignatureRepository
        ): DocumentUploadService {
            return DocumentUploadService(context, signatureRepository)
        }
    }
}