package com.nextgenbuildpro.data

import com.nextgenbuildpro.pm.data.model.*
import java.util.*

/**
 * Comprehensive Construction Assemblies Database
 * 
 * Sources: RSMeans Building Construction Cost Data 2024, BNI General Construction Costbook,
 * Craftsman Construction Cost Database, NIST Building Cost Manual, AIA Cost Data
 * 
 * This database contains detailed construction assemblies with component breakdowns,
 * labor times, material quantities, and equipment requirements for accurate estimating.
 */
object ComprehensiveAssembliesDatabase {

    /**
     * Foundation and Structural Assemblies
     * Based on RSMeans Foundation Systems and ACI Standards
     */
    fun getFoundationAssemblies(): Map<String, ConstructionAssembly> = mapOf(
        
        "concrete_footing_24x12" to ConstructionAssembly(
            id = "concrete_footing_24x12",
            name = "Concrete Strip Footing 24\"W x 12\"D",
            category = "Foundation",
            description = "Reinforced concrete strip footing for residential foundation",
            unit = "linear foot",
            components = listOf(
                AssemblyComponent(
                    description = "Excavation by machine",
                    trade = "excavation",
                    laborHours = 0.055,
                    materialCost = 0.0,
                    materialQuantity = 2.0,
                    materialUnit = "cubic foot"
                ),
                AssemblyComponent(
                    description = "Forms, footing, 4 use",
                    trade = "carpenter_concrete",
                    laborHours = 0.133,
                    materialCost = 2.89,
                    materialQuantity = 3.0,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Reinforcing steel, #4 bars",
                    trade = "ironworker",
                    laborHours = 0.027,
                    materialCost = 4.12,
                    materialQuantity = 12.0,
                    materialUnit = "pounds"
                ),
                AssemblyComponent(
                    description = "Concrete 3000 PSI",
                    trade = "concrete_finisher",
                    laborHours = 0.067,
                    materialCost = 8.95,
                    materialQuantity = 0.074,
                    materialUnit = "cubic yard"
                ),
                AssemblyComponent(
                    description = "Form stripping",
                    trade = "general_laborer",
                    laborHours = 0.044,
                    materialCost = 0.0,
                    materialQuantity = 3.0,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Backfill and compact",
                    trade = "equipment_operator",
                    laborHours = 0.033,
                    materialCost = 0.0,
                    materialQuantity = 1.5,
                    materialUnit = "cubic foot"
                )
            ),
            totalLaborHours = 0.359,
            totalMaterialCost = 15.96,
            equipmentCost = 2.87,
            difficulty = TaskDifficulty.STANDARD,
            renovationMultiplier = 1.4, // Existing foundation work is more complex
            qualityGrades = mapOf(
                "basic" to QualityMultiplier(0.85, "Standard residential footing"),
                "standard" to QualityMultiplier(1.0, "Code-compliant residential footing"),
                "premium" to QualityMultiplier(1.35, "Enhanced reinforcement and waterproofing")
            ),
            seasonalFactors = mapOf(
                "winter" to 1.25, // Cold weather concrete
                "summer" to 0.95,
                "spring" to 1.0,
                "fall" to 1.05
            ),
            accessibilityFactors = mapOf(
                "easy" to 1.0,
                "restricted" to 1.3,
                "very_restricted" to 1.6
            ),
            source = "RSMeans 2024, ACI 318-19",
            lastUpdated = Date()
        ),

        "basement_wall_8ft" to ConstructionAssembly(
            id = "basement_wall_8ft",
            name = "Basement Wall 8\" Concrete Block",
            category = "Foundation",
            description = "8-inch concrete masonry unit basement wall with reinforcement",
            unit = "square foot",
            components = listOf(
                AssemblyComponent(
                    description = "8\" concrete block, normal weight",
                    trade = "mason",
                    laborHours = 0.107,
                    materialCost = 2.45,
                    materialQuantity = 1.0,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Mortar for CMU",
                    trade = "mason",
                    laborHours = 0.0, // Included in block laying
                    materialCost = 0.68,
                    materialQuantity = 0.017,
                    materialUnit = "cubic foot"
                ),
                AssemblyComponent(
                    description = "Reinforcing steel #4 vertical",
                    trade = "ironworker",
                    laborHours = 0.013,
                    materialCost = 0.89,
                    materialQuantity = 3.0,
                    materialUnit = "pounds"
                ),
                AssemblyComponent(
                    description = "Concrete grout in cores",
                    trade = "mason",
                    laborHours = 0.020,
                    materialCost = 1.12,
                    materialQuantity = 0.0125,
                    materialUnit = "cubic yard"
                ),
                AssemblyComponent(
                    description = "Damp-proofing exterior",
                    trade = "waterproofing",
                    laborHours = 0.016,
                    materialCost = 0.78,
                    materialQuantity = 1.0,
                    materialUnit = "square foot"
                )
            ),
            totalLaborHours = 0.156,
            totalMaterialCost = 5.92,
            equipmentCost = 0.45,
            difficulty = TaskDifficulty.STANDARD,
            renovationMultiplier = 1.8, // Basement renovation very complex
            qualityGrades = mapOf(
                "basic" to QualityMultiplier(0.90, "Standard CMU wall"),
                "standard" to QualityMultiplier(1.0, "Reinforced CMU with dampproofing"),
                "premium" to QualityMultiplier(1.45, "Insulated CMU with full waterproofing")
            ),
            seasonalFactors = mapOf(
                "winter" to 1.15, // Cold weather masonry
                "summer" to 1.0,
                "spring" to 0.98,
                "fall" to 1.05
            ),
            accessibilityFactors = mapOf(
                "easy" to 1.0,
                "restricted" to 1.4,
                "very_restricted" to 1.8
            ),
            source = "RSMeans 2024, NCMA TEK Manual",
            lastUpdated = Date()
        ),

        "slab_on_grade_6in" to ConstructionAssembly(
            id = "slab_on_grade_6in",
            name = "Slab on Grade 6\" Reinforced",
            category = "Foundation",
            description = "6-inch reinforced concrete slab with vapor barrier and insulation",
            unit = "square foot",
            components = listOf(
                AssemblyComponent(
                    description = "Excavation and grading",
                    trade = "equipment_operator",
                    laborHours = 0.008,
                    materialCost = 0.0,
                    materialQuantity = 0.5,
                    materialUnit = "cubic foot"
                ),
                AssemblyComponent(
                    description = "Gravel base 4\" compacted",
                    trade = "general_laborer",
                    laborHours = 0.012,
                    materialCost = 0.89,
                    materialQuantity = 0.33,
                    materialUnit = "cubic foot"
                ),
                AssemblyComponent(
                    description = "Vapor barrier 6 mil poly",
                    trade = "general_laborer",
                    laborHours = 0.003,
                    materialCost = 0.18,
                    materialQuantity = 1.1,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Rigid foam insulation 2\"",
                    trade = "insulator",
                    laborHours = 0.008,
                    materialCost = 1.25,
                    materialQuantity = 1.0,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Welded wire mesh 6x6 W2.9",
                    trade = "ironworker",
                    laborHours = 0.005,
                    materialCost = 0.45,
                    materialQuantity = 1.1,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Concrete 4000 PSI",
                    trade = "concrete_finisher",
                    laborHours = 0.033,
                    materialCost = 3.12,
                    materialQuantity = 0.0185,
                    materialUnit = "cubic yard"
                ),
                AssemblyComponent(
                    description = "Finishing and curing",
                    trade = "concrete_finisher",
                    laborHours = 0.017,
                    materialCost = 0.25,
                    materialQuantity = 1.0,
                    materialUnit = "square foot"
                )
            ),
            totalLaborHours = 0.086,
            totalMaterialCost = 6.14,
            equipmentCost = 1.28,
            difficulty = TaskDifficulty.STANDARD,
            renovationMultiplier = 2.2, // Existing slab work very complex
            qualityGrades = mapOf(
                "basic" to QualityMultiplier(0.80, "Plain concrete slab"),
                "standard" to QualityMultiplier(1.0, "Reinforced with vapor barrier"),
                "premium" to QualityMultiplier(1.25, "Radiant heat-ready with premium finish")
            ),
            seasonalFactors = mapOf(
                "winter" to 1.35, // Cold weather concrete challenges
                "summer" to 0.95,
                "spring" to 1.0,
                "fall" to 1.08
            ),
            accessibilityFactors = mapOf(
                "easy" to 1.0,
                "restricted" to 1.25,
                "very_restricted" to 1.55
            ),
            source = "ACI 302.1R, RSMeans 2024",
            lastUpdated = Date()
        )
    )

