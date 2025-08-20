package com.nextgenbuildpro.pm.data.model

import com.nextgenbuildpro.core.Address
import java.time.LocalDateTime
import java.util.UUID
import com.nextgenbuildpro.pm.data.model.ContextMode
import com.nextgenbuildpro.pm.data.model.UnitType
import com.nextgenbuildpro.pm.data.model.ResolvedTask

/**
 * Data models for the Project Management (PM) module
 */

/**
 * Represents a project in the PM system
 */
data class Project(
    val id: String,
    val name: String,
    val clientId: String,
    val clientName: String,
    val leadId: String, // Added for Daily Log feature
    val address: Address,
    val status: String,
    val startDate: String,
    val endDate: String,
    val budget: Double,
    val actualCost: Double,
    val progress: Int, // 0-100
    val description: String,
    val notes: String,
    val lastActivityDate: String, // Added for Daily Log feature
    val createdAt: String,
    val updatedAt: String,
    val dailyLogs: List<DailyLog> = emptyList() // Added for Daily Log feature
)

/**
 * Represents an estimate in the PM system
 * Now part of the PM module instead of CRM
 */
data class Estimate(
    val id: String,
    val projectId: String?, // Can be null for estimates not yet associated with a project
    val title: String,
    val clientName: String,
    val amount: Double,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val items: List<EstimateItem> = emptyList(),
    val categories: List<EstimateCategory> = emptyList()
)

/**
 * Represents a template-based estimate in the PM system
 * This is the new estimate structure for the template library
 */
data class TemplateEstimate(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val contextMode: ContextMode, // remodeling, new_construction, etc.
    val assemblies: MutableList<TemplateAssembly>,
    val subtotalLabor: Double = 0.0,
    val subtotalMaterial: Double = 0.0,
    val markupTotal: Double = 0.0,
    val grandTotal: Double = 0.0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val status: EstimateStatus = EstimateStatus.DRAFT
)

/**
 * Represents an assembly in a template-based estimate
 * This is created from an AssemblyTemplate but includes resolved tasks and quantities
 */
data class TemplateAssembly(
    val id: String = UUID.randomUUID().toString(),
    val templateId: String,
    val name: String,
    val category: String,
    val description: String,
    val quantityUnit: UnitType,
    val quantity: Double,
    val tasks: List<ResolvedTask>,
    val subtotalLabor: Double = 0.0,
    val subtotalMaterial: Double = 0.0,
    val subtotalMarkup: Double = 0.0,
    val total: Double = 0.0
)

/**
 * Represents an item in an estimate
 */
data class EstimateItem(
    val id: String,
    val name: String,
    val description: String,
    val quantity: Double,
    val unitPrice: Double,
    val unit: String,
    val type: String, // Material, Labor, Equipment, etc.
    val categoryId: String? = null
)

/**
 * Represents a category in an estimate
 */
data class EstimateCategory(
    val id: String,
    val label: String,
    val total: Double,
    val items: List<EstimateItem> = emptyList()
)

/**
 * Represents a task that can be linked to various entities
 */
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val assignedTo: String,
    val status: String,
    val priority: String,
    val dueDate: String,
    val completedDate: String?,
    val estimatedHours: Double,
    val actualHours: Double,
    val dependencies: List<String>, // IDs of tasks this task depends on
    val createdAt: String,
    val updatedAt: String,

    // Entity linking fields
    val projectId: String? = null,
    val estimateId: String? = null,
    val materialId: String? = null,
    val noteId: String? = null,
    val clientId: String? = null,

    // Type of entity this task is primarily linked to
    val linkedEntityType: String? = null
)

/**
 * Represents a material used in a project
 */
data class Material(
    val id: String,
    val projectId: String,
    val name: String,
    val description: String,
    val quantity: Double,
    val unit: String,
    val unitCost: Double,
    val totalCost: Double,
    val supplier: String,
    val orderDate: String?,
    val deliveryDate: String?,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents a timesheet entry for a project
 */
data class TimesheetEntry(
    val id: String,
    val projectId: String,
    val taskId: String?,
    val userId: String,
    val userName: String,
    val date: String,
    val hours: Double,
    val description: String,
    val billable: Boolean,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents a change order for a project
 */
data class ChangeOrder(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String,
    val amount: Double,
    val status: String,
    val requestedBy: String,
    val requestDate: String,
    val approvedBy: String?,
    val approvedDate: String?,
    val items: List<ChangeOrderItem>,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents an item in a change order
 */
data class ChangeOrderItem(
    val id: String,
    val description: String,
    val quantity: Double,
    val unitPrice: Double,
    val unit: String,
    val type: String,
    val amount: Double
)

/**
 * Enum for project status
 */
enum class ProjectStatus(val displayName: String) {
    PLANNING("Planning"),
    IN_PROGRESS("In Progress"),
    ON_HOLD("On Hold"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

/**
 * Enum for estimate status
 */
enum class EstimateStatus(val displayName: String) {
    DRAFT("Draft"),
    SENT("Sent"),
    APPROVED("Approved"),
    DECLINED("Declined"),
    EXPIRED("Expired")
}

/**
 * Enum for task status
 */
enum class TaskStatus(val displayName: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    BLOCKED("Blocked"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

/**
 * Enum for task priority
 */
enum class TaskPriority(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent")
}

/**
 * Enum for material status
 */
enum class MaterialStatus(val displayName: String) {
    NEEDED("Needed"),
    ORDERED("Ordered"),
    DELIVERED("Delivered"),
    INSTALLED("Installed"),
    RETURNED("Returned")
}

/**
 * Enum for change order status
 */
enum class ChangeOrderStatus(val displayName: String) {
    REQUESTED("Requested"),
    UNDER_REVIEW("Under Review"),
    APPROVED("Approved"),
    DECLINED("Declined"),
    COMPLETED("Completed")
}
