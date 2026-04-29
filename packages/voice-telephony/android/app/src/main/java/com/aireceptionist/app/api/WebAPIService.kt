package com.aireceptionist.app.api

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.aireceptionist.app.ai.agents.AgentManager
import com.aireceptionist.app.data.repository.CallRepository
import com.aireceptionist.app.data.repository.AppointmentRepository
import com.aireceptionist.app.utils.Logger
import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.*
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

/**
 * Service that exposes REST API endpoints for external integrations
 */
@AndroidEntryPoint
class WebAPIService : Service() {
    
    @Inject
    lateinit var agentManager: AgentManager
    
    @Inject
    lateinit var callRepository: CallRepository
    
    @Inject
    lateinit var appointmentRepository: AppointmentRepository
    
    private var httpServer: HttpServer? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val gson = Gson()
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        Logger.i(TAG, "WebAPIService created")
        startAPIServer()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.i(TAG, "WebAPIService started")
        return START_STICKY
    }
    
    private fun startAPIServer() {
        scope.launch {
            try {
                httpServer = HttpServer.create(InetSocketAddress(8080), 0).apply {
                    
                    // Health check endpoint
                    createContext("/api/health", HealthHandler())
                    
                    // Call-related endpoints
                    createContext("/api/calls", CallsHandler())
                    createContext("/api/calls/stats", CallStatsHandler())
                    createContext("/api/calls/search", CallSearchHandler())
                    
                    // Appointment endpoints
                    createContext("/api/appointments", AppointmentsHandler())
                    createContext("/api/appointments/book", BookAppointmentHandler())
                    createContext("/api/appointments/cancel", CancelAppointmentHandler())
                    
                    // AI agent endpoints
                    createContext("/api/agents/status", AgentStatusHandler())
                    createContext("/api/agents/process", ProcessInputHandler())
                    
                    // Webhook endpoints for external systems
                    createContext("/api/webhooks/call-event", CallEventWebhookHandler())
                    createContext("/api/webhooks/appointment", AppointmentWebhookHandler())
                    
                    // Configuration endpoints
                    createContext("/api/config", ConfigHandler())
                    
                    executor = null // Use default executor
                    start()
                }
                
                Logger.i(TAG, "API Server started on port 8080")
                
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to start API server", e)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        httpServer?.stop(0)
        scope.cancel()
        Logger.i(TAG, "WebAPIService destroyed")
    }
    
    // Health check handler
    inner class HealthHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            scope.launch {
                try {
                    val agentHealth = agentManager.getSystemHealth()
                    val response = mapOf(
                        "status" to "healthy",
                        "timestamp" to System.currentTimeMillis(),
                        "agents" to agentHealth,
                        "version" to "1.0.0"
                    )
                    
                    sendJsonResponse(exchange, 200, response)
                } catch (e: Exception) {
                    sendErrorResponse(exchange, 500, "Health check failed")
                }
            }
        }
    }
    
    // Calls handler
    inner class CallsHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            scope.launch {
                try {
                    when (exchange.requestMethod) {
                        "GET" -> {
                            val calls = callRepository.getRecentCallRecords(50)
                            sendJsonResponse(exchange, 200, mapOf("calls" to calls))
                        }
                        else -> {
                            sendErrorResponse(exchange, 405, "Method not allowed")
                        }
                    }
                } catch (e: Exception) {
                    sendErrorResponse(exchange, 500, "Failed to fetch calls")
                }
            }
        }
    }
    
    // Call statistics handler
    inner class CallStatsHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            scope.launch {
                try {
                    val stats = callRepository.getTodaysCallStats()
                    sendJsonResponse(exchange, 200, stats)
                } catch (e: Exception) {
                    sendErrorResponse(exchange, 500, "Failed to fetch call stats")
                }
            }
        }
    }
    
    // Call search handler
    inner class CallSearchHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            scope.launch {
                try {
                    val query = getQueryParameter(exchange, "q") ?: ""
                    val limit = getQueryParameter(exchange, "limit")?.toIntOrNull() ?: 20
                    
                    val results = callRepository.searchCallRecords(query, limit)
                    sendJsonResponse(exchange, 200, mapOf("results" to results))
                } catch (e: Exception) {
                    sendErrorResponse(exchange, 500, "Search failed")
                }
            }
        }
    }
    
    // Appointments handler
    inner class AppointmentsHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            scope.launch {
                try {
                    when (exchange.requestMethod) {
                        "GET" -> {
                            val phone = getQueryParameter(exchange, "phone")
                            if (phone != null) {
                                val appointments = appointmentRepository.getAppointments(phone)
                                sendJsonResponse(exchange, 200, mapOf("appointments" to appointments))
                            } else {
                                val upcoming = appointmentRepository.getUpcomingAppointments()
                                sendJsonResponse(exchange, 200, mapOf("appointments" to upcoming))
                            }
                        }
                        else -> {
                            sendErrorResponse(exchange, 405, "Method not allowed")
                        }
                    }
                } catch (e: Exception) {
                    sendErrorResponse(exchange, 500, "Failed to fetch appointments")
                }
            }
        }
    }
    
    // Book appointment handler
    inner class BookAppointmentHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            scope.launch {
                try {
                    if (exchange.requestMethod == "POST") {
                        val requestBody = exchange.requestBody.bufferedReader().readText()
                        val appointmentRequest = gson.fromJson(requestBody, AppointmentRequest::class.java)
                        
                        // Convert to AppointmentDetails and book
                        val details = com.aireceptionist.app.ai.agents.impl.AppointmentDetails(
                            date = appointmentRequest.date?.let { java.util.Date(it) },
                            time = appointmentRequest.time,
                            serviceType = appointmentRequest.serviceType,
                            customerName = appointmentRequest.customerName,
                            customerPhone = appointmentRequest.customerPhone,
                            notes = appointmentRequest.notes
                        )
                        
                        val result = appointmentRepository.bookAppointment(details)
                        sendJsonResponse(exchange, if (result.isSuccess) 200 else 400, result)
                    } else {
                        sendErrorResponse(exchange, 405, "Method not allowed")
                    }
                } catch (e: Exception) {
                    sendErrorResponse(exchange, 500, "Failed to book appointment")
                }
            }
        }
    }
    
    // Cancel appointment handler
    inner class CancelAppointmentHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            scope.launch {
                try {
                    if (exchange.requestMethod == "POST") {
                        val requestBody = exchange.requestBody.bufferedReader().readText()
                        val cancelRequest = gson.fromJson(requestBody, CancelRequest::class.java)
                        
                        val result = appointmentRepository.cancelAppointment(cancelRequest.phoneNumber)
                        sendJsonResponse(exchange, if (result.isSuccess) 200 else 400, result)
                    } else {
                        sendErrorResponse(exchange, 405, "Method not allowed")
                    }
                } catch (e: Exception) {
                    sendErrorResponse(exchange, 500, "Failed to cancel appointment")
                }
            }
        }
    }
    
    // Agent status handler
    inner class AgentStatusHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            scope.launch {
                try {
                    val agents = agentManager.getAllAgents()
                    val agentInfo = agents.map { agent ->
                        mapOf(
                            "id" to agent.agentId,
                            "name" to agent.agentName,
                            "capabilities" to agent.capabilities,
                            "priority" to agent.priority,
                            "healthy" to agent.isHealthy()
                        )
                    }
                    
                    sendJsonResponse(exchange, 200, mapOf("agents" to agentInfo))
                } catch (e: Exception) {
                    sendErrorResponse(exchange, 500, "Failed to get agent status")
                }
            }
        }
    }
    
    // Process input handler for AI
    inner class ProcessInputHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            scope.launch {
                try {
                    if (exchange.requestMethod == "POST") {
                        val requestBody = exchange.requestBody.bufferedReader().readText()
                        val processRequest = gson.fromJson(requestBody, ProcessRequest::class.java)
                        
                        // Create AgentInput and CallContext from request
                        val input = com.aireceptionist.app.ai.agents.AgentInput(
                            type = com.aireceptionist.app.ai.agents.InputType.valueOf(processRequest.inputType),
                            content = processRequest.content,
                            context = com.aireceptionist.app.data.models.CallContext(
                                callId = processRequest.callId ?: "api_call_${System.currentTimeMillis()}",
                                callerNumber = processRequest.callerNumber,
                                callerName = processRequest.callerName,
                                callStartTime = System.currentTimeMillis(),
                                isIncoming = true,
                                callState = "active"
                            ),
                            metadata = processRequest.metadata ?: emptyMap()
                        )
                        
                        val responses = mutableListOf<com.aireceptionist.app.ai.agents.AgentResponse>()
                        agentManager.processInput(input, input.context).collect { response ->
                            responses.add(response)
                        }
                        
                        sendJsonResponse(exchange, 200, mapOf("responses" to responses))
                    } else {
                        sendErrorResponse(exchange, 405, "Method not allowed")
                    }
                } catch (e: Exception) {
                    sendErrorResponse(exchange, 500, "Failed to process input")
                }
            }
        }
    }
    
    // Call event webhook handler
    inner class CallEventWebhookHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            scope.launch {
                try {
                    if (exchange.requestMethod == "POST") {
                        val requestBody = exchange.requestBody.bufferedReader().readText()
                        Logger.i(TAG, "Received call event webhook: $requestBody")
                        
                        // Process the webhook payload
                        val event = gson.fromJson(requestBody, Map::class.java)
                        
                        // Handle different event types
                        when (event["type"]) {
                            "call.started" -> handleCallStartedEvent(event)
                            "call.ended" -> handleCallEndedEvent(event)
                            "call.transferred" -> handleCallTransferredEvent(event)
                        }
                        
                        sendJsonResponse(exchange, 200, mapOf("status" to "processed"))
                    } else {
                        sendErrorResponse(exchange, 405, "Method not allowed")
                    }
                } catch (e: Exception) {
                    sendErrorResponse(exchange, 500, "Failed to process webhook")
                }
            }
        }
    }
    
    // Appointment webhook handler
    inner class AppointmentWebhookHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            scope.launch {
                try {
                    if (exchange.requestMethod == "POST") {
                        val requestBody = exchange.requestBody.bufferedReader().readText()
                        Logger.i(TAG, "Received appointment webhook: $requestBody")
                        
                        sendJsonResponse(exchange, 200, mapOf("status" to "received"))
                    } else {
                        sendErrorResponse(exchange, 405, "Method not allowed")
                    }
                } catch (e: Exception) {
                    sendErrorResponse(exchange, 500, "Failed to process webhook")
                }
            }
        }
    }
    
    // Configuration handler
    inner class ConfigHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            scope.launch {
                try {
                    val config = mapOf(
                        "version" to "1.0.0",
                        "api_version" to "v1",
                        "features" to listOf(
                            "call_handling",
                            "appointment_booking", 
                            "ai_agents",
                            "webhooks",
                            "integrations"
                        ),
                        "endpoints" to mapOf(
                            "health" to "/api/health",
                            "calls" to "/api/calls",
                            "appointments" to "/api/appointments",
                            "agents" to "/api/agents/status"
                        )
                    )
                    
                    sendJsonResponse(exchange, 200, config)
                } catch (e: Exception) {
                    sendErrorResponse(exchange, 500, "Failed to get configuration")
                }
            }
        }
    }
    
    // Helper methods
    private suspend fun sendJsonResponse(exchange: HttpExchange, statusCode: Int, data: Any) {
        withContext(Dispatchers.IO) {
            try {
                val response = gson.toJson(data)
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
                exchange.sendResponseHeaders(statusCode, response.length.toLong())
                
                val outputStream: OutputStream = exchange.responseBody
                outputStream.write(response.toByteArray())
                outputStream.close()
            } catch (e: IOException) {
                Logger.e(TAG, "Failed to send response", e)
            }
        }
    }
    
    private suspend fun sendErrorResponse(exchange: HttpExchange, statusCode: Int, message: String) {
        val errorResponse = mapOf(
            "error" to message,
            "status" to statusCode,
            "timestamp" to System.currentTimeMillis()
        )
        sendJsonResponse(exchange, statusCode, errorResponse)
    }
    
    private fun getQueryParameter(exchange: HttpExchange, paramName: String): String? {
        val query = exchange.requestURI.query ?: return null
        return query.split("&")
            .find { it.startsWith("$paramName=") }
            ?.substring(paramName.length + 1)
    }
    
    private suspend fun handleCallStartedEvent(event: Map<*, *>) {
        // Handle call started event
        Logger.i(TAG, "Processing call started event")
    }
    
    private suspend fun handleCallEndedEvent(event: Map<*, *>) {
        // Handle call ended event
        Logger.i(TAG, "Processing call ended event")
    }
    
    private suspend fun handleCallTransferredEvent(event: Map<*, *>) {
        // Handle call transferred event
        Logger.i(TAG, "Processing call transferred event")
    }
    
    companion object {
        private const val TAG = "WebAPIService"
    }
}

// Data classes for API requests
data class AppointmentRequest(
    val date: Long? = null,
    val time: String? = null,
    val serviceType: String? = null,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val notes: String? = null
)

data class CancelRequest(
    val phoneNumber: String
)

data class ProcessRequest(
    val inputType: String,
    val content: String,
    val callId: String? = null,
    val callerNumber: String? = null,
    val callerName: String? = null,
    val metadata: Map<String, Any>? = null
)