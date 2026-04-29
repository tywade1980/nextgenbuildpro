package com.aireceptionist.app.ai.agents.impl

import com.aireceptionist.app.ai.agents.*
import com.aireceptionist.app.ai.voice.VoiceProcessor
import com.aireceptionist.app.utils.Logger
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Agent responsible for converting speech to text and language detection
 */
class SpeechRecognitionAgent @Inject constructor(
    private val voiceProcessor: VoiceProcessor
) : Agent {
    
    override val agentId = "speech_recognition"
    override val agentName = "Speech Recognition Agent"
    override val capabilities = listOf(
        AgentCapability.SPEECH_RECOGNITION,
        AgentCapability.MULTI_LANGUAGE
    )
    override val priority = 10
    
    private lateinit var languageIdentifier: LanguageIdentifier
    private var isInitialized = false
    
    override suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.i(TAG, "Initializing Speech Recognition Agent")
            
            // Initialize ML Kit Language Identifier
            languageIdentifier = LanguageIdentification.getClient()
            
            // Initialize voice processor
            voiceProcessor.initialize()
            
            isInitialized = true
            Logger.i(TAG, "Speech Recognition Agent initialized successfully")
            true
            
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize Speech Recognition Agent", e)
            false
        }
    }
    
    override suspend fun processInput(input: AgentInput): AgentResponse = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            return@withContext createErrorResponse("Agent not initialized")
        }
        
        when (input.type) {
            InputType.AUDIO_SPEECH -> processAudioInput(input)
            else -> createErrorResponse("Unsupported input type: ${input.type}")
        }
    }
    
    private suspend fun processAudioInput(input: AgentInput): AgentResponse {
        return try {
            Logger.d(TAG, "Processing audio input")
            
            // Convert speech to text
            val recognizedText = voiceProcessor.speechToText(input.content)
            
            if (recognizedText.isNotBlank()) {
                // Detect language
                val detectedLanguage = detectLanguage(recognizedText)
                
                Logger.d(TAG, "Recognized text: $recognizedText (Language: $detectedLanguage)")
                
                AgentResponse(
                    agentId = agentId,
                    responseType = ResponseType.TEXT_RESPONSE,
                    content = recognizedText,
                    confidence = 0.9f,
                    nextSuggestedAgent = "natural_language", // Chain to NLU agent
                    metadata = mapOf(
                        "detected_language" to detectedLanguage,
                        "audio_duration" to (input.metadata["duration"] ?: 0),
                        "confidence_score" to 0.9f
                    )
                )
            } else {
                Logger.w(TAG, "No speech recognized")
                createErrorResponse("No speech detected")
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error processing audio input", e)
            createErrorResponse("Speech recognition failed: ${e.message}")
        }
    }
    
    private suspend fun detectLanguage(text: String): String = withContext(Dispatchers.IO) {
        try {
            var detectedLanguage = "en" // Default to English
            
            languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener { languageCode ->
                    if (languageCode != "und") { // "und" means undetermined
                        detectedLanguage = languageCode
                        Logger.d(TAG, "Detected language: $languageCode")
                    }
                }
                .addOnFailureListener { e ->
                    Logger.w(TAG, "Language detection failed", e)
                }
            
            detectedLanguage
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error in language detection", e)
            "en" // Default to English on error
        }
    }
    
    override suspend fun shutdown() {
        try {
            Logger.i(TAG, "Shutting down Speech Recognition Agent")
            
            if (::languageIdentifier.isInitialized) {
                languageIdentifier.close()
            }
            
            voiceProcessor.shutdown()
            isInitialized = false
            
            Logger.i(TAG, "Speech Recognition Agent shutdown complete")
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error during shutdown", e)
        }
    }
    
    override fun isHealthy(): Boolean {
        return isInitialized && voiceProcessor.isHealthy()
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
        private const val TAG = "SpeechRecognitionAgent"
    }
}