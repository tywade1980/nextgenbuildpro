package com.nextgenbuildpro.agents.crm

import android.util.Log
import com.nextgenbuildpro.shared.*
import com.nextgenbuildpro.mcp.MCPServer
import kotlinx.coroutines.flow.*

/**
 * Contact Management Agent - CRM Orchestrator
 * 
 * Specialized in smart contact creation from calls, SMS, and voice commands.
 * Handles contact integration with device contacts and project assignment.
 */
class ContactManagementAgent : SpecializedAgent {
    override val agentId = "contact_management_agent"
    override val agentType = AgentType.CRM_ORCHESTRATOR
    override val specialization = "Smart contact creation and management from multiple sources"
    
    private val mcpServer = MCPServer.getInstance()
    private var mcpConnection: com.nextgenbuildpro.mcp.MCPConnection? = null
    
    private val _isActive = MutableStateFlow(false)
    override val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    private val _contactDatabase = MutableStateFlow<Map<String, ContactInfo>>(emptyMap())
    val contactDatabase: StateFlow<Map<String, ContactInfo>> = _contactDatabase.asStateFlow()
    
    override suspend fun initialize(): Result<Unit> = try {
        Log.i("ContactManagementAgent", "Initializing Contact Management Agent...")
        
        val connectionResult = mcpServer.createConnection(agentId, agentType)
        connectionResult.fold(
            onSuccess = { connection ->
                mcpConnection = connection
                loadExistingContacts()
                _isActive.value = true
                Log.i("ContactManagementAgent", "Contact Management Agent initialized successfully")
                Result.success(Unit)
            },
            onFailure = { error ->
                Log.e("ContactManagementAgent", "Failed to create MCP connection", error)
                Result.failure(error)
            }
        )
    } catch (e: Exception) {
        Log.e("ContactManagementAgent", "Failed to initialize Contact Management Agent", e)
        Result.failure(e)
    }
    
    override suspend fun processTask(task: NextGenTask): Result<NextGenTask> = try {
        Log.d("ContactManagementAgent", "Processing contact task: ${task.description}")
        
        val result = when (task.type) {
            "create_contact_from_call" -> createContactFromCall(task)
            "create_contact_from_sms" -> createContactFromSMS(task)
            "create_contact_from_voice" -> createContactFromVoice(task)
            "update_contact" -> updateContact(task)
            "merge_contacts" -> mergeContacts(task)
            "auto_populate_contact" -> autoPopulateContact(task)
            else -> throw IllegalArgumentException("Unknown contact task type: ${task.type}")
        }
        
        Result.success(result)
    } catch (e: Exception) {
        Log.e("ContactManagementAgent", "Error processing contact task", e)
        Result.failure(e)
    }
    
    /**
     * Create contact from recent call data
     */
    private suspend fun createContactFromCall(task: NextGenTask): NextGenTask {
        val phoneNumber = task.parameters["phone_number"] as? String
            ?: throw IllegalArgumentException("Phone number required")
        
        val callDuration = task.parameters["call_duration"] as? Long ?: 0L
        val callTime = task.parameters["call_time"] as? Long ?: System.currentTimeMillis()
        
        // Check if contact already exists
        val existingContact = findContactByPhone(phoneNumber)
        
        val contactInfo = if (existingContact != null) {
            // Update existing contact with call history
            existingContact.copy(
                callHistory = existingContact.callHistory + CallRecord(
                    phoneNumber = phoneNumber,
                    duration = callDuration,
                    timestamp = callTime,
                    type = "incoming"
                ),
                lastContact = callTime,
                contactFrequency = existingContact.contactFrequency + 1
            )
        } else {
            // Create new contact from call
            ContactInfo(
                id = generateContactId(),
                name = extractNameFromCallLog(phoneNumber) ?: "Unknown Contact",
                phoneNumber = phoneNumber,
                email = null,
                address = null,
                source = "call_log",
                callHistory = listOf(
                    CallRecord(
                        phoneNumber = phoneNumber,
                        duration = callDuration,
                        timestamp = callTime,
                        type = "incoming"
                    )
                ),
                createdAt = System.currentTimeMillis(),
                lastContact = callTime,
                contactFrequency = 1,
                projectAssociations = emptyList(),
                tags = listOf("recent_call"),
                leadScore = calculateInitialLeadScore(callDuration)
            )
        }
        
        // Save contact
        saveContact(contactInfo)
        
        return task.copy(
            status = "completed",
            result = mapOf(
                "contact_id" to contactInfo.id,
                "contact_name" to contactInfo.name,
                "action" to if (existingContact != null) "updated" else "created",
                "lead_score" to contactInfo.leadScore
            )
        )
    }
    
