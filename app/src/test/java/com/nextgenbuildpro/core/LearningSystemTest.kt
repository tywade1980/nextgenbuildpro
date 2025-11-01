package com.nextgenbuildpro.core

import com.nextgenbuildpro.shared.*
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime

/**
 * Test suite for the Learning System data models
 * Tests knowledge base, workflow monitoring, and automation suggestion structures
 */
class LearningSystemTest {
    
    @Test
    fun testKnowledgeEntryCreation() {
        val entry = KnowledgeEntry(
            title = "Concrete Curing Best Practices",
            content = "Concrete should cure for at least 7 days before load application...",
            category = KnowledgeCategory.BEST_PRACTICES,
            tags = listOf("concrete", "curing", "safety"),
            sourceType = KnowledgeSourceType.USER_INPUT
        )
        
        assertNotNull(entry.id)
        assertEquals("Concrete Curing Best Practices", entry.title)
        assertEquals(KnowledgeCategory.BEST_PRACTICES, entry.category)
        assertEquals(3, entry.tags.size)
        assertTrue(entry.tags.contains("concrete"))
    }
    
    @Test
    fun testWorkflowPatternCreation() {
        val pattern = WorkflowPattern(
            name = "Daily Site Inspection",
            description = "Standard workflow for daily site inspection",
            steps = listOf(
                WorkflowStep(
                    stepNumber = 1,
                    action = "Review safety checklist",
                    agentType = AgentType.CSO_SAFETY_ORCHESTRATOR
                ),
                WorkflowStep(
                    stepNumber = 2,
                    action = "Take site photos",
                    agentType = AgentType.COO_OPERATIONS_ORCHESTRATOR
                ),
                WorkflowStep(
                    stepNumber = 3,
                    action = "Update progress log",
                    agentType = AgentType.PROJECT_MANAGEMENT_ORCHESTRATOR
                )
            ),
            frequency = 25,
            successRate = 0.96
        )
        
        assertNotNull(pattern.id)
        assertEquals("Daily Site Inspection", pattern.name)
        assertEquals(3, pattern.steps.size)
        assertEquals(25, pattern.frequency)
        assertEquals(0.96, pattern.successRate, 0.001)
    }
    
    @Test
    fun testBottleneckAnalysisCreation() {
        val bottleneck = BottleneckAnalysis(
            workflowName = "Permit Application Process",
            bottleneckType = BottleneckType.APPROVAL_DELAY,
            location = "Step 3: Wait for city approval",
            description = "Average wait time of 15 days for city approval",
            impactScore = 0.85,
            frequencyCount = 12,
            averageDelay = 15 * 24 * 60 * 60 * 1000L, // 15 days in milliseconds
            affectedUsers = setOf("user1", "user2", "user3"),
            possibleCauses = listOf("City backlog", "Incomplete documentation", "Holiday delays"),
            suggestedFixes = listOf("Submit early", "Use expedited process", "Pre-review checklist")
        )
        
        assertNotNull(bottleneck.id)
        assertEquals(BottleneckType.APPROVAL_DELAY, bottleneck.bottleneckType)
        assertEquals(0.85, bottleneck.impactScore, 0.001)
        assertEquals(3, bottleneck.affectedUsers.size)
        assertEquals(3, bottleneck.possibleCauses.size)
        assertEquals(3, bottleneck.suggestedFixes.size)
    }
    
    @Test
    fun testAutomationSuggestionCreation() {
        val suggestion = AutomationSuggestion(
            title = "Automate Daily Log Entry",
            description = "Daily log entry is performed consistently and takes 10 minutes per day",
            suggestedAutomationLevel = AutomationLevel.SUPERVISED,
            currentAutomationLevel = AutomationLevel.MANUAL,
            confidenceScore = 0.92,
            estimatedTimeSavings = 10 * 60 * 1000L, // 10 minutes
            estimatedErrorReduction = 0.7,
            riskLevel = RiskLevel.LOW,
            benefits = listOf("Save 10 minutes daily", "Reduce data entry errors", "Improve consistency"),
            risks = listOf("Initial setup time", "Requires training"),
            implementationSteps = listOf(
                "Configure automated log template",
                "Test with sample data",
                "Train users on review process",
                "Deploy with monitoring"
            ),
            status = SuggestionStatus.PENDING
        )
        
        assertNotNull(suggestion.id)
        assertEquals(AutomationLevel.SUPERVISED, suggestion.suggestedAutomationLevel)
        assertEquals(0.92, suggestion.confidenceScore, 0.001)
        assertEquals(RiskLevel.LOW, suggestion.riskLevel)
        assertEquals(3, suggestion.benefits.size)
        assertEquals(4, suggestion.implementationSteps.size)
        assertEquals(SuggestionStatus.PENDING, suggestion.status)
    }
    
    @Test
    fun testSystemMetricsCreation() {
        val metric = SystemMetrics(
            metricType = MetricType.TASK_COMPLETION_TIME,
            agentType = AgentType.PROJECT_MANAGEMENT_ORCHESTRATOR,
            userId = "user123",
            value = 5432.0,
            unit = "milliseconds",
            context = mapOf(
                "action" to "Create estimate",
                "projectType" to "Residential"
            )
        )
        
        assertNotNull(metric.id)
        assertEquals(MetricType.TASK_COMPLETION_TIME, metric.metricType)
        assertEquals(AgentType.PROJECT_MANAGEMENT_ORCHESTRATOR, metric.agentType)
        assertEquals(5432.0, metric.value, 0.001)
        assertEquals("milliseconds", metric.unit)
        assertEquals(2, metric.context.size)
    }
    
