package com.nextgenbuildpro.core.services

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.shared.NextGenService
import com.nextgenbuildpro.shared.ServiceHealth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

/**
 * AutoFillService
 * 
 * Provides intelligent auto-fill capabilities by aggregating data from all
 * available sources across the NextGenBuildPro ecosystem.
 */
class AutoFillService(private val context: Context) : NextGenService {
    
    override val serviceName: String = "AutoFillService"
    
    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val dataSourceRegistry = DataSourceRegistry()
    private val autoFillCache = mutableMapOf<String, AutoFillData>()
    private val validationService = DataValidationService()
    
    override suspend fun start(): Result<Unit> = try {
        Log.d(TAG, "Starting AutoFillService...")
        
        // Initialize data sources
        initializeDataSources()
        
        _isRunning.value = true
        Log.i(TAG, "AutoFillService started successfully")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to start AutoFillService", e)
        Result.failure(e)
    }
    
    override suspend fun stop(): Result<Unit> = try {
        Log.d(TAG, "Stopping AutoFillService...")
        
        // Clear cache
        autoFillCache.clear()
        
        _isRunning.value = false
        Log.i(TAG, "AutoFillService stopped successfully")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to stop AutoFillService", e)
        Result.failure(e)
    }
    
    override suspend fun restart(): Result<Unit> {
        stop()
        return start()
    }
    
    override suspend fun getHealthStatus(): ServiceHealth {
        return ServiceHealth(
            isHealthy = _isRunning.value,
            lastCheckTime = LocalDateTime.now(),
            issues = if (!_isRunning.value) listOf("Service not running") else emptyList(),
            metrics = mapOf(
                "cached_entries" to autoFillCache.size.toDouble(),
                "data_sources" to dataSourceRegistry.getActiveSourceCount().toDouble()
            )
        )
    }
    
    /**
     * Get auto-fill suggestions for a form field
     */
    suspend fun getAutoFillSuggestions(fieldType: FormFieldType, context: AutoFillContext): List<AutoFillSuggestion> {
        try {
            val cacheKey = "${fieldType.name}_${context.contextId}"
            
            // Check cache first
            autoFillCache[cacheKey]?.let { cachedData ->
                if (!cachedData.isExpired()) {
                    return cachedData.suggestions
                }
            }
            
            // Gather data from all sources
            val suggestions = mutableListOf<AutoFillSuggestion>()
            
            for (source in dataSourceRegistry.getActiveSources()) {
                try {
                    val sourceData = source.getData(fieldType, context)
                    suggestions.addAll(sourceData)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get data from source ${source.name}", e)
                }
            }
            
            // Sort by confidence and relevance
            val sortedSuggestions = suggestions
                .sortedByDescending { it.confidence * it.relevance }
                .take(MAX_SUGGESTIONS)
            
            // Validate and resolve conflicts
            val validatedSuggestions = validationService.validateAndResolveSuggestions(fieldType, sortedSuggestions)
            
            // Cache the results
            autoFillCache[cacheKey] = AutoFillData(
                suggestions = validatedSuggestions,
                timestamp = LocalDateTime.now()
            )
            
            return validatedSuggestions
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get auto-fill suggestions for $fieldType", e)
            return emptyList()
        }
    }
    
