package com.aireceptionist.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Data class representing the context of a phone call
 */
@Entity(tableName = "call_contexts")
data class CallContext(
    @PrimaryKey
    val callId: String,
    val callerNumber: String?,
    val callerName: String?,
    val callStartTime: Long,
    val callEndTime: Long? = null,
    val isIncoming: Boolean,
    val callState: String,
    val intent: String? = null,
    val sentiment: String? = null,
    val language: String? = null,
    val isVipCaller: Boolean? = null,
    val lastUserInput: String? = null,
    val aiResponseCount: Int = 0,
    val humanTransferRequested: Boolean = false,
    val satisfactionScore: Float? = null,
    val notes: String? = null,
    val metadata: String? = null // JSON string for additional metadata
)

/**
 * Represents detected intent in user input
 */
data class Intent(
    val name: String,
    val confidence: Float,
    val parameters: Map<String, Any>
)

/**
 * Represents extracted entities from user input  
 */
data class Entity(
    val type: String,
    val value: String,
    val confidence: Float,
    val startIndex: Int = -1,
    val endIndex: Int = -1
)

/**
 * Call history summary for a phone number
 */
@Entity(tableName = "caller_history")
data class CallerHistoryEntity(
    @PrimaryKey
    val phoneNumber: String,
    val totalCalls: Int,
    val lastCallDate: Long,
    val averageSatisfaction: Float,
    val isVip: Boolean = false,
    val preferredLanguage: String? = null,
    val commonIntents: String? = null, // JSON array of common intents
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)