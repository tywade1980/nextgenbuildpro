package com.nextgenbuildpro.crm.data.repository

import com.nextgenbuildpro.crm.data.model.ClientPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing client photos
 */
class ClientPhotoRepository {

    // In-memory storage for photos (in a real app, this would be a database)
    private val _photos = MutableStateFlow<List<ClientPhoto>>(emptyList())
    val photos: StateFlow<List<ClientPhoto>> = _photos.asStateFlow()

    /**
     * Get all photos for a specific lead
     */
    fun getPhotosForLead(leadId: String): List<ClientPhoto> {
        return _photos.value.filter { it.leadId == leadId }
    }

    /**
     * Add a new photo
     */
    fun addPhoto(photo: ClientPhoto) {
        val currentPhotos = _photos.value.toMutableList()
        currentPhotos.add(photo)
        _photos.value = currentPhotos
    }

    /**
     * Delete a photo by ID
     */
    fun deletePhoto(photoId: String): Boolean {
        val currentPhotos = _photos.value.toMutableList()
        val removed = currentPhotos.removeIf { it.id == photoId }
        if (removed) {
            _photos.value = currentPhotos
        }
        return removed
    }

    /**
     * Update a photo
     */
    fun updatePhoto(photo: ClientPhoto): Boolean {
        val currentPhotos = _photos.value.toMutableList()
        val index = currentPhotos.indexOfFirst { it.id == photo.id }
        if (index != -1) {
            currentPhotos[index] = photo
            _photos.value = currentPhotos
            return true
        }
        return false
    }

    /**
     * Save a photo (alias for addPhoto for compatibility)
     */
    fun save(photo: ClientPhoto) {
        addPhoto(photo)
    }

    /**
     * Upload lead photo (placeholder implementation)
     */
    suspend fun uploadLeadPhoto(leadId: String, uri: android.net.Uri): String {
        // In a real implementation, this would upload to a server
        // For now, return the URI as string
        return uri.toString()
    }
}
