package com.nextgenbuildpro.core.service

import com.nextgenbuildpro.shared.*
import com.nextgenbuildpro.core.repository.KnowledgeBaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.util.Log
import java.time.LocalDateTime

/**
 * Service for presenting automation suggestions to users for approval
 * Manages the human-in-the-loop workflow for automation adoption
 */
class AutomationSuggestionService(
    private val knowledgeBaseRepository: KnowledgeBaseRepository
) {
    companion object {
        private const val TAG = "AutomationSuggestionService"
    }
    
    private val mutex = Mutex()
    
    private val _activeSuggestions = MutableStateFlow<List<AutomationSuggestion>>(emptyList())
    val activeSuggestions: StateFlow<List<AutomationSuggestion>> = _activeSuggestions.asStateFlow()
    
    /**
     * Initialize the service
     */
    suspend fun initialize(): Result<Unit> = try {
        loadActiveSuggestions()
        Log.i(TAG, "AutomationSuggestionService initialized")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize service", e)
        Result.failure(e)
    }
    
    /**
     * Get pending suggestions for user review
     */
    suspend fun getPendingSuggestions(): Result<List<AutomationSuggestion>> = try {
        val suggestions = knowledgeBaseRepository.getPendingSuggestions().getOrNull() ?: emptyList()
        Result.success(suggestions)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get pending suggestions", e)
        Result.failure(e)
    }
    
    /**
     * Get suggestions filtered by risk level
     */
    suspend fun getSuggestionsByRiskLevel(riskLevel: RiskLevel): Result<List<AutomationSuggestion>> = try {
        val suggestions = _activeSuggestions.value.filter { it.riskLevel == riskLevel }
        Result.success(suggestions)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get suggestions by risk level", e)
        Result.failure(e)
    }
    
    /**
     * Get high-confidence suggestions (confidence >= 0.8)
     */
    suspend fun getHighConfidenceSuggestions(): Result<List<AutomationSuggestion>> = try {
        val suggestions = _activeSuggestions.value.filter { 
            it.confidenceScore >= 0.8 && it.status == SuggestionStatus.PENDING
        }.sortedByDescending { it.confidenceScore }
        
        Result.success(suggestions)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get high confidence suggestions", e)
        Result.failure(e)
    }
    
    /**
     * Approve an automation suggestion
     */
    suspend fun approveSuggestion(
        suggestionId: EntityId,
        approverName: String,
        comments: String? = null
    ): Result<AutomationSuggestion> = try {
        mutex.withLock {
            val updated = knowledgeBaseRepository.updateSuggestionStatus(
                suggestionId = suggestionId,
                status = SuggestionStatus.APPROVED,
                reviewedBy = approverName,
                comments = comments
            ).getOrThrow()
            
            updateActiveSuggestions(updated)
            
            Log.i(TAG, "Approved suggestion: ${updated.title}")
            
            // Generate knowledge entry documenting the approval
            createApprovalKnowledgeEntry(updated, approverName)
            
            Result.success(updated)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to approve suggestion", e)
        Result.failure(e)
    }
    
    /**
     * Reject an automation suggestion
     */
    suspend fun rejectSuggestion(
        suggestionId: EntityId,
        reviewerName: String,
        reason: String
    ): Result<AutomationSuggestion> = try {
        mutex.withLock {
            val updated = knowledgeBaseRepository.updateSuggestionStatus(
                suggestionId = suggestionId,
                status = SuggestionStatus.REJECTED,
                reviewedBy = reviewerName,
                comments = reason
            ).getOrThrow()
            
            updateActiveSuggestions(updated)
            
            Log.i(TAG, "Rejected suggestion: ${updated.title}")
            
            // Learn from rejection to improve future suggestions
            createRejectionKnowledgeEntry(updated, reviewerName, reason)
            
            Result.success(updated)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to reject suggestion", e)
        Result.failure(e)
    }
    
    /**
     * Mark suggestion as under review
     */
    suspend fun markUnderReview(
        suggestionId: EntityId,
        reviewerName: String
    ): Result<AutomationSuggestion> = try {
        mutex.withLock {
            val updated = knowledgeBaseRepository.updateSuggestionStatus(
                suggestionId = suggestionId,
                status = SuggestionStatus.UNDER_REVIEW,
                reviewedBy = reviewerName,
                comments = "Under review by $reviewerName"
            ).getOrThrow()
            
            updateActiveSuggestions(updated)
            
            Result.success(updated)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to mark suggestion under review", e)
        Result.failure(e)
    }
    
    /**
     * Mark suggestion as implemented
     */
    suspend fun markImplemented(
        suggestionId: EntityId,
        implementerName: String,
        implementationNotes: String? = null
    ): Result<AutomationSuggestion> = try {
        mutex.withLock {
            val updated = knowledgeBaseRepository.updateSuggestionStatus(
                suggestionId = suggestionId,
                status = SuggestionStatus.IMPLEMENTED,
                reviewedBy = implementerName,
                comments = implementationNotes ?: "Implementation completed"
            ).getOrThrow()
            
            updateActiveSuggestions(updated)
            
            Log.i(TAG, "Marked suggestion as implemented: ${updated.title}")
            
            // Document successful implementation
            createImplementationKnowledgeEntry(updated, implementerName)
            
            Result.success(updated)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to mark suggestion as implemented", e)
        Result.failure(e)
    }
    
    /**
     * Roll back an implemented suggestion
     */
    suspend fun rollbackSuggestion(
        suggestionId: EntityId,
        rolledBackBy: String,
        reason: String
    ): Result<AutomationSuggestion> = try {
        mutex.withLock {
            val updated = knowledgeBaseRepository.updateSuggestionStatus(
                suggestionId = suggestionId,
                status = SuggestionStatus.ROLLED_BACK,
                reviewedBy = rolledBackBy,
                comments = "Rolled back: $reason"
            ).getOrThrow()
            
            updateActiveSuggestions(updated)
            
            Log.i(TAG, "Rolled back suggestion: ${updated.title}")
            
            // Learn from rollback to improve future suggestions
            createRollbackKnowledgeEntry(updated, rolledBackBy, reason)
            
            Result.success(updated)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to rollback suggestion", e)
        Result.failure(e)
    }
    
    /**
     * Get suggestion statistics
     */
    suspend fun getSuggestionStats(): Result<SuggestionStats> = try {
        val allSuggestions = knowledgeBaseRepository.automationSuggestions.value
        
        val stats = SuggestionStats(
            totalSuggestions = allSuggestions.size,
            pendingSuggestions = allSuggestions.count { it.status == SuggestionStatus.PENDING },
            approvedSuggestions = allSuggestions.count { it.status == SuggestionStatus.APPROVED },
            rejectedSuggestions = allSuggestions.count { it.status == SuggestionStatus.REJECTED },
            implementedSuggestions = allSuggestions.count { it.status == SuggestionStatus.IMPLEMENTED },
            rolledBackSuggestions = allSuggestions.count { it.status == SuggestionStatus.ROLLED_BACK },
            averageConfidenceScore = if (allSuggestions.isNotEmpty()) {
                allSuggestions.map { it.confidenceScore }.average()
            } else {
                0.0
            },
            totalTimeSavings = allSuggestions
                .filter { it.status == SuggestionStatus.IMPLEMENTED }
                .sumOf { it.estimatedTimeSavings },
            approvalRate = if (allSuggestions.isNotEmpty()) {
                allSuggestions.count { it.status == SuggestionStatus.APPROVED || it.status == SuggestionStatus.IMPLEMENTED }.toDouble() / allSuggestions.size
            } else {
                0.0
            }
        )
        
        Result.success(stats)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get suggestion stats", e)
        Result.failure(e)
    }
    
    /**
     * Generate a summary report of automation suggestions
     */
    suspend fun generateSuggestionReport(): Result<String> = try {
        val stats = getSuggestionStats().getOrThrow()
        val pending = getPendingSuggestions().getOrThrow()
        
        val report = buildString {
            appendLine("=== Automation Suggestion Report ===")
            appendLine()
            appendLine("Summary Statistics:")
            appendLine("- Total Suggestions: ${stats.totalSuggestions}")
            appendLine("- Pending: ${stats.pendingSuggestions}")
            appendLine("- Approved: ${stats.approvedSuggestions}")
            appendLine("- Implemented: ${stats.implementedSuggestions}")
            appendLine("- Rejected: ${stats.rejectedSuggestions}")
            appendLine("- Rolled Back: ${stats.rolledBackSuggestions}")
            appendLine("- Approval Rate: ${(stats.approvalRate * 100).format(1)}%")
            appendLine("- Average Confidence: ${(stats.averageConfidenceScore * 100).format(1)}%")
            appendLine("- Total Time Savings: ${stats.totalTimeSavings / 1000}s")
            appendLine()
            
            if (pending.isNotEmpty()) {
                appendLine("Pending Suggestions:")
                pending.take(5).forEach { suggestion ->
                    appendLine("- ${suggestion.title}")
                    appendLine("  Confidence: ${(suggestion.confidenceScore * 100).format(1)}%")
                    appendLine("  Risk Level: ${suggestion.riskLevel}")
                    appendLine("  Est. Time Savings: ${suggestion.estimatedTimeSavings / 1000}s per execution")
                    appendLine()
                }
                
                if (pending.size > 5) {
                    appendLine("... and ${pending.size - 5} more pending suggestions")
                }
            }
        }
        
        Result.success(report)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to generate suggestion report", e)
        Result.failure(e)
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    private suspend fun loadActiveSuggestions() {
        val suggestions = knowledgeBaseRepository.getPendingSuggestions().getOrNull() ?: emptyList()
        _activeSuggestions.value = suggestions
    }
    
    private fun updateActiveSuggestions(updated: AutomationSuggestion) {
        _activeSuggestions.value = _activeSuggestions.value.map {
            if (it.id == updated.id) updated else it
        }
    }
    
    private suspend fun createApprovalKnowledgeEntry(
        suggestion: AutomationSuggestion,
        approverName: String
    ) {
        val entry = KnowledgeEntry(
            title = "Approved Automation: ${suggestion.title}",
            content = buildString {
                appendLine("Automation suggestion approved by $approverName")
                appendLine()
                appendLine("Description: ${suggestion.description}")
                appendLine("Confidence Score: ${suggestion.confidenceScore}")
                appendLine("Risk Level: ${suggestion.riskLevel}")
                appendLine("Est. Time Savings: ${suggestion.estimatedTimeSavings}ms")
                appendLine()
                appendLine("Benefits:")
                suggestion.benefits.forEach { benefit ->
                    appendLine("- $benefit")
                }
            },
            category = KnowledgeCategory.WORKFLOW_PATTERNS,
            tags = listOf("automation", "approved", "suggestion"),
            sourceType = KnowledgeSourceType.SYSTEM_OBSERVATION,
            verified = true,
            verifiedBy = approverName
        )
        
        knowledgeBaseRepository.addKnowledgeEntry(entry)
    }
    
    private suspend fun createRejectionKnowledgeEntry(
        suggestion: AutomationSuggestion,
        reviewerName: String,
        reason: String
    ) {
        val entry = KnowledgeEntry(
            title = "Rejected Automation: ${suggestion.title}",
            content = buildString {
                appendLine("Automation suggestion rejected by $reviewerName")
                appendLine("Reason: $reason")
                appendLine()
                appendLine("Original Description: ${suggestion.description}")
                appendLine("Confidence Score: ${suggestion.confidenceScore}")
                appendLine("Risk Level: ${suggestion.riskLevel}")
            },
            category = KnowledgeCategory.WORKFLOW_PATTERNS,
            tags = listOf("automation", "rejected", "learning"),
            sourceType = KnowledgeSourceType.AGENT_LEARNING,
            metadata = mapOf(
                "suggestionId" to suggestion.id,
                "rejectionReason" to reason
            )
        )
        
        knowledgeBaseRepository.addKnowledgeEntry(entry)
    }
    
    private suspend fun createImplementationKnowledgeEntry(
        suggestion: AutomationSuggestion,
        implementerName: String
    ) {
        val entry = KnowledgeEntry(
            title = "Implemented Automation: ${suggestion.title}",
            content = buildString {
                appendLine("Automation successfully implemented by $implementerName")
                appendLine()
                appendLine("Description: ${suggestion.description}")
                appendLine("Implementation Steps:")
                suggestion.implementationSteps.forEach { step ->
                    appendLine("- $step")
                }
            },
            category = KnowledgeCategory.BEST_PRACTICES,
            tags = listOf("automation", "implemented", "success"),
            sourceType = KnowledgeSourceType.SYSTEM_OBSERVATION,
            verified = true,
            verifiedBy = implementerName,
            relevanceScore = 1.0
        )
        
        knowledgeBaseRepository.addKnowledgeEntry(entry)
    }
    
    private suspend fun createRollbackKnowledgeEntry(
        suggestion: AutomationSuggestion,
        rolledBackBy: String,
        reason: String
    ) {
        val entry = KnowledgeEntry(
            title = "Rolled Back Automation: ${suggestion.title}",
            content = buildString {
                appendLine("Automation rolled back by $rolledBackBy")
                appendLine("Reason: $reason")
                appendLine()
                appendLine("Lessons learned:")
                appendLine("- Monitor automation closely after implementation")
                appendLine("- Consider additional testing before deployment")
                appendLine("- Review risk assessment procedures")
            },
            category = KnowledgeCategory.WORKFLOW_PATTERNS,
            tags = listOf("automation", "rollback", "learning", "risk"),
            sourceType = KnowledgeSourceType.AGENT_LEARNING,
            metadata = mapOf(
                "suggestionId" to suggestion.id,
                "rollbackReason" to reason
            ),
            relevanceScore = 0.9
        )
        
        knowledgeBaseRepository.addKnowledgeEntry(entry)
    }
    
    private fun Double.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
}

/**
 * Statistics for automation suggestions
 */
data class SuggestionStats(
    val totalSuggestions: Int,
    val pendingSuggestions: Int,
    val approvedSuggestions: Int,
    val rejectedSuggestions: Int,
    val implementedSuggestions: Int,
    val rolledBackSuggestions: Int,
    val averageConfidenceScore: Double,
    val totalTimeSavings: Long,
    val approvalRate: Double
)
