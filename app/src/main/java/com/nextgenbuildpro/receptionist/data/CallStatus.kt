package com.nextgenbuildpro.receptionist.data

/**
 * Enum representing the status of a call handled by the AI receptionist
 */
enum class CallStatus {
    /**
     * Call was answered and the caller was identified as a known lead/client
     */
    ANSWERED_KNOWN_CALLER,
    
    /**
     * Call was answered but the caller was not identified (potential new lead)
     */
    ANSWERED_UNKNOWN_CALLER,
    
    /**
     * Call was rejected because it was identified as spam
     */
    REJECTED_SPAM,
    
    /**
     * Call was forwarded to a human
     */
    FORWARDED_TO_HUMAN,
    
    /**
     * Call was missed (no answer)
     */
    MISSED,
    
    /**
     * Call ended with a message taken
     */
    MESSAGE_TAKEN,
    
    /**
     * Call ended with an appointment scheduled
     */
    APPOINTMENT_SCHEDULED,
    
    /**
     * Call ended with an order placed
     */
    ORDER_PLACED
}