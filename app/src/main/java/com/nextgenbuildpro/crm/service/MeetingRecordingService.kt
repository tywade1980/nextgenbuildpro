package com.nextgenbuildpro.crm.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.telecom.Call
import android.util.Log
import com.nextgenbuildpro.shared.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * MeetingRecordingService - Handles recording for both phone calls and in-person meetings
 * 
 * Features:
 * - Automatic call recording when enabled
 * - Manual recording for in-person meetings
 * - Integration with transcription service
 * - Metadata tracking for each recording
 */
class MeetingRecordingService(private val context: Context) {
    private val TAG = "MeetingRecordingService"
    
    private var recorder: MediaRecorder? = null
    private var currentRecording: MeetingRecording? = null
    private var isRecording = false
    
    // Recording settings
    private val _autoRecordCalls = MutableStateFlow(false)
    val autoRecordCalls: StateFlow<Boolean> = _autoRecordCalls.asStateFlow()
    
    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    private val _currentRecordingInfo = MutableStateFlow<MeetingRecording?>(null)
    val currentRecordingInfo: StateFlow<MeetingRecording?> = _currentRecordingInfo.asStateFlow()
    
    /**
     * Start recording a meeting
     * @param meetingType Type of meeting (call or in-person)
     * @param meetingTitle Optional title for the meeting
     * @param participants List of participants
     * @return Result with the recording info
     */
    suspend fun startRecording(
        meetingType: MeetingType,
        meetingTitle: String? = null,
        participants: List<String> = emptyList()
    ): Result<MeetingRecording> {
        if (isRecording) {
            Log.w(TAG, "Already recording")
            return Result.failure(IllegalStateException("Already recording a meeting"))
        }
        
        return try {
            // Create directory for meeting recordings if it doesn't exist
            val recordingsDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "meeting_recordings"
            )
            if (!recordingsDir.exists()) {
                recordingsDir.mkdirs()
            }
            
            // Generate unique filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val outputFile = File(recordingsDir, "meeting_${timestamp}.3gp")
            
            // Create recording metadata
            val recording = MeetingRecording(
                id = UUID.randomUUID().toString(),
                filePath = outputFile.absolutePath,
                meetingType = meetingType,
                title = meetingTitle ?: "Meeting $timestamp",
                participants = participants,
                startTime = LocalDateTime.now()
            )
            
            // Initialize media recorder
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            recorder?.apply {
                // Use VOICE_COMMUNICATION for calls, MIC for meetings
                val audioSource = if (meetingType == MeetingType.PHONE_CALL) {
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION
                } else {
                    MediaRecorder.AudioSource.MIC
                }
                
                setAudioSource(audioSource)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                
                try {
                    prepare()
                    start()
                    
                    isRecording = true
                    currentRecording = recording
                    _recordingState.value = RecordingState.RECORDING
                    _currentRecordingInfo.value = recording
                    
                    Log.i(TAG, "Recording started: ${recording.title} (${recording.id})")
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to start recording", e)
                    releaseRecorder()
                    return Result.failure(e)
                }
            }
            
            if (recorder == null || !isRecording) {
                return Result.failure(IllegalStateException("Failed to initialize MediaRecorder"))
            }
            
            Result.success(recording)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            releaseRecorder()
            Result.failure(e)
        }
    }
    
    /**
     * Stop the current recording
     * @return Result with the completed recording info
     */
    suspend fun stopRecording(): Result<MeetingRecording> {
        if (!isRecording || currentRecording == null) {
            Log.w(TAG, "Not currently recording")
            return Result.failure(IllegalStateException("Not currently recording"))
        }
        
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            
            val recording = currentRecording!!.copy(
                endTime = LocalDateTime.now(),
                status = RecordingStatus.COMPLETED
            )
            
            currentRecording = null
            isRecording = false
            _recordingState.value = RecordingState.IDLE
            _currentRecordingInfo.value = null
            
            Log.i(TAG, "Recording stopped: ${recording.title} (${recording.id})")
            Result.success(recording)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            releaseRecorder()
            Result.failure(e)
        }
    }
    
    /**
     * Pause the current recording
     */
    suspend fun pauseRecording(): Result<Unit> {
        if (!isRecording) {
            return Result.failure(IllegalStateException("Not currently recording"))
        }
        
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder?.pause()
                _recordingState.value = RecordingState.PAUSED
                Log.i(TAG, "Recording paused")
                Result.success(Unit)
            } else {
                Result.failure(UnsupportedOperationException("Pause not supported on this device"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing recording", e)
            Result.failure(e)
        }
    }
    
    /**
     * Resume a paused recording
     */
    suspend fun resumeRecording(): Result<Unit> {
        if (_recordingState.value != RecordingState.PAUSED) {
            return Result.failure(IllegalStateException("Recording is not paused"))
        }
        
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder?.resume()
                _recordingState.value = RecordingState.RECORDING
                Log.i(TAG, "Recording resumed")
                Result.success(Unit)
            } else {
                Result.failure(UnsupportedOperationException("Resume not supported on this device"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming recording", e)
            Result.failure(e)
        }
    }
    
    /**
     * Enable/disable automatic call recording
     */
    fun setAutoRecordCalls(enabled: Boolean) {
        _autoRecordCalls.value = enabled
        Log.i(TAG, "Auto-record calls: $enabled")
    }
    
    /**
     * Check if currently recording
     */
    fun isRecording(): Boolean = isRecording
    
    /**
     * Get current recording state
     */
    fun getRecordingState(): RecordingState = _recordingState.value
    
    /**
     * Clean up recorder resources
     */
    private fun releaseRecorder() {
        try {
            recorder?.release()
            recorder = null
            isRecording = false
            currentRecording = null
            _recordingState.value = RecordingState.IDLE
            _currentRecordingInfo.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing recorder", e)
        }
    }
    
    /**
     * Handle call state changes for automatic recording
     */
    suspend fun handleCallStateChange(callState: Int, phoneNumber: String?) {
        if (!_autoRecordCalls.value) {
            return
        }
        
        when (callState) {
            Call.STATE_ACTIVE -> {
                // Start recording when call becomes active
                if (!isRecording) {
                    val participants = if (phoneNumber != null) listOf(phoneNumber) else emptyList()
                    startRecording(
                        meetingType = MeetingType.PHONE_CALL,
                        meetingTitle = "Call with ${phoneNumber ?: "Unknown"}",
                        participants = participants
                    )
                }
            }
            Call.STATE_DISCONNECTED -> {
                // Stop recording when call ends
                if (isRecording) {
                    stopRecording()
                }
            }
        }
    }
    
    /**
     * Cleanup on service destruction
     */
    fun cleanup() {
        if (isRecording) {
            try {
                recorder?.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping recorder on cleanup", e)
            }
        }
        releaseRecorder()
    }
}

