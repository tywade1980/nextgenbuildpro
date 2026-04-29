package com.aireceptionist.app.ai.agents.impl

import com.aireceptionist.app.ai.agents.*
import com.aireceptionist.app.ai.voice.TextToSpeechManager
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Agent responsible for converting text to speech output
 */
class VoiceSynthesisAgent @Inject constructor(
    private val ttsManager: TextToSpeechManager
) : Agent {
    
    override val agentId = "voice_synthesis"
    override val agentName = "Voice Synthesis Agent"
    override val capabilities = listOf(
        AgentCapability.VOICE_SYNTHESIS,
        AgentCapability.MULTI_LANGUAGE
    )
    override val priority = 5
    
    private var isInitialized = false
    
    override suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.i(TAG, "Initializing Voice Synthesis Agent")
            
            ttsManager.initialize()
            
            isInitialized = true
            Logger.i(TAG, "Voice Synthesis Agent initialized successfully")
            true
            
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize Voice Synthesis Agent", e)
            false
        }
    }
    
    override suspend fun processInput(input: AgentInput): AgentResponse = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            return@withContext createErrorResponse("Agent not initialized")
        }
        
        when (input.type) {
            InputType.TEXT_MESSAGE, InputType.SYSTEM_EVENT -> synthesizeSpeech(input)
            else -> createErrorResponse("Unsupported input type: ${input.type}")
        }
    }
    
    private suspend fun synthesizeSpeech(input: AgentInput): AgentResponse {
        return try {
            Logger.d(TAG, "Synthesizing speech for: ${input.content}")
            
            val language = input.metadata["detected_language"] as? String ?: "en"
            val voiceStyle = determineVoiceStyle(input.context)
            
            // Convert text to speech
            val audioResult = ttsManager.textToSpeech(
                text = input.content,
                language = language,
                voiceStyle = voiceStyle
            )
            
            if (audioResult.isSuccess) {
                Logger.d(TAG, "Speech synthesis successful")
                
                AgentResponse(
                    agentId = agentId,
                    responseType = ResponseType.SPEECH_OUTPUT,
                    content = "Speech synthesis completed",
                    confidence = 0.9f,
                    actions = listOf(
                        AgentAction(
                            actionType = ActionType.PLAY_AUDIO,
                            parameters = mapOf(
                                "audio_file" to audioResult.audioFile,
                                "duration" to audioResult.duration
                            )
                        )
                    ),
                    metadata = mapOf(
                        "audio_file" to audioResult.audioFile,
                        "language" to language,
                        "voice_style" to voiceStyle,
                        "duration" to audioResult.duration
                    )
                )
            } else {
                Logger.e(TAG, "Speech synthesis failed: ${audioResult.error}")
                createErrorResponse("Speech synthesis failed")
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error synthesizing speech", e)
            createErrorResponse("Speech synthesis error: ${e.message}")
        }
    }
    
    private fun determineVoiceStyle(context: com.aireceptionist.app.data.models.CallContext): String {
        return when {
            context.intent == "emergency" -> "urgent"
            context.sentiment == "negative" -> "empathetic"
            context.sentiment == "positive" -> "friendly"
            context.isVipCaller == true -> "professional_warm"
            else -> "professional"
        }
    }
    
    override suspend fun shutdown() {
        try {
            Logger.i(TAG, "Shutting down Voice Synthesis Agent")
            ttsManager.shutdown()
            isInitialized = false
            Logger.i(TAG, "Voice Synthesis Agent shutdown complete")
        } catch (e: Exception) {
            Logger.e(TAG, "Error during shutdown", e)
        }
    }
    
    override fun isHealthy(): Boolean {
        return isInitialized && ttsManager.isHealthy()
    }
    
    private fun createErrorResponse(message: String): AgentResponse {
        return AgentResponse(
            agentId = agentId,
            responseType = ResponseType.TEXT_RESPONSE,
            content = message,
            confidence = 0.0f
        )
    }
    
    companion object {
        private const val TAG = "VoiceSynthesisAgent"
    }
}

/**
 * Data class for TTS results
 */
data class TTSResult(
    val isSuccess: Boolean,
    val audioFile: String? = null,
    val duration: Long = 0L,
    val error: String? = null
)