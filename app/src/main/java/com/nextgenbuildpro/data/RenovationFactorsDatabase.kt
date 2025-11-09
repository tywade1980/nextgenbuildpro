package com.nextgenbuildpro.data

import com.nextgenbuildpro.pm.data.model.*
import java.util.*

/**
 * Comprehensive Renovation and Specialty Factors Database
 * 
 * Sources: National Association of Home Builders (NAHB), Remodeling Magazine Cost vs. Value,
 * Joint Center for Housing Studies Harvard, RSMeans Renovation Cost Data,
 * National Kitchen and Bath Association (NKBA), Historic Preservation Standards
 * 
 * This database provides detailed multipliers and factors for renovation work,
 * which is typically 1.3-2.5x more expensive than new construction due to:
 * - Existing condition assessment and surprises
 * - Limited access and workspace constraints  
 * - Integration with existing systems
 * - Code upgrade requirements
 * - Material matching and custom work
 * - Disposal and hazardous material handling
 */
object RenovationFactorsDatabase {

    /**
     * Project Type Renovation Multipliers
     * Based on NAHB Remodeling Cost Study 2024 and industry experience
     */
    fun getProjectRenovationFactors(): Map<String, ProjectRenovationFactor> = mapOf(
        
        "kitchen_renovation" to ProjectRenovationFactor(
            projectType = "Kitchen Renovation",
            baseMultiplier = 1.45,
            complexityFactors = mapOf(
                "minor_refresh" to 1.15, // Paint, hardware, countertops
                "moderate_remodel" to 1.45, // New cabinets, appliances, flooring
                "major_renovation" to 1.85, // Layout changes, structural work
                "luxury_remodel" to 2.25 // High-end everything, complex layouts
            ),
            ageFactors = mapOf(
                "0-10_years" to 1.0,
                "11-20_years" to 1.15,
                "21-30_years" to 1.35,
                "31-50_years" to 1.65,
                "over_50_years" to 2.1
            ),
            discoveryFactors = mapOf(
                "plumbing_surprises" to 1.25, // Old galvanized pipes, poor routing
                "electrical_upgrades" to 1.35, // Code compliance, new circuits
                "structural_issues" to 1.8, // Sagging floors, load bearing changes
                "asbestos_lead" to 1.6, // Hazardous material abatement
                "water_damage" to 1.4 // Hidden damage from leaks
            ),
            tradeSpecificFactors = mapOf(
                "demolition" to 1.6, // Careful demo, disposal, dust control
                "plumbing" to 1.7, // Working around existing, code upgrades
                "electrical" to 1.5, // New circuits, GFCI upgrades
                "drywall" to 1.8, // Patching, matching textures
                "flooring" to 1.4, // Removal, subfloor issues
                "painting" to 1.3, // Prep work, multiple coats
                "cabinets" to 1.2, // Fitting to existing space
                "countertops" to 1.3 // Template work, field measurements
            ),
            description = "Full kitchen renovation with typical complexities",
            source = "NKBA Kitchen Remodeling Study 2024, RSMeans Renovation Data"
        ),

        "bathroom_renovation" to ProjectRenovationFactor(
            projectType = "Bathroom Renovation",
            baseMultiplier = 1.65,
            complexityFactors = mapOf(
                "cosmetic_update" to 1.25, // New fixtures, tile, paint
                "standard_remodel" to 1.65, // New layout, plumbing moves
                "luxury_renovation" to 2.1, // Steam showers, heated floors
                "accessibility_conversion" to 1.9 // ADA compliance, safety features
            ),
            ageFactors = mapOf(
                "0-10_years" to 1.0,
                "11-20_years" to 1.2,
                "21-30_years" to 1.45,
                "31-50_years" to 1.8,
                "over_50_years" to 2.3
            ),
            discoveryFactors = mapOf(
                "plumbing_rerouting" to 1.6, // Moving fixtures, new rough-in
                "ventilation_upgrades" to 1.4, // Exhaust fan, humidity control
                "waterproofing_issues" to 1.5, // Shower pan, membrane repairs
                "structural_reinforcement" to 1.7, // Heavy tile, tub support
                "mold_remediation" to 1.8 // Water damage, mold treatment
            ),
            tradeSpecificFactors = mapOf(
                "demolition" to 1.8, // Careful tile removal, disposal
                "plumbing" to 2.0, // Complex rough-in, fixture installation
                "electrical" to 1.6, // GFCI, ventilation, lighting
                "tile_work" to 1.6, // Waterproofing, precise cuts
                "drywall" to 1.9, // Moisture resistance, perfect finish
                "painting" to 1.4, // Moisture-resistant primers
                "flooring" to 1.5, // Waterproofing, transitions
                "fixtures" to 1.3 // Precise installation, testing
            ),
            description = "Complete bathroom renovation with plumbing and electrical updates",
            source = "NKBA Bathroom Remodeling Trends 2024"
        ),

        "basement_finishing" to ProjectRenovationFactor(
            projectType = "Basement Finishing",
            baseMultiplier = 1.55,
            complexityFactors = mapOf(
                "basic_finish" to 1.35, // Drywall, basic flooring, lighting
                "recreation_room" to 1.55, // Full finish, entertainment features
                "apartment_conversion" to 1.85, // Kitchen, bathroom, separate entrance
                "walkout_basement" to 1.25 // Natural light, easier access
            ),
            ageFactors = mapOf(
                "0-10_years" to 1.0,
                "11-20_years" to 1.15,
                "21-30_years" to 1.3,
                "31-50_years" to 1.5,
                "over_50_years" to 1.8
            ),
            discoveryFactors = mapOf(
                "moisture_control" to 1.7, // Waterproofing, drainage
                "ceiling_height" to 1.4, // Low ceilings, duct work
                "foundation_repairs" to 1.9, // Crack repair, stabilization
                "radon_mitigation" to 1.3, // Radon system installation
                "code_compliance" to 1.6 // Egress windows, smoke detectors
            ),
            tradeSpecificFactors = mapOf(
                "excavation" to 1.8, // Egress window wells
                "waterproofing" to 1.9, // Interior/exterior moisture control
                "framing" to 1.4, // Furring strips, drop ceilings
                "electrical" to 1.5, // New circuits, code compliance
                "plumbing" to 1.7, // Rough-in for bathroom
                "insulation" to 1.6, // Moisture control, thermal barriers
                "drywall" to 1.5, // Moisture-resistant, careful finishing
                "flooring" to 1.6, // Moisture barriers, transitions
                "hvac" to 1.8 // Ductwork extensions, humidity control
            ),
            description = "Converting unfinished basement to livable space",
            source = "NAHB Basement Finishing Guidelines 2024"
        ),

        "historic_restoration" to ProjectRenovationFactor(
            projectType = "Historic Restoration",
            baseMultiplier = 2.45,
            complexityFactors = mapOf(
                "maintenance_restoration" to 1.8, // Preserve existing features
                "rehabilitation" to 2.45, // Adaptive reuse, code compliance
                "reconstruction" to 2.8, // Recreate missing elements
                "preservation" to 3.2 // Museum-quality restoration
            ),
            ageFactors = mapOf(
                "50-75_years" to 1.8,
                "75-100_years" to 2.2,
                "100-150_years" to 2.6,
                "over_150_years" to 3.1
            ),
            discoveryFactors = mapOf(
                "structural_assessment" to 2.1, // Engineering analysis required
                "hazardous_materials" to 2.5, // Lead, asbestos, other toxins
                "code_variances" to 1.9, // Historic exemptions, alternatives
                "material_sourcing" to 2.8, // Period-correct materials
                "specialty_trades" to 3.0, // Craftspeople with historic skills
                "documentation" to 1.6, // Photos, drawings, approvals
                "archaeology" to 2.2 // Site surveys, artifact preservation
            ),
            tradeSpecificFactors = mapOf(
                "masonry_restoration" to 3.5, // Matching mortars, stone carving
                "timber_framing" to 3.0, // Traditional joinery techniques
                "plastering" to 2.8, // Horsehair plaster, lime mortars
                "roofing" to 2.6, // Slate, clay tile, metal restoration
                "windows" to 3.2, // Restoration vs replacement decisions
                "millwork" to 3.8, // Custom reproduction of details
                "painting" to 2.4, // Paint analysis, period colors
                "mechanical_systems" to 2.0 // Discrete modern systems
            ),
            description = "Historic building restoration following preservation standards",
            source = "National Park Service Preservation Briefs, ACHP Guidelines"
        ),

        "home_addition" to ProjectRenovationFactor(
            projectType = "Home Addition",
            baseMultiplier = 1.35,
            complexityFactors = mapOf(
                "bump_out" to 1.15, // Small extension, simple connection
                "room_addition" to 1.35, // New foundation, roof tie-in
                "second_story" to 1.65, // Structural reinforcement required
                "whole_house_renovation" to 1.85 // Multiple systems integration
            ),
            ageFactors = mapOf(
                "0-10_years" to 1.0,
                "11-20_years" to 1.1,
                "21-30_years" to 1.25,
                "31-50_years" to 1.4,
                "over_50_years" to 1.7
            ),
            discoveryFactors = mapOf(
                "foundation_matching" to 1.4, // Connecting to existing foundation
                "roof_integration" to 1.6, // Complex roof lines, flashing
                "utility_extensions" to 1.3, // HVAC, electrical, plumbing
                "structural_reinforcement" to 1.8, // Existing structure upgrades
                "code_compliance" to 1.5, // Bringing existing up to code
                "site_conditions" to 1.3 // Drainage, setbacks, easements
            ),
            tradeSpecificFactors = mapOf(
                "foundation" to 1.4, // Connecting new to old
                "framing" to 1.3, // Tying into existing structure
                "roofing" to 1.7, // Complex integration, flashing
                "siding" to 1.5, // Matching existing materials
                "electrical" to 1.4, // Service upgrades, new circuits
                "plumbing" to 1.5, // Extending systems
                "hvac" to 1.6, // System capacity, ductwork
                "insulation" to 1.2, // Thermal bridging concerns
                "drywall" to 1.4, // Tying into existing
                "flooring" to 1.3 // Level transitions, matching
            ),
            description = "Home addition requiring integration with existing structure",
            source = "NAHB Addition Cost Analysis 2024"
        ),

        "commercial_renovation" to ProjectRenovationFactor(
            projectType = "Commercial Renovation",
            baseMultiplier = 1.75,
            complexityFactors = mapOf(
                "tenant_improvement" to 1.45, // Within existing shell
                "major_renovation" to 1.75, // Structural, MEP upgrades
                "historic_commercial" to 2.3, // Preservation requirements
                "occupied_renovation" to 2.1 // Working around tenants
            ),
            ageFactors = mapOf(
                "0-10_years" to 1.0,
                "11-25_years" to 1.2,
                "26-50_years" to 1.5,
                "over_50_years" to 1.9
            ),
            discoveryFactors = mapOf(
                "code_upgrades" to 1.8, // ADA, fire safety, structural
                "asbestos_lead" to 2.0, // Hazmat abatement
                "structural_deficiencies" to 2.2, // Seismic, load capacity
                "utility_capacity" to 1.6, // Electrical, HVAC upgrades
                "environmental_issues" to 1.9 // Contamination, remediation
            ),
            tradeSpecificFactors = mapOf(
                "demolition" to 1.8, // Selective demo, protection
                "structural" to 2.0, // Reinforcement, modifications
                "mechanical" to 1.9, // Complex systems, coordination
                "electrical" to 1.7, // High-voltage, life safety
                "plumbing" to 1.6, // Commercial fixtures, backflow
                "fire_protection" to 1.8, // Sprinklers, alarms
                "elevators" to 2.5, // Modernization, code compliance
                "roofing" to 1.5, // Membrane systems, equipment
                "glazing" to 1.9 // Curtain wall, storefront systems
            ),
            description = "Commercial building renovation with code upgrades",
            source = "Building Owners and Managers Association Cost Study 2024"
        )
    )

