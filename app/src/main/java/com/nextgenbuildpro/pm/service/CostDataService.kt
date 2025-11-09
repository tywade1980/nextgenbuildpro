package com.nextgenbuildpro.pm.service

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.data.IndustryLaborDatabase
import com.nextgenbuildpro.pm.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

/**
 * Service for managing construction cost data from legitimate industry sources
 * Integrates with RSMeans, BNI, Bureau of Labor Statistics, and other authoritative sources
 */
class CostDataService(private val context: Context) {
    
    private val TAG = "CostDataService"
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    // State flows for reactive data
    private val _laborRates = MutableStateFlow<Map<String, LaborRate>>(emptyMap())
    val laborRates: StateFlow<Map<String, LaborRate>> = _laborRates.asStateFlow()
    
    private val _materialCosts = MutableStateFlow<Map<String, MaterialCost>>(emptyMap())
    val materialCosts: StateFlow<Map<String, MaterialCost>> = _materialCosts.asStateFlow()
    
    private val _regionalAdjustments = MutableStateFlow<Map<String, RegionalAdjustment>>(emptyMap())
    val regionalAdjustments: StateFlow<Map<String, RegionalAdjustment>> = _regionalAdjustments.asStateFlow()
    
    private val _standardLaborTimes = MutableStateFlow<Map<String, Map<String, Double>>>(emptyMap())
    val standardLaborTimes: StateFlow<Map<String, Map<String, Double>>> = _standardLaborTimes.asStateFlow()
    
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    init {
        loadBaselineData()
    }

    /**
     * Load baseline data from industry-standard sources
     */
    private fun loadBaselineData() {
        serviceScope.launch {
            try {
                _isUpdating.value = true
                
                // Load data from IndustryLaborDatabase
                _laborRates.value = IndustryLaborDatabase.getLaborRates()
                _materialCosts.value = IndustryLaborDatabase.getMaterialCosts()
                _regionalAdjustments.value = IndustryLaborDatabase.getRegionalAdjustments()
                _standardLaborTimes.value = IndustryLaborDatabase.getStandardLaborTimes()
                
                Log.i(TAG, "Baseline cost data loaded successfully")
                Log.i(TAG, "Loaded ${_laborRates.value.size} labor rates")
                Log.i(TAG, "Loaded ${_materialCosts.value.size} material costs")
                Log.i(TAG, "Loaded ${_regionalAdjustments.value.size} regional adjustments")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading baseline data", e)
            } finally {
                _isUpdating.value = false
            }
        }
    }

    /**
     * Calculate estimate for a construction task
     */
    fun calculateTaskEstimate(
        taskDescription: String,
        trade: String,
        quantity: Double,
        unit: String,
        region: String = "national_average",
        difficulty: TaskDifficulty = TaskDifficulty.STANDARD,
        includeOverhead: Boolean = true,
        includeProfitMargin: Boolean = true
    ): CostBreakdown {
        
        // Get labor rate for trade
        val laborRate = _laborRates.value[trade]?.journeymanRate ?: 35.0
        val benefits = _laborRates.value[trade]?.benefits ?: 10.0
        val totalLaborRate = laborRate + benefits
        
        // Get regional adjustment
        val regionalMultiplier = _regionalAdjustments.value[region]?.laborMultiplier ?: 1.0
        val adjustedLaborRate = totalLaborRate * regionalMultiplier
        
        // Get standard labor time
        val laborTimePerUnit = getTaskLaborTime(trade, taskDescription) * difficulty.multiplier
        val totalLaborHours = quantity * laborTimePerUnit
        val laborCost = totalLaborHours * adjustedLaborRate
        
        // Estimate material costs (simplified - in practice would be more detailed)
        val materialCostPerUnit = estimateMaterialCost(taskDescription, trade)
        val totalMaterialCost = quantity * materialCostPerUnit
        
        // Equipment costs (if applicable)
        val equipmentCost = estimateEquipmentCost(taskDescription, totalLaborHours)
        
        // Subtotal
        val subtotal = laborCost + totalMaterialCost + equipmentCost
        
        // Overhead (typically 10-20%)
        val overheadPercentage = if (includeOverhead) 15.0 else 0.0
        val overheadAmount = subtotal * (overheadPercentage / 100)
        
        // Profit margin (typically 8-15%)
        val profitPercentage = if (includeProfitMargin) 10.0 else 0.0
        val profitAmount = (subtotal + overheadAmount) * (profitPercentage / 100)
        
        // Total cost
        val totalCost = subtotal + overheadAmount + profitAmount
        val costPerUnit = totalCost / quantity
        
        return CostBreakdown(
            description = taskDescription,
            quantity = quantity,
            unit = unit,
            laborHours = totalLaborHours,
            laborRate = adjustedLaborRate,
            laborCost = laborCost,
            materialCost = totalMaterialCost,
            equipmentCost = equipmentCost,
            subtotal = subtotal,
            overhead = overheadAmount,
            profit = profitAmount,
            totalCost = totalCost,
            costPerUnit = costPerUnit,
            tradeType = trade,
            region = region
        )
    }

