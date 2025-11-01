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
import java.time.Duration
import java.util.UUID

/**
 * Service for monitoring user workflows and system functions
 * Tracks user actions, detects patterns, identifies bottlenecks
 */
class WorkflowMonitorService(
    private val knowledgeBaseRepository: KnowledgeBaseRepository
) {
    companion object {
        private const val TAG = "WorkflowMonitorService"
        private const val PATTERN_DETECTION_THRESHOLD = 3 // Number of occurrences to consider a pattern
        private const val BOTTLENECK_THRESHOLD_MS = 5000L // 5 seconds
    }
    
    private val mutex = Mutex()
    
    // Current monitoring state
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()
    
    // Recent user actions for pattern detection
    private val recentActions = mutableListOf<UserAction>()
    private val maxRecentActions = 1000
    
    // Detected patterns
    private val detectedPatterns = mutableMapOf<String, WorkflowPattern>()
    
    // Active workflow sessions
    private val activeWorkflows = mutableMapOf<String, WorkflowSession>()
    
    /**
     * Start monitoring user workflows
     */
    suspend fun startMonitoring(): Result<Unit> = try {
        mutex.withLock {
            _isMonitoring.value = true
            Log.i(TAG, "Workflow monitoring started")
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to start monitoring", e)
        Result.failure(e)
    }
    
    /**
     * Stop monitoring user workflows
     */
    suspend fun stopMonitoring(): Result<Unit> = try {
        mutex.withLock {
            _isMonitoring.value = false
            Log.i(TAG, "Workflow monitoring stopped")
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to stop monitoring", e)
        Result.failure(e)
    }
    
    /**
     * Record a user action
     */
    suspend fun recordUserAction(
        userId: String,
        action: String,
        agentType: AgentType? = null,
        context: Map<String, Any> = emptyMap()
    ): Result<Unit> = try {
        if (!_isMonitoring.value) {
            return Result.success(Unit)
        }
        
        val userAction = UserAction(
            userId = userId,
            action = action,
            agentType = agentType,
            timestamp = LocalDateTime.now(),
            context = context
        )
        
        mutex.withLock {
            // Add to recent actions
            recentActions.add(userAction)
            if (recentActions.size > maxRecentActions) {
                recentActions.removeAt(0)
            }
            
            // Record metric
            recordActionMetric(userAction)
            
            // Update active workflow if exists
            updateActiveWorkflow(userAction)
            
            // Check for patterns periodically
            if (recentActions.size % 10 == 0) {
                detectPatterns()
            }
        }
        
        Log.d(TAG, "Recorded user action: $action")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to record user action", e)
        Result.failure(e)
    }
    
    /**
     * Start a new workflow session
     */
    suspend fun startWorkflowSession(
        userId: String,
        workflowName: String,
        context: Map<String, Any> = emptyMap()
    ): Result<String> = try {
        val sessionId = UUID.randomUUID().toString()
        
        val session = WorkflowSession(
            sessionId = sessionId,
            userId = userId,
            workflowName = workflowName,
            startTime = LocalDateTime.now(),
            context = context
        )
        
        mutex.withLock {
            activeWorkflows[sessionId] = session
        }
        
        Log.i(TAG, "Started workflow session: $workflowName")
        Result.success(sessionId)
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
        val session = mutex.withLock {
            activeWorkflows.remove(sessionId)
        } ?: return Result.failure(Exception("Session not found"))
        
        val duration = Duration.between(session.startTime, LocalDateTime.now())
        
        // Record workflow completion metric
        knowledgeBaseRepository.recordMetric(
            SystemMetrics(
                metricType = MetricType.WORKFLOW_DURATION,
                userId = session.userId,
                value = duration.toMillis().toDouble(),
                unit = "milliseconds",
                context = mapOf(
                    "workflowName" to session.workflowName,
                    "success" to success,
                    "stepCount" to session.steps.size
                )
            )
        )
        
        // Analyze for bottlenecks
        analyzeWorkflowBottlenecks(session, duration.toMillis())
        
        // Update or create workflow pattern
        updateWorkflowPattern(session, success, duration.toMillis())
        
        Log.i(TAG, "Completed workflow session: ${session.workflowName} in ${duration.toMillis()}ms")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to complete workflow session", e)
        Result.failure(e)
    }
    
    /**
     * Detect workflow patterns from recent actions
     */
    suspend fun detectPatterns(): Result<List<WorkflowPattern>> = try {
        mutex.withLock {
            // Group actions by user and similarity
            val actionSequences = groupActionSequences(recentActions)
            
            val patterns = mutableListOf<WorkflowPattern>()
            
            for ((sequenceKey, actions) in actionSequences) {
                if (actions.size >= PATTERN_DETECTION_THRESHOLD) {
                    val pattern = createPatternFromSequence(sequenceKey, actions)
                    detectedPatterns[pattern.id] = pattern
                    patterns.add(pattern)
                    
                    // Save to repository
                    knowledgeBaseRepository.saveWorkflowPattern(pattern)
                }
            }
            
            Log.i(TAG, "Detected ${patterns.size} workflow patterns")
            Result.success(patterns)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to detect patterns", e)
        Result.failure(e)
    }
    
    /**
     * Identify bottlenecks in workflows
     */
    suspend fun identifyBottlenecks(): Result<List<BottleneckAnalysis>> = try {
        val patterns = knowledgeBaseRepository.workflowPatterns.value
        val bottlenecks = mutableListOf<BottleneckAnalysis>()
        
        for (pattern in patterns) {
            // Analyze each step for potential bottlenecks
            for (step in pattern.steps) {
                if (step.averageDuration > BOTTLENECK_THRESHOLD_MS || step.failureRate > 0.1) {
                    val bottleneck = BottleneckAnalysis(
                        workflowId = pattern.id,
                        workflowName = pattern.name,
                        bottleneckType = determineBottleneckType(step),
                        location = "Step ${step.stepNumber}: ${step.action}",
                        description = "Step taking ${step.averageDuration}ms on average with ${step.failureRate * 100}% failure rate",
                        impactScore = calculateImpactScore(step, pattern),
                        frequencyCount = pattern.frequency,
                        averageDelay = step.averageDuration,
                        affectedUsers = pattern.userIds,
                        possibleCauses = suggestPossibleCauses(step),
                        suggestedFixes = suggestFixes(step)
                    )
                    
                    bottlenecks.add(bottleneck)
                    knowledgeBaseRepository.addBottleneck(bottleneck)
                }
            }
        }
        
        Log.i(TAG, "Identified ${bottlenecks.size} bottlenecks")
        Result.success(bottlenecks)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to identify bottlenecks", e)
        Result.failure(e)
    }
    
    /**
     * Get workflow statistics
     */
    suspend fun getWorkflowStats(): Result<WorkflowStats> = try {
        val patterns = knowledgeBaseRepository.workflowPatterns.value
        val metrics = knowledgeBaseRepository.getMetrics(
            metricType = MetricType.WORKFLOW_DURATION,
            limit = 500
        ).getOrNull() ?: emptyList()
        
        val stats = WorkflowStats(
            totalPatterns = patterns.size,
            totalWorkflowExecutions = patterns.sumOf { it.frequency },
            averageWorkflowDuration = if (metrics.isNotEmpty()) metrics.map { it.value }.average() else 0.0,
            automationReadyPatterns = patterns.count { it.automationReadiness >= 0.8 },
            mostFrequentWorkflow = patterns.maxByOrNull { it.frequency }?.name ?: "None"
        )
        
        Result.success(stats)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get workflow stats", e)
        Result.failure(e)
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    private suspend fun recordActionMetric(action: UserAction) {
        knowledgeBaseRepository.recordMetric(
            SystemMetrics(
                metricType = MetricType.USER_ACTION_COUNT,
                agentType = action.agentType,
                userId = action.userId,
                value = 1.0,
                unit = "count",
                context = mapOf(
                    "action" to action.action
                )
            )
        )
    }
    
    private fun updateActiveWorkflow(action: UserAction) {
        // Find active workflow for this user
        val session = activeWorkflows.values.find { it.userId == action.userId }
        session?.let {
            it.steps.add(action)
        }
    }
    
    private suspend fun analyzeWorkflowBottlenecks(session: WorkflowSession, totalDuration: Long) {
        if (session.steps.isEmpty()) return
        
        // Find steps that took unusually long
        val averageDurationPerStep = totalDuration / session.steps.size.toDouble()
        
        for ((index, step) in session.steps.withIndex()) {
            val stepDuration = if (index < session.steps.size - 1) {
                Duration.between(step.timestamp, session.steps[index + 1].timestamp).toMillis()
            } else {
                averageDurationPerStep.toLong()
            }
            
            if (stepDuration > BOTTLENECK_THRESHOLD_MS) {
                val bottleneck = BottleneckAnalysis(
                    workflowName = session.workflowName,
                    bottleneckType = BottleneckType.MANUAL_INPUT,
                    location = "Step ${index + 1}: ${step.action}",
                    description = "Step took ${stepDuration}ms, significantly longer than average",
                    impactScore = (stepDuration / totalDuration.toDouble()).coerceAtMost(1.0),
                    frequencyCount = 1,
                    averageDelay = stepDuration,
                    affectedUsers = setOf(session.userId)
                )
                
                knowledgeBaseRepository.addBottleneck(bottleneck)
            }
        }
    }
    
    private suspend fun updateWorkflowPattern(session: WorkflowSession, success: Boolean, duration: Long) {
        // Find or create pattern for this workflow
        val existingPattern = detectedPatterns.values.find { it.name == session.workflowName }
        
        val pattern = if (existingPattern != null) {
            // Update existing pattern
            val newFrequency = existingPattern.frequency + 1
            val newAvgDuration = ((existingPattern.averageDuration * existingPattern.frequency) + duration) / newFrequency
            val newSuccessRate = if (success) {
                (existingPattern.successRate * existingPattern.frequency + 1.0) / newFrequency
            } else {
                (existingPattern.successRate * existingPattern.frequency) / newFrequency
            }
            
            existingPattern.copy(
                frequency = newFrequency,
                averageDuration = newAvgDuration.toLong(),
                lastExecuted = LocalDateTime.now(),
                successRate = newSuccessRate,
                userIds = existingPattern.userIds + session.userId,
                automationReadiness = calculateAutomationReadiness(newSuccessRate, newFrequency)
            )
        } else {
            // Create new pattern
            WorkflowPattern(
                name = session.workflowName,
                description = "Workflow pattern for ${session.workflowName}",
                steps = session.steps.mapIndexed { index, action ->
                    WorkflowStep(
                        stepNumber = index + 1,
                        action = action.action,
                        agentType = action.agentType
                    )
                },
                frequency = 1,
                averageDuration = duration,
                lastExecuted = LocalDateTime.now(),
                successRate = if (success) 1.0 else 0.0,
                userIds = setOf(session.userId),
                automationReadiness = 0.0
            )
        }
        
        detectedPatterns[pattern.id] = pattern
        knowledgeBaseRepository.saveWorkflowPattern(pattern)
    }
    
    private fun groupActionSequences(actions: List<UserAction>): Map<String, List<UserAction>> {
        val sequences = mutableMapOf<String, MutableList<UserAction>>()
        
        // Group by user and action patterns
        val userActions = actions.groupBy { it.userId }
        
        for ((_, userActionList) in userActions) {
            // Look for repeating sequences of 3-5 actions
            for (windowSize in 3..5) {
                for (i in 0..userActionList.size - windowSize) {
                    val window = userActionList.subList(i, i + windowSize)
                    val sequenceKey = window.joinToString("->") { it.action }
                    
                    sequences.getOrPut(sequenceKey) { mutableListOf() }.addAll(window)
                }
            }
        }
        
        return sequences
    }
    
    private fun createPatternFromSequence(sequenceKey: String, actions: List<UserAction>): WorkflowPattern {
        val uniqueActions = actions.distinctBy { it.action }
        val avgTimeBetweenActions = if (actions.size > 1) {
            val durations = actions.zipWithNext { a, b ->
                Duration.between(a.timestamp, b.timestamp).toMillis()
            }
            durations.average().toLong()
        } else {
            0L
        }
        
        return WorkflowPattern(
            name = "Pattern: $sequenceKey",
            description = "Detected workflow pattern with ${uniqueActions.size} steps",
            steps = uniqueActions.mapIndexed { index, action ->
                WorkflowStep(
                    stepNumber = index + 1,
                    action = action.action,
                    agentType = action.agentType,
                    averageDuration = avgTimeBetweenActions
                )
            },
            frequency = actions.size / uniqueActions.size,
            averageDuration = avgTimeBetweenActions * uniqueActions.size,
            lastExecuted = actions.maxOfOrNull { it.timestamp },
            successRate = 1.0, // Assume success if completed
            userIds = actions.map { it.userId }.toSet(),
            automationReadiness = 0.5 // Initial readiness
        )
    }
    
    private fun determineBottleneckType(step: WorkflowStep): BottleneckType {
        return when {
            step.action.contains("wait", ignoreCase = true) -> BottleneckType.APPROVAL_DELAY
            step.action.contains("fetch", ignoreCase = true) -> BottleneckType.DATA_RETRIEVAL
            step.action.contains("api", ignoreCase = true) -> BottleneckType.EXTERNAL_API
            step.action.contains("input", ignoreCase = true) -> BottleneckType.MANUAL_INPUT
            step.action.contains("calculate", ignoreCase = true) -> BottleneckType.COMPUTATION
            step.failureRate > 0.2 -> BottleneckType.MISSING_INFORMATION
            else -> BottleneckType.MANUAL_INPUT
        }
    }
    
    private fun calculateImpactScore(step: WorkflowStep, pattern: WorkflowPattern): Double {
        val durationImpact = (step.averageDuration.toDouble() / pattern.averageDuration).coerceAtMost(1.0)
        val failureImpact = step.failureRate
        val frequencyImpact = (pattern.frequency.toDouble() / 100.0).coerceAtMost(1.0)
        
        return (durationImpact * 0.4 + failureImpact * 0.4 + frequencyImpact * 0.2).coerceAtMost(1.0)
    }
    
    private fun suggestPossibleCauses(step: WorkflowStep): List<String> {
        return when (determineBottleneckType(step)) {
            BottleneckType.MANUAL_INPUT -> listOf(
                "Requires manual data entry",
                "Complex form or interface",
                "Unclear instructions"
            )
            BottleneckType.APPROVAL_DELAY -> listOf(
                "Waiting for human approval",
                "Approver not available",
                "Approval process unclear"
            )
            BottleneckType.DATA_RETRIEVAL -> listOf(
                "Slow database query",
                "Large dataset",
                "Network latency"
            )
            BottleneckType.EXTERNAL_API -> listOf(
                "Third-party API slow",
                "API rate limiting",
                "Network issues"
            )
            else -> listOf("Unknown cause")
        }
    }
    
    private fun suggestFixes(step: WorkflowStep): List<String> {
        return when (determineBottleneckType(step)) {
            BottleneckType.MANUAL_INPUT -> listOf(
                "Pre-fill form data from context",
                "Simplify input interface",
                "Add auto-complete suggestions"
            )
            BottleneckType.APPROVAL_DELAY -> listOf(
                "Implement automatic approval for low-risk cases",
                "Set up notification system",
                "Create approval delegation rules"
            )
            BottleneckType.DATA_RETRIEVAL -> listOf(
                "Add caching layer",
                "Optimize database queries",
                "Implement pagination"
            )
            BottleneckType.EXTERNAL_API -> listOf(
                "Cache API responses",
                "Implement request batching",
                "Add fallback mechanisms"
            )
            else -> listOf("Review and optimize")
        }
    }
    
    private fun calculateAutomationReadiness(successRate: Double, frequency: Int): Double {
        val successFactor = successRate
        val frequencyFactor = (frequency.toDouble() / 50.0).coerceAtMost(1.0)
        
        return (successFactor * 0.7 + frequencyFactor * 0.3).coerceAtMost(1.0)
    }
}

// ===== INTERNAL DATA CLASSES =====
// These are kept private to WorkflowMonitorService to encapsulate monitoring implementation details

/**
 * Internal representation of a user action for workflow monitoring
 * Intentionally private to avoid coupling external code to monitoring internals
 */
private data class UserAction(
    val userId: String,
    val action: String,
    val agentType: AgentType?,
    val timestamp: LocalDateTime,
    val context: Map<String, Any>
)

/**
 * Represents an active workflow session being monitored
 * Note: Uses MutableList for steps as sessions are built incrementally during monitoring
 */
private data class WorkflowSession(
    val sessionId: String,
    val userId: String,
    val workflowName: String,
    val startTime: LocalDateTime,
    val context: Map<String, Any>,
    val steps: MutableList<UserAction> = mutableListOf()
)

data class WorkflowStats(
    val totalPatterns: Int,
    val totalWorkflowExecutions: Int,
    val averageWorkflowDuration: Double,
    val automationReadyPatterns: Int,
    val mostFrequentWorkflow: String
)
