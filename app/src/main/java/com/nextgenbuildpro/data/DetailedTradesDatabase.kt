package com.nextgenbuildpro.data

import com.nextgenbuildpro.pm.data.model.*
import java.util.*

/**
 * Detailed Construction Trades Database
 * 
 * Comprehensive database of all construction trades with detailed task breakdowns,
 * productivity factors, skill requirements, and specialization levels.
 * 
 * Sources: Bureau of Labor Statistics Occupational Employment Statistics 2024,
 * Trade Union Rate Sheets, Professional Trade Associations,
 * Apprenticeship and Training Programs, OSHA Training Requirements
 */
object DetailedTradesDatabase {

    /**
     * Carpentry and Woodworking Trades
     * Multiple specializations within carpentry
     */
    fun getCarpentryTrades(): Map<String, DetailedTrade> = mapOf(
        
        "rough_carpenter" to DetailedTrade(
            tradeId = "rough_carpenter",
            tradeName = "Rough Carpenter",
            specialization = "Structural Framing",
            skillLevel = SkillLevel.JOURNEYMAN,
            baseHourlyRate = 32.00,
            benefits = 14.50,
            tasks = mapOf(
                "wall_framing" to TaskDefinition(
                    task = "Wall Framing",
                    unit = "linear foot",
                    hoursPerUnit = 0.25,
                    skillRequired = SkillLevel.SKILLED,
                    toolsRequired = listOf("Circular saw", "Hammer", "Speed square", "Level"),
                    safetyRequirements = listOf("Safety glasses", "Hard hat", "Steel toe boots"),
                    qualityFactors = mapOf(
                        "straight_walls" to 1.0,
                        "complex_angles" to 1.3,
                        "cathedral_ceiling" to 1.4
                    )
                ),
                "floor_joist_installation" to TaskDefinition(
                    task = "Floor Joist Installation",
                    unit = "square foot",
                    hoursPerUnit = 0.042,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Circular saw", "Speed square", "Hammer", "Chalk line"),
                    safetyRequirements = listOf("Fall protection", "Safety glasses", "Hard hat"),
                    qualityFactors = mapOf(
                        "standard_spans" to 1.0,
                        "long_spans" to 1.2,
                        "engineered_lumber" to 0.9
                    )
                ),
                "roof_framing" to TaskDefinition(
                    task = "Roof Framing",
                    unit = "square foot",
                    hoursPerUnit = 0.055,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Circular saw", "Framing square", "Chalk line", "Level"),
                    safetyRequirements = listOf("Fall protection system", "Safety harness", "Hard hat"),
                    qualityFactors = mapOf(
                        "simple_gable" to 1.0,
                        "hip_roof" to 1.3,
                        "complex_geometry" to 1.6
                    )
                ),
                "sheathing_installation" to TaskDefinition(
                    task = "Sheathing Installation",
                    unit = "square foot",
                    hoursPerUnit = 0.018,
                    skillRequired = SkillLevel.SKILLED,
                    toolsRequired = listOf("Circular saw", "Nail gun", "Chalk line"),
                    safetyRequirements = listOf("Fall protection", "Safety glasses"),
                    qualityFactors = mapOf(
                        "wall_sheathing" to 1.0,
                        "roof_sheathing" to 1.2, // More difficult
                        "structural_sheathing" to 1.1
                    )
                )
            ),
            certifications = listOf(
                "OSHA 10-hour Construction",
                "Fall Protection Training",
                "Scaffold Training"
            ),
            apprenticeshipRequired = true,
            unionAffiliation = "United Brotherhood of Carpenters",
            regionalVariation = 0.25,
            seasonalDemand = mapOf(
                "spring" to 1.2, // High demand
                "summer" to 1.3,
                "fall" to 1.1,
                "winter" to 0.8 // Lower demand
            ),
            source = "UBC Training Standards 2024"
        ),

        "finish_carpenter" to DetailedTrade(
            tradeId = "finish_carpenter",
            tradeName = "Finish Carpenter",
            specialization = "Interior Trim and Millwork",
            skillLevel = SkillLevel.JOURNEYMAN,
            baseHourlyRate = 38.00,
            benefits = 16.25,
            tasks = mapOf(
                "door_installation" to TaskDefinition(
                    task = "Interior Door Installation",
                    unit = "each",
                    hoursPerUnit = 2.5,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Router", "Chisels", "Level", "Measuring tools"),
                    safetyRequirements = listOf("Safety glasses", "Hearing protection"),
                    qualityFactors = mapOf(
                        "pre_hung_door" to 1.0,
                        "slab_door_custom" to 1.6,
                        "pocket_door" to 1.4
                    )
                ),
                "baseboard_installation" to TaskDefinition(
                    task = "Baseboard Installation",
                    unit = "linear foot",
                    hoursPerUnit = 0.12,
                    skillRequired = SkillLevel.SKILLED,
                    toolsRequired = listOf("Miter saw", "Coping saw", "Nail gun", "Level"),
                    safetyRequirements = listOf("Safety glasses", "Hearing protection"),
                    qualityFactors = mapOf(
                        "painted_pine" to 1.0,
                        "stain_grade_hardwood" to 1.3,
                        "custom_profiles" to 1.5
                    )
                ),
                "crown_molding" to TaskDefinition(
                    task = "Crown Molding Installation",
                    unit = "linear foot",
                    hoursPerUnit = 0.25,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Compound miter saw", "Coping saw", "Level", "Ladder"),
                    safetyRequirements = listOf("Fall protection", "Safety glasses"),
                    qualityFactors = mapOf(
                        "simple_profile" to 1.0,
                        "complex_profile" to 1.4,
                        "coffered_ceiling" to 2.0
                    )
                ),
                "cabinet_installation" to TaskDefinition(
                    task = "Cabinet Installation",
                    unit = "linear foot",
                    hoursPerUnit = 1.2,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Level", "Drill", "Shims", "Clamps"),
                    safetyRequirements = listOf("Safety glasses", "Back support"),
                    qualityFactors = mapOf(
                        "stock_cabinets" to 1.0,
                        "semi_custom" to 1.2,
                        "full_custom" to 1.5
                    )
                )
            ),
            certifications = listOf(
                "OSHA 10-hour Construction",
                "Millwork Installation Certification",
                "Cabinet Installation Training"
            ),
            apprenticeshipRequired = true,
            unionAffiliation = "United Brotherhood of Carpenters",
            regionalVariation = 0.30,
            seasonalDemand = mapOf(
                "spring" to 1.0,
                "summer" to 1.1,
                "fall" to 1.2, // Interior work season
                "winter" to 1.3 // Peak interior season
            ),
            source = "AWI Quality Standards, UBC Training"
        ),

        "cabinet_maker" to DetailedTrade(
            tradeId = "cabinet_maker",
            tradeName = "Cabinet Maker",
            specialization = "Custom Cabinetry Manufacturing",
            skillLevel = SkillLevel.MASTER_CRAFTSMAN,
            baseHourlyRate = 45.00,
            benefits = 18.75,
            tasks = mapOf(
                "custom_cabinet_construction" to TaskDefinition(
                    task = "Custom Cabinet Construction",
                    unit = "linear foot",
                    hoursPerUnit = 8.5,
                    skillRequired = SkillLevel.MASTER_CRAFTSMAN,
                    toolsRequired = listOf("Table saw", "Router table", "Jointer", "Planer", "Dovetail jig"),
                    safetyRequirements = listOf("Safety glasses", "Hearing protection", "Dust collection"),
                    qualityFactors = mapOf(
                        "plywood_construction" to 1.0,
                        "solid_wood_construction" to 1.4,
                        "dovetail_joinery" to 1.6,
                        "hand_carved_details" to 2.2
                    )
                ),
                "face_frame_construction" to TaskDefinition(
                    task = "Face Frame Construction",
                    unit = "linear foot",
                    hoursPerUnit = 2.8,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Pocket screw jig", "Router", "Sanding equipment"),
                    safetyRequirements = listOf("Safety glasses", "Dust protection"),
                    qualityFactors = mapOf(
                        "pocket_screw_construction" to 1.0,
                        "mortise_tenon_joinery" to 1.5,
                        "hand_fitted_joints" to 1.8
                    )
                ),
                "door_and_drawer_construction" to TaskDefinition(
                    task = "Doors and Drawers",
                    unit = "each",
                    hoursPerUnit = 3.2,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Router", "Drill press", "Edge bander", "Hinge jigs"),
                    safetyRequirements = listOf("Safety glasses", "Hearing protection"),
                    qualityFactors = mapOf(
                        "overlay_doors" to 1.0,
                        "inset_doors" to 1.4,
                        "raised_panel_doors" to 1.6,
                        "hand_carved_doors" to 2.8
                    )
                ),
                "finishing_and_installation" to TaskDefinition(
                    task = "Finishing and Installation",
                    unit = "linear foot",
                    hoursPerUnit = 2.5,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Spray equipment", "Sanders", "Installation tools"),
                    safetyRequirements = listOf("Respirator", "Ventilation", "Fire safety"),
                    qualityFactors = mapOf(
                        "painted_finish" to 1.0,
                        "stained_clear_coat" to 1.3,
                        "hand_rubbed_finish" to 1.8,
                        "french_polish" to 2.5
                    )
                )
            ),
            certifications = listOf(
                "AWI Certified Installer",
                "Finishing Certification",
                "OSHA 10-hour Construction"
            ),
            apprenticeshipRequired = true,
            unionAffiliation = "United Brotherhood of Carpenters",
            regionalVariation = 0.35, // High variation for custom work
            seasonalDemand = mapOf(
                "spring" to 1.0,
                "summer" to 1.0,
                "fall" to 1.1,
                "winter" to 1.2 // Indoor work, high demand
            ),
            source = "AWI Architectural Woodwork Standards"
        )
    )

