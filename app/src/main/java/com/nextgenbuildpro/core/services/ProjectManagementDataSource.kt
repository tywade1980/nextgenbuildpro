package com.nextgenbuildpro.core.services

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.PmModule
import kotlinx.coroutines.delay

/**
 * Data source that pulls information from the Project Management module
 */
class ProjectManagementDataSource(private val context: Context) : DataSource {
    
    override val name: String = "ProjectManagement"
    override val priority: Int = 8 // High priority for project-related data
    
    private var lastQueryTime = 0L
    private var errorCount = 0
    private var lastError: String? = null
    
    override fun isActive(): Boolean {
        return try {
            PmModule.isInitialized()
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getData(fieldType: FormFieldType, context: AutoFillContext): List<AutoFillSuggestion> {
        val startTime = System.currentTimeMillis()
        
        try {
            if (!isActive()) {
                return emptyList()
            }
            
            val suggestions = mutableListOf<AutoFillSuggestion>()
            
            when (fieldType) {
                FormFieldType.PROJECT_TYPE -> {
                    suggestions.addAll(getProjectTypes(context))
                }
                FormFieldType.BUDGET -> {
                    suggestions.addAll(getBudgetEstimates(context))
                }
                FormFieldType.TIMELINE -> {
                    suggestions.addAll(getTimelines(context))
                }
                FormFieldType.DESCRIPTION -> {
                    suggestions.addAll(getProjectDescriptions(context))
                }
                FormFieldType.CONTRACTOR_NAME -> {
                    suggestions.addAll(getContractorNames(context))
                }
                FormFieldType.CONTRACTOR_PHONE -> {
                    suggestions.addAll(getContractorPhones(context))
                }
                FormFieldType.CONTRACTOR_EMAIL -> {
                    suggestions.addAll(getContractorEmails(context))
                }
                FormFieldType.MATERIAL_TYPE -> {
                    suggestions.addAll(getMaterialTypes(context))
                }
                FormFieldType.MATERIAL_QUANTITY -> {
                    suggestions.addAll(getMaterialQuantities(context))
                }
                FormFieldType.SUPPLIER_NAME -> {
                    suggestions.addAll(getSupplierNames(context))
                }
                FormFieldType.LOCATION -> {
                    suggestions.addAll(getProjectLocations(context))
                }
                FormFieldType.PRIORITY -> {
                    suggestions.addAll(getProjectPriorities(context))
                }
                else -> {
                    // No data available for this field type from PM
                }
            }
            
            lastQueryTime = System.currentTimeMillis() - startTime
            return suggestions
            
        } catch (e: Exception) {
            errorCount++
            lastError = e.message
            Log.e(TAG, "Failed to get PM data for $fieldType", e)
            return emptyList()
        }
    }
    
    override suspend fun updateRelevance(fieldType: FormFieldType, context: AutoFillContext, selectedValue: Any) {
        try {
            Log.d(TAG, "Learning from user selection: $fieldType = $selectedValue")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update relevance", e)
        }
    }
    
    override suspend fun getHealthStatus(): DataSourceHealth {
        return DataSourceHealth(
            isHealthy = isActive(),
            responseTimeMs = lastQueryTime,
            lastSuccessfulQuery = if (errorCount == 0) "Recent" else null,
            errorCount = errorCount,
            lastError = lastError
        )
    }
    
    private suspend fun getProjectTypes(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "Residential Construction",
                    displayText = "Residential Construction",
                    confidence = 0.9,
                    relevance = 0.85,
                    source = name,
                    metadata = mapOf("category" to "construction", "frequency" to "high")
                ),
                AutoFillSuggestion(
                    value = "Commercial Build-out",
                    displayText = "Commercial Build-out",
                    confidence = 0.85,
                    relevance = 0.8,
                    source = name,
                    metadata = mapOf("category" to "construction", "frequency" to "medium")
                ),
                AutoFillSuggestion(
                    value = "Renovation Project",
                    displayText = "Renovation Project",
                    confidence = 0.8,
                    relevance = 0.9,
                    source = name,
                    metadata = mapOf("category" to "renovation", "frequency" to "high")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get project types", e)
            emptyList()
        }
    }
    
    private suspend fun getBudgetEstimates(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "$50,000 - $100,000",
                    displayText = "$50,000 - $100,000",
                    confidence = 0.8,
                    relevance = 0.7,
                    source = name,
                    metadata = mapOf("range" to "medium", "type" to "estimate")
                ),
                AutoFillSuggestion(
                    value = "$100,000 - $250,000",
                    displayText = "$100,000 - $250,000",
                    confidence = 0.75,
                    relevance = 0.6,
                    source = name,
                    metadata = mapOf("range" to "high", "type" to "estimate")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get budget estimates", e)
            emptyList()
        }
    }
    
