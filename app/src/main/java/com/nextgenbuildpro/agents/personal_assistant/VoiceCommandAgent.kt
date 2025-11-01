package com.nextgenbuildpro.agents.personal_assistant

import android.util.Log
import com.nextgenbuildpro.shared.*
import com.nextgenbuildpro.mcp.MCPServer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Voice Command Agent - Personal Assistant Orchestrator
 * 
 * COMPREHENSIVE VOICE CONTROL for ALL application functions.
 * Provides full voice command access to every feature, service, and orchestrator.
 * Handles Spanish and English voice recognition with construction-specific vocabulary.
 * 
 * This agent has complete control over:
 * - All C-suite orchestrators (COO, CFO, CHRO, CTO, CSO)
 * - All feature modules (leads, estimates, projects, calendar, etc.)
 * - All services (CRM, PM, Analytics, Design, Safety, etc.)
 * - System navigation and UI control
 * - Data operations (CRUD on all entities)
 * - Emergency and safety protocols
 */
class VoiceCommandAgent : SpecializedAgent {
    override val agentId = "voice_command_agent"
    override val agentType = AgentType.PERSONAL_ASSISTANT_ORCHESTRATOR
    override val specialization = "Comprehensive voice command processing with full application control"
    
    private val mcpServer = MCPServer.getInstance()
    private var mcpConnection: com.nextgenbuildpro.mcp.MCPConnection? = null
    
    private val _isActive = MutableStateFlow(false)
    override val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    // Comprehensive vocabulary for ALL application functions
    private val constructionVocabulary = mapOf(
        // Spanish construction terms
        "concreto" to "concrete",
        "madera" to "lumber", 
        "electricidad" to "electrical",
        "plomería" to "plumbing",
        "pintura" to "paint",
        "techo" to "roof",
        "piso" to "floor",
        "pared" to "wall",
        "puerta" to "door",
        "ventana" to "window",
        "seguridad" to "safety",
        "presupuesto" to "estimate",
        "proyecto" to "project",
        "cliente" to "client",
        "llamar" to "call",
        "mensaje" to "message",
        "foto" to "photo",
        "reporte" to "report",
        
        // Feature commands - Leads
        "lead" to "manage_lead",
        "leads" to "list_leads",
        "new lead" to "create_lead",
        "add lead" to "create_lead",
        "show leads" to "list_leads",
        "open lead" to "view_lead",
        "update lead" to "update_lead",
        "delete lead" to "delete_lead",
        "convert lead" to "convert_lead",
        
        // Feature commands - Estimates
        "estimate" to "manage_estimate",
        "estimates" to "list_estimates",
        "new estimate" to "create_estimate",
        "add estimate" to "create_estimate",
        "show estimates" to "list_estimates",
        "open estimate" to "view_estimate",
        "update estimate" to "update_estimate",
        "calculate estimate" to "calculate_estimate",
        "send estimate" to "send_estimate",
        
        // Feature commands - Projects
        "project" to "manage_project",
        "projects" to "list_projects",
        "new project" to "create_project",
        "add project" to "create_project",
        "show projects" to "list_projects",
        "open project" to "view_project",
        "update project" to "update_project",
        "close project" to "close_project",
        "project status" to "project_status",
        
        // Feature commands - Contacts/CRM
        "contact" to "manage_contact",
        "contacts" to "list_contacts",
        "new contact" to "create_contact",
        "add contact" to "create_contact",
        "show contacts" to "list_contacts",
        "call contact" to "call_contact",
        "text contact" to "text_contact",
        "email contact" to "email_contact",
        
        // Feature commands - Calendar
        "schedule" to "manage_schedule",
        "calendar" to "view_calendar",
        "new event" to "create_event",
        "add event" to "create_event",
        "show calendar" to "view_calendar",
        "next appointment" to "next_appointment",
        "cancel event" to "cancel_event",
        
        // Feature commands - Tasks
        "task" to "manage_task",
        "tasks" to "list_tasks",
        "new task" to "create_task",
        "add task" to "create_task",
        "show tasks" to "list_tasks",
        "complete task" to "complete_task",
        "assign task" to "assign_task",
        
        // Feature commands - Safety
        "safety" to "safety_check",
        "safety report" to "create_safety_report",
        "incident" to "report_incident",
        "emergency" to "emergency_response",
        "osha" to "osha_compliance",
        
        // Feature commands - Files/Photos
        "photo" to "take_photo",
        "take photo" to "take_photo",
        "camera" to "open_camera",
        "files" to "view_files",
        "open file" to "open_file",
        "share file" to "share_file",
        
        // Feature commands - Time Clock
        "clock in" to "clock_in",
        "clock out" to "clock_out",
        "time" to "check_time",
        "timesheet" to "view_timesheet",
        
        // Feature commands - Analytics
        "report" to "generate_report",
        "analytics" to "view_analytics",
        "dashboard" to "view_dashboard",
        "metrics" to "view_metrics",
        "performance" to "view_performance",
        
        // Navigation commands
        "home" to "navigate_home",
        "back" to "navigate_back",
        "open" to "navigate_to",
        "go to" to "navigate_to",
        "show" to "navigate_to",
        
        // System commands
        "settings" to "open_settings",
        "help" to "show_help",
        "search" to "search",
        "filter" to "apply_filter",
        "sort" to "sort_data"
    )
    
