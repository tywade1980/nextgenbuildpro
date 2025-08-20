package com.nextgenbuildpro.core.api.impl

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.core.api.ApiUsageRegistry
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of ApiUsageRegistry interface.
 * 
 * This class tracks API usage throughout the codebase and provides:
 * 1. Registration of API calls with details about the service, method, caller, and timestamp
 * 2. Retrieval of API calls for specific services or callers
 * 3. Usage statistics for services
 * 4. Export functionality for analysis
 */
class ApiUsageRegistryImpl(
    private val context: Context
) : ApiUsageRegistry {
    
    private val TAG = "ApiUsageRegistry"
    private val apiCalls = mutableListOf<ApiUsageRegistry.ApiCallRecord>()
    private val serviceCallCounts = ConcurrentHashMap<String, Int>()
    private val methodCallCounts = ConcurrentHashMap<Pair<String, String>, Int>()
    private val callerCallCounts = ConcurrentHashMap<String, Int>()
    
    /**
     * Register an API call
     * @param serviceName The name of the service being called
     * @param methodName The name of the method being called
     * @param callerClass The name of the class making the call
     * @param callerMethod The name of the method making the call
     * @param timestamp The timestamp of the call (defaults to current time)
     */
    override fun registerApiCall(
        serviceName: String,
        methodName: String,
        callerClass: String,
        callerMethod: String,
        timestamp: Long
    ) {
        try {
            // Create a record of the API call
            val record = ApiUsageRegistry.ApiCallRecord(
                serviceName = serviceName,
                methodName = methodName,
                callerClass = callerClass,
                callerMethod = callerMethod,
                timestamp = timestamp
            )
            
            // Add the record to the list
            synchronized(apiCalls) {
                apiCalls.add(record)
            }
            
            // Update call counts
            serviceCallCounts.compute(serviceName) { _, count -> (count ?: 0) + 1 }
            methodCallCounts.compute(Pair(serviceName, methodName)) { _, count -> (count ?: 0) + 1 }
            callerCallCounts.compute(callerClass) { _, count -> (count ?: 0) + 1 }
            
            // Log the API call (debug level)
            Log.d(TAG, "API call registered: $serviceName.$methodName called by $callerClass.$callerMethod")
        } catch (e: Exception) {
            // Don't let registry errors affect the application
            Log.e(TAG, "Error registering API call", e)
        }
    }
    
    /**
     * Get all registered API calls for a service
     * @param serviceName The name of the service
     * @return A list of API call records
     */
    override fun getApiCallsForService(serviceName: String): List<ApiUsageRegistry.ApiCallRecord> {
        synchronized(apiCalls) {
            return apiCalls.filter { it.serviceName == serviceName }
        }
    }
    
    /**
     * Get all registered API calls from a specific caller
     * @param callerClass The name of the class making the calls
     * @return A list of API call records
     */
    override fun getApiCallsFromCaller(callerClass: String): List<ApiUsageRegistry.ApiCallRecord> {
        synchronized(apiCalls) {
            return apiCalls.filter { it.callerClass == callerClass }
        }
    }
    
    /**
     * Get usage statistics for a service
     * @param serviceName The name of the service
     * @return Usage statistics for the service
     */
    override fun getServiceUsageStats(serviceName: String): ApiUsageRegistry.ServiceUsageStats {
        synchronized(apiCalls) {
            val serviceCalls = apiCalls.filter { it.serviceName == serviceName }
            
            if (serviceCalls.isEmpty()) {
                return ApiUsageRegistry.ServiceUsageStats(
                    serviceName = serviceName,
                    totalCalls = 0,
                    uniqueCallers = 0,
                    mostFrequentMethod = "",
                    mostFrequentCaller = "",
                    firstCallTimestamp = 0,
                    lastCallTimestamp = 0
                )
            }
            
            // Calculate statistics
            val totalCalls = serviceCalls.size
            val uniqueCallers = serviceCalls.map { it.callerClass }.toSet().size
            
            // Find most frequent method
            val methodCounts = serviceCalls.groupBy { it.methodName }.mapValues { it.value.size }
            val mostFrequentMethod = methodCounts.maxByOrNull { it.value }?.key ?: ""
            
            // Find most frequent caller
            val callerCounts = serviceCalls.groupBy { it.callerClass }.mapValues { it.value.size }
            val mostFrequentCaller = callerCounts.maxByOrNull { it.value }?.key ?: ""
            
            // Find first and last call timestamps
            val timestamps = serviceCalls.map { it.timestamp }
            val firstCallTimestamp = timestamps.minOrNull() ?: 0
            val lastCallTimestamp = timestamps.maxOrNull() ?: 0
            
            return ApiUsageRegistry.ServiceUsageStats(
                serviceName = serviceName,
                totalCalls = totalCalls,
                uniqueCallers = uniqueCallers,
                mostFrequentMethod = mostFrequentMethod,
                mostFrequentCaller = mostFrequentCaller,
                firstCallTimestamp = firstCallTimestamp,
                lastCallTimestamp = lastCallTimestamp
            )
        }
    }
    
    /**
     * Clear all registered API calls
     */
    override fun clearRegistry() {
        synchronized(apiCalls) {
            apiCalls.clear()
            serviceCallCounts.clear()
            methodCallCounts.clear()
            callerCallCounts.clear()
        }
        Log.d(TAG, "API usage registry cleared")
    }
    
    /**
     * Export the registry to a file for analysis
     * @param filePath The path to the file
     * @return True if the export was successful, false otherwise
     */
    override fun exportRegistry(filePath: String): Boolean {
        try {
            val file = File(filePath)
            
            // Create parent directories if they don't exist
            file.parentFile?.mkdirs()
            
            // Write the registry to the file
            FileOutputStream(file).use { outputStream ->
                outputStream.write("Service,Method,CallerClass,CallerMethod,Timestamp\n".toByteArray())
                
                synchronized(apiCalls) {
                    for (call in apiCalls) {
                        val line = "${call.serviceName},${call.methodName},${call.callerClass},${call.callerMethod},${call.timestamp}\n"
                        outputStream.write(line.toByteArray())
                    }
                }
            }
            
            Log.d(TAG, "API usage registry exported to $filePath")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting API usage registry", e)
            return false
        }
    }
}