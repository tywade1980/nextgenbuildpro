package com.example.modeldownloader

import android.app.Application
import android.content.Context

/**
 * AppGlobals provides a globally accessible application context.  This
 * allows utility classes such as [AuthManager] to store and retrieve
 * information from [android.content.SharedPreferences] without holding a
 * direct reference to an [android.app.Activity].
 */
class AppGlobals : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        @JvmStatic
        lateinit var appContext: Context
            private set
    }
}