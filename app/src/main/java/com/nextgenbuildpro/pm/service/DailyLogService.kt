package com.nextgenbuildpro.pm.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.nextgenbuildpro.core.DateUtils
import com.nextgenbuildpro.pm.data.model.DailyLog
import com.nextgenbuildpro.pm.data.model.Project
import com.nextgenbuildpro.pm.data.repository.ProjectRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

/**
 * Service to detect end of workday and prompt for daily log entries
 */
class DailyLogService : Service() {
    private val TAG = "DailyLogService"
    private lateinit var projectRepository: ProjectRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "DailyLogService created")

        // Initialize repository
        projectRepository = ProjectRepository()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "DailyLogService started")

        when (intent?.action) {
            ACTION_CLOCK_OUT -> {
                val projectId = intent.getStringExtra(EXTRA_PROJECT_ID)
                val leadId = intent.getStringExtra(EXTRA_LEAD_ID)

                if (projectId != null && leadId != null) {
                    handleClockOut(projectId, leadId)
                }
            }
            ACTION_CHECK_WORKDAY_END -> {
                serviceScope.launch {
                    checkActiveProjects()
                    // Stop the service after processing
                    stopSelf()
                }
            }
            else -> {
                Log.d(TAG, "No specific action provided, stopping service")
                stopSelf()
            }
        }

        // Don't restart automatically - trigger-based approach
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "DailyLogService destroyed")

        // No need to cancel anything - service is now trigger-based
    }

    // Continuous monitoring removed in favor of trigger-based approach

    /**
     * Check for active projects at end of workday
     */
    private suspend fun checkActiveProjects() {
        try {
            val projects = projectRepository.getAll()

            for (project in projects) {
                if (project.status == "In Progress") {
                    // Check if there was activity on this project today
                    val lastActivity = project.lastActivityDate
                    val today = DateUtils.getCurrentTimestamp().split(" ")[0] // Get just the date part

                    if (lastActivity.startsWith(today)) {
                        // There was activity today, prompt for daily log
                        promptForDailyLog(project)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking active projects", e)
        }
    }

    /**
     * Handle clock out event
     */
    private fun handleClockOut(projectId: String, leadId: String) {
        serviceScope.launch {
            try {
                val project = projectRepository.getById(projectId)

                if (project != null) {
                    promptForDailyLog(project)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling clock out", e)
            }
        }
    }

    /**
     * Prompt for daily log entry
     */
    private fun promptForDailyLog(project: Project) {
        // In a real app, this would show a notification or dialog
        // For this implementation, we'll simulate the user's response

        val dailyLog = DailyLog(
            id = UUID.randomUUID().toString(),
            projectId = project.id,
            leadId = project.leadId,
            date = DateUtils.getCurrentTimestamp(),
            completedTasks = "Completed framing and electrical rough-in",
            hoursWorked = 8,
            materialsNeeded = "Need more 2x4s and electrical boxes",
            issues = "None",
            notes = "Making good progress, should finish ahead of schedule"
        )

        // Save the daily log
        serviceScope.launch {
            try {
                projectRepository.addDailyLog(dailyLog)
                Log.d(TAG, "Daily log added for project ${project.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding daily log", e)
            }
        }
    }

    companion object {
        private const val ACTION_CLOCK_OUT = "com.nextgenbuildpro.action.CLOCK_OUT"
        private const val ACTION_CHECK_WORKDAY_END = "com.nextgenbuildpro.action.CHECK_WORKDAY_END"
        private const val EXTRA_PROJECT_ID = "project_id"
        private const val EXTRA_LEAD_ID = "lead_id"

        /**
         * Start the daily log service (deprecated - use trigger-based methods instead)
         */
        fun startService(context: Context) {
            val intent = Intent(context, DailyLogService::class.java)
            context.startService(intent)
        }

        /**
         * Stop the daily log service
         */
        fun stopService(context: Context) {
            val intent = Intent(context, DailyLogService::class.java)
            context.stopService(intent)
        }

        /**
         * Notify the service of a clock out event
         */
        fun notifyClockOut(context: Context, projectId: String, leadId: String) {
            val intent = Intent(context, DailyLogService::class.java).apply {
                action = ACTION_CLOCK_OUT
                putExtra(EXTRA_PROJECT_ID, projectId)
                putExtra(EXTRA_LEAD_ID, leadId)
            }
            context.startService(intent)
        }

        /**
         * Trigger a check for workday end (trigger-based approach)
         * This will check for active projects and prompt for daily logs if needed
         */
        fun checkWorkdayEnd(context: Context) {
            val intent = Intent(context, DailyLogService::class.java).apply {
                action = ACTION_CHECK_WORKDAY_END
            }
            context.startService(intent)
        }
    }
}
