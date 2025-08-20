package com.nextgenbuildpro.receptionist.service

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.receptionist.data.CallStatus
import java.util.regex.Pattern

/**
 * Service for handling phone calls
 * 
 * This service provides functionality for:
 * - Screening spam calls
 * - Managing call status
 * - Handling call transcription
 */
class CallHandlingService(private val context: Context) {
    
    private val TAG = "CallHandlingService"
    
    // List of known spam patterns
    private val spamPatterns = listOf(
        "^\\+1800",
        "^\\+1888",
        "^\\+1877",
        "^\\+1866",
        "^\\+1855",
        "^\\+1844",
        "^\\+1833",
        "^\\+1822",
        "^\\+1900"
    )
    
    // List of explicitly blocked numbers
    private val blockedNumbers = mutableSetOf<String>()
    
    /**
     * Check if a phone number is likely spam
     */
    fun isSpamNumber(phoneNumber: String): Boolean {
        // Check if number is explicitly blocked
        if (blockedNumbers.contains(phoneNumber)) {
            Log.d(TAG, "Number $phoneNumber is explicitly blocked")
            return true
        }
        
        // Check against spam patterns
        for (pattern in spamPatterns) {
            if (Pattern.compile(pattern).matcher(phoneNumber).find()) {
                Log.d(TAG, "Number $phoneNumber matches spam pattern $pattern")
                return true
            }
        }
        
        // Not identified as spam
        return false
    }
    
    /**
     * Block a specific phone number
     */
    fun blockNumber(phoneNumber: String) {
        Log.d(TAG, "Blocking number: $phoneNumber")
        blockedNumbers.add(phoneNumber)
    }
    
    /**
     * Unblock a specific phone number
     */
    fun unblockNumber(phoneNumber: String): Boolean {
        Log.d(TAG, "Unblocking number: $phoneNumber")
        return blockedNumbers.remove(phoneNumber)
    }
    
    /**
     * Get a list of all blocked numbers
     */
    fun getBlockedNumbers(): Set<String> {
        return blockedNumbers.toSet()
    }
    
    /**
     * Add a spam pattern
     */
    fun addSpamPattern(pattern: String) {
        Log.d(TAG, "Adding spam pattern: $pattern")
        spamPatterns.toMutableList().add(pattern)
    }
    
    /**
     * Simulate call transcription
     * In a real app, this would use a speech-to-text service
     */
    fun transcribeCall(callDuration: Int, isIncoming: Boolean, callerName: String): String {
        Log.d(TAG, "Transcribing call with $callerName, duration: $callDuration seconds")
        
        // For demonstration purposes, we'll generate a simulated transcription
        if (callDuration < 10) {
            return "Call was too short for transcription"
        }
        
        val transcriptionBuilder = StringBuilder()
        
        if (isIncoming) {
            transcriptionBuilder.append("$callerName: Hello, I'm calling about my project.\n")
            transcriptionBuilder.append("You: Hi $callerName, thanks for calling. How can I help you today?\n")
            
            if (callDuration > 30) {
                transcriptionBuilder.append("$callerName: I was wondering about the timeline for completion.\n")
                transcriptionBuilder.append("You: We're currently on schedule. The next milestone is scheduled for next week.\n")
            }
            
            if (callDuration > 60) {
                transcriptionBuilder.append("$callerName: Great, and what about the materials we discussed?\n")
                transcriptionBuilder.append("You: I've ordered them and they should arrive in 3-4 business days.\n")
                transcriptionBuilder.append("$callerName: Perfect, thank you for the update.\n")
                transcriptionBuilder.append("You: You're welcome. I'll keep you posted on any changes.\n")
            }
        } else {
            transcriptionBuilder.append("You: Hello $callerName, I'm calling to update you on your project.\n")
            
            if (callDuration > 30) {
                transcriptionBuilder.append("$callerName: Hi, thanks for calling. What's the update?\n")
                transcriptionBuilder.append("You: We've made good progress. The framing is complete and we're moving to the next phase.\n")
            }
            
            if (callDuration > 60) {
                transcriptionBuilder.append("$callerName: That sounds great. Any issues I should know about?\n")
                transcriptionBuilder.append("You: Nothing major. We had a small delay with a material delivery but we're still on schedule.\n")
                transcriptionBuilder.append("$callerName: Good to hear. When do you think you'll be finished?\n")
                transcriptionBuilder.append("You: We're still on track for completion by the end of the month.\n")
            }
        }
        
        return transcriptionBuilder.toString()
    }
    
    /**
     * Handle an incoming call
     */
    fun handleIncomingCall(phoneNumber: String, callerName: String?): CallStatus {
        Log.d(TAG, "Handling incoming call from: $phoneNumber, caller: $callerName")
        
        // Check if this is a known spam number
        if (isSpamNumber(phoneNumber)) {
            Log.d(TAG, "Identified as spam call, rejecting")
            return CallStatus.REJECTED_SPAM
        }
        
        // If we have a caller name, it's a known caller
        return if (callerName != null) {
            CallStatus.ANSWERED_KNOWN_CALLER
        } else {
            CallStatus.ANSWERED_UNKNOWN_CALLER
        }
    }
}