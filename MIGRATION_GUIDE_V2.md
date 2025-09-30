# Migration Guide: Legacy Agents to v2.0 Framework

## Overview

This guide provides step-by-step instructions for migrating from legacy agent patterns to the NextGen BuildPro v2.0 agent modeling framework.

## Why Migrate?

The v2.0 framework provides:

- ✅ **MCP Integration**: Robust inter-agent communication
- ✅ **Standardized Interfaces**: Consistent patterns across all agents
- ✅ **Better Organization**: Hierarchical orchestrator → specialized agent structure
- ✅ **Enhanced Scalability**: Clear separation of concerns
- ✅ **Type Safety**: Proper enum types instead of strings
- ✅ **Future Compatibility**: New features built on v2.0 framework

## Migration: CrmAgent → CRMOrchestrator + Specialized Agents

### Legacy Pattern (Deprecated)

```kotlin
// OLD: ServiceModule.kt
class ServiceModule {
    private lateinit var crmAgent: CrmAgent
    
    fun initialize(context: Context) {
        crmAgent = CrmAgent(context)
    }
    
    fun getCrmAgent(): CrmAgent {
        return crmAgent
    }
}

// Usage in MainActivity
val crmAgent = serviceModule.getCrmAgent()
crmAgent.callLead(lead, permissionManager)
crmAgent.sendMessage(lead, message, permissionManager)
```

### v2.0 Pattern (Recommended)

```kotlin
// NEW: Use OrchestratorManager
class MainActivity : ComponentActivity() {
    private lateinit var orchestratorManager: OrchestratorManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize orchestrator system
        orchestratorManager = OrchestratorManager(this)
        lifecycleScope.launch {
            orchestratorManager.initialize().getOrThrow()
        }
    }
    
    // Process CRM tasks through the orchestrator
    private suspend fun handleCrmTask(taskType: String, parameters: Map<String, Any>) {
        val task = NextGenTask(
            description = "CRM task: $taskType",
            type = taskType,
            priority = Priority.MEDIUM,
            parameters = parameters
        )
        
        val result = orchestratorManager.processTask(task).getOrThrow()
        // Handle result
    }
}
```

### Detailed Migration Steps

#### Step 1: Update Initialization

**Before:**
```kotlin
// ServiceModule.kt
crmAgent = CrmAgent(context)
```

**After:**
```kotlin
// In your Application or Activity
orchestratorManager = OrchestratorManager(context)
orchestratorManager.initialize()
```

#### Step 2: Update Function Calls

**Before: Making a call**
```kotlin
crmAgent.callLead(lead, permissionManager)
```

**After: Using task-based approach**
```kotlin
val task = NextGenTask(
    description = "Call lead: ${lead.name}",
    type = "call_lead",
    priority = Priority.HIGH,
    parameters = mapOf(
        "lead_id" to lead.id,
        "phone_number" to lead.phone
    )
)
orchestratorManager.processTask(task)
```

**Before: Sending a message**
```kotlin
crmAgent.sendMessage(lead, message, permissionManager)
```

**After: Using task-based approach**
```kotlin
val task = NextGenTask(
    description = "Send SMS to: ${lead.name}",
    type = "send_sms",
    priority = Priority.MEDIUM,
    parameters = mapOf(
        "lead_id" to lead.id,
        "phone_number" to lead.phone,
        "message" to message
    )
)
orchestratorManager.processTask(task)
```

#### Step 3: Update Data Access

**Before: Direct state access**
```kotlin
val leads = crmAgent.leads.value
val callHistory = crmAgent.callHistory.value
```

**After: Request through orchestrator**
```kotlin
val task = NextGenTask(
    description = "Get all leads",
    type = "get_leads",
    priority = Priority.LOW,
    parameters = mapOf("status" to "active")
)
val result = orchestratorManager.processTask(task).getOrThrow()
val leads = result.result?.get("leads") as? List<Lead>
```

#### Step 4: Handle Voice Commands

**Before: Not available in legacy**
```kotlin
// No voice command support
```

**After: Built-in voice support**
```kotlin
orchestratorManager.executeVoiceCommand("add contact John Doe")
```

### Function Mapping Table

| Legacy CrmAgent Method | v2.0 Framework Equivalent | Task Type |
|------------------------|---------------------------|-----------|
| `callLead(lead, pm)` | `processTask()` | `call_lead` |
| `sendMessage(lead, msg, pm)` | `processTask()` | `send_sms` |
| `scheduleCall(lead, time, notes)` | `processTask()` | `schedule_call` |
| `scheduleFollowUp(lead, type, time, notes)` | `processTask()` | `schedule_followup` |
| `updateLeadStatus(id, status)` | `processTask()` | `update_lead_status` |
| `addLeadNote(id, note)` | `processTask()` | `add_lead_note` |
| `getLeadsByStatus(status)` | `processTask()` | `get_leads_by_status` |
| `getTodaysCalls()` | `processTask()` | `get_todays_calls` |
| `getTodaysFollowUps()` | `processTask()` | `get_todays_followups` |
| `getLeadCommunicationHistory(id)` | `processTask()` | `get_communication_history` |
| `generateMessageTemplate(lead, type)` | `processTask()` | `generate_message_template` |

### Creating Custom Task Types

If you need functionality not covered by existing specialized agents:

