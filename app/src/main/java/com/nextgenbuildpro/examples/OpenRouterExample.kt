package com.nextgenbuildpro.examples

import android.util.Log
import com.nextgenbuildpro.ai.llm.*
import com.nextgenbuildpro.core.api.FirestoreService
import com.nextgenbuildpro.shared.AgentType
import kotlinx.coroutines.runBlocking
import java.util.UUID

/**
 * OpenRouter Usage Examples
 * 
 * This file demonstrates how to use the OpenRouter integration
 * in NextGen BuildPro for LLM-powered multi-agent coordination.
 * 
 * PREREQUISITES:
 * 1. Add your OpenRouter API key to local.properties:
 *    openrouter.api.key=sk-or-v1-your-key-here
 * 2. Ensure ApiKeyManager is initialized in your Application class
 * 3. Add credits to your OpenRouter account
 */
object OpenRouterExample {
    
    private const val TAG = "OpenRouterExample"
    
    /**
     * Example 1: Basic LLM Response Generation
     * 
     * This example shows how to get a simple response from an LLM
     * through the OpenRouter service.
     */
    fun example1_BasicResponse(
        firestoreService: FirestoreService
    ) = runBlocking {
        Log.d(TAG, "=== Example 1: Basic Response Generation ===")
        
        val openRouterService = OpenRouterService(firestoreService)
        
        val result = openRouterService.generateResponse(
            prompt = "What are the key steps in planning a commercial construction project?",
            context = null,
            agentType = AgentType.COO_OPERATIONS_ORCHESTRATOR
        )
        
        result.onSuccess { response ->
            Log.d(TAG, "✅ Success!")
            Log.d(TAG, "Response: ${response.content}")
            Log.d(TAG, "Tokens used: ${response.tokenUsage.totalTokens}")
            Log.d(TAG, "Conversation ID: ${response.conversationId}")
        }
        
        result.onFailure { error ->
            Log.e(TAG, "❌ Error: ${error.message}", error)
        }
    }
    
    /**
     * Example 2: Multi-Agent Coordination
     * 
     * This example demonstrates coordinating multiple agents
     * to handle a complex construction project task.
     */
    fun example2_MultiAgentCoordination(
        firestoreService: FirestoreService
    ) = runBlocking {
        Log.d(TAG, "=== Example 2: Multi-Agent Coordination ===")
        
        val openRouterService = OpenRouterService(firestoreService)
        
        // Create a coordination request
        val request = MultiAgentCoordinationRequest(
            requestingAgent = AgentType.ORCHESTRATOR,
            targetAgents = listOf(
                AgentType.COO_OPERATIONS_ORCHESTRATOR,  // Handle operations
                AgentType.CFO_FINANCIAL_ORCHESTRATOR,    // Handle budget
                AgentType.CTO_DESIGN_ORCHESTRATOR        // Handle design
            ),
            task = "Plan a 50,000 sq ft commercial office building project",
            context = """
                Project Details:
                - Location: Downtown Seattle
                - Timeline: 18 months
                - Budget: $15 million
                - Requirements: LEED Gold certification
                - Client: Tech startup needing modern workspace
            """.trimIndent(),
            priority = "HIGH"
        )
        
        val result = openRouterService.generateCoordinationResponse(request)
        
        result.onSuccess { response ->
            Log.d(TAG, "✅ Coordination Plan Generated!")
            Log.d(TAG, "\n--- PLAN ---\n${response.plan}\n")
            
            Log.d(TAG, "--- AGENT ASSIGNMENTS ---")
            response.agentAssignments.forEach { (agent, task) ->
                Log.d(TAG, "• $agent: $task")
            }
            
            Log.d(TAG, "\n--- TIMELINE ---")
            Log.d(TAG, "Estimated Duration: ${response.estimatedDuration} minutes")
            
            if (response.dependencies.isNotEmpty()) {
                Log.d(TAG, "\n--- DEPENDENCIES ---")
                response.dependencies.forEach { dependency ->
                    Log.d(TAG, "• $dependency")
                }
            }
        }
        
        result.onFailure { error ->
            Log.e(TAG, "❌ Coordination Error: ${error.message}", error)
        }
    }
    
