package com.nextgenbuildpro.brain

import android.util.Log
import com.nextgenbuildpro.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Converted from unified-brain/unified_brain_api.py.
 * Provides /llm/generate and /speech/synthesize equivalents as
 * suspend functions backed by HTTP calls to the configured vLLM
 * and Fish Speech endpoints (or xAI as fallback).
 */
@Singleton
class UnifiedBrainService @Inject constructor() {

    private val TAG = "UnifiedBrainService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Equivalent of POST /llm/generate in unified_brain_api.py.
     * Sends messages to vLLM (or falls back to xAI) and returns the response text.
     */
    suspend fun generate(
        messages: List<Map<String, String>>,
        persona: String = "caroline",
        maxTokens: Int = 512,
        temperature: Float = 0.7f
    ): String = withContext(Dispatchers.IO) {
        val systemPrompt = SystemPrompts.forPersona(persona)

        val allMessages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            messages.forEach { msg ->
                put(JSONObject().apply {
                    put("role", msg["role"] ?: "user")
                    put("content", msg["content"] ?: "")
                })
            }
        }

        val bodyJson = JSONObject().apply {
            put("model", "grok-voice-think-fast-1.0")
            put("messages", allMessages)
            put("max_tokens", maxTokens)
            put("temperature", temperature)
        }

        // Try vLLM first, fall back to xAI
        val vllmBase = BuildConfig.VLLM_BASE_URL
        return@withContext if (vllmBase.isNotBlank()) {
            try {
                callLlmEndpoint("$vllmBase/v1/chat/completions", bodyJson, apiKey = null)
            } catch (e: Exception) {
                Log.w(TAG, "vLLM call failed, falling back to xAI: ${e.message}")
                callLlmEndpoint(
                    "https://api.x.ai/v1/chat/completions",
                    bodyJson,
                    apiKey = BuildConfig.XAI_API_KEY
                )
            }
        } else {
            callLlmEndpoint(
                "https://api.x.ai/v1/chat/completions",
                bodyJson,
                apiKey = BuildConfig.XAI_API_KEY
            )
        }
    }

    private fun callLlmEndpoint(url: String, body: JSONObject, apiKey: String?): String {
        val requestBuilder = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
            .header("Content-Type", "application/json")
        if (!apiKey.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $apiKey")
        }
        val response = client.newCall(requestBuilder.build()).execute()
        val responseBody = response.body?.string() ?: throw IllegalStateException("Empty response from $url")
        if (!response.isSuccessful) throw IllegalStateException("LLM error ${response.code}: $responseBody")
        val json = JSONObject(responseBody)
        return json
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }

    /**
     * Equivalent of POST /speech/synthesize in unified_brain_api.py.
     * Returns raw PCM/WAV bytes from Fish Speech or another TTS backend.
     */
    suspend fun synthesizeSpeech(
        text: String,
        persona: String = "caroline"
    ): ByteArray = withContext(Dispatchers.IO) {
        val profile = VoiceProfiles.forPersona(persona)
        val fishBase = BuildConfig.FISH_SPEECH_BASE_URL

        if (fishBase.isBlank()) {
            Log.w(TAG, "FISH_SPEECH_BASE_URL not set; returning empty audio")
            return@withContext ByteArray(0)
        }

        val bodyJson = JSONObject().apply {
            put("text", text)
            put("voice_id", profile.voiceId)
            put("speed", profile.speed)
            put("emotion", profile.emotion)
            put("pitch", profile.pitch)
            put("language", profile.language)
            put("format", "wav")
        }

        val request = Request.Builder()
            .url("$fishBase/v1/tts")
            .post(bodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .header("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.e(TAG, "TTS error ${response.code}")
            return@withContext ByteArray(0)
        }
        response.body?.bytes() ?: ByteArray(0)
    }
}
