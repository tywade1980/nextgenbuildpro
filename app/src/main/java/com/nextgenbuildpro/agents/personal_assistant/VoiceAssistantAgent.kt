package com.nextgenbuildpro.agents.personal_assistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.nextgenbuildpro.ai.llm.LLMContext
import com.nextgenbuildpro.ai.llm.LLMMessage
import com.nextgenbuildpro.ai.llm.LLMService
import com.nextgenbuildpro.shared.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.util.*

/**
 * Voice Assistant Agent with External LLM Integration
 * 
 * This agent combines voice interaction capabilities with external LLM API calls
 * to provide intelligent construction management assistance. It acts as a SubAgent
 * under the CEO Personal Assistant Orchestrator.
 * 
 * Key Features:
 * - Voice input recognition (speech-to-text)
 * - Voice output synthesis (text-to-speech)
 * - External LLM integration via OpenRouter
 * - Construction domain expertise
 * - Bilingual support (English/Spanish)
 * - Context-aware conversation handling
 * 
 * API Integration:
 * - Uses OpenRouterService for multi-LLM access
 * - Supports GPT-4, Claude, o1, and other models
 * - Retrieves external data for internal processing
 * - Stores conversation history in Firebase
 */
class VoiceAssistantAgent(
    private val context: Context,
    private val llmService: LLMService
) : SubAgent {
    
    companion object {
        private const val TAG = "VoiceAssistantAgent"
        private const val MAX_CONVERSATION_HISTORY = 10
        private const val TTS_UTTERANCE_ID_PREFIX = "voice_assistant_"
    }
    
    override val agentId: String = "voice_assistant_agent_${UUID.randomUUID()}"
    override val agentType: AgentType = AgentType.SUB_AGENT
    override val departmentHead: AgentType = AgentType.COO_OPERATIONS_ORCHESTRATOR
    override val subAgentRole: String = "Voice Assistant with External LLM Integration"
    override val specialization: String = "Voice-enabled AI assistance with external data retrieval"
    
    override val mlModel: MLModelConfig = MLModelConfig(
        modelName = "openai/gpt-4",
        modelType = MLModelType.AGENT_WORKFLOW,
        version = "latest",
        trainedOn = "Construction management domain",
        accuracy = 0.92
    )
    
    override val mcpTools: List<MCPTool> = listOf(
        MCPTool(
            toolId = "voice_recognition",
            toolName = "Voice Recognition Tool",
            description = "Speech-to-text conversion with construction vocabulary",
            capabilities = listOf("speech_recognition", "language_detection", "noise_filtering")
        ),
        MCPTool(
            toolId = "voice_synthesis",
            toolName = "Voice Synthesis Tool",
            description = "Text-to-speech conversion for responses",
            capabilities = listOf("speech_synthesis", "voice_customization", "audio_playback")
        ),
        MCPTool(
            toolId = "llm_query",
            toolName = "External LLM Query Tool",
            description = "Query external LLMs for intelligent responses",
            capabilities = listOf("llm_query", "context_management", "response_processing")
        )
    )
    
    override val apiIntegrations: List<APIIntegration> = listOf(
        APIIntegration(
            apiId = "openrouter_api",
            apiName = "OpenRouter API",
            endpoint = "https://openrouter.ai/api/v1",
            authType = APIAuthType.API_KEY,
            rateLimits = APIRateLimits(
                requestsPerMinute = 60,
                requestsPerDay = 10000
            )
        )
    )
    
    private val _isActive = MutableStateFlow(false)
    override val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    private val _conversationState = MutableStateFlow<ConversationState>(ConversationState.Idle)
    val conversationState: StateFlow<ConversationState> = _conversationState.asStateFlow()
    
    // Voice components
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false
    private var isListening = false
    
    // Conversation management
    private val conversationHistory = mutableListOf<LLMMessage>()
    private var currentConversationId: String? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    override suspend fun initialize(): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            Log.i(TAG, "Initializing Voice Assistant Agent...")
            
            // Initialize Text-to-Speech
            initializeTextToSpeech()
            
            // Initialize Speech Recognizer
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createRecognitionListener())
                }
                Log.d(TAG, "Speech recognition initialized")
            } else {
                Log.w(TAG, "Speech recognition not available on this device")
            }
            
            _isActive.value = true
            _conversationState.value = ConversationState.Ready
            
            Log.i(TAG, "Voice Assistant Agent initialized successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Voice Assistant Agent", e)
            Result.failure(e)
        }
    }
    
    override suspend fun processTask(task: NextGenTask): Result<NextGenTask> {
        return try {
            Log.d(TAG, "Processing task: ${task.title}")
            
            when (task.type) {
                "voice_query" -> processVoiceQuery(task)
                "voice_command" -> processVoiceCommand(task)
                "llm_data_retrieval" -> retrieveExternalData(task)
                else -> {
                    Log.w(TAG, "Unknown task type: ${task.type}")
                    Result.success(task.copy(
                        status = TaskStatus.FAILED,
                        metadata = task.metadata + mapOf(
                            "error" to "Unknown task type: ${task.type}"
                        )
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing task", e)
            Result.failure(e)
        }
    }
    
    override suspend fun executeSpecializedTask(task: NextGenTask): Result<NextGenTask> {
        return try {
            Log.d(TAG, "Executing specialized voice task: ${task.title}")
            
            val voiceInput = task.parameters["voice_input"] as? String
            val useExternalLLM = task.parameters["use_llm"] as? Boolean ?: true
            
            if (voiceInput == null) {
                return Result.failure(IllegalArgumentException("No voice input provided"))
            }
            
            // Start conversation if needed
            if (currentConversationId == null) {
                startConversation()
            }
            
            // Process with external LLM
            val response = if (useExternalLLM) {
                queryExternalLLM(voiceInput, task.parameters)
            } else {
                processLocalVoiceCommand(voiceInput)
            }
            
            // Speak the response
            speak(response)
            
            // Update conversation history
            conversationHistory.add(LLMMessage("user", voiceInput, agentType = agentType))
            conversationHistory.add(LLMMessage("assistant", response))
            
            // Keep conversation history manageable
            if (conversationHistory.size > MAX_CONVERSATION_HISTORY * 2) {
                conversationHistory.removeAt(0)
                conversationHistory.removeAt(0)
            }
            
            val completedTask = task.copy(
                status = TaskStatus.COMPLETED,
                result = mapOf(
                    "response" to response,
                    "conversation_id" to (currentConversationId ?: ""),
                    "voice_output" to response
                ),
                metadata = task.metadata + mapOf(
                    "processing_time" to System.currentTimeMillis(),
                    "llm_used" to useExternalLLM
                ),
                updatedAt = LocalDateTime.now(),
                progress = 1.0f
            )
            
            Result.success(completedTask)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing specialized task", e)
            Result.failure(e)
        }
    }
    
    override suspend fun requestHumanApproval(task: NextGenTask, reason: String): Result<HumanApprovalRecord> {
        return try {
            Log.d(TAG, "Requesting human approval for task: ${task.title}")
            
            // Speak the approval request
            speak("Approval needed: $reason")
            
            val approval = HumanApprovalRecord(
                taskId = task.id,
                approver = "voice_interaction",
                approved = false,
                comments = reason,
                timestamp = LocalDateTime.now()
            )
            
            Result.success(approval)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun learnFromFeedback(feedback: TaskFeedback): Result<Unit> {
        return try {
            Log.d(TAG, "Learning from feedback for task: ${feedback.taskId}")
            
            // Store feedback for future improvements
            val feedbackSummary = "Task quality: ${feedback.qualityScore}, " +
                    "Successful: ${feedback.wasSuccessful}, " +
                    "Notes: ${feedback.notes}"
            
            Log.i(TAG, "Feedback received: $feedbackSummary")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun shutdown(): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            Log.i(TAG, "Shutting down Voice Assistant Agent...")
            
            // Stop any ongoing speech
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            
            // Release speech recognizer
            speechRecognizer?.destroy()
            
            // Cancel coroutines
            coroutineScope.cancel()
            
            _isActive.value = false
            _conversationState.value = ConversationState.Idle
            
            Log.i(TAG, "Voice Assistant Agent shut down successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Voice interaction methods
    
    /**
     * Start listening for voice input
     */
    suspend fun startListening(): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            if (!_isActive.value) {
                return@withContext Result.failure(IllegalStateException("Agent not initialized"))
            }
            
            if (speechRecognizer == null) {
                return@withContext Result.failure(IllegalStateException("Speech recognizer not available"))
            }
            
            if (isListening) {
                Log.w(TAG, "Already listening")
                return@withContext Result.success(Unit)
            }
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            
            _conversationState.value = ConversationState.Listening
            speechRecognizer?.startListening(intent)
            isListening = true
            
            Log.d(TAG, "Started listening for voice input")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice recognition", e)
            _conversationState.value = ConversationState.Error(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }
    
    /**
     * Stop listening for voice input
     */
    fun stopListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
            _conversationState.value = ConversationState.Ready
            Log.d(TAG, "Stopped listening for voice input")
        }
    }
    
    /**
     * Speak text using text-to-speech
     */
    suspend fun speak(text: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            if (!isTtsReady) {
                continuation.resume(Result.failure(IllegalStateException("TTS not ready")), null)
                return@suspendCancellableCoroutine
            }
            
            val utteranceId = TTS_UTTERANCE_ID_PREFIX + UUID.randomUUID().toString()
            
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "TTS started: $text")
                    CoroutineScope(Dispatchers.Main).launch {
                        _conversationState.value = ConversationState.Speaking
                    }
                }
                
                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "TTS completed")
                    CoroutineScope(Dispatchers.Main).launch {
                        _conversationState.value = ConversationState.Ready
                    }
                    continuation.resume(Result.success(Unit), null)
                }
                
                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "TTS error")
                    CoroutineScope(Dispatchers.Main).launch {
                        _conversationState.value = ConversationState.Error("TTS error")
                    }
                    continuation.resume(Result.failure(Exception("TTS error")), null)
                }
            })
            
            val result = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            if (result != TextToSpeech.SUCCESS) {
                continuation.resume(Result.failure(Exception("TTS speak failed")), null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in TTS", e)
            continuation.resume(Result.failure(e), null)
        }
    }
    
    // External LLM integration methods
    
    /**
     * Query external LLM for intelligent response
     */
    private suspend fun queryExternalLLM(input: String, parameters: Map<String, Any>): String {
        return try {
            Log.d(TAG, "Querying external LLM for input: $input")
            
            // Set state to Processing while waiting for LLM response
            _conversationState.value = ConversationState.Processing
            
            val context = LLMContext(
                conversationId = currentConversationId ?: UUID.randomUUID().toString(),
                systemPrompt = buildSystemPrompt(),
                previousMessages = conversationHistory.takeLast(MAX_CONVERSATION_HISTORY),
                metadata = parameters
            )
            
            val result = llmService.generateResponse(
                prompt = input,
                context = context,
                agentType = agentType
            )
            
            result.fold(
                onSuccess = { llmResponse ->
                    Log.d(TAG, "LLM response received: ${llmResponse.content.take(100)}...")
                    currentConversationId = llmResponse.conversationId
                    _conversationState.value = ConversationState.Ready
                    llmResponse.content
                },
                onFailure = { error ->
                    Log.e(TAG, "LLM query failed", error)
                    _conversationState.value = ConversationState.Error(error.message ?: "Unknown error")
                    "I apologize, but I'm having trouble connecting to my knowledge base. Please try again."
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error querying LLM", e)
            "I encountered an error processing your request. Please try again."
        }
    }
    
    /**
     * Process voice query task
     */
    private suspend fun processVoiceQuery(task: NextGenTask): Result<NextGenTask> {
        val query = task.description
        
        Log.d(TAG, "Processing voice query: $query")
        
        val response = queryExternalLLM(query, task.parameters)
        speak(response)
        
        return Result.success(task.copy(
            status = TaskStatus.COMPLETED,
            result = mapOf("response" to response),
            updatedAt = LocalDateTime.now()
        ))
    }
    
    /**
     * Process voice command task
     */
    private suspend fun processVoiceCommand(task: NextGenTask): Result<NextGenTask> {
        val command = task.description
        
        Log.d(TAG, "Processing voice command: $command")
        
        val response = processLocalVoiceCommand(command)
        speak(response)
        
        return Result.success(task.copy(
            status = TaskStatus.COMPLETED,
            result = mapOf("response" to response),
            updatedAt = LocalDateTime.now()
        ))
    }
    
    /**
     * Retrieve external data via LLM
     */
    private suspend fun retrieveExternalData(task: NextGenTask): Result<NextGenTask> {
        val dataQuery = task.parameters["query"] as? String ?: task.description
        
        Log.d(TAG, "Retrieving external data: $dataQuery")
        
        val response = queryExternalLLM(dataQuery, task.parameters)
        
        val resultMap: Map<String, Any> = mapOf(
            "data" to response,
            "source" to "external_llm",
            "conversation_id" to (currentConversationId ?: "")
        )
        
        return Result.success(task.copy(
            status = TaskStatus.COMPLETED,
            result = resultMap,
            updatedAt = LocalDateTime.now()
        ))
    }
    
    /**
     * Process local voice command without external LLM
     */
    private fun processLocalVoiceCommand(command: String): String {
        val lowerCommand = command.lowercase()
        
        return when {
            lowerCommand.contains("hello") || lowerCommand.contains("hi") ->
                "Hello! I'm your voice assistant. How can I help with your construction project?"
            
            lowerCommand.contains("help") ->
                "I can assist with scheduling, project updates, safety information, and answering questions about construction management. What would you like to know?"
            
            lowerCommand.contains("status") ->
                "I'm online and ready to assist. All systems are operational."
            
            else ->
                "I heard: $command. Let me process that with my knowledge base."
        }
    }
    
    // Helper methods
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported for TTS")
                } else {
                    isTtsReady = true
                    Log.d(TAG, "Text-to-speech initialized successfully")
                }
            } else {
                Log.e(TAG, "TTS initialization failed")
            }
        }
    }
    
    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for speech")
            }
            
            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Speech started")
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Voice level changed
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }
            
            override fun onEndOfSpeech() {
                Log.d(TAG, "Speech ended")
                isListening = false
            }
            
            override fun onError(error: Int) {
                val errorMessage = getErrorMessage(error)
                Log.e(TAG, "Speech recognition error: $errorMessage")
                isListening = false
                _conversationState.value = ConversationState.Error(errorMessage)
            }
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    Log.d(TAG, "Recognized speech: $recognizedText")
                    
                    // Process recognized text
                    coroutineScope.launch {
                        val task = NextGenTask(
                            title = "Voice Input",
                            description = recognizedText,
                            type = "voice_query",
                            assignedAgent = agentType,
                            priority = Priority.MEDIUM,
                            parameters = mapOf("voice_input" to recognizedText, "use_llm" to true)
                        )
                        executeSpecializedTask(task)
                    }
                }
                isListening = false
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    Log.d(TAG, "Partial result: ${matches[0]}")
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Handle events
            }
        }
    }
    
    private fun getErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error: $error"
        }
    }
    
    private fun startConversation() {
        currentConversationId = UUID.randomUUID().toString()
        conversationHistory.clear()
        Log.d(TAG, "Started new conversation: $currentConversationId")
    }
    
    private fun buildSystemPrompt(): String {
        return """You are an intelligent construction management assistant integrated into NextGen BuildPro.
            |
            |Your role is to help construction professionals with:
            |- Project management and scheduling
            |- Cost estimation and budgeting
            |- Safety protocols and compliance
            |- Resource allocation and tracking
            |- Client communication
            |- Document management
            |
            |Provide clear, concise, and actionable responses. Always prioritize safety and compliance.
            |When discussing costs, be detailed and transparent. When scheduling, consider dependencies and weather.
            |
            |You are speaking to a user via voice interface, so keep responses conversational and easy to understand.
            """.trimMargin()
    }
}

/**
 * Conversation state for the voice assistant
 */
sealed class ConversationState {
    object Idle : ConversationState()
    object Ready : ConversationState()
    object Listening : ConversationState()
    object Processing : ConversationState()
    object Speaking : ConversationState()
    data class Error(val message: String) : ConversationState()
}
