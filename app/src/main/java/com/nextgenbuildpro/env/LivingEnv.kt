package com.nextgenbuildpro.env

import com.nextgenbuildpro.shared.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import android.util.Log
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * LivingEnv (Living Environment Mesh)
 * 
 * The Living Environment Mesh serves as the intelligent, adaptive network layer
 * that connects all agents, applications, and services in the NextGen AI OS.
 * It provides real-time communication, context awareness, and dynamic adaptation
 * to changing conditions.
 */
class LivingEnv : EnvironmentMesh {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mutex = Mutex()
    
    // Environment state
    private val _environmentContext = MutableStateFlow(createInitialContext())
    val environmentContext: StateFlow<EnvironmentContext> = _environmentContext.asStateFlow()
    
    // Agent registry and management
    private val registeredAgents = ConcurrentHashMap<AgentType, NextGenAgent>()
    private val agentChannels = ConcurrentHashMap<AgentType, Channel<AgentMessage>>()
    private val agentStatuses = ConcurrentHashMap<AgentType, SystemStatus>()
    
    // Communication infrastructure
    private val messageRouter = MessageRouter()
    private val contextManager = ContextManager()
    private val adaptationEngine = AdaptationEngine()
    private val networkMonitor = NetworkMonitor()
    
    // Environment data
    private val environmentMetrics = mutableListOf<EnvironmentMetrics>()
    private val communicationHistory = mutableListOf<CommunicationRecord>()
    private val adaptationHistory = mutableListOf<AdaptationRecord>()
    
    // Configuration
    private val config = EnvironmentConfig(
        maxConcurrentMessages = 1000,
        adaptationThreshold = 0.7,
        contextUpdateInterval = 5000L, // 5 seconds
        networkHealthCheckInterval = 10000L, // 10 seconds
        messageRetentionPeriod = 86400000L, // 24 hours
        autoOptimizationEnabled = true
    )
    
    init {
        initializeLivingEnvironment()
    }
    