    /**
     * Electrical Trades - Multiple Specializations
     */
    fun getElectricalTrades(): Map<String, DetailedTrade> = mapOf(
        
        "residential_electrician" to DetailedTrade(
            tradeId = "residential_electrician",
            tradeName = "Residential Electrician",
            specialization = "Single and Multi-Family Electrical",
            skillLevel = SkillLevel.JOURNEYMAN,
            baseHourlyRate = 42.00,
            benefits = 19.50,
            tasks = mapOf(
                "service_installation" to TaskDefinition(
                    task = "Electrical Service Installation",
                    unit = "each",
                    hoursPerUnit = 8.5,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Multimeter", "Wire strippers", "Conduit bender", "Fish tape"),
                    safetyRequirements = listOf("Arc flash protection", "Lockout/tagout", "PPE"),
                    qualityFactors = mapOf(
                        "100_amp_service" to 0.7,
                        "200_amp_service" to 1.0,
                        "400_amp_service" to 1.4
                    )
                ),
                "rough_in_wiring" to TaskDefinition(
                    task = "Rough-in Wiring",
                    unit = "outlet",
                    hoursPerUnit = 0.75,
                    skillRequired = SkillLevel.SKILLED,
                    toolsRequired = listOf("Drill", "Fish tape", "Wire strippers", "Electrical boxes"),
                    safetyRequirements = listOf("Safety glasses", "LOTO procedures"),
                    qualityFactors = mapOf(
                        "standard_outlet" to 1.0,
                        "gfci_outlet" to 1.2,
                        "usb_outlet" to 1.1,
                        "smart_switch" to 1.4
                    )
                ),
                "panel_wiring" to TaskDefinition(
                    task = "Panel Wiring and Circuit Installation",
                    unit = "circuit",
                    hoursPerUnit = 1.2,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Wire strippers", "Torque wrench", "Multimeter"),
                    safetyRequirements = listOf("Arc flash protection", "Electrical testing equipment"),
                    qualityFactors = mapOf(
                        "15_amp_circuit" to 0.9,
                        "20_amp_circuit" to 1.0,
                        "240v_circuit" to 1.3,
                        "smart_panel" to 1.5
                    )
                ),
                "fixture_installation" to TaskDefinition(
                    task = "Light Fixture Installation",
                    unit = "each",
                    hoursPerUnit = 1.5,
                    skillRequired = SkillLevel.SKILLED,
                    toolsRequired = listOf("Drill", "Wire strippers", "Level", "Stud finder"),
                    safetyRequirements = listOf("Safety glasses", "Ladder safety"),
                    qualityFactors = mapOf(
                        "standard_fixture" to 1.0,
                        "chandelier" to 1.8,
                        "recessed_lighting" to 0.8,
                        "landscape_lighting" to 1.3
                    )
                )
            ),
            certifications = listOf(
                "State Electrical License",
                "OSHA 10-hour Electrical",
                "NEC Code Training"
            ),
            apprenticeshipRequired = true,
            unionAffiliation = "International Brotherhood of Electrical Workers (IBEW)",
            regionalVariation = 0.30,
            seasonalDemand = mapOf(
                "spring" to 1.1,
                "summer" to 1.2,
                "fall" to 1.0,
                "winter" to 0.9 // Indoor work but lower construction activity
            ),
            source = "IBEW Training Standards, NECA Installation Guidelines"
        ),

        "commercial_electrician" to DetailedTrade(
            tradeId = "commercial_electrician",
            tradeName = "Commercial Electrician",
            specialization = "Commercial and Industrial Electrical",
            skillLevel = SkillLevel.JOURNEYMAN,
            baseHourlyRate = 48.00,
            benefits = 22.50,
            tasks = mapOf(
                "high_voltage_installation" to TaskDefinition(
                    task = "High Voltage Installation",
                    unit = "linear foot",
                    hoursPerUnit = 0.45,
                    skillRequired = SkillLevel.MASTER_ELECTRICIAN,
                    toolsRequired = listOf("High voltage test equipment", "Cable pulling equipment", "Torque wrenches"),
                    safetyRequirements = listOf("Arc flash suits", "High voltage safety training", "Confined space training"),
                    qualityFactors = mapOf(
                        "480v_systems" to 1.0,
                        "medium_voltage" to 1.6,
                        "switchgear" to 1.8
                    )
                ),
                "fire_alarm_systems" to TaskDefinition(
                    task = "Fire Alarm System Installation",
                    unit = "device",
                    hoursPerUnit = 1.8,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Programming laptop", "Multimeter", "Wire strippers"),
                    safetyRequirements = listOf("Safety glasses", "System testing protocols"),
                    qualityFactors = mapOf(
                        "conventional_system" to 1.0,
                        "addressable_system" to 1.3,
                        "voice_evacuation" to 1.6
                    )
                ),
                "motor_control_centers" to TaskDefinition(
                    task = "Motor Control Center Installation",
                    unit = "each",
                    hoursPerUnit = 16.0,
                    skillRequired = SkillLevel.MASTER_ELECTRICIAN,
                    toolsRequired = listOf("Torque wrenches", "Megohmmeter", "Power quality analyzer"),
                    safetyRequirements = listOf("Arc flash protection", "Electrical safety procedures"),
                    qualityFactors = mapOf(
                        "standard_mcc" to 1.0,
                        "variable_frequency_drives" to 1.4,
                        "intelligent_motor_control" to 1.7
                    )
                )
            ),
            certifications = listOf(
                "State Electrical License",
                "NFPA 70E Training", 
                "Arc Flash Safety Training",
                "Fire Alarm Certification"
            ),
            apprenticeshipRequired = true,
            unionAffiliation = "International Brotherhood of Electrical Workers (IBEW)",
            regionalVariation = 0.35,
            seasonalDemand = mapOf(
                "spring" to 1.0,
                "summer" to 1.1,
                "fall" to 1.0,
                "winter" to 1.0 // More stable commercial work
            ),
            source = "IBEW Training Standards, NECA Commercial Standards"
        )
    )

