# NextGen BuildPro v2.0 Agent Modeling Framework

## Overview

The NextGen BuildPro v2.0 agent modeling framework provides a standardized, scalable architecture for building AI agents in the construction management platform. This framework ensures consistency, maintainability, and seamless integration across all AI components.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────────┐
│                     Application Layer                            │
│  (MainActivity, UI Screens, Service Integrations)               │
└──────────────────────┬──────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│                  MainOrchestrator                                │
│  (Central coordination, System initialization)                  │
└──────────────────────┬──────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│                 OrchestratorManager                              │
│  (Manages 6 departmental orchestrators + 48 specialized agents) │
└─────┬────────────────────────────────────────────────────┬──────┘
      │                                                     │
┌─────▼──────────────────┐                    ┌───────────▼────────┐
│ DepartmentalOrchestrator│                    │  SpecializedAgent  │
│  (6 departments)       │◄───────manages─────►│  (48 agents)       │
└────────────────────────┘                    └────────────────────┘
      │                                                     │
      └─────────────────┬───────────────────────────────┬──┘
                        │                               │
                ┌───────▼──────┐              ┌────────▼────────┐
                │  MCP Server  │              │  Living Env     │
                │ (Communication)             │  (Context Mesh) │
                └──────────────┘              └─────────────────┘
```

## Core Interfaces

### 1. NextGenAgent (Base Interface)

All agents in the system implement this interface either directly or through inheritance.

```kotlin
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
```

**Key Characteristics:**
- Defines the contract for all agents
- Uses Kotlin coroutines for asynchronous operations
- Returns `Result<T>` for error handling
- Maintains reactive state via `StateFlow`

### 2. LearningAgent (Extends NextGenAgent)

Agents that can learn and adapt over time.

```kotlin
interface LearningAgent : NextGenAgent {
    suspend fun learn(data: LearningData): Result<Unit>
    suspend fun getKnowledgeBase(): Map<String, Any>
    suspend fun updateModel(parameters: Map<String, Any>): Result<Unit>
}
```

**Key Characteristics:**
- Continuous improvement capability
- Knowledge base management
- Model parameter updates
- Supports multiple learning types (supervised, reinforcement, etc.)

### 3. DepartmentalOrchestrator (Extends LearningAgent)

Top-level orchestrators managing department-specific functionality.

```kotlin
interface DepartmentalOrchestrator : LearningAgent {
    val departmentName: String
    val toolsets: List<OrchestratorTool>
    val sharedContext: StateFlow<SharedContext>
    
    suspend fun processVoiceCommand(command: String): Result<String>
    suspend fun getSpecializedCapabilities(): List<AgentCapability>
    suspend fun coordinateWithOtherDepartments(
        request: InterDepartmentalRequest
    ): Result<InterDepartmentalResponse>
}
```

**Key Characteristics:**
- Manages 8 specialized agents per department
- Provides department-specific toolsets
- Handles voice commands
- Enables inter-departmental coordination
- Maintains shared context across the department

**Six Departmental Orchestrators:**
1. **PersonalAssistantOrchestrator** - Voice commands, hands-free operation
2. **CRMOrchestrator** - Contact management, lead automation
3. **ProjectManagementOrchestrator** - Scheduling, cost estimation, resource planning
4. **AnalyticsOrchestrator** - Data analysis, reporting, predictions
5. **DesignDepartmentOrchestrator** - 3D modeling, blueprints, design review
6. **MarketingOrchestrator** - Proposals, campaigns, client presentations

### 4. SpecializedAgent (Focused Implementation)

Lightweight, single-purpose agents managed by orchestrators.

```kotlin
interface SpecializedAgent {
    val agentId: String
    val agentType: AgentType
    val specialization: String
    val isActive: StateFlow<Boolean>
    
