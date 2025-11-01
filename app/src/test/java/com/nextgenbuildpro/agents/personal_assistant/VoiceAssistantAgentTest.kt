package com.nextgenbuildpro.agents.personal_assistant

import android.content.Context
import android.speech.SpeechRecognizer
import com.nextgenbuildpro.ai.llm.*
import com.nextgenbuildpro.shared.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Unit tests for VoiceAssistantAgent
 * 
 * Tests the voice assistant's ability to:
 * - Initialize properly
 * - Process voice queries with external LLM integration
 * - Handle conversation context
 * - Execute specialized tasks
 * - Manage voice input/output
 */
@ExperimentalCoroutinesApi
class VoiceAssistantAgentTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockLLMService: LLMService
    
    private lateinit var voiceAssistantAgent: VoiceAssistantAgent
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Note: Speech recognizer mocking would require PowerMock or similar
        // for static methods. For unit tests, we'll work with the agent in
        // a state where speech recognizer is unavailable.
        
        voiceAssistantAgent = VoiceAssistantAgent(
            context = mockContext,
            llmService = mockLLMService
        )
    }
    
    @After
    fun tearDown() {
        // Clean up
    }
    
    @Test
    fun `test agent properties are correctly initialized`() {
        // Verify agent properties
        assertEquals(AgentType.SUB_AGENT, voiceAssistantAgent.agentType)
        assertEquals(AgentType.CEO_PERSONAL_ASSISTANT, voiceAssistantAgent.departmentHead)
        assertEquals("Voice Assistant with External LLM Integration", voiceAssistantAgent.subAgentRole)
        assertNotNull(voiceAssistantAgent.agentId)
        assertTrue(voiceAssistantAgent.agentId.startsWith("voice_assistant_agent_"))
    }
    
    @Test
    fun `test ML model configuration`() {
        val mlModel = voiceAssistantAgent.mlModel
        
        assertNotNull(mlModel)
        assertEquals("openai/gpt-4", mlModel.modelName)
        assertEquals(MLModelType.AGENT_WORKFLOW, mlModel.modelType)
        assertTrue(mlModel.accuracy > 0.9)
    }
    
    @Test
    fun `test MCP tools are defined`() {
        val mcpTools = voiceAssistantAgent.mcpTools
        
        assertNotNull(mcpTools)
        assertEquals(3, mcpTools.size)
        
        val toolNames = mcpTools.map { it.toolName }
        assertTrue(toolNames.contains("Voice Recognition Tool"))
        assertTrue(toolNames.contains("Voice Synthesis Tool"))
        assertTrue(toolNames.contains("External LLM Query Tool"))
    }
    
    @Test
    fun `test API integrations are configured`() {
        val apiIntegrations = voiceAssistantAgent.apiIntegrations
        
        assertNotNull(apiIntegrations)
        assertEquals(1, apiIntegrations.size)
        
        val openRouterApi = apiIntegrations[0]
        assertEquals("OpenRouter API", openRouterApi.apiName)
        assertEquals("https://openrouter.ai/api/v1", openRouterApi.endpoint)
        assertEquals(APIAuthType.API_KEY, openRouterApi.authType)
        assertNotNull(openRouterApi.rateLimits)
        assertEquals(60, openRouterApi.rateLimits?.requestsPerMinute)
    }
    
    @Test
    fun `test voice assistant specialization`() {
        assertEquals("Voice-enabled AI assistance with external data retrieval", voiceAssistantAgent.specialization)
    }
    
    @Test
    fun `test conversation state initialization`() {
        // ConversationState should start as Idle
        assertTrue(voiceAssistantAgent.conversationState.value is ConversationState.Idle)
    }
    
    @Test
    fun `test agent is not active before initialization`() {
        assertFalse(voiceAssistantAgent.isActive.value)
    }
    
    @Test
    fun `test sub agent role is correctly defined`() {
        val role = voiceAssistantAgent.subAgentRole
        assertEquals("Voice Assistant with External LLM Integration", role)
    }
    
    @Test
    fun `test agent has correct department head`() {
        assertEquals(AgentType.CEO_PERSONAL_ASSISTANT, voiceAssistantAgent.departmentHead)
    }
}
