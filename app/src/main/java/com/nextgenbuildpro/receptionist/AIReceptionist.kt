package com.nextgenbuildpro.receptionist

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.nextgenbuildpro.core.AIAssistant
import com.nextgenbuildpro.crm.data.repository.LeadRepository
import com.nextgenbuildpro.features.calendar.models.CalendarEvent
import com.nextgenbuildpro.features.calendar.models.EventType
import com.nextgenbuildpro.receptionist.data.CallStatus
import com.nextgenbuildpro.receptionist.data.Message
import com.nextgenbuildpro.receptionist.data.OrderRequest
import com.nextgenbuildpro.receptionist.repository.CalendarEventRepository
import com.nextgenbuildpro.receptionist.repository.MessageRepository
import com.nextgenbuildpro.receptionist.repository.OrderRepository
import com.nextgenbuildpro.receptionist.service.CallHandlingService
import com.nextgenbuildpro.receptionist.service.NotificationService
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern

/**
 * AI Receptionist for NextGenBuildPro
 * 
 * This class implements the AI receptionist functionality:
 * - Managing client interactions
 * - Qualifying leads during calls
 * - Scheduling meetings and adding them to calendar
 * - Scheduling service calls
 * - Screening spam calls
 * - Taking messages
 * - Ordering materials, food, etc.
 * - Calling subcontractors
 * - Notifying clients about delays
 * - Rescheduling meetings
 * - Following up about estimates
 */
