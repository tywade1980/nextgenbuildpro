package com.nextgenbuildpro.core.api.impl

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.nextgenbuildpro.core.ApiKeyManager
import com.nextgenbuildpro.core.api.SecureCredentialStorage
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Implementation of SecureCredentialStorage interface.
 * 
 * This class securely stores and retrieves API credentials and sensitive information.
 * It provides:
 * 1. Basic encryption for storing credentials in SharedPreferences
 * 2. Credential rotation policies
 * 3. Timestamp tracking for credentials
 * 4. Fallback to ApiKeyManager for backward compatibility
 */
class SecureCredentialStorageImpl : SecureCredentialStorage {
    
    private val TAG = "SecureCredentialStorage"
    private lateinit var sharedPreferences: SharedPreferences
    private val credentialTimestamps = ConcurrentHashMap<String, Long>()
    private var isInitialized = false
    private lateinit var encryptionKey: SecretKeySpec
    
    /**
     * Initialize the secure storage
     * @param context The application context
     * @return True if initialization was successful, false otherwise
     */
    override fun initialize(context: Context): Boolean {
        if (isInitialized) {
            return true
        }
        
        return try {
            // Create the SharedPreferences
            sharedPreferences = context.getSharedPreferences(
                "secure_api_credentials",
                Context.MODE_PRIVATE
            )
            
            // Generate encryption key from device-specific information
            val deviceId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
            
            // Create a key from the device ID
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(deviceId.toByteArray())
            encryptionKey = SecretKeySpec(bytes, "AES")
            
            // Load existing credentials from ApiKeyManager for backward compatibility
            migrateFromApiKeyManager()
            
            isInitialized = true
            Log.d(TAG, "Secure credential storage initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing secure credential storage", e)
            false
        }
    }
    
