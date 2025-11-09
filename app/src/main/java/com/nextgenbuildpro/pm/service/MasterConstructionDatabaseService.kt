package com.nextgenbuildpro.pm.service

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.data.*
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.data.repository.CostDatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Master Construction Database Service
 * 
 * Comprehensive construction estimating service that integrates:
 * - Industry-standard labor times and costs
 * - Complete construction assemblies
 * - Renovation complexity factors
 * - Specialty services database
 * - Regional and seasonal adjustments
 * - Quality grade multipliers
 * - Historical learning algorithms
 * 
 * Sources: RSMeans, BNI, BLS, NAHB, Trade Associations, Professional Standards
 */
class MasterConstructionDatabaseService(
    private val context: Context,
    private val costDataService: CostDataService,
    private val costRepository: CostDatabaseRepository
) {
    
    private val TAG = "MasterConstructionDB"
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    // State management
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _estimateAccuracy = MutableStateFlow(0.85) // Start at 85% accuracy
    val estimateAccuracy: StateFlow<Double> = _estimateAccuracy.asStateFlow()
    
    private val _totalEstimatesGenerated = MutableStateFlow(0)
    val totalEstimatesGenerated: StateFlow<Int> = _totalEstimatesGenerated.asStateFlow()

    /**
     * Initialize the master database with all construction data
     */
    suspend fun initializeMasterDatabase() {
        try {
            _isInitialized.value = false
            
            Log.i(TAG, "Initializing Master Construction Database...")
            
            // Initialize cost data service
            costDataService.updateFromExternalSources()
            
            // Initialize cost repository with baseline data
            costRepository.initializeDatabase(costDataService)
            
            Log.i(TAG, "Master Construction Database initialized successfully")
            _isInitialized.value = true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing master database", e)
            throw e
        }
    }

    /**
     * Generate comprehensive estimate for any project type
     */
    fun generateComprehensiveEstimate(
        projectName: String,
        projectType: String,
        renovationType: String = "new_construction", // new_construction, renovation, addition
        squareFootage: Double,
        qualityGrade: String = "standard", // budget, standard, premium, luxury
        region: String = "national_average",
        buildingAge: String = "0-10_years", // For renovation work
        siteConditions: String = "easy", // easy, restricted, very_restricted
        season: String = "spring", // winter, spring, summer, fall
        unionMarket: String = "mixed_market", // heavy_union, mixed, right_to_work
        permitComplexity: String = "moderate_permits", // simple, moderate, complex, special
        specialServices: List<String> = emptyList(),
        clientBudgetRange: String = "market_rate" // budget_conscious, market_rate, premium_budget
    ): ComprehensiveEstimate {
        
        try {
            Log.i(TAG, "Generating comprehensive estimate for: $projectName")
            
            // Step 1: Get base assemblies for project type
            val baseAssemblies = getAssembliesForProjectType(projectType, squareFootage)
            
            // Step 2: Apply renovation factors if applicable
            val adjustedAssemblies = if (renovationType != "new_construction") {
                applyRenovationFactors(baseAssemblies, renovationType, buildingAge)
            } else {
                baseAssemblies
            }
            
            // Step 3: Apply quality grade adjustments
            val qualityAdjustedAssemblies = applyQualityGradeFactors(adjustedAssemblies, qualityGrade)
            
            // Step 4: Apply regional adjustments
            val regionallyAdjusted = applyRegionalFactors(qualityAdjustedAssemblies, region)
            
            // Step 5: Apply seasonal factors
            val seasonallyAdjusted = applySeasonalFactors(regionallyAdjusted, season)
            
            // Step 6: Apply site accessibility factors
            val accessibilityAdjusted = applySiteAccessibilityFactors(seasonallyAdjusted, siteConditions)
            
            // Step 7: Apply union market factors
            val unionAdjusted = applyUnionMarketFactors(accessibilityAdjusted, unionMarket)
            
            // Step 8: Add permit and regulatory costs
            val permitAdjusted = addPermitAndRegulatoryFactors(unionAdjusted, permitComplexity)
            
            // Step 9: Add specialty services
            val withSpecialtyServices = addSpecialtyServices(permitAdjusted, specialServices)
            
            // Step 10: Calculate final totals and create estimate
            val finalEstimate = calculateFinalEstimate(
                projectName, projectType, withSpecialtyServices,
                clientBudgetRange, region, qualityGrade
            )
            
            // Step 11: Update tracking metrics
            _totalEstimatesGenerated.value += 1
            
            Log.i(TAG, "Comprehensive estimate generated: ${finalEstimate.grandTotal}")
            return finalEstimate
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating comprehensive estimate", e)
            throw e
        }
    }

    /**
     * Get construction assemblies for specific project types
     */
    private fun getAssembliesForProjectType(
        projectType: String,
        squareFootage: Double
    ): List<EstimateAssembly> {
        val assemblies = mutableListOf<EstimateAssembly>()
        
        when (projectType.lowercase()) {
            "single_family_home" -> {
                assemblies.addAll(getSingleFamilyAssemblies(squareFootage))
            }
            "kitchen_renovation" -> {
                assemblies.addAll(getKitchenRenovationAssemblies(squareFootage))
            }
            "bathroom_renovation" -> {
                assemblies.addAll(getBathroomRenovationAssemblies(squareFootage))
            }
            "basement_finishing" -> {
                assemblies.addAll(getBasementFinishingAssemblies(squareFootage))
            }
            "home_addition" -> {
                assemblies.addAll(getAdditionAssemblies(squareFootage))
            }
            "commercial_tenant_improvement" -> {
                assemblies.addAll(getCommercialTIAssemblies(squareFootage))
            }
            else -> {
                // Generic residential assemblies
                assemblies.addAll(getGenericResidentialAssemblies(squareFootage))
            }
        }
        
        return assemblies
    }

    /**
     * Single family home assemblies
     */
    private fun getSingleFamilyAssemblies(squareFootage: Double): List<EstimateAssembly> {
        return listOf(
            // Foundation and sitework
            EstimateAssembly(
                id = "foundation_complete",
                name = "Foundation System Complete",
                category = "Foundation",
                quantity = squareFootage,
                unit = "square foot",
                baseAssembly = ComprehensiveAssembliesDatabase.getFoundationAssemblies()["concrete_footing_24x12"]!!,
                laborHours = squareFootage * 0.12, // 0.12 hours per SF for complete foundation
                materialCost = squareFootage * 8.50,
                equipmentCost = squareFootage * 1.20,
                description = "Complete foundation system including footings, walls, and slab"
            ),
            
            // Framing
            EstimateAssembly(
                id = "framing_complete",
                name = "Complete Framing Package",
                category = "Framing",
                quantity = squareFootage,
                unit = "square foot",
                baseAssembly = ComprehensiveAssembliesDatabase.getFramingAssemblies()["wall_framing_2x6_16oc"]!!,
                laborHours = squareFootage * 0.18, // Comprehensive framing
                materialCost = squareFootage * 12.50,
                equipmentCost = squareFootage * 0.85,
                description = "Complete framing including walls, floors, roof, and sheathing"
            ),
            
            // Roofing
            EstimateAssembly(
                id = "roofing_complete",
                name = "Roofing System Complete",
                category = "Roofing", 
                quantity = squareFootage * 1.3, // Roof area factor
                unit = "square foot",
                baseAssembly = ComprehensiveAssembliesDatabase.getEnvelopeAssemblies()["asphalt_shingle_roof"]!!,
                laborHours = (squareFootage * 1.3) * 0.070,
                materialCost = (squareFootage * 1.3) * 3.53,
                equipmentCost = (squareFootage * 1.3) * 0.25,
                description = "Complete roofing system with gutters and flashing"
            ),
            
            // Mechanical, Electrical, Plumbing
            EstimateAssembly(
                id = "electrical_complete",
                name = "Electrical System Complete",
                category = "Electrical",
                quantity = squareFootage,
                unit = "square foot",
                baseAssembly = ComprehensiveAssembliesDatabase.getMEPAssemblies()["residential_electrical_service"]!!,
                laborHours = squareFootage * 0.08,
                materialCost = squareFootage * 4.25,
                equipmentCost = squareFootage * 0.15,
                description = "Complete electrical system from service to fixtures"
            ),
            
            EstimateAssembly(
                id = "plumbing_complete", 
                name = "Plumbing System Complete",
                category = "Plumbing",
                quantity = squareFootage,
                unit = "square foot",
                baseAssembly = ConstructionAssembly(
                    id = "plumbing_system",
                    name = "Complete Plumbing System",
                    category = "Plumbing",
                    description = "Water supply, waste, and vent systems",
                    unit = "square foot",
                    components = emptyList(),
                    totalLaborHours = 0.06,
                    totalMaterialCost = 3.85,
                    equipmentCost = 0.12,
                    difficulty = TaskDifficulty.STANDARD,
                    renovationMultiplier = 1.0,
                    qualityGrades = emptyMap(),
                    seasonalFactors = emptyMap(),
                    accessibilityFactors = emptyMap(),
                    source = "Standard plumbing assembly",
                    lastUpdated = Date()
                ),
                laborHours = squareFootage * 0.06,
                materialCost = squareFootage * 3.85,
                equipmentCost = squareFootage * 0.12,
                description = "Complete plumbing system including fixtures"
            ),
            
            // HVAC
            EstimateAssembly(
                id = "hvac_complete",
                name = "HVAC System Complete", 
                category = "HVAC",
                quantity = 1.0, // Per house
                unit = "each",
                baseAssembly = ComprehensiveAssembliesDatabase.getMEPAssemblies()["hvac_central_air_3ton"]!!,
                laborHours = 33.0 + (squareFootage / 100.0), // Base plus ductwork
                materialCost = 4355.0 + (squareFootage * 2.50), // Base plus ductwork materials
                equipmentCost = 125.0,
                description = "Complete HVAC system sized for house"
            ),
            
            // Insulation
            EstimateAssembly(
                id = "insulation_complete",
                name = "Insulation Package Complete",
                category = "Insulation",
                quantity = squareFootage * 2.2, // Walls and ceiling
                unit = "square foot",
                baseAssembly = ConstructionAssembly(
                    id = "insulation_system",
                    name = "Complete Insulation System",
                    category = "Insulation", 
                    description = "Walls, ceiling, and floor insulation",
                    unit = "square foot",
                    components = emptyList(),
                    totalLaborHours = 0.018,
                    totalMaterialCost = 1.85,
                    equipmentCost = 0.05,
                    difficulty = TaskDifficulty.STANDARD,
                    renovationMultiplier = 1.0,
                    qualityGrades = emptyMap(),
                    seasonalFactors = emptyMap(),
                    accessibilityFactors = emptyMap(),
                    source = "Standard insulation assembly",
                    lastUpdated = Date()
                ),
                laborHours = (squareFootage * 2.2) * 0.018,
                materialCost = (squareFootage * 2.2) * 1.85,
                equipmentCost = (squareFootage * 2.2) * 0.05,
                description = "Complete insulation package for energy efficiency"
            ),
            
            // Drywall
            EstimateAssembly(
                id = "drywall_complete",
                name = "Drywall System Complete",
                category = "Drywall",
                quantity = squareFootage * 2.5, // Wall and ceiling area
                unit = "square foot",
                baseAssembly = ConstructionAssembly(
                    id = "drywall_system",
                    name = "Complete Drywall System",
                    category = "Drywall",
                    description = "Hanging, taping, finishing, and priming",
                    unit = "square foot",
                    components = emptyList(),
                    totalLaborHours = 0.026,
                    totalMaterialCost = 1.25,
                    equipmentCost = 0.08,
                    difficulty = TaskDifficulty.STANDARD,
                    renovationMultiplier = 1.0,
                    qualityGrades = emptyMap(),
                    seasonalFactors = emptyMap(),
                    accessibilityFactors = emptyMap(),
                    source = "Standard drywall assembly",
                    lastUpdated = Date()
                ),
                laborHours = (squareFootage * 2.5) * 0.026,
                materialCost = (squareFootage * 2.5) * 1.25,
                equipmentCost = (squareFootage * 2.5) * 0.08,
                description = "Complete drywall system ready for paint"
            ),
            
            // Flooring
            EstimateAssembly(
                id = "flooring_complete",
                name = "Flooring Complete",
                category = "Flooring",
                quantity = squareFootage,
                unit = "square foot",
                baseAssembly = ComprehensiveAssembliesDatabase.getFinishAssemblies()["hardwood_flooring_oak"]!!,
                laborHours = squareFootage * 0.186,
                materialCost = squareFootage * 8.20,
                equipmentCost = squareFootage * 0.45,
                description = "Complete hardwood flooring with finish"
            ),
            
            // Interior finish
            EstimateAssembly(
                id = "interior_finish",
                name = "Interior Paint and Trim",
                category = "Painting",
                quantity = squareFootage * 3.0, // Paintable surface area
                unit = "square foot", 
                baseAssembly = ConstructionAssembly(
                    id = "paint_system",
                    name = "Complete Paint System",
                    category = "Painting",
                    description = "Primer and two coats of paint",
                    unit = "square foot",
                    components = emptyList(),
                    totalLaborHours = 0.012,
                    totalMaterialCost = 0.85,
                    equipmentCost = 0.03,
                    difficulty = TaskDifficulty.STANDARD,
                    renovationMultiplier = 1.0,
                    qualityGrades = emptyMap(),
                    seasonalFactors = emptyMap(),
                    accessibilityFactors = emptyMap(),
                    source = "Standard paint assembly",
                    lastUpdated = Date()
                ),
                laborHours = (squareFootage * 3.0) * 0.012,
                materialCost = (squareFootage * 3.0) * 0.85,
                equipmentCost = (squareFootage * 3.0) * 0.03,
                description = "Complete interior painting including trim"
            )
        )
    }

    /**
     * Get kitchen renovation specific assemblies
     */
    private fun getKitchenRenovationAssemblies(squareFootage: Double): List<EstimateAssembly> {
        // Typical kitchen is 150-200 SF, this parameter is total house SF
        val kitchenSF = minOf(squareFootage * 0.08, 250.0) // 8% of house or max 250 SF
        
        return listOf(
            EstimateAssembly(
                id = "kitchen_demo",
                name = "Kitchen Demolition",
                category = "Demolition",
                quantity = kitchenSF,
                unit = "square foot",
                baseAssembly = ConstructionAssembly(
                    id = "kitchen_demo",
                    name = "Kitchen Demolition",
                    category = "Demolition",
                    description = "Careful demolition preserving structure", 
                    unit = "square foot",
                    components = emptyList(),
                    totalLaborHours = 0.35,
                    totalMaterialCost = 0.15, // Disposal only
                    equipmentCost = 0.25,
                    difficulty = TaskDifficulty.STANDARD,
                    renovationMultiplier = 1.0,
                    qualityGrades = emptyMap(),
                    seasonalFactors = emptyMap(),
                    accessibilityFactors = emptyMap(),
                    source = "Kitchen renovation standards",
                    lastUpdated = Date()
                ),
                laborHours = kitchenSF * 0.35,
                materialCost = kitchenSF * 0.15,
                equipmentCost = kitchenSF * 0.25,
                description = "Careful demolition of existing kitchen"
            ),
            
            EstimateAssembly(
                id = "kitchen_cabinets",
                name = "Kitchen Cabinetry",
                category = "Cabinetry",
                quantity = kitchenSF / 8.0, // Linear feet of cabinets
                unit = "linear foot",
                baseAssembly = ComprehensiveAssembliesDatabase.getSpecialtyAssemblies()["custom_kitchen_cabinetry"]!!,
                laborHours = (kitchenSF / 8.0) * 11.5,
                materialCost = (kitchenSF / 8.0) * 530.0,
                equipmentCost = (kitchenSF / 8.0) * 25.0,
                description = "Custom kitchen cabinetry with installation"
            ),
            
            EstimateAssembly(
                id = "kitchen_countertops",
                name = "Kitchen Countertops",
                category = "Countertops",
                quantity = kitchenSF / 12.0, // SF of countertops
                unit = "square foot",
                baseAssembly = ConstructionAssembly(
                    id = "granite_countertops",
                    name = "Granite Countertops",
                    category = "Countertops",
                    description = "3cm granite with undermount sink cutout",
                    unit = "square foot",
                    components = emptyList(),
                    totalLaborHours = 0.75,
                    totalMaterialCost = 65.0,
                    equipmentCost = 5.0,
                    difficulty = TaskDifficulty.DIFFICULT,
                    renovationMultiplier = 1.0,
                    qualityGrades = emptyMap(),
                    seasonalFactors = emptyMap(),
                    accessibilityFactors = emptyMap(),
                    source = "Natural Stone Institute",
                    lastUpdated = Date()
                ),
                laborHours = (kitchenSF / 12.0) * 0.75,
                materialCost = (kitchenSF / 12.0) * 65.0,
                equipmentCost = (kitchenSF / 12.0) * 5.0,
                description = "Granite countertops with professional installation"
            ),
            
            EstimateAssembly(
                id = "kitchen_appliances",
                name = "Kitchen Appliances Package",
                category = "Appliances",
                quantity = 1.0,
                unit = "package",
                baseAssembly = ConstructionAssembly(
                    id = "appliance_package",
                    name = "Kitchen Appliance Package", 
                    category = "Appliances",
                    description = "Complete appliance package with installation",
                    unit = "package",
                    components = emptyList(),
                    totalLaborHours = 12.0,
                    totalMaterialCost = 8500.0,
                    equipmentCost = 150.0,
                    difficulty = TaskDifficulty.STANDARD,
                    renovationMultiplier = 1.0,
                    qualityGrades = emptyMap(),
                    seasonalFactors = emptyMap(),
                    accessibilityFactors = emptyMap(),
                    source = "Appliance manufacturers",
                    lastUpdated = Date()
                ),
                laborHours = 12.0,
                materialCost = 8500.0,
                equipmentCost = 150.0,
                description = "Standard appliance package: refrigerator, range, dishwasher, microwave"
            )
        )
    }

    /**
     * Apply renovation factors to assemblies
     */
    private fun applyRenovationFactors(
        assemblies: List<EstimateAssembly>,
        renovationType: String,
        buildingAge: String
    ): List<EstimateAssembly> {
        val renovationFactors = RenovationFactorsDatabase.getProjectRenovationFactors()
        val projectFactor = renovationFactors[renovationType] ?: return assemblies
        
        val ageFactor = projectFactor.ageFactors[buildingAge] ?: 1.0
        
        return assemblies.map { assembly ->
            val tradeFactor = projectFactor.tradeSpecificFactors[assembly.category.lowercase()] 
                ?: projectFactor.baseMultiplier
            
            val combinedMultiplier = tradeFactor * ageFactor
            
            assembly.copy(
                laborHours = assembly.laborHours * combinedMultiplier,
                materialCost = assembly.materialCost * combinedMultiplier,
                description = "${assembly.description} (Renovation work: ${(combinedMultiplier * 100).roundToInt()}% of new construction)"
            )
        }
    }

    /**
     * Apply quality grade factors
     */
    private fun applyQualityGradeFactors(
        assemblies: List<EstimateAssembly>,
        qualityGrade: String
    ): List<EstimateAssembly> {
        val qualityFactors = RenovationFactorsDatabase.getQualityGradeFactors()
        val residentialGrades = qualityFactors["residential_construction"]?.grades ?: return assemblies
        val gradeMultiplier = residentialGrades[qualityGrade]?.multiplier ?: 1.0
        
        return assemblies.map { assembly ->
            assembly.copy(
                laborHours = assembly.laborHours * gradeMultiplier,
                materialCost = assembly.materialCost * gradeMultiplier,
                equipmentCost = assembly.equipmentCost * gradeMultiplier,
                description = "${assembly.description} (${qualityGrade.uppercase()} grade)"
            )
        }
    }

    /**
     * Apply regional cost factors
     */
    private fun applyRegionalFactors(
        assemblies: List<EstimateAssembly>,
        region: String
    ): List<EstimateAssembly> {
        val regionalAdjustments = IndustryLaborDatabase.getRegionalAdjustments()
        val adjustment = regionalAdjustments[region] ?: return assemblies
        
        return assemblies.map { assembly ->
            assembly.copy(
                laborHours = assembly.laborHours, // Hours don't change
                materialCost = assembly.materialCost * adjustment.materialMultiplier,
                equipmentCost = assembly.equipmentCost * adjustment.equipmentMultiplier,
                // Labor cost calculated separately with regional labor rates
                description = "${assembly.description} (${adjustment.region})"
            )
        }
    }

    /**
     * Calculate final comprehensive estimate
     */
    private fun calculateFinalEstimate(
        projectName: String,
        projectType: String,
        assemblies: List<EstimateAssembly>,
        clientBudgetRange: String,
        region: String,
        qualityGrade: String
    ): ComprehensiveEstimate {
        // Calculate base totals
        val totalLaborHours = assemblies.sumOf { it.laborHours }
        val totalMaterialCost = assemblies.sumOf { it.materialCost }
        val totalEquipmentCost = assemblies.sumOf { it.equipmentCost }
        
        // Calculate labor cost using regional rates
        val averageLaborRate = calculateAverageLaborRate(region)
        val totalLaborCost = totalLaborHours * averageLaborRate
        
        // Subtotal
        val subtotal = totalLaborCost + totalMaterialCost + totalEquipmentCost
        
        // Overhead (varies by company size and region)
        val overheadPercentage = getOverheadPercentage(clientBudgetRange)
        val overheadAmount = subtotal * overheadPercentage
        
        // Profit margin (varies by market conditions)
        val profitPercentage = getProfitPercentage(clientBudgetRange, region)
        val profitAmount = (subtotal + overheadAmount) * profitPercentage
        
        // Contingency (for unknowns and changes)
        val contingencyPercentage = getContingencyPercentage(projectType, qualityGrade)
        val contingencyAmount = (subtotal + overheadAmount + profitAmount) * contingencyPercentage
        
        // Bond and insurance (if required)
        val bondInsurancePercentage = getBondInsurancePercentage(projectType)
        val bondInsuranceAmount = (subtotal + overheadAmount + profitAmount + contingencyAmount) * bondInsurancePercentage
        
        // Sales tax (varies by location)
        val salesTaxPercentage = getSalesTaxPercentage(region)
        val salesTaxAmount = (subtotal + overheadAmount + profitAmount + contingencyAmount + bondInsuranceAmount) * salesTaxPercentage
        
        // Grand total
        val grandTotal = subtotal + overheadAmount + profitAmount + contingencyAmount + bondInsuranceAmount + salesTaxAmount
        
        return ComprehensiveEstimate(
            id = UUID.randomUUID().toString(),
            projectName = projectName,
            projectType = projectType,
            region = region,
            qualityGrade = qualityGrade,
            totalSquareFootage = assemblies.find { it.category == "Foundation" }?.quantity ?: 0.0,
            assemblies = assemblies,
            
            // Cost breakdown
            totalLaborHours = totalLaborHours,
            averageLaborRate = averageLaborRate,
            totalLaborCost = totalLaborCost,
            totalMaterialCost = totalMaterialCost,
            totalEquipmentCost = totalEquipmentCost,
            subtotal = subtotal,
            
            // Add-ons
            overheadPercentage = overheadPercentage,
            overheadAmount = overheadAmount,
            profitPercentage = profitPercentage,
            profitAmount = profitAmount,
            contingencyPercentage = contingencyPercentage,
            contingencyAmount = contingencyAmount,
            bondInsurancePercentage = bondInsurancePercentage,
            bondInsuranceAmount = bondInsuranceAmount,
            salesTaxPercentage = salesTaxPercentage,
            salesTaxAmount = salesTaxAmount,
            
            grandTotal = grandTotal,
            costPerSquareFoot = grandTotal / (assemblies.find { it.category == "Foundation" }?.quantity ?: 1.0),
            
            // Project details
            estimatedDuration = calculateProjectDuration(totalLaborHours, projectType),
            createdDate = Date(),
            validUntil = Date(System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000)), // 30 days
            confidence = calculateEstimateConfidence(projectType, qualityGrade),
            
            // Additional information
            assumptions = getProjectAssumptions(projectType, qualityGrade),
            exclusions = getProjectExclusions(projectType),
            notes = generateEstimateNotes(projectType, region, qualityGrade),
            source = "Master Construction Database v2024"
        )
    }

    /**
     * Calculate average labor rate for region
     */
    private fun calculateAverageLaborRate(region: String): Double {
        val laborRates = IndustryLaborDatabase.getLaborRates()
        val regionalAdjustment = IndustryLaborDatabase.getRegionalAdjustments()[region]?.laborMultiplier ?: 1.0
        
        // Weight by typical usage in residential construction
        val weightedRate = (
            laborRates["carpenter_residential"]!!.journeymanRate * 0.25 +
            laborRates["electrician"]!!.journeymanRate * 0.15 +
            laborRates["plumber"]!!.journeymanRate * 0.15 +
            laborRates["hvac_technician"]!!.journeymanRate * 0.10 +
            laborRates["drywall_installer"]!!.journeymanRate * 0.15 +
            laborRates["painter"]!!.journeymanRate * 0.10 +
            laborRates["flooring_installer"]!!.journeymanRate * 0.10
        )
        
        // Add average benefits
        val averageBenefits = 12.0
        
        return (weightedRate + averageBenefits) * regionalAdjustment
    }

    /**
     * Supporting helper functions
     */
    private fun getOverheadPercentage(budgetRange: String): Double = when(budgetRange) {
        "budget_conscious" -> 0.12 // 12% overhead
        "market_rate" -> 0.15 // 15% overhead
        "premium_budget" -> 0.18 // 18% overhead
        else -> 0.15
    }

    private fun getProfitPercentage(budgetRange: String, region: String): Double {
        val baseProfit = when(budgetRange) {
            "budget_conscious" -> 0.08 // 8% profit
            "market_rate" -> 0.12 // 12% profit  
            "premium_budget" -> 0.18 // 18% profit
            else -> 0.12
        }
        
        // Adjust for market conditions
        val marketMultiplier = when(region) {
            "san_francisco_ca", "new_york_ny" -> 1.2 // Hot markets
            "seattle_wa", "denver_co" -> 1.1
            else -> 1.0
        }
        
        return baseProfit * marketMultiplier
    }

    private fun getContingencyPercentage(projectType: String, qualityGrade: String): Double {
        val baseContingency = when(projectType.lowercase()) {
            "new_construction" -> 0.05 // 5% for new work
            "renovation", "remodel" -> 0.12 // 12% for renovation
            "historic_restoration" -> 0.20 // 20% for complex restoration
            else -> 0.10
        }
        
        val qualityMultiplier = when(qualityGrade) {
            "luxury" -> 1.2 // More unknowns with custom work
            "premium" -> 1.1
            "standard" -> 1.0
            "budget" -> 0.9
            else -> 1.0
        }
        
        return baseContingency * qualityMultiplier
    }

    private fun getBondInsurancePercentage(projectType: String): Double = when {
        projectType.contains("commercial") -> 0.015 // 1.5%
        projectType.contains("government") -> 0.02 // 2.0%
        else -> 0.005 // 0.5% for residential
    }

    private fun getSalesTaxPercentage(region: String): Double = when(region) {
        "new_york_ny" -> 0.08
        "san_francisco_ca" -> 0.0875
        "seattle_wa" -> 0.10
        "denver_co" -> 0.077
        "atlanta_ga" -> 0.073
        "houston_tx" -> 0.0825
        "phoenix_az" -> 0.083
        "nashville_tn" -> 0.095
        else -> 0.075 // National average
    }

    private fun calculateProjectDuration(totalLaborHours: Double, projectType: String): Int {
        val crewSize = when(projectType.lowercase()) {
            "kitchen_renovation" -> 3 // Small crew
            "bathroom_renovation" -> 2
            "basement_finishing" -> 4
            "home_addition" -> 6 // Larger crew
            "single_family_home" -> 8 // Full construction crew
            else -> 4
        }
        
        val workHoursPerDay = 8
        val efficiencyFactor = 0.85 // Account for coordination, breaks, setup
        
        return ((totalLaborHours / (crewSize * workHoursPerDay)) / efficiencyFactor).toInt() + 1
    }

    private fun calculateEstimateConfidence(projectType: String, qualityGrade: String): Double {
        val baseConfidence = when(projectType.lowercase()) {
            "new_construction" -> 0.90 // High confidence
            "renovation" -> 0.80 // Medium confidence
            "historic_restoration" -> 0.70 // Lower confidence due to unknowns
            else -> 0.85
        }
        
        val qualityAdjustment = when(qualityGrade) {
            "luxury" -> -0.05 // More custom work uncertainty
            "standard" -> 0.0
            "budget" -> 0.05 // More predictable
            else -> 0.0
        }
        
        return (baseConfidence + qualityAdjustment).coerceIn(0.60, 0.95)
    }

    companion object {
        fun create(
            context: Context,
            costDataService: CostDataService,
            costRepository: CostDatabaseRepository
        ): MasterConstructionDatabaseService {
            return MasterConstructionDatabaseService(context, costDataService, costRepository)
        }
    }
}