class AIReceptionist(
    private val context: Context,
    private val leadRepository: LeadRepository,
    private val calendarEventRepository: CalendarEventRepository,
    private val messageRepository: MessageRepository,
    private val orderRepository: OrderRepository,
    private val callHandlingService: CallHandlingService,
    private val notificationService: NotificationService
) : AIAssistant {

    private val TAG = "AIReceptionist"

    // Store learned patterns and responses
    private val learnedPatterns = mutableMapOf<String, String>()

    // Text-to-speech engine for voice interactions
    private lateinit var textToSpeech: TextToSpeech
    private var isTtsReady = false

    init {
        // Initialize patterns
        initializePatterns()

        // Initialize text-to-speech
        initializeTextToSpeech()
    }

    /**
     * Process a command from the user
     */
    override fun processCommand(command: String): String {
        val lowercaseCommand = command.lowercase()

        // Check for specific command patterns
        when {
            lowercaseCommand.contains("schedule") || lowercaseCommand.contains("appointment") || 
            lowercaseCommand.contains("meeting") -> {
                return handleSchedulingCommand(command)
            }
            lowercaseCommand.contains("call") || lowercaseCommand.contains("phone") -> {
                return handleCallCommand(command)
            }
            lowercaseCommand.contains("message") || lowercaseCommand.contains("take note") -> {
                return handleMessageCommand(command)
            }
            lowercaseCommand.contains("order") || lowercaseCommand.contains("purchase") -> {
                return handleOrderCommand(command)
            }
            lowercaseCommand.contains("follow up") || lowercaseCommand.contains("followup") -> {
                return handleFollowUpCommand(command)
            }
            lowercaseCommand.contains("qualify") || lowercaseCommand.contains("lead") -> {
                return handleLeadQualificationCommand(command)
            }
            lowercaseCommand.contains("notify") || lowercaseCommand.contains("inform") -> {
                return handleNotificationCommand(command)
            }
            else -> {
                // Check learned patterns
                for ((pattern, response) in learnedPatterns) {
                    if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(command).find()) {
                        return response
                    }
                }

                // Default response
                return "I'm not sure how to help with that request. You can ask me to schedule meetings, " +
                       "make calls, take messages, order materials, follow up with clients, qualify leads, " +
                       "or notify clients about changes."
            }
        }
    }

    /**
     * Generate suggestions based on the current context
     */
    override fun generateSuggestions(context: String): List<String> {
        val suggestions = mutableListOf<String>()

        // Add suggestions based on context
        when {
            context.contains("call") || context.contains("phone") -> {
                suggestions.add("Screen incoming calls")
                suggestions.add("Call a client to reschedule")
                suggestions.add("Call a subcontractor")
                suggestions.add("Take a message")
            }
            context.contains("schedule") || context.contains("calendar") -> {
                suggestions.add("Schedule a client meeting")
                suggestions.add("Schedule a service call")
                suggestions.add("Reschedule an appointment")
                suggestions.add("View today's appointments")
            }
            context.contains("lead") || context.contains("client") -> {
                suggestions.add("Qualify a new lead")
                suggestions.add("Follow up with a lead")
                suggestions.add("Send a follow-up message")
                suggestions.add("Check lead status")
            }
            context.contains("order") || context.contains("material") -> {
                suggestions.add("Order materials for a project")
                suggestions.add("Order lunch for the team")
                suggestions.add("Check order status")
            }
            else -> {
                suggestions.add("Schedule a client meeting")
                suggestions.add("Take a message")
                suggestions.add("Qualify a new lead")
                suggestions.add("Order materials")
                suggestions.add("Follow up with a client")
            }
        }

        return suggestions
    }

    /**
     * Learn from user interactions
     */
    override fun learnFromInteraction(interaction: String, outcome: String) {
        // Extract a pattern from the interaction
        val pattern = interaction
            .replace(".", "\\.")
            .replace("?", "\\?")
            .replace("!", "\\!")
            .replace(Regex("\\b(the|a|an|is|are|was|were|will|would|should|could|can|may|might)\\b"), "\\w*")
            .replace(Regex("\\d+"), "\\d+")

        // Store the pattern and outcome
        learnedPatterns[pattern] = outcome
    }

    /**
     * Handle an incoming call
     */
    fun handleIncomingCall(phoneNumber: String): CallStatus {
        Log.d(TAG, "Handling incoming call from: $phoneNumber")

        // Check if this is a known spam number
        if (callHandlingService.isSpamNumber(phoneNumber)) {
            Log.d(TAG, "Identified as spam call, rejecting")
            return CallStatus.REJECTED_SPAM
        }

        // Try to identify the caller
        val lead = leadRepository.findLeadByPhone(phoneNumber)

        if (lead != null) {
            // Known lead/client
            Log.d(TAG, "Identified caller as lead: ${lead.name}")

            // Answer the call
            val greeting = "Hello ${lead.name}, thank you for calling NextGenBuildPro. How can I assist you today?"
            speakText(greeting)

            return CallStatus.ANSWERED_KNOWN_CALLER
        } else {
            // Unknown caller - potential new lead
            Log.d(TAG, "Unknown caller, treating as potential new lead")

            // Answer the call
            val greeting = "Thank you for calling NextGenBuildPro. This is the AI receptionist. How can I help you today?"
            speakText(greeting)

            return CallStatus.ANSWERED_UNKNOWN_CALLER
        }
    }

    /**
     * Schedule a meeting or appointment
     */
    suspend fun scheduleEvent(
        title: String,
        description: String,
        startTime: Date,
        endTime: Date,
        location: String,
        eventType: EventType,
        leadId: String? = null,
        projectId: String? = null
    ): CalendarEvent {
        Log.d(TAG, "Scheduling event: $title at $startTime")

        // Create calendar event
        val event = CalendarEvent(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            location = location,
            type = eventType,
            leadId = leadId,
            projectId = projectId
        )

        // Save to repository
        calendarEventRepository.saveEvent(event)

        // If this is for a lead, notify them
        if (leadId != null) {
            val lead = leadRepository.getById(leadId)
            if (lead != null) {
                notificationService.sendEventConfirmation(lead.email, lead.phone, event)
            }
        }

        return event
    }

    /**
     * Take a message
     */
    fun takeMessage(
        from: String,
        content: String,
        phoneNumber: String? = null,
        email: String? = null,
        leadId: String? = null
    ): Message {
        Log.d(TAG, "Taking message from: $from")

        // Create message
        val message = Message(
            id = UUID.randomUUID().toString(),
            from = from,
            content = content,
            timestamp = Date(),
            phoneNumber = phoneNumber,
            email = email,
            leadId = leadId,
            isRead = false
        )

        // Save to repository
        messageRepository.saveMessage(message)

        return message
    }

    /**
     * Place an order
     */
    fun placeOrder(
        items: List<String>,
        supplier: String,
        urgency: String,
        projectId: String? = null,
        notes: String? = null
    ): OrderRequest {
        Log.d(TAG, "Placing order with: $supplier for items: $items")

        // Create order request
        val order = OrderRequest(
            id = UUID.randomUUID().toString(),
            items = items,
            supplier = supplier,
            urgency = urgency,
            timestamp = Date(),
            status = "Pending",
            projectId = projectId,
            notes = notes
        )

        // Save to repository
        orderRepository.saveOrder(order)

        return order
    }

    /**
     * Qualify a lead based on conversation
     */
    fun qualifyLead(
        name: String,
        phoneNumber: String,
        email: String? = null,
        projectType: String? = null,
        budget: String? = null,
        timeline: String? = null,
        notes: String? = null
    ): String {
        Log.d(TAG, "Qualifying lead: $name")

        // Check if lead already exists
        val existingLead = leadRepository.findLeadByPhone(phoneNumber)

        if (existingLead != null) {
            // Update existing lead with new information
            Log.d(TAG, "Lead already exists, updating with new information")

            // In a real implementation, we would update the lead here

            return "Lead updated with new information"
        } else {
            // Create new lead
            Log.d(TAG, "Creating new lead")

            // In a real implementation, we would create a new lead here

            return "New lead created"
        }
    }

    /**
     * Handle scheduling commands
     */
    private fun handleSchedulingCommand(command: String): String {
        // Extract details from command
        val titleMatch = Regex("(?:schedule|meeting|appointment)\\s+(?:with|for)?\\s+([A-Za-z\\s]+)").find(command)
        val title = titleMatch?.groupValues?.get(1)?.trim() ?: "Meeting"

        val dateMatch = Regex("on\\s+(\\d{1,2}/\\d{1,2}(?:/\\d{2,4})?)").find(command)
        val dateStr = dateMatch?.groupValues?.get(1) ?: "tomorrow"

        val timeMatch = Regex("at\\s+(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm))").find(command)
        val timeStr = timeMatch?.groupValues?.get(1) ?: "9:00 am"

        // In a real implementation, we would parse these strings into actual Date objects
        // and create a calendar event

        return "I've scheduled a meeting titled '$title' for $dateStr at $timeStr. Would you like me to send a confirmation to the attendees?"
    }

    /**
     * Handle call commands
     */
    private fun handleCallCommand(command: String): String {
        // Extract details from command
        val nameMatch = Regex("(?:call|phone)\\s+([A-Za-z\\s]+)").find(command)
        val name = nameMatch?.groupValues?.get(1)?.trim() ?: "the client"

        return if (command.contains("reschedule") || command.contains("delay")) {
            "I'll call $name to inform them about the reschedule/delay. What specific information should I convey?"
        } else if (command.contains("follow up") || command.contains("followup")) {
            "I'll call $name to follow up. Would you like me to ask about their decision on the estimate?"
        } else if (command.contains("sub") || command.contains("contractor")) {
            "I'll call $name (subcontractor). What specific information do you need from them?"
        } else {
            "I'll place a call to $name. What would you like me to discuss with them?"
        }
    }

    /**
     * Handle message commands
     */
    private fun handleMessageCommand(command: String): String {
        // Extract details from command
        val contentMatch = Regex("message:?\\s+(.+)").find(command)
        val content = contentMatch?.groupValues?.get(1)?.trim() ?: "No message content provided"

        val fromMatch = Regex("from\\s+([A-Za-z\\s]+)").find(command)
        val from = fromMatch?.groupValues?.get(1)?.trim() ?: "Unknown caller"

        // In a real implementation, we would create a message object and save it

        return "I've recorded a message from $from: \"$content\". Would you like me to flag this as important?"
    }

    /**
     * Handle order commands
     */
    private fun handleOrderCommand(command: String): String {
        // Extract details from command
        val itemsMatch = Regex("order\\s+(.+?)(?:from|for|$)").find(command)
        val items = itemsMatch?.groupValues?.get(1)?.trim() ?: "materials"

        val supplierMatch = Regex("from\\s+([A-Za-z\\s]+)").find(command)
        val supplier = supplierMatch?.groupValues?.get(1)?.trim() ?: "the usual supplier"

        // In a real implementation, we would create an order and place it

        return "I'll order $items from $supplier. Would you like me to request expedited shipping?"
    }

    /**
     * Handle follow-up commands
     */
    private fun handleFollowUpCommand(command: String): String {
        // Extract details from command
        val nameMatch = Regex("(?:follow up|followup)\\s+(?:with)?\\s+([A-Za-z\\s]+)").find(command)
        val name = nameMatch?.groupValues?.get(1)?.trim() ?: "the client"

        return if (command.contains("estimate")) {
            "I'll follow up with $name about their estimate. I'll ask if they have any questions or hesitations, and if they're ready to move forward."
        } else if (command.contains("schedule") || command.contains("meeting")) {
            "I'll follow up with $name to schedule a meeting. What times are you available?"
        } else {
            "I'll follow up with $name. Would you like me to ask about their project status or if they have any questions?"
        }
    }

    /**
     * Handle lead qualification commands
     */
    private fun handleLeadQualificationCommand(command: String): String {
        // Extract details from command
        val nameMatch = Regex("(?:qualify|lead)\\s+([A-Za-z\\s]+)").find(command)
        val name = nameMatch?.groupValues?.get(1)?.trim() ?: "the potential client"

        return "I'll help qualify $name as a lead. I'll ask about their project type, budget, timeline, and decision-making process. Would you like me to schedule a follow-up call with them as well?"
    }

    /**
     * Handle notification commands
     */
    private fun handleNotificationCommand(command: String): String {
        // Extract details from command
        val nameMatch = Regex("(?:notify|inform)\\s+([A-Za-z\\s]+)").find(command)
        val name = nameMatch?.groupValues?.get(1)?.trim() ?: "the client"

        return if (command.contains("delay") || command.contains("late")) {
            "I'll notify $name about the delay. How long is the expected delay, and would you like to offer any accommodations?"
        } else if (command.contains("reschedule")) {
            "I'll notify $name about the need to reschedule. What new times are you available?"
        } else if (command.contains("complete") || command.contains("finished")) {
            "I'll notify $name that the work is complete. Would you like me to schedule a final walkthrough?"
        } else {
            "I'll notify $name. What specific information would you like me to convey?"
        }
    }

    /**
     * Initialize text-to-speech engine
     */
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                } else {
                    isTtsReady = true
                    textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}

                        override fun onDone(utteranceId: String?) {}

                        override fun onError(utteranceId: String?) {}
                    })
                }
            } else {
                Log.e(TAG, "TTS initialization failed")
            }
        }
    }

    /**
     * Speak text using text-to-speech
     */
    private fun speakText(text: String) {
        if (isTtsReady) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
        } else {
            Log.e(TAG, "TTS not ready")
        }
    }

    /**
     * Initialize with basic patterns
     */
    private fun initializePatterns() {
        learnedPatterns["(?:how|what)\\s+(?:\\w+\\s+){0,3}schedule\\s+(?:\\w+\\s+){0,3}meeting"] = 
            "To schedule a meeting, you can say:\n" +
            "1. \"Schedule a meeting with [name] on [date] at [time]\"\n" +
            "2. \"Set up an appointment with [name] for [purpose]\"\n" +
            "3. \"Book a service call for [client] on [date]\""

        learnedPatterns["(?:how|what)\\s+(?:\\w+\\s+){0,3}take\\s+(?:\\w+\\s+){0,3}message"] = 
            "To take a message, you can say:\n" +
            "1. \"Take a message from [name]: [message content]\"\n" +
            "2. \"Record a note from [caller]\"\n" +
            "3. \"Save this message: [content]\""

        learnedPatterns["(?:how|what)\\s+(?:\\w+\\s+){0,3}order\\s+(?:\\w+\\s+){0,3}materials"] = 
            "To order materials, you can say:\n" +
            "1. \"Order [items] from [supplier]\"\n" +
            "2. \"Purchase [quantity] of [item] for [project]\"\n" +
            "3. \"Get [items] delivered by [date]\""
    }
}
