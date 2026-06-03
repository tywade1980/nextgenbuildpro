package com.aireceptionist.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.aireceptionist.app.AIReceptionistApp
import com.aireceptionist.app.R
import com.aireceptionist.app.ai.agents.AgentManager
import com.aireceptionist.app.ai.agents.AgentInput
import com.aireceptionist.app.ai.agents.InputType
import com.aireceptionist.app.data.models.CallContext
import com.aireceptionist.app.data.repository.CallRepository
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

/**
 * Foreground service that handles call processing with AI agents
 */
@AndroidEntryPoint
class CallHandlingService : Service() {
    
    @Inject
    lateinit var agentManager: AgentManager
    
    @Inject
    lateinit var callRepository: CallRepository
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentCallId: String? = null
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        Logger.i(TAG, "Call Handling Service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.i(TAG, "Call Handling Service started")
        
        intent?.let { processCallIntent(it) }
        
        return START_STICKY
    }
    
    private fun processCallIntent(intent: Intent) {
        scope.launch {
            try {
                val callId = intent.getStringExtra("call_id")
                val callerNumber = intent.getStringExtra("caller_number")
                val isIncoming = intent.getBooleanExtra("is_incoming", true)
                
                if (callId != null) {
                    currentCallId = callId
                    startForeground(NOTIFICATION_ID, createNotification(callerNumber, isIncoming))
                    handleCall(callId, callerNumber, isIncoming)
                }
                
            } catch (e: Exception) {
                Logger.e(TAG, "Error processing call intent", e)
            }
        }
    }
    
