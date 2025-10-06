package com.nextgenbuildpro.crm.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody

/**
 * Service for recording voice notes
 */
class VoiceRecorderService(private val context: Context) {
    private val TAG = "VoiceRecorderService"
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false

    /**
     * Start recording
     * @return The output file path if recording started successfully, null otherwise
     */
    fun startRecording(): String? {
        if (isRecording) {
            Log.d(TAG, "Already recording")
            return null
        }

        try {
            // Create directory for voice notes if it doesn't exist
            val voiceNotesDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "voice_notes"
            )
            if (!voiceNotesDir.exists()) {
                voiceNotesDir.mkdirs()
            }

            // Generate a unique filename
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            outputFile = File(voiceNotesDir, "voice_note_$timestamp.3gp")

            // Initialize recorder
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile?.absolutePath)

                try {
                    prepare()
                    start()
                    isRecording = true
                    Log.d(TAG, "Recording started: ${outputFile?.absolutePath}")
                    return outputFile?.absolutePath
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to prepare recorder", e)
                    releaseRecorder()
                    return null
                }
            }

            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            releaseRecorder()
            return null
        }
    }

    /**
     * Stop recording
     * @return The output file path if recording stopped successfully, null otherwise
     */
    fun stopRecording(): String? {
        if (!isRecording) {
            Log.d(TAG, "Not recording")
            return null
        }

        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            isRecording = false

            Log.d(TAG, "Recording stopped: ${outputFile?.absolutePath}")
            return outputFile?.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            releaseRecorder()
            return null
        }
    }

    /**
     * Check if recording is in progress
     */
    fun isRecording(): Boolean {
        return isRecording
    }

    /**
     * Release recorder resources
     */
    private fun releaseRecorder() {
        try {
            recorder?.release()
            recorder = null
            isRecording = false
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing recorder", e)
        }
    }

    /**
     * Transcribe the recorded audio using the speech API
     * @param filePath Path to the audio file to transcribe
     * @return Transcribed text
     */
    suspend fun transcribeAudio(filePath: String): String {
        Log.d(TAG, "Transcribing audio file: $filePath")

        try {
            // Get the API key from secure storage
            val secureStorage = com.nextgenbuildpro.core.api.impl.SecureCredentialStorageImpl()
            secureStorage.initialize(context)

            val apiKey = secureStorage.getCredential(com.nextgenbuildpro.core.api.SecureCredentialStorage.KEY_SPEECH_API)

            if (apiKey.isNullOrEmpty()) {
                Log.e(TAG, "Speech API key not found in secure storage")
                return "Error: Speech API key not configured. Please configure your OpenAI API key in settings."
            }

            // Use OpenAI Whisper API for speech-to-text transcription
            return transcribeWithWhisperAPI(filePath, apiKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error transcribing audio", e)
            return "Error transcribing audio: ${e.message}"
        }
    }

    /**
     * Transcribe audio using OpenAI's Whisper API
     */
    private suspend fun transcribeWithWhisperAPI(filePath: String, apiKey: String): String {
        return try {
            val file = java.io.File(filePath)
            if (!file.exists()) {
                return "Error: Audio file not found"
            }

            // Create multipart request for Whisper API
            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody("audio/mpeg".toMediaType())
                )
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("language", "en") // English for construction terminology
                .addFormDataPart("response_format", "text")
                .build()

            val request = Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                Log.d(TAG, "Audio transcription successful")
                responseBody.trim()
            } else {
                Log.e(TAG, "Whisper API error: ${response.code} - ${responseBody}")
                "Error: Failed to transcribe audio (HTTP ${response.code})"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Whisper API", e)
            "Error: Failed to transcribe audio - ${e.message}"
        }
    }
}