    /**
     * Framing Assemblies - Wood and Steel
     * Based on ICC Building Codes, AWC Standards, AISC Steel Manual
     */
    fun getFramingAssemblies(): Map<String, ConstructionAssembly> = mapOf(
        
        "wall_framing_2x6_16oc" to ConstructionAssembly(
            id = "wall_framing_2x6_16oc",
            name = "Wood Stud Wall 2x6 @ 16\" O.C.",
            category = "Framing",
            description = "Interior/exterior wood stud wall with plates and blocking",
            unit = "square foot",
            components = listOf(
                AssemblyComponent(
                    description = "Studs 2x6x8' SPF",
                    trade = "carpenter_framing",
                    laborHours = 0.015,
                    materialCost = 0.89,
                    materialQuantity = 0.75,
                    materialUnit = "board foot"
                ),
                AssemblyComponent(
                    description = "Top and bottom plates 2x6",
                    trade = "carpenter_framing",
                    laborHours = 0.008,
                    materialCost = 0.25,
                    materialQuantity = 0.25,
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Blocking and miscellaneous",
                    trade = "carpenter_framing",
                    laborHours = 0.005,
                    materialCost = 0.18,
                    materialQuantity = 0.15,
                    materialUnit = "board foot"
                ),
                AssemblyComponent(
                    description = "Nails and fasteners",
                    trade = "carpenter_framing",
                    laborHours = 0.002,
                    materialCost = 0.12,
                    materialQuantity = 0.25,
                    materialUnit = "pounds"
                )
            ),
            totalLaborHours = 0.030,
            totalMaterialCost = 1.44,
            equipmentCost = 0.08,
            difficulty = TaskDifficulty.STANDARD,
            renovationMultiplier = 1.6, // Working around existing systems
            qualityGrades = mapOf(
                "basic" to QualityMultiplier(0.85, "Standard grade lumber"),
                "standard" to QualityMultiplier(1.0, "Construction grade lumber"),
                "premium" to QualityMultiplier(1.25, "Select structural grade")
            ),
            seasonalFactors = mapOf(
                "winter" to 1.10, // Cold weather slower productivity
                "summer" to 1.0,
                "spring" to 0.95,
                "fall" to 1.0
            ),
            accessibilityFactors = mapOf(
                "easy" to 1.0,
                "restricted" to 1.2,
                "very_restricted" to 1.45
            ),
            source = "ICC 2021, AWC NDS 2018",
            lastUpdated = Date()
        ),

        "floor_joist_2x10_16oc" to ConstructionAssembly(
            id = "floor_joist_2x10_16oc",
            name = "Floor Joist System 2x10 @ 16\" O.C.",
            category = "Framing",
            description = "Wood floor joist system with bridging and subflooring",
            unit = "square foot",
            components = listOf(
                AssemblyComponent(
                    description = "Floor joists 2x10x12' #2 SPF",
                    trade = "carpenter_framing",
                    laborHours = 0.022,
                    materialCost = 1.68,
                    materialQuantity = 1.33,
                    materialUnit = "board foot"
                ),
                AssemblyComponent(
                    description = "Rim joist and blocking",
                    trade = "carpenter_framing",
                    laborHours = 0.008,
                    materialCost = 0.35,
                    materialQuantity = 0.25,
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Cross bridging steel",
                    trade = "carpenter_framing",
                    laborHours = 0.005,
                    materialCost = 0.28,
                    materialQuantity = 1.0,
                    materialUnit = "each"
                ),
                AssemblyComponent(
                    description = "Subflooring 3/4\" OSB T&G",
                    trade = "carpenter_framing",
                    laborHours = 0.012,
                    materialCost = 2.45,
                    materialQuantity = 1.1,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Adhesive and fasteners",
                    trade = "carpenter_framing",
                    laborHours = 0.003,
                    materialCost = 0.18,
                    materialQuantity = 1.0,
                    materialUnit = "square foot"
                )
            ),
            totalLaborHours = 0.050,
            totalMaterialCost = 4.94,
            equipmentCost = 0.15,
            difficulty = TaskDifficulty.STANDARD,
            renovationMultiplier = 1.8, // Existing floor modification complex
            qualityGrades = mapOf(
                "basic" to QualityMultiplier(0.90, "Standard OSB subflooring"),
                "standard" to QualityMultiplier(1.0, "T&G OSB with adhesive"),
                "premium" to QualityMultiplier(1.35, "Plywood subflooring with sound control")
            ),
            seasonalFactors = mapOf(
                "winter" to 1.08,
                "summer" to 1.0,
                "spring" to 0.98,
                "fall" to 1.02
            ),
            accessibilityFactors = mapOf(
                "easy" to 1.0,
                "restricted" to 1.3,
                "very_restricted" to 1.6
            ),
            source = "AWC Span Tables, RSMeans 2024",
            lastUpdated = Date()
        ),

        "roof_truss_system" to ConstructionAssembly(
            id = "roof_truss_system",
            name = "Roof Truss System 24\" O.C.",
            category = "Framing",
            description = "Engineered roof trusses with bracing and sheathing",
            unit = "square foot",
            components = listOf(
                AssemblyComponent(
                    description = "Engineered roof trusses 24\" O.C.",
                    trade = "crane_operator",
                    laborHours = 0.018,
                    materialCost = 3.85,
                    materialQuantity = 1.0,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Installation and bracing",
                    trade = "carpenter_framing",
                    laborHours = 0.025,
                    materialCost = 0.45,
                    materialQuantity = 0.1,
                    materialUnit = "board foot"
                ),
                AssemblyComponent(
                    description = "Roof sheathing 7/16\" OSB",
                    trade = "carpenter_framing",
                    laborHours = 0.015,
                    materialCost = 1.28,
                    materialQuantity = 1.1,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Hurricane ties and fasteners",
                    trade = "carpenter_framing",
                    laborHours = 0.008,
                    materialCost = 0.35,
                    materialQuantity = 0.5,
                    materialUnit = "each"
                )
            ),
            totalLaborHours = 0.066,
            totalMaterialCost = 5.93,
            equipmentCost = 0.85, // Crane rental
            difficulty = TaskDifficulty.DIFFICULT,
            renovationMultiplier = 2.5, // Roof work on existing structure very complex
            qualityGrades = mapOf(
                "basic" to QualityMultiplier(0.85, "Standard truss design"),
                "standard" to QualityMultiplier(1.0, "Engineered truss system"),
                "premium" to QualityMultiplier(1.40, "Custom trusses with advanced bracing")
            ),
            seasonalFactors = mapOf(
                "winter" to 1.40, // Weather delays and safety issues
                "summer" to 0.95,
                "spring" to 1.0,
                "fall" to 1.15
            ),
            accessibilityFactors = mapOf(
                "easy" to 1.0,
                "restricted" to 1.35,
                "very_restricted" to 1.85
            ),
            source = "TPI-1 Standards, RSMeans 2024",
            lastUpdated = Date()
        )
    )

