package shared

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Core types for the NextGen AI OS architecture
 * Provides foundational data structures and interfaces for all components
 */

// Agent Types
@Serializable
enum class AgentType {
    MRM,
    HERMES_BRAIN,
    BIG_DADDY,
    HRM_MODEL,
    ELITE_HUMAN
}

@Serializable
enum class AgentState {
    IDLE,
    PROCESSING,
    LEARNING,
    COMMUNICATING,
    ERROR
}

// Message System
@Serializable
data class Message(
    val id: String,
    val fromAgent: AgentType,
    val toAgent: AgentType?,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val priority: Priority = Priority.NORMAL,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
enum class Priority {
    LOW, NORMAL, HIGH, CRITICAL
}

// Environment Types
@Serializable
data class EnvironmentState(
    val entities: List<Entity>,
    val connections: List<Connection>,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Entity(
    val id: String,
    val type: EntityType,
    val position: Vector3D,
    val properties: Map<String, Any>
)

@Serializable
enum class EntityType {
    AGENT,
    RESOURCE,
    OBSTACLE,
    COMMUNICATION_NODE
}

@Serializable
data class Vector3D(
    val x: Float,
    val y: Float,
    val z: Float
)

@Serializable
data class Connection(
    val fromEntityId: String,
    val toEntityId: String,
    val strength: Float,
    val type: ConnectionType
)

@Serializable
enum class ConnectionType {
    COMMUNICATION,
    RESOURCE_SHARING,
    HIERARCHY,
    COLLABORATION
}

// Task and Decision Types
@Serializable
data class Task(
    val id: String,
    val type: TaskType,
    val priority: Priority,
    val assignedAgent: AgentType?,
    val status: TaskStatus,
    val parameters: Map<String, Any>,
    val createdAt: Long = System.currentTimeMillis(),
    val deadline: Long? = null
)

@Serializable
enum class TaskType {
    ANALYZE,
    COMMUNICATE,
    LEARN,
    EXECUTE,
    MONITOR,
    COORDINATE
}

@Serializable
enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Serializable
data class Decision(
    val id: String,
    val context: String,
    val options: List<DecisionOption>,
    val selectedOption: String?,
    val confidence: Float,
    val reasoningPath: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class DecisionOption(
    val id: String,
    val description: String,
    val expectedOutcome: String,
    val riskLevel: Float,
    val benefits: List<String>
)

// Application Types
@Serializable
data class CallSession(
    val id: String,
    val participants: List<String>,
    val status: CallStatus,
    val startTime: Long,
    val endTime: Long? = null
)

@Serializable
enum class CallStatus {
    INCOMING,
    OUTGOING,
    ACTIVE,
    ON_HOLD,
    ENDED
}

@Serializable
data class ConstructionProject(
    val id: String,
    val name: String,
    val type: ConstructionType,
    val status: ProjectStatus,
    val resources: List<String>,
    val milestones: List<Milestone>
)

@Serializable
enum class ConstructionType {
    BUILDING,
    INFRASTRUCTURE,
    PLATFORM,
    SYSTEM
}

@Serializable
enum class ProjectStatus {
    PLANNING,
    IN_PROGRESS,
    TESTING,
    COMPLETED,
    SUSPENDED
}

@Serializable
data class Milestone(
    val id: String,
    val name: String,
    val description: String,
    val targetDate: Long,
    val completed: Boolean = false
)

// Learning and Knowledge Types
@Serializable
data class KnowledgeNode(
    val id: String,
    val concept: String,
    val relationships: List<String>,
    val confidence: Float,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class LearningEvent(
    val id: String,
    val agent: AgentType,
    val eventType: LearningType,
    val data: String,
    val improvement: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class LearningType {
    PATTERN_RECOGNITION,
    BEHAVIOR_ADAPTATION,
    KNOWLEDGE_INTEGRATION,
    SKILL_ACQUISITION
}

// Base Interfaces
interface Agent {
    val id: String
    val type: AgentType
    val state: AgentState
    
    suspend fun process(message: Message): Message?
    suspend fun learn(event: LearningEvent)
    fun getCapabilities(): List<String>
}

interface Environment {
    suspend fun getState(): EnvironmentState
    suspend fun updateEntity(entity: Entity)
    suspend fun addConnection(connection: Connection)
    fun observe(): Flow<EnvironmentState>
}

interface Application {
    val name: String
    val version: String
    
    suspend fun start()
    suspend fun stop()
    suspend fun handleMessage(message: Message): Boolean
}

// Result Types
@Serializable
sealed class Result<out T> {
    @Serializable
    data class Success<T>(val data: T) : Result<T>()
    
    @Serializable
    data class Error(val message: String, val cause: String? = null) : Result<Nothing>()
}

// Configuration Types
@Serializable
data class SystemConfig(
    val agentConfigs: Map<AgentType, AgentConfig>,
    val environmentConfig: EnvironmentConfig,
    val networkConfig: NetworkConfig
)

@Serializable
data class AgentConfig(
    val maxMemory: Long,
    val learningRate: Float,
    val communicationTimeout: Long,
    val capabilities: List<String>
)

@Serializable
data class EnvironmentConfig(
    val maxEntities: Int,
    val updateFrequency: Long,
    val persistentStorage: Boolean
)

@Serializable
data class NetworkConfig(
    val maxConnections: Int,
    val compressionEnabled: Boolean,
    val encryptionEnabled: Boolean
)

// ========== DEPARTMENT AND ORCHESTRATION TYPES ==========

@Serializable
enum class Department {
    SCHEDULING, CALL_HANDLING, LOCATION_TRACKING, NOTE_TAKING, 
    DATA_STORAGE, CRM, ACCOUNTING, FINANCIAL_REPORTS, COMMUNICATIONS, 
    RESOURCE_MANAGEMENT, QUALITY_ASSURANCE, AUTOMATION
}

@Serializable
enum class TaskStatus {
    PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
}

@Serializable
enum class ReportingFrequency {
    REAL_TIME, HOURLY, DAILY, WEEKLY, ON_DEMAND
}

@Serializable
enum class KPIMeasurement {
    COUNT, PERCENTAGE, AVERAGE, SUM, RATIO
}

@Serializable
data class AgentRole(
    val id: String,
    val name: String,
    val department: Department,
    val responsibilities: List<String>,
    val requiredCapabilities: List<String>,
    val autonomyLevel: Float,
    val reportingStructure: ReportingStructure,
    val kpis: List<KPI>
)

@Serializable
data class ReportingStructure(
    val managerAgent: String?,
    val subordinates: List<String>,
    val escalationPath: List<String>,
    val reportingFrequency: ReportingFrequency
)

@Serializable
data class KPI(
    val name: String,
    val target: Float,
    val current: Float,
    val unit: String,
    val measurement: KPIMeasurement
)

@Serializable
data class DecisionOption(
    val id: String,
    val description: String,
    val expectedOutcome: String,
    val risk: Float,
    val benefit: Float
)