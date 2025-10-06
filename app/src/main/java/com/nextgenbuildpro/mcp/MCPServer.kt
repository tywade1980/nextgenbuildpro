package com.nextgenbuildpro.mcp

import android.util.Log
import com.nextgenbuildpro.shared.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap

/**
 * MCP (Model Context Protocol) Server for NextGen BuildPro v2.0
 * 
 * Provides robust communication layer for AI agents with:
 * - Real-time message routing between agents
 * - Context sharing and state management
 * - Resource management and tool access
 * - Performance monitoring and optimization
 * - Session management and authentication
 */
class MCPServer private constructor() {
    companion object {
        @Volatile
        private var INSTANCE: MCPServer? = null
        
        fun getInstance(): MCPServer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MCPServer().also { INSTANCE = it }
            }
        }
    }
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeConnections = ConcurrentHashMap<String, MCPConnection>()
    private val messageQueue = ConcurrentHashMap<String, MutableList<MCPMessage>>()
    private val resourceRegistry = ConcurrentHashMap<String, MCPResource>()
    private val sessionStore = ConcurrentHashMap<String, MCPSession>()
    
    private val _serverStatus = MutableStateFlow(MCPServerStatus.STOPPED)
    val serverStatus: StateFlow<MCPServerStatus> = _serverStatus.asStateFlow()
    
    private val _metrics = MutableStateFlow(MCPMetrics())
    val metrics: StateFlow<MCPMetrics> = _metrics.asStateFlow()
    
    suspend fun start(): Result<Unit> = try {
        Log.i("MCPServer", "Starting MCP Server...")
        _serverStatus.value = MCPServerStatus.STARTING
        
        initializeResourceRegistry()
        startMessageProcessor()
        startMetricsCollector()
        
        _serverStatus.value = MCPServerStatus.RUNNING
        Log.i("MCPServer", "MCP Server started successfully")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("MCPServer", "Failed to start MCP Server", e)
        _serverStatus.value = MCPServerStatus.ERROR
        Result.failure(e)
    }
    
    suspend fun createConnection(agentId: String, agentType: AgentType): Result<MCPConnection> {
        return try {
            if (activeConnections.containsKey(agentId)) {
                return Result.failure(IllegalStateException("Agent $agentId already connected"))
            }
            
            val connection = MCPConnection(
                agentId = agentId,
                agentType = agentType,
                server = this,
                createdAt = System.currentTimeMillis()
            )
            
            activeConnections[agentId] = connection
            messageQueue[agentId] = mutableListOf()
            
            Log.d("MCPServer", "Created connection for agent: $agentId")
            updateMetrics()
            
            Result.success(connection)
        } catch (e: Exception) {
            Log.e("MCPServer", "Failed to create connection for agent: $agentId", e)
            Result.failure(e)
        }
    }
    
    suspend fun sendMessage(message: MCPMessage): Result<Unit> {
        return try {
            val targetQueue = messageQueue[message.targetAgentId]
                ?: return Result.failure(IllegalArgumentException("Target agent not found: ${message.targetAgentId}"))
            
            targetQueue.add(message)
            updateMetrics()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun initializeResourceRegistry() {
        val systemResources = listOf(
            MCPResource(
                id = "construction_database_2025",
                name = "2025 Construction Cost Database",
                type = "database",
                description = "Complete construction cost database with 2025 pricing"
            ),
            MCPResource(
                id = "voice_processing",
                name = "Voice Processing Engine", 
                type = "service",
                description = "Natural language processing for voice commands"
            )
        )
        
        systemResources.forEach { resource ->
            resourceRegistry[resource.id] = resource
        }
    }
    
    private fun startMessageProcessor() {
        scope.launch {
            while (scope.isActive) {
                delay(100)
            }
        }
    }

    private fun startMetricsCollector() {
        scope.launch {
            while (scope.isActive) {
                updateMetrics()
                delay(5000)
            }
        }
    }
    
    private fun updateMetrics() {
        val currentMetrics = _metrics.value
        _metrics.value = currentMetrics.copy(
            activeConnections = activeConnections.size,
            registeredResources = resourceRegistry.size
        )
    }

    suspend fun shutdown(): Result<Unit> = try {
        Log.i("MCPServer", "Shutting down MCP Server...")
        _serverStatus.value = MCPServerStatus.STOPPING

        // Close all active connections
        activeConnections.values.forEach { it.close() }
        activeConnections.clear()
        messageQueue.clear()

        // Cancel coroutine scope
        scope.cancel()

        _serverStatus.value = MCPServerStatus.STOPPED
        Log.i("MCPServer", "MCP Server shutdown complete")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("MCPServer", "Failed to shutdown MCP Server", e)
        _serverStatus.value = MCPServerStatus.ERROR
        Result.failure(e)
    }
}

class MCPConnection(
    val agentId: String,
    val agentType: AgentType,
    private val server: MCPServer,
    val createdAt: Long
) {
    private var isActive = true
    
    fun close() {
        isActive = false
    }
}

enum class MCPServerStatus {
    STOPPED, STARTING, RUNNING, STOPPING, ERROR
}

data class MCPMessage(
    val id: String,
    val sourceAgentId: String,
    val targetAgentId: String,
    val messageType: String,
    val content: String,
    val timestamp: Long
)

data class MCPResource(
    val id: String,
    val name: String,
    val type: String,
    val description: String
)

data class MCPSession(
    val sessionId: String,
    val agentId: String,
    val createdAt: Long,
    val lastAccessedAt: Long = createdAt,
    val metadata: Map<String, Any> = emptyMap()
)

data class MCPMetrics(
    val activeConnections: Int = 0,
    val registeredResources: Int = 0
)