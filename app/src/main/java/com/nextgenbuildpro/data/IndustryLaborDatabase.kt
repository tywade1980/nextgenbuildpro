package com.nextgenbuildpro.data

import com.nextgenbuildpro.pm.data.model.LaborRate
import com.nextgenbuildpro.pm.data.model.MaterialCost
import com.nextgenbuildpro.pm.data.model.RegionalAdjustment
import java.util.*

/**
 * Industry-standard labor times and cost database
 * Sources: RSMeans, BNI Building News, NECA, Associated General Contractors of America
 * Updated: 2024-2025 rates with regional adjustments
 */
object IndustryLaborDatabase {

    /**
     * Labor rates by trade (per hour)
     * Based on Bureau of Labor Statistics 2024 and union rates
     */
    fun getLaborRates(): Map<String, LaborRate> = mapOf(
        // Carpenter/Framing
        "carpenter_residential" to LaborRate(
            trade = "Carpenter - Residential",
            baseHourlyRate = 28.50,
            skilledRate = 35.00,
            journeymanRate = 42.00,
            foremanRate = 48.00,
            source = "Bureau of Labor Statistics 2024, United Brotherhood of Carpenters",
            region = "National Average",
            effectiveDate = Date(),
            benefits = 12.75, // Health, retirement, etc.
            overhead = 15.0, // Company overhead percentage
            notes = "Includes residential framing, finish carpentry, cabinet installation"
        ),
        
        // Electrical
        "electrician" to LaborRate(
            trade = "Electrician",
            baseHourlyRate = 32.00,
            skilledRate = 42.00,
            journeymanRate = 56.00,
            foremanRate = 68.00,
            source = "IBEW Local Unions 2024, NECA",
            region = "National Average",
            effectiveDate = Date(),
            benefits = 18.50,
            overhead = 15.0,
            notes = "Licensed electricians for residential and commercial work"
        ),
        
        // Plumbing
        "plumber" to LaborRate(
            trade = "Plumber",
            baseHourlyRate = 30.00,
            skilledRate = 38.00,
            journeymanRate = 52.00,
            foremanRate = 62.00,
            source = "United Association of Plumbers 2024",
            region = "National Average",
            effectiveDate = Date(),
            benefits = 16.25,
            overhead = 15.0,
            notes = "Licensed plumbers for rough-in and finish plumbing"
        ),
        
        // HVAC
        "hvac_technician" to LaborRate(
            trade = "HVAC Technician",
            baseHourlyRate = 29.00,
            skilledRate = 36.00,
            journeymanRate = 48.00,
            foremanRate = 58.00,
            source = "Sheet Metal Workers Union 2024, SMACNA",
            region = "National Average",
            effectiveDate = Date(),
            benefits = 15.75,
            overhead = 15.0,
            notes = "Heating, ventilation, air conditioning installation and service"
        ),
        
        // Drywall
        "drywall_installer" to LaborRate(
            trade = "Drywall Installer",
            baseHourlyRate = 24.00,
            skilledRate = 30.00,
            journeymanRate = 38.00,
            foremanRate = 45.00,
            source = "Bureau of Labor Statistics 2024",
            region = "National Average",
            effectiveDate = Date(),
            benefits = 9.50,
            overhead = 15.0,
            notes = "Drywall hanging, taping, finishing"
        ),
        
        // Painting
        "painter" to LaborRate(
            trade = "Painter",
            baseHourlyRate = 22.00,
            skilledRate = 28.00,
            journeymanRate = 35.00,
            foremanRate = 42.00,
            source = "International Union of Painters 2024",
            region = "National Average",
            effectiveDate = Date(),
            benefits = 8.75,
            overhead = 15.0,
            notes = "Interior and exterior painting, surface preparation"
        ),
        
        // Flooring
        "flooring_installer" to LaborRate(
            trade = "Flooring Installer",
            baseHourlyRate = 25.00,
            skilledRate = 32.00,
            journeymanRate = 40.00,
            foremanRate = 48.00,
            source = "Bureau of Labor Statistics 2024, flooring trade associations",
            region = "National Average",
            effectiveDate = Date(),
            benefits = 10.25,
            overhead = 15.0,
            notes = "Hardwood, tile, carpet, vinyl installation"
        ),
        
        // Roofing
        "roofer" to LaborRate(
            trade = "Roofer",
            baseHourlyRate = 26.00,
            skilledRate = 33.00,
            journeymanRate = 42.00,
            foremanRate = 52.00,
            source = "United Union of Roofers 2024",
            region = "National Average",
            effectiveDate = Date(),
            benefits = 12.00,
            overhead = 15.0,
            notes = "Asphalt shingles, metal roofing, flat roofing systems"
        ),
        
        // Concrete
        "concrete_finisher" to LaborRate(
            trade = "Concrete Finisher",
            baseHourlyRate = 28.00,
            skilledRate = 35.00,
            journeymanRate = 45.00,
            foremanRate = 55.00,
            source = "Operating Engineers Union 2024, Concrete trade associations",
            region = "National Average",
            effectiveDate = Date(),
            benefits = 13.50,
            overhead = 15.0,
            notes = "Foundation work, slabs, decorative concrete"
        ),
        
        // General Laborer
        "general_laborer" to LaborRate(
            trade = "General Laborer",
            baseHourlyRate = 18.00,
            skilledRate = 22.00,
            journeymanRate = 28.00,
            foremanRate = 35.00,
            source = "Bureau of Labor Statistics 2024, LiUNA",
            region = "National Average",
            effectiveDate = Date(),
            benefits = 7.25,
            overhead = 15.0,
            notes = "General construction labor, cleanup, material handling"
        )
    )

