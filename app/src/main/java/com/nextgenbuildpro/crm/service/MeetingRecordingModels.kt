package com.nextgenbuildpro.crm.service

import java.time.LocalDateTime

/**
 * Recording state enumeration
 */
enum class RecordingState {
    IDLE, RECORDING, PAUSED, PROCESSING
}

/**
 * Meeting type enumeration
 */
enum class MeetingType {
    PHONE_CALL, IN_PERSON, VIDEO_CALL
}

/**
 * Recording status enumeration
 */
enum class RecordingStatus {
    IN_PROGRESS, COMPLETED, FAILED, CANCELLED
}

/**
 * Meeting recording metadata
 */
data class MeetingRecording(
    val id: String,
    val filePath: String,
    val meetingType: MeetingType,
    val title: String,
    val participants: List<String> = emptyList(),
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val status: RecordingStatus = RecordingStatus.IN_PROGRESS,
    val transcription: String? = null,
    val actionableItems: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Meeting context for extraction
 */
data class MeetingContext(
    val meetingId: String,
    val meetingTitle: String,
    val meetingType: MeetingType,
    val meetingDate: LocalDateTime,
    val participants: List<String>,
    val projectId: String? = null,
    val projectName: String? = null
)

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
    val extractedTasks: List<com.nextgenbuildpro.shared.NextGenTask>,
    val processedAt: LocalDateTime
)
