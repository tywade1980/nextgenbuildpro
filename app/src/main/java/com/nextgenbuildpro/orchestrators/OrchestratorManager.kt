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
 * Manages C-suite executive orchestrators and their operational agents.
 * Structured as a corporate hierarchy: CEO coordinates with COO, CFO, CHRO, CTO, CSO.
 * Each C-suite executive manages 5-8 operational agents (sub-agents) in their domain.
 */
class OrchestratorManager(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mcpServer = MCPServer.getInstance()
    private val navigationManager = IntuitiveNavigationManager()
    
    // C-Suite Executive Orchestrators
    private lateinit var ceoPersonalAssistantOrchestrator: CEOPersonalAssistantOrchestrator
    private lateinit var cooOperationsOrchestrator: COOOperationsOrchestrator
    private lateinit var cfoFinancialOrchestrator: CFOFinancialOrchestrator
    private lateinit var chroClientHROrchestrator: CHROClientHROrchestrator
    private lateinit var ctoDesignOrchestrator: CTODesignOrchestrator
    private lateinit var csoSafetyOrchestrator: CSOSafetyOrchestrator
    
    // Operational agents registry (5-8 per C-suite executive)
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
        orchestrator.processTask(task)
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
            type = "voice_command",
            description = "Process voice command: $voiceInput",
            parameters = mapOf("voice_input" to voiceInput),
            priority = Priority.HIGH
        )
        
        val result = voiceAgent.processTask(task).getOrThrow()
        val response = result.result?.get("execution_result") as? String ?: "Command processed"
        
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
            totalOrchestrators = 6, // 6 C-suite executives
            totalSpecializedAgents = specializedAgents.size,
            activeAgents = activeAgents,
            systemStatus = _systemStatus.value,
            mcpServerStatus = runBlocking { mcpServer.serverStatus.first() },
            uptime = System.currentTimeMillis() // Simplified
        )
    }
    
    private suspend fun initializeOrchestrators() {
        // C-Suite Executive Orchestrators
        ceoPersonalAssistantOrchestrator = CEOPersonalAssistantOrchestrator(context)
        cooOperationsOrchestrator = COOOperationsOrchestrator(context)
        cfoFinancialOrchestrator = CFOFinancialOrchestrator(context)
        chroClientHROrchestrator = CHROClientHROrchestrator(context)
        ctoDesignOrchestrator = CTODesignOrchestrator(context)
        csoSafetyOrchestrator = CSOSafetyOrchestrator(context)
        
        // Initialize all C-suite executives (6 total)
        listOf(
            ceoPersonalAssistantOrchestrator,
            cooOperationsOrchestrator,
            cfoFinancialOrchestrator,
            chroClientHROrchestrator,
            ctoDesignOrchestrator,
            csoSafetyOrchestrator
        ).forEach { orchestrator ->
            orchestrator.initialize()
        }
    }
    
    private suspend fun initializeSpecializedAgents() {
        // CEO Personal Assistant Operational Agents
        val ceoAgents = listOf(
            VoiceCommandAgent(),
            // Add more specialized agents for CEO
        )
        
        // CHRO Client Relations & HR Operational Agents
        val chroAgents = listOf(
            ContactManagementAgent(),
            // Add more specialized agents for CHRO
        )
        
        // Initialize all operational agents
        val allAgents = ceoAgents + chroAgents
        
        allAgents.forEach { agent ->
            try {
                agent.initialize().getOrThrow()
                specializedAgents[agent.agentId] = agent
                Log.d("OrchestratorManager", "Initialized operational agent: ${agent.agentId}")
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
        return when (task.type) {
            "voice_command", "emergency_response", "executive_decision" -> ceoPersonalAssistantOrchestrator
            // COO: Operations & PM tasks (field ops, equipment, PM, field quality)
            "crew_scheduling", "material_delivery", "field_issue",
            "equipment_tracking", "maintenance", "rental", "tool_receipt",
            "project_creation", "scheduling", "resource_allocation",
            "field_inspection", "progress_report", "quality_check" -> cooOperationsOrchestrator
            // CFO: Financial & Analytics tasks (estimating, accounting, analytics)
            "cost_estimation", "bid_preparation", "value_engineering", "change_order",
            "invoicing", "payroll", "financial_report", "budget_tracking",
            "analytics", "reporting", "predictions", "performance_metrics" -> cfoFinancialOrchestrator
            // CHRO: Client Relations & HR tasks (CRM, marketing, HR, client quality)
            "contact_management", "lead_management", "client_communication",
            "proposal_creation", "marketing_campaign", "content_creation",
            "recruitment", "onboarding", "training", "time_tracking",
            "client_punch_list", "client_satisfaction", "defect_resolution" -> chroClientHROrchestrator
            // CTO: Design tasks
            "design", "3d_modeling", "blueprints", "cad", "technical_drawing" -> ctoDesignOrchestrator
            // CSO: Safety & Compliance tasks
            "safety_inspection", "permit_application", "compliance_check", "incident_report" -> csoSafetyOrchestrator
            else -> ceoPersonalAssistantOrchestrator // Default to CEO
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
        
        // Stop MCP server
        mcpServer.stop()
        
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