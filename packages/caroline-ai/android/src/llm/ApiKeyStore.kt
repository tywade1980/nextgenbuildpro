package com.caroline.ai.llm

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure API key storage using EncryptedSharedPreferences.
 * Falls back to BuildConfig/local.properties for development.
 */
class ApiKeyStore(context: Context) {

    private val prefs = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "caroline_api_keys",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("caroline_api_keys_fallback", Context.MODE_PRIVATE)
    }

    fun get(key: String): String? {
        val stored = prefs.getString(key, null)
        if (!stored.isNullOrBlank()) return stored
        // Fall through to build-time injection via manifest meta-data
        return try {
            val ai = context.packageManager.getApplicationInfo(
                context.packageName, android.content.pm.PackageManager.GET_META_DATA
            )
            ai.metaData?.getString(key)
        } catch (_: Exception) { null }
    }

    fun set(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun clear(key: String) {
        prefs.edit().remove(key).apply()
    }

    private val context: Context = context.applicationContext
}
