package com.nextgenbuildpro.orchestrator

/**
 * Kotlin representation of wade-global-state/wade_global_state.json.
 * Mutable so WgsRepository and AgentDispatcher can update fields in-place
 * before the final save() call.
 */
data class WadeGlobalState(
    var currentUser: String = "Tyler Wade",
    var activeContext: String = "general",
    var conversationHistory: MutableList<ConversationTurn> = mutableListOf(),
    var memory: MutableMap<String, String> = mutableMapOf(),
    var activeCalls: MutableList<CallRecord> = mutableListOf(),
    var scheduledJobs: MutableList<JobRecord> = mutableListOf(),
    var crmContacts: MutableList<CrmContact> = mutableListOf(),
    var agentStatus: MutableMap<String, String> = mutableMapOf(),
    var lastUpdated: Long = System.currentTimeMillis()
)

data class ConversationTurn(
    val role: String,      // "user" | "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CallRecord(
    val callId: String,
    val callerNumber: String,
    val startTime: Long,
    var endTime: Long? = null,
    var summary: String = ""
)

data class JobRecord(
    val jobId: String,
    val clientName: String,
    val description: String,
    val scheduledTime: Long,
    var status: String = "pending"
)

data class CrmContact(
    val contactId: String,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val notes: String = ""
)
