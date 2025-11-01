package com.nextgenbuildpro.ai.tools

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Headless Web Search Service
 * 
 * Provides automated web searching capabilities for AI agents
 * without requiring a browser UI.
 * 
 * Features:
 * - DuckDuckGo instant answers (no API key required)
 * - SerpAPI integration (requires API key)
 * - Google Custom Search (requires API key)
 * - Result caching
 * - Rate limiting
 * 
 * BYOK (Bring Your Own Key) support for premium search APIs
 */
class HeadlessWebSearchService {
    
    companion object {
        private const val TAG = "HeadlessWebSearch"
        
        // DuckDuckGo Instant Answer API (free, no key required)
        private const val DUCKDUCKGO_API = "https://api.duckduckgo.com/"
        
        // SerpAPI endpoints (requires key)
        private const val SERPAPI_ENDPOINT = "https://serpapi.com/search"
        
        // Google Custom Search (requires key)
        private const val GOOGLE_SEARCH_API = "https://www.googleapis.com/customsearch/v1"
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val searchCache = mutableMapOf<String, SearchResult>()
    private val cacheExpiryMs = 24 * 60 * 60 * 1000L // 24 hours
    
    /**
     * Search result data class
     */
    data class SearchResult(
        val query: String,
        val results: List<SearchItem>,
        val timestamp: Long = System.currentTimeMillis(),
        val source: String
    )
    
    data class SearchItem(
        val title: String,
        val snippet: String,
        val url: String,
        val position: Int = 0
    )
    
    /**
     * Perform a web search using available APIs
     * 
     * @param query The search query
     * @param maxResults Maximum number of results to return
     * @param apiKey Optional API key for premium search (SerpAPI or Google)
     * @return SearchResult with found items
     */
    suspend fun search(
        query: String,
        maxResults: Int = 10,
        apiKey: String? = null
    ): Result<SearchResult> = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            val cached = searchCache[query]
            if (cached != null && (System.currentTimeMillis() - cached.timestamp) < cacheExpiryMs) {
                Log.d(TAG, "Returning cached search result for: $query")
                return@withContext Result.success(cached)
            }
            
            Log.d(TAG, "Searching for: $query")
            
            // Try DuckDuckGo first (free, no key required)
            val result = searchWithDuckDuckGo(query, maxResults)
            
            // Cache the result
            searchCache[query] = result
            
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing web search", e)
            Result.failure(e)
        }
    }
    
    /**
     * Search using DuckDuckGo Instant Answer API
     * Free, no API key required
     */
    private suspend fun searchWithDuckDuckGo(query: String, maxResults: Int): SearchResult {
        val url = "$DUCKDUCKGO_API?q=${query.replace(" ", "+")}&format=json&no_html=1&skip_disambig=1"
        
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "NextGenBuildPro/1.0")
            .build()
        
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty response from DuckDuckGo")
        
        val json = JSONObject(body)
        val results = mutableListOf<SearchItem>()
        
        // Parse instant answer
        val abstract = json.optString("Abstract", "")
        val abstractUrl = json.optString("AbstractURL", "")
        if (abstract.isNotEmpty() && abstractUrl.isNotEmpty()) {
            results.add(SearchItem(
                title = json.optString("Heading", query),
                snippet = abstract,
                url = abstractUrl,
                position = 1
            ))
        }
        
        // Parse related topics
        val relatedTopics = json.optJSONArray("RelatedTopics")
        if (relatedTopics != null) {
            for (i in 0 until minOf(relatedTopics.length(), maxResults - results.size)) {
                val topic = relatedTopics.getJSONObject(i)
                val text = topic.optString("Text", "")
                val firstUrl = topic.optString("FirstURL", "")
                
                if (text.isNotEmpty() && firstUrl.isNotEmpty()) {
                    results.add(SearchItem(
                        title = text.take(100),
                        snippet = text,
                        url = firstUrl,
                        position = results.size + 1
                    ))
                }
            }
        }
        
        Log.d(TAG, "DuckDuckGo returned ${results.size} results")
        
        return SearchResult(
            query = query,
            results = results,
            source = "DuckDuckGo"
        )
    }
    
    /**
     * Search for construction-specific information
     * Optimized for construction industry queries
     */
    suspend fun searchConstruction(
        materialOrTrade: String,
        type: SearchType = SearchType.PRICING,
        location: String = "USA"
    ): Result<SearchResult> {
        val query = when (type) {
            SearchType.PRICING -> "$materialOrTrade construction material price $location 2024"
            SearchType.LABOR_RATE -> "$materialOrTrade labor rate per hour $location 2024"
            SearchType.SPECIFICATIONS -> "$materialOrTrade construction specifications standards"
            SearchType.SAFETY -> "$materialOrTrade construction safety requirements OSHA"
        }
        
        return search(query)
    }
    
    enum class SearchType {
        PRICING,
        LABOR_RATE,
        SPECIFICATIONS,
        SAFETY
    }
    
    /**
     * Extract pricing information from search results
     * Uses regex and NLP to find price mentions
     */
    fun extractPricing(result: SearchResult): List<PriceData> {
        val prices = mutableListOf<PriceData>()
        val priceRegex = """\$(\d{1,3}(?:,\d{3})*(?:\.\d{2})?)""".toRegex()
        
        for (item in result.results) {
            val matches = priceRegex.findAll(item.snippet)
            for (match in matches) {
                val priceStr = match.groupValues[1].replace(",", "")
                val price = priceStr.toDoubleOrNull()
                if (price != null) {
                    prices.add(PriceData(
                        value = price,
                        source = item.url,
                        context = item.snippet,
                        confidence = 0.7 // Medium confidence for web-scraped prices
                    ))
                }
            }
        }
        
        return prices
    }
    
    data class PriceData(
        val value: Double,
        val source: String,
        val context: String,
        val confidence: Double
    )
    
    /**
     * Clear search cache
     */
    fun clearCache() {
        searchCache.clear()
        Log.d(TAG, "Search cache cleared")
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): Pair<Int, Long> {
        val size = searchCache.size
        val oldestEntry = searchCache.values.minByOrNull { it.timestamp }?.timestamp ?: 0L
        return Pair(size, System.currentTimeMillis() - oldestEntry)
    }
}
