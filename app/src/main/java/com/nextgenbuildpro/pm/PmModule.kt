package com.nextgenbuildpro.pm

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nextgenbuildpro.pm.data.repository.EstimateRepository
import com.nextgenbuildpro.pm.data.repository.ProjectRepository
import com.nextgenbuildpro.pm.data.repository.TaskRepository
import com.nextgenbuildpro.pm.service.DailyLogService
import com.nextgenbuildpro.pm.viewmodel.EstimatesViewModel
import com.nextgenbuildpro.pm.viewmodel.ProjectsViewModel

/**
 * PM Module for NextGenBuildPro
 * 
 * This module handles all Project Management functionality:
 * - Project management
 * - Estimates and proposals
 * - Tasks and scheduling
 * - Materials tracking
 * - Timesheets
 * - Change orders
 */
object PmModule {
    private var initialized = false
    private lateinit var projectRepository: ProjectRepository
    private lateinit var estimateRepository: EstimateRepository
    private lateinit var taskRepository: TaskRepository

    /**
     * Check if the module is already initialized
     */
    fun isInitialized(): Boolean {
        return initialized
    }

    /**
     * Initialize the PM module
     */
    fun initialize(context: Context) {
        if (initialized) return

        // Initialize repositories
        projectRepository = ProjectRepository()
        estimateRepository = EstimateRepository(context)
        taskRepository = TaskRepository(context)

        // Temporarily disabled daily log service until context is clarified
        // DailyLogService.startService(context)

        initialized = true
    }

    /**
     * Get the project repository
     */
    fun getProjectRepository(): ProjectRepository {
        checkInitialized()
        return projectRepository
    }

    /**
     * Get the estimate repository
     */
    fun getEstimateRepository(): EstimateRepository {
        checkInitialized()
        return estimateRepository
    }

    /**
     * Get the task repository
     */
    fun getTaskRepository(): TaskRepository {
        checkInitialized()
        return taskRepository
    }

    /**
     * Trigger a workday end check (trigger-based approach)
     * This will check for active projects and prompt for daily logs if needed
     */
    fun triggerWorkdayEndCheck(context: Context) {
        checkInitialized()
        DailyLogService.checkWorkdayEnd(context)
    }

    /**
     * Trigger a clock out event for a specific project
     * This will prompt for a daily log entry for the project
     */
    fun triggerClockOutEvent(context: Context, projectId: String, leadId: String) {
        checkInitialized()
        DailyLogService.notifyClockOut(context, projectId, leadId)
    }

    /**
     * Create a new estimates view model
     */
    fun createEstimatesViewModel(): EstimatesViewModel {
        checkInitialized()
        return EstimatesViewModel(estimateRepository)
    }

    /**
     * Create a new projects view model
     */
    fun createProjectsViewModel(): ProjectsViewModel {
        checkInitialized()
        return ProjectsViewModel(projectRepository)
    }

    /**
     * Check if the module is initialized
     */
    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("PM Module is not initialized. Call initialize() first.")
        }
    }
}

/**
 * Composable function to remember PM module components
 */
@Composable
fun rememberPmComponents(): PmComponents {
    val context = LocalContext.current

    // Initialize module if needed
    if (!PmModule.isInitialized()) {
        PmModule.initialize(context)
    }

    // Create view models
    val projectsViewModel = remember { PmModule.createProjectsViewModel() }
    val estimatesViewModel = remember { PmModule.createEstimatesViewModel() }

    return PmComponents(
        projectRepository = PmModule.getProjectRepository(),
        estimateRepository = PmModule.getEstimateRepository(),
        taskRepository = PmModule.getTaskRepository(),
        projectsViewModel = projectsViewModel,
        estimatesViewModel = estimatesViewModel
    )
}

/**
 * Data class to hold PM components
 */
data class PmComponents(
    val projectRepository: ProjectRepository,
    val estimateRepository: EstimateRepository,
    val taskRepository: TaskRepository,
    val projectsViewModel: ProjectsViewModel,
    val estimatesViewModel: EstimatesViewModel
)
