package com.nextgenbuildpro.mcp.tools

import android.util.Log
import com.nextgenbuildpro.shared.MCPTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Web Tools for MCP - Comprehensive web search, scraping, and data harvesting
 * 
 * Provides agents with capabilities to:
 * - Search the web for information
 * - Scrape content from websites
 * - Extract structured data
 * - Download and process web resources
 * - Parse and analyze web content
 */
object WebTools {
    private const val TAG = "WebTools"
    
    /**
     * Get list of all available web tools
     */
    fun getAvailableTools(): List<MCPTool> = listOf(
        MCPTool(
            toolId = "web_search",
            toolName = "Web Search",
            description = "Search the web using multiple search engines. Supports complex queries and returns ranked results.",
            capabilities = listOf(
                "general_search",
                "news_search", 
                "image_search",
                "academic_search",
                "construction_specific_search"
            )
        ),
        MCPTool(
            toolId = "web_scrape",
            toolName = "Web Scraper",
            description = "Extract content from web pages. Can parse HTML, extract text, images, links, and structured data.",
            capabilities = listOf(
                "html_parsing",
                "content_extraction",
                "link_extraction",
                "metadata_extraction",
                "table_parsing"
            )
        ),
        MCPTool(
            toolId = "web_harvest",
            toolName = "Data Harvester",
            description = "Harvest and aggregate data from multiple sources. Supports batch operations and data consolidation.",
            capabilities = listOf(
                "multi_source_aggregation",
                "data_consolidation",
                "format_conversion",
                "data_validation",
                "incremental_updates"
            )
        ),
        MCPTool(
            toolId = "web_download",
            toolName = "Resource Downloader",
            description = "Download files, documents, and resources from the web. Supports various formats and protocols.",
            capabilities = listOf(
                "file_download",
                "document_parsing",
                "format_conversion",
                "batch_download",
                "resume_support"
            )
        ),
        MCPTool(
            toolId = "web_monitor",
            toolName = "Web Monitor",
            description = "Monitor websites for changes and updates. Can track specific content, prices, or availability.",
            capabilities = listOf(
                "change_detection",
                "content_tracking",
                "price_monitoring",
                "availability_alerts",
                "scheduled_checks"
            )
        ),
        MCPTool(
            toolId = "api_client",
            toolName = "API Client",
            description = "Interact with web APIs. Supports REST, GraphQL, and other API protocols.",
            capabilities = listOf(
                "rest_api_calls",
                "graphql_queries",
                "authentication",
                "rate_limiting",
                "response_caching"
            )
        )
    )
    
    /**
     * Execute a web search query
     */
    suspend fun search(
        query: String,
        searchType: String = "general",
        maxResults: Int = 10
    ): Result<List<SearchResult>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Executing web search: $query (type: $searchType)")
            
            // Use DuckDuckGo as a privacy-friendly search option
            // In production, could use Google Custom Search, Bing API, etc.
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://api.duckduckgo.com/?q=$encodedQuery&format=json&no_html=1"
            
            val results = mutableListOf<SearchResult>()
            val url = URL(searchUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.setRequestProperty("User-Agent", "NextGenBuildPro/1.0")
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()
                    
                    // Parse DuckDuckGo JSON response
                    val jsonResponse = JSONObject(response)
                    
                    // Extract abstract if available
                    if (jsonResponse.has("Abstract") && jsonResponse.getString("Abstract").isNotEmpty()) {
                        results.add(SearchResult(
                            title = jsonResponse.optString("Heading", "DuckDuckGo Result"),
                            url = jsonResponse.optString("AbstractURL", ""),
                            snippet = jsonResponse.getString("Abstract"),
                            source = "DuckDuckGo"
                        ))
                    }
                    
                    // Extract related topics
                    if (jsonResponse.has("RelatedTopics")) {
                        val relatedTopics = jsonResponse.getJSONArray("RelatedTopics")
                        for (i in 0 until minOf(relatedTopics.length(), maxResults - results.size)) {
                            val topic = relatedTopics.getJSONObject(i)
                            if (topic.has("Text")) {
                                results.add(SearchResult(
                                    title = topic.optString("Text", "").take(100),
                                    url = topic.optString("FirstURL", ""),
                                    snippet = topic.optString("Text", ""),
                                    source = "DuckDuckGo"
                                ))
                            }
                        }
                    }
                }
                
                Log.d(TAG, "Search completed: ${results.size} results found")
                Result.success(results)
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Scrape content from a web page
     */
    suspend fun scrape(
        url: String,
        extractionRules: Map<String, String> = emptyMap()
    ): Result<ScrapedContent> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Scraping URL: $url")
            
            val urlObj = URL(url)
            val connection = urlObj.openConnection() as HttpURLConnection
            
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.setRequestProperty("User-Agent", "NextGenBuildPro/1.0")
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val htmlContent = reader.readText()
                    reader.close()
                    
                    // Basic content extraction
                    val scrapedContent = ScrapedContent(
                        url = url,
                        title = extractTitle(htmlContent),
                        text = extractText(htmlContent),
                        links = extractLinks(htmlContent, url),
                        images = extractImages(htmlContent, url),
                        metadata = extractMetadata(htmlContent)
                    )
                    
