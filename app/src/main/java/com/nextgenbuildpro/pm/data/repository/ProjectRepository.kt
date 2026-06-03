package com.nextgenbuildpro.pm.data.repository

import com.nextgenbuildpro.core.Address
import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.core.DateUtils
import com.nextgenbuildpro.pm.data.model.DailyLog
import com.nextgenbuildpro.pm.data.model.Project
import com.nextgenbuildpro.pm.data.model.ProjectStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing projects in the PM module
 */
class ProjectRepository : Repository<Project> {
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val projectsCollection = db.collection("projects")

    /**
     * Load all projects from Firestore and populate the in-memory StateFlow
     */
    suspend fun loadFromFirestore() {
        try {
            val snapshot = projectsCollection.get().await()
            val loadedProjects = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                Project(
                    id = doc.id,
                    name = data["name"] as? String ?: "",
                    clientId = data["clientId"] as? String ?: "",
                    clientName = data["clientName"] as? String ?: "",
                    leadId = data["leadId"] as? String ?: "",
                    address = (data["address"] as? Map<*, *>)?.let { addr ->
                        Address(
                            street = addr["street"] as? String ?: "",
                            city = addr["city"] as? String ?: "",
                            state = addr["state"] as? String ?: "",
                            zipCode = addr["zipCode"] as? String ?: ""
                        )
                    } ?: Address(),
                    status = data["status"] as? String ?: ProjectStatus.PLANNING.displayName,
                    startDate = data["startDate"] as? String ?: "",
                    endDate = data["endDate"] as? String ?: "",
                    budget = (data["budget"] as? Number)?.toDouble() ?: 0.0,
                    actualCost = (data["actualCost"] as? Number)?.toDouble() ?: 0.0,
                    progress = (data["progress"] as? Number)?.toInt() ?: 0,
                    description = data["description"] as? String ?: "",
                    notes = data["notes"] as? String ?: "",
                    lastActivityDate = data["lastActivityDate"] as? String ?: "",
                    createdAt = data["createdAt"] as? String ?: "",
                    updatedAt = data["updatedAt"] as? String ?: ""
                )
            }
            _projects.value = loadedProjects
        } catch (e: Exception) {
            // Leave cache unchanged on error
        }
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
        return try {
            projectsCollection.document(item.id).set(item.toFirestoreMap()).await()
            _projects.value = _projects.value + item
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update an existing project
     */
    override suspend fun update(item: Project): Boolean {
        return try {
            projectsCollection.document(item.id).set(item.toFirestoreMap()).await()
            _projects.value = _projects.value.map {
                if (it.id == item.id) item else it
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete a project by ID
     */
    override suspend fun delete(id: String): Boolean {
        return try {
            projectsCollection.document(id).delete().await()
            _projects.value = _projects.value.filter { it.id != id }
            true
        } catch (e: Exception) {
            false
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
            val updatedList = _projects.value.map { project ->
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
            val updated = updatedList.find { it.id == projectId } ?: return false
            projectsCollection.document(projectId).set(updated.toFirestoreMap()).await()
            _projects.value = updatedList
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
            val updatedList = _projects.value.map { project ->
                if (project.id == projectId) {
                    project.copy(
                        status = newStatus,
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    project
                }
            }
            val updated = updatedList.find { it.id == projectId } ?: return false
            projectsCollection.document(projectId).update(
                mapOf("status" to newStatus, "updatedAt" to updated.updatedAt)
            ).await()
            _projects.value = updatedList
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
            val updatedList = _projects.value.map { project ->
                if (project.id == projectId) {
                    project.copy(
                        progress = progress,
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    project
                }
            }
            val updated = updatedList.find { it.id == projectId } ?: return false
            projectsCollection.document(projectId).update(
                mapOf("progress" to progress, "updatedAt" to updated.updatedAt)
            ).await()
            _projects.value = updatedList
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
            val updatedList = _projects.value.map { project ->
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
            val updated = updatedList.find { it.id == dailyLog.projectId } ?: return false
            projectsCollection.document(dailyLog.projectId).set(updated.toFirestoreMap()).await()
            _projects.value = updatedList
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Convert a Project to a Firestore-compatible map
     */
    private fun Project.toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "clientId" to clientId,
        "clientName" to clientName,
        "leadId" to leadId,
        "address" to mapOf(
            "street" to address.street,
            "city" to address.city,
            "state" to address.state,
            "zipCode" to address.zipCode
        ),
        "status" to status,
        "startDate" to startDate,
        "endDate" to endDate,
        "budget" to budget,
        "actualCost" to actualCost,
        "progress" to progress,
        "description" to description,
        "notes" to notes,
        "lastActivityDate" to lastActivityDate,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}
