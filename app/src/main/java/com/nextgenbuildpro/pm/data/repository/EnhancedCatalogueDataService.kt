package com.nextgenbuildpro.pm.data.repository

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

/**
 * Enhanced Catalogue Data Service
 * 
 * Provides comprehensive CRUD operations for the hierarchical catalogue structure
 * Based on the Firebase service pattern suggested in the comments but adapted for Android
 */
class EnhancedCatalogueDataService(private val context: Context) {
    private val TAG = "EnhancedCatalogueDataService"
    
    // In-memory storage for demonstration (would be replaced with Room database)
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _trades = MutableStateFlow<List<Trade>>(emptyList())
    val trades: StateFlow<List<Trade>> = _trades.asStateFlow()
    
    private val _scopes = MutableStateFlow<List<Scope>>(emptyList())
    val scopes: StateFlow<List<Scope>> = _scopes.asStateFlow()
    
    private val _assemblies = MutableStateFlow<List<EnhancedAssembly>>(emptyList())
    val assemblies: StateFlow<List<EnhancedAssembly>> = _assemblies.asStateFlow()
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    
    private val _materials = MutableStateFlow<List<Material>>(emptyList())
    val materials: StateFlow<List<Material>> = _materials.asStateFlow()
    
    init {
        loadSampleData()
    }
    
    // Create operations
    
