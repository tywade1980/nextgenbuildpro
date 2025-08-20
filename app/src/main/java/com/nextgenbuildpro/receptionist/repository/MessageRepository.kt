package com.nextgenbuildpro.receptionist.repository

import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.receptionist.data.Message
import com.nextgenbuildpro.receptionist.data.MessagePriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.UUID

/**
 * Repository for managing messages taken by the AI receptionist
 */
class MessageRepository : Repository<Message> {
    
    // In-memory storage for messages
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    /**
     * Get all messages
     */
    override suspend fun getAll(): List<Message> {
        return _messages.value
    }
    
    /**
     * Get a message by ID
     */
    override suspend fun getById(id: String): Message? {
        return _messages.value.find { it.id == id }
    }
    
    /**
     * Save a new message
     */
    override suspend fun save(item: Message): Boolean {
        try {
            _messages.value = _messages.value + item
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Update an existing message
     */
    override suspend fun update(item: Message): Boolean {
        try {
            _messages.value = _messages.value.map { 
                if (it.id == item.id) item else it 
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Delete a message by ID
     */
    override suspend fun delete(id: String): Boolean {
        try {
            _messages.value = _messages.value.filter { it.id != id }
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Save a message (non-suspend version for use from AIReceptionist)
     */
    fun saveMessage(message: Message): Boolean {
        try {
            _messages.value = _messages.value + message
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Mark a message as read
     */
    suspend fun markAsRead(id: String): Boolean {
        try {
            val message = getById(id) ?: return false
            val updatedMessage = message.copy(isRead = true)
            return update(updatedMessage)
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Get unread messages
     */
    suspend fun getUnreadMessages(): List<Message> {
        return _messages.value.filter { !it.isRead }
    }
    
    /**
     * Get messages for a specific lead
     */
    suspend fun getMessagesForLead(leadId: String): List<Message> {
        return _messages.value.filter { it.leadId == leadId }
    }
    
    /**
     * Get messages by priority
     */
    suspend fun getMessagesByPriority(priority: MessagePriority): List<Message> {
        return _messages.value.filter { it.priority == priority }
    }
    
    /**
     * Get messages by tag
     */
    suspend fun getMessagesByTag(tag: String): List<Message> {
        return _messages.value.filter { it.tags.contains(tag) }
    }
    
    /**
     * Get messages from a specific sender
     */
    suspend fun getMessagesBySender(sender: String): List<Message> {
        return _messages.value.filter { 
            it.from.contains(sender, ignoreCase = true)
        }
    }
    
    /**
     * Get messages containing specific text
     */
    suspend fun searchMessages(query: String): List<Message> {
        return _messages.value.filter { 
            it.content.contains(query, ignoreCase = true) || 
            it.from.contains(query, ignoreCase = true)
        }
    }
    
    /**
     * Get messages from a specific date range
     */
    suspend fun getMessagesInDateRange(startDate: Date, endDate: Date): List<Message> {
        return _messages.value.filter { 
            it.timestamp.after(startDate) && it.timestamp.before(endDate)
        }
    }
    
    /**
     * Get recent messages
     */
    suspend fun getRecentMessages(limit: Int = 10): List<Message> {
        return _messages.value
            .sortedByDescending { it.timestamp }
            .take(limit)
    }
}