    @Test
    fun testLearningStatsCreation() {
        val stats = LearningStats(
            agentId = "agent_pm_001",
            agentType = AgentType.PROJECT_MANAGEMENT_ORCHESTRATOR,
            knowledgeEntriesLearned = 150,
            patternsIdentified = 25,
            bottlenecksDetected = 8,
            suggestionsGenerated = 12,
            suggestionsApproved = 9,
            averageConfidenceScore = 0.87,
            lastLearningAt = LocalDateTime.now()
        )
        
        assertEquals("agent_pm_001", stats.agentId)
        assertEquals(150, stats.knowledgeEntriesLearned)
        assertEquals(25, stats.patternsIdentified)
        assertEquals(8, stats.bottlenecksDetected)
        assertEquals(12, stats.suggestionsGenerated)
        assertEquals(9, stats.suggestionsApproved)
        assertEquals(0.87, stats.averageConfidenceScore, 0.001)
        assertNotNull(stats.lastLearningAt)
    }
    
    @Test
    fun testKnowledgeCategoryEnum() {
        val categories = KnowledgeCategory.values()
        
        assertTrue(categories.contains(KnowledgeCategory.MATERIALS))
        assertTrue(categories.contains(KnowledgeCategory.LABOR_RATES))
        assertTrue(categories.contains(KnowledgeCategory.BUILDING_CODES))
        assertTrue(categories.contains(KnowledgeCategory.BEST_PRACTICES))
        assertTrue(categories.contains(KnowledgeCategory.SAFETY_PROCEDURES))
        assertTrue(categories.contains(KnowledgeCategory.WORKFLOW_PATTERNS))
        assertTrue(categories.contains(KnowledgeCategory.UNSTRUCTURED))
        
        // Verify we have comprehensive coverage
        assertTrue(categories.size >= 15)
    }
    
    @Test
    fun testBottleneckTypeEnum() {
        val types = BottleneckType.values()
        
        assertTrue(types.contains(BottleneckType.MANUAL_INPUT))
        assertTrue(types.contains(BottleneckType.APPROVAL_DELAY))
        assertTrue(types.contains(BottleneckType.DATA_RETRIEVAL))
        assertTrue(types.contains(BottleneckType.EXTERNAL_API))
        assertTrue(types.contains(BottleneckType.MISSING_INFORMATION))
        
        // Verify comprehensive coverage
        assertTrue(types.size >= 10)
    }
    
    @Test
    fun testAutomationLevelProgression() {
        val levels = AutomationLevel.values()
        
        assertTrue(levels.contains(AutomationLevel.MANUAL))
        assertTrue(levels.contains(AutomationLevel.HUMAN_IN_LOOP))
        assertTrue(levels.contains(AutomationLevel.SUPERVISED))
        assertTrue(levels.contains(AutomationLevel.AUTOMATED))
        assertTrue(levels.contains(AutomationLevel.LEARNING))
        
        // Manual is lowest, Automated is highest (excluding LEARNING)
        assertEquals(AutomationLevel.MANUAL, levels[0])
    }
    
    @Test
    fun testWorkflowStepValidation() {
        val step = WorkflowStep(
            stepNumber = 1,
            action = "Review estimate",
            agentType = AgentType.CFO_FINANCIAL_ORCHESTRATOR,
            parameters = mapOf("estimateId" to "EST-001", "reviewLevel" to "detailed"),
            averageDuration = 300000L, // 5 minutes
            failureRate = 0.05
        )
        
        assertEquals(1, step.stepNumber)
        assertEquals("Review estimate", step.action)
        assertEquals(AgentType.CFO_FINANCIAL_ORCHESTRATOR, step.agentType)
        assertEquals(2, step.parameters.size)
        assertEquals(300000L, step.averageDuration)
        assertEquals(0.05, step.failureRate, 0.001)
    }
    
    @Test
    fun testKnowledgeEntryRelevanceScore() {
        val highRelevanceEntry = KnowledgeEntry(
            title = "Critical Safety Protocol",
            content = "Always wear hard hat on site",
            category = KnowledgeCategory.SAFETY_PROCEDURES,
            tags = listOf("safety", "ppe", "critical"),
            sourceType = KnowledgeSourceType.USER_INPUT,
            relevanceScore = 1.0,
            verified = true
        )
        
        assertEquals(1.0, highRelevanceEntry.relevanceScore, 0.001)
        assertTrue(highRelevanceEntry.verified)
        
        val lowRelevanceEntry = KnowledgeEntry(
            title = "Optional Best Practice",
            content = "Consider using eco-friendly materials",
            category = KnowledgeCategory.BEST_PRACTICES,
            tags = listOf("optional", "eco"),
            sourceType = KnowledgeSourceType.WEB_SCRAPING,
            relevanceScore = 0.3,
            verified = false
        )
        
        assertEquals(0.3, lowRelevanceEntry.relevanceScore, 0.001)
        assertFalse(lowRelevanceEntry.verified)
    }
    
    @Test
    fun testAutomationReadinessCalculation() {
        // High readiness pattern
        val highReadinessPattern = WorkflowPattern(
            name = "Proven Workflow",
            description = "Highly consistent workflow",
            steps = listOf(WorkflowStep(1, "Action", null)),
            frequency = 50,
            successRate = 0.98,
            automationReadiness = 0.95
        )
        
        assertTrue(highReadinessPattern.automationReadiness >= 0.8)
        
        // Low readiness pattern
        val lowReadinessPattern = WorkflowPattern(
            name = "New Workflow",
            description = "Recently identified workflow",
            steps = listOf(WorkflowStep(1, "Action", null)),
            frequency = 5,
            successRate = 0.85,
            automationReadiness = 0.35
        )
        
        assertTrue(lowReadinessPattern.automationReadiness < 0.6)
    }
}
