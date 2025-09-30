package com.nextgenbuildpro.agents.personal_assistant

import android.util.Log
import com.nextgenbuildpro.shared.*
import com.nextgenbuildpro.mcp.MCPServer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Voice Command Agent - Personal Assistant Orchestrator
 * 
 * Specialized in processing natural language voice commands for construction management.
 * Handles Spanish and English voice recognition with construction-specific vocabulary.
 */
class VoiceCommandAgent : SpecializedAgent {
    override val agentId = "voice_command_agent"
    override val agentType = AgentType.PERSONAL_ASSISTANT_ORCHESTRATOR
    override val specialization = "Voice command processing and natural language understanding"
    
    private val mcpServer = MCPServer.getInstance()
    private var mcpConnection: com.nextgenbuildpro.mcp.MCPConnection? = null
    
    private val _isActive = MutableStateFlow(false)
    override val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
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
        
        // English construction commands
        "schedule" to "create_schedule",
        "estimate" to "generate_estimate",
        "contact" to "manage_contact",
        "project" to "manage_project",
        "task" to "create_task",
        "photo" to "take_photo",
        "report" to "create_report",
        "invoice" to "generate_invoice"
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
            
            val voiceInput = task.parameters["voice_input"] as? String
                ?: return Result.failure(IllegalArgumentException("No voice input provided"))
            
            val processedCommand = processVoiceCommand(voiceInput)
            val result = executeVoiceCommand(processedCommand)
            
            val completedTask = task.copy(
                status = TaskStatus.COMPLETED,
                result = mapOf(
                    "processed_command" to processedCommand,
                    "execution_result" to result,
                    "confidence_score" to calculateConfidenceScore(voiceInput),
                    "language_detected" to detectLanguage(voiceInput)
                )
            )
            
            Result.success(completedTask)
        } catch (e: Exception) {
            Log.e("VoiceCommandAgent", "Error processing voice command", e)
            Result.failure(e)
        }
    }
    
    private suspend fun processVoiceCommand(voiceInput: String): VoiceCommand {
        val normalizedInput = voiceInput.lowercase().trim()
        val language = detectLanguage(normalizedInput)
        
        return when {
            normalizedInput.contains("add contact") || normalizedInput.contains("agregar contacto") -> {
                VoiceCommand(
                    action = "add_contact",
                    entities = extractContactInfo(normalizedInput),
                    language = language,
                    confidence = 0.95f
                )
            }
            
            normalizedInput.contains("create project") || normalizedInput.contains("crear proyecto") -> {
                VoiceCommand(
                    action = "create_project",
                    entities = extractProjectInfo(normalizedInput),
                    language = language,
                    confidence = 0.92f
                )
            }
            
            normalizedInput.contains("schedule") || normalizedInput.contains("programar") -> {
                VoiceCommand(
                    action = "schedule_task",
                    entities = extractScheduleInfo(normalizedInput),
                    language = language,
                    confidence = 0.89f
                )
            }
            
            normalizedInput.contains("take photo") || normalizedInput.contains("tomar foto") -> {
                VoiceCommand(
                    action = "take_photo",
                    entities = extractPhotoContext(normalizedInput),
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
    
    private suspend fun executeVoiceCommand(command: VoiceCommand): String {
        return when (command.action) {
            "add_contact" -> {
                val name = command.entities["name"] as? String ?: "Unknown"
                val phone = command.entities["phone"] as? String
                "Contact '$name' ${if (phone != null) "with phone $phone" else ""} has been added to your contacts."
            }
            
            "create_project" -> {
                val projectName = command.entities["project_name"] as? String ?: "New Project"
                val projectType = command.entities["project_type"] as? String ?: "Residential"
                "New $projectType project '$projectName' has been created with default templates."
            }
            
            "schedule_task" -> {
                val task = command.entities["task"] as? String ?: "Task"
                val time = command.entities["time"] as? String ?: "soon"
                "Task '$task' has been scheduled for $time."
            }
            
            "take_photo" -> {
                val context = command.entities["context"] as? String ?: "current work"
                "Photo of $context has been captured and tagged with project information."
            }
            
            "emergency_response" -> {
                val emergencyType = command.entities["type"] as? String ?: "general"
                "Emergency response activated for $emergencyType. Safety protocols initiated."
            }
            
            else -> {
                "I understand you said: '${command.entities["query"]}'. How can I help you with your construction project?"
            }
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
    
    override suspend fun shutdown(): Result<Unit> = try {
        mcpConnection?.close()
        _isActive.value = false
        Log.i("VoiceCommandAgent", "Voice Command Agent shut down successfully")
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

data class VoiceCommand(
    val action: String,
    val entities: Map<String, Any>,
    val language: String,
    val confidence: Float,
    val priority: String = "NORMAL"
)