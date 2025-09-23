package com.nextgenbuildpro.pm.data.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Hierarchical Indexed Catalogue Models for Construction Project Management
 * 
 * This creates a comprehensive hierarchy:
 * ProjectCatalogue (Home Construction) -> ProjectType -> Trade -> Assembly/SubAssembly -> Task/MacroTask
 */

/**
 * Root level catalogue for construction projects
 * Represents the parent level for home construction
 */
data class ProjectCatalogue(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Home Construction",
    val description: String = "Comprehensive catalogue for residential construction projects with detailed assemblies, tasks, and cost data",
    val version: String = "1.0",
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val projectTypes: List<ProjectType>
)

/**
 * Project types that are children of the parent project catalogue
 * Examples: New Construction, Remodeling, Addition, etc.
 */
data class ProjectType(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val code: String, // e.g., "NC", "RM", "AD"
    val contextMode: ContextMode,
    val applicablePhases: List<HomeLifecyclePhase>,
    val trades: List<TradeIndex>,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Trade index within a project type
 * Organizes all assemblies and tasks by trade type
 */
data class TradeIndex(
    val id: String = UUID.randomUUID().toString(),
    val tradeName: String,
    val tradeCode: String,
    val description: String,
    val workDescription: String, // Detailed description of work performed by this trade
    val lifecyclePhase: HomeLifecyclePhase,
    val masterAssembly: MasterAssembly, // Contains all assemblies for this trade
    val avgLaborRate: Double, // Average labor rate per hour for this trade
    val webResourceUrls: List<WebResourceUrl> = emptyList() // Sources for cost data
)

/**
 * Master assembly for a trade containing all sub-assemblies
 * This is the container for all assemblies within a trade
 */
data class MasterAssembly(
    val id: String = UUID.randomUUID().toString(),
    val name: String, // e.g., "Framing Master Assembly"
    val description: String,
    val tradeCode: String,
    val assemblies: List<DetailedAssembly>,
    val subAssemblies: List<SubAssembly>,
    val totalEstimatedHours: Double,
    val totalEstimatedCost: Double
)

/**
 * Detailed assembly with comprehensive information
 * Enhanced version of existing AssemblyTemplate
 */
data class DetailedAssembly(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val description: String,
    val workDescription: String, // Detailed description of work performed for this assembly
    val validModes: List<ContextMode>,
    val defaultQuantityUnit: UnitType,
    val baseQuantity: Double,
    val lifecyclePhase: HomeLifecyclePhase,
    val tasks: List<DetailedTask>,
    val macroTasks: List<MacroTask>,
    val prerequisites: List<String> = emptyList(),
    val deliverables: List<String> = emptyList(),
    val qualityStandards: List<String> = emptyList(),
    val safetyRequirements: List<String> = emptyList()
)

/**
 * Sub-assembly for more granular organization
 */
data class SubAssembly(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val workDescription: String,
    val parentAssemblyId: String,
    val tasks: List<DetailedTask>,
    val estimatedHours: Double,
    val estimatedCost: Double,
    val skillLevel: SkillLevel = SkillLevel.INTERMEDIATE
)

/**
 * Enhanced task with detailed information and web-sourced cost data
 */
data class DetailedTask(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val workDescription: String, // Specific description of work performed for this task
    val unitType: UnitType,
    val defaultQty: Double,
    val laborTimePerUnit: Double, // Labor hours per unit
    val laborCostPerUnit: Double, // Labor cost per unit
    val materialCostPerUnit: Double,
    val equipmentCostPerUnit: Double = 0.0,
    val markup: Double,
    val skillLevel: SkillLevel = SkillLevel.INTERMEDIATE,
    val requiredTools: List<String> = emptyList(),
    val requiredMaterials: List<TaskMaterial> = emptyList(),
    val safetyNotes: List<String> = emptyList(),
    val qualityCheckpoints: List<String> = emptyList(),
    val webSourcedData: LaborCostData? = null, // Data sourced from web resources
    val flags: List<String> = emptyList()
)

/**
 * Macro task for grouping related tasks
 */
data class MacroTask(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val workDescription: String,
    val tasks: List<DetailedTask>,
    val totalEstimatedHours: Double,
    val totalEstimatedCost: Double,
    val sequenceOrder: Int,
    val dependencies: List<String> = emptyList()
)

/**
 * Material information for tasks
 */
data class TaskMaterial(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val quantity: Double,
    val unit: String,
    val unitCost: Double,
    val supplier: String? = null,
    val partNumber: String? = null,
    val webSourceUrl: String? = null
)

/**
 * Labor cost data sourced from web resources
 */
data class LaborCostData(
    val source: String, // e.g., "BLS.gov", "RSMeans", "Local Market Data"
    val sourceUrl: String,
    val region: String, // Geographic region for cost data
    val lastUpdated: LocalDateTime,
    val avgHourlyRate: Double,
    val lowRate: Double,
    val highRate: Double,
    val currency: String = "USD",
    val reliability: DataReliability = DataReliability.MEDIUM
)

/**
 * Web resource URL for cost and time data
 */
data class WebResourceUrl(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val url: String,
    val description: String,
    val dataType: WebResourceType,
    val lastAccessed: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true
)

/**
 * Skill level required for tasks
 */
enum class SkillLevel {
    ENTRY_LEVEL,
    INTERMEDIATE,
    ADVANCED,
    EXPERT,
    MASTER_CRAFTSMAN
}

/**
 * Web resource types
 */
enum class WebResourceType {
    LABOR_RATES,
    MATERIAL_COSTS,
    EQUIPMENT_COSTS,
    TIME_STANDARDS,
    INDUSTRY_BENCHMARKS,
    GOVERNMENT_DATA,
    SUPPLIER_CATALOGS
}

/**
 * Data reliability levels
 */
enum class DataReliability {
    LOW,
    MEDIUM,
    HIGH,
    VERIFIED
}

/**
 * Navigation helper for the hierarchical catalogue
 */
data class CatalogueNavigation(
    val currentLevel: CatalogueLevel,
    val breadcrumb: List<String>,
    val availableChildren: List<String>,
    val parentId: String? = null
)

/**
 * Catalogue hierarchy levels
 */
enum class CatalogueLevel {
    PROJECT_CATALOGUE,
    PROJECT_TYPE,
    TRADE_INDEX,
    MASTER_ASSEMBLY,
    ASSEMBLY,
    SUB_ASSEMBLY,
    MACRO_TASK,
    TASK
}

/**
 * Search and filter criteria for the catalogue
 */
data class CatalogueSearchCriteria(
    val keyword: String? = null,
    val tradeType: String? = null,
    val projectType: String? = null,
    val lifecyclePhase: HomeLifecyclePhase? = null,
    val skillLevel: SkillLevel? = null,
    val priceRange: Pair<Double, Double>? = null
)