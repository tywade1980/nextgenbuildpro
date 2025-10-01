package com.nextgenbuildpro.ai.llm

import android.util.Log
import com.nextgenbuildpro.core.ApiKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * OpenRouter API Client
 * 
 * Provides access to multiple LLM providers through OpenRouter's unified API.
 * Supports various models including GPT-4, Claude, Llama, and more.
 * 
 * API Documentation: https://openrouter.ai/docs
 */
class OpenRouterClient {
    
    companion object {
        private const val TAG = "OpenRouterClient"
        private const val BASE_URL = "https://openrouter.ai/api/v1"
        private const val CHAT_COMPLETIONS_ENDPOINT = "$BASE_URL/chat/completions"
        private const val DEFAULT_MODEL = "openai/gpt-3.5-turbo" // Cost-effective default
        private const val TIMEOUT_MS = 60000 // 60 seconds
    }
    
    /**
     * Send a chat completion request to OpenRouter
     * 
     * @param messages List of chat messages (role: "system", "user", "assistant")
     * @param model OpenRouter model identifier (e.g., "openai/gpt-4", "anthropic/claude-3-opus")
     * @param temperature Sampling temperature (0.0 to 2.0)
     * @param maxTokens Maximum tokens to generate
     * @return OpenRouterResponse containing the completion
     */
    suspend fun chatCompletion(
        messages: List<ChatMessage>,
        model: String = DEFAULT_MODEL,
        temperature: Double = 0.7,
        maxTokens: Int = 2048
    ): Result<OpenRouterResponse> = withContext(Dispatchers.IO) {
        try {
            val apiKey = ApiKeyManager.getOpenRouterApiKey()
                ?: return@withContext Result.failure(
                    IllegalStateException("OpenRouter API key not configured. Please add openrouter.api.key to local.properties")
                )
            
            Log.d(TAG, "Sending chat completion request to OpenRouter (model: $model)")
            
            // Build request JSON
            val requestBody = JSONObject().apply {
                put("model", model)
                put("messages", JSONArray().apply {
                    messages.forEach { message ->
                        put(JSONObject().apply {
                            put("role", message.role)
                            put("content", message.content)
                        })
                    }
                })
                put("temperature", temperature)
                put("max_tokens", maxTokens)
            }
            
            // Make HTTP request
            val url = URL(CHAT_COMPLETIONS_ENDPOINT)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                doOutput = true
                doInput = true
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("HTTP-Referer", "https://nextgenbuildpro.com") // Optional: for OpenRouter analytics
                setRequestProperty("X-Title", "NextGen BuildPro") // Optional: for OpenRouter analytics
            }
            
            // Send request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }
            
            // Read response
            val responseCode = connection.responseCode
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                
                val responseJson = JSONObject(response)
                val response = parseResponse(responseJson)
                
                Log.d(TAG, "OpenRouter request successful. Tokens used: ${response.usage.totalTokens}")
                Result.success(response)
            } else {
                val errorResponse = BufferedReader(InputStreamReader(connection.errorStream)).use { reader ->
                    reader.readText()
                }
                Log.e(TAG, "OpenRouter API error (${responseCode}): $errorResponse")
                Result.failure(Exception("OpenRouter API error: $responseCode - $errorResponse"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling OpenRouter API", e)
            Result.failure(e)
        }
    }
    
    /**
     * List available models from OpenRouter
     * 
     * @return List of available model identifiers
     */
    suspend fun listModels(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = ApiKeyManager.getOpenRouterApiKey()
                ?: return@withContext Result.failure(
                    IllegalStateException("OpenRouter API key not configured")
                )
            
            val url = URL("$BASE_URL/models")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("Authorization", "Bearer $apiKey")
            }
            
            val responseCode = connection.responseCode
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                
                val responseJson = JSONObject(response)
                val modelsArray = responseJson.getJSONArray("data")
                val models = mutableListOf<String>()
                
                for (i in 0 until modelsArray.length()) {
                    val model = modelsArray.getJSONObject(i)
                    models.add(model.getString("id"))
                }
                
                Log.d(TAG, "Retrieved ${models.size} models from OpenRouter")
                Result.success(models)
            } else {
                Result.failure(Exception("Failed to list models: $responseCode"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing models", e)
            Result.failure(e)
        }
    }
    
    private fun parseResponse(json: JSONObject): OpenRouterResponse {
        val choices = json.getJSONArray("choices")
        val firstChoice = choices.getJSONObject(0)
        val message = firstChoice.getJSONObject("message")
        
        val usage = json.getJSONObject("usage")
        
        return OpenRouterResponse(
            id = json.getString("id"),
            model = json.getString("model"),
            content = message.getString("content"),
            role = message.getString("role"),
            finishReason = firstChoice.optString("finish_reason", "stop"),
            usage = TokenUsage(
                promptTokens = usage.getInt("prompt_tokens"),
                completionTokens = usage.getInt("completion_tokens"),
                totalTokens = usage.getInt("total_tokens")
            )
        )
    }
}

/**
 * Chat message for OpenRouter API
 */
data class ChatMessage(
    val role: String, // "system", "user", or "assistant"
    val content: String
)

/**
 * Response from OpenRouter API
 */
data class OpenRouterResponse(
    val id: String,
    val model: String,
    val content: String,
    val role: String,
    val finishReason: String,
    val usage: TokenUsage
)
