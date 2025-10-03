package com.nextgenbuildpro.ai.llm

import android.util.Log
import com.nextgenbuildpro.core.api.FirestoreService
import com.nextgenbuildpro.shared.AgentType
import java.time.LocalDateTime
import java.util.UUID

/**
 * Kilocode LLM Service Implementation
 * 
 * Specialized service for large-scale code analysis and refactoring using Kilocode's
 * 1M+ token context window. This service is ideal for:
 * - Comprehensive codebase analysis
 * - System-wide refactoring
 * - Architecture review and recommendations
 * - Technical debt assessment
 * - Large-scale code understanding
 * 
 * Falls back to OpenRouter for standard queries when Kilocode is not available.
 */
class KilocodeService(
    private val firestoreService: FirestoreService,
    private val kilocodeClient: KilocodeClient = KilocodeClient(),
    private val openRouterFallback: OpenRouterService? = null
) : LLMService {
    
    companion object {
        private const val TAG = "KilocodeService"
        private const val CONVERSATIONS_COLLECTION = "kilocode_conversations"
        private const val ANALYSIS_COLLECTION = "kilocode_analyses"
        
        // Threshold for using Kilocode vs fallback (token count)
        private const val LARGE_CONTEXT_THRESHOLD = 32000
    }
    
    override suspend fun generateResponse(
        prompt: String,
        context: LLMContext?,
        agentType: AgentType
    ): Result<LLMResponse> {
        return try {
            Log.d(TAG, "Generating Kilocode response for agent: $agentType")
            
            val conversationId = context?.conversationId ?: UUID.randomUUID().toString()
            val systemPrompt = getSystemPrompt(agentType, context?.systemPrompt)
            
            // Build messages
            val messages = mutableListOf<ChatMessage>()
            messages.add(ChatMessage("system", systemPrompt))
            
            // Add previous messages if available
            context?.previousMessages?.forEach { msg ->
                messages.add(ChatMessage(msg.role, msg.content))
            }
            
            messages.add(ChatMessage("user", prompt))
            
            // Estimate context size
            val estimatedTokens = estimateTokenCount(messages)
            
            // Use Kilocode for large contexts, fallback for smaller ones
            val result = if (estimatedTokens >= LARGE_CONTEXT_THRESHOLD || context?.metadata?.get("force_kilocode") == true) {
                Log.d(TAG, "Using Kilocode for large context ($estimatedTokens tokens)")
                kilocodeClient.chatCompletion(
                    messages = messages,
                    model = selectKilocodeModel(agentType),
                    temperature = 0.7,
                    maxTokens = 4096,
                    contextSize = estimatedTokens.toLong()
                )
            } else if (openRouterFallback != null) {
                Log.d(TAG, "Using OpenRouter fallback for standard context ($estimatedTokens tokens)")
                return openRouterFallback.generateResponse(prompt, context, agentType)
            } else {
                Log.d(TAG, "Using Kilocode despite smaller context (no fallback available)")
                kilocodeClient.chatCompletion(
                    messages = messages,
                    model = KilocodeClient.MODEL_GENERAL,
                    temperature = 0.7,
                    maxTokens = 4096
                )
            }
            
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull()!!)
            }
            
            val kilocodeResponse = result.getOrNull()!!
            
            val llmResponse = LLMResponse(
                content = kilocodeResponse.content,
                conversationId = conversationId,
                tokenUsage = kilocodeResponse.usage,
                timestamp = LocalDateTime.now(),
                metadata = mapOf(
                    "model" to kilocodeResponse.model,
                    "provider" to "kilocode",
                    "finish_reason" to kilocodeResponse.finishReason,
                    "agent_type" to agentType.name,
                    "context_size" to estimatedTokens
                )
            )
            
            // Store conversation
            context?.let {
                val conversation = LLMConversation(
                    id = conversationId,
                    participants = listOf(agentType),
                    messages = it.previousMessages + listOf(
                        LLMMessage("user", prompt, agentType = agentType),
                        LLMMessage("assistant", kilocodeResponse.content)
                    ),
                    startTime = it.previousMessages.firstOrNull()?.timestamp ?: LocalDateTime.now(),
                    lastUpdate = LocalDateTime.now(),
                    metadata = mapOf(
                        "model" to kilocodeResponse.model,
                        "provider" to "kilocode"
                    )
                )
                storeConversation(conversation)
            }
            
            Log.d(TAG, "Kilocode response generated successfully")
            Result.success(llmResponse)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Kilocode response", e)
            // Try fallback if available
            if (openRouterFallback != null) {
                Log.d(TAG, "Attempting OpenRouter fallback after Kilocode error")
                return openRouterFallback.generateResponse(prompt, context, agentType)
            }
            Result.failure(e)
        }
    }
    
    override suspend fun generateCoordinationResponse(
        request: MultiAgentCoordinationRequest
    ): Result<MultiAgentCoordinationResponse> {
        return try {
            Log.d(TAG, "Generating Kilocode coordination response")
            
            val coordinationPrompt = buildCoordinationPrompt(request)
            val messages = listOf(
                ChatMessage("system", "You are Kilocode, an expert AI coordinator with 1M token context. Analyze the full system state and provide optimal multi-agent coordination plans."),
                ChatMessage("user", coordinationPrompt)
            )
            
            val result = kilocodeClient.chatCompletion(
                messages = messages,
                model = KilocodeClient.MODEL_ARCHITECTURE,
                temperature = 0.5,
                maxTokens = 4096
            )
            
            if (result.isFailure) {
                // Try fallback if available
                if (openRouterFallback != null) {
                    return openRouterFallback.generateCoordinationResponse(request)
                }
                return Result.failure(result.exceptionOrNull()!!)
            }
            
            val response = result.getOrNull()!!
            val coordinationResponse = parseCoordinationResponse(request, response.content)
            
            Log.d(TAG, "Kilocode coordination response generated successfully")
            Result.success(coordinationResponse)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Kilocode coordination response", e)
            if (openRouterFallback != null) {
                return openRouterFallback.generateCoordinationResponse(request)
            }
            Result.failure(e)
        }
    }
    
    override suspend fun storeConversation(conversation: LLMConversation): Result<Unit> = try {
        Log.d(TAG, "Storing Kilocode conversation: ${conversation.id}")
        
        val conversationData = mapOf(
            "id" to conversation.id,
            "participants" to conversation.participants.map { it.name },
            "messageCount" to conversation.messages.size,
            "startTime" to conversation.startTime.toString(),
            "lastUpdate" to conversation.lastUpdate.toString(),
            "status" to conversation.status.name,
            "metadata" to conversation.metadata,
            "provider" to "kilocode"
        )
        
        firestoreService.setDocument(CONVERSATIONS_COLLECTION, conversation.id, conversationData)
        Result.success(Unit)
        
    } catch (e: Exception) {
        Log.e(TAG, "Error storing Kilocode conversation", e)
        Result.failure(e)
    }
    
    override suspend fun getConversationHistory(conversationId: String): Result<LLMConversation> = try {
        Log.d(TAG, "Retrieving Kilocode conversation: $conversationId")
        
        val result = firestoreService.getDocument(CONVERSATIONS_COLLECTION, conversationId)
        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull()!!)
        }
        
        val data = result.getOrNull()!!
        
        // Simple conversation reconstruction
        val conversation = LLMConversation(
            id = conversationId,
            participants = emptyList(),
            messages = emptyList(),
            startTime = LocalDateTime.now(),
            lastUpdate = LocalDateTime.now(),
            metadata = data["metadata"] as? Map<String, Any> ?: emptyMap()
        )
        
        Result.success(conversation)
        
    } catch (e: Exception) {
        Log.e(TAG, "Error retrieving Kilocode conversation", e)
        Result.failure(e)
    }
    
    override suspend fun getAgentContext(agentType: AgentType): Result<AgentLLMContext> = try {
        Log.d(TAG, "Getting Kilocode agent context for: $agentType")
        
        val systemPrompt = getSystemPrompt(agentType)
        val capabilities = getAgentCapabilities(agentType)
        
        val context = AgentLLMContext(
            agentType = agentType,
            systemPrompt = systemPrompt,
            capabilities = capabilities,
            currentTasks = emptyList(),
            knowledgeBase = mapOf("provider" to "kilocode", "context_window" to "1M+")
        )
        
        Result.success(context)
        
    } catch (e: Exception) {
        Log.e(TAG, "Error getting Kilocode agent context", e)
        Result.failure(e)
    }
    
    /**
     * Analyze entire codebase using Kilocode's large context window
     */
    suspend fun analyzeCodebase(
        codebaseContent: Map<String, String>,
        analysisType: String = "comprehensive",
        focusAreas: List<String> = emptyList()
    ): Result<CodebaseAnalysis> {
        return try {
            Log.d(TAG, "Starting Kilocode codebase analysis - Files: ${codebaseContent.size}")
            
            val result = kilocodeClient.analyzeCodebase(
                codebaseContent = codebaseContent,
                analysisType = analysisType,
                focusAreas = focusAreas
            )
            
            if (result.isSuccess) {
                val analysis = result.getOrNull()!!
                
                // Store analysis in Firestore
                val analysisData = mapOf(
                    "analysisType" to analysis.analysisType,
                    "fileCount" to analysis.fileCount,
                    "totalLines" to analysis.totalLines,
                    "tokenUsage" to analysis.tokenUsage,
                    "model" to analysis.model,
                    "timestamp" to analysis.timestamp
                )
                
                firestoreService.setDocument(
                    ANALYSIS_COLLECTION,
                    "analysis_${analysis.timestamp}",
                    analysisData
                )
                
                Log.d(TAG, "Codebase analysis completed and stored")
            }
            
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing codebase with Kilocode", e)
            Result.failure(e)
        }
    }
    
    // === PRIVATE HELPER METHODS ===
    
    private fun getSystemPrompt(agentType: AgentType, customPrompt: String? = null): String {
        return customPrompt ?: when (agentType) {
            AgentType.ORCHESTRATOR -> """You are the Main Orchestrator for NextGen BuildPro with access to Kilocode's 1M token context.
                Coordinate multiple AI agents, analyze entire project codebases, and provide system-wide insights."""
            
            AgentType.COO_OPERATIONS_ORCHESTRATOR -> """You are the COO Operations Orchestrator with Kilocode's large context capabilities.
                Analyze project operations across the entire system, optimize resource allocation, and provide comprehensive insights."""
            
            AgentType.CFO_FINANCIAL_ORCHESTRATOR -> """You are the CFO Financial Orchestrator using Kilocode for comprehensive financial analysis.
                Review entire project portfolios, identify cost optimization opportunities across all projects."""
            
            AgentType.CTO_DESIGN_ORCHESTRATOR -> """You are the CTO Design Orchestrator with Kilocode's architectural analysis capabilities.
                Review system architecture, identify refactoring opportunities, and provide design recommendations."""
            
            else -> """You are an AI assistant with Kilocode's 1M token context window for construction project management.
                Provide comprehensive analysis and insights across large codebases and project data."""
        }
    }
    
    private fun selectKilocodeModel(agentType: AgentType): String {
        return when (agentType) {
            AgentType.CTO_DESIGN_ORCHESTRATOR -> KilocodeClient.MODEL_ARCHITECTURE
            AgentType.ORCHESTRATOR -> KilocodeClient.MODEL_ARCHITECTURE
            else -> KilocodeClient.MODEL_GENERAL
        }
    }
    
    private fun getAgentCapabilities(agentType: AgentType): List<String> {
        return listOf(
            "Large context understanding (1M+ tokens)",
            "Comprehensive code analysis",
            "System-wide refactoring recommendations",
            "Architecture review",
            "Technical debt identification"
        )
    }
    
    private fun estimateTokenCount(messages: List<ChatMessage>): Int {
        // Rough estimation: ~4 characters per token
        return messages.sumOf { it.content.length / 4 }
    }
    
    private fun buildCoordinationPrompt(request: MultiAgentCoordinationRequest): String {
        return buildString {
            append("# Multi-Agent Coordination Request\n\n")
            append("**Requesting Agent**: ${request.requestingAgent}\n")
            append("**Target Agents**: ${request.targetAgents.joinToString(", ")}\n")
            append("**Priority**: ${request.priority}\n\n")
            append("**Task**: ${request.task}\n\n")
            append("**Context**: ${request.context}\n\n")
            append("Please provide a detailed coordination plan including:\n")
            append("1. Agent assignments and responsibilities\n")
            append("2. Task dependencies and sequencing\n")
            append("3. Estimated duration for each component\n")
            append("4. Success criteria and validation steps\n")
        }
    }
    
    private fun parseCoordinationResponse(
        request: MultiAgentCoordinationRequest,
        responseContent: String
    ): MultiAgentCoordinationResponse {
        // Simple parsing - in production, use structured output
        val agentAssignments = request.targetAgents.associateWith { agent ->
            "Assigned to: ${request.task}"
        }
        
        return MultiAgentCoordinationResponse(
            plan = responseContent,
            agentAssignments = agentAssignments,
            estimatedDuration = 60, // 1 hour default
            dependencies = emptyList()
        )
    }
}
