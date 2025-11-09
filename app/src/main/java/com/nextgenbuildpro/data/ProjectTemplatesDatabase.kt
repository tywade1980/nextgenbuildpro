package com.nextgenbuildpro.data

import com.nextgenbuildpro.pm.data.model.*
import java.util.*

/**
 * Comprehensive Project Templates Database
 * 
 * Complete construction project templates with detailed scope, phases, and requirements
 * 
 * Sources: National Association of Home Builders (NAHB), Associated General Contractors (AGC),
 * Construction Industry Institute (CII), Project Management Institute Construction Extension,
 * RSMeans Project Cost Database, Building Performance Institute (BPI)
 */
object ProjectTemplatesDatabase {

    /**
     * Residential Project Templates
     * Complete home construction and renovation projects
     */
    fun getResidentialProjectTemplates(): Map<String, ProjectTemplate> = mapOf(
        
        "single_family_custom_2000sf" to ProjectTemplate(
            templateId = "single_family_custom_2000sf",
            name = "Single Family Custom Home 2,000 SF",
            category = "New Construction",
            description = "Custom single family home with standard amenities",
            typicalSquareFootage = 2000.0,
            typicalDuration = 120, // Calendar days
            phases = listOf(
                ConstructionPhase(
                    phaseName = "Site Preparation and Foundation",
                    duration = 15,
                    percentOfTotal = 0.12,
                    criticalPath = true,
                    tasks = listOf(
                        "Site clearing and grading",
                        "Utility connections", 
                        "Foundation excavation",
                        "Footings and foundation walls",
                        "Foundation waterproofing",
                        "Backfill and compaction"
                    ),
                    trades = listOf("excavation", "concrete_finisher", "waterproofing"),
                    inspections = listOf("Footing inspection", "Foundation inspection"),
                    materialPercentage = 0.08
                ),
                
                ConstructionPhase(
                    phaseName = "Framing and Structure",
                    duration = 20,
                    percentOfTotal = 0.18,
                    criticalPath = true,
                    tasks = listOf(
                        "Floor framing and subflooring",
                        "Wall framing and sheathing",
                        "Roof framing or truss installation",
                        "Roof sheathing and weatherization",
                        "Windows and exterior doors",
                        "Siding and exterior trim"
                    ),
                    trades = listOf("carpenter_framing", "crane_operator", "siding_installer"),
                    inspections = listOf("Framing inspection"),
                    materialPercentage = 0.22
                ),
                
                ConstructionPhase(
                    phaseName = "Mechanical, Electrical, Plumbing",
                    duration = 18,
                    percentOfTotal = 0.20,
                    criticalPath = true,
                    tasks = listOf(
                        "Electrical rough-in",
                        "Plumbing rough-in", 
                        "HVAC ductwork installation",
                        "HVAC equipment installation",
                        "Insulation installation",
                        "Utility inspections"
                    ),
                    trades = listOf("electrician", "plumber", "hvac_technician", "insulator"),
                    inspections = listOf("Electrical rough", "Plumbing rough", "Mechanical rough"),
                    materialPercentage = 0.25
                ),
                
                ConstructionPhase(
                    phaseName = "Interior Finishes",
                    duration = 35,
                    percentOfTotal = 0.30,
                    criticalPath = false,
                    tasks = listOf(
                        "Drywall installation and finishing",
                        "Interior painting",
                        "Flooring installation",
                        "Kitchen cabinet installation",
                        "Bathroom finish work",
                        "Interior trim and millwork",
                        "Electrical and plumbing fixture installation"
                    ),
                    trades = listOf("drywall_installer", "painter", "flooring_installer", "cabinet_installer", "trim_carpenter"),
                    inspections = listOf("Insulation inspection", "Final electrical", "Final plumbing"),
                    materialPercentage = 0.35
                ),
                
                ConstructionPhase(
                    phaseName = "Final and Cleanup",
                    duration = 12,
                    percentOfTotal = 0.08,
                    criticalPath = false,
                    tasks = listOf(
                        "Final inspections",
                        "Exterior landscaping",
                        "Driveway and walkways",
                        "Final cleanup",
                        "Punch list completion",
                        "Owner walkthrough"
                    ),
                    trades = listOf("landscaper", "concrete_finisher", "general_laborer"),
                    inspections = listOf("Final building inspection", "Certificate of occupancy"),
                    materialPercentage = 0.05
                )
            ),
            typicalCostPerSF = 165.0, // National average for custom home
            costBreakdown = CostBreakdown(
                description = "Single Family Custom Home",
                quantity = 2000.0,
                unit = "square foot",
                laborHours = 0.0, // Calculated from assemblies
                laborRate = 0.0, // Calculated
                laborCost = 95000.0, // Estimated
                materialCost = 180000.0,
                equipmentCost = 25000.0,
                subtotal = 300000.0,
                overhead = 45000.0, // 15%
                profit = 36000.0, // 12%
                totalCost = 381000.0, // With contingency
                costPerUnit = 190.50,
                tradeType = "Multiple",
                region = "National Average"
            ),
            qualityOptions = mapOf(
                "starter_home" to QualityOption(
                    multiplier = 0.75,
                    description = "Entry-level finishes and fixtures",
                    typicalFeatures = listOf(
                        "Vinyl flooring throughout",
                        "Laminate countertops", 
                        "Basic appliance package",
                        "Painted trim and doors",
                        "Standard fixtures"
                    )
                ),
                "standard_home" to QualityOption(
                    multiplier = 1.0,
                    description = "Mid-grade finishes and features",
                    typicalFeatures = listOf(
                        "Hardwood floors in main areas",
                        "Granite countertops",
                        "Stainless steel appliances",
                        "Crown molding and trim package",
                        "Quality fixtures throughout"
                    )
                ),
                "luxury_home" to QualityOption(
                    multiplier = 1.65,
                    description = "High-end finishes and custom features",
                    typicalFeatures = listOf(
                        "Exotic hardwood floors",
                        "Natural stone countertops",
                        "Professional appliance suite",
                        "Custom millwork throughout", 
                        "Designer fixtures and finishes",
                        "Smart home automation"
                    )
                )
            ),
            regionalFactors = IndustryLaborDatabase.getRegionalAdjustments().mapValues { it.value.laborMultiplier },
            specialRequirements = listOf(
                "Building permit required",
                "Structural engineer for spans over 20 feet",
                "Energy code compliance",
                "Smoke detector and GFCI requirements",
                "Handicap accessibility considerations"
            ),
            commonAddOns = mapOf(
                "covered_patio" to 8500.0,
                "fireplace" to 4500.0,
                "deck_400sf" to 12000.0,
                "garage_2car" to 18000.0,
                "basement_finish" to 25000.0,
                "swimming_pool" to 45000.0
            ),
            source = "NAHB Cost Study 2024, RSMeans Residential"
        ),

        "kitchen_renovation_major" to ProjectTemplate(
            templateId = "kitchen_renovation_major", 
            name = "Major Kitchen Renovation",
            category = "Renovation",
            description = "Complete kitchen renovation with layout changes",
            typicalSquareFootage = 200.0,
            typicalDuration = 28, // Calendar days
            phases = listOf(
                ConstructionPhase(
                    phaseName = "Planning and Demolition",
                    duration = 5,
                    percentOfTotal = 0.15,
                    criticalPath = true,
                    tasks = listOf(
                        "Final design and material selection",
                        "Permit application",
                        "Temporary kitchen setup",
                        "Careful demolition",
                        "Debris removal",
                        "Structural assessment"
                    ),
                    trades = listOf("general_contractor", "general_laborer"),
                    inspections = listOf("Demo inspection if structural"),
                    materialPercentage = 0.05
                ),
                
                ConstructionPhase(
                    phaseName = "Infrastructure Updates",
                    duration = 8,
                    percentOfTotal = 0.25,
                    criticalPath = true,
                    tasks = listOf(
                        "Electrical upgrades and new circuits",
                        "Plumbing modifications",
                        "HVAC adjustments",
                        "Structural modifications if needed",
                        "Window or door modifications",
                        "Rough inspections"
                    ),
                    trades = listOf("electrician", "plumber", "hvac_technician", "carpenter_residential"),
                    inspections = listOf("Electrical rough", "Plumbing rough", "Framing if applicable"),
                    materialPercentage = 0.20
                ),
                
                ConstructionPhase(
                    phaseName = "Surfaces and Finishes",
                    duration = 10,
                    percentOfTotal = 0.35,
                    criticalPath = false,
                    tasks = listOf(
                        "Drywall repairs and new walls",
                        "Flooring installation",
                        "Tile work (backsplash, counters)",
                        "Interior painting",
                        "Ceiling work",
                        "Window trim and casing"
                    ),
                    trades = listOf("drywall_installer", "flooring_installer", "tile_installer", "painter"),
                    inspections = listOf("Insulation if applicable"),
                    materialPercentage = 0.30
                ),
                
                ConstructionPhase(
                    phaseName = "Cabinetry and Appliances",
                    duration = 12,
                    percentOfTotal = 0.40,
                    criticalPath = false,
                    tasks = listOf(
                        "Cabinet installation",
                        "Countertop template and installation",
                        "Appliance installation",
                        "Plumbing fixture installation",
                        "Electrical fixture installation",
                        "Hardware and accessories"
                    ),
                    trades = listOf("cabinet_installer", "countertop_installer", "appliance_installer", "plumber", "electrician"),
                    inspections = listOf("Final electrical", "Final plumbing"),
                    materialPercentage = 0.50
                ),
                
                ConstructionPhase(
                    phaseName = "Final Details and Cleanup",
                    duration = 3,
                    percentOfTotal = 0.08,
                    criticalPath = false,
                    tasks = listOf(
                        "Touch-up painting",
                        "Hardware adjustments",
                        "Appliance testing and training",
                        "Final cleaning",
                        "Punch list completion",
                        "Client walkthrough"
                    ),
                    trades = listOf("painter", "general_contractor", "general_laborer"),
                    inspections = listOf("Final inspection if required"),
                    materialPercentage = 0.02
                )
            ),
            typicalCostPerSF = 185.0, // Kitchen renovation cost per SF
            costBreakdown = CostBreakdown(
                description = "Major Kitchen Renovation",
                quantity = 200.0,
                unit = "square foot",
                laborHours = 0.0,
                laborRate = 0.0,
                laborCost = 18500.0,
                materialCost = 28000.0,
                equipmentCost = 2500.0,
                subtotal = 49000.0,
                overhead = 7350.0, // 15%
                profit = 5880.0, // 12%
                totalCost = 62230.0,
                costPerUnit = 311.15,
                tradeType = "Multiple",
                region = "National Average"
            ),
            qualityOptions = mapOf(
                "budget_renovation" to QualityOption(
                    multiplier = 0.65,
                    description = "Budget-friendly renovation with basic improvements",
                    typicalFeatures = listOf(
                        "Laminate countertops",
                        "Stock cabinets with basic hardware",
                        "Standard appliance package",
                        "Ceramic tile backsplash",
                        "Vinyl or basic hardwood flooring"
                    )
                ),
                "standard_renovation" to QualityOption(
                    multiplier = 1.0,
                    description = "Quality renovation with good materials",
                    typicalFeatures = listOf(
                        "Granite or quartz countertops",
                        "Semi-custom cabinets",
                        "Stainless steel appliances",
                        "Natural stone or glass tile backsplash",
                        "Hardwood or quality tile flooring"
                    )
                ),
                "luxury_renovation" to QualityOption(
                    multiplier = 1.85,
                    description = "High-end renovation with premium everything",
                    typicalFeatures = listOf(
                        "Natural stone or premium engineered counters",
                        "Full custom cabinetry",
                        "Professional grade appliances",
                        "Designer tile and stonework",
                        "Exotic hardwood or natural stone flooring",
                        "Custom lighting and smart home features"
                    )
                )
            ),
            regionalFactors = mapOf(
                "high_cost_markets" to 1.4, // SF, NYC, Seattle
                "standard_markets" to 1.0,
                "low_cost_markets" to 0.8 // Rural, some Southern markets
            ),
            specialRequirements = listOf(
                "Building permit typically required",
                "Electrical permit for new circuits",
                "Plumbing permit for fixture moves",
                "Temporary kitchen planning",
                "Lead paint considerations in pre-1978 homes",
                "Structural engineering if load-bearing changes"
            ),
            commonAddOns = mapOf(
                "kitchen_island" to 8500.0,
                "butler_pantry" to 12000.0,
                "wine_storage" to 5500.0,
                "breakfast_nook" to 6500.0,
                "pot_filler" to 750.0,
                "undermount_lighting" to 2200.0,
                "tile_to_ceiling" to 3500.0,
                "crown_molding" to 2800.0
            ),
            riskFactors = mapOf(
                "plumbing_surprises" to 0.15, // 15% chance of issues
                "electrical_upgrades" to 0.25,
                "structural_modifications" to 0.10,
                "permit_delays" to 0.20,
                "material_delays" to 0.30
            ),
            seasonalConsiderations = mapOf(
                "best_start_months" to listOf("March", "April", "September", "October"),
                "avoid_months" to listOf("November", "December"), // Holiday disruption
                "weather_dependent_phases" to listOf("None - mostly interior work")
            ),
            source = "NKBA Kitchen Remodeling Guidelines 2024"
        ),

        "bathroom_renovation_master" to ProjectTemplate(
            templateId = "bathroom_renovation_master",
            name = "Master Bathroom Renovation",
            category = "Renovation", 
            description = "Complete master bathroom renovation with luxury features",
            typicalSquareFootage = 120.0,
            typicalDuration = 21, // Calendar days
            phases = listOf(
                ConstructionPhase(
                    phaseName = "Planning and Demolition",
                    duration = 3,
                    percentOfTotal = 0.12,
                    criticalPath = true,
                    tasks = listOf(
                        "Material selections finalized",
                        "Permits obtained",
                        "Temporary bathroom arrangements",
                        "Careful demolition",
                        "Debris removal and disposal",
                        "Structural assessment"
                    ),
                    trades = listOf("general_contractor", "general_laborer"),
                    inspections = emptyList(),
                    materialPercentage = 0.03
                ),
                
                ConstructionPhase(
                    phaseName = "Infrastructure and Rough-in",
                    duration = 7,
                    percentOfTotal = 0.30,
                    criticalPath = true,
                    tasks = listOf(
                        "Plumbing rough-in modifications",
                        "Electrical upgrades and GFCI",
                        "Ventilation fan installation",
                        "Shower pan and waterproofing",
                        "Structural modifications if needed",
                        "Rough inspections"
                    ),
                    trades = listOf("plumber", "electrician", "hvac_technician", "waterproofing"),
                    inspections = listOf("Plumbing rough", "Electrical rough", "Framing if structural"),
                    materialPercentage = 0.20
                ),
                
                ConstructionPhase(
                    phaseName = "Surfaces and Tile Work",
                    duration = 8,
                    percentOfTotal = 0.35,
                    criticalPath = false,
                    tasks = listOf(
                        "Drywall repairs and installation",
                        "Waterproofing behind tile areas",
                        "Floor tile installation", 
                        "Wall tile installation",
                        "Shower tile and glass installation",
                        "Grouting and sealing"
                    ),
                    trades = listOf("drywall_installer", "tile_installer", "waterproofing", "glazier"),
                    inspections = listOf("Waterproofing inspection"),
                    materialPercentage = 0.40
                ),
                
                ConstructionPhase(
                    phaseName = "Fixtures and Finishes",
                    duration = 6,
                    percentOfTotal = 0.28,
                    criticalPath = false,
                    tasks = listOf(
                        "Vanity and countertop installation",
                        "Plumbing fixture installation",
                        "Mirror and medicine cabinet installation",
                        "Electrical fixture installation",
                        "Hardware and accessory installation",
                        "Final caulking and touch-up"
                    ),
                    trades = listOf("cabinet_installer", "countertop_installer", "plumber", "electrician", "glazier"),
                    inspections = listOf("Final plumbing", "Final electrical"),
                    materialPercentage = 0.45
                ),
                
                ConstructionPhase(
                    phaseName = "Final Details",
                    duration = 2,
                    percentOfTotal = 0.05,
                    criticalPath = false,
                    tasks = listOf(
                        "Final cleaning",
                        "Touch-up work",
                        "Ventilation and fixture testing",
                        "Client walkthrough",
                        "Punch list completion"
                    ),
                    trades = listOf("general_laborer", "general_contractor"),
                    inspections = listOf("Final inspection"),
                    materialPercentage = 0.02
                )
            ),
            typicalCostPerSF = 425.0, // Bathroom renovation cost per SF
            costBreakdown = CostBreakdown(
                description = "Master Bathroom Renovation",
                quantity = 120.0,
                unit = "square foot",
                laborHours = 0.0,
                laborRate = 0.0,
                laborCost = 22000.0,
                materialCost = 28500.0,
                equipmentCost = 1500.0,
                subtotal = 52000.0,
                overhead = 7800.0, // 15%
                profit = 6240.0, // 12%
                totalCost = 66040.0,
                costPerUnit = 550.33,
                tradeType = "Multiple",
                region = "National Average"
            ),
            qualityOptions = mapOf(
                "budget_bathroom" to QualityOption(
                    multiplier = 0.60,
                    description = "Budget bathroom with basic fixtures",
                    typicalFeatures = listOf(
                        "Fiberglass tub/shower unit",
                        "Laminate vanity top",
                        "Ceramic tile flooring",
                        "Standard fixtures",
                        "Basic lighting"
                    )
                ),
                "standard_bathroom" to QualityOption(
                    multiplier = 1.0,
                    description = "Quality bathroom renovation",
                    typicalFeatures = listOf(
                        "Tile shower with glass door",
                        "Granite or quartz vanity top",
                        "Porcelain tile flooring",
                        "Mid-range fixtures",
                        "Vanity and mirror lighting"
                    )
                ),
                "luxury_bathroom" to QualityOption(
                    multiplier = 1.95,
                    description = "Spa-like luxury bathroom",
                    typicalFeatures = listOf(
                        "Custom tile shower with multiple heads",
                        "Natural stone countertops and flooring",
                        "Custom vanity with luxury hardware",
                        "High-end fixtures and fittings",
                        "Heated floors and towel warmers",
                        "Custom lighting and mirrors",
                        "Steam shower or soaking tub"
                    )
                )
            ),
            regionalFactors = mapOf(
                "high_cost_markets" to 1.35,
                "standard_markets" to 1.0,
                "low_cost_markets" to 0.85
            ),
            specialRequirements = listOf(
                "Plumbing permit for fixture changes",
                "Electrical permit for new circuits",
                "Ventilation fan on timer or humidity sensor",
                "GFCI protection required",
                "Waterproofing critical in shower areas",
                "Lead paint considerations in older homes"
            ),
            commonAddOns = mapOf(
                "heated_flooring" to 2200.0,
                "steam_shower" to 3500.0,
                "soaking_tub" to 2800.0,
                "double_vanity" to 1500.0,
                "linen_closet" to 1200.0,
                "skylight" to 1800.0,
                "exhaust_fan_upgrade" to 450.0
            ),
            riskFactors = mapOf(
                "plumbing_surprises" to 0.30, // High chance in bathrooms
                "structural_issues" to 0.15,
                "mold_water_damage" to 0.25,
                "permit_delays" to 0.15,
                "material_lead_times" to 0.35
            ),
            seasonalConsiderations = mapOf(
                "best_start_months" to listOf("March", "April", "September", "October"),
                "avoid_months" to listOf("December"), // Holiday disruption
                "weather_dependent_phases" to listOf("None - interior work")
            ),
            source = "NKBA Bathroom Guidelines 2024, Remodeling Magazine Cost vs Value"
        ),

        "home_addition_family_room" to ProjectTemplate(
            templateId = "home_addition_family_room",
            name = "Family Room Addition 400 SF",
            category = "Addition",
            description = "Single-story family room addition with bathroom",
            typicalSquareFootage = 400.0,
            typicalDuration = 45, // Calendar days
            phases = listOf(
                ConstructionPhase(
                    phaseName = "Site Work and Foundation",
                    duration = 8,
                    percentOfTotal = 0.18,
                    criticalPath = true,
                    tasks = listOf(
                        "Site layout and excavation",
                        "Foundation footings",
                        "Foundation walls or stem walls",
                        "Foundation waterproofing",
                        "Backfill and compaction",
                        "Utility rough-ins"
                    ),
                    trades = listOf("excavation", "concrete_finisher", "waterproofing"),
                    inspections = listOf("Footing inspection", "Foundation inspection"),
                    materialPercentage = 0.12
                ),
                
                ConstructionPhase(
                    phaseName = "Framing and Envelope",
                    duration = 12,
                    percentOfTotal = 0.22,
                    criticalPath = true,
                    tasks = listOf(
                        "Floor framing tied to existing",
                        "Wall framing",
                        "Roof framing and tie-in to existing",
                        "Exterior sheathing",
                        "Roofing installation and flashing",
                        "Windows and exterior door",
                        "Siding to match existing"
                    ),
                    trades = listOf("carpenter_framing", "roofer", "siding_installer"),
                    inspections = listOf("Framing inspection"),
                    materialPercentage = 0.20
                ),
                
                ConstructionPhase(
                    phaseName = "Mechanical, Electrical, Plumbing",
                    duration = 10,
                    percentOfTotal = 0.20,
                    criticalPath = true,
                    tasks = listOf(
                        "Electrical service upgrade if needed",
                        "Electrical rough-in",
                        "Plumbing rough-in for bathroom",
                        "HVAC system extension",
                        "Insulation installation",
                        "MEP rough inspections"
                    ),
                    trades = listOf("electrician", "plumber", "hvac_technician", "insulator"),
                    inspections = listOf("Electrical rough", "Plumbing rough", "Mechanical rough"),
                    materialPercentage = 0.18
                ),
                
                ConstructionPhase(
                    phaseName = "Interior Finishes",
                    duration = 18,
                    percentOfTotal = 0.35,
                    criticalPath = false,
                    tasks = listOf(
                        "Drywall installation and finishing",
                        "Interior painting",
                        "Flooring installation to match existing",
                        "Bathroom tile work",
                        "Interior trim and doors",
                        "Electrical and plumbing fixture installation"
                    ),
                    trades = listOf("drywall_installer", "painter", "flooring_installer", "tile_installer", "trim_carpenter"),
                    inspections = listOf("Insulation inspection", "Final MEP"),
                    materialPercentage = 0.25
                ),
                
                ConstructionPhase(
                    phaseName = "Final and Exterior",
                    duration = 7,
                    percentOfTotal = 0.12,
                    criticalPath = false,
                    tasks = listOf(
                        "Exterior painting or staining",
                        "Landscaping restoration",
                        "Concrete work (patio, walkways)",
                        "Final inspections",
                        "Punch list completion",
                        "Final cleaning"
                    ),
                    trades = listOf("painter", "landscaper", "concrete_finisher", "general_laborer"),
                    inspections = listOf("Final building inspection"),
                    materialPercentage = 0.08
                )
            ),
            typicalCostPerSF = 185.0, // Addition cost per SF including bathroom
            costBreakdown = CostBreakdown(
                description = "Family Room Addition with Bathroom",
                quantity = 400.0,
                unit = "square foot",
                laborHours = 0.0,
                laborRate = 0.0,
                laborCost = 35000.0,
                materialCost = 42000.0,
                equipmentCost = 5000.0,
                subtotal = 82000.0,
                overhead = 12300.0, // 15%
                profit = 9840.0, // 12%
                totalCost = 104140.0,
                costPerUnit = 260.35,
                tradeType = "Multiple", 
                region = "National Average"
            ),
            qualityOptions = mapOf(
                "basic_addition" to QualityOption(
                    multiplier = 0.80,
                    description = "Basic addition with standard finishes",
                    typicalFeatures = listOf(
                        "Standard windows and doors",
                        "Basic trim package",
                        "Carpet or vinyl flooring",
                        "Standard bathroom fixtures",
                        "Basic electrical and lighting"
                    )
                ),
                "standard_addition" to QualityOption(
                    multiplier = 1.0,
                    description = "Quality addition matching existing home",
                    typicalFeatures = listOf(
                        "Quality windows matching existing",
                        "Hardwood flooring to match",
                        "Crown molding and trim package",
                        "Mid-range bathroom finishes",
                        "Recessed lighting and electrical"
                    )
                ),
                "premium_addition" to QualityOption(
                    multiplier = 1.45,
                    description = "High-end addition with premium features",
                    typicalFeatures = listOf(
                        "Premium windows with advanced glazing",
                        "Exotic hardwood or stone flooring",
                        "Custom millwork and built-ins",
                        "Luxury bathroom with premium fixtures",
                        "Smart home integration",
                        "Cathedral ceilings with beams"
                    )
                )
            ),
            regionalFactors = mapOf(
                "high_cost_markets" to 1.25,
                "standard_markets" to 1.0,
                "low_cost_markets" to 0.85
            ),
            specialRequirements = listOf(
                "Building permit required",
                "Structural engineer for roof tie-in",
                "Electrical service capacity analysis",
                "HVAC load calculations",
                "Setback and easement verification",
                "Drainage and grading considerations"
            ),
            commonAddOns = mapOf(
                "covered_patio" to 6500.0,
                "fireplace" to 4500.0,
                "built_in_entertainment_center" to 3200.0,
                "coffered_ceiling" to 2800.0,
                "french_doors" to 2200.0,
                "upgraded_windows" to 3500.0
            ),
            riskFactors = mapOf(
                "foundation_matching" to 0.20,
                "roof_integration" to 0.25,
                "utility_capacity" to 0.15,
                "permit_complications" to 0.18,
                "weather_delays" to 0.30,
                "existing_structure_surprises" to 0.22
            ),
            seasonalConsiderations = mapOf(
                "best_start_months" to listOf("April", "May", "September"),
                "avoid_months" to listOf("December", "January"), // Weather and holidays
                "weather_dependent_phases" to listOf("Foundation", "Framing", "Roofing", "Exterior")
            ),
            source = "NAHB Addition Guidelines, Cost vs Value Report 2024"
        )
    )

