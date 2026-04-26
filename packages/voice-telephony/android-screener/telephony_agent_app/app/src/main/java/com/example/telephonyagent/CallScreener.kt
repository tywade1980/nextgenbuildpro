package com.example.telephonyagent

import android.telecom.CallScreeningService
import android.telecom.Call
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * CallScreener implements the CallScreeningService API to intercept incoming
 * calls before they are presented to the user. It queries the AiProcessor to
 * determine whether to allow or reject a call based on the phone number and
 * historical patterns. The result is returned asynchronously via a
 * CallResponse.
 */
class CallScreener : CallScreeningService() {
    private val aiProcessor = AiProcessor()
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onScreenCall(callDetails: Call.Details) {
        // Always perform long‑running work off the main binder thread
        scope.launch {
            val phoneNumber = callDetails.handle?.schemeSpecificPart
            val prefs = PreferencesManager(applicationContext)

            // Compute a simple risk score based on the phone number's hash.
            // This is a placeholder for actual AI‑based evaluation.  The
            // result is normalised to 0–100 using absolute value modulo 100.
            val riskScore: Int = phoneNumber?.hashCode()?.let { kotlin.math.abs(it) % 100 } ?: 0
            val threshold = prefs.getScreeningThreshold()

            val allowCall: Boolean = try {
                // If the riskScore is below the threshold, allow the call.
                val preliminaryDecision = riskScore <= threshold
                // Combine with AI inference (currently always true) when integrated.
                val aiDecision = aiProcessor.evaluateIncomingCall(phoneNumber)
                preliminaryDecision && aiDecision
            } catch (e: Exception) {
                Log.e("CallScreener", "Call evaluation failed", e)
                true // default to allow on failure
            }

            val response = CallResponse.Builder()
                .setDisallowCall(!allowCall)
                .setRejectCall(!allowCall)
                .setSkipCallLog(!allowCall)
                .setSkipNotification(!allowCall)
                .build()
            respondToCall(callDetails, response)
        }
    }
}