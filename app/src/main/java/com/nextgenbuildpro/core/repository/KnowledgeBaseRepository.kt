package com.nextgenbuildpro.core.repository

import com.nextgenbuildpro.shared.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import android.util.Log
import java.time.LocalDateTime

/**
 * Repository for managing construction industry knowledge base
 * Supports storage and retrieval of unstructured data with flexible categorization
 */
class KnowledgeBaseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "KnowledgeBaseRepository"
        private const val COLLECTION_KNOWLEDGE = "knowledge_base"
        private const val COLLECTION_PATTERNS = "workflow_patterns"
        private const val COLLECTION_BOTTLENECKS = "bottlenecks"
        private const val COLLECTION_METRICS = "system_metrics"
        private const val COLLECTION_SUGGESTIONS = "automation_suggestions"
    }
    
    private val _knowledgeEntries = MutableStateFlow<List<KnowledgeEntry>>(emptyList())
    val knowledgeEntries: StateFlow<List<KnowledgeEntry>> = _knowledgeEntries.asStateFlow()
    
    private val _workflowPatterns = MutableStateFlow<List<WorkflowPattern>>(emptyList())
    val workflowPatterns: StateFlow<List<WorkflowPattern>> = _workflowPatterns.asStateFlow()
    
    private val _automationSuggestions = MutableStateFlow<List<AutomationSuggestion>>(emptyList())
    val automationSuggestions: StateFlow<List<AutomationSuggestion>> = _automationSuggestions.asStateFlow()
    
    /**
     * Initialize repository and load data
     */
    suspend fun initialize(): Result<Unit> = try {
        Log.i(TAG, "Initializing KnowledgeBaseRepository...")
        loadKnowledgeEntries()
        loadWorkflowPatterns()
        loadAutomationSuggestions()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize repository", e)
        Result.failure(e)
    }
    
    // ===== KNOWLEDGE ENTRY OPERATIONS =====
    
    /**
     * Add a new knowledge entry to the repository
     */
    suspend fun addKnowledgeEntry(entry: KnowledgeEntry): Result<KnowledgeEntry> = try {
        firestore.collection(COLLECTION_KNOWLEDGE)
            .document(entry.id)
            .set(mapOf(
                "id" to entry.id,
                "title" to entry.title,
                "content" to entry.content,
                "category" to entry.category.name,
                "tags" to entry.tags,
                "sourceType" to entry.sourceType.name,
                "sourceReference" to entry.sourceReference,
                "metadata" to entry.metadata,
                "createdAt" to entry.createdAt.toString(),
                "updatedAt" to entry.updatedAt.toString(),
                "accessCount" to entry.accessCount,
                "relevanceScore" to entry.relevanceScore,
                "verified" to entry.verified,
                "verifiedBy" to entry.verifiedBy,
                "relatedEntries" to entry.relatedEntries
            ))
            .await()
        
        _knowledgeEntries.value = _knowledgeEntries.value + entry
        Log.i(TAG, "Added knowledge entry: ${entry.title}")
        Result.success(entry)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to add knowledge entry", e)
        Result.failure(e)
    }
    
    /**
     * Update an existing knowledge entry
     */
    suspend fun updateKnowledgeEntry(entry: KnowledgeEntry): Result<KnowledgeEntry> = try {
        val updatedEntry = entry.copy(
            updatedAt = LocalDateTime.now(),
            accessCount = entry.accessCount + 1
        )
        
        firestore.collection(COLLECTION_KNOWLEDGE)
            .document(updatedEntry.id)
            .set(mapOf(
                "id" to updatedEntry.id,
                "title" to updatedEntry.title,
                "content" to updatedEntry.content,
                "category" to updatedEntry.category.name,
                "tags" to updatedEntry.tags,
                "sourceType" to updatedEntry.sourceType.name,
                "sourceReference" to updatedEntry.sourceReference,
                "metadata" to updatedEntry.metadata,
                "createdAt" to updatedEntry.createdAt.toString(),
                "updatedAt" to updatedEntry.updatedAt.toString(),
                "accessCount" to updatedEntry.accessCount,
                "relevanceScore" to updatedEntry.relevanceScore,
                "verified" to updatedEntry.verified,
                "verifiedBy" to updatedEntry.verifiedBy,
                "relatedEntries" to updatedEntry.relatedEntries
            ))
            .await()
        
        _knowledgeEntries.value = _knowledgeEntries.value.map { 
            if (it.id == updatedEntry.id) updatedEntry else it 
        }
        
        Result.success(updatedEntry)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to update knowledge entry", e)
        Result.failure(e)
    }
    
    /**
     * Get knowledge entry by ID
     */
    suspend fun getKnowledgeEntry(id: EntityId): Result<KnowledgeEntry?> = try {
        val document = firestore.collection(COLLECTION_KNOWLEDGE)
            .document(id)
            .get()
            .await()
        
        val entry = document.data?.let { data ->
            KnowledgeEntry(
                id = data["id"] as? String ?: "",
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                category = KnowledgeCategory.valueOf(data["category"] as? String ?: "UNSTRUCTURED"),
                tags = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                sourceType = KnowledgeSourceType.valueOf(data["sourceType"] as? String ?: "USER_INPUT"),
                sourceReference = data["sourceReference"] as? String,
                @Suppress("UNCHECKED_CAST")
                metadata = (data["metadata"] as? Map<String, Any>) ?: emptyMap(),
                createdAt = LocalDateTime.parse(data["createdAt"] as? String ?: LocalDateTime.now().toString()),
                updatedAt = LocalDateTime.parse(data["updatedAt"] as? String ?: LocalDateTime.now().toString()),
                accessCount = (data["accessCount"] as? Long)?.toInt() ?: 0,
                relevanceScore = data["relevanceScore"] as? Double ?: 0.0,
                verified = data["verified"] as? Boolean ?: false,
                verifiedBy = data["verifiedBy"] as? String,
                relatedEntries = (data["relatedEntries"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        }
        Result.success(entry)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get knowledge entry", e)
        Result.failure(e)
    }
    
    /**
     * Search knowledge entries by query
     */
    suspend fun searchKnowledge(
        query: String,
        categories: List<KnowledgeCategory> = emptyList(),
        tags: List<String> = emptyList()
    ): Result<List<KnowledgeEntry>> = try {
        var entries = _knowledgeEntries.value
        
        // Filter by category
        if (categories.isNotEmpty()) {
            entries = entries.filter { it.category in categories }
        }
        
        // Filter by tags
        if (tags.isNotEmpty()) {
            entries = entries.filter { entry ->
                tags.any { tag -> entry.tags.contains(tag) }
            }
        }
        
        // Search in title and content
        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            entries = entries.filter { 
                it.title.lowercase().contains(lowerQuery) || 
                it.content.lowercase().contains(lowerQuery)
            }
        }
        
        // Sort by relevance score and access count
        entries = entries.sortedByDescending { it.relevanceScore * it.accessCount }
        
        Result.success(entries)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to search knowledge", e)
        Result.failure(e)
    }
    
    /**
     * Get knowledge entries by category
     */
    suspend fun getKnowledgeByCategory(category: KnowledgeCategory): Result<List<KnowledgeEntry>> = try {
        val entries = _knowledgeEntries.value.filter { it.category == category }
        Result.success(entries)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get knowledge by category", e)
        Result.failure(e)
    }
    
    /**
     * Get most accessed knowledge entries
     */
    suspend fun getMostAccessedKnowledge(limit: Int = 10): Result<List<KnowledgeEntry>> = try {
        val entries = _knowledgeEntries.value
            .sortedByDescending { it.accessCount }
            .take(limit)
        Result.success(entries)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get most accessed knowledge", e)
        Result.failure(e)
    }
    
    // ===== WORKFLOW PATTERN OPERATIONS =====
    
    /**
     * Add or update a workflow pattern
     */
    suspend fun saveWorkflowPattern(pattern: WorkflowPattern): Result<WorkflowPattern> = try {
        firestore.collection(COLLECTION_PATTERNS)
            .document(pattern.id)
            .set(mapOf(
                "id" to pattern.id,
                "name" to pattern.name,
                "description" to pattern.description,
                "steps" to pattern.steps.map { step ->
                    mapOf(
                        "stepNumber" to step.stepNumber,
                        "action" to step.action,
                        "agentType" to step.agentType?.name,
                        "parameters" to step.parameters,
                        "averageDuration" to step.averageDuration,
                        "failureRate" to step.failureRate
                    )
                },
                "frequency" to pattern.frequency,
                "averageDuration" to pattern.averageDuration,
                "lastExecuted" to pattern.lastExecuted?.toString(),
                "successRate" to pattern.successRate,
                "userIds" to pattern.userIds.toList(),
                "triggerConditions" to pattern.triggerConditions,
                "automationReadiness" to pattern.automationReadiness,
                "metadata" to pattern.metadata
            ))
            .await()
        
        val updated = _workflowPatterns.value.filter { it.id != pattern.id } + pattern
        _workflowPatterns.value = updated
        
        Log.i(TAG, "Saved workflow pattern: ${pattern.name}")
        Result.success(pattern)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to save workflow pattern", e)
        Result.failure(e)
    }
    
    /**
     * Get workflow patterns with high automation readiness
     */
    suspend fun getAutomationReadyPatterns(threshold: Double = 0.8): Result<List<WorkflowPattern>> = try {
        val patterns = _workflowPatterns.value.filter { 
            it.automationReadiness >= threshold && it.successRate >= 0.9
        }
        Result.success(patterns)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get automation ready patterns", e)
        Result.failure(e)
    }
    
    /**
     * Get most frequent workflow patterns
     */
    suspend fun getMostFrequentPatterns(limit: Int = 10): Result<List<WorkflowPattern>> = try {
        val patterns = _workflowPatterns.value
            .sortedByDescending { it.frequency }
            .take(limit)
        Result.success(patterns)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get most frequent patterns", e)
        Result.failure(e)
    }
    
    // ===== BOTTLENECK OPERATIONS =====
    
    /**
     * Add a bottleneck analysis
     */
    suspend fun addBottleneck(bottleneck: BottleneckAnalysis): Result<BottleneckAnalysis> = try {
        firestore.collection(COLLECTION_BOTTLENECKS)
            .document(bottleneck.id)
            .set(mapOf(
                "id" to bottleneck.id,
                "workflowId" to bottleneck.workflowId,
                "workflowName" to bottleneck.workflowName,
                "bottleneckType" to bottleneck.bottleneckType.name,
                "location" to bottleneck.location,
                "description" to bottleneck.description,
                "impactScore" to bottleneck.impactScore,
                "frequencyCount" to bottleneck.frequencyCount,
                "averageDelay" to bottleneck.averageDelay,
                "affectedUsers" to bottleneck.affectedUsers.toList(),
                "detectedAt" to bottleneck.detectedAt.toString(),
                "possibleCauses" to bottleneck.possibleCauses,
                "suggestedFixes" to bottleneck.suggestedFixes
            ))
            .await()
        
        Log.i(TAG, "Added bottleneck: ${bottleneck.workflowName}")
        Result.success(bottleneck)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to add bottleneck", e)
        Result.failure(e)
    }
    
    /**
     * Get bottlenecks by impact score
     */
    suspend fun getHighImpactBottlenecks(threshold: Double = 0.7): Result<List<BottleneckAnalysis>> = try {
        val snapshot = firestore.collection(COLLECTION_BOTTLENECKS)
            .whereGreaterThan("impactScore", threshold)
            .orderBy("impactScore", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .await()
        
        val bottlenecks = snapshot.documents.mapNotNull { doc ->
            doc.data?.let { data ->
                BottleneckAnalysis(
                    id = data["id"] as? String ?: "",
                    workflowId = data["workflowId"] as? String,
                    workflowName = data["workflowName"] as? String ?: "",
                    bottleneckType = BottleneckType.valueOf(data["bottleneckType"] as? String ?: "MANUAL_INPUT"),
                    location = data["location"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    impactScore = data["impactScore"] as? Double ?: 0.0,
                    frequencyCount = (data["frequencyCount"] as? Long)?.toInt() ?: 0,
                    averageDelay = data["averageDelay"] as? Long ?: 0L,
                    affectedUsers = (data["affectedUsers"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet(),
                    detectedAt = LocalDateTime.parse(data["detectedAt"] as? String ?: LocalDateTime.now().toString()),
                    possibleCauses = (data["possibleCauses"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    suggestedFixes = (data["suggestedFixes"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                )
            }
        }
        
        Result.success(bottlenecks)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get high impact bottlenecks", e)
        Result.failure(e)
    }
    
    // ===== METRICS OPERATIONS =====
    
    /**
     * Record a system metric
     */
    suspend fun recordMetric(metric: SystemMetrics): Result<SystemMetrics> = try {
        firestore.collection(COLLECTION_METRICS)
            .document(metric.id)
            .set(mapOf(
                "id" to metric.id,
                "timestamp" to metric.timestamp.toString(),
                "metricType" to metric.metricType.name,
                "agentType" to metric.agentType?.name,
                "userId" to metric.userId,
                "value" to metric.value,
                "unit" to metric.unit,
                "context" to metric.context
            ))
            .await()
        
        Result.success(metric)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to record metric", e)
        Result.failure(e)
    }
    
    /**
     * Get metrics for analysis
     */
    suspend fun getMetrics(
        metricType: MetricType? = null,
        agentType: AgentType? = null,
        startTime: LocalDateTime? = null,
        limit: Int = 100
    ): Result<List<SystemMetrics>> = try {
        var query: Query = firestore.collection(COLLECTION_METRICS)
        
        if (metricType != null) {
            query = query.whereEqualTo("metricType", metricType.name)
        }
        
        if (agentType != null) {
            query = query.whereEqualTo("agentType", agentType.name)
        }
        
        query = query.orderBy("timestamp", Query.Direction.DESCENDING).limit(limit.toLong())
        
        val snapshot = query.get().await()
        val metrics = snapshot.documents.mapNotNull { doc ->
            doc.data?.let { data ->
                SystemMetrics(
                    id = data["id"] as? String ?: "",
                    timestamp = LocalDateTime.parse(data["timestamp"] as? String ?: LocalDateTime.now().toString()),
                    metricType = MetricType.valueOf(data["metricType"] as? String ?: "USER_ACTION_COUNT"),
                    agentType = (data["agentType"] as? String)?.let { AgentType.valueOf(it) },
                    userId = data["userId"] as? String,
                    value = data["value"] as? Double ?: 0.0,
                    unit = data["unit"] as? String ?: "",
                    @Suppress("UNCHECKED_CAST")
                    context = (data["context"] as? Map<String, Any>) ?: emptyMap()
                )
            }
        }
        
        Result.success(metrics)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get metrics", e)
        Result.failure(e)
    }
    
    // ===== AUTOMATION SUGGESTION OPERATIONS =====
    
    /**
     * Add an automation suggestion
     */
    suspend fun addAutomationSuggestion(suggestion: AutomationSuggestion): Result<AutomationSuggestion> = try {
        firestore.collection(COLLECTION_SUGGESTIONS)
            .document(suggestion.id)
            .set(mapOf(
                "id" to suggestion.id,
                "title" to suggestion.title,
                "description" to suggestion.description,
                "workflowPattern" to suggestion.workflowPattern?.let { pattern ->
                    mapOf(
                        "id" to pattern.id,
                        "name" to pattern.name,
                        "description" to pattern.description,
                        "frequency" to pattern.frequency,
                        "successRate" to pattern.successRate
                    )
                },
                "suggestedAutomationLevel" to suggestion.suggestedAutomationLevel.name,
                "currentAutomationLevel" to suggestion.currentAutomationLevel.name,
                "confidenceScore" to suggestion.confidenceScore,
                "estimatedTimeSavings" to suggestion.estimatedTimeSavings,
                "estimatedErrorReduction" to suggestion.estimatedErrorReduction,
                "requiredApprovals" to suggestion.requiredApprovals,
                "riskLevel" to suggestion.riskLevel.name,
                "benefits" to suggestion.benefits,
                "risks" to suggestion.risks,
                "implementationSteps" to suggestion.implementationSteps,
                "status" to suggestion.status.name,
                "createdAt" to suggestion.createdAt.toString(),
                "reviewedAt" to suggestion.reviewedAt?.toString(),
                "reviewedBy" to suggestion.reviewedBy,
                "reviewComments" to suggestion.reviewComments
            ))
            .await()
        
        _automationSuggestions.value = _automationSuggestions.value + suggestion
        
        Log.i(TAG, "Added automation suggestion: ${suggestion.title}")
        Result.success(suggestion)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to add automation suggestion", e)
        Result.failure(e)
    }
    
    /**
     * Update automation suggestion status
     */
    suspend fun updateSuggestionStatus(
        suggestionId: EntityId,
        status: SuggestionStatus,
        reviewedBy: String? = null,
        comments: String? = null
    ): Result<AutomationSuggestion> = try {
        val suggestion = _automationSuggestions.value.find { it.id == suggestionId }
            ?: return Result.failure(Exception("Suggestion not found"))
        
        val updated = suggestion.copy(
            status = status,
            reviewedAt = LocalDateTime.now(),
            reviewedBy = reviewedBy,
            reviewComments = comments
        )
        
        firestore.collection(COLLECTION_SUGGESTIONS)
            .document(suggestionId)
            .set(mapOf(
                "id" to updated.id,
                "title" to updated.title,
                "description" to updated.description,
                "suggestedAutomationLevel" to updated.suggestedAutomationLevel.name,
                "currentAutomationLevel" to updated.currentAutomationLevel.name,
                "confidenceScore" to updated.confidenceScore,
                "estimatedTimeSavings" to updated.estimatedTimeSavings,
                "estimatedErrorReduction" to updated.estimatedErrorReduction,
                "requiredApprovals" to updated.requiredApprovals,
                "riskLevel" to updated.riskLevel.name,
                "benefits" to updated.benefits,
                "risks" to updated.risks,
                "implementationSteps" to updated.implementationSteps,
                "status" to updated.status.name,
                "createdAt" to updated.createdAt.toString(),
                "reviewedAt" to updated.reviewedAt?.toString(),
                "reviewedBy" to updated.reviewedBy,
                "reviewComments" to updated.reviewComments
            ))
            .await()
        
        _automationSuggestions.value = _automationSuggestions.value.map {
            if (it.id == suggestionId) updated else it
        }
        
        Result.success(updated)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to update suggestion status", e)
        Result.failure(e)
    }
    
    /**
     * Get pending automation suggestions
     */
    suspend fun getPendingSuggestions(): Result<List<AutomationSuggestion>> = try {
        val suggestions = _automationSuggestions.value.filter { 
            it.status == SuggestionStatus.PENDING || it.status == SuggestionStatus.UNDER_REVIEW
        }.sortedByDescending { it.confidenceScore }
        
        Result.success(suggestions)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get pending suggestions", e)
        Result.failure(e)
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    private suspend fun loadKnowledgeEntries() {
        try {
            val snapshot = firestore.collection(COLLECTION_KNOWLEDGE)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(500)
                .get()
                .await()
            
            val entries = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    KnowledgeEntry(
                        id = data["id"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        content = data["content"] as? String ?: "",
                        category = KnowledgeCategory.valueOf(data["category"] as? String ?: "UNSTRUCTURED"),
                        tags = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        sourceType = KnowledgeSourceType.valueOf(data["sourceType"] as? String ?: "USER_INPUT"),
                        sourceReference = data["sourceReference"] as? String,
                        @Suppress("UNCHECKED_CAST")
                        metadata = (data["metadata"] as? Map<String, Any>) ?: emptyMap(),
                        createdAt = LocalDateTime.parse(data["createdAt"] as? String ?: LocalDateTime.now().toString()),
                        updatedAt = LocalDateTime.parse(data["updatedAt"] as? String ?: LocalDateTime.now().toString()),
                        accessCount = (data["accessCount"] as? Long)?.toInt() ?: 0,
                        relevanceScore = data["relevanceScore"] as? Double ?: 0.0,
                        verified = data["verified"] as? Boolean ?: false,
                        verifiedBy = data["verifiedBy"] as? String,
                        relatedEntries = (data["relatedEntries"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    )
                }
            }
            
            _knowledgeEntries.value = entries
            Log.i(TAG, "Loaded ${entries.size} knowledge entries")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load knowledge entries", e)
        }
    }
    
    private suspend fun loadWorkflowPatterns() {
        try {
            val snapshot = firestore.collection(COLLECTION_PATTERNS)
                .orderBy("frequency", Query.Direction.DESCENDING)
                .limit(200)
                .get()
                .await()
            
            val patterns = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    val stepsData = data["steps"] as? List<*> ?: emptyList<Any>()
                    WorkflowPattern(
                        id = data["id"] as? String ?: "",
                        name = data["name"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        steps = stepsData.mapNotNull { step ->
                            (step as? Map<*, *>)?.let { stepMap ->
                                WorkflowStep(
                                    stepNumber = (stepMap["stepNumber"] as? Long)?.toInt() ?: 0,
                                    action = stepMap["action"] as? String ?: "",
                                    agentType = (stepMap["agentType"] as? String)?.let { AgentType.valueOf(it) },
                                    @Suppress("UNCHECKED_CAST")
                                    parameters = (stepMap["parameters"] as? Map<String, Any>) ?: emptyMap(),
                                    averageDuration = stepMap["averageDuration"] as? Long ?: 0L,
                                    failureRate = stepMap["failureRate"] as? Double ?: 0.0
                                )
                            }
                        },
                        frequency = (data["frequency"] as? Long)?.toInt() ?: 0,
                        averageDuration = data["averageDuration"] as? Long ?: 0L,
                        lastExecuted = (data["lastExecuted"] as? String)?.let { LocalDateTime.parse(it) },
                        successRate = data["successRate"] as? Double ?: 0.0,
                        userIds = (data["userIds"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet(),
                        triggerConditions = (data["triggerConditions"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        automationReadiness = data["automationReadiness"] as? Double ?: 0.0,
                        @Suppress("UNCHECKED_CAST")
                        metadata = (data["metadata"] as? Map<String, Any>) ?: emptyMap()
                    )
                }
            }
            
            _workflowPatterns.value = patterns
            Log.i(TAG, "Loaded ${patterns.size} workflow patterns")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load workflow patterns", e)
        }
    }
    
    private suspend fun loadAutomationSuggestions() {
        try {
            val snapshot = firestore.collection(COLLECTION_SUGGESTIONS)
                .whereIn("status", listOf(
                    SuggestionStatus.PENDING.name,
                    SuggestionStatus.UNDER_REVIEW.name,
                    SuggestionStatus.APPROVED.name
                ))
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()
            
            val suggestions = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    AutomationSuggestion(
                        id = data["id"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        workflowPattern = null, // Simplified for now
                        suggestedAutomationLevel = AutomationLevel.valueOf(data["suggestedAutomationLevel"] as? String ?: "HUMAN_IN_LOOP"),
                        currentAutomationLevel = AutomationLevel.valueOf(data["currentAutomationLevel"] as? String ?: "MANUAL"),
                        confidenceScore = data["confidenceScore"] as? Double ?: 0.0,
                        estimatedTimeSavings = data["estimatedTimeSavings"] as? Long ?: 0L,
                        estimatedErrorReduction = data["estimatedErrorReduction"] as? Double ?: 0.0,
                        requiredApprovals = (data["requiredApprovals"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        riskLevel = RiskLevel.valueOf(data["riskLevel"] as? String ?: "MEDIUM"),
                        benefits = (data["benefits"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        risks = (data["risks"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        implementationSteps = (data["implementationSteps"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        status = SuggestionStatus.valueOf(data["status"] as? String ?: "PENDING"),
                        createdAt = LocalDateTime.parse(data["createdAt"] as? String ?: LocalDateTime.now().toString()),
                        reviewedAt = (data["reviewedAt"] as? String)?.let { LocalDateTime.parse(it) },
                        reviewedBy = data["reviewedBy"] as? String,
                        reviewComments = data["reviewComments"] as? String
                    )
                }
            }
            
            _automationSuggestions.value = suggestions
            Log.i(TAG, "Loaded ${suggestions.size} automation suggestions")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load automation suggestions", e)
        }
    }
}
