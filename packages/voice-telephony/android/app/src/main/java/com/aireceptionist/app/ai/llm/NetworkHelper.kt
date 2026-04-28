package com.aireceptionist.app.ai.llm

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Network helper for model downloading
 */
object NetworkHelper {
    
    /**
     * Check if device has internet connectivity
     */
    fun hasInternetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Check if Hugging Face is accessible
     */
    suspend fun canAccessHuggingFace(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL("https://huggingface.co/microsoft/Phi-3.5-mini-instruct-onnx")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "HEAD"
            
            val responseCode = connection.responseCode
            connection.disconnect()
            
            responseCode == HttpURLConnection.HTTP_OK
            
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get connection type for user information
     */
    fun getConnectionType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "No Connection"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Unknown"
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Unknown"
        }
    }
    
    /**
     * Check if connection is metered (mobile data)
     */
    fun isMeteredConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.isActiveNetworkMetered
    }
    
    /**
     * Get recommended download advice based on connection
     */
    fun getDownloadAdvice(context: Context): String {
        val connectionType = getConnectionType(context)
        val isMetered = isMeteredConnection(context)
        
        return when {
            connectionType == "WiFi" -> "✅ WiFi detected - Perfect for large downloads"
            connectionType == "Mobile Data" && !isMetered -> "📱 Mobile data (unlimited) - Download OK"
            connectionType == "Mobile Data" && isMetered -> "⚠️ Mobile data (metered) - Large download may use data allowance"
            connectionType == "No Connection" -> "❌ No internet connection - Connect to WiFi or mobile data"
            else -> "🔍 Connection detected - Ready to download"
        }
    }
}