    /**
     * Regional cost adjustment factors
     * Based on RSMeans City Cost Index 2024
     */
    fun getRegionalAdjustments(): Map<String, RegionalAdjustment> = mapOf(
        "san_francisco_ca" to RegionalAdjustment(
            region = "San Francisco, CA",
            laborMultiplier = 1.68,
            materialMultiplier = 1.15,
            equipmentMultiplier = 1.22,
            source = "RSMeans City Cost Index 2024",
            notes = "High cost of living area"
        ),
        "new_york_ny" to RegionalAdjustment(
            region = "New York, NY",
            laborMultiplier = 1.45,
            materialMultiplier = 1.08,
            equipmentMultiplier = 1.12,
            source = "RSMeans City Cost Index 2024",
            notes = "Metropolitan area with union presence"
        ),
        "seattle_wa" to RegionalAdjustment(
            region = "Seattle, WA",
            laborMultiplier = 1.32,
            materialMultiplier = 1.06,
            equipmentMultiplier = 1.15,
            source = "RSMeans City Cost Index 2024",
            notes = "Tech boom impacting construction costs"
        ),
        "denver_co" to RegionalAdjustment(
            region = "Denver, CO",
            laborMultiplier = 1.08,
            materialMultiplier = 1.02,
            equipmentMultiplier = 1.05,
            source = "RSMeans City Cost Index 2024",
            notes = "Growing market with moderate cost increases"
        ),
        "atlanta_ga" to RegionalAdjustment(
            region = "Atlanta, GA",
            laborMultiplier = 0.92,
            materialMultiplier = 0.98,
            equipmentMultiplier = 0.95,
            source = "RSMeans City Cost Index 2024",
            notes = "Lower cost region with competitive market"
        ),
        "houston_tx" to RegionalAdjustment(
            region = "Houston, TX",
            laborMultiplier = 0.95,
            materialMultiplier = 0.96,
            equipmentMultiplier = 0.98,
            source = "RSMeans City Cost Index 2024",
            notes = "Energy sector influence on construction"
        ),
        "phoenix_az" to RegionalAdjustment(
            region = "Phoenix, AZ",
            laborMultiplier = 0.98,
            materialMultiplier = 1.01,
            equipmentMultiplier = 1.03,
            source = "RSMeans City Cost Index 2024",
            notes = "Rapid growth market"
        ),
        "nashville_tn" to RegionalAdjustment(
            region = "Nashville, TN",
            laborMultiplier = 0.89,
            materialMultiplier = 0.95,
            equipmentMultiplier = 0.92,
            source = "RSMeans City Cost Index 2024",
            notes = "Below average costs with growing market"
        )
    )

