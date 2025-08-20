package com.nextgenbuildpro.crm.data.model

import android.location.Location
import java.io.File

/**
 * Data models for the Photos feature
 */

/**
 * Represents a photo associated with a lead/client
 */
data class ClientPhoto(
    val id: String,
    val leadId: String,
    val filePath: String,
    val fileName: String,
    val timestamp: String,
    val location: PhotoLocation?,
    val description: String = "",
    val isSync: Boolean = true
)

/**
 * Represents a location where a photo was taken
 */
data class PhotoLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
) {
    /**
     * Check if this location is near another location
     * @param other The other location to compare with
     * @param distanceThreshold The maximum distance in meters to consider "near"
     * @return True if the locations are within the threshold distance
     */
    fun isNear(other: PhotoLocation, distanceThreshold: Float = 100f): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(
            latitude, longitude,
            other.latitude, other.longitude,
            results
        )
        return results[0] <= distanceThreshold
    }
}

/**
 * Represents a project location for matching photos
 */
data class ProjectLocation(
    val projectId: String,
    val leadId: String,
    val location: PhotoLocation,
    val name: String,
    val radius: Float = 100f // Default radius in meters
)