    /**
     * Accessibility and Site Condition Multipliers
     * Based on site survey data and industry experience
     */
    fun getAccessibilityFactors(): Map<String, AccessibilityFactor> = mapOf(
        
        "site_access" to AccessibilityFactor(
            category = "Site Access",
            factors = mapOf(
                "street_level" to 1.0, // Easy truck and equipment access
                "hillside_moderate" to 1.25, // Some slope, limited access
                "hillside_steep" to 1.6, // Steep grades, special equipment
                "remote_location" to 1.4, // Long material hauls
                "urban_restricted" to 1.3, // Parking restrictions, permits
                "narrow_lot" to 1.35, // Limited staging area
                "no_vehicle_access" to 1.8 // Hand-carry only
            ),
            description = "Site access affects material delivery and equipment use"
        ),

        "working_space" to AccessibilityFactor(
            category = "Working Space",
            factors = mapOf(
                "open_access" to 1.0, // Plenty of room to work
                "tight_spaces" to 1.3, // Cramped conditions
                "crawl_space_work" to 1.7, // Under house, tight access
                "attic_work" to 1.5, // Limited headroom
                "confined_spaces" to 1.9, // Safety requirements, ventilation
                "occupied_spaces" to 1.4, // Working around occupants
                "night_work" to 1.25, // After-hours premium
                "weekend_work" to 1.15 // Weekend rates
            ),
            description = "Working space constraints affect productivity"
        ),

        "utility_accessibility" to AccessibilityFactor(
            category = "Utility Access",
            factors = mapOf(
                "easy_shutoff" to 1.0, // Accessible shutoffs, clear routing
                "difficult_access" to 1.4, // Hard to reach utilities
                "no_shutoffs" to 1.6, // Must install new shutoffs
                "live_work" to 1.8, // Working around live systems
                "underground_utilities" to 1.5, // Locating, hand digging
                "overhead_lines" to 1.3, // Safety clearances
                "shared_utilities" to 1.7 // Multi-unit building complexities
            ),
            description = "Utility access affects safety and efficiency"
        ),

        "environmental_conditions" to AccessibilityFactor(
            category = "Environmental Conditions",
            factors = mapOf(
                "standard_conditions" to 1.0, // Normal indoor/outdoor work
                "extreme_heat" to 1.2, // Over 90°F, safety breaks
                "extreme_cold" to 1.3, // Below 32°F, material issues
                "high_humidity" to 1.15, // Drying time, comfort
                "dust_control" to 1.25, // Containment, protection
                "noise_restrictions" to 1.2, // Limited hours, equipment
                "contaminated_site" to 1.9, // Hazmat suits, disposal
                "lead_paint_present" to 1.6, // EPA RRP compliance
                "asbestos_present" to 2.1 // Licensed abatement required
            ),
            description = "Environmental conditions affecting worker safety and productivity"
        )
    )

