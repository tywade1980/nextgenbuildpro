package com.nextgenbuildpro.pm.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgenbuildpro.core.ViewModel as CoreViewModel
import com.nextgenbuildpro.pm.data.model.Project
import com.nextgenbuildpro.pm.data.model.ProjectStatus
import com.nextgenbuildpro.pm.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing projects in the PM module
 */
class ProjectsViewModel(
    private val repository: ProjectRepository
) : ViewModel(), CoreViewModel<Project> {

    // All projects
    private val _projects = mutableStateOf<List<Project>>(emptyList())
    override val items: State<List<Project>> = _projects

    // Filtered projects
    private val _filteredProjects = MutableStateFlow<List<Project>>(emptyList())
    val filteredProjects: StateFlow<List<Project>> = _filteredProjects.asStateFlow()

    // Selected project
    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Status filter
    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refresh()
    }

    /**
     * Refresh projects from repository
     */
    override fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _projects.value = repository.getAll()
                updateFilteredProjects()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to refresh projects: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredProjects()
    }

    /**
     * Update status filter
     */
    fun updateStatusFilter(status: String) {
        _statusFilter.value = status
        updateFilteredProjects()
    }

    /**
     * Select a project
     */
    fun selectProject(project: Project) {
        _selectedProject.value = project
    }

    /**
     * Clear selected project
     */
    fun clearSelectedProject() {
        _selectedProject.value = null
    }

    /**
     * Create a new project
     */
    fun createProject(project: Project) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.save(project)
                if (!success) {
                    _error.value = "Failed to create project"
                } else {
                    _error.value = null
                    refresh()
                }
            } catch (e: Exception) {
                _error.value = "Failed to create project: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing project
     */
    fun updateProject(project: Project) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.update(project)
                if (!success) {
                    _error.value = "Failed to update project"
                } else {
                    _error.value = null
                    refresh()
                    if (_selectedProject.value?.id == project.id) {
                        _selectedProject.value = project
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to update project: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a project
     */
    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.delete(projectId)
                if (!success) {
                    _error.value = "Failed to delete project"
                } else {
                    _error.value = null
                    refresh()
                    if (_selectedProject.value?.id == projectId) {
                        _selectedProject.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete project: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update a project's status
     */
    fun updateProjectStatus(projectId: String, newStatus: ProjectStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.updateProjectStatus(projectId, newStatus.displayName)
                if (!success) {
                    _error.value = "Failed to update project status"
                } else {
                    _error.value = null
                    refresh()
                    // Refresh selected project if it's the one we updated
                    if (_selectedProject.value?.id == projectId) {
                        _selectedProject.value = repository.getById(projectId)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to update project status: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update a project's progress
     */
    fun updateProjectProgress(projectId: String, progress: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.updateProjectProgress(projectId, progress)
                if (!success) {
                    _error.value = "Failed to update project progress"
                } else {
                    _error.value = null
                    refresh()
                    // Refresh selected project if it's the one we updated
                    if (_selectedProject.value?.id == projectId) {
                        _selectedProject.value = repository.getById(projectId)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to update project progress: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a note to a project
     */
    fun addProjectNote(projectId: String, note: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.addProjectNote(projectId, note)
                if (!success) {
                    _error.value = "Failed to add note to project"
                } else {
                    _error.value = null
                    refresh()
                    // Refresh selected project if it's the one we updated
                    if (_selectedProject.value?.id == projectId) {
                        _selectedProject.value = repository.getById(projectId)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to add note to project: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update filtered projects based on search query and status filter
     */
    private fun updateFilteredProjects() {
        val query = _searchQuery.value
        val status = _statusFilter.value

        val filtered = if (query.isBlank() && status == "All") {
            _projects.value
        } else {
            _projects.value.filter { project ->
                val matchesQuery = query.isBlank() || 
                    project.name.contains(query, ignoreCase = true) || 
                    project.clientName.contains(query, ignoreCase = true) ||
                    project.description.contains(query, ignoreCase = true)

                val matchesStatus = status == "All" || project.status == status

                matchesQuery && matchesStatus
            }
        }

        _filteredProjects.value = filtered
    }
}