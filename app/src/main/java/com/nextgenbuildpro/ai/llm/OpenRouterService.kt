package com.nextgenbuildpro.ai.llm

import android.util.Log
import com.nextgenbuildpro.core.api.FirestoreService
import com.nextgenbuildpro.shared.AgentType
import java.time.LocalDateTime
import java.util.UUID

/**
 * OpenRouter LLM Service Implementation
 * 
 * Provides LLM capabilities using OpenRouter's unified API for access to multiple
 * LLM providers (OpenAI, Anthropic, Meta, Google, etc.) through a single interface.
 * 
 * This service integrates with the NextGen BuildPro multi-agent system and stores
 * conversation history in Firestore for learning and audit purposes.
 */
class OpenRouterService(
    private val firestoreService: FirestoreService,
    private val openRouterClient: OpenRouterClient = OpenRouterClient()
) : LLMService {
    
    companion object {
        private const val TAG = "OpenRouterService"
        private const val CONVERSATIONS_COLLECTION = "llm_conversations"
        private const val AGENT_CONTEXTS_COLLECTION = "agent_llm_contexts"
        
        // Recommended models for different use cases
        const val MODEL_REASONING = "openai/o1-preview" // Complex reasoning
        const val MODEL_AGENT_WORKFLOW = "anthropic/claude-3-opus" // Agent coordination
        const val MODEL_FAST_INFERENCE = "openai/gpt-3.5-turbo" // Quick tasks
        const val MODEL_CODE_GEN = "anthropic/claude-3-sonnet" // Code generation
        const val MODEL_COST_EFFECTIVE = "meta-llama/llama-3-8b-instruct" // Budget-friendly
    }
    
    // Agent-specific system prompts
    private val agentSystemPrompts = mapOf(
        AgentType.ORCHESTRATOR to """You are the Main Orchestrator for NextGen BuildPro, a construction management AI system.
            Your role is to coordinate between multiple specialized AI agents and ensure smooth workflow execution.
            You prioritize tasks, delegate to appropriate agents, and maintain system coherence.""",
        
        AgentType.COO_OPERATIONS_ORCHESTRATOR to """You are the COO Operations Orchestrator for NextGen BuildPro.
            You manage project operations, resource allocation, and schedule optimization for construction projects.
            You coordinate with field teams and ensure project milestones are met.""",
        
        AgentType.CFO_FINANCIAL_ORCHESTRATOR to """You are the CFO Financial Orchestrator for NextGen BuildPro.
            You handle financial analysis, cost estimation, budget management, and profitability tracking.
            You provide insights on project finances and recommend cost optimization strategies.""",
        
        AgentType.CHRO_CLIENT_HR_ORCHESTRATOR to """You are the CHRO/CMO Client & HR Orchestrator for NextGen BuildPro.
            You manage client relationships, marketing initiatives, and human resource coordination.
            You handle client communications and ensure customer satisfaction.""",
        
        AgentType.CTO_DESIGN_ORCHESTRATOR to """You are the CTO Design Orchestrator for NextGen BuildPro.
            You coordinate design processes, manage blueprints, and oversee technical specifications.
            You ensure design quality and technical feasibility of construction projects.""",
        
        AgentType.CSO_SAFETY_ORCHESTRATOR to """You are the CSO Safety Orchestrator for NextGen BuildPro.
            You manage safety protocols, compliance requirements, and risk mitigation strategies.
            You ensure all operations meet safety standards and regulatory requirements."""
    )
    
    override suspend fun generateResponse(
        prompt: String,
        context: LLMContext?,
        agentType: AgentType
    ): Result<LLMResponse> {
        return try {
        Log.d(TAG, "Generating response for agent: $agentType")
        
        val conversationId = context?.conversationId ?: UUID.randomUUID().toString()
        val systemPrompt = context?.systemPrompt 
            ?: agentSystemPrompts[agentType] 
            ?: "You are a helpful AI assistant for construction project management."
        
        // Build messages for OpenRouter
        val messages = mutableListOf<ChatMessage>()
        
        // Add system prompt
        messages.add(ChatMessage("system", systemPrompt))
        
        // Add previous messages if available
        context?.previousMessages?.forEach { msg ->
            messages.add(ChatMessage(msg.role, msg.content))
        }
        
        // Add current prompt
        messages.add(ChatMessage("user", prompt))
        
        // Select appropriate model based on task complexity
        val model = selectModelForTask(prompt, agentType)
        
        // Call OpenRouter API
        val openRouterResult = openRouterClient.chatCompletion(
            messages = messages,
            model = model,
            temperature = 0.7,
            maxTokens = 2048
        )
        
        if (openRouterResult.isFailure) {
            return Result.failure(openRouterResult.exceptionOrNull()!!)
        }
        
        val openRouterResponse = openRouterResult.getOrNull()!!
        
        // Convert to LLM response format
        val llmResponse = LLMResponse(
            content = openRouterResponse.content,
            conversationId = conversationId,
            tokenUsage = com.nextgenbuildpro.ai.llm.TokenUsage(
                promptTokens = openRouterResponse.usage.promptTokens,
                completionTokens = openRouterResponse.usage.completionTokens,
                totalTokens = openRouterResponse.usage.totalTokens
            ),
            timestamp = LocalDateTime.now(),
            metadata = mapOf(
                "model" to openRouterResponse.model,
                "finish_reason" to openRouterResponse.finishReason,
                "agent_type" to agentType.name
            )
        )
        
        // Store conversation if context provided
        context?.let {
            val conversation = LLMConversation(
                id = conversationId,
                participants = listOf(agentType),
                messages = it.previousMessages + listOf(
                    LLMMessage("user", prompt, agentType = agentType),
                    LLMMessage("assistant", openRouterResponse.content)
                ),
                startTime = it.previousMessages.firstOrNull()?.timestamp ?: LocalDateTime.now(),
                lastUpdate = LocalDateTime.now(),
                metadata = mapOf("model" to openRouterResponse.model)
            )
            storeConversation(conversation)
        }
        
        Log.d(TAG, "Response generated successfully using model: ${openRouterResponse.model}")
        Result.success(llmResponse)
        
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response", e)
            Result.failure(e)
        }
    }
    
    override suspend fun generateCoordinationResponse(
        request: MultiAgentCoordinationRequest
    ): Result<MultiAgentCoordinationResponse> {
        return try {
        Log.d(TAG, "Generating coordination response for: ${request.requestingAgent}")
        
        // Build coordination prompt
        val coordinationPrompt = buildCoordinationPrompt(request)
        
        // Use reasoning model for complex coordination
        val messages = listOf(
            ChatMessage("system", "You are an expert at coordinating multiple AI agents in a construction management system. Provide clear, actionable coordination plans."),
            ChatMessage("user", coordinationPrompt)
        )
        
        val openRouterResult = openRouterClient.chatCompletion(
            messages = messages,
            model = MODEL_AGENT_WORKFLOW,
            temperature = 0.5, // Lower temperature for more consistent coordination
            maxTokens = 4096
        )
        
        if (openRouterResult.isFailure) {
            return Result.failure(openRouterResult.exceptionOrNull()!!)
        }
        
        val openRouterResponse = openRouterResult.getOrNull()!!
        
        // Parse coordination response
        val response = parseCoordinationResponse(request, openRouterResponse.content)
        
        Log.d(TAG, "Coordination response generated successfully")
        Result.success(response)
        
        } catch (e: Exception) {
            Log.e(TAG, "Error generating coordination response", e)
            Result.failure(e)
        }
    }
    
    override suspend fun storeConversation(conversation: LLMConversation): Result<Unit> = try {
        // Delegate to FirestoreService for storage
        Log.d(TAG, "Storing conversation: ${conversation.id}")
        
        val conversationData = mapOf(
            "id" to conversation.id,
            "participants" to conversation.participants.map { it.name },
            "messageCount" to conversation.messages.size,
            "startTime" to conversation.startTime.toString(),
            "lastUpdate" to conversation.lastUpdate.toString(),
            "status" to conversation.status.name,
            "metadata" to conversation.metadata
        )
        
        firestoreService.getCollection(CONVERSATIONS_COLLECTION)
            .document(conversation.id)
            .set(conversationData)
        
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error storing conversation", e)
        Result.failure(e)
    }
    
    override suspend fun getConversationHistory(conversationId: String): Result<LLMConversation> {
        // Implementation would retrieve from Firestore
        return Result.failure(NotImplementedError("Conversation history retrieval not yet implemented"))
    }
    
    override suspend fun getAgentContext(agentType: AgentType): Result<AgentLLMContext> = try {
        Log.d(TAG, "Getting agent context for: $agentType")
        
        val context = AgentLLMContext(
            agentType = agentType,
            systemPrompt = agentSystemPrompts[agentType] 
                ?: "You are a helpful AI assistant for construction project management.",
            capabilities = getAgentCapabilities(agentType),
            currentTasks = emptyList(),
            knowledgeBase = mapOf(
                "provider" to "OpenRouter",
                "available_models" to listOf(
                    MODEL_REASONING,
                    MODEL_AGENT_WORKFLOW,
                    MODEL_FAST_INFERENCE,
                    MODEL_CODE_GEN,
                    MODEL_COST_EFFECTIVE
                )
            )
        )
        
        Result.success(context)
    } catch (e: Exception) {
        Log.e(TAG, "Error getting agent context", e)
        Result.failure(e)
    }
    
    // Private helper methods
    
    /**
     * Select the most appropriate OpenRouter model based on task complexity
     */
    private fun selectModelForTask(prompt: String, agentType: AgentType): String {
        return when {
            // Complex reasoning tasks
            prompt.contains("analyze", ignoreCase = true) ||
            prompt.contains("optimize", ignoreCase = true) ||
            prompt.contains("strategy", ignoreCase = true) -> MODEL_REASONING
            
            // Agent coordination
            prompt.contains("coordinate", ignoreCase = true) ||
            prompt.contains("orchestrate", ignoreCase = true) -> MODEL_AGENT_WORKFLOW
            
            // Code generation
            prompt.contains("code", ignoreCase = true) ||
            prompt.contains("script", ignoreCase = true) -> MODEL_CODE_GEN
            
            // Simple queries - use cost-effective model
            prompt.length < 100 -> MODEL_FAST_INFERENCE
            
            // Default to agent workflow model for most tasks
            else -> MODEL_AGENT_WORKFLOW
        }
    }
    
    private fun buildCoordinationPrompt(request: MultiAgentCoordinationRequest): String {
        return """
            Multi-Agent Coordination Request:
            
            Requesting Agent: ${request.requestingAgent}
            Target Agents: ${request.targetAgents.joinToString(", ")}
            Task: ${request.task}
            Context: ${request.context}
            Priority: ${request.priority}
            
            Please provide a detailed coordination plan that includes:
            1. Overall execution strategy
            2. Specific assignments for each agent
            3. Estimated duration in minutes
            4. Dependencies and prerequisites
            5. Success criteria
            
            Format your response as a structured plan.
        """.trimIndent()
    }
    
    private fun parseCoordinationResponse(
        request: MultiAgentCoordinationRequest,
        response: String
    ): MultiAgentCoordinationResponse {
        // Simple parsing - in production, this could be more sophisticated
        val agentAssignments = request.targetAgents.associateWith { agent ->
            "Execute ${request.task} - Specialized handling by ${agent.name}"
        }
        
        // Estimate duration based on complexity
        val estimatedDuration = when {
            request.priority == "CRITICAL" -> 30L
            request.targetAgents.size > 3 -> 90L
            else -> 60L
        }
        
        return MultiAgentCoordinationResponse(
            plan = response,
            agentAssignments = agentAssignments,
            estimatedDuration = estimatedDuration,
            dependencies = extractDependencies(request.targetAgents)
        )
    }
    
    private fun extractDependencies(agents: List<AgentType>): List<String> {
        val dependencies = mutableListOf<String>()
        
        if (agents.contains(AgentType.COO_OPERATIONS_ORCHESTRATOR)) {
            dependencies.add("Resource availability validation")
        }
        if (agents.contains(AgentType.CFO_FINANCIAL_ORCHESTRATOR)) {
            dependencies.add("Budget approval")
        }
        if (agents.contains(AgentType.CSO_SAFETY_ORCHESTRATOR)) {
            dependencies.add("Safety compliance check")
        }
        
        return dependencies
    }
    
    private fun getAgentCapabilities(agentType: AgentType): List<String> {
        return when (agentType) {
            AgentType.ORCHESTRATOR -> listOf(
                "Multi-agent coordination",
                "Task routing",
                "System management",
                "Workflow optimization"
            )
            AgentType.COO_OPERATIONS_ORCHESTRATOR -> listOf(
                "Project management",
                "Resource allocation",
                "Schedule optimization",
                "Field coordination"
            )
            AgentType.CFO_FINANCIAL_ORCHESTRATOR -> listOf(
                "Financial analysis",
                "Cost estimation",
                "Budget management",
                "Profitability tracking"
            )
            AgentType.CHRO_CLIENT_HR_ORCHESTRATOR -> listOf(
                "Client relationship management",
                "Marketing coordination",
                "HR management",
                "Communication handling"
            )
            AgentType.CTO_DESIGN_ORCHESTRATOR -> listOf(
                "Design coordination",
                "Blueprint management",
                "Technical specifications",
                "CAD integration"
            )
            AgentType.CSO_SAFETY_ORCHESTRATOR -> listOf(
                "Safety protocol management",
                "Compliance monitoring",
                "Risk assessment",
                "Incident response"
            )
            else -> listOf("Task processing", "Information retrieval", "General assistance")
        }
    }
}
