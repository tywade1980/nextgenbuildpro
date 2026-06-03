package com.nextgentele.ai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.nextgentele.ai.service.AICallHandlerService

class IncomingCallReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "IncomingCallReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            
            if (state == TelephonyManager.EXTRA_STATE_RINGING && phoneNumber != null) {
                Log.d(TAG, "Incoming call detected: $phoneNumber")
                
                // Start AI call handler service
                val serviceIntent = Intent(context, AICallHandlerService::class.java).apply {
                    putExtra("incoming_number", phoneNumber)
                    putExtra("action", "handle_incoming_call")
                }
                context.startForegroundService(serviceIntent)
            }
        }
    }
}