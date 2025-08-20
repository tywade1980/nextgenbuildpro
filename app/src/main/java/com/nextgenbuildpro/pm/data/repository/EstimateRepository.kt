package com.nextgenbuildpro.pm.data.repository

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.core.DateUtils
import com.nextgenbuildpro.pm.data.model.Estimate
import com.nextgenbuildpro.pm.data.model.EstimateItem
import com.nextgenbuildpro.pm.data.model.EstimateStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.NumberFormat
import java.util.Locale

/**
 * Repository for managing estimates in the PM module
 */
class EstimateRepository(private val context: Context) : Repository<Estimate> {
    private val _estimates = MutableStateFlow<List<Estimate>>(emptyList())
    val estimates: StateFlow<List<Estimate>> = _estimates.asStateFlow()

    private val _componentTree = MutableStateFlow<JSONObject?>(null)
    private val _assemblyLibrary = MutableStateFlow<JSONArray?>(null)

    init {
        // Load sample data and JSON files
        loadSampleData()
        loadJsonFiles()
    }

    /**
     * Get all estimates
     */
    override suspend fun getAll(): List<Estimate> {
        return _estimates.value
    }

    /**
     * Get an estimate by ID
     */
    override suspend fun getById(id: String): Estimate? {
        return _estimates.value.find { it.id == id }
    }

    /**
     * Save a new estimate
     */
    override suspend fun save(item: Estimate): Boolean {
        try {
            _estimates.value = _estimates.value + item
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving estimate: ${e.message}")
            return false
        }
    }

