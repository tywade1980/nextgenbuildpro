package com.nextgenbuildpro.hermes

import com.nextgenbuildpro.hermes.models.AgentResult
import com.nextgenbuildpro.hermes.models.TaskChunk

data class AuditResult(
    val passed: Boolean,
    val issues: List<String> = emptyList(),
    val confidence: Float = 1.0f
)

/**
 * Converted from wade-global-state/hermes.py audit_results().
 * Validates agent outputs before they are surfaced to the user.
 */
object AuditAgent {

    fun audit(chunks: List<TaskChunk>, results: List<AgentResult>, originalRequest: String): AuditResult {
        val issues = mutableListOf<String>()

        // Check every chunk got a result
        val resultIds = results.map { it.chunkId }.toSet()
        for (chunk in chunks) {
            if (!resultIds.contains(chunk.id)) {
                issues.add("No result for chunk ${chunk.id} (agent=${chunk.agentType})")
            }
        }

        // Check for agent-reported failures
        val failures = results.filter { !it.success }
        for (f in failures) {
            issues.add("Agent ${f.agentType} failed: ${f.errorMessage ?: "unknown error"}")
        }

        // Basic relevance check — at least one result must reference content from the original request
        val keywords = originalRequest.lowercase().split(" ").filter { it.length > 3 }
        val anyRelevant = results.any { r ->
            keywords.any { kw -> r.output.lowercase().contains(kw) }
        }
        if (results.isNotEmpty() && !anyRelevant) {
            issues.add("No result appears relevant to the original request")
        }

        val confidence = if (results.isEmpty()) 0f
                         else results.count { it.success }.toFloat() / results.size.toFloat()

        return AuditResult(
            passed = issues.isEmpty(),
            issues = issues,
            confidence = confidence
        )
    }
}
