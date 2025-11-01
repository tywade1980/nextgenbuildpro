package com.nextgenbuildpro.core.agent

import com.nextgenbuildpro.shared.*
import com.nextgenbuildpro.core.repository.KnowledgeBaseRepository
import com.nextgenbuildpro.core.service.WorkflowMonitorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log
import java.time.LocalDateTime

/**
 * Base implementation of LearningAgent interface
 * Provides learning capabilities for agents to improve from knowledge and workflows
 */
abstract class LearningAgentImpl(
    override val agentId: String,
    override val agentType: AgentType,
    override val specialization: String,
    protected val knowledgeBaseRepository: KnowledgeBaseRepository,
    protected val workflowMonitorService: WorkflowMonitorService
) : LearningAgent {
    
    companion object {
        private const val TAG = "LearningAgentImpl"
    }
    
    private val _isActive = MutableStateFlow(false)
    override val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    protected val learnedKnowledge = mutableListOf<KnowledgeEntry>()
    protected val identifiedPatterns = mutableListOf<WorkflowPattern>()
    protected val detectedBottlenecks = mutableListOf<BottleneckAnalysis>()
    protected val generatedSuggestions = mutableListOf<AutomationSuggestion>()
    
    override suspend fun initialize(): Result<Unit> = try {
        _isActive.value = true
        Log.i(TAG, "Initialized learning agent: $specialization")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize learning agent", e)
        Result.failure(e)
    }
    
    override suspend fun shutdown(): Result<Unit> = try {
        _isActive.value = false
        Log.i(TAG, "Shutdown learning agent: $specialization")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to shutdown learning agent", e)
        Result.failure(e)
    }
    
    override suspend fun processTask(task: NextGenTask): Result<NextGenTask> {
        // Default implementation - subclasses should override for specific behavior
        return Result.success(task.copy(
            status = TaskStatus.COMPLETED,
            result = mapOf("message" to "Task processed by learning agent")
        ))
    }
    
    override suspend fun learnFromKnowledge(entries: List<KnowledgeEntry>): Result<Unit> = try {
        Log.i(TAG, "Learning from ${entries.size} knowledge entries...")
        
        // Filter relevant entries based on agent specialization
        val relevantEntries = entries.filter { entry ->
            isRelevantKnowledge(entry)
        }
        
        // Process and store learned knowledge
        learnedKnowledge.addAll(relevantEntries)
        
        // Update knowledge entry access counts
        relevantEntries.forEach { entry ->
            knowledgeBaseRepository.updateKnowledgeEntry(entry)
        }
        
        Log.i(TAG, "Learned from ${relevantEntries.size} relevant entries")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to learn from knowledge", e)
        Result.failure(e)
    }
    
    override suspend fun identifyPatterns(metrics: List<SystemMetrics>): Result<List<WorkflowPattern>> = try {
        Log.i(TAG, "Identifying patterns from ${metrics.size} metrics...")
        
        // Filter metrics relevant to this agent
        val relevantMetrics = metrics.filter { metric ->
            metric.agentType == agentType || metric.agentType == null
        }
        
        // Analyze metrics for patterns
        val patterns = analyzeMetricsForPatterns(relevantMetrics)
        
        identifiedPatterns.addAll(patterns)
        
        // Save patterns to repository
        patterns.forEach { pattern ->
            knowledgeBaseRepository.saveWorkflowPattern(pattern)
        }
        
        Log.i(TAG, "Identified ${patterns.size} workflow patterns")
        Result.success(patterns)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to identify patterns", e)
        Result.failure(e)
    }
    
    override suspend fun detectBottlenecks(patterns: List<WorkflowPattern>): Result<List<BottleneckAnalysis>> = try {
        Log.i(TAG, "Detecting bottlenecks in ${patterns.size} patterns...")
        
        val bottlenecks = mutableListOf<BottleneckAnalysis>()
        
        for (pattern in patterns) {
            // Analyze pattern for bottlenecks
            val patternBottlenecks = analyzePatternForBottlenecks(pattern)
            bottlenecks.addAll(patternBottlenecks)
        }
        
        detectedBottlenecks.addAll(bottlenecks)
        
        // Save bottlenecks to repository
        bottlenecks.forEach { bottleneck ->
            knowledgeBaseRepository.addBottleneck(bottleneck)
        }
        
        Log.i(TAG, "Detected ${bottlenecks.size} bottlenecks")
        Result.success(bottlenecks)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to detect bottlenecks", e)
        Result.failure(e)
    }
    
    override suspend fun generateAutomationSuggestions(
        patterns: List<WorkflowPattern>,
        bottlenecks: List<BottleneckAnalysis>
    ): Result<List<AutomationSuggestion>> = try {
        Log.i(TAG, "Generating automation suggestions...")
        
        val suggestions = mutableListOf<AutomationSuggestion>()
        
        // Generate suggestions from high-readiness patterns
        for (pattern in patterns.filter { it.automationReadiness >= 0.7 }) {
            val suggestion = createSuggestionFromPattern(pattern, bottlenecks)
            suggestions.add(suggestion)
        }
        
        // Generate suggestions from high-impact bottlenecks
        for (bottleneck in bottlenecks.filter { it.impactScore >= 0.7 }) {
            val suggestion = createSuggestionFromBottleneck(bottleneck)
            suggestions.add(suggestion)
        }
        
        generatedSuggestions.addAll(suggestions)
        
        // Save suggestions to repository
        suggestions.forEach { suggestion ->
            knowledgeBaseRepository.addAutomationSuggestion(suggestion)
        }
        
        Log.i(TAG, "Generated ${suggestions.size} automation suggestions")
        Result.success(suggestions)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to generate automation suggestions", e)
        Result.failure(e)
    }
    
    override suspend fun getLearningStats(): Result<LearningStats> = try {
        val approvedSuggestions = generatedSuggestions.count { 
            it.status == SuggestionStatus.APPROVED || it.status == SuggestionStatus.IMPLEMENTED
        }
        
        val avgConfidence = if (generatedSuggestions.isNotEmpty()) {
            generatedSuggestions.map { it.confidenceScore }.average()
        } else {
            0.0
        }
        
        val stats = LearningStats(
            agentId = agentId,
            agentType = agentType,
            knowledgeEntriesLearned = learnedKnowledge.size,
            patternsIdentified = identifiedPatterns.size,
            bottlenecksDetected = detectedBottlenecks.size,
            suggestionsGenerated = generatedSuggestions.size,
            suggestionsApproved = approvedSuggestions,
            averageConfidenceScore = avgConfidence,
            lastLearningAt = if (learnedKnowledge.isNotEmpty()) {
                learnedKnowledge.maxOfOrNull { it.createdAt }
            } else null
        )
        
        Result.success(stats)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get learning stats", e)
        Result.failure(e)
    }
    
    // ===== PROTECTED METHODS FOR SUBCLASSES =====
    
    /**
     * Determine if a knowledge entry is relevant to this agent
     * Subclasses can override to customize relevance logic
     */
    protected open fun isRelevantKnowledge(entry: KnowledgeEntry): Boolean {
        // Default: check if tags or specialization match
        return entry.tags.any { tag ->
            tag.contains(specialization, ignoreCase = true) ||
            specialization.contains(tag, ignoreCase = true)
        }
    }
    
    /**
     * Analyze metrics to identify workflow patterns
     * Subclasses can override for specialized pattern recognition
     */
    protected open fun analyzeMetricsForPatterns(metrics: List<SystemMetrics>): List<WorkflowPattern> {
        val patterns = mutableListOf<WorkflowPattern>()
        
        // Group metrics by context to find patterns
        val groupedMetrics = metrics.groupBy { metric ->
            metric.context["action"] as? String ?: "unknown"
        }
        
        for ((action, actionMetrics) in groupedMetrics) {
            if (actionMetrics.size >= 3) { // Minimum threshold for a pattern
                val avgDuration = actionMetrics.map { it.value }.average()
                val userIds = actionMetrics.mapNotNull { it.userId }.toSet()
                
                val pattern = WorkflowPattern(
                    name = "Pattern: $action",
                    description = "Detected pattern for action: $action",
                    steps = listOf(
                        WorkflowStep(
                            stepNumber = 1,
                            action = action,
                            agentType = agentType,
                            averageDuration = avgDuration.toLong()
                        )
                    ),
                    frequency = actionMetrics.size,
                    averageDuration = avgDuration.toLong(),
                    lastExecuted = actionMetrics.maxOfOrNull { it.timestamp },
                    successRate = 0.95, // Assume high success if no errors recorded
                    userIds = userIds,
                    automationReadiness = calculateAutomationReadiness(actionMetrics.size, 0.95)
                )
                
                patterns.add(pattern)
            }
        }
        
        return patterns
    }
    
    /**
     * Analyze a workflow pattern for bottlenecks
     * Subclasses can override for specialized bottleneck detection
     */
    protected open fun analyzePatternForBottlenecks(pattern: WorkflowPattern): List<BottleneckAnalysis> {
        val bottlenecks = mutableListOf<BottleneckAnalysis>()
        
        // Find steps with high duration or failure rate
        for (step in pattern.steps) {
            if (step.averageDuration > 5000 || step.failureRate > 0.1) {
                val bottleneck = BottleneckAnalysis(
                    workflowId = pattern.id,
                    workflowName = pattern.name,
                    bottleneckType = determineBottleneckType(step.action),
                    location = "Step ${step.stepNumber}: ${step.action}",
                    description = "High duration (${step.averageDuration}ms) or failure rate (${step.failureRate})",
                    impactScore = calculateBottleneckImpact(step, pattern),
                    frequencyCount = pattern.frequency,
                    averageDelay = step.averageDuration,
                    affectedUsers = pattern.userIds,
                    possibleCauses = listOf("Manual processing", "Complex calculation", "External dependency"),
                    suggestedFixes = listOf("Automate with agent", "Optimize algorithm", "Add caching")
                )
                
                bottlenecks.add(bottleneck)
            }
        }
        
        return bottlenecks
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    private fun createSuggestionFromPattern(
        pattern: WorkflowPattern,
        bottlenecks: List<BottleneckAnalysis>
    ): AutomationSuggestion {
        val estimatedTimeSavings = pattern.averageDuration * 0.6 // Assume 60% time savings
        
        return AutomationSuggestion(
            title = "Automate workflow: ${pattern.name}",
            description = "This workflow has been executed ${pattern.frequency} times with ${pattern.successRate * 100}% success rate. Consider increasing automation level.",
            workflowPattern = pattern,
            suggestedAutomationLevel = suggestAutomationLevel(pattern),
            currentAutomationLevel = AutomationLevel.MANUAL,
            confidenceScore = pattern.automationReadiness,
            estimatedTimeSavings = estimatedTimeSavings.toLong(),
            estimatedErrorReduction = (1.0 - pattern.successRate) * 0.8, // Assume 80% error reduction
            requiredApprovals = listOf("Manager", "Operations Lead"),
            riskLevel = assessRiskLevel(pattern),
            benefits = listOf(
                "Save ${estimatedTimeSavings / 1000} seconds per execution",
                "Reduce manual errors",
                "Free up staff time",
                "Improve consistency"
            ),
            risks = listOf(
                "Initial setup time required",
                "May need fine-tuning",
                "Requires monitoring during transition"
            ),
            implementationSteps = listOf(
                "Review workflow pattern details",
                "Test automation in safe environment",
                "Train relevant staff",
                "Deploy with monitoring",
                "Collect feedback and adjust"
            ),
            status = SuggestionStatus.PENDING,
            createdAt = LocalDateTime.now()
        )
    }
    
    private fun createSuggestionFromBottleneck(bottleneck: BottleneckAnalysis): AutomationSuggestion {
        return AutomationSuggestion(
            title = "Optimize bottleneck: ${bottleneck.workflowName}",
            description = "Bottleneck detected in ${bottleneck.location} with ${bottleneck.impactScore * 100}% impact score. ${bottleneck.description}",
            workflowPattern = null,
            suggestedAutomationLevel = AutomationLevel.SUPERVISED,
            currentAutomationLevel = AutomationLevel.MANUAL,
            confidenceScore = bottleneck.impactScore,
            estimatedTimeSavings = bottleneck.averageDelay,
            estimatedErrorReduction = 0.5,
            requiredApprovals = listOf("Manager"),
            riskLevel = RiskLevel.MEDIUM,
            benefits = bottleneck.suggestedFixes,
            risks = bottleneck.possibleCauses,
            implementationSteps = bottleneck.suggestedFixes.map { "Implement: $it" },
            status = SuggestionStatus.PENDING,
            createdAt = LocalDateTime.now()
        )
    }
    
    private fun suggestAutomationLevel(pattern: WorkflowPattern): AutomationLevel {
        return when {
            pattern.automationReadiness >= 0.9 && pattern.successRate >= 0.95 -> AutomationLevel.AUTOMATED
            pattern.automationReadiness >= 0.8 -> AutomationLevel.SUPERVISED
            pattern.automationReadiness >= 0.6 -> AutomationLevel.HUMAN_IN_LOOP
            else -> AutomationLevel.MANUAL
        }
    }
    
    private fun assessRiskLevel(pattern: WorkflowPattern): RiskLevel {
        return when {
            pattern.successRate < 0.8 -> RiskLevel.HIGH
            pattern.frequency < 10 -> RiskLevel.MEDIUM
            pattern.successRate >= 0.95 && pattern.frequency >= 20 -> RiskLevel.LOW
            else -> RiskLevel.MEDIUM
        }
    }
    
    private fun determineBottleneckType(action: String): BottleneckType {
        return when {
            action.contains("input", ignoreCase = true) -> BottleneckType.MANUAL_INPUT
            action.contains("approval", ignoreCase = true) -> BottleneckType.APPROVAL_DELAY
            action.contains("fetch", ignoreCase = true) -> BottleneckType.DATA_RETRIEVAL
            action.contains("api", ignoreCase = true) -> BottleneckType.EXTERNAL_API
            action.contains("calculate", ignoreCase = true) -> BottleneckType.COMPUTATION
            else -> BottleneckType.MANUAL_INPUT
        }
    }
    
    private fun calculateBottleneckImpact(step: WorkflowStep, pattern: WorkflowPattern): Double {
        val durationImpact = step.averageDuration.toDouble() / pattern.averageDuration
        val failureImpact = step.failureRate
        return ((durationImpact * 0.6) + (failureImpact * 0.4)).coerceAtMost(1.0)
    }
    
    private fun calculateAutomationReadiness(frequency: Int, successRate: Double): Double {
        val frequencyScore = (frequency.toDouble() / 50.0).coerceAtMost(1.0)
        return (successRate * 0.7 + frequencyScore * 0.3).coerceAtMost(1.0)
    }
}
