package com.aireceptionist.app.ai.agents.impl

import com.aireceptionist.app.ai.agents.*
import com.aireceptionist.app.data.repository.KnowledgeBaseRepository
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Agent responsible for customer service interactions and information retrieval
 */
class CustomerServiceAgent @Inject constructor(
    private val knowledgeBase: KnowledgeBaseRepository
) : Agent {
    
    override val agentId = "customer_service"
    override val agentName = "Customer Service Agent"
    override val capabilities = listOf(
        AgentCapability.CUSTOMER_SERVICE,
        AgentCapability.INFORMATION_RETRIEVAL,
        AgentCapability.CONTEXT_AWARENESS
    )
    override val priority = 7
    
    private var isInitialized = false
    
    override suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.i(TAG, "Initializing Customer Service Agent")
            
            // Load knowledge base and FAQ data
            knowledgeBase.initialize()
            
            isInitialized = true
            Logger.i(TAG, "Customer Service Agent initialized successfully")
            true
            
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize Customer Service Agent", e)
            false
        }
    }
    
    override suspend fun processInput(input: AgentInput): AgentResponse = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            return@withContext createErrorResponse("Agent not initialized")
        }
        
        when (input.type) {
            InputType.TEXT_MESSAGE, InputType.SYSTEM_EVENT -> processCustomerQuery(input)
            else -> createErrorResponse("Unsupported input type: ${input.type}")
        }
    }
    
    private suspend fun processCustomerQuery(input: AgentInput): AgentResponse {
        return try {
            Logger.d(TAG, "Processing customer query: ${input.content}")
            
            val intent = input.context.intent ?: "general_inquiry"
            val entities = input.metadata["entities"] as? List<*> ?: emptyList<Any>()
            
            // Search knowledge base for relevant information
            val knowledgeResults = searchKnowledgeBase(input.content, intent)
            
            // Generate appropriate response
            val response = generateCustomerResponse(intent, input.content, knowledgeResults)
            
            // Determine if we need to escalate or transfer
            val shouldEscalate = shouldEscalateToHuman(intent, input.context, knowledgeResults)
            
            val actions = if (shouldEscalate) {
                listOf(
                    AgentAction(
                        actionType = ActionType.REQUEST_HUMAN_OPERATOR,
                        parameters = mapOf("reason" to "complex_query"),
                        priority = 5
                    )
                )
            } else {
                emptyList()
            }
            
            AgentResponse(
                agentId = agentId,
                responseType = ResponseType.SPEECH_OUTPUT,
                content = response,
                confidence = if (knowledgeResults.isNotEmpty()) 0.8f else 0.5f,
                actions = actions,
                nextSuggestedAgent = if (shouldEscalate) null else "voice_synthesis",
                metadata = mapOf(
                    "knowledge_results_count" to knowledgeResults.size,
                    "escalation_recommended" to shouldEscalate,
                    "response_source" to if (knowledgeResults.isNotEmpty()) "knowledge_base" else "fallback"
                )
            )
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error processing customer query", e)
            createErrorResponse("I apologize, but I'm having trouble processing your request. Let me connect you with someone who can help.")
        }
    }
    
    private suspend fun searchKnowledgeBase(
        query: String,
        intent: String
    ): List<KnowledgeResult> {
        return try {
            knowledgeBase.search(query, intent)
        } catch (e: Exception) {
            Logger.w(TAG, "Knowledge base search failed", e)
            emptyList()
        }
    }
    
    private fun generateCustomerResponse(
        intent: String,
        query: String,
        knowledgeResults: List<KnowledgeResult>
    ): String {
        
        return when {
            knowledgeResults.isNotEmpty() -> {
                val bestResult = knowledgeResults.first()
                when (intent) {
                    "information_request" -> {
                        "Based on your question, here's what I can tell you: ${bestResult.content}"
                    }
                    "hours_inquiry" -> {
                        "Our business hours are: ${bestResult.content}"
                    }
                    "location_inquiry" -> {
                        "Our location information: ${bestResult.content}"
                    }
                    "service_inquiry" -> {
                        "Regarding our services: ${bestResult.content}"
                    }
                    else -> bestResult.content
                }
            }
            intent == "greeting" -> {
                "Hello! Thank you for calling. How can I assist you today?"
            }
            intent == "general_inquiry" -> {
                "I understand you have a question. While I search for the best way to help you, could you please provide a bit more detail about what you're looking for?"
            }
            intent == "complaint" -> {
                "I sincerely apologize for any inconvenience you've experienced. I want to make sure we address your concerns properly. Let me see how I can help resolve this for you."
            }
            intent == "thank_you" -> {
                "You're very welcome! I'm glad I could help. Is there anything else I can assist you with today?"
            }
            else -> {
                "I want to make sure I understand your request correctly. Could you please tell me a bit more about what you need help with?"
            }
        }
    }
    
    private fun shouldEscalateToHuman(
        intent: String,
        context: com.aireceptistent.app.data.models.CallContext,
        knowledgeResults: List<KnowledgeResult>
    ): Boolean {
        
        // Always escalate certain intents
        val alwaysEscalateIntents = setOf(
            "emergency",
            "legal_issue",
            "refund_request",
            "complex_technical_issue",
            "manager_request"
        )
        
        if (intent in alwaysEscalateIntents) {
            return true
        }
        
        // Escalate if no knowledge base results found for important queries
        if (knowledgeResults.isEmpty() && intent in setOf(
            "billing_inquiry",
            "account_issue",
            "service_problem"
        )) {
            return true
        }
        
        // Escalate if customer explicitly requests human
        val userInput = context.lastUserInput?.toLowerCase() ?: ""
        val humanRequestKeywords = listOf(
            "human", "person", "representative", "agent", "manager",
            "speak to someone", "talk to someone", "transfer me"
        )
        
        if (humanRequestKeywords.any { userInput.contains(it) }) {
            return true
        }
        
        // Escalate if sentiment is very negative
        val sentiment = context.sentiment
        if (sentiment == "very_negative" || sentiment == "angry") {
            return true
        }
        
        return false
    }
    
    override suspend fun shutdown() {
        try {
            Logger.i(TAG, "Shutting down Customer Service Agent")
            knowledgeBase.shutdown()
            isInitialized = false
            Logger.i(TAG, "Customer Service Agent shutdown complete")
        } catch (e: Exception) {
            Logger.e(TAG, "Error during shutdown", e)
        }
    }
    
    override fun isHealthy(): Boolean {
        return isInitialized && knowledgeBase.isHealthy()
    }
    
    private fun createErrorResponse(message: String): AgentResponse {
        return AgentResponse(
            agentId = agentId,
            responseType = ResponseType.SPEECH_OUTPUT,
            content = message,
            confidence = 0.0f,
            actions = listOf(
                AgentAction(
                    actionType = ActionType.REQUEST_HUMAN_OPERATOR,
                    parameters = mapOf("reason" to "system_error"),
                    priority = 8
                )
            )
        )
    }
    
    companion object {
        private const val TAG = "CustomerServiceAgent"
    }
}

/**
 * Data class for knowledge base search results
 */
data class KnowledgeResult(
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val confidence: Float,
    val tags: List<String>
)