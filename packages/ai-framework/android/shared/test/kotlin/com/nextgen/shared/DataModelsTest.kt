package com.nextgen.shared.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for NextGen APK data models
 */
class DataModelsTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @Test
    fun `test VoiceCommand serialization`() {
        val command = VoiceCommand(
            id = "test-id",
            text = "test command",
            confidence = 0.95f,
            timestamp = 1234567890L,
            language = "en-US",
            processed = true,
            response = "test response"
        )
        
        val jsonString = json.encodeToString(command)
        val deserializedCommand = json.decodeFromString<VoiceCommand>(jsonString)
        
        assertEquals(command.id, deserializedCommand.id)
        assertEquals(command.text, deserializedCommand.text)
        assertEquals(command.confidence, deserializedCommand.confidence, 0.001f)
        assertEquals(command.timestamp, deserializedCommand.timestamp)
        assertEquals(command.language, deserializedCommand.language)
        assertEquals(command.processed, deserializedCommand.processed)
        assertEquals(command.response, deserializedCommand.response)
    }
    
    @Test
    fun `test ServiceStatus creation`() {
        val status = ServiceStatus(
            serviceName = "test-service",
            status = ServiceStatus.Status.ONLINE,
            lastUpdate = 1234567890L,
            details = "Service is running"
        )
        
        assertEquals("test-service", status.serviceName)
        assertEquals(ServiceStatus.Status.ONLINE, status.status)
        assertEquals(1234567890L, status.lastUpdate)
        assertEquals("Service is running", status.details)
    }
    
    @Test
    fun `test DatabaseOperation creation`() {
        val operation = DatabaseOperation(
            id = "db-op-1",
            operation = DatabaseOperation.Operation.CREATE,
            table = "test_table",
            data = mapOf("key1" to "value1", "key2" to "value2"),
            timestamp = 1234567890L,
            success = true
        )
        
        assertEquals("db-op-1", operation.id)
        assertEquals(DatabaseOperation.Operation.CREATE, operation.operation)
        assertEquals("test_table", operation.table)
        assertEquals(2, operation.data.size)
        assertTrue(operation.success)
    }
    
    @Test
    fun `test IntegrationConnection creation`() {
        val connection = IntegrationConnection(
            id = "conn-1",
            targetPackage = "com.example.app",
            connectionType = IntegrationConnection.ConnectionType.SERVICE_BINDING,
            isActive = true,
            lastCommunication = 1234567890L
        )
        
        assertEquals("conn-1", connection.id)
        assertEquals("com.example.app", connection.targetPackage)
        assertEquals(IntegrationConnection.ConnectionType.SERVICE_BINDING, connection.connectionType)
        assertTrue(connection.isActive)
        assertEquals(1234567890L, connection.lastCommunication)
    }
    
    @Test
    fun `test MCPMessage serialization`() {
        val message = MCPMessage(
            id = "msg-1",
            type = MCPMessage.MessageType.COMMAND,
            source = "client-1",
            target = "server",
            payload = "test payload",
            timestamp = 1234567890L
        )
        
        val jsonString = json.encodeToString(message)
        val deserializedMessage = json.decodeFromString<MCPMessage>(jsonString)
        
        assertEquals(message.id, deserializedMessage.id)
        assertEquals(message.type, deserializedMessage.type)
        assertEquals(message.source, deserializedMessage.source)
        assertEquals(message.target, deserializedMessage.target)
        assertEquals(message.payload, deserializedMessage.payload)
        assertEquals(message.timestamp, deserializedMessage.timestamp)
    }
    
    @Test
    fun `test VectorEntry creation`() {
        val vectorEntry = VectorEntry(
            id = "vec-1",
            vector = listOf(0.1f, 0.2f, 0.3f),
            metadata = mapOf("type" to "document", "category" to "test"),
            timestamp = 1234567890L
        )
        
        assertEquals("vec-1", vectorEntry.id)
        assertEquals(3, vectorEntry.vector.size)
        assertEquals(0.1f, vectorEntry.vector[0], 0.001f)
        assertEquals(2, vectorEntry.metadata.size)
        assertEquals("document", vectorEntry.metadata["type"])
    }
    
    @Test
    fun `test VectorSearchQuery creation`() {
        val query = VectorSearchQuery(
            queryVector = listOf(0.5f, 0.6f, 0.7f),
            topK = 5,
            filters = mapOf("category" to "test"),
            threshold = 0.8f
        )
        
        assertEquals(3, query.queryVector.size)
        assertEquals(5, query.topK)
        assertEquals(1, query.filters.size)
        assertEquals(0.8f, query.threshold, 0.001f)
    }
    
    @Test
    fun `test all enum values are accessible`() {
        // Test ServiceStatus.Status enum
        assertNotNull(ServiceStatus.Status.ONLINE)
        assertNotNull(ServiceStatus.Status.OFFLINE)
        assertNotNull(ServiceStatus.Status.STARTING)
        assertNotNull(ServiceStatus.Status.ERROR)
        
        // Test DatabaseOperation.Operation enum
        assertNotNull(DatabaseOperation.Operation.CREATE)
        assertNotNull(DatabaseOperation.Operation.READ)
        assertNotNull(DatabaseOperation.Operation.UPDATE)
        assertNotNull(DatabaseOperation.Operation.DELETE)
        assertNotNull(DatabaseOperation.Operation.QUERY)
        
        // Test IntegrationConnection.ConnectionType enum
        assertNotNull(IntegrationConnection.ConnectionType.SERVICE_BINDING)
        assertNotNull(IntegrationConnection.ConnectionType.CONTENT_PROVIDER)
        assertNotNull(IntegrationConnection.ConnectionType.BROADCAST)
        assertNotNull(IntegrationConnection.ConnectionType.INTENT)
        
        // Test MCPMessage.MessageType enum
        assertNotNull(MCPMessage.MessageType.COMMAND)
        assertNotNull(MCPMessage.MessageType.RESPONSE)
        assertNotNull(MCPMessage.MessageType.EVENT)
        assertNotNull(MCPMessage.MessageType.HEARTBEAT)
    }
}