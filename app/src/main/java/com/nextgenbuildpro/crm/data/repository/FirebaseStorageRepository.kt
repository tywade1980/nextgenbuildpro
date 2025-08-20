package com.nextgenbuildpro.crm.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.nextgenbuildpro.crm.data.model.MediaAttachment
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

/**
 * Repository for managing file uploads and downloads with Firebase Storage
 * Uses the bucket path: gs://nextgenbuildpro.firebasestorage.app
 */
class FirebaseStorageRepository {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    
    // Root folders for different modules
    private val leadsFolder = "leads"
    private val estimatesFolder = "estimates"
    private val projectsFolder = "projects"
    private val clientsFolder = "clients"
    private val tasksFolder = "tasks"
    
    /**
     * Upload a file to Firebase Storage in the appropriate folder structure
     * @param file The local file to upload
     * @param module The module this file belongs to (e.g., "leads", "estimates")
     * @param entityId The ID of the entity this file is associated with (e.g., leadId, estimateId)
     * @param fileType The type of file (e.g., "photos", "documents", "audio")
     * @param fileName Optional custom file name. If null, the original file name will be used
     * @return A MediaAttachment object with the file details
     */
    suspend fun uploadFile(
        file: File,
        module: String,
        entityId: String,
        fileType: String,
        fileName: String? = null
    ): MediaAttachment {
        try {
            // Create folder structure: module/entityId/fileType/
            val folderPath = "$module/$entityId/$fileType/"
            
            // Use original filename or generate a new one if not provided
            val actualFileName = fileName ?: "${UUID.randomUUID()}_${file.name}"
            
            // Get reference to the file location in Firebase Storage
            val fileRef = storageRef.child("$folderPath$actualFileName")
            
            // Upload the file
            fileRef.putFile(Uri.fromFile(file)).await()
            
            // Get the download URL
            val downloadUrl = fileRef.downloadUrl.await().toString()
            
            // Create and return a MediaAttachment object
            return MediaAttachment(
                id = UUID.randomUUID().toString(),
                leadId = entityId, // This assumes the entity is a lead
                name = actualFileName,
                type = getMimeType(file),
                url = downloadUrl,
                thumbnailUrl = if (isImageFile(file)) downloadUrl else null,
                size = file.length(),
                uploadedAt = System.currentTimeMillis(),
                description = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading file", e)
            throw e
        }
    }
    
    /**
     * Upload a file specifically for a lead
     * @param file The local file to upload
     * @param leadId The ID of the lead
     * @param fileType The type of file (e.g., "photos", "documents", "audio")
     * @param fileName Optional custom file name
     * @return A MediaAttachment object with the file details
     */
    suspend fun uploadLeadFile(
        file: File,
        leadId: String,
        fileType: String,
        fileName: String? = null
    ): MediaAttachment {
        return uploadFile(file, leadsFolder, leadId, fileType, fileName)
    }
    
    /**
     * Upload a file specifically for an estimate
     * @param file The local file to upload
     * @param estimateId The ID of the estimate
     * @param fileType The type of file (e.g., "photos", "documents", "audio")
     * @param fileName Optional custom file name
     * @return A MediaAttachment object with the file details
     */
    suspend fun uploadEstimateFile(
        file: File,
        estimateId: String,
        fileType: String,
        fileName: String? = null
    ): MediaAttachment {
        return uploadFile(file, estimatesFolder, estimateId, fileType, fileName)
    }
    
    /**
     * Upload a file specifically for a project
     * @param file The local file to upload
     * @param projectId The ID of the project
     * @param fileType The type of file (e.g., "photos", "documents", "audio")
     * @param fileName Optional custom file name
     * @return A MediaAttachment object with the file details
     */
    suspend fun uploadProjectFile(
        file: File,
        projectId: String,
        fileType: String,
        fileName: String? = null
    ): MediaAttachment {
        return uploadFile(file, projectsFolder, projectId, fileType, fileName)
    }
    
    /**
     * Download a file from Firebase Storage
     * @param mediaAttachment The MediaAttachment object containing the file details
     * @param destinationFile The local file to save the downloaded content to
     * @return True if download was successful, false otherwise
     */
    suspend fun downloadFile(mediaAttachment: MediaAttachment, destinationFile: File): Boolean {
        return try {
            // Get reference to the file in Firebase Storage from its URL
            val fileRef = storage.getReferenceFromUrl(mediaAttachment.url)
            
            // Download the file to the local destination
            fileRef.getFile(destinationFile).await()
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file", e)
            false
        }
    }
    
    /**
     * Delete a file from Firebase Storage
     * @param mediaAttachment The MediaAttachment object containing the file details
     * @return True if deletion was successful, false otherwise
     */
    suspend fun deleteFile(mediaAttachment: MediaAttachment): Boolean {
        return try {
            // Get reference to the file in Firebase Storage from its URL
            val fileRef = storage.getReferenceFromUrl(mediaAttachment.url)
            
            // Delete the file
            fileRef.delete().await()
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file", e)
            false
        }
    }
    
    /**
     * List all files in a specific folder
     * @param module The module to list files from (e.g., "leads", "estimates")
     * @param entityId The ID of the entity (e.g., leadId, estimateId)
     * @param fileType Optional file type filter (e.g., "photos", "documents")
     * @return List of StorageReference objects
     */
    suspend fun listFiles(
        module: String,
        entityId: String,
        fileType: String? = null
    ): List<StorageReference> {
        val path = if (fileType != null) {
            "$module/$entityId/$fileType"
        } else {
            "$module/$entityId"
        }
        
        val folderRef = storageRef.child(path)
        return folderRef.listAll().await().items
    }
    
    /**
     * Get all media attachments for a specific entity
     * @param module The module to get attachments from (e.g., "leads", "estimates")
     * @param entityId The ID of the entity (e.g., leadId, estimateId)
     * @return List of MediaAttachment objects
     */
    suspend fun getMediaAttachments(module: String, entityId: String): List<MediaAttachment> {
        val result = mutableListOf<MediaAttachment>()
        
        try {
            // Get all file types for this entity
            val folderRef = storageRef.child("$module/$entityId")
            val fileTypes = folderRef.listAll().await().prefixes
            
            // For each file type, get all files
            for (fileTypeRef in fileTypes) {
                fileTypeRef.name
                val files = fileTypeRef.listAll().await().items
                
                // For each file, create a MediaAttachment
                for (fileRef in files) {
                    val downloadUrl = fileRef.downloadUrl.await().toString()
                    val metadata = fileRef.metadata.await()
                    
                    result.add(
                        MediaAttachment(
                            id = UUID.randomUUID().toString(), // Generate a new ID
                            leadId = entityId, // This assumes the entity is a lead
                            name = fileRef.name,
                            type = metadata.contentType ?: "application/octet-stream",
                            url = downloadUrl,
                            thumbnailUrl = if (isImageType(metadata.contentType)) downloadUrl else null,
                            size = metadata.sizeBytes,
                            uploadedAt = metadata.creationTimeMillis,
                            description = null
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting media attachments", e)
        }
        
        return result
    }
    
    /**
     * Get all media attachments for a lead
     * @param leadId The ID of the lead
     * @return List of MediaAttachment objects
     */
    suspend fun getLeadMediaAttachments(leadId: String): List<MediaAttachment> {
        return getMediaAttachments(leadsFolder, leadId)
    }
    
    /**
     * Get all media attachments for an estimate
     * @param estimateId The ID of the estimate
     * @return List of MediaAttachment objects
     */
    suspend fun getEstimateMediaAttachments(estimateId: String): List<MediaAttachment> {
        return getMediaAttachments(estimatesFolder, estimateId)
    }
    
    /**
     * Get all media attachments for a project
     * @param projectId The ID of the project
     * @return List of MediaAttachment objects
     */
    suspend fun getProjectMediaAttachments(projectId: String): List<MediaAttachment> {
        return getMediaAttachments(projectsFolder, projectId)
    }
    
    /**
     * Determine the MIME type of a file
     * @param file The file to check
     * @return The MIME type as a string
     */
    private fun getMimeType(file: File): String {
        val name = file.name.lowercase()
        return when {
            name.endsWith(".jpg") || name.endsWith(".jpeg") -> "image/jpeg"
            name.endsWith(".png") -> "image/png"
            name.endsWith(".gif") -> "image/gif"
            name.endsWith(".pdf") -> "application/pdf"
            name.endsWith(".doc") || name.endsWith(".docx") -> "application/msword"
            name.endsWith(".xls") || name.endsWith(".xlsx") -> "application/vnd.ms-excel"
            name.endsWith(".txt") -> "text/plain"
            name.endsWith(".mp3") -> "audio/mpeg"
            name.endsWith(".mp4") -> "video/mp4"
            else -> "application/octet-stream"
        }
    }
    
    /**
     * Check if a file is an image based on its extension
     * @param file The file to check
     * @return True if the file is an image, false otherwise
     */
    private fun isImageFile(file: File): Boolean {
        val name = file.name.lowercase()
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
               name.endsWith(".png") || name.endsWith(".gif") ||
               name.endsWith(".bmp") || name.endsWith(".webp")
    }
    
    /**
     * Check if a MIME type is an image type
     * @param mimeType The MIME type to check
     * @return True if the MIME type is an image type, false otherwise
     */
    private fun isImageType(mimeType: String?): Boolean {
        return mimeType?.startsWith("image/") ?: false
    }
    
    companion object {
        private const val TAG = "FirebaseStorageRepo"
    }
}