package com.nextgenbuildpro.ai.llm

import android.util.Log
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
 * Kilocode API Client
 * 
 * Provides integration with Kilocode's large context window LLM service.
 * Kilocode supports up to 1M+ token context windows, making it ideal for
 * comprehensive code analysis, large-scale refactoring, and system-wide reviews.
 * 
 * Key Features:
 * - 1M+ token context window (vs 128K for GPT-4)
 * - Optimized for code understanding and refactoring
 * - Suitable for analyzing entire codebases
 * - Strong reasoning capabilities for complex architectural decisions
 */
class KilocodeClient(
    private val apiKey: String = System.getenv("KILOCODE_API_KEY") ?: "",
    private val apiEndpoint: String = "https://api.kilocode.ai/v1"
) {
    companion object {
        private const val TAG = "KilocodeClient"
        private const val DEFAULT_TIMEOUT = 120_000 // 2 minutes for large context processing
        
        // Kilocode model identifiers
        const val MODEL_REFACTOR = "kilocode-refactor-v1"           // Specialized for code refactoring
        const val MODEL_ANALYSIS = "kilocode-analysis-v1"           // Code analysis and review
        const val MODEL_ARCHITECTURE = "kilocode-architecture-v1"   // System architecture planning
        const val MODEL_GENERAL = "kilocode-1m"                     // General purpose with 1M context
    }
    
    /**
     * Chat completion with Kilocode
     * 
     * @param messages List of chat messages (system, user, assistant)
     * @param model Kilocode model to use (default: general 1M context)
     * @param temperature Sampling temperature (0.0 to 2.0)
     * @param maxTokens Maximum tokens in response
     * @param contextSize Expected context size (optimizes processing)
     * @return Result containing OpenRouterResponse or error
     */
    suspend fun chatCompletion(
        messages: List<ChatMessage>,
        model: String = MODEL_GENERAL,
        temperature: Double = 0.7,
        maxTokens: Int = 4096,
        contextSize: Long? = null
    ): Result<OpenRouterResponse> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isEmpty()) {
                Log.w(TAG, "Kilocode API key not configured, using fallback")
                return@withContext Result.failure(
                    IllegalStateException("Kilocode API key not configured")
                )
            }
            
            Log.d(TAG, "Kilocode request - Model: $model, Messages: ${messages.size}, Context size: ${contextSize ?: "auto"}")
            
            val url = URL("$apiEndpoint/chat/completions")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("X-Context-Size", contextSize?.toString() ?: "auto")
                connectTimeout = DEFAULT_TIMEOUT
                readTimeout = DEFAULT_TIMEOUT
                doOutput = true
            }
            
            // Build request payload
            val payload = JSONObject().apply {
                put("model", model)
                put("messages", JSONArray().apply {
                    messages.forEach { msg ->
                        put(JSONObject().apply {
                            put("role", msg.role)
                            put("content", msg.content)
                        })
                    }
                })
                put("temperature", temperature)
                put("max_tokens", maxTokens)
                
                // Kilocode-specific parameters
                put("stream", false)
                put("enable_refactoring_analysis", model.contains("refactor"))
                put("enable_architecture_insights", model.contains("architecture"))
            }
            
            // Send request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            val responseBody = if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            } else {
                val errorBody = BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
                Log.e(TAG, "Kilocode API error ($responseCode): $errorBody")
                return@withContext Result.failure(
                    Exception("Kilocode API error: $responseCode - $errorBody")
                )
            }
            
            // Parse response
            val jsonResponse = JSONObject(responseBody)
            val choices = jsonResponse.getJSONArray("choices")
            if (choices.length() == 0) {
                return@withContext Result.failure(Exception("No choices in Kilocode response"))
            }
            
            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.getJSONObject("message")
            val content = message.getString("content")
            val finishReason = firstChoice.optString("finish_reason", "stop")
            
            val usage = jsonResponse.getJSONObject("usage")
            val usageData = TokenUsage(
                promptTokens = usage.optInt("prompt_tokens", 0),
                completionTokens = usage.optInt("completion_tokens", 0),
                totalTokens = usage.optInt("total_tokens", 0)
            )
            
            // Return OpenRouter-compatible response format
            val response = OpenRouterResponse(
                id = jsonResponse.optString("id", "kilocode-${System.currentTimeMillis()}"),
                content = content,
                model = jsonResponse.optString("model", model),
                role = "assistant",
                finishReason = finishReason,
                usage = usageData
            )
            
            Log.d(TAG, "Kilocode response received - Tokens: ${usageData.totalTokens}, Finish: $finishReason")
            Result.success(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Kilocode API call failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Analyze entire codebase with Kilocode's large context window
     * 
     * @param codebaseContent Map of file paths to content
     * @param analysisType Type of analysis (refactor, review, architecture)
     * @param focusAreas Specific areas to focus on
     * @return Result containing analysis results
     */
    suspend fun analyzeCodebase(
        codebaseContent: Map<String, String>,
        analysisType: String = "comprehensive",
        focusAreas: List<String> = emptyList()
    ): Result<CodebaseAnalysis> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Analyzing codebase - Files: ${codebaseContent.size}, Type: $analysisType")
            
            // Build consolidated prompt with codebase content
            val codebasePrompt = buildString {
                append("# Codebase Analysis Request\n\n")
                append("Analysis Type: $analysisType\n")
                if (focusAreas.isNotEmpty()) {
                    append("Focus Areas: ${focusAreas.joinToString(", ")}\n")
                }
                append("\n## Codebase Files\n\n")
                
                codebaseContent.forEach { (path, content) ->
                    append("### File: $path\n")
                    append("```\n$content\n```\n\n")
                }
                
                append("\n## Analysis Instructions\n")
                append("Please provide a comprehensive analysis including:\n")
                append("1. Code quality assessment\n")
                append("2. Architecture recommendations\n")
                append("3. Refactoring opportunities\n")
                append("4. Technical debt identification\n")
                append("5. Performance optimization suggestions\n")
                append("6. Security concerns\n")
                append("7. Actionable improvement plan\n")
            }
            
            val messages = listOf(
                ChatMessage("system", "You are Kilocode, an expert AI specialized in comprehensive codebase analysis and refactoring with a 1M token context window. Provide detailed, actionable insights."),
                ChatMessage("user", codebasePrompt)
            )
            
            // Select appropriate model based on analysis type
            val model = when (analysisType) {
                "refactor" -> MODEL_REFACTOR
                "architecture" -> MODEL_ARCHITECTURE
                "review", "analysis" -> MODEL_ANALYSIS
                else -> MODEL_GENERAL
            }
            
            val result = chatCompletion(
                messages = messages,
                model = model,
                temperature = 0.3, // Lower temperature for more consistent analysis
                maxTokens = 8192,
                contextSize = codebasePrompt.length.toLong()
            )
            
            if (result.isFailure) {
                return@withContext Result.failure(result.exceptionOrNull()!!)
            }
            
            val response = result.getOrNull()!!
            val analysis = CodebaseAnalysis(
                analysisType = analysisType,
                fileCount = codebaseContent.size,
                totalLines = codebaseContent.values.sumOf { it.lines().size },
                findings = response.content,
                tokenUsage = response.usage.totalTokens,
                model = response.model
            )
            
            Log.d(TAG, "Codebase analysis complete - Files: ${analysis.fileCount}, Tokens: ${analysis.tokenUsage}")
            Result.success(analysis)
            
        } catch (e: Exception) {
            Log.e(TAG, "Codebase analysis failed", e)
            Result.failure(e)
        }
    }
}

/**
 * Codebase analysis result
 */
data class CodebaseAnalysis(
    val analysisType: String,
    val fileCount: Int,
    val totalLines: Int,
    val findings: String,
    val tokenUsage: Int,
    val model: String,
    val timestamp: Long = System.currentTimeMillis()
)
