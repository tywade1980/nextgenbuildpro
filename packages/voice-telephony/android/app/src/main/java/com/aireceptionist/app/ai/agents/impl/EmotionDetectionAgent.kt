package com.aireceptionist.app.ai.agents.impl

import com.aireceptionist.app.ai.agents.*
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Agent responsible for detecting emotions in speech and text
 */
class EmotionDetectionAgent @Inject constructor() : Agent {
    
    override val agentId = "emotion_detection"
    override val agentName = "Emotion Detection Agent"
    override val capabilities = listOf(
        AgentCapability.EMOTION_DETECTION,
        AgentCapability.CONTEXT_AWARENESS
    )
    override val priority = 6
    
    private var isInitialized = false
    
    // Emotion detection patterns and weights
    private val emotionKeywords = mapOf(
        "anger" to listOf("angry", "mad", "furious", "upset", "irritated", "annoyed", "frustrated"),
        "sadness" to listOf("sad", "disappointed", "unhappy", "depressed", "down", "miserable"),
        "joy" to listOf("happy", "glad", "excited", "thrilled", "delighted", "pleased", "satisfied"),
        "fear" to listOf("scared", "afraid", "worried", "anxious", "nervous", "concerned"),
        "surprise" to listOf("surprised", "shocked", "amazed", "astonished", "unexpected"),
        "disgust" to listOf("disgusted", "appalled", "revolted", "sickened"),
        "neutral" to listOf("okay", "fine", "alright", "normal", "regular")
    )
    
    private val intensityModifiers = mapOf(
        "very" to 1.5f,
        "extremely" to 2.0f,
        "really" to 1.3f,
        "quite" to 1.2f,
        "somewhat" to 0.8f,
        "a bit" to 0.7f,
        "slightly" to 0.6f
    )
    
    override suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.i(TAG, "Initializing Emotion Detection Agent")
            
            // Initialize emotion detection models (TensorFlow Lite models would go here)
            
