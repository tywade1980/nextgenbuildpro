package com.nextgenbuildpro.clientengagement.data.model

import java.util.UUID

/**
 * Data class representing a progress update
 */
data class ProgressUpdate(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val description: String,
    val completionPercentage: Float,
    val photoUrls: List<String> = emptyList(),
    val isSharedWithClient: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String
)

/**
 * Data class representing a milestone update
 */
data class MilestoneUpdate(
    val id: String = UUID.randomUUID().toString(),
    val progressUpdateId: String,
    val milestoneName: String,
    val isCompleted: Boolean,
    val completedAt: Long? = null,
    val notes: String? = null
)

/**
 * Data class representing a scheduled update
 */
data class ScheduledUpdate(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val frequency: UpdateFrequency,
    val dayOfWeek: Int? = null, // 1-7 for Monday-Sunday
    val dayOfMonth: Int? = null, // 1-31
    val time: String? = null, // HH:MM format
    val isActive: Boolean = true,
    val lastSentAt: Long? = null,
    val nextScheduledAt: Long? = null,
    val recipientIds: List<String> = emptyList(),
    val includePhotos: Boolean = true,
    val includeMilestones: Boolean = true
)

/**
 * Enum representing update frequency options
 */
enum class UpdateFrequency {
    DAILY,
    WEEKLY,
    BI_WEEKLY,
    MONTHLY,
    MILESTONE_BASED
}

/**
 * Data class representing a client notification
 */
data class UpdateNotification(
    val id: String = UUID.randomUUID().toString(),
    val progressUpdateId: String,
    val recipientId: String,
    val notificationType: NotificationType,
    val sentAt: Long = System.currentTimeMillis(),
    val readAt: Long? = null,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING
)

/**
 * Enum representing notification types
 */
enum class NotificationType {
    EMAIL,
    SMS,
    PUSH,
    IN_APP
}

/**
 * Enum representing delivery status
 */
enum class DeliveryStatus {
    PENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}