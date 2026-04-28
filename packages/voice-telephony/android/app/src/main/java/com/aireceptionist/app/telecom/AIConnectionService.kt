package com.aireceptionist.app.telecom

import android.net.Uri
import android.telecom.*
import com.aireceptionist.app.utils.Logger
import dagger.hilt.android.AndroidEntryPoint

/**
 * Connection Service for handling VoIP calls and AI-initiated calls
 */
@AndroidEntryPoint
class AIConnectionService : ConnectionService() {
    
    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection? {
        Logger.i(TAG, "Creating outgoing connection to: ${request?.address}")
        
        return request?.let { 
            AIConnection(it.address, true).apply {
                setInitializing()
                // Connection will be handled by the AI system
            }
        }
    }
    
    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection? {
        Logger.i(TAG, "Creating incoming connection from: ${request?.address}")
        
        return request?.let {
            AIConnection(it.address, false).apply {
                setRinging()
                // Connection will be handled by the AI system
            }
        }
    }
    
    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
        Logger.e(TAG, "Failed to create outgoing connection to: ${request?.address}")
    }
    
    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
        Logger.e(TAG, "Failed to create incoming connection from: ${request?.address}")
    }
    
    /**
     * Custom Connection class for AI-managed calls
     */
    inner class AIConnection(
        private val address: Uri,
        private val isOutgoing: Boolean
    ) : Connection() {
        
        init {
            connectionCapabilities = CAPABILITY_SUPPORT_HOLD or 
                                   CAPABILITY_HOLD or 
                                   CAPABILITY_MUTE or
                                   CAPABILITY_SUPPORTS_VT_LOCAL_RX or
                                   CAPABILITY_SUPPORTS_VT_LOCAL_TX or
                                   CAPABILITY_SUPPORTS_VT_REMOTE_RX or
                                   CAPABILITY_SUPPORTS_VT_REMOTE_TX
            
            audioModeIsVoip = true
            callerDisplayName = "AI Receptionist"
            
            Logger.d(TAG, "AIConnection initialized for: $address (outgoing: $isOutgoing)")
        }
        
        override fun onAnswer() {
            Logger.i(TAG, "Connection answered: $address")
            setActive()
        }
        
        override fun onAnswer(videoState: Int) {
            Logger.i(TAG, "Connection answered with video state $videoState: $address")
            setActive()
        }
        
        override fun onReject() {
            Logger.i(TAG, "Connection rejected: $address")
            setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
            destroy()
        }
        
        override fun onReject(rejectReason: Int) {
            Logger.i(TAG, "Connection rejected with reason $rejectReason: $address")
            setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
            destroy()
        }
        
        override fun onDisconnect() {
            Logger.i(TAG, "Connection disconnected: $address")
            setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
            destroy()
        }
        
        override fun onAbort() {
            Logger.i(TAG, "Connection aborted: $address")
            setDisconnected(DisconnectCause(DisconnectCause.CANCELED))
            destroy()
        }
        
        override fun onHold() {
            Logger.i(TAG, "Connection held: $address")
            setOnHold()
        }
        
        override fun onUnhold() {
            Logger.i(TAG, "Connection unheld: $address")
            setActive()
        }
        
        override fun onMuteStateChanged(isMuted: Boolean) {
            Logger.d(TAG, "Mute state changed to $isMuted: $address")
        }
        
        override fun onCallAudioStateChanged(state: CallAudioState) {
            Logger.d(TAG, "Audio state changed: ${state.route} for $address")
        }
        
        override fun onPlayDtmfTone(c: Char) {
            Logger.d(TAG, "DTMF tone: $c for $address")
        }
        
        override fun onStopDtmfTone() {
            Logger.d(TAG, "Stop DTMF tone for $address")
        }
        
        override fun onStateChanged(state: Int) {
            super.onStateChanged(state)
            Logger.d(TAG, "Connection state changed to $state: $address")
        }
        
        override fun onSeparate() {
            Logger.i(TAG, "Connection separated: $address")
            setDisconnected(DisconnectCause(DisconnectCause.OTHER))
            destroy()
        }
        
        override fun onPostDialContinue(proceed: Boolean) {
            Logger.d(TAG, "Post dial continue: $proceed for $address")
        }
    }
    
    companion object {
        private const val TAG = "AIConnectionService"
    }
}