package com.nextgenbuildpro.pm.service

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.data.repository.CostDatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

/**
 * Enhanced estimate service using real industry labor times and cost data
 * Integrates with legitimate construction cost sources for accurate estimates
 */
class EnhancedEstimateService(
    private val context: Context,
    private val costDataService: CostDataService,
    private val costDatabaseRepository: CostDatabaseRepository
) {
    
    private val TAG = "EnhancedEstimateService"
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    // State management
    private val _estimateInProgress = MutableStateFlow<EstimateProject?>(null)
    val estimateInProgress: StateFlow<EstimateProject?> = _estimateInProgress.asStateFlow()
    
    private val _costBreakdowns = MutableStateFlow<List<CostBreakdown>>(emptyList())
    val costBreakdowns: StateFlow<List<CostBreakdown>> = _costBreakdowns.asStateFlow()
    
    private val _totalEstimate = MutableStateFlow(0.0)
    val totalEstimate: StateFlow<Double> = _totalEstimate.asStateFlow()
    
    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    /**
     * Create a new estimate project
     */
    fun createEstimate(
        projectName: String,
        projectType: String,
        clientName: String,
        address: String,
        region: String = "national_average"
    ): EstimateProject {
        val estimate = EstimateProject(
            id = UUID.randomUUID().toString(),
            name = projectName,
            type = projectType,
            client = clientName,
            address = address,
            region = region,
            createdDate = Date(),
            lastModified = Date(),
            status = "Draft",
            lineItems = emptyList(),
            totalCost = 0.0,
            laborCost = 0.0,
            materialCost = 0.0,
            equipmentCost = 0.0,
            overhead = 0.0,
            profit = 0.0,
            taxRate = 8.5, // Default sales tax
            notes = ""
        )
        
        _estimateInProgress.value = estimate
        Log.i(TAG, "Created new estimate: ${estimate.name}")
        return estimate
    }

    /**
     * Add line item to estimate using industry-standard calculations
     */
    fun addLineItem(
        description: String,
        trade: String,
        quantity: Double,
        unit: String,
        difficulty: TaskDifficulty = TaskDifficulty.STANDARD,
        customLaborTime: Double? = null,
        customMaterialCost: Double? = null
    ) {
        serviceScope.launch {
            try {
                _isCalculating.value = true
                
                val currentEstimate = _estimateInProgress.value ?: return@launch
                
                // Calculate cost breakdown using real industry data
                val costBreakdown = if (customLaborTime != null || customMaterialCost != null) {
                    calculateCustomLineItem(
                        description, trade, quantity, unit, difficulty,
                        customLaborTime, customMaterialCost, currentEstimate.region
                    )
                } else {
                    costDataService.calculateTaskEstimate(
                        description, trade, quantity, unit, 
                        currentEstimate.region, difficulty
                    )
                }
                
                // Get historical data for validation
                val historicalData = costDatabaseRepository.getHistoricalDataForTask(description, trade)
                val validation = costDataService.validateEstimate(costBreakdown, historicalData)
                
                // Add variance warning if significant
                val warningNotes = if (validation != null && kotlin.math.abs(validation.variance) > 20.0) {
                    "Warning: Estimate varies ${validation.variance.roundToInt()}% from historical average"
                } else {
                    ""
                }
                
                // Create estimate line item
                val lineItem = EstimateLineItem(
                    id = UUID.randomUUID().toString(),
                    description = description,
                    trade = trade,
                    quantity = quantity,
                    unit = unit,
                    laborHours = costBreakdown.laborHours,
                    laborRate = costBreakdown.laborRate,
                    laborCost = costBreakdown.laborCost,
                    materialCost = costBreakdown.materialCost,
                    equipmentCost = costBreakdown.equipmentCost,
                    subtotal = costBreakdown.subtotal,
                    overhead = costBreakdown.overhead,
                    profit = costBreakdown.profit,
                    totalCost = costBreakdown.totalCost,
                    costPerUnit = costBreakdown.costPerUnit,
                    difficulty = difficulty,
                    notes = warningNotes,
                    source = "Industry Database",
                    lastUpdated = Date()
                )
                
                // Update estimate
                val updatedLineItems = currentEstimate.lineItems + lineItem
                val updatedEstimate = currentEstimate.copy(
                    lineItems = updatedLineItems,
                    lastModified = Date()
                )
                
                _estimateInProgress.value = updatedEstimate
                
                // Update cost breakdowns for UI display
                val currentBreakdowns = _costBreakdowns.value.toMutableList()
                currentBreakdowns.add(costBreakdown)
                _costBreakdowns.value = currentBreakdowns
                
                // Recalculate totals
                recalculateTotals(updatedEstimate)
                
                Log.i(TAG, "Added line item: $description - $${costBreakdown.totalCost}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error adding line item", e)
            } finally {
                _isCalculating.value = false
            }
        }
    }

    /**
     * Calculate custom line item with user overrides
     */
    private suspend fun calculateCustomLineItem(
        description: String,
        trade: String,
        quantity: Double,
        unit: String,
        difficulty: TaskDifficulty,
        customLaborTime: Double?,
        customMaterialCost: Double?,
        region: String
    ): CostBreakdown {
        // Get base rates
        val laborRates = costDatabaseRepository.laborRates.value
        val materialCosts = costDatabaseRepository.materialCosts.value
        
        val laborRate = laborRates[trade]?.let { rate ->
            val regionalAdjustment = costDataService.regionalAdjustments.value[region]?.laborMultiplier ?: 1.0
            (rate.journeymanRate + rate.benefits) * regionalAdjustment
        } ?: 35.0
        
        // Use custom or calculated values
        val laborTimePerUnit = customLaborTime ?: getStandardLaborTime(trade, description) * difficulty.multiplier
        val totalLaborHours = quantity * laborTimePerUnit
        val laborCost = totalLaborHours * laborRate
        
        val materialCostPerUnit = customMaterialCost ?: estimateMaterialCost(description, materialCosts)
        val totalMaterialCost = quantity * materialCostPerUnit
        
        val equipmentCost = estimateEquipmentCost(description, totalLaborHours)
        val subtotal = laborCost + totalMaterialCost + equipmentCost
        
        // Apply overhead and profit
        val overheadAmount = subtotal * 0.15 // 15% overhead
        val profitAmount = (subtotal + overheadAmount) * 0.10 // 10% profit
        val totalCost = subtotal + overheadAmount + profitAmount
        
        return CostBreakdown(
            description = description,
            quantity = quantity,
            unit = unit,
            laborHours = totalLaborHours,
            laborRate = laborRate,
            laborCost = laborCost,
            materialCost = totalMaterialCost,
            equipmentCost = equipmentCost,
            subtotal = subtotal,
            overhead = overheadAmount,
            profit = profitAmount,
            totalCost = totalCost,
            costPerUnit = totalCost / quantity,
            tradeType = trade,
            region = region
        )
    }

    /**
     * Get standard labor time for a task
     */
    private suspend fun getStandardLaborTime(trade: String, description: String): Double {
        val standardTimes = costDataService.standardLaborTimes.value[trade] ?: return 1.0
        
        val lowerDescription = description.lowercase()
        return when {
            lowerDescription.contains("outlet") || lowerDescription.contains("switch") ->
                standardTimes["rough_in_per_outlet"] ?: 0.75
            lowerDescription.contains("wall") && lowerDescription.contains("fram") ->
                standardTimes["wall_framing_per_lf"] ?: 0.25
            lowerDescription.contains("drywall") ->
                standardTimes["total_per_sf"] ?: 0.026
            lowerDescription.contains("paint") ->
                standardTimes["paint_per_sf"] ?: 0.007
            lowerDescription.contains("hardwood") ->
                standardTimes["hardwood_per_sf"] ?: 0.085
            lowerDescription.contains("tile") ->
                standardTimes["tile_per_sf"] ?: 0.12
            lowerDescription.contains("plumbing") && lowerDescription.contains("fixture") ->
                standardTimes["rough_in_per_fixture"] ?: 4.5
            lowerDescription.contains("roof") && lowerDescription.contains("shingle") ->
                standardTimes["asphalt_shingles_per_sf"] ?: 0.065
            else -> 1.0 // Default
        }
    }

    /**
     * Estimate material cost based on description
     */
    private fun estimateMaterialCost(description: String, materialCosts: Map<String, MaterialCost>): Double {
        val lowerDescription = description.lowercase()
        
        return when {
            lowerDescription.contains("drywall") ->
                materialCosts["drywall_1_2_4x8"]?.cost?.div(32) ?: 0.45
            lowerDescription.contains("lumber") || lowerDescription.contains("fram") ->
                materialCosts["lumber_2x4_8ft"]?.cost ?: 4.85
            lowerDescription.contains("electrical") ->
                materialCosts["electrical_12_2_wire"]?.cost ?: 0.85
            lowerDescription.contains("plywood") ->
                materialCosts["plywood_3_4_4x8"]?.cost?.div(32) ?: 1.81
            lowerDescription.contains("concrete") ->
                materialCosts["concrete_3000_psi"]?.cost?.div(27) ?: 5.0
            lowerDescription.contains("insulation") ->
                materialCosts["insulation_r13_batts"]?.cost ?: 1.25
            else -> 2.50 // Default
        }
    }

    /**
     * Estimate equipment costs
     */
    private fun estimateEquipmentCost(description: String, laborHours: Double): Double {
        val lowerDescription = description.lowercase()
        
        return when {
            lowerDescription.contains("excavat") || lowerDescription.contains("foundation") ->
                (laborHours / 8.0) * 350.0 // Excavator daily rate
            lowerDescription.contains("concrete") && lowerDescription.contains("pour") ->
                165.0 // Concrete mixer per load
            lowerDescription.contains("crane") || lowerDescription.contains("lift") ->
                (laborHours / 8.0) * 1200.0 // Crane daily rate
            else -> 0.0
        }
    }

    /**
     * Add common construction tasks with pre-calculated costs
     */
    fun addCommonTasks(projectType: String, squareFootage: Double) {
        serviceScope.launch {
            when (projectType.lowercase()) {
                "kitchen renovation" -> addKitchenTasks(squareFootage)
                "bathroom remodel" -> addBathroomTasks(squareFootage)
                "basement finish" -> addBasementTasks(squareFootage)
                "addition" -> addAdditionTasks(squareFootage)
                "full house renovation" -> addFullRenovationTasks(squareFootage)
            }
        }
    }

    /**
     * Add typical kitchen renovation tasks
     */
    private suspend fun addKitchenTasks(squareFootage: Double) {
        val typicalTasks = listOf(
            Triple("Demolition - Remove cabinets and counters", "general_laborer", squareFootage * 0.5),
            Triple("Electrical - Add outlets and lighting", "electrician", squareFootage * 0.75),
            Triple("Plumbing - Rough-in for sink and dishwasher", "plumber", 2.0),
            Triple("Drywall - Patch and paint", "drywall_installer", squareFootage * 1.2),
            Triple("Flooring - Install tile or hardwood", "flooring_installer", squareFootage),
            Triple("Cabinet installation", "carpenter_residential", squareFootage * 0.8),
            Triple("Countertop installation", "carpenter_residential", squareFootage * 0.3),
            Triple("Paint walls and trim", "painter", squareFootage * 2.5)
        )
        
        for ((description, trade, quantity) in typicalTasks) {
            addLineItem(description, trade, quantity, "sq ft")
        }
    }

    /**
     * Add typical bathroom remodel tasks
     */
    private suspend fun addBathroomTasks(squareFootage: Double) {
        val typicalTasks = listOf(
            Triple("Demolition - Remove fixtures and tile", "general_laborer", squareFootage * 0.75),
            Triple("Plumbing - Rough-in for fixtures", "plumber", 3.0),
            Triple("Electrical - Add GFCI outlets and fan", "electrician", squareFootage * 0.5),
            Triple("Tile installation - Floor and walls", "flooring_installer", squareFootage * 2.0),
            Triple("Fixture installation - Toilet, vanity, tub", "plumber", 3.0),
            Triple("Paint walls and ceiling", "painter", squareFootage * 3.0)
        )
        
        for ((description, trade, quantity) in typicalTasks) {
            addLineItem(description, trade, quantity, "sq ft")
        }
    }

    /**
     * Add typical basement finishing tasks
     */
    private suspend fun addBasementTasks(squareFootage: Double) {
        val typicalTasks = listOf(
            Triple("Framing - Interior walls", "carpenter_residential", squareFootage * 0.15),
            Triple("Electrical - Outlets and lighting", "electrician", squareFootage * 0.5),
            Triple("Plumbing - Rough-in for bathroom", "plumber", 1.0),
            Triple("Insulation - Walls and ceiling", "general_laborer", squareFootage * 1.5),
            Triple("Drywall - Walls and ceiling", "drywall_installer", squareFootage * 1.8),
            Triple("Flooring - Carpet or LVP", "flooring_installer", squareFootage),
            Triple("Paint - Walls and ceiling", "painter", squareFootage * 2.2)
        )
        
        for ((description, trade, quantity) in typicalTasks) {
            addLineItem(description, trade, quantity, "sq ft")
        }
    }

    /**
     * Add typical addition tasks
     */
    private suspend fun addAdditionTasks(squareFootage: Double) {
        val typicalTasks = listOf(
            Triple("Foundation - Concrete footings and slab", "concrete_finisher", squareFootage * 0.15),
            Triple("Framing - Walls, floor, and roof", "carpenter_residential", squareFootage * 0.8),
            Triple("Roofing - Shingles and flashing", "roofer", squareFootage * 1.2),
            Triple("Electrical - Full rough-in", "electrician", squareFootage * 0.75),
            Triple("Plumbing - Full rough-in", "plumber", squareFootage * 0.5),
            Triple("HVAC - Ductwork and connections", "hvac_technician", squareFootage * 0.3),
            Triple("Insulation - Walls and ceiling", "general_laborer", squareFootage * 1.2),
            Triple("Drywall - Full installation", "drywall_installer", squareFootage * 1.5),
            Triple("Flooring - Throughout", "flooring_installer", squareFootage),
            Triple("Paint - Interior complete", "painter", squareFootage * 2.0)
        )
        
        for ((description, trade, quantity) in typicalTasks) {
            addLineItem(description, trade, quantity, "sq ft")
        }
    }

    /**
     * Add full house renovation tasks
     */
    private suspend fun addFullRenovationTasks(squareFootage: Double) {
        // This would include all major systems and finishes
        addAdditionTasks(squareFootage) // Use addition as base
        
        // Add specific renovation items
        addLineItem("Demolition - Interior walls and finishes", "general_laborer", squareFootage * 0.5, "sq ft")
        addLineItem("Window replacement - Throughout house", "carpenter_residential", 15.0, "each")
        addLineItem("Kitchen renovation complete", "carpenter_residential", 200.0, "sq ft")
        addLineItem("Bathroom renovations (2)", "plumber", 150.0, "sq ft")
    }

    /**
     * Recalculate estimate totals
     */
    private fun recalculateTotals(estimate: EstimateProject) {
        val totalLabor = estimate.lineItems.sumOf { it.laborCost }
        val totalMaterial = estimate.lineItems.sumOf { it.materialCost }
        val totalEquipment = estimate.lineItems.sumOf { it.equipmentCost }
        val totalOverhead = estimate.lineItems.sumOf { it.overhead }
        val totalProfit = estimate.lineItems.sumOf { it.profit }
        val grandTotal = estimate.lineItems.sumOf { it.totalCost }
        
        val updatedEstimate = estimate.copy(
            laborCost = totalLabor,
            materialCost = totalMaterial,
            equipmentCost = totalEquipment,
            overhead = totalOverhead,
            profit = totalProfit,
            totalCost = grandTotal,
            lastModified = Date()
        )
        
        _estimateInProgress.value = updatedEstimate
        _totalEstimate.value = grandTotal
        
        Log.i(TAG, "Recalculated totals - Grand Total: $${grandTotal}")
    }

    /**
     * Save historical actual costs for learning
     */
    suspend fun recordActualCosts(
        estimateLineItemId: String,
        actualLaborHours: Double,
        actualMaterialCost: Double,
        actualTotalCost: Double,
        qualityRating: Int,
        notes: String
    ) {
        try {
            val currentEstimate = _estimateInProgress.value ?: return
            val lineItem = currentEstimate.lineItems.find { it.id == estimateLineItemId } ?: return
            
            val historicalData = HistoricalLaborData(
                id = UUID.randomUUID().toString(),
                projectId = currentEstimate.id,
                taskDescription = lineItem.description,
                trade = lineItem.trade,
                estimatedHours = lineItem.laborHours,
                actualHours = actualLaborHours,
                completedDate = Date(),
                workerId = "current_user", // Would come from auth
                qualityRating = qualityRating,
                notes = notes,
                siteComplexity = lineItem.difficulty
            )
            
            costDatabaseRepository.saveHistoricalLaborData(historicalData)
            Log.i(TAG, "Recorded actual costs for line item: ${lineItem.description}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error recording actual costs", e)
        }
    }

    /**
     * Generate estimate summary report
     */
    fun generateEstimateSummary(): EstimateSummary? {
        val estimate = _estimateInProgress.value ?: return null
        
        return EstimateSummary(
            projectName = estimate.name,
            client = estimate.client,
            totalCost = estimate.totalCost,
            laborCost = estimate.laborCost,
            materialCost = estimate.materialCost,
            equipmentCost = estimate.equipmentCost,
            overhead = estimate.overhead,
            profit = estimate.profit,
            taxAmount = estimate.totalCost * (estimate.taxRate / 100),
            finalTotal = estimate.totalCost * (1 + estimate.taxRate / 100),
            lineItemCount = estimate.lineItems.size,
            estimatedDuration = estimate.lineItems.sumOf { it.laborHours } / 8.0, // Convert to days
            createdDate = estimate.createdDate,
            validUntil = Date(estimate.createdDate.time + (30 * 24 * 60 * 60 * 1000)), // 30 days
            notes = estimate.notes
        )
    }

    companion object {
        fun create(
            context: Context,
            costDataService: CostDataService,
            costDatabaseRepository: CostDatabaseRepository
        ): EnhancedEstimateService {
            return EnhancedEstimateService(context, costDataService, costDatabaseRepository)
        }
    }
}

