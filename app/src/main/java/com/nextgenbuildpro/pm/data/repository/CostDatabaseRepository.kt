package com.nextgenbuildpro.pm.data.repository

import android.util.Log
import com.nextgenbuildpro.core.firestore.BaseFirestoreRepository
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.service.CostDataService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Repository for managing construction cost database
 * Handles Firebase persistence and local caching of cost data
 */
class CostDatabaseRepository : BaseFirestoreRepository() {
    
    private val TAG = "CostDatabaseRepository"
    private val db = FirebaseFirestore.getInstance()
    
    // Collections
    private val laborRatesCollection = db.collection("labor_rates")
    private val materialCostsCollection = db.collection("material_costs")
    private val regionalAdjustmentsCollection = db.collection("regional_adjustments")
    private val laborTimesCollection = db.collection("labor_times")
    private val historicalDataCollection = db.collection("historical_labor_data")
    private val marketRatesCollection = db.collection("market_rates")
    
    // Local cache
    private val _laborRates = MutableStateFlow<Map<String, LaborRate>>(emptyMap())
    val laborRates: StateFlow<Map<String, LaborRate>> = _laborRates.asStateFlow()
    
    private val _materialCosts = MutableStateFlow<Map<String, MaterialCost>>(emptyMap())
    val materialCosts: StateFlow<Map<String, MaterialCost>> = _materialCosts.asStateFlow()
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    /**
     * Initialize the cost database with baseline data
     */
    suspend fun initializeDatabase(costDataService: CostDataService) {
        try {
            Log.i(TAG, "Initializing cost database...")
            
            // Check if data already exists in Firebase
            val laborRateCount = laborRatesCollection.get().await().size()
            
            if (laborRateCount == 0) {
                Log.i(TAG, "No existing data found, seeding with baseline data")
                await seedBaselineData(costDataService)
            }
            
            // Load data into local cache
            await loadDataFromFirestore()
            
            _isInitialized.value = true
            Log.i(TAG, "Cost database initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing cost database", e)
            throw e
        }
    }

    /**
     * Seed Firebase with baseline industry data
     */
    private suspend fun seedBaselineData(costDataService: CostDataService) {
        try {
            Log.i(TAG, "Seeding baseline cost data to Firebase...")
            
            // Get baseline data from service
            val laborRates = costDataService.laborRates.value
            val materialCosts = costDataService.materialCosts.value
            val regionalAdjustments = costDataService.regionalAdjustments.value
            val laborTimes = costDataService.standardLaborTimes.value
            
            // Batch write to Firebase
            val batch = db.batch()
            
            // Save labor rates
            laborRates.forEach { (key, rate) ->
                val docRef = laborRatesCollection.document(key)
                batch.set(docRef, mapOf(
                    "trade" to rate.trade,
                    "baseHourlyRate" to rate.baseHourlyRate,
                    "skilledRate" to rate.skilledRate,
                    "journeymanRate" to rate.journeymanRate,
                    "foremanRate" to rate.foremanRate,
                    "source" to rate.source,
                    "region" to rate.region,
                    "effectiveDate" to rate.effectiveDate,
                    "benefits" to rate.benefits,
                    "overhead" to rate.overhead,
                    "notes" to rate.notes,
                    "lastUpdated" to Date(),
                    "isBaseline" to true
                ))
            }
            
            // Save material costs
            materialCosts.forEach { (key, cost) ->
                val docRef = materialCostsCollection.document(key)
                batch.set(docRef, mapOf(
                    "item" to cost.item,
                    "unit" to cost.unit,
                    "cost" to cost.cost,
                    "supplier" to cost.supplier,
                    "lastUpdated" to cost.lastUpdated,
                    "region" to cost.region,
                    "notes" to cost.notes,
                    "isBaseline" to true
                ))
            }
            
            // Save regional adjustments
            regionalAdjustments.forEach { (key, adjustment) ->
                val docRef = regionalAdjustmentsCollection.document(key)
                batch.set(docRef, mapOf(
                    "region" to adjustment.region,
                    "laborMultiplier" to adjustment.laborMultiplier,
                    "materialMultiplier" to adjustment.materialMultiplier,
                    "equipmentMultiplier" to adjustment.equipmentMultiplier,
                    "source" to adjustment.source,
                    "notes" to adjustment.notes,
                    "lastUpdated" to Date(),
                    "isBaseline" to true
                ))
            }
            
            // Save standard labor times
            laborTimes.forEach { (trade, times) ->
                val docRef = laborTimesCollection.document(trade)
                val timeData = times.toMutableMap().apply {
                    put("trade", trade)
                    put("lastUpdated", Date().toString())
                    put("isBaseline", true)
                }
                batch.set(docRef, timeData)
            }
            
            // Commit batch
            batch.commit().await()
            
            Log.i(TAG, "Baseline data seeded successfully")
            Log.i(TAG, "Seeded ${laborRates.size} labor rates")
            Log.i(TAG, "Seeded ${materialCosts.size} material costs")
            Log.i(TAG, "Seeded ${regionalAdjustments.size} regional adjustments")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding baseline data", e)
            throw e
        }
    }

