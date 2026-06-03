package com.nextgenbuildpro.voice

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgenbuildpro.brain.SystemPrompts
import com.nextgenbuildpro.orchestrator.CarolineOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceUiState(
    val isConnected: Boolean = false,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val transcriptText: String = "",
    val statusMessage: String = "Tap to speak",
    val orbPulse: Float = 0f,
    val errorMessage: String? = null
)

/**
 * Converted from voice-ai-app/App.tsx.
 * useState hooks  →  MutableStateFlow fields
 * useRef fields   →  class-level vars
 * useCallback fns →  regular suspend/fun methods
 * Audio chunks via AudioRecord (PCM16) mirroring expo-av M4A chunks re-encoded to PCM.
 */
@HiltViewModel
class CarolineVoiceViewModel @Inject constructor(
    private val xaiClient: XAIRealtimeClient,
    private val carolineOrchestrator: CarolineOrchestrator
) : ViewModel() {

    private val TAG = "CarolineVoiceVM"

    private val SAMPLE_RATE = 24000
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 4

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var recordingJob: Job? = null
    private var eventJob: Job? = null

    init {
        observeXAIEvents()
    }

    fun connect() {
        xaiClient.connect()
        viewModelScope.launch {
            // Wait briefly for session.created then configure
            kotlinx.coroutines.delay(1000)
            xaiClient.updateSession(
                systemPrompt = SystemPrompts.CAROLINE,
                voice = "alloy"
            )
        }
        _uiState.value = _uiState.value.copy(statusMessage = "Connecting…")
    }

    fun disconnect() {
        stopListening()
        xaiClient.disconnect()
        _uiState.value = _uiState.value.copy(isConnected = false, statusMessage = "Tap to speak")
    }

    @SuppressLint("MissingPermission")
    fun startListening() {
        if (_uiState.value.isListening) return
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE
        ).also { it.startRecording() }

        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            val buffer = ByteArray(BUFFER_SIZE / 4)
            while (_uiState.value.isListening) {
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: break
                if (bytesRead > 0) {
                    xaiClient.sendAudioChunk(buffer.copyOf(bytesRead))
                    // Update orb pulse based on RMS
                    val rms = rms(buffer, bytesRead)
                    _uiState.value = _uiState.value.copy(orbPulse = rms)
                }
            }
        }
        _uiState.value = _uiState.value.copy(isListening = true, statusMessage = "Listening…")
    }

    fun stopListening() {
        recordingJob?.cancel()
        audioRecord?.apply { stop(); release() }
        audioRecord = null
        if (_uiState.value.isListening) {
            xaiClient.commitAudio()
        }
        _uiState.value = _uiState.value.copy(isListening = false, orbPulse = 0f, statusMessage = "Processing…")
    }

    /** Text-only path: send message through CarolineOrchestrator (Hermes + LLM) */
    fun sendTextMessage(text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(statusMessage = "Thinking…")
            try {
                val response = carolineOrchestrator.handleRequest(text)
                _uiState.value = _uiState.value.copy(
                    transcriptText = response,
                    statusMessage = "Done"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    private fun observeXAIEvents() {
        eventJob = viewModelScope.launch {
            xaiClient.events.collect { event ->
                when (event) {
                    is XAIEvent.SessionCreated -> {
                        _uiState.value = _uiState.value.copy(
                            isConnected = true,
                            statusMessage = "Ready"
                        )
                    }
                    is XAIEvent.AudioDelta -> {
                        playAudioChunk(Base64.decode(event.audioBase64, Base64.NO_WRAP))
                        _uiState.value = _uiState.value.copy(isSpeaking = true)
                    }
                    is XAIEvent.TextDelta -> {
                        _uiState.value = _uiState.value.copy(
                            transcriptText = _uiState.value.transcriptText + event.text
                        )
                    }
                    is XAIEvent.ResponseDone -> {
                        _uiState.value = _uiState.value.copy(
                            isSpeaking = false,
                            statusMessage = "Tap to speak"
                        )
                    }
                    is XAIEvent.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = event.message,
                            statusMessage = "Error"
                        )
                    }
                    XAIEvent.Disconnected -> {
                        _uiState.value = _uiState.value.copy(
                            isConnected = false,
                            statusMessage = "Disconnected"
                        )
                    }
                }
            }
        }
    }

    private fun playAudioChunk(pcm16Bytes: ByteArray) {
        if (audioTrack == null) {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build())
                .setAudioFormat(AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build())
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(BUFFER_SIZE)
                .build()
                .also { it.play() }
        }
        audioTrack?.write(pcm16Bytes, 0, pcm16Bytes.size)
    }

    private fun rms(buffer: ByteArray, length: Int): Float {
        var sum = 0.0
        for (i in 0 until length step 2) {
            val sample = (buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)
            sum += sample.toLong() * sample.toLong()
        }
        val rms = Math.sqrt(sum / (length / 2))
        return (rms / 32768.0).toFloat().coerceIn(0f, 1f)
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
        audioTrack?.apply { stop(); release() }
    }
}
