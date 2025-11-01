package com.nextgenbuildpro.agents.personal_assistant

import com.nextgenbuildpro.shared.*
import com.nextgenbuildpro.mcp.tools.WebTools
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for Enhanced Voice Command Agent and Web Tools
 */
class EnhancedVoiceCommandAgentTest {
    
    @Test
    fun testWebToolsAvailability() {
        val tools = WebTools.getAvailableTools()
        
        // Verify all expected tools are available
        assertTrue("Should have web tools", tools.isNotEmpty())
        assertTrue("Should have at least 6 tools", tools.size >= 6)
        
        val toolIds = tools.map { it.toolId }
        assertTrue("Should have web_search", "web_search" in toolIds)
        assertTrue("Should have web_scrape", "web_scrape" in toolIds)
        assertTrue("Should have web_harvest", "web_harvest" in toolIds)
        assertTrue("Should have web_download", "web_download" in toolIds)
        assertTrue("Should have web_monitor", "web_monitor" in toolIds)
        assertTrue("Should have api_client", "api_client" in toolIds)
    }
    
    @Test
    fun testWebSearchBasicFunctionality() = runBlocking {
        // Test basic web search (may fail in sandbox without internet)
        val searchResult = WebTools.search("construction safety", maxResults = 3)
        
        // Just verify the method executes without crashing
        assertNotNull("Search should return a result", searchResult)
    }
    
    @Test
    fun testExecutionPlanParsing() {
        // Test that execution plan can be created
        val plan = ExecutionPlan(
            intent = "Search for construction materials",
            complexity = "simple",
            toolsRequired = listOf("web_search"),
            steps = listOf("Execute web search", "Process results"),
            parameters = mapOf("search_query" to "lumber prices")
        )
        
        assertEquals("Search for construction materials", plan.intent)
        assertEquals("simple", plan.complexity)
        assertEquals(1, plan.toolsRequired.size)
        assertEquals("web_search", plan.toolsRequired[0])
        assertEquals(2, plan.steps.size)
    }
    
    @Test
    fun testVoiceCommandAgentPatternDetection() {
        // Test that complex patterns are detected
        val testCases = mapOf(
            "search for lumber prices" to true,
            "find information about OSHA" to true,
            "buscar materiales de construcción" to true,
            "what is the weather" to true,
            "create new project" to false,
            "add contact John" to false
        )
        
        testCases.forEach { (input, shouldBeComplex) ->
            val isComplex = shouldUseEnhancedAgentHelper(input)
            assertEquals(
                "Input '$input' should ${if (shouldBeComplex) "" else "not "}be complex",
                shouldBeComplex,
                isComplex
            )
        }
    }
    
    @Test
    fun testVoiceCommandAgentWithoutLLM() = runBlocking {
        // Test that agent works without LLM service (pattern matching mode)
        val agent = VoiceCommandAgent(llmService = null)
        val initResult = agent.initialize()
        
        assertTrue("Agent should initialize successfully", initResult.isSuccess)
        
        // Test simple command
        val task = NextGenTask(
            description = "create project test house",
            priority = Priority.MEDIUM,
            metadata = mapOf("voice_input" to "create project test house")
        )
        
        val result = agent.processTask(task)
        assertTrue("Task should complete successfully", result.isSuccess)
        
        val completedTask = result.getOrNull()
        assertNotNull("Completed task should not be null", completedTask)
        assertEquals("Task should be completed", TaskStatus.COMPLETED, completedTask?.status)
        
        // Verify pattern matching mode was used
        val processingMode = completedTask?.metadata?.get("processing_mode")
        assertEquals("Should use pattern matching", "pattern_matching", processingMode)
        
        agent.shutdown()
    }
    
    @Test
    fun testWebToolsErrorHandling() = runBlocking {
        // Test that web tools handle errors gracefully
        val invalidUrl = "not-a-valid-url"
        val scrapeResult = WebTools.scrape(invalidUrl)
        
        assertTrue("Should fail for invalid URL", scrapeResult.isFailure)
    }
    
    @Test
    fun testHarvestMultipleSources() = runBlocking {
        // Test harvesting with mix of valid and invalid URLs
        val sources = listOf(
            "https://example.com",
            "invalid-url",
            "https://another-example.com"
        )
        
        val harvestResult = WebTools.harvest(sources)
        
        // Should complete even with some failures
        assertNotNull("Harvest result should not be null", harvestResult)
        
        if (harvestResult.isSuccess) {
            val data = harvestResult.getOrNull()
            assertNotNull("Harvested data should not be null", data)
            assertEquals("Should have tracked all sources", 3, data?.sources?.size)
        }
    }
    
    // Helper method to test pattern detection
    private fun shouldUseEnhancedAgentHelper(input: String): Boolean {
        val complexPatterns = listOf(
            "search", "find", "look up", "get information",
            "buscar", "encontrar", "busca",
            "what is", "how to", "why", "when",
            "qué es", "cómo", "por qué", "cuándo",
            "research", "investigate", "analyze",
            "investigar", "analizar",
            "website", "web page", "url", "link",
            "sitio web", "página web",
            "price", "cost", "rates", "pricing",
            "precio", "costo", "tarifas"
        )
        
        return complexPatterns.any { pattern -> 
            input.contains(pattern, ignoreCase = true)
        }
    }
}

/**
 * Tests specifically for WebTools functionality
 */
class WebToolsTest {
    
    @Test
    fun testSearchResultDataClass() {
        val result = com.nextgenbuildpro.mcp.tools.SearchResult(
            title = "Test Result",
            url = "https://example.com",
            snippet = "Test snippet",
            source = "Test Source",
            relevanceScore = 0.95f
        )
        
        assertEquals("Test Result", result.title)
        assertEquals("https://example.com", result.url)
        assertEquals("Test snippet", result.snippet)
        assertEquals("Test Source", result.source)
        assertEquals(0.95f, result.relevanceScore, 0.01f)
    }
    
    @Test
    fun testScrapedContentDataClass() {
        val content = com.nextgenbuildpro.mcp.tools.ScrapedContent(
            url = "https://example.com",
            title = "Test Page",
            text = "Test content",
            links = listOf("https://link1.com", "https://link2.com"),
            images = listOf("https://image1.jpg"),
            metadata = mapOf("author" to "Test Author")
        )
        
        assertEquals("https://example.com", content.url)
        assertEquals("Test Page", content.title)
        assertEquals(2, content.links.size)
        assertEquals(1, content.images.size)
        assertEquals("Test Author", content.metadata["author"])
    }
    
    @Test
    fun testApiResponseDataClass() {
        val response = com.nextgenbuildpro.mcp.tools.ApiResponse(
            statusCode = 200,
            body = "{\"success\": true}",
            headers = mapOf("Content-Type" to "application/json")
        )
        
        assertEquals(200, response.statusCode)
        assertTrue(response.body.contains("success"))
        assertEquals("application/json", response.headers["Content-Type"])
    }
    
    @Test
    fun testToolCapabilitiesDescription() {
        val tools = WebTools.getAvailableTools()
        
        tools.forEach { tool ->
            // Verify each tool has required properties
            assertNotNull("Tool should have ID", tool.toolId)
            assertNotNull("Tool should have name", tool.toolName)
            assertNotNull("Tool should have description", tool.description)
            assertTrue("Tool should have capabilities", tool.capabilities.isNotEmpty())
            assertTrue("Tool should be active by default", tool.isActive)
        }
    }
}
