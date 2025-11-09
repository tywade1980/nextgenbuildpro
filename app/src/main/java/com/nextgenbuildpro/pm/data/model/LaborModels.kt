package com.nextgenbuildpro.pm.data.model

import java.util.Date

/**
 * Data models for labor rates, costs, and time tracking
 * Based on industry standards from RSMeans, BNI, and trade associations
 */

/**
 * Labor rate structure for different trades
 */
data class LaborRate(
    val trade: String,
    val baseHourlyRate: Double, // Entry level
    val skilledRate: Double, // Experienced worker
    val journeymanRate: Double, // Licensed/certified
    val foremanRate: Double, // Supervisory level
    val source: String, // Data source (BLS, union rates, etc.)
    val region: String, // Geographic region
    val effectiveDate: Date, // When rates are effective
    val benefits: Double, // Hourly benefit cost
    val overhead: Double, // Company overhead percentage
    val notes: String = ""
)

/**
 * Material cost tracking
 */
data class MaterialCost(
    val item: String,
    val unit: String, // each, sq ft, linear ft, cubic yard, etc.
    val cost: Double, // Cost per unit
    val supplier: String,
    val lastUpdated: Date,
    val region: String,
    val notes: String = ""
)

/**
 * Regional cost adjustments
 */
data class RegionalAdjustment(
    val region: String,
    val laborMultiplier: Double, // Factor to adjust labor costs
    val materialMultiplier: Double, // Factor to adjust material costs  
    val equipmentMultiplier: Double, // Factor to adjust equipment costs
    val source: String, // RSMeans City Cost Index, etc.
    val notes: String = ""
)

/**
 * Standard task labor times
 */
data class LaborTime(
    val taskName: String,
    val trade: String,
    val timePerUnit: Double, // Hours per unit of work
    val unit: String, // sq ft, linear ft, each, etc.
    val difficulty: TaskDifficulty,
    val source: String,
    val notes: String = ""
)

/**
 * Task difficulty levels affecting labor time
 */
enum class TaskDifficulty(val multiplier: Double) {
    EASY(0.8),
    STANDARD(1.0),
    DIFFICULT(1.3),
    COMPLEX(1.6)
}

/**
 * Complete cost breakdown for an estimate item
 */
data class CostBreakdown(
    val description: String,
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
    val tradeType: String,
    val region: String
)

/**
 * Historical labor data for learning and improvement
 */
data class HistoricalLaborData(
    val id: String,
    val projectId: String,
    val taskDescription: String,
    val trade: String,
    val estimatedHours: Double,
    val actualHours: Double,
    val completedDate: Date,
    val workerId: String,
    val qualityRating: Int, // 1-5 scale
    val notes: String,
    val weatherConditions: String = "",
    val siteComplexity: TaskDifficulty
)

/**
 * Labor productivity tracking
 */
data class LaborProductivity(
    val trade: String,
    val taskType: String,
    val averageHoursPerUnit: Double,
    val standardDeviation: Double,
    val sampleSize: Int,
    val lastUpdated: Date,
    val seasonalFactors: Map<String, Double> = emptyMap(), // Winter, Spring, etc.
    val experienceFactors: Map<String, Double> = emptyMap() // Apprentice, Journeyman, etc.
)

/**
 * Cost escalation tracking
 */
data class CostEscalation(
    val category: String, // Labor, Materials, Equipment
    val region: String,
    val yearOverYearChange: Double, // Percentage change
    val monthOverMonthChange: Double,
    val lastUpdated: Date,
    val source: String,
    val forecast: Map<String, Double> = emptyMap() // Future projections
)

/**
 * Estimate validation and accuracy tracking
 */
data class EstimateAccuracy(
    val estimateId: String,
    val projectId: String,
    val estimatedCost: Double,
    val actualCost: Double,
    val variance: Double, // Percentage difference
    val completionDate: Date,
    val factors: List<String>, // What caused variances
    val category: String // Kitchen, Bathroom, Addition, etc.
)

/**
 * Market rate intelligence
 */
data class MarketRates(
    val region: String,
    val trade: String,
    val lowRate: Double,
    val averageRate: Double,
    val highRate: Double,
    val demandLevel: String, // High, Medium, Low
    val lastUpdated: Date,
    val source: String,
    val trendDirection: String // Increasing, Stable, Decreasing
)

/**
 * Wage and benefit package tracking
 */
data class WageBenefitPackage(
    val trade: String,
    val region: String,
    val baseWage: Double,
    val healthInsurance: Double,
    val retirement: Double,
    val workersComp: Double,
    val unemployment: Double,
    val socialSecurity: Double,
    val medicare: Double,
    val totalHourlyBurden: Double,
    val effectiveDate: Date,
    val source: String
)

/**
 * Equipment and tool costs
 */
data class EquipmentCost(
    val equipmentName: String,
    val type: String, // Rental, Purchase, Lease
    val dailyRate: Double,
    val weeklyRate: Double,
    val monthlyRate: Double,
    val operatorRequired: Boolean,
    val operatorRate: Double,
    val fuelCostPerHour: Double,
    val maintenanceCostPerHour: Double,
    val supplier: String,
    val lastUpdated: Date
)