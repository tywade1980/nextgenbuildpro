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
    INITIALIZING, ACTIVE, IDLE, BUSY, ERROR, MAINTENANCE, SHUTDOWN, HEALTHY, STOPPED
}

/**
 * Agent types in the NextGen ecosystem
 * Structured as corporate C-suite executives with operational agents beneath
 */
enum class AgentType {
    ORCHESTRATOR,
    // CEO Level - Main Personal Assistant (directs the orchestrator)
    CEO_PERSONAL_ASSISTANT,
    PERSONAL_ASSISTANT_ORCHESTRATOR,  // Alias for CEO Personal Assistant
    // C-Suite Department Heads (Orchestrators)
    COO_OPERATIONS_ORCHESTRATOR,      // COO: Operations & Project Management
    CFO_FINANCIAL_ORCHESTRATOR,        // CFO: Financial & Analytics
    CHRO_CLIENT_HR_ORCHESTRATOR,       // CHRO/CMO: Client Relations & HR
    CTO_DESIGN_ORCHESTRATOR,           // CTO: Design & Technology
    CSO_SAFETY_ORCHESTRATOR,           // CSO: Safety & Compliance
    // Department-level Orchestrators (aliases and specific departments)
    CRM_ORCHESTRATOR,                  // Customer Relationship Management
    PROJECT_MANAGEMENT_ORCHESTRATOR,   // Project Management
    ANALYTICS_ORCHESTRATOR,            // Analytics & Reporting
    DESIGN_DEPARTMENT_ORCHESTRATOR,    // Design Department (CTO)
    ESTIMATING_DEPARTMENT_ORCHESTRATOR, // Estimating (CFO)
    MARKETING_ORCHESTRATOR,            // Marketing (CHRO/CMO)
    // Operational Agent Types (specialized agents under C-suite)
    OPERATIONAL_AGENT,
    SUB_AGENT
}

/**
 * Specialized Agent Interface for MCP-enabled agents
 */
interface SpecializedAgent {
    val agentId: String
    val agentType: AgentType
    val specialization: String
    val isActive: StateFlow<Boolean>
    
    suspend fun initialize(): Result<Unit>
    suspend fun processTask(task: NextGenTask): Result<NextGenTask>
    suspend fun shutdown(): Result<Unit>
}

/**
 * Sub-Agent Interface for fine-tuned specialized agents under department heads
 * These are the 5-8 agents per department with specific tooling and ML capabilities
 */
interface SubAgent : SpecializedAgent {
    val departmentHead: AgentType
    val subAgentRole: String
    val mlModel: MLModelConfig?
    val mcpTools: List<MCPTool>
    val apiIntegrations: List<APIIntegration>
    
    suspend fun executeSpecializedTask(task: NextGenTask): Result<NextGenTask>
    suspend fun requestHumanApproval(task: NextGenTask, reason: String): Result<HumanApprovalRecord>
    suspend fun learnFromFeedback(feedback: TaskFeedback): Result<Unit>
}

/**
 * ML Model configuration for sub-agents
 */
data class MLModelConfig(
    val modelName: String,
    val modelType: MLModelType,
    val version: String,
    val trainedOn: String,
    val accuracy: Double = 0.0,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)

enum class MLModelType {
    CLASSIFICATION, REGRESSION, NLP, COMPUTER_VISION, 
    REINFORCEMENT_LEARNING, ENSEMBLE, CUSTOM, REASONING, AGENT_WORKFLOW
}

/**
 * Multi-LLM System Configuration
 * Supports specialized LLM models for different task types
 */
data class MultiLLMConfig(
    val systemId: String,
    val reasoningModel: LLMModel,      // For complex reasoning tasks
    val agentWorkflowModel: LLMModel,  // For agent orchestration
    val routingStrategy: LLMRoutingStrategy = LLMRoutingStrategy.TASK_BASED
)

/**
 * LLM Model configuration
 */
data class LLMModel(
    val modelId: String,
    val modelName: String,
    val provider: LLMProvider,
    val modelType: LLMModelType,
    val contextWindow: Int,
    val temperature: Double = 0.7,
    val maxTokens: Int = 4096,
    val capabilities: List<LLMCapability>
)

