package com.nextgenbuildpro.pm.data.repository

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.core.DateUtils
import com.nextgenbuildpro.pm.data.model.Task
import com.nextgenbuildpro.pm.data.model.TaskStatus
import com.nextgenbuildpro.pm.data.model.TaskPriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository for managing tasks in the PM module
 * Tasks can be linked to various entities like projects, estimates, materials, notes, and clients
 */
class TaskRepository(private val context: Context) : Repository<Task> {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        // Load sample data for demonstration
        loadSampleData()
    }

    /**
     * Get all tasks
     */
    override suspend fun getAll(): List<Task> {
        return _tasks.value
    }

    /**
     * Get a task by ID
     */
    override suspend fun getById(id: String): Task? {
        return _tasks.value.find { it.id == id }
    }

    /**
     * Save a new task
     */
    override suspend fun save(item: Task): Boolean {
        try {
            _tasks.value = _tasks.value + item
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving task: ${e.message}")
            return false
        }
    }

    /**
     * Update an existing task
     */
    override suspend fun update(item: Task): Boolean {
        try {
            _tasks.value = _tasks.value.map { 
                if (it.id == item.id) item else it 
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task: ${e.message}")
            return false
        }
    }

    /**
     * Delete a task by ID
     */
    override suspend fun delete(id: String): Boolean {
        try {
            _tasks.value = _tasks.value.filter { it.id != id }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task: ${e.message}")
            return false
        }
    }

    /**
     * Get tasks filtered by status
     */
    suspend fun getTasksByStatus(status: String): List<Task> {
        return _tasks.value.filter { it.status == status }
    }

    /**
     * Get tasks filtered by priority
     */
    suspend fun getTasksByPriority(priority: String): List<Task> {
        return _tasks.value.filter { it.priority == priority }
    }

    /**
     * Get tasks for a specific project
     */
    suspend fun getTasksForProject(projectId: String): List<Task> {
        return _tasks.value.filter { it.projectId == projectId }
    }

    /**
     * Get tasks for a specific estimate
     */
    suspend fun getTasksForEstimate(estimateId: String): List<Task> {
        return _tasks.value.filter { it.estimateId == estimateId }
    }

    /**
     * Get tasks for a specific material
     */
    suspend fun getTasksForMaterial(materialId: String): List<Task> {
        return _tasks.value.filter { it.materialId == materialId }
    }

    /**
     * Get tasks for a specific note
     */
    suspend fun getTasksForNote(noteId: String): List<Task> {
        return _tasks.value.filter { it.noteId == noteId }
    }

    /**
     * Get tasks for a specific client
     */
    suspend fun getTasksForClient(clientId: String): List<Task> {
        return _tasks.value.filter { it.clientId == clientId }
    }

    /**
     * Get tasks by entity type
     */
    suspend fun getTasksByEntityType(entityType: String): List<Task> {
        return _tasks.value.filter { it.linkedEntityType == entityType }
    }

    /**
     * Search tasks by title or description
     */
    suspend fun searchTasks(query: String): List<Task> {
        if (query.isBlank()) return _tasks.value
        
        val lowercaseQuery = query.lowercase()
        return _tasks.value.filter { 
            it.title.lowercase().contains(lowercaseQuery) || 
            it.description.lowercase().contains(lowercaseQuery) 
        }
    }

    /**
     * Update a task's status
     */
    suspend fun updateTaskStatus(taskId: String, newStatus: String): Boolean {
        try {
            _tasks.value = _tasks.value.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        status = newStatus,
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    task
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task status: ${e.message}")
            return false
        }
    }

    /**
     * Update a task's priority
     */
    suspend fun updateTaskPriority(taskId: String, newPriority: String): Boolean {
        try {
            _tasks.value = _tasks.value.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        priority = newPriority,
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    task
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task priority: ${e.message}")
            return false
        }
    }

    /**
     * Mark a task as completed
     */
    suspend fun completeTask(taskId: String): Boolean {
        try {
            _tasks.value = _tasks.value.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        status = TaskStatus.COMPLETED.displayName,
                        completedDate = DateUtils.getCurrentTimestamp(),
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    task
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error completing task: ${e.message}")
            return false
        }
    }

    /**
     * Link a task to a project
     */
    suspend fun linkTaskToProject(taskId: String, projectId: String): Boolean {
        try {
            _tasks.value = _tasks.value.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        projectId = projectId,
                        linkedEntityType = "project",
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    task
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error linking task to project: ${e.message}")
            return false
        }
    }

    /**
     * Link a task to an estimate
     */
    suspend fun linkTaskToEstimate(taskId: String, estimateId: String): Boolean {
        try {
            _tasks.value = _tasks.value.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        estimateId = estimateId,
                        linkedEntityType = "estimate",
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    task
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error linking task to estimate: ${e.message}")
            return false
        }
    }

    /**
     * Link a task to a material
     */
    suspend fun linkTaskToMaterial(taskId: String, materialId: String): Boolean {
        try {
            _tasks.value = _tasks.value.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        materialId = materialId,
                        linkedEntityType = "material",
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    task
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error linking task to material: ${e.message}")
            return false
        }
    }

    /**
     * Link a task to a note
     */
    suspend fun linkTaskToNote(taskId: String, noteId: String): Boolean {
        try {
            _tasks.value = _tasks.value.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        noteId = noteId,
                        linkedEntityType = "note",
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    task
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error linking task to note: ${e.message}")
            return false
        }
    }

    /**
     * Link a task to a client
     */
    suspend fun linkTaskToClient(taskId: String, clientId: String): Boolean {
        try {
            _tasks.value = _tasks.value.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        clientId = clientId,
                        linkedEntityType = "client",
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    task
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error linking task to client: ${e.message}")
            return false
        }
    }

    /**
     * Create a task from an assembly item
     */
    suspend fun createTaskFromAssembly(assemblyId: String, title: String, description: String, estimateId: String? = null): Task {
        val task = Task(
            id = "task_${System.currentTimeMillis()}",
            title = title,
            description = description,
            assignedTo = "",
            status = TaskStatus.TODO.displayName,
            priority = TaskPriority.MEDIUM.displayName,
            dueDate = "", // Would be set in a real app
            completedDate = null,
            estimatedHours = 0.0, // Would be calculated in a real app
            actualHours = 0.0,
            dependencies = emptyList(),
            createdAt = DateUtils.getCurrentTimestamp(),
            updatedAt = DateUtils.getCurrentTimestamp(),
            estimateId = estimateId,
            linkedEntityType = if (estimateId != null) "estimate" else null
        )
        
        save(task)
        return task
    }

    /**
     * Split a task into subtasks
     */
    suspend fun splitTask(taskId: String, subtasks: List<Task>): Boolean {
        try {
            // Get the parent task
            val parentTask = _tasks.value.find { it.id == taskId } ?: return false
            
            // Update the parent task to indicate it has been split
            val updatedParentTask = parentTask.copy(
                status = "Split",
                updatedAt = DateUtils.getCurrentTimestamp()
            )
            
            // Create a new list with the updated parent task and new subtasks
            val updatedTasks = _tasks.value.map { 
                if (it.id == taskId) updatedParentTask else it 
            } + subtasks
            
            _tasks.value = updatedTasks
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error splitting task: ${e.message}")
            return false
        }
    }

    /**
     * Load sample data for demonstration
     */
    private fun loadSampleData() {
        val sampleTasks = listOf(
            Task(
                id = "task_1",
                title = "Design Kitchen Layout",
                description = "Create detailed kitchen layout design including cabinet placement, appliance locations, and dimensions.",
                assignedTo = "designer_1",
                status = TaskStatus.COMPLETED.displayName,
                priority = TaskPriority.HIGH.displayName,
                dueDate = "2023-06-20",
                completedDate = "2023-06-18",
                estimatedHours = 8.0,
                actualHours = 10.5,
                dependencies = emptyList(),
                createdAt = "2023-06-10 09:00:00",
                updatedAt = "2023-06-18 15:30:00",
                projectId = "project_1",
                linkedEntityType = "project"
            ),
            Task(
                id = "task_2",
                title = "Order Kitchen Cabinets",
                description = "Place order for custom kitchen cabinets based on approved design.",
                assignedTo = "pm_1",
                status = TaskStatus.IN_PROGRESS.displayName,
                priority = TaskPriority.HIGH.displayName,
                dueDate = "2023-06-25",
                completedDate = null,
                estimatedHours = 2.0,
                actualHours = 1.5,
                dependencies = listOf("task_1"),
                createdAt = "2023-06-18 16:00:00",
                updatedAt = "2023-06-20 10:15:00",
                projectId = "project_1",
                estimateId = "1",
                linkedEntityType = "project"
            ),
            Task(
                id = "task_3",
                title = "Schedule Plumbing Inspection",
                description = "Schedule city inspector to review bathroom plumbing rough-in.",
                assignedTo = "pm_1",
                status = TaskStatus.TODO.displayName,
                priority = TaskPriority.MEDIUM.displayName,
                dueDate = "2023-07-05",
                completedDate = null,
                estimatedHours = 1.0,
                actualHours = 0.0,
                dependencies = emptyList(),
                createdAt = "2023-06-25 11:30:00",
                updatedAt = "2023-06-25 11:30:00",
                projectId = "project_2",
                linkedEntityType = "project"
            ),
            Task(
                id = "task_4",
                title = "Review Deck Material Options",
                description = "Research and compare composite decking material options for client approval.",
                assignedTo = "designer_2",
                status = TaskStatus.IN_PROGRESS.displayName,
                priority = TaskPriority.LOW.displayName,
                dueDate = "2023-07-10",
                completedDate = null,
                estimatedHours = 4.0,
                actualHours = 2.5,
                dependencies = emptyList(),
                createdAt = "2023-06-22 14:00:00",
                updatedAt = "2023-06-26 09:45:00",
                projectId = "project_3",
                materialId = "material_1",
                linkedEntityType = "material"
            ),
            Task(
                id = "task_5",
                title = "Prepare Bathroom Estimate",
                description = "Create detailed estimate for bathroom renovation project.",
                assignedTo = "estimator_1",
                status = TaskStatus.COMPLETED.displayName,
                priority = TaskPriority.HIGH.displayName,
                dueDate = "2023-06-15",
                completedDate = "2023-06-14",
                estimatedHours = 6.0,
                actualHours = 5.5,
                dependencies = emptyList(),
                createdAt = "2023-06-10 08:30:00",
                updatedAt = "2023-06-14 16:20:00",
                estimateId = "3",
                linkedEntityType = "estimate"
            )
        )
        
        _tasks.value = sampleTasks
    }

    companion object {
        private const val TAG = "TaskRepository"
    }
}