    /**
     * Standard labor times for common construction tasks
     * Based on RSMeans, BNI, and industry standards (hours per unit)
     */
    fun getStandardLaborTimes(): Map<String, Map<String, Double>> = mapOf(
        "framing" to mapOf(
            "wall_framing_per_lf" to 0.25, // Linear foot of wall
            "floor_joist_per_sf" to 0.035, // Square foot of floor
            "roof_framing_per_sf" to 0.042, // Square foot of roof
            "subflooring_per_sf" to 0.015, // Square foot of subflooring
            "sheathing_per_sf" to 0.018, // Square foot of sheathing
            "source" to 0.0 // RSMeans 2024
        ),
        
        "electrical" to mapOf(
            "rough_in_per_outlet" to 0.75, // Per outlet/switch
            "panel_installation" to 8.0, // Per 200A panel
            "fixture_installation" to 1.25, // Per light fixture
            "service_upgrade" to 16.0, // 200A service upgrade
            "circuit_per_foot" to 0.08, // Per foot of circuit run
            "source" to 0.0 // NECA Manual of Labor Units 2024
        ),
        
        "plumbing" to mapOf(
            "rough_in_per_fixture" to 4.5, // Per plumbing fixture
            "water_line_per_foot" to 0.25, // Per foot of water line
            "drain_line_per_foot" to 0.35, // Per foot of drain line
            "fixture_installation" to 3.0, // Per fixture (toilet, sink, etc.)
            "water_heater_install" to 6.0, // Per water heater
            "source" to 0.0 // Plumbing trade standards 2024
        ),
        
        "drywall" to mapOf(
            "hanging_per_sf" to 0.012, // Square foot of drywall
            "taping_per_sf" to 0.008, // Square foot of taping
            "texture_per_sf" to 0.006, // Square foot of texture
            "total_per_sf" to 0.026, // Total drywall process per SF
            "corner_bead_per_lf" to 0.15, // Linear foot of corner bead
            "source" to 0.0 // Gypsum Association standards 2024
        ),
        
        "painting" to mapOf(
            "primer_per_sf" to 0.008, // Square foot of primer
            "paint_per_sf" to 0.007, // Square foot of paint
            "trim_per_lf" to 0.12, // Linear foot of trim painting
            "door_per_unit" to 2.0, // Per door (both sides)
            "cabinet_per_sf" to 0.25, // Square foot of cabinet painting
            "source" to 0.0 // Painting Contractors Association 2024
        ),
        
        "flooring" to mapOf(
            "hardwood_per_sf" to 0.085, // Square foot of hardwood
            "tile_per_sf" to 0.12, // Square foot of tile
            "carpet_per_sf" to 0.045, // Square foot of carpet
            "vinyl_per_sf" to 0.055, // Square foot of vinyl
            "subfloor_prep_per_sf" to 0.025, // Square foot of prep
            "source" to 0.0 // National Floor Covering Association 2024
        ),
        
        "roofing" to mapOf(
            "asphalt_shingles_per_sf" to 0.065, // Square foot of shingles
            "underlayment_per_sf" to 0.015, // Square foot of underlayment
            "flashing_per_lf" to 0.35, // Linear foot of flashing
            "gutters_per_lf" to 0.25, // Linear foot of gutters
            "tear_off_per_sf" to 0.025, // Square foot of tear-off
            "source" to 0.0 // National Roofing Contractors Association 2024
        )
    )

