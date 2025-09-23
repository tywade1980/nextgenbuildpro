package com.nextgenbuildpro.pm.service

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Performance optimization service for large catalogue operations
 * Implements caching, batching, and async processing for better performance
 */
class CataloguePerformanceOptimizer(private val context: Context) {
    private val TAG = "CataloguePerformanceOptimizer"
    
    // Cache for search results
    private val searchCache = ConcurrentHashMap<String, CachedSearchResult>()
    private val assembliesCache = ConcurrentHashMap<String, Assembly>()
    
    // Batch processing
    private val batchProcessor = Channel<BatchRequest>(Channel.UNLIMITED)
    private val batchResults = Channel<BatchResult>(Channel.UNLIMITED)
    
    // Configuration
    private val cacheExpirationMs = 5 * 60 * 1000L // 5 minutes
    private val maxCacheSize = 1000
    private val batchSize = 50
    
    init {
        // Start batch processor
        startBatchProcessor()
    }
    
    /**
     * Optimized assembly search with caching and indexing
     */
    suspend fun optimizedSearch(
        keyword: String? = null,
        tradeType: String? = null,
        contextMode: ContextMode? = null,
        projectType: String? = null
    ): List<AssemblySearchResult> = withContext(Dispatchers.IO) {
        try {
            val cacheKey = generateCacheKey(keyword, tradeType, contextMode, projectType)
            
            // Check cache first
            val cachedResult = getCachedResult(cacheKey)
            if (cachedResult != null) {
                Log.d(TAG, "Cache hit for search: $cacheKey")
                return@withContext cachedResult.results
            }
            
            // Perform search with optimization
            val results = performOptimizedSearch(keyword, tradeType, contextMode, projectType)
            
            // Cache results
            cacheSearchResult(cacheKey, results)
            
            results
        } catch (e: Exception) {
            Log.e(TAG, "Optimized search failed: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Bulk assembly loading with batch processing
     */
    suspend fun bulkLoadAssemblies(assemblyIds: List<String>): Map<String, Assembly> = withContext(Dispatchers.IO) {
        try {
            val resultMap = mutableMapOf<String, Assembly>()
            val uncachedIds = mutableListOf<String>()
            
            // Check cache for each assembly
            assemblyIds.forEach { id ->
                val cached = assembliesCache[id]
                if (cached != null) {
                    resultMap[id] = cached
                } else {
                    uncachedIds.add(id)
                }
            }
            
            // Batch load uncached assemblies
            if (uncachedIds.isNotEmpty()) {
                val batchResults = loadAssembliesBatch(uncachedIds)
                resultMap.putAll(batchResults)
                
                // Cache the loaded assemblies
                batchResults.forEach { (id, assembly) ->
                    assembliesCache[id] = assembly
                }
            }
            
            resultMap
        } catch (e: Exception) {
            Log.e(TAG, "Bulk assembly loading failed: ${e.message}")
            emptyMap()
        }
    }
    
    /**
     * Parallel calculation processing for multiple estimates
     */
    suspend fun parallelCalculateEstimates(estimates: List<TemplateEstimate>): List<EstimateCalculationResult> = withContext(Dispatchers.IO) {
        try {
            val calculationEngine = CalculationEngineService()
            
            // Process estimates in parallel
            estimates.map { estimate ->
                async {
                    val startTime = System.currentTimeMillis()
                    
                    try {
                        // Calculate assembly totals
                        val assemblyCalculations = estimate.assemblies.map { assembly ->
                            calculationEngine.calculateTemplateAssemblyTotals(assembly)
                        }
                        
                        // Calculate section totals
                        val sectionCalculations = assemblyCalculations.chunked(10).map { chunk ->
                            val lineItems = chunk.map { calc ->
                                LineItemCalculation(
                                    laborCost = calc.laborTotal,
                                    materialCost = calc.materialTotal,
                                    total = calc.total
                                )
                            }
                            calculationEngine.calculateSectionTotals(lineItems)
                        }
                        
                        // Calculate final estimate total
                        val estimateCalculation = calculationEngine.calculateEstimateTotals(
                            sectionCalculations,
                            TaxSettings(TaxType.PERCENTAGE, rate = 8.25),
                            MarkupSettings(MarkupType.PERCENTAGE, value = 20.0)
                        )
                        
                        val duration = System.currentTimeMillis() - startTime
                        
                        EstimateCalculationResult(
                            estimateId = estimate.id,
                            calculation = estimateCalculation,
                            processingTimeMs = duration,
                            success = true
                        )
                    } catch (e: Exception) {
                        EstimateCalculationResult(
                            estimateId = estimate.id,
                            error = e.message ?: "Unknown error",
                            success = false
                        )
                    }
                }
            }.awaitAll()
        } catch (e: Exception) {
            Log.e(TAG, "Parallel calculation failed: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Memory-efficient large dataset processing with streaming
     */
    fun processLargeDatasetStream(
        assemblies: Flow<Assembly>,
        processor: suspend (Assembly) -> AssemblySearchResult?
    ): Flow<AssemblySearchResult> = flow {
        assemblies
            .buffer(batchSize)
            .map { assembly ->
                try {
                    processor(assembly)
                } catch (e: Exception) {
                    Log.w(TAG, "Error processing assembly ${assembly.id}: ${e.message}")
                    null
                }
            }
            .filterNotNull()
            .collect { result ->
                emit(result)
            }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Preload and index frequently accessed assemblies
     */
    suspend fun preloadFrequentAssemblies(contextMode: ContextMode) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Preloading frequent assemblies for context: $contextMode")
            
            // Get frequently used assemblies based on context mode
            val frequentAssemblyIds = getFrequentAssemblyIds(contextMode)
            
            // Preload in batches
            frequentAssemblyIds.chunked(batchSize).forEach { batch ->
                launch {
                    bulkLoadAssemblies(batch)
                }
            }
            
            Log.d(TAG, "Preloaded ${frequentAssemblyIds.size} frequent assemblies")
        } catch (e: Exception) {
            Log.e(TAG, "Preloading failed: ${e.message}")
        }
    }
    
    /**
     * Search indexing for faster text searches
     */
    suspend fun buildSearchIndex(assemblies: List<Assembly>): SearchIndex = withContext(Dispatchers.Default) {
        try {
            val index = SearchIndex()
            
            assemblies.forEach { assembly ->
                // Index assembly name
                indexText(assembly.name, assembly.id, index.nameIndex)
                
                // Index description
                indexText(assembly.description, assembly.id, index.descriptionIndex)
                
                // Index trade name
                indexText(assembly.tradeName, assembly.id, index.tradeIndex)
                
                // Index tags
                assembly.tags.forEach { tag ->
                    indexText(tag, assembly.id, index.tagIndex)
                }
                
                // Index materials
                assembly.materials.forEach { material ->
                    indexText(material.name, assembly.id, index.materialIndex)
                }
            }
            
            Log.d(TAG, "Built search index for ${assemblies.size} assemblies")
            index
        } catch (e: Exception) {
            Log.e(TAG, "Search index building failed: ${e.message}")
            SearchIndex()
        }
    }
    
    /**
     * Clear caches to free memory
     */
    fun clearCaches() {
        searchCache.clear()
        assembliesCache.clear()
        Log.d(TAG, "Caches cleared")
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            searchCacheSize = searchCache.size,
            assemblyCacheSize = assembliesCache.size,
            searchHitRate = calculateHitRate(searchCache.values),
            memoryUsageKB = estimateMemoryUsage()
        )
    }
    
    // Private helper methods
    
    private suspend fun performOptimizedSearch(
        keyword: String?,
        tradeType: String?,
        contextMode: ContextMode?,
        projectType: String?
    ): List<AssemblySearchResult> {
        // Implement optimized search logic here
        // This would use search indexes, filters, and other optimizations
        return emptyList() // Placeholder
    }
    
    private fun generateCacheKey(
        keyword: String?,
        tradeType: String?,
        contextMode: ContextMode?,
        projectType: String?
    ): String {
        return listOfNotNull(
            keyword?.lowercase(),
            tradeType?.lowercase(),
            contextMode?.name?.lowercase(),
            projectType?.lowercase()
        ).joinToString("|")
    }
    
    private fun getCachedResult(cacheKey: String): CachedSearchResult? {
        val cached = searchCache[cacheKey]
        return if (cached != null && !cached.isExpired()) {
            cached
        } else {
            searchCache.remove(cacheKey)
            null
        }
    }
    
    private fun cacheSearchResult(cacheKey: String, results: List<AssemblySearchResult>) {
        // Implement LRU eviction if cache is full
        if (searchCache.size >= maxCacheSize) {
            val oldestKey = searchCache.entries.minByOrNull { it.value.timestamp }?.key
            oldestKey?.let { searchCache.remove(it) }
        }
        
        searchCache[cacheKey] = CachedSearchResult(results, System.currentTimeMillis())
    }
    
    private suspend fun loadAssembliesBatch(assemblyIds: List<String>): Map<String, Assembly> {
        // Implement batch loading logic
        return emptyMap() // Placeholder
    }
    
    private fun startBatchProcessor() {
        CoroutineScope(Dispatchers.IO).launch {
            // Implement batch processing logic
            // This would collect requests and process them in batches
        }
    }
    
    private fun getFrequentAssemblyIds(contextMode: ContextMode): List<String> {
        // Return frequently used assembly IDs based on context mode
        return when (contextMode) {
            ContextMode.REMODELING -> listOf("framing-interior-wall", "drywall-standard", "paint-interior")
            ContextMode.NEW_CONSTRUCTION -> listOf("framing-exterior-wall", "foundation", "roofing")
            else -> emptyList()
        }
    }
    
    private fun indexText(text: String, assemblyId: String, index: MutableMap<String, MutableSet<String>>) {
        val words = text.lowercase().split("\\s+".toRegex())
        words.forEach { word ->
            if (word.length > 2) { // Ignore very short words
                index.getOrPut(word) { mutableSetOf() }.add(assemblyId)
            }
        }
    }
    
    private fun calculateHitRate(cachedResults: Collection<CachedSearchResult>): Double {
        if (cachedResults.isEmpty()) return 0.0
        val hits = cachedResults.count { !it.isExpired() }
        return hits.toDouble() / cachedResults.size
    }
    
    private fun estimateMemoryUsage(): Long {
        // Rough estimation of memory usage in KB
        val searchCacheMemory = searchCache.size * 1024L // Rough estimate
        val assemblyCacheMemory = assembliesCache.size * 2048L // Rough estimate
        return (searchCacheMemory + assemblyCacheMemory) / 1024
    }
}

// Data classes for performance optimization

data class CachedSearchResult(
    val results: List<AssemblySearchResult>,
    val timestamp: Long
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - timestamp > 5 * 60 * 1000L // 5 minutes
    }
}

data class SearchIndex(
    val nameIndex: MutableMap<String, MutableSet<String>> = mutableMapOf(),
    val descriptionIndex: MutableMap<String, MutableSet<String>> = mutableMapOf(),
    val tradeIndex: MutableMap<String, MutableSet<String>> = mutableMapOf(),
    val tagIndex: MutableMap<String, MutableSet<String>> = mutableMapOf(),
    val materialIndex: MutableMap<String, MutableSet<String>> = mutableMapOf()
)

data class EstimateCalculationResult(
    val estimateId: String,
    val calculation: EstimateCalculation? = null,
    val processingTimeMs: Long = 0,
    val success: Boolean,
    val error: String? = null
)

data class CacheStats(
    val searchCacheSize: Int,
    val assemblyCacheSize: Int,
    val searchHitRate: Double,
    val memoryUsageKB: Long
)

data class BatchRequest(
    val type: String,
    val data: Any
)

data class BatchResult(
    val requestId: String,
    val result: Any?,
    val success: Boolean,
    val error: String? = null
)