    /**
     * Quality Grade Definitions and Multipliers
     * Based on construction industry standards and client expectations
     */
    fun getQualityGradeFactors(): Map<String, QualityGradeFactor> = mapOf(
        
        "residential_construction" to QualityGradeFactor(
            category = "Residential Construction",
            grades = mapOf(
                "budget" to QualityGrade(
                    multiplier = 0.70,
                    description = "Budget-grade materials and finishes",
                    characteristics = listOf(
                        "Builder-grade materials",
                        "Standard fixtures and hardware",
                        "Basic installation techniques",
                        "Limited warranty coverage",
                        "Code-minimum requirements"
                    ),
                    typicalUse = "Entry-level homes, rental properties"
                ),
                "standard" to QualityGrade(
                    multiplier = 1.0,
                    description = "Standard residential grade",
                    characteristics = listOf(
                        "Mid-grade materials",
                        "Good quality fixtures",
                        "Professional installation",
                        "Standard warranties",
                        "Code-compliant plus"
                    ),
                    typicalUse = "Typical family homes, spec homes"
                ),
                "premium" to QualityGrade(
                    multiplier = 1.45,
                    description = "Premium residential grade",
                    characteristics = listOf(
                        "High-quality materials",
                        "Designer fixtures and finishes",
                        "Skilled craftsman installation",
                        "Extended warranties",
                        "Energy efficiency upgrades"
                    ),
                    typicalUse = "Move-up homes, custom builds"
                ),
                "luxury" to QualityGrade(
                    multiplier = 2.1,
                    description = "Luxury custom grade",
                    characteristics = listOf(
                        "Exotic and premium materials",
                        "Custom and designer everything",
                        "Master craftsman level work",
                        "Comprehensive warranties",
                        "Smart home integration"
                    ),
                    typicalUse = "Luxury homes, high-end custom"
                )
            ),
            source = "NAHB Quality Standards 2024"
        ),

        "commercial_construction" to QualityGradeFactor(
            category = "Commercial Construction",
            grades = mapOf(
                "basic" to QualityGrade(
                    multiplier = 0.85,
                    description = "Basic commercial grade",
                    characteristics = listOf(
                        "Standard commercial materials",
                        "Functional design focus",
                        "Code-minimum compliance",
                        "Basic finishes",
                        "Standard warranties"
                    ),
                    typicalUse = "Warehouses, basic office"
                ),
                "standard" to QualityGrade(
                    multiplier = 1.0,
                    description = "Standard commercial grade",
                    characteristics = listOf(
                        "Quality commercial materials",
                        "Good design and function",
                        "Code-plus specifications",
                        "Professional finishes",
                        "Standard commercial warranties"
                    ),
                    typicalUse = "Office buildings, retail"
                ),
                "premium" to QualityGrade(
                    multiplier = 1.6,
                    description = "Premium commercial grade",
                    characteristics = listOf(
                        "High-end commercial materials",
                        "Architectural design features",
                        "Enhanced specifications",
                        "Premium finishes",
                        "Extended warranties"
                    ),
                    typicalUse = "Class A office, upscale retail"
                ),
                "landmark" to QualityGrade(
                    multiplier = 2.4,
                    description = "Landmark commercial grade",
                    characteristics = listOf(
                        "Signature architectural materials",
                        "Custom design elements",
                        "Highest specifications",
                        "Luxury finishes",
                        "Comprehensive guarantees"
                    ),
                    typicalUse = "Corporate headquarters, iconic buildings"
                )
            ),
            source = "Construction Specifications Institute Standards"
        )
    )