    /**
     * Create new category
     */
    suspend fun createCategory(
        name: String,
        description: String,
        sequence: Int,
        imageUrl: String? = null
    ): Result<Category> {
        return try {
            val category = Category(
                name = name,
                description = description,
                sequence = sequence,
                imageUrl = imageUrl
            )
            
            val currentCategories = _categories.value.toMutableList()
            currentCategories.add(category)
            _categories.value = currentCategories
            
            Log.d(TAG, "Created category: ${category.name}")
            Result.success(category)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating category: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Create new trade
     */
    suspend fun createTrade(
        categoryId: String,
        name: String,
        description: String,
        sequence: Int,
        imageUrl: String? = null
    ): Result<Trade> {
        return try {
            val trade = Trade(
                categoryId = categoryId,
                name = name,
                description = description,
                sequence = sequence,
                imageUrl = imageUrl
            )
            
            val currentTrades = _trades.value.toMutableList()
            currentTrades.add(trade)
            _trades.value = currentTrades
            
            Log.d(TAG, "Created trade: ${trade.name}")
            Result.success(trade)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating trade: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Create new scope
     */
    suspend fun createScope(
        tradeId: String,
        name: String,
        description: String,
        sequence: Int,
        imageUrl: String? = null
    ): Result<Scope> {
        return try {
            val scope = Scope(
                tradeId = tradeId,
                name = name,
                description = description,
                sequence = sequence,
                imageUrl = imageUrl
            )
            
            val currentScopes = _scopes.value.toMutableList()
            currentScopes.add(scope)
            _scopes.value = currentScopes
            
            Log.d(TAG, "Created scope: ${scope.name}")
            Result.success(scope)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating scope: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Create new assembly
     */
    suspend fun createAssembly(
        scopeId: String,
        name: String,
        description: String,
        sequence: Int,
        unit: String,
        laborHours: Double,
        materialCost: Double,
        laborCost: Double,
        equipmentCost: Double = 0.0,
        subcontractorCost: Double = 0.0,
        otherCost: Double = 0.0,
        markupPercentage: Double = 0.2,
        notes: String = "",
        tags: List<String> = emptyList(),
        imageUrl: String? = null
    ): Result<EnhancedAssembly> {
        return try {
            val totalCost = laborCost + materialCost + equipmentCost + subcontractorCost + otherCost
            
            val assembly = EnhancedAssembly(
                scopeId = scopeId,
                name = name,
                description = description,
                sequence = sequence,
                unit = unit,
                laborHours = laborHours,
                materialCost = materialCost,
                laborCost = laborCost,
                equipmentCost = equipmentCost,
                subcontractorCost = subcontractorCost,
                otherCost = otherCost,
                totalCost = totalCost,
                markupPercentage = markupPercentage,
                notes = notes,
                tags = tags,
                imageUrl = imageUrl
            )
            
            val currentAssemblies = _assemblies.value.toMutableList()
            currentAssemblies.add(assembly)
            _assemblies.value = currentAssemblies
            
            Log.d(TAG, "Created assembly: ${assembly.name}")
            Result.success(assembly)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating assembly: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Create new task
     */
    suspend fun createTask(
        assemblyId: String,
        name: String,
        description: String,
        sequence: Int,
        laborHours: Double,
        materialCost: Double,
        laborCost: Double,
        equipmentCost: Double = 0.0,
        notes: String = ""
    ): Result<Task> {
        return try {
            val task = Task(
                assemblyId = assemblyId,
                name = name,
                description = description,
                sequence = sequence,
                laborHours = laborHours,
                materialCost = materialCost,
                laborCost = laborCost,
                equipmentCost = equipmentCost,
                notes = notes
            )
            
            val currentTasks = _tasks.value.toMutableList()
            currentTasks.add(task)
            _tasks.value = currentTasks
            
            Log.d(TAG, "Created task: ${task.name}")
            Result.success(task)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating task: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Create new material
     */
    suspend fun createMaterial(
        taskId: String? = null,
        assemblyId: String? = null,
        name: String,
        description: String,
        quantity: Double,
        unit: String,
        unitCost: Double,
        waste: Double = 0.0,
        notes: String = ""
    ): Result<Material> {
        return try {
            val totalCost = unitCost * quantity * (1 + waste)
            
            val material = Material(
                taskId = taskId,
                assemblyId = assemblyId,
                name = name,
                description = description,
                quantity = quantity,
                unit = unit,
                unitCost = unitCost,
                totalCost = totalCost,
                waste = waste,
                notes = notes
            )
            
            val currentMaterials = _materials.value.toMutableList()
            currentMaterials.add(material)
            _materials.value = currentMaterials
            
            Log.d(TAG, "Created material: ${material.name}")
            Result.success(material)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating material: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Create complete assembly with tasks and materials
     */
    suspend fun createCompleteAssembly(request: CompleteAssemblyRequest): Result<AssemblyWithChildren> {
        return try {
            // Create the assembly first
            val assemblyResult = createAssembly(
                scopeId = request.assembly.scopeId,
                name = request.assembly.name,
                description = request.assembly.description,
                sequence = request.assembly.sequence,
                unit = request.assembly.unit,
                laborHours = request.assembly.laborHours,
                materialCost = request.assembly.materialCost,
                laborCost = request.assembly.laborCost,
                equipmentCost = request.assembly.equipmentCost,
                subcontractorCost = request.assembly.subcontractorCost,
                otherCost = request.assembly.otherCost,
                markupPercentage = request.assembly.markupPercentage,
                notes = request.assembly.notes,
                tags = request.assembly.tags,
                imageUrl = request.assembly.imageUrl
            )
            
            val assembly = assemblyResult.getOrThrow()
            
            // Create tasks
            val createdTasks = mutableListOf<TaskWithMaterials>()
            for (taskData in request.tasks) {
                val taskResult = createTask(
                    assemblyId = assembly.id,
                    name = taskData.name,
                    description = taskData.description,
                    sequence = taskData.sequence,
                    laborHours = taskData.laborHours,
                    materialCost = taskData.materialCost,
                    laborCost = taskData.laborCost,
                    equipmentCost = taskData.equipmentCost,
                    notes = taskData.notes
                )
                
                val task = taskResult.getOrThrow()
                
                // Create materials for this task
                val taskMaterials = request.materials.filter { it.taskId == taskData.id }
                val createdTaskMaterials = mutableListOf<Material>()
                
                for (materialData in taskMaterials) {
                    val materialResult = createMaterial(
                        taskId = task.id,
                        assemblyId = assembly.id,
                        name = materialData.name,
                        description = materialData.description,
                        quantity = materialData.quantity,
                        unit = materialData.unit,
                        unitCost = materialData.unitCost,
                        waste = materialData.waste,
                        notes = materialData.notes
                    )
                    
                    createdTaskMaterials.add(materialResult.getOrThrow())
                }
                
                createdTasks.add(TaskWithMaterials(task, createdTaskMaterials))
            }
            
            // Create direct assembly materials
            val directMaterials = request.materials.filter { it.assemblyId != null && it.taskId == null }
            val createdDirectMaterials = mutableListOf<Material>()
            
            for (materialData in directMaterials) {
                val materialResult = createMaterial(
                    assemblyId = assembly.id,
                    name = materialData.name,
                    description = materialData.description,
                    quantity = materialData.quantity,
                    unit = materialData.unit,
                    unitCost = materialData.unitCost,
                    waste = materialData.waste,
                    notes = materialData.notes
                )
                
                createdDirectMaterials.add(materialResult.getOrThrow())
            }
            
            val completeAssembly = AssemblyWithChildren(
                assembly = assembly,
                tasks = createdTasks,
                materials = createdDirectMaterials
            )
            
            Log.d(TAG, "Created complete assembly: ${assembly.name} with ${createdTasks.size} tasks")
            Result.success(completeAssembly)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating complete assembly: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Read operations
    
    /**
     * Get categories with all children
     */
    suspend fun getCategoriesWithChildren(): Result<List<CategoryWithChildren>> {
        return try {
            val categoriesWithChildren = _categories.value
                .filter { it.isActive }
                .sortedBy { it.sequence }
                .map { category ->
                    val categoryTrades = _trades.value
                        .filter { it.categoryId == category.id && it.isActive }
                        .sortedBy { it.sequence }
                        .map { trade ->
                            val tradeScopes = _scopes.value
                                .filter { it.tradeId == trade.id && it.isActive }
                                .sortedBy { it.sequence }
                                .map { scope ->
                                    val scopeAssemblies = _assemblies.value
                                        .filter { it.scopeId == scope.id && it.isActive }
                                        .sortedBy { it.sequence }
                                        .map { assembly ->
                                            val assemblyTasks = _tasks.value
                                                .filter { it.assemblyId == assembly.id && it.isActive }
                                                .sortedBy { it.sequence }
                                                .map { task ->
                                                    val taskMaterials = _materials.value
                                                        .filter { it.taskId == task.id && it.isActive }
                                                    TaskWithMaterials(task, taskMaterials)
                                                }
                                            
                                            val directMaterials = _materials.value
                                                .filter { it.assemblyId == assembly.id && it.taskId == null && it.isActive }
                                            
                                            AssemblyWithChildren(assembly, assemblyTasks, directMaterials)
                                        }
                                    
                                    ScopeWithChildren(scope, scopeAssemblies)
                                }
                            
                            TradeWithChildren(trade, tradeScopes)
                        }
                    
                    CategoryWithChildren(category, categoryTrades)
                }
            
            Result.success(categoriesWithChildren)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting categories with children: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Search assemblies by criteria
     */
    suspend fun searchAssemblies(criteria: EnhancedCatalogueSearchCriteria): Result<List<AssemblySearchResultWithContext>> {
        return try {
            var filteredAssemblies = _assemblies.value.filter { it.isActive }
            
            // Apply filters
            criteria.categoryId?.let { categoryId ->
                val categoryTradeIds = _trades.value.filter { it.categoryId == categoryId }.map { it.id }
                val categoryScopeIds = _scopes.value.filter { it.tradeId in categoryTradeIds }.map { it.id }
                filteredAssemblies = filteredAssemblies.filter { it.scopeId in categoryScopeIds }
            }
            
            criteria.tradeId?.let { tradeId ->
                val tradeScopeIds = _scopes.value.filter { it.tradeId == tradeId }.map { it.id }
                filteredAssemblies = filteredAssemblies.filter { it.scopeId in tradeScopeIds }
            }
            
            criteria.scopeId?.let { scopeId ->
                filteredAssemblies = filteredAssemblies.filter { it.scopeId == scopeId }
            }
            
            criteria.query?.let { query ->
                val searchText = query.lowercase()
                filteredAssemblies = filteredAssemblies.filter { assembly ->
                    assembly.name.lowercase().contains(searchText) ||
                            assembly.description.lowercase().contains(searchText) ||
                            assembly.tags.any { it.lowercase().contains(searchText) }
                }
            }
            
            if (criteria.tags.isNotEmpty()) {
                filteredAssemblies = filteredAssemblies.filter { assembly ->
                    criteria.tags.any { tag -> assembly.tags.contains(tag) }
                }
            }
            
            // Build results with context
            val results = filteredAssemblies.map { assembly ->
                val scope = _scopes.value.first { it.id == assembly.scopeId }
                val trade = _trades.value.first { it.id == scope.tradeId }
                val category = _categories.value.first { it.id == trade.categoryId }
                val tasks = _tasks.value.filter { it.assemblyId == assembly.id && it.isActive }
                val materials = _materials.value.filter { it.assemblyId == assembly.id && it.isActive }
                
                AssemblySearchResultWithContext(
                    assembly = assembly,
                    scope = scope,
                    trade = trade,
                    category = category,
                    tasks = tasks,
                    materials = materials
                )
            }
            
            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching assemblies: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Get cost breakdown for an assembly
     */
    suspend fun getAssemblyCostBreakdown(assemblyId: String): Result<AssemblyCostBreakdown> {
        return try {
            val assembly = _assemblies.value.first { it.id == assemblyId }
            
            val directCosts = AssemblyDirectCosts(
                labor = assembly.laborCost,
                materials = assembly.materialCost,
                equipment = assembly.equipmentCost,
                subcontractors = assembly.subcontractorCost,
                total = assembly.laborCost + assembly.materialCost + assembly.equipmentCost + assembly.subcontractorCost
            )
            
            val markupAmount = directCosts.total * assembly.markupPercentage
            val indirectCosts = AssemblyIndirectCosts(
                overhead = markupAmount * 0.6, // Assume 60% of markup is overhead
                profit = markupAmount * 0.3,   // 30% profit
                contingency = markupAmount * 0.1, // 10% contingency
                total = markupAmount
            )
            
            val finalCost = directCosts.total + indirectCosts.total
            
            val breakdown = AssemblyCostBreakdown(
                assemblyId = assembly.id,
                assemblyName = assembly.name,
                directCosts = directCosts,
                indirectCosts = indirectCosts,
                totalCost = assembly.totalCost,
                markup = markupAmount,
                finalCost = finalCost
            )
            
            Result.success(breakdown)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting assembly cost breakdown: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Update operations
    
    /**
     * Update category
     */
    suspend fun updateCategory(categoryId: String, updates: Map<String, Any>): Result<Category> {
        return try {
            val currentCategories = _categories.value.toMutableList()
            val index = currentCategories.indexOfFirst { it.id == categoryId }
            
            if (index == -1) {
                return Result.failure(IllegalArgumentException("Category not found"))
            }
            
            val category = currentCategories[index]
            val updatedCategory = category.copy(
                name = updates["name"] as? String ?: category.name,
                description = updates["description"] as? String ?: category.description,
                sequence = updates["sequence"] as? Int ?: category.sequence,
                imageUrl = updates["imageUrl"] as? String ?: category.imageUrl,
                isActive = updates["isActive"] as? Boolean ?: category.isActive,
                updatedAt = LocalDateTime.now()
            )
            
            currentCategories[index] = updatedCategory
            _categories.value = currentCategories
            
            Log.d(TAG, "Updated category: ${updatedCategory.name}")
            Result.success(updatedCategory)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating category: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Delete operations
    
    /**
     * Soft delete category (set isActive = false)
     */
    suspend fun deleteCategory(categoryId: String): Result<Boolean> {
        return updateCategory(categoryId, mapOf("isActive" to false))
            .map { true }
    }
    
    /**
     * Generate unique ID (placeholder implementation)
     */
    private fun generateId(): String {
        return "id-${System.currentTimeMillis()}-${(Math.random() * 1000).toInt()}"
    }
    
    /**
     * Load sample data for demonstration
     */
    private fun loadSampleData() {
        // Sample categories
        val structureCategory = Category(
            name = "Structure",
            description = "Structural components and framing",
            sequence = 1
        )
        
        val enclosureCategory = Category(
            name = "Enclosure",
            description = "Building envelope components",
            sequence = 2
        )
        
        val systemsCategory = Category(
            name = "Systems",
            description = "Mechanical, electrical, and plumbing systems",
            sequence = 3
        )
        
        _categories.value = listOf(structureCategory, enclosureCategory, systemsCategory)
        
        // Sample trades
        val framingTrade = Trade(
            categoryId = structureCategory.id,
            name = "Framing",
            description = "Wood and steel framing",
            sequence = 1
        )
        
        val electricalTrade = Trade(
            categoryId = systemsCategory.id,
            name = "Electrical",
            description = "Electrical systems and wiring",
            sequence = 1
        )
        
        _trades.value = listOf(framingTrade, electricalTrade)
        
        // Sample scopes
        val interiorWallsScope = Scope(
            tradeId = framingTrade.id,
            name = "Interior Walls",
            description = "Interior wall framing",
            sequence = 1
        )
        
        _scopes.value = listOf(interiorWallsScope)
        
        Log.d(TAG, "Sample data loaded successfully")
    }
    
    /**
     * Overloaded createCompleteAssembly method for easier seeding
     * Similar to the JavaScript interface in the seedCatalogue script
     */
    suspend fun createCompleteAssembly(
        assemblyData: Map<String, Any>,
        taskDataList: List<Map<String, Any>>,
        materialDataList: List<Map<String, Any>>
    ): Result<AssemblyWithChildren> {
        return try {
            // Create the assembly first
            val assembly = EnhancedAssembly(
                scopeId = assemblyData["scopeId"] as String,
                name = assemblyData["name"] as String,
                description = assemblyData["description"] as String,
                sequence = assemblyData["sequence"] as Int,
                unit = assemblyData["unit"] as String,
                laborHours = assemblyData["laborHours"] as Double,
                materialCost = assemblyData["materialCost"] as Double,
                laborCost = assemblyData["laborCost"] as Double,
                equipmentCost = (assemblyData["equipmentCost"] as? Double) ?: 0.0,
                subcontractorCost = (assemblyData["subcontractorCost"] as? Double) ?: 0.0,
                otherCost = (assemblyData["otherCost"] as? Double) ?: 0.0,
                totalCost = assemblyData["totalCost"] as Double,
                markupPercentage = (assemblyData["markupPercentage"] as? Double) ?: 0.2,
                notes = (assemblyData["notes"] as? String) ?: "",
                tags = (assemblyData["tags"] as? List<String>) ?: emptyList(),
                imageUrl = assemblyData["imageUrl"] as? String
            )
            
            val currentAssemblies = _assemblies.value.toMutableList()
            currentAssemblies.add(assembly)
            _assemblies.value = currentAssemblies
            
            // Create tasks
            val createdTasks = mutableListOf<TaskWithMaterials>()
            for (taskData in taskDataList) {
                val task = Task(
                    assemblyId = assembly.id,
                    name = taskData["name"] as String,
                    description = taskData["description"] as String,
                    sequence = taskData["sequence"] as Int,
                    laborHours = taskData["laborHours"] as Double,
                    materialCost = taskData["materialCost"] as Double,
                    laborCost = taskData["laborCost"] as Double,
                    equipmentCost = (taskData["equipmentCost"] as? Double) ?: 0.0,
                    notes = (taskData["notes"] as? String) ?: "",
                    isActive = (taskData["isActive"] as? Boolean) ?: true
                )
                
                val currentTasks = _tasks.value.toMutableList()
                currentTasks.add(task)
                _tasks.value = currentTasks
                
                // Find materials for this task (materials with no taskId or matching taskId)
                val taskMaterials = materialDataList.filter { materialData ->
                    val taskIdFromMaterial = materialData["taskId"] as? String
                    taskIdFromMaterial == null || taskIdFromMaterial == task.id
                }.map { materialData ->
                    Material(
                        assemblyId = assembly.id,
                        taskId = task.id,
                        name = materialData["name"] as String,
                        description = materialData["description"] as String,
                        quantity = materialData["quantity"] as Double,
                        unit = materialData["unit"] as String,
                        unitCost = materialData["unitCost"] as Double,
                        totalCost = materialData["totalCost"] as Double,
                        waste = (materialData["waste"] as? Double) ?: 0.0,
                        notes = (materialData["notes"] as? String) ?: "",
                        isActive = (materialData["isActive"] as? Boolean) ?: true
                    )
                }
                
                val currentMaterials = _materials.value.toMutableList()
                currentMaterials.addAll(taskMaterials)
                _materials.value = currentMaterials
                
                createdTasks.add(TaskWithMaterials(task, taskMaterials))
            }
            
            // Create direct assembly materials (materials with no specific task)
            val directMaterials = materialDataList.filter { materialData ->
                materialData["taskId"] == null
            }.map { materialData ->
                Material(
                    assemblyId = assembly.id,
                    taskId = null,
                    name = materialData["name"] as String,
                    description = materialData["description"] as String,
                    quantity = materialData["quantity"] as Double,
                    unit = materialData["unit"] as String,
                    unitCost = materialData["unitCost"] as Double,
                    totalCost = materialData["totalCost"] as Double,
                    waste = (materialData["waste"] as? Double) ?: 0.0,
                    notes = (materialData["notes"] as? String) ?: "",
                    isActive = (materialData["isActive"] as? Boolean) ?: true
                )
            }
            
            val currentMaterials = _materials.value.toMutableList()
            currentMaterials.addAll(directMaterials)
            _materials.value = currentMaterials
            
            val assemblyWithChildren = AssemblyWithChildren(assembly, createdTasks, directMaterials)
            
            Log.d(TAG, "Created complete assembly: ${assembly.name}")
            Result.success(assemblyWithChildren)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating complete assembly: ${e.message}")
            Result.failure(e)
        }
    }
}