    /**
     * Update an existing estimate
     */
    override suspend fun update(item: Estimate): Boolean {
        try {
            _estimates.value = _estimates.value.map { 
                if (it.id == item.id) item else it 
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating estimate: ${e.message}")
            return false
        }
    }

    /**
     * Delete an estimate by ID
     */
    override suspend fun delete(id: String): Boolean {
        try {
            _estimates.value = _estimates.value.filter { it.id != id }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting estimate: ${e.message}")
            return false
        }
    }

    /**
     * Get estimates filtered by status
     */
    suspend fun getEstimatesByStatus(status: String): List<Estimate> {
        return _estimates.value.filter { it.status == status }
    }

    /**
     * Get estimates for a specific project
     */
    suspend fun getEstimatesForProject(projectId: String): List<Estimate> {
        return _estimates.value.filter { it.projectId == projectId }
    }

    /**
     * Get estimates for a specific client by name
     */
    suspend fun getEstimatesForClient(clientName: String): List<Estimate> {
        return _estimates.value.filter { it.clientName == clientName }
    }

    /**
     * Search estimates by title or client name
     */
    suspend fun searchEstimates(query: String): List<Estimate> {
        if (query.isBlank()) return _estimates.value

        val lowercaseQuery = query.lowercase()
        return _estimates.value.filter { 
            it.title.lowercase().contains(lowercaseQuery) || 
            it.clientName.lowercase().contains(lowercaseQuery) 
        }
    }

    /**
     * Update an estimate's status
     */
    suspend fun updateEstimateStatus(estimateId: String, newStatus: String): Boolean {
        try {
            _estimates.value = _estimates.value.map { estimate ->
                if (estimate.id == estimateId) {
                    estimate.copy(
                        status = newStatus,
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    estimate
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating estimate status: ${e.message}")
            return false
        }
    }

    /**
     * Add an item to an estimate
     */
    suspend fun addItemToEstimate(estimateId: String, item: EstimateItem): Boolean {
        try {
            _estimates.value = _estimates.value.map { estimate ->
                if (estimate.id == estimateId) {
                    val updatedItems = estimate.items + item
                    val newTotal = calculateEstimateTotal(updatedItems)

                    estimate.copy(
                        items = updatedItems,
                        amount = newTotal,
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    estimate
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding item to estimate: ${e.message}")
            return false
        }
    }

    /**
     * Remove an item from an estimate
     */
    suspend fun removeItemFromEstimate(estimateId: String, itemId: String): Boolean {
        try {
            _estimates.value = _estimates.value.map { estimate ->
                if (estimate.id == estimateId) {
                    val updatedItems = estimate.items.filter { it.id != itemId }
                    val newTotal = calculateEstimateTotal(updatedItems)

                    estimate.copy(
                        items = updatedItems,
                        amount = newTotal,
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    estimate
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing item from estimate: ${e.message}")
            return false
        }
    }

    /**
     * Update an item in an estimate
     */
    suspend fun updateEstimateItem(estimateId: String, updatedItem: EstimateItem): Boolean {
        try {
            _estimates.value = _estimates.value.map { estimate ->
                if (estimate.id == estimateId) {
                    val updatedItems = estimate.items.map { 
                        if (it.id == updatedItem.id) updatedItem else it 
                    }
                    val newTotal = calculateEstimateTotal(updatedItems)

                    estimate.copy(
                        items = updatedItems,
                        amount = newTotal,
                        updatedAt = DateUtils.getCurrentTimestamp()
                    )
                } else {
                    estimate
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating estimate item: ${e.message}")
            return false
        }
    }

    /**
     * Calculate the total amount for an estimate based on its items
     */
    private fun calculateEstimateTotal(items: List<EstimateItem>): Double {
        return items.sumOf { it.quantity * it.unitPrice }
    }

    /**
     * Load JSON files for component tree and assembly library
     */
    private fun loadJsonFiles() {
        try {
            // Load component tree
            val componentTreeFile = File("C:\\Users\\Tyler\\Downloads\\Quick Share\\Caroline_Estimate_Stack\\component_tree.json")
            if (componentTreeFile.exists()) {
                val jsonString = componentTreeFile.readText()
                _componentTree.value = JSONObject(jsonString)
                Log.d(TAG, "Component tree loaded successfully")
            } else {
                Log.e(TAG, "Component tree file not found")
            }

            // Load assembly library
            val assemblyLibraryFile = File("C:\\Users\\Tyler\\Downloads\\Quick Share\\Caroline_Estimate_Stack\\assembly_library.json")
            if (assemblyLibraryFile.exists()) {
                val jsonString = assemblyLibraryFile.readText()
                _assemblyLibrary.value = JSONArray(jsonString)
                Log.d(TAG, "Assembly library loaded successfully")
            } else {
                Log.e(TAG, "Assembly library file not found")
            }

            // Process JSON data into estimates
            processJsonDataIntoEstimates()

        } catch (e: Exception) {
            Log.e(TAG, "Error loading JSON files: ${e.message}")
        }
    }

    /**
     * Process JSON data into estimates
     */
    private fun processJsonDataIntoEstimates() {
        val newEstimates = mutableListOf<Estimate>()

        // Process component tree into estimates
        _componentTree.value?.let { componentTree ->
            val categories = componentTree.keys()
            while (categories.hasNext()) {
                val category = categories.next()
                val subCategory = componentTree.getJSONObject(category)

                // Create an estimate for each main category
                val estimate = Estimate(
                    id = "comp_${System.currentTimeMillis()}",
                    projectId = null, // Not associated with a project yet
                    title = "$category Package",
                    clientName = "New Client",
                    amount = calculateCategoryAmount(subCategory),
                    status = EstimateStatus.DRAFT.displayName,
                    createdAt = DateUtils.getCurrentTimestamp(),
                    updatedAt = DateUtils.getCurrentTimestamp(),
                    items = createEstimateItemsFromCategory(category, subCategory)
                )
                newEstimates.add(estimate)
            }
        }

        // Add estimates from assembly library
        _assemblyLibrary.value?.let { assemblyLibrary ->
            for (i in 0 until assemblyLibrary.length()) {
                val assembly = assemblyLibrary.getJSONObject(i)
                val trade = assembly.optString("trade", "Unknown")
                val scope = assembly.optString("scope", "Unknown")

                // Create estimate items from materials
                val items = mutableListOf<EstimateItem>()
                val materialsArray = assembly.optJSONArray("materials")
                if (materialsArray != null) {
                    for (j in 0 until materialsArray.length()) {
                        val material = materialsArray.getJSONObject(j)
                        items.add(
                            EstimateItem(
                                id = "mat_${System.currentTimeMillis() + j}",
                                name = material.optString("name", "Unknown Material"),
                                description = "Material for $scope",
                                quantity = material.optDouble("qtyPerUnit", 1.0),
                                unitPrice = 0.0, // Price would be calculated in a real app
                                unit = material.optString("unit", "ea"),
                                type = "Material"
                            )
                        )
                    }
                }

                // Add labor item
                val labor = assembly.optJSONObject("labor")
                if (labor != null) {
                    items.add(
                        EstimateItem(
                            id = "labor_${System.currentTimeMillis()}",
                            name = "${labor.optString("trade", "Unknown")} Labor",
                            description = "Labor for $scope",
                            quantity = labor.optDouble("hoursPerUnit", 1.0),
                            unitPrice = labor.optDouble("rate", 0.0),
                            unit = "hours",
                            type = "Labor"
                        )
                    )
                }

                // Create estimate
                val estimate = Estimate(
                    id = "asm_${assembly.optString("assemblyId", System.currentTimeMillis().toString())}",
                    projectId = null, // Not associated with a project yet
                    title = "$trade - $scope",
                    clientName = "New Client",
                    amount = calculateEstimateTotal(items),
                    status = EstimateStatus.DRAFT.displayName,
                    createdAt = DateUtils.getCurrentTimestamp(),
                    updatedAt = DateUtils.getCurrentTimestamp(),
                    items = items
                )
                newEstimates.add(estimate)
            }
        }

        // Add new estimates to existing ones
        _estimates.value = _estimates.value + newEstimates
    }

    /**
     * Create estimate items from a category in the component tree
     */
    private fun createEstimateItemsFromCategory(category: String, subCategory: JSONObject): List<EstimateItem> {
        val items = mutableListOf<EstimateItem>()
        val subCategories = subCategory.keys()

        while (subCategories.hasNext()) {
            val subCat = subCategories.next()
            items.add(
                EstimateItem(
                    id = "item_${System.currentTimeMillis()}",
                    name = subCat,
                    description = "Part of $category category",
                    quantity = 1.0,
                    unitPrice = 100.0, // Default price, would be calculated in a real app
                    unit = "ea",
                    type = "Material"
                )
            )

            // Process nested subcategories if they exist
            val nestedSubCat = subCategory.optJSONObject(subCat)
            if (nestedSubCat != null && nestedSubCat.length() > 0) {
                items.addAll(createEstimateItemsFromCategory("$category - $subCat", nestedSubCat))
            }
        }

        return items
    }

    /**
     * Calculate the amount for a category in the component tree
     */
    private fun calculateCategoryAmount(subCategory: JSONObject): Double {
        // In a real app, this would calculate based on actual prices
        // For now, just use a simple calculation
        return subCategory.length() * 100.0
    }

    /**
     * Load sample data for demonstration
     */
    private fun loadSampleData() {
        val sampleEstimates = listOf(
            Estimate(
                id = "1",
                projectId = "project_1",
                title = "Ashley F. Built ins",
                clientName = "Ashley Fernandes",
                amount = 7500.0,
                status = EstimateStatus.DRAFT.displayName,
                createdAt = "2023-05-15 10:30:00",
                updatedAt = "2023-05-15 10:30:00",
                items = listOf(
                    EstimateItem(
                        id = "item_1",
                        name = "Custom Cabinets",
                        description = "Built-in cabinets for living room",
                        quantity = 1.0,
                        unitPrice = 4500.0,
                        unit = "set",
                        type = "Material"
                    ),
                    EstimateItem(
                        id = "item_2",
                        name = "Installation Labor",
                        description = "Labor for cabinet installation",
                        quantity = 20.0,
                        unitPrice = 75.0,
                        unit = "hour",
                        type = "Labor"
                    ),
                    EstimateItem(
                        id = "item_3",
                        name = "Hardware",
                        description = "Cabinet handles and hinges",
                        quantity = 1.0,
                        unitPrice = 500.0,
                        unit = "set",
                        type = "Material"
                    )
                )
            ),
            Estimate(
                id = "2",
                projectId = "project_2",
                title = "Laurie H. Kitchen",
                clientName = "Laurie Huth",
                amount = 12350.0,
                status = EstimateStatus.SENT.displayName,
                createdAt = "2023-05-10 14:45:00",
                updatedAt = "2023-05-12 09:15:00",
                items = listOf(
                    EstimateItem(
                        id = "item_4",
                        name = "Kitchen Cabinets",
                        description = "New kitchen cabinets",
                        quantity = 1.0,
                        unitPrice = 7500.0,
                        unit = "set",
                        type = "Material"
                    ),
                    EstimateItem(
                        id = "item_5",
                        name = "Countertops",
                        description = "Granite countertops",
                        quantity = 30.0,
                        unitPrice = 85.0,
                        unit = "sqft",
                        type = "Material"
                    ),
                    EstimateItem(
                        id = "item_6",
                        name = "Installation Labor",
                        description = "Labor for kitchen renovation",
                        quantity = 40.0,
                        unitPrice = 65.0,
                        unit = "hour",
                        type = "Labor"
                    )
                )
            ),
            Estimate(
                id = "3",
                projectId = "project_3",
                title = "Michael J. Bathroom",
                clientName = "Michael Johnson",
                amount = 8750.0,
                status = EstimateStatus.APPROVED.displayName,
                createdAt = "2023-05-05 11:20:00",
                updatedAt = "2023-05-11 16:30:00",
                items = listOf(
                    EstimateItem(
                        id = "item_7",
                        name = "Bathroom Vanity",
                        description = "Double sink vanity with marble top",
                        quantity = 1.0,
                        unitPrice = 2200.0,
                        unit = "ea",
                        type = "Material"
                    ),
                    EstimateItem(
                        id = "item_8",
                        name = "Shower Enclosure",
                        description = "Glass shower enclosure",
                        quantity = 1.0,
                        unitPrice = 1800.0,
                        unit = "ea",
                        type = "Material"
                    ),
                    EstimateItem(
                        id = "item_9",
                        name = "Tile Work",
                        description = "Floor and wall tile installation",
                        quantity = 120.0,
                        unitPrice = 18.0,
                        unit = "sqft",
                        type = "Material"
                    ),
                    EstimateItem(
                        id = "item_10",
                        name = "Plumbing Labor",
                        description = "Plumbing installation and hookups",
                        quantity = 16.0,
                        unitPrice = 85.0,
                        unit = "hour",
                        type = "Labor"
                    )
                )
            )
        )

        _estimates.value = sampleEstimates
    }

    companion object {
        private const val TAG = "EstimateRepository"
    }
}
