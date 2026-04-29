package com.nextgentele.ai.integration

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Bridge between Android privileged telephony services and Node.js backend server
 * Handles real-time communication for AI processing, IVR management, and call analytics
 */
class NodeJSBridge(private val context: Context) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    private val baseUrl = "http://localhost:3000"
    private val socketUrl = "ws://localhost:3000"
    
    companion object {
        private const val TAG = "NodeJSBridge"
        private val JSON = "application/json".toMediaType()
    }
    
    // === System Status Integration ===
    
    /**
     * Get system status from Node.js server
     */
    suspend fun getSystemStatus(): SystemStatus? {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/api/status")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string()
                json?.let { gson.fromJson(it, SystemStatus::class.java) }
            } else {
                Log.e(TAG, "Failed to get system status: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting system status", e)
            null
        }
    }
    
    // === IVR Integration ===
    
    /**
     * Start IVR session for incoming call
     */
    suspend fun startIVRSession(callId: String, menuId: String = "standard_transfer"): IVRSession? {
        return try {
            val requestBody = JsonObject().apply {
                addProperty("menuId", menuId)
                addProperty("options", "{}")
            }
            
            val request = Request.Builder()
                .url("$baseUrl/api/ivr/start/$callId")
                .post(requestBody.toString().toRequestBody(JSON))
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string()
                json?.let { 
                    val result = gson.fromJson(it, JsonObject::class.java)
                    if (result.get("success").asBoolean) {
                        gson.fromJson(result.get("session"), IVRSession::class.java)
                    } else null
                }
            } else {
                Log.e(TAG, "Failed to start IVR session: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting IVR session", e)
            null
        }
    }
    
    /**
     * Process DTMF input through Node.js IVR service
     */
    suspend fun processDTMFInput(callId: String, digit: String): IVRResponse? {
        return try {
            val requestBody = JsonObject().apply {
                addProperty("digit", digit)
            }
            
            val request = Request.Builder()
                .url("$baseUrl/api/ivr/process/$callId")
                .post(requestBody.toString().toRequestBody(JSON))
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string()
                json?.let { 
                    val result = gson.fromJson(it, JsonObject::class.java)
                    if (result.get("success").asBoolean) {
                        gson.fromJson(result.get("response"), IVRResponse::class.java)
                    } else null
                }
            } else {
                Log.e(TAG, "Failed to process DTMF: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing DTMF", e)
            null
        }
    }
    
    // === AI Integration ===
    
    /**
     * Initialize AI processing for a call
     */
    suspend fun initializeAI(callId: String, context: CallContext): AIInitResponse? {
        return try {
            val requestBody = JsonObject().apply {
                addProperty("callId", callId)
                addProperty("fromNumber", context.fromNumber)
                addProperty("toNumber", context.toNumber)
                addProperty("direction", context.direction)
                addProperty("contactName", context.contactName)
                addProperty("callTime", context.callTime)
            }
            
            val request = Request.Builder()
                .url("$baseUrl/api/ai/initialize/$callId")
                .post(requestBody.toString().toRequestBody(JSON))
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string()
                json?.let { 
                    val result = gson.fromJson(it, JsonObject::class.java)
                    if (result.get("success").asBoolean) {
                        gson.fromJson(result.get("response"), AIInitResponse::class.java)
                    } else null
                }
            } else {
                Log.e(TAG, "Failed to initialize AI: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AI", e)
            null
        }
    }
    
    /**
     * Process speech input through AI service
     */
    suspend fun processAudioInput(callId: String, audioText: String): AIResponse? {
        return try {
            val requestBody = JsonObject().apply {
                addProperty("text", audioText)
                addProperty("timestamp", System.currentTimeMillis())
            }
            
            val request = Request.Builder()
                .url("$baseUrl/api/ai/process-audio/$callId")
                .post(requestBody.toString().toRequestBody(JSON))
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string()
                json?.let { 
                    val result = gson.fromJson(it, JsonObject::class.java)
                    if (result.get("success").asBoolean) {
                        gson.fromJson(result.get("response"), AIResponse::class.java)
                    } else null
                }
            } else {
                Log.e(TAG, "Failed to process audio: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing audio", e)
            null
        }
    }
    
    // === Agent Integration ===
    
    /**
     * Find available human agent
     */
    suspend fun findAvailableAgent(callId: String, requirements: AgentRequirements): Agent? {
        return try {
            val requestBody = JsonObject().apply {
                addProperty("callId", callId)
                addProperty("skills", gson.toJson(requirements.skills))
                addProperty("language", requirements.language)
                addProperty("priority", requirements.priority)
            }
            
            val request = Request.Builder()
                .url("$baseUrl/api/agents/find")
                .post(requestBody.toString().toRequestBody(JSON))
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string()
                json?.let { 
                    val result = gson.fromJson(it, JsonObject::class.java)
                    if (result.get("success").asBoolean) {
                        gson.fromJson(result.get("agent"), Agent::class.java)
                    } else null
                }
            } else {
                Log.e(TAG, "Failed to find agent: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding agent", e)
            null
        }
    }
    
    /**
     * Transfer call to human agent
     */
    suspend fun transferToAgent(callId: String, agentId: String): TransferResult? {
        return try {
            val requestBody = JsonObject().apply {
                addProperty("agentId", agentId)
                addProperty("transferType", "warm")
            }
            
            val request = Request.Builder()
                .url("$baseUrl/api/agents/assign/$callId")
                .post(requestBody.toString().toRequestBody(JSON))
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string()
                json?.let { 
                    val result = gson.fromJson(it, JsonObject::class.java)
                    if (result.get("success").asBoolean) {
                        gson.fromJson(result.get("result"), TransferResult::class.java)
                    } else null
                }
            } else {
                Log.e(TAG, "Failed to transfer to agent: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error transferring to agent", e)
            null
        }
    }
    
    // === Call Analytics ===
    
    /**
     * Log call completion for analytics
     */
    suspend fun logCallCompletion(callId: String, callResult: CallResult): Boolean {
        return try {
            val requestBody = JsonObject().apply {
                addProperty("callId", callId)
                addProperty("duration", callResult.duration)
                addProperty("endReason", callResult.endReason)
                addProperty("satisfaction", callResult.satisfaction)
                addProperty("resolved", callResult.resolved)
                addProperty("aiHandled", callResult.aiHandled)
                addProperty("transferredToAgent", callResult.transferredToAgent)
            }
            
            val request = Request.Builder()
                .url("$baseUrl/api/calls/complete")
                .post(requestBody.toString().toRequestBody(JSON))
                .build()
            
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error logging call completion", e)
            false
        }
    }
    
    // === Health Check ===
    
    /**
     * Check if Node.js server is available
     */
    suspend fun isServerAvailable(): Boolean {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/health")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Server health check failed", e)
            false
        }
    }
}