enum class LLMProvider {
    OPENAI, ANTHROPIC, GOOGLE, META, MISTRAL, OPENROUTER, LOCAL, CUSTOM
}

enum class LLMModelType {
    REASONING,          // o1, o3-mini for complex reasoning
    AGENT_WORKFLOW,     // GPT-4, Claude for agent coordination
    FAST_INFERENCE,     // GPT-3.5-turbo for quick tasks
    SPECIALIZED         // Domain-specific models
}

enum class LLMCapability {
    REASONING, FUNCTION_CALLING, CODE_GENERATION, 
    VISION, AUDIO, MULTIMODAL, LONG_CONTEXT
}

enum class LLMRoutingStrategy {
    TASK_BASED,         // Route based on task complexity
    LOAD_BALANCED,      // Distribute across models
    COST_OPTIMIZED,     // Use cheaper models when possible
    QUALITY_FIRST       // Always use best model
}

/**
 * MCP Tool configuration
 */
data class MCPTool(
    val toolId: String,
    val toolName: String,
    val description: String,
    val capabilities: List<String>,
    val isActive: Boolean = true
)

/**
 * API Integration configuration
 */
data class APIIntegration(
    val apiId: String,
    val apiName: String,
    val endpoint: String,
    val authType: APIAuthType,
    val rateLimits: APIRateLimits? = null
)

enum class APIAuthType {
    API_KEY, OAUTH2, BASIC_AUTH, JWT, NONE
}

data class APIRateLimits(
    val requestsPerMinute: Int,
    val requestsPerDay: Int
)

/**
 * Task feedback for learning
 */
data class TaskFeedback(
    val taskId: EntityId,
    val wasSuccessful: Boolean,
    val humanCorrections: Map<String, Any> = emptyMap(),
    val executionTime: Long,
    val qualityScore: Double, // 0.0 to 1.0
    val notes: String = ""
)

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
 * Task representation in the NextGen system (v2.0)
 */
data class NextGenTask(
    val id: EntityId = UUID.randomUUID().toString(),
    val title: String = "",
    val type: String = "",
    val description: String,
    val type: String = "generic",
    val assignedAgent: AgentType = AgentType.ORCHESTRATOR,
    val priority: Priority,
    val status: TaskStatus = TaskStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val dueDate: LocalDateTime? = null,
    val dependencies: List<EntityId> = emptyList(),
    val metadata: Map<String, Any> = emptyMap(),
    val parameters: Map<String, Any> = emptyMap(),
    val result: Map<String, Any>? = null,
    val progress: Float = 0f, // 0.0 to 1.0
    val requiresHumanApproval: Boolean = false,
    val automationLevel: AutomationLevel = AutomationLevel.HUMAN_IN_LOOP
)

/**
 * Task status enumeration
 */
enum class TaskStatus {
    PENDING, IN_PROGRESS, PAUSED, COMPLETED, FAILED, CANCELLED
}

/**
 * Automation level for tasks - tracks human-in-the-loop progression
 */
enum class AutomationLevel {
    MANUAL,              // Requires human execution
    HUMAN_IN_LOOP,       // AI assists, human approves
    SUPERVISED,          // AI executes, human reviews
    AUTOMATED,           // Fully automated, no approval needed
    LEARNING             // System is learning this task pattern
}

/**
 * Human approval tracking for tasks
 */
data class HumanApprovalRecord(
    val taskId: EntityId,
    val approver: String,
    val approved: Boolean,
    val comments: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val reviewTime: Long = 0L // milliseconds
)

/**
 * Task pattern for automation detection
 */
