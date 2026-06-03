package agents

import shared.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * MRM (Main Resource Manager)
 * Responsible for resource allocation, system optimization, and high-level coordination
 */
class MRM(
    override val id: String = "mrm-001",
    private val config: AgentConfig
) : Agent {
    
    override val type = AgentType.MRM
    override var state = AgentState.IDLE
        private set
    
    private val resourcePool = mutableMapOf<String, Resource>()
    private val allocationHistory = mutableListOf<AllocationEvent>()
    private val performanceMetrics = mutableMapOf<String, Float>()
    
    private var isActive = false
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    data class Resource(
        val id: String,
        val type: ResourceType,
        val capacity: Float,
        val currentUsage: Float,
        val efficiency: Float,
        val lastUpdated: Long = System.currentTimeMillis()
    )
    
    enum class ResourceType {
        COMPUTATIONAL,
        MEMORY,
        NETWORK,
        STORAGE,
        ENERGY,
        HUMAN_ATTENTION
    }
    
    data class AllocationEvent(
        val resourceId: String,
        val requestingAgent: AgentType,
        val amount: Float,
        val granted: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    init {
        initializeResources()
        startResourceMonitoring()
    }
    
    override suspend fun process(message: Message): Message? {
        state = AgentState.PROCESSING
        
        return try {
            when (message.content) {
                "REQUEST_RESOURCE" -> handleResourceRequest(message)
                "OPTIMIZE_ALLOCATION" -> handleOptimizationRequest(message)
                "STATUS_REPORT" -> generateStatusReport(message)
                "PERFORMANCE_METRICS" -> getPerformanceMetrics(message)
                else -> handleGenericMessage(message)
            }
        } catch (e: Exception) {
            state = AgentState.ERROR
            Message(
                id = generateMessageId(),
                fromAgent = type,
                toAgent = message.fromAgent,
                content = "ERROR: ${e.message}",
                priority = Priority.HIGH
            )
        } finally {
            state = AgentState.IDLE
        }
    }
    
    override suspend fun learn(event: LearningEvent) {
        when (event.eventType) {
            LearningType.PATTERN_RECOGNITION -> {
                // Learn resource usage patterns
                analyzeUsagePatterns()
            }
            LearningType.BEHAVIOR_ADAPTATION -> {
                // Adapt allocation strategies based on performance
                adaptAllocationStrategy(event.improvement)
            }
            LearningType.KNOWLEDGE_INTEGRATION -> {
                // Integrate new knowledge about resource efficiency
                updateResourceEfficiency(event.data)
            }
            LearningType.SKILL_ACQUISITION -> {
                // Acquire new optimization techniques
                learnOptimizationTechnique(event.data)
            }
        }
    }
    
    override fun getCapabilities(): List<String> {
        return listOf(
            "Resource Allocation",
            "System Optimization",
            "Performance Monitoring",
            "Load Balancing",
            "Predictive Analytics",
            "Multi-Agent Coordination",
            "Energy Management",
            "Capacity Planning"
        )
    }
    
    fun start() {
        isActive = true
        startResourceMonitoring()
        startOptimizationCycle()
    }
    
    fun stop() {
        isActive = false
        scope.cancel()
    }
    
    private fun initializeResources() {
        // Initialize system resources
        resourcePool["cpu"] = Resource("cpu", ResourceType.COMPUTATIONAL, 100f, 0f, 0.95f)
        resourcePool["memory"] = Resource("memory", ResourceType.MEMORY, 32000f, 0f, 0.90f)
        resourcePool["network"] = Resource("network", ResourceType.NETWORK, 1000f, 0f, 0.85f)
        resourcePool["storage"] = Resource("storage", ResourceType.STORAGE, 500000f, 0f, 0.98f)
        resourcePool["energy"] = Resource("energy", ResourceType.ENERGY, 100f, 0f, 0.80f)
        resourcePool["attention"] = Resource("attention", ResourceType.HUMAN_ATTENTION, 10f, 0f, 0.60f)
    }
    
    private fun startResourceMonitoring() {
        scope.launch {
            while (isActive) {
                monitorResourceUsage()
                updatePerformanceMetrics()
                delay(5000) // Monitor every 5 seconds
            }
        }
    }
    
    private fun startOptimizationCycle() {
        scope.launch {
            while (isActive) {
                optimizeResourceAllocation()
                rebalanceLoad()
                delay(30000) // Optimize every 30 seconds
            }
        }
    }
    
    private suspend fun handleResourceRequest(message: Message): Message {
        val requestedResource = message.metadata["resource"] ?: return createErrorMessage(message, "No resource specified")
        val requestedAmount = message.metadata["amount"]?.toFloatOrNull() ?: return createErrorMessage(message, "Invalid amount")
        
        val allocation = allocateResource(requestedResource, requestedAmount, message.fromAgent)
        
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = if (allocation.granted) "RESOURCE_GRANTED" else "RESOURCE_DENIED",
            metadata = mapOf(
                "resource" to requestedResource,
                "amount" to allocation.amount.toString(),
                "granted" to allocation.granted.toString()
            )
        )
    }
    
    private suspend fun handleOptimizationRequest(message: Message): Message {
        val optimizationResults = optimizeResourceAllocation()
        
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = "OPTIMIZATION_COMPLETE",
            metadata = mapOf(
                "improvements" to optimizationResults.toString(),
                "efficiency_gain" to calculateEfficiencyGain().toString()
            )
        )
    }
    
    private suspend fun generateStatusReport(message: Message): Message {
        val report = buildString {
            appendLine("=== MRM Status Report ===")
            appendLine("State: $state")
            appendLine("Active Resources: ${resourcePool.size}")
            appendLine("Allocation Events: ${allocationHistory.size}")
            appendLine("System Efficiency: ${calculateOverallEfficiency()}%")
            appendLine()
            
            resourcePool.forEach { (id, resource) ->
                val utilizationPercent = (resource.currentUsage / resource.capacity * 100).coerceAtMost(100f)
                appendLine("$id: ${utilizationPercent.format(1)}% utilized (${resource.efficiency.format(2)} efficiency)")
            }
        }
        
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = report
        )
    }
    
    private suspend fun getPerformanceMetrics(message: Message): Message {
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = "PERFORMANCE_METRICS",
            metadata = performanceMetrics.mapValues { it.value.toString() }
        )
    }
    
    private suspend fun handleGenericMessage(message: Message): Message {
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = "MRM_ACKNOWLEDGMENT",
            metadata = mapOf("original_message" to message.content)
        )
    }
    
    private fun allocateResource(resourceId: String, amount: Float, requestingAgent: AgentType): AllocationEvent {
        val resource = resourcePool[resourceId]
        
        if (resource == null) {
            return AllocationEvent(resourceId, requestingAgent, amount, false)
        }
        
        val availableCapacity = resource.capacity - resource.currentUsage
        val canGrant = availableCapacity >= amount
        
        if (canGrant) {
            resourcePool[resourceId] = resource.copy(currentUsage = resource.currentUsage + amount)
        }
        
        val event = AllocationEvent(resourceId, requestingAgent, amount, canGrant)
        allocationHistory.add(event)
        
        return event
    }
    
    private suspend fun optimizeResourceAllocation(): Int {
        var optimizations = 0
        
        // Implement resource optimization algorithms
        resourcePool.forEach { (id, resource) ->
            if (resource.currentUsage < resource.capacity * 0.3) {
                // Underutilized resource - reallocate or consolidate
                optimizations++
            } else if (resource.currentUsage > resource.capacity * 0.9) {
                // Overutilized resource - redistribute load
                optimizations++
            }
        }
        
        return optimizations
    }
    
    private suspend fun rebalanceLoad() {
        // Implement load balancing logic
        val overloadedResources = resourcePool.filter { (_, resource) ->
            resource.currentUsage > resource.capacity * 0.8
        }
        
        val underutilizedResources = resourcePool.filter { (_, resource) ->
            resource.currentUsage < resource.capacity * 0.4
        }
        
        // Balance between overloaded and underutilized resources
        // Implementation would involve moving workloads
    }
    
    private suspend fun monitorResourceUsage() {
        // Simulate resource usage updates
        resourcePool.forEach { (id, resource) ->
            val usageVariation = (-5f..5f).random()
            val newUsage = (resource.currentUsage + usageVariation).coerceIn(0f, resource.capacity)
            resourcePool[id] = resource.copy(currentUsage = newUsage)
        }
    }
    
    private fun updatePerformanceMetrics() {
        performanceMetrics["overall_efficiency"] = calculateOverallEfficiency()
        performanceMetrics["resource_utilization"] = calculateResourceUtilization()
        performanceMetrics["allocation_success_rate"] = calculateAllocationSuccessRate()
        performanceMetrics["response_time"] = calculateAverageResponseTime()
    }
    
    private fun analyzeUsagePatterns() {
        // Analyze historical usage patterns for prediction
    }
    
    private fun adaptAllocationStrategy(improvement: Float) {
        // Adapt allocation strategy based on learning
    }
    
    private fun updateResourceEfficiency(data: String) {
        // Update resource efficiency based on new data
    }
    
    private fun learnOptimizationTechnique(data: String) {
        // Learn new optimization techniques
    }
    
    private fun calculateOverallEfficiency(): Float {
        return resourcePool.values.map { it.efficiency }.average().toFloat() * 100
    }
    
    private fun calculateResourceUtilization(): Float {
        return resourcePool.values.map { it.currentUsage / it.capacity }.average().toFloat() * 100
    }
    
    private fun calculateAllocationSuccessRate(): Float {
        if (allocationHistory.isEmpty()) return 100f
        return allocationHistory.count { it.granted }.toFloat() / allocationHistory.size * 100
    }
    
    private fun calculateAverageResponseTime(): Float {
        // Simulate response time calculation
        return 50f + (-10f..10f).random()
    }
    
    private fun calculateEfficiencyGain(): Float {
        // Calculate efficiency gain from last optimization
        return (5f..15f).random()
    }
    
    private fun createErrorMessage(originalMessage: Message, error: String): Message {
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = originalMessage.fromAgent,
            content = "ERROR: $error",
            priority = Priority.HIGH
        )
    }
    
    private fun generateMessageId(): String {
        return "mrm-msg-${System.currentTimeMillis()}-${(1000..9999).random()}"
    }
    
    private fun Float.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
    
    private fun ClosedFloatingPointRange<Float>.random(): Float {
        return start + (endInclusive - start) * kotlin.random.Random.nextFloat()
    }
}