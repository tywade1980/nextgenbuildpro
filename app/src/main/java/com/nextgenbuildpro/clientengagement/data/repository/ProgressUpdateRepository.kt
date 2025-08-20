package com.nextgenbuildpro.clientengagement.data.repository

import android.content.Context
import com.nextgenbuildpro.clientengagement.data.model.DeliveryStatus
import com.nextgenbuildpro.clientengagement.data.model.MilestoneUpdate
import com.nextgenbuildpro.clientengagement.data.model.NotificationType
import com.nextgenbuildpro.clientengagement.data.model.ProgressUpdate
import com.nextgenbuildpro.clientengagement.data.model.ScheduledUpdate
import com.nextgenbuildpro.clientengagement.data.model.UpdateFrequency
import com.nextgenbuildpro.clientengagement.data.model.UpdateNotification
import com.nextgenbuildpro.core.Repository
import java.util.Calendar
import java.util.UUID

/**
 * Repository for managing progress updates
 */
class ProgressUpdateRepository(private val context: Context) : Repository<ProgressUpdate> {
    
    // In-memory storage for demo purposes
    private val progressUpdates = mutableListOf<ProgressUpdate>()
    private val milestoneUpdates = mutableListOf<MilestoneUpdate>()
    private val scheduledUpdates = mutableListOf<ScheduledUpdate>()
    private val notifications = mutableListOf<UpdateNotification>()
    
    /**
     * Get all progress updates
     */
    override suspend fun getAll(): List<ProgressUpdate> {
        return progressUpdates
    }
    
    /**
     * Get a progress update by ID
     */
    override suspend fun getById(id: String): ProgressUpdate? {
        return progressUpdates.find { it.id == id }
    }
    
