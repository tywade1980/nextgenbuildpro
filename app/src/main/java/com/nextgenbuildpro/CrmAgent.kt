package com.nextgenbuildpro

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CRM Agent for NextGenBuildPro
 * 
 * Features:
 * - Customer relationship management
 * - Call scheduling and management
 * - Automated follow-ups
 * - Lead prioritization
 * - Communication history tracking
 * - Smart message templates
 */
class CrmAgent(private val context: Context) {

    // Maximum number of items to keep in history lists
    private val MAX_HISTORY_SIZE = 100

    // Cached date formatters for better performance
    private val timestampFormatter = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US)
    private val dateTimeFormatter = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US)
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    // State for tracking CRM data
    private val _leads = mutableStateOf<List<Lead>>(emptyList())
    private val _callHistory = mutableStateOf<List<CallRecord>>(emptyList())
    private val _messageHistory = mutableStateOf<List<MessageRecord>>(emptyList())
    private val _scheduledCalls = mutableStateOf<List<ScheduledCall>>(emptyList())
    private val _scheduledFollowUps = mutableStateOf<List<FollowUp>>(emptyList())

    // Public access to state
    val leads: State<List<Lead>> = _leads
    val callHistory: State<List<CallRecord>> = _callHistory
    val messageHistory: State<List<MessageRecord>> = _messageHistory
    val scheduledCalls: State<List<ScheduledCall>> = _scheduledCalls
    val scheduledFollowUps: State<List<FollowUp>> = _scheduledFollowUps

    init {
        // Initialize with sample data
        loadSampleData()
    }

    /**
     * Loads sample data for demonstration
     */
    private fun loadSampleData() {
        _leads.value = sampleLeads
        _callHistory.value = sampleCallHistory
        _messageHistory.value = sampleMessageHistory
        _scheduledCalls.value = sampleScheduledCalls
        _scheduledFollowUps.value = sampleFollowUps
    }

    /**
     * Makes a phone call to a lead
     */
    fun callLead(lead: Lead, permissionManager: PermissionManager) {
        if (lead.phone.isNotBlank()) {
            try {
                permissionManager.makePhoneCall(lead.phone)

                // Record the call
                val newCall = CallRecord(
                    id = "call_${System.currentTimeMillis()}",
                    leadId = lead.id,
                    leadName = lead.name,
                    phoneNumber = lead.phone,
                    timestamp = getCurrentTimestamp(),
                    duration = 0, // Will be updated when call ends
                    notes = "",
                    type = "Outgoing"
                )

                // Add new call to history and limit the size
                val updatedHistory = listOf(newCall) + _callHistory.value
                _callHistory.value = if (updatedHistory.size > MAX_HISTORY_SIZE) {
                    updatedHistory.take(MAX_HISTORY_SIZE)
                } else {
                    updatedHistory
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Failed to make call: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "No phone number available for this lead", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Sends an SMS to a lead
     */
    fun sendMessage(lead: Lead, message: String, permissionManager: PermissionManager) {
        if (lead.phone.isNotBlank() && message.isNotBlank()) {
            try {
                permissionManager.sendSms(lead.phone, message)

                // Record the message
                val newMessage = MessageRecord(
                    id = "msg_${System.currentTimeMillis()}",
                    leadId = lead.id,
                    leadName = lead.name,
                    phoneNumber = lead.phone,
                    timestamp = getCurrentTimestamp(),
                    content = message,
                    type = "Outgoing"
                )

                // Add new message to history and limit the size
                val updatedHistory = listOf(newMessage) + _messageHistory.value
                _messageHistory.value = if (updatedHistory.size > MAX_HISTORY_SIZE) {
                    updatedHistory.take(MAX_HISTORY_SIZE)
                } else {
                    updatedHistory
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Cannot send message: Missing phone number or message content", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Schedules a call with a lead
     */
    fun scheduleCall(lead: Lead, scheduledTime: String, notes: String) {
        val newScheduledCall = ScheduledCall(
            id = "scheduled_call_${System.currentTimeMillis()}",
            leadId = lead.id,
            leadName = lead.name,
            phoneNumber = lead.phone,
            scheduledTime = scheduledTime,
            notes = notes,
            status = "Scheduled"
        )

        // Add new scheduled call to list and limit the size
        val updatedCalls = listOf(newScheduledCall) + _scheduledCalls.value
        _scheduledCalls.value = if (updatedCalls.size > MAX_HISTORY_SIZE) {
            updatedCalls.take(MAX_HISTORY_SIZE)
        } else {
            updatedCalls
        }
        Toast.makeText(context, "Call scheduled with ${lead.name} for $scheduledTime", Toast.LENGTH_SHORT).show()
    }

    /**
     * Schedules a follow-up with a lead
     */
    fun scheduleFollowUp(lead: Lead, followUpType: String, scheduledTime: String, notes: String) {
        val newFollowUp = FollowUp(
            id = "followup_${System.currentTimeMillis()}",
            leadId = lead.id,
            leadName = lead.name,
            type = followUpType,
            scheduledTime = scheduledTime,
            notes = notes,
            status = "Scheduled"
        )

        // Add new follow-up to list and limit the size
        val updatedFollowUps = listOf(newFollowUp) + _scheduledFollowUps.value
        _scheduledFollowUps.value = if (updatedFollowUps.size > MAX_HISTORY_SIZE) {
            updatedFollowUps.take(MAX_HISTORY_SIZE)
        } else {
            updatedFollowUps
        }
        Toast.makeText(context, "$followUpType follow-up scheduled with ${lead.name} for $scheduledTime", Toast.LENGTH_SHORT).show()
    }

    /**
     * Updates a lead's status
     */
    fun updateLeadStatus(leadId: String, newStatus: String) {
        _leads.value = _leads.value.map { lead ->
            if (lead.id == leadId) {
                lead.copy(status = newStatus)
            } else {
                lead
            }
        }
    }

    /**
     * Adds a note to a lead
     */
    fun addLeadNote(leadId: String, note: String) {
        _leads.value = _leads.value.map { lead ->
            if (lead.id == leadId) {
                val updatedNotes = lead.notes + "\n" + getCurrentTimestamp() + ": " + note
                lead.copy(notes = updatedNotes.trim())
            } else {
                lead
            }
        }
    }

    /**
     * Gets a list of leads filtered by status
     */
    fun getLeadsByStatus(status: String): List<Lead> {
        return _leads.value.filter { it.status == status }
    }

    /**
     * Gets a list of today's scheduled calls
     */
    fun getTodaysCalls(): List<ScheduledCall> {
        try {
            // Get today's date at midnight for comparison
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val todayStart = calendar.time

            // Set to end of day
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            val todayEnd = calendar.time

            // Parse each scheduled call date and compare
            val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US)
            return _scheduledCalls.value.filter { call ->
                try {
                    val callDate = dateFormat.parse(call.scheduledTime)
                    callDate != null && callDate >= todayStart && callDate <= todayEnd
                } catch (e: Exception) {
                    android.util.Log.e("CrmAgent", "Error parsing date for call: ${e.message}")
                    // If we can't parse the date, use string comparison as fallback
                    val today = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date())
                    call.scheduledTime.startsWith(today)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CrmAgent", "Error getting today's calls: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Gets a list of today's follow-ups
     */
    fun getTodaysFollowUps(): List<FollowUp> {
        try {
            // Get today's date at midnight for comparison
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val todayStart = calendar.time

            // Set to end of day
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            val todayEnd = calendar.time

            // Parse each follow-up date and compare
            val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US)
            return _scheduledFollowUps.value.filter { followUp ->
                try {
                    val followUpDate = dateFormat.parse(followUp.scheduledTime)
                    followUpDate != null && followUpDate >= todayStart && followUpDate <= todayEnd
                } catch (e: Exception) {
                    android.util.Log.e("CrmAgent", "Error parsing date for follow-up: ${e.message}")
                    // If we can't parse the date, use string comparison as fallback
                    val today = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date())
                    followUp.scheduledTime.startsWith(today)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CrmAgent", "Error getting today's follow-ups: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Gets communication history for a specific lead
     */
    fun getLeadCommunicationHistory(leadId: String): LeadCommunicationHistory {
        val calls = _callHistory.value.filter { it.leadId == leadId }
        val messages = _messageHistory.value.filter { it.leadId == leadId }
        val scheduledCalls = _scheduledCalls.value.filter { it.leadId == leadId }
        val followUps = _scheduledFollowUps.value.filter { it.leadId == leadId }

        return LeadCommunicationHistory(
            leadId = leadId,
            calls = calls,
            messages = messages,
            scheduledCalls = scheduledCalls,
            followUps = followUps
        )
    }

    /**
     * Generates a personalized message template for a lead
     */
    fun generateMessageTemplate(lead: Lead, templateType: String): String {
        val template = when (templateType) {
            "follow_up" -> "Hi ${lead.name}, I'm following up on our conversation about your project. Would you like to schedule a time to discuss further?"
            "estimate" -> "Hi ${lead.name}, your estimate for the ${lead.projectType} project is ready. The total comes to $${lead.estimateAmount}. Please let me know if you have any questions."
            "appointment" -> "Hi ${lead.name}, this is a reminder about your appointment on ${lead.nextAppointment}. Please confirm if this still works for you."
            "project_update" -> "Hi ${lead.name}, your ${lead.projectType} project is now ${lead.projectProgress}% complete. We're on track to finish by ${lead.projectEndDate}."
            else -> "Hi ${lead.name}, thank you for choosing NextGenBuildPro. How can we help you today?"
        }

        return template
    }

    /**
     * Gets the current timestamp as a formatted string
     */
    private fun getCurrentTimestamp(): String {
        return try {
            timestampFormatter.format(Date())
        } catch (e: Exception) {
            android.util.Log.e("CrmAgent", "Error formatting timestamp: ${e.message}")
            // Fallback to a simple timestamp if formatting fails
            "${Date().time}"
        }
    }

    companion object {
        // Sample data
        private val sampleLeads = listOf(
            Lead(
                id = "lead_1",
                name = "John Smith",
                phone = "555-123-4567",
                email = "john.smith@example.com",
                address = "123 Main St, Anytown, USA",
                projectType = "Kitchen Renovation",
                status = "New",
                source = "Website",
                estimateAmount = "8,500.00",
                nextAppointment = "06/15/2023 10:00 AM",
                projectProgress = 0,
                projectEndDate = "08/30/2023",
                notes = "Initial consultation scheduled for 06/15/2023"
            ),
            Lead(
                id = "lead_2",
                name = "Sarah Johnson",
                phone = "555-234-5678",
                email = "sarah.johnson@example.com",
                address = "456 Oak Ave, Somewhere, USA",
                projectType = "Bathroom Remodel",
                status = "In Progress",
                source = "Referral",
                estimateAmount = "6,200.00",
                nextAppointment = "06/10/2023 2:00 PM",
                projectProgress = 25,
                projectEndDate = "07/15/2023",
                notes = "Approved estimate, scheduled start date for 06/20/2023"
            ),
            Lead(
                id = "lead_3",
                name = "Michael Brown",
                phone = "555-345-6789",
                email = "michael.brown@example.com",
                address = "789 Pine Rd, Nowhere, USA",
                projectType = "Deck Construction",
                status = "Estimate Sent",
                source = "Google Ads",
                estimateAmount = "4,800.00",
                nextAppointment = "",
                projectProgress = 0,
                projectEndDate = "",
                notes = "Sent estimate on 06/01/2023, awaiting response"
            )
        )

        private val sampleCallHistory = listOf(
            CallRecord(
                id = "call_1",
                leadId = "lead_1",
                leadName = "John Smith",
                phoneNumber = "555-123-4567",
                timestamp = "06/01/2023 14:30:22",
                duration = 325, // seconds
                notes = "Discussed kitchen renovation options, scheduled in-person consultation",
                type = "Outgoing"
            ),
            CallRecord(
                id = "call_2",
                leadId = "lead_2",
                leadName = "Sarah Johnson",
                phoneNumber = "555-234-5678",
                timestamp = "06/02/2023 10:15:45",
                duration = 412, // seconds
                notes = "Reviewed estimate details, answered questions about timeline",
                type = "Incoming"
            )
        )

        private val sampleMessageHistory = listOf(
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
            )
        )

        private val sampleScheduledCalls = listOf(
            ScheduledCall(
                id = "scheduled_call_1",
                leadId = "lead_3",
                leadName = "Michael Brown",
                phoneNumber = "555-345-6789",
                scheduledTime = "06/05/2023 11:00 AM",
                notes = "Follow up on estimate",
                status = "Scheduled"
            )
        )

        private val sampleFollowUps = listOf(
            FollowUp(
                id = "followup_1",
                leadId = "lead_2",
                leadName = "Sarah Johnson",
                type = "Email",
                scheduledTime = "06/08/2023 09:00 AM",
                notes = "Send project timeline and material options",
                status = "Scheduled"
            ),
            FollowUp(
                id = "followup_2",
                leadId = "lead_3",
                leadName = "Michael Brown",
                type = "SMS",
                scheduledTime = "06/07/2023 02:00 PM",
                notes = "Check if estimate was received and if there are any questions",
                status = "Scheduled"
            )
        )
    }
}

// Data classes for CRM
data class Lead(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val projectType: String,
    val status: String,
    val source: String,
    val estimateAmount: String,
    val nextAppointment: String,
    val projectProgress: Int,
    val projectEndDate: String,
    val notes: String
)

data class CallRecord(
    val id: String,
    val leadId: String,
    val leadName: String,
    val phoneNumber: String,
    val timestamp: String,
    val duration: Int, // in seconds
    val notes: String,
    val type: String // Incoming or Outgoing
)

data class MessageRecord(
    val id: String,
    val leadId: String,
    val leadName: String,
    val phoneNumber: String,
    val timestamp: String,
    val content: String,
    val type: String // Incoming or Outgoing
)

data class ScheduledCall(
    val id: String,
    val leadId: String,
    val leadName: String,
    val phoneNumber: String,
    val scheduledTime: String,
    val notes: String,
    val status: String // Scheduled, Completed, Missed
)

data class FollowUp(
    val id: String,
    val leadId: String,
    val leadName: String,
    val type: String, // Call, Email, SMS, In-person
    val scheduledTime: String,
    val notes: String,
    val status: String // Scheduled, Completed, Missed
)

data class LeadCommunicationHistory(
    val leadId: String,
    val calls: List<CallRecord>,
    val messages: List<MessageRecord>,
    val scheduledCalls: List<ScheduledCall>,
    val followUps: List<FollowUp>
)

/**
 * Composable function to use CRM agent in Compose UI
 */
@Composable
fun rememberCrmAgent(): CrmAgent {
    val context = LocalContext.current
    val crmAgent = remember { CrmAgent(context) }

    DisposableEffect(crmAgent) {
        onDispose {
            // Clean up if needed
        }
    }

    return crmAgent
}
