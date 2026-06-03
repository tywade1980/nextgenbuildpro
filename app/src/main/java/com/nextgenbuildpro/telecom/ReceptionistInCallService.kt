package com.nextgenbuildpro.telecom

import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import com.nextgenbuildpro.brain.UnifiedBrainService
import com.nextgenbuildpro.hermes.WgsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Absorbed from smart-incallservice.
 * Manages active calls via Android Telecom InCallService API.
 * Each incoming call gets a CallHandler that drives AI receptionist logic.
 */
@AndroidEntryPoint
class ReceptionistInCallService : InCallService() {

    private val TAG = "ReceptionistInCall"

    @Inject lateinit var unifiedBrainService: UnifiedBrainService
    @Inject lateinit var wgsRepository: WgsRepository

    private val activeHandlers = mutableMapOf<Call, CallHandler>()

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d(TAG, "Call added: ${call.details?.handle}")
        val handler = CallHandler(call, unifiedBrainService, wgsRepository)
        handler.attach()
        activeHandlers[call] = handler
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d(TAG, "Call removed: ${call.details?.handle}")
        activeHandlers.remove(call)?.detach()
    }
}
