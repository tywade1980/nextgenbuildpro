package com.aireceptionist.app.ai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.aireceptionist.app.ai.agents.impl.TTSResult
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Text-to-Speech manager for voice synthesis
 */
@Singleton
class TextToSpeechManager @Inject constructor(
    private val context: Context
) {
    
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private val audioDir: File by lazy {
        File(context.filesDir, "tts_audio").apply {
            if (!exists()) mkdirs()
        }
    }
    
    suspend fun initialize(): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            Logger.i(TAG, "Initializing TextToSpeechManager")
            
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    setupTTS()
                    isInitialized = true
                    Logger.i(TAG, "TextToSpeechManager initialized successfully")
                    continuation.resume(true)
                } else {
                    Logger.e(TAG, "TextToSpeech initialization failed with status: $status")
                    continuation.resume(false)
                }
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize TextToSpeechManager", e)
            continuation.resume(false)
        }
    }
    
    private fun setupTTS() {
        textToSpeech?.let { tts ->
            // Set default language
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Logger.w(TAG, "Language not supported, using default")
            }
            
            // Set speech parameters
            tts.setSpeechRate(1.0f)
            tts.setPitch(1.0f)
            
            Logger.d(TAG, "TTS setup completed")
        }
    }
    
    suspend fun textToSpeech(
        text: String,
        language: String = "en",
        voiceStyle: String = "professional"
    ): TTSResult = suspendCancellableCoroutine { continuation ->
        
        if (!isInitialized || textToSpeech == null) {
            continuation.resume(TTSResult(false, error = "TTS not initialized"))
            return@suspendCancellableCoroutine
        }
        
        try {
            Logger.d(TAG, "Converting text to speech: $text")
            
            // Set language
            val locale = getLocaleFromLanguage(language)
            textToSpeech?.setLanguage(locale)
            
            // Apply voice style
            applyVoiceStyle(voiceStyle)
            
            // Generate unique filename
            val utteranceId = "tts_${System.currentTimeMillis()}"
            val audioFile = File(audioDir, "$utteranceId.wav")
            
            // Set up utterance progress listener
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Logger.d(TAG, "TTS synthesis started")
                }
                
                override fun onDone(utteranceId: String?) {
                    Logger.d(TAG, "TTS synthesis completed")
                    val duration = calculateAudioDuration(text)
                    
                    if (continuation.isActive) {
                        continuation.resume(
                            TTSResult(
                                isSuccess = true,
                                audioFile = audioFile.absolutePath,
                                duration = duration
                            )
                        )
                    }
                }
                
                override fun onError(utteranceId: String?) {
                    Logger.e(TAG, "TTS synthesis error")
                    if (continuation.isActive) {
                        continuation.resume(TTSResult(false, error = "TTS synthesis failed"))
                    }
                }
                
                override fun onError(utteranceId: String?, errorCode: Int) {
                    Logger.e(TAG, "TTS synthesis error with code: $errorCode")
                    if (continuation.isActive) {
                        continuation.resume(TTSResult(false, error = "TTS synthesis failed with code: $errorCode"))
                    }
                }
            })
            
            // Synthesize to file
            val params = HashMap<String, String>().apply {
                put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            
            val result = textToSpeech?.synthesizeToFile(text, params, audioFile.absolutePath)
            
            if (result != TextToSpeech.SUCCESS) {
                Logger.e(TAG, "Failed to start TTS synthesis")
                continuation.resume(TTSResult(false, error = "Failed to start synthesis"))
            }
            
            continuation.invokeOnCancellation {
                textToSpeech?.stop()
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error in text-to-speech conversion", e)
            if (continuation.isActive) {
                continuation.resume(TTSResult(false, error = e.message))
            }
        }
    }
    
    private fun getLocaleFromLanguage(language: String): Locale {
        return when (language.toLowerCase()) {
            "en" -> Locale.US
            "es" -> Locale("es", "ES")
            "fr" -> Locale.FRANCE
            "de" -> Locale.GERMAN
            "it" -> Locale.ITALIAN
            "pt" -> Locale("pt", "BR")
            "zh" -> Locale.CHINESE
            "ja" -> Locale.JAPANESE
            "ko" -> Locale.KOREAN
            else -> Locale.US
        }
    }
    
    private fun applyVoiceStyle(style: String) {
        textToSpeech?.let { tts ->
            when (style) {
                "professional" -> {
                    tts.setSpeechRate(0.9f)
                    tts.setPitch(1.0f)
                }
                "friendly" -> {
                    tts.setSpeechRate(1.0f)
                    tts.setPitch(1.1f)
                }
                "empathetic" -> {
                    tts.setSpeechRate(0.8f)
                    tts.setPitch(0.9f)
                }
                "urgent" -> {
                    tts.setSpeechRate(1.2f)
                    tts.setPitch(1.0f)
                }
                "professional_warm" -> {
                    tts.setSpeechRate(0.95f)
                    tts.setPitch(1.05f)
                }
                else -> {
                    tts.setSpeechRate(1.0f)
                    tts.setPitch(1.0f)
                }
            }
            
            Logger.d(TAG, "Applied voice style: $style")
        }
    }
    
    private fun calculateAudioDuration(text: String): Long {
        // Rough estimation: average speaking rate is about 150 words per minute
        val words = text.split("\\s+".toRegex()).size
        val wordsPerMinute = 150
        val durationMinutes = words.toDouble() / wordsPerMinute
        return (durationMinutes * 60 * 1000).toLong() // Convert to milliseconds
    }
    
    suspend fun speak(text: String, utteranceId: String? = null): Boolean = suspendCancellableCoroutine { continuation ->
        if (!isInitialized || textToSpeech == null) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        
        try {
            val id = utteranceId ?: "speak_${System.currentTimeMillis()}"
            
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Logger.d(TAG, "TTS speaking started")
                }
                
                override fun onDone(utteranceId: String?) {
                    Logger.d(TAG, "TTS speaking completed")
                    if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }
                
                override fun onError(utteranceId: String?) {
                    Logger.e(TAG, "TTS speaking error")
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }
            })
            
            val params = HashMap<String, String>().apply {
                put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id)
            }
            
            val result = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params)
            
            if (result != TextToSpeech.SUCCESS) {
                continuation.resume(false)
            }
            
            continuation.invokeOnCancellation {
                textToSpeech?.stop()
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error in TTS speak", e)
            continuation.resume(false)
        }
    }
    
    fun stop() {
        textToSpeech?.stop()
        Logger.d(TAG, "TTS stopped")
    }
    
    fun isHealthy(): Boolean {
        return isInitialized && textToSpeech != null
    }
    
    suspend fun shutdown() {
        try {
            Logger.i(TAG, "Shutting down TextToSpeechManager")
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
            isInitialized = false
            Logger.i(TAG, "TextToSpeechManager shutdown complete")
        } catch (e: Exception) {
            Logger.e(TAG, "Error during TextToSpeechManager shutdown", e)
        }
    }
    
    companion object {
        private const val TAG = "TextToSpeechManager"
    }
}