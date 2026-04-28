package com.aireceptionist.app.ai.agents.impl

import com.aireceptionist.app.ai.agents.*
import com.aireceptionist.app.ai.llm.OnDeviceLLM
import com.aireceptionist.app.ai.llm.LLMScenario
import com.aireceptionist.app.data.models.Intent
import com.aireceptionist.app.data.models.Entity
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Agent responsible for natural language understanding and intent recognition
 */
class NaturalLanguageAgent @Inject constructor(
    private val onDeviceLLM: OnDeviceLLM? = null
) : Agent {
    
    override val agentId = "natural_language"
    override val agentName = "Natural Language Understanding Agent"
    override val capabilities = listOf(
        AgentCapability.NATURAL_LANGUAGE_UNDERSTANDING,
        AgentCapability.CONTEXT_AWARENESS
    )
    override val priority = 9
    
    private var tfInterpreter: Interpreter? = null
    private var isInitialized = false
    
    // Intent patterns for rule-based fallback
    private val intentPatterns = mapOf(
        "appointment_booking" to listOf(
            Pattern.compile(".*\\b(book|schedule|make)\\s+(appointment|meeting)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(need|want)\\s+(to\\s+)?(see|meet)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(available|free)\\s+(time|slot|appointment)\\b.*", Pattern.CASE_INSENSITIVE)
        ),
        "information_request" to listOf(
            Pattern.compile(".*\\b(what|when|where|how|why|who)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(tell|info|information|details)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(hours|location|address|phone)\\b.*", Pattern.CASE_INSENSITIVE)
        ),
        "transfer_request" to listOf(
            Pattern.compile(".*\\b(transfer|connect|speak)\\s+(to|with)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(manager|supervisor|human|person)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(can't|cannot|won't|will not)\\s+help\\b.*", Pattern.CASE_INSENSITIVE)
        ),
        "emergency" to listOf(
            Pattern.compile(".*\\b(emergency|urgent|help|911)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(crisis|critical|immediate)\\b.*", Pattern.CASE_INSENSITIVE)
        ),
        "greeting" to listOf(
            Pattern.compile(".*\\b(hello|hi|hey|good\\s+(morning|afternoon|evening))\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(thank\\s+you|thanks|goodbye|bye)\\b.*", Pattern.CASE_INSENSITIVE)
        ),
        "complaint" to listOf(
            Pattern.compile(".*\\b(problem|issue|wrong|error|mistake)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(unhappy|unsatisfied|disappointed|angry)\\b.*", Pattern.CASE_INSENSITIVE)
        )
    )
    
    override suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.i(TAG, "Initializing Natural Language Agent")
            
            // Initialize TensorFlow Lite model (if available)
            // For now, we'll use rule-based approach as fallback
            
            isInitialized = true
            Logger.i(TAG, "Natural Language Agent initialized successfully")
            true
            
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize Natural Language Agent", e)
            false
        }
    }
    
    override suspend fun processInput(input: AgentInput): AgentResponse = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            return@withContext createErrorResponse("Agent not initialized")
        }
        
        when (input.type) {
            InputType.TEXT_MESSAGE, InputType.SYSTEM_EVENT -> processTextInput(input)
            else -> createErrorResponse("Unsupported input type: ${input.type}")
        }
    }
    
    private suspend fun processTextInput(input: AgentInput): AgentResponse {
        return try {
            Logger.d(TAG, "Processing text input: ${input.content}")
            
            // Try LLM-powered understanding first (if available)
            if (onDeviceLLM?.isReady() == true) {
                return processWithLLM(input)
            }
            
            // Fallback to traditional NLP methods
            val intent = extractIntent(input.content)
            val entities = extractEntities(input.content)
            val sentiment = analyzeSentiment(input.content)
            
            Logger.d(TAG, "Intent: ${intent.name}, Confidence: ${intent.confidence}")
            
            // Determine next agent based on intent
            val nextAgent = determineNextAgent(intent)
            
            // Create contextual actions
            val actions = createActionsForIntent(intent, entities)
            
            AgentResponse(
                agentId = agentId,
                responseType = ResponseType.ACTION_COMMAND,
                content = generateResponse(intent, entities),
                confidence = intent.confidence,
                actions = actions,
                nextSuggestedAgent = nextAgent,
                metadata = mapOf(
                    "intent" to intent.name,
                    "entities" to entities,
                    "sentiment" to sentiment,
                    "detected_language" to (input.metadata["detected_language"] ?: "en")
                )
            )
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error processing text input", e)
            createErrorResponse("Natural language processing failed: ${e.message}")
        }
    }
    
    /**
     * Process input using on-device LLM for superior understanding
     */
    private suspend fun processWithLLM(input: AgentInput): AgentResponse {
        return try {
            Logger.d(TAG, "Processing with on-device LLM: ${input.content}")
            
            // Build context for LLM understanding
            val context = buildLLMContext(input)
            
            // Generate intelligent analysis using LLM
            val analysisPrompt = \"\"\"
                Analyze this customer message for intent, entities, and sentiment:
                
                Message: "${input.content}"
                Context: Call in progress, customer service scenario
                
                Provide analysis in format:
                Intent: [primary_intent]
                Entities: [extracted_entities]  
                Sentiment: [positive/negative/neutral]
                Confidence: [0.0-1.0]
                Next_Action: [suggested_next_step]
            \"\"\".trimIndent()
            
            val llmResponse = onDeviceLLM!!.generateResponse(
                prompt = analysisPrompt,
                context = context
            )
            
            // Parse LLM response to extract structured information
            val parsedAnalysis = parseLLMAnalysis(llmResponse)
            
            AgentResponse(
                agentId = agentId,
                responseType = ResponseType.ANALYSIS_RESULT,
                content = llmResponse,
                confidence = parsedAnalysis["confidence"] as? Float ?: 0.8f,
                actions = createActionsFromLLMAnalysis(parsedAnalysis),
                nextSuggestedAgent = parsedAnalysis["next_agent"] as? String,
                metadata = mapOf(
                    "llm_analysis" to parsedAnalysis,
                    "processing_method" to "on_device_llm",
                    "model_info" to onDeviceLLM.getModelInfo(),
                    "timestamp" to System.currentTimeMillis()
                )
            )
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error processing with LLM", e)
            // Fallback to traditional methods
            val intent = extractIntent(input.content)
            val entities = extractEntities(input.content)
            val sentiment = analyzeSentiment(input.content)
            
            AgentResponse(
                agentId = agentId,
                responseType = ResponseType.ACTION_COMMAND,
                content = generateResponse(intent, entities),
                confidence = intent.confidence,
                actions = createActionsForIntent(intent, entities),
                nextSuggestedAgent = determineNextAgent(intent),
                metadata = mapOf(
                    "fallback_reason" to e.message,
                    "processing_method" to "traditional_nlp"
                )
            )
        }
    }
    
    /**
     * Build context for LLM processing
     */
    private fun buildLLMContext(input: AgentInput): Map<String, Any> {
        return mapOf(
            "call_context" to (input.context?.toString() ?: ""),
            "conversation_history" to (input.metadata["conversation_history"] ?: emptyList<String>()),
            "business_context" to mapOf(
                "type" to "customer_service",
                "industry" to "general_business", 
                "capabilities" to listOf("appointments", "information", "support")
            ),
            "agent_context" to mapOf(
                "role" to "natural_language_understanding",
                "capabilities" to capabilities.map { it.name }
            )
        )
    }
    
    /**
     * Parse LLM analysis response into structured format
     */
    private fun parseLLMAnalysis(llmResponse: String): Map<String, Any> {
        val analysis = mutableMapOf<String, Any>()
        val lines = llmResponse.lines()
        
        for (line in lines) {
            when {
                line.startsWith("Intent:", ignoreCase = true) -> {
                    analysis["intent"] = line.substringAfter(":").trim()
                }
                line.startsWith("Entities:", ignoreCase = true) -> {
                    val entitiesStr = line.substringAfter(":").trim()
                    analysis["entities"] = entitiesStr.split(",").map { it.trim() }
                }
                line.startsWith("Sentiment:", ignoreCase = true) -> {
                    analysis["sentiment"] = line.substringAfter(":").trim()
                }
                line.startsWith("Confidence:", ignoreCase = true) -> {
                    val confidenceStr = line.substringAfter(":").trim()
                    analysis["confidence"] = confidenceStr.toFloatOrNull() ?: 0.8f
                }
                line.startsWith("Next_Action:", ignoreCase = true) -> {
                    val action = line.substringAfter(":").trim()
                    analysis["next_action"] = action
                    analysis["next_agent"] = mapActionToAgent(action)
                }
            }
        }
        
        return analysis
    }
    
    /**
     * Create actions based on LLM analysis
     */
    private fun createActionsFromLLMAnalysis(analysis: Map<String, Any>): List<AgentAction> {
        val actions = mutableListOf<AgentAction>()
        
        val intent = analysis["intent"] as? String ?: ""
        val nextAction = analysis["next_action"] as? String ?: ""
        
        when {
            intent.contains("appointment", ignoreCase = true) -> {
                actions.add(AgentAction.SCHEDULE_APPOINTMENT)
                actions.add(AgentAction.CHECK_CALENDAR)
            }
            intent.contains("information", ignoreCase = true) -> {
                actions.add(AgentAction.PROVIDE_INFORMATION)
                actions.add(AgentAction.SEARCH_KNOWLEDGE_BASE)
            }
            intent.contains("transfer", ignoreCase = true) || intent.contains("routing", ignoreCase = true) -> {
                actions.add(AgentAction.ROUTE_CALL)
                actions.add(AgentAction.CHECK_AVAILABILITY)
            }
            nextAction.contains("escalate", ignoreCase = true) -> {
                actions.add(AgentAction.ESCALATE_TO_HUMAN)
            }
        }
        
        if (actions.isEmpty()) {
            actions.add(AgentAction.CONTINUE_CONVERSATION)
        }
        
        return actions
    }
    
    /**
     * Map LLM-suggested actions to specific agents
     */
    private fun mapActionToAgent(action: String): String? {
        return when {
            action.contains("appointment", ignoreCase = true) -> "appointment-agent"
            action.contains("route", ignoreCase = true) || action.contains("transfer", ignoreCase = true) -> "call-routing-agent"
            action.contains("emotional", ignoreCase = true) || action.contains("support", ignoreCase = true) -> "customer-service-agent"
            action.contains("integrate", ignoreCase = true) || action.contains("external", ignoreCase = true) -> "integration-agent"
            else -> null
        }
    }
    
    private fun extractIntent(text: String): Intent {
        // Try ML model first (if available), then fallback to rules
        return extractIntentWithRules(text)
    }
    
    private fun extractIntentWithRules(text: String): Intent {
        for ((intentName, patterns) in intentPatterns) {
            for (pattern in patterns) {
                if (pattern.matcher(text).matches()) {
                    return Intent(
                        name = intentName,
                        confidence = 0.8f,
                        parameters = emptyMap()
                    )
                }
            }
        }
        
        // Default intent
        return Intent(
            name = "general_inquiry",
            confidence = 0.5f,
            parameters = emptyMap()
        )
    }
    
    private fun extractEntities(text: String): List<Entity> {
        val entities = mutableListOf<Entity>()
        
        // Extract phone numbers
        val phonePattern = Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b")
        val phoneMatcher = phonePattern.matcher(text)
        while (phoneMatcher.find()) {
            entities.add(Entity(
                type = "phone_number",
                value = phoneMatcher.group(),
                confidence = 0.9f
            ))
        }
        
        // Extract email addresses
        val emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b")
        val emailMatcher = emailPattern.matcher(text)
        while (emailMatcher.find()) {
            entities.add(Entity(
                type = "email",
                value = emailMatcher.group(),
                confidence = 0.9f
            ))
        }
        
        // Extract dates and times (simple patterns)
        val dateTimePatterns = listOf(
            "\\b(today|tomorrow|yesterday)\\b" to "date_relative",
            "\\b\\d{1,2}/\\d{1,2}/\\d{2,4}\\b" to "date",
            "\\b\\d{1,2}:\\d{2}\\s*(am|pm)?\\b" to "time"
        )
        
        for ((pattern, entityType) in dateTimePatterns) {
            val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text)
            while (matcher.find()) {
                entities.add(Entity(
                    type = entityType,
                    value = matcher.group(),
                    confidence = 0.7f
                ))
            }
        }
        
        return entities
    }
    
    private fun analyzeSentiment(text: String): String {
        // Simple sentiment analysis based on keywords
        val positiveWords = listOf("good", "great", "excellent", "happy", "satisfied", "thank", "thanks")
        val negativeWords = listOf("bad", "terrible", "awful", "angry", "upset", "problem", "issue", "wrong")
        
        val words = text.toLowerCase().split("\\s+".toRegex())
        val positiveCount = words.count { it in positiveWords }
        val negativeCount = words.count { it in negativeWords }
        
        return when {
            positiveCount > negativeCount -> "positive"
            negativeCount > positiveCount -> "negative"
            else -> "neutral"
        }
    }
    
    private fun determineNextAgent(intent: Intent): String? {
        return when (intent.name) {
            "appointment_booking" -> "appointment_agent"
            "transfer_request" -> "call_routing"
            "information_request" -> "customer_service"
            "emergency" -> "call_routing"
            else -> "customer_service"
        }
    }
    
    private fun createActionsForIntent(intent: Intent, entities: List<Entity>): List<AgentAction> {
        return when (intent.name) {
            "emergency" -> listOf(
                AgentAction(ActionType.REQUEST_HUMAN_OPERATOR, emptyMap(), priority = 10)
            )
            "transfer_request" -> listOf(
                AgentAction(ActionType.REQUEST_HUMAN_OPERATOR, emptyMap(), priority = 8)
            )
            else -> emptyList()
        }
    }
    
    private fun generateResponse(intent: Intent, entities: List<Entity>): String {
        return when (intent.name) {
            "greeting" -> "Hello! How can I help you today?"
            "appointment_booking" -> "I'd be happy to help you schedule an appointment."
            "information_request" -> "Let me find that information for you."
            "transfer_request" -> "I'll connect you with someone who can help."
            "emergency" -> "I understand this is urgent. Let me get someone to help you right away."
            else -> "I understand. Let me see how I can help you."
        }
    }
    
    override suspend fun shutdown() {
        try {
            Logger.i(TAG, "Shutting down Natural Language Agent")
            
            tfInterpreter?.close()
            tfInterpreter = null
            isInitialized = false
            
            Logger.i(TAG, "Natural Language Agent shutdown complete")
            
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
            responseType = ResponseType.TEXT_RESPONSE,
            content = message,
            confidence = 0.0f
        )
    }
    
    companion object {
        private const val TAG = "NaturalLanguageAgent"
    }
}