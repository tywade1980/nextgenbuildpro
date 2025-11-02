package com.nextgenbuildpro.ai.gemini

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.generationConfig
import com.nextgenbuildpro.core.ApiKeyManager
import com.nextgenbuildpro.shared.AgentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Gemini AI Service for on-device and cloud inference
 * 
 * Supports:
 * - Gemini Nano for on-device inference (requires AICore)
 * - Gemini Pro for cloud inference
 * - Gemini Ultra for advanced reasoning
 * - MLKit Prompt API integration
 * - Streaming and non-streaming responses
 * 
 * BYOK (Bring Your Own Key) enabled via ApiKeyManager
 */
class GeminiService(private val context: Context) {
    
    companion object {
        private const val TAG = "GeminiService"
        
        // Model identifiers
        const val MODEL_NANO = "gemini-nano" // On-device
        const val MODEL_PRO = "gemini-pro" // Cloud - balanced
        const val MODEL_PRO_VISION = "gemini-pro-vision" // Cloud - with vision
        const val MODEL_ULTRA = "gemini-ultra" // Cloud - most capable
    }
    
    private var apiKey: String? = null
    private val modelCache = mutableMapOf<String, GenerativeModel>()
    
    /**
     * Initialize the Gemini service
     * Loads API key from ApiKeyManager (BYOK support)
     */
    fun initialize(): Result<Unit> = try {
        apiKey = ApiKeyManager.getGeminiApiKey()
        
        if (apiKey.isNullOrEmpty()) {
            Log.w(TAG, "Gemini API key not found. Set 'gemini.api.key' in local.properties for cloud inference.")
            Log.i(TAG, "On-device Gemini Nano will be used if available (requires AICore)")
        } else {
            Log.d(TAG, "Gemini service initialized with API key")
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Gemini service", e)
        Result.failure(e)
    }
    
    /**
     * Generate text response using specified Gemini model
     * 
     * @param prompt The input prompt
     * @param modelName The model to use (nano, pro, ultra)
     * @param agentType Optional agent type for context-specific prompting
     * @param temperature Controls randomness (0.0 to 1.0)
     * @param maxTokens Maximum tokens in response
     * @return Result with generated text
     */
    suspend fun generateText(
        prompt: String,
        modelName: String = MODEL_PRO,
        agentType: AgentType? = null,
        temperature: Float = 0.7f,
        maxTokens: Int = 2048
    ): Result<String> = try {
        val model = getOrCreateModel(modelName, temperature, maxTokens)
        
        // Add agent-specific context if provided
        val enhancedPrompt = if (agentType != null) {
            enhancePromptForAgent(prompt, agentType)
        } else {
            prompt
        }
        
        Log.d(TAG, "Generating text with model: $modelName")
        val response = model.generateContent(enhancedPrompt)
        val text = response.text ?: ""
        
        Log.d(TAG, "Generated ${text.length} characters")
        Result.success(text)
    } catch (e: Exception) {
        Log.e(TAG, "Error generating text with Gemini", e)
        Result.failure(e)
    }
    
    /**
     * Generate streaming text response
     * 
     * @param prompt The input prompt
     * @param modelName The model to use
     * @param agentType Optional agent type for context
     * @param temperature Controls randomness
     * @param maxTokens Maximum tokens in response
     * @return Flow of text chunks
     */
    fun generateTextStream(
        prompt: String,
        modelName: String = MODEL_PRO,
        agentType: AgentType? = null,
        temperature: Float = 0.7f,
        maxTokens: Int = 2048
    ): Flow<String> = flow {
        val model = getOrCreateModel(modelName, temperature, maxTokens)
        
        val enhancedPrompt = if (agentType != null) {
            enhancePromptForAgent(prompt, agentType)
        } else {
            prompt
        }
        
        Log.d(TAG, "Starting streaming generation with model: $modelName")
        
        model.generateContentStream(enhancedPrompt).collect { chunk ->
            chunk.text?.let { text ->
                emit(text)
            }
        }
    }.catch { e ->
        Log.e(TAG, "Error in streaming generation", e)
        throw e
    }
    
    /**
     * Generate text with conversation history for multi-turn conversations
     * 
     * @param history List of previous messages (user and model)
     * @param newPrompt The new user prompt
     * @param modelName The model to use
     * @return Result with generated text
     */
    suspend fun generateTextWithHistory(
        history: List<Pair<String, String>>, // List of (role, content)
        newPrompt: String,
        modelName: String = MODEL_PRO
    ): Result<String> = try {
        val model = getOrCreateModel(modelName)
        
        // Build content history
        val contentList = history.flatMap { (role, content) ->
            listOf(
                Content(role = role, parts = listOf(TextPart(content)))
            )
        }
        
        val chat = model.startChat(history = contentList)
        val response = chat.sendMessage(newPrompt)
        val text = response.text ?: ""
        
        Result.success(text)
    } catch (e: Exception) {
        Log.e(TAG, "Error generating text with history", e)
        Result.failure(e)
    }
    
    /**
     * Check if on-device Gemini Nano is available
     * Requires Google AICore to be installed on the device
     * 
     * @return True if Gemini Nano is available for on-device inference
     */
    fun isNanoAvailable(): Boolean {
        // Note: In a production app, you would check for AICore availability
        // For now, we'll return false and use cloud models
        // Implementation would use: GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        Log.d(TAG, "Checking Gemini Nano availability (requires AICore)")
        return false // Will be true when AICore is available on device
    }
    
    /**
     * Enhance prompt with agent-specific context
     */
    private fun enhancePromptForAgent(prompt: String, agentType: AgentType): String {
        val agentContext = when (agentType) {
            AgentType.COO_OPERATIONS_ORCHESTRATOR -> 
                "You are an operations expert for construction project management. Focus on scheduling, resource allocation, and field operations."
            AgentType.CFO_FINANCIAL_ORCHESTRATOR -> 
                "You are a financial expert for construction projects. Focus on cost estimation, budgeting, and financial analysis."
            AgentType.CHRO_CLIENT_HR_ORCHESTRATOR -> 
                "You are a client relations and HR expert. Focus on customer satisfaction, team management, and client communications."
            AgentType.CTO_DESIGN_ORCHESTRATOR -> 
                "You are a design and technology expert. Focus on blueprints, technical specifications, and design quality."
            AgentType.CSO_SAFETY_ORCHESTRATOR -> 
                "You are a safety and compliance expert. Focus on OSHA regulations, safety protocols, and risk mitigation."
            else -> ""
        }
        
        return if (agentContext.isNotEmpty()) {
            "$agentContext\n\n$prompt"
        } else {
            prompt
        }
    }
    
    /**
     * Get or create a Gemini model with specified configuration
     */
    private fun getOrCreateModel(
        modelName: String,
        temperature: Float = 0.7f,
        maxTokens: Int = 2048
    ): GenerativeModel {
        val cacheKey = "$modelName-$temperature-$maxTokens"
        
        return modelCache.getOrPut(cacheKey) {
            val config = generationConfig {
                this.temperature = temperature
                this.maxOutputTokens = maxTokens
            }
            
            if (modelName == MODEL_NANO) {
                // On-device Gemini Nano
                // Note: Requires AICore to be available on device
                Log.d(TAG, "Creating on-device Gemini Nano model")
                GenerativeModel(
                    modelName = "gemini-nano",
                    apiKey = apiKey ?: "", // Nano doesn't require API key
                    generationConfig = config
                )
            } else {
                // Cloud-based models (Pro, Ultra, Pro Vision)
                if (apiKey.isNullOrEmpty()) {
                    throw IllegalStateException("Gemini API key required for cloud models. Set 'gemini.api.key' in local.properties")
                }
                
                Log.d(TAG, "Creating cloud Gemini model: $modelName")
                GenerativeModel(
                    modelName = modelName,
                    apiKey = apiKey!!,
                    generationConfig = config
                )
            }
        }
    }
    
    /**
     * Clear model cache to free memory
     */
    fun clearCache() {
        modelCache.clear()
        Log.d(TAG, "Model cache cleared")
    }
}
