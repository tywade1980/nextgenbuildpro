package com.nextgenbuildpro.crm.service

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.ai.llm.LLMService
import com.nextgenbuildpro.core.MainOrchestrator
import com.nextgenbuildpro.shared.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

/**
 * MeetingRecordingManager - High-level coordinator for meeting recording functionality
 * 
 * Orchestrates the complete flow:
 * 1. Record meeting audio (calls or in-person)
 * 2. Transcribe audio to text
 * 3. Extract actionable items using AI
 * 4. Create tasks in the system
 * 5. Route tasks to appropriate agents
 */
class MeetingRecordingManager(
    private val context: Context,
    private val llmService: LLMService,
    private val mainOrchestrator: MainOrchestrator
) {
    private val TAG = "MeetingRecordingManager"
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Service instances
    private val recordingService = MeetingRecordingService(context)
    private val voiceRecorderService = VoiceRecorderService(context)
    private val actionableItemExtractor = ActionableItemExtractor(context, llmService)
    
    // Manager state
    private val _processingState = MutableStateFlow(ProcessingState.IDLE)
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()
    
    private val _recentMeetings = MutableStateFlow<List<ProcessedMeeting>>(emptyList())
    val recentMeetings: StateFlow<List<ProcessedMeeting>> = _recentMeetings.asStateFlow()
    
    /**
     * Start recording a meeting
     */
    suspend fun startMeetingRecording(
        meetingType: MeetingType,
        meetingTitle: String? = null,
        participants: List<String> = emptyList()
    ): Result<MeetingRecording> {
        Log.i(TAG, "Starting meeting recording: $meetingTitle")
        return recordingService.startRecording(meetingType, meetingTitle, participants)
    }
    
    /**
     * Stop recording and process the meeting
     * This will:
     * 1. Stop recording
     * 2. Transcribe the audio
     * 3. Extract actionable items
     * 4. Create tasks in the system
     */
    suspend fun stopAndProcessMeeting(): Result<ProcessedMeeting> {
        Log.i(TAG, "Stopping and processing meeting")
        
        return try {
            // Stop recording
            val recordingResult = recordingService.stopRecording()
            if (recordingResult.isFailure) {
                return Result.failure(recordingResult.exceptionOrNull()!!)
            }
            
            val recording = recordingResult.getOrNull()!!
            _processingState.value = ProcessingState.TRANSCRIBING
            
            // Transcribe audio
            Log.i(TAG, "Transcribing recording: ${recording.filePath}")
            val transcription = voiceRecorderService.transcribeAudio(recording.filePath)
            
            if (transcription.startsWith("Error")) {
                Log.e(TAG, "Transcription failed: $transcription")
                _processingState.value = ProcessingState.ERROR
                return Result.failure(Exception(transcription))
            }
            
            Log.i(TAG, "Transcription completed (${transcription.length} chars)")
            _processingState.value = ProcessingState.EXTRACTING
            
            // Extract actionable items
            val meetingContext = MeetingContext(
                meetingId = recording.id,
                meetingTitle = recording.title,
                meetingType = recording.meetingType,
                meetingDate = recording.startTime,
                participants = recording.participants
            )
            
            val itemsResult = actionableItemExtractor.extractActionableItems(transcription, meetingContext)
            
            val tasks = if (itemsResult.isSuccess) {
                itemsResult.getOrNull()!!
            } else {
                Log.e(TAG, "Failed to extract actionable items: ${itemsResult.exceptionOrNull()?.message}")
                emptyList()
            }
            
            Log.i(TAG, "Extracted ${tasks.size} actionable items")
            _processingState.value = ProcessingState.CREATING_TASKS
            
            // Create tasks in the system via MainOrchestrator
            val createdTasks = mutableListOf<NextGenTask>()
            for (task in tasks) {
                try {
                    // Route task through appropriate orchestrator
                    val result = mainOrchestrator.delegateTask(task)
                    if (result.isSuccess) {
                        createdTasks.add(result.getOrNull()!!)
                        Log.d(TAG, "Task created: ${task.title}")
                    } else {
                        Log.w(TAG, "Failed to create task: ${task.title}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating task: ${task.title}", e)
                }
            }
            
            // Create processed meeting record
            val processedMeeting = ProcessedMeeting(
                recording = recording.copy(
                    transcription = transcription,
                    actionableItems = tasks.map { it.title }
                ),
                transcription = transcription,
                extractedTasks = createdTasks,
                processedAt = LocalDateTime.now()
            )
            
            // Add to recent meetings
            val updated = _recentMeetings.value.toMutableList()
            updated.add(0, processedMeeting)
            if (updated.size > 50) {
                updated.removeAt(updated.size - 1)
            }
            _recentMeetings.value = updated
            
            _processingState.value = ProcessingState.COMPLETED
            
            Log.i(TAG, "Meeting processing completed: ${createdTasks.size} tasks created")
            Result.success(processedMeeting)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing meeting", e)
            _processingState.value = ProcessingState.ERROR
            Result.failure(e)
        } finally {
            // Reset to idle after a delay
            scope.launch {
                delay(3000)
                if (_processingState.value != ProcessingState.TRANSCRIBING &&
                    _processingState.value != ProcessingState.EXTRACTING &&
                    _processingState.value != ProcessingState.CREATING_TASKS) {
                    _processingState.value = ProcessingState.IDLE
                }
            }
        }
    }
    
    /**
     * Pause current recording
     */
    suspend fun pauseRecording(): Result<Unit> {
        return recordingService.pauseRecording()
    }
    
    /**
     * Resume paused recording
     */
    suspend fun resumeRecording(): Result<Unit> {
        return recordingService.resumeRecording()
    }
    
    /**
     * Enable/disable automatic call recording
     */
    fun setAutoRecordCalls(enabled: Boolean) {
        recordingService.setAutoRecordCalls(enabled)
        Log.i(TAG, "Auto-record calls: $enabled")
    }
    
    /**
     * Get auto-record calls setting
     */
    fun getAutoRecordCalls(): StateFlow<Boolean> {
        return recordingService.autoRecordCalls
    }
    
    /**
     * Get current recording state
     */
    fun getRecordingState(): StateFlow<RecordingState> {
        return recordingService.recordingState
    }
    
    /**
     * Get current recording info
     */
    fun getCurrentRecordingInfo(): StateFlow<MeetingRecording?> {
        return recordingService.currentRecordingInfo
    }
    
    /**
     * Handle call state changes for automatic recording
     */
    suspend fun handleCallStateChange(callState: Int, phoneNumber: String?) {
        recordingService.handleCallStateChange(callState, phoneNumber)
    }
    
    /**
     * Process an existing recording file
     * Useful for processing recordings that were made without automatic processing
     */
    suspend fun processExistingRecording(
        filePath: String,
        meetingTitle: String,
        meetingType: MeetingType,
        participants: List<String> = emptyList()
    ): Result<ProcessedMeeting> {
        Log.i(TAG, "Processing existing recording: $filePath")
        
        return try {
            _processingState.value = ProcessingState.TRANSCRIBING
            
            // Transcribe audio
            val transcription = voiceRecorderService.transcribeAudio(filePath)
            
            if (transcription.startsWith("Error")) {
                _processingState.value = ProcessingState.ERROR
                return Result.failure(Exception(transcription))
            }
            
            _processingState.value = ProcessingState.EXTRACTING
            
            // Extract actionable items
            val meetingContext = MeetingContext(
                meetingId = java.util.UUID.randomUUID().toString(),
                meetingTitle = meetingTitle,
                meetingType = meetingType,
                meetingDate = LocalDateTime.now(),
                participants = participants
            )
            
            val itemsResult = actionableItemExtractor.extractActionableItems(transcription, meetingContext)
            val tasks = itemsResult.getOrNull() ?: emptyList()
            
            _processingState.value = ProcessingState.CREATING_TASKS
            
            // Create tasks in system
            val createdTasks = tasks.mapNotNull { task ->
                mainOrchestrator.delegateTask(task).getOrNull()
            }
            
            val processedMeeting = ProcessedMeeting(
                recording = MeetingRecording(
                    id = meetingContext.meetingId,
                    filePath = filePath,
                    meetingType = meetingType,
                    title = meetingTitle,
                    participants = participants,
                    startTime = LocalDateTime.now(),
                    endTime = LocalDateTime.now(),
                    status = RecordingStatus.COMPLETED,
                    transcription = transcription,
                    actionableItems = tasks.map { it.title }
                ),
                transcription = transcription,
                extractedTasks = createdTasks,
                processedAt = LocalDateTime.now()
            )
            
            _processingState.value = ProcessingState.COMPLETED
            Result.success(processedMeeting)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing existing recording", e)
            _processingState.value = ProcessingState.ERROR
            Result.failure(e)
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        recordingService.cleanup()
        scope.cancel()
    }
}

/**
 * Processing state for the manager
 */
enum class ProcessingState {
    IDLE, TRANSCRIBING, EXTRACTING, CREATING_TASKS, COMPLETED, ERROR
}

/**
 * Processed meeting with all extracted information
 */
data class ProcessedMeeting(
    val recording: MeetingRecording,
    val transcription: String,
    val extractedTasks: List<NextGenTask>,
    val processedAt: LocalDateTime
)