    /**
     * Load data from Firestore into local cache
     */
    private suspend fun loadDataFromFirestore() {
        try {
            Log.i(TAG, "Loading cost data from Firestore...")
            
            // Load labor rates
            val laborRateSnapshot = laborRatesCollection.get().await()
            val laborRateMap = mutableMapOf<String, LaborRate>()
            
            for (document in laborRateSnapshot.documents) {
                val data = document.data ?: continue
                val laborRate = LaborRate(
                    trade = data["trade"] as? String ?: "",
                    baseHourlyRate = (data["baseHourlyRate"] as? Number)?.toDouble() ?: 0.0,
                    skilledRate = (data["skilledRate"] as? Number)?.toDouble() ?: 0.0,
                    journeymanRate = (data["journeymanRate"] as? Number)?.toDouble() ?: 0.0,
                    foremanRate = (data["foremanRate"] as? Number)?.toDouble() ?: 0.0,
                    source = data["source"] as? String ?: "",
                    region = data["region"] as? String ?: "",
                    effectiveDate = (data["effectiveDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                    benefits = (data["benefits"] as? Number)?.toDouble() ?: 0.0,
                    overhead = (data["overhead"] as? Number)?.toDouble() ?: 0.0,
                    notes = data["notes"] as? String ?: ""
                )
                laborRateMap[document.id] = laborRate
            }
            _laborRates.value = laborRateMap
            
            // Load material costs
            val materialCostSnapshot = materialCostsCollection.get().await()
            val materialCostMap = mutableMapOf<String, MaterialCost>()
            
            for (document in materialCostSnapshot.documents) {
                val data = document.data ?: continue
                val materialCost = MaterialCost(
                    item = data["item"] as? String ?: "",
                    unit = data["unit"] as? String ?: "",
                    cost = (data["cost"] as? Number)?.toDouble() ?: 0.0,
                    supplier = data["supplier"] as? String ?: "",
                    lastUpdated = (data["lastUpdated"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                    region = data["region"] as? String ?: "",
                    notes = data["notes"] as? String ?: ""
                )
                materialCostMap[document.id] = materialCost
            }
            _materialCosts.value = materialCostMap
            
            Log.i(TAG, "Loaded ${laborRateMap.size} labor rates from Firestore")
            Log.i(TAG, "Loaded ${materialCostMap.size} material costs from Firestore")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading data from Firestore", e)
            throw e
        }
    }

    /**
     * Save historical labor data for learning and accuracy improvement
     */
    suspend fun saveHistoricalLaborData(data: HistoricalLaborData): String {
        return try {
            val documentData = mapOf(
                "projectId" to data.projectId,
                "taskDescription" to data.taskDescription,
                "trade" to data.trade,
                "estimatedHours" to data.estimatedHours,
                "actualHours" to data.actualHours,
                "completedDate" to data.completedDate,
                "workerId" to data.workerId,
                "qualityRating" to data.qualityRating,
                "notes" to data.notes,
                "weatherConditions" to data.weatherConditions,
                "siteComplexity" to data.siteComplexity.name,
                "createdAt" to Date(),
                "variance" to ((data.actualHours - data.estimatedHours) / data.estimatedHours * 100).coerceIn(-100.0, 1000.0)
            )
            
            val docRef = historicalDataCollection.add(documentData).await()
            Log.i(TAG, "Historical labor data saved: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error saving historical labor data", e)
            throw e
        }
    }

    /**
     * Get historical data for estimate validation
     */
    suspend fun getHistoricalDataForTask(taskDescription: String, trade: String): List<HistoricalLaborData> {
        return try {
            val query = historicalDataCollection
                .whereEqualTo("trade", trade)
                .orderBy("completedDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()
            
            val results = mutableListOf<HistoricalLaborData>()
            for (document in query.documents) {
                val data = document.data ?: continue
                
                // Filter by task description similarity
                val storedTask = data["taskDescription"] as? String ?: ""
                if (storedTask.contains(taskDescription, ignoreCase = true) || 
                    taskDescription.contains(storedTask, ignoreCase = true)) {
                    
                    val historicalData = HistoricalLaborData(
                        id = document.id,
                        projectId = data["projectId"] as? String ?: "",
                        taskDescription = storedTask,
                        trade = data["trade"] as? String ?: "",
                        estimatedHours = (data["estimatedHours"] as? Number)?.toDouble() ?: 0.0,
                        actualHours = (data["actualHours"] as? Number)?.toDouble() ?: 0.0,
                        completedDate = (data["completedDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                        workerId = data["workerId"] as? String ?: "",
                        qualityRating = (data["qualityRating"] as? Number)?.toInt() ?: 3,
                        notes = data["notes"] as? String ?: "",
                        weatherConditions = data["weatherConditions"] as? String ?: "",
                        siteComplexity = TaskDifficulty.valueOf(data["siteComplexity"] as? String ?: "STANDARD")
                    )
                    results.add(historicalData)
                }
            }
            
            Log.i(TAG, "Retrieved ${results.size} historical data points for task: $taskDescription")
            results
        } catch (e: Exception) {
            Log.e(TAG, "Error getting historical data", e)
            emptyList()
        }
    }

    /**
     * Update labor rate
     */
    suspend fun updateLaborRate(tradeKey: String, laborRate: LaborRate): Boolean {
        return try {
            val updateData = mapOf(
                "trade" to laborRate.trade,
                "baseHourlyRate" to laborRate.baseHourlyRate,
                "skilledRate" to laborRate.skilledRate,
                "journeymanRate" to laborRate.journeymanRate,
                "foremanRate" to laborRate.foremanRate,
                "source" to laborRate.source,
                "region" to laborRate.region,
                "effectiveDate" to laborRate.effectiveDate,
                "benefits" to laborRate.benefits,
                "overhead" to laborRate.overhead,
                "notes" to laborRate.notes,
                "lastUpdated" to Date(),
                "isBaseline" to false // User customization
            )
            
            laborRatesCollection.document(tradeKey).update(updateData).await()
            
            // Update local cache
            _laborRates.value = _laborRates.value.toMutableMap().apply {
                put(tradeKey, laborRate)
            }
            
            Log.i(TAG, "Labor rate updated: $tradeKey")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating labor rate", e)
            false
        }
    }

    /**
     * Update material cost
     */
    suspend fun updateMaterialCost(materialKey: String, materialCost: MaterialCost): Boolean {
        return try {
            val updateData = mapOf(
                "item" to materialCost.item,
                "unit" to materialCost.unit,
                "cost" to materialCost.cost,
                "supplier" to materialCost.supplier,
                "lastUpdated" to materialCost.lastUpdated,
                "region" to materialCost.region,
                "notes" to materialCost.notes,
                "isBaseline" to false // User customization
            )
            
            materialCostsCollection.document(materialKey).update(updateData).await()
            
            // Update local cache
            _materialCosts.value = _materialCosts.value.toMutableMap().apply {
                put(materialKey, materialCost)
            }
            
            Log.i(TAG, "Material cost updated: $materialKey")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating material cost", e)
            false
        }
    }

    /**
     * Get labor rates for a specific region
     */
    suspend fun getLaborRatesForRegion(region: String): Map<String, LaborRate> {
        return try {
            val query = laborRatesCollection
                .whereEqualTo("region", region)
                .get()
                .await()
            
            val results = mutableMapOf<String, LaborRate>()
            for (document in query.documents) {
                val data = document.data ?: continue
                val laborRate = LaborRate(
                    trade = data["trade"] as? String ?: "",
                    baseHourlyRate = (data["baseHourlyRate"] as? Number)?.toDouble() ?: 0.0,
                    skilledRate = (data["skilledRate"] as? Number)?.toDouble() ?: 0.0,
                    journeymanRate = (data["journeymanRate"] as? Number)?.toDouble() ?: 0.0,
                    foremanRate = (data["foremanRate"] as? Number)?.toDouble() ?: 0.0,
                    source = data["source"] as? String ?: "",
                    region = data["region"] as? String ?: "",
                    effectiveDate = (data["effectiveDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                    benefits = (data["benefits"] as? Number)?.toDouble() ?: 0.0,
                    overhead = (data["overhead"] as? Number)?.toDouble() ?: 0.0,
                    notes = data["notes"] as? String ?: ""
                )
                results[document.id] = laborRate
            }
            
            Log.i(TAG, "Retrieved ${results.size} labor rates for region: $region")
            results
        } catch (e: Exception) {
            Log.e(TAG, "Error getting labor rates for region", e)
            emptyMap()
        }
    }

    /**
     * Search materials by name or category
     */
    suspend fun searchMaterials(query: String): List<Pair<String, MaterialCost>> {
        return try {
            val allMaterials = materialCostsCollection.get().await()
            val results = mutableListOf<Pair<String, MaterialCost>>()
            
            for (document in allMaterials.documents) {
                val data = document.data ?: continue
                val item = data["item"] as? String ?: ""
                
                if (item.contains(query, ignoreCase = true)) {
                    val materialCost = MaterialCost(
                        item = item,
                        unit = data["unit"] as? String ?: "",
                        cost = (data["cost"] as? Number)?.toDouble() ?: 0.0,
                        supplier = data["supplier"] as? String ?: "",
                        lastUpdated = (data["lastUpdated"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                        region = data["region"] as? String ?: "",
                        notes = data["notes"] as? String ?: ""
                    )
                    results.add(Pair(document.id, materialCost))
                }
            }
            
            Log.i(TAG, "Found ${results.size} materials matching query: $query")
            results
        } catch (e: Exception) {
            Log.e(TAG, "Error searching materials", e)
            emptyList()
        }
    }

    companion object {
        private var instance: CostDatabaseRepository? = null
        
        fun getInstance(): CostDatabaseRepository {
            return instance ?: synchronized(this) {
                instance ?: CostDatabaseRepository().also { instance = it }
            }
        }
    }
}