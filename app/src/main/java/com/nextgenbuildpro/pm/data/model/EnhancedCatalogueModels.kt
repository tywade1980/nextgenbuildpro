package com.nextgenbuildpro.pm.data.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Enhanced Catalogue Models based on hierarchical structure
 * Category -> Trade -> Scope -> Assembly -> Task/Material
 * 
 * These models align with the TypeScript interfaces provided in the comments
 * and provide a more structured approach to construction catalogue management
 */

/**
 * Top-level category for organizing trades
 * Examples: Structure, Enclosure, Systems, Finishes
 */
data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val sequence: Int,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Trade represents a specific construction trade within a category
 * Examples: Concrete, Framing, Electrical, Plumbing
 */
data class Trade(
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String,
    val name: String,
    val description: String,
    val sequence: Int,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Scope represents a specific area of work within a trade
 * Examples: Interior Walls, Exterior Walls, Foundation Walls
 */
data class Scope(
    val id: String = UUID.randomUUID().toString(),
    val tradeId: String,
    val name: String,
    val description: String,
    val sequence: Int,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Enhanced Assembly model with comprehensive cost breakdown
 * Represents a complete assembly with detailed cost components
 */
data class EnhancedAssembly(
    val id: String = UUID.randomUUID().toString(),
    val scopeId: String,
    val name: String,
    val description: String,
    val sequence: Int,
    val imageUrl: String? = null,
    val unit: String,
    val laborHours: Double,
    val materialCost: Double,
    val laborCost: Double,
    val equipmentCost: Double,
    val subcontractorCost: Double,
    val otherCost: Double,
    val totalCost: Double,
    val markupPercentage: Double,
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * CatalogueTask represents a specific work item within an assembly
 * Provides detailed breakdown of labor and costs for each task
 * Renamed from Task to avoid collision with PM Task model
 */
data class CatalogueTask(
    val id: String = UUID.randomUUID().toString(),
    val assemblyId: String,
    val name: String,
    val description: String,
    val sequence: Int,
    val laborHours: Double,
    val materialCost: Double,
    val laborCost: Double,
    val equipmentCost: Double,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * CatalogueMaterial represents individual materials used in tasks or assemblies
 * Can be associated with either a specific task or directly with an assembly
 * Renamed from Material to avoid collision with PM Material model
 */
data class CatalogueMaterial(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String? = null,
    val assemblyId: String? = null,
    val name: String,
    val description: String,
    val quantity: Double,
    val unit: String,
    val unitCost: Double,
    val totalCost: Double,
    val waste: Double = 0.0, // Waste percentage
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Complete catalogue structure with full hierarchy
 */
data class CompleteCatalogue(
    val categories: List<CategoryWithChildren>,
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val version: String = "1.0"
)

/**
 * Category with all its children (trades, scopes, assemblies)
 */
data class CategoryWithChildren(
    val category: Category,
    val trades: List<TradeWithChildren>
)

/**
 * Trade with all its children (scopes, assemblies)
 */
data class TradeWithChildren(
    val trade: Trade,
    val scopes: List<ScopeWithChildren>
)

/**
 * Scope with all its children (assemblies)
 */
data class ScopeWithChildren(
    val scope: Scope,
    val assemblies: List<AssemblyWithChildren>
)

/**
 * Assembly with all its children (tasks, materials)
 */
data class AssemblyWithChildren(
    val assembly: EnhancedAssembly,
    val tasks: List<TaskWithMaterials>,
    val materials: List<CatalogueMaterial> // Direct assembly materials
)

/**
 * CatalogueTask with its associated materials
 */
data class TaskWithMaterials(
    val task: CatalogueTask,
    val materials: List<CatalogueMaterial>
)

/**
 * Search criteria for enhanced catalogue search operations
 */
data class EnhancedCatalogueSearchCriteria(
    val query: String? = null,
    val categoryId: String? = null,
    val tradeId: String? = null,
    val scopeId: String? = null,
    val tags: List<String> = emptyList(),
    val isActive: Boolean = true
)

/**
 * Search result with hierarchy context
 */
data class AssemblySearchResultWithContext(
    val assembly: EnhancedAssembly,
    val scope: Scope,
    val trade: Trade,
    val category: Category,
    val tasks: List<Task> = emptyList(),
    val materials: List<Material> = emptyList()
)

/**
 * Cost breakdown summary for an assembly
 */
data class AssemblyCostBreakdown(
    val assemblyId: String,
    val assemblyName: String,
    val directCosts: AssemblyDirectCosts,
    val indirectCosts: AssemblyIndirectCosts,
    val totalCost: Double,
    val markup: Double,
    val finalCost: Double
)

data class AssemblyDirectCosts(
    val labor: Double,
    val materials: Double,
    val equipment: Double,
    val subcontractors: Double,
    val total: Double
)

data class AssemblyIndirectCosts(
    val overhead: Double,
    val profit: Double,
    val contingency: Double,
    val total: Double
)

/**
 * Assembly creation request with complete data
 */
data class CompleteAssemblyRequest(
    val assembly: EnhancedAssembly,
    val tasks: List<Task>,
    val materials: List<Material>
)

/**
 * Batch operation result
 */
data class BatchOperationResult<T>(
    val successful: List<T>,
    val failed: List<BatchOperationError>,
    val totalProcessed: Int,
    val successCount: Int,
    val failureCount: Int
)

data class BatchOperationError(
    val itemId: String,
    val error: String,
    val details: String? = null
)