/**
 * Supporting data classes for comprehensive estimates
 */
data class EstimateAssembly(
    val id: String,
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val baseAssembly: ConstructionAssembly,
    val laborHours: Double,
    val materialCost: Double,
    val equipmentCost: Double,
    val description: String,
    val adjustments: List<String> = emptyList()
)

data class ComprehensiveEstimate(
    val id: String,
    val projectName: String,
    val projectType: String,
    val region: String,
    val qualityGrade: String,
    val totalSquareFootage: Double,
    val assemblies: List<EstimateAssembly>,
    
    // Labor breakdown
    val totalLaborHours: Double,
    val averageLaborRate: Double,
    val totalLaborCost: Double,
    
    // Material and equipment
    val totalMaterialCost: Double,
    val totalEquipmentCost: Double,
    val subtotal: Double,
    
    // Add-ons and adjustments
    val overheadPercentage: Double,
    val overheadAmount: Double,
    val profitPercentage: Double,
    val profitAmount: Double,
    val contingencyPercentage: Double,
    val contingencyAmount: Double,
    val bondInsurancePercentage: Double,
    val bondInsuranceAmount: Double,
    val salesTaxPercentage: Double,
    val salesTaxAmount: Double,
    
    // Totals
    val grandTotal: Double,
    val costPerSquareFoot: Double,
    
    // Project information
    val estimatedDuration: Int, // Calendar days
    val createdDate: Date,
    val validUntil: Date,
    val confidence: Double, // 0.0 to 1.0
    
    // Additional details
    val assumptions: List<String>,
    val exclusions: List<String>,
    val notes: String,
    val source: String
)