    /**
     * Plumbing Trades - Multiple Specializations
     */
    fun getPlumbingTrades(): Map<String, DetailedTrade> = mapOf(
        
        "residential_plumber" to DetailedTrade(
            tradeId = "residential_plumber",
            tradeName = "Residential Plumber",
            specialization = "Single and Multi-Family Plumbing",
            skillLevel = SkillLevel.JOURNEYMAN,
            baseHourlyRate = 45.00,
            benefits = 18.25,
            tasks = mapOf(
                "rough_in_plumbing" to TaskDefinition(
                    task = "Rough-in Plumbing",
                    unit = "fixture",
                    hoursPerUnit = 4.5,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Pipe cutters", "Torch", "Pipe wrenches", "Level"),
                    safetyRequirements = listOf("Safety glasses", "Fire extinguisher nearby"),
                    qualityFactors = mapOf(
                        "copper_pipe" to 1.0,
                        "pex_pipe" to 0.8,
                        "complex_routing" to 1.3
                    )
                ),
                "fixture_installation" to TaskDefinition(
                    task = "Fixture Installation",
                    unit = "each",
                    hoursPerUnit = 2.8,
                    skillRequired = SkillLevel.SKILLED,
                    toolsRequired = listOf("Pipe wrenches", "Basin wrench", "Level", "Sealants"),
                    safetyRequirements = listOf("Safety glasses", "Back protection"),
                    qualityFactors = mapOf(
                        "standard_fixtures" to 1.0,
                        "high_end_fixtures" to 1.3,
                        "wall_hung_fixtures" to 1.4,
                        "complex_faucets" to 1.6
                    )
                ),
                "water_heater_installation" to TaskDefinition(
                    task = "Water Heater Installation",
                    unit = "each",
                    hoursPerUnit = 6.0,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Pipe wrenches", "Torch", "Gas leak detector", "Multimeter"),
                    safetyRequirements = listOf("Gas safety procedures", "Ventilation requirements"),
                    qualityFactors = mapOf(
                        "standard_tank" to 1.0,
                        "tankless_gas" to 1.4,
                        "tankless_electric" to 1.2,
                        "hybrid_heat_pump" to 1.6
                    )
                ),
                "drain_cleaning_repair" to TaskDefinition(
                    task = "Drain Cleaning and Repair",
                    unit = "linear foot",
                    hoursPerUnit = 0.25,
                    skillRequired = SkillLevel.SKILLED,
                    toolsRequired = listOf("Drain snake", "Hydro jetter", "Camera inspection"),
                    safetyRequirements = listOf("Safety glasses", "Protective clothing"),
                    qualityFactors = mapOf(
                        "simple_cleaning" to 1.0,
                        "camera_inspection" to 1.3,
                        "pipe_replacement" to 2.0
                    )
                )
            ),
            certifications = listOf(
                "State Plumbing License",
                "Backflow Prevention Certification",
                "Gas Piping Certification",
                "OSHA 10-hour Construction"
            ),
            apprenticeshipRequired = true,
            unionAffiliation = "United Association of Plumbers and Pipefitters",
            regionalVariation = 0.28,
            seasonalDemand = mapOf(
                "spring" to 1.2, // New construction season
                "summer" to 1.3,
                "fall" to 1.0,
                "winter" to 1.1 // Emergency repairs, indoor work
            ),
            source = "UA Training Standards, Plumbing Codes"
        )
    )

