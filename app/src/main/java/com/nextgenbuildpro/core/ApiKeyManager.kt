package com.nextgenbuildpro.core

import android.content.Context
import android.util.Log
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties

/**
 * Utility class for managing API keys and sensitive configuration values
 * Reads values from local.properties file which should not be committed to version control
 */
object ApiKeyManager {
    private const val TAG = "ApiKeyManager"
    private val properties = Properties()
    private var isInitialized = false

    /**
     * Initialize the API key manager
     * This should be called early in the application lifecycle, such as in Application.onCreate()
     * @param context The application context
     * @return True if initialization was successful, false otherwise
     */
    fun initialize(context: Context): Boolean {
        if (isInitialized) {
            return true
        }

        return try {
            // Get the path to the local.properties file
            val localPropertiesFile = context.filesDir.parentFile?.parentFile?.parentFile?.parentFile?.resolve("local.properties")
            
            if (localPropertiesFile?.exists() == true) {
                // Load the properties from the file
                FileInputStream(localPropertiesFile).use { inputStream ->
                    properties.load(inputStream)
                }
                isInitialized = true
                Log.d(TAG, "API keys loaded successfully")
                true
            } else {
                Log.e(TAG, "local.properties file not found at ${localPropertiesFile?.absolutePath}")
                false
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error loading API keys", e)
            false
        }
    }

    /**
     * Get the Google API key
     * @return The Google API key or null if not found
     */
    fun getGoogleApiKey(): String? {
        return properties.getProperty("google.api.key")
    }

    /**
     * Get the Firebase Storage URL
     * @return The Firebase Storage URL or null if not found
     */
    fun getFirebaseStorageUrl(): String? {
        return properties.getProperty("firebase.storage.url")
    }

    /**
     * Get a property value by key
     * @param key The property key
     * @param defaultValue The default value to return if the property is not found
     * @return The property value or the default value if not found
     */
    fun getProperty(key: String, defaultValue: String? = null): String? {
        return properties.getProperty(key, defaultValue)
    }
}