    /**
     * Seasonal Construction Factors
     * Weather impacts on productivity and costs
     */
    fun getSeasonalFactors(): Map<String, SeasonalFactor> = mapOf(
        
        "winter_construction" to SeasonalFactor(
            season = "Winter (Dec-Feb)",
            temperatureRange = "Below 40°F",
            factors = mapOf(
                "concrete_work" to 1.4, // Heating, additives, curing blankets
                "masonry" to 1.5, // Cold weather mortar, heating
                "roofing" to 1.6, // Safety, material handling
                "exterior_painting" to 1.8, // Temperature restrictions
                "excavation" to 1.3, // Frozen ground, equipment warm-up
                "general_productivity" to 1.2, // Shorter days, weather delays
                "material_costs" to 1.1, // Heating fuel, storage
                "equipment_costs" to 1.3 // Winterization, longer warm-up
            ),
            challenges = listOf(
                "Shortened daylight hours",
                "Weather delays and stoppages",
                "Material protection from moisture",
                "Equipment winterization required",
                "Concrete cold weather procedures",
                "Site access and safety issues",
                "Higher heating and energy costs"
            ),
            advantages = listOf(
                "Lower labor demand, better availability",
                "Material suppliers offer discounts",
                "Less construction competition",
                "Indoor work not significantly affected"
            )
        ),

        "spring_construction" to SeasonalFactor(
            season = "Spring (Mar-May)",
            temperatureRange = "40-70°F",
            factors = mapOf(
                "concrete_work" to 0.95, // Optimal curing conditions
                "masonry" to 0.98, // Good working conditions
                "roofing" to 1.0, // Standard conditions
                "exterior_painting" to 0.92, // Ideal temperature and humidity
                "excavation" to 1.1, // Wet conditions, mud season
                "general_productivity" to 0.98, // Good working weather
                "material_costs" to 1.05, // Spring price increases
                "equipment_costs" to 1.0 // Standard rates
            ),
            challenges = listOf(
                "Mud season and wet conditions",
                "Spring rains cause delays",
                "Material price increases",
                "Labor shortage begins"
            ),
            advantages = listOf(
                "Optimal working conditions",
                "Good concrete curing weather",
                "Longer daylight hours",
                "Material supplier stock replenished"
            )
        ),

        "summer_construction" to SeasonalFactor(
            season = "Summer (Jun-Aug)",
            temperatureRange = "Above 70°F",
            factors = mapOf(
                "concrete_work" to 1.15, // Rapid curing, cracking risk
                "masonry" to 1.1, // Heat stress, more water needed
                "roofing" to 0.9, // Good weather, but heat stress
                "exterior_painting" to 1.2, // High heat, fast drying issues
                "excavation" to 0.95, // Dry conditions, dust control
                "general_productivity" to 1.05, // Heat stress, more breaks
                "material_costs" to 1.15, // Peak season pricing
                "equipment_costs" to 1.0 // Standard rates
            ),
            challenges = listOf(
                "Extreme heat affects workers",
                "Concrete rapid curing issues",
                "High demand, labor shortage",
                "Peak material pricing",
                "Vacation schedules disrupt crews"
            ),
            advantages = listOf(
                "Dry conditions for most work",
                "Long daylight hours",
                "Good weather for exterior work",
                "Optimal for roofing and siding"
            )
        ),

        "fall_construction" to SeasonalFactor(
            season = "Fall (Sep-Nov)",
            temperatureRange = "40-70°F",
            factors = mapOf(
                "concrete_work" to 1.0, // Good conditions
                "masonry" to 1.0, // Ideal working weather
                "roofing" to 1.05, // Rush to beat winter
                "exterior_painting" to 0.98, // Good conditions
                "excavation" to 1.0, // Dry, stable conditions
                "general_productivity" to 1.0, // Comfortable working weather
                "material_costs" to 0.98, // Post-summer normalization
                "equipment_costs" to 1.0 // Standard rates
            ),
            challenges = listOf(
                "Shorter days approaching",
                "Rush to complete exterior work",
                "Weather becoming unpredictable",
                "Material suppliers reducing stock"
            ),
            advantages = listOf(
                "Ideal working conditions",
                "Good concrete and masonry weather",
                "Lower material costs",
                "Labor availability improving"
            )
        )
    )

