package com.nextgentele.ai.telecom

import android.net.Uri
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.util.Log
import com.nextgentele.ai.ai.AICallProcessor

class NextGenConnectionService : ConnectionService() {
    
    companion object {
        private const val TAG = "NextGenConnectionService"
    }
    
    private lateinit var aiCallProcessor: AICallProcessor
    
    override fun onCreate() {
        super.onCreate()
        aiCallProcessor = AICallProcessor(this)
    }
    
    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection? {
        
        val phoneNumber = request?.address?.schemeSpecificPart
        Log.d(TAG, "Creating outgoing connection to: $phoneNumber")
        
        return NextGenConnection(this, request, aiCallProcessor).apply {
            setDialing()
            if (phoneNumber != null) {
                aiCallProcessor.handleOutgoingCall(null, phoneNumber) // Pass actual call when available
            }
        }
    }
    
    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection? {
        
        val phoneNumber = request?.address?.schemeSpecificPart
        Log.d(TAG, "Creating incoming connection from: $phoneNumber")
        
        return NextGenConnection(this, request, aiCallProcessor).apply {
            setRinging()
            // AI will decide whether to answer
        }
    }
}

class NextGenConnection(
    private val context: ConnectionService,
    private val request: ConnectionRequest?,
    private val aiCallProcessor: AICallProcessor
) : Connection() {
    
    companion object {
        private const val TAG = "NextGenConnection"
    }
    
    init {
        connectionProperties = PROPERTY_SELF_MANAGED
        audioModeIsVoip = false
    }
    
    override fun onAnswer() {
        Log.d(TAG, "Connection answered")
        setActive()
        // AI takes over the call
        // aiCallProcessor.handleIncomingCall(this) // Would need to adapt for Connection instead of Call
    }
    
    override fun onReject() {
        Log.d(TAG, "Connection rejected")
        setDisconnected(android.telecom.DisconnectCause(android.telecom.DisconnectCause.REJECTED))
        destroy()
    }
    
    override fun onDisconnect() {
        Log.d(TAG, "Connection disconnected")
        setDisconnected(android.telecom.DisconnectCause(android.telecom.DisconnectCause.LOCAL))
        destroy()
    }
    
    override fun onHold() {
        Log.d(TAG, "Connection held")
        setOnHold()
    }
    
    override fun onUnhold() {
        Log.d(TAG, "Connection unheld")
        setActive()
    }
    
    override fun onAbort() {
        Log.d(TAG, "Connection aborted")
        setDisconnected(android.telecom.DisconnectCause(android.telecom.DisconnectCause.CANCELED))
        destroy()
    }
}