    /**
     * Store a credential securely
     * @param key The key to identify the credential
     * @param value The credential value to store
     * @return True if the credential was stored successfully, false otherwise
     */
    override fun storeCredential(key: String, value: String): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "Secure credential storage not initialized")
            return false
        }
        
        return try {
            // Encrypt the credential
            val encryptedValue = encrypt(value)
            
            // Store the encrypted credential
            sharedPreferences.edit().putString(key, encryptedValue).apply()
            
            // Update the timestamp
            val timestamp = System.currentTimeMillis()
            credentialTimestamps[key] = timestamp
            sharedPreferences.edit().putLong("${key}_timestamp", timestamp).apply()
            
            Log.d(TAG, "Credential stored successfully: $key")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error storing credential: $key", e)
            false
        }
    }
    
    /**
     * Retrieve a credential
     * @param key The key identifying the credential
     * @return The credential value or null if not found
     */
    override fun getCredential(key: String): String? {
        if (!isInitialized) {
            Log.e(TAG, "Secure credential storage not initialized")
            return null
        }
        
        return try {
            // Try to get from SharedPreferences
            val encryptedValue = sharedPreferences.getString(key, null)
            
            // If not found, try to get from ApiKeyManager for backward compatibility
            if (encryptedValue == null) {
                return ApiKeyManager.getProperty(key)
            }
            
            // Decrypt the value
            decrypt(encryptedValue)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving credential: $key", e)
            // Try to get from ApiKeyManager as fallback
            ApiKeyManager.getProperty(key)
        }
    }
    
    /**
     * Remove a credential
     * @param key The key identifying the credential
     * @return True if the credential was removed successfully, false otherwise
     */
    override fun removeCredential(key: String): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "Secure credential storage not initialized")
            return false
        }
        
        return try {
            // Remove the credential
            sharedPreferences.edit().remove(key).apply()
            
            // Remove the timestamp
            credentialTimestamps.remove(key)
            sharedPreferences.edit().remove("${key}_timestamp").apply()
            
            Log.d(TAG, "Credential removed successfully: $key")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing credential: $key", e)
            false
        }
    }
    
    /**
     * Check if a credential exists
     * @param key The key identifying the credential
     * @return True if the credential exists, false otherwise
     */
    override fun hasCredential(key: String): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "Secure credential storage not initialized")
            return false
        }
        
        return try {
            // Check if the credential exists in SharedPreferences
            val hasInPrefs = sharedPreferences.contains(key)
            
            // If not found, check if it exists in ApiKeyManager for backward compatibility
            if (!hasInPrefs) {
                return ApiKeyManager.getProperty(key) != null
            }
            
            hasInPrefs
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if credential exists: $key", e)
            // Try to check in ApiKeyManager as fallback
            ApiKeyManager.getProperty(key) != null
        }
    }
    
    /**
     * Rotate a credential (replace with a new value)
     * @param key The key identifying the credential
     * @param newValue The new credential value
     * @return True if the credential was rotated successfully, false otherwise
     */
    override fun rotateCredential(key: String, newValue: String): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "Secure credential storage not initialized")
            return false
        }
        
        return try {
            // Store the new credential
            val result = storeCredential(key, newValue)
            
            if (result) {
                Log.d(TAG, "Credential rotated successfully: $key")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error rotating credential: $key", e)
            false
        }
    }
    
    /**
     * Get the creation timestamp for a credential
     * @param key The key identifying the credential
     * @return The timestamp when the credential was created or last rotated, or null if not found
     */
    override fun getCredentialTimestamp(key: String): Long? {
        if (!isInitialized) {
            Log.e(TAG, "Secure credential storage not initialized")
            return null
        }
        
        return try {
            // Try to get from memory cache first
            var timestamp = credentialTimestamps[key]
            
            // If not in memory, try to get from SharedPreferences
            if (timestamp == null) {
                timestamp = sharedPreferences.getLong("${key}_timestamp", 0)
                if (timestamp > 0) {
                    credentialTimestamps[key] = timestamp
                } else {
                    return null
                }
            }
            
            timestamp
        } catch (e: Exception) {
            Log.e(TAG, "Error getting credential timestamp: $key", e)
            null
        }
    }
    
    /**
     * Check if a credential needs rotation based on age
     * @param key The key identifying the credential
     * @param maxAgeMillis The maximum age in milliseconds
     * @return True if the credential needs rotation, false otherwise
     */
    override fun isCredentialExpired(key: String, maxAgeMillis: Long): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "Secure credential storage not initialized")
            return false
        }
        
        val timestamp = getCredentialTimestamp(key) ?: return false
        val currentTime = System.currentTimeMillis()
        val age = currentTime - timestamp
        
        return age > maxAgeMillis
    }
    
    /**
     * Encrypt a string value
     * @param value The value to encrypt
     * @return The encrypted value as a Base64-encoded string
     */
    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey)
        val encryptedBytes = cipher.doFinal(value.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }
    
    /**
     * Decrypt a Base64-encoded encrypted string
     * @param encryptedValue The encrypted value as a Base64-encoded string
     * @return The decrypted value
     */
    private fun decrypt(encryptedValue: String): String {
        val encryptedBytes = Base64.decode(encryptedValue, Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
    
    /**
     * Migrate credentials from ApiKeyManager to secure storage
     */
    private fun migrateFromApiKeyManager() {
        try {
            // Migrate Google API key
            val googleApiKey = ApiKeyManager.getGoogleApiKey()
            if (googleApiKey != null && !hasCredential(SecureCredentialStorage.KEY_GOOGLE_API)) {
                storeCredential(SecureCredentialStorage.KEY_GOOGLE_API, googleApiKey)
                Log.d(TAG, "Migrated Google API key from ApiKeyManager")
            }
            
            // Migrate Firebase Storage URL
            val firebaseStorageUrl = ApiKeyManager.getFirebaseStorageUrl()
            if (firebaseStorageUrl != null && !hasCredential(SecureCredentialStorage.KEY_FIREBASE_STORAGE_URL)) {
                storeCredential(SecureCredentialStorage.KEY_FIREBASE_STORAGE_URL, firebaseStorageUrl)
                Log.d(TAG, "Migrated Firebase Storage URL from ApiKeyManager")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating credentials from ApiKeyManager", e)
        }
    }
}