/**
 * Supporting data classes
 */
data class EstimateProject(
    val id: String,
    val name: String,
    val type: String,
    val client: String,
    val address: String,
    val region: String,
    val createdDate: Date,
    val lastModified: Date,
    val status: String,
    val lineItems: List<EstimateLineItem>,
    val totalCost: Double,
    val laborCost: Double,
    val materialCost: Double,
    val equipmentCost: Double,
    val overhead: Double,
    val profit: Double,
    val taxRate: Double,
    val notes: String
)

data class EstimateLineItem(
    val id: String,
    val description: String,
    val trade: String,
    val quantity: Double,
    val unit: String,
    val laborHours: Double,
    val laborRate: Double,
    val laborCost: Double,
    val materialCost: Double,
    val equipmentCost: Double,
    val subtotal: Double,
    val overhead: Double,
    val profit: Double,
    val totalCost: Double,
    val costPerUnit: Double,
    val difficulty: TaskDifficulty,
    val notes: String,
    val source: String,
    val lastUpdated: Date
)

data class EstimateSummary(
    val projectName: String,
    val client: String,
    val totalCost: Double,
    val laborCost: Double,
    val materialCost: Double,
    val equipmentCost: Double,
    val overhead: Double,
    val profit: Double,
    val taxAmount: Double,
    val finalTotal: Double,
    val lineItemCount: Int,
    val estimatedDuration: Double, // Days
    val createdDate: Date,
    val validUntil: Date,
    val notes: String
)