    /**
     * Auto-fill a complete form based on available data
     */
    suspend fun autoFillForm(formSchema: FormSchema, context: AutoFillContext): AutoFillResult {
        try {
            val filledFields = mutableMapOf<String, Any>()
            val suggestions = mutableMapOf<String, List<AutoFillSuggestion>>()
            
            for (field in formSchema.fields) {
                val fieldSuggestions = getAutoFillSuggestions(field.type, context.copy(fieldName = field.name))
                suggestions[field.name] = fieldSuggestions
                
                // Auto-fill with highest confidence suggestion if confidence > threshold
                fieldSuggestions.firstOrNull()?.let { topSuggestion ->
                    if (topSuggestion.confidence >= AUTO_FILL_CONFIDENCE_THRESHOLD) {
                        filledFields[field.name] = topSuggestion.value
                    }
                }
            }
            
            return AutoFillResult(
                formId = formSchema.id,
                filledFields = filledFields,
                suggestions = suggestions,
                confidence = calculateOverallConfidence(filledFields, suggestions),
                timestamp = LocalDateTime.now()
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to auto-fill form ${formSchema.id}", e)
            return AutoFillResult(
                formId = formSchema.id,
                filledFields = emptyMap(),
                suggestions = emptyMap(),
                confidence = 0.0,
                timestamp = LocalDateTime.now(),
                error = e.message
            )
        }
    }
    
    /**
     * Register a new data source
     */
    fun registerDataSource(dataSource: DataSource) {
        dataSourceRegistry.register(dataSource)
        Log.d(TAG, "Registered data source: ${dataSource.name}")
    }
    
    /**
     * Validate a field value
     */
    fun validateFieldValue(fieldType: FormFieldType, value: Any?): ValidationResult {
        return if (validationService.isValidForFieldType(fieldType, value)) {
            ValidationResult(isValid = true)
        } else {
            ValidationResult(
                isValid = false,
                errorMessage = "Invalid value for ${fieldType.name.lowercase().replace('_', ' ')}",
                validationDetails = mapOf("fieldType" to fieldType.name, "value" to value.toString())
            )
        }
    }
    
    /**
     * Learn from user selections to improve suggestions
     */
    suspend fun learnFromUserSelection(fieldType: FormFieldType, context: AutoFillContext, selectedValue: Any) {
        try {
            // Update relevance scores based on user selection
            for (source in dataSourceRegistry.getActiveSources()) {
                source.updateRelevance(fieldType, context, selectedValue)
            }
            
            // Clear cache for this field type to force refresh
            val keysToRemove = autoFillCache.keys.filter { it.startsWith(fieldType.name) }
            keysToRemove.forEach { autoFillCache.remove(it) }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to learn from user selection", e)
        }
    }
    
    private fun initializeDataSources() {
        // Register built-in data sources
        registerDataSource(CrmDataSource(context))
        registerDataSource(ProjectManagementDataSource(context))
        registerDataSource(ClientEngagementDataSource(context))
        registerDataSource(ReceptionistDataSource(context))
        registerDataSource(UserHistoryDataSource(context))
    }
    
    private fun calculateOverallConfidence(
        filledFields: Map<String, Any>,
        suggestions: Map<String, List<AutoFillSuggestion>>
    ): Double {
        if (suggestions.isEmpty()) return 0.0
        
        val totalConfidence = suggestions.values.sumOf { fieldSuggestions ->
            fieldSuggestions.firstOrNull()?.confidence ?: 0.0
        }
        
        return totalConfidence / suggestions.size
    }
    
    companion object {
        private const val TAG = "AutoFillService"
        private const val MAX_SUGGESTIONS = 5
        private const val AUTO_FILL_CONFIDENCE_THRESHOLD = 0.8
        private const val CACHE_EXPIRY_MINUTES = 30
    }
}

/**
 * Data structure for cached auto-fill data
 */
data class AutoFillData(
    val suggestions: List<AutoFillSuggestion>,
    val timestamp: LocalDateTime
) {
    fun isExpired(): Boolean {
        return timestamp.plusMinutes(30).isBefore(LocalDateTime.now())
    }
}

/**
 * Auto-fill suggestion with confidence and relevance scores
 */
data class AutoFillSuggestion(
    val value: Any,
    val displayText: String,
    val confidence: Double, // 0.0 to 1.0
    val relevance: Double,  // 0.0 to 1.0
    val source: String,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Context information for auto-fill operations
 */
data class AutoFillContext(
    val contextId: String,
    val formType: String,
    val userId: String? = null,
    val projectId: String? = null,
    val clientId: String? = null,
    val fieldName: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Result of auto-fill operation
 */
data class AutoFillResult(
    val formId: String,
    val filledFields: Map<String, Any>,
    val suggestions: Map<String, List<AutoFillSuggestion>>,
    val confidence: Double,
    val timestamp: LocalDateTime,
    val error: String? = null
)

/**
 * Schema definition for forms
 */
data class FormSchema(
    val id: String,
    val name: String,
    val fields: List<FormFieldSchema>
)

data class FormFieldSchema(
    val name: String,
    val type: FormFieldType,
    val required: Boolean = false,
    val validation: String? = null
)

/**
 * Types of form fields that can be auto-filled
 */
enum class FormFieldType {
    NAME, PHONE, EMAIL, ADDRESS, PROJECT_TYPE, NOTES, URGENCY, SOURCE,
    COMPANY_NAME, JOB_TITLE, BUDGET, TIMELINE, DESCRIPTION, 
    CLIENT_NAME, CLIENT_PHONE, CLIENT_EMAIL, CLIENT_ADDRESS,
    CONTRACTOR_NAME, CONTRACTOR_PHONE, CONTRACTOR_EMAIL,
    MATERIAL_TYPE, MATERIAL_QUANTITY, SUPPLIER_NAME,
    DATE, TIME, DURATION, LOCATION, PRIORITY
}