    /**
     * Envelope Assemblies - Roofing, Siding, Windows, Doors
     * Based on NRCA, AAMA, WDMA Standards
     */
    fun getEnvelopeAssemblies(): Map<String, ConstructionAssembly> = mapOf(
        
        "asphalt_shingle_roof" to ConstructionAssembly(
            id = "asphalt_shingle_roof",
            name = "Asphalt Shingle Roof System",
            category = "Roofing",
            description = "Complete asphalt shingle roof with underlayment and accessories",
            unit = "square foot",
            components = listOf(
                AssemblyComponent(
                    description = "Synthetic underlayment",
                    trade = "roofer",
                    laborHours = 0.008,
                    materialCost = 0.45,
                    materialQuantity = 1.1,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Ice and water shield",
                    trade = "roofer",
                    laborHours = 0.005,
                    materialCost = 0.85,
                    materialQuantity = 0.15, // First 3 feet
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Architectural shingles 30-year",
                    trade = "roofer",
                    laborHours = 0.035,
                    materialCost = 1.25,
                    materialQuantity = 1.15, // Include waste
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Ridge cap shingles",
                    trade = "roofer",
                    laborHours = 0.008,
                    materialCost = 0.18,
                    materialQuantity = 0.083, // Linear feet / 12
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Nails and fasteners",
                    trade = "roofer",
                    laborHours = 0.002,
                    materialCost = 0.15,
                    materialQuantity = 2.5,
                    materialUnit = "pounds"
                ),
                AssemblyComponent(
                    description = "Drip edge and flashing",
                    trade = "roofer",
                    laborHours = 0.012,
                    materialCost = 0.65,
                    materialQuantity = 0.125, // Linear feet / 8
                    materialUnit = "linear foot"
                )
            ),
            totalLaborHours = 0.070,
            totalMaterialCost = 3.53,
            equipmentCost = 0.25,
            difficulty = TaskDifficulty.DIFFICULT,
            renovationMultiplier = 1.8, // Tear-off and disposal adds complexity
            qualityGrades = mapOf(
                "basic" to QualityMultiplier(0.75, "25-year 3-tab shingles"),
                "standard" to QualityMultiplier(1.0, "30-year architectural shingles"),
                "premium" to QualityMultiplier(1.45, "50-year designer shingles with enhanced warranty")
            ),
            seasonalFactors = mapOf(
                "winter" to 1.60, // Weather restrictions and safety
                "summer" to 0.90, // Optimal conditions but heat stress
                "spring" to 1.0,
                "fall" to 1.10
            ),
            accessibilityFactors = mapOf(
                "easy" to 1.0, // Simple ranch roof
                "restricted" to 1.35, // Complex roof lines
                "very_restricted" to 1.75 // Steep, multi-level roofs
            ),
            source = "NRCA Manual, GAF Installation Guide",
            lastUpdated = Date()
        ),

        "vinyl_siding_system" to ConstructionAssembly(
            id = "vinyl_siding_system",
            name = "Vinyl Siding System Complete",
            category = "Siding",
            description = "Complete vinyl siding system with housewrap and trim",
            unit = "square foot",
            components = listOf(
                AssemblyComponent(
                    description = "House wrap weather barrier",
                    trade = "siding_installer",
                    laborHours = 0.008,
                    materialCost = 0.35,
                    materialQuantity = 1.1,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Furring strips 1x3",
                    trade = "carpenter_residential",
                    laborHours = 0.012,
                    materialCost = 0.28,
                    materialQuantity = 0.75,
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Vinyl siding panels",
                    trade = "siding_installer",
                    laborHours = 0.025,
                    materialCost = 1.85,
                    materialQuantity = 1.1,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "J-channel and trim",
                    trade = "siding_installer",
                    laborHours = 0.015,
                    materialCost = 0.45,
                    materialQuantity = 0.25,
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Corner posts and accessories",
                    trade = "siding_installer",
                    laborHours = 0.008,
                    materialCost = 0.65,
                    materialQuantity = 0.125,
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Fasteners and sealants",
                    trade = "siding_installer",
                    laborHours = 0.005,
                    materialCost = 0.22,
                    materialQuantity = 1.0,
                    materialUnit = "square foot"
                )
            ),
            totalLaborHours = 0.073,
            totalMaterialCost = 3.80,
            equipmentCost = 0.15,
            difficulty = TaskDifficulty.STANDARD,
            renovationMultiplier = 1.5, // Removal and prep work
            qualityGrades = mapOf(
                "basic" to QualityMultiplier(0.80, "Builder grade vinyl siding"),
                "standard" to QualityMultiplier(1.0, "Premium vinyl with insulation backing"),
                "premium" to QualityMultiplier(1.35, "Heavy-duty vinyl with designer colors")
            ),
            seasonalFactors = mapOf(
                "winter" to 1.25, // Cold weather installation issues
                "summer" to 1.0,
                "spring" to 0.95,
                "fall" to 1.05
            ),
            accessibilityFactors = mapOf(
                "easy" to 1.0,
                "restricted" to 1.25,
                "very_restricted" to 1.55
            ),
            source = "VSI Installation Manual, RSMeans 2024",
            lastUpdated = Date()
        )
    )

