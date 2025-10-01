package com.nextgenbuildpro.pm.service

import android.content.Context
import com.nextgenbuildpro.pm.data.repository.TemplateEstimateRepository
import com.nextgenbuildpro.pm.data.repository.EnhancedCatalogueDataService
import com.nextgenbuildpro.pm.data.model.TemplateEstimate
import com.nextgenbuildpro.pm.data.model.AssemblyTemplate
import com.nextgenbuildpro.pm.data.model.TemplateAssembly
import com.nextgenbuildpro.shared.ClientInfo
import org.json.JSONObject
import org.json.JSONArray

/**
 * REST API Service for Estimate Management
 * 
 * Provides REST-like endpoints for the EstimateEditor frontend component.
 * This service acts as a bridge between the JavaScript frontend and Kotlin backend.
 * 
 * Endpoints implemented:
 * - GET /api/clients - Fetch all clients
 * - GET /api/estimates/:id - Fetch estimate by ID
 * - GET /api/templates/:id - Fetch template by ID
 * - GET /api/assemblies/search?q=:query - Search assemblies
 * - POST /api/assemblies/convert-to-line-item - Convert assembly to line item
 * - POST /api/estimates - Create new estimate
 * - PUT /api/estimates/:id - Update estimate
 * - POST /api/estimates/:id/apply-tax-markup - Apply tax and markup
 */
class EstimateAPIService(private val context: Context) {
    
    private val estimateRepository = TemplateEstimateRepository(context)
    private val catalogueService = EnhancedCatalogueDataService(context)
    
