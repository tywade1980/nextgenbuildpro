package com.nextgenbuildpro.bms.data.model

/**
 * Building information model for BMS
 */
data class Building(
    val id: String,
    val name: String,
    val address: String,
    val type: BuildingType,
    val floors: Int,
    val totalArea: Double,
    val constructionStart: String,
    val estimatedCompletion: String,
    val status: BuildingStatus,
    val projectManager: String,
    val description: String
)

enum class BuildingType {
    RESIDENTIAL,
    COMMERCIAL,
    INDUSTRIAL,
    INSTITUTIONAL,
    MIXED_USE
}

enum class BuildingStatus {
    PLANNING,
    DESIGN,
    PERMITS,
    CONSTRUCTION,
    INSPECTION,
    COMPLETED,
    MAINTENANCE
}

/**
 * Building component (walls, floors, etc.)
 */
data class BuildingComponent(
    val id: String,
    val buildingId: String,
    val name: String,
    val type: ComponentType,
    val floor: Int,
    val materials: List<Material>,
    val dimensions: Dimensions,
    val status: ComponentStatus,
    val installDate: String?,
    val inspectionDate: String?,
    val notes: String?
)

enum class ComponentType {
    FOUNDATION,
    WALLS,
    FLOORS,
    ROOF,
    DOORS,
    WINDOWS,
    ELECTRICAL,
    PLUMBING,
    HVAC,
    INSULATION,
    DRYWALL,
    FINISHING
}

enum class ComponentStatus {
    PLANNED,
    ORDERED,
    DELIVERED,
    INSTALLED,
    INSPECTED,
    APPROVED,
    REJECTED,
    COMPLETED
}

/**
 * Material information
 */
data class Material(
    val id: String,
    val name: String,
    val type: MaterialType,
    val quantity: Double,
    val unit: String,
    val unitCost: Double,
    val supplier: String,
    val specifications: String,
    val deliveryDate: String?,
    val status: MaterialStatus
)

enum class MaterialType {
    CONCRETE,
    STEEL,
    LUMBER,
    INSULATION,
    ROOFING,
    ELECTRICAL,
    PLUMBING,
    FINISHING,
    HARDWARE,
    OTHER
}

enum class MaterialStatus {
    PLANNED,
    ORDERED,
    SHIPPED,
    DELIVERED,
    INSTALLED,
    RETURNED
}

/**
 * Dimensions for components
 */
data class Dimensions(
    val length: Double,
    val width: Double,
    val height: Double,
    val unit: String = "ft"
)

/**
 * Inspection record
 */
data class Inspection(
    val id: String,
    val buildingId: String,
    val componentId: String?,
    val type: InspectionType,
    val inspector: String,
    val scheduledDate: String,
    val completedDate: String?,
    val status: InspectionStatus,
    val results: InspectionResults?,
    val notes: String?,
    val photos: List<String> = emptyList()
)

enum class InspectionType {
    FOUNDATION,
    FRAMING,
    ELECTRICAL,
    PLUMBING,
    HVAC,
    INSULATION,
    DRYWALL,
    FINAL,
    SAFETY,
    QUALITY,
    CODE_COMPLIANCE
}

enum class InspectionStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    PASSED,
    FAILED,
    CONDITIONAL,
    CANCELLED
}

/**
 * Inspection results
 */
data class InspectionResults(
    val passed: Boolean,
    val score: Double?,
    val violations: List<CodeViolation>,
    val recommendations: List<String>,
    val nextInspectionDate: String?
)

/**
 * Code violation
 */
data class CodeViolation(
    val id: String,
    val code: String,
    val description: String,
    val severity: ViolationSeverity,
    val location: String,
    val status: ViolationStatus,
    val correctionDeadline: String?
)

enum class ViolationSeverity {
    MINOR,
    MAJOR,
    CRITICAL
}

enum class ViolationStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    WAIVED
}

/**
 * Building performance metrics
 */
data class BuildingPerformance(
    val buildingId: String,
    val energyEfficiency: EnergyMetrics,
    val sustainability: SustainabilityMetrics,
    val safetyMetrics: SafetyMetrics,
    val qualityMetrics: QualityMetrics,
    val lastUpdated: String
)

data class EnergyMetrics(
    val estimatedConsumption: Double,
    val efficiency: Double,
    val certificationLevel: String?
)

data class SustainabilityMetrics(
    val leedRating: String?,
    val wasteReduction: Double,
    val recycledMaterials: Double,
    val carbonFootprint: Double
)

data class SafetyMetrics(
    val incidentCount: Int,
    val safetyScore: Double,
    val lastSafetyInspection: String?
)

data class QualityMetrics(
    val defectRate: Double,
    val reworkRate: Double,
    val customerSatisfaction: Double?,
    val qualityScore: Double
)