    /**
     * Create contact from SMS with address extraction
     */
    private suspend fun createContactFromSMS(task: NextGenTask): NextGenTask {
        val phoneNumber = task.parameters["phone_number"] as? String
            ?: throw IllegalArgumentException("Phone number required")
        
        val messageContent = task.parameters["message_content"] as? String
            ?: throw IllegalArgumentException("Message content required")
        
        val senderName = task.parameters["sender_name"] as? String
        
        // Extract information from SMS
        val extractedInfo = extractInfoFromSMS(messageContent)
        
        val contactInfo = ContactInfo(
            id = generateContactId(),
            name = senderName ?: extractedInfo["name"] as? String ?: "SMS Contact",
            phoneNumber = phoneNumber,
            email = extractedInfo["email"] as? String,
            address = extractedInfo["address"] as? String,
            source = "sms",
            callHistory = emptyList(),
            smsHistory = listOf(
                SMSRecord(
                    phoneNumber = phoneNumber,
                    content = messageContent,
                    timestamp = System.currentTimeMillis(),
                    type = "received"
                )
            ),
            createdAt = System.currentTimeMillis(),
            lastContact = System.currentTimeMillis(),
            contactFrequency = 1,
            projectAssociations = extractProjectAssociations(messageContent),
            tags = extractedInfo["tags"] as? List<String> ?: listOf("sms_contact"),
            leadScore = calculateLeadScoreFromSMS(messageContent)
        )
        
        saveContact(contactInfo)
        
        return task.copy(
            status = "completed",
            result = mapOf(
                "contact_id" to contactInfo.id,
                "contact_name" to contactInfo.name,
                "extracted_info" to extractedInfo,
                "address_found" to (extractedInfo["address"] != null),
                "lead_score" to contactInfo.leadScore
            )
        )
    }
    
    /**
     * Create contact from voice command
     */
    private suspend fun createContactFromVoice(task: NextGenTask): NextGenTask {
        val voiceInput = task.parameters["voice_input"] as? String
            ?: throw IllegalArgumentException("Voice input required")
        
        val recentCallNumber = task.parameters["recent_call_number"] as? String
        
        // Parse voice command for contact details
        val parsedInfo = parseVoiceContactCommand(voiceInput)
        
        val contactInfo = ContactInfo(
            id = generateContactId(),
            name = parsedInfo["name"] as? String ?: "Voice Contact",
            phoneNumber = recentCallNumber ?: parsedInfo["phone"] as? String,
            email = parsedInfo["email"] as? String,
            address = parsedInfo["address"] as? String,
            source = "voice_command",
            callHistory = if (recentCallNumber != null) {
                listOf(
                    CallRecord(
                        phoneNumber = recentCallNumber,
                        duration = 0L,
                        timestamp = System.currentTimeMillis(),
                        type = "voice_referenced"
                    )
                )
            } else emptyList(),
            createdAt = System.currentTimeMillis(),
            lastContact = System.currentTimeMillis(),
            contactFrequency = 1,
            projectAssociations = parsedInfo["projects"] as? List<String> ?: emptyList(),
            tags = listOf("voice_created"),
            leadScore = calculateLeadScoreFromVoice(voiceInput)
        )
        
        saveContact(contactInfo)
        
        return task.copy(
            status = "completed",
            result = mapOf(
                "contact_id" to contactInfo.id,
                "contact_name" to contactInfo.name,
                "voice_confidence" to parsedInfo["confidence"] ?: 0.8f,
                "lead_score" to contactInfo.leadScore
            )
        )
    }
    
    private suspend fun updateContact(task: NextGenTask): NextGenTask {
        val contactId = task.parameters["contact_id"] as? String
            ?: throw IllegalArgumentException("Contact ID required")
        
        val updates = task.parameters["updates"] as? Map<String, Any>
            ?: throw IllegalArgumentException("Updates required")
        
        val currentContacts = _contactDatabase.value.toMutableMap()
        val existingContact = currentContacts[contactId]
            ?: throw IllegalArgumentException("Contact not found: $contactId")
        
        val updatedContact = existingContact.copy(
            name = updates["name"] as? String ?: existingContact.name,
            phoneNumber = updates["phone"] as? String ?: existingContact.phoneNumber,
            email = updates["email"] as? String ?: existingContact.email,
            address = updates["address"] as? String ?: existingContact.address,
            tags = updates["tags"] as? List<String> ?: existingContact.tags,
            lastContact = System.currentTimeMillis()
        )
        
        currentContacts[contactId] = updatedContact
        _contactDatabase.value = currentContacts
        
        return task.copy(
            status = "completed",
            result = mapOf(
                "contact_id" to contactId,
                "updated_fields" to updates.keys.toList()
            )
        )
    }
    