    /**
     * Mechanical, Electrical, and Plumbing (MEP) Assemblies
     * Based on NECA, NFPA, UPC, IPC Codes
     */
    fun getMEPAssemblies(): Map<String, ConstructionAssembly> = mapOf(
        
        "residential_electrical_service" to ConstructionAssembly(
            id = "residential_electrical_service",
            name = "200A Residential Electrical Service",
            category = "Electrical",
            description = "Complete 200A electrical service with panel and grounding",
            unit = "each",
            components = listOf(
                AssemblyComponent(
                    description = "200A main breaker panel",
                    trade = "electrician",
                    laborHours = 4.5,
                    materialCost = 385.00,
                    materialQuantity = 1.0,
                    materialUnit = "each"
                ),
                AssemblyComponent(
                    description = "Service entrance cable 4/0 AL",
                    trade = "electrician",
                    laborHours = 1.2,
                    materialCost = 125.00,
                    materialQuantity = 25.0,
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Meter socket and disconnect",
                    trade = "electrician",
                    laborHours = 2.0,
                    materialCost = 165.00,
                    materialQuantity = 1.0,
                    materialUnit = "each"
                ),
                AssemblyComponent(
                    description = "Grounding electrode system",
                    trade = "electrician",
                    laborHours = 1.8,
                    materialCost = 85.00,
                    materialQuantity = 1.0,
                    materialUnit = "each"
                ),
                AssemblyComponent(
                    description = "Conduit and fittings",
                    trade = "electrician",
                    laborHours = 1.5,
                    materialCost = 95.00,
                    materialQuantity = 15.0,
                    materialUnit = "linear foot"
                )
            ),
            totalLaborHours = 11.0,
            totalMaterialCost = 855.00,
            equipmentCost = 45.00,
            difficulty = TaskDifficulty.DIFFICULT,
            renovationMultiplier = 1.6, // Existing service upgrade complexity
            qualityGrades = mapOf(
                "basic" to QualityMultiplier(0.85, "Standard 200A service"),
                "standard" to QualityMultiplier(1.0, "Code-compliant with surge protection"),
                "premium" to QualityMultiplier(1.25, "Smart panel with whole-house surge protection")
            ),
            seasonalFactors = mapOf(
                "winter" to 1.15,
                "summer" to 1.0,
                "spring" to 0.98,
                "fall" to 1.05
            ),
            accessibilityFactors = mapOf(
                "easy" to 1.0,
                "restricted" to 1.3,
                "very_restricted" to 1.65
            ),
            source = "NEC 2023, NECA Installation Standards",
            lastUpdated = Date()
        ),

        "hvac_central_air_3ton" to ConstructionAssembly(
            id = "hvac_central_air_3ton",
            name = "Central Air System 3-Ton Split",
            category = "HVAC",
            description = "Complete 3-ton central air conditioning system with ductwork",
            unit = "each",
            components = listOf(
                AssemblyComponent(
                    description = "3-ton condensing unit 14 SEER",
                    trade = "hvac_technician",
                    laborHours = 6.0,
                    materialCost = 1850.00,
                    materialQuantity = 1.0,
                    materialUnit = "each"
                ),
                AssemblyComponent(
                    description = "Air handler with coil",
                    trade = "hvac_technician",
                    laborHours = 4.5,
                    materialCost = 1250.00,
                    materialQuantity = 1.0,
                    materialUnit = "each"
                ),
                AssemblyComponent(
                    description = "Ductwork supply and return",
                    trade = "sheet_metal_worker",
                    laborHours = 16.0,
                    materialCost = 850.00,
                    materialQuantity = 180.0,
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Refrigerant lines and insulation",
                    trade = "hvac_technician",
                    laborHours = 3.0,
                    materialCost = 185.00,
                    materialQuantity = 30.0,
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Thermostat programmable",
                    trade = "hvac_technician",
                    laborHours = 1.5,
                    materialCost = 125.00,
                    materialQuantity = 1.0,
                    materialUnit = "each"
                ),
                AssemblyComponent(
                    description = "Electrical connections and startup",
                    trade = "electrician",
                    laborHours = 2.0,
                    materialCost = 95.00,
                    materialQuantity = 1.0,
                    materialUnit = "each"
                )
            ),
            totalLaborHours = 33.0,
            totalMaterialCost = 4355.00,
            equipmentCost = 125.00,
            difficulty = TaskDifficulty.COMPLEX,
            renovationMultiplier = 1.8, // Retrofitting ductwork very complex
            qualityGrades = mapOf(
                "basic" to QualityMultiplier(0.80, "13-14 SEER standard efficiency"),
                "standard" to QualityMultiplier(1.0, "16 SEER high efficiency"),
                "premium" to QualityMultiplier(1.45, "20+ SEER with variable speed and smart controls")
            ),
            seasonalFactors = mapOf(
                "winter" to 0.90, // Less demand, better pricing
                "summer" to 1.25, // Peak season pricing
                "spring" to 1.0,
                "fall" to 0.95
            ),
            accessibilityFactors = mapOf(
                "easy" to 1.0,
                "restricted" to 1.4,
                "very_restricted" to 1.8
            ),
            source = "ACCA Manual J/D, RSMeans 2024",
            lastUpdated = Date()
        )
    )

