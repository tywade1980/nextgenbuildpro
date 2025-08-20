package com.nextgenbuildpro.core.storage

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.nextgenbuildpro.core.FirebaseStorageInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.UUID

/**
 * Repository for handling Firebase Storage operations
 */
class FirebaseStorageRepository {
    
    // Firebase Storage instance
    private val storage: FirebaseStorage = FirebaseStorageInitializer.initialize()
    
    // Root reference to the storage bucket
    private val storageRef: StorageReference = storage.reference
    
    // Module folders
    private val leadsFolder = "leads"
    private val estimatesFolder = "estimates"
    private val projectsFolder = "projects"
    private val clientsFolder = "clients"
    private val tasksFolder = "tasks"
    
    /**
     * Upload a file to Firebase Storage
     * 
     * @param uri The URI of the file to upload
     * @param module The module folder to upload to (leads, estimates, projects, clients, tasks)
     * @param id The ID of the entity (lead ID, estimate ID, etc.)
     * @param fileName The name of the file (optional, will be generated if not provided)
     * @param contentType The content type of the file (optional)
     * @param onProgress Callback for upload progress (optional)
     * @return The download URL of the uploaded file
     */
    suspend fun uploadFile(
        uri: Uri,
        module: String,
        id: String,
        fileName: String? = null,
        contentType: String? = null,
        onProgress: ((progress: Double) -> Unit)? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            // Create a reference to the file location
            val fileRef = getModuleReference(module, id)
                .child(fileName ?: "${UUID.randomUUID()}_${uri.lastPathSegment}")
            
            // Create metadata if content type is provided
            val metadata = contentType?.let {
                StorageMetadata.Builder()
                    .setContentType(it)
                    .build()
            }
            
            // Start upload task
            val uploadTask = if (metadata != null) {
                fileRef.putFile(uri, metadata)
            } else {
                fileRef.putFile(uri)
            }
            
            // Add progress listener if callback is provided
            onProgress?.let { callback ->
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    callback(progress)
                }
            }
            
            // Wait for upload to complete
            uploadTask.await()
            