    suspend fun initialize(): Result<Unit>
    suspend fun processTask(task: NextGenTask): Result<NextGenTask>
    suspend fun shutdown(): Result<Unit>
}
```

**Key Characteristics:**
- Focused on a specific task domain
- Lightweight and efficient
- MCP-integrated for communication
- Managed lifecycle by parent orchestrator

**Example Specialized Agents:**
- `VoiceCommandAgent` - Natural language processing
- `ContactManagementAgent` - Smart contact creation
- More agents per department (48 total across 6 departments)

## Core Data Models

### NextGenTask (v2.0 Enhanced)

```kotlin
data class NextGenTask(
    val id: EntityId = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String,
    val type: String = "generic",                    // v2.0: Task type classification
    val assignedAgent: AgentType = AgentType.ORCHESTRATOR,
    val priority: Priority,
    val status: TaskStatus = TaskStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val dueDate: LocalDateTime? = null,
    val dependencies: List<EntityId> = emptyList(),
    val metadata: Map<String, Any> = emptyMap(),
    val parameters: Map<String, Any> = emptyMap(),   // v2.0: Task input parameters
    val result: Map<String, Any>? = null,            // v2.0: Task execution result
    val progress: Float = 0f
)
```

**v2.0 Enhancements:**
- `type`: Enables task routing and classification
- `parameters`: Input data for task execution
- `result`: Output data after task completion
- All fields support default values for easier construction

### AgentMessage

```kotlin
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
```

## MCP Integration (Model Context Protocol)

The v2.0 framework uses MCP for agent communication and coordination.

```kotlin
class MCPServer private constructor() {
    suspend fun start(): Result<Unit>
    suspend fun createConnection(agentId: String, agentType: AgentType): Result<MCPConnection>
    suspend fun sendMessage(message: MCPMessage): Result<Unit>
    suspend fun stop(): Result<Unit>
}
```

**MCP Features:**
- Real-time message routing
- Context sharing and state management
- Resource management and tool access
- Performance monitoring
- Session management

## Implementation Guidelines

### Creating a DepartmentalOrchestrator

```kotlin
class MyDepartmentOrchestrator(
    private val context: Context
) : DepartmentalOrchestrator {
    
    override val agentType: AgentType = AgentType.MY_DEPARTMENT_ORCHESTRATOR
    override val departmentName: String = "My Department"
    
    private val _status = MutableStateFlow(SystemStatus.INITIALIZING)
    override val status: StateFlow<SystemStatus> = _status.asStateFlow()
    
    private val mutex = Mutex()
    private val knowledgeBase = mutableMapOf<String, Any>()
    
    override val toolsets = listOf(
        OrchestratorTool(
            name = "My Tool",
            description = "Tool description",
            toolType = ToolType.AI_SERVICE,
            permissions = listOf(Permission.INTERNET_ACCESS)
        )
    )
    
    override val capabilities = listOf(
        AgentCapability(
            name = "My Capability",
            description = "Capability description",
            inputTypes = listOf("Input1", "Input2"),
            outputTypes = listOf("Output1"),
            skillLevel = SkillLevel.EXPERT
        )
    )
    
    override suspend fun initialize(): Result<Unit> = try {
        mutex.withLock {
            _status.value = SystemStatus.INITIALIZING
            // Initialize department-specific components
            _status.value = SystemStatus.ACTIVE
            Result.success(Unit)
        }
    } catch (e: Exception) {
        _status.value = SystemStatus.ERROR
        Result.failure(e)
    }
    
    override suspend fun processTask(task: NextGenTask): Result<NextGenTask> {
        // Process task logic
        return Result.success(task.copy(status = TaskStatus.COMPLETED))
    }
    
    // Implement other required methods...
}
```

### Creating a SpecializedAgent

```kotlin
class MySpecializedAgent : SpecializedAgent {
    override val agentId = "my_specialized_agent"
    override val agentType = AgentType.MY_DEPARTMENT_ORCHESTRATOR
    override val specialization = "Specific task handling"
    
    private val mcpServer = MCPServer.getInstance()
    private var mcpConnection: MCPConnection? = null
    