    /**
     * Commercial Project Templates
     */
    fun getCommercialProjectTemplates(): Map<String, ProjectTemplate> = mapOf(
        
        "office_tenant_improvement" to ProjectTemplate(
            templateId = "office_tenant_improvement",
            name = "Office Tenant Improvement 5,000 SF",
            category = "Commercial TI",
            description = "Office build-out in existing commercial shell",
            typicalSquareFootage = 5000.0,
            typicalDuration = 60,
            phases = listOf(
                ConstructionPhase(
                    phaseName = "Design and Permitting",
                    duration = 14,
                    percentOfTotal = 0.10,
                    criticalPath = true,
                    tasks = listOf(
                        "Space planning and design",
                        "Permit applications",
                        "MEP engineering",
                        "Landlord approvals",
                        "Material selections"
                    ),
                    trades = listOf("architect", "engineer"),
                    inspections = emptyList(),
                    materialPercentage = 0.02
                ),
                
                ConstructionPhase(
                    phaseName = "Infrastructure and MEP",
                    duration = 18,
                    percentOfTotal = 0.35,
                    criticalPath = true,
                    tasks = listOf(
                        "Demolition of existing improvements",
                        "New partition framing",
                        "Electrical distribution and circuits",
                        "HVAC modifications and extensions",
                        "Plumbing for break rooms and restrooms",
                        "Fire sprinkler modifications",
                        "Low voltage and data cabling"
                    ),
                    trades = listOf("general_laborer", "carpenter_commercial", "electrician", "hvac_technician", "plumber", "fire_sprinkler", "low_voltage_technician"),
                    inspections = listOf("Rough MEP inspections"),
                    materialPercentage = 0.25
                ),
                
                ConstructionPhase(
                    phaseName = "Finishes",
                    duration = 20,
                    percentOfTotal = 0.35,
                    criticalPath = false,
                    tasks = listOf(
                        "Drywall and acoustical ceilings",
                        "Commercial flooring installation",
                        "Interior painting",
                        "Glass partition installation",
                        "Door hardware and locksets",
                        "Window treatments"
                    ),
                    trades = listOf("drywall_installer", "acoustical_contractor", "commercial_flooring", "painter", "glazier", "locksmith"),
                    inspections = listOf("Fire safety inspection"),
                    materialPercentage = 0.30
                )
            ),
            typicalCostPerSF = 85.0,
            costBreakdown = CostBreakdown(
                description = "Office Tenant Improvement",
                quantity = 5000.0,
                unit = "square foot",
                laborHours = 0.0,
                laborRate = 0.0,
                laborCost = 180000.0,
                materialCost = 220000.0,
                equipmentCost = 15000.0,
                subtotal = 415000.0,
                overhead = 62250.0, // 15%
                profit = 49800.0, // 12%
                totalCost = 527050.0,
                costPerUnit = 105.41,
                tradeType = "Multiple",
                region = "National Average"
            ),
            qualityOptions = mapOf(
                "basic_office" to QualityOption(
                    multiplier = 0.75,
                    description = "Basic office build-out",
                    typicalFeatures = listOf("VCT flooring", "Standard ACT ceiling", "Basic lighting", "Painted walls")
                ),
                "standard_office" to QualityOption(
                    multiplier = 1.0,
                    description = "Professional office environment", 
                    typicalFeatures = listOf("Carpet and LVT", "Acoustical ceiling tiles", "LED lighting", "Glass partitions")
                ),
                "executive_office" to QualityOption(
                    multiplier = 1.6,
                    description = "High-end executive office space",
                    typicalFeatures = listOf("Hardwood and stone", "Custom millwork", "Designer lighting", "High-end finishes")
                )
            ),
            regionalFactors = mapOf(
                "high_cost_markets" to 1.30,
                "standard_markets" to 1.0,
                "low_cost_markets" to 0.80
            ),
            specialRequirements = listOf(
                "Tenant improvement permit",
                "ADA compliance review",
                "Fire and life safety compliance",
                "Building standard compliance",
                "HVAC load calculations",
                "Electrical load analysis"
            ),
            commonAddOns = mapOf(
                "conference_room_av" to 8500.0,
                "kitchen_break_room" to 12000.0,
                "server_room" to 15000.0,
                "security_system" to 5500.0,
                "access_control" to 7500.0,
                "custom_millwork" to 18000.0
            ),
            riskFactors = mapOf(
                "landlord_delays" to 0.25,
                "permit_complications" to 0.20,
                "existing_condition_surprises" to 0.30,
                "change_orders" to 0.40, // High change order rate in TI work
                "schedule_compression" to 0.35
            ),
            seasonalConsiderations = mapOf(
                "best_start_months" to listOf("Any month - interior work"),
                "avoid_months" to emptyList(),
                "weather_dependent_phases" to emptyList()
            ),
            source = "BOMA Commercial Construction Guidelines"
        )
    )
}

