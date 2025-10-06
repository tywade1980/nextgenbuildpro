package com.nextgenbuildpro.pm.service

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.data.repository.HierarchicalCatalogueRepository
import com.nextgenbuildpro.pm.data.repository.AssemblyRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

/**
 * Service for integrating assembly catalogue with estimates
 * Provides search, retrieval, and conversion functions
 */
class AssemblyCatalogueService(
    private val context: Context,
    private val hierarchicalCatalogueRepository: HierarchicalCatalogueRepository,
    private val assemblyRepository: AssemblyRepository
) {
    private val TAG = "AssemblyCatalogueService"

    /**
     * Search assemblies in the catalogue based on criteria
     */
    suspend fun searchAssemblies(
        keyword: String? = null,
        tradeType: String? = null,
        projectType: String? = null,
        contextMode: ContextMode? = null
    ): List<AssemblySearchResult> {
        try {
            val criteria = HierarchicalCatalogueSearchCriteria(
                keyword = keyword,
                tradeType = tradeType,
                projectType = projectType
            )
            
            val results = hierarchicalCatalogueRepository.searchCatalogue(criteria)
            
            return results.mapNotNull { result ->
                when (result) {
                    is Assembly -> convertToSearchResult(result)
                    is MasterAssembly -> convertToSearchResult(result)
                    else -> null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching assemblies: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Get detailed assembly information by ID
     */
    suspend fun getAssemblyDetails(assemblyId: String): AssemblyDetails? {
        try {
            // First try to find in assembly repository
            val assembly = assemblyRepository.getAssemblyById(assemblyId)
            if (assembly != null) {
                return convertToAssemblyDetails(assembly)
            }
            
            // If not found, search in hierarchical catalogue
            val catalogue = hierarchicalCatalogueRepository.projectCatalogue.first()
            catalogue?.let { cat ->
                cat.projectTypes.forEach { projectType ->
                    projectType.trades.forEach { trade ->
                        trade.masterAssembly.assemblies.find { it.id == assemblyId }?.let { foundAssembly ->
                            // TODO: Implement convertToAssemblyDetails for Assembly type
                            return null
                        }
                    }
                }
            }
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting assembly details: ${e.message}")
            return null
        }
    }

    /**
     * Convert assembly to estimate line items
     */
    suspend fun convertAssemblyToLineItems(
        assembly: Assembly,
        quantity: Double,
        contextMode: ContextMode
    ): List<EstimateLineItem> {
        try {
            val lineItems = mutableListOf<EstimateLineItem>()
            
            // Add labor line item
            lineItems.add(
                EstimateLineItem(
                    description = "${assembly.name} - Labor",
                    quantity = quantity,
                    unit = "hours",
                    unitPrice = assembly.laborHours * getHourlyRate(assembly.tradeName),
                    totalPrice = quantity * assembly.laborHours * getHourlyRate(assembly.tradeName),
                    category = "Labor",
                    tradeCategory = assembly.tradeName
                )
            )
            
            // Add material line items
            assembly.materials.forEach { material ->
                lineItems.add(
                    EstimateLineItem(
                        description = "${assembly.name} - ${material.name}",
                        quantity = quantity * material.quantity,
                        unit = material.unit,
                        unitPrice = material.unitCost,
                        totalPrice = quantity * material.totalCost,
                        category = "Material",
                        tradeCategory = assembly.tradeName
                    )
                )
            }
            
            return lineItems
        } catch (e: Exception) {
            Log.e(TAG, "Error converting assembly to line items: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Convert AssemblyTemplate to TemplateAssembly for estimates
     */
    suspend fun convertTemplateToAssembly(
        assemblyTemplate: AssemblyTemplate,
        quantity: Double
    ): TemplateAssembly? {
        try {
            // Resolve tasks for the assembly
            val resolvedTasks = assemblyTemplate.tasks.map { taskTemplate ->
                ResolvedTask(
                    task = taskTemplate,
                    quantity = quantity * taskTemplate.defaultQty,
                    laborCost = quantity * taskTemplate.defaultQty * taskTemplate.laborPerUnit,
                    materialCost = quantity * taskTemplate.defaultQty * taskTemplate.materialPerUnit,
                    markupCost = quantity * taskTemplate.defaultQty * 
                        (taskTemplate.laborPerUnit + taskTemplate.materialPerUnit) * taskTemplate.markup
                )
            }
            
            // Calculate subtotals
            val subtotalLabor = resolvedTasks.sumOf { it.laborCost }
            val subtotalMaterial = resolvedTasks.sumOf { it.materialCost }
            val subtotalMarkup = resolvedTasks.sumOf { it.markupCost }
            val total = subtotalLabor + subtotalMaterial + subtotalMarkup
            
            return TemplateAssembly(
                templateId = assemblyTemplate.id,
                name = assemblyTemplate.name,
                category = assemblyTemplate.category,
                description = assemblyTemplate.description,
                quantityUnit = assemblyTemplate.defaultQuantityUnit,
                quantity = quantity,
                tasks = resolvedTasks,
                subtotalLabor = subtotalLabor,
                subtotalMaterial = subtotalMaterial,
                subtotalMarkup = subtotalMarkup,
                total = total
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error converting template to assembly: ${e.message}")
            return null
        }
    }

    /**
     * Get assemblies by trade category
     */
    suspend fun getAssembliesByTrade(tradeCategory: String): List<AssemblySearchResult> {
        return searchAssemblies(tradeType = tradeCategory)
    }

    /**
     * Get assemblies by context mode
     */
    suspend fun getAssembliesByContextMode(contextMode: ContextMode): List<AssemblySearchResult> {
        return searchAssemblies(contextMode = contextMode)
    }

    private fun convertToSearchResult(assembly: Assembly): AssemblySearchResult {
        return AssemblySearchResult(
            id = assembly.id,
            name = assembly.name,
            description = assembly.description,
            tradeCategory = assembly.tradeName,
            estimatedCost = assembly.estimatedCost,
            laborHours = assembly.laborHours,
            tags = assembly.tags
        )
    }

    private fun convertToSearchResult(masterAssembly: MasterAssembly): AssemblySearchResult {
        return AssemblySearchResult(
            id = masterAssembly.assemblies.firstOrNull()?.id ?: "",
            name = masterAssembly.name,
            description = masterAssembly.description,
            tradeCategory = masterAssembly.tradeCode,
            estimatedCost = masterAssembly.totalEstimatedCost,
            laborHours = masterAssembly.totalEstimatedHours,
            tags = emptyList()
        )
    }

    private fun convertToAssemblyDetails(assembly: Assembly): AssemblyDetails {
        return AssemblyDetails(
            id = assembly.id,
            name = assembly.name,
            description = assembly.description,
            tradeCategory = assembly.tradeName,
            materials = assembly.materials,
            laborHours = assembly.laborHours,
            estimatedCost = assembly.estimatedCost,
            tags = assembly.tags,
            createdAt = assembly.createdAt,
            updatedAt = assembly.updatedAt
        )
    }

    private fun convertToAssemblyDetails(masterAssembly: MasterAssembly): AssemblyDetails {
        return AssemblyDetails(
            id = masterAssembly.assemblies.firstOrNull()?.id ?: "",
            name = masterAssembly.name,
            description = masterAssembly.description,
            tradeCategory = masterAssembly.tradeCode,
            materials = emptyList(), // Master assemblies don't have direct materials
            laborHours = masterAssembly.totalEstimatedHours,
            estimatedCost = masterAssembly.totalEstimatedCost,
            tags = emptyList(),
            createdAt = "",
            updatedAt = ""
        )
    }

    private fun getHourlyRate(tradeName: String): Double {
        // Standard hourly rates by trade - this could be configurable
        return when (tradeName.lowercase()) {
            "framing" -> 45.0
            "drywall" -> 40.0
            "electrical" -> 65.0
            "plumbing" -> 55.0
            "hvac" -> 60.0
            "roofing" -> 50.0
            "flooring" -> 35.0
            "painting" -> 30.0
            "tile" -> 40.0
            "cabinets" -> 55.0
            else -> 45.0 // Default rate
        }
    }
}

/**
 * Search result for assembly catalogue
 */
data class AssemblySearchResult(
    val id: String,
    val name: String,
    val description: String,
    val tradeCategory: String,
    val estimatedCost: Double,
    val laborHours: Double,
    val tags: List<String>
)

/**
 * Detailed assembly information
 */
data class AssemblyDetails(
    val id: String,
    val name: String,
    val description: String,
    val tradeCategory: String,
    val materials: List<AssemblyMaterial>,
    val laborHours: Double,
    val estimatedCost: Double,
    val tags: List<String>,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Estimate line item for conversion
 */
data class EstimateLineItem(
    val description: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val totalPrice: Double,
    val category: String,
    val tradeCategory: String
)

/**
 * Search criteria for catalogue
 */
data class CatalogueSearchCriteria(
    val keyword: String? = null,
    val tradeType: String? = null,
    val projectType: String? = null
)