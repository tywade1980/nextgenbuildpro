package com.aireceptionist.app.api

import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for external API integrations
 */
@Singleton
class ExternalAPIManager @Inject constructor() {
    
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var retrofit: Retrofit
    private lateinit var gson: Gson
    private var isInitialized = false
    
    suspend fun initialize() {
        if (isInitialized) return
        
        try {
            Logger.i(TAG, "Initializing External API Manager")
            
            gson = Gson()
            
            // Configure OkHttp client
            okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("User-Agent", "AI-Receptionist/1.0")
                        .addHeader("Content-Type", "application/json")
                        .build()
                    chain.proceed(request)
                }
                .build()
            
            // Configure Retrofit
            retrofit = Retrofit.Builder()
                .baseUrl("https://api.example.com/") // Base URL for default API
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            
            isInitialized = true
            Logger.i(TAG, "External API Manager initialized")
            
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize External API Manager", e)
            throw e
        }
    }
    
    // CRM Integration
    suspend fun lookupCustomer(phoneNumber: String): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Looking up customer: $phoneNumber")
            
            // Example CRM API call
            val response = makeAPICall(
                endpoint = "https://crm-api.example.com/customers/lookup",
                method = "GET",
                headers = mapOf(
                    "Authorization" to "Bearer ${getCRMApiKey()}",
                    "X-Phone-Number" to phoneNumber
                )
            )
            
            response ?: mapOf(
                "customer_id" to "unknown",
                "name" to "Unknown Customer",
                "phone" to phoneNumber,
                "status" to "new",
                "notes" to "No CRM record found"
            )
            
        } catch (e: Exception) {
            Logger.e(TAG, "CRM lookup failed", e)
            mapOf(
                "error" to "CRM lookup failed",
                "phone" to phoneNumber
            )
        }
    }
    
    // Calendar Integration
    suspend fun checkCalendarAvailability(): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Checking calendar availability")
            
            val response = makeAPICall(
                endpoint = "https://calendar-api.example.com/availability",
                method = "GET",
                headers = mapOf(
                    "Authorization" to "Bearer ${getCalendarApiKey()}"
                )
            )
            
            response ?: mapOf(
                "available_slots" to listOf(
                    "2024-01-15T09:00:00Z",
                    "2024-01-15T10:00:00Z",
                    "2024-01-15T14:00:00Z"
                ),
                "timezone" to "America/New_York"
            )
            
        } catch (e: Exception) {
            Logger.e(TAG, "Calendar check failed", e)
            mapOf("error" to "Calendar check failed")
        }
    }
    
    // Email Integration
    suspend fun sendEmail(recipient: String, subject: String, message: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Sending email to: $recipient")
            
            val payload = mapOf(
                "to" to recipient,
                "subject" to subject,
                "body" to message,
                "from" to "ai-receptionist@company.com"
            )
            
            val response = makeAPICall(
                endpoint = "https://email-api.example.com/send",
                method = "POST",
                headers = mapOf(
                    "Authorization" to "Bearer ${getEmailApiKey()}"
                ),
                body = payload
            )
            
            response?.get("status") == "sent"
            
        } catch (e: Exception) {
            Logger.e(TAG, "Email send failed", e)
            false
        }
    }
    
    // SMS Integration
    suspend fun sendSMS(phoneNumber: String, message: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Sending SMS to: $phoneNumber")
            
            val payload = mapOf(
                "to" to phoneNumber,
                "message" to message,
                "from" to "AI-Receptionist"
            )
            
            val response = makeAPICall(
                endpoint = "https://sms-api.example.com/send",
                method = "POST",
                headers = mapOf(
                    "Authorization" to "Bearer ${getSMSApiKey()}"
                ),
                body = payload
            )
            
            response?.get("status") == "delivered"
            
        } catch (e: Exception) {
            Logger.e(TAG, "SMS send failed", e)
            false
        }
    }
    
    // Slack Integration
    suspend fun sendSlackMessage(channel: String, message: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Sending Slack message to: $channel")
            
            val payload = mapOf(
                "channel" to channel,
                "text" to message,
                "username" to "AI Receptionist",
                "icon_emoji" to ":robot_face:"
            )
            
            val response = makeAPICall(
                endpoint = "https://hooks.slack.com/services/${getSlackWebhookToken()}",
                method = "POST",
                body = payload
            )
            
            true // Slack webhook typically returns 200 OK on success
            
        } catch (e: Exception) {
            Logger.e(TAG, "Slack message send failed", e)
            false
        }
    }
    
    // Database Query Integration
    suspend fun getCustomerHistory(parameters: Map<String, Any>): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val customerId = parameters["customer_id"] as? String ?: return@withContext emptyMap()
            
            val response = makeAPICall(
                endpoint = "https://api.example.com/customers/$customerId/history",
                method = "GET",
                headers = mapOf(
                    "Authorization" to "Bearer ${getAPIKey()}"
                )
            )
            
            response ?: emptyMap()
            
        } catch (e: Exception) {
            Logger.e(TAG, "Customer history query failed", e)
            emptyMap()
        }
    }
    
    suspend fun getAvailableSlots(parameters: Map<String, Any>): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val date = parameters["date"] as? String ?: return@withContext emptyMap()
            
            val response = makeAPICall(
                endpoint = "https://api.example.com/appointments/available-slots",
                method = "GET",
                headers = mapOf(
                    "Authorization" to "Bearer ${getAPIKey()}"
                ),
                body = mapOf("date" to date)
            )
            
            response ?: emptyMap()
            
        } catch (e: Exception) {
            Logger.e(TAG, "Available slots query failed", e)
            emptyMap()
        }
    }
    
    suspend fun getServiceCatalog(parameters: Map<String, Any>): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val response = makeAPICall(
                endpoint = "https://api.example.com/services/catalog",
                method = "GET",
                headers = mapOf(
                    "Authorization" to "Bearer ${getAPIKey()}"
                )
            )
            
            response ?: mapOf(
                "services" to listOf(
                    mapOf("id" to "consultation", "name" to "Consultation", "duration" to 60),
                    mapOf("id" to "checkup", "name" to "Check-up", "duration" to 30),
                    mapOf("id" to "followup", "name" to "Follow-up", "duration" to 45)
                )
            )
            
        } catch (e: Exception) {
            Logger.e(TAG, "Service catalog query failed", e)
            emptyMap()
        }
    }
    
    // Webhook Integration
    suspend fun triggerWebhook(webhookUrl: String, payload: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Triggering webhook: $webhookUrl")
            
            makeAPICall(
                endpoint = webhookUrl,
                method = "POST",
                body = payload
            )
            
            true
            
        } catch (e: Exception) {
            Logger.e(TAG, "Webhook trigger failed", e)
            false
        }
    }
    
    // Generic API Call Method
    suspend fun makeAPICall(
        endpoint: String,
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        body: Map<String, Any>? = null
    ): Map<String, Any>? = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url(endpoint)
            
            // Add headers
            headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }
            
            // Add body for POST/PUT requests
            when (method.uppercase()) {
                "POST", "PUT", "PATCH" -> {
                    val jsonBody = body?.let { gson.toJson(it) } ?: "{}"
                    val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
                    requestBuilder.method(method, requestBody)
                }
                else -> {
                    requestBuilder.method(method, null)
                }
            }
            
            val request = requestBuilder.build()
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrBlank()) {
                    val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
                    return@withContext gson.fromJson(jsonResponse, Map::class.java) as Map<String, Any>
                }
            } else {
                Logger.w(TAG, "API call failed: ${response.code} ${response.message}")
            }
            
            null
            
        } catch (e: Exception) {
            Logger.e(TAG, "API call error for $endpoint", e)
            null
        }
    }
    
    // Configuration methods (in production, these would read from secure storage)
    private fun getAPIKey(): String = "your-api-key-here"
    private fun getCRMApiKey(): String = "your-crm-api-key"
    private fun getCalendarApiKey(): String = "your-calendar-api-key"
    private fun getEmailApiKey(): String = "your-email-api-key"
    private fun getSMSApiKey(): String = "your-sms-api-key"
    private fun getSlackWebhookToken(): String = "your-slack-webhook-token"
    
    fun isHealthy(): Boolean {
        return isInitialized
    }
    
    suspend fun shutdown() {
        isInitialized = false
        Logger.i(TAG, "External API Manager shutdown")
    }
    
    companion object {
        private const val TAG = "ExternalAPIManager"
    }
}