    /**
     * Example 3: Conversation with Context
     * 
     * This example shows how to maintain a conversation
     * with context across multiple exchanges.
     */
    fun example3_ConversationWithContext(
        firestoreService: FirestoreService
    ) = runBlocking {
        Log.d(TAG, "=== Example 3: Conversation with Context ===")
        
        val openRouterService = OpenRouterService(firestoreService)
        val conversationId = UUID.randomUUID().toString()
        
        // First message
        val context1 = LLMContext(
            conversationId = conversationId,
            systemPrompt = "You are a construction project financial advisor. Provide accurate cost estimates and budget advice.",
            previousMessages = emptyList()
        )
        
        val result1 = openRouterService.generateResponse(
            prompt = "What's the typical cost per square foot for a commercial office building in Seattle?",
            context = context1,
            agentType = AgentType.CFO_FINANCIAL_ORCHESTRATOR
        )
        
        result1.onSuccess { response1 ->
            Log.d(TAG, "💬 Question 1: Cost per sq ft")
            Log.d(TAG, "📝 Answer: ${response1.content}\n")
            
            // Second message with context from first
            val context2 = LLMContext(
                conversationId = conversationId,
                systemPrompt = context1.systemPrompt,
                previousMessages = listOf(
                    LLMMessage("user", "What's the typical cost per square foot for a commercial office building in Seattle?"),
                    LLMMessage("assistant", response1.content)
                )
            )
            
            val result2 = openRouterService.generateResponse(
                prompt = "Based on that, what would be the estimated total cost for a 50,000 sq ft building?",
                context = context2,
                agentType = AgentType.CFO_FINANCIAL_ORCHESTRATOR
            )
            
            result2.onSuccess { response2 ->
                Log.d(TAG, "💬 Question 2: Total cost estimate")
                Log.d(TAG, "📝 Answer: ${response2.content}\n")
                Log.d(TAG, "✅ Conversation completed successfully!")
            }
        }
    }
    
    /**
     * Example 4: Direct OpenRouter Client Usage
     * 
     * For advanced use cases, you can use the OpenRouterClient directly
     * to have more control over the API parameters.
     */
    fun example4_DirectClientUsage() = runBlocking {
        Log.d(TAG, "=== Example 4: Direct Client Usage ===")
        
        val client = OpenRouterClient()
        
        // Build custom messages
        val messages = listOf(
            ChatMessage(
                role = "system",
                content = "You are an expert construction estimator. Provide detailed, accurate estimates."
            ),
            ChatMessage(
                role = "user",
                content = "Estimate the material cost for a 10,000 sq ft concrete slab, 6 inches thick."
            )
        )
        
        // Use a specific model
        val result = client.chatCompletion(
            messages = messages,
            model = "anthropic/claude-3-sonnet",  // Specific model
            temperature = 0.3,  // Lower temperature for more consistent estimates
            maxTokens = 1024
        )
        
        result.onSuccess { response ->
            Log.d(TAG, "✅ Direct API Call Successful!")
            Log.d(TAG, "Model: ${response.model}")
            Log.d(TAG, "Response: ${response.content}")
            Log.d(TAG, "Tokens: ${response.usage.totalTokens} (${response.usage.promptTokens} prompt + ${response.usage.completionTokens} completion)")
        }
        
        result.onFailure { error ->
            Log.e(TAG, "❌ API Error: ${error.message}", error)
        }
    }
    
