package com.nextgenbuildpro.crm

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nextgenbuildpro.crm.ai.CrmAIAssistant
import com.nextgenbuildpro.crm.data.repository.LeadRepository
import com.nextgenbuildpro.crm.data.repository.LeadRepositoryAdapter
import com.nextgenbuildpro.crm.data.repository.MessageRepository
import com.nextgenbuildpro.crm.data.repository.PhotoRepository
import com.nextgenbuildpro.features.leads.di.LeadRepositoryModule
import com.nextgenbuildpro.crm.service.CallMonitorService
import com.nextgenbuildpro.crm.service.PhotoMonitorService
import com.nextgenbuildpro.crm.viewmodel.LeadsViewModel
import com.nextgenbuildpro.crm.viewmodel.MessagesViewModel
import com.nextgenbuildpro.receptionist.repository.CalendarEventRepository
import com.nextgenbuildpro.receptionist.repository.MessageRepository as ReceptionistMessageRepository
import com.nextgenbuildpro.receptionist.repository.OrderRepository
import com.nextgenbuildpro.receptionist.service.CallHandlingService
import com.nextgenbuildpro.receptionist.service.NotificationService

/**
 * CRM Module for NextGenBuildPro
 * 
 * This module handles all Customer Relationship Management functionality:
 * - Leads management
 * - Communication (calls, messages)
 * - Follow-ups and scheduling
 * - CRM-specific AI assistant
 */
object CrmModule {
    private var initialized = false
    private lateinit var leadRepository: LeadRepository
    private lateinit var messageRepository: MessageRepository
    private lateinit var photoRepository: PhotoRepository
    private lateinit var crmAIAssistant: CrmAIAssistant

    // Receptionist repositories and services
    private lateinit var calendarEventRepository: CalendarEventRepository
    private lateinit var receptionistMessageRepository: ReceptionistMessageRepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var callHandlingService: CallHandlingService
    private lateinit var notificationService: NotificationService

    /**
     * Check if the module is already initialized
     */
    fun isInitialized(): Boolean {
        return initialized
    }

    /**
     * Initialize the CRM module
     */
    fun initialize(context: Context) {
        if (initialized) return

        // Initialize CRM repositories
        leadRepository = LeadRepository()
        messageRepository = MessageRepository()
        photoRepository = PhotoRepository(context)

        // Initialize receptionist repositories and services
        calendarEventRepository = CalendarEventRepository()
        receptionistMessageRepository = ReceptionistMessageRepository()
        orderRepository = OrderRepository()
        callHandlingService = CallHandlingService(context)
        notificationService = NotificationService(context)

        // Initialize AI assistant with all repositories and services
        crmAIAssistant = CrmAIAssistant(
            context = context,
            leadRepository = leadRepository,
            calendarEventRepository = calendarEventRepository,
            messageRepository = receptionistMessageRepository,
            orderRepository = orderRepository,
            callHandlingService = callHandlingService,
            notificationService = notificationService
        )

        // Completely disable all background services to prevent excessive system activity
        // PhotoMonitorService.scanPhotos(context)
        // CallMonitorService.startService(context)

        initialized = true
    }

    /**
     * Get the lead repository
     */
    fun getLeadRepository(): LeadRepository {
        checkInitialized()
        return leadRepository
    }

    /**
     * Get the message repository
     */
    fun getMessageRepository(): MessageRepository {
        checkInitialized()
        return messageRepository
    }

    /**
     * Get the CRM AI assistant
     */
    fun getAIAssistant(): CrmAIAssistant {
        checkInitialized()
        return crmAIAssistant
    }

    /**
     * Get the photo repository
     */
    fun getPhotoRepository(): PhotoRepository {
        checkInitialized()
        return photoRepository
    }

    /**
     * Get the calendar event repository
     */
    fun getCalendarEventRepository(): CalendarEventRepository {
        checkInitialized()
        return calendarEventRepository
    }

    /**
     * Get the receptionist message repository
     */
    fun getReceptionistMessageRepository(): ReceptionistMessageRepository {
        checkInitialized()
        return receptionistMessageRepository
    }

    /**
     * Get the order repository
     */
    fun getOrderRepository(): OrderRepository {
        checkInitialized()
        return orderRepository
    }

    /**
     * Get the call handling service
     */
    fun getCallHandlingService(): CallHandlingService {
        checkInitialized()
        return callHandlingService
    }

    /**
     * Get the notification service
     */
    fun getNotificationService(): NotificationService {
        checkInitialized()
        return notificationService
    }

    /**
     * Create a new leads view model
     */
    fun createLeadsViewModel(): LeadsViewModel {
        checkInitialized()
        return LeadsViewModel(leadRepository)
    }

    /**
     * Create a new messages view model
     */
    fun createMessagesViewModel(): MessagesViewModel {
        checkInitialized()
        return MessagesViewModel(messageRepository)
    }

    /**
     * Trigger a photo scan (trigger-based approach)
     * This will scan for new photos once and then stop
     */
    fun triggerPhotoScan(context: Context) {
        checkInitialized()
        PhotoMonitorService.scanPhotos(context)
    }

    /**
     * Trigger a call log check (trigger-based approach)
     * This will check for new calls once and then stop
     */
    fun triggerCallCheck(context: Context) {
        checkInitialized()
        CallMonitorService.checkCalls(context)
    }

    /**
     * Check if the module is initialized
     */
    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("CRM Module is not initialized. Call initialize() first.")
        }
    }
}

/**
 * Composable function to remember CRM module components
 */
@Composable
fun rememberCrmComponents(): CrmComponents {
    val context = LocalContext.current

    // Initialize module if needed
    if (!CrmModule.isInitialized()) {
        CrmModule.initialize(context)
    }

    // Create view models
    val leadsViewModel = remember { CrmModule.createLeadsViewModel() }
    val messagesViewModel = remember { CrmModule.createMessagesViewModel() }

    return CrmComponents(
        leadsViewModel = leadsViewModel,
        messagesViewModel = messagesViewModel,
        aiAssistant = CrmModule.getAIAssistant(),
        photoRepository = CrmModule.getPhotoRepository(),
        calendarEventRepository = CrmModule.getCalendarEventRepository(),
        receptionistMessageRepository = CrmModule.getReceptionistMessageRepository(),
        orderRepository = CrmModule.getOrderRepository(),
        callHandlingService = CrmModule.getCallHandlingService(),
        notificationService = CrmModule.getNotificationService()
    )
}

/**
 * Data class to hold CRM components
 */
data class CrmComponents(
    val leadsViewModel: LeadsViewModel,
    val messagesViewModel: MessagesViewModel,
    val aiAssistant: CrmAIAssistant,
    val photoRepository: PhotoRepository,
    val calendarEventRepository: CalendarEventRepository,
    val receptionistMessageRepository: ReceptionistMessageRepository,
    val orderRepository: OrderRepository,
    val callHandlingService: CallHandlingService,
    val notificationService: NotificationService
)
