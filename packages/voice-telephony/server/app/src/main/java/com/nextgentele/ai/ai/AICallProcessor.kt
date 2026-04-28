package com.nextgentele.ai.ai

import android.content.Context
import android.media.AudioManager
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.telecom.Call
import android.util.Log
import com.nextgentele.ai.integration.NodeJSBridge
import com.nextgentele.ai.integration.CallContext
import com.nextgentele.ai.integration.AgentRequirements
import kotlinx.coroutines.*
import java.util.*

class AICallProcessor(private val context: Context) : RecognitionListener, TextToSpeech.OnInitListener {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var nodeJSBridge: NodeJSBridge = NodeJSBridge(context)
    private var isProcessing = false
    private var currentCall: Call? = null
    private var currentCallId: String? = null
    private var processingJob: Job? = null
    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    companion object {
        private const val TAG = "AICallProcessor"
    }
    
    fun startProcessing() {
        if (isProcessing) return
        
        isProcessing = true
        initializeSpeechRecognizer()
        initializeTextToSpeech()
        Log.d(TAG, "AI Call Processor started")
    }
    
    fun stopProcessing() {
        isProcessing = false
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
        Log.d(TAG, "AI Call Processor stopped")
    }
    
    fun handleIncomingCall(call: Call) {
        currentCall = call
        currentCallId = "call_${System.currentTimeMillis()}"
        Log.d(TAG, "Processing incoming call: $currentCallId")
        
        // Check Node.js server availability first
        processingJob = CoroutineScope(Dispatchers.IO).launch {
            val serverAvailable = nodeJSBridge.isServerAvailable()
            
            withContext(Dispatchers.Main) {
                if (serverAvailable) {
                    // Auto-answer logic based on AI decision with Node.js integration
                    if (shouldAnswerCall(call)) {
                        answerCallWithAI(call)
                    } else {
                        // Send to voicemail or decline
                        call.reject(false, "AI determined not to answer")
                    }
                } else {
                    // Fallback to basic handling without Node.js
                    Log.w(TAG, "Node.js server unavailable, using fallback mode")
                    if (shouldAnswerCall(call)) {
                        answerCall(call)
                    } else {
                        call.reject(false, "Service unavailable")
                    }
                }
            }
        }
    }
    
    fun handleOutgoingCall(call: Call, phoneNumber: String) {
        currentCall = call
        Log.d(TAG, "Processing outgoing call to: $phoneNumber")
        
        // AI can prepare for the call, set context, etc.
        prepareForOutgoingCall(phoneNumber)
    }
    
    private fun shouldAnswerCall(call: Call): Boolean {
        // AI logic to determine if call should be answered
        // This would use machine learning models to analyze:
        // - Time of day
        // - Caller ID
        // - User preferences
        // - Calendar availability
        // - Contact importance
        
        val details = call.details
        val callerNumber = details.handle?.schemeSpecificPart
        
        // Simple example logic - in real implementation, this would use ML models
        return when {
            callerNumber == null -> false // Unknown number
            isBusinessHours() -> true
            isEmergencyContact(callerNumber) -> true
            isSpamNumber(callerNumber) -> false
            else -> getUserPreferenceForUnknownCalls()
        }
    }
    
    private fun answerCallWithAI(call: Call) {
        call.answer(0) // Answer with default video state
        
        currentCallId?.let { callId ->
            processingJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Initialize AI processing with Node.js backend
                    val callContext = createCallContext(call)
                    val aiInit = nodeJSBridge.initializeAI(callId, callContext)
                    
                    if (aiInit?.contextInitialized == true) {
                        Log.d(TAG, "AI context initialized for call $callId")
                        
                        // Start IVR session
                        val ivrSession = nodeJSBridge.startIVRSession(callId)
                        
                        withContext(Dispatchers.Main) {
                            if (ivrSession != null) {
                                // Start with IVR menu
                                speakText(aiInit.initialPrompt)
                                startDTMFListening()
                            } else {
                                // Fallback to basic AI conversation
                                startAIConversationWithNodeJS(callId)
                            }
                        }
                    } else {
                        // Fallback to local AI processing
                        withContext(Dispatchers.Main) {
                            startAIConversation()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing AI for call", e)
                    withContext(Dispatchers.Main) {
                        startAIConversation() // Fallback
                    }
                }
            }
        }
    }