    /**
     * Get labor time for a specific task
     */
    private fun getTaskLaborTime(trade: String, taskDescription: String): Double {
        val tradeTimes = _standardLaborTimes.value[trade] ?: return 1.0 // Default 1 hour
        
        // Match task description to standard times
        val lowerTask = taskDescription.lowercase()
        
        return when {
            lowerTask.contains("outlet") || lowerTask.contains("switch") -> 
                tradeTimes["rough_in_per_outlet"] ?: 0.75
            lowerTask.contains("wall") && lowerTask.contains("fram") -> 
                tradeTimes["wall_framing_per_lf"] ?: 0.25
            lowerTask.contains("drywall") -> 
                tradeTimes["total_per_sf"] ?: 0.026
            lowerTask.contains("paint") -> 
                tradeTimes["paint_per_sf"] ?: 0.007
            lowerTask.contains("floor") && lowerTask.contains("hardwood") -> 
                tradeTimes["hardwood_per_sf"] ?: 0.085
            lowerTask.contains("tile") -> 
                tradeTimes["tile_per_sf"] ?: 0.12
            lowerTask.contains("plumbing") && lowerTask.contains("fixture") -> 
                tradeTimes["rough_in_per_fixture"] ?: 4.5
            lowerTask.contains("roof") && lowerTask.contains("shingle") -> 
                tradeTimes["asphalt_shingles_per_sf"] ?: 0.065
            else -> 1.0 // Default fallback
        }
    }

    /**
     * Estimate material costs for a task
     */
    private fun estimateMaterialCost(taskDescription: String, trade: String): Double {
        val lowerTask = taskDescription.lowercase()
        val materials = _materialCosts.value
        
        return when {
            lowerTask.contains("drywall") -> 
                materials["drywall_1_2_4x8"]?.cost?.div(32) ?: 0.45 // Per sq ft
            lowerTask.contains("lumber") || lowerTask.contains("fram") -> 
                materials["lumber_2x4_8ft"]?.cost ?: 4.85
            lowerTask.contains("electrical") -> 
                materials["electrical_12_2_wire"]?.cost ?: 0.85
            lowerTask.contains("plywood") -> 
                materials["plywood_3_4_4x8"]?.cost?.div(32) ?: 1.81 // Per sq ft
            lowerTask.contains("concrete") -> 
                materials["concrete_3000_psi"]?.cost?.div(27) ?: 5.0 // Per cubic foot
            lowerTask.contains("insulation") -> 
                materials["insulation_r13_batts"]?.cost ?: 1.25
            else -> 2.50 // Default material cost per unit
        }
    }

    /**
     * Estimate equipment costs
     */
    private fun estimateEquipmentCost(taskDescription: String, laborHours: Double): Double {
        val lowerTask = taskDescription.lowercase()
        val equipment = IndustryLaborDatabase.getEquipmentCosts()
        
        return when {
            lowerTask.contains("excavat") || lowerTask.contains("foundation") -> {
                val dailyRate = equipment["excavator_small"]?.cost ?: 350.0
                (laborHours / 8.0) * dailyRate // Convert hours to days
            }
            lowerTask.contains("concrete") && lowerTask.contains("pour") -> {
                equipment["concrete_mixer"]?.cost ?: 165.0
            }
            lowerTask.contains("crane") || lowerTask.contains("lift") -> {
                val dailyRate = equipment["crane_small"]?.cost ?: 1200.0
                (laborHours / 8.0) * dailyRate
            }
            else -> 0.0 // No equipment needed
        }
    }

