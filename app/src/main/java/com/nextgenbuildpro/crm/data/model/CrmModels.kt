package com.nextgenbuildpro.crm.data.model

import com.nextgenbuildpro.core.Address

/**
 * Data models for the CRM module
 */

/**
 * Represents a lead in the CRM system
 */
data class Lead(
    val id: String,
    val name: String,
    val phone: String,
    val email: String?,
    val address: String,
    val projectType: String,
    val phase: LeadPhase,
    val notes: String,
    val urgency: String,
    val source: String,
    val intakeTimestamp: Long,
    val attachments: List<MediaAttachment> = emptyList()
)

/**
 * Represents a call record in the CRM system
 */
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

/**
 * Represents a message record in the CRM system
 */
data class MessageRecord(
    val id: String,
    val leadId: String,
    val leadName: String,
    val phoneNumber: String,
    val timestamp: String,
    val content: String,
    val type: String // Incoming or Outgoing
)

/**
 * Represents a scheduled call in the CRM system
 */
data class ScheduledCall(
    val id: String,
    val leadId: String,
    val leadName: String,
    val phoneNumber: String,
    val scheduledTime: String,
    val notes: String,
    val status: String // Scheduled, Completed, Missed
)

/**
 * Represents a follow-up in the CRM system
 */
data class FollowUp(
    val id: String,
    val leadId: String,
    val leadName: String,
    val type: String, // Call, Email, SMS, In-person
    val scheduledTime: String,
    val notes: String,
    val status: String // Scheduled, Completed, Missed
)

/**
 * Represents the communication history for a lead
 */
data class LeadCommunicationHistory(
    val leadId: String,
    val calls: List<CallRecord>,
    val messages: List<MessageRecord>,
    val scheduledCalls: List<ScheduledCall>,
    val followUps: List<FollowUp>
)

/**
 * Enum for lead status
 */
enum class LeadStatus(val displayName: String) {
    NEW("New"),
    CONTACTED("Contacted"),
    QUALIFIED("Qualified"),
    PROPOSAL_SENT("Proposal Sent"),
    NEGOTIATION("Negotiation"),
    WON("Won"),
    LOST("Lost"),
    ON_HOLD("On Hold")
}

/**
 * Enum for lead source
 */
enum class LeadSource(val displayName: String) {
    WEBSITE("Website"),
    REFERRAL("Referral"),
    SOCIAL_MEDIA("Social Media"),
    GOOGLE_ADS("Google Ads"),
    DIRECT_MAIL("Direct Mail"),
    TRADE_SHOW("Trade Show"),
    COLD_CALL("Cold Call"),
    OTHER("Other")
}
