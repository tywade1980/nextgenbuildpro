package com.nextgenbuildpro.ai.llm

import com.nextgenbuildpro.core.api.FirestoreService
import com.nextgenbuildpro.shared.AgentType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Test suite for LLM Service
 *
 * Tests OpenAI integration, construction domain expertise,
 * multi-turn conversations, and agent coordination.
 */
class LLMServiceTest {

    @Mock
    private lateinit var mockFirestoreService: FirestoreService

    private lateinit var llmService: LLMServiceImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        // Use a mock API key for testing - in real implementation, this would be securely injected
        llmService = LLMServiceImpl(mockFirestoreService, "test-api-key")
    }

    @Test
    fun `test agent context retrieval`() = runTest {
        // When
        val contextResult = llmService.getAgentContext(AgentType.ORCHESTRATOR)

        // Then
        assertTrue("Context retrieval should succeed", contextResult.isSuccess)
        val context = contextResult.getOrNull()
        assertNotNull("Context should not be null", context)
        assertEquals("Agent type should match", AgentType.ORCHESTRATOR, context?.agentType)
        assertTrue("Should have system prompt", context?.systemPrompt?.isNotEmpty() == true)
        assertTrue("Should have capabilities", context?.capabilities?.isNotEmpty() == true)
    }

    @Test
    fun `test construction domain expertise prompts`() = runTest {
        // Test different agent types have appropriate construction expertise
        val agentTypes = listOf(
            AgentType.ORCHESTRATOR,
            AgentType.PROJECT_MANAGEMENT_ORCHESTRATOR,
            AgentType.CRM_ORCHESTRATOR,
            AgentType.ESTIMATING_DEPARTMENT_ORCHESTRATOR,
            AgentType.ANALYTICS_ORCHESTRATOR,
            AgentType.DESIGN_DEPARTMENT_ORCHESTRATOR,
            AgentType.MARKETING_ORCHESTRATOR
        )

        for (agentType in agentTypes) {
            val contextResult = llmService.getAgentContext(agentType)
            assertTrue("Context for $agentType should succeed", contextResult.isSuccess)

            val context = contextResult.getOrNull()
            assertNotNull("Context for $agentType should not be null", context)

            // Verify construction-specific terminology in prompts
            val prompt = context?.systemPrompt?.lowercase()
            assertTrue("$agentType should have construction expertise",
                prompt?.contains("construction") == true ||
                prompt?.contains("building") == true ||
                prompt?.contains("project") == true)
        }
    }

    @Test
    fun `test multi-turn conversation handling`() = runTest {
        // Given
        val conversationId = "test_conversation_001"
        val context = LLMContext(
            conversationId = conversationId,
            systemPrompt = "You are a construction project manager.",
            previousMessages = listOf(
                LLMMessage("user", "What's the status of the foundation work?", agentType = AgentType.PROJECT_MANAGEMENT_ORCHESTRATOR),
                LLMMessage("assistant", "The foundation work is 75% complete and on schedule.")
            )
        )

        // When
        val responseResult = llmService.generateResponse(
            prompt = "What about the framing work?",
            context = context,
            agentType = AgentType.PROJECT_MANAGEMENT_ORCHESTRATOR
        )

        // Then
        assertTrue("Response generation should succeed", responseResult.isSuccess)
        val response = responseResult.getOrNull()
        assertNotNull("Response should not be null", response)
        assertEquals("Conversation ID should match", conversationId, response?.conversationId)
        assertTrue("Should have token usage", response?.tokenUsage?.totalTokens ?: 0 > 0)
    }

    @Test
    fun `test multi-agent coordination response generation`() = runTest {
        // Given
        val coordinationRequest = MultiAgentCoordinationRequest(
            requestingAgent = AgentType.ORCHESTRATOR,
            targetAgents = listOf(AgentType.PROJECT_MANAGEMENT_ORCHESTRATOR, AgentType.CRM_ORCHESTRATOR),
            task = "Coordinate client meeting for project status update",
            context = "Client ABC Corp wants to review progress on their warehouse construction",
            priority = "HIGH"
        )

        // When
        val coordinationResult = llmService.generateCoordinationResponse(coordinationRequest)

        // Then
        assertTrue("Coordination response should succeed", coordinationResult.isSuccess)
        val coordination = coordinationResult.getOrNull()
        assertNotNull("Coordination response should not be null", coordination)
        assertTrue("Should have a plan", coordination?.plan?.isNotEmpty() == true)
        assertTrue("Should have agent assignments", coordination?.agentAssignments?.isNotEmpty() == true)
        assertTrue("Should have estimated duration", (coordination?.estimatedDuration ?: 0) > 0)
    }

    @Test
    fun `test conversation storage and retrieval`() = runTest {
        // Given
        val testConversation = LLMConversation(
            id = "test_conv_001",
            participants = listOf(AgentType.ORCHESTRATOR),
            messages = listOf(
                LLMMessage("user", "Test message", agentType = AgentType.ORCHESTRATOR),
                LLMMessage("assistant", "Test response")
            ),
            startTime = java.time.LocalDateTime.now(),
            lastUpdate = java.time.LocalDateTime.now()
        )

        // When - Store conversation
        val storeResult = llmService.storeConversation(testConversation)

        // Then
        assertTrue("Conversation storage should succeed", storeResult.isSuccess)

        // When - Retrieve conversation
        val retrieveResult = llmService.getConversationHistory(testConversation.id)

        // Then
        assertTrue("Conversation retrieval should succeed", retrieveResult.isSuccess)
        val retrieved = retrieveResult.getOrNull()
        assertNotNull("Retrieved conversation should not be null", retrieved)
        assertEquals("Conversation ID should match", testConversation.id, retrieved?.id)
        assertEquals("Message count should match", testConversation.messages.size, retrieved?.messages?.size)
    }

    @Test
    fun `test construction-specific query handling`() = runTest {
        // Test various construction-specific queries
        val constructionQueries = listOf(
            "What's the typical concrete curing time for a foundation?",
            "How do I calculate the load-bearing capacity of a beam?",
            "What are the OSHA requirements for scaffolding safety?",
            "How should I schedule electrical work in a commercial building?",
            "What's the standard markup for construction estimating?"
        )

        for (query in constructionQueries) {
            val responseResult = llmService.generateResponse(
                prompt = query,
                context = null,
                agentType = AgentType.ESTIMATING_DEPARTMENT_ORCHESTRATOR
            )

            assertTrue("Response for '$query' should succeed", responseResult.isSuccess)
            val response = responseResult.getOrNull()
            assertNotNull("Response for '$query' should not be null", response)
            assertTrue("Response should have content", response?.content?.isNotEmpty() == true)
        }
    }

    @Test
    fun `test error handling and fallback responses`() = runTest {
        // Given - Invalid API key to force fallback
        val invalidLlmService = LLMServiceImpl(mockFirestoreService, "invalid-key")

        // When
        val responseResult = invalidLlmService.generateResponse(
            prompt = "Test construction query",
            context = null,
            agentType = AgentType.ORCHESTRATOR
        )

        // Then - Should still return a response (fallback)
        assertTrue("Should handle API errors gracefully", responseResult.isSuccess)
        val response = responseResult.getOrNull()
        assertNotNull("Fallback response should be provided", response)
        assertTrue("Fallback response should have content", response?.content?.isNotEmpty() == true)
    }

    @Test
    fun `test agent capability definitions`() = runTest {
        // Test that each agent has appropriate capabilities defined
        val agentCapabilities = mapOf(
            AgentType.ORCHESTRATOR to listOf("Task Coordination", "System Management"),
            AgentType.PROJECT_MANAGEMENT_ORCHESTRATOR to listOf("Project Planning", "Resource Allocation"),
            AgentType.CRM_ORCHESTRATOR to listOf("Contact Management", "Lead Management"),
            AgentType.ESTIMATING_DEPARTMENT_ORCHESTRATOR to listOf("Cost Analysis", "Bidding"),
            AgentType.ANALYTICS_ORCHESTRATOR to listOf("Data Analysis", "Reporting"),
            AgentType.DESIGN_DEPARTMENT_ORCHESTRATOR to listOf("Design Coordination", "Blueprint Management"),
            AgentType.MARKETING_ORCHESTRATOR to listOf("Marketing Campaigns", "Client Engagement")
        )

        for ((agentType, expectedCapabilities) in agentCapabilities) {
            val contextResult = llmService.getAgentContext(agentType)
            assertTrue("Context for $agentType should succeed", contextResult.isSuccess)

            val context = contextResult.getOrNull()
            val capabilities = context?.capabilities ?: emptyList()

            for (expectedCapability in expectedCapabilities) {
                assertTrue("$agentType should have capability: $expectedCapability",
                    capabilities.any { it.contains(expectedCapability, ignoreCase = true) })
            }
        }
    }

    @Test
    fun `test token usage tracking`() = runTest {
        // When
        val responseResult = llmService.generateResponse(
            prompt = "Calculate the cost of 1000 square feet of drywall installation",
            context = null,
            agentType = AgentType.ESTIMATING_DEPARTMENT_ORCHESTRATOR
        )

        // Then
        assertTrue("Response should succeed", responseResult.isSuccess)
        val response = responseResult.getOrNull()
        assertNotNull("Response should not be null", response)

        val tokenUsage = response?.tokenUsage
        assertNotNull("Token usage should be tracked", tokenUsage)
        assertTrue("Prompt tokens should be > 0", (tokenUsage?.promptTokens ?: 0) > 0)
        assertTrue("Completion tokens should be > 0", (tokenUsage?.completionTokens ?: 0) > 0)
        assertTrue("Total tokens should be > 0", (tokenUsage?.totalTokens ?: 0) > 0)
    }

    @Test
    fun `test concurrent LLM requests handling`() = runTest {
        // When - Make multiple concurrent requests
        val concurrentRequests = (1..5).map { requestId ->
            kotlinx.coroutines.async {
                llmService.generateResponse(
                    prompt = "Construction query $requestId: What's the standard size for a residential garage?",
                    context = null,
                    agentType = AgentType.DESIGN_DEPARTMENT_ORCHESTRATOR
                )
            }
        }

        val results = concurrentRequests.map { it.await() }

        // Then - All requests should succeed
        results.forEachIndexed { index, result ->
            assertTrue("Concurrent request ${index + 1} should succeed", result.isSuccess)
            val response = result.getOrNull()
            assertNotNull("Response ${index + 1} should not be null", response)
            assertTrue("Response ${index + 1} should have content", response?.content?.isNotEmpty() == true)
        }
    }
}