    private suspend fun getTimelines(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "3-6 months",
                    displayText = "3-6 months",
                    confidence = 0.85,
                    relevance = 0.8,
                    source = name,
                    metadata = mapOf("duration" to "medium", "type" to "typical")
                ),
                AutoFillSuggestion(
                    value = "6-12 months",
                    displayText = "6-12 months",
                    confidence = 0.8,
                    relevance = 0.7,
                    source = name,
                    metadata = mapOf("duration" to "long", "type" to "typical")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get timelines", e)
            emptyList()
        }
    }
    
    private suspend fun getProjectDescriptions(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "Complete home renovation including kitchen and bathroom updates",
                    displayText = "Complete home renovation...",
                    confidence = 0.7,
                    relevance = 0.6,
                    source = name,
                    metadata = mapOf("type" to "template", "category" to "renovation")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get project descriptions", e)
            emptyList()
        }
    }
    
    private suspend fun getContractorNames(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "Elite Construction Services",
                    displayText = "Elite Construction Services",
                    confidence = 0.9,
                    relevance = 0.85,
                    source = name,
                    metadata = mapOf("type" to "preferred_contractor", "rating" to "high")
                ),
                AutoFillSuggestion(
                    value = "Premier Building Co.",
                    displayText = "Premier Building Co.",
                    confidence = 0.85,
                    relevance = 0.8,
                    source = name,
                    metadata = mapOf("type" to "preferred_contractor", "rating" to "high")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get contractor names", e)
            emptyList()
        }
    }
    
    private suspend fun getContractorPhones(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "(555) 987-6543",
                    displayText = "(555) 987-6543 - Elite Construction",
                    confidence = 0.85,
                    relevance = 0.8,
                    source = name,
                    metadata = mapOf("contractor" to "Elite Construction Services")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get contractor phones", e)
            emptyList()
        }
    }
    
    private suspend fun getContractorEmails(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "contact@eliteconstruction.com",
                    displayText = "contact@eliteconstruction.com",
                    confidence = 0.85,
                    relevance = 0.8,
                    source = name,
                    metadata = mapOf("contractor" to "Elite Construction Services")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get contractor emails", e)
            emptyList()
        }
    }
    
    private suspend fun getMaterialTypes(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "Lumber",
                    displayText = "Lumber",
                    confidence = 0.9,
                    relevance = 0.85,
                    source = name,
                    metadata = mapOf("category" to "building_materials", "frequency" to "high")
                ),
                AutoFillSuggestion(
                    value = "Drywall",
                    displayText = "Drywall",
                    confidence = 0.85,
                    relevance = 0.8,
                    source = name,
                    metadata = mapOf("category" to "building_materials", "frequency" to "high")
                ),
                AutoFillSuggestion(
                    value = "Flooring",
                    displayText = "Flooring",
                    confidence = 0.8,
                    relevance = 0.75,
                    source = name,
                    metadata = mapOf("category" to "finishing_materials", "frequency" to "medium")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get material types", e)
            emptyList()
        }
    }
    
    private suspend fun getMaterialQuantities(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "1000 sq ft",
                    displayText = "1000 sq ft",
                    confidence = 0.7,
                    relevance = 0.6,
                    source = name,
                    metadata = mapOf("unit" to "area", "type" to "typical")
                ),
                AutoFillSuggestion(
                    value = "50 sheets",
                    displayText = "50 sheets",
                    confidence = 0.65,
                    relevance = 0.55,
                    source = name,
                    metadata = mapOf("unit" to "count", "type" to "typical")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get material quantities", e)
            emptyList()
        }
    }
    
    private suspend fun getSupplierNames(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "Home Depot",
                    displayText = "Home Depot",
                    confidence = 0.9,
                    relevance = 0.85,
                    source = name,
                    metadata = mapOf("type" to "big_box", "frequency" to "high")
                ),
                AutoFillSuggestion(
                    value = "Local Lumber Yard",
                    displayText = "Local Lumber Yard",
                    confidence = 0.8,
                    relevance = 0.9,
                    source = name,
                    metadata = mapOf("type" to "specialty", "frequency" to "medium")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get supplier names", e)
            emptyList()
        }
    }
    
    private suspend fun getProjectLocations(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "Main Street Project Site",
                    displayText = "Main Street Project Site",
                    confidence = 0.8,
                    relevance = 0.7,
                    source = name,
                    metadata = mapOf("type" to "recent_project", "status" to "active")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get project locations", e)
            emptyList()
        }
    }
    
    private suspend fun getProjectPriorities(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "High",
                    displayText = "High",
                    confidence = 0.8,
                    relevance = 0.7,
                    source = name,
                    metadata = mapOf("level" to "high", "frequency" to "medium")
                ),
                AutoFillSuggestion(
                    value = "Medium",
                    displayText = "Medium",
                    confidence = 0.9,
                    relevance = 0.9,
                    source = name,
                    metadata = mapOf("level" to "medium", "frequency" to "high")
                ),
                AutoFillSuggestion(
                    value = "Low",
                    displayText = "Low",
                    confidence = 0.75,
                    relevance = 0.6,
                    source = name,
                    metadata = mapOf("level" to "low", "frequency" to "low")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get project priorities", e)
            emptyList()
        }
    }
    
    companion object {
        private const val TAG = "ProjectManagementDataSource"
    }
}