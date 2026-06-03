package com.constructionmanager.data.database.dao

import androidx.room.*
import com.constructionmanager.data.database.entity.MaterialEntity
import com.constructionmanager.domain.model.MaterialCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    
    @Query("SELECT * FROM materials WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveMaterials(): Flow<List<MaterialEntity>>
    
    @Query("SELECT * FROM materials WHERE category = :category AND isActive = 1 ORDER BY name ASC")
    fun getMaterialsByCategory(category: MaterialCategory): Flow<List<MaterialEntity>>
    
    @Query("SELECT * FROM materials WHERE id = :materialId")
    suspend fun getMaterialById(materialId: String): MaterialEntity?
    
    @Query("SELECT * FROM materials WHERE name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' AND isActive = 1")
    fun searchMaterials(searchQuery: String): Flow<List<MaterialEntity>>
    
    @Query("SELECT * FROM materials WHERE supplier = :supplier AND isActive = 1 ORDER BY name ASC")
    fun getMaterialsBySupplier(supplier: String): Flow<List<MaterialEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<MaterialEntity>)
    
    @Update
    suspend fun updateMaterial(material: MaterialEntity)
    
    @Delete
    suspend fun deleteMaterial(material: MaterialEntity)
    
    @Query("UPDATE materials SET isActive = 0 WHERE id = :materialId")
    suspend fun deactivateMaterial(materialId: String)
    
    @Query("SELECT DISTINCT category FROM materials WHERE isActive = 1 ORDER BY category ASC")
    suspend fun getAllCategories(): List<MaterialCategory>
    
    @Query("SELECT DISTINCT supplier FROM materials WHERE isActive = 1 ORDER BY supplier ASC")
    suspend fun getAllSuppliers(): List<String>
}