    /**
     * Save a new progress update
     */
    override suspend fun save(item: ProgressUpdate): Boolean {
        return try {
            progressUpdates.add(item)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Update an existing progress update
     */
    override suspend fun update(item: ProgressUpdate): Boolean {
        return try {
            val index = progressUpdates.indexOfFirst { it.id == item.id }
            if (index != -1) {
                progressUpdates[index] = item
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Delete a progress update by ID
     */
    override suspend fun delete(id: String): Boolean {
        return try {
            val removed = progressUpdates.removeIf { it.id == id }
            
            // Also remove related milestone updates and notifications
            if (removed) {
                milestoneUpdates.removeIf { it.progressUpdateId == id }
                notifications.removeIf { it.progressUpdateId == id }
            }
            
            removed
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get progress updates for a project
     */
    suspend fun getProgressUpdatesByProject(projectId: String): List<ProgressUpdate> {
        return progressUpdates.filter { it.projectId == projectId }
    }
    
    /**
     * Get progress updates that are shared with clients
     */
    suspend fun getClientSharedUpdates(projectId: String): List<ProgressUpdate> {
        return progressUpdates.filter { it.projectId == projectId && it.isSharedWithClient }
    }
    
    /**
     * Share a progress update with clients
     */
    suspend fun shareWithClient(updateId: String): Boolean {
        val update = getById(updateId) ?: return false
        
        val sharedUpdate = update.copy(
            isSharedWithClient = true,
            updatedAt = System.currentTimeMillis()
        )
        
        return update(sharedUpdate)
    }
    
    /**
     * Save a milestone update
     */
    suspend fun saveMilestoneUpdate(milestone: MilestoneUpdate): Boolean {
        return try {
            milestoneUpdates.add(milestone)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get milestone updates for a progress update
     */
    suspend fun getMilestoneUpdates(progressUpdateId: String): List<MilestoneUpdate> {
        return milestoneUpdates.filter { it.progressUpdateId == progressUpdateId }
    }
    
    /**
     * Update a milestone update
     */
    suspend fun updateMilestone(milestone: MilestoneUpdate): Boolean {
        return try {
            val index = milestoneUpdates.indexOfFirst { it.id == milestone.id }
            if (index != -1) {
                milestoneUpdates[index] = milestone
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Complete a milestone
     */
    suspend fun completeMilestone(milestoneId: String): Boolean {
        val milestone = milestoneUpdates.find { it.id == milestoneId } ?: return false
        
        val completedMilestone = milestone.copy(
            isCompleted = true,
            completedAt = System.currentTimeMillis()
        )
        
        return updateMilestone(completedMilestone)
    }
    
    /**
     * Save a scheduled update
     */
    suspend fun saveScheduledUpdate(scheduledUpdate: ScheduledUpdate): Boolean {
        return try {
            // Calculate next scheduled time
            val updatedSchedule = calculateNextScheduledTime(scheduledUpdate)
            scheduledUpdates.add(updatedSchedule)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get scheduled updates for a project
     */
    suspend fun getScheduledUpdates(projectId: String): List<ScheduledUpdate> {
        return scheduledUpdates.filter { it.projectId == projectId }
    }
    
    /**
     * Update a scheduled update
     */
    suspend fun updateScheduledUpdate(scheduledUpdate: ScheduledUpdate): Boolean {
        return try {
            val index = scheduledUpdates.indexOfFirst { it.id == scheduledUpdate.id }
            if (index != -1) {
                // Calculate next scheduled time
                val updatedSchedule = calculateNextScheduledTime(scheduledUpdate)
                scheduledUpdates[index] = updatedSchedule
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Record that a scheduled update was sent
     */
    suspend fun recordScheduledUpdateSent(scheduledUpdateId: String): Boolean {
        val scheduledUpdate = scheduledUpdates.find { it.id == scheduledUpdateId } ?: return false
        
        val now = System.currentTimeMillis()
        val updatedSchedule = scheduledUpdate.copy(
            lastSentAt = now
        )
        
        // Calculate next scheduled time
        val finalSchedule = calculateNextScheduledTime(updatedSchedule)
        
        return updateScheduledUpdate(finalSchedule)
    }
    
    /**
     * Calculate the next scheduled time for an update
     */
    private fun calculateNextScheduledTime(scheduledUpdate: ScheduledUpdate): ScheduledUpdate {
        val calendar = Calendar.getInstance()
        
        // If there's a last sent time, use that as the base, otherwise use now
        if (scheduledUpdate.lastSentAt != null) {
            calendar.timeInMillis = scheduledUpdate.lastSentAt
        }
        
        // Calculate next time based on frequency
        when (scheduledUpdate.frequency) {
            UpdateFrequency.DAILY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            UpdateFrequency.WEEKLY -> {
                if (scheduledUpdate.dayOfWeek != null) {
                    // Set to the specified day of week
                    while (calendar.get(Calendar.DAY_OF_WEEK) != scheduledUpdate.dayOfWeek) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }
                } else {
                    // Just add 7 days
                    calendar.add(Calendar.DAY_OF_YEAR, 7)
                }
            }
            UpdateFrequency.BI_WEEKLY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 14)
            }
            UpdateFrequency.MONTHLY -> {
                if (scheduledUpdate.dayOfMonth != null) {
                    // Set to the specified day of month
                    calendar.add(Calendar.MONTH, 1)
                    calendar.set(Calendar.DAY_OF_MONTH, 
                        minOf(scheduledUpdate.dayOfMonth, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)))
                } else {
                    // Just add a month
                    calendar.add(Calendar.MONTH, 1)
                }
            }
            UpdateFrequency.MILESTONE_BASED -> {
                // For milestone-based, we don't automatically schedule the next one
                return scheduledUpdate.copy(nextScheduledAt = null)
            }
        }
        
        // If there's a specific time, set it
        if (scheduledUpdate.time != null) {
            val timeParts = scheduledUpdate.time.split(":")
            if (timeParts.size == 2) {
                val hour = timeParts[0].toIntOrNull() ?: 9 // Default to 9 AM
                val minute = timeParts[1].toIntOrNull() ?: 0
                
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        }
        
        return scheduledUpdate.copy(nextScheduledAt = calendar.timeInMillis)
    }
    
    /**
     * Send a notification for a progress update
     */
    suspend fun sendNotification(
        progressUpdateId: String,
        recipientId: String,
        notificationType: NotificationType
    ): Boolean {
        return try {
            val notification = UpdateNotification(
                id = UUID.randomUUID().toString(),
                progressUpdateId = progressUpdateId,
                recipientId = recipientId,
                notificationType = notificationType,
                sentAt = System.currentTimeMillis(),
                deliveryStatus = DeliveryStatus.SENT
            )
            
            notifications.add(notification)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get notifications for a progress update
     */
    suspend fun getNotifications(progressUpdateId: String): List<UpdateNotification> {
        return notifications.filter { it.progressUpdateId == progressUpdateId }
    }
    
    /**
     * Get notifications for a recipient
     */
    suspend fun getNotificationsByRecipient(recipientId: String): List<UpdateNotification> {
        return notifications.filter { it.recipientId == recipientId }
    }
    
    /**
     * Update notification status
     */
    suspend fun updateNotificationStatus(notificationId: String, status: DeliveryStatus): Boolean {
        return try {
            val index = notifications.indexOfFirst { it.id == notificationId }
            if (index != -1) {
                val notification = notifications[index]
                val updatedNotification = notification.copy(
                    deliveryStatus = status,
                    readAt = if (status == DeliveryStatus.READ) System.currentTimeMillis() else notification.readAt
                )
                notifications[index] = updatedNotification
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}