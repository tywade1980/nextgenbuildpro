package com.aireceptionist.app.ai.agents.impl

import com.aireceptionist.app.ai.agents.*
import com.aireceptionist.app.api.ExternalAPIManager
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Agent responsible for external system integrations and API calls
 */
class IntegrationAgent @Inject constructor(
    private val apiManager: ExternalAPIManager
) : Agent {
    
    override val agentId = "integration_agent"
    override val agentName = "Integration Management Agent"
    override val capabilities = listOf(
        AgentCapability.INTEGRATION_MANAGEMENT,
        AgentCapability.CONTEXT_AWARENESS
    )
    override val priority = 4
    
    private var isInitialized = false
    
    override suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.i(TAG, "Initializing Integration Agent")
            
            apiManager.initialize()
            
            isInitialized = true
            Logger.i(TAG, "Integration Agent initialized successfully")
            true
            
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize Integration Agent", e)
            false
        }
    }
    
    override suspend fun processInput(input: AgentInput): AgentResponse = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            return@withContext createErrorResponse("Agent not initialized")
        }
        
        when (input.type) {
            InputType.SYSTEM_EVENT -> processIntegrationRequest(input)
            else -> createErrorResponse("Unsupported input type: ${input.type}")
        }
    }
    
    private suspend fun processIntegrationRequest(input: AgentInput): AgentResponse {
        return try {
            Logger.d(TAG, "Processing integration request: ${input.content}")
            
            val intent = input.context.intent ?: ""
            val integrationResult = when (intent) {
                "crm_lookup" -> handleCRMLookup(input)
                "calendar_check" -> handleCalendarCheck(input)
                "send_notification" -> handleNotificationSend(input)
                "database_query" -> handleDatabaseQuery(input)
                "webhook_trigger" -> handleWebhookTrigger(input)
                else -> handleGenericIntegration(input)
            }
            
            AgentResponse(
                agentId = agentId,
                responseType = ResponseType.INTEGRATION_CALL,
                content = integrationResult.message,
                confidence = if (integrationResult.isSuccess) 0.9f else 0.3f,
                metadata = mapOf(
                    "integration_type" to intent,
                    "success" to integrationResult.isSuccess,
                    "data" to (integrationResult.data ?: emptyMap<String, Any>())
                )
            )
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error processing integration request", e)
            createErrorResponse("Integration failed: ${e.message}")
        }
    }
    
    private suspend fun handleCRMLookup(input: AgentInput): IntegrationResult {
        val callerNumber = input.context.callerNumber
        return if (callerNumber != null) {
            try {
                val customerData = apiManager.lookupCustomer(callerNumber)
                IntegrationResult(
                    isSuccess = true,
                    message = "Customer information retrieved successfully",
                    data = customerData
                )
            } catch (e: Exception) {
                Logger.w(TAG, "CRM lookup failed", e)
                IntegrationResult(
                    isSuccess = false,
                    message = "Customer lookup failed: ${e.message}"
                )
            }
        } else {
            IntegrationResult(
                isSuccess = false,
                message = "No phone number available for CRM lookup"
            )
        }
    }
    
    private suspend fun handleCalendarCheck(input: AgentInput): IntegrationResult {
        return try {
            val availability = apiManager.checkCalendarAvailability()
            IntegrationResult(
                isSuccess = true,
                message = "Calendar availability retrieved",
                data = mapOf("availability" to availability)
            )
        } catch (e: Exception) {
            Logger.w(TAG, "Calendar check failed", e)
            IntegrationResult(
                isSuccess = false,
                message = "Calendar check failed: ${e.message}"
            )
        }
    }
    
    private suspend fun handleNotificationSend(input: AgentInput): IntegrationResult {
        val metadata = input.metadata
        val recipient = metadata["recipient"] as? String
        val message = metadata["message"] as? String
        val type = metadata["type"] as? String ?: "email"
        
        return if (recipient != null && message != null) {
            try {
                when (type) {
                    "email" -> apiManager.sendEmail(recipient, "AI Receptionist", message)
                    "sms" -> apiManager.sendSMS(recipient, message)
                    "slack" -> apiManager.sendSlackMessage(recipient, message)
                    else -> throw IllegalArgumentException("Unsupported notification type: $type")
                }
                
                IntegrationResult(
                    isSuccess = true,
                    message = "Notification sent successfully"
                )
            } catch (e: Exception) {
                Logger.w(TAG, "Notification send failed", e)
                IntegrationResult(
                    isSuccess = false,
                    message = "Failed to send notification: ${e.message}"
                )
            }
        } else {
            IntegrationResult(
                isSuccess = false,
                message = "Missing recipient or message for notification"
            )
        }
    }
    
    private suspend fun handleDatabaseQuery(input: AgentInput): IntegrationResult {
        val queryType = input.metadata["query_type"] as? String
        val parameters = input.metadata["parameters"] as? Map<String, Any> ?: emptyMap()
        
        return try {
            val result = when (queryType) {
                "customer_history" -> apiManager.getCustomerHistory(parameters)
                "appointment_slots" -> apiManager.getAvailableSlots(parameters)
                "service_catalog" -> apiManager.getServiceCatalog(parameters)
                else -> throw IllegalArgumentException("Unsupported query type: $queryType")
            }
            
            IntegrationResult(
                isSuccess = true,
                message = "Database query completed successfully",
                data = result
            )
        } catch (e: Exception) {
            Logger.w(TAG, "Database query failed", e)
            IntegrationResult(
                isSuccess = false,
                message = "Database query failed: ${e.message}"
            )
        }
    }
    
    private suspend fun handleWebhookTrigger(input: AgentInput): IntegrationResult {
        val webhookUrl = input.metadata["webhook_url"] as? String
        val payload = input.metadata["payload"] as? Map<String, Any> ?: emptyMap()
        
        return if (webhookUrl != null) {
            try {
                apiManager.triggerWebhook(webhookUrl, payload)
                IntegrationResult(
                    isSuccess = true,
                    message = "Webhook triggered successfully"
                )
            } catch (e: Exception) {
                Logger.w(TAG, "Webhook trigger failed", e)
                IntegrationResult(
                    isSuccess = false,
                    message = "Webhook trigger failed: ${e.message}"
                )
            }
        } else {
            IntegrationResult(
                isSuccess = false,
                message = "Webhook URL not provided"
            )
        }
    }
    
    private suspend fun handleGenericIntegration(input: AgentInput): IntegrationResult {
        // Handle generic integration requests
        val apiEndpoint = input.metadata["api_endpoint"] as? String
        val method = input.metadata["method"] as? String ?: "GET"
        val headers = input.metadata["headers"] as? Map<String, String> ?: emptyMap()
        val body = input.metadata["body"] as? Map<String, Any>
        
        return if (apiEndpoint != null) {
            try {
                val result = apiManager.makeAPICall(
                    endpoint = apiEndpoint,
                    method = method,
                    headers = headers,
                    body = body
                )
                
                IntegrationResult(
                    isSuccess = true,
                    message = "API call completed successfully",
                    data = result
                )
            } catch (e: Exception) {
                Logger.w(TAG, "Generic API call failed", e)
                IntegrationResult(
                    isSuccess = false,
                    message = "API call failed: ${e.message}"
                )
            }
        } else {
            IntegrationResult(
                isSuccess = false,
                message = "No API endpoint specified"
            )
        }
    }
    
    override suspend fun shutdown() {
        try {
            Logger.i(TAG, "Shutting down Integration Agent")
            apiManager.shutdown()
            isInitialized = false
            Logger.i(TAG, "Integration Agent shutdown complete")
        } catch (e: Exception) {
            Logger.e(TAG, "Error during shutdown", e)
        }
    }
    
    override fun isHealthy(): Boolean {
        return isInitialized && apiManager.isHealthy()
    }
    
    private fun createErrorResponse(message: String): AgentResponse {
        return AgentResponse(
            agentId = agentId,
            responseType = ResponseType.INTEGRATION_CALL,
            content = message,
            confidence = 0.0f
        )
    }
    
    companion object {
        private const val TAG = "IntegrationAgent"
    }
}

/**
 * Data class for integration results
 */
data class IntegrationResult(
    val isSuccess: Boolean,
    val message: String,
    val data: Map<String, Any>? = null
)