    /**
     * Finish Assemblies - Interior finishes with quality grades
     * Based on PDCA, NWFA, NTCA Standards
     */
    fun getFinishAssemblies(): Map<String, ConstructionAssembly> = mapOf(
        
        "hardwood_flooring_oak" to ConstructionAssembly(
            id = "hardwood_flooring_oak",
            name = "Oak Hardwood Flooring 3/4\"",
            category = "Flooring",
            description = "Solid oak hardwood flooring with finish",
            unit = "square foot",
            components = listOf(
                AssemblyComponent(
                    description = "Red oak flooring 3/4\" x 2-1/4\"",
                    trade = "flooring_installer",
                    laborHours = 0.085,
                    materialCost = 4.85,
                    materialQuantity = 1.1, // Include waste
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Underlayment and moisture barrier",
                    trade = "flooring_installer",
                    laborHours = 0.008,
                    materialCost = 0.65,
                    materialQuantity = 1.05,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Nails and fasteners",
                    trade = "flooring_installer",
                    laborHours = 0.005,
                    materialCost = 0.25,
                    materialQuantity = 2.0,
                    materialUnit = "pounds"
                ),
                AssemblyComponent(
                    description = "Sanding 3 grits",
                    trade = "floor_sander",
                    laborHours = 0.045,
                    materialCost = 0.35,
                    materialQuantity = 1.0,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Polyurethane finish 3 coats",
                    trade = "floor_finisher",
                    laborHours = 0.025,
                    materialCost = 1.25,
                    materialQuantity = 1.0,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Base shoe molding",
                    trade = "trim_carpenter",
                    laborHours = 0.018,
                    materialCost = 0.85,
                    materialQuantity = 0.25,
                    materialUnit = "linear foot"
                )
            ),
            totalLaborHours = 0.186,
            totalMaterialCost = 8.20,
            equipmentCost = 0.45,
            difficulty = TaskDifficulty.DIFFICULT,
            renovationMultiplier = 1.4, // Removal and prep of existing flooring
            qualityGrades = mapOf(
                "basic" to QualityMultiplier(0.75, "Select grade oak, water-based finish"),
                "standard" to QualityMultiplier(1.0, "Premium grade oak, oil-based polyurethane"),
                "premium" to QualityMultiplier(1.65, "Exotic hardwoods with European finishes")
            ),
            seasonalFactors = mapOf(
                "winter" to 1.08, // Humidity control issues
                "summer" to 1.15, // Expansion concerns
                "spring" to 0.95,
                "fall" to 1.0
            ),
            accessibilityFactors = mapOf(
                "easy" to 1.0,
                "restricted" to 1.25,
                "very_restricted" to 1.50
            ),
            source = "NWFA Installation Guidelines",
            lastUpdated = Date()
        ),

        "tile_floor_ceramic" to ConstructionAssembly(
            id = "tile_floor_ceramic",
            name = "Ceramic Tile Floor 12\"x12\"",
            category = "Flooring",
            description = "Ceramic tile floor with proper substrate preparation",
            unit = "square foot",
            components = listOf(
                AssemblyComponent(
                    description = "Cement backer board 1/4\"",
                    trade = "tile_installer",
                    laborHours = 0.025,
                    materialCost = 1.15,
                    materialQuantity = 1.1,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Thinset adhesive",
                    trade = "tile_installer",
                    laborHours = 0.015,
                    materialCost = 0.85,
                    materialQuantity = 0.02,
                    materialUnit = "cubic foot"
                ),
                AssemblyComponent(
                    description = "12\"x12\" ceramic tile",
                    trade = "tile_installer",
                    laborHours = 0.095,
                    materialCost = 2.45,
                    materialQuantity = 1.1,
                    materialUnit = "square foot"
                ),
                AssemblyComponent(
                    description = "Grout sanded",
                    trade = "tile_installer",
                    laborHours = 0.035,
                    materialCost = 0.35,
                    materialQuantity = 0.005,
                    materialUnit = "cubic foot"
                ),
                AssemblyComponent(
                    description = "Sealer and caulk",
                    trade = "tile_installer",
                    laborHours = 0.012,
                    materialCost = 0.25,
                    materialQuantity = 1.0,
                    materialUnit = "square foot"
                )
            ),
            totalLaborHours = 0.182,
            totalMaterialCost = 5.05,
            equipmentCost = 0.25,
            difficulty = TaskDifficulty.STANDARD,
            renovationMultiplier = 1.6, // Substrate modification needed
            qualityGrades = mapOf(
                "basic" to QualityMultiplier(0.70, "Builder grade ceramic tile"),
                "standard" to QualityMultiplier(1.0, "Mid-grade ceramic with standard grout"),
                "premium" to QualityMultiplier(1.85, "Designer porcelain with epoxy grout")
            ),
            seasonalFactors = mapOf(
                "winter" to 1.12, // Temperature and humidity control
                "summer" to 1.05,
                "spring" to 0.98,
                "fall" to 1.0
            ),
            accessibilityFactors = mapOf(
                "easy" to 1.0,
                "restricted" to 1.3,
                "very_restricted" to 1.6
            ),
            source = "TCNA Installation Handbook",
            lastUpdated = Date()
        )
    )

