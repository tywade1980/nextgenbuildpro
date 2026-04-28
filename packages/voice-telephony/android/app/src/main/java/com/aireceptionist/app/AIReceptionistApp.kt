package com.aireceptionist.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkManager
import com.aireceptionist.app.ai.agents.AgentManager
import com.aireceptionist.app.data.database.AIDatabase
import com.aireceptionist.app.utils.Logger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AIReceptionistApp : Application(), Configuration.Provider {

    @Inject
    lateinit var agentManager: AgentManager

    @Inject
    lateinit var database: AIDatabase

    companion object {
        const val CALL_NOTIFICATION_CHANNEL_ID = "call_notifications"
        const val AI_PROCESSING_CHANNEL_ID = "ai_processing"
        const val GENERAL_CHANNEL_ID = "general_notifications"
        
        lateinit var instance: AIReceptionistApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        Logger.init(this)
        createNotificationChannels()
        initializeAI()
        
        Logger.d("AIReceptionistApp", "Application started")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Call notifications channel
            val callChannel = NotificationChannel(
                CALL_NOTIFICATION_CHANNEL_ID,
                "Call Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming and ongoing calls"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // AI processing channel
            val aiChannel = NotificationChannel(
                AI_PROCESSING_CHANNEL_ID,
                "AI Processing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background AI processing notifications"
                enableVibration(false)
                setShowBadge(false)
            }
            
            // General notifications channel
            val generalChannel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
                enableVibration(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannels(listOf(callChannel, aiChannel, generalChannel))
        }
    }

    private fun initializeAI() {
        try {
            agentManager.initialize()
            Logger.i("AIReceptionistApp", "AI agents initialized successfully")
        } catch (e: Exception) {
            Logger.e("AIReceptionistApp", "Failed to initialize AI agents", e)
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }

    override fun onTerminate() {
        super.onTerminate()
        agentManager.shutdown()
        Logger.d("AIReceptionistApp", "Application terminated")
    }
}