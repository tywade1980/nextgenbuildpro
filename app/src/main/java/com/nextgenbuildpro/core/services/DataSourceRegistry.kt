package com.nextgenbuildpro.core.services

import android.util.Log

/**
 * Registry for managing data sources used by the AutoFillService
 */
class DataSourceRegistry {
    
    private val dataSources = mutableMapOf<String, DataSource>()
    
    /**
     * Register a new data source
     */
    fun register(dataSource: DataSource) {
        dataSources[dataSource.name] = dataSource
        Log.d(TAG, "Registered data source: ${dataSource.name}")
    }
    
    /**
     * Unregister a data source
     */
    fun unregister(name: String) {
        dataSources.remove(name)
        Log.d(TAG, "Unregistered data source: $name")
    }
    
    /**
     * Get all active data sources
     */
    fun getActiveSources(): List<DataSource> {
        return dataSources.values.filter { it.isActive() }
    }
    
    /**
     * Get a specific data source by name
     */
    fun getDataSource(name: String): DataSource? {
        return dataSources[name]
    }
    
    /**
     * Get count of active data sources
     */
    fun getActiveSourceCount(): Int {
        return getActiveSources().size
    }
    
    companion object {
        private const val TAG = "DataSourceRegistry"
    }
}

/**
 * Interface for data sources that provide auto-fill data
 */
interface DataSource {
    val name: String
    val priority: Int // Higher number = higher priority
    
    /**
     * Check if this data source is currently active and available
     */
    fun isActive(): Boolean
    
    /**
     * Get data for a specific field type and context
     */
    suspend fun getData(fieldType: FormFieldType, context: AutoFillContext): List<AutoFillSuggestion>
    
    /**
     * Update relevance scores based on user feedback
     */
    suspend fun updateRelevance(fieldType: FormFieldType, context: AutoFillContext, selectedValue: Any)
    
    /**
     * Get health status of this data source
     */
    suspend fun getHealthStatus(): DataSourceHealth
}

/**
 * Health status for data sources
 */
data class DataSourceHealth(
    val isHealthy: Boolean,
    val responseTimeMs: Long,
    val lastSuccessfulQuery: String?,
    val errorCount: Int = 0,
    val lastError: String? = null
)