    /**
     * HVAC and Mechanical Trades
     */
    fun getHVACTrades(): Map<String, DetailedTrade> = mapOf(
        
        "hvac_installer" to DetailedTrade(
            tradeId = "hvac_installer",
            tradeName = "HVAC Installation Technician",
            specialization = "Heating, Ventilation, and Air Conditioning",
            skillLevel = SkillLevel.JOURNEYMAN,
            baseHourlyRate = 41.00,
            benefits = 17.50,
            tasks = mapOf(
                "ductwork_installation" to TaskDefinition(
                    task = "Ductwork Installation",
                    unit = "linear foot",
                    hoursPerUnit = 0.18,
                    skillRequired = SkillLevel.SKILLED,
                    toolsRequired = listOf("Sheet metal snips", "Duct blaster", "Sealing materials"),
                    safetyRequirements = listOf("Cut protection gloves", "Safety glasses"),
                    qualityFactors = mapOf(
                        "flex_duct" to 1.0,
                        "rigid_metal_duct" to 1.3,
                        "insulated_ductwork" to 1.2,
                        "duct_sealing" to 1.1
                    )
                ),
                "equipment_installation" to TaskDefinition(
                    task = "HVAC Equipment Installation",
                    unit = "ton",
                    hoursPerUnit = 12.0,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Refrigeration tools", "Vacuum pump", "Manifold gauges", "Leak detector"),
                    safetyRequirements = listOf("Refrigerant handling certification", "Electrical safety"),
                    qualityFactors = mapOf(
                        "standard_efficiency" to 1.0,
                        "high_efficiency" to 1.2,
                        "variable_speed" to 1.4,
                        "geothermal_system" to 1.8
                    )
                ),
                "system_commissioning" to TaskDefinition(
                    task = "System Testing and Commissioning",
                    unit = "system",
                    hoursPerUnit = 6.0,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Digital manifolds", "Combustion analyzer", "Airflow meter"),
                    safetyRequirements = listOf("Gas safety procedures", "Electrical safety"),
                    qualityFactors = mapOf(
                        "basic_startup" to 1.0,
                        "performance_testing" to 1.3,
                        "full_commissioning" to 1.6
                    )
                )
            ),
            certifications = listOf(
                "EPA Section 608 Certification",
                "NATE Certification",
                "State HVAC License",
                "OSHA 10-hour Construction"
            ),
            apprenticeshipRequired = true,
            unionAffiliation = "Sheet Metal Workers Union",
            regionalVariation = 0.25,
            seasonalDemand = mapOf(
                "spring" to 1.3, // Installation season
                "summer" to 1.5, // Peak demand for AC
                "fall" to 1.1, 
                "winter" to 0.8 // Lower installation demand
            ),
            source = "SMACNA Installation Standards, ACCA Guidelines"
        )
    )

