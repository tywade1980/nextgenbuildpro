package com.nextgenbuildpro.clientengagement.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nextgenbuildpro.MainActivity
import com.nextgenbuildpro.clientengagement.data.model.SignatureRequest
import com.nextgenbuildpro.clientengagement.data.model.SignatureRequestStatus
import com.nextgenbuildpro.clientengagement.data.repository.DigitalSignatureRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Service for handling notifications related to digital signatures
 */
class NotificationService(
    private val context: Context,
    private val signatureRepository: DigitalSignatureRepository
) {
    private val TAG = "NotificationService"
    private val CHANNEL_ID = "digital_signature_channel"
    private val NOTIFICATION_GROUP = "digital_signature_group"
    
    init {
        // Create the notification channel
        createNotificationChannel()
    }
    
    /**
     * Create the notification channel for digital signature notifications
     */
    private fun createNotificationChannel() {
        // Create the notification channel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Digital Signatures"
            val descriptionText = "Notifications for digital signature requests and updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            // Register the channel with the system
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }
    
    /**
     * Send a notification for a new signature request
     * 
     * @param request The signature request to send a notification for
     */
    suspend fun sendSignatureRequestNotification(request: SignatureRequest) = withContext(Dispatchers.IO) {
        try {
            // Get the document details
            val document = signatureRepository.getSignableDocumentById(request.documentId)
            
            if (document != null) {
                // Create an intent for when the notification is tapped
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("NOTIFICATION_TYPE", "SIGNATURE_REQUEST")
                    putExtra("REQUEST_ID", request.id)
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    context, 
                    request.id.hashCode(), 
                    intent, 
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Build the notification
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Signature Request")
                    .setContentText("You have a new signature request for ${document.title}")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setGroup(NOTIFICATION_GROUP)
                
                // Show the notification
                with(NotificationManagerCompat.from(context)) {
                    notify(request.id.hashCode(), builder.build())
                }
                
                Log.d(TAG, "Signature request notification sent for request: ${request.id}")
            } else {
                Log.e(TAG, "Document not found for signature request: ${request.id}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending signature request notification", e)
        }
    }
    
    /**
     * Send a notification for a completed signature
     * 
     * @param requestId The ID of the signature request that was completed
     */
    suspend fun sendSignatureCompletedNotification(requestId: String) = withContext(Dispatchers.IO) {
        try {
            // Get the signature request
            val request = signatureRepository.getSignatureRequestById(requestId)
            
            if (request != null && request.status == SignatureRequestStatus.COMPLETED) {
                // Get the document details
                val document = signatureRepository.getSignableDocumentById(request.documentId)
                
                if (document != null) {
                    // Create an intent for when the notification is tapped
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("NOTIFICATION_TYPE", "SIGNATURE_COMPLETED")
                        putExtra("REQUEST_ID", request.id)
                    }
                    
                    val pendingIntent = PendingIntent.getActivity(
                        context, 
                        request.id.hashCode(), 
                        intent, 
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    
                    // Build the notification
                    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Signature Completed")
                        .setContentText("${request.requestedFrom} has signed ${document.title}")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setGroup(NOTIFICATION_GROUP)
                    
                    // Show the notification
                    with(NotificationManagerCompat.from(context)) {
                        notify(request.id.hashCode() + 1000, builder.build())
                    }
                    
                    Log.d(TAG, "Signature completed notification sent for request: ${request.id}")
                } else {
                    Log.e(TAG, "Document not found for signature request: ${request.id}")
                }
            } else {
                Log.e(TAG, "Signature request not found or not completed: $requestId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending signature completed notification", e)
        }
    }
    
    /**
     * Send a reminder notification for a pending signature request
     * 
     * @param requestId The ID of the signature request to send a reminder for
     */
    suspend fun sendSignatureReminderNotification(requestId: String) = withContext(Dispatchers.IO) {
        try {
            // Get the signature request
            val request = signatureRepository.getSignatureRequestById(requestId)
            
            if (request != null && (request.status == SignatureRequestStatus.PENDING || request.status == SignatureRequestStatus.VIEWED)) {
                // Get the document details
                val document = signatureRepository.getSignableDocumentById(request.documentId)
                
                if (document != null) {
                    // Create an intent for when the notification is tapped
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("NOTIFICATION_TYPE", "SIGNATURE_REMINDER")
                        putExtra("REQUEST_ID", request.id)
                    }
                    
                    val pendingIntent = PendingIntent.getActivity(
                        context, 
                        request.id.hashCode(), 
                        intent, 
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    
                    // Build the notification
                    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Signature Reminder")
                        .setContentText("Reminder: Please sign ${document.title}")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setGroup(NOTIFICATION_GROUP)
                    
                    // Show the notification
                    with(NotificationManagerCompat.from(context)) {
                        notify(request.id.hashCode() + 2000, builder.build())
                    }
                    
                    // Update the reminder count in the repository
                    signatureRepository.sendReminder(requestId)
                    
                    Log.d(TAG, "Signature reminder notification sent for request: ${request.id}")
                } else {
                    Log.e(TAG, "Document not found for signature request: ${request.id}")
                }
            } else {
                Log.e(TAG, "Signature request not found or not pending: $requestId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending signature reminder notification", e)
        }
    }
    
    /**
     * Send a notification for a signature request that is about to expire
     * 
     * @param requestId The ID of the signature request that is about to expire
     */
    suspend fun sendExpirationWarningNotification(requestId: String) = withContext(Dispatchers.IO) {
        try {
            // Get the signature request
            val request = signatureRepository.getSignatureRequestById(requestId)
            
            if (request != null && 
                (request.status == SignatureRequestStatus.PENDING || request.status == SignatureRequestStatus.VIEWED) && 
                request.expiresAt != null) {
                
                // Check if the request is about to expire (within 24 hours)
                val currentTime = System.currentTimeMillis()
                val timeUntilExpiration = request.expiresAt - currentTime
                
                if (timeUntilExpiration > 0 && timeUntilExpiration <= TimeUnit.DAYS.toMillis(1)) {
                    // Get the document details
                    val document = signatureRepository.getSignableDocumentById(request.documentId)
                    
                    if (document != null) {
                        // Create an intent for when the notification is tapped
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra("NOTIFICATION_TYPE", "SIGNATURE_EXPIRATION")
                            putExtra("REQUEST_ID", request.id)
                        }
                        
                        val pendingIntent = PendingIntent.getActivity(
                            context, 
                            request.id.hashCode(), 
                            intent, 
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        
                        // Build the notification
                        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_dialog_alert)
                            .setContentTitle("Signature Request Expiring Soon")
                            .setContentText("Your signature request for ${document.title} will expire in ${TimeUnit.MILLISECONDS.toHours(timeUntilExpiration)} hours")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setGroup(NOTIFICATION_GROUP)
                        
                        // Show the notification
                        with(NotificationManagerCompat.from(context)) {
                            notify(request.id.hashCode() + 3000, builder.build())
                        }
                        
                        Log.d(TAG, "Expiration warning notification sent for request: ${request.id}")
                    } else {
                        Log.e(TAG, "Document not found for signature request: ${request.id}")
                    }
                } else {
                    Log.d(TAG, "Signature request not about to expire: $requestId")
                }
            } else {
                Log.e(TAG, "Signature request not found, not pending, or has no expiration: $requestId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending expiration warning notification", e)
        }
    }
    
    /**
     * Check for pending signature requests that need reminders or expiration warnings
     */
    fun checkPendingRequests() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get all pending signature requests
                val pendingRequests = signatureRepository.getSignatureRequestsByStatus(SignatureRequestStatus.PENDING)
                
                for (request in pendingRequests) {
                    // Check if the request needs a reminder
                    val currentTime = System.currentTimeMillis()
                    val lastReminderTime = request.lastReminderSent ?: request.requestedAt
                    val timeSinceLastReminder = currentTime - lastReminderTime
                    
                    // Send a reminder if it's been more than 3 days since the last one
                    if (timeSinceLastReminder > TimeUnit.DAYS.toMillis(3)) {
                        sendSignatureReminderNotification(request.id)
                    }
                    
                    // Check if the request is about to expire
                    if (request.expiresAt != null) {
                        val timeUntilExpiration = request.expiresAt - currentTime
                        
                        // Send an expiration warning if it's within 24 hours
                        if (timeUntilExpiration > 0 && timeUntilExpiration <= TimeUnit.DAYS.toMillis(1)) {
                            sendExpirationWarningNotification(request.id)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking pending requests", e)
            }
        }
    }
    
    companion object {
        /**
         * Create a NotificationService
         * 
         * @param context The context to use
         * @param signatureRepository The repository to use for retrieving signature requests and documents
         * @return A new NotificationService
         */
        fun create(
            context: Context,
            signatureRepository: DigitalSignatureRepository
        ): NotificationService {
            return NotificationService(context, signatureRepository)
        }
    }
}