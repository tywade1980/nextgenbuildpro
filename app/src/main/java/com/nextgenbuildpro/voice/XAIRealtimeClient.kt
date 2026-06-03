package com.nextgenbuildpro.voice

import android.util.Base64
import android.util.Log
import com.nextgenbuildpro.BuildConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed class XAIEvent {
    data class SessionCreated(val sessionId: String) : XAIEvent()
    data class AudioDelta(val audioBase64: String) : XAIEvent()
    data class TextDelta(val text: String) : XAIEvent()
    data class ResponseDone(val transcript: String) : XAIEvent()
    data class Error(val message: String) : XAIEvent()
    object Disconnected : XAIEvent()
}

/**
 * Converted from voice-ai-app/App.tsx WebSocket logic.
 * Connects to wss://api.x.ai/v1/realtime and streams audio/text events
 * as a SharedFlow so the ViewModel can observe them.
 */
@Singleton
class XAIRealtimeClient @Inject constructor() {

    private val TAG = "XAIRealtimeClient"
    private val WS_URL = "wss://api.x.ai/v1/realtime?model=grok-voice-think-fast-1.0"

    private val _events = MutableSharedFlow<XAIEvent>(replay = 0, extraBufferCapacity = 64)
    val events: SharedFlow<XAIEvent> = _events

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // no timeout for streaming
        .build()

    private var webSocket: WebSocket? = null
    private val transcriptBuilder = StringBuilder()

    fun connect() {
        if (webSocket != null) return
        val request = Request.Builder()
            .url(WS_URL)
            .header("Authorization", "Bearer ${BuildConfig.XAI_API_KEY}")
            .header("OpenAI-Beta", "realtime=v1")
            .build()
        webSocket = client.newWebSocket(request, listener)
        Log.d(TAG, "Connecting to xAI Realtime…")
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }

    /** Send raw PCM16 audio bytes, base64-encoded, mirroring App.tsx sendAudioChunk(). */
    fun sendAudioChunk(pcm16Bytes: ByteArray) {
        val b64 = Base64.encodeToString(pcm16Bytes, Base64.NO_WRAP)
        val msg = JSONObject().apply {
            put("type", "input_audio_buffer.append")
            put("audio", b64)
        }
        webSocket?.send(msg.toString())
    }

    /** Signal end of user speech turn (mirrors commitAudio() in App.tsx). */
    fun commitAudio() {
        val msg = JSONObject().apply { put("type", "input_audio_buffer.commit") }
        webSocket?.send(msg.toString())
        val createResponse = JSONObject().apply { put("type", "response.create") }
        webSocket?.send(createResponse.toString())
    }

    /** Update session with system prompt / voice config. */
    fun updateSession(systemPrompt: String, voice: String = "alloy") {
        val msg = JSONObject().apply {
            put("type", "session.update")
            put("session", JSONObject().apply {
                put("instructions", systemPrompt)
                put("voice", voice)
                put("modalities", org.json.JSONArray().apply {
                    put("audio")
                    put("text")
                })
                put("input_audio_format", "pcm16")
                put("output_audio_format", "pcm16")
                put("turn_detection", JSONObject().apply {
                    put("type", "server_vad")
                    put("threshold", 0.5)
                    put("silence_duration_ms", 800)
                })
            })
        }
        webSocket?.send(msg.toString())
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(ws: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket connected")
        }

        override fun onMessage(ws: WebSocket, text: String) {
            try {
                val json = JSONObject(text)
                when (json.optString("type")) {
                    "session.created" -> {
                        val sid = json.optJSONObject("session")?.optString("id") ?: ""
                        _events.tryEmit(XAIEvent.SessionCreated(sid))
                    }
                    "response.audio.delta" -> {
                        val audio = json.optString("delta", "")
                        if (audio.isNotBlank()) _events.tryEmit(XAIEvent.AudioDelta(audio))
                    }
                    "response.text.delta" -> {
                        val delta = json.optString("delta", "")
                        transcriptBuilder.append(delta)
                        _events.tryEmit(XAIEvent.TextDelta(delta))
                    }
                    "response.done" -> {
                        _events.tryEmit(XAIEvent.ResponseDone(transcriptBuilder.toString()))
                        transcriptBuilder.clear()
                    }
                    "error" -> {
                        val msg = json.optJSONObject("error")?.optString("message") ?: "Unknown error"
                        _events.tryEmit(XAIEvent.Error(msg))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "WS message parse error", e)
            }
        }

        override fun onClosing(ws: WebSocket, code: Int, reason: String) {
            _events.tryEmit(XAIEvent.Disconnected)
            webSocket = null
        }

        override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure", t)
            _events.tryEmit(XAIEvent.Error(t.message ?: "WebSocket failure"))
            webSocket = null
        }
    }
}