// === Data Models ===

data class SystemStatus(
    val system: String,
    val timestamp: String,
    val services: Services
)

data class Services(
    val carrier: CarrierStatus?,
    val agents: AgentStats?,
    val ivr: IVRStats?,
    val activeServices: List<String>
)

data class CarrierStatus(
    val connected: Boolean,
    val provider: String,
    val quality: String
)

data class AgentStats(
    val total: Int,
    val available: Int,
    val busy: Int
)

data class IVRStats(
    val availableMenus: Int,
    val activeSessions: Int
)

data class IVRSession(
    val sessionId: String,
    val callId: String,
    val menuId: String,
    val currentMenu: String,
    val attempts: Int,
    val startTime: Long
)

data class IVRResponse(
    val action: String,
    val destination: String?,
    val message: String?,
    val shouldTransfer: Boolean,
    val nextMenu: String?
)

data class CallContext(
    val fromNumber: String,
    val toNumber: String,
    val direction: String,
    val contactName: String?,
    val callTime: Long
)

data class AIInitResponse(
    val sessionId: String,
    val contextInitialized: Boolean,
    val initialPrompt: String
)

data class AIResponse(
    val text: String,
    val action: String?,
    val confidence: Float,
    val shouldSpeak: Boolean,
    val endConversation: Boolean
)

data class AgentRequirements(
    val skills: List<String>,
    val language: String,
    val priority: String
)

data class Agent(
    val id: String,
    val name: String,
    val skills: List<String>,
    val status: String,
    val averageRating: Float
)

data class TransferResult(
    val success: Boolean,
    val agentId: String,
    val transferTime: Long,
    val estimatedWaitTime: Int
)

data class CallResult(
    val duration: Long,
    val endReason: String,
    val satisfaction: Int?,
    val resolved: Boolean,
    val aiHandled: Boolean,
    val transferredToAgent: Boolean
)