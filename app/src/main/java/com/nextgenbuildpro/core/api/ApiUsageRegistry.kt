package com.nextgenbuildpro.core.api

/**
 * Interface for tracking API usage throughout the codebase.
 * 
 * This is a delicate API that helps maintain a registry of where and how APIs are used.
 * It provides a way to:
 * 1. Track which parts of the application use which APIs
 * 2. Monitor API call frequency and patterns
 * 3. Identify potential areas for optimization
 * 4. Assist with API migration planning
 */
interface ApiUsageRegistry {
    /**
     * Register an API call
     * @param serviceName The name of the service being called
     * @param methodName The name of the method being called
     * @param callerClass The name of the class making the call
     * @param callerMethod The name of the method making the call
     * @param timestamp The timestamp of the call (defaults to current time)
     */
    fun registerApiCall(
        serviceName: String,
        methodName: String,
        callerClass: String,
        callerMethod: String,
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Get all registered API calls for a service
     * @param serviceName The name of the service
     * @return A list of API call records
     */
    fun getApiCallsForService(serviceName: String): List<ApiCallRecord>
    
    /**
     * Get all registered API calls from a specific caller
     * @param callerClass The name of the class making the calls
     * @return A list of API call records
     */
    fun getApiCallsFromCaller(callerClass: String): List<ApiCallRecord>
    
    /**
     * Get usage statistics for a service
     * @param serviceName The name of the service
     * @return Usage statistics for the service
     */
    fun getServiceUsageStats(serviceName: String): ServiceUsageStats
    
    /**
     * Clear all registered API calls
     */
    fun clearRegistry()
    
    /**
     * Export the registry to a file for analysis
     * @param filePath The path to the file
     * @return True if the export was successful, false otherwise
     */
    fun exportRegistry(filePath: String): Boolean
    
    /**
     * Data class representing an API call record
     */
    data class ApiCallRecord(
        val serviceName: String,
        val methodName: String,
        val callerClass: String,
        val callerMethod: String,
        val timestamp: Long
    )
    
    /**
     * Data class representing usage statistics for a service
     */
    data class ServiceUsageStats(
        val serviceName: String,
        val totalCalls: Int,
        val uniqueCallers: Int,
        val mostFrequentMethod: String,
        val mostFrequentCaller: String,
        val firstCallTimestamp: Long,
        val lastCallTimestamp: Long
    )
}