                    Log.d(TAG, "Scraping completed: ${scrapedContent.text.length} chars")
                    Result.success(scrapedContent)
                } else {
                    Result.failure(Exception("HTTP $responseCode"))
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Scraping failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Harvest data from multiple sources
     */
    suspend fun harvest(
        sources: List<String>,
        consolidationStrategy: String = "merge"
    ): Result<HarvestedData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Harvesting data from ${sources.size} sources")
            
            val allData = mutableListOf<ScrapedContent>()
            val errors = mutableListOf<String>()
            
            sources.forEach { source ->
                val result = scrape(source)
                result.onSuccess { data -> allData.add(data) }
                result.onFailure { error -> errors.add("$source: ${error.message}") }
            }
            
            val harvestedData = HarvestedData(
                sources = sources,
                data = allData,
                consolidatedText = consolidateText(allData, consolidationStrategy),
                errors = errors,
                timestamp = System.currentTimeMillis()
            )
            
            Log.d(TAG, "Harvesting completed: ${allData.size}/${sources.size} successful")
            Result.success(harvestedData)
        } catch (e: Exception) {
            Log.e(TAG, "Harvesting failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Make an API request
     */
    suspend fun apiRequest(
        endpoint: String,
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        body: String? = null
    ): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Making API request: $method $endpoint")
            
            val url = URL(endpoint)
            val connection = url.openConnection() as HttpURLConnection
            
            try {
                connection.requestMethod = method
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                
                // Set headers
                headers.forEach { (key, value) ->
                    connection.setRequestProperty(key, value)
                }
                
                // Send body if present
                if (body != null && method in listOf("POST", "PUT", "PATCH")) {
                    connection.doOutput = true
                    connection.outputStream.use { it.write(body.toByteArray()) }
                }
                
                val responseCode = connection.responseCode
                val reader = BufferedReader(InputStreamReader(
                    if (responseCode < 400) connection.inputStream else connection.errorStream
                ))
                val response = reader.readText()
                reader.close()
                
                val apiResponse = ApiResponse(
                    statusCode = responseCode,
                    body = response,
                    headers = connection.headerFields.mapValues { it.value.firstOrNull() ?: "" }
                )
                
                Log.d(TAG, "API request completed: $responseCode")
                Result.success(apiResponse)
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "API request failed", e)
            Result.failure(e)
        }
    }
    
    // Helper methods for content extraction
    
    private fun extractTitle(html: String): String {
        val titleRegex = Regex("<title>([^<]+)</title>", RegexOption.IGNORE_CASE)
        return titleRegex.find(html)?.groupValues?.get(1)?.trim() ?: "Untitled"
    }
    
    private fun extractText(html: String): String {
        // Remove script and style tags
        var text = html.replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
        text = text.replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
        
        // Remove HTML tags
        text = text.replace(Regex("<[^>]+>"), " ")
        
        // Decode HTML entities
        text = text.replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
        
        // Clean up whitespace
        return text.replace(Regex("\\s+"), " ").trim()
    }
    
    private fun extractLinks(html: String, baseUrl: String): List<String> {
        val linkRegex = Regex("<a[^>]+href=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
        return linkRegex.findAll(html)
            .map { it.groupValues[1] }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .map { resolveUrl(it, baseUrl) }
            .distinct()
            .take(50) // Limit to 50 links
            .toList()
    }
    
    private fun extractImages(html: String, baseUrl: String): List<String> {
        val imgRegex = Regex("<img[^>]+src=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
        return imgRegex.findAll(html)
            .map { it.groupValues[1] }
            .filter { it.isNotEmpty() }
            .map { resolveUrl(it, baseUrl) }
            .distinct()
            .take(20) // Limit to 20 images
            .toList()
    }
    
    private fun extractMetadata(html: String): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        
        // Extract meta tags
        val metaRegex = Regex("<meta[^>]+name=\"([^\"]+)\"[^>]+content=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
        metaRegex.findAll(html).forEach { match ->
            metadata[match.groupValues[1]] = match.groupValues[2]
        }
        
        // Extract og: tags
        val ogRegex = Regex("<meta[^>]+property=\"og:([^\"]+)\"[^>]+content=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
        ogRegex.findAll(html).forEach { match ->
            metadata["og:${match.groupValues[1]}"] = match.groupValues[2]
        }
        
        return metadata
    }
    
    private fun resolveUrl(url: String, baseUrl: String): String {
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("//") -> "https:$url"
            url.startsWith("/") -> {
                val base = URL(baseUrl)
                "${base.protocol}://${base.host}$url"
            }
            else -> {
                val base = URL(baseUrl)
                val path = base.path.substringBeforeLast('/') + "/"
                "${base.protocol}://${base.host}$path$url"
            }
        }
    }
    
    private fun consolidateText(data: List<ScrapedContent>, strategy: String): String {
        return when (strategy) {
            "merge" -> data.joinToString("\n\n") { "${it.title}\n${it.text}" }
            "concatenate" -> data.joinToString(" ") { it.text }
            "summarize" -> {
                // Simple summarization: take first 500 chars from each
                data.joinToString("\n\n") { 
                    "${it.title}\n${it.text.take(500)}${if (it.text.length > 500) "..." else ""}"
                }
            }
            else -> data.joinToString("\n---\n") { it.text }
        }
    }
}

/**
 * Search result data class
 */
data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String,
    val source: String,
    val relevanceScore: Float = 0.0f
)

/**
 * Scraped content data class
 */
data class ScrapedContent(
    val url: String,
    val title: String,
    val text: String,
    val links: List<String>,
    val images: List<String>,
    val metadata: Map<String, String>
)

/**
 * Harvested data from multiple sources
 */
data class HarvestedData(
    val sources: List<String>,
    val data: List<ScrapedContent>,
    val consolidatedText: String,
    val errors: List<String>,
    val timestamp: Long
)

/**
 * API response data class
 */
data class ApiResponse(
    val statusCode: Int,
    val body: String,
    val headers: Map<String, String>
)
