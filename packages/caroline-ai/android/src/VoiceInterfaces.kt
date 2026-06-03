package com.example.caroline.voice

// STT Related Data Models and Callbacks
enum class SttStatus {
    LISTENING, PROCESSING, ERROR, IDLE
}

data class SttResult(
    val transcript: String?,
    val isFinal: Boolean,
    val confidence: Float? = null,
    val error: String? = null,
    val status: SttStatus
)

// TTS Related Data Models and Callbacks
enum class TtsStatus {
    SPEAKING, COMPLETED, ERROR, IDLE
}

data class VoiceConfig(
    val voiceName: String? = null, // Specific voice model, e.g., from Eleven Labs
    val pitch: Float = 1.0f,
    val rate: Float = 1.0f,
    val language: String = "en-US"
)

enum class SynthesisStatus {
    SUCCESS, ERROR, IN_PROGRESS
}

// Voice Input Service Interface
interface VoiceInputService {
    fun startListening(callback: (result: SttResult) -> Unit)
    fun stopListening()
    fun setWakeWord(wakeWord: String, onWakeWordDetected: () -> Unit)
    fun currentStatus(): SttStatus
}

// Voice Output Service Interface
interface VoiceOutputService {
    fun speak(text: String, voiceConfig: VoiceConfig, callback: (status: TtsStatus) -> Unit)
    fun stopSpeaking()
    fun synthesizeToFile(text: String, voiceConfig: VoiceConfig, filePath: String, callback: (status: SynthesisStatus, outputFile: String?) -> Unit)
    fun currentStatus(): TtsStatus
}

