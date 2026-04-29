package com.nextgentele.ai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Build
import androidx.core.app.NotificationCompat
import com.nextgentele.ai.MainActivity
import com.nextgentele.ai.R
import com.nextgentele.ai.ai.AICallProcessor

class CallManagementService : Service() {
    
    private lateinit var aiCallProcessor: AICallProcessor
    private lateinit var notificationManager: NotificationManager
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "call_management_channel"
        private const val CHANNEL_NAME = "AI Call Management"
    }
    
    override fun onCreate() {
        super.onCreate()
        initializeNotificationChannel()
        aiCallProcessor = AICallProcessor(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        aiCallProcessor.startProcessing()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        aiCallProcessor.stopProcessing()
    }
    
    private fun initializeNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for AI call management service"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.ai_call_handler_notification_title))
            .setContentText(getString(R.string.ai_call_handler_notification_text))
            .setSmallIcon(R.drawable.ic_phone_24)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}