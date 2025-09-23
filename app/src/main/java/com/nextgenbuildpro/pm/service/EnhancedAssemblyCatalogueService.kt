package com.nextgenbuildpro.pm.service

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.data.repository.EnhancedCatalogueDataService
import com.nextgenbuildpro.pm.data.repository.HierarchicalCatalogueRepository
import com.nextgenbuildpro.pm.data.repository.AssemblyRepository
import kotlinx.coroutines.flow.first

/**
 * Enhanced Assembly Catalogue Integration Service
 * 
 * This service integrates the new enhanced catalogue models with the existing
 * assembly catalogue service, providing backward compatibility while enabling
 * the improved hierarchical structure suggested in the comments.
 */
class EnhancedAssemblyCatalogueService(
    private val context: Context,
    private val enhancedCatalogueDataService: EnhancedCatalogueDataService,
    private val hierarchicalCatalogueRepository: HierarchicalCatalogueRepository,
    private val assemblyRepository: AssemblyRepository
) {
    private val TAG = "EnhancedAssemblyCatalogueService"
    
    /**
     * Search assemblies using the enhanced catalogue structure
     */
    suspend fun searchAssembliesEnhanced(
        keyword: String? = null,
        categoryId: String? = null,
        tradeId: String? = null,
        scopeId: String? = null,
        tags: List<String> = emptyList()
    ): List<AssemblySearchResultWithContext> {
        try {
            val criteria = CatalogueSearchCriteria(
                query = keyword,
                categoryId = categoryId,
                tradeId = tradeId,
                scopeId = scopeId,
                tags = tags
            )
            
            val result = enhancedCatalogueDataService.searchAssemblies(criteria)
            return result.getOrElse { 
                Log.e(TAG, "Enhanced search failed, falling back to basic search")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in enhanced search: ${e.message}")
            return emptyList()
        }
    }
    
    /**
     * Get complete catalogue hierarchy
     */
    suspend fun getCompleteCatalogue(): CompleteCatalogue? {
        return try {
            val categoriesResult = enhancedCatalogueDataService.getCategoriesWithChildren()
            categoriesResult.getOrNull()?.let { categories ->
                CompleteCatalogue(categories = categories)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting complete catalogue: ${e.message}")
            null
        }
    }
    
    /**
     * Convert enhanced assembly to legacy assembly for backward compatibility
     */
    fun convertToLegacyAssembly(
        enhancedAssembly: EnhancedAssembly,
        scope: Scope,
        trade: Trade
    ): Assembly {
        return Assembly(
            id = enhancedAssembly.id,
            name = enhancedAssembly.name,
            description = enhancedAssembly.description,
            tradeId = trade.id,
            tradeName = trade.name,
            materials = emptyList(), // Would need to convert materials
            laborHours = enhancedAssembly.laborHours,
            estimatedCost = enhancedAssembly.totalCost,
            tags = enhancedAssembly.tags,
            createdAt = enhancedAssembly.createdAt.toString(),
            updatedAt = enhancedAssembly.updatedAt.toString()
        )
    }
    
    /**
     * Convert enhanced assembly with context to assembly search result
     */
    fun convertToSearchResult(contextResult: AssemblySearchResultWithContext): AssemblySearchResult {
        return AssemblySearchResult(
            id = contextResult.assembly.id,
            name = contextResult.assembly.name,
            description = contextResult.assembly.description,
            tradeCategory = contextResult.trade.name,
            estimatedCost = contextResult.assembly.totalCost,
            laborHours = contextResult.assembly.laborHours,
            tags = contextResult.assembly.tags
        )
    }
    
    /**
     * Get detailed assembly information with enhanced breakdown
     */
    suspend fun getEnhancedAssemblyDetails(assemblyId: String): EnhancedAssemblyDetails? {
        return try {
            val costBreakdownResult = enhancedCatalogueDataService.getAssemblyCostBreakdown(assemblyId)
            val costBreakdown = costBreakdownResult.getOrNull()
            
            if (costBreakdown != null) {
                // Get assembly context
                val searchResult = searchAssembliesEnhanced().firstOrNull { it.assembly.id == assemblyId }
                
                if (searchResult != null) {
                    EnhancedAssemblyDetails(
                        assembly = searchResult.assembly,
                        scope = searchResult.scope,
                        trade = searchResult.trade,
                        category = searchResult.category,
                        tasks = searchResult.tasks,
                        materials = searchResult.materials,
                        costBreakdown = costBreakdown
                    )
                } else null
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting enhanced assembly details: ${e.message}")
            null
        }
    }
    
    /**
     * Create a complete assembly with tasks and materials
     */
    suspend fun createCompleteAssembly(
        scopeId: String,
        assemblyData: AssemblyCreationData,
        tasks: List<TaskCreationData>,
        materials: List<MaterialCreationData>
    ): AssemblyWithChildren? {
        return try {
            // Convert creation data to enhanced models
            val enhancedAssembly = EnhancedAssembly(
                scopeId = scopeId,
                name = assemblyData.name,
                description = assemblyData.description,
                sequence = assemblyData.sequence,
                unit = assemblyData.unit,
                laborHours = assemblyData.laborHours,
                materialCost = assemblyData.materialCost,
                laborCost = assemblyData.laborCost,
                equipmentCost = assemblyData.equipmentCost,
                subcontractorCost = assemblyData.subcontractorCost,
                otherCost = assemblyData.otherCost,
                totalCost = assemblyData.laborCost + assemblyData.materialCost + 
                           assemblyData.equipmentCost + assemblyData.subcontractorCost + assemblyData.otherCost,
                markupPercentage = assemblyData.markupPercentage,
                notes = assemblyData.notes,
                tags = assemblyData.tags,
                imageUrl = assemblyData.imageUrl
            )
            
            val enhancedTasks = tasks.map { taskData ->
                Task(
                    assemblyId = "", // Will be set when assembly is created
                    name = taskData.name,
                    description = taskData.description,
                    sequence = taskData.sequence,
                    laborHours = taskData.laborHours,
                    materialCost = taskData.materialCost,
                    laborCost = taskData.laborCost,
                    equipmentCost = taskData.equipmentCost,
                    notes = taskData.notes
                )
            }
            
            val enhancedMaterials = materials.map { materialData ->
                Material(
                    taskId = materialData.taskId,
                    assemblyId = materialData.assemblyId,
                    name = materialData.name,
                    description = materialData.description,
                    quantity = materialData.quantity,
                    unit = materialData.unit,
                    unitCost = materialData.unitCost,
                    totalCost = materialData.unitCost * materialData.quantity * (1 + materialData.waste),
                    waste = materialData.waste,
                    notes = materialData.notes
                )
            }
            
            val request = CompleteAssemblyRequest(
                assembly = enhancedAssembly,
                tasks = enhancedTasks,
                materials = enhancedMaterials
            )
            
            val result = enhancedCatalogueDataService.createCompleteAssembly(request)
            result.getOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating complete assembly: ${e.message}")
            null
        }
    }
    
    /**
     * Get assemblies by category with enhanced details
     */
    suspend fun getAssembliesByCategory(categoryId: String): List<AssemblySearchResultWithContext> {
        return searchAssembliesEnhanced(categoryId = categoryId)
    }
    
    /**
     * Get assemblies by trade with enhanced details
     */
    suspend fun getAssembliesByTrade(tradeId: String): List<AssemblySearchResultWithContext> {
        return searchAssembliesEnhanced(tradeId = tradeId)
    }
    
    /**
     * Get assemblies by scope with enhanced details
     */
    suspend fun getAssembliesByScope(scopeId: String): List<AssemblySearchResultWithContext> {
        return searchAssembliesEnhanced(scopeId = scopeId)
    }
    
    /**
     * Migrate legacy assembly to enhanced structure
     */
    suspend fun migrateLegacyAssembly(
        legacyAssembly: Assembly,
        scopeId: String
    ): EnhancedAssembly? {
        return try {
            val result = enhancedCatalogueDataService.createAssembly(
                scopeId = scopeId,
                name = legacyAssembly.name,
                description = legacyAssembly.description,
                sequence = 1, // Default sequence
                unit = "EA", // Default unit
                laborHours = legacyAssembly.laborHours,
                materialCost = legacyAssembly.materials.sumOf { it.totalCost },
                laborCost = legacyAssembly.laborHours * 45.0, // Default rate
                markupPercentage = 0.2,
                tags = legacyAssembly.tags
            )
            
            result.getOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating legacy assembly: ${e.message}")
            null
        }
    }
}

// Data classes for assembly creation

data class AssemblyCreationData(
    val name: String,
    val description: String,
    val sequence: Int,
    val unit: String,
    val laborHours: Double,
    val materialCost: Double,
    val laborCost: Double,
    val equipmentCost: Double = 0.0,
    val subcontractorCost: Double = 0.0,
    val otherCost: Double = 0.0,
    val markupPercentage: Double = 0.2,
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val imageUrl: String? = null
)

data class TaskCreationData(
    val name: String,
    val description: String,
    val sequence: Int,
    val laborHours: Double,
    val materialCost: Double,
    val laborCost: Double,
    val equipmentCost: Double = 0.0,
    val notes: String = ""
)

data class MaterialCreationData(
    val taskId: String? = null,
    val assemblyId: String? = null,
    val name: String,
    val description: String,
    val quantity: Double,
    val unit: String,
    val unitCost: Double,
    val waste: Double = 0.0,
    val notes: String = ""
)

data class EnhancedAssemblyDetails(
    val assembly: EnhancedAssembly,
    val scope: Scope,
    val trade: Trade,
    val category: Category,
    val tasks: List<Task>,
    val materials: List<Material>,
    val costBreakdown: AssemblyCostBreakdown
)