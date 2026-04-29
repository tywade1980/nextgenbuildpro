package com.aireceptionist.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

/**
 * Represents a complete call record
 */
@Entity(
    tableName = "call_records",
    foreignKeys = [
        ForeignKey(
            entity = CallContext::class,
            parentColumns = ["callId"],
            childColumns = ["callId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CallRecord(
    @PrimaryKey
    val id: String,
    val callId: String,
    val callerNumber: String?,
    val callerName: String?,
    val duration: Long, // in milliseconds
    val startTime: Long,
    val endTime: Long,
    val callType: CallType,
    val callResult: CallResult,
    val agentsUsed: String, // JSON array of agent IDs used
    val transcript: String? = null,
    val audioFilePath: String? = null,
    val finalIntent: String? = null,
    val satisfactionScore: Float? = null,
    val transferredToHuman: Boolean = false,
    val humanOperatorId: String? = null,
    val resolvedIssue: Boolean? = null,
    val followUpRequired: Boolean = false,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class CallType {
    INCOMING,
    OUTGOING,
    INTERNAL,
    CONFERENCE
}

enum class CallResult {
    COMPLETED,
    TRANSFERRED_TO_HUMAN,
    DROPPED,
    BUSY,
    NO_ANSWER,
    FAILED,
    VOICEMAIL
}

/**
 * Represents individual interactions within a call
 */
@Entity(
    tableName = "call_interactions",
    foreignKeys = [
        ForeignKey(
            entity = CallRecord::class,
            parentColumns = ["id"],
            childColumns = ["callRecordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CallInteraction(
    @PrimaryKey
    val id: String,
    val callRecordId: String,
    val timestamp: Long,
    val speakerType: SpeakerType,
    val agentId: String? = null,
    val content: String,
    val audioStartTime: Long? = null,
    val audioEndTime: Long? = null,
    val intent: String? = null,
    val sentiment: String? = null,
    val confidence: Float? = null,
    val entities: String? = null, // JSON array of entities
    val responseTime: Long? = null, // milliseconds to generate response
)

enum class SpeakerType {
    CALLER,
    AI_AGENT,
    HUMAN_OPERATOR,
    SYSTEM
}

/**
 * Tracks AI agent performance metrics
 */
@Entity(tableName = "agent_metrics")
data class AgentMetrics(
    @PrimaryKey
    val id: String,
    val agentId: String,
    val date: String, // YYYY-MM-DD format
    val totalInteractions: Int = 0,
    val successfulInteractions: Int = 0,
    val averageConfidence: Float = 0.0f,
    val averageResponseTime: Long = 0L, // milliseconds
    val escalationsToHuman: Int = 0,
    val customerSatisfactionScore: Float = 0.0f,
    val commonIntents: String? = null, // JSON array
    val errorCount: Int = 0,
    val uptime: Long = 0L, // milliseconds
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Knowledge base entries for AI training
 */
@Entity(tableName = "knowledge_base")
data class KnowledgeBaseEntry(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val tags: String, // JSON array of tags
    val intent: String? = null,
    val priority: Int = 0,
    val isActive: Boolean = true,
    val usageCount: Int = 0,
    val successRate: Float = 0.0f,
    val language: String = "en",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * FAQ entries
 */
@Entity(tableName = "faq_entries")  
data class FAQEntry(
    @PrimaryKey
    val id: String,
    val question: String,
    val answer: String,
    val category: String,
    val keywords: String, // JSON array of keywords
    val priority: Int = 0,
    val isActive: Boolean = true,
    val viewCount: Int = 0,
    val helpfulVotes: Int = 0,
    val unhelpfulVotes: Int = 0,
    val language: String = "en",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Appointment scheduling data
 */
@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey
    val id: String,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String? = null,
    val appointmentDate: Long,
    val appointmentTime: String,
    val duration: Int = 60, // minutes
    val serviceType: String,
    val status: AppointmentStatus,
    val notes: String? = null,
    val reminderSent: Boolean = false,
    val createdBy: String, // "ai" or agent ID
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    NO_SHOW,
    RESCHEDULED
}

/**
 * AI training data
 */
@Entity(tableName = "training_data")
data class TrainingData(
    @PrimaryKey
    val id: String,
    val inputText: String,
    val expectedIntent: String,
    val expectedEntities: String, // JSON array
    val actualIntent: String? = null,
    val actualEntities: String? = null, // JSON array
    val isCorrect: Boolean? = null,
    val confidence: Float? = null,
    val source: String, // "manual", "call_interaction", "feedback"
    val domain: String? = null,
    val language: String = "en",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)