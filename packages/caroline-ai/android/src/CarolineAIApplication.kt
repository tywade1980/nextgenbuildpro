package com.caroline.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * CarolineAI — Executive Assistant Application
 *
 * Consolidated from: caroline-alpha, caroline-android
 * Features:
 *  - On-device call handling & screening
 *  - xAI Grok + ElevenLabs TTS voice assistant
 *  - Realtime WebSocket voice sessions
 *  - Calendar / email / contact management
 *  - Construction-domain knowledge (NextGen BuildPro integration)
 */
class CarolineAIApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        private const val TAG = "CarolineAI"
        const val NOTIFICATION_CHANNEL_CALLS = "caroline_calls"
        const val NOTIFICATION_CHANNEL_ASSISTANT = "caroline_assistant"

        lateinit var instance: CarolineAIApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
        applicationScope.launch {
            CarolineServiceLocator.initialize(this@CarolineAIApplication)
        }
        Log.i(TAG, "CarolineAI initialized")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_CALLS,
                    "Call Handling",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "AI call screening and receptionist notifications" }
            )
            nm.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ASSISTANT,
                    "Caroline Assistant",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Executive assistant alerts and reminders" }
            )
        }
    }
}
