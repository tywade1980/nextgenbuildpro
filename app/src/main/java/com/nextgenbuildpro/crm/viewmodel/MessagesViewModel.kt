package com.nextgenbuildpro.crm.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgenbuildpro.core.ViewModel as CoreViewModel
import com.nextgenbuildpro.crm.data.model.MessageRecord
import com.nextgenbuildpro.crm.data.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for managing messages in the CRM module
 */
class MessagesViewModel(
    private val repository: MessageRepository
) : ViewModel(), CoreViewModel<MessageRecord> {
    
    // All messages
    private val _messages = mutableStateOf<List<MessageRecord>>(emptyList())
    override val items: State<List<MessageRecord>> = _messages
    
    // Messages for selected lead
    private val _leadMessages = MutableStateFlow<List<MessageRecord>>(emptyList())
    val leadMessages: StateFlow<List<MessageRecord>> = _leadMessages.asStateFlow()
    
    // Selected lead ID
    private val _selectedLeadId = MutableStateFlow<String?>(null)
    val selectedLeadId: StateFlow<String?> = _selectedLeadId.asStateFlow()
    
    // Message draft
    private val _messageDraft = MutableStateFlow("")
    val messageDraft: StateFlow<String> = _messageDraft.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        // Collect messages from repository
        viewModelScope.launch {
            repository.messages.collectLatest { messages ->
                _messages.value = messages
                updateLeadMessages()
            }
        }
    }
    
    /**
     * Refresh messages from repository
     */
    override fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _messages.value = repository.getAll()
                updateLeadMessages()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to refresh messages: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Select a lead to view messages for
     */
    fun selectLead(leadId: String) {
        _selectedLeadId.value = leadId
        updateLeadMessages()
    }
    
    /**
     * Clear selected lead
     */
    fun clearSelectedLead() {
        _selectedLeadId.value = null
        _leadMessages.value = emptyList()
    }
    
    /**
     * Update message draft
     */
    fun updateMessageDraft(draft: String) {
        _messageDraft.value = draft
    }
    
    /**
     * Send a message
     */
    fun sendMessage(leadId: String, leadName: String, phoneNumber: String) {
        val content = _messageDraft.value.trim()
        if (content.isBlank()) {
            _error.value = "Message cannot be empty"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val message = repository.sendMessage(leadId, leadName, phoneNumber, content)
                if (message == null) {
                    _error.value = "Failed to send message"
                } else {
                    _error.value = null
                    _messageDraft.value = ""
                    updateLeadMessages()
                }
            } catch (e: Exception) {
                _error.value = "Failed to send message: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Generate a message template
     */
    fun generateMessageTemplate(leadName: String, templateType: String, additionalInfo: Map<String, String> = emptyMap()) {
        val template = repository.generateMessageTemplate(leadName, templateType, additionalInfo)
        _messageDraft.value = template
    }
    
    /**
     * Simulate receiving a message (for demo purposes)
     */
    fun simulateReceiveMessage(leadId: String, leadName: String, phoneNumber: String, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val message = repository.receiveMessage(leadId, leadName, phoneNumber, content)
                if (message == null) {
                    _error.value = "Failed to receive message"
                } else {
                    _error.value = null
                    updateLeadMessages()
                }
            } catch (e: Exception) {
                _error.value = "Failed to receive message: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get messages for a specific lead
     */
    fun getMessagesForLead(leadId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val messages = repository.getMessagesForLead(leadId)
                _leadMessages.value = messages
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to get messages for lead: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Delete a message
     */
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.delete(messageId)
                if (!success) {
                    _error.value = "Failed to delete message"
                } else {
                    _error.value = null
                    updateLeadMessages()
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete message: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update lead messages based on selected lead
     */
    private fun updateLeadMessages() {
        val leadId = _selectedLeadId.value
        if (leadId != null) {
            _leadMessages.value = _messages.value.filter { it.leadId == leadId }
        } else {
            _leadMessages.value = emptyList()
        }
    }
}