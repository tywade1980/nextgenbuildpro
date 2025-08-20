package com.nextgenbuildpro.core.api

import android.content.Context

/**
 * Interface for securely storing and retrieving API credentials and sensitive information.
 * 
 * This is a delicate API that abstracts credential storage operations.
 * It provides a layer of abstraction to make it easier to:
 * 1. Store credentials securely using platform-appropriate mechanisms
 * 2. Implement credential rotation policies
 * 3. Apply the principle of least privilege
 * 4. Avoid hardcoding sensitive information
 */
interface SecureCredentialStorage {
    /**
     * Initialize the secure storage
     * @param context The application context
     * @return True if initialization was successful, false otherwise
     */
    fun initialize(context: Context): Boolean

    /**
     * Store a credential securely
     * @param key The key to identify the credential
     * @param value The credential value to store
     * @return True if the credential was stored successfully, false otherwise
     */
    fun storeCredential(key: String, value: String): Boolean

    /**
     * Retrieve a credential
     * @param key The key identifying the credential
     * @return The credential value or null if not found
     */
    fun getCredential(key: String): String?

    /**
     * Remove a credential
     * @param key The key identifying the credential
     * @return True if the credential was removed successfully, false otherwise
     */
    fun removeCredential(key: String): Boolean

    /**
     * Check if a credential exists
     * @param key The key identifying the credential
     * @return True if the credential exists, false otherwise
     */
    fun hasCredential(key: String): Boolean

    /**
     * Rotate a credential (replace with a new value)
     * @param key The key identifying the credential
     * @param newValue The new credential value
     * @return True if the credential was rotated successfully, false otherwise
     */
    fun rotateCredential(key: String, newValue: String): Boolean

    /**
     * Get the creation timestamp for a credential
     * @param key The key identifying the credential
     * @return The timestamp when the credential was created or last rotated, or null if not found
     */
    fun getCredentialTimestamp(key: String): Long?

    /**
     * Check if a credential needs rotation based on age
     * @param key The key identifying the credential
     * @param maxAgeMillis The maximum age in milliseconds
     * @return True if the credential needs rotation, false otherwise
     */
    fun isCredentialExpired(key: String, maxAgeMillis: Long): Boolean

    companion object {
        /**
         * Key for Google API key
         */
        const val KEY_GOOGLE_API = "google_api_key"

        /**
         * Key for Firebase Storage URL
         */
        const val KEY_FIREBASE_STORAGE_URL = "firebase_storage_url"

        /**
         * Key for Speech API (STT/TTS)
         */
        const val KEY_SPEECH_API = "speech_api_key"

        /**
         * Default maximum age for credentials (90 days)
         */
        const val DEFAULT_CREDENTIAL_MAX_AGE_MILLIS = 90 * 24 * 60 * 60 * 1000L
    }
}