    private suspend fun mergeContacts(task: NextGenTask): NextGenTask {
        val primaryContactId = task.parameters["primary_contact_id"] as? String
            ?: throw IllegalArgumentException("Primary contact ID required")
        
        val secondaryContactId = task.parameters["secondary_contact_id"] as? String
            ?: throw IllegalArgumentException("Secondary contact ID required")
        
        val currentContacts = _contactDatabase.value.toMutableMap()
        val primaryContact = currentContacts[primaryContactId]
            ?: throw IllegalArgumentException("Primary contact not found")
        
        val secondaryContact = currentContacts[secondaryContactId]
            ?: throw IllegalArgumentException("Secondary contact not found")
        
        // Merge contact information
        val mergedContact = primaryContact.copy(
            phoneNumber = primaryContact.phoneNumber ?: secondaryContact.phoneNumber,
            email = primaryContact.email ?: secondaryContact.email,
            address = primaryContact.address ?: secondaryContact.address,
            callHistory = primaryContact.callHistory + secondaryContact.callHistory,
            smsHistory = primaryContact.smsHistory + secondaryContact.smsHistory,
            projectAssociations = (primaryContact.projectAssociations + secondaryContact.projectAssociations).distinct(),
            tags = (primaryContact.tags + secondaryContact.tags).distinct(),
            contactFrequency = primaryContact.contactFrequency + secondaryContact.contactFrequency,
            leadScore = maxOf(primaryContact.leadScore, secondaryContact.leadScore)
        )
        
        currentContacts[primaryContactId] = mergedContact
        currentContacts.remove(secondaryContactId)
        _contactDatabase.value = currentContacts
        
        return task.copy(
            status = "completed",
            result = mapOf(
                "merged_contact_id" to primaryContactId,
                "removed_contact_id" to secondaryContactId,
                "merge_score" to calculateMergeScore(primaryContact, secondaryContact)
            )
        )
    }
    
    private suspend fun autoPopulateContact(task: NextGenTask): NextGenTask {
        val phoneNumber = task.parameters["phone_number"] as? String
            ?: throw IllegalArgumentException("Phone number required")
        
        // Auto-populate from recent calls and device contacts
        val deviceContactInfo = getDeviceContactInfo(phoneNumber)
        val callHistoryInfo = getCallHistoryInfo(phoneNumber)
        
        val populatedInfo = mutableMapOf<String, Any?>()
        
        deviceContactInfo?.let { deviceInfo ->
            populatedInfo["name"] = deviceInfo["name"]
            populatedInfo["email"] = deviceInfo["email"]
            populatedInfo["address"] = deviceInfo["address"]
        }
        
        callHistoryInfo?.let { callInfo ->
            populatedInfo["call_frequency"] = callInfo["frequency"]
            populatedInfo["last_call"] = callInfo["lastCall"]
            populatedInfo["total_duration"] = callInfo["totalDuration"]
        }
        
        return task.copy(
            status = "completed",
            result = mapOf(
                "populated_info" to populatedInfo,
                "confidence_score" to calculatePopulationConfidence(populatedInfo)
            )
        )
    }
    
    private fun loadExistingContacts() {
        // Load contacts from local storage/database
        val contacts = mapOf<String, ContactInfo>()
        _contactDatabase.value = contacts
    }
    
    private fun saveContact(contact: ContactInfo) {
        val currentContacts = _contactDatabase.value.toMutableMap()
        currentContacts[contact.id] = contact
        _contactDatabase.value = currentContacts
        
        Log.d("ContactManagementAgent", "Saved contact: ${contact.name} (${contact.id})")
    }
    
    private fun findContactByPhone(phoneNumber: String): ContactInfo? {
        return _contactDatabase.value.values.find { it.phoneNumber == phoneNumber }
    }
    
    private fun extractNameFromCallLog(phoneNumber: String): String? {
        // Extract name from call log or return null
        return null
    }
    
    private fun extractInfoFromSMS(messageContent: String): Map<String, Any> {
        val info = mutableMapOf<String, Any>()
        
        // Extract address using regex patterns
        val addressPattern = """\d+\s+[\w\s]+(?:St|Street|Ave|Avenue|Rd|Road|Dr|Drive|Blvd|Boulevard|Ln|Lane)[\w\s,]*""".toRegex(RegexOption.IGNORE_CASE)
        addressPattern.find(messageContent)?.let { match ->
            info["address"] = match.value.trim()
        }
        
        // Extract email
        val emailPattern = """[\w._%+-]+@[\w.-]+\.[A-Z]{2,}""".toRegex(RegexOption.IGNORE_CASE)
        emailPattern.find(messageContent)?.let { match ->
            info["email"] = match.value
        }
        
        // Extract project-related keywords
        val projectKeywords = listOf("renovation", "construction", "build", "remodel", "repair", "install")
        val foundKeywords = projectKeywords.filter { messageContent.contains(it, ignoreCase = true) }
        if (foundKeywords.isNotEmpty()) {
            info["tags"] = foundKeywords
        }
        
        return info
    }
    