            // Return the download URL
            return@withContext fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading file", e)
            throw e
        }
    }
    
    /**
     * Upload a file from an input stream
     * 
     * @param inputStream The input stream of the file
     * @param module The module folder to upload to
     * @param id The ID of the entity
     * @param fileName The name of the file
     * @param contentType The content type of the file (optional)
     * @param onProgress Callback for upload progress (optional)
     * @return The download URL of the uploaded file
     */
    suspend fun uploadFile(
        inputStream: InputStream,
        module: String,
        id: String,
        fileName: String,
        contentType: String? = null,
        onProgress: ((progress: Double) -> Unit)? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            // Create a reference to the file location
            val fileRef = getModuleReference(module, id).child(fileName)
            
            // Create metadata if content type is provided
            val metadata = contentType?.let {
                StorageMetadata.Builder()
                    .setContentType(it)
                    .build()
            }
            
            // Start upload task
            val uploadTask = if (metadata != null) {
                fileRef.putStream(inputStream, metadata)
            } else {
                fileRef.putStream(inputStream)
            }
            
            // Add progress listener if callback is provided
            onProgress?.let { callback ->
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    callback(progress)
                }
            }
            
            // Wait for upload to complete
            uploadTask.await()
            
            // Return the download URL
            return@withContext fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading file from input stream", e)
            throw e
        }
    }
    
    /**
     * Upload a file from a local file
     * 
     * @param file The file to upload
     * @param module The module folder to upload to
     * @param id The ID of the entity
     * @param fileName The name of the file (optional, will use file.name if not provided)
     * @param contentType The content type of the file (optional)
     * @param onProgress Callback for upload progress (optional)
     * @return The download URL of the uploaded file
     */
    suspend fun uploadFile(
        file: File,
        module: String,
        id: String,
        fileName: String? = null,
        contentType: String? = null,
        onProgress: ((progress: Double) -> Unit)? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            val fileInputStream = FileInputStream(file)
            return@withContext uploadFile(
                inputStream = fileInputStream,
                module = module,
                id = id,
                fileName = fileName ?: file.name,
                contentType = contentType,
                onProgress = onProgress
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading file from local file", e)
            throw e
        }
    }
    
    /**
     * Download a file from Firebase Storage
     * 
     * @param downloadUrl The download URL of the file
     * @param destinationFile The local file to download to
     * @param onProgress Callback for download progress (optional)
     * @return The local file
     */
    suspend fun downloadFile(
        downloadUrl: String,
        destinationFile: File,
        onProgress: ((progress: Double) -> Unit)? = null
    ): File = withContext(Dispatchers.IO) {
        try {
            // Get a reference to the file from the download URL
            val fileRef = storage.getReferenceFromUrl(downloadUrl)
            
            // Start download task
            val downloadTask = fileRef.getFile(destinationFile)
            
            // Add progress listener if callback is provided
            onProgress?.let { callback ->
                downloadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    callback(progress)
                }
            }
            
            // Wait for download to complete
            downloadTask.await()
            
            return@withContext destinationFile
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file", e)
            throw e
        }
    }
    
    /**
     * Download a file from Firebase Storage by path
     * 
     * @param module The module folder
     * @param id The ID of the entity
     * @param fileName The name of the file
     * @param destinationFile The local file to download to
     * @param onProgress Callback for download progress (optional)
     * @return The local file
     */
    suspend fun downloadFile(
        module: String,
        id: String,
        fileName: String,
        destinationFile: File,
        onProgress: ((progress: Double) -> Unit)? = null
    ): File = withContext(Dispatchers.IO) {
        try {
            // Get a reference to the file
            val fileRef = getModuleReference(module, id).child(fileName)
            
            // Start download task
            val downloadTask = fileRef.getFile(destinationFile)
            
            // Add progress listener if callback is provided
            onProgress?.let { callback ->
                downloadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    callback(progress)
                }
            }
            
            // Wait for download to complete
            downloadTask.await()
            
            return@withContext destinationFile
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file by path", e)
            throw e
        }
    }
    
    /**
     * List all files in a module folder
     * 
     * @param module The module folder
     * @param id The ID of the entity
     * @return List of file names
     */
    suspend fun listFiles(module: String, id: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val folderRef = getModuleReference(module, id)
            val result = folderRef.listAll().await()
            return@withContext result.items.map { it.name }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing files", e)
            throw e
        }
    }
    
    /**
     * Delete a file from Firebase Storage
     * 
     * @param downloadUrl The download URL of the file
     */
    suspend fun deleteFile(downloadUrl: String) = withContext(Dispatchers.IO) {
        try {
            val fileRef = storage.getReferenceFromUrl(downloadUrl)
            fileRef.delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file", e)
            throw e
        }
    }
    
    /**
     * Delete a file from Firebase Storage by path
     * 
     * @param module The module folder
     * @param id The ID of the entity
     * @param fileName The name of the file
     */
    suspend fun deleteFile(module: String, id: String, fileName: String) = withContext(Dispatchers.IO) {
        try {
            val fileRef = getModuleReference(module, id).child(fileName)
            fileRef.delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file by path", e)
            throw e
        }
    }
    
    /**
     * Get a reference to a module folder
     * 
     * @param module The module name
     * @param id The ID of the entity
     * @return StorageReference to the module folder
     */
    private fun getModuleReference(module: String, id: String): StorageReference {
        val folderName = when (module.lowercase()) {
            "lead", "leads" -> leadsFolder
            "estimate", "estimates" -> estimatesFolder
            "project", "projects" -> projectsFolder
            "client", "clients" -> clientsFolder
            "task", "tasks" -> tasksFolder
            else -> module.lowercase()
        }
        
        return storageRef.child(folderName).child(id)
    }
    
    companion object {
        private const val TAG = "FirebaseStorageRepo"
    }
}