package com.nextgenbuildpro.crm.data.model

/**
 * Models for lead lifecycle management
 */

/**
 * Represents the phases a lead goes through from initial contact to closed
 */
enum class LeadPhase(val displayName: String) {
    CONTACTED("Contacted"),     // Basic info received (POC)
    QUALIFIED("Qualified"),     // Warm/hot, passed pre-checks
    DISCOVERY("Discovery"),     // Site visit / notes collected
    SCOPED("Scoped"),        // Project goals defined
    ESTIMATING("Estimating"),    // Assemblies + trade logic mapped
    DELIVERED("Delivered"),     // Estimate submitted
    CLOSED_WON("Closed Won"),    // Contract signed
    CLOSED_LOST("Closed Lost")    // Lead archived or rejected
}

/**
 * Represents a media attachment for a lead (photos, documents, etc.)
 */
data class MediaAttachment(
    val id: String,
    val leadId: String,
    val name: String,
    val type: String,  // e.g., "image/jpeg", "application/pdf"
    val url: String,
    val thumbnailUrl: String? = null,
    val size: Long,    // Size in bytes
    val uploadedAt: Long,
    val description: String? = null
)