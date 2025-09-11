package com.nextgenbuildpro.shared

import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.util.UUID

/**
 * NextGen AI OS - Shared Types and Data Structures
 * 
 * This file contains all the shared types, interfaces, and data structures
 * used across the NextGen AI OS architecture for seamless integration
 * between agents, environments, and applications.
 */

// ===== CORE TYPES =====

/**
 * Unique identifier for all system entities
 */
typealias EntityId = String

/**
 * Priority levels for tasks and communications
 */
enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL, EMERGENCY
}

/**
 * System-wide status indicators
 */
enum class SystemStatus {
    INITIALIZING, ACTIVE, IDLE, BUSY, ERROR, MAINTENANCE, SHUTDOWN
}

/**
 * Agent types in the NextGen ecosystem
 */
enum class AgentType {
    MRM, HERMES_BRAIN, BIG_DADDY, HRM_MODEL, ELITE_HUMAN, ORCHESTRATOR
}

// ===== DATA MODELS =====

/**
 * Core message structure for inter-agent communication
 */
data class AgentMessage(
    val id: EntityId = UUID.randomUUID().toString(),
    val fromAgent: AgentType,
    val toAgent: AgentType,
    val messageType: MessageType,
    val content: String,
    val metadata: Map<String, Any> = emptyMap(),
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val priority: Priority = Priority.MEDIUM,
    val requiresResponse: Boolean = false,
    val correlationId: EntityId? = null
)

/**
 * Types of messages that can be exchanged between agents
 */
enum class MessageType {
    COMMAND, QUERY, RESPONSE, NOTIFICATION, STATUS_UPDATE, 
    ALERT, DATA_SYNC, HEARTBEAT, ERROR, ACKNOWLEDGMENT
}

/**
 * Task representation in the NextGen system
 */
data class NextGenTask(
    val id: EntityId = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val assignedAgent: AgentType,
    val priority: Priority,
    val status: TaskStatus,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val dueDate: LocalDateTime? = null,
    val dependencies: List<EntityId> = emptyList(),
    val metadata: Map<String, Any> = emptyMap(),
    val progress: Float = 0f // 0.0 to 1.0
)

/**
 * Task status enumeration
 */
enum class TaskStatus {
    PENDING, IN_PROGRESS, PAUSED, COMPLETED, FAILED, CANCELLED
}

/**
 * Agent capability definition
 */
data class AgentCapability(
    val name: String,
    val description: String,
    val inputTypes: List<String>,
    val outputTypes: List<String>,
    val skillLevel: SkillLevel,
    val isEnabled: Boolean = true
)

/**
 * Skill level for capabilities
 */
enum class SkillLevel {
    BASIC, INTERMEDIATE, ADVANCED, EXPERT, MASTER
}

/**
 * Environment context for the Living Environment mesh
 */
