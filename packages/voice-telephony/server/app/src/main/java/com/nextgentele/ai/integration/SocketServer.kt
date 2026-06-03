package com.nextgentele.ai.integration

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nextgentele.ai.service.IntegrationSocketService
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class SocketServer(
    private val port: Int,
    private val integrationService: IntegrationSocketService
) {
    
    private var serverSocket: ServerSocket? = null
    private val clients = ConcurrentHashMap<String, Socket>()
    private val executor = Executors.newCachedThreadPool()
    private val gson = Gson()
    private var isRunning = false
    
    companion object {
        private const val TAG = "SocketServer"
    }
    
    fun start() {
        if (isRunning) return
        
        isRunning = true
        executor.execute {
            try {
                serverSocket = ServerSocket(port)
                Log.d(TAG, "Socket server listening on port $port")
                
                while (isRunning) {
                    val clientSocket = serverSocket?.accept()
                    if (clientSocket != null) {
                        val clientId = "client_${System.currentTimeMillis()}"
                        clients[clientId] = clientSocket
                        handleClient(clientId, clientSocket)
                    }
                }
            } catch (e: Exception) {
                if (isRunning) {
                    Log.e(TAG, "Socket server error", e)
                }
            }
        }
    }
    
    fun stop() {
        isRunning = false
        try {
            clients.values.forEach { it.close() }
            clients.clear()
            serverSocket?.close()
            executor.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping socket server", e)
        }
    }
    
    private fun handleClient(clientId: String, clientSocket: Socket) {
        executor.execute {
            try {
                val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                val writer = PrintWriter(clientSocket.getOutputStream(), true)
                
                // Send welcome message
                val welcomeMessage = createMessage("welcome", mapOf(
                    "clientId" to clientId,
                    "server" to "NextGenTele Integration API",
                    "version" to "1.0"
                ))
                writer.println(welcomeMessage)
                
                var line: String?
                while (clientSocket.isConnected && reader.readLine().also { line = it } != null) {
                    handleMessage(clientId, line!!, writer)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error handling client $clientId", e)
            } finally {
                clients.remove(clientId)
                try {
                    clientSocket.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error closing client socket", e)
                }
            }
        }
    }
    
    private fun handleMessage(clientId: String, message: String, writer: PrintWriter) {
        try {
            val jsonMessage = gson.fromJson(message, JsonObject::class.java)
            val action = jsonMessage.get("action")?.asString
            val data = jsonMessage.getAsJsonObject("data")
            
            Log.d(TAG, "Received message from $clientId: $action")
            
            val response = when (action) {
                "get_call_status" -> handleGetCallStatus()
                "get_contacts" -> handleGetContacts(data)
                "schedule_callback" -> handleScheduleCallback(data)
                "update_crm" -> handleUpdateCRM(data)
                "get_calendar" -> handleGetCalendar(data)
                "add_calendar_event" -> handleAddCalendarEvent(data)
                "send_notification" -> handleSendNotification(data)
                "get_call_history" -> handleGetCallHistory(data)
                else -> createErrorResponse("Unknown action: $action")
            }
            
            writer.println(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing message from $clientId", e)
            val errorResponse = createErrorResponse("Error processing message: ${e.message}")
            writer.println(errorResponse)
        }
    }
    
    private fun handleGetCallStatus(): String {
        // Return current call status
        return createMessage("call_status", mapOf(
            "active_calls" to 0,
            "ai_enabled" to true,
            "status" to "ready"
        ))
    }
    
    private fun handleGetContacts(data: JsonObject?): String {
        // Get contacts from CRM integration
        val contacts = integrationService.getCrmIntegration().getContacts()
        return createMessage("contacts", mapOf("contacts" to contacts))
    }
    
    private fun handleScheduleCallback(data: JsonObject?): String {
        val phoneNumber = data?.get("phone_number")?.asString
        val datetime = data?.get("datetime")?.asString
        val notes = data?.get("notes")?.asString
        
        if (phoneNumber != null && datetime != null) {
            val success = integrationService.getCalendarIntegration()
                .scheduleCallback(phoneNumber, datetime, notes ?: "")
            
            return if (success) {
                createMessage("callback_scheduled", mapOf(
                    "phone_number" to phoneNumber,
                    "datetime" to datetime,
                    "status" to "success"
                ))
            } else {
                createErrorResponse("Failed to schedule callback")
            }
        } else {
            return createErrorResponse("Missing required fields: phone_number, datetime")
        }
    }
    
    private fun handleUpdateCRM(data: JsonObject?): String {
        val contactId = data?.get("contact_id")?.asString
        val updateData = data?.getAsJsonObject("update_data")
        
        if (contactId != null && updateData != null) {
            val success = integrationService.getCrmIntegration()
                .updateContact(contactId, updateData.toString())
            
            return if (success) {
                createMessage("crm_updated", mapOf(
                    "contact_id" to contactId,
                    "status" to "success"
                ))
            } else {
                createErrorResponse("Failed to update CRM")
            }
        } else {
            return createErrorResponse("Missing required fields: contact_id, update_data")
        }
    }
    
    private fun handleGetCalendar(data: JsonObject?): String {
        val date = data?.get("date")?.asString
        val events = integrationService.getCalendarIntegration().getEvents(date)
        return createMessage("calendar_events", mapOf("events" to events))
    }
    
    private fun handleAddCalendarEvent(data: JsonObject?): String {
        val title = data?.get("title")?.asString
        val datetime = data?.get("datetime")?.asString
        val duration = data?.get("duration")?.asInt ?: 60
        
        if (title != null && datetime != null) {
            val success = integrationService.getCalendarIntegration()
                .addEvent(title, datetime, duration)
            
            return if (success) {
                createMessage("event_added", mapOf(
                    "title" to title,
                    "datetime" to datetime,
                    "status" to "success"
                ))
            } else {
                createErrorResponse("Failed to add calendar event")
            }
        } else {
            return createErrorResponse("Missing required fields: title, datetime")
        }
    }
    
    private fun handleSendNotification(data: JsonObject?): String {
        val message = data?.get("message")?.asString
        val priority = data?.get("priority")?.asString ?: "normal"
        
        if (message != null) {
            // Send notification through Android notification system
            return createMessage("notification_sent", mapOf(
                "message" to message,
                "priority" to priority,
                "status" to "success"
            ))
        } else {
            return createErrorResponse("Missing required field: message")
        }
    }
    
    private fun handleGetCallHistory(data: JsonObject?): String {
        val limit = data?.get("limit")?.asInt ?: 50
        val callHistory = listOf<Map<String, Any>>() // Placeholder
        
        return createMessage("call_history", mapOf(
            "calls" to callHistory,
            "total" to callHistory.size
        ))
    }
    
    private fun createMessage(type: String, data: Any): String {
        val message = mapOf(
            "type" to type,
            "data" to data,
            "timestamp" to System.currentTimeMillis()
        )
        return gson.toJson(message)
    }
    
    private fun createErrorResponse(error: String): String {
        return createMessage("error", mapOf("message" to error))
    }
    
    fun broadcastMessage(message: String) {
        clients.values.forEach { socket ->
            try {
                val writer = PrintWriter(socket.getOutputStream(), true)
                writer.println(message)
            } catch (e: Exception) {
                Log.e(TAG, "Error broadcasting message", e)
            }
        }
    }
}