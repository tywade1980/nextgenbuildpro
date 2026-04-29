package com.nextgenbuildpro.caroline

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
 * Converted from manus-master-archive/caroline-ai/caroline-server-v2/main.py.
 * Provides Android-native equivalents of the Python FastAPI server endpoints.
 * The /ws/voice proxy is handled directly by XAIRealtimeClient on Android.
 */
@Singleton
class CarolineServerClient @Inject constructor() {

    private val TAG = "CarolineServerClient"
    private val JSON_TYPE = "application/json; charset=utf-8".toMediaType()

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Mint a short-lived xAI Realtime client secret (mirrors POST /token). */
    suspend fun mintToken(): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.XAI_API_KEY
        if (apiKey.isBlank()) { Log.w(TAG, "XAI_API_KEY not set"); return@withContext null }
        val body = JSONObject().apply { put("expires_after", JSONObject().apply { put("seconds", 300) }) }
        val req = Request.Builder()
            .url("https://api.x.ai/v1/realtime/client_secrets")
            .post(body.toString().toRequestBody(JSON_TYPE))
            .header("Authorization", "Bearer $apiKey")
            .build()
        runCatching {
            val resp = http.newCall(req).execute()
            val text = resp.body?.string() ?: return@withContext null
            if (!resp.isSuccessful) { Log.e(TAG, "mintToken ${resp.code}: $text"); return@withContext null }
            JSONObject(text).optString("value")
        }.getOrElse { e -> Log.e(TAG, "mintToken error", e); null }
    }

    /** Relay chat completions to OpenRouter (mirrors POST /llm). */
    suspend fun llmRelay(messages: List<Map<String, String>>, model: String = "mistralai/mistral-7b-instruct"): String =
        withContext(Dispatchers.IO) {
            val msgArray = JSONArray().apply {
                messages.forEach { m -> put(JSONObject().apply { put("role", m["role"]); put("content", m["content"]) }) }
            }
            val body = JSONObject().apply { put("model", model); put("messages", msgArray) }
            val req = Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .post(body.toString().toRequestBody(JSON_TYPE))
                .header("Authorization", "Bearer ${BuildConfig.OPENROUTER_API_KEY}")
                .header("HTTP-Referer", "https://caroline-ai.app")
                .build()
            val resp = http.newCall(req).execute()
            val text = resp.body?.string() ?: throw IllegalStateException("Empty OpenRouter response")
            JSONObject(text).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
        }

    /** Convert text to speech via ElevenLabs (mirrors POST /tts). Returns MP3 bytes. */
    suspend fun ttsSpeech(text: String, voiceId: String = "wvVfSeWpAEhEqciDp1gK"): ByteArray =
        withContext(Dispatchers.IO) {
            val body = JSONObject().apply { put("text", text); put("model_id", "eleven_turbo_v2_5") }
            val req = Request.Builder()
                .url("https://api.elevenlabs.io/v1/text-to-speech/$voiceId")
                .post(body.toString().toRequestBody(JSON_TYPE))
                .header("xi-api-key", BuildConfig.ELEVENLABS_API_KEY)
                .build()
            val resp = http.newCall(req).execute()
            if (!resp.isSuccessful) throw IllegalStateException("ElevenLabs error ${resp.code}")
            resp.body?.bytes() ?: ByteArray(0)
        }
}