    private fun createCallContext(call: Call): CallContext {
        val details = call.details
        val fromNumber = details.handle?.schemeSpecificPart ?: "unknown"
        val toNumber = "business_line" // This would be the business number
        
        return CallContext(
            fromNumber = fromNumber,
            toNumber = toNumber,
            direction = "inbound",
            contactName = getContactName(fromNumber),
            callTime = System.currentTimeMillis()
        )
    }

    private fun startAIConversationWithNodeJS(callId: String) {
        // Begin speech recognition to understand caller
        startListening()
        
        // The initial greeting is handled by Node.js AI service
        Log.d(TAG, "AI conversation started with Node.js backend for call $callId")
    }

    private fun startDTMFListening() {
        // Set up DTMF tone detection
        Log.d(TAG, "DTMF listening started for IVR navigation")
        // This would integrate with the telephony framework to detect DTMF tones
        // and forward them to the Node.js IVR service via nodeJSBridge.processDTMFInput()
    }

    private fun getContactName(phoneNumber: String): String? {
        // This would query the contacts database
        // For now, return null as placeholder
        return null
    }
    
    private fun startAIConversation() {
        // Begin speech recognition to understand caller
        startListening()
        
        // Greet the caller
        speakText(getAIGreeting())
    }
    
    private fun prepareForOutgoingCall(phoneNumber: String) {
        // AI preparation for outgoing calls
        // Could include looking up contact info, recent communications, etc.
        Log.d(TAG, "Preparing AI context for outgoing call to: $phoneNumber")
    }
    
    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(this)
        }
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context, this)
    }
    
    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }
    
    private fun speakText(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    
    private fun getAIGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning! This is NextGenTele AI assistant. How may I help you?"
            hour < 17 -> "Good afternoon! This is NextGenTele AI assistant. How may I help you?"
            else -> "Good evening! This is NextGenTele AI assistant. How may I help you?"
        }
    }
    
    private fun processSpokenText(spokenText: String) {
        Log.d(TAG, "Processing spoken text: $spokenText")
        
        currentCallId?.let { callId ->
            processingJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Send to Node.js AI service for processing
                    val aiResponse = nodeJSBridge.processAudioInput(callId, spokenText)
                    
                    withContext(Dispatchers.Main) {
                        if (aiResponse != null) {
                            if (aiResponse.shouldSpeak) {
                                speakText(aiResponse.text)
                            }
                            
                            // Handle AI actions
                            when (aiResponse.action) {
                                "transfer_to_agent" -> transferToHumanAgent(callId)
                                "end_call" -> endCall()
                                "collect_info" -> continueListening()
                                else -> continueListening()
                            }
                            
                            if (aiResponse.endConversation) {
                                endCall()
                            }
                        } else {
                            // Fallback to local AI processing
                            val localResponse = generateAIResponse(spokenText)
                            speakText(localResponse)
                            continueListening()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing speech with Node.js", e)
                    withContext(Dispatchers.Main) {
                        // Fallback to local processing
                        val response = generateAIResponse(spokenText)
                        speakText(response)
                        continueListening()
                    }
                }
            }
        }
    }

    private fun continueListening() {
        // Continue listening for more input
        if (isProcessing && currentCall?.state == Call.STATE_ACTIVE) {
            startListening()
        }
    }

    private fun transferToHumanAgent(callId: String) {
        processingJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val agentRequirements = AgentRequirements(
                    skills = listOf("customer_service"),
                    language = "en",
                    priority = "normal"
                )
                
                val availableAgent = nodeJSBridge.findAvailableAgent(callId, agentRequirements)
                
                withContext(Dispatchers.Main) {
                    if (availableAgent != null) {
                        speakText("I'm transferring you to one of our specialists. Please hold on.")
                        
                        // Transfer the call
                        CoroutineScope(Dispatchers.IO).launch {
                            val transferResult = nodeJSBridge.transferToAgent(callId, availableAgent.id)
                            withContext(Dispatchers.Main) {
                                if (transferResult?.success == true) {
                                    Log.d(TAG, "Successfully transferred call to agent ${availableAgent.name}")
                                } else {
                                    speakText("I'm sorry, all our agents are currently busy. How else can I help you?")
                                    continueListening()
                                }
                            }
                        }
                    } else {
                        speakText("I'm sorry, all our agents are currently busy. How else can I help you?")
                        continueListening()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error transferring to agent", e)
                withContext(Dispatchers.Main) {
                    speakText("I'm sorry, I'm having trouble connecting you to an agent right now. How else can I help you?")
                    continueListening()
                }
            }
        }
    }

    private fun endCall() {
        currentCall?.disconnect()
        stopProcessing()
    }
    
    private fun generateAIResponse(input: String): String {
        // This would use a real AI model for response generation
        // For now, simple pattern matching
        val lowercaseInput = input.lowercase()
        
        return when {
            lowercaseInput.contains("hello") || lowercaseInput.contains("hi") -> 
                "Hello! How can I assist you today?"
            lowercaseInput.contains("appointment") || lowercaseInput.contains("schedule") ->
                "I can help you with scheduling. Let me check the calendar for available times."
            lowercaseInput.contains("emergency") ->
                "I understand this is urgent. Let me connect you immediately."
            lowercaseInput.contains("thanks") || lowercaseInput.contains("thank you") ->
                "You're welcome! Is there anything else I can help you with?"
            lowercaseInput.contains("goodbye") || lowercaseInput.contains("bye") ->
                "Thank you for calling. Have a great day!"
            else ->
                "I understand. Let me help you with that. Could you provide more details?"
        }
    }
    
    // Helper methods for decision making
    private fun isBusinessHours(): Boolean {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        return dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY && hour in 9..17
    }
    
    private fun isEmergencyContact(phoneNumber: String): Boolean {
        // Check against emergency contact list
        return false // Placeholder
    }
    
    private fun isSpamNumber(phoneNumber: String): Boolean {
        // Check against spam database
        return false // Placeholder
    }
    
    private fun getUserPreferenceForUnknownCalls(): Boolean {
        // Get user preference for handling unknown calls
        return true // Default to answering
    }
    
    // SpeechRecognizer callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "Ready for speech")
    }
    
    override fun onBeginningOfSpeech() {
        Log.d(TAG, "Beginning of speech")
    }
    
    override fun onRmsChanged(rmsdB: Float) {
        // Audio level changed
    }
    
    override fun onBufferReceived(buffer: ByteArray?) {
        // Audio buffer received
    }
    
    override fun onEndOfSpeech() {
        Log.d(TAG, "End of speech")
    }
    
    override fun onError(error: Int) {
        Log.e(TAG, "Speech recognition error: $error")
        // Restart listening if still in call
        if (isProcessing && currentCall?.state == Call.STATE_ACTIVE) {
            startListening()
        }
    }
    
    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            processSpokenText(matches[0])
        }
    }
    
    override fun onPartialResults(partialResults: Bundle?) {
        // Handle partial results if needed
    }
    
    override fun onEvent(eventType: Int, params: Bundle?) {
        // Handle speech events
    }
    
    // TextToSpeech callback
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.getDefault()
            Log.d(TAG, "TextToSpeech initialized successfully")
        } else {
            Log.e(TAG, "TextToSpeech initialization failed")
        }
    }
}