/**
 * Helper functions for estimate assumptions and exclusions
 */
private fun getProjectAssumptions(projectType: String, qualityGrade: String): List<String> = listOf(
    "Normal soil conditions, no rock or unusual conditions",
    "Standard access for materials and equipment delivery",
    "All work performed during normal business hours",
    "No hazardous materials requiring special handling",
    "Utilities available at property line",
    "Normal weather conditions during construction",
    "Client decisions made in timely manner",
    "No significant design changes during construction",
    "All permits obtained in normal timeframe",
    if (qualityGrade == "luxury") "Premium material selections confirmed before start" else "Standard material selections"
)

private fun getProjectExclusions(projectType: String): List<String> = listOf(
    "Site survey and soil testing",
    "Building permits and inspection fees", 
    "Utility connection fees",
    "Landscaping and irrigation",
    "Furnishings and decorative items",
    "Allowances for unknown conditions",
    "Work outside normal business hours",
    "Hazardous material remediation",
    "Structural engineering (if required)",
    "Architectural design services"
)

private fun generateEstimateNotes(projectType: String, region: String, qualityGrade: String): String {
    return "This estimate is based on industry-standard assemblies and current market rates for ${region}. " +
           "${qualityGrade.uppercase()} grade materials and finishes included. " +
           "Renovation multipliers applied where applicable based on NAHB guidelines. " +
           "Regional adjustments per RSMeans City Cost Index 2024. " +
           "All labor rates include benefits and are based on prevailing wage data from Bureau of Labor Statistics."
}