    /**
     * Specialty and Custom Work Assemblies
     * High-end finishes and restoration work
     */
    fun getSpecialtyAssemblies(): Map<String, ConstructionAssembly> = mapOf(
        
        "custom_kitchen_cabinetry" to ConstructionAssembly(
            id = "custom_kitchen_cabinetry",
            name = "Custom Kitchen Cabinetry Premium",
            category = "Cabinetry",
            description = "Custom-built kitchen cabinets with premium finishes",
            unit = "linear foot",
            components = listOf(
                AssemblyComponent(
                    description = "Cabinet boxes plywood construction",
                    trade = "cabinet_maker",
                    laborHours = 3.5,
                    materialCost = 125.00,
                    materialQuantity = 1.0,
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Face frames solid hardwood",
                    trade = "cabinet_maker",
                    laborHours = 1.8,
                    materialCost = 65.00,
                    materialQuantity = 1.0,
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Doors and drawer fronts",
                    trade = "cabinet_maker",
                    laborHours = 2.2,
                    materialCost = 185.00,
                    materialQuantity = 1.0,
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Hardware premium soft-close",
                    trade = "cabinet_installer",
                    laborHours = 0.8,
                    materialCost = 95.00,
                    materialQuantity = 1.0,
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Installation and adjustment",
                    trade = "cabinet_installer",
                    laborHours = 1.2,
                    materialCost = 15.00,
                    materialQuantity = 1.0,
                    materialUnit = "linear foot"
                ),
                AssemblyComponent(
                    description = "Finishing and staining",
                    trade = "cabinet_finisher",
                    laborHours = 2.0,
                    materialCost = 45.00,
                    materialQuantity = 1.0,
                    materialUnit = "linear foot"
                )
            ),
            totalLaborHours = 11.5,
            totalMaterialCost = 530.00,
            equipmentCost = 25.00,
            difficulty = TaskDifficulty.COMPLEX,
            renovationMultiplier = 1.3, // Fitting to existing space
            qualityGrades = mapOf(
                "basic" to QualityMultiplier(0.60, "Stock cabinets with basic hardware"),
                "standard" to QualityMultiplier(1.0, "Custom cabinets with premium finishes"),
                "premium" to QualityMultiplier(1.75, "Hand-crafted with exotic woods and luxury hardware")
            ),
            seasonalFactors = mapOf(
                "winter" to 1.0,
                "summer" to 1.0,
                "spring" to 1.0,
                "fall" to 1.0
            ),
            accessibilityFactors = mapOf(
                "easy" to 1.0,
                "restricted" to 1.2,
                "very_restricted" to 1.4
            ),
            source = "AWI Quality Standards, Custom Cabinet Makers",
            lastUpdated = Date()
        )
    )

    /**
     * Get renovation complexity multipliers by project type
     */
    fun getRenovationMultipliers(): Map<String, Map<String, Double>> = mapOf(
        "kitchen_renovation" to mapOf(
            "demo_factor" to 1.4,
            "plumbing_factor" to 1.6,
            "electrical_factor" to 1.5,
            "cabinet_factor" to 1.2,
            "countertop_factor" to 1.3,
            "flooring_factor" to 1.4,
            "drywall_factor" to 1.5,
            "painting_factor" to 1.3
        ),
        "bathroom_renovation" to mapOf(
            "demo_factor" to 1.6,
            "plumbing_factor" to 1.8,
            "electrical_factor" to 1.4,
            "tile_factor" to 1.5,
            "fixtures_factor" to 1.3,
            "vanity_factor" to 1.2,
            "flooring_factor" to 1.6,
            "ventilation_factor" to 1.7
        ),
        "basement_finish" to mapOf(
            "moisture_control" to 1.8,
            "ceiling_height" to 1.4,
            "access_factor" to 1.6,
            "utilities_factor" to 1.5,
            "egress_factor" to 1.7,
            "insulation_factor" to 1.3,
            "flooring_factor" to 1.4
        ),
        "historic_restoration" to mapOf(
            "matching_materials" to 2.5,
            "specialty_trades" to 2.2,
            "permit_complexity" to 1.8,
            "access_restrictions" to 2.0,
            "discovery_factor" to 1.9,
            "preservation_requirements" to 2.3
        )
    )
}

/**
 * Supporting data classes for comprehensive assemblies
 */
data class ConstructionAssembly(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val unit: String,
    val components: List<AssemblyComponent>,
    val totalLaborHours: Double,
    val totalMaterialCost: Double,
    val equipmentCost: Double,
    val difficulty: TaskDifficulty,
    val renovationMultiplier: Double, // Factor for renovation work
    val qualityGrades: Map<String, QualityMultiplier>,
    val seasonalFactors: Map<String, Double>,
    val accessibilityFactors: Map<String, Double>,
    val source: String,
    val lastUpdated: Date,
    val specialRequirements: List<String> = emptyList(),
    val permitRequired: Boolean = false,
    val inspectionPoints: List<String> = emptyList(),
    val safetyRequirements: List<String> = emptyList()
)

data class AssemblyComponent(
    val description: String,
    val trade: String,
    val laborHours: Double,
    val materialCost: Double,
    val materialQuantity: Double,
    val materialUnit: String,
    val equipmentHours: Double = 0.0,
    val specialSkillRequired: Boolean = false,
    val wastePercentage: Double = 5.0 // Default 5% waste
)

data class QualityMultiplier(
    val multiplier: Double,
    val description: String
)