    /**
     * Union vs Non-Union Labor Factors
     * Regional variations in union presence and rates
     */
    fun getUnionFactors(): Map<String, UnionFactor> = mapOf(
        
        "heavy_union_market" to UnionFactor(
            marketType = "Heavy Union Market",
            unionPresence = 0.75, // 75% union workforce
            regions = listOf("New York", "Chicago", "San Francisco", "Boston", "Detroit"),
            factors = mapOf(
                "wages" to 1.35,
                "benefits" to 1.65,
                "productivity" to 1.1, // Higher skill level
                "work_rules" to 1.2, // Jurisdiction restrictions
                "apprenticeship" to 1.0, // Structured training
                "quality" to 1.15 // Generally higher quality
            ),
            advantages = listOf(
                "Highly skilled workforce",
                "Standardized training programs",
                "Better safety records",
                "Established work practices"
            ),
            challenges = listOf(
                "Higher wage and benefit costs",
                "Work rule restrictions",
                "Jurisdictional disputes",
                "Strike risk"
            )
        ),

        "mixed_market" to UnionFactor(
            marketType = "Mixed Union Market",
            unionPresence = 0.35, // 35% union workforce
            regions = listOf("Seattle", "Denver", "Atlanta", "Philadelphia", "Washington DC"),
            factors = mapOf(
                "wages" to 1.15,
                "benefits" to 1.25,
                "productivity" to 1.05,
                "work_rules" to 1.1,
                "apprenticeship" to 1.0,
                "quality" to 1.08
            ),
            advantages = listOf(
                "Competitive wage rates",
                "Choice of union or open shop",
                "Some skilled trades availability",
                "Balanced market conditions"
            ),
            challenges = listOf(
                "Varying skill levels",
                "Inconsistent training standards",
                "Market competition effects"
            )
        ),

        "right_to_work_market" to UnionFactor(
            marketType = "Right-to-Work Market",
            unionPresence = 0.15, // 15% union workforce
            regions = listOf("Austin", "Phoenix", "Nashville", "Charlotte", "Tampa"),
            factors = mapOf(
                "wages" to 0.85,
                "benefits" to 0.75,
                "productivity" to 0.95, // More variable skill levels
                "work_rules" to 1.0, // Flexible work practices
                "apprenticeship" to 0.9, // Less structured training
                "quality" to 0.95 // More variable quality
            ),
            advantages = listOf(
                "Lower wage and benefit costs",
                "Flexible work practices",
                "No work rule restrictions",
                "Competitive bidding environment"
            ),
            challenges = listOf(
                "Variable skill levels",
                "Less standardized training",
                "Higher turnover rates",
                "Quality consistency issues"
            )
        )
    )

