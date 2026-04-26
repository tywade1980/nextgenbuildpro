package com.constructionmanager.data.database.dao

import androidx.room.*
import com.constructionmanager.data.database.entity.ProjectEntity
import com.constructionmanager.domain.model.ProjectStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>
    
    @Query("SELECT * FROM projects WHERE status = :status ORDER BY updatedAt DESC")
    fun getProjectsByStatus(status: ProjectStatus): Flow<List<ProjectEntity>>
    
    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: String): ProjectEntity?
    
    @Query("SELECT * FROM projects WHERE name LIKE '%' || :searchQuery || '%' OR address LIKE '%' || :searchQuery || '%' OR clientName LIKE '%' || :searchQuery || '%'")
    fun searchProjects(searchQuery: String): Flow<List<ProjectEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)
    
    @Update
    suspend fun updateProject(project: ProjectEntity)
    
    @Delete
    suspend fun deleteProject(project: ProjectEntity)
    
    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: String)
    
    @Query("SELECT COUNT(*) FROM projects WHERE status = :status")
    suspend fun getProjectCountByStatus(status: ProjectStatus): Int
    
    @Query("SELECT SUM(CAST(totalBudget AS REAL)) FROM projects WHERE status IN (:statuses)")
    suspend fun getTotalBudgetByStatuses(statuses: List<ProjectStatus>): Double?
    
    @Query("SELECT SUM(CAST(currentCost AS REAL)) FROM projects WHERE status IN (:statuses)")
    suspend fun getTotalCurrentCostByStatuses(statuses: List<ProjectStatus>): Double?
}