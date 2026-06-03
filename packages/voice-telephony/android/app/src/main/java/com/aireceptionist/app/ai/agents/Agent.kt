package com.aireceptionist.app.ai.agents

import com.aireceptionist.app.data.models.CallContext
import com.aireceptionist.app.data.models.AgentResponse
import kotlinx.coroutines.flow.Flow

/**
 * Base interface for all AI agents in the system
 */
interface Agent {
    val agentId: String
    val agentName: String
    val capabilities: List<AgentCapability>
    val priority: Int
    
    suspend fun initialize(): Boolean
    suspend fun processInput(input: AgentInput): AgentResponse
    suspend fun shutdown()
    fun isHealthy(): Boolean
}

/**
 * Capabilities that an agent can have
 */
enum class AgentCapability {
    SPEECH_RECOGNITION,
    NATURAL_LANGUAGE_UNDERSTANDING,
    CALL_ROUTING,
    APPOINTMENT_SCHEDULING,
    INFORMATION_RETRIEVAL,
    CUSTOMER_SERVICE,
    VOICE_SYNTHESIS,
    EMOTION_DETECTION,
    CONTEXT_AWARENESS,
    MULTI_LANGUAGE,
    INTEGRATION_MANAGEMENT
}

/**
 * Input data structure for agents
 */
data class AgentInput(
    val type: InputType,
    val content: String,
    val context: CallContext,
    val metadata: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class InputType {
    AUDIO_SPEECH,
    TEXT_MESSAGE,
    CALL_EVENT,
    SYSTEM_EVENT,
    USER_COMMAND
}

/**
 * Response from an agent
 */
data class AgentResponse(
    val agentId: String,
    val responseType: ResponseType,
    val content: String,
    val confidence: Float,
    val actions: List<AgentAction> = emptyList(),
    val nextSuggestedAgent: String? = null,
    val metadata: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class ResponseType {
    SPEECH_OUTPUT,
    TEXT_RESPONSE,
    ACTION_COMMAND,
    ROUTING_DECISION,
    DATA_QUERY,
    INTEGRATION_CALL
}

/**
 * Actions that an agent can request
 */
data class AgentAction(
    val actionType: ActionType,
    val parameters: Map<String, Any>,
    val priority: Int = 0
)

enum class ActionType {
    TRANSFER_CALL,
    END_CALL,
    HOLD_CALL,
    PLAY_AUDIO,
    SEND_SMS,
    SEND_EMAIL,
    SCHEDULE_APPOINTMENT,
    UPDATE_DATABASE,
    TRIGGER_INTEGRATION,
    REQUEST_HUMAN_OPERATOR
}