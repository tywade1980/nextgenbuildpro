package com.nextgenbuildpro.core.service

import com.nextgenbuildpro.shared.*
import com.nextgenbuildpro.core.repository.KnowledgeBaseRepository
import com.nextgenbuildpro.core.agent.LearningAgentImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.content.Context
import android.util.Log
import java.time.LocalDateTime

/**
 * Central manager for the learning system
 * Coordinates knowledge base, workflow monitoring, and automation suggestions
 */
class LearningSystemManager(
    private val context: Context
) {
    companion object {
        private const val TAG = "LearningSystemManager"
        private const val LEARNING_CYCLE_INTERVAL_MS = 300000L // 5 minutes
        private const val PATTERN_ANALYSIS_INTERVAL_MS = 600000L // 10 minutes
    }
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mutex = Mutex()
    
    // Core components
    private val knowledgeBaseRepository = KnowledgeBaseRepository()
    private val workflowMonitorService = WorkflowMonitorService(knowledgeBaseRepository)
    private val automationSuggestionService = AutomationSuggestionService(knowledgeBaseRepository)
    
    // Learning agents registry
    private val learningAgents = mutableListOf<LearningAgent>()
    
    // System state
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _learningEnabled = MutableStateFlow(false)
    val learningEnabled: StateFlow<Boolean> = _learningEnabled.asStateFlow()
    
    /**
     * Initialize the learning system
     */
    suspend fun initialize(): Result<Unit> = try {
        mutex.withLock {
            Log.i(TAG, "Initializing Learning System...")
            
            // Initialize knowledge base repository
            knowledgeBaseRepository.initialize().getOrThrow()
            
            // Initialize automation suggestion service
            automationSuggestionService.initialize().getOrThrow()
            
            // Start workflow monitoring
            workflowMonitorService.startMonitoring().getOrThrow()
            
            _isInitialized.value = true
            
            Log.i(TAG, "Learning System initialized successfully")
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Learning System", e)
        Result.failure(e)
    }
    
    /**
     * Start the learning system
     */
    suspend fun startLearning(): Result<Unit> = try {
        if (!_isInitialized.value) {
            return Result.failure(Exception("Learning system not initialized"))
        }
        
        mutex.withLock {
            _learningEnabled.value = true
            
            // Start learning cycle
            startLearningCycle()
            
            // Start pattern analysis cycle
            startPatternAnalysisCycle()
            
            Log.i(TAG, "Learning system started")
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to start learning system", e)
        Result.failure(e)
    }
    
    /**
     * Stop the learning system
     */
    suspend fun stopLearning(): Result<Unit> = try {
        mutex.withLock {
            _learningEnabled.value = false
            workflowMonitorService.stopMonitoring()
            
            Log.i(TAG, "Learning system stopped")
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to stop learning system", e)
        Result.failure(e)
    }
    
    /**
     * Register a learning agent
     */
    suspend fun registerLearningAgent(agent: LearningAgent): Result<Unit> = try {
        mutex.withLock {
            learningAgents.add(agent)
            Log.i(TAG, "Registered learning agent: ${agent.specialization}")
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to register learning agent", e)
        Result.failure(e)
    }
    
    /**
     * Unregister a learning agent
     */
    suspend fun unregisterLearningAgent(agentId: String): Result<Unit> = try {
        mutex.withLock {
            learningAgents.removeIf { it.agentId == agentId }
            Log.i(TAG, "Unregistered learning agent: $agentId")
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to unregister learning agent", e)
        Result.failure(e)
    }
    
    /**
     * Add knowledge entry to the knowledge base
     */
    suspend fun addKnowledge(
        title: String,
        content: String,
        category: KnowledgeCategory,
        tags: List<String> = emptyList(),
        sourceType: KnowledgeSourceType = KnowledgeSourceType.USER_INPUT
    ): Result<KnowledgeEntry> = try {
        val entry = KnowledgeEntry(
            title = title,
            content = content,
            category = category,
            tags = tags,
            sourceType = sourceType,
            createdAt = LocalDateTime.now()
        )
        
        knowledgeBaseRepository.addKnowledgeEntry(entry).getOrThrow()
        
        // Notify learning agents of new knowledge
        notifyAgentsOfNewKnowledge(listOf(entry))
        
        Log.i(TAG, "Added knowledge entry: $title")
        Result.success(entry)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to add knowledge entry", e)
        Result.failure(e)
    }
    
    /**
     * Search knowledge base
     */
    suspend fun searchKnowledge(
        query: String,
        categories: List<KnowledgeCategory> = emptyList(),
        tags: List<String> = emptyList()
    ): Result<List<KnowledgeEntry>> = try {
        knowledgeBaseRepository.searchKnowledge(query, categories, tags)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to search knowledge", e)
        Result.failure(e)
    }
    
    /**
     * Record a user action for workflow monitoring
     */
    suspend fun recordUserAction(
        userId: String,
        action: String,
        agentType: AgentType? = null,
        context: Map<String, Any> = emptyMap()
    ): Result<Unit> = try {
        workflowMonitorService.recordUserAction(userId, action, agentType, context)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to record user action", e)
        Result.failure(e)
    }
    
    /**
     * Start a workflow session
     */
    suspend fun startWorkflowSession(
        userId: String,
        workflowName: String,
        context: Map<String, Any> = emptyMap()
    ): Result<String> = try {
        workflowMonitorService.startWorkflowSession(userId, workflowName, context)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to start workflow session", e)
        Result.failure(e)
    }
    
    /**
     * Complete a workflow session
     */
    suspend fun completeWorkflowSession(
        sessionId: String,
        success: Boolean = true,
        result: Map<String, Any> = emptyMap()
    ): Result<Unit> = try {
        workflowMonitorService.completeWorkflowSession(sessionId, success, result)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to complete workflow session", e)
        Result.failure(e)
    }
    
    /**
     * Get pending automation suggestions
     */
    suspend fun getPendingSuggestions(): Result<List<AutomationSuggestion>> = try {
        automationSuggestionService.getPendingSuggestions()
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get pending suggestions", e)
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
        automationSuggestionService.approveSuggestion(suggestionId, approverName, comments)
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
        automationSuggestionService.rejectSuggestion(suggestionId, reviewerName, reason)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to reject suggestion", e)
        Result.failure(e)
    }
    
    /**
     * Get learning system statistics
     */
    suspend fun getLearningStats(): Result<SystemLearningStats> = try {
        val knowledgeCount = knowledgeBaseRepository.knowledgeEntries.value.size
        val patternCount = knowledgeBaseRepository.workflowPatterns.value.size
        val suggestionStats = automationSuggestionService.getSuggestionStats().getOrThrow()
        val workflowStats = workflowMonitorService.getWorkflowStats().getOrThrow()
        
        val agentStats = learningAgents.map { agent ->
            agent.getLearningStats().getOrNull()
        }.filterNotNull()
        
        val stats = SystemLearningStats(
            knowledgeEntries = knowledgeCount,
            workflowPatterns = patternCount,
            bottlenecksDetected = knowledgeBaseRepository.getHighImpactBottlenecks(0.0).getOrNull()?.size ?: 0,
            totalSuggestions = suggestionStats.totalSuggestions,
            implementedSuggestions = suggestionStats.implementedSuggestions,
            approvalRate = suggestionStats.approvalRate,
            totalTimeSavings = suggestionStats.totalTimeSavings,
            learningAgents = learningAgents.size,
            totalWorkflowExecutions = workflowStats.totalWorkflowExecutions,
            automationReadyPatterns = workflowStats.automationReadyPatterns,
            averageWorkflowDuration = workflowStats.averageWorkflowDuration
        )
        
        Result.success(stats)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get learning stats", e)
        Result.failure(e)
    }
    
    /**
     * Generate learning system report
     */
    suspend fun generateReport(): Result<String> = try {
        val stats = getLearningStats().getOrThrow()
        val suggestionReport = automationSuggestionService.generateSuggestionReport().getOrThrow()
        
        val report = buildString {
            appendLine("=== Learning System Report ===")
            appendLine("Generated: ${LocalDateTime.now()}")
            appendLine()
            appendLine("System Statistics:")
            appendLine("- Knowledge Entries: ${stats.knowledgeEntries}")
            appendLine("- Workflow Patterns: ${stats.workflowPatterns}")
            appendLine("- Bottlenecks Detected: ${stats.bottlenecksDetected}")
            appendLine("- Learning Agents: ${stats.learningAgents}")
            appendLine("- Total Workflow Executions: ${stats.totalWorkflowExecutions}")
            appendLine("- Automation Ready Patterns: ${stats.automationReadyPatterns}")
            appendLine("- Average Workflow Duration: ${stats.averageWorkflowDuration / 1000}s")
            appendLine()
            append(suggestionReport)
        }
        
        Result.success(report)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to generate report", e)
        Result.failure(e)
    }
    
    // ===== PRIVATE METHODS =====
    
    private fun startLearningCycle() {
        scope.launch {
            while (_learningEnabled.value) {
                try {
                    performLearningCycle()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in learning cycle", e)
                }
                delay(LEARNING_CYCLE_INTERVAL_MS)
            }
        }
    }
    
    private fun startPatternAnalysisCycle() {
        scope.launch {
            while (_learningEnabled.value) {
                try {
                    performPatternAnalysis()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in pattern analysis", e)
                }
                delay(PATTERN_ANALYSIS_INTERVAL_MS)
            }
        }
    }
    
    private suspend fun performLearningCycle() {
        Log.d(TAG, "Performing learning cycle...")
        
        // Get recent knowledge entries
        val recentKnowledge = knowledgeBaseRepository.knowledgeEntries.value
            .sortedByDescending { it.createdAt }
            .take(100)
        
        // Notify all learning agents
        notifyAgentsOfNewKnowledge(recentKnowledge)
    }
    
    private suspend fun performPatternAnalysis() {
        Log.d(TAG, "Performing pattern analysis...")
        
        // Detect workflow patterns
        workflowMonitorService.detectPatterns()
        
        // Identify bottlenecks
        workflowMonitorService.identifyBottlenecks()
        
        // Get metrics for analysis
        val metrics = knowledgeBaseRepository.getMetrics(limit = 500).getOrNull() ?: emptyList()
        
        // Have learning agents analyze patterns
        for (agent in learningAgents) {
            try {
                val patterns = agent.identifyPatterns(metrics).getOrNull() ?: emptyList()
                
                if (patterns.isNotEmpty()) {
                    // Detect bottlenecks in identified patterns
                    val bottlenecks = agent.detectBottlenecks(patterns).getOrNull() ?: emptyList()
                    
                    // Generate automation suggestions
                    agent.generateAutomationSuggestions(patterns, bottlenecks)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in pattern analysis for agent ${agent.agentId}", e)
            }
        }
    }
    
    private suspend fun notifyAgentsOfNewKnowledge(entries: List<KnowledgeEntry>) {
        for (agent in learningAgents) {
            try {
                agent.learnFromKnowledge(entries)
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying agent ${agent.agentId} of new knowledge", e)
            }
        }
    }
    
    /**
     * Shutdown the learning system
     */
    suspend fun shutdown(): Result<Unit> = try {
        stopLearning()
        
        // Shutdown all learning agents
        for (agent in learningAgents) {
            agent.shutdown()
        }
        
        Log.i(TAG, "Learning system shutdown complete")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to shutdown learning system", e)
        Result.failure(e)
    }
}

/**
 * System-wide learning statistics
 */
data class SystemLearningStats(
    val knowledgeEntries: Int,
    val workflowPatterns: Int,
    val bottlenecksDetected: Int,
    val totalSuggestions: Int,
    val implementedSuggestions: Int,
    val approvalRate: Double,
    val totalTimeSavings: Long,
    val learningAgents: Int,
    val totalWorkflowExecutions: Int,
    val automationReadyPatterns: Int,
    val averageWorkflowDuration: Double
)
