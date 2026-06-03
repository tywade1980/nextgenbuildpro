package com.aireceptionist.app.ai.agents.impl

import com.aireceptionist.app.ai.agents.*
import com.aireceptionist.app.data.repository.CallRepository
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Agent responsible for call routing and transfer decisions
 */
class CallRoutingAgent @Inject constructor(
    private val callRepository: CallRepository
) : Agent {
    
    override val agentId = "call_routing"
    override val agentName = "Call Routing Agent"
    override val capabilities = listOf(
        AgentCapability.CALL_ROUTING,
        AgentCapability.CONTEXT_AWARENESS
    )
    override val priority = 8
    
    private var isInitialized = false
    
    // Routing rules based on intent and context
    private val routingRules = mapOf(
        "emergency" to RoutingDecision(
            destination = "emergency_line",
            priority = 10,
            requiresHuman = true,
            message = "Connecting you to emergency services immediately."
        ),
        "appointment_booking" to RoutingDecision(
            destination = "appointment_scheduler",
            priority = 5,
            requiresHuman = false,
            message = "I'll help you schedule an appointment."
        ),
        "technical_support" to RoutingDecision(
            destination = "tech_support",
            priority = 6,
            requiresHuman = true,
            message = "Transferring you to our technical support team."
        ),
        "billing_inquiry" to RoutingDecision(
            destination = "billing_department",
            priority = 4,
            requiresHuman = true,
            message = "Connecting you with our billing department."
        ),
        "general_complaint" to RoutingDecision(
            destination = "customer_relations",
            priority = 7,
            requiresHuman = true,
            message = "I'll connect you with our customer relations team."
        ),
        "sales_inquiry" to RoutingDecision(
            destination = "sales_team",
            priority = 3,
            requiresHuman = false,
            message = "Let me connect you with our sales team."
        )
    )
    
    override suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.i(TAG, "Initializing Call Routing Agent")
            
            // Load routing configuration from database
            // Initialize routing rules and departments
            
            isInitialized = true
            Logger.i(TAG, "Call Routing Agent initialized successfully")
            true
            
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize Call Routing Agent", e)
            false
        }
    }
    
    override suspend fun processInput(input: AgentInput): AgentResponse = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            return@withContext createErrorResponse("Agent not initialized")
        }
        
        when (input.type) {
            InputType.CALL_EVENT, InputType.SYSTEM_EVENT -> processRoutingRequest(input)
            else -> createErrorResponse("Unsupported input type: ${input.type}")
        }
    }
    
    private suspend fun processRoutingRequest(input: AgentInput): AgentResponse {
        return try {
            Logger.d(TAG, "Processing routing request for: ${input.content}")
            
            val callContext = input.context
            val intent = callContext.intent ?: "general_inquiry"
            
            // Get caller history for better routing
            val callerHistory = getCallerHistory(callContext.callerNumber)
            
            // Make routing decision
            val routingDecision = makeRoutingDecision(intent, callContext, callerHistory)
            
            Logger.i(TAG, "Routing decision: ${routingDecision.destination} (Priority: ${routingDecision.priority})")
            
            // Create actions based on routing decision
            val actions = createRoutingActions(routingDecision)
            
            AgentResponse(
                agentId = agentId,
                responseType = ResponseType.ROUTING_DECISION,
                content = routingDecision.message,
                confidence = routingDecision.confidence,
                actions = actions,
                nextSuggestedAgent = if (routingDecision.requiresHuman) null else determineNextAgent(routingDecision),
                metadata = mapOf(
                    "destination" to routingDecision.destination,
                    "priority" to routingDecision.priority,
                    "requires_human" to routingDecision.requiresHuman,
                    "caller_history_score" to (callerHistory?.satisfactionScore ?: 0.5f)
                )
            )
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error processing routing request", e)
            createErrorResponse("Routing failed: ${e.message}")
        }
    }
    
    private suspend fun getCallerHistory(phoneNumber: String?): CallerHistory? {
        return phoneNumber?.let {
            try {
                callRepository.getCallerHistory(it)
            } catch (e: Exception) {
                Logger.w(TAG, "Failed to get caller history for $it", e)
                null
            }
        }
    }
    
    private fun makeRoutingDecision(
        intent: String,
        callContext: com.aireceptionist.app.data.models.CallContext,
        callerHistory: CallerHistory?
    ): RoutingDecision {
        
        // Get base routing decision from rules
        val baseDecision = routingRules[intent] ?: routingRules["general_inquiry"]!!
        
        // Adjust based on caller history
        val adjustedDecision = adjustForCallerHistory(baseDecision, callerHistory)
        
        // Adjust based on time and availability
        return adjustForAvailability(adjustedDecision, callContext)
    }
    
    private fun adjustForCallerHistory(
        baseDecision: RoutingDecision,
        callerHistory: CallerHistory?
    ): RoutingDecision {
        
        callerHistory?.let { history ->
            // VIP customers get higher priority
            if (history.isVip) {
                return baseDecision.copy(
                    priority = minOf(baseDecision.priority + 3, 10),
                    requiresHuman = true,
                    message = "Thank you for being a valued customer. Connecting you with priority support."
                )
            }
            
            // Frequent callers with low satisfaction need human help
            if (history.callCount > 5 && history.satisfactionScore < 0.6f) {
                return baseDecision.copy(
                    requiresHuman = true,
                    priority = minOf(baseDecision.priority + 2, 10),
                    message = "I see you've called before. Let me connect you with someone who can provide personalized assistance."
                )
            }
            
            // New customers get extra attention
            if (history.callCount <= 1) {
                return baseDecision.copy(
                    message = "Welcome! I'm here to help make sure you have a great experience with us."
                )
            }
        }
        
        return baseDecision
    }
    
    private fun adjustForAvailability(
        decision: RoutingDecision,
        callContext: com.aireceptionist.app.data.models.CallContext
    ): RoutingDecision {
        
        // Check if it's outside business hours
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val isBusinessHours = currentHour in 9..17 // 9 AM to 5 PM
        
        if (!isBusinessHours && decision.requiresHuman) {
            return decision.copy(
                requiresHuman = false,
                message = "Our office is currently closed. I'll help you as much as I can, or you can leave a message for a callback during business hours.",
                nextSuggestedAgent = "customer_service"
            )
        }
        
        return decision
    }
    
    private fun createRoutingActions(decision: RoutingDecision): List<AgentAction> {
        val actions = mutableListOf<AgentAction>()
        
        when {
            decision.requiresHuman -> {
                actions.add(
                    AgentAction(
                        actionType = ActionType.REQUEST_HUMAN_OPERATOR,
                        parameters = mapOf(
                            "department" to decision.destination,
                            "priority" to decision.priority
                        ),
                        priority = decision.priority
                    )
                )
            }
            decision.destination == "appointment_scheduler" -> {
                // No immediate transfer, let appointment agent handle it
            }
            else -> {
                actions.add(
                    AgentAction(
                        actionType = ActionType.TRANSFER_CALL,
                        parameters = mapOf(
                            "destination" to decision.destination,
                            "priority" to decision.priority
                        ),
                        priority = decision.priority
                    )
                )
            }
        }
        
        return actions
    }
    
    private fun determineNextAgent(decision: RoutingDecision): String? {
        return when (decision.destination) {
            "appointment_scheduler" -> "appointment_agent"
            "emergency_line" -> null // Human takeover
            else -> "customer_service"
        }
    }
    
    override suspend fun shutdown() {
        try {
            Logger.i(TAG, "Shutting down Call Routing Agent")
            isInitialized = false
            Logger.i(TAG, "Call Routing Agent shutdown complete")
        } catch (e: Exception) {
            Logger.e(TAG, "Error during shutdown", e)
        }
    }
    
    override fun isHealthy(): Boolean {
        return isInitialized
    }
    
    private fun createErrorResponse(message: String): AgentResponse {
        return AgentResponse(
            agentId = agentId,
            responseType = ResponseType.ROUTING_DECISION,
            content = message,
            confidence = 0.0f
        )
    }
    
    companion object {
        private const val TAG = "CallRoutingAgent"
    }
}

/**
 * Data class for routing decisions
 */
data class RoutingDecision(
    val destination: String,
    val priority: Int,
    val requiresHuman: Boolean,
    val message: String,
    val confidence: Float = 0.8f,
    val nextSuggestedAgent: String? = null
)

/**
 * Data class for caller history
 */
data class CallerHistory(
    val phoneNumber: String,
    val callCount: Int,
    val lastCallDate: Long,
    val satisfactionScore: Float,
    val isVip: Boolean,
    val preferredLanguage: String?,
    val commonIssues: List<String>
)