data class TaskPattern(
    val patternId: EntityId = UUID.randomUUID().toString(),
    val taskType: String,
    val occurrences: Int = 0,
    val successRate: Double = 0.0,
    val averageReviewTime: Long = 0L,
    val consistentOutcomes: Boolean = false,
    val readyForAutomation: Boolean = false
)

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
    BEGINNER, BASIC, INTERMEDIATE, ADVANCED, EXPERT, MASTER
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
    val phases: List<ProjectPhaseDetails> = emptyList(),
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
data class ProjectPhaseDetails(
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
 * Health status enum for services
 */
enum class HealthStatus {
    HEALTHY, DEGRADED, UNHEALTHY, STOPPED, STARTING, UNKNOWN
}

/**
 * Service health status
 */
data class ServiceHealth(
    val serviceName: String = "",
    val status: HealthStatus = HealthStatus.HEALTHY,
    val lastCheck: LocalDateTime = LocalDateTime.now(),
    val isHealthy: Boolean = (status == HealthStatus.HEALTHY),
    val lastCheckTime: LocalDateTime = lastCheck,
    val issues: List<String> = emptyList(),
    val metrics: Map<String, Any> = emptyMap()
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

// ===== DEPARTMENTAL ORCHESTRATOR TYPES =====

/**
 * Base interface for departmental orchestrators (Department Heads)
 * Department heads manage 5-8 sub-agents with specialized capabilities
 */
interface DepartmentalOrchestrator : LearningAgent {
    val departmentName: String
    val toolsets: List<OrchestratorTool>
    val sharedContext: StateFlow<SharedContext>
    val subAgents: List<SubAgent>
    
    suspend fun processVoiceCommand(command: String): Result<String>
    suspend fun getSpecializedCapabilities(): List<AgentCapability>
    suspend fun coordinateWithOtherDepartments(request: InterDepartmentalRequest): Result<InterDepartmentalResponse>
    suspend fun delegateToSubAgent(task: NextGenTask, subAgentRole: String): Result<NextGenTask>
    suspend fun getSubAgentStatus(): Map<String, AgentStatus>
    suspend fun trainSubAgent(subAgentRole: String, trainingData: LearningData): Result<Unit>
}

/**
 * Agent status information
 */
data class AgentStatus(
    val agentId: String,
    val isActive: Boolean,
    val currentTasks: Int,
    val completedTasks: Int,
    val successRate: Double,
    val lastActivity: LocalDateTime?
)

/**
 * Shared context across all orchestrators
 */
data class SharedContext(
    val currentProjects: List<ConstructionProject>,
    val activeClients: List<ClientInfo>,
    val systemMetrics: PerformanceMetrics,
    val weatherConditions: WeatherInfo? = null,
    val locationContext: LocationContext? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * Tools available to orchestrators
 */
data class OrchestratorTool(
    val name: String,
    val description: String,
    val toolType: ToolType,
    val permissions: List<Permission>,
    val isActive: Boolean = true
)

/**
 * Tool types available to orchestrators
 */
enum class ToolType {
    SYSTEM_INTEGRATION, VOICE_COMMAND, DATA_ANALYSIS, COMMUNICATION,
    DESIGN_TOOL, MODELING_TOOL, REPORTING_TOOL, AUTOMATION_TOOL,
    ANDROID_NATIVE, THIRD_PARTY_API, AI_SERVICE
}

/**
 * Permission system for orchestrator tools
 */
enum class Permission {
    READ_CONTACTS, WRITE_CONTACTS, MAKE_CALLS, SEND_SMS, ACCESS_CAMERA,
    ACCESS_LOCATION, ACCESS_MICROPHONE, READ_CALENDAR, WRITE_CALENDAR,
    ACCESS_STORAGE, INTERNET_ACCESS, SYSTEM_ADMIN
}

/**
 * Inter-departmental communication
 */
data class InterDepartmentalRequest(
    val requestingDepartment: AgentType,
    val targetDepartment: AgentType,
    val requestType: RequestType,
    val data: Map<String, Any>,
    val priority: Priority = Priority.MEDIUM
)

data class InterDepartmentalResponse(
    val success: Boolean,
    val data: Map<String, Any>,
    val nextActions: List<String> = emptyList(),
    val collaborationNeeded: Boolean = false
)

enum class RequestType {
    DATA_QUERY, TASK_EXECUTION, RESOURCE_REQUEST, STATUS_UPDATE,
    COLLABORATION_REQUEST, EXPERTISE_REQUEST
}

/**
 * Client information model
 */
data class ClientInfo(
    val id: EntityId = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val projects: List<EntityId> = emptyList(),
    val communicationHistory: List<CommunicationRecord> = emptyList(),
    val preferences: ClientPreferences = ClientPreferences(),
    val tags: List<String> = emptyList()
)

data class ClientPreferences(
    val preferredCommunicationMethod: CommunicationMethod = CommunicationMethod.PHONE,
    val bestTimeToContact: String = "9AM-5PM",
    val language: String = "en",
    val specialRequirements: List<String> = emptyList()
)

enum class CommunicationMethod {
    PHONE, EMAIL, SMS, IN_PERSON, VIDEO_CALL
}

data class CommunicationRecord(
    val id: EntityId = UUID.randomUUID().toString(),
    val method: CommunicationMethod,
    val direction: CommunicationDirection,
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val outcome: String? = null
)

enum class CommunicationDirection {
    INBOUND, OUTBOUND
}

/**
 * Weather information for schedule optimization
 */
data class WeatherInfo(
    val temperature: Double,
    val humidity: Double,
    val precipitation: Double,
    val windSpeed: Double,
    val conditions: WeatherConditions,
    val forecast: List<WeatherForecast> = emptyList()
)

enum class WeatherConditions {
    CLEAR, PARTLY_CLOUDY, CLOUDY, LIGHT_RAIN, HEAVY_RAIN, SNOW, STORM
}

data class WeatherForecast(
    val date: LocalDateTime,
    val conditions: WeatherConditions,
    val temperature: Double,
    val precipitationChance: Double
)

/**
 * Location context for field operations
 */
data class LocationContext(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val accuracy: Float,
    val isJobSite: Boolean = false,
    val geofenceId: String? = null
)

/**
 * Construction cost data models
 */
data class CostDatabase(
    val residentialTemplates: Map<String, ResidentialTemplate>,
    val laborRates: Map<String, LaborRate>,
    val materialCosts: Map<String, MaterialCost>,
    val equipmentRates: Map<String, EquipmentRate>,
    val regionalMultipliers: Map<String, Double>
)

data class ResidentialTemplate(
    val category: String,
    val description: String,
    val costPerSqFt: CostRange,
    val laborHours: Double,
    val materials: List<String>
)

data class CostRange(
    val min: Double,
    val max: Double,
    val average: Double = (min + max) / 2.0
)

data class LaborRate(
    val trade: String,
    val hourlyRate: CostRange,
    val skillLevel: SkillLevel,
    val region: String,
    // Legacy properties for backward compatibility
    val tradeName: String? = null,
    val description: String? = null
)

data class MaterialCost(
    val name: String,
    val unit: String,
    val cost: Double,
    val supplier: String,
    val lastUpdated: LocalDateTime
)

data class EquipmentRate(
    val name: String,
    val dailyRate: Double,
    val weeklyRate: Double,
    val monthlyRate: Double,
    val category: String
)

// ===== CONSTRUCTION KNOWLEDGE BASE =====

/**
 * Construction-specific pricing data (2025+ data)
 * Used by CFO's Estimator Agent for accurate job costing
 */
data class ConstructionPricingData(
    val costDatabase: CostDatabase2025,
    val laborRates: Map<String, LaborRate>,
    val materialPrices: Map<String, MaterialPrice>,
    val equipmentRates: Map<String, EquipmentRentalRate>,
    val assemblyTimes: Map<String, AssemblyTime>,
    val lastUpdated: LocalDateTime
)

data class CostDatabase2025(
    val source: String,  // "RSMeans", "BLS", "regional"
    val version: String,
    val regionalFactors: Map<String, Double>,  // Regional cost multipliers
    val inflationRate: Double,
    val categories: Map<String, CostCategory>
)

data class CostCategory(
    val categoryId: String,
    val name: String,
    val baseUnit: String,
    val averageCost: Double,
    val costRange: CostRange,
    val laborComponent: Double,  // Percentage
    val materialComponent: Double  // Percentage
)

data class AssemblyTime(
    val assemblyId: String,
    val description: String,
    val crewSize: Int,
    val crewComposition: Map<String, Int>,  // Trade -> count
    val hoursPerUnit: Double,
    val unitsPerDay: Double,
    val difficultyFactor: Double = 1.0
)

data class MaterialPrice(
    val materialId: String,
    val name: String,
    val unit: String,
    val pricePerUnit: Double,
    val supplier: String,
    val leadTime: Int,  // Days
    val minimumOrder: Double,
    val availability: MaterialAvailability
)

enum class MaterialAvailability {
    IN_STOCK, LOW_STOCK, BACKORDER, SPECIAL_ORDER, DISCONTINUED
}

data class EquipmentRentalRate(
    val equipmentId: String,
    val name: String,
    val hourlyRate: Double,
    val dailyRate: Double,
    val weeklyRate: Double,
    val monthlyRate: Double,
    val deliveryFee: Double,
    val operator: OperatorRequirement
)

enum class OperatorRequirement {
    INCLUDED, NOT_INCLUDED, OPTIONAL, CERTIFIED_ONLY
}

/**
 * Project lifecycle data flow structure
 * Enables natural workflow between C-suite executives
 */
data class ProjectLifecycleFlow(
    val projectId: String,
    val currentPhase: ProjectPhase,
    val flowSteps: List<FlowStep>,
    val dependencies: Map<String, List<String>>
)

enum class ProjectPhase {
    ESTIMATING, SCHEDULING, PROCUREMENT, EXECUTION, CLOSEOUT
}

data class FlowStep(
    val stepId: String,
    val executiveOwner: AgentType,  // Which C-suite executive
    val operationalAgent: String,   // Which sub-agent
    val action: String,
    val inputs: List<DataArtifact>,
    val outputs: List<DataArtifact>,
    val nextSteps: List<String>,
    val automationLevel: AutomationLevel
)

data class DataArtifact(
    val artifactId: String,
    val artifactType: ArtifactType,
    val data: Map<String, Any>,
    val producedBy: String,
    val consumedBy: List<String>,
    val timestamp: LocalDateTime
)

enum class ArtifactType {
    ESTIMATE, LABOR_DATA, SCHEDULE, GANTT_CHART, 
    MATERIAL_LIST, CREW_ASSIGNMENT, PROGRESS_REPORT,
    INVOICE, BUDGET, ANALYTICS_REPORT
}

/**
 * Example workflow: Estimate → Schedule → Execute
 * CFO Estimator → COO Field Operations → Analytics
 */
data class WorkflowHandoff(
    val fromExecutive: AgentType,
    val toExecutive: AgentType,
    val artifact: DataArtifact,
    val handoffReason: String,
    val completionCriteria: List<String>
)

// Design & Technical Document Types for CTO Design Department

// Project requirements for design generation
data class ProjectRequirements(
    val projectId: String,
    val projectType: String,
    val squareFootage: Double,
    val floors: Int,
    val bedrooms: Int,
    val bathrooms: Double,
    val specialFeatures: List<String> = emptyList(),
    val buildingCode: String = "IBC 2021"
)

data class Blueprint(
    val blueprintId: String,
    val projectId: String,
    val title: String = "",
    val drawingNumber: String = "",
    val revisionNumber: Int = 1,
    val drawingType: DrawingType = DrawingType.FLOOR_PLAN,
    val scale: String = "1/4\" = 1'",
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: DrawingStatus = DrawingStatus.DRAFT,
    val fileUrl: String = "",
    val layers: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
    // Legacy properties for backward compatibility
    val projectType: String? = null,
    val squareFootage: Double? = null,
    val floors: Int? = null,
    val bedrooms: Int? = null,
    val bathrooms: Double? = null,
    val specialFeatures: List<String>? = null,
    val floorPlans: List<String>? = null,
    val elevations: List<String>? = null,
    val sections: List<String>? = null,
    val version: String? = null,
    val id: String? = null
)

enum class DrawingType {
    SITE_PLAN, FLOOR_PLAN, ELEVATION, SECTION, DETAIL, ELECTRICAL, PLUMBING, STRUCTURAL
}

enum class DrawingStatus {
    DRAFT, UNDER_REVIEW, APPROVED, SUPERSEDED, AS_BUILT
}

data class ThreeDModel(
    val modelId: String,
    val projectId: String,
    val name: String,
    val modelType: ModelType = ModelType.RENDERING,
    val fileFormat: String = "OBJ",
    val fileUrl: String = "",
    val fileSize: Long = 0,
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val renderingQuality: RenderingQuality = RenderingQuality.STANDARD,
    val materials: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    // Legacy properties
    val id: String? = null,
    val blueprintId: String? = null,
    val renderType: String? = null,
    val renderQuality: String? = null
)

enum class ModelType {
    BIM_MODEL, CAD_MODEL, RENDERING, VR_MODEL, AR_MODEL, EXTERIOR_INTERIOR
}

enum class RenderingQuality {
    DRAFT, STANDARD, HIGH_QUALITY, PHOTOREALISTIC
}

data class ShopDrawing(
    val drawingId: String,
    val projectId: String,
    val trade: String = "",
    val description: String = "",
    val drawingNumber: String = "",
    val revisionNumber: Int = 1,
    val submittedDate: LocalDateTime = LocalDateTime.now(),
    val reviewStatus: ReviewStatus = ReviewStatus.SUBMITTED,
    val fileUrl: String = "",
    val reviewer: String? = null,
    val comments: List<String> = emptyList(),
    // Legacy properties
    val id: String? = null
)

enum class ReviewStatus {
    SUBMITTED, UNDER_REVIEW, APPROVED, APPROVED_AS_NOTED, REJECTED, RESUBMIT
}

data class MaterialTakeoff(
    val takeoffId: String,
    val projectId: String,
    val trade: String = "",
    val description: String = "",
    val items: List<MaterialItem> = emptyList(),
    val totalCost: Double = 0.0,
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: TakeoffStatus = TakeoffStatus.DRAFT,
    // Legacy properties
    val id: String? = null
)

data class MaterialItem(
    val itemId: String,
    val description: String,
    val category: Any? = null,  // Can be MaterialCategory enum or String
    val quantity: Double = 0.0,
    val unit: String = "",
    val unitCost: Double = 0.0,
    val totalCost: Double = 0.0,
    val supplier: String? = null,
    val notes: String? = null,
    // Legacy property
    val categoryName: String? = null
)

enum class MaterialCategory {
    LUMBER, CONCRETE, STEEL, DRYWALL, INSULATION, ROOFING, SIDING, 
    WINDOWS, DOORS, FLOORING, HVAC, ELECTRICAL, PLUMBING, FINISHES, OTHER
}

// MaterialCategoryGroup: Used for grouping material items by category name
// This is different from the MaterialCategory enum which is used for individual items
data class MaterialCategoryGroup(
    val name: String,
    val items: List<MaterialItem>
)

enum class TakeoffStatus {
    DRAFT, IN_PROGRESS, COMPLETE, APPROVED, ORDERED
}

enum class TemplateType {
    RESIDENTIAL, COMMERCIAL, INDUSTRIAL, MIXED_USE
}

data class DesignTemplate(
    val templateId: String,
    val name: String,
    val description: String,
    val category: String = "",
    val designType: String = "",
    val parameters: Map<String, Any> = emptyMap(),
    val defaultValues: Map<String, Any> = emptyMap(),
    val thumbnail: String? = null,
    val tags: List<String> = emptyList(),
    // Legacy properties
    val templateType: TemplateType? = null
)

data class DesignKnowledgeBase(
    val buildingCodes: Map<String, String>,
    val structuralStandards: Map<String, String>,
    val designGuidelines: Map<String, String> = emptyMap(),
    val materialSpecs: Map<String, String> = emptyMap(),
    val bestPractices: List<String> = emptyList()
)

// Agent health status (detailed health info for agents)
data class AgentHealthStatus(
    val isHealthy: Boolean,
    val lastCheckTime: LocalDateTime,
    val issues: List<String> = emptyList(),
    val metrics: Map<String, Double> = emptyMap()
)