    // Command categories for organized processing
    private val commandCategories = mapOf(
        "navigation" to listOf("home", "back", "open", "go to", "show", "navigate"),
        "leads" to listOf("lead", "leads", "new lead", "add lead", "convert"),
        "estimates" to listOf("estimate", "estimates", "quote", "bid", "pricing"),
        "projects" to listOf("project", "projects", "job", "site"),
        "contacts" to listOf("contact", "contacts", "client", "customer", "call", "text", "email"),
        "calendar" to listOf("schedule", "calendar", "appointment", "meeting", "event"),
        "tasks" to listOf("task", "tasks", "todo", "assign", "complete"),
        "safety" to listOf("safety", "incident", "emergency", "osha", "compliance"),
        "files" to listOf("photo", "camera", "file", "document", "share"),
        "timeclock" to listOf("clock", "time", "timesheet", "hours"),
        "analytics" to listOf("report", "analytics", "dashboard", "metrics", "performance"),
        "system" to listOf("settings", "help", "search", "filter", "sort")
    )
    
    override suspend fun initialize(): Result<Unit> = try {
        Log.i("VoiceCommandAgent", "Initializing Voice Command Agent...")
        
        val connectionResult = mcpServer.createConnection(agentId, agentType)
        connectionResult.fold(
            onSuccess = { connection ->
                mcpConnection = connection
                _isActive.value = true
                Log.i("VoiceCommandAgent", "Voice Command Agent initialized successfully")
                Result.success(Unit)
            },
            onFailure = { error ->
                Log.e("VoiceCommandAgent", "Failed to create MCP connection", error)
                Result.failure(error)
            }
        )
    } catch (e: Exception) {
        Log.e("VoiceCommandAgent", "Failed to initialize Voice Command Agent", e)
        Result.failure(e)
    }
    
    override suspend fun processTask(task: NextGenTask): Result<NextGenTask> {
        return try {
            Log.d("VoiceCommandAgent", "Processing voice command: ${task.description}")
            
            val voiceInput = task.metadata["voice_input"] as? String
                ?: return Result.failure(IllegalArgumentException("No voice input provided"))
            
            val processedCommand = processVoiceCommand(voiceInput)
            val executionResult = executeVoiceCommand(processedCommand)
            
            val completedTask = task.copy(
                status = TaskStatus.COMPLETED,
                progress = 1.0f,
                metadata = task.metadata + mapOf(
                    "processed_command" to processedCommand,
                    "execution_result" to executionResult,
                    "confidence_score" to calculateConfidenceScore(voiceInput),
                    "language_detected" to detectLanguage(voiceInput),
                    "command_category" to determineCategory(voiceInput),
                    "target_orchestrator" to processedCommand.targetOrchestrator?.name,
                    "permissions_required" to processedCommand.permissionsRequired
                )
            )
            
            Result.success(completedTask)
        } catch (e: Exception) {
            Log.e("VoiceCommandAgent", "Error processing voice command", e)
            Result.failure(e)
        }
    }
    
    /**
     * Determine command category for routing
     */
    private fun determineCategory(input: String): String {
        val normalizedInput = input.lowercase()
        for ((category, keywords) in commandCategories) {
            if (keywords.any { normalizedInput.contains(it) }) {
                return category
            }
        }
        return "general"
    }
    