    /**
     * Masonry and Concrete Trades
     */
    fun getMasonryTrades(): Map<String, DetailedTrade> = mapOf(
        
        "brick_mason" to DetailedTrade(
            tradeId = "brick_mason",
            tradeName = "Brick Mason",
            specialization = "Brick and Stone Masonry",
            skillLevel = SkillLevel.JOURNEYMAN,
            baseHourlyRate = 44.00,
            benefits = 19.75,
            tasks = mapOf(
                "brick_veneer" to TaskDefinition(
                    task = "Brick Veneer Installation",
                    unit = "square foot",
                    hoursPerUnit = 0.25,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Trowels", "Levels", "Line blocks", "Joint tools"),
                    safetyRequirements = listOf("Safety glasses", "Back support", "Scaffold training"),
                    qualityFactors = mapOf(
                        "standard_brick" to 1.0,
                        "handmade_brick" to 1.4,
                        "complex_patterns" to 1.6,
                        "restoration_matching" to 2.1
                    )
                ),
                "stone_veneer" to TaskDefinition(
                    task = "Stone Veneer Installation",
                    unit = "square foot",
                    hoursPerUnit = 0.35,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Stone cutting tools", "Trowels", "Levels", "Safety equipment"),
                    safetyRequirements = listOf("Dust protection", "Cut protection", "Lifting assistance"),
                    qualityFactors = mapOf(
                        "manufactured_stone" to 1.0,
                        "natural_stone" to 1.5,
                        "dry_stack_stone" to 1.3,
                        "fieldstone" to 1.8
                    )
                ),
                "mortar_pointing" to TaskDefinition(
                    task = "Tuckpointing and Mortar Repair",
                    unit = "square foot",
                    hoursPerUnit = 0.45,
                    skillRequired = SkillLevel.JOURNEYMAN,
                    toolsRequired = listOf("Grinders", "Tuckpointing tools", "Mortar mixers"),
                    safetyRequirements = listOf("Dust protection", "Fall protection", "Eye protection"),
                    qualityFactors = mapOf(
                        "standard_repointing" to 1.0,
                        "historic_lime_mortar" to 1.6,
                        "color_matching" to 1.3,
                        "structural_repair" to 1.8
                    )
                )
            ),
            certifications = listOf(
                "MCAA Certification",
                "Scaffold Training",
                "OSHA 10-hour Construction"
            ),
            apprenticeshipRequired = true,
            unionAffiliation = "International Union of Bricklayers and Allied Craftworkers",
            regionalVariation = 0.40, // High variation based on local stone/brick availability
            seasonalDemand = mapOf(
                "spring" to 1.2,
                "summer" to 1.4, // Optimal masonry weather
                "fall" to 1.3,
                "winter" to 0.6 // Mortar freezing issues
            ),
            source = "BIA Technical Notes, MCAA Guidelines"
        )
    )

