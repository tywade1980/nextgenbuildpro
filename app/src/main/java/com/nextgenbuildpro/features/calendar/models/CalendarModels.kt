package com.nextgenbuildpro.features.calendar.models

import java.util.Date

/**
 * Data models for the Calendar feature
 */

/**
 * Represents a project timeline
 */
data class ProjectTimeline(
    val id: String,
    val projectId: String,
    val projectName: String,
    val startDate: Date,
    val endDate: Date,
    val progress: Int, // 0-100
    val phases: List<TimelinePhase> = emptyList(),
    val milestones: List<TimelineMilestone> = emptyList(),
    val materialDeliveries: List<MaterialDelivery> = emptyList(),
    val changeOrders: List<TimelineChangeOrder> = emptyList()
)

/**
 * Represents a phase in a project timeline
 */
data class TimelinePhase(
    val id: String,
    val name: String,
    val startDate: Date,
    val endDate: Date,
    val progress: Int, // 0-100
    val tasks: List<TimelineTask> = emptyList(),
    val dependencies: List<String> = emptyList() // IDs of phases this phase depends on
)

/**
 * Represents a task in a project timeline
 */
data class TimelineTask(
    val id: String,
    val name: String,
    val startDate: Date,
    val endDate: Date,
    val progress: Int, // 0-100
    val assignedTo: String,
    val status: TaskStatus,
    val dependencies: List<String> = emptyList() // IDs of tasks this task depends on
)

/**
 * Represents a milestone in a project timeline
 */
data class TimelineMilestone(
    val id: String,
    val name: String,
    val date: Date,
    val completed: Boolean,
    val description: String
)

/**
 * Represents a material delivery in a project timeline
 */
data class MaterialDelivery(
    val id: String,
    val name: String,
    val scheduledDate: Date,
    val actualDate: Date?,
    val status: DeliveryStatus,
    val materials: List<String> // Material names or IDs
)

/**
 * Represents a change order in a project timeline
 */
data class TimelineChangeOrder(
    val id: String,
    val name: String,
    val requestDate: Date,
    val approvalDate: Date?,
    val impact: TimelineImpact,
    val status: ChangeOrderStatus
)

/**
 * Represents the impact of a change order on the timeline
 */
data class TimelineImpact(
    val daysAdded: Int,
    val costImpact: Double,
    val affectedPhases: List<String> // IDs of affected phases
)

/**
 * Represents a calendar event
 */
data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String,
    val startTime: Date,
    val endTime: Date,
    val location: String,
    val type: EventType,
    val projectId: String?,
    val leadId: String?
)

/**
 * Enum for task status
 */
enum class TaskStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    BLOCKED,
    DELAYED
}

/**
 * Enum for delivery status
 */
enum class DeliveryStatus {
    SCHEDULED,
    SHIPPED,
    DELIVERED,
    DELAYED,
    CANCELLED
}

/**
 * Enum for change order status
 */
enum class ChangeOrderStatus {
    REQUESTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    IMPLEMENTED
}

/**
 * Enum for event type
 */
enum class EventType {
    MEETING,
    SITE_VISIT,
    INSPECTION,
    DELIVERY,
    MILESTONE,
    PERSONAL
}