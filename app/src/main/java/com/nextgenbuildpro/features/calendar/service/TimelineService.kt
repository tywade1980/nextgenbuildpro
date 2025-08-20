package com.nextgenbuildpro.features.calendar.service

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.features.calendar.models.*
import com.nextgenbuildpro.features.calendar.repository.CalendarRepository
import com.nextgenbuildpro.pm.data.model.DailyLog
import com.nextgenbuildpro.pm.data.repository.ProjectRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Service for managing and updating project timelines
 */
class TimelineService(
    private val context: Context,
    private val calendarRepository: CalendarRepository,
    private val projectRepository: ProjectRepository
) {
    private val TAG = "TimelineService"
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    /**
     * Initialize the service
     */
    fun initialize() {
        Log.d(TAG, "TimelineService initialized")
        
        // Start monitoring for updates
        startMonitoring()
    }

    /**
     * Start monitoring for updates to project timelines
     */
    private fun startMonitoring() {
        serviceScope.launch {
            // Monitor for daily logs
            monitorDailyLogs()
            
            // Monitor for material deliveries
            monitorMaterialDeliveries()
            
            // Monitor for change orders
            monitorChangeOrders()
        }
    }

    /**
     * Monitor for new daily logs and update timelines accordingly
     */
    private suspend fun monitorDailyLogs() {
        try {
            // Get all projects
            val projects = projectRepository.getAll()
            
            // Process each project
            for (project in projects) {
                // Get daily logs for the project
                val dailyLogs = project.dailyLogs
                
                if (dailyLogs.isNotEmpty()) {
                    // Update timeline based on daily logs
                    calendarRepository.updateTimelineFromDailyLogs(project.id, dailyLogs)
                    
                    Log.d(TAG, "Updated timeline for project ${project.id} based on ${dailyLogs.size} daily logs")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error monitoring daily logs", e)
        }
    }

    /**
     * Monitor for material deliveries and update timelines accordingly
     */
    private suspend fun monitorMaterialDeliveries() {
        // This would connect to a material delivery tracking system
        // For now, we'll just log a message
        Log.d(TAG, "Monitoring material deliveries")
    }

    /**
     * Monitor for change orders and update timelines accordingly
     */
    private suspend fun monitorChangeOrders() {
        // This would connect to a change order tracking system
        // For now, we'll just log a message
        Log.d(TAG, "Monitoring change orders")
    }

    /**
     * Update a project timeline based on a new daily log
     */
    suspend fun updateTimelineWithDailyLog(projectId: String, dailyLog: DailyLog): Boolean {
        try {
            // Get the current timeline
            calendarRepository.getTimelineByProjectId(projectId) ?: return false
            
            // Get all daily logs for the project
            val project = projectRepository.getById(projectId) ?: return false
            val allDailyLogs = project.dailyLogs + dailyLog
            
            // Update the timeline
            return calendarRepository.updateTimelineFromDailyLogs(projectId, allDailyLogs)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating timeline with daily log", e)
            return false
        }
    }

    /**
     * Update a project timeline with a material delivery
     */
    suspend fun updateTimelineWithMaterialDelivery(
        projectId: String,
        deliveryId: String,
        actualDate: Date,
        status: DeliveryStatus
    ): Boolean {
        try {
            // Get the current timeline
            val timeline = calendarRepository.getTimelineByProjectId(projectId) ?: return false
            
            // Find the delivery in the timeline
            val updatedDeliveries = timeline.materialDeliveries.map { delivery ->
                if (delivery.id == deliveryId) {
                    delivery.copy(actualDate = actualDate, status = status)
                } else {
                    delivery
                }
            }
            
            // Create updated timeline
            val updatedTimeline = timeline.copy(materialDeliveries = updatedDeliveries)
            
            // Save the updated timeline
            return calendarRepository.update(updatedTimeline)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating timeline with material delivery", e)
            return false
        }
    }

    /**
     * Update a project timeline with a change order
     */
    suspend fun updateTimelineWithChangeOrder(
        projectId: String,
        changeOrder: TimelineChangeOrder
    ): Boolean {
        try {
            // Get the current timeline
            val timeline = calendarRepository.getTimelineByProjectId(projectId) ?: return false
            
            // Add the change order to the timeline
            val updatedChangeOrders = timeline.changeOrders + changeOrder
            
            // If the change order is approved, update the end date
            val updatedEndDate = if (changeOrder.status == ChangeOrderStatus.APPROVED && changeOrder.approvalDate != null) {
                // Calculate new end date based on impact
                val currentEndMillis = timeline.endDate.time
                val additionalMillis = TimeUnit.DAYS.toMillis(changeOrder.impact.daysAdded.toLong())
                Date(currentEndMillis + additionalMillis)
            } else {
                timeline.endDate
            }
            
            // Create updated timeline
            val updatedTimeline = timeline.copy(
                endDate = updatedEndDate,
                changeOrders = updatedChangeOrders
            )
            
            // Save the updated timeline
            return calendarRepository.update(updatedTimeline)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating timeline with change order", e)
            return false
        }
    }

    companion object {
        /**
         * Create a TimelineService instance
         */
        fun create(
            context: Context,
            calendarRepository: CalendarRepository,
            projectRepository: ProjectRepository
        ): TimelineService {
            return TimelineService(context, calendarRepository, projectRepository)
        }
    }
}