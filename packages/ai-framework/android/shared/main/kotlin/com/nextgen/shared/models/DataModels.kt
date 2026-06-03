package com.nextgen.shared.models

import kotlinx.serialization.Serializable

/**
 * Voice command data model
 */
@Serializable
data class VoiceCommand(
    val id: String,
    val text: String,
    val confidence: Float,
    val timestamp: Long,
    val language: String = "en-US",
    val processed: Boolean = false,
    val response: String? = null
)

/**
 * Service status model
 */
@Serializable
data class ServiceStatus(
    val serviceName: String,
    val status: Status,
    val lastUpdate: Long,
    val details: String? = null
) {
    enum class Status {
        ONLINE, OFFLINE, STARTING, ERROR
    }
}

/**
 * Database operation model
 */
@Serializable
data class DatabaseOperation(
    val id: String,
    val operation: Operation,
    val table: String,
    val data: Map<String, String>,
    val timestamp: Long,
    val success: Boolean = false,
    val errorMessage: String? = null
) {
    enum class Operation {
        CREATE, READ, UPDATE, DELETE, QUERY
    }
}

/**
 * API request/response models
 */
@Serializable
data class ApiRequest(
    val id: String,
    val endpoint: String,
    val method: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val timestamp: Long
)

@Serializable
data class ApiResponse(
    val requestId: String,
    val statusCode: Int,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val timestamp: Long,
    val duration: Long
)

/**
 * Integration hub models
 */
@Serializable
data class IntegrationConnection(
    val id: String,
    val targetPackage: String,
    val connectionType: ConnectionType,
    val isActive: Boolean,
    val lastCommunication: Long? = null
) {
    enum class ConnectionType {
        SERVICE_BINDING, CONTENT_PROVIDER, BROADCAST, INTENT
    }
}

/**
 * MCP server models
 */
@Serializable
data class MCPMessage(
    val id: String,
    val type: MessageType,
    val source: String,
    val target: String,
    val payload: String,
    val timestamp: Long
) {
    enum class MessageType {
        COMMAND, RESPONSE, EVENT, HEARTBEAT
    }
}

/**
 * Vector database models
 */
@Serializable
data class VectorEntry(
    val id: String,
    val vector: List<Float>,
    val metadata: Map<String, String>,
    val timestamp: Long
)

@Serializable
data class VectorSearchQuery(
    val queryVector: List<Float>,
    val topK: Int = 10,
    val filters: Map<String, String> = emptyMap(),
    val threshold: Float = 0.0f
)

@Serializable
data class VectorSearchResult(
    val entries: List<VectorSearchResultEntry>,
    val totalResults: Int,
    val searchTime: Long
)

@Serializable
data class VectorSearchResultEntry(
    val entry: VectorEntry,
    val similarity: Float,
    val distance: Float
)