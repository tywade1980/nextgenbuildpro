package com.example.telephonyagent

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ApiManager centralises the creation and configuration of Retrofit
 * clients for calling external services such as CRMs, project
 * management tools, communication platforms and speech providers. It
 * retrieves API keys via the SecretManager and injects them into
 * request headers automatically. Add additional service interfaces as
 * needed for your workflow automation.
 */
class ApiManager(context: Context) {
    private val secretManager = SecretManager(context)

    // Example Retrofit service for a CRM
    val crmService: CrmService by lazy { buildService(getBaseUrl("CRM_BASE_URL"), CrmService::class.java) }

    // Example Retrofit service for Slack
    val slackService: SlackService by lazy { buildService(getBaseUrl("SLACK_BASE_URL"), SlackService::class.java) }

    /**
     * Builds a Retrofit service with logging and API key injection. The
     * name of the API key stored in SecretManager should follow the
     * pattern `${serviceName}_API_KEY`.
     */
    private fun <T> buildService(baseUrl: String, serviceClass: Class<T>): T {
        val keyName = serviceClass.simpleName.uppercase() + "_API_KEY"
        val apiKey = secretManager.getSecret(keyName) ?: ""
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(serviceClass)
    }

    /**
     * Helper to obtain base URLs from secrets. Base URLs should be
     * provisioned in SecretManager under keys like `CRM_BASE_URL`.
     */
    private fun getBaseUrl(name: String): String {
        return secretManager.getSecret(name) ?: throw IllegalStateException("Missing base URL for $name")
    }
}

// Example service definitions. Replace with actual API endpoints.
interface CrmService {
    // Define Retrofit annotated methods here
}

interface SlackService {
    // Define Retrofit annotated methods here
}