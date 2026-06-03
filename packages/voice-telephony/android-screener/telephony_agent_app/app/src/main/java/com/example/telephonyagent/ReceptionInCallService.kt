package com.example.telephonyagent

import android.telecom.InCallService
import android.telecom.Call
import android.util.Log

/**
 * ReceptionInCallService handles ongoing calls after they have been approved
 * by the CallScreener. It delegates to the AiProcessor to provide real‑time
 * conversational assistance and transcription. When a call is added or
 * removed, appropriate session management methods are called.
 */
class ReceptionInCallService : InCallService() {
    private val aiProcessor = AiProcessor()

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d("ReceptionInCallService", "Call added: ${call.details}")
        // Start AI session for this call (e.g. begin recording and inference)
        aiProcessor.startInCallSession(call)
    }

    override fun onCallRemoved(call: Call) {
        Log.d("ReceptionInCallService", "Call removed: ${call.details}")
        // End AI session and release resources
        aiProcessor.endInCallSession(call)
        super.onCallRemoved(call)
    }
}