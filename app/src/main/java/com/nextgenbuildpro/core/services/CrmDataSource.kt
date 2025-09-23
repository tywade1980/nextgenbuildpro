package com.nextgenbuildpro.core.services

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.crm.CrmModule
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * Data source that pulls information from the CRM module
 */
class CrmDataSource(private val context: Context) : DataSource {
    
    override val name: String = "CRM"
    override val priority: Int = 10 // High priority for client-related data
    
    private var lastQueryTime = 0L
    private var errorCount = 0
    private var lastError: String? = null
    
    override fun isActive(): Boolean {
        return try {
            CrmModule.isInitialized()
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
                FormFieldType.NAME, FormFieldType.CLIENT_NAME -> {
                    suggestions.addAll(getClientNames(context))
                }
                FormFieldType.PHONE, FormFieldType.CLIENT_PHONE -> {
                    suggestions.addAll(getClientPhones(context))
                }
                FormFieldType.EMAIL, FormFieldType.CLIENT_EMAIL -> {
                    suggestions.addAll(getClientEmails(context))
                }
                FormFieldType.ADDRESS, FormFieldType.CLIENT_ADDRESS -> {
                    suggestions.addAll(getClientAddresses(context))
                }
                FormFieldType.COMPANY_NAME -> {
                    suggestions.addAll(getCompanyNames(context))
                }
                FormFieldType.PROJECT_TYPE -> {
                    suggestions.addAll(getProjectTypes(context))
                }
                FormFieldType.SOURCE -> {
                    suggestions.addAll(getLeadSources(context))
                }
                FormFieldType.NOTES -> {
                    suggestions.addAll(getPreviousNotes(context))
                }
                else -> {
                    // No data available for this field type from CRM
                }
            }
            
            lastQueryTime = System.currentTimeMillis() - startTime
            return suggestions
            
        } catch (e: Exception) {
            errorCount++
            lastError = e.message
            Log.e(TAG, "Failed to get CRM data for $fieldType", e)
            return emptyList()
        }
    }
    
    override suspend fun updateRelevance(fieldType: FormFieldType, context: AutoFillContext, selectedValue: Any) {
        try {
            // Store user preference for future suggestions
            // This could be implemented as a learning system that tracks user choices
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
    
    private suspend fun getClientNames(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            // In a real implementation, this would query the lead repository
            // For now, we'll return mock data that could come from recent leads
            delay(10) // Simulate database query
            
            listOf(
                AutoFillSuggestion(
                    value = "John Smith",
                    displayText = "John Smith",
                    confidence = 0.9,
                    relevance = 0.8,
                    source = name,
                    metadata = mapOf("type" to "recent_lead")
                ),
                AutoFillSuggestion(
                    value = "Sarah Johnson",
                    displayText = "Sarah Johnson",
                    confidence = 0.85,
                    relevance = 0.7,
                    source = name,
                    metadata = mapOf("type" to "recent_lead")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get client names", e)
            emptyList()
        }
    }
    
    private suspend fun getClientPhones(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "(555) 123-4567",
                    displayText = "(555) 123-4567",
                    confidence = 0.9,
                    relevance = 0.8,
                    source = name,
                    metadata = mapOf("type" to "recent_contact")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get client phones", e)
            emptyList()
        }
    }
    
    private suspend fun getClientEmails(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "john.smith@email.com",
                    displayText = "john.smith@email.com",
                    confidence = 0.9,
                    relevance = 0.8,
                    source = name,
                    metadata = mapOf("type" to "recent_contact")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get client emails", e)
            emptyList()
        }
    }
    
    private suspend fun getClientAddresses(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "123 Main St, Anytown, ST 12345",
                    displayText = "123 Main St, Anytown, ST 12345",
                    confidence = 0.85,
                    relevance = 0.7,
                    source = name,
                    metadata = mapOf("type" to "recent_project_address")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get client addresses", e)
            emptyList()
        }
    }
    
    private suspend fun getCompanyNames(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "ABC Construction",
                    displayText = "ABC Construction",
                    confidence = 0.8,
                    relevance = 0.9,
                    source = name,
                    metadata = mapOf("type" to "frequent_client")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get company names", e)
            emptyList()
        }
    }
    
    private suspend fun getProjectTypes(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "Home Renovation",
                    displayText = "Home Renovation",
                    confidence = 0.9,
                    relevance = 0.85,
                    source = name,
                    metadata = mapOf("type" to "popular_project_type")
                ),
                AutoFillSuggestion(
                    value = "Kitchen Remodel",
                    displayText = "Kitchen Remodel",
                    confidence = 0.85,
                    relevance = 0.8,
                    source = name,
                    metadata = mapOf("type" to "popular_project_type")
                ),
                AutoFillSuggestion(
                    value = "Bathroom Renovation",
                    displayText = "Bathroom Renovation",
                    confidence = 0.8,
                    relevance = 0.75,
                    source = name,
                    metadata = mapOf("type" to "popular_project_type")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get project types", e)
            emptyList()
        }
    }
    
    private suspend fun getLeadSources(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "Website",
                    displayText = "Website",
                    confidence = 0.9,
                    relevance = 0.9,
                    source = name,
                    metadata = mapOf("type" to "common_source")
                ),
                AutoFillSuggestion(
                    value = "Referral",
                    displayText = "Referral",
                    confidence = 0.85,
                    relevance = 0.85,
                    source = name,
                    metadata = mapOf("type" to "common_source")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get lead sources", e)
            emptyList()
        }
    }
    
    private suspend fun getPreviousNotes(context: AutoFillContext): List<AutoFillSuggestion> {
        return try {
            delay(10)
            
            listOf(
                AutoFillSuggestion(
                    value = "Follow up in 1 week",
                    displayText = "Follow up in 1 week",
                    confidence = 0.7,
                    relevance = 0.6,
                    source = name,
                    metadata = mapOf("type" to "common_note")
                ),
                AutoFillSuggestion(
                    value = "Interested in spring timeline",
                    displayText = "Interested in spring timeline",
                    confidence = 0.65,
                    relevance = 0.5,
                    source = name,
                    metadata = mapOf("type" to "common_note")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get previous notes", e)
            emptyList()
        }
    }
    
    companion object {
        private const val TAG = "CrmDataSource"
    }
}