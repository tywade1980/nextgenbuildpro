package com.nextgenbuildpro.agents.personal_assistant

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.shared.*
import com.nextgenbuildpro.orchestrators.*
import com.nextgenbuildpro.core.MainOrchestrator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.util.UUID

/**
 * Voice Control Handler
 * 
 * Central handler for ALL voice-controlled application functions.
 * Provides comprehensive voice command processing and routing to appropriate orchestrators.
 * 
 * FULL VOICE CONTROL CAPABILITIES:
 * ================================
 * 
 * LEADS MANAGEMENT:
 * - "Create new lead for [name]"
 * - "Show all leads" / "List leads"
 * - "Open lead [name/id]"
 * - "Convert lead [name] to project"
 * 
 * ESTIMATES & BIDDING:
 * - "Create estimate for [project]"
 * - "Show all estimates"
 * - "Calculate estimate [id]"
 * - "Send estimate to [client]"
 * 
 * PROJECTS & JOBS:
 * - "Create new project [name]"
 * - "Show all projects"
 * - "Open project [name/id]"
 * - "What's the project status"
 * 
 * CONTACTS & CRM:
 * - "Add contact [name] [phone]"
 * - "Call [contact name]"
 * - "Text [contact name] [message]"
 * - "Email [contact name]"
 * 
 * CALENDAR & SCHEDULING:
 * - "Schedule [task] for [time]"
 * - "Show calendar"
 * - "What's my next appointment"
 * - "Cancel event [name]"
 * 
 * TASKS & TODO:
 * - "Create task [description]"
 * - "Show all tasks"
 * - "Complete task [id]"
 * - "Assign task to [person]"
 * 
 * SAFETY & EMERGENCY:
 * - "Emergency!" / "Safety incident!"
 * - "Create safety report"
 * - "Report incident"
 * - "OSHA compliance check"
 * 
 * FILES & PHOTOS:
 * - "Take photo" / "Capture photo"
 * - "Open camera"
 * - "Show files"
 * - "Share file [name]"
 * 
 * TIME CLOCK:
 * - "Clock in" / "Clock out"
 * - "Show timesheet"
 * - "What time is it"
 * 
 * ANALYTICS & REPORTING:
 * - "Generate [type] report"
 * - "Show dashboard"
 * - "View analytics"
 * - "Show metrics"
 * 
 * NAVIGATION:
 * - "Go home" / "Navigate home"
 * - "Go back"
 * - "Open settings"
 * 
 * SYSTEM:
 * - "Search for [query]"
 * - "Filter by [criteria]"
 * - "Help"
 * 
 * BILINGUAL SUPPORT:
 * - Full Spanish support for all commands
 * - Automatic language detection
 */
