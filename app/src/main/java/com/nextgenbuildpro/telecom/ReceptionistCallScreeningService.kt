package com.nextgenbuildpro.telecom

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.nextgenbuildpro.brain.UnifiedBrainService
import com.nextgenbuildpro.hermes.WgsRepository
import com.nextgenbuildpro.orchestrator.CrmContact
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Absorbed from smart-incallservice.
 * Screens incoming calls: known contacts pass through, unknown callers
 * get AI screening to decide whether to ring or silence.
 */
@AndroidEntryPoint
class ReceptionistCallScreeningService : CallScreeningService() {

    private val TAG = "CallScreening"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject lateinit var unifiedBrainService: UnifiedBrainService
    @Inject lateinit var wgsRepository: WgsRepository

    override fun onScreenCall(callDetails: Call.Details) {
        val callerNumber = callDetails.handle?.schemeSpecificPart ?: ""
        Log.d(TAG, "Screening call from: $callerNumber")

        scope.launch {
            val response = buildScreeningResponse(callerNumber)
            respondToCall(response)
        }
    }

    private suspend fun buildScreeningResponse(callerNumber: String): CallResponse {
        val wgs = wgsRepository.load()
        val knownContact: CrmContact? = wgs.crmContacts.find {
            it.phone?.contains(callerNumber.takeLast(10)) == true
        }

        return if (knownContact != null) {
            Log.d(TAG, "Known contact: ${knownContact.name} — allowing")
            CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSilenceCall(false)
                .build()
        } else {
            // Unknown caller — ask AI whether to screen
            try {
                val decision = unifiedBrainService.generate(
                    messages = listOf(
                        mapOf(
                            "role" to "user",
                            "content" to "Unknown call from $callerNumber. Should this be silenced as likely spam? Reply only YES or NO."
                        )
                    ),
                    persona = "receptionist",
                    maxTokens = 10
                )
                val silence = decision.trim().uppercase().startsWith("YES")
                Log.d(TAG, "AI screening decision for $callerNumber: silence=$silence")
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .setSilenceCall(silence)
                    .build()
            } catch (e: Exception) {
                Log.e(TAG, "AI screening error", e)
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .setSilenceCall(false)
                    .build()
            }
        }
    }
}
