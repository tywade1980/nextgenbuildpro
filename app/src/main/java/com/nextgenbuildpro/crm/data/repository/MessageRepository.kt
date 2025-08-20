package com.nextgenbuildpro.crm.data.repository

import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.core.DateUtils
import com.nextgenbuildpro.crm.data.model.MessageRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing messages in the CRM module
 */
class MessageRepository : Repository<MessageRecord> {
    private val _messages = MutableStateFlow<List<MessageRecord>>(emptyList())
    val messages: StateFlow<List<MessageRecord>> = _messages.asStateFlow()
    
    init {
        // Load sample data for demonstration
        loadSampleData()
    }
    
    /**
     * Get all messages
     */
    override suspend fun getAll(): List<MessageRecord> {
        return _messages.value
    }
    
    /**
     * Get a message by ID
     */
    override suspend fun getById(id: String): MessageRecord? {
        return _messages.value.find { it.id == id }
    }
    
    /**
     * Save a new message
     */
    override suspend fun save(item: MessageRecord): Boolean {
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
    override suspend fun update(item: MessageRecord): Boolean {
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
     * Get messages for a specific lead
     */
    suspend fun getMessagesForLead(leadId: String): List<MessageRecord> {
        return _messages.value.filter { it.leadId == leadId }
    }
    
    /**
     * Send a new message
     */
    suspend fun sendMessage(leadId: String, leadName: String, phoneNumber: String, content: String): MessageRecord? {
        try {
            val newMessage = MessageRecord(
                id = "msg_${System.currentTimeMillis()}",
                leadId = leadId,
                leadName = leadName,
                phoneNumber = phoneNumber,
                timestamp = DateUtils.getCurrentTimestamp(),
                content = content,
                type = "Outgoing"
            )
            
            _messages.value = listOf(newMessage) + _messages.value
            return newMessage
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * Receive a new message
     */
    suspend fun receiveMessage(leadId: String, leadName: String, phoneNumber: String, content: String): MessageRecord? {
        try {
            val newMessage = MessageRecord(
                id = "msg_${System.currentTimeMillis()}",
                leadId = leadId,
                leadName = leadName,
                phoneNumber = phoneNumber,
                timestamp = DateUtils.getCurrentTimestamp(),
                content = content,
                type = "Incoming"
            )
            
            _messages.value = listOf(newMessage) + _messages.value
            return newMessage
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * Generate a message template for a specific purpose
     */
    fun generateMessageTemplate(leadName: String, templateType: String, additionalInfo: Map<String, String> = emptyMap()): String {
        return when (templateType) {
            "follow_up" -> "Hi $leadName, I'm following up on our conversation about your project. Would you like to schedule a time to discuss further?"
            "estimate" -> {
                val amount = additionalInfo["amount"] ?: "your estimate"
                val projectType = additionalInfo["projectType"] ?: "your project"
                "Hi $leadName, your estimate for the $projectType project is ready. The total comes to $$amount. Please let me know if you have any questions."
            }
            "appointment" -> {
                val date = additionalInfo["date"] ?: "your appointment"
                "Hi $leadName, this is a reminder about your appointment on $date. Please confirm if this still works for you."
            }
            "project_update" -> {
                val projectType = additionalInfo["projectType"] ?: "your project"
                val progress = additionalInfo["progress"] ?: "0"
                val endDate = additionalInfo["endDate"] ?: "the scheduled completion date"
                "Hi $leadName, your $projectType project is now $progress% complete. We're on track to finish by $endDate."
            }
            else -> "Hi $leadName, thank you for choosing NextGenBuildPro. How can we help you today?"
        }
    }
    
    /**
     * Load sample data for demonstration
     */
    private fun loadSampleData() {
        val sampleMessages = listOf(
            MessageRecord(
                id = "msg_1",
                leadId = "lead_1",
                leadName = "John Smith",
                phoneNumber = "555-123-4567",
                timestamp = "06/02/2023 09:30:15",
                content = "Hi John, this is a reminder about your consultation tomorrow at 10:00 AM. Please confirm if this still works for you.",
                type = "Outgoing"
            ),
            MessageRecord(
                id = "msg_2",
                leadId = "lead_1",
                leadName = "John Smith",
                phoneNumber = "555-123-4567",
                timestamp = "06/02/2023 10:05:22",
                content = "Yes, that works for me. See you tomorrow!",
                type = "Incoming"
            ),
            MessageRecord(
                id = "msg_3",
                leadId = "lead_3",
                leadName = "Michael Brown",
                phoneNumber = "555-345-6789",
                timestamp = "06/01/2023 16:45:30",
                content = "Hi Michael, I've sent your deck construction estimate to your email. Please let me know if you have any questions.",
                type = "Outgoing"
            ),
            MessageRecord(
                id = "msg_4",
                leadId = "lead_2",
                leadName = "Sarah Johnson",
                phoneNumber = "555-234-5678",
                timestamp = "06/02/2023 14:20:10",
                content = "Hi Sarah, I wanted to confirm our meeting next week to discuss the bathroom remodel project. Does Tuesday at 2 PM still work for you?",
                type = "Outgoing"
            ),
            MessageRecord(
                id = "msg_5",
                leadId = "lead_2",
                leadName = "Sarah Johnson",
                phoneNumber = "555-234-5678",
                timestamp = "06/02/2023 15:05:45",
                content = "Hi, Tuesday works great. Looking forward to it!",
                type = "Incoming"
            )
        )
        
        _messages.value = sampleMessages
    }
}