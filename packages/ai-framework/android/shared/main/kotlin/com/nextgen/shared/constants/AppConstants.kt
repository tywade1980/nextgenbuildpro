package com.nextgen.shared.constants

/**
 * Application-wide constants for NextGen APK
 */
object AppConstants {
    
    // Application Info
    const val APP_NAME = "NextGen APK"
    const val APP_VERSION = "1.0.0"
    const val APP_PACKAGE = "com.nextgen.apk"
    
    // Database
    const val DATABASE_NAME = "nextgen_db"
    const val DATABASE_VERSION = 1
    
    // Vector Database
    const val VECTOR_DIMENSION = 1536 // OpenAI embedding dimension
    const val VECTOR_COLLECTION_NAME = "nextgen_vectors"
    
    // Services
    const val VOICE_SERVICE_NAME = "VoiceProcessingService"
    const val BACKEND_SERVICE_NAME = "BackendService"
    const val MCP_SERVER_SERVICE_NAME = "MCPServerService"
    const val INTEGRATION_HUB_SERVICE_NAME = "IntegrationHubService"
    
    // Ports and Network
    const val MCP_SERVER_PORT = 8080
    const val BACKEND_API_PORT = 8081
    const val INTEGRATION_HUB_PORT = 8082
    const val DATABASE_PORT = 5432
    
    // Voice Recognition
    const val VOICE_CONFIDENCE_THRESHOLD = 0.7f
    const val VOICE_TIMEOUT_MS = 5000L
    const val TTS_QUEUE_FLUSH_TIMEOUT = 1000L
    
    // File Paths
    const val DATA_DIRECTORY = "nextgen_data"
    const val LOG_DIRECTORY = "logs"
    const val CACHE_DIRECTORY = "cache"
    const val BACKUP_DIRECTORY = "backups"
    
    // API Endpoints
    const val API_BASE_URL = "http://localhost:8081/api/v1"
    const val MCP_BASE_URL = "http://localhost:8080/mcp"
    const val INTEGRATION_BASE_URL = "http://localhost:8082/integration"
    
    // Integration
    const val INTEGRATION_INTENT_ACTION = "com.nextgen.apk.INTEGRATION"
    const val MCP_INTENT_ACTION = "com.nextgen.apk.MCP_SERVICE"
    const val VOICE_COMMAND_ACTION = "com.nextgen.apk.VOICE_COMMAND"
    
    // Permissions
    val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.INTERNET,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    
    // Timeouts
    const val NETWORK_TIMEOUT_MS = 30000L
    const val DATABASE_TIMEOUT_MS = 10000L
    const val SERVICE_STARTUP_TIMEOUT_MS = 15000L
    
    // Cache
    const val CACHE_SIZE_MB = 100L
    const val CACHE_EXPIRY_HOURS = 24L
    
    // Performance
    const val MAX_CONCURRENT_OPERATIONS = 10
    const val THREAD_POOL_SIZE = 8
    const val COROUTINE_POOL_SIZE = 16
}

/**
 * Message types for inter-service communication
 */
object MessageTypes {
    const val VOICE_COMMAND = "VOICE_COMMAND"
    const val DATABASE_OPERATION = "DATABASE_OPERATION"
    const val API_REQUEST = "API_REQUEST"
    const val MCP_MESSAGE = "MCP_MESSAGE"
    const val INTEGRATION_REQUEST = "INTEGRATION_REQUEST"
    const val SYSTEM_STATUS = "SYSTEM_STATUS"
    const val ERROR_REPORT = "ERROR_REPORT"
    const val HEARTBEAT = "HEARTBEAT"
}

/**
 * Error codes for the application
 */
object ErrorCodes {
    const val SUCCESS = 0
    const val GENERAL_ERROR = 1000
    const val VOICE_ERROR = 1100
    const val DATABASE_ERROR = 1200
    const val NETWORK_ERROR = 1300
    const val PERMISSION_ERROR = 1400
    const val SERVICE_ERROR = 1500
    const val INTEGRATION_ERROR = 1600
    const val MCP_ERROR = 1700
}

/**
 * Configuration keys
 */
object ConfigKeys {
    const val VOICE_ENABLED = "voice_enabled"
    const val TTS_ENABLED = "tts_enabled"
    const val DATABASE_URL = "database_url"
    const val MCP_SERVER_ENABLED = "mcp_server_enabled"
    const val INTEGRATION_HUB_ENABLED = "integration_hub_enabled"
    const val LOG_LEVEL = "log_level"
    const val AUTO_START_SERVICES = "auto_start_services"
    const val VOICE_LANGUAGE = "voice_language"
    const val TTS_SPEED = "tts_speed"
    const val TTS_PITCH = "tts_pitch"
}