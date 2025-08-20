package com.nextgenbuildpro.receptionist

import android.content.Context
import java.time.LocalTime

/**
 * Receptionist Module for NextGenBuildPro
 * 
 * This module manages the receptionist functionality:
 * - Handles telecom activities
 * - Manages incoming calls during business hours (7am to 5pm)
 * - Provides front-end interface for users to interact with the system
 * - Handles scheduling and appointment management
 * - Conducts lead qualifying questions
 * - Manages material ordering
 * - Handles lunch ordering for the team
 * - Executes task logic tree operations
 */
object ReceptionistModule {
    private var initialized = false
    private lateinit var context: Context
    private lateinit var receptionist: Receptionist

    // Business hours
    private val businessHoursStart = LocalTime.of(7, 0) // 7:00 AM
    private val businessHoursEnd = LocalTime.of(17, 0) // 5:00 PM

    /**
     * Initialize the Receptionist module
     */
    fun initialize(context: Context) {
        if (initialized) return

        this.context = context

        // Initialize repositories and services
        val leadRepository = com.nextgenbuildpro.crm.CrmModule.getLeadRepository()
        val calendarEventRepository = com.nextgenbuildpro.receptionist.repository.CalendarEventRepository()
        val messageRepository = com.nextgenbuildpro.receptionist.repository.MessageRepository()
        val orderRepository = com.nextgenbuildpro.receptionist.repository.OrderRepository()
        val callHandlingService = com.nextgenbuildpro.receptionist.service.CallHandlingService(context)
        val notificationService = com.nextgenbuildpro.receptionist.service.NotificationService(context)
        val aiModule = com.nextgenbuildpro.ai.AIModule

        // Initialize receptionist
        receptionist = ReceptionistImpl(
            context = context,
            leadRepository = leadRepository,
            calendarEventRepository = calendarEventRepository,
            messageRepository = messageRepository,
            orderRepository = orderRepository,
            callHandlingService = callHandlingService,
            notificationService = notificationService,
            aiModule = aiModule
        )

        initialized = true
    }

    /**
     * Get the receptionist instance
     */
    fun getReceptionist(): Receptionist {
        checkInitialized()
        return receptionist
    }

    /**
     * Check if current time is within business hours
     */
    fun isWithinBusinessHours(): Boolean {
        val currentTime = LocalTime.now()
        return !currentTime.isBefore(businessHoursStart) && !currentTime.isAfter(businessHoursEnd)
    }

    /**
     * Check if the module is initialized
     */
    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("ReceptionistModule is not initialized. Call initialize() first.")
        }
    }

    /**
     * Define the receptionist's role as telecom manager and administrative assistant
     */
    interface Receptionist {
        /**
         * Handle an incoming call
         * Only processes calls during business hours (7am to 5pm)
         */
        fun handleIncomingCall(call: Call)

        /**
         * Schedule an appointment
         */
        fun scheduleAppointment(request: AppointmentRequest)

        /**
         * Notify a team member
         */
        fun notifyTeamMember(notification: Notification)

        /**
         * Ask lead qualifying questions and record responses
         */
        fun qualifyLead(leadInfo: LeadQualificationInfo): QualificationResult

        /**
         * Place an order for materials
         */
        fun orderMaterials(orderRequest: MaterialOrderRequest): OrderConfirmation

        /**
         * Order lunch for the team
         */
        fun orderLunch(lunchOrder: LunchOrderRequest): OrderConfirmation

        /**
         * Execute a task based on the task logic tree
         */
        fun executeTask(taskRequest: TaskRequest): TaskResult
    }

    /**
     * Model classes for Receptionist module
     */
    data class Call(
        val id: String,
        val callerNumber: String,
        val callerName: String?,
        val timestamp: Long,
        val status: CallStatus
    )

    enum class CallStatus {
        INCOMING,
        IN_PROGRESS,
        COMPLETED,
        MISSED,
        REJECTED
    }

    data class AppointmentRequest(
        val id: String,
        val clientName: String,
        val clientContact: String,
        val requestedDate: String,
        val requestedTime: String,
        val purpose: String
    )

    data class Notification(
        val id: String,
        val recipientId: String,
        val message: String,
        val priority: NotificationPriority,
        val timestamp: Long
    )

    enum class NotificationPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    /**
     * Lead qualification information
     */
    data class LeadQualificationInfo(
        val name: String,
        val contactInfo: String,
        val projectType: String,
        val budget: String?,
        val timeline: String?,
        val additionalNotes: String?
    )

    /**
     * Result of lead qualification
     */
    data class QualificationResult(
        val qualified: Boolean,
        val score: Int,
        val recommendedFollowUp: String,
        val assignedTo: String?
    )

    /**
     * Material order request
     */
    data class MaterialOrderRequest(
        val projectId: String?,
        val items: List<OrderItem>,
        val supplier: String,
        val deliveryDate: String,
        val priority: OrderPriority,
        val notes: String?
    )

    /**
     * Lunch order request
     */
    data class LunchOrderRequest(
        val date: String,
        val time: String,
        val restaurant: String,
        val items: List<OrderItem>,
        val teamMembers: List<String>,
        val notes: String?
    )

    /**
     * Order item
     */
    data class OrderItem(
        val name: String,
        val quantity: Int,
        val unitPrice: Double?,
        val notes: String?
    )

    /**
     * Order priority
     */
    enum class OrderPriority {
        STANDARD,
        EXPEDITED,
        URGENT
    }

    /**
     * Order confirmation
     */
    data class OrderConfirmation(
        val orderId: String,
        val status: String,
        val estimatedDelivery: String,
        val totalCost: Double?,
        val confirmationDetails: String
    )

    /**
     * Task request for the task logic tree
     */
    data class TaskRequest(
        val taskType: TaskType,
        val parameters: Map<String, String>,
        val priority: TaskPriority,
        val dueDate: String?
    )

    /**
     * Task types for the task logic tree
     */
    enum class TaskType {
        SCHEDULE_FOLLOW_UP,
        SEND_ESTIMATE,
        PREPARE_CONTRACT,
        ORDER_MATERIALS,
        COORDINATE_SUBCONTRACTORS,
        PROCESS_PAYMENT,
        UPDATE_CLIENT,
        QUALITY_CHECK
    }

    /**
     * Task priority
     */
    enum class TaskPriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Result of task execution
     */
    data class TaskResult(
        val successful: Boolean,
        val taskId: String?,
        val message: String,
        val nextSteps: List<String>?
    )
}
