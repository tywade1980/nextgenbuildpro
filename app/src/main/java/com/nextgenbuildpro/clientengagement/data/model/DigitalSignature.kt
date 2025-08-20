package com.nextgenbuildpro.clientengagement.data.model

import java.util.UUID

/**
 * Data class representing a digital signature
 */
data class DigitalSignature(
    val id: String = UUID.randomUUID().toString(),
    val signatureImageUrl: String,
    val signedBy: String,
    val signedAt: Long = System.currentTimeMillis(),
    val ipAddress: String? = null,
    val deviceInfo: String? = null,
    val geoLocation: GeoLocation? = null,
    val documentId: String,
    val documentType: DocumentType,
    val isValid: Boolean = true
)

/**
 * Data class representing a signature request
 */
data class SignatureRequest(
    val id: String = UUID.randomUUID().toString(),
    val documentId: String,
    val documentType: DocumentType,
    val requestedBy: String,
    val requestedFrom: String,
    val requestedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val status: SignatureRequestStatus = SignatureRequestStatus.PENDING,
    val completedAt: Long? = null,
    val signatureId: String? = null,
    val message: String? = null,
    val remindersSent: Int = 0,
    val lastReminderSent: Long? = null
)

/**
 * Data class representing a document to be signed
 */
data class SignableDocument(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val documentUrl: String,
    val documentType: DocumentType,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val signatureFields: List<SignatureField> = emptyList(),
    val isTemplate: Boolean = false
)

/**
 * Data class representing a signature field in a document
 */
data class SignatureField(
    val id: String = UUID.randomUUID().toString(),
    val documentId: String,
    val fieldType: SignatureFieldType,
    val pageNumber: Int,
    val xPosition: Float,
    val yPosition: Float,
    val width: Float,
    val height: Float,
    val isRequired: Boolean = true,
    val label: String? = null,
    val assignedTo: String? = null
)

/**
 * Data class representing a geographical location
 */
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val locationName: String? = null
)

/**
 * Enum representing document types
 */
enum class DocumentType {
    ESTIMATE,
    CONTRACT,
    CHANGE_ORDER,
    INVOICE,
    WAIVER,
    COMPLETION_CERTIFICATE,
    OTHER
}

/**
 * Enum representing signature request status
 */
enum class SignatureRequestStatus {
    PENDING,
    VIEWED,
    COMPLETED,
    DECLINED,
    EXPIRED,
    CANCELLED
}

/**
 * Enum representing signature field types
 */
enum class SignatureFieldType {
    SIGNATURE,
    INITIAL,
    DATE,
    TEXT,
    CHECKBOX
}