    /**
     * Material costs with supplier pricing
     * Based on current market rates from major suppliers
     */
    fun getMaterialCosts(): Map<String, MaterialCost> = mapOf(
        "lumber_2x4_8ft" to MaterialCost(
            item = "2x4 Lumber 8ft",
            unit = "each",
            cost = 4.85,
            supplier = "Home Depot/Lowes Average",
            lastUpdated = Date(),
            region = "National Average",
            notes = "Construction grade lumber, subject to market volatility"
        ),
        
        "lumber_2x6_8ft" to MaterialCost(
            item = "2x6 Lumber 8ft",
            unit = "each", 
            cost = 7.25,
            supplier = "Home Depot/Lowes Average",
            lastUpdated = Date(),
            region = "National Average",
            notes = "Construction grade lumber"
        ),
        
        "plywood_3_4_4x8" to MaterialCost(
            item = "Plywood 3/4\" 4x8",
            unit = "sheet",
            cost = 58.00,
            supplier = "Wholesale lumber yards",
            lastUpdated = Date(),
            region = "National Average",
            notes = "Construction grade plywood"
        ),
        
        "osb_7_16_4x8" to MaterialCost(
            item = "OSB 7/16\" 4x8",
            unit = "sheet",
            cost = 32.00,
            supplier = "Wholesale lumber yards", 
            lastUpdated = Date(),
            region = "National Average",
            notes = "Oriented strand board sheathing"
        ),
        
        "drywall_1_2_4x8" to MaterialCost(
            item = "Drywall 1/2\" 4x8",
            unit = "sheet",
            cost = 14.50,
            supplier = "Drywall supply houses",
            lastUpdated = Date(),
            region = "National Average",
            notes = "Standard gypsum drywall"
        ),
        
        "insulation_r13_batts" to MaterialCost(
            item = "Insulation R-13 Batts",
            unit = "sq ft",
            cost = 1.25,
            supplier = "Insulation suppliers",
            lastUpdated = Date(),
            region = "National Average", 
            notes = "Fiberglass batt insulation"
        ),
        
        "electrical_12_2_wire" to MaterialCost(
            item = "12-2 Romex Wire",
            unit = "linear foot",
            cost = 0.85,
            supplier = "Electrical supply houses",
            lastUpdated = Date(),
            region = "National Average",
            notes = "Copper wire for 20A circuits"
        ),
        
        "pvc_pipe_3_inch" to MaterialCost(
            item = "PVC Pipe 3 inch",
            unit = "linear foot", 
            cost = 3.50,
            supplier = "Plumbing supply houses",
            lastUpdated = Date(),
            region = "National Average",
            notes = "Schedule 40 PVC for drain lines"
        ),
        
        "concrete_3000_psi" to MaterialCost(
            item = "Concrete 3000 PSI",
            unit = "cubic yard",
            cost = 135.00,
            supplier = "Ready-mix concrete plants",
            lastUpdated = Date(),
            region = "National Average",
            notes = "Standard residential concrete mix"
        )
    )

    /**
     * Equipment costs and rental rates
     * Based on United Rentals, Home Depot Tool Rental pricing
     */
    fun getEquipmentCosts(): Map<String, MaterialCost> = mapOf(
        "excavator_small" to MaterialCost(
            item = "Mini Excavator Rental",
            unit = "day",
            cost = 350.00,
            supplier = "United Rentals",
            lastUpdated = Date(),
            region = "National Average",
            notes = "8,000-15,000 lb excavator with operator"
        ),
        
        "dump_truck" to MaterialCost(
            item = "Dump Truck Rental",
            unit = "day",
            cost = 450.00,
            supplier = "Equipment rental companies",
            lastUpdated = Date(), 
            region = "National Average",
            notes = "10-yard dump truck with driver"
        ),
        
        "concrete_mixer" to MaterialCost(
            item = "Concrete Mixer Truck",
            unit = "load",
            cost = 165.00,
            supplier = "Ready-mix companies",
            lastUpdated = Date(),
            region = "National Average", 
            notes = "Delivery and pour service per cubic yard"
        ),
        
        "crane_small" to MaterialCost(
            item = "Mobile Crane 25-ton",
            unit = "day",
            cost = 1200.00,
            supplier = "Crane rental companies",
            lastUpdated = Date(),
            region = "National Average",
            notes = "Including certified operator"
        )
    )

    /**
     * Calculate total cost including labor, materials, and overhead
     */
    fun calculateProjectCost(
        laborHours: Double,
        tradeType: String,
        materialCosts: Double,
        equipmentCosts: Double = 0.0,
        region: String = "national_average",
        overheadPercentage: Double = 15.0,
        profitMargin: Double = 10.0
    ): Double {
        val laborRate = getLaborRates()[tradeType]?.journeymanRate ?: 35.0
        val benefits = getLaborRates()[tradeType]?.benefits ?: 10.0
        
        val regionalAdjustment = getRegionalAdjustments()[region]?.laborMultiplier ?: 1.0
        
        val totalLaborCost = (laborHours * (laborRate + benefits)) * regionalAdjustment
        val subtotal = totalLaborCost + materialCosts + equipmentCosts
        val withOverhead = subtotal * (1 + overheadPercentage / 100)
        val finalCost = withOverhead * (1 + profitMargin / 100)
        
        return finalCost
    }
}