    /**
     * Get all trades combined
     */
    fun getAllDetailedTrades(): Map<String, DetailedTrade> {
        return getCarpentryTrades() + getElectricalTrades() + getPlumbingTrades() + getHVACTrades() + getMasonryTrades()
    }

    /**
     * Calculate productivity factors based on multiple variables
     */
    fun calculateProductivityFactor(
        tradeId: String,
        taskType: String,
        crewSize: Int,
        experienceLevel: SkillLevel,
        siteConditions: String,
        timeOfYear: String,
        workingHours: String = "standard" // standard, overtime, night
    ): Double {
        val trade = getAllDetailedTrades()[tradeId] ?: return 1.0
        val task = trade.tasks[taskType] ?: return 1.0
        
        var factor = 1.0
        
        // Crew size efficiency
        factor *= when(crewSize) {
            1 -> 0.8 // Solo work less efficient
            2 -> 1.0 // Optimal for most tasks
            3-4 -> 1.1 // Good efficiency
            5-8 -> 1.05 // Coordination challenges start
            else -> 0.95 // Large crew coordination issues
        }
        
        // Experience level factor
        factor *= when(experienceLevel) {
            SkillLevel.APPRENTICE -> 0.6
            SkillLevel.SKILLED -> 0.85
            SkillLevel.JOURNEYMAN -> 1.0
            SkillLevel.MASTER_CRAFTSMAN -> 1.15
            SkillLevel.MASTER_ELECTRICIAN -> 1.15
        }
        
        // Site conditions
        factor *= when(siteConditions) {
            "ideal" -> 1.05
            "good" -> 1.0
            "average" -> 0.95
            "difficult" -> 0.8
            "very_difficult" -> 0.65
            else -> 1.0
        }
        
        // Time of year (seasonal)
        factor *= trade.seasonalDemand[timeOfYear] ?: 1.0
        
        // Working hours
        factor *= when(workingHours) {
            "standard" -> 1.0,
            "overtime" -> 0.9, // Fatigue factor
            "night" -> 0.8, // Reduced visibility and productivity
            "weekend" -> 0.95 // Slightly reduced productivity
            else -> 1.0
        }
        
        return factor.coerceIn(0.4, 1.3) // Reasonable bounds
    }
}

