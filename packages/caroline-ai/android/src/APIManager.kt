package com.minime.codeassist.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class APIManager(context: Context) {

    private val sharedPrefs: SharedPreferences

    init {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPrefs = EncryptedSharedPreferences.create(
            "secure_api_storage",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveApiKey(serviceName: String, apiKey: String) {
        val encryptedKey = Base64.encodeToString(apiKey.toByteArray(), Base64.DEFAULT)
        sharedPrefs.edit().putString(serviceName, encryptedKey).apply()
    }

    fun getApiKey(serviceName: String): String? {
        val encryptedKey = sharedPrefs.getString(serviceName, null)
        return encryptedKey?.let {
            String(Base64.decode(it, Base64.DEFAULT))
        }
    }

    fun deleteApiKey(serviceName: String) {
        sharedPrefs.edit().remove(serviceName).apply()
    }

    fun listAllServices(): Set<String> {
        return sharedPrefs.all.keys
    }
}
