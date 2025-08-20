package com.nextgenbuildpro.core.api.impl

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.core.api.DataValidationAndCachingService
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of DataValidationAndCachingService interface.
 * 
 * This class handles data validation and caching strategies for API data.
 * It provides:
 * 1. Data validation with customizable validation rules
 * 2. In-memory and disk-based caching with expiration times
 * 3. Cache statistics and management
 * 4. Data purging policies
 */
class DataValidationAndCachingServiceImpl(
    private val context: Context
) : DataValidationAndCachingService {
    
    private val TAG = "DataValidationAndCaching"
    private val memoryCache = ConcurrentHashMap<String, CacheEntry<Any>>()
    private val cacheDir: File by lazy { File(context.cacheDir, "api_data_cache") }
    
    init {
        // Create cache directory if it doesn't exist
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    /**
     * Validate data received from an API
     * @param data The data to validate
     * @param validationRules The validation rules to apply
     * @return A validation result indicating success or failure with error details
     */
    override fun <T> validateData(
        data: T,
        validationRules: DataValidationAndCachingService.ValidationRules
    ): DataValidationAndCachingService.ValidationResult {
        val errors = mutableListOf<String>()
        
        try {
            // Check if data is required but null
            if (validationRules.required && data == null) {
                errors.add("Data is required but was null")
                return DataValidationAndCachingService.ValidationResult(false, errors)
            }
            
            // If data is null and not required, it's valid
            if (data == null) {
                return DataValidationAndCachingService.ValidationResult(true)
            }
            
            // Check string length if applicable
            if (data is String) {
                validationRules.minLength?.let { minLength ->
                    if (data.length < minLength) {
                        errors.add("String length ${data.length} is less than minimum length $minLength")
                    }
                }
                
                validationRules.maxLength?.let { maxLength ->
                    if (data.length > maxLength) {
                        errors.add("String length ${data.length} is greater than maximum length $maxLength")
                    }
                }
                
                // Check pattern if applicable
                validationRules.pattern?.let { pattern ->
                    if (!data.matches(Regex(pattern))) {
                        errors.add("String does not match pattern: $pattern")
                    }
                }
            }
            
            // Apply custom validator if provided
            validationRules.customValidator?.let { validator ->
                if (!validator(data)) {
                    errors.add("Custom validation failed")
                }
            }
            
            return DataValidationAndCachingService.ValidationResult(errors.isEmpty(), errors)
        } catch (e: Exception) {
            Log.e(TAG, "Error validating data", e)
            errors.add("Validation error: ${e.message}")
            return DataValidationAndCachingService.ValidationResult(false, errors)
        }
    }
    
    /**
     * Cache data with a specified key
     * @param key The key to identify the cached data
     * @param data The data to cache
     * @param expirationTimeMillis The time in milliseconds after which the cache entry expires
     * @return True if the data was cached successfully, false otherwise
     */
    override fun <T> cacheData(
        key: String,
        data: T,
        expirationTimeMillis: Long
    ): Boolean {
        try {
            // Only cache Serializable data
            if (data !is Serializable) {
                Log.e(TAG, "Cannot cache non-serializable data for key: $key")
                return false
            }
            
            val timestamp = System.currentTimeMillis()
            val expirationTime = timestamp + expirationTimeMillis
            
            // Store in memory cache
            memoryCache[key] = CacheEntry(data as Any, timestamp, expirationTime)
            
            // Store on disk
            val cacheFile = File(cacheDir, key.hashCode().toString())
            ObjectOutputStream(FileOutputStream(cacheFile)).use { outputStream ->
                outputStream.writeObject(CacheEntry(data, timestamp, expirationTime))
            }
            
            Log.d(TAG, "Data cached successfully for key: $key")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error caching data for key: $key", e)
            return false
        }
    }
    
    /**
     * Get cached data for a key
     * @param key The key identifying the cached data
     * @return The cached data or null if not found or expired
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> getCachedData(key: String): T? {
        try {
            // Check memory cache first
            val memoryCacheEntry = memoryCache[key]
            if (memoryCacheEntry != null) {
                // Check if expired
                if (System.currentTimeMillis() < memoryCacheEntry.expirationTime) {
                    return memoryCacheEntry.data as T
                } else {
                    // Remove expired entry from memory cache
                    memoryCache.remove(key)
                }
            }
            
            // Check disk cache
            val cacheFile = File(cacheDir, key.hashCode().toString())
            if (cacheFile.exists()) {
                ObjectInputStream(FileInputStream(cacheFile)).use { inputStream ->
                    val diskCacheEntry = inputStream.readObject() as CacheEntry<*>
                    
                    // Check if expired
                    if (System.currentTimeMillis() < diskCacheEntry.expirationTime) {
                        // Update memory cache
                        memoryCache[key] = diskCacheEntry as CacheEntry<Any>
                        return diskCacheEntry.data as T
                    } else {
                        // Remove expired file
                        cacheFile.delete()
                    }
                }
            }
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached data for key: $key", e)
            return null
        }
    }
    
    /**
     * Check if cached data exists and is not expired
     * @param key The key identifying the cached data
     * @return True if valid cached data exists, false otherwise
     */
    override fun hasCachedData(key: String): Boolean {
        try {
            // Check memory cache first
            val memoryCacheEntry = memoryCache[key]
            if (memoryCacheEntry != null) {
                // Check if expired
                return System.currentTimeMillis() < memoryCacheEntry.expirationTime
            }
            
            // Check disk cache
            val cacheFile = File(cacheDir, key.hashCode().toString())
            if (cacheFile.exists()) {
                ObjectInputStream(FileInputStream(cacheFile)).use { inputStream ->
                    val diskCacheEntry = inputStream.readObject() as CacheEntry<*>
                    
                    // Check if expired
                    return System.currentTimeMillis() < diskCacheEntry.expirationTime
                }
            }
            
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if cached data exists for key: $key", e)
            return false
        }
    }
    
    /**
     * Remove cached data
     * @param key The key identifying the cached data
     * @return True if the cached data was removed successfully, false otherwise
     */
    override fun removeCachedData(key: String): Boolean {
        try {
            // Remove from memory cache
            memoryCache.remove(key)
            
            // Remove from disk cache
            val cacheFile = File(cacheDir, key.hashCode().toString())
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
            
            Log.d(TAG, "Cached data removed for key: $key")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing cached data for key: $key", e)
            return false
        }
    }
    
    /**
     * Clear all cached data
     */
    override fun clearCache() {
        try {
            // Clear memory cache
            memoryCache.clear()
            
            // Clear disk cache
            cacheDir.listFiles()?.forEach { file ->
                file.delete()
            }
            
            Log.d(TAG, "Cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }
    
    /**
     * Purge expired cache entries
     * @return The number of entries purged
     */
    override fun purgeExpiredCache(): Int {
        var purgedCount = 0
        
        try {
            val currentTime = System.currentTimeMillis()
            
            // Purge memory cache
            val expiredMemoryKeys = memoryCache.entries
                .filter { currentTime >= it.value.expirationTime }
                .map { it.key }
            
            expiredMemoryKeys.forEach { key ->
                memoryCache.remove(key)
                purgedCount++
            }
            
            // Purge disk cache
            cacheDir.listFiles()?.forEach { file ->
                try {
                    ObjectInputStream(FileInputStream(file)).use { inputStream ->
                        val diskCacheEntry = inputStream.readObject() as CacheEntry<*>
                        
                        if (currentTime >= diskCacheEntry.expirationTime) {
                            file.delete()
                            purgedCount++
                        }
                    }
                } catch (e: Exception) {
                    // If we can't read the file, delete it
                    file.delete()
                    purgedCount++
                }
            }
            
            Log.d(TAG, "Purged $purgedCount expired cache entries")
            return purgedCount
        } catch (e: Exception) {
            Log.e(TAG, "Error purging expired cache", e)
            return purgedCount
        }
    }
    
    /**
     * Get cache statistics
     * @return Statistics about the cache
     */
    override fun getCacheStats(): DataValidationAndCachingService.CacheStats {
        try {
            val currentTime = System.currentTimeMillis()
            
            // Get memory cache stats
            val memoryCacheEntries = memoryCache.values.toList()
            val memoryCacheSize = memoryCacheEntries.size
            
            // Get disk cache stats
            val diskCacheFiles = cacheDir.listFiles() ?: emptyArray()
            val diskCacheSize = diskCacheFiles.size
            
            // Calculate total entries
            val totalEntries = memoryCacheSize + diskCacheSize
            
            // Calculate expired entries
            val expiredMemoryEntries = memoryCacheEntries.count { currentTime >= it.expirationTime }
            var expiredDiskEntries = 0
            var totalSizeBytes = 0L
            
            diskCacheFiles.forEach { file ->
                totalSizeBytes += file.length()
                
                try {
                    ObjectInputStream(FileInputStream(file)).use { inputStream ->
                        val diskCacheEntry = inputStream.readObject() as CacheEntry<*>
                        
                        if (currentTime >= diskCacheEntry.expirationTime) {
                            expiredDiskEntries++
                        }
                    }
                } catch (e: Exception) {
                    // If we can't read the file, consider it expired
                    expiredDiskEntries++
                }
            }
            
            val expiredEntries = expiredMemoryEntries + expiredDiskEntries
            
            // Calculate entry ages
            val entryAges = memoryCacheEntries.map { currentTime - it.timestamp }
            val averageEntryAgeMillis = if (entryAges.isNotEmpty()) entryAges.average().toLong() else 0L
            val oldestEntryAgeMillis = entryAges.maxOrNull() ?: 0L
            val newestEntryAgeMillis = entryAges.minOrNull() ?: 0L
            
            return DataValidationAndCachingService.CacheStats(
                totalEntries = totalEntries,
                expiredEntries = expiredEntries,
                averageEntryAgeMillis = averageEntryAgeMillis,
                oldestEntryAgeMillis = oldestEntryAgeMillis,
                newestEntryAgeMillis = newestEntryAgeMillis,
                totalSizeBytes = totalSizeBytes
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cache stats", e)
            return DataValidationAndCachingService.CacheStats(
                totalEntries = 0,
                expiredEntries = 0,
                averageEntryAgeMillis = 0,
                oldestEntryAgeMillis = 0,
                newestEntryAgeMillis = 0,
                totalSizeBytes = 0
            )
        }
    }
    
    /**
     * Data class representing a cache entry
     */
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long,
        val expirationTime: Long
    ) : Serializable
}