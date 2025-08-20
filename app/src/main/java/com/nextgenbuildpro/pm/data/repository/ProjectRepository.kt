package com.nextgenbuildpro.pm.data.repository

import com.nextgenbuildpro.core.Address
import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.core.DateUtils
import com.nextgenbuildpro.pm.data.model.DailyLog
import com.nextgenbuildpro.pm.data.model.Project
import com.nextgenbuildpro.pm.data.model.ProjectStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing projects in the PM module
 */
class ProjectRepository : Repository<Project> {
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    init {
        // Load sample data for demonstration
        loadSampleData()
    }

    /**
     * Get all projects
     */
    override suspend fun getAll(): List<Project> {
        return _projects.value
    }

    /**
     * Get a project by ID
     */
    override suspend fun getById(id: String): Project? {
        return _projects.value.find { it.id == id }
    }

    /**
     * Save a new project
     */
    override suspend fun save(item: Project): Boolean {
        try {
            _projects.value = _projects.value + item
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Update an existing project
     */
    override suspend fun update(item: Project): Boolean {
        try {
            _projects.value = _projects.value.map { 
                if (it.id == item.id) item else it 
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Delete a project by ID
     */
    override suspend fun delete(id: String): Boolean {
        try {
            _projects.value = _projects.value.filter { it.id != id }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Get projects filtered by status
     */
    suspend fun getProjectsByStatus(status: String): List<Project> {
        return _projects.value.filter { it.status == status }
    }

    /**
     * Get projects for a specific client
     */
    suspend fun getProjectsForClient(clientId: String): List<Project> {
        return _projects.value.filter { it.clientId == clientId }
    }

    /**
     * Search projects by name or description
     */
    suspend fun searchProjects(query: String): List<Project> {
        if (query.isBlank()) return _projects.value

        val lowercaseQuery = query.lowercase()
        return _projects.value.filter { 
            it.name.lowercase().contains(lowercaseQuery) || 
            it.description.lowercase().contains(lowercaseQuery) 
        }
    }

    /**
     * Add a note to a project
     */
    suspend fun addProjectNote(projectId: String, note: String): Boolean {
        try {
            _projects.value = _projects.value.map { project ->
                if (project.id == projectId) {
                    val updatedNotes = project.notes + "\n" + DateUtils.getCurrentTimestamp() + ": " + note
                    project.copy(
                        notes = updatedNotes.trim(),
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    project
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Update a project's status
     */
    suspend fun updateProjectStatus(projectId: String, newStatus: String): Boolean {
        try {
            _projects.value = _projects.value.map { project ->
                if (project.id == projectId) {
                    project.copy(
                        status = newStatus,
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    project
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Update a project's progress
     */
    suspend fun updateProjectProgress(projectId: String, progress: Int): Boolean {
        try {
            _projects.value = _projects.value.map { project ->
                if (project.id == projectId) {
                    project.copy(
                        progress = progress,
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    project
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Add a daily log to a project
     */
    suspend fun addDailyLog(dailyLog: DailyLog): Boolean {
        try {
            _projects.value = _projects.value.map { project ->
                if (project.id == dailyLog.projectId) {
                    project.copy(
                        dailyLogs = project.dailyLogs + dailyLog,
                        lastActivityDate = DateUtils.getCurrentTimestamp(),
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    project
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Load sample data for demonstration
     */
    private fun loadSampleData() {
        val sampleProjects = listOf(
            Project(
                id = "project_1",
                name = "Kitchen Renovation - Smith",
                clientId = "lead_1",
                clientName = "John Smith",
                leadId = "lead_1",
                address = Address(
                    street = "123 Main St",
                    city = "Anytown",
                    state = "CA",
                    zipCode = "12345"
                ),
                status = ProjectStatus.PLANNING.displayName,
                startDate = "07/01/2023",
                endDate = "08/30/2023",
                budget = 25000.0,
                actualCost = 0.0,
                progress = 0,
                description = "Complete kitchen renovation including new cabinets, countertops, appliances, and flooring.",
                notes = "Initial consultation completed. Client prefers modern style with white cabinets.",
                lastActivityDate = "06/15/2023 10:30:00",
                createdAt = "06/15/2023 10:30:00",
                updatedAt = "06/15/2023 10:30:00"
            ),
            Project(
                id = "project_2",
                name = "Bathroom Remodel - Johnson",
                clientId = "lead_2",
                clientName = "Sarah Johnson",
                leadId = "lead_2",
                address = Address(
                    street = "456 Oak Ave",
                    city = "Somewhere",
                    state = "NY",
                    zipCode = "67890"
                ),
                status = ProjectStatus.IN_PROGRESS.displayName,
                startDate = "06/20/2023",
                endDate = "07/15/2023",
                budget = 12000.0,
                actualCost = 5000.0,
                progress = 40,
                description = "Master bathroom remodel with new shower, vanity, and tile work.",
                notes = "Demo completed. Plumbing rough-in scheduled for next week.",
                lastActivityDate = "06/25/2023 09:15:00",
                createdAt = "06/10/2023 14:45:00",
                updatedAt = "06/25/2023 09:15:00"
            ),
            Project(
                id = "project_3",
                name = "Deck Construction - Brown",
                clientId = "lead_3",
                clientName = "Michael Brown",
                leadId = "lead_3",
                address = Address(
                    street = "789 Pine Rd",
                    city = "Nowhere",
                    state = "TX",
                    zipCode = "54321"
                ),
                status = ProjectStatus.PLANNING.displayName,
                startDate = "07/15/2023",
                endDate = "08/15/2023",
                budget = 8500.0,
                actualCost = 0.0,
                progress = 0,
                description = "New 400 sq ft composite deck with railing and stairs.",
                notes = "Permit application submitted. Awaiting approval.",
                lastActivityDate = "06/20/2023 16:10:00",
                createdAt = "06/05/2023 11:20:00",
                updatedAt = "06/20/2023 16:10:00"
            )
        )

        _projects.value = sampleProjects
    }
}
