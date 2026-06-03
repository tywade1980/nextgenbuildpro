package com.example.telephonyagent

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * SecretManager securely stores and retrieves API keys and sensitive
 * configuration values. In production, secrets should be provisioned
 * through a secure channel (e.g. Android Keystore, encrypted config) and
 * never hard‑coded. For demonstration this class uses
 * EncryptedSharedPreferences backed by the Android Keystore.
 */
class SecretManager(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "telephony_agent_secrets",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Retrieves a secret value (e.g. API key) by name. Returns null if the
     * key is absent.
     */
    fun getSecret(name: String): String? {
        return prefs.getString(name, null)
    }

    /**
     * Stores a secret value. In a production app you should restrict this
     * operation and provide secure provisioning rather than exposing setter
     * methods.
     */
    fun setSecret(name: String, value: String) {
        prefs.edit().putString(name, value).apply()
    }
}