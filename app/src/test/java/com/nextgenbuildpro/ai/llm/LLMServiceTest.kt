package com.nextgenbuildpro.ai.llm

import com.nextgenbuildpro.shared.AgentType
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for LLM Service functionality
 */
class LLMServiceTest {
    
    @Mock
    private lateinit var mockFirestoreService: com.nextgenbuildpro.core.api.FirestoreService
    
    private lateinit var llmService: LLMService
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        llmService = LLMServiceImpl(mockFirestoreService)
    }
    
    @Test
    fun testGenerateResponse() = runBlocking {
        // Given
        val prompt = "Help coordinate resources for a construction project"
        val agentType = AgentType.HERMES_BRAIN
        
        // When
        val result = llmService.generateResponse(prompt, null, agentType)
        
        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertTrue(response != null)
        assertTrue(response!!.content.isNotEmpty())
        assertTrue(response.tokenUsage.totalTokens > 0)
    }
    
    @Test
    fun testGenerateCoordinationResponse() = runBlocking {
        // Given
        val request = MultiAgentCoordinationRequest(
            requestingAgent = AgentType.HERMES_BRAIN,
            targetAgents = listOf(AgentType.MRM, AgentType.BIG_DADDY),
            task = "Coordinate resource allocation for new project",
            context = "High priority construction project with tight deadline"
        )
        
        // When
        val result = llmService.generateCoordinationResponse(request)
        
        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertTrue(response != null)
        assertTrue(response!!.plan.isNotEmpty())
        assertEquals(2, response.agentAssignments.size)
        assertTrue(response.estimatedDuration > 0)
    }
    
    @Test
    fun testGetAgentContext() = runBlocking {
        // Given
        val agentType = AgentType.HERMES_BRAIN
        
        // When
        val result = llmService.getAgentContext(agentType)
        
        // Then
        assertTrue(result.isSuccess)
        val context = result.getOrNull()
        assertTrue(context != null)
        assertEquals(agentType, context!!.agentType)
        assertTrue(context.systemPrompt.isNotEmpty())
        assertTrue(context.capabilities.isNotEmpty())
    }
    
    @Test
    fun testLLMContextCreation() {
        // Given
        val conversationId = "test-conversation-123"
        val systemPrompt = "You are a helpful assistant"
        val messages = listOf(
            LLMMessage("user", "Hello"),
            LLMMessage("assistant", "Hi there!")
        )
        
        // When
        val context = LLMContext(
            conversationId = conversationId,
            systemPrompt = systemPrompt,
            previousMessages = messages
        )
        
        // Then
        assertEquals(conversationId, context.conversationId)
        assertEquals(systemPrompt, context.systemPrompt)
        assertEquals(2, context.previousMessages.size)
    }
    
    @Test
    fun testLLMConversationCreation() {
        // Given
        val conversationId = "test-conversation-123"
        val participants = listOf(AgentType.HERMES_BRAIN, AgentType.MRM)
        val messages = listOf(
            LLMMessage("user", "Start coordination"),
            LLMMessage("assistant", "Coordination initiated")
        )
        val startTime = LocalDateTime.now()
        
        // When
        val conversation = LLMConversation(
            id = conversationId,
            participants = participants,
            messages = messages,
            startTime = startTime,
            lastUpdate = startTime
        )
        
        // Then
        assertEquals(conversationId, conversation.id)
        assertEquals(2, conversation.participants.size)
        assertEquals(2, conversation.messages.size)
        assertEquals(ConversationStatus.ACTIVE, conversation.status)
    }
    
    @Test
    fun testMultiAgentCoordinationRequest() {
        // Given
        val requestingAgent = AgentType.HERMES_BRAIN
        val targetAgents = listOf(AgentType.MRM, AgentType.BIG_DADDY, AgentType.HRM_MODEL)
        val task = "Setup new construction project"
        val context = "Large commercial building project"
        
        // When
        val request = MultiAgentCoordinationRequest(
            requestingAgent = requestingAgent,
            targetAgents = targetAgents,
            task = task,
            context = context
        )
        
        // Then
        assertEquals(requestingAgent, request.requestingAgent)
        assertEquals(3, request.targetAgents.size)
        assertEquals(task, request.task)
        assertEquals(context, request.context)
        assertEquals("MEDIUM", request.priority)
    }
}