    /**
     * Update costs from external data sources
     * In a production app, this would connect to APIs from RSMeans, BLS, etc.
     */
    fun updateFromExternalSources() {
        serviceScope.launch {
            try {
                _isUpdating.value = true
                
                // Simulate API calls to external data sources
                Log.i(TAG, "Updating from external sources...")
                
                // In production, these would be real API calls:
                // - RSMeans API for detailed cost data
                // - Bureau of Labor Statistics API for wage data
                // - Material supplier APIs for current pricing
                // - Regional construction boards for local rates
                
                kotlinx.coroutines.delay(2000) // Simulate network delay
                
                // For now, just refresh baseline data
                loadBaselineData()
                
                Log.i(TAG, "External data update completed")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error updating from external sources", e)
            } finally {
                _isUpdating.value = false
            }
        }
    }

    /**
     * Get market rates for a specific trade and region
     */
    fun getMarketRates(trade: String, region: String): MarketRates? {
        val baseLaborRate = _laborRates.value[trade] ?: return null
        val regionalAdjustment = _regionalAdjustments.value[region]?.laborMultiplier ?: 1.0
        
        val adjustedRate = baseLaborRate.journeymanRate * regionalAdjustment
        
        return MarketRates(
            region = region,
            trade = trade,
            lowRate = adjustedRate * 0.8,
            averageRate = adjustedRate,
            highRate = adjustedRate * 1.3,
            demandLevel = "Medium", // Would come from market data
            lastUpdated = Date(),
            source = baseLaborRate.source,
            trendDirection = "Stable"
        )
    }

    /**
     * Validate estimate accuracy against historical data
     */
    fun validateEstimate(estimate: CostBreakdown, historicalData: List<HistoricalLaborData>): EstimateAccuracy? {
        val similarTasks = historicalData.filter { 
            it.taskDescription.contains(estimate.description, ignoreCase = true) ||
            it.trade == estimate.tradeType
        }
        
        if (similarTasks.isEmpty()) return null
        
        val averageActualHours = similarTasks.map { it.actualHours }.average()
        val variance = ((estimate.laborHours - averageActualHours) / averageActualHours) * 100
        
        return EstimateAccuracy(
            estimateId = UUID.randomUUID().toString(),
            projectId = "", // Would be provided
            estimatedCost = estimate.totalCost,
            actualCost = 0.0, // Would be filled in after completion
            variance = variance,
            completionDate = Date(),
            factors = listOf("Historical comparison"),
            category = estimate.tradeType
        )
    }

    /**
     * Get cost escalation trends
     */
    fun getCostEscalation(category: String, region: String): CostEscalation {
        // In production, this would use real market data
        return CostEscalation(
            category = category,
            region = region,
            yearOverYearChange = when(category.lowercase()) {
                "labor" -> 4.2 // Labor costs increasing ~4% annually
                "materials" -> 6.8 // Materials more volatile
                "equipment" -> 3.1 // Equipment costs more stable
                else -> 4.0
            },
            monthOverMonthChange = 0.3,
            lastUpdated = Date(),
            source = "Industry trend analysis",
            forecast = mapOf(
                "3_months" to 1.2,
                "6_months" to 2.1,
                "12_months" to 4.0
            )
        )
    }

    /**
     * Compare estimated vs actual costs for learning
     */
    fun recordActualCosts(
        estimateId: String,
        actualLaborHours: Double,
        actualMaterialCost: Double,
        actualTotalCost: Double,
        qualityRating: Int,
        notes: String
    ): HistoricalLaborData {
        return HistoricalLaborData(
            id = UUID.randomUUID().toString(),
            projectId = "", // Would be provided
            taskDescription = "", // Would be provided
            trade = "", // Would be provided
            estimatedHours = 0.0, // Would come from original estimate
            actualHours = actualLaborHours,
            completedDate = Date(),
            workerId = "", // Would be provided
            qualityRating = qualityRating,
            notes = notes,
            siteComplexity = TaskDifficulty.STANDARD
        )
    }

    companion object {
        /**
         * Create instance of CostDataService
         */
        fun create(context: Context): CostDataService {
            return CostDataService(context)
        }
    }
}