    private suspend fun handleCall(callId: String, callerNumber: String?, isIncoming: Boolean) {
        try {
            Logger.i(TAG, "Handling call: $callId from $callerNumber (incoming: $isIncoming)")
            
            // Create call context
            val callContext = CallContext(
                callId = callId,
                callerNumber = callerNumber,
                callerName = null, // Could be looked up from contacts
                callStartTime = System.currentTimeMillis(),
                isIncoming = isIncoming,
                callState = "ringing"
            )
            
            // Save initial call context
            callRepository.insertCallContext(callContext)
            
            // If incoming call, auto-answer after a brief delay
            if (isIncoming) {
                delay(2000) // Give user a chance to answer manually
                processCallStarted(callContext)
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error handling call", e)
        }
    }
    
    private suspend fun processCallStarted(callContext: CallContext) {
        try {
            Logger.i(TAG, "Processing call started for: ${callContext.callId}")
            
            // Update call state
            val updatedContext = callContext.copy(callState = "active")
            callRepository.updateCallContext(updatedContext)
            
            // Create initial greeting input
            val greetingInput = AgentInput(
                type = InputType.CALL_EVENT,
                content = "call_started",
                context = updatedContext,
                metadata = mapOf(
                    "caller_number" to (callContext.callerNumber ?: "unknown"),
                    "is_incoming" to callContext.isIncoming
                )
            )
            
            // Process through agent manager
            agentManager.processInput(greetingInput, updatedContext).collect { response ->
                Logger.d(TAG, "Agent response: ${response.responseType} - ${response.content}")
                
                // Handle agent responses
                when (response.responseType) {
                    com.aireceptionist.app.ai.agents.ResponseType.SPEECH_OUTPUT -> {
                        // Play TTS response
                        playTTSResponse(response.content)
                    }
                    com.aireceptionist.app.ai.agents.ResponseType.ACTION_COMMAND -> {
                        // Execute actions
                        executeActions(response.actions)
                    }
                    else -> {
                        Logger.d(TAG, "Unhandled response type: ${response.responseType}")
                    }
                }
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error processing call started", e)
        }
    }
    
    private suspend fun playTTSResponse(content: String) {
        try {
            Logger.d(TAG, "Playing TTS response: $content")
            // TTS playback would be handled here
            // This would integrate with the audio system during a call
        } catch (e: Exception) {
            Logger.e(TAG, "Error playing TTS response", e)
        }
    }
    
    private suspend fun executeActions(actions: List<com.aireceptionist.app.ai.agents.AgentAction>) {
        for (action in actions) {
            try {
                Logger.d(TAG, "Executing action: ${action.actionType}")
                
                when (action.actionType) {
                    com.aireceptionist.app.ai.agents.ActionType.SEND_SMS -> {
                        val phone = action.parameters["phone"] as? String
                        val message = action.parameters["message"] as? String
                        if (phone != null && message != null) {
                            sendSMS(phone, message)
                        }
                    }
                    com.aireceptionist.app.ai.agents.ActionType.SEND_EMAIL -> {
                        val email = action.parameters["email"] as? String
                        val subject = action.parameters["subject"] as? String ?: "AI Receptionist"
                        val message = action.parameters["message"] as? String
                        if (email != null && message != null) {
                            sendEmail(email, subject, message)
                        }
                    }
                    com.aireceptionist.app.ai.agents.ActionType.UPDATE_DATABASE -> {
                        // Update database with call information
                        updateCallDatabase(action.parameters)
                    }
                    com.aireceptionist.app.ai.agents.ActionType.TRIGGER_INTEGRATION -> {
                        // Trigger external integrations
                        triggerIntegration(action.parameters)
                    }
                    else -> {
                        Logger.d(TAG, "Unhandled action type: ${action.actionType}")
                    }
                }
                
            } catch (e: Exception) {
                Logger.e(TAG, "Error executing action: ${action.actionType}", e)
            }
        }
    }
    
    private suspend fun sendSMS(phone: String, message: String) {
        try {
            // SMS sending logic would go here
            Logger.i(TAG, "Sending SMS to $phone: $message")
        } catch (e: Exception) {
            Logger.e(TAG, "Error sending SMS", e)
        }
    }
    
    private suspend fun sendEmail(email: String, subject: String, message: String) {
        try {
            // Email sending logic would go here
            Logger.i(TAG, "Sending email to $email: $subject")
        } catch (e: Exception) {
            Logger.e(TAG, "Error sending email", e)
        }
    }
    
    private suspend fun updateCallDatabase(parameters: Map<String, Any>) {
        try {
            // Database update logic
            Logger.d(TAG, "Updating call database with parameters: $parameters")
        } catch (e: Exception) {
            Logger.e(TAG, "Error updating call database", e)
        }
    }
    
    private suspend fun triggerIntegration(parameters: Map<String, Any>) {
        try {
            // Integration trigger logic
            Logger.d(TAG, "Triggering integration with parameters: $parameters")
        } catch (e: Exception) {
            Logger.e(TAG, "Error triggering integration", e)
        }
    }
    
    private fun createNotification(callerNumber: String?, isIncoming: Boolean): Notification {
        val title = if (isIncoming) "Incoming Call" else "Outgoing Call"
        val content = "AI handling call ${callerNumber?.let { "from $it" } ?: ""}"
        
        return NotificationCompat.Builder(this, AIReceptionistApp.CALL_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_phone) // You'll need to add this icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .build()
    }
    
    fun endCall(callId: String) {
        scope.launch {
            try {
                Logger.i(TAG, "Ending call: $callId")
                
                // Update call context
                val callContext = callRepository.getCallContext(callId)
                callContext?.let { context ->
                    val updatedContext = context.copy(
                        callState = "ended",
                        callEndTime = System.currentTimeMillis()
                    )
                    callRepository.updateCallContext(updatedContext)
                }
                
                if (currentCallId == callId) {
                    currentCallId = null
                    stopForeground(true)
                    stopSelf()
                }
                
            } catch (e: Exception) {
                Logger.e(TAG, "Error ending call", e)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        Logger.i(TAG, "Call Handling Service destroyed")
    }
    
    companion object {
        private const val TAG = "CallHandlingService"
        private const val NOTIFICATION_ID = 1001
        
        fun start(context: Context, callId: String, callerNumber: String?, isIncoming: Boolean) {
            val intent = Intent(context, CallHandlingService::class.java).apply {
                putExtra("call_id", callId)
                putExtra("caller_number", callerNumber)
                putExtra("is_incoming", isIncoming)
            }
            context.startForegroundService(intent)
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, CallHandlingService::class.java)
            context.stopService(intent)
        }
    }
}