/**
 * Supporting data classes for detailed trades
 */
data class DetailedTrade(
    val tradeId: String,
    val tradeName: String,
    val specialization: String,
    val skillLevel: SkillLevel,
    val baseHourlyRate: Double,
    val benefits: Double,
    val tasks: Map<String, TaskDefinition>,
    val certifications: List<String>,
    val apprenticeshipRequired: Boolean,
    val unionAffiliation: String,
    val regionalVariation: Double,
    val seasonalDemand: Map<String, Double>,
    val source: String,
    val equipmentOwnership: List<String> = emptyList(), // Tools worker typically owns
    val companyEquipment: List<String> = emptyList(), // Equipment company provides
    val safetyTraining: List<String> = emptyList(),
    val continuingEducation: List<String> = emptyList()
)

data class TaskDefinition(
    val task: String,
    val unit: String,
    val hoursPerUnit: Double,
    val skillRequired: SkillLevel,
    val toolsRequired: List<String>,
    val safetyRequirements: List<String>,
    val qualityFactors: Map<String, Double>,
    val materialWasteFactor: Double = 0.05, // 5% default waste
    val setupTime: Double = 0.0, // Hours of setup per task
    val cleanupTime: Double = 0.0, // Hours of cleanup per task
    val inspectionRequired: Boolean = false,
    val permitRequired: Boolean = false
)

/**
 * Skill levels with progression requirements
 */
enum class SkillLevel(val yearsExperience: IntRange, val description: String) {
    APPRENTICE(0..2, "Learning basic skills under supervision"),
    SKILLED(2..5, "Competent in most common tasks"),
    JOURNEYMAN(4..10, "Fully qualified tradesperson"),
    MASTER_CRAFTSMAN(8..99, "Expert level with specialized skills"),
    MASTER_ELECTRICIAN(8..99, "Licensed to supervise and design electrical work")
}

/**
 * Equipment and tool requirements by trade
 */
data class ToolRequirement(
    val toolName: String,
    val toolType: String, // hand_tool, power_tool, specialty_equipment
    val ownershipType: String, // worker_owned, company_provided, rental
    val dailyCost: Double, // Cost per day if rental
    val safetyRequired: Boolean,
    val certificationRequired: Boolean = false,
    val maintenanceRequired: Boolean = false
)