            isInitialized = true
            Logger.i(TAG, "Emotion Detection Agent initialized successfully")
            true
            
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize Emotion Detection Agent", e)
            false
        }
    }
    
    override suspend fun processInput(input: AgentInput): AgentResponse = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            return@withContext createErrorResponse("Agent not initialized")
        }
        
        when (input.type) {
            InputType.TEXT_MESSAGE, InputType.AUDIO_SPEECH, InputType.SYSTEM_EVENT -> 
                detectEmotion(input)
            else -> createErrorResponse("Unsupported input type: ${input.type}")
        }
    }
    
    private suspend fun detectEmotion(input: AgentInput): AgentResponse {
        return try {
            Logger.d(TAG, "Detecting emotion in: ${input.content}")
            
            val emotionAnalysis = when (input.type) {
                InputType.AUDIO_SPEECH -> analyzeAudioEmotion(input.content)
                InputType.TEXT_MESSAGE, InputType.SYSTEM_EVENT -> analyzeTextEmotion(input.content)
                else -> EmotionAnalysis("neutral", 0.5f, emptyMap())
            }
            
            Logger.d(TAG, "Detected emotion: ${emotionAnalysis.primaryEmotion} (${emotionAnalysis.confidence})")
            
            // Create response with emotion context
            AgentResponse(
                agentId = agentId,
                responseType = ResponseType.DATA_QUERY,
                content = "Emotion analysis completed",
                confidence = emotionAnalysis.confidence,
                metadata = mapOf(
                    "primary_emotion" to emotionAnalysis.primaryEmotion,
                    "emotion_confidence" to emotionAnalysis.confidence,
                    "emotion_scores" to emotionAnalysis.emotionScores,
                    "emotional_intensity" to calculateIntensity(emotionAnalysis),
                    "recommended_response_style" to recommendResponseStyle(emotionAnalysis)
                )
            )
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error detecting emotion", e)
            createErrorResponse("Emotion detection failed: ${e.message}")
        }
    }
    
    private fun analyzeTextEmotion(text: String): EmotionAnalysis {
        val words = text.toLowerCase().split("\\s+".toRegex())
        val emotionScores = mutableMapOf<String, Float>()
        
        // Initialize emotion scores
        emotionKeywords.keys.forEach { emotion ->
            emotionScores[emotion] = 0.0f
        }
        
        // Analyze each word
        var totalMatches = 0
        for (word in words) {
            for ((emotion, keywords) in emotionKeywords) {
                if (keywords.contains(word)) {
                    val intensity = getIntensityMultiplier(words, words.indexOf(word))
                    emotionScores[emotion] = emotionScores[emotion]!! + (1.0f * intensity)
                    totalMatches++
                }
            }
        }
        
        // Normalize scores
        if (totalMatches > 0) {
            emotionScores.keys.forEach { emotion ->
                emotionScores[emotion] = emotionScores[emotion]!! / totalMatches
            }
        }
        
        // Find primary emotion
        val primaryEmotion = emotionScores.maxByOrNull { it.value }?.key ?: "neutral"
        val confidence = emotionScores[primaryEmotion] ?: 0.5f
        
        return EmotionAnalysis(
            primaryEmotion = primaryEmotion,
            confidence = minOf(confidence, 1.0f),
            emotionScores = emotionScores.toMap()
        )
    }
    
    private fun analyzeAudioEmotion(audioData: String): EmotionAnalysis {
        // This would use ML models to analyze audio features like:
        // - Pitch variations
        // - Speaking rate
        // - Voice intensity
        // - Spectral features
        
        // For now, return neutral emotion
        // In a real implementation, this would process audio features
        return EmotionAnalysis(
            primaryEmotion = "neutral",
            confidence = 0.5f,
            emotionScores = mapOf("neutral" to 0.5f)
        )
    }
    
    private fun getIntensityMultiplier(words: List<String>, wordIndex: Int): Float {
        // Check for intensity modifiers before the emotional word
        val contextRange = maxOf(0, wordIndex - 2)..minOf(words.size - 1, wordIndex + 2)
        
        for (i in contextRange) {
            val word = words[i]
            intensityModifiers[word]?.let { multiplier ->
                return multiplier
            }
        }
        
        return 1.0f
    }
    
    private fun calculateIntensity(emotionAnalysis: EmotionAnalysis): String {
        return when {
            emotionAnalysis.confidence >= 0.8f -> "high"
            emotionAnalysis.confidence >= 0.6f -> "medium"
            else -> "low"
        }
    }
    
    private fun recommendResponseStyle(emotionAnalysis: EmotionAnalysis): String {
        return when (emotionAnalysis.primaryEmotion) {
            "anger" -> "calm_empathetic"
            "sadness" -> "supportive_understanding"
            "fear", "anxiety" -> "reassuring_confident"
            "joy" -> "enthusiastic_positive"
            "surprise" -> "informative_clarifying"
            "disgust" -> "professional_apologetic"
            else -> "neutral_professional"
        }
    }
    
    override suspend fun shutdown() {
        try {
            Logger.i(TAG, "Shutting down Emotion Detection Agent")
            isInitialized = false
            Logger.i(TAG, "Emotion Detection Agent shutdown complete")
        } catch (e: Exception) {
            Logger.e(TAG, "Error during shutdown", e)
        }
    }
    
    override fun isHealthy(): Boolean {
        return isInitialized
    }
    
    private fun createErrorResponse(message: String): AgentResponse {
        return AgentResponse(
            agentId = agentId,
            responseType = ResponseType.DATA_QUERY,
            content = message,
            confidence = 0.0f
        )
    }
    
    companion object {
        private const val TAG = "EmotionDetectionAgent"
    }
}

/**
 * Data class for emotion analysis results
 */
data class EmotionAnalysis(
    val primaryEmotion: String,
    val confidence: Float,
    val emotionScores: Map<String, Float>
)