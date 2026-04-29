package com.nextgentele.ai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.nextgentele.ai.service.CallManagementService

class PhoneStateReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "PhoneStateReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                
                Log.d(TAG, "Phone state changed: $state, number: $phoneNumber")
                
                when (state) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        handleIncomingCall(context, phoneNumber)
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        handleCallAnswered(context, phoneNumber)
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        handleCallEnded(context)
                    }
                }
            }
        }
    }
    
    private fun handleIncomingCall(context: Context, phoneNumber: String?) {
        Log.d(TAG, "Incoming call from: $phoneNumber")
        
        // Start the call management service if it's not already running
        val intent = Intent(context, CallManagementService::class.java).apply {
            putExtra("action", "incoming_call")
            putExtra("phone_number", phoneNumber)
        }
        context.startForegroundService(intent)
    }
    
    private fun handleCallAnswered(context: Context, phoneNumber: String?) {
        Log.d(TAG, "Call answered: $phoneNumber")
        
        // Notify the call management service
        val intent = Intent(context, CallManagementService::class.java).apply {
            putExtra("action", "call_answered")
            putExtra("phone_number", phoneNumber)
        }
        context.startService(intent)
    }
    
    private fun handleCallEnded(context: Context) {
        Log.d(TAG, "Call ended")
        
        // Notify the call management service
        val intent = Intent(context, CallManagementService::class.java).apply {
            putExtra("action", "call_ended")
        }
        context.startService(intent)
    }
}