package com.nextgenbuildpro.pm.data.model

import com.nextgenbuildpro.core.Address

/**
 * Data models for building management functionality
 */

/**
 * Represents a building in the system
 */
data class Building(
    val id: String,
    val name: String,
    val address: Address,
    val projectId: String,
    val type: String, // Residential, Commercial, Industrial, etc.
    val status: String,
    val squareFootage: Double,
    val floors: Int,
    val description: String,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents a floor plan in the system
 */
data class FloorPlan(
    val id: String,
    val buildingId: String,
    val name: String,
    val floorNumber: Int,
    val imageUrl: String,
    val modelUrl: String?, // URL to 3D model if available
    val squareFootage: Double,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents a structural component in the system
 */
data class StructuralComponent(
    val id: String,
    val buildingId: String,
    val floorPlanId: String?,
    val name: String,
    val type: String, // Foundation, Wall, Beam, Column, etc.
    val material: String,
    val location: String,
    val dimensions: String,
    val notes: String,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents a MEP (Mechanical, Electrical, Plumbing) system
 */
data class MepSystem(
    val id: String,
    val buildingId: String,
    val name: String,
    val type: String, // Mechanical, Electrical, Plumbing
    val subtype: String, // HVAC, Lighting, Water Supply, etc.
    val location: String,
    val specifications: String,
    val installationDate: String?,
    val lastMaintenanceDate: String?,
    val nextMaintenanceDate: String?,
    val notes: String,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents a building material
 */
data class BuildingMaterial(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val supplier: String,
    val unit: String,
    val unitCost: Double,
    val quantity: Double,
    val minimumStock: Double,
    val location: String,
    val orderStatus: String,
    val lastOrderDate: String?,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents a material usage record
 */
data class MaterialUsage(
    val id: String,
    val materialId: String,
    val projectId: String?,
    val buildingId: String?,
    val componentId: String?,
    val quantity: Double,
    val usedBy: String,
    val usageDate: String,
    val purpose: String,
    val notes: String,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents an inspection
 */
data class Inspection(
    val id: String,
    val buildingId: String,
    val projectId: String?,
    val type: String, // Structural, Electrical, Plumbing, etc.
    val inspector: String,
    val scheduledDate: String?,
    val completedDate: String?,
    val status: String,
    val result: String?, // Pass, Fail, Conditional Pass
    val notes: String,
    val attachments: List<String>, // URLs to attachment files
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents a compliance checklist
 */
data class ComplianceChecklist(
    val id: String,
    val buildingId: String,
    val name: String,
    val category: String, // Safety, Building Code, Environmental, etc.
    val items: List<ChecklistItem>,
    val completedBy: String?,
    val completedDate: String?,
    val status: String, // Not Started, In Progress, Completed
    val notes: String,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents an item in a compliance checklist
 */
data class ChecklistItem(
    val id: String,
    val description: String,
    val completed: Boolean,
    val completedBy: String?,
    val completedDate: String?,
    val notes: String
)

/**
 * Represents a permit
 */
data class Permit(
    val id: String,
    val buildingId: String,
    val projectId: String?,
    val type: String, // Building, Electrical, Plumbing, etc.
    val number: String,
    val issuedBy: String,
    val issuedDate: String,
    val expirationDate: String?,
    val status: String, // Pending, Approved, Expired, Revoked
    val attachmentUrl: String?,
    val notes: String,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents a code violation
 */
data class CodeViolation(
    val id: String,
    val buildingId: String,
    val inspectionId: String?,
    val code: String,
    val description: String,
    val severity: String, // Low, Medium, High, Critical
    val reportedDate: String,
    val reportedBy: String,
    val resolved: Boolean,
    val resolvedDate: String?,
    val resolvedBy: String?,
    val resolutionNotes: String,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents building performance metrics
 */
data class BuildingPerformance(
    val id: String,
    val buildingId: String,
    val date: String,
    val energyUsage: Double?,
    val waterUsage: Double?,
    val wasteProduction: Double?,
    val indoorAirQuality: String?,
    val temperatureAverage: Double?,
    val humidityAverage: Double?,
    val occupancyRate: Double?,
    val maintenanceIssues: Int,
    val notes: String,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Enum for building status
 */
enum class BuildingStatus(val displayName: String) {
    PLANNING("Planning"),
    UNDER_CONSTRUCTION("Under Construction"),
    COMPLETED("Completed"),
    OCCUPIED("Occupied"),
    RENOVATION("Under Renovation"),
    DEMOLISHED("Demolished")
}

/**
 * Enum for inspection status
 */
enum class InspectionStatus(val displayName: String) {
    SCHEDULED("Scheduled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    RESCHEDULED("Rescheduled")
}

/**
 * Enum for inspection result
 */
enum class InspectionResult(val displayName: String) {
    PASS("Pass"),
    CONDITIONAL_PASS("Conditional Pass"),
    FAIL("Fail"),
    NOT_APPLICABLE("Not Applicable")
}

/**
 * Enum for permit status
 */
enum class PermitStatus(val displayName: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    EXPIRED("Expired"),
    REVOKED("Revoked"),
    RENEWED("Renewed")
}

/**
 * Enum for material order status
 */
enum class MaterialOrderStatus(val displayName: String) {
    IN_STOCK("In Stock"),
    LOW_STOCK("Low Stock"),
    ORDERED("Ordered"),
    BACK_ORDERED("Back Ordered"),
    DISCONTINUED("Discontinued")
}

/**
 * Enum for checklist status
 */
enum class ChecklistStatus(val displayName: String) {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    OVERDUE("Overdue")
}

/**
 * Enum for violation severity
 */
enum class ViolationSeverity(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical")
}