    override suspend fun registerAgent(agent: NextGenAgent): Result<Unit> = try {
        mutex.withLock {
            Log.i("LivingEnv", "Registering agent: ${agent.agentType}")
            
            // Register the agent
            registeredAgents[agent.agentType] = agent
            
            // Create communication channel
            val channel = Channel<AgentMessage>(Channel.UNLIMITED)
            agentChannels[agent.agentType] = channel
            
            // Monitor agent status
            monitorAgentStatus(agent)
            
            // Update environment context
            updateEnvironmentContext()
            
            // Notify other agents of new registration
            broadcastAgentRegistration(agent.agentType)
            
            Log.i("LivingEnv", "Agent ${agent.agentType} registered successfully")
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("LivingEnv", "Failed to register agent: ${agent.agentType}", e)
        Result.failure(e)
    }
    
    override suspend fun unregisterAgent(agentType: AgentType): Result<Unit> = try {
        mutex.withLock {
            Log.i("LivingEnv", "Unregistering agent: $agentType")
            
            // Remove from registry
            registeredAgents.remove(agentType)
            
            // Close communication channel
            agentChannels[agentType]?.close()
            agentChannels.remove(agentType)
            
            // Remove status tracking
            agentStatuses.remove(agentType)
            
            // Update environment context
            updateEnvironmentContext()
            
            // Notify other agents of unregistration
            broadcastAgentUnregistration(agentType)
            
            Log.i("LivingEnv", "Agent $agentType unregistered successfully")
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("LivingEnv", "Failed to unregister agent: $agentType", e)
        Result.failure(e)
    }
    
    override suspend fun broadcastMessage(message: AgentMessage): Result<Unit> = try {
        Log.d("LivingEnv", "Broadcasting message: ${message.messageType}")
        
        val recipients = registeredAgents.keys.filter { it != message.fromAgent }
        var successCount = 0
        
        recipients.forEach { agentType ->
            val result = routeMessageToAgent(message.copy(toAgent = agentType))
            if (result.isSuccess) successCount++
        }
        
        // Record broadcast statistics
        recordBroadcastMetrics(message, recipients.size, successCount)
        
        if (successCount > 0) {
            Result.success(Unit)
        } else {
            Result.failure(RuntimeException("Broadcast failed to all recipients"))
        }
    } catch (e: Exception) {
        Log.e("LivingEnv", "Error broadcasting message", e)
        Result.failure(e)
    }
    
    override suspend fun routeMessage(message: AgentMessage): Result<Unit> = try {
        Log.d("LivingEnv", "Routing message from ${message.fromAgent} to ${message.toAgent}")
        
        // Apply message routing intelligence
        val optimizedMessage = messageRouter.optimizeMessage(message)
        
        // Route to specific agent
        val result = routeMessageToAgent(optimizedMessage)
        
        // Record communication
        recordCommunication(optimizedMessage, result.isSuccess)
        
        // Trigger adaptation if needed
        triggerAdaptationIfNeeded(optimizedMessage, result)
        
        result
    } catch (e: Exception) {
        Log.e("LivingEnv", "Error routing message", e)
        Result.failure(e)
    }
    
    override suspend fun getEnvironmentContext(): EnvironmentContext = _environmentContext.value
    
    override suspend fun updateContext(context: EnvironmentContext): Result<Unit> = try {
        mutex.withLock {
            Log.d("LivingEnv", "Updating environment context")
            
            _environmentContext.value = context
            
            // Trigger context-based adaptations
            contextManager.processContextUpdate(context)
            
            // Notify all agents of context change
            notifyAgentsOfContextChange(context)
            
            // Record context change
            recordContextChange(context)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("LivingEnv", "Error updating context", e)
        Result.failure(e)
    }
    
    // === ENVIRONMENT INTELLIGENCE METHODS ===
    
    suspend fun optimizeEnvironment(): EnvironmentOptimization {
        return try {
            Log.d("LivingEnv", "Optimizing living environment...")
            
            val currentMetrics = gatherEnvironmentMetrics()
            val bottlenecks = identifyBottlenecks(currentMetrics)
            val optimizations = generateOptimizations(bottlenecks)
            val adaptations = applyAdaptations(optimizations)
            
            EnvironmentOptimization(
                metricsAnalyzed = currentMetrics,
                bottlenecksIdentified = bottlenecks,
                optimizationsApplied = optimizations,
                adaptationsExecuted = adaptations,
                performanceImprovement = calculatePerformanceImprovement(currentMetrics, adaptations),
                timestamp = LocalDateTime.now()
            )
        } catch (e: Exception) {
            Log.e("LivingEnv", "Error optimizing environment", e)
            EnvironmentOptimization(mapOf(), listOf(), listOf(), listOf(), 0.0, LocalDateTime.now())
        }
    }
    
    suspend fun monitorNetworkHealth(): NetworkHealthReport {
        return try {
            Log.d("LivingEnv", "Monitoring network health...")
            
            val latency = networkMonitor.measureAverageLatency()
            val throughput = networkMonitor.measureThroughput()
            val errorRate = networkMonitor.calculateErrorRate()
            val congestionLevel = networkMonitor.assessCongestion()
            
            val healthScore = calculateNetworkHealthScore(latency, throughput, errorRate, congestionLevel)
            val recommendations = generateNetworkRecommendations(healthScore, latency, throughput, errorRate)
            
            NetworkHealthReport(
                overallHealth = healthScore,
                averageLatency = latency,
                throughput = throughput,
                errorRate = errorRate,
                congestionLevel = congestionLevel,
                activeConnections = registeredAgents.size,
                recommendations = recommendations,
                timestamp = LocalDateTime.now()
            )
        } catch (e: Exception) {
            Log.e("LivingEnv", "Error monitoring network health", e)
            NetworkHealthReport(0.0, 0.0, 0.0, 1.0, "high", 0, listOf(), LocalDateTime.now())
        }
    }
    
    suspend fun adaptToLoadChanges(loadMetrics: LoadMetrics): AdaptationResult {
        return try {
            Log.d("LivingEnv", "Adapting to load changes...")
            
            val adaptationStrategy = adaptationEngine.analyzeLoadChanges(loadMetrics)
            val recommendations = adaptationEngine.generateAdaptations(adaptationStrategy)
            val results = adaptationEngine.executeAdaptations(recommendations)
            
            // Update environment configuration based on adaptations
            updateEnvironmentConfiguration(results)
            
            AdaptationResult(
                strategy = adaptationStrategy,
                adaptationsApplied = recommendations,
                results = results,
                effectivenessScore = calculateAdaptationEffectiveness(results),
                timestamp = LocalDateTime.now()
            )
        } catch (e: Exception) {
            Log.e("LivingEnv", "Error adapting to load changes", e)
            AdaptationResult("fallback", listOf(), mapOf(), 0.0, LocalDateTime.now())
        }
    }
    
    suspend fun facilitateAgentCollaboration(collaborationRequest: AgentCollaborationRequest): CollaborationFacilitation {
        return try {
            Log.d("LivingEnv", "Facilitating agent collaboration...")
            
            val participatingAgents = collaborationRequest.participantTypes.mapNotNull { registeredAgents[it] }
            val collaborationContext = createCollaborationContext(collaborationRequest, participatingAgents)
            val communicationPlan = designCommunicationPlan(collaborationContext)
            val coordination = coordinateCollaboration(participatingAgents, communicationPlan)
            
            CollaborationFacilitation(
                requestId = collaborationRequest.id,
                participantsCount = participatingAgents.size,
                collaborationContext = collaborationContext,
                communicationPlan = communicationPlan,
                coordinationResult = coordination,
                expectedOutcome = predictCollaborationOutcome(coordination),
                timestamp = LocalDateTime.now()
            )
        } catch (e: Exception) {
            Log.e("LivingEnv", "Error facilitating collaboration", e)
            CollaborationFacilitation("error", 0, mapOf(), listOf(), "failed", "unsuccessful", LocalDateTime.now())
        }
    }
    
    // === PRIVATE METHODS ===
    
    private fun initializeLivingEnvironment() {
        Log.i("LivingEnv", "Initializing Living Environment Mesh...")
        
        // Start background monitoring processes
        startContextMonitoring()
        startNetworkMonitoring()
        startAdaptationEngine()
        startPerformanceTracking()
        
        Log.i("LivingEnv", "Living Environment Mesh initialized successfully")
    }
    
    private fun createInitialContext(): EnvironmentContext {
        return EnvironmentContext(
            locationId = "default_location",
            environmentType = EnvironmentType.HYBRID,
            activeAgents = emptySet(),
            currentTasks = emptyList(),
            systemLoad = 0.0f,
            networkQuality = NetworkQuality.GOOD,
            timestamp = LocalDateTime.now()
        )
    }
    
    private fun monitorAgentStatus(agent: NextGenAgent) {
        scope.launch {
            agent.status.collect { status ->
                agentStatuses[agent.agentType] = status
                
                // Update environment context when agent status changes
                if (status == SystemStatus.ERROR || status == SystemStatus.SHUTDOWN) {
                    handleAgentStatusChange(agent.agentType, status)
                }
            }
        }
    }
    
    private suspend fun routeMessageToAgent(message: AgentMessage): Result<Unit> {
        return try {
            val targetAgent = registeredAgents[message.toAgent]
            if (targetAgent != null) {
                // Process message through the agent
                val response = targetAgent.processMessage(message)
                
                // Handle response if present
                response.getOrNull()?.let { responseMessage ->
                    if (responseMessage.toAgent != message.fromAgent) {
                        // Route response to appropriate agent
                        routeMessage(responseMessage)
                    }
                }
                
                Result.success(Unit)
            } else {
                Log.w("LivingEnv", "Target agent not registered: ${message.toAgent}")
                Result.failure(IllegalArgumentException("Agent not registered: ${message.toAgent}"))
            }
        } catch (e: Exception) {
            Log.e("LivingEnv", "Error routing message to agent: ${message.toAgent}", e)
            Result.failure(e)
        }
    }
    
    private suspend fun updateEnvironmentContext() {
        val currentContext = _environmentContext.value
        val updatedContext = currentContext.copy(
            activeAgents = registeredAgents.keys.toSet(),
            systemLoad = calculateSystemLoad(),
            networkQuality = assessNetworkQuality(),
            timestamp = LocalDateTime.now()
        )
        
        _environmentContext.value = updatedContext
    }
    
    private suspend fun broadcastAgentRegistration(agentType: AgentType) {
        val message = AgentMessage(
            fromAgent = AgentType.COO_OPERATIONS_ORCHESTRATOR,
            toAgent = AgentType.COO_OPERATIONS_ORCHESTRATOR, // Will be overridden for each recipient
            messageType = MessageType.NOTIFICATION,
            content = "Agent registered: $agentType",
            metadata = mapOf("event" to "agent_registration", "agent_type" to agentType.name)
        )
        
        broadcastMessage(message)
    }
    
    private suspend fun broadcastAgentUnregistration(agentType: AgentType) {
        val message = AgentMessage(
            fromAgent = AgentType.COO_OPERATIONS_ORCHESTRATOR,
            toAgent = AgentType.COO_OPERATIONS_ORCHESTRATOR, // Will be overridden for each recipient
            messageType = MessageType.NOTIFICATION,
            content = "Agent unregistered: $agentType",
            metadata = mapOf("event" to "agent_unregistration", "agent_type" to agentType.name)
        )
        
        broadcastMessage(message)
    }
    
    private fun recordCommunication(message: AgentMessage, success: Boolean) {
        val record = CommunicationRecord(
            messageId = message.id,
            fromAgent = message.fromAgent,
            toAgent = message.toAgent,
            messageType = message.messageType,
            success = success,
            timestamp = LocalDateTime.now(),
            latency = 0.0 // Would be measured in real implementation
        )
        
        communicationHistory.add(record)
        
        // Keep history within limits
        if (communicationHistory.size > 10000) {
            communicationHistory.removeAt(0)
        }
    }
    
    private suspend fun triggerAdaptationIfNeeded(message: AgentMessage, result: Result<Unit>) {
        if (result.isFailure || shouldTriggerAdaptation(message)) {
            val adaptationTrigger = AdaptationTrigger(
                trigger = if (result.isFailure) "communication_failure" else "performance_threshold",
                context = mapOf(
                    "message_type" to message.messageType.name,
                    "from_agent" to message.fromAgent.name,
                    "to_agent" to message.toAgent.name
                ),
                timestamp = LocalDateTime.now()
            )
            
            adaptationEngine.processAdaptationTrigger(adaptationTrigger)
        }
    }
    
    private fun startContextMonitoring() {
        scope.launch {
            while (true) {
                try {
                    kotlinx.coroutines.delay(config.contextUpdateInterval)
                    updateEnvironmentContext()
                } catch (e: Exception) {
                    Log.e("LivingEnv", "Error in context monitoring", e)
                }
            }
        }
    }
    
    private fun startNetworkMonitoring() {
        scope.launch {
            while (true) {
                try {
                    kotlinx.coroutines.delay(config.networkHealthCheckInterval)
                    val healthReport = monitorNetworkHealth()
                    
                    if (healthReport.overallHealth < 0.5) {
                        Log.w("LivingEnv", "Network health degraded: ${healthReport.overallHealth}")
                        // Trigger network optimizations
                        networkMonitor.optimizeNetwork(healthReport)
                    }
                } catch (e: Exception) {
                    Log.e("LivingEnv", "Error in network monitoring", e)
                }
            }
        }
    }
    
    private fun startAdaptationEngine() {
        scope.launch {
            adaptationEngine.startContinuousAdaptation(config)
        }
    }
    
    private fun startPerformanceTracking() {
        scope.launch {
            while (true) {
                try {
                    kotlinx.coroutines.delay(30000) // 30 seconds
                    gatherEnvironmentMetrics()
                    environmentMetrics.add(EnvironmentMetrics(
                        timestamp = LocalDateTime.now(),
                        activeAgents = registeredAgents.size,
                        messagesPerSecond = calculateMessagesPerSecond(),
                        averageLatency = calculateAverageLatency(),
                        systemLoad = calculateSystemLoad(),
                        networkQuality = assessNetworkQuality()
                    ))
                    
                    // Keep metrics history manageable
                    if (environmentMetrics.size > 1000) {
                        environmentMetrics.removeAt(0)
                    }
                } catch (e: Exception) {
                    Log.e("LivingEnv", "Error in performance tracking", e)
                }
            }
        }
    }
    
    // Helper classes and data structures
    
    private data class EnvironmentConfig(
        val maxConcurrentMessages: Int,
        val adaptationThreshold: Double,
        val contextUpdateInterval: Long,
        val networkHealthCheckInterval: Long,
        val messageRetentionPeriod: Long,
        val autoOptimizationEnabled: Boolean
    )
    
    private data class CommunicationRecord(
        val messageId: String,
        val fromAgent: AgentType,
        val toAgent: AgentType,
        val messageType: MessageType,
        val success: Boolean,
        val timestamp: LocalDateTime,
        val latency: Double
    )
    
    private data class EnvironmentMetrics(
        val timestamp: LocalDateTime,
        val activeAgents: Int,
        val messagesPerSecond: Double,
        val averageLatency: Double,
        val systemLoad: Float,
        val networkQuality: NetworkQuality
    )
    
    data class EnvironmentOptimization(
        val metricsAnalyzed: Map<String, Any>,
        val bottlenecksIdentified: List<String>,
        val optimizationsApplied: List<String>,
        val adaptationsExecuted: List<String>,
        val performanceImprovement: Double,
        val timestamp: LocalDateTime
    )

    data class NetworkHealthReport(
        val overallHealth: Double,
        val averageLatency: Double,
        val throughput: Double,
        val errorRate: Double,
        val congestionLevel: String,
        val activeConnections: Int,
        val recommendations: List<String>,
        val timestamp: LocalDateTime
    )

    data class LoadMetrics(
        val cpuUsage: Double,
        val memoryUsage: Double,
        val networkUtilization: Double,
        val messageQueueSize: Int,
        val responseTime: Double
    )

    data class AdaptationResult(
        val strategy: String,
        val adaptationsApplied: List<String>,
        val results: Map<String, Any>,
        val effectivenessScore: Double,
        val timestamp: LocalDateTime
    )
    
    private data class AdaptationTrigger(
        val trigger: String,
        val context: Map<String, Any>,
        val timestamp: LocalDateTime
    )
    
    private data class AdaptationRecord(
        val trigger: AdaptationTrigger,
        val result: AdaptationResult,
        val timestamp: LocalDateTime
    )
    
    data class AgentCollaborationRequest(
        val id: String,
        val participantTypes: List<AgentType>,
        val objective: String,
        val timeframe: String,
        val constraints: List<String>
    )

    data class CollaborationFacilitation(
        val requestId: String,
        val participantsCount: Int,
        val collaborationContext: Map<String, Any>,
        val communicationPlan: List<String>,
        val coordinationResult: String,
        val expectedOutcome: String,
        val timestamp: LocalDateTime
    )
    
    // Helper classes for environment intelligence
    
    private class MessageRouter {
        fun optimizeMessage(message: AgentMessage): AgentMessage {
            // Apply message optimization logic
            return message.copy(
                metadata = message.metadata + mapOf(
                    "route_timestamp" to LocalDateTime.now().toString(),
                    "optimization_applied" to true
                )
            )
        }
    }
    
    private class ContextManager {
        fun processContextUpdate(context: EnvironmentContext) {
            // Process context updates and trigger necessary adaptations
            Log.d("LivingEnv", "Processing context update for environment: ${context.environmentType}")
        }
    }
    
    private class AdaptationEngine {
        fun analyzeLoadChanges(loadMetrics: LoadMetrics): String {
            return when {
                loadMetrics.cpuUsage > 0.8 -> "scale_up"
                loadMetrics.cpuUsage < 0.3 -> "scale_down"
                loadMetrics.responseTime > 1000 -> "optimize_performance"
                else -> "maintain"
            }
        }
        
        fun generateAdaptations(strategy: String): List<String> {
            return when (strategy) {
                "scale_up" -> listOf("increase_worker_threads", "optimize_message_queues")
                "scale_down" -> listOf("reduce_worker_threads", "consolidate_resources")
                "optimize_performance" -> listOf("cache_optimization", "message_batching")
                else -> listOf("monitor_continue")
            }
        }
        
        fun executeAdaptations(adaptations: List<String>): Map<String, Any> {
            val results = mutableMapOf<String, Any>()
            adaptations.forEach { adaptation ->
                results[adaptation] = executeAdaptation(adaptation)
            }
            return results
        }
        
        private fun executeAdaptation(adaptation: String): Boolean {
            Log.d("LivingEnv", "Executing adaptation: $adaptation")
            // Execute specific adaptation logic
            return true
        }
        
        fun processAdaptationTrigger(trigger: AdaptationTrigger) {
            Log.d("LivingEnv", "Processing adaptation trigger: ${trigger.trigger}")
            // Process adaptation trigger and queue necessary adaptations
        }
        
        suspend fun startContinuousAdaptation(config: EnvironmentConfig) {
            // Start continuous adaptation monitoring
            Log.d("LivingEnv", "Starting continuous adaptation engine")
        }
    }
    
    private class NetworkMonitor {
        fun measureAverageLatency(): Double = 50.0 // Placeholder
        fun measureThroughput(): Double = 1000.0 // Placeholder
        fun calculateErrorRate(): Double = 0.01 // Placeholder
        fun assessCongestion(): String = "low" // Placeholder
        
        fun optimizeNetwork(healthReport: NetworkHealthReport) {
            Log.d("LivingEnv", "Optimizing network based on health report")
            // Implement network optimization logic
        }
    }
    
    private fun handleAgentStatusChange(agentType: AgentType, status: SystemStatus) {
        Log.w("LivingEnv", "Agent status change: $agentType → $status")
        agentStatuses[agentType] = status
        if (status == SystemStatus.ERROR || status == SystemStatus.SHUTDOWN) {
            registeredAgents.remove(agentType)
            agentChannels[agentType]?.close()
            agentChannels.remove(agentType)
            scope.launch { updateEnvironmentContext() }
        }
    }

    private fun calculateSystemLoad(): Float {
        val rt = Runtime.getRuntime()
        return ((rt.totalMemory() - rt.freeMemory()).toFloat() / rt.maxMemory().toFloat()).coerceIn(0f, 1f)
    }

    private fun assessNetworkQuality(): NetworkQuality {
        val errorRate = networkMonitor.calculateErrorRate()
        val latency = networkMonitor.measureAverageLatency()
        return when {
            errorRate > 0.1 || latency > 500 -> NetworkQuality.POOR
            errorRate > 0.05 || latency > 200 -> NetworkQuality.FAIR
            else -> NetworkQuality.GOOD
        }
    }

    private fun recordBroadcastMetrics(message: AgentMessage, recipients: Int, successCount: Int) {
        val rate = if (recipients > 0) successCount.toDouble() / recipients else 1.0
        Log.d("LivingEnv", "Broadcast [${message.messageType}] → $successCount/$recipients (${(rate * 100).toInt()}%)")
        val record = CommunicationRecord(
            messageId = message.id,
            fromAgent = message.fromAgent,
            toAgent = message.fromAgent,
            messageType = message.messageType,
            success = successCount > 0,
            timestamp = LocalDateTime.now(),
            latency = 0.0
        )
        communicationHistory.add(record)
        if (communicationHistory.size > 10_000) communicationHistory.removeAt(0)
    }

    private fun notifyAgentsOfContextChange(context: EnvironmentContext) {
        scope.launch {
            val notification = AgentMessage(
                fromAgent = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                toAgent = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                messageType = MessageType.STATUS_UPDATE,
                content = "Environment context updated",
                metadata = mapOf(
                    "event" to "context_change",
                    "environment_type" to context.environmentType.name,
                    "active_agents" to context.activeAgents.size.toString(),
                    "system_load" to context.systemLoad.toString(),
                    "network_quality" to context.networkQuality.name
                )
            )
            registeredAgents.keys.forEach { agentType ->
                routeMessageToAgent(notification.copy(toAgent = agentType))
            }
        }
    }

    private fun recordContextChange(context: EnvironmentContext) {
        Log.d("LivingEnv", "Context recorded — env: ${context.environmentType}, agents: ${context.activeAgents.size}, load: ${context.systemLoad}")
        environmentMetrics.add(EnvironmentMetrics(
            timestamp = LocalDateTime.now(),
            activeAgents = registeredAgents.size,
            messagesPerSecond = calculateMessagesPerSecond(),
            averageLatency = calculateAverageLatency(),
            systemLoad = context.systemLoad,
            networkQuality = context.networkQuality
        ))
        if (environmentMetrics.size > 1_000) environmentMetrics.removeAt(0)
    }

    private fun gatherEnvironmentMetrics(): Map<String, Any> {
        val rt = Runtime.getRuntime()
        val usedMem = rt.totalMemory() - rt.freeMemory()
        return mapOf(
            "active_agents" to registeredAgents.size,
            "history_size" to communicationHistory.size,
            "memory_used_bytes" to usedMem,
            "memory_max_bytes" to rt.maxMemory(),
            "memory_pct" to (usedMem.toDouble() / rt.maxMemory() * 100).toInt(),
            "timestamp" to LocalDateTime.now().toString()
        )
    }

    private fun identifyBottlenecks(metrics: Map<String, Any>): List<String> {
        val bottlenecks = mutableListOf<String>()
        val memPct = (metrics["memory_pct"] as? Int) ?: 0
        val histSize = (metrics["history_size"] as? Int) ?: 0
        if (memPct > 80) bottlenecks.add("high_memory:${memPct}pct")
        if (histSize > 8_000) bottlenecks.add("large_history:$histSize")
        if (registeredAgents.isEmpty()) bottlenecks.add("no_active_agents")
        return bottlenecks
    }

    private fun generateOptimizations(bottlenecks: List<String>): List<String> =
        bottlenecks.map { b ->
            when {
                b.startsWith("high_memory") -> "optimize_message_queues"
                b.startsWith("large_history") -> "prune_old_records"
                b.startsWith("no_active_agents") -> "restart_core_agents"
                else -> "log_and_monitor"
            }
        }

    private fun applyAdaptations(optimizations: List<String>): List<String> {
        val applied = mutableListOf<String>()
        optimizations.forEach { opt ->
            when (opt) {
                "optimize_message_queues", "prune_old_records" -> {
                    if (communicationHistory.size > 5_000) {
                        communicationHistory.subList(0, communicationHistory.size / 2).clear()
                    }
                    applied.add(opt)
                }
                else -> { Log.d("LivingEnv", "Queuing adaptation for review: $opt"); applied.add(opt) }
            }
        }
        return applied
    }

    private fun calculatePerformanceImprovement(metrics: Map<String, Any>, adaptations: List<String>): Double =
        (adaptations.size * 0.05).coerceAtMost(0.5)

    private fun calculateNetworkHealthScore(latency: Double, throughput: Double, errorRate: Double, congestion: String): Double {
        val l = (1.0 - (latency / 1000.0).coerceIn(0.0, 1.0))
        val t = (throughput / 10_000.0).coerceIn(0.0, 1.0)
        val e = (1.0 - errorRate.coerceIn(0.0, 1.0))
        val c = when (congestion) { "low" -> 1.0; "medium" -> 0.7; else -> 0.3 }
        return ((l + t + e + c) / 4.0).coerceIn(0.0, 1.0)
    }

    private fun generateNetworkRecommendations(health: Double, latency: Double, throughput: Double, errorRate: Double): List<String> {
        val recs = mutableListOf<String>()
        if (health < 0.5) recs.add("Network health critical (${(health * 100).toInt()}%) — investigate immediately")
        if (latency > 200) recs.add("High latency ${latency}ms — check message routing")
        if (throughput < 100) recs.add("Low throughput ${throughput}/s — consider message batching")
        if (errorRate > 0.05) recs.add("Error rate ${(errorRate * 100).toInt()}% — check agent availability")
        return recs
    }

    private fun updateEnvironmentConfiguration(results: Map<String, Any>) {
        val failures = results.values.count { it == false }
        val successes = results.values.count { it == true }
        Log.i("LivingEnv", "Adaptation results — success: $successes, failed: $failures")
        results.forEach { (adaptation, result) ->
            when (adaptation) {
                "consolidate_resources" -> {
                    if (environmentMetrics.size > 500)
                        environmentMetrics.subList(0, environmentMetrics.size / 2).clear()
                }
                else -> Log.d("LivingEnv", "Adaptation '$adaptation': $result")
            }
        }
    }

    private fun calculateAdaptationEffectiveness(results: Map<String, Any>): Double {
        if (results.isEmpty()) return 1.0
        return (results.values.count { it == true }.toDouble() / results.size).coerceIn(0.0, 1.0)
    }

    private fun createCollaborationContext(request: AgentCollaborationRequest, agents: List<NextGenAgent>): Map<String, Any> =
        mapOf(
            "request_id" to request.id,
            "objective" to request.objective,
            "timeframe" to request.timeframe,
            "participant_count" to agents.size,
            "participant_types" to agents.map { it.agentType.name },
            "constraints" to request.constraints,
            "created_at" to LocalDateTime.now().toString()
        )

    private fun designCommunicationPlan(context: Map<String, Any>): List<String> {
        val count = (context["participant_count"] as? Int) ?: 0
        return when {
            count <= 2 -> listOf("direct_messaging", "shared_context")
            count <= 5 -> listOf("hub_spoke_routing", "shared_context", "status_broadcasts")
            else -> listOf("hierarchical_routing", "shared_context", "batched_updates", "coordinator_agent")
        }
    }

    private fun coordinateCollaboration(agents: List<NextGenAgent>, plan: List<String>): String =
        if (agents.isEmpty()) "no_participants"
        else if (plan.contains("coordinator_agent")) "hierarchical_coordination"
        else "peer_coordination"

    private fun predictCollaborationOutcome(coordination: String): String = when (coordination) {
        "hierarchical_coordination" -> "structured_outcome_likely"
        "peer_coordination" -> "collaborative_outcome_likely"
        else -> "outcome_uncertain"
    }

    private fun shouldTriggerAdaptation(message: AgentMessage): Boolean =
        communicationHistory.takeLast(10).count { !it.success } >= 3

    private fun calculateMessagesPerSecond(): Double {
        if (communicationHistory.size < 2) return 0.0
        val recent = communicationHistory.takeLast(100)
        if (recent.size < 2) return 0.0
        val durationSec = java.time.Duration.between(recent.first().timestamp, recent.last().timestamp).toMillis() / 1000.0
        return if (durationSec > 0) recent.size / durationSec else 0.0
    }

    private fun calculateAverageLatency(): Double =
        communicationHistory.takeLast(50).map { it.latency }.average().takeIf { !it.isNaN() } ?: 0.0
}