    private val _isActive = MutableStateFlow(false)
    override val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    override suspend fun initialize(): Result<Unit> = try {
        val connectionResult = mcpServer.createConnection(agentId, agentType)
        connectionResult.fold(
            onSuccess = { connection ->
                mcpConnection = connection
                _isActive.value = true
                Result.success(Unit)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun processTask(task: NextGenTask): Result<NextGenTask> = try {
        // Process task based on task.type
        val result = when (task.type) {
            "my_task_type" -> handleMyTask(task)
            else -> throw IllegalArgumentException("Unknown task type: ${task.type}")
        }
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    private suspend fun handleMyTask(task: NextGenTask): NextGenTask {
        // Extract parameters
        val param1 = task.parameters["param1"] as? String
        
        // Process task
        // ...
        
        // Return completed task with results
        return task.copy(
            status = TaskStatus.COMPLETED,
            result = mapOf(
                "output1" to "value1",
                "output2" to "value2"
            )
        )
    }
    
    override suspend fun shutdown(): Result<Unit> = try {
        mcpConnection?.close()
        _isActive.value = false
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## Best Practices

### 1. Error Handling
- Always use `Result<T>` for methods that can fail
- Log errors with appropriate severity
- Provide meaningful error messages

```kotlin
override suspend fun processTask(task: NextGenTask): Result<NextGenTask> = try {
    // Task processing logic
    Result.success(processedTask)
} catch (e: Exception) {
    Log.e(TAG, "Failed to process task: ${task.description}", e)
    Result.failure(e)
}
```

### 2. State Management
- Use `StateFlow` for reactive state
- Use `Mutex` for thread safety
- Update state atomically

```kotlin
private val _status = MutableStateFlow(SystemStatus.INITIALIZING)
val status: StateFlow<SystemStatus> = _status.asStateFlow()

suspend fun updateStatus(newStatus: SystemStatus) {
    mutex.withLock {
        _status.value = newStatus
    }
}
```

### 3. Task Processing
- Use `TaskStatus` enum (not strings!)
- Always populate `result` map on completion
- Copy tasks with updated fields

```kotlin
return task.copy(
    status = TaskStatus.COMPLETED,  // NOT "completed"
    result = mapOf(
        "key1" to value1,
        "key2" to value2
    )
)
```

### 4. MCP Integration
- Initialize MCP connection in `initialize()`
- Close connection in `shutdown()`
- Use MCP for agent-to-agent communication

### 5. Context Parameter
- All orchestrators require `Context` parameter
- Pass context from parent to child components
- Use context for Android system integrations

## Migration from Legacy Patterns

### Legacy CrmAgent → v2.0 Framework

**Legacy Pattern:**
```kotlin
class CrmAgent(private val context: Context) {
    fun callLead(lead: Lead, permissionManager: PermissionManager) {
        // Direct Android integration
    }
}
```

**v2.0 Pattern:**
```kotlin
// 1. Create CRMOrchestrator (DepartmentalOrchestrator)
class CRMOrchestrator(private val context: Context) : DepartmentalOrchestrator {
    // Orchestrator-level coordination
}

// 2. Create specialized agents for specific tasks
class ContactManagementAgent : SpecializedAgent {
    // Focused contact management
}
```

**Migration Steps:**
1. Identify functionality areas in legacy class
2. Create appropriate SpecializedAgents for each area
3. Create or update DepartmentalOrchestrator to manage agents
4. Update callers to use OrchestratorManager
5. Deprecate legacy class with @Deprecated annotation

## Testing

### Unit Testing Agents

```kotlin
@Test
fun testAgentProcessesTask() = runBlocking {
    val agent = MySpecializedAgent()
    agent.initialize().getOrThrow()
    
    val task = NextGenTask(
        description = "Test task",
        type = "my_task_type",
        priority = Priority.MEDIUM,
        parameters = mapOf("param1" to "value1")
    )
    
    val result = agent.processTask(task).getOrThrow()
    
    assertEquals(TaskStatus.COMPLETED, result.status)
    assertNotNull(result.result)
    assertEquals("expected_value", result.result?.get("output1"))
    
    agent.shutdown()
}
```

### Integration Testing with MCP

```kotlin
@Test
fun testAgentCommunication() = runBlocking {
    val mcpServer = MCPServer.getInstance()
    mcpServer.start().getOrThrow()
    
    val agent1 = MyAgent1()
    val agent2 = MyAgent2()
    
    agent1.initialize().getOrThrow()
    agent2.initialize().getOrThrow()
    
    // Test inter-agent communication
    // ...
    
    agent1.shutdown()
    agent2.shutdown()
    mcpServer.stop()
}
```

## Summary

The NextGen BuildPro v2.0 agent modeling framework provides:

✅ **Standardized Interfaces**: Clear contracts for all agent types  
✅ **Hierarchical Organization**: Orchestrators → Specialized Agents  
✅ **MCP Integration**: Robust communication infrastructure  
✅ **Type Safety**: Kotlin coroutines + Result types  
✅ **Scalability**: 6 departments × 8 agents = 48 specialized agents  
✅ **Maintainability**: Consistent patterns across all agents  
✅ **Testability**: Clear interfaces for unit and integration testing  

By following this framework, all agents in the NextGen BuildPro system maintain consistency, interoperability, and adherence to best practices.