    /**
     * Example 5: List Available Models
     * 
     * This example shows how to retrieve the list of available models
     * from OpenRouter.
     */
    fun example5_ListAvailableModels() = runBlocking {
        Log.d(TAG, "=== Example 5: List Available Models ===")
        
        val client = OpenRouterClient()
        
        val result = client.listModels()
        
        result.onSuccess { models ->
            Log.d(TAG, "✅ Found ${models.size} available models:")
            
            // Show some popular models
            val popularModels = models.filter { model ->
                model.contains("gpt", ignoreCase = true) ||
                model.contains("claude", ignoreCase = true) ||
                model.contains("llama", ignoreCase = true)
            }
            
            Log.d(TAG, "\n--- Popular Models ---")
            popularModels.take(10).forEach { model ->
                Log.d(TAG, "• $model")
            }
            
            if (popularModels.size > 10) {
                Log.d(TAG, "... and ${popularModels.size - 10} more")
            }
        }
        
        result.onFailure { error ->
            Log.e(TAG, "❌ Error listing models: ${error.message}", error)
        }
    }
    
    /**
     * Example 6: Error Handling
     * 
     * This example demonstrates proper error handling
     * for various failure scenarios.
     */
    fun example6_ErrorHandling(
        firestoreService: FirestoreService
    ) = runBlocking {
        Log.d(TAG, "=== Example 6: Error Handling ===")
        
        val openRouterService = OpenRouterService(firestoreService)
        
        val result = openRouterService.generateResponse(
            prompt = "Generate a project plan",
            context = null,
            agentType = AgentType.ORCHESTRATOR
        )
        
        result.fold(
            onSuccess = { response ->
                Log.d(TAG, "✅ Success: ${response.content}")
                
                // Process successful response
                processResponse(response)
            },
            onFailure = { error ->
                Log.e(TAG, "❌ Error occurred: ${error.message}")
                
                // Handle specific error types
                when (error) {
                    is IllegalStateException -> {
                        // API key not configured
                        Log.e(TAG, "⚠️  Configuration Error: API key not found")
                        Log.e(TAG, "Please add 'openrouter.api.key' to local.properties")
                        // Show user-friendly error message
                    }
                    
                    is java.net.SocketTimeoutException -> {
                        // Network timeout
                        Log.e(TAG, "⏱️  Timeout: Request took too long")
                        Log.e(TAG, "Consider using a faster model or increasing timeout")
                        // Retry with exponential backoff
                    }
                    
                    is java.net.UnknownHostException -> {
                        // No internet connection
                        Log.e(TAG, "🌐 Network Error: No internet connection")
                        // Queue request for later or show offline mode
                    }
                    
                    else -> {
                        // Generic error
                        Log.e(TAG, "💥 Unexpected Error: ${error.javaClass.simpleName}")
                        // Log to crash reporting service
                    }
                }
            }
        )
    }
    
    /**
     * Process a successful LLM response
     */
    private fun processResponse(response: LLMResponse) {
        // Store in database for learning
        Log.d(TAG, "Processing response: ${response.conversationId}")
        
        // Track token usage for cost monitoring
        Log.d(TAG, "Token usage: ${response.tokenUsage.totalTokens} tokens")
        
        // Update agent knowledge base
        Log.d(TAG, "Updating agent knowledge from response")
        
        // Trigger follow-up actions based on content
        if (response.content.contains("approve", ignoreCase = true)) {
            Log.d(TAG, "Response requires approval - routing to human")
        }
    }
    
    /**
     * Run all examples
     * 
     * Call this method to run all the examples in sequence.
     * Note: This will consume OpenRouter credits!
     */
    fun runAllExamples(firestoreService: FirestoreService) {
        Log.d(TAG, "\n" + "=".repeat(50))
        Log.d(TAG, "OpenRouter Integration Examples")
        Log.d(TAG, "=".repeat(50) + "\n")
        
        example1_BasicResponse(firestoreService)
        Thread.sleep(2000) // Brief pause between examples
        
        example2_MultiAgentCoordination(firestoreService)
        Thread.sleep(2000)
        
        example3_ConversationWithContext(firestoreService)
        Thread.sleep(2000)
        
        example4_DirectClientUsage()
        Thread.sleep(2000)
        
        example5_ListAvailableModels()
        Thread.sleep(2000)
        
        example6_ErrorHandling(firestoreService)
        
        Log.d(TAG, "\n" + "=".repeat(50))
        Log.d(TAG, "All examples completed!")
        Log.d(TAG, "=".repeat(50) + "\n")
    }
}
