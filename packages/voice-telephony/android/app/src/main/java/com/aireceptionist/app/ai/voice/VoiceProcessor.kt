package com.aireceptionist.app.ai.voice

import android.content.Context
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import android.content.Intent
import android.os.Bundle
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Voice processing component for speech-to-text conversion
 */
@Singleton
class VoiceProcessor @Inject constructor(
    private val context: Context
) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var isInitialized = false
    
    suspend fun initialize() {
        try {
            Logger.i(TAG, "Initializing VoiceProcessor")
            
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                isInitialized = true
                Logger.i(TAG, "VoiceProcessor initialized successfully")
            } else {
                Logger.e(TAG, "Speech recognition not available on this device")
                throw IllegalStateException("Speech recognition not available")
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize VoiceProcessor", e)
            throw e
        }
    }
    
    suspend fun speechToText(audioData: String): String = suspendCancellableCoroutine { continuation ->
        if (!isInitialized) {
            continuation.resume("")
            return@suspendCancellableCoroutine
        }
        
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }
            
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Logger.d(TAG, "Ready for speech")
                }
                
                override fun onBeginningOfSpeech() {
                    Logger.d(TAG, "Beginning of speech")
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // RMS changed - could be used for volume monitoring
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                    // Audio buffer received
                }
                
                override fun onEndOfSpeech() {
                    Logger.d(TAG, "End of speech")
                }
                
                override fun onError(error: Int) {
                    val errorMessage = getErrorMessage(error)
                    Logger.e(TAG, "Speech recognition error: $errorMessage")
                    if (continuation.isActive) {
                        continuation.resume("")
                    }
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val recognizedText = matches?.firstOrNull() ?: ""
                    
                    Logger.d(TAG, "Speech recognition result: $recognizedText")
                    
                    if (continuation.isActive) {
                        continuation.resume(recognizedText)
                    }
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    // Partial results - could be used for real-time feedback
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Speech recognition event
                }
            })
            
            speechRecognizer?.startListening(intent)
            
            continuation.invokeOnCancellation {
                speechRecognizer?.stopListening()
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error in speech-to-text conversion", e)
            if (continuation.isActive) {
                continuation.resume("")
            }
        }
    }
    
    private fun getErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error: $error"
        }
    }
    
    fun isHealthy(): Boolean {
        return isInitialized && speechRecognizer != null
    }
    
    suspend fun shutdown() {
        try {
            Logger.i(TAG, "Shutting down VoiceProcessor")
            speechRecognizer?.destroy()
            speechRecognizer = null
            isInitialized = false
            Logger.i(TAG, "VoiceProcessor shutdown complete")
        } catch (e: Exception) {
            Logger.e(TAG, "Error during VoiceProcessor shutdown", e)
        }
    }
    
    companion object {
        private const val TAG = "VoiceProcessor"
    }
}