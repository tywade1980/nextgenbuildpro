package com.nextgenbuildpro.examples

import android.util.Log
import com.nextgenbuildpro.ai.AIModule
import com.nextgenbuildpro.ai.llm.*
import com.nextgenbuildpro.agents.HermesBrain
import com.nextgenbuildpro.shared.AgentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Example usage of LLM Multi-Agent Cloud Database Integration
 * 
 * This example demonstrates how to use the LLM service for multi-agent coordination
 * with cloud database storage in the NextGenBuildPro system.
 */
class LLMMultiAgentExample {
    
    private val TAG = "LLMMultiAgentExample"
    private val scope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Example 1: Basic multi-agent coordination
     */
    fun exampleBasicCoordination() {
        scope.launch {
            try {
                val llmService = AIModule.getLLMService()
                if (llmService == null) {
                    Log.e(TAG, "LLM service not available")
                    return@launch
                }
                
                Log.i(TAG, "Starting basic multi-agent coordination example...")
                
                // Create a coordination request
                val request = MultiAgentCoordinationRequest(
                    requestingAgent = AgentType.HERMES_BRAIN,
                    targetAgents = listOf(AgentType.MRM, AgentType.BIG_DADDY, AgentType.HRM_MODEL),
                    task = "Setup and coordinate a new commercial construction project",
                    context = "Large office building with 50,000 sq ft, tight 18-month deadline, budget of $5M"
                )
                
                // Generate coordination response
                val result = llmService.generateCoordinationResponse(request)
                
                result.fold(
                    onSuccess = { response ->
                        Log.i(TAG, "Coordination Plan Generated:")
                        Log.i(TAG, "Plan: ${response.plan}")
                        Log.i(TAG, "Agent Assignments:")
                        response.agentAssignments.forEach { (agent, assignment) ->
                            Log.i(TAG, "  $agent: $assignment")
                        }
                        Log.i(TAG, "Estimated Duration: ${response.estimatedDuration} minutes")
                        Log.i(TAG, "Dependencies: ${response.dependencies}")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to generate coordination response", error)
                    }
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in basic coordination example", e)
            }
        }
    }
    
    /**
     * Run all examples
     */
    fun runAllExamples() {
        Log.i(TAG, "Running all LLM Multi-Agent examples...")
        exampleBasicCoordination()
        Log.i(TAG, "All examples initiated. Check logs for results.")
    }
}