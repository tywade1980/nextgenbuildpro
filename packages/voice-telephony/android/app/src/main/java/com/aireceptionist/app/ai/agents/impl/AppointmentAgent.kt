package com.aireceptionist.app.ai.agents.impl

import com.aireceptionist.app.ai.agents.*
import com.aireceptionist.app.data.repository.AppointmentRepository
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Agent responsible for appointment scheduling and management
 */
class AppointmentAgent @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : Agent {
    
    override val agentId = "appointment_agent"
    override val agentName = "Appointment Scheduling Agent"
    override val capabilities = listOf(
        AgentCapability.APPOINTMENT_SCHEDULING,
        AgentCapability.CONTEXT_AWARENESS
    )
    override val priority = 7
    
    private var isInitialized = false
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    override suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.i(TAG, "Initializing Appointment Agent")
            
            appointmentRepository.initialize()
            
            isInitialized = true
            Logger.i(TAG, "Appointment Agent initialized successfully")
            true
            
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize Appointment Agent", e)
            false
        }
    }
    
    override suspend fun processInput(input: AgentInput): AgentResponse = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            return@withContext createErrorResponse("Agent not initialized")
        }
        
        when (input.type) {
            InputType.TEXT_MESSAGE, InputType.SYSTEM_EVENT -> processAppointmentRequest(input)
            else -> createErrorResponse("Unsupported input type: ${input.type}")
        }
    }
    
    private suspend fun processAppointmentRequest(input: AgentInput): AgentResponse {
        return try {
            Logger.d(TAG, "Processing appointment request: ${input.content}")
            
            val intent = input.context.intent ?: "appointment_booking"
            val entities = input.metadata["entities"] as? List<*> ?: emptyList<Any>()
            
            when (intent) {
                "appointment_booking" -> handleBookingRequest(input, entities)
                "appointment_inquiry" -> handleAppointmentInquiry(input)
                "appointment_cancellation" -> handleCancellationRequest(input, entities)
                "appointment_reschedule" -> handleRescheduleRequest(input, entities)
                else -> handleGeneralAppointmentQuery(input)
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error processing appointment request", e)
            createErrorResponse("I apologize, but I'm having trouble with your appointment request. Let me connect you with someone who can help.")
        }
    }
    
    private suspend fun handleBookingRequest(
        input: AgentInput,
        entities: List<Any>
    ): AgentResponse {
        
        // Extract appointment details from entities
        val appointmentDetails = extractAppointmentDetails(input.content, entities)
        
        return if (appointmentDetails.isComplete()) {
            // Try to book the appointment
            val bookingResult = appointmentRepository.bookAppointment(appointmentDetails)
            
            if (bookingResult.isSuccess) {
                AgentResponse(
                    agentId = agentId,
                    responseType = ResponseType.SPEECH_OUTPUT,
                    content = "Perfect! I've scheduled your appointment for ${formatAppointmentDetails(appointmentDetails)}. You'll receive a confirmation shortly.",
                    confidence = 0.9f,
                    actions = listOf(
                        AgentAction(
                            actionType = ActionType.SEND_SMS,
                            parameters = mapOf(
                                "phone" to input.context.callerNumber,
                                "message" to "Appointment confirmed: ${formatAppointmentDetails(appointmentDetails)}"
                            )
                        )
                    ),
                    nextSuggestedAgent = "voice_synthesis",
                    metadata = mapOf(
                        "appointment_id" to bookingResult.appointmentId,
                        "booking_status" to "confirmed"
                    )
                )
            } else {
                AgentResponse(
                    agentId = agentId,
                    responseType = ResponseType.SPEECH_OUTPUT,
                    content = "I'm sorry, that time slot isn't available. Let me suggest some alternative times: ${formatAvailableSlots(bookingResult.alternativeSlots)}",
                    confidence = 0.8f,
                    nextSuggestedAgent = "voice_synthesis",
                    metadata = mapOf(
                        "booking_status" to "failed",
                        "reason" to bookingResult.reason,
                        "alternatives" to bookingResult.alternativeSlots
                    )
                )
            }
        } else {
            // Need more information
            val missingInfo = appointmentDetails.getMissingInformation()
            AgentResponse(
                agentId = agentId,
                responseType = ResponseType.SPEECH_OUTPUT,
                content = "I'd be happy to schedule that for you. I just need a few more details: ${missingInfo.joinToString(", ")}",
                confidence = 0.7f,
                nextSuggestedAgent = "voice_synthesis",
                metadata = mapOf(
                    "booking_status" to "incomplete",
                    "missing_info" to missingInfo
                )
            )
        }
    }
    
    private suspend fun handleAppointmentInquiry(input: AgentInput): AgentResponse {
        val callerNumber = input.context.callerNumber
        val appointments = callerNumber?.let { 
            appointmentRepository.getAppointments(it) 
        } ?: emptyList()
        
        return if (appointments.isNotEmpty()) {
            val upcomingAppointment = appointments.firstOrNull()
            AgentResponse(
                agentId = agentId,
                responseType = ResponseType.SPEECH_OUTPUT,
                content = "You have an upcoming appointment on ${formatAppointmentDetails(upcomingAppointment)}. Would you like me to provide more details or help with any changes?",
                confidence = 0.9f,
                nextSuggestedAgent = "voice_synthesis"
            )
        } else {
            AgentResponse(
                agentId = agentId,
                responseType = ResponseType.SPEECH_OUTPUT,
                content = "I don't see any appointments scheduled for your number. Would you like to book a new appointment?",
                confidence = 0.8f,
                nextSuggestedAgent = "voice_synthesis"
            )
        }
    }
    
    private suspend fun handleCancellationRequest(
        input: AgentInput,
        entities: List<Any>
    ): AgentResponse {
        val callerNumber = input.context.callerNumber
        return if (callerNumber != null) {
            val cancellationResult = appointmentRepository.cancelAppointment(callerNumber)
            
            if (cancellationResult.isSuccess) {
                AgentResponse(
                    agentId = agentId,
                    responseType = ResponseType.SPEECH_OUTPUT,
                    content = "Your appointment has been successfully cancelled. Is there anything else I can help you with?",
                    confidence = 0.9f,
                    nextSuggestedAgent = "voice_synthesis"
                )
            } else {
                AgentResponse(
                    agentId = agentId,
                    responseType = ResponseType.SPEECH_OUTPUT,
                    content = "I couldn't find an appointment to cancel. Could you please provide more details about your appointment?",
                    confidence = 0.7f,
                    nextSuggestedAgent = "voice_synthesis"
                )
            }
        } else {
            createErrorResponse("I need your phone number to cancel an appointment.")
        }
    }
    
    private suspend fun handleRescheduleRequest(
        input: AgentInput,
        entities: List<Any>
    ): AgentResponse {
        // Implementation for rescheduling appointments
        return AgentResponse(
            agentId = agentId,
            responseType = ResponseType.SPEECH_OUTPUT,
            content = "I'd be happy to help reschedule your appointment. What new date and time would work better for you?",
            confidence = 0.8f,
            nextSuggestedAgent = "voice_synthesis"
        )
    }
    
    private suspend fun handleGeneralAppointmentQuery(input: AgentInput): AgentResponse {
        return AgentResponse(
            agentId = agentId,
            responseType = ResponseType.SPEECH_OUTPUT,
            content = "I can help you with scheduling, checking, or modifying appointments. What would you like to do?",
            confidence = 0.7f,
            nextSuggestedAgent = "voice_synthesis"
        )
    }
    
    private fun extractAppointmentDetails(text: String, entities: List<Any>): AppointmentDetails {
        // Extract date, time, service type, etc. from text and entities
        return AppointmentDetails(
            date = extractDate(text),
            time = extractTime(text),
            serviceType = extractServiceType(text),
            customerName = null, // Would be extracted from context
            customerPhone = null, // Would be from caller ID
            notes = text
        )
    }
    
    private fun extractDate(text: String): Date? {
        // Simple date extraction - in production, use more sophisticated NLP
        val today = Calendar.getInstance()
        
        when {
            text.contains("today", ignoreCase = true) -> return today.time
            text.contains("tomorrow", ignoreCase = true) -> {
                today.add(Calendar.DAY_OF_YEAR, 1)
                return today.time
            }
            text.contains("next week", ignoreCase = true) -> {
                today.add(Calendar.WEEK_OF_YEAR, 1)
                return today.time
            }
        }
        
        return null
    }
    
    private fun extractTime(text: String): String? {
        // Extract time from text
        val timePattern = Regex("\\b\\d{1,2}:\\d{2}\\s*(am|pm)?\\b", RegexOption.IGNORE_CASE)
        return timePattern.find(text)?.value
    }
    
    private fun extractServiceType(text: String): String? {
        val serviceKeywords = mapOf(
            "consultation" to listOf("consultation", "consult", "meeting"),
            "checkup" to listOf("checkup", "check-up", "examination"),
            "followup" to listOf("follow-up", "followup", "follow up")
        )
        
        for ((service, keywords) in serviceKeywords) {
            if (keywords.any { text.contains(it, ignoreCase = true) }) {
                return service
            }
        }
        
        return null
    }
    
    private fun formatAppointmentDetails(details: AppointmentDetails?): String {
        return details?.let {
            "${it.date?.let { dateFormat.format(it) } ?: "TBD"} at ${it.time ?: "TBD"}"
        } ?: "TBD"
    }
    
    private fun formatAvailableSlots(slots: List<String>): String {
        return slots.joinToString(", ")
    }
    
    override suspend fun shutdown() {
        try {
            Logger.i(TAG, "Shutting down Appointment Agent")
            appointmentRepository.shutdown()
            isInitialized = false
            Logger.i(TAG, "Appointment Agent shutdown complete")
        } catch (e: Exception) {
            Logger.e(TAG, "Error during shutdown", e)
        }
    }
    
    override fun isHealthy(): Boolean {
        return isInitialized && appointmentRepository.isHealthy()
    }
    
    private fun createErrorResponse(message: String): AgentResponse {
        return AgentResponse(
            agentId = agentId,
            responseType = ResponseType.SPEECH_OUTPUT,
            content = message,
            confidence = 0.0f,
            nextSuggestedAgent = "voice_synthesis"
        )
    }
    
    companion object {
        private const val TAG = "AppointmentAgent"
    }
}

data class AppointmentDetails(
    val date: Date?,
    val time: String?,
    val serviceType: String?,
    val customerName: String?,
    val customerPhone: String?,
    val notes: String?
) {
    fun isComplete(): Boolean {
        return date != null && time != null && serviceType != null
    }
    
    fun getMissingInformation(): List<String> {
        val missing = mutableListOf<String>()
        if (date == null) missing.add("date")
        if (time == null) missing.add("time")
        if (serviceType == null) missing.add("service type")
        return missing
    }
}

data class BookingResult(
    val isSuccess: Boolean,
    val appointmentId: String? = null,
    val reason: String? = null,
    val alternativeSlots: List<String> = emptyList()
)

data class CancellationResult(
    val isSuccess: Boolean,
    val reason: String? = null
)