data class EnvironmentContext(
    val locationId: EntityId,
    val environmentType: EnvironmentType,
    val activeAgents: Set<AgentType>,
    val currentTasks: List<NextGenTask>,
    val systemLoad: Float, // 0.0 to 1.0
    val networkQuality: NetworkQuality,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * Types of environments in the NextGen ecosystem
 */
enum class EnvironmentType {
    CONSTRUCTION_SITE, OFFICE, MOBILE, REMOTE, HYBRID, VIRTUAL
}

/**
 * Network quality indicators
 */
enum class NetworkQuality {
    EXCELLENT, GOOD, FAIR, POOR, OFFLINE
}

/**
 * Construction project data model
 */
data class ConstructionProject(
    val id: EntityId = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val address: String,
    val clientId: EntityId,
    val projectManager: EntityId,
    val status: ProjectStatus,
    val startDate: LocalDateTime,
    val estimatedEndDate: LocalDateTime,
    val actualEndDate: LocalDateTime? = null,
    val budget: Double,
    val currentCost: Double = 0.0,
    val phases: List<ProjectPhase> = emptyList(),
    val tasks: List<NextGenTask> = emptyList(),
    val documents: List<ProjectDocument> = emptyList()
)

/**
 * Project status enumeration
 */
enum class ProjectStatus {
    PLANNING, APPROVED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED
}

/**
 * Project phase data
 */
data class ProjectPhase(
    val id: EntityId = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val status: TaskStatus,
    val milestones: List<String> = emptyList()
)

/**
 * Project document metadata
 */
data class ProjectDocument(
    val id: EntityId = UUID.randomUUID().toString(),
    val name: String,
    val type: DocumentType,
    val url: String,
    val uploadedBy: EntityId,
    val uploadedAt: LocalDateTime = LocalDateTime.now(),
    val version: String = "1.0",
    val tags: List<String> = emptyList()
)

/**
 * Document types
 */
enum class DocumentType {
    BLUEPRINT, SPECIFICATION, CONTRACT, PERMIT, PHOTO, VIDEO, 
    REPORT, INVOICE, EMAIL, OTHER
}

// ===== INTERFACES =====

/**
 * Base interface for all NextGen agents
 */
interface NextGenAgent {
    val agentType: AgentType
    val capabilities: List<AgentCapability>
    val status: StateFlow<SystemStatus>
    
    suspend fun initialize(): Result<Unit>
    suspend fun processMessage(message: AgentMessage): Result<AgentMessage?>
    suspend fun executeTask(task: NextGenTask): Result<NextGenTask>
    suspend fun getStatus(): SystemStatus
    suspend fun shutdown(): Result<Unit>
}

/**
 * Interface for agents that can learn and adapt
 */
interface LearningAgent : NextGenAgent {
    suspend fun learn(data: LearningData): Result<Unit>
    suspend fun getKnowledgeBase(): Map<String, Any>
    suspend fun updateModel(parameters: Map<String, Any>): Result<Unit>
}

/**
 * Learning data structure
 */
data class LearningData(
    val type: LearningType,
    val input: Any,
    val expectedOutput: Any?,
    val feedback: Double, // -1.0 to 1.0
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Types of learning
 */
enum class LearningType {
    SUPERVISED, UNSUPERVISED, REINFORCEMENT, TRANSFER, ONLINE
}

/**
 * Interface for environment mesh components
 */
interface EnvironmentMesh {
    suspend fun registerAgent(agent: NextGenAgent): Result<Unit>
    suspend fun unregisterAgent(agentType: AgentType): Result<Unit>
    suspend fun broadcastMessage(message: AgentMessage): Result<Unit>
    suspend fun routeMessage(message: AgentMessage): Result<Unit>
    suspend fun getEnvironmentContext(): EnvironmentContext
    suspend fun updateContext(context: EnvironmentContext): Result<Unit>
}

/**
 * Interface for application services
 */
interface NextGenService {
    val serviceName: String
    val isRunning: StateFlow<Boolean>
    
    suspend fun start(): Result<Unit>
    suspend fun stop(): Result<Unit>
    suspend fun restart(): Result<Unit>
    suspend fun getHealthStatus(): ServiceHealth
}

/**
 * Service health status
 */
data class ServiceHealth(
    val isHealthy: Boolean,
    val lastCheckTime: LocalDateTime,
    val issues: List<String> = emptyList(),
    val metrics: Map<String, Double> = emptyMap()
)

/**
 * Interface for orchestrator components
 */
interface Orchestrator {
    suspend fun orchestrateTask(task: NextGenTask): Result<Unit>
    suspend fun coordinateAgents(agents: List<AgentType>, task: NextGenTask): Result<Unit>
    suspend fun optimizeWorkflow(workflow: List<NextGenTask>): Result<List<NextGenTask>>
    suspend fun handleFailure(failedTask: NextGenTask, error: Throwable): Result<Unit>
}

// ===== EVENT SYSTEM =====

/**
 * System-wide event for reactive programming
 */
data class NextGenEvent(
    val id: EntityId = UUID.randomUUID().toString(),
    val type: EventType,
    val source: AgentType,
    val data: Map<String, Any>,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val priority: Priority = Priority.MEDIUM
)

/**
 * Event types in the system
 */
enum class EventType {
    AGENT_STARTED, AGENT_STOPPED, TASK_CREATED, TASK_COMPLETED, 
    TASK_FAILED, MESSAGE_SENT, MESSAGE_RECEIVED, ERROR_OCCURRED,
    SYSTEM_ALERT, PERFORMANCE_METRIC, USER_ACTION
}

/**
 * Interface for event handlers
 */
interface EventHandler {
    val supportedEventTypes: Set<EventType>
    suspend fun handleEvent(event: NextGenEvent): Result<Unit>
}

// ===== UTILITY TYPES =====

/**
 * Result wrapper for consistent error handling
 */
sealed class NextGenResult<out T> {
    data class Success<T>(val data: T) : NextGenResult<T>()
    data class Error(val exception: Throwable, val message: String) : NextGenResult<Nothing>()
    
    inline fun <R> map(transform: (T) -> R): NextGenResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    inline fun onSuccess(action: (T) -> Unit): NextGenResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (Throwable, String) -> Unit): NextGenResult<T> {
        if (this is Error) action(exception, message)
        return this
    }
}

/**
 * Configuration data for system components
 */
data class NextGenConfig(
    val componentName: String,
    val settings: Map<String, Any>,
    val version: String = "1.0.0",
    val environment: String = "production"
)

/**
 * Performance metrics for monitoring
 */
data class PerformanceMetrics(
    val cpuUsage: Double,
    val memoryUsage: Double,
    val networkLatency: Double,
    val throughput: Double,
    val errorRate: Double,
    val timestamp: LocalDateTime = LocalDateTime.now()
)