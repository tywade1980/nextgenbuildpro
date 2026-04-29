package com.caroline.ai.llm

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Multi-provider LLM service for Caroline AI.
 *
 * Supports:
 *  - xAI Grok (primary — conversational + reasoning)
 *  - OpenRouter (fallback — routes to best available model)
 *  - OpenAI GPT-4 (executive workflow tasks)
 *  - Anthropic Claude (long-context document analysis)
 *
 * Consolidated from: caroline-alpha, caroline-android, caroline-server-v2, caroline-app-v2
 */
class CarolineLLMService(private val context: Context) {

    companion object {
        private const val TAG = "CarolineLLMService"

        // Provider base URLs
        private const val XAI_BASE = "https://api.x.ai/v1"
        private const val OPENROUTER_BASE = "https://openrouter.ai/api/v1"
        private const val OPENAI_BASE = "https://api.openai.com/v1"
        private const val ANTHROPIC_BASE = "https://api.anthropic.com/v1"

        // Default models per use-case
        val MODEL_CONVERSATION = LLMModel("grok-2-1212", Provider.XAI)
        val MODEL_REASONING = LLMModel("grok-2-1212", Provider.XAI)
        val MODEL_DOCUMENT = LLMModel("claude-sonnet-4-6", Provider.ANTHROPIC)
        val MODEL_WORKFLOW = LLMModel("gpt-4-turbo", Provider.OPENAI)
        val MODEL_FALLBACK = LLMModel("anthropic/claude-sonnet-4-6", Provider.OPENROUTER)
    }

    enum class Provider { XAI, OPENAI, ANTHROPIC, OPENROUTER }

    data class LLMModel(val modelId: String, val provider: Provider)

    data class Message(val role: String, val content: String)

    data class CompletionRequest(
        val messages: List<Message>,
        val model: LLMModel = MODEL_CONVERSATION,
        val maxTokens: Int = 2048,
        val temperature: Double = 0.7,
        val systemPrompt: String = CAROLINE_SYSTEM_PROMPT
    )

    private val apiKeys = ApiKeyStore(context)

    // === PUBLIC API ===

    suspend fun complete(request: CompletionRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = when (request.model.provider) {
                Provider.XAI -> callXAI(request)
                Provider.OPENAI -> callOpenAI(request)
                Provider.ANTHROPIC -> callAnthropic(request)
                Provider.OPENROUTER -> callOpenRouter(request)
            }
            Result.success(response)
        } catch (e: Exception) {
            Log.w(TAG, "Primary LLM failed, trying fallback", e)
            try {
                val fallback = request.copy(model = MODEL_FALLBACK)
                Result.success(callOpenRouter(fallback))
            } catch (fe: Exception) {
                Log.e(TAG, "Fallback LLM also failed", fe)
                Result.failure(fe)
            }
        }
    }

    suspend fun chat(userMessage: String, history: List<Message> = emptyList()): Result<String> {
        return complete(
            CompletionRequest(
                messages = history + Message("user", userMessage),
                model = MODEL_CONVERSATION
            )
        )
    }

    suspend fun reason(prompt: String): Result<String> {
        return complete(
            CompletionRequest(
                messages = listOf(Message("user", prompt)),
                model = MODEL_REASONING,
                temperature = 0.3
            )
        )
    }

    suspend fun analyzeDocument(content: String, instruction: String): Result<String> {
        return complete(
            CompletionRequest(
                messages = listOf(
                    Message("user", "Document:\n$content\n\nInstruction: $instruction")
                ),
                model = MODEL_DOCUMENT,
                maxTokens = 4096
            )
        )
    }

    // === PROVIDER CALLS ===

    private fun callXAI(req: CompletionRequest): String {
        val key = apiKeys.get("XAI_API_KEY") ?: throw IllegalStateException("xAI API key not configured")
        return callOpenAICompatible("$XAI_BASE/chat/completions", key, req)
    }

    private fun callOpenAI(req: CompletionRequest): String {
        val key = apiKeys.get("OPENAI_API_KEY") ?: throw IllegalStateException("OpenAI API key not configured")
        return callOpenAICompatible("$OPENAI_BASE/chat/completions", key, req)
    }

    private fun callOpenRouter(req: CompletionRequest): String {
        val key = apiKeys.get("OPENROUTER_API_KEY") ?: throw IllegalStateException("OpenRouter key not configured")
        return callOpenAICompatible("$OPENROUTER_BASE/chat/completions", key, req)
    }

    private fun callAnthropic(req: CompletionRequest): String {
        val key = apiKeys.get("ANTHROPIC_API_KEY") ?: throw IllegalStateException("Anthropic key not configured")
        val url = URL("$ANTHROPIC_BASE/messages")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("x-api-key", key)
        conn.setRequestProperty("anthropic-version", "2023-06-01")
        conn.doOutput = true

        val body = JSONObject().apply {
            put("model", req.model.modelId)
            put("max_tokens", req.maxTokens)
            put("system", req.systemPrompt)
            put("messages", JSONArray().apply {
                req.messages.forEach { msg ->
                    put(JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                    })
                }
            })
        }

        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
        val response = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(response)
        return json.getJSONArray("content").getJSONObject(0).getString("text")
    }

    private fun callOpenAICompatible(endpoint: String, key: String, req: CompletionRequest): String {
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $key")
        conn.doOutput = true

        val messages = JSONArray()
        messages.put(JSONObject().put("role", "system").put("content", req.systemPrompt))
        req.messages.forEach { msg ->
            messages.put(JSONObject().put("role", msg.role).put("content", msg.content))
        }

        val body = JSONObject().apply {
            put("model", req.model.modelId)
            put("messages", messages)
            put("max_tokens", req.maxTokens)
            put("temperature", req.temperature)
        }

        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
        val response = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(response)
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }
}

private const val CAROLINE_SYSTEM_PROMPT = """
You are Caroline, an intelligent executive assistant for NextGen BuildPro construction professionals.

Your capabilities:
- Managing calls, calendar, contacts, and email
- Construction project management support
- Voice commands and hands-free operation
- Smart scheduling and follow-ups
- Real-time document analysis
- Bilingual English/Spanish support

Be concise, professional, and proactive. Prioritize safety-critical information.
Construction domain knowledge: estimates, project management, subcontractors, permits, inspections.
"""
