package com.nextgenbuildpro.receptionist.data

import java.util.Date

/**
 * Data class representing a message taken by the AI receptionist
 */
data class Message(
    /**
     * Unique identifier for the message
     */
    val id: String,
    
    /**
     * Name of the person who left the message
     */
    val from: String,
    
    /**
     * Content of the message
     */
    val content: String,
    
    /**
     * Timestamp when the message was recorded
     */
    val timestamp: Date,
    
    /**
     * Phone number of the person who left the message (optional)
     */
    val phoneNumber: String? = null,
    
    /**
     * Email of the person who left the message (optional)
     */
    val email: String? = null,
    
    /**
     * ID of the lead associated with this message (optional)
     */
    val leadId: String? = null,
    
    /**
     * Whether the message has been read
     */
    val isRead: Boolean = false,
    
    /**
     * Priority of the message (normal, high, urgent)
     */
    val priority: MessagePriority = MessagePriority.NORMAL,
    
    /**
     * Tags associated with the message
     */
    val tags: List<String> = emptyList()
)

/**
 * Enum representing the priority of a message
 */
enum class MessagePriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}