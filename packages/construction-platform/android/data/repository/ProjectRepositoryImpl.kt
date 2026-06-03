package com.constructionmanager.data.repository

import com.constructionmanager.data.database.dao.ProjectDao
import com.constructionmanager.data.database.entity.ProjectEntity
import com.constructionmanager.domain.model.Project
import com.constructionmanager.domain.model.ProjectStatus
import com.constructionmanager.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao
) : ProjectRepository {
    
    override fun getAllProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getProjectsByStatus(status: ProjectStatus): Flow<List<Project>> {
        return projectDao.getProjectsByStatus(status).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getProjectById(projectId: String): Project? {
        return projectDao.getProjectById(projectId)?.toDomainModel()
    }
    
    override fun searchProjects(searchQuery: String): Flow<List<Project>> {
        return projectDao.searchProjects(searchQuery).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun insertProject(project: Project) {
        projectDao.insertProject(project.toEntity())
    }
    
    override suspend fun updateProject(project: Project) {
        projectDao.updateProject(project.toEntity())
    }
    
    override suspend fun deleteProject(project: Project) {
        projectDao.deleteProjectById(project.id)
    }
    
    override suspend fun getProjectCountByStatus(status: ProjectStatus): Int {
        return projectDao.getProjectCountByStatus(status)
    }
    
    override suspend fun getTotalBudgetByStatuses(statuses: List<ProjectStatus>): Double {
        return projectDao.getTotalBudgetByStatuses(statuses) ?: 0.0
    }
    
    override suspend fun getTotalCurrentCostByStatuses(statuses: List<ProjectStatus>): Double {
        return projectDao.getTotalCurrentCostByStatuses(statuses) ?: 0.0
    }
    
    private fun ProjectEntity.toDomainModel(): Project {
        return Project(
            id = id,
            name = name,
            address = address,
            city = city,
            state = state,
            zipCode = zipCode,
            clientName = clientName,
            clientEmail = clientEmail,
            clientPhone = clientPhone,
            projectType = projectType,
            currentPhase = currentPhase,
            startDate = LocalDate.parse(startDate),
            estimatedEndDate = LocalDate.parse(estimatedEndDate),
            actualEndDate = actualEndDate?.let { LocalDate.parse(it) },
            totalBudget = BigDecimal(totalBudget),
            currentCost = BigDecimal(currentCost),
            status = status,
            notes = notes,
            createdAt = LocalDateTime.parse(createdAt),
            updatedAt = LocalDateTime.parse(updatedAt)
        )
    }
    
    private fun Project.toEntity(): ProjectEntity {
        return ProjectEntity(
            id = id,
            name = name,
            address = address,
            city = city,
            state = state,
            zipCode = zipCode,
            clientName = clientName,
            clientEmail = clientEmail,
            clientPhone = clientPhone,
            projectType = projectType,
            currentPhase = currentPhase,
            startDate = startDate.toString(),
            estimatedEndDate = estimatedEndDate.toString(),
            actualEndDate = actualEndDate?.toString(),
            totalBudget = totalBudget.toString(),
            currentCost = currentCost.toString(),
            status = status,
            notes = notes,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString()
        )
    }
}