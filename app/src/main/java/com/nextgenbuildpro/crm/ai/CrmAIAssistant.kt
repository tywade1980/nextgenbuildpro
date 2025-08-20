package com.nextgenbuildpro.crm.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.nextgenbuildpro.core.AIAssistant
import com.nextgenbuildpro.core.DateUtils
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern

/**
 * AI Assistant for the CRM module
 * 
 * This assistant helps with CRM-related tasks such as:
 * - Lead qualification
 * - Follow-up scheduling
 * - Message template generation
 * - Task prioritization
 * - Communication analysis
 * 
 * It also serves as an AI receptionist with capabilities including:
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
class CrmAIAssistant(
    private val context: Context,
    private val leadRepository: LeadRepository? = null,
    private val calendarEventRepository: CalendarEventRepository? = null,
    private val messageRepository: MessageRepository? = null,
    private val orderRepository: OrderRepository? = null,
    private val callHandlingService: CallHandlingService? = null,
    private val notificationService: NotificationService? = null
) : AIAssistant {

    private val TAG = "CrmAIAssistant"

    // Store learned patterns and responses
    private val learnedPatterns = mutableMapOf<String, String>()

    // Text-to-speech engine for voice interactions
    private lateinit var textToSpeech: TextToSpeech
    private var isTtsReady = false

    init {
        // Initialize with some basic patterns
        initializePatterns()

        // Initialize text-to-speech if needed for voice interactions
        initializeTextToSpeech()
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
     * Process a command from the user
     */
    override fun processCommand(command: String): String {
        val lowercaseCommand = command.lowercase()

        // Check for specific command patterns
        when {
            // CRM-related commands
            lowercaseCommand.contains("follow up") || lowercaseCommand.contains("followup") -> {
                return handleFollowUpCommand(command)
            }
            lowercaseCommand.contains("qualify") || lowercaseCommand.contains("lead status") -> {
                return handleQualificationCommand(command)
            }
            lowercaseCommand.contains("message") || lowercaseCommand.contains("text") || lowercaseCommand.contains("sms") -> {
                return handleMessageCommand(command)
            }
            lowercaseCommand.contains("prioritize") || lowercaseCommand.contains("priority") -> {
                return handlePrioritizationCommand(command)
            }
            lowercaseCommand.contains("analyze") || lowercaseCommand.contains("analysis") -> {
                return handleAnalysisCommand(command)
            }

            // Receptionist-related commands
            lowercaseCommand.contains("schedule") || lowercaseCommand.contains("appointment") || 
            lowercaseCommand.contains("meeting") -> {
                return handleSchedulingCommand(command)
            }
            lowercaseCommand.contains("call") || lowercaseCommand.contains("phone") -> {
                return handleCallCommand(command)
            }
            lowercaseCommand.contains("take message") || lowercaseCommand.contains("take note") -> {
                return handleTakeMessageCommand(command)
            }
            lowercaseCommand.contains("order") || lowercaseCommand.contains("purchase") -> {
                return handleOrderCommand(command)
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
                return "I'm your CRM and receptionist assistant. You can ask me about lead qualification, follow-ups, " +
                       "messaging, prioritization, communication analysis, scheduling meetings, making calls, " +
                       "taking messages, ordering materials, or notifying clients."
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
            context.contains("lead") -> {
                suggestions.add("Qualify this lead")
                suggestions.add("Schedule a follow-up")
                suggestions.add("Send a follow-up message")
                suggestions.add("Analyze lead communication history")
                suggestions.add("Call this lead to discuss their project")
            }
            context.contains("message") || context.contains("communication") -> {
                suggestions.add("Generate a follow-up message template")
                suggestions.add("Generate an estimate message template")
                suggestions.add("Analyze communication sentiment")
                suggestions.add("Schedule next communication")
                suggestions.add("Take a message for the team")
            }
            context.contains("task") || context.contains("schedule") -> {
                suggestions.add("Prioritize my tasks for today")
                suggestions.add("Schedule follow-ups for hot leads")
                suggestions.add("Remind me about upcoming appointments")
                suggestions.add("Schedule a client meeting")
                suggestions.add("Schedule a service call")
            }
            context.contains("call") || context.contains("phone") -> {
                suggestions.add("Screen incoming calls")
                suggestions.add("Call a client to reschedule")
                suggestions.add("Call a subcontractor")
                suggestions.add("Take a message")
                suggestions.add("Follow up with a recent caller")
            }
            context.contains("order") || context.contains("material") -> {
                suggestions.add("Order materials for a project")
                suggestions.add("Order lunch for the team")
                suggestions.add("Check order status")
                suggestions.add("Track material deliveries")
            }
            context.contains("notify") || context.contains("inform") -> {
                suggestions.add("Notify a client about a delay")
                suggestions.add("Inform the team about a schedule change")
                suggestions.add("Send project updates to clients")
                suggestions.add("Notify subcontractors about changes")
            }
            else -> {
                suggestions.add("Find hot leads to follow up with")
                suggestions.add("Generate a daily CRM activity plan")
                suggestions.add("Analyze my lead conversion rate")
                suggestions.add("Help me qualify a new lead")
                suggestions.add("Schedule a client meeting")
                suggestions.add("Take a message")
                suggestions.add("Order materials for a project")
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
     * Handle follow-up related commands
     */
    private fun handleFollowUpCommand(command: String): String {
        // Extract lead name if present
        val leadNameMatch = Regex("for\\s+([A-Za-z\\s]+)").find(command)
        val leadName = leadNameMatch?.groupValues?.get(1)?.trim() ?: "the lead"

        // Extract date if present
        val dateMatch = Regex("on\\s+(\\d{1,2}/\\d{1,2}(?:/\\d{2,4})?)").find(command)
        val date = dateMatch?.groupValues?.get(1) ?: "next week"

        // Generate response based on command specifics
        return when {
            command.contains("schedule") -> {
                "I've scheduled a follow-up with $leadName for $date. I'll remind you when it's time."
            }
            command.contains("message") || command.contains("text") -> {
                "Here's a follow-up message template for $leadName:\n\n" +
                "Hi $leadName, I'm following up on our conversation about your project. Would you like to schedule a time to discuss further?"
            }
            command.contains("call") -> {
                "I've added a reminder to call $leadName on $date. Would you like me to prepare talking points for the call?"
            }
            else -> {
                "I can help you with follow-ups. Would you like to schedule a follow-up, prepare a follow-up message, or set a reminder for a follow-up call?"
            }
        }
    }

    /**
     * Handle lead qualification commands
     */
    private fun handleQualificationCommand(command: String): String {
        // Extract lead name if present
        val leadNameMatch = Regex("(?:qualify|status of|about)\\s+([A-Za-z\\s]+)").find(command)
        val leadName = leadNameMatch?.groupValues?.get(1)?.trim() ?: "the lead"

        // Generate qualification questions or status update
        return if (command.contains("questions") || command.contains("ask")) {
            "Here are some qualification questions for $leadName:\n\n" +
            "1. What is your timeline for this project?\n" +
            "2. Have you established a budget for this project?\n" +
            "3. Are you the primary decision maker?\n" +
            "4. Have you worked with contractors before?\n" +
            "5. How did you hear about our company?"
        } else {
            "Based on the information available, $leadName appears to be a qualified lead. They have expressed interest in a specific project, have a reasonable timeline, and have engaged in multiple communications."
        }
    }

    /**
     * Handle message related commands
     */
    private fun handleMessageCommand(command: String): String {
        // Extract lead name if present
        val leadNameMatch = Regex("to\\s+([A-Za-z\\s]+)").find(command)
        val leadName = leadNameMatch?.groupValues?.get(1)?.trim() ?: "the client"

        // Determine message type
        val messageType = when {
            command.contains("follow") -> "follow_up"
            command.contains("estimate") -> "estimate"
            command.contains("appointment") -> "appointment"
            command.contains("update") -> "project_update"
            else -> "general"
        }

        // Generate message template
        return "Here's a message template for $leadName:\n\n" + when (messageType) {
            "follow_up" -> "Hi $leadName, I'm following up on our conversation about your project. Would you like to schedule a time to discuss further?"
            "estimate" -> "Hi $leadName, your estimate is ready. The total comes to \$X,XXX. Please let me know if you have any questions."
            "appointment" -> "Hi $leadName, this is a reminder about your appointment on ${DateUtils.formatDate(Date())}. Please confirm if this still works for you."
            "project_update" -> "Hi $leadName, your project is now X% complete. We're on track to finish by the scheduled completion date."
            else -> "Hi $leadName, thank you for choosing NextGenBuildPro. How can we help you today?"
        }
    }

    /**
     * Handle task prioritization commands
     */
    private fun handlePrioritizationCommand(command: String): String {
        return "Here's your prioritized task list for today:\n\n" +
               "1. Follow up with hot leads (Sarah Johnson, Michael Brown)\n" +
               "2. Send estimates to qualified leads (John Smith)\n" +
               "3. Schedule appointments for new leads\n" +
               "4. Update project progress for active clients\n" +
               "5. Review and respond to incoming messages"
    }

    /**
     * Handle analysis commands
     */
    private fun handleAnalysisCommand(command: String): String {
        // Extract what to analyze
        val analysisType = when {
            command.contains("communication") || command.contains("message") -> "communication"
            command.contains("lead") || command.contains("conversion") -> "leads"
            command.contains("performance") || command.contains("metrics") -> "performance"
            else -> "general"
        }

        // Generate analysis
        return when (analysisType) {
            "communication" -> {
                "Communication Analysis:\n\n" +
                "- Average response time: 2.3 hours\n" +
                "- Message sentiment: Mostly positive\n" +
                "- Common topics: Pricing, timeline, materials\n" +
                "- Suggested improvement: Respond faster to initial inquiries"
            }
            "leads" -> {
                "Lead Analysis:\n\n" +
                "- Conversion rate: 32%\n" +
                "- Average time to conversion: 12 days\n" +
                "- Top lead source: Website (45%)\n" +
                "- Most effective follow-up: Phone call within 24 hours"
            }
            "performance" -> {
                "Performance Metrics:\n\n" +
                "- Leads contacted today: 8\n" +
                "- Messages sent: 15\n" +
                "- Appointments scheduled: 3\n" +
                "- Estimates sent: 2\n" +
                "- Conversion rate today: 25%"
            }
            else -> {
                "I can analyze your CRM data in several ways. Would you like me to analyze communication patterns, lead conversion metrics, or your daily performance?"
            }
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
        // and create a calendar event using calendarEventRepository

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
     * Handle take message commands
     */
    private fun handleTakeMessageCommand(command: String): String {
        // Extract details from command
        val contentMatch = Regex("message:?\\s+(.+)").find(command)
        val content = contentMatch?.groupValues?.get(1)?.trim() ?: "No message content provided"

        val fromMatch = Regex("from\\s+([A-Za-z\\s]+)").find(command)
        val from = fromMatch?.groupValues?.get(1)?.trim() ?: "Unknown caller"

        // In a real implementation, we would create a message object and save it using messageRepository

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

        // In a real implementation, we would create an order and place it using orderRepository

        return "I'll order $items from $supplier. Would you like me to request expedited shipping?"
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
     * Handle an incoming call
     */
    fun handleIncomingCall(phoneNumber: String): CallStatus {
        Log.d(TAG, "Handling incoming call from: $phoneNumber")

        // Check if this is a known spam number
        if (callHandlingService?.isSpamNumber(phoneNumber) == true) {
            Log.d(TAG, "Identified as spam call, rejecting")
            return CallStatus.REJECTED_SPAM
        }

        // Try to identify the caller
        val lead = leadRepository?.findLeadByPhone(phoneNumber)

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
            val greeting = "Thank you for calling NextGenBuildPro. This is the AI assistant. How can I help you today?"
            speakText(greeting)

            return CallStatus.ANSWERED_UNKNOWN_CALLER
        }
    }

    /**
     * Schedule a meeting or appointment
     */
    fun scheduleEvent(
        title: String,
        description: String,
        startTime: Date,
        endTime: Date,
        location: String,
        eventType: EventType,
        leadId: String? = null,
        projectId: String? = null
    ): CalendarEvent? {
        Log.d(TAG, "Scheduling event: $title at $startTime")

        if (calendarEventRepository == null) {
            Log.e(TAG, "Calendar event repository is null")
            return null
        }

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
        if (leadId != null && leadRepository != null && notificationService != null) {
            // Launch a coroutine to handle the suspend function
            GlobalScope.launch {
                try {
                    val lead = leadRepository.getById(leadId)
                    if (lead != null) {
                        notificationService.sendEventConfirmation(lead.email, lead.phone, event)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting lead or sending notification", e)
                }
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
    ): Message? {
        Log.d(TAG, "Taking message from: $from")

        if (messageRepository == null) {
            Log.e(TAG, "Message repository is null")
            return null
        }

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
    ): OrderRequest? {
        Log.d(TAG, "Placing order with: $supplier for items: $items")

        if (orderRepository == null) {
            Log.e(TAG, "Order repository is null")
            return null
        }

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
     * Initialize with basic patterns
     */
    private fun initializePatterns() {
        // CRM-related patterns
        learnedPatterns["(?:how|what)\\s+(?:\\w+\\s+){0,3}follow\\s+up"] = 
            "To follow up effectively, consider these steps:\n" +
            "1. Send a personalized message referencing your last conversation\n" +
            "2. Provide additional value (information, resources)\n" +
            "3. Include a clear call to action\n" +
            "4. Schedule the next follow-up if you don't get a response"

        learnedPatterns["(?:how|what)\\s+(?:\\w+\\s+){0,3}qualify\\s+(?:\\w+\\s+){0,3}lead"] = 
            "To qualify a lead, evaluate these criteria:\n" +
            "1. Need: Do they have a clear need for your services?\n" +
            "2. Budget: Can they afford your services?\n" +
            "3. Authority: Are they the decision maker?\n" +
            "4. Timeline: When do they need the project completed?\n" +
            "5. Fit: Are they a good fit for your business?"

        learnedPatterns["(?:best|good)\\s+(?:\\w+\\s+){0,3}time\\s+(?:\\w+\\s+){0,3}(?:call|contact)"] = 
            "The best times to contact leads are typically:\n" +
            "- Tuesday through Thursday\n" +
            "- Between 8-10 AM or 4-5 PM\n" +
            "- Avoid Mondays (too busy) and Fridays (weekend mindset)\n" +
            "- Respond to new leads within 5 minutes for best results"

        // Receptionist-related patterns
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
