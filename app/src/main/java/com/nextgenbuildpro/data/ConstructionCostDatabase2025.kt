package com.nextgenbuildpro.data

import com.nextgenbuildpro.shared.*
import android.util.Log
import java.time.LocalDateTime

/**
 * 2025 Construction Cost Database
 * 
 * Pre-loaded with current construction costs, labor rates, and material pricing
 * for accurate project estimation and completion. This is the single source of
 * truth for all construction cost data in the system.
 */
object ConstructionCostDatabase2025 {
    
    private const val TAG = "ConstructionCostDatabase2025"
    
    /**
     * Get complete cost database for a specific region
     */
    fun getCostDatabase(region: String = "national_average"): CostDatabase {
        val multiplier = regionalMultipliers[region] ?: 1.0
        
        return CostDatabase(
            residentialTemplates = adjustResidentialTemplates(multiplier),
            laborRates = adjustLaborRates(region, multiplier),
            materialCosts = adjustMaterialCosts(multiplier),
            equipmentRates = adjustEquipmentRates(multiplier),
            regionalMultipliers = regionalMultipliers
        )
    }
    
    /**
     * Calculate project cost estimate using 2025 database
     */
    fun calculateProjectCost(
        projectType: String,
        squareFootage: Double,
        region: String = "national_average",
        qualityLevel: String = "standard"
    ): DetailedProjectCost {
        
        Log.d(TAG, "Calculating project cost: $projectType, ${squareFootage}sq ft, $region, $qualityLevel")
        
        val costDatabase = getCostDatabase(region)
        val qualityMultiplier = when (qualityLevel.lowercase()) {
            "economy" -> 0.85
            "standard" -> 1.0
            "high-end" -> 1.25
            "luxury" -> 1.50
            else -> 1.0
        }
        
        val costs = mutableMapOf<String, Double>()
        var totalCost = 0.0
        
        // Calculate costs for each phase using 2025 pricing
        residentialTemplates.forEach { (key, template) ->
            val phaseCost = (squareFootage * template.costPerSqFt.average * qualityMultiplier * (regionalMultipliers[region] ?: 1.0))
            costs[template.category] = phaseCost
            totalCost += phaseCost
        }
        
        // Add contingency, overhead, and profit
        val contingency = totalCost * 0.10 // 10% contingency
        val overhead = totalCost * 0.08   // 8% overhead
        val profit = totalCost * 0.15     // 15% profit
        
        val finalCost = totalCost + contingency + overhead + profit
        
        return DetailedProjectCost(
            projectType = projectType,
            squareFootage = squareFootage,
            region = region,
            qualityLevel = qualityLevel,
            phaseCosts = costs,
            subtotal = totalCost,
            contingency = contingency,
            overhead = overhead,
            profit = profit,
            totalCost = finalCost,
            costPerSqFt = finalCost / squareFootage,
            generatedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Residential New Construction Templates with 2025 Pricing
     */
    private val residentialTemplates = mapOf(
        "site_preparation" to ResidentialTemplate(
            category = "Site Preparation",
            description = "Site clearing, grading, excavation (varies by terrain)",
            costPerSqFt = CostRange(2.50, 4.00, 3.25),
            laborHours = 8.0,
            materials = listOf("Excavation", "Grading", "Erosion Control", "Temporary Utilities")
        ),
        "foundation" to ResidentialTemplate(
            category = "Foundation",
            description = "Concrete foundation work (slab to full basement)",
            costPerSqFt = CostRange(8.00, 12.00, 10.00),
            laborHours = 16.0,
            materials = listOf("Concrete", "Rebar", "Forms", "Waterproofing", "Insulation")
        ),
        "framing" to ResidentialTemplate(
            category = "Framing",
            description = "Structural framing (stick frame to engineered lumber)",
            costPerSqFt = CostRange(12.00, 18.00, 15.00),
            laborHours = 24.0,
            materials = listOf("Lumber", "Hardware", "Sheathing", "House Wrap")
        ),
        "electrical_rough_in" to ResidentialTemplate(
            category = "Electrical Rough-In",
            description = "Electrical rough-in (basic to smart home)",
            costPerSqFt = CostRange(4.50, 7.00, 5.75),
            laborHours = 12.0,
            materials = listOf("Wire", "Conduit", "Boxes", "Panel", "Breakers")
        ),
        "plumbing_rough_in" to ResidentialTemplate(
            category = "Plumbing Rough-In", 
            description = "Plumbing rough-in (basic to luxury fixtures)",
            costPerSqFt = CostRange(3.50, 6.00, 4.75),
            laborHours = 14.0,
            materials = listOf("PEX Pipe", "PVC Pipe", "Fittings", "Valves")
        ),
        "hvac" to ResidentialTemplate(
            category = "HVAC",
            description = "HVAC system (standard to high-efficiency)",
            costPerSqFt = CostRange(6.00, 10.00, 8.00),
            laborHours = 16.0,
            materials = listOf("Ductwork", "Unit", "Vents", "Thermostat", "Refrigerant")
        ),
        "insulation" to ResidentialTemplate(
            category = "Insulation",
            description = "Insulation (standard to high-performance)",
            costPerSqFt = CostRange(2.00, 3.50, 2.75),
            laborHours = 6.0,
            materials = listOf("Fiberglass Batts", "Blown-in", "Vapor Barrier")
        ),
        "drywall" to ResidentialTemplate(
            category = "Drywall",
            description = "Drywall installation (basic to textured finishes)",
            costPerSqFt = CostRange(2.25, 3.75, 3.00),
            laborHours = 10.0,
            materials = listOf("Drywall Sheets", "Compound", "Tape", "Primer")
        ),
        "flooring" to ResidentialTemplate(
            category = "Flooring",
            description = "Flooring installation (vinyl to hardwood)",
            costPerSqFt = CostRange(8.00, 25.00, 16.50),
            laborHours = 8.0,
            materials = listOf("Flooring Material", "Underlayment", "Adhesive", "Trim")
        ),
        "interior_painting" to ResidentialTemplate(
            category = "Interior Painting",
            description = "Interior painting (basic to premium)",
            costPerSqFt = CostRange(2.00, 4.00, 3.00),
            laborHours = 6.0,
            materials = listOf("Paint", "Primer", "Brushes", "Rollers")
        ),
        "exterior_finishing" to ResidentialTemplate(
            category = "Exterior Finishing",
            description = "Exterior finishing (siding type dependent)",
            costPerSqFt = CostRange(12.00, 22.00, 17.00),
            laborHours = 12.0,
            materials = listOf("Siding", "Trim", "Caulk", "Paint/Stain")
        ),
        "roofing" to ResidentialTemplate(
            category = "Roofing",
            description = "Roofing installation (asphalt to metal/tile)",
            costPerSqFt = CostRange(8.00, 15.00, 11.50),
            laborHours = 10.0,
            materials = listOf("Shingles/Material", "Underlayment", "Flashing", "Nails")
        )
    )
    
    /**
     * Labor Rates by Trade - 2025 Current Rates
     */
    private val laborRates = mapOf(
        "general_laborer" to LaborRate(
            trade = "General Laborer",
            hourlyRate = CostRange(18.0, 25.0, 21.5),
            skillLevel = SkillLevel.BASIC,
            region = "national_average"
        ),
        "framer" to LaborRate(
            trade = "Framer",
            hourlyRate = CostRange(22.0, 32.0, 27.0),
            skillLevel = SkillLevel.INTERMEDIATE,
            region = "national_average"
        ),
        "electrician" to LaborRate(
            trade = "Electrician",
            hourlyRate = CostRange(28.0, 45.0, 36.5),
            skillLevel = SkillLevel.ADVANCED,
            region = "national_average"
        ),
        "plumber" to LaborRate(
            trade = "Plumber",
            hourlyRate = CostRange(25.0, 40.0, 32.5),
            skillLevel = SkillLevel.ADVANCED,
            region = "national_average"
        ),
        "hvac_tech" to LaborRate(
            trade = "HVAC Technician",
            hourlyRate = CostRange(24.0, 38.0, 31.0),
            skillLevel = SkillLevel.ADVANCED,
            region = "national_average"
        ),
        "drywall_installer" to LaborRate(
            trade = "Drywall Installer",
            hourlyRate = CostRange(20.0, 30.0, 25.0),
            skillLevel = SkillLevel.INTERMEDIATE,
            region = "national_average"
        ),
        "painter" to LaborRate(
            trade = "Painter",
            hourlyRate = CostRange(18.0, 28.0, 23.0),
            skillLevel = SkillLevel.BASIC,
            region = "national_average"
        ),
        "roofer" to LaborRate(
            trade = "Roofer",
            hourlyRate = CostRange(20.0, 35.0, 27.5),
            skillLevel = SkillLevel.INTERMEDIATE,
            region = "national_average"
        ),
        "finish_carpenter" to LaborRate(
            trade = "Finish Carpenter",
            hourlyRate = CostRange(25.0, 40.0, 32.5),
            skillLevel = SkillLevel.ADVANCED,
            region = "national_average"
        ),
        "concrete_finisher" to LaborRate(
            trade = "Concrete Finisher",
            hourlyRate = CostRange(22.0, 35.0, 28.5),
            skillLevel = SkillLevel.INTERMEDIATE,
            region = "national_average"
        )
    )
    
    /**
     * Material Costs - 2025 Current Pricing
     */
    private val materialCosts = mapOf(
        "lumber_2x4x8_spf" to MaterialCost(
            name = "2x4x8 SPF Lumber",
            unit = "each",
            cost = 5.25,
            supplier = "Lumber Suppliers",
            lastUpdated = LocalDateTime.now()
        ),
        "concrete" to MaterialCost(
            name = "Ready-Mix Concrete",
            unit = "cubic yard",
            cost = 137.50,
            supplier = "Concrete Suppliers",
            lastUpdated = LocalDateTime.now()
        ),
        "rebar_4" to MaterialCost(
            name = "Rebar #4",
            unit = "linear foot",
            cost = 0.75,
            supplier = "Steel Suppliers",
            lastUpdated = LocalDateTime.now()
        ),
        "electrical_wire_12_2" to MaterialCost(
            name = "12-2 Romex Wire",
            unit = "linear foot",
            cost = 0.98,
            supplier = "Electrical Suppliers",
            lastUpdated = LocalDateTime.now()
        ),
        "pvc_pipe_3_4" to MaterialCost(
            name = "PVC Pipe 3/4\"",
            unit = "linear foot",
            cost = 0.65,
            supplier = "Plumbing Suppliers",
            lastUpdated = LocalDateTime.now()
        ),
        "drywall_4x8_half" to MaterialCost(
            name = "Drywall 4x8x1/2\"",
            unit = "sheet",
            cost = 14.00,
            supplier = "Building Suppliers",
            lastUpdated = LocalDateTime.now()
        ),
        "paint_premium" to MaterialCost(
            name = "Premium Interior Paint",
            unit = "gallon",
            cost = 55.00,
            supplier = "Paint Suppliers",
            lastUpdated = LocalDateTime.now()
        ),
        "asphalt_shingles" to MaterialCost(
            name = "Architectural Asphalt Shingles",
            unit = "square",
            cost = 115.00,
            supplier = "Roofing Suppliers",
            lastUpdated = LocalDateTime.now()
        ),
        "hardwood_flooring" to MaterialCost(
            name = "Hardwood Flooring (Oak)",
            unit = "sq ft",
            cost = 13.00,
            supplier = "Flooring Suppliers",
            lastUpdated = LocalDateTime.now()
        )
    )
    
    /**
     * Equipment Rental Rates - 2025 Current
     */
    private val equipmentRates = mapOf(
        "excavator_small" to EquipmentRate(
            name = "Small Excavator (14,000 lbs)",
            dailyRate = 450.0,
            weeklyRate = 2250.0,
            monthlyRate = 9000.0,
            category = "Heavy Equipment"
        ),
        "concrete_mixer" to EquipmentRate(
            name = "Concrete Mixer Truck",
            dailyRate = 125.0,
            weeklyRate = 625.0,
            monthlyRate = 2500.0,
            category = "Concrete Equipment"
        ),
        "skid_steer" to EquipmentRate(
            name = "Skid Steer Loader",
            dailyRate = 300.0,
            weeklyRate = 1500.0,
            monthlyRate = 6000.0,
            category = "Equipment"
        )
    )
    
    /**
     * Regional Cost Multipliers - 2025 Adjusted
     */
    private val regionalMultipliers = mapOf(
        "national_average" to 1.0,
        "san_francisco_ca" to 1.65,
        "new_york_ny" to 1.45,
        "seattle_wa" to 1.35,
        "boston_ma" to 1.30,
        "los_angeles_ca" to 1.25,
        "chicago_il" to 1.15,
        "denver_co" to 1.10,
        "atlanta_ga" to 1.00,
        "phoenix_az" to 0.95,
        "dallas_tx" to 0.90,
        "charlotte_nc" to 0.85,
        "kansas_city_mo" to 0.75
    )
    
    // Helper methods
    private fun adjustResidentialTemplates(multiplier: Double): Map<String, ResidentialTemplate> {
        return residentialTemplates.mapValues { (_, template) ->
            template.copy(
                costPerSqFt = CostRange(
                    template.costPerSqFt.min * multiplier,
                    template.costPerSqFt.max * multiplier,
                    template.costPerSqFt.average * multiplier
                )
            )
        }
    }
    
    private fun adjustLaborRates(region: String, multiplier: Double): Map<String, LaborRate> {
        return laborRates.mapValues { (_, rate) ->
            rate.copy(
                hourlyRate = CostRange(
                    rate.hourlyRate.min * multiplier,
                    rate.hourlyRate.max * multiplier,
                    rate.hourlyRate.average * multiplier
                ),
                region = region
            )
        }
    }
    
    private fun adjustMaterialCosts(multiplier: Double): Map<String, MaterialCost> {
        return materialCosts.mapValues { (_, cost) ->
            cost.copy(cost = cost.cost * multiplier)
        }
    }
    
    private fun adjustEquipmentRates(multiplier: Double): Map<String, EquipmentRate> {
        return equipmentRates.mapValues { (_, rate) ->
            rate.copy(
                dailyRate = rate.dailyRate * multiplier,
                weeklyRate = rate.weeklyRate * multiplier,
                monthlyRate = rate.monthlyRate * multiplier
            )
        }
    }
}

// Supporting data classes for cost calculations
data class DetailedProjectCost(
    val projectType: String,
    val squareFootage: Double,
    val region: String,
    val qualityLevel: String,
    val phaseCosts: Map<String, Double>,
    val subtotal: Double,
    val contingency: Double,
    val overhead: Double,
    val profit: Double,
    val totalCost: Double,
    val costPerSqFt: Double,
    val generatedDate: LocalDateTime
)