    private fun extractProjectAssociations(messageContent: String): List<String> {
        val projectKeywords = listOf("project", "job", "construction", "renovation", "build")
        return projectKeywords.filter { messageContent.contains(it, ignoreCase = true) }
    }
    
    private fun parseVoiceContactCommand(voiceInput: String): Map<String, Any> {
        val info = mutableMapOf<String, Any>()
        
        // Parse name
        val namePattern = """add contact (\w+(?:\s+\w+)*)""".toRegex(RegexOption.IGNORE_CASE)
        namePattern.find(voiceInput)?.let { match ->
            info["name"] = match.groupValues[1]
        }
        
        // Parse phone
        val phonePattern = """\b\d{3}[-.]?\d{3}[-.]?\d{4}\b""".toRegex()
        phonePattern.find(voiceInput)?.let { match ->
            info["phone"] = match.value
        }
        
        info["confidence"] = 0.85f
        return info
    }
    
    private fun calculateInitialLeadScore(callDuration: Long): Float {
        return when {
            callDuration > 300000 -> 0.9f  // 5+ minutes
            callDuration > 120000 -> 0.7f  // 2+ minutes  
            callDuration > 60000 -> 0.5f   // 1+ minute
            else -> 0.3f
        }
    }
    
    private fun calculateLeadScoreFromSMS(messageContent: String): Float {
        val constructionKeywords = listOf("build", "construction", "renovation", "remodel", "contractor", "estimate")
        val urgencyKeywords = listOf("urgent", "asap", "soon", "emergency", "immediate")
        
        var score = 0.4f
        constructionKeywords.forEach { keyword ->
            if (messageContent.contains(keyword, ignoreCase = true)) score += 0.1f
        }
        urgencyKeywords.forEach { keyword ->
            if (messageContent.contains(keyword, ignoreCase = true)) score += 0.15f
        }
        
        return score.coerceAtMost(1.0f)
    }
    
    private fun calculateLeadScoreFromVoice(voiceInput: String): Float {
        return 0.6f // Base score for voice-created contacts
    }
    
    private fun calculateMergeScore(contact1: ContactInfo, contact2: ContactInfo): Float {
        var score = 0.0f
        
        // Phone number match
        if (contact1.phoneNumber == contact2.phoneNumber) score += 0.4f
        
        // Name similarity
        if (contact1.name.lowercase() == contact2.name.lowercase()) score += 0.3f
        
        // Email match
        if (contact1.email != null && contact1.email == contact2.email) score += 0.2f
        
        // Address similarity
        if (contact1.address != null && contact2.address != null) {
            if (contact1.address.lowercase().contains(contact2.address.lowercase()) ||
                contact2.address.lowercase().contains(contact1.address.lowercase())) {
                score += 0.1f
            }
        }
        
        return score
    }
    
    private fun calculatePopulationConfidence(populatedInfo: Map<String, Any?>): Float {
        val nonNullFields = populatedInfo.values.count { it != null }
        return (nonNullFields.toFloat() / populatedInfo.size.toFloat()).coerceAtMost(1.0f)
    }
    
    private fun getDeviceContactInfo(phoneNumber: String): Map<String, Any>? {
        // Mock implementation - would integrate with Android ContactsContract
        return null
    }
    
    private fun getCallHistoryInfo(phoneNumber: String): Map<String, Any>? {
        // Mock implementation - would integrate with Android CallLog
        return null
    }
    
    private fun generateContactId(): String {
        return "contact_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    override suspend fun shutdown(): Result<Unit> = try {
        mcpConnection?.close()
        _isActive.value = false
        Log.i("ContactManagementAgent", "Contact Management Agent shut down successfully")
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Supporting data classes
data class ContactInfo(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val email: String?,
    val address: String?,
    val source: String,
    val callHistory: List<CallRecord> = emptyList(),
    val smsHistory: List<SMSRecord> = emptyList(),
    val createdAt: Long,
    val lastContact: Long,
    val contactFrequency: Int,
    val projectAssociations: List<String>,
    val tags: List<String>,
    val leadScore: Float
)

data class CallRecord(
    val phoneNumber: String,
    val duration: Long,
    val timestamp: Long,
    val type: String
)

data class SMSRecord(
    val phoneNumber: String,
    val content: String,
    val timestamp: Long,
    val type: String
)