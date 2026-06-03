package com.nextgenbuildpro.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nextgenbuildpro.brain.UnifiedBrainService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Keeps UnifiedBrainService warm in the background.
 * Mirrors unified-brain/unified_brain_api.py FastAPI server lifecycle.
 */
@AndroidEntryPoint
class UnifiedBrainBackgroundService : Service() {

    @Inject lateinit var unifiedBrainService: UnifiedBrainService

    private val CHANNEL_ID = "brain_channel"
    private val NOTIF_ID = 1002

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NextGen AI")
            .setContentText("Unified Brain active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Unified Brain",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}