```kotlin
// 1. Create a new SpecializedAgent
class CustomCrmAgent : SpecializedAgent {
    override val agentId = "custom_crm_agent"
    override val agentType = AgentType.CRM_ORCHESTRATOR
    override val specialization = "Custom CRM functionality"
    
    // ... implement interface methods
    
    override suspend fun processTask(task: NextGenTask): Result<NextGenTask> {
        return when (task.type) {
            "my_custom_task" -> handleCustomTask(task)
            else -> Result.failure(IllegalArgumentException("Unknown task type"))
        }
    }
}

// 2. Register with OrchestratorManager
// (This would be done in OrchestratorManager.initializeSpecializedAgents())
```

## Common Migration Issues

### Issue 1: Direct Android API Access

**Problem:**
```kotlin
// Legacy: Direct Android API calls
val intent = Intent(Intent.ACTION_CALL)
intent.data = Uri.parse("tel:${phoneNumber}")
context.startActivity(intent)
```

**Solution:**
```kotlin
// v2.0: Use permissions through task parameters
val task = NextGenTask(
    type = "make_call",
    parameters = mapOf(
        "phone_number" to phoneNumber,
        "require_permission" to Permission.MAKE_CALLS
    )
)
```

### Issue 2: State Management

**Problem:**
```kotlin
// Legacy: Mutable state in agent
private val _leads = mutableStateOf<List<Lead>>(emptyList())
```

**Solution:**
```kotlin
// v2.0: State managed by orchestrator, accessed through tasks
// State is encapsulated within orchestrator/agent
// External access through well-defined tasks
```

### Issue 3: Synchronous vs Asynchronous

**Problem:**
```kotlin
// Legacy: Synchronous methods
fun callLead(lead: Lead) {
    // Blocking operation
}
```

**Solution:**
```kotlin
// v2.0: All operations are suspending functions
suspend fun processTask(task: NextGenTask): Result<NextGenTask> {
    // Non-blocking operation
}

// Usage:
lifecycleScope.launch {
    orchestratorManager.processTask(task)
}
```

## Testing After Migration

### Unit Tests

```kotlin
@Test
fun testCrmTaskProcessing() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val orchestratorManager = OrchestratorManager(context)
    orchestratorManager.initialize().getOrThrow()
    
    val task = NextGenTask(
        description = "Test CRM task",
        type = "test_task",
        priority = Priority.MEDIUM,
        parameters = mapOf("test_param" to "test_value")
    )
    
    val result = orchestratorManager.processTask(task).getOrThrow()
    assertEquals(TaskStatus.COMPLETED, result.status)
    
    orchestratorManager.shutdown()
}
```

### Integration Tests

```kotlin
@Test
fun testEndToEndCrmFlow() = runBlocking {
    // 1. Initialize system
    val orchestratorManager = OrchestratorManager(context)
    orchestratorManager.initialize().getOrThrow()
    
    // 2. Create contact
    val createTask = NextGenTask(
        type = "create_contact_from_call",
        parameters = mapOf(
            "phone_number" to "555-1234",
            "call_duration" to 120000L
        )
    )
    val createResult = orchestratorManager.processTask(createTask).getOrThrow()
    val contactId = createResult.result?.get("contact_id") as String
    
    // 3. Update contact
    val updateTask = NextGenTask(
        type = "update_contact",
        parameters = mapOf(
            "contact_id" to contactId,
            "updates" to mapOf("name" to "John Doe")
        )
    )
    orchestratorManager.processTask(updateTask).getOrThrow()
    
    // 4. Verify
    // ...
}
```

## Gradual Migration Strategy

You don't have to migrate everything at once. Here's a gradual approach:

### Phase 1: Parallel Operation
```kotlin
// Keep both systems running
private val legacyCrmAgent = CrmAgent(context)
private val orchestratorManager = OrchestratorManager(context)

// Use legacy for critical operations
legacyCrmAgent.callLead(lead, permissionManager)

// Use v2.0 for new features
orchestratorManager.executeVoiceCommand("add contact")
```

### Phase 2: Feature-by-Feature Migration
```kotlin
// Migrate one feature at a time
fun callLead(lead: Lead) {
    if (useV2Framework) {
        // New v2.0 approach
        val task = NextGenTask(type = "call_lead", ...)
        orchestratorManager.processTask(task)
    } else {
        // Legacy approach
        legacyCrmAgent.callLead(lead, permissionManager)
    }
}
```

### Phase 3: Complete Migration
```kotlin
// Remove all legacy code
// Use only OrchestratorManager
```

## Benefits After Migration

After completing the migration, you'll gain:

1. **Voice Command Support**: Built-in natural language processing
2. **MCP Communication**: Robust inter-agent messaging
3. **Better Testing**: Clear interfaces for mocking
4. **Scalability**: Easy to add new specialized agents
5. **Consistency**: All agents follow the same pattern
6. **Future-Proof**: New features built on v2.0 framework

## Support and Resources

- **Framework Documentation**: See [AGENT_FRAMEWORK_V2.md](./AGENT_FRAMEWORK_V2.md)
- **Example Implementations**:
  - `ContactManagementAgent.kt` - Specialized agent example
  - `VoiceCommandAgent.kt` - Voice processing example
  - `CRMOrchestrator.kt` - Departmental orchestrator example
- **Type Definitions**: See `shared/Types.kt` for all interfaces and data models

## Questions?

If you encounter issues during migration:

1. Review the example implementations in `/app/src/main/java/com/nextgenbuildpro/agents/`
2. Check the framework documentation in `AGENT_FRAMEWORK_V2.md`
3. Examine existing orchestrators in `/app/src/main/java/com/nextgenbuildpro/orchestrators/`
4. Ensure you're using the correct enum types (TaskStatus, not strings)
5. Verify Context is properly passed through the initialization chain

---

**Remember**: The legacy `CrmAgent` is deprecated but will continue to work during the transition period. Plan your migration timeline based on your project needs.
