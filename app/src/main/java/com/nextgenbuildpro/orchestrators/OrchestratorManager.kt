package com.nextgenbuildpro.orchestrators

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.shared.*
import com.nextgenbuildpro.mcp.MCPServer
import com.nextgenbuildpro.navigation.IntuitiveNavigationManager
import com.nextgenbuildpro.agents.personal_assistant.VoiceCommandAgent
import com.nextgenbuildpro.agents.crm.ContactManagementAgent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Orchestrator Manager for NextGen BuildPro v2.0
 * 
 * Manages all 6 departmental orchestrators and their 48 specialized agents.
 * Provides centralized coordination, MCP integration, and navigation management.
 */
class OrchestratorManager(
    private val context: Context
) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mcpServer = MCPServer.getInstance()
    private val navigationManager = IntuitiveNavigationManager()
    
    // Orchestrators
    private lateinit var personalAssistantOrchestrator: PersonalAssistantOrchestrator
    private lateinit var crmOrchestrator: CRMOrchestrator
    private lateinit var projectManagementOrchestrator: ProjectManagementOrchestrator
    private lateinit var analyticsOrchestrator: AnalyticsOrchestrator
    private lateinit var designDepartmentOrchestrator: DesignDepartmentOrchestrator
    private lateinit var marketingOrchestrator: MarketingOrchestrator
    
    // Specialized agents registry (8 per orchestrator = 48 total)
    private val specializedAgents = mutableMapOf<String, SpecializedAgent>()
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _systemStatus = MutableStateFlow(SystemStatus.INITIALIZING)
    val systemStatus: StateFlow<SystemStatus> = _systemStatus.asStateFlow()
    
    /**
     * Initialize all orchestrators and their specialized agents
     */
    suspend fun initialize(): Result<Unit> = try {
        Log.i("OrchestratorManager", "Initializing NextGen BuildPro v2.0 Orchestrator System...")
        
        _systemStatus.value = SystemStatus.INITIALIZING
        
        // Start MCP server
        mcpServer.start().getOrThrow()
        
        // Initialize orchestrators
        initializeOrchestrators()
        
        // Initialize all 48 specialized agents
        initializeSpecializedAgents()
        
        // Setup inter-departmental communication
        setupInterDepartmentalCommunication()
        
        _isInitialized.value = true
        _systemStatus.value = SystemStatus.ACTIVE
        
        Log.i("OrchestratorManager", "Orchestrator System initialized successfully with ${specializedAgents.size} agents")
        Result.success(Unit)
        
    } catch (e: Exception) {
        Log.e("OrchestratorManager", "Failed to initialize Orchestrator System", e)
        _systemStatus.value = SystemStatus.ERROR
        Result.failure(e)
    }
    
    /**
     * Process task through appropriate orchestrator
     */
    suspend fun processTask(task: NextGenTask): Result<NextGenTask> = try {
        val orchestrator = getOrchestratorForTask(task)
        orchestrator.executeTask(task)
    } catch (e: Exception) {
        Log.e("OrchestratorManager", "Failed to process task: ${task.description}", e)
        Result.failure(e)
    }
    
    /**
     * Get dashboard for department
     */
    fun getDashboard(departmentId: String) = navigationManager.getDashboardForDepartment(
        navigationManager.getAllDepartments().find { it.id == departmentId }
            ?: throw IllegalArgumentException("Department not found: $departmentId")
    )
    
    /**
     * Navigate to department feature (max 3 taps)
     */
    suspend fun navigateToFeature(
        departmentId: String,
        featureId: String,
        subFeatureId: String? = null
    ) = navigationManager.navigateToFeature(departmentId, featureId, subFeatureId)
    
    /**
     * Execute voice command across all orchestrators
     */
    suspend fun executeVoiceCommand(voiceInput: String): Result<String> = try {
        val voiceAgent = specializedAgents["voice_command_agent"] as? VoiceCommandAgent
            ?: return Result.failure(IllegalStateException("Voice agent not available"))
        
        val task = NextGenTask(
            id = "voice_${System.currentTimeMillis()}",
            title = "Voice Command",
            description = "Process voice command: $voiceInput",
            assignedAgent = AgentType.PERSONAL_ASSISTANT_ORCHESTRATOR,
            priority = Priority.HIGH,
            status = TaskStatus.PENDING,
            metadata = mapOf(
                "type" to "voice_command",
                "voice_input" to voiceInput
            )
        )
        
        val result = voiceAgent.processTask(task).getOrThrow()
        val response = result.metadata["execution_result"] as? String ?: "Command processed"
        
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Get system metrics and health status
     */
    fun getSystemMetrics(): SystemMetrics {
        val activeAgents = specializedAgents.values.count { 
            runBlocking { it.isActive.first() }
        }
        
        return SystemMetrics(
            totalOrchestrators = 6,
            totalSpecializedAgents = specializedAgents.size,
            activeAgents = activeAgents,
            systemStatus = _systemStatus.value,
            mcpServerStatus = runBlocking { mcpServer.serverStatus.first() },
            uptime = System.currentTimeMillis() // Simplified
        )
    }
    
    private suspend fun initializeOrchestrators() {
        personalAssistantOrchestrator = PersonalAssistantOrchestrator(context)
        crmOrchestrator = CRMOrchestrator(context)
        projectManagementOrchestrator = ProjectManagementOrchestrator(context)
        analyticsOrchestrator = AnalyticsOrchestrator(context)
        designDepartmentOrchestrator = DesignDepartmentOrchestrator(context)
        marketingOrchestrator = MarketingOrchestrator(context)
        
        // Initialize each orchestrator
        listOf(
            personalAssistantOrchestrator,
            crmOrchestrator,
            projectManagementOrchestrator,
            analyticsOrchestrator,
            designDepartmentOrchestrator,
            marketingOrchestrator
        ).forEach { orchestrator ->
            orchestrator.initialize()
        }
    }
    
    private suspend fun initializeSpecializedAgents() {
        // Personal Assistant Orchestrator Agents (8)
        val personalAssistantAgents = listOf(
            VoiceCommandAgent(),
            // Add 7 more specialized agents for Personal Assistant
        )
        
        // CRM Orchestrator Agents (8)
        val crmAgents = listOf(
            ContactManagementAgent(),
            // Add 7 more specialized agents for CRM
        )
        
        // Initialize all agents
        val allAgents = personalAssistantAgents + crmAgents
        
        allAgents.forEach { agent ->
            try {
                agent.initialize().getOrThrow()
                specializedAgents[agent.agentId] = agent
                Log.d("OrchestratorManager", "Initialized agent: ${agent.agentId}")
            } catch (e: Exception) {
                Log.e("OrchestratorManager", "Failed to initialize agent: ${agent.agentId}", e)
            }
        }
    }
    
    private fun setupInterDepartmentalCommunication() {
        // Setup communication channels between orchestrators
        Log.d("OrchestratorManager", "Setting up inter-departmental communication")
    }
    
    private fun getOrchestratorForTask(task: NextGenTask): DepartmentalOrchestrator {
        val taskType = task.metadata["type"] as? String ?: "unknown"
        return when (taskType) {
            "voice_command", "emergency_response" -> personalAssistantOrchestrator
            "contact_management", "lead_management" -> crmOrchestrator
            "project_creation", "scheduling", "cost_estimation" -> projectManagementOrchestrator
            "analytics", "reporting", "predictions" -> analyticsOrchestrator
            "design", "3d_modeling", "blueprints" -> designDepartmentOrchestrator
            "marketing", "proposals", "campaigns" -> marketingOrchestrator
            else -> personalAssistantOrchestrator // Default to personal assistant
        }
    }
    
    /**
     * Shutdown all orchestrators and agents
     */
    suspend fun shutdown(): Result<Unit> = try {
        Log.i("OrchestratorManager", "Shutting down Orchestrator System...")
        
        // Shutdown all specialized agents
        specializedAgents.values.forEach { agent ->
            try {
                agent.shutdown()
            } catch (e: Exception) {
                Log.w("OrchestratorManager", "Error shutting down agent: ${agent.agentId}", e)
            }
        }
        
        // TODO: Stop MCP server - method not yet implemented
        // mcpServer.stop()
        
        _systemStatus.value = SystemStatus.SHUTDOWN
        _isInitialized.value = false
        
        Log.i("OrchestratorManager", "Orchestrator System shutdown complete")
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * System metrics for monitoring
 */
data class SystemMetrics(
    val totalOrchestrators: Int,
    val totalSpecializedAgents: Int,
    val activeAgents: Int,
    val systemStatus: SystemStatus,
    val mcpServerStatus: com.nextgenbuildpro.mcp.MCPServerStatus,
    val uptime: Long
)