class VoiceControlHandler(
    private val context: Context,
    private val mainOrchestrator: MainOrchestrator
) {
    companion object {
        private const val TAG = "VoiceControlHandler"
        
        @Volatile
        private var instance: VoiceControlHandler? = null
        
        fun getInstance(context: Context, mainOrchestrator: MainOrchestrator): VoiceControlHandler {
            return instance ?: synchronized(this) {
                instance ?: VoiceControlHandler(context, mainOrchestrator).also { instance = it }
            }
        }
    }
    
    private val voiceCommandAgent = VoiceCommandAgent()
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _lastCommand = MutableStateFlow<String?>(null)
    val lastCommand: StateFlow<String?> = _lastCommand.asStateFlow()
    
    private val _lastResponse = MutableStateFlow<String?>(null)
    val lastResponse: StateFlow<String?> = _lastResponse.asStateFlow()
    
    private val commandHistory = mutableListOf<VoiceControlRecord>()
    
    /**
     * Initialize voice control system
     */
    suspend fun initialize(): Result<Unit> = try {
        Log.i(TAG, "Initializing comprehensive voice control system...")
        
        // Initialize voice command agent
        voiceCommandAgent.initialize().getOrThrow()
        
        Log.i(TAG, "Voice control system initialized successfully")
        Log.i(TAG, "Voice control is ready for ALL application functions")
        
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize voice control system", e)
        Result.failure(e)
    }
    
    /**
     * Start listening for voice commands
     */
    suspend fun startListening(): Result<Unit> = try {
        _isListening.value = true
        Log.d(TAG, "Started listening for voice commands")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to start listening", e)
        Result.failure(e)
    }
    
    /**
     * Stop listening for voice commands
     */
    suspend fun stopListening(): Result<Unit> = try {
        _isListening.value = false
        Log.d(TAG, "Stopped listening for voice commands")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to stop listening", e)
        Result.failure(e)
    }
    
    /**
     * Process voice command with FULL application control
     */
    suspend fun processVoiceCommand(voiceInput: String): Result<String> = try {
        Log.i(TAG, "Processing voice command: $voiceInput")
        
        _lastCommand.value = voiceInput
        
        // Create task for voice command agent
        val voiceTask = NextGenTask(
            id = UUID.randomUUID().toString(),
            title = "Voice Command",
            description = voiceInput,
            type = "voice_command",
            priority = Priority.HIGH,
            status = TaskStatus.PENDING,
            assignedAgent = AgentType.PERSONAL_ASSISTANT_ORCHESTRATOR,
            metadata = mapOf("voice_input" to voiceInput),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        // Process through voice command agent
        val result = voiceCommandAgent.processTask(voiceTask)
        
        result.fold(
            onSuccess = { completedTask ->
                val response = completedTask.metadata["execution_result"] as? String
                    ?: "Command processed successfully"
                
                _lastResponse.value = response
                
                // Record in history
                recordCommandExecution(
                    voiceInput = voiceInput,
                    response = response,
                    success = true,
                    metadata = completedTask.metadata
                )
                
                // Route to MainOrchestrator for actual execution
                val orchestratorResult = routeToOrchestrator(completedTask)
                
                Log.i(TAG, "Voice command processed successfully: $response")
                Result.success(response)
            },
            onFailure = { error ->
                val errorMessage = "Failed to process voice command: ${error.message}"
                _lastResponse.value = errorMessage
                
                recordCommandExecution(
                    voiceInput = voiceInput,
                    response = errorMessage,
                    success = false,
                    metadata = mapOf("error" to error.message)
                )
                
                Log.e(TAG, errorMessage, error)
                Result.failure(error)
            }
        )
    } catch (e: Exception) {
        val errorMessage = "Voice command processing error: ${e.message}"
        _lastResponse.value = errorMessage
        Log.e(TAG, errorMessage, e)
        Result.failure(e)
    }
    
    /**
     * Route completed voice command task to MainOrchestrator for execution
     */
    private suspend fun routeToOrchestrator(task: NextGenTask): Result<Unit> {
        val targetOrchestrator = task.metadata["target_orchestrator"] as? String
        val category = task.metadata["command_category"] as? String
        
        Log.d(TAG, "Routing command to orchestrator: $targetOrchestrator (category: $category)")
        
        return try {
            // Submit task to MainOrchestrator for actual execution
            mainOrchestrator.orchestrateTask(task)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to route command to orchestrator", e)
            Result.failure(e)
        }
    }
    
    /**
     * Record voice command execution in history
     */
    private fun recordCommandExecution(
        voiceInput: String,
        response: String,
        success: Boolean,
        metadata: Map<String, Any>
    ) {
        val record = VoiceControlRecord(
            id = UUID.randomUUID().toString(),
            voiceInput = voiceInput,
            response = response,
            success = success,
            timestamp = LocalDateTime.now(),
            metadata = metadata
        )
        
        commandHistory.add(record)
        
        // Keep only last 100 commands
        if (commandHistory.size > 100) {
            commandHistory.removeFirst()
        }
    }
    
    /**
     * Get command history
     */
    fun getCommandHistory(): List<VoiceControlRecord> {
        return commandHistory.toList()
    }
    
    /**
     * Get command statistics
     */
    fun getCommandStatistics(): VoiceControlStatistics {
        val totalCommands = commandHistory.size
        val successfulCommands = commandHistory.count { it.success }
        val failedCommands = totalCommands - successfulCommands
        val successRate = if (totalCommands > 0) {
            (successfulCommands.toDouble() / totalCommands * 100)
        } else {
            0.0
        }
        
        val categoryCounts = commandHistory
            .groupBy { it.metadata["command_category"] as? String ?: "unknown" }
            .mapValues { it.value.size }
        
        return VoiceControlStatistics(
            totalCommands = totalCommands,
            successfulCommands = successfulCommands,
            failedCommands = failedCommands,
            successRate = successRate,
            commandsByCategory = categoryCounts,
            lastCommandTime = commandHistory.lastOrNull()?.timestamp
        )
    }
    
    /**
     * Clear command history
     */
    fun clearHistory() {
        commandHistory.clear()
        Log.d(TAG, "Command history cleared")
    }
    
    /**
     * Shutdown voice control system
     */
    suspend fun shutdown(): Result<Unit> = try {
        stopListening()
        voiceCommandAgent.shutdown()
        Log.i(TAG, "Voice control system shut down")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to shutdown voice control system", e)
        Result.failure(e)
    }
}

/**
 * Voice control record for history tracking
 */
data class VoiceControlRecord(
    val id: String,
    val voiceInput: String,
    val response: String,
    val success: Boolean,
    val timestamp: LocalDateTime,
    val metadata: Map<String, Any>
)

/**
 * Voice control statistics
 */
data class VoiceControlStatistics(
    val totalCommands: Int,
    val successfulCommands: Int,
    val failedCommands: Int,
    val successRate: Double,
    val commandsByCategory: Map<String, Int>,
    val lastCommandTime: LocalDateTime?
)