    /**
     * GET /api/clients
     * Fetch all clients
     */
    suspend fun fetchClients(): Result<List<ClientInfo>> {
        return try {
            // For now, return sample clients
            // TODO: Connect to actual client repository when available
            val sampleClients = listOf(
                ClientInfo(
                    id = "1",
                    name = "John Doe Construction",
                    phone = "(555) 123-4567",
                    email = "john@example.com",
                    address = "123 Main St, City, State"
                ),
                ClientInfo(
                    id = "2",
                    name = "ABC Builders",
                    phone = "(555) 234-5678",
                    email = "abc@builders.com",
                    address = "456 Oak Ave, City, State"
                ),
                ClientInfo(
                    id = "3",
                    name = "XYZ Contractors",
                    phone = "(555) 345-6789",
                    email = "xyz@contractors.com",
                    address = "789 Pine Rd, City, State"
                )
            )
            Result.success(sampleClients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * GET /api/estimates/:id
     * Fetch estimate by ID
     */
    suspend fun fetchEstimate(estimateId: String): Result<TemplateEstimate?> {
        return try {
            val estimate = estimateRepository.getById(estimateId)
            Result.success(estimate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * GET /api/templates/:id
     * Fetch template by ID (returns an estimate template)
     */
    suspend fun fetchTemplate(templateId: String): Result<TemplateEstimate?> {
        return try {
            // For now, templates are the same as estimates
            // Could be extended to fetch from a separate template library
            val template = estimateRepository.getById(templateId)
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * GET /api/assemblies/search?q=:query
     * Search assemblies by query string
     */
    suspend fun searchAssemblies(query: String): Result<List<JSONObject>> {
        return try {
            val catalogueResult = catalogueService.getCompleteCatalogue()
            if (catalogueResult.isFailure) {
                return Result.failure(catalogueResult.exceptionOrNull()!!)
            }
            
            val categories = catalogueResult.getOrThrow()
            val matchingAssemblies = mutableListOf<JSONObject>()
            
            // Search through all assemblies in all categories
            categories.forEach { categoryWithChildren ->
                categoryWithChildren.trades.forEach { tradeWithChildren ->
                    tradeWithChildren.scopes.forEach { scopeWithChildren ->
                        scopeWithChildren.assemblies.forEach { assemblyWithChildren ->
                            val assembly = assemblyWithChildren.assembly
                            
                            // Check if query matches assembly name or description
                            if (assembly.name.contains(query, ignoreCase = true) ||
                                assembly.description.contains(query, ignoreCase = true)) {
                                
                                val assemblyJson = JSONObject().apply {
                                    put("id", assembly.id)
                                    put("name", assembly.name)
                                    put("description", assembly.description)
                                    put("estimatedCost", assembly.totalCost)
                                    put("unit", assembly.unit)
                                    put("laborHours", assembly.laborHours)
                                    put("materialCost", assembly.materialCost)
                                    put("laborCost", assembly.laborCost)
                                }
                                
                                matchingAssemblies.add(assemblyJson)
                            }
                        }
                    }
                }
            }
            
            Result.success(matchingAssemblies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * POST /api/assemblies/convert-to-line-item
     * Convert assembly to line item for estimate
     */
    suspend fun convertAssemblyToLineItem(
        assemblyId: String,
        quantity: Double
    ): Result<JSONObject> {
        return try {
            // Fetch the assembly from catalogue
            val catalogueResult = catalogueService.getCompleteCatalogue()
            if (catalogueResult.isFailure) {
                return Result.failure(catalogueResult.exceptionOrNull()!!)
            }
            
            val categories = catalogueResult.getOrThrow()
            var foundAssembly: com.nextgenbuildpro.pm.data.model.Assembly? = null
            
            // Find the assembly by ID
            categories.forEach { categoryWithChildren ->
                categoryWithChildren.trades.forEach { tradeWithChildren ->
                    tradeWithChildren.scopes.forEach { scopeWithChildren ->
                        scopeWithChildren.assemblies.forEach { assemblyWithChildren ->
                            if (assemblyWithChildren.assembly.id == assemblyId) {
                                foundAssembly = assemblyWithChildren.assembly
                            }
                        }
                    }
                }
            }
            
            if (foundAssembly == null) {
                return Result.failure(IllegalArgumentException("Assembly not found: $assemblyId"))
            }
            
            val assembly = foundAssembly!!
            
            // Create line item JSON
            val lineItem = JSONObject().apply {
                put("id", "item-${System.currentTimeMillis()}")
                put("name", assembly.name)
                put("description", assembly.description)
                put("quantity", quantity)
                put("unit", assembly.unit)
                put("unitCost", assembly.totalCost)
                put("totalCost", assembly.totalCost * quantity)
                put("laborHours", assembly.laborHours * quantity)
                put("materialCost", assembly.materialCost * quantity)
                put("laborCost", assembly.laborCost * quantity)
                put("equipmentCost", assembly.equipmentCost * quantity)
            }
            
            Result.success(lineItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * POST /api/estimates
     * Create new estimate
     */
    suspend fun createEstimate(estimateData: JSONObject): Result<TemplateEstimate> {
        return try {
            // Parse estimate data from JSON
            val estimate = TemplateEstimate(
                id = estimateData.optString("id", java.util.UUID.randomUUID().toString()),
                projectId = estimateData.optString("projectId", ""),
                contextMode = com.nextgenbuildpro.pm.data.model.ContextMode.SINGLE_FAMILY_NEW_CONSTRUCTION,
                assemblies = mutableListOf(), // Will be populated from sections
                subtotalLabor = 0.0,
                subtotalMaterial = 0.0,
                markupTotal = 0.0,
                grandTotal = 0.0,
                createdAt = java.time.LocalDateTime.now(),
                status = com.nextgenbuildpro.pm.data.model.EstimateStatus.DRAFT
            )
            
            // Save to repository
            val success = estimateRepository.save(estimate)
            if (success) {
                Result.success(estimate)
            } else {
                Result.failure(Exception("Failed to save estimate"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * PUT /api/estimates/:id
     * Update existing estimate
     */
    suspend fun updateEstimate(estimateId: String, estimateData: JSONObject): Result<TemplateEstimate> {
        return try {
            // Fetch existing estimate
            val existingEstimate = estimateRepository.getById(estimateId)
                ?: return Result.failure(IllegalArgumentException("Estimate not found: $estimateId"))
            
            // Update the estimate (TemplateEstimate is immutable, so we need to update it directly)
            // For now, we'll just return the existing estimate
            // TODO: Implement proper update logic when needed
            
            val success = estimateRepository.update(existingEstimate)
            if (success) {
                Result.success(existingEstimate)
            } else {
                Result.failure(Exception("Failed to update estimate"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * POST /api/estimates/:id/apply-tax-markup
     * Apply tax and markup to an estimate
     */
    suspend fun applyTaxAndMarkup(
        estimateId: String,
        taxSettings: TaxSettings,
        markupSettings: MarkupSettings
    ): Result<TemplateEstimate> {
        return try {
            val result = estimateRepository.applyTaxAndMarkup(
                estimateId,
                taxSettings,
                markupSettings
            )
            
            if (result != null) {
                Result.success(result)
            } else {
                Result.failure(Exception("Failed to apply tax and markup"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Helper method to convert JSON array to list
     */
    private fun JSONArray.toList(): List<JSONObject> {
        val list = mutableListOf<JSONObject>()
        for (i in 0 until this.length()) {
            list.add(this.getJSONObject(i))
        }
        return list
    }
    
    companion object {
        @Volatile
        private var INSTANCE: EstimateAPIService? = null
        
        fun getInstance(context: Context): EstimateAPIService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EstimateAPIService(context).also { INSTANCE = it }
            }
        }
    }
}