    /**
     * Permitting and Inspection Complexity Factors
     * Varies significantly by jurisdiction
     */
    fun getPermitComplexityFactors(): Map<String, PermitComplexityFactor> = mapOf(
        
        "simple_permits" to PermitComplexityFactor(
            complexityLevel = "Simple Permits",
            typicalProjects = listOf("Interior remodel", "Deck", "Fence", "HVAC replacement"),
            timeFactor = 1.05, // Minimal schedule impact
            costFactor = 1.02, // Low permit fees
            processingTime = "1-2 weeks",
            inspections = listOf("Final inspection"),
            requirements = listOf(
                "Basic application",
                "Simple drawings",
                "Standard fees"
            )
        ),

        "moderate_permits" to PermitComplexityFactor(
            complexityLevel = "Moderate Permits",
            typicalProjects = listOf("Room addition", "Kitchen remodel", "Bathroom remodel", "Electrical upgrade"),
            timeFactor = 1.15, // Some schedule impact
            costFactor = 1.08, // Moderate fees and requirements
            processingTime = "2-4 weeks",
            inspections = listOf("Foundation", "Framing", "Mechanical/Electrical", "Final"),
            requirements = listOf(
                "Detailed drawings",
                "Engineering stamps (some)",
                "Energy compliance",
                "Standard permit fees"
            )
        ),

        "complex_permits" to PermitComplexityFactor(
            complexityLevel = "Complex Permits",
            typicalProjects = listOf("New construction", "Major renovation", "Commercial build-out", "Multi-family"),
            timeFactor = 1.3, // Significant schedule impact
            costFactor = 1.18, // Higher fees and professional costs
            processingTime = "4-8 weeks",
            inspections = listOf("Foundation", "Framing", "Rough MEP", "Insulation", "Drywall", "Final"),
            requirements = listOf(
                "Architectural drawings",
                "Structural engineering",
                "MEP engineering",
                "Energy compliance",
                "Accessibility compliance",
                "Environmental reviews",
                "Higher permit fees"
            )
        ),

        "special_permits" to PermitComplexityFactor(
            complexityLevel = "Special Permits",
            typicalProjects = listOf("Historic renovation", "Flood zone construction", "Seismic upgrades", "High-rise"),
            timeFactor = 1.6, // Major schedule impact
            costFactor = 1.35, // High professional and fee costs
            processingTime = "8-16 weeks",
            inspections = listOf("Multiple specialized inspections", "Third-party testing", "Special inspectors"),
            requirements = listOf(
                "Specialized engineering",
                "Environmental assessments",
                "Historic preservation review",
                "Special use permits",
                "Variance applications",
                "Public hearings",
                "High fees and professional costs"
            )
        )
    )
}

