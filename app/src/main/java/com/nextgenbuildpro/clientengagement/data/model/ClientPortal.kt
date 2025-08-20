package com.nextgenbuildpro.clientengagement.data.model

import java.util.UUID

/**
 * Data class representing a client portal
 */
data class ClientPortal(
    val id: String = UUID.randomUUID().toString(),
    val clientId: String,
    val projectId: String,
    val accessToken: String,
    val lastAccessDate: Long? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Data class representing client portal settings
 */
data class ClientPortalSettings(
    val id: String = UUID.randomUUID().toString(),
    val portalId: String,
    val allowFileDownload: Boolean = true,
    val allowMessageSending: Boolean = true,
    val allowEstimateApproval: Boolean = true,
    val allowInvoicePayment: Boolean = true,
    val notifyOnUpdates: Boolean = true,
    val notifyOnMessages: Boolean = true,
    val notifyOnEstimates: Boolean = true,
    val notifyOnInvoices: Boolean = true
)

/**
 * Data class representing client portal activity
 */
data class ClientPortalActivity(
    val id: String = UUID.randomUUID().toString(),
    val portalId: String,
    val clientId: String,
    val activityType: ClientPortalActivityType,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val relatedItemId: String? = null,
    val relatedItemType: String? = null
)

/**
 * Enum representing types of client portal activities
 */
enum class ClientPortalActivityType {
    LOGIN,
    LOGOUT,
    VIEW_PROJECT,
    VIEW_ESTIMATE,
    VIEW_INVOICE,
    DOWNLOAD_FILE,
    SEND_MESSAGE,
    APPROVE_ESTIMATE,
    PAY_INVOICE,
    UPDATE_PROFILE
}