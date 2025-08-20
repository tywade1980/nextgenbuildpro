package com.nextgenbuildpro.pm.data.model

/**
 * Data models for the Assemblies Library and Job Templates
 */

/**
 * Represents a trade category for organizing assemblies and job templates
 */
data class TradeCategory(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val assemblies: List<Assembly> = emptyList(),
    val jobTemplates: List<JobTemplate> = emptyList()
)

/**
 * Represents an assembly in the assemblies library
 * An assembly is a pre-defined component that can be used in projects
 */
data class Assembly(
    val id: String,
    val name: String,
    val description: String,
    val tradeId: String,
    val tradeName: String,
    val materials: List<AssemblyMaterial> = emptyList(),
    val laborHours: Double,
    val estimatedCost: Double,
    val tags: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents a material used in an assembly
 */
data class AssemblyMaterial(
    val id: String,
    val name: String,
    val description: String,
    val quantity: Double,
    val unit: String,
    val unitCost: Double,
    val totalCost: Double,
    val optional: Boolean = false,
    val alternatives: List<String> = emptyList() // IDs of alternative materials
)

/**
 * Represents a job template in the assemblies library
 * A job template is a pre-defined project template that can be used to create new projects
 */
data class JobTemplate(
    val id: String,
    val name: String,
    val description: String,
    val tradeId: String,
    val tradeName: String,
    val assemblies: List<Assembly> = emptyList(),
    val estimatedDuration: Int, // in days
    val estimatedCost: Double,
    val phases: List<TemplatePhase> = emptyList(),
    val dataFields: List<EditableDataField> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents a phase in a job template
 */
data class TemplatePhase(
    val id: String,
    val name: String,
    val description: String,
    val order: Int,
    val estimatedDuration: Int, // in days
    val tasks: List<TemplateTask> = emptyList(),
    val dependencies: List<String> = emptyList() // IDs of phases this phase depends on
)

/**
 * Represents a task in a job template phase
 */
data class TemplateTask(
    val id: String,
    val name: String,
    val description: String,
    val estimatedHours: Double,
    val assignedToRole: String, // e.g., "Electrician", "Plumber", etc.
    val dependencies: List<String> = emptyList() // IDs of tasks this task depends on
)

/**
 * Represents an editable data field in a job template
 * These fields can be customized when creating a project from a template
 */
data class EditableDataField(
    val id: String,
    val name: String,
    val description: String,
    val type: DataFieldType,
    val required: Boolean = false,
    val defaultValue: String? = null,
    val options: List<String> = emptyList(), // For dropdown/select fields
    val min: Double? = null, // For number fields
    val max: Double? = null, // For number fields
    val placeholder: String? = null
)

/**
 * Enum for data field types
 */
enum class DataFieldType {
    TEXT,
    NUMBER,
    BOOLEAN,
    DATE,
    SELECT,
    MULTI_SELECT,
    TEXTAREA
}

/**
 * Enum for trade categories
 */
enum class TradeType(val displayName: String) {
    GENERAL("General Contractor"),
    CARPENTRY("Carpentry"),
    ELECTRICAL("Electrical"),
    PLUMBING("Plumbing"),
    HVAC("HVAC"),
    MASONRY("Masonry"),
    PAINTING("Painting"),
    ROOFING("Roofing"),
    FLOORING("Flooring"),
    LANDSCAPING("Landscaping"),
    CONCRETE("Concrete"),
    DRYWALL("Drywall"),
    INSULATION("Insulation"),
    CABINETRY("Cabinetry"),
    TILE("Tile"),
    GLASS("Glass"),
    METAL("Metal"),
    OTHER("Other")
}
