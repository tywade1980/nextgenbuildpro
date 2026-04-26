package com.nextgenbuildpro.telecom

import android.telecom.Call
import android.util.Log
import com.nextgenbuildpro.brain.SystemPrompts
import com.nextgenbuildpro.brain.UnifiedBrainService
import com.nextgenbuildpro.hermes.WgsRepository
import com.nextgenbuildpro.orchestrator.CallRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Absorbed from smart-incallservice.
 * Manages a single active call: tracks state, runs AI-driven
 * greeting/screening, and updates WadeGlobalState on call end.
 */
class CallHandler(
    private val call: Call,
    private val unifiedBrainService: UnifiedBrainService,
    private val wgsRepository: WgsRepository
) {
    private val TAG = "CallHandler"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val callId = UUID.randomUUID().toString()
    private val startTime = System.currentTimeMillis()

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            when (state) {
                Call.STATE_ACTIVE  -> onCallAnswered()
                Call.STATE_DISCONNECTED -> onCallEnded()
                Call.STATE_RINGING -> onCallRinging()
            }
        }
    }

    fun attach() {
        call.registerCallback(callback)
    }

    fun detach() {
        call.unregisterCallback(callback)
    }

    private fun onCallRinging() {
        Log.d(TAG, "[$callId] Ringing")
        val callerNumber = call.details?.handle?.schemeSpecificPart ?: "unknown"
        wgsRepository.update {
            activeCalls.add(CallRecord(
                callId = callId,
                callerNumber = callerNumber,
                startTime = startTime
            ))
        }
    }

    private fun onCallAnswered() {
        Log.d(TAG, "[$callId] Answered — starting AI receptionist")
        scope.launch {
            try {
                // Generate receptionist greeting using AI
                val callerNumber = call.details?.handle?.schemeSpecificPart ?: "unknown"
                val greeting = unifiedBrainService.generate(
                    messages = listOf(
                        mapOf(
                            "role" to "user",
                            "content" to "A call just came in from $callerNumber. Generate a brief, professional greeting as the AI receptionist."
                        )
                    ),
                    persona = "receptionist",
                    maxTokens = 128
                )
                Log.d(TAG, "Receptionist greeting: $greeting")
                // In a full implementation, synthesize greeting to audio and play via AudioTrack
            } catch (e: Exception) {
                Log.e(TAG, "AI greeting failed", e)
            }
        }
    }

    private fun onCallEnded() {
        Log.d(TAG, "[$callId] Ended")
        wgsRepository.update {
            activeCalls.find { it.callId == callId }?.endTime = System.currentTimeMillis()
        }
    }
}
