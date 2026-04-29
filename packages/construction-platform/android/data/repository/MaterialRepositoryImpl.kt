package com.constructionmanager.data.repository

import com.constructionmanager.data.database.SeedData
import com.constructionmanager.data.database.dao.MaterialDao
import com.constructionmanager.data.database.entity.MaterialEntity
import com.constructionmanager.domain.model.Material
import com.constructionmanager.domain.model.MaterialCategory
import com.constructionmanager.domain.repository.MaterialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaterialRepositoryImpl @Inject constructor(
    private val materialDao: MaterialDao
) : MaterialRepository {
    
    override fun getAllActiveMaterials(): Flow<List<Material>> {
        return materialDao.getAllActiveMaterials().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getMaterialsByCategory(category: MaterialCategory): Flow<List<Material>> {
        return materialDao.getMaterialsByCategory(category).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getMaterialById(materialId: String): Material? {
        return materialDao.getMaterialById(materialId)?.toDomainModel()
    }
    
    override fun searchMaterials(searchQuery: String): Flow<List<Material>> {
        return materialDao.searchMaterials(searchQuery).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getMaterialsBySupplier(supplier: String): Flow<List<Material>> {
        return materialDao.getMaterialsBySupplier(supplier).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun insertMaterial(material: Material) {
        materialDao.insertMaterial(material.toEntity())
    }
    
    override suspend fun insertMaterials(materials: List<Material>) {
        materialDao.insertMaterials(materials.map { it.toEntity() })
    }
    
    override suspend fun updateMaterial(material: Material) {
        materialDao.updateMaterial(material.toEntity())
    }
    
    override suspend fun deactivateMaterial(materialId: String) {
        materialDao.deactivateMaterial(materialId)
    }
    
    override suspend fun getAllCategories(): List<MaterialCategory> {
        return materialDao.getAllCategories()
    }
    
    override suspend fun getAllSuppliers(): List<String> {
        return materialDao.getAllSuppliers()
    }
    
    override suspend fun initializeSeedData() {
        val seedMaterials = SeedData.getMaterials2025()
        materialDao.insertMaterials(seedMaterials)
    }
    
    private fun MaterialEntity.toDomainModel(): Material {
        val specifications: Map<String, String> = try {
            Json.decodeFromString(specifications)
        } catch (e: Exception) {
            emptyMap()
        }
        
        val regionalPricing: Map<String, BigDecimal> = try {
            val stringMap: Map<String, String> = Json.decodeFromString(regionalPricing)
            stringMap.mapValues { BigDecimal(it.value) }
        } catch (e: Exception) {
            emptyMap()
        }
        
        return Material(
            id = id,
            name = name,
            category = category,
            subcategory = subcategory,
            unitOfMeasurement = unitOfMeasurement,
            currentPrice = BigDecimal(currentPrice),
            supplier = supplier,
            supplierSku = supplierSku,
            description = description,
            specifications = specifications,
            isActive = isActive,
            lastPriceUpdate = LocalDateTime.parse(lastPriceUpdate),
            regionalPricing = regionalPricing
        )
    }
    
    private fun Material.toEntity(): MaterialEntity {
        return MaterialEntity(
            id = id,
            name = name,
            category = category,
            subcategory = subcategory,
            unitOfMeasurement = unitOfMeasurement,
            currentPrice = currentPrice.toString(),
            supplier = supplier,
            supplierSku = supplierSku,
            description = description,
            specifications = if (specifications.isEmpty()) "" else specifications.entries.joinToString(",") { "${it.key}:${it.value}" },
            isActive = isActive,
            lastPriceUpdate = lastPriceUpdate.toString(),
            regionalPricing = if (regionalPricing.isEmpty()) "" else regionalPricing.entries.joinToString(",") { "${it.key}:${it.value}" }
        )
    }
}