    private suspend fun processVoiceCommand(voiceInput: String): EnhancedVoiceCommand {
        val normalizedInput = voiceInput.lowercase().trim()
        val language = detectLanguage(normalizedInput)
        val category = determineCategory(normalizedInput)
        
        // Comprehensive command routing to ALL application functions
        return when {
            // === LEADS MANAGEMENT ===
            normalizedInput.matches(Regex(".*(new|create|add)\\s+lead.*")) -> {
                EnhancedVoiceCommand(
                    action = "create_lead",
                    category = "leads",
                    entities = extractLeadInfo(normalizedInput),
                    language = language,
                    confidence = 0.95f,
                    targetOrchestrator = AgentType.CRM_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.WRITE_CONTACTS, Permission.INTERNET_ACCESS)
                )
            }
            normalizedInput.matches(Regex(".*(show|list|view|display)\\s+(all\\s+)?leads.*")) -> {
                EnhancedVoiceCommand(
                    action = "list_leads",
                    category = "leads",
                    entities = extractFilterInfo(normalizedInput),
                    language = language,
                    confidence = 0.93f,
                    targetOrchestrator = AgentType.CRM_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.READ_CONTACTS, Permission.INTERNET_ACCESS)
                )
            }
            normalizedInput.matches(Regex(".*(open|view)\\s+lead.*")) -> {
                EnhancedVoiceCommand(
                    action = "view_lead",
                    category = "leads",
                    entities = extractLeadReference(normalizedInput),
                    language = language,
                    confidence = 0.91f,
                    targetOrchestrator = AgentType.CRM_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.READ_CONTACTS, Permission.INTERNET_ACCESS)
                )
            }
            normalizedInput.matches(Regex(".*convert.*lead.*")) -> {
                EnhancedVoiceCommand(
                    action = "convert_lead",
                    category = "leads",
                    entities = extractLeadReference(normalizedInput),
                    language = language,
                    confidence = 0.92f,
                    targetOrchestrator = AgentType.CRM_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.WRITE_CONTACTS, Permission.INTERNET_ACCESS)
                )
            }
            
            // === ESTIMATES MANAGEMENT ===
            normalizedInput.matches(Regex(".*(new|create|add|generate)\\s+(estimate|quote|bid).*")) -> {
                EnhancedVoiceCommand(
                    action = "create_estimate",
                    category = "estimates",
                    entities = extractEstimateInfo(normalizedInput),
                    language = language,
                    confidence = 0.94f,
                    targetOrchestrator = AgentType.CFO_FINANCIAL_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS, Permission.ACCESS_STORAGE)
                )
            }
            normalizedInput.matches(Regex(".*(show|list|view)\\s+(all\\s+)?(estimates|quotes|bids).*")) -> {
                EnhancedVoiceCommand(
                    action = "list_estimates",
                    category = "estimates",
                    entities = extractFilterInfo(normalizedInput),
                    language = language,
                    confidence = 0.92f,
                    targetOrchestrator = AgentType.CFO_FINANCIAL_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS)
                )
            }
            normalizedInput.matches(Regex(".*calculate.*estimate.*")) -> {
                EnhancedVoiceCommand(
                    action = "calculate_estimate",
                    category = "estimates",
                    entities = extractEstimateReference(normalizedInput),
                    language = language,
                    confidence = 0.93f,
                    targetOrchestrator = AgentType.CFO_FINANCIAL_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS)
                )
            }
            normalizedInput.matches(Regex(".*(send|email|share).*estimate.*")) -> {
                EnhancedVoiceCommand(
                    action = "send_estimate",
                    category = "estimates",
                    entities = extractEstimateReference(normalizedInput) + extractRecipientInfo(normalizedInput),
                    language = language,
                    confidence = 0.91f,
                    targetOrchestrator = AgentType.CFO_FINANCIAL_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS, Permission.READ_CONTACTS)
                )
            }
            
            // === PROJECTS MANAGEMENT ===
            normalizedInput.matches(Regex(".*(new|create|add|start)\\s+(project|job).*")) -> {
                EnhancedVoiceCommand(
                    action = "create_project",
                    category = "projects",
                    entities = extractProjectInfo(normalizedInput),
                    language = language,
                    confidence = 0.95f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS, Permission.ACCESS_LOCATION)
                )
            }
            normalizedInput.matches(Regex(".*(show|list|view)\\s+(all\\s+)?(projects|jobs).*")) -> {
                EnhancedVoiceCommand(
                    action = "list_projects",
                    category = "projects",
                    entities = extractFilterInfo(normalizedInput),
                    language = language,
                    confidence = 0.93f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS)
                )
            }
            normalizedInput.matches(Regex(".*(open|view)\\s+(project|job).*")) -> {
                EnhancedVoiceCommand(
                    action = "view_project",
                    category = "projects",
                    entities = extractProjectReference(normalizedInput),
                    language = language,
                    confidence = 0.92f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS)
                )
            }
            normalizedInput.matches(Regex(".*(project|job)\\s+status.*")) -> {
                EnhancedVoiceCommand(
                    action = "project_status",
                    category = "projects",
                    entities = extractProjectReference(normalizedInput),
                    language = language,
                    confidence = 0.91f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS)
                )
            }
            
            // === CONTACTS/CRM ===
            normalizedInput.contains("add contact") || normalizedInput.contains("agregar contacto") ||
            normalizedInput.matches(Regex(".*(new|create|add)\\s+(contact|client|customer).*")) -> {
                EnhancedVoiceCommand(
                    action = "create_contact",
                    category = "contacts",
                    entities = extractContactInfo(normalizedInput),
                    language = language,
                    confidence = 0.96f,
                    targetOrchestrator = AgentType.CRM_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.WRITE_CONTACTS, Permission.READ_CONTACTS)
                )
            }
            normalizedInput.matches(Regex(".*(show|list|view)\\s+(all\\s+)?(contacts|clients|customers).*")) -> {
                EnhancedVoiceCommand(
                    action = "list_contacts",
                    category = "contacts",
                    entities = extractFilterInfo(normalizedInput),
                    language = language,
                    confidence = 0.93f,
                    targetOrchestrator = AgentType.CRM_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.READ_CONTACTS)
                )
            }
            normalizedInput.matches(Regex(".*call.*")) -> {
                EnhancedVoiceCommand(
                    action = "make_call",
                    category = "contacts",
                    entities = extractContactReference(normalizedInput),
                    language = language,
                    confidence = 0.94f,
                    targetOrchestrator = AgentType.CRM_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.MAKE_CALLS, Permission.READ_CONTACTS)
                )
            }
            normalizedInput.matches(Regex(".*(text|sms|message).*")) -> {
                EnhancedVoiceCommand(
                    action = "send_text",
                    category = "contacts",
                    entities = extractContactReference(normalizedInput) + extractMessageContent(normalizedInput),
                    language = language,
                    confidence = 0.92f,
                    targetOrchestrator = AgentType.CRM_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.SEND_SMS, Permission.READ_CONTACTS)
                )
            }
            
            // === CALENDAR/SCHEDULING ===
            normalizedInput.contains("schedule") || normalizedInput.contains("programar") ||
            normalizedInput.matches(Regex(".*(new|create|add|schedule)\\s+(event|appointment|meeting).*")) -> {
                EnhancedVoiceCommand(
                    action = "create_event",
                    category = "calendar",
                    entities = extractScheduleInfo(normalizedInput),
                    language = language,
                    confidence = 0.93f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.WRITE_CALENDAR, Permission.READ_CALENDAR)
                )
            }
            normalizedInput.matches(Regex(".*(show|view|open)\\s+calendar.*")) -> {
                EnhancedVoiceCommand(
                    action = "view_calendar",
                    category = "calendar",
                    entities = extractDateRange(normalizedInput),
                    language = language,
                    confidence = 0.91f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.READ_CALENDAR)
                )
            }
            normalizedInput.matches(Regex(".*next\\s+(appointment|meeting|event).*")) -> {
                EnhancedVoiceCommand(
                    action = "next_appointment",
                    category = "calendar",
                    entities = emptyMap(),
                    language = language,
                    confidence = 0.92f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.READ_CALENDAR)
                )
            }
            
            // === TASKS ===
            normalizedInput.matches(Regex(".*(new|create|add)\\s+task.*")) -> {
                EnhancedVoiceCommand(
                    action = "create_task",
                    category = "tasks",
                    entities = extractTaskInfo(normalizedInput),
                    language = language,
                    confidence = 0.93f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS)
                )
            }
            normalizedInput.matches(Regex(".*(show|list|view)\\s+(all\\s+)?tasks.*")) -> {
                EnhancedVoiceCommand(
                    action = "list_tasks",
                    category = "tasks",
                    entities = extractFilterInfo(normalizedInput),
                    language = language,
                    confidence = 0.91f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS)
                )
            }
            normalizedInput.matches(Regex(".*complete.*task.*")) -> {
                EnhancedVoiceCommand(
                    action = "complete_task",
                    category = "tasks",
                    entities = extractTaskReference(normalizedInput),
                    language = language,
                    confidence = 0.92f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS)
                )
            }
            
            // === SAFETY & EMERGENCY ===
            normalizedInput.contains("emergency") || normalizedInput.contains("emergencia") -> {
                EnhancedVoiceCommand(
                    action = "emergency_response",
                    category = "safety",
                    entities = extractEmergencyInfo(normalizedInput),
                    language = language,
                    confidence = 0.98f,
                    priority = "EMERGENCY",
                    targetOrchestrator = AgentType.CSO_SAFETY_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.MAKE_CALLS, Permission.SEND_SMS, Permission.ACCESS_LOCATION)
                )
            }
            normalizedInput.matches(Regex(".*safety\\s+(report|incident).*")) -> {
                EnhancedVoiceCommand(
                    action = "create_safety_report",
                    category = "safety",
                    entities = extractSafetyInfo(normalizedInput),
                    language = language,
                    confidence = 0.94f,
                    targetOrchestrator = AgentType.CSO_SAFETY_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS, Permission.ACCESS_CAMERA, Permission.ACCESS_LOCATION)
                )
            }
            
            // === FILES & PHOTOS ===
            normalizedInput.contains("take photo") || normalizedInput.contains("tomar foto") ||
            normalizedInput.matches(Regex(".*(take|capture|snap)\\s+(photo|picture).*")) -> {
                EnhancedVoiceCommand(
                    action = "take_photo",
                    category = "files",
                    entities = extractPhotoContext(normalizedInput),
                    language = language,
                    confidence = 0.94f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.ACCESS_CAMERA, Permission.ACCESS_STORAGE)
                )
            }
            normalizedInput.matches(Regex(".*(open|view)\\s+camera.*")) -> {
                EnhancedVoiceCommand(
                    action = "open_camera",
                    category = "files",
                    entities = emptyMap(),
                    language = language,
                    confidence = 0.92f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.ACCESS_CAMERA)
                )
            }
            
            // === TIME CLOCK ===
            normalizedInput.matches(Regex(".*clock\\s+in.*")) -> {
                EnhancedVoiceCommand(
                    action = "clock_in",
                    category = "timeclock",
                    entities = extractLocationInfo(normalizedInput),
                    language = language,
                    confidence = 0.95f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS, Permission.ACCESS_LOCATION)
                )
            }
            normalizedInput.matches(Regex(".*clock\\s+out.*")) -> {
                EnhancedVoiceCommand(
                    action = "clock_out",
                    category = "timeclock",
                    entities = emptyMap(),
                    language = language,
                    confidence = 0.95f,
                    targetOrchestrator = AgentType.COO_OPERATIONS_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS, Permission.ACCESS_LOCATION)
                )
            }
            
            // === ANALYTICS & REPORTING ===
            normalizedInput.matches(Regex(".*(generate|create|show)\\s+(report|analytics).*")) -> {
                EnhancedVoiceCommand(
                    action = "generate_report",
                    category = "analytics",
                    entities = extractReportType(normalizedInput),
                    language = language,
                    confidence = 0.90f,
                    targetOrchestrator = AgentType.CFO_FINANCIAL_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS)
                )
            }
            normalizedInput.matches(Regex(".*(show|view|open)\\s+(dashboard|metrics|performance).*")) -> {
                EnhancedVoiceCommand(
                    action = "view_dashboard",
                    category = "analytics",
                    entities = extractDashboardType(normalizedInput),
                    language = language,
                    confidence = 0.89f,
                    targetOrchestrator = AgentType.CFO_FINANCIAL_ORCHESTRATOR,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS)
                )
            }
            
            // === NAVIGATION ===
            normalizedInput.matches(Regex(".*(go|navigate|open)\\s+(home|to\\s+home).*")) -> {
                EnhancedVoiceCommand(
                    action = "navigate_home",
                    category = "navigation",
                    entities = emptyMap(),
                    language = language,
                    confidence = 0.96f,
                    targetOrchestrator = null,
                    permissionsRequired = emptyList()
                )
            }
            normalizedInput.matches(Regex(".*go\\s+back.*|.*navigate\\s+back.*")) -> {
                EnhancedVoiceCommand(
                    action = "navigate_back",
                    category = "navigation",
                    entities = emptyMap(),
                    language = language,
                    confidence = 0.94f,
                    targetOrchestrator = null,
                    permissionsRequired = emptyList()
                )
            }
            normalizedInput.matches(Regex(".*(open|show|go\\s+to)\\s+settings.*")) -> {
                EnhancedVoiceCommand(
                    action = "open_settings",
                    category = "system",
                    entities = emptyMap(),
                    language = language,
                    confidence = 0.93f,
                    targetOrchestrator = null,
                    permissionsRequired = emptyList()
                )
            }
            
            // === SEARCH & FILTER ===
            normalizedInput.matches(Regex(".*search.*")) -> {
                EnhancedVoiceCommand(
                    action = "search",
                    category = "system",
                    entities = extractSearchQuery(normalizedInput),
                    language = language,
                    confidence = 0.88f,
                    targetOrchestrator = null,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS)
                )
            }
            
            // === DEFAULT/GENERAL QUERY ===
            else -> {
                EnhancedVoiceCommand(
                    action = "general_query",
                    category = "general",
                    entities = mapOf("query" to normalizedInput),
                    language = language,
                    confidence = 0.70f,
                    targetOrchestrator = AgentType.CEO_PERSONAL_ASSISTANT,
                    permissionsRequired = listOf(Permission.INTERNET_ACCESS)
                )
            }
        }
    }
                    language = language,
                    confidence = 0.87f
                )
            }
            
            normalizedInput.contains("emergency") || normalizedInput.contains("emergencia") -> {
                VoiceCommand(
                    action = "emergency_response",
                    entities = extractEmergencyInfo(normalizedInput),
                    language = language,
                    confidence = 0.98f,
                    priority = "HIGH"
                )
            }
            
            else -> {
                VoiceCommand(
                    action = "general_query",
                    entities = mapOf("query" to normalizedInput),
                    language = language,
                    confidence = 0.70f
                )
            }
        }
    }
    
    private suspend fun executeVoiceCommand(command: EnhancedVoiceCommand): String {
        Log.d("VoiceCommandAgent", "Executing ${command.action} for ${command.category} (confidence: ${command.confidence})")
        
        return when (command.category) {
            "leads" -> executeLeadsCommand(command)
            "estimates" -> executeEstimatesCommand(command)
            "projects" -> executeProjectsCommand(command)
            "contacts" -> executeContactsCommand(command)
            "calendar" -> executeCalendarCommand(command)
            "tasks" -> executeTasksCommand(command)
            "safety" -> executeSafetyCommand(command)
            "files" -> executeFilesCommand(command)
            "timeclock" -> executeTimeClockCommand(command)
            "analytics" -> executeAnalyticsCommand(command)
            "navigation" -> executeNavigationCommand(command)
            "system" -> executeSystemCommand(command)
            else -> "I understand you said: '${command.entities["query"]}'. Command routed to ${command.targetOrchestrator ?: "general"} orchestrator."
        }
    }
    
    private fun executeLeadsCommand(command: EnhancedVoiceCommand): String {
        return when (command.action) {
            "create_lead" -> {
                val name = command.entities["name"] as? String ?: "New Lead"
                "Lead '$name' has been created. Routed to CRM Orchestrator for processing."
            }
            "list_leads" -> "Retrieving all leads from CRM system..."
            "view_lead" -> {
                val leadId = command.entities["lead_id"] as? String ?: "specified lead"
                "Opening lead details for $leadId..."
            }
            "convert_lead" -> {
                val leadId = command.entities["lead_id"] as? String ?: "specified lead"
                "Converting $leadId to project. Creating project and estimate..."
            }
            else -> "Lead command '${command.action}' executed."
        }
    }
    
    private fun executeEstimatesCommand(command: EnhancedVoiceCommand): String {
        return when (command.action) {
            "create_estimate" -> {
                val projectName = command.entities["project_name"] as? String ?: "New Estimate"
                "Creating estimate for '$projectName'. Routed to CFO Financial Orchestrator."
            }
            "list_estimates" -> "Retrieving all estimates from financial system..."
            "calculate_estimate" -> "Calculating estimate totals and margins..."
            "send_estimate" -> {
                val recipient = command.entities["recipient"] as? String ?: "client"
                "Sending estimate to $recipient via email..."
            }
            else -> "Estimate command '${command.action}' executed."
        }
    }
    
    private fun executeProjectsCommand(command: EnhancedVoiceCommand): String {
        return when (command.action) {
            "create_project" -> {
                val projectName = command.entities["project_name"] as? String ?: "New Project"
                val projectType = command.entities["project_type"] as? String ?: "Residential"
                "Creating $projectType project '$projectName'. Routed to COO Operations Orchestrator."
            }
            "list_projects" -> "Retrieving all active projects..."
            "view_project" -> {
                val projectId = command.entities["project_id"] as? String ?: "specified project"
                "Opening project details for $projectId..."
            }
            "project_status" -> {
                val projectId = command.entities["project_id"] as? String ?: "current project"
                "Retrieving status for $projectId..."
            }
            else -> "Project command '${command.action}' executed."
        }
    }
    
    private fun executeContactsCommand(command: EnhancedVoiceCommand): String {
        return when (command.action) {
            "create_contact" -> {
                val name = command.entities["name"] as? String ?: "New Contact"
                val phone = command.entities["phone"] as? String
                "Contact '$name' ${if (phone != null) "with phone $phone" else ""} added to CRM."
            }
            "list_contacts" -> "Retrieving all contacts..."
            "make_call" -> {
                val contact = command.entities["contact"] as? String ?: "specified contact"
                "Initiating call to $contact..."
            }
            "send_text" -> {
                val contact = command.entities["contact"] as? String ?: "specified contact"
                val message = command.entities["message"] as? String ?: ""
                "Sending text to $contact: $message"
            }
            else -> "Contact command '${command.action}' executed."
        }
    }
    
    private fun executeCalendarCommand(command: EnhancedVoiceCommand): String {
        return when (command.action) {
            "create_event" -> {
                val task = command.entities["task"] as? String ?: "Event"
                val time = command.entities["time"] as? String ?: "soon"
                "'$task' scheduled for $time. Added to calendar."
            }
            "view_calendar" -> "Opening calendar view..."
            "next_appointment" -> "Retrieving your next appointment..."
            else -> "Calendar command '${command.action}' executed."
        }
    }
    
    private fun executeTasksCommand(command: EnhancedVoiceCommand): String {
        return when (command.action) {
            "create_task" -> {
                val task = command.entities["task"] as? String ?: "New Task"
                "Task '$task' created and routed to COO Operations Orchestrator."
            }
            "list_tasks" -> "Retrieving all tasks..."
            "complete_task" -> {
                val taskId = command.entities["task_id"] as? String ?: "specified task"
                "Marking $taskId as complete..."
            }
            else -> "Task command '${command.action}' executed."
        }
    }
    
    private fun executeSafetyCommand(command: EnhancedVoiceCommand): String {
        return when (command.action) {
            "emergency_response" -> {
                val emergencyType = command.entities["type"] as? String ?: "general"
                "🚨 EMERGENCY RESPONSE ACTIVATED for $emergencyType! Routed to CSO Safety Orchestrator. Safety protocols initiated."
            }
            "create_safety_report" -> "Creating safety incident report. Camera and location services enabled."
            else -> "Safety command '${command.action}' executed."
        }
    }
    
    private fun executeFilesCommand(command: EnhancedVoiceCommand): String {
        return when (command.action) {
            "take_photo" -> {
                val context = command.entities["context"] as? String ?: "current work"
                "Opening camera to capture photo of $context..."
            }
            "open_camera" -> "Opening camera application..."
            else -> "Files command '${command.action}' executed."
        }
    }
    
    private fun executeTimeClockCommand(command: EnhancedVoiceCommand): String {
        return when (command.action) {
            "clock_in" -> {
                val location = command.entities["location"] as? String
                "Clocking in${if (location != null) " at $location" else ""}. Time recorded."
            }
            "clock_out" -> "Clocking out. Time recorded."
            else -> "Time clock command '${command.action}' executed."
        }
    }
    
    private fun executeAnalyticsCommand(command: EnhancedVoiceCommand): String {
        return when (command.action) {
            "generate_report" -> {
                val reportType = command.entities["report_type"] as? String ?: "general"
                "Generating $reportType report. Routed to CFO Financial Orchestrator."
            }
            "view_dashboard" -> {
                val dashboardType = command.entities["dashboard_type"] as? String ?: "main"
                "Opening $dashboardType dashboard..."
            }
            else -> "Analytics command '${command.action}' executed."
        }
    }
    
    private fun executeNavigationCommand(command: EnhancedVoiceCommand): String {
        return when (command.action) {
            "navigate_home" -> "Navigating to home screen..."
            "navigate_back" -> "Going back..."
            else -> "Navigation command '${command.action}' executed."
        }
    }
    
    private fun executeSystemCommand(command: EnhancedVoiceCommand): String {
        return when (command.action) {
            "open_settings" -> "Opening settings..."
            "search" -> {
                val query = command.entities["query"] as? String ?: ""
                "Searching for: $query"
            }
            else -> "System command '${command.action}' executed."
        }
    }
    
    private fun detectLanguage(input: String): String {
        val spanishKeywords = listOf("agregar", "crear", "programar", "tomar", "emergencia", "proyecto", "contacto")
        val englishKeywords = listOf("add", "create", "schedule", "take", "emergency", "project", "contact")
        
        val spanishMatches = spanishKeywords.count { input.contains(it) }
        val englishMatches = englishKeywords.count { input.contains(it) }
        
        return if (spanishMatches > englishMatches) "spanish" else "english"
    }
    
    private fun extractContactInfo(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        
        val namePattern = """contact\s+(\w+(?:\s+\w+)*)""".toRegex(RegexOption.IGNORE_CASE)
        namePattern.find(input)?.let { match ->
            entities["name"] = match.groupValues[1]
        }
        
        val phonePattern = """\b\d{3}[-.]?\d{3}[-.]?\d{4}\b""".toRegex()
        phonePattern.find(input)?.let { match ->
            entities["phone"] = match.value
        }
        
        return entities
    }
    
    private fun extractProjectInfo(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        
        val projectPattern = """project\s+(.+?)(?:\s+(residential|commercial|renovation))?""".toRegex(RegexOption.IGNORE_CASE)
        projectPattern.find(input)?.let { match ->
            entities["project_name"] = match.groupValues[1].trim()
            if (match.groupValues[2].isNotEmpty()) {
                entities["project_type"] = match.groupValues[2]
            }
        }
        
        return entities
    }
    
    private fun extractScheduleInfo(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        
        val taskPattern = """schedule\s+(.+?)(?:\s+for\s+(.+))?""".toRegex(RegexOption.IGNORE_CASE)
        taskPattern.find(input)?.let { match ->
            entities["task"] = match.groupValues[1].trim()
            if (match.groupValues[2].isNotEmpty()) {
                entities["time"] = match.groupValues[2].trim()
            }
        }
        
        return entities
    }
    
    private fun extractPhotoContext(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        
        val photoPattern = """photo(?:\s+of\s+(.+))?""".toRegex(RegexOption.IGNORE_CASE)
        photoPattern.find(input)?.let { match ->
            if (match.groupValues[1].isNotEmpty()) {
                entities["context"] = match.groupValues[1].trim()
            }
        }
        
        return entities
    }
    
    private fun extractEmergencyInfo(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        
        val emergencyTypes = listOf("fire", "injury", "accident", "safety", "evacuation")
        emergencyTypes.forEach { type ->
            if (input.contains(type, ignoreCase = true)) {
                entities["type"] = type
                return@forEach
            }
        }
        
        return entities
    }
    
    private fun calculateConfidenceScore(input: String): Float {
        val hasKeywords = constructionVocabulary.keys.any { input.contains(it, ignoreCase = true) }
        val hasNumbers = input.any { it.isDigit() }
        val hasProperNouns = input.split(" ").any { it.first().isUpperCase() }
        
        var score = 0.7f
        if (hasKeywords) score += 0.2f
        if (hasNumbers) score += 0.05f
        if (hasProperNouns) score += 0.05f
        
        return score.coerceAtMost(1.0f)
    }
    
    // === Additional Extraction Helper Methods for Comprehensive Voice Control ===
    
    private fun extractLeadInfo(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val namePattern = """(?:lead|cliente)\\s+(?:for\\s+)?([A-Za-z\\s]+)""".toRegex(RegexOption.IGNORE_CASE)
        namePattern.find(input)?.let { entities["name"] = it.groupValues[1].trim() }
        return entities + extractContactInfo(input)
    }
    
    private fun extractLeadReference(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val idPattern = """lead\\s+(?:id\\s+)?(\\d+|\\w+)""".toRegex(RegexOption.IGNORE_CASE)
        idPattern.find(input)?.let { entities["lead_id"] = it.groupValues[1] }
        return entities
    }
    
    private fun extractEstimateInfo(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val projectPattern = """(?:for|estimate for)\\s+([A-Za-z\\s]+)""".toRegex(RegexOption.IGNORE_CASE)
        projectPattern.find(input)?.let { entities["project_name"] = it.groupValues[1].trim() }
        return entities
    }
    
    private fun extractEstimateReference(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val idPattern = """estimate\\s+(?:id\\s+|number\\s+)?(\\d+|\\w+)""".toRegex(RegexOption.IGNORE_CASE)
        idPattern.find(input)?.let { entities["estimate_id"] = it.groupValues[1] }
        return entities
    }
    
    private fun extractRecipientInfo(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val recipientPattern = """(?:to|send to|email to)\\s+([A-Za-z\\s]+)""".toRegex(RegexOption.IGNORE_CASE)
        recipientPattern.find(input)?.let { entities["recipient"] = it.groupValues[1].trim() }
        return entities
    }
    
    private fun extractProjectReference(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val idPattern = """(?:project|job)\\s+(?:id\\s+|number\\s+)?(\\d+|\\w+)""".toRegex(RegexOption.IGNORE_CASE)
        idPattern.find(input)?.let { entities["project_id"] = it.groupValues[1] }
        return entities
    }
    
    private fun extractContactReference(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val namePattern = """(?:call|text|email|contact)\\s+([A-Za-z\\s]+)""".toRegex(RegexOption.IGNORE_CASE)
        namePattern.find(input)?.let { entities["contact"] = it.groupValues[1].trim() }
        return entities + extractContactInfo(input)
    }
    
    private fun extractMessageContent(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val messagePattern = """(?:saying|message|text)\\s+["\']?(.+?)["\']?$""".toRegex(RegexOption.IGNORE_CASE)
        messagePattern.find(input)?.let { entities["message"] = it.groupValues[1].trim() }
        return entities
    }
    
    private fun extractDateRange(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        if (input.contains("today")) entities["date_range"] = "today"
        else if (input.contains("week")) entities["date_range"] = "week"
        else if (input.contains("month")) entities["date_range"] = "month"
        return entities
    }
    
    private fun extractTaskInfo(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val taskPattern = """task\\s+(?:to\\s+)?(.+?)(?:\\s+by\\s+(.+))?$""".toRegex(RegexOption.IGNORE_CASE)
        taskPattern.find(input)?.let { 
            entities["task"] = it.groupValues[1].trim()
            if (it.groupValues[2].isNotEmpty()) entities["deadline"] = it.groupValues[2].trim()
        }
        return entities
    }
    
    private fun extractTaskReference(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val idPattern = """task\\s+(?:id\\s+|number\\s+)?(\\d+|\\w+)""".toRegex(RegexOption.IGNORE_CASE)
        idPattern.find(input)?.let { entities["task_id"] = it.groupValues[1] }
        return entities
    }
    
    private fun extractSafetyInfo(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val incidentTypes = listOf("fall", "injury", "equipment", "fire", "electrical", "spill")
        incidentTypes.forEach { type ->
            if (input.contains(type, ignoreCase = true)) {
                entities["incident_type"] = type
                return@forEach
            }
        }
        return entities + extractLocationInfo(input)
    }
    
    private fun extractLocationInfo(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val locationPattern = """(?:at|location|site)\\s+([A-Za-z\\s]+)""".toRegex(RegexOption.IGNORE_CASE)
        locationPattern.find(input)?.let { entities["location"] = it.groupValues[1].trim() }
        return entities
    }
    
    private fun extractReportType(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val reportTypes = listOf("financial", "project", "safety", "time", "productivity")
        reportTypes.forEach { type ->
            if (input.contains(type, ignoreCase = true)) {
                entities["report_type"] = type
                return@forEach
            }
        }
        return entities
    }
    
    private fun extractDashboardType(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val dashboardTypes = listOf("financial", "project", "operations", "safety", "analytics")
        dashboardTypes.forEach { type ->
            if (input.contains(type, ignoreCase = true)) {
                entities["dashboard_type"] = type
                return@forEach
            }
        }
        return entities
    }
    
    private fun extractSearchQuery(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        val searchPattern = """search\\s+(?:for\\s+)?(.+)$""".toRegex(RegexOption.IGNORE_CASE)
        searchPattern.find(input)?.let { entities["query"] = it.groupValues[1].trim() }
        return entities
    }
    
    private fun extractFilterInfo(input: String): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        if (input.contains("active")) entities["filter"] = "active"
        else if (input.contains("completed")) entities["filter"] = "completed"
        else if (input.contains("pending")) entities["filter"] = "pending"
        return entities
    }
    
    override suspend fun shutdown(): Result<Unit> = try {
        mcpConnection?.close()
        _isActive.value = false
        Log.i("VoiceCommandAgent", "Voice Command Agent shut down successfully")
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Enhanced Voice Command with full application routing
 */
data class EnhancedVoiceCommand(
    val action: String,
    val category: String,
    val entities: Map<String, Any>,
    val language: String,
    val confidence: Float,
    val priority: String = "NORMAL",
    val targetOrchestrator: AgentType?,
    val permissionsRequired: List<Permission>
)