package com.nextgenbuildpro.crm.data.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.core.DateUtils
import com.nextgenbuildpro.crm.data.model.ClientPhoto
import com.nextgenbuildpro.crm.data.model.PhotoLocation
import com.nextgenbuildpro.crm.data.model.ProjectLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

/**
 * Repository for managing client photos
 */
class PhotoRepository(private val context: Context) : Repository<ClientPhoto> {
    private val _photos = MutableStateFlow<List<ClientPhoto>>(emptyList())
    val photos: StateFlow<List<ClientPhoto>> = _photos.asStateFlow()

    private val _projectLocations = MutableStateFlow<List<ProjectLocation>>(emptyList())
    val projectLocations: StateFlow<List<ProjectLocation>> = _projectLocations.asStateFlow()

    init {
        // Load sample data for demonstration
        loadSampleData()
    }

    /**
     * Get all photos
     */
    override suspend fun getAll(): List<ClientPhoto> {
        return _photos.value
    }

    /**
     * Get a photo by ID
     */
    override suspend fun getById(id: String): ClientPhoto? {
        return _photos.value.find { it.id == id }
    }

    /**
     * Save a new photo
     */
    override suspend fun save(item: ClientPhoto): Boolean {
        try {
            _photos.value = _photos.value + item
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Update an existing photo
     */
    override suspend fun update(item: ClientPhoto): Boolean {
        try {
            _photos.value = _photos.value.map { 
                if (it.id == item.id) item else it 
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Delete a photo by ID
     */
    override suspend fun delete(id: String): Boolean {
        try {
            val photoToDelete = _photos.value.find { it.id == id }
            if (photoToDelete != null) {
                // Delete the actual file
                val file = File(photoToDelete.filePath)
                if (file.exists()) {
                    file.delete()
                }

                // Remove from the list
                _photos.value = _photos.value.filter { it.id != id }
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Get photos for a specific lead
     */
    suspend fun getPhotosByLeadId(leadId: String): List<ClientPhoto> {
        return _photos.value.filter { it.leadId == leadId }
    }

    /**
     * Add a project location for photo matching
     */
    suspend fun addProjectLocation(projectLocation: ProjectLocation): Boolean {
        try {
            _projectLocations.value = _projectLocations.value + projectLocation
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Get project locations for a specific lead
     */
    suspend fun getProjectLocationsByLeadId(leadId: String): List<ProjectLocation> {
        return _projectLocations.value.filter { it.leadId == leadId }
    }

    /**
     * Find a matching project location for a given photo location
     */
    suspend fun findMatchingProjectLocation(photoLocation: PhotoLocation): ProjectLocation? {
        return _projectLocations.value.find { projectLocation ->
            photoLocation.isNear(projectLocation.location, projectLocation.radius)
        }
    }

    /**
     * Copy a photo from the device to the app's storage and associate it with a lead
     */
    suspend fun copyPhotoToAppStorage(
        sourceUri: Uri,
        leadId: String,
        photoLocation: PhotoLocation?,
        description: String = ""
    ): ClientPhoto? {
        try {
            // Create directory for client photos if it doesn't exist
            val clientPhotoDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "client_photos/$leadId"
            )
            if (!clientPhotoDir.exists()) {
                clientPhotoDir.mkdirs()
            }

            // Generate a unique filename
            val timestamp = DateUtils.getCurrentTimestamp()
            val fileName = "photo_${timestamp.replace(":", "-").replace(" ", "_")}.jpg"
            val destinationFile = File(clientPhotoDir, fileName)

            // Copy the file
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Create and save the ClientPhoto object
            val clientPhoto = ClientPhoto(
                id = UUID.randomUUID().toString(),
                leadId = leadId,
                filePath = destinationFile.absolutePath,
                fileName = fileName,
                timestamp = timestamp,
                location = photoLocation,
                description = description
            )

            save(clientPhoto)
            return clientPhoto
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Upload a photo for a lead and return the file path
     */
    suspend fun uploadLeadPhoto(leadId: String, uri: Uri): String {
        val photo = copyPhotoToAppStorage(uri, leadId, null)
        return photo?.filePath ?: ""
    }

    /**
     * Load sample data for demonstration
     */
    private fun loadSampleData() {
        // Sample project locations
        val sampleProjectLocations = listOf(
            ProjectLocation(
                projectId = "project_1",
                leadId = "lead_1",
                location = PhotoLocation(
                    latitude = 37.7749,
                    longitude = -122.4194,
                    address = "123 Main St, San Francisco, CA"
                ),
                name = "Kitchen Renovation"
            ),
            ProjectLocation(
                projectId = "project_2",
                leadId = "lead_2",
                location = PhotoLocation(
                    latitude = 40.7128,
                    longitude = -74.0060,
                    address = "456 Oak Ave, New York, NY"
                ),
                name = "Bathroom Remodel"
            )
        )

        _projectLocations.value = sampleProjectLocations

        // We don't load sample photos as they would require actual files
        // Photos will be added as they are detected from the device
    }
}