/**
 * Supporting data classes for renovation factors
 */
data class ProjectRenovationFactor(
    val projectType: String,
    val baseMultiplier: Double,
    val complexityFactors: Map<String, Double>,
    val ageFactors: Map<String, Double>,
    val discoveryFactors: Map<String, Double>,
    val tradeSpecificFactors: Map<String, Double>,
    val description: String,
    val source: String
)

data class AccessibilityFactor(
    val category: String,
    val factors: Map<String, Double>,
    val description: String
)

data class QualityGradeFactor(
    val category: String,
    val grades: Map<String, QualityGrade>,
    val source: String
)

data class QualityGrade(
    val multiplier: Double,
    val description: String,
    val characteristics: List<String>,
    val typicalUse: String
)

data class SeasonalFactor(
    val season: String,
    val temperatureRange: String,
    val factors: Map<String, Double>,
    val challenges: List<String>,
    val advantages: List<String>
)

data class UnionFactor(
    val marketType: String,
    val unionPresence: Double,
    val regions: List<String>,
    val factors: Map<String, Double>,
    val advantages: List<String>,
    val challenges: List<String>
)

data class PermitComplexityFactor(
    val complexityLevel: String,
    val typicalProjects: List<String>,
    val timeFactor: Double,
    val costFactor: Double,
    val processingTime: String,
    val inspections: List<String>,
    val requirements: List<String>
)