package com.aireceptionist.app.ai.agents

import com.aireceptionist.app.ai.agents.impl.*
import com.aireceptionist.app.ai.llm.OnDeviceLLM
import com.aireceptionist.app.ai.llm.LLMScenario
import com.aireceptionist.app.data.models.CallContext
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central coordinator for all AI agents
 * Manages agent lifecycle, routing, and orchestration
 */
@Singleton
class AgentManager @Inject constructor(
    private val onDeviceLLM: OnDeviceLLM
) {
    
    private val agents = ConcurrentHashMap<String, Agent>()
    private val agentResponses = MutableSharedFlow<AgentResponse>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private var isInitialized = false
    
    /**
     * Initialize all agents
     */
    suspend fun initialize() {
        if (isInitialized) return
        
        Logger.i(TAG, "Initializing AI Agent Manager")
        
        try {
            // Initialize the on-device LLM first
            val llmInitialized = onDeviceLLM.initialize()
            Logger.i(TAG, "On-Device LLM initialized: $llmInitialized")
            
            // Create and initialize all agents (now with LLM support)
            val agentList = listOf(
                SpeechRecognitionAgent(),
                NaturalLanguageAgent(onDeviceLLM),
                CallRoutingAgent(onDeviceLLM),
                CustomerServiceAgent(onDeviceLLM),
                VoiceSynthesisAgent(),
                EmotionDetectionAgent(onDeviceLLM),
                IntegrationAgent(),
                AppointmentAgent(onDeviceLLM)
            )
            
            // Initialize agents concurrently
            agentList.map { agent ->
                scope.async {
                    try {
                        if (agent.initialize()) {
                            agents[agent.agentId] = agent
                            Logger.i(TAG, "Agent ${agent.agentName} initialized successfully")
                        } else {
                            Logger.e(TAG, "Failed to initialize agent ${agent.agentName}")
                        }
                    } catch (e: Exception) {
                        Logger.e(TAG, "Error initializing agent ${agent.agentName}", e)
                    }
                }
            }.awaitAll()
            
            isInitialized = true
            Logger.i(TAG, "AgentManager initialized with ${agents.size} agents")
            
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize AgentManager", e)
            throw e
        }
    }
    
    /**
     * Process input through the appropriate agent chain
     */
    suspend fun processInput(
        input: AgentInput,
        callContext: CallContext
    ): Flow<AgentResponse> = flow {
        
        Logger.d(TAG, "Processing input: ${input.type} - ${input.content}")
        
        try {
            // Determine the best agent for this input
            val primaryAgent = selectPrimaryAgent(input, callContext)
            
            if (primaryAgent != null) {
                Logger.d(TAG, "Selected primary agent: ${primaryAgent.agentName}")
                
                // Process with primary agent
                val response = primaryAgent.processInput(input.copy(context = callContext))
                emit(response)
                
                // Check if we need to chain to another agent
                response.nextSuggestedAgent?.let { nextAgentId ->
                    val nextAgent = agents[nextAgentId]
                    if (nextAgent != null) {
                        Logger.d(TAG, "Chaining to agent: ${nextAgent.agentName}")
                        
                        val chainedInput = AgentInput(
                            type = InputType.SYSTEM_EVENT,
                            content = response.content,
                            context = callContext,
                            metadata = response.metadata
                        )
                        
                        val chainedResponse = nextAgent.processInput(chainedInput)
                        emit(chainedResponse)
                    }
                }
                
            } else {
                Logger.w(TAG, "No suitable agent found for input type: ${input.type}")
                emit(createErrorResponse("No suitable agent available"))
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error processing input", e)
            emit(createErrorResponse("Processing error: ${e.message}"))
        }
    }
    
    /**
     * Select the most appropriate agent for the given input
     */
    private fun selectPrimaryAgent(input: AgentInput, context: CallContext): Agent? {
        return when (input.type) {
            InputType.AUDIO_SPEECH -> {
                // Start with speech recognition, then chain to NLU
                agents.values.find { 
                    it.capabilities.contains(AgentCapability.SPEECH_RECOGNITION) 
                }
            }
            InputType.TEXT_MESSAGE -> {
                // Direct to natural language understanding
                agents.values.find { 
                    it.capabilities.contains(AgentCapability.NATURAL_LANGUAGE_UNDERSTANDING) 
                }
            }
            InputType.CALL_EVENT -> {
                // Route to call routing agent
                agents.values.find { 
                    it.capabilities.contains(AgentCapability.CALL_ROUTING) 
                }
            }
            InputType.SYSTEM_EVENT -> {
                // Context-based selection
                selectContextualAgent(context)
            }
            InputType.USER_COMMAND -> {
                // Route to customer service agent
                agents.values.find { 
                    it.capabilities.contains(AgentCapability.CUSTOMER_SERVICE) 
                }
            }
        }
    }
    
    /**
     * Select agent based on call context
     */
    private fun selectContextualAgent(context: CallContext): Agent? {
        return when {
            context.intent?.contains("appointment") == true -> {
                agents.values.find { 
                    it.capabilities.contains(AgentCapability.APPOINTMENT_SCHEDULING) 
                }
            }
            context.intent?.contains("information") == true -> {
                agents.values.find { 
                    it.capabilities.contains(AgentCapability.INFORMATION_RETRIEVAL) 
                }
            }
            else -> {
                agents.values.find { 
                    it.capabilities.contains(AgentCapability.CUSTOMER_SERVICE) 
                }
            }
        }
    }
    
    /**
     * Get agent by ID
     */
    fun getAgent(agentId: String): Agent? = agents[agentId]
    
    /**
     * Get all active agents
     */
    fun getAllAgents(): List<Agent> = agents.values.toList()
    
    /**
     * Check system health
     */
    fun getSystemHealth(): Map<String, Boolean> {
        return agents.mapValues { it.value.isHealthy() }
    }
    
    /**
     * Shutdown all agents
     */
    suspend fun shutdown() {
        Logger.i(TAG, "Shutting down AgentManager")
        
        agents.values.map { agent ->
            scope.async {
                try {
                    agent.shutdown()
                    Logger.d(TAG, "Agent ${agent.agentName} shut down successfully")
                } catch (e: Exception) {
                    Logger.e(TAG, "Error shutting down agent ${agent.agentName}", e)
                }
            }
        }.awaitAll()
        
        agents.clear()
        scope.cancel()
        isInitialized = false
        
        Logger.i(TAG, "AgentManager shutdown complete")
    }
    
    private fun createErrorResponse(message: String): AgentResponse {
        return AgentResponse(
            agentId = "system",
            responseType = ResponseType.TEXT_RESPONSE,
            content = message,
            confidence = 0.0f
        )
    }
    
    /**
     * Generate intelligent response using on-device LLM
     */
    suspend fun generateIntelligentResponse(
        userInput: String,
        callContext: CallContext,
        conversationHistory: List<String> = emptyList()
    ): AgentResponse {
        return try {
            Logger.d(TAG, "Generating intelligent response for: $userInput")
            
            // Determine the scenario based on context and input
            val scenario = determineScenario(userInput, callContext)
            
            // Build context for LLM
            val llmContext = buildLLMContext(callContext, conversationHistory)
            
            // Generate response using LLM
            val response = onDeviceLLM.generateContextualResponse(
                scenario = scenario,
                userInput = userInput,
                context = llmContext
            )
            
            AgentResponse(
                agentId = "llm-agent",
                content = response,
                confidence = 0.95f,
                responseType = ResponseType.TEXT_RESPONSE,
                metadata = mapOf(
                    "llm_scenario" to scenario.name,
                    "model_info" to onDeviceLLM.getModelInfo(),
                    "timestamp" to System.currentTimeMillis()
                ),
                nextSuggestedAgent = determineNextAgent(scenario, response)
            )
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error generating intelligent response", e)
            createErrorResponse("Failed to generate intelligent response: ${e.message}")
        }
    }
    
    /**
     * Determine the appropriate LLM scenario based on input and context
     */
    private fun determineScenario(userInput: String, callContext: CallContext): LLMScenario {
        return when {
            callContext.callState == "RINGING" && userInput.isEmpty() -> 
                LLMScenario.CALL_GREETING
                
            userInput.contains("appointment", ignoreCase = true) ||
            userInput.contains("schedule", ignoreCase = true) ||
            userInput.contains("book", ignoreCase = true) ->
                LLMScenario.APPOINTMENT_SCHEDULING
                
            userInput.contains("transfer", ignoreCase = true) ||
            userInput.contains("speak to", ignoreCase = true) ||
            userInput.contains("department", ignoreCase = true) ->
                LLMScenario.CALL_ROUTING
                
            callContext.emotionalState != null && 
            callContext.emotionalState in listOf("frustrated", "angry", "upset") ->
                LLMScenario.EMOTIONAL_RESPONSE
                
            else -> LLMScenario.CUSTOMER_INQUIRY
        }
    }
    
    /**
     * Build context map for LLM processing
     */
    private fun buildLLMContext(
        callContext: CallContext, 
        conversationHistory: List<String>
    ): Map<String, Any> {
        return mapOf(
            "businessName" to (callContext.businessContext?.get("name") ?: "our business"),
            "timeOfDay" to determineTimeOfDay(),
            "conversationHistory" to conversationHistory,
            "callerId" to (callContext.callerId ?: "unknown"),
            "callDuration" to callContext.callDuration,
            "emotionalState" to (callContext.emotionalState ?: "neutral"),
            "availableSlots" to getAvailableSlots(),
            "services" to getBusinessServices(),
            "departments" to listOf("sales", "support", "billing", "general"),
            "policies" to getBusinessPolicies(),
            "staffStatus" to getStaffAvailability()
        )
    }
    
    /**
     * Determine next agent based on scenario and response
     */
    private fun determineNextAgent(scenario: LLMScenario, response: String): String? {
        return when (scenario) {
            LLMScenario.APPOINTMENT_SCHEDULING -> {
                if (response.contains("available") || response.contains("schedule")) {
                    "appointment-agent"
                } else null
            }
            LLMScenario.CALL_ROUTING -> {
                if (response.contains("transfer") || response.contains("connecting")) {
                    "call-routing-agent"
                } else null
            }
            LLMScenario.EMOTIONAL_RESPONSE -> {
                if (response.contains("understand") || response.contains("help")) {
                    "customer-service-agent"
                } else null
            }
            else -> null
        }
    }
    
    /**
     * Helper methods for context building
     */
    private fun determineTimeOfDay(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon" 
            in 18..21 -> "evening"
            else -> "night"
        }
    }
    
    private fun getAvailableSlots(): List<String> {
        // This would typically come from a calendar integration
        return listOf("9:00 AM", "11:00 AM", "2:00 PM", "4:00 PM")
    }
    
    private fun getBusinessServices(): List<String> {
        // This would come from business configuration
        return listOf("consultation", "support", "sales", "technical assistance")
    }
    
    private fun getBusinessPolicies(): String {
        return "Standard business policies apply. We're committed to excellent customer service."
    }
    
    private fun getStaffAvailability(): String {
        return "Customer service representatives are available during business hours"
    }
    
    /**
     * Check if on-device LLM is ready
     */
    fun isLLMReady(): Boolean = onDeviceLLM.isReady()
    
    /**
     * Get LLM model information
     */
    fun getLLMInfo(): Map<String, String> = onDeviceLLM.getModelInfo()
    
    companion object {
        private const val TAG = "AgentManager"
    }
}