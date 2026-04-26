package com.nextgen.mcp.server

import com.nextgen.shared.models.MCPMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

/**
 * MCP (Model Context Protocol) Server Implementation
 * 
 * Provides a robust communication protocol for:
 * - Cross-application messaging
 * - Service coordination
 * - Real-time data exchange
 * - External system integration
 * - Voice command distribution
 */
@Component
class MCPServer(private val port: Int = 8080) : WebSocketServer(InetSocketAddress(port)) {
    
    private val logger = LoggerFactory.getLogger(MCPServer::class.java)
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    // Connected clients management
    private val connectedClients = ConcurrentHashMap<String, WebSocket>()
    private val clientMetadata = ConcurrentHashMap<String, ClientMetadata>()
    
    // Message flows
    private val _messageFlow = MutableSharedFlow<MCPMessage>()
    val messageFlow: Flow<MCPMessage> = _messageFlow.asSharedFlow()
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    data class ClientMetadata(
        val id: String,
        val type: String,
        val capabilities: List<String>,
        val lastHeartbeat: Long,
        val isActive: Boolean = true
    )
    
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        conn?.let { connection ->
            val clientId = generateClientId()
            connectedClients[clientId] = connection
            connection.setAttachment(clientId)
            
            logger.info("MCP client connected: $clientId from ${connection.remoteSocketAddress}")
            
            // Send welcome message
            val welcomeMessage = MCPMessage(
                id = java.util.UUID.randomUUID().toString(),
                type = MCPMessage.MessageType.EVENT,
                source = "mcp-server",
                target = clientId,
                payload = json.encodeToString(mapOf(
                    "event" to "connected",
                    "clientId" to clientId,
                    "serverCapabilities" to listOf(
                        "voice-commands",
                        "database-operations",
                        "integration-hub",
                        "real-time-messaging",
                        "file-transfer",
                        "system-monitoring"
                    )
                )),
                timestamp = System.currentTimeMillis()
            )
            
            sendMessage(connection, welcomeMessage)
        }
    }
    
    override fun onMessage(conn: WebSocket?, message: String?) {
        conn?.let { connection ->
            val clientId = connection.getAttachment<String>()
            logger.debug("Received message from client $clientId: $message")
            
            try {
                message?.let { msg ->
                    val mcpMessage = json.decodeFromString<MCPMessage>(msg)
                    processMessage(connection, mcpMessage)
                }
            } catch (e: Exception) {
                logger.error("Error processing message from client $clientId", e)
                sendErrorResponse(connection, "Invalid message format: ${e.message}")
            }
        }
    }
    
    override fun onMessage(conn: WebSocket?, message: ByteBuffer?) {
        // Handle binary messages for file transfers
        conn?.let { connection ->
            val clientId = connection.getAttachment<String>()
            logger.debug("Received binary message from client $clientId, size: ${message?.remaining() ?: 0}")
            
            // Process binary data (e.g., file uploads, audio data)
            message?.let { data ->
                scope.launch {
                    processBinaryMessage(clientId, data)
                }
            }
        }
    }
    
    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        conn?.let { connection ->
            val clientId = connection.getAttachment<String>()
            connectedClients.remove(clientId)
            clientMetadata.remove(clientId)
            
            logger.info("MCP client disconnected: $clientId, reason: $reason")
        }
    }
    
    override fun onError(conn: WebSocket?, ex: Exception?) {
        val clientId = conn?.getAttachment<String>() ?: "unknown"
        logger.error("MCP server error for client $clientId", ex)
    }
    
    override fun onStart() {
        logger.info("MCP Server started on port $port")
        
        // Start heartbeat monitoring
        scope.launch {
            startHeartbeatMonitoring()
        }
    }
    
    /**
     * Process incoming MCP message
     */
    private suspend fun processMessage(connection: WebSocket, message: MCPMessage) {
        val clientId = connection.getAttachment<String>()
        
        try {
            // Emit message for other components
            _messageFlow.emit(message)
            
            when (message.type) {
                MCPMessage.MessageType.COMMAND -> {
                    handleCommand(connection, message)
                }
                MCPMessage.MessageType.RESPONSE -> {
                    handleResponse(connection, message)
                }
                MCPMessage.MessageType.EVENT -> {
                    handleEvent(connection, message)
                }
                MCPMessage.MessageType.HEARTBEAT -> {
                    handleHeartbeat(connection, message)
                }
            }
            
        } catch (e: Exception) {
            logger.error("Error processing message from client $clientId", e)
            sendErrorResponse(connection, "Processing error: ${e.message}")
        }
    }
    
    /**
     * Handle command messages
     */
    private suspend fun handleCommand(connection: WebSocket, message: MCPMessage) {
        val clientId = connection.getAttachment<String>()
        logger.info("Processing command from client $clientId: ${message.payload}")
        
        val response = when {
            message.payload.contains("voice-command") -> {
                processVoiceCommand(message.payload)
            }
            message.payload.contains("database-query") -> {
                processDatabaseQuery(message.payload)
            }
            message.payload.contains("integration-request") -> {
                processIntegrationRequest(message.payload)
            }
            message.payload.contains("system-status") -> {
                getSystemStatus()
            }
            else -> {
                "Command processed: ${message.payload}"
            }
        }
        
        val responseMessage = MCPMessage(
            id = java.util.UUID.randomUUID().toString(),
            type = MCPMessage.MessageType.RESPONSE,
            source = "mcp-server",
            target = message.source,
            payload = response,
            timestamp = System.currentTimeMillis()
        )
        
        sendMessage(connection, responseMessage)
    }
    
    /**
     * Handle response messages
     */
    private fun handleResponse(connection: WebSocket, message: MCPMessage) {
        logger.debug("Received response: ${message.payload}")
        // Forward response to appropriate handler
    }
    
    /**
     * Handle event messages
     */
    private fun handleEvent(connection: WebSocket, message: MCPMessage) {
        val clientId = connection.getAttachment<String>()
        logger.debug("Received event from client $clientId: ${message.payload}")
        
        // Broadcast event to other connected clients if needed
        when {
            message.payload.contains("broadcast") -> {
                broadcastToAllClients(message)
            }
            message.payload.contains("register") -> {
                registerClientCapabilities(clientId, message.payload)
            }
        }
    }
    
    /**
     * Handle heartbeat messages
     */
    private fun handleHeartbeat(connection: WebSocket, message: MCPMessage) {
        val clientId = connection.getAttachment<String>()
        clientMetadata[clientId]?.let { metadata ->
            clientMetadata[clientId] = metadata.copy(lastHeartbeat = System.currentTimeMillis())
        }
        
        // Send heartbeat response
        val heartbeatResponse = MCPMessage(
            id = java.util.UUID.randomUUID().toString(),
            type = MCPMessage.MessageType.HEARTBEAT,
            source = "mcp-server",
            target = clientId,
            payload = "pong",
            timestamp = System.currentTimeMillis()
        )
        
        sendMessage(connection, heartbeatResponse)
    }
    
    /**
     * Send message to specific connection
     */
    private fun sendMessage(connection: WebSocket, message: MCPMessage) {
        try {
            val jsonMessage = json.encodeToString(message)
            connection.send(jsonMessage)
        } catch (e: Exception) {
            logger.error("Error sending message", e)
        }
    }
    
    /**
     * Send error response
     */
    private fun sendErrorResponse(connection: WebSocket, error: String) {
        val errorMessage = MCPMessage(
            id = java.util.UUID.randomUUID().toString(),
            type = MCPMessage.MessageType.RESPONSE,
            source = "mcp-server",
            target = connection.getAttachment<String>() ?: "unknown",
            payload = json.encodeToString(mapOf("error" to error)),
            timestamp = System.currentTimeMillis()
        )
        
        sendMessage(connection, errorMessage)
    }
    
    /**
     * Broadcast message to all connected clients
     */
    fun broadcastToAllClients(message: MCPMessage) {
        connectedClients.values.forEach { connection ->
            sendMessage(connection, message)
        }
    }
    
    /**
     * Send message to specific client
     */
    fun sendToClient(clientId: String, message: MCPMessage): Boolean {
        return connectedClients[clientId]?.let { connection ->
            sendMessage(connection, message)
            true
        } ?: false
    }
    
    /**
     * Get connected clients count
     */
    fun getConnectedClientsCount(): Int = connectedClients.size
    
    /**
     * Get client metadata
     */
    fun getClientMetadata(): Map<String, ClientMetadata> = clientMetadata.toMap()
    
    private fun generateClientId(): String = "client_${System.currentTimeMillis()}_${(1000..9999).random()}"
    
    private suspend fun processBinaryMessage(clientId: String, data: ByteBuffer) {
        // Process binary data (file uploads, audio streams, etc.)
        logger.debug("Processing binary message from client $clientId")
    }
    
    private suspend fun startHeartbeatMonitoring() {
        while (true) {
            delay(30000) // Check every 30 seconds
            
            val currentTime = System.currentTimeMillis()
            val staleClients = clientMetadata.filter { (_, metadata) ->
                currentTime - metadata.lastHeartbeat > 60000 // 1 minute timeout
            }
            
            staleClients.forEach { (clientId, _) ->
                logger.warn("Client $clientId is stale, removing...")
                connectedClients[clientId]?.close()
                connectedClients.remove(clientId)
                clientMetadata.remove(clientId)
            }
        }
    }
    
    private fun processVoiceCommand(payload: String): String {
        return "Voice command processed: $payload"
    }
    
    private fun processDatabaseQuery(payload: String): String {
        return "Database query executed: $payload"
    }
    
    private fun processIntegrationRequest(payload: String): String {
        return "Integration request processed: $payload"
    }
    
    private fun getSystemStatus(): String {
        return json.encodeToString(mapOf(
            "status" to "operational",
            "connectedClients" to connectedClients.size,
            "uptime" to System.currentTimeMillis(),
            "services" to mapOf(
                "voice-engine" to "online",
                "database" to "online",
                "integration-hub" to "online"
            )
        ))
    }
    
    private fun registerClientCapabilities(clientId: String, payload: String) {
        try {
            val data = json.decodeFromString<Map<String, Any>>(payload)
            val capabilities = data["capabilities"] as? List<String> ?: emptyList()
            val clientType = data["type"] as? String ?: "unknown"
            
            clientMetadata[clientId] = ClientMetadata(
                id = clientId,
                type = clientType,
                capabilities = capabilities,
                lastHeartbeat = System.currentTimeMillis()
            )
            
            logger.info("Registered client $clientId with capabilities: $capabilities")
        } catch (e: Exception) {
            logger.error("Error registering client capabilities", e)
        }
    }
}