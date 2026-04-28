package com.aireceptionist.app.telecom

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.content.Intent
import android.os.IBinder
import com.aireceptionist.app.ai.agents.AgentManager
import com.aireceptionist.app.ai.agents.AgentInput
import com.aireceptionist.app.ai.agents.InputType
import com.aireceptionist.app.data.models.CallContext
import com.aireceptionist.app.service.CallHandlingService
import com.aireceptionist.app.ui.call.CallActivity
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

/**
 * Android InCallService implementation for handling incoming and outgoing calls
 * Integrates with the AI agent system for intelligent call management
 */
@AndroidEntryPoint
class AIInCallService : InCallService() {
    
    @Inject
    lateinit var agentManager: AgentManager
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentCall: Call? = null
    private val callCallbacks = mutableMapOf<Call, CallCallback>()
    
    override fun onBind(intent: Intent?): IBinder? {
        Logger.d(TAG, "AIInCallService bound")
        return super.onBind(intent)
    }
    
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Logger.i(TAG, "Call added: ${call.details}")
        
        currentCall = call
        
        // Register callback for this call
        val callback = CallCallback(call)
        call.registerCallback(callback)
        callCallbacks[call] = callback
        
        // Start AI processing for this call
        startAICallHandling(call)
        
        // Show call UI if this is an incoming call
        if (call.state == Call.STATE_RINGING) {
            showCallActivity(call)
        }
    }
    
    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Logger.i(TAG, "Call removed: ${call.details}")
        
        // Cleanup callbacks
        callCallbacks[call]?.let { callback ->
            call.unregisterCallback(callback)
            callCallbacks.remove(call)
        }
        
        if (currentCall == call) {
            currentCall = null
        }
        
        // Stop AI processing for this call
        stopAICallHandling(call)
    }
    
    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)
        Logger.d(TAG, "Audio state changed: ${audioState.route}")
        
        // Handle audio routing changes
        handleAudioStateChange(audioState)
    }
    
    private fun startAICallHandling(call: Call) {
        scope.launch {
            try {
                Logger.i(TAG, "Starting AI call handling for call: ${call.details.handle}")
                
                // Create call context
                val callContext = createCallContext(call)
                
                // Start the call handling service
                val serviceIntent = Intent(this@AIInCallService, CallHandlingService::class.java).apply {
                    putExtra("call_id", call.details.telecomeCallId)
                    putExtra("caller_number", call.details.handle?.schemeSpecificPart)
                    putExtra("is_incoming", call.state == Call.STATE_RINGING)
                }
                startService(serviceIntent)
                
                // Process initial call event
                val input = AgentInput(
                    type = InputType.CALL_EVENT,
                    content = "call_started",
                    context = callContext,
                    metadata = mapOf(
                        "call_state" to call.state,
                        "call_direction" to if (call.state == Call.STATE_RINGING) "incoming" else "outgoing"
                    )
                )
                
                // Process through agent manager
                agentManager.processInput(input, callContext).collect { response ->
                    handleAgentResponse(call, response)
                }
                
            } catch (e: Exception) {
                Logger.e(TAG, "Error starting AI call handling", e)
            }
        }
    }
    
    private fun stopAICallHandling(call: Call) {
        scope.launch {
            try {
                Logger.i(TAG, "Stopping AI call handling for call: ${call.details.handle}")
                
                // Stop the call handling service
                val serviceIntent = Intent(this@AIInCallService, CallHandlingService::class.java)
                stopService(serviceIntent)
                
            } catch (e: Exception) {
                Logger.e(TAG, "Error stopping AI call handling", e)
            }
        }
    }
    
    private fun createCallContext(call: Call): CallContext {
        return CallContext(
            callId = call.details.telecomeCallId,
            callerNumber = call.details.handle?.schemeSpecificPart,
            callerName = call.details.callerDisplayName,
            callStartTime = System.currentTimeMillis(),
            isIncoming = call.state == Call.STATE_RINGING,
            callState = mapCallState(call.state)
        )
    }
    
    private fun mapCallState(telecomState: Int): String {
        return when (telecomState) {
            Call.STATE_NEW -> "new"
            Call.STATE_RINGING -> "ringing" 
            Call.STATE_DIALING -> "dialing"
            Call.STATE_ACTIVE -> "active"
            Call.STATE_HOLDING -> "holding"
            Call.STATE_DISCONNECTED -> "disconnected"
            else -> "unknown"
        }
    }
    
    private suspend fun handleAgentResponse(call: Call, response: com.aireceptionist.app.ai.agents.AgentResponse) {
        try {
            Logger.d(TAG, "Handling agent response: ${response.responseType}")
            
            // Execute actions based on agent response
            response.actions.forEach { action ->
                when (action.actionType) {
                    com.aireceptionist.app.ai.agents.ActionType.ANSWER_CALL -> {
                        if (call.state == Call.STATE_RINGING) {
                            call.answer(0) // Answer with video state
                            Logger.i(TAG, "Call answered by AI")
                        }
                    }
                    com.aireceptionist.app.ai.agents.ActionType.END_CALL -> {
                        call.disconnect()
                        Logger.i(TAG, "Call ended by AI")
                    }
                    com.aireceptionist.app.ai.agents.ActionType.HOLD_CALL -> {
                        call.hold()
                        Logger.i(TAG, "Call put on hold by AI")
                    }
                    com.aireceptionist.app.ai.agents.ActionType.TRANSFER_CALL -> {
                        // Handle call transfer
                        val destination = action.parameters["destination"] as? String
                        destination?.let { transferCall(call, it) }
                    }
                    com.aireceptionist.app.ai.agents.ActionType.PLAY_AUDIO -> {
                        // Handle audio playback during call
                        val audioFile = action.parameters["audio_file"] as? String
                        audioFile?.let { playAudioInCall(call, it) }
                    }
                    else -> {
                        Logger.d(TAG, "Unhandled action type: ${action.actionType}")
                    }
                }
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error handling agent response", e)
        }
    }
    
    private fun showCallActivity(call: Call) {
        try {
            val intent = Intent(this, CallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("call_id", call.details.telecomeCallId)
                putExtra("caller_number", call.details.handle?.schemeSpecificPart)
                putExtra("caller_name", call.details.callerDisplayName)
                putExtra("is_incoming", call.state == Call.STATE_RINGING)
            }
            startActivity(intent)
            Logger.d(TAG, "Call activity started")
        } catch (e: Exception) {
            Logger.e(TAG, "Error starting call activity", e)
        }
    }
    
    private fun handleAudioStateChange(audioState: CallAudioState) {
        scope.launch {
            try {
                // Notify AI agents about audio state changes
                currentCall?.let { call ->
                    val callContext = createCallContext(call)
                    val input = AgentInput(
                        type = InputType.SYSTEM_EVENT,
                        content = "audio_state_changed",
                        context = callContext,
                        metadata = mapOf(
                            "audio_route" to audioState.route,
                            "is_muted" to audioState.isMuted,
                            "supported_routes" to audioState.supportedRouteMask
                        )
                    )
                    
                    agentManager.processInput(input, callContext).collect { response ->
                        // Handle audio-related responses
                        Logger.d(TAG, "Audio state response: ${response.content}")
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error handling audio state change", e)
            }
        }
    }
    
    private suspend fun transferCall(call: Call, destination: String) {
        try {
            // Implement call transfer logic
            Logger.i(TAG, "Transferring call to: $destination")
            
            // This would involve creating a new call to the destination
            // and then conferencing or transferring the original call
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error transferring call", e)
        }
    }
    
    private suspend fun playAudioInCall(call: Call, audioFile: String) {
        try {
            // Implement in-call audio playback
            Logger.i(TAG, "Playing audio in call: $audioFile")
            
            // This would use the CallAudioState to play audio during the call
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error playing audio in call", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        Logger.d(TAG, "AIInCallService destroyed")
    }
    
    /**
     * Callback class for monitoring call state changes
     */
    inner class CallCallback(private val call: Call) : Call.Callback() {
        
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Logger.d(TAG, "Call state changed to: $state")
            
            scope.launch {
                try {
                    val callContext = createCallContext(call)
                    val input = AgentInput(
                        type = InputType.CALL_EVENT,
                        content = "state_changed",
                        context = callContext,
                        metadata = mapOf(
                            "old_state" to call.state,
                            "new_state" to state
                        )
                    )
                    
                    agentManager.processInput(input, callContext).collect { response ->
                        handleAgentResponse(call, response)
                    }
                    
                } catch (e: Exception) {
                    Logger.e(TAG, "Error processing state change", e)
                }
            }
        }
        
        override fun onDetailsChanged(call: Call, details: Call.Details) {
            super.onDetailsChanged(call, details)
            Logger.d(TAG, "Call details changed: ${details.handle}")
        }
        
        override fun onCannedTextResponsesLoaded(call: Call, cannedTextResponses: List<String>) {
            super.onCannedTextResponsesLoaded(call, cannedTextResponses)
            Logger.d(TAG, "Canned responses loaded: ${cannedTextResponses.size}")
        }
        
        override fun onPostDialWait(call: Call, remainingPostDialSequence: String) {
            super.onPostDialWait(call, remainingPostDialSequence)
            Logger.d(TAG, "Post dial wait: $remainingPostDialSequence")
        }
        
        override fun onVideoCallChanged(call: Call, videoCall: Call.VideoCall) {
            super.onVideoCallChanged(call, videoCall)
            Logger.d(TAG, "Video call changed")
        }
        
        override fun onCallDestroyed(call: Call) {
            super.onCallDestroyed(call)
            Logger.d(TAG, "Call destroyed")
        }
        
        override fun onConferenceableCallsChanged(call: Call, conferenceableCalls: List<Call>) {
            super.onConferenceableCallsChanged(call, conferenceableCalls)
            Logger.d(TAG, "Conferenceable calls changed: ${conferenceableCalls.size}")
        }
    }
    
    companion object {
        private const val TAG = "AIInCallService"
    }
}