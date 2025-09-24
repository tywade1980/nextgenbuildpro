package com.nextgenbuildpro.ai.llm

import com.nextgenbuildpro.shared.AgentType
import java.time.LocalDateTime

/**
 * LLM Service Interface
 * 
 * Defines the contract for Large Language Model integration
 * in the NextGenBuildPro multi-agent system with cloud database storage.
 */
interface LLMService {
    /**
     * Generate a response using the LLM
     * @param prompt The input prompt
     * @param context Optional context for the conversation
     * @param agentType The agent requesting the response
     * @return LLM response
     */
    suspend fun generateResponse(
        prompt: String,
        context: LLMContext? = null,
        agentType: AgentType
    ): Result<LLMResponse>
    
    /**
     * Generate a response for multi-agent coordination
     * @param request The coordination request
     * @return Coordination response
     */
    suspend fun generateCoordinationResponse(
        request: MultiAgentCoordinationRequest
    ): Result<MultiAgentCoordinationResponse>
    
    /**
     * Store conversation in cloud database
     * @param conversation The conversation to store
     * @return Success or failure result
     */
    suspend fun storeConversation(conversation: LLMConversation): Result<Unit>
    
    /**
     * Retrieve conversation history from cloud database
     * @param conversationId The conversation ID
     * @return Conversation history
     */
    suspend fun getConversationHistory(conversationId: String): Result<LLMConversation>
    
    /**
     * Get agent context for LLM processing
     * @param agentType The agent type
     * @return Agent-specific context
     */
    suspend fun getAgentContext(agentType: AgentType): Result<AgentLLMContext>
}

/**
 * LLM Context for maintaining conversation state
 */
data class LLMContext(
    val conversationId: String,
    val systemPrompt: String,
    val previousMessages: List<LLMMessage> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * LLM Response from the service
 */
data class LLMResponse(
    val content: String,
    val conversationId: String,
    val tokenUsage: TokenUsage,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Multi-agent coordination request
 */
data class MultiAgentCoordinationRequest(
    val requestingAgent: AgentType,
    val targetAgents: List<AgentType>,
    val task: String,
    val context: String,
    val priority: String = "MEDIUM"
)

/**
 * Multi-agent coordination response
 */
data class MultiAgentCoordinationResponse(
    val plan: String,
    val agentAssignments: Map<AgentType, String>,
    val estimatedDuration: Long, // minutes
    val dependencies: List<String> = emptyList()
)

/**
 * LLM Message in a conversation
 */
data class LLMMessage(
    val role: String, // "system", "user", "assistant"
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val agentType: AgentType? = null
)

/**
 * LLM Conversation stored in cloud database
 */
data class LLMConversation(
    val id: String,
    val participants: List<AgentType>,
    val messages: List<LLMMessage>,
    val startTime: LocalDateTime,
    val lastUpdate: LocalDateTime,
    val status: ConversationStatus = ConversationStatus.ACTIVE,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Conversation status
 */
enum class ConversationStatus {
    ACTIVE, COMPLETED, ARCHIVED, ERROR
}

/**
 * Token usage tracking
 */
data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

/**
 * Agent-specific LLM context
 */
data class AgentLLMContext(
    val agentType: AgentType,
    val systemPrompt: String,
    val capabilities: List<String>,
    val currentTasks: List<String> = emptyList(),
    val knowledgeBase: Map<String, Any> = emptyMap()
)