package com.nextgenbuildpro.core.api

/**
 * Interface for data validation and caching strategies for API data.
 * 
 * This is a delicate API that abstracts data validation and caching operations.
 * It provides a layer of abstraction to make it easier to:
 * 1. Validate data received from APIs
 * 2. Implement caching strategies to reduce API calls
 * 3. Handle API outages gracefully
 * 4. Implement data purging policies
 */
interface DataValidationAndCachingService {
    /**
     * Validate data received from an API
     * @param data The data to validate
     * @param validationRules The validation rules to apply
     * @return A validation result indicating success or failure with error details
     */
    fun <T> validateData(data: T, validationRules: ValidationRules): ValidationResult
    
    /**
     * Cache data with a specified key
     * @param key The key to identify the cached data
     * @param data The data to cache
     * @param expirationTimeMillis The time in milliseconds after which the cache entry expires
     * @return True if the data was cached successfully, false otherwise
     */
    fun <T> cacheData(key: String, data: T, expirationTimeMillis: Long = DEFAULT_CACHE_EXPIRATION_MILLIS): Boolean
    
    /**
     * Get cached data for a key
     * @param key The key identifying the cached data
     * @return The cached data or null if not found or expired
     */
    fun <T> getCachedData(key: String): T?
    
    /**
     * Check if cached data exists and is not expired
     * @param key The key identifying the cached data
     * @return True if valid cached data exists, false otherwise
     */
    fun hasCachedData(key: String): Boolean
    
    /**
     * Remove cached data
     * @param key The key identifying the cached data
     * @return True if the cached data was removed successfully, false otherwise
     */
    fun removeCachedData(key: String): Boolean
    
    /**
     * Clear all cached data
     */
    fun clearCache()
    
    /**
     * Purge expired cache entries
     * @return The number of entries purged
     */
    fun purgeExpiredCache(): Int
    
    /**
     * Get cache statistics
     * @return Statistics about the cache
     */
    fun getCacheStats(): CacheStats
    
    /**
     * Data class representing validation rules
     */
    data class ValidationRules(
        val required: Boolean = false,
        val minLength: Int? = null,
        val maxLength: Int? = null,
        val pattern: String? = null,
        val customValidator: ((Any?) -> Boolean)? = null
    )
    
    /**
     * Data class representing a validation result
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    )
    
    /**
     * Data class representing cache statistics
     */
    data class CacheStats(
        val totalEntries: Int,
        val expiredEntries: Int,
        val averageEntryAgeMillis: Long,
        val oldestEntryAgeMillis: Long,
        val newestEntryAgeMillis: Long,
        val totalSizeBytes: Long
    )
    
    companion object {
        /**
         * Default cache expiration time (1 hour)
         */
        const val DEFAULT_CACHE_EXPIRATION_MILLIS = 60 * 60 * 1000L
        
        /**
         * Short cache expiration time (5 minutes)
         */
        const val SHORT_CACHE_EXPIRATION_MILLIS = 5 * 60 * 1000L
        
        /**
         * Long cache expiration time (1 day)
         */
        const val LONG_CACHE_EXPIRATION_MILLIS = 24 * 60 * 60 * 1000L
    }
}