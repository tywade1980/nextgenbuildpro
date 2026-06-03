package com.nextgenbuildpro.hermes

import com.nextgenbuildpro.hermes.models.TaskChunk
import com.nextgenbuildpro.orchestrator.WadeGlobalState
import java.util.UUID

/**
 * Converted from wade-global-state/hermes.py openclaw_chunk().
 * Splits an incoming request into typed TaskChunks routed to the
 * appropriate agent based on keyword/regex matching.
 */
object OpenClawChunker {

    private data class RoutingRule(
        val agentType: String,
        val keywords: List<String>,
        val priority: Int = 0
    )

    private val rules = listOf(
        RoutingRule("voice",        listOf("speak", "say", "voice", "audio", "listen", "hear"), priority = 10),
        RoutingRule("calendar",     listOf("schedule", "meeting", "appointment", "calendar", "remind", "event"), priority = 8),
        RoutingRule("construction", listOf("job", "site", "build", "permit", "contractor", "estimate", "bid", "crew"), priority = 8),
        RoutingRule("crm",          listOf("client", "contact", "lead", "customer", "follow up", "proposal"), priority = 7),
        RoutingRule("phone",        listOf("call", "text", "sms", "phone", "dial", "voicemail", "receptionist"), priority = 9),
        RoutingRule("search",       listOf("search", "find", "lookup", "google", "research", "browse"), priority = 5),
        RoutingRule("memory",       listOf("remember", "forget", "recall", "note", "save", "store"), priority = 6),
        RoutingRule("general",      listOf(), priority = 0)
    )

    fun chunk(request: String, wgs: WadeGlobalState): List<TaskChunk> {
        val lower = request.lowercase()
        val matched = mutableListOf<TaskChunk>()
        val usedTypes = mutableSetOf<String>()

        for (rule in rules.sortedByDescending { it.priority }) {
            if (rule.agentType == "general") continue
            val hit = rule.keywords.any { lower.contains(it) }
            if (hit && !usedTypes.contains(rule.agentType)) {
                usedTypes.add(rule.agentType)
                matched.add(
                    TaskChunk(
                        id = UUID.randomUUID().toString(),
                        agentType = rule.agentType,
                        content = request,
                        priority = rule.priority,
                        metadata = mapOf("user" to wgs.currentUser, "context" to wgs.activeContext)
                    )
                )
            }
        }

        // Always include at least a general chunk
        if (matched.isEmpty()) {
            matched.add(
                TaskChunk(
                    id = UUID.randomUUID().toString(),
                    agentType = "general",
                    content = request,
                    priority = 0,
                    metadata = mapOf("user" to wgs.currentUser, "context" to wgs.activeContext)
                )
            )
        }

        return matched
    }
}
