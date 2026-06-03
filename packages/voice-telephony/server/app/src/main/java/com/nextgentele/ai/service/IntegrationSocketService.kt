package com.nextgentele.ai.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.nextgentele.ai.integration.SocketServer
import com.nextgentele.ai.integration.CRMIntegration
import com.nextgentele.ai.integration.CalendarIntegration

class IntegrationSocketService : Service() {
    
    private lateinit var socketServer: SocketServer
    private lateinit var crmIntegration: CRMIntegration
    private lateinit var calendarIntegration: CalendarIntegration
    
    companion object {
        private const val TAG = "IntegrationSocketService"
        const val SOCKET_PORT = 8080
    }
    
    override fun onCreate() {
        super.onCreate()
        initializeIntegrations()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startSocketServer()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopSocketServer()
    }
    
    private fun initializeIntegrations() {
        crmIntegration = CRMIntegration(this)
        calendarIntegration = CalendarIntegration(this)
        socketServer = SocketServer(SOCKET_PORT, this)
    }
    
    private fun startSocketServer() {
        try {
            socketServer.start()
            Log.d(TAG, "Socket server started on port $SOCKET_PORT")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start socket server", e)
        }
    }
    
    private fun stopSocketServer() {
        try {
            socketServer.stop()
            Log.d(TAG, "Socket server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping socket server", e)
        }
    }
    
    fun getCrmIntegration(): CRMIntegration = crmIntegration
    fun getCalendarIntegration(): CalendarIntegration = calendarIntegration
}