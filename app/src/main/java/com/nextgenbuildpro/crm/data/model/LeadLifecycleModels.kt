package com.nextgenbuildpro.crm.data.model

/**
 * Models for lead lifecycle management
 */

/**
 * Represents the phases a lead goes through from initial contact to closed
 */
enum class LeadPhase {
    CONTACTED,     // Basic info received (POC)
    QUALIFIED,     // Warm/hot, passed pre-checks
    DISCOVERY,     // Site visit / notes collected
    SCOPED,        // Project goals defined
    ESTIMATING,    // Assemblies + trade logic mapped
    DELIVERED,     // Estimate submitted
    CLOSED_WON,    // Contract signed
    CLOSED_LOST    // Lead archived or rejected
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