/**
 * Supporting data classes for project templates
 */
data class ProjectTemplate(
    val templateId: String,
    val name: String,
    val category: String,
    val description: String,
    val typicalSquareFootage: Double,
    val typicalDuration: Int, // Calendar days
    val phases: List<ConstructionPhase>,
    val typicalCostPerSF: Double,
    val costBreakdown: CostBreakdown,
    val qualityOptions: Map<String, QualityOption>,
    val regionalFactors: Map<String, Double>,
    val specialRequirements: List<String>,
    val commonAddOns: Map<String, Double>,
    val riskFactors: Map<String, Double>, // Probability of issues
    val seasonalConsiderations: Map<String, List<String>>,
    val source: String,
    val lastUpdated: Date = Date(),
    val minimumProject: Double = 0.0, // Minimum project size
    val maximumProject: Double = Double.MAX_VALUE, // Maximum recommended size
    val crewSizeRecommendation: String = "Standard construction crew",
    val specialtyTradesRequired: List<String> = emptyList()
)

data class ConstructionPhase(
    val phaseName: String,
    val duration: Int, // Calendar days
    val percentOfTotal: Double, // Percentage of total project value
    val criticalPath: Boolean,
    val tasks: List<String>,
    val trades: List<String>,
    val inspections: List<String>,
    val materialPercentage: Double, // Percentage of total materials used in this phase
    val prerequisites: List<String> = emptyList(),
    val weatherSensitive: Boolean = false,
    val longLeadItems: List<String> = emptyList()
)

data class QualityOption(
    val multiplier: Double,
    val description: String,
    val typicalFeatures: List<String>,
    val additionalCosts: Map<String, Double> = emptyMap()
)