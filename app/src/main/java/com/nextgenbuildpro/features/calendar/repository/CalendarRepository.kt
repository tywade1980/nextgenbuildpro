package com.nextgenbuildpro.features.calendar.repository

import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.features.calendar.models.*
import com.nextgenbuildpro.pm.data.model.DailyLog
import com.nextgenbuildpro.pm.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Repository for managing calendar data and project timelines
 */
class CalendarRepository(
    private val projectRepository: ProjectRepository
) : Repository<ProjectTimeline> {
    private val _timelines = MutableStateFlow<List<ProjectTimeline>>(emptyList())
    val timelines: StateFlow<List<ProjectTimeline>> = _timelines.asStateFlow()

    init {
        // Initialize with sample data
        loadSampleData()
    }

    /**
     * Get all project timelines
     */
    override suspend fun getAll(): List<ProjectTimeline> {
        return _timelines.value
    }

    /**
     * Get a project timeline by ID
     */
    override suspend fun getById(id: String): ProjectTimeline? {
        return _timelines.value.find { it.id == id }
    }

    /**
     * Save a new project timeline
     */
    override suspend fun save(item: ProjectTimeline): Boolean {
        try {
            _timelines.value = _timelines.value + item
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Update an existing project timeline
     */
    override suspend fun update(item: ProjectTimeline): Boolean {
        try {
            _timelines.value = _timelines.value.map { 
                if (it.id == item.id) item else it 
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Delete a project timeline by ID
     */
    override suspend fun delete(id: String): Boolean {
        try {
            _timelines.value = _timelines.value.filter { it.id != id }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Get a project timeline by project ID
     */
    suspend fun getTimelineByProjectId(projectId: String): ProjectTimeline? {
        return _timelines.value.find { it.projectId == projectId }
    }

    /**
     * Update project timeline based on daily logs
     */
    suspend fun updateTimelineFromDailyLogs(projectId: String, dailyLogs: List<DailyLog>): Boolean {
        try {
            val timeline = getTimelineByProjectId(projectId) ?: return false
            
            // Calculate progress based on daily logs
            val updatedProgress = calculateProgressFromDailyLogs(timeline, dailyLogs)
            
            // Update timeline with new progress
            val updatedTimeline = timeline.copy(progress = updatedProgress)
            
            return update(updatedTimeline)
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Calculate progress from daily logs
     */
    private fun calculateProgressFromDailyLogs(timeline: ProjectTimeline, dailyLogs: List<DailyLog>): Int {
        // This is a simplified calculation
        // In a real app, this would be more complex based on task completion, etc.
        if (dailyLogs.isEmpty()) return timeline.progress
        
        // Calculate total days in project
        val totalDays = TimeUnit.MILLISECONDS.toDays(
            timeline.endDate.time - timeline.startDate.time
        ).toInt()
        
        if (totalDays <= 0) return timeline.progress
        
        // Calculate days elapsed
        val today = Date()
        val daysElapsed = TimeUnit.MILLISECONDS.toDays(
            today.time - timeline.startDate.time
        ).toInt()
        
        // Calculate progress based on time elapsed and daily logs
        val timeProgress = (daysElapsed * 100) / totalDays
        
        // Adjust progress based on daily logs
        // This is a simplified calculation
        return minOf(timeProgress, 100)
    }

    /**
     * Load sample data
     */
    private fun loadSampleData() {
        // Create sample timelines
        Calendar.getInstance()
        
        // Project 1 timeline
        val project1StartDate = Calendar.getInstance().apply {
            set(2023, Calendar.JUNE, 1)
        }.time
        
        val project1EndDate = Calendar.getInstance().apply {
            set(2023, Calendar.AUGUST, 30)
        }.time
        
        val project1Timeline = ProjectTimeline(
            id = "timeline_1",
            projectId = "project_1",
            projectName = "Kitchen Renovation - Smith",
            startDate = project1StartDate,
            endDate = project1EndDate,
            progress = 0
        )
        
        // Project 2 timeline
        val project2StartDate = Calendar.getInstance().apply {
            set(2023, Calendar.JUNE, 20)
        }.time
        
        val project2EndDate = Calendar.getInstance().apply {
            set(2023, Calendar.JULY, 15)
        }.time
        
        val project2Timeline = ProjectTimeline(
            id = "timeline_2",
            projectId = "project_2",
            projectName = "Bathroom Remodel - Johnson",
            startDate = project2StartDate,
            endDate = project2EndDate,
            progress = 40
        )
        
        // Save sample timelines
        _timelines.value = listOf(project1Timeline, project2Timeline)
    }
}

/**
 * Data class for Gantt chart items
 */
data class GanttChartItem(
    val id: String,
    val name: String,
    val startDate: Date,
    val endDate: Date,
    val progress: Int,
    val dependencies: List<String>,
    val type: GanttChartItemType,
    val status: GanttChartItemStatus
)

/**
 * Enum for Gantt chart item types
 */
enum class GanttChartItemType {
    TASK,
    MILESTONE,
    MATERIAL_DELIVERY
}

/**
 * Enum for Gantt chart item status
 */
enum class GanttChartItemStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    DELAYED,
    BLOCKED
}