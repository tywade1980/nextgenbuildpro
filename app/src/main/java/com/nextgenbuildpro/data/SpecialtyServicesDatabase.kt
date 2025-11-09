package com.nextgenbuildpro.data

import com.nextgenbuildpro.pm.data.model.*
import java.util.*

/**
 * Comprehensive Specialty Construction Services Database
 * 
 * Sources: Associated General Contractors (AGC), National Trade Association Data,
 * Specialized Contractor Directories, OSHA Requirements, EPA Regulations,
 * Professional Certification Bodies, Insurance Industry Guidelines
 * 
 * This database covers all specialized construction services including:
 * - Environmental remediation and hazmat
 * - Structural and seismic work
 * - Technology and smart systems
 * - Accessibility and ADA compliance
 * - Energy efficiency and green building
 * - Historic preservation and restoration
 * - Safety and security systems
 * - Specialty finishes and materials
 */
object SpecialtyServicesDatabase {

    /**
     * Environmental and Hazardous Material Services
     * EPA and OSHA regulated work requiring specialized contractors
     */
    fun getEnvironmentalServices(): Map<String, SpecialtyService> = mapOf(
        
        "asbestos_abatement" to SpecialtyService(
            id = "asbestos_abatement",
            name = "Asbestos Abatement",
            category = "Environmental",
            description = "EPA-regulated asbestos removal and remediation",
            unit = "square foot",
            baseRate = 12.50,
            laborHours = 0.15, // Per square foot
            minimumCharge = 2500.0,
            certificationRequired = true,
            licenseRequired = true,
            specialRequirements = listOf(
                "EPA RRP certification",
                "State asbestos contractor license",
                "OSHA 10-hour training minimum",
                "Medical surveillance program",
                "Specialized containment equipment",
                "HEPA filtration systems",
                "Air monitoring required",
                "Hazmat disposal documentation"
            ),
            equipmentNeeded = listOf(
                "Negative air machines",
                "HEPA vacuums", 
                "Personal protective equipment",
                "Containment plastic and tape",
                "Decontamination units",
                "Air sampling equipment"
            ),
            safetyRequirements = listOf(
                "Respiratory protection program",
                "Medical examinations",
                "Training documentation", 
                "Exposure monitoring",
                "Work area containment",
                "Decontamination procedures"
            ),
            typicalProjects = listOf(
                "Asbestos floor tile removal",
                "Pipe insulation abatement",
                "Ceiling texture removal",
                "Siding and roofing material",
                "Boiler and ductwork insulation"
            ),
            costFactors = mapOf(
                "friable_asbestos" to 1.8, // Easily damaged, airborne risk
                "non_friable_asbestos" to 1.0, // More stable materials
                "limited_access" to 1.4,
                "occupied_building" to 1.6,
                "high_fiber_count" to 2.1
            ),
            regionalVariation = 0.25, // 25% variation by region
            seasonalFactors = mapOf(
                "winter" to 1.1,
                "spring" to 1.0,
                "summer" to 1.05,
                "fall" to 1.0
            ),
            source = "EPA Asbestos NESHAP, OSHA Construction Standard",
            lastUpdated = Date()
        ),

        "lead_paint_remediation" to SpecialtyService(
            id = "lead_paint_remediation",
            name = "Lead Paint Remediation",
            category = "Environmental",
            description = "EPA RRP-compliant lead paint removal and encapsulation",
            unit = "square foot",
            baseRate = 8.75,
            laborHours = 0.12,
            minimumCharge = 1500.0,
            certificationRequired = true,
            licenseRequired = true,
            specialRequirements = listOf(
                "EPA RRP certification",
                "Lead-safe work practices",
                "Pre-renovation testing",
                "Containment procedures",
                "HEPA cleanup verification",
                "Documentation and recordkeeping"
            ),
            equipmentNeeded = listOf(
                "HEPA vacuums",
                "Containment plastic",
                "Lead test kits",
                "Personal protective equipment",
                "Specialized tools for minimal dust"
            ),
            safetyRequirements = listOf(
                "Worker protection training",
                "Dust containment",
                "Proper disposal methods",
                "Cleaning verification",
                "Air monitoring if required"
            ),
            typicalProjects = listOf(
                "Window replacement prep",
                "Exterior paint removal",
                "Interior renovation prep",
                "Demolition containment",
                "Soil remediation"
            ),
            costFactors = mapOf(
                "exterior_work" to 1.2,
                "window_work" to 1.5, // High friction surfaces
                "interior_work" to 1.0,
                "soil_remediation" to 1.8,
                "encapsulation_vs_removal" to 0.6
            ),
            regionalVariation = 0.20,
            seasonalFactors = mapOf(
                "winter" to 1.15, // Indoor containment challenges
                "spring" to 1.0,
                "summer" to 0.95, // Optimal for exterior work
                "fall" to 1.05
            ),
            source = "EPA RRP Rule, HUD Lead-Safe Housing Rule",
            lastUpdated = Date()
        ),

        "mold_remediation" to SpecialtyService(
            id = "mold_remediation",
            name = "Mold Remediation",
            category = "Environmental",
            description = "Professional mold removal and moisture control",
            unit = "square foot",
            baseRate = 6.25,
            laborHours = 0.08,
            minimumCharge = 1000.0,
            certificationRequired = true,
            licenseRequired = false, // Varies by state
            specialRequirements = listOf(
                "IICRC mold remediation certification",
                "Moisture testing and assessment",
                "Containment and air filtration",
                "Source moisture elimination",
                "Post-remediation verification"
            ),
            equipmentNeeded = listOf(
                "Dehumidifiers",
                "Air scrubbers with HEPA",
                "Moisture meters",
                "Containment barriers",
                "Antimicrobial treatments"
            ),
            safetyRequirements = listOf(
                "Personal protective equipment",
                "Respiratory protection",
                "Containment procedures",
                "Proper disposal methods"
            ),
            typicalProjects = listOf(
                "Water damage remediation",
                "Basement mold cleanup",
                "HVAC system cleaning",
                "Attic mold removal",
                "Crawl space treatment"
            ),
            costFactors = mapOf(
                "black_mold" to 1.6, // Stachybotrys, more dangerous
                "surface_mold" to 1.0,
                "hvac_contamination" to 1.8,
                "structural_materials" to 1.4,
                "extensive_damage" to 2.1
            ),
            regionalVariation = 0.30, // High humidity areas more expensive
            seasonalFactors = mapOf(
                "winter" to 1.0,
                "spring" to 1.15, // High moisture season
                "summer" to 1.25, // Peak humidity
                "fall" to 1.05
            ),
            source = "IICRC S520 Mold Remediation Standard",
            lastUpdated = Date()
        ),

        "radon_mitigation" to SpecialtyService(
            id = "radon_mitigation",
            name = "Radon Mitigation System",
            category = "Environmental",
            description = "Radon gas reduction system installation",
            unit = "system",
            baseRate = 1250.0,
            laborHours = 8.0, // Per system
            minimumCharge = 1200.0,
            certificationRequired = true,
            licenseRequired = true, // Some states require
            specialRequirements = listOf(
                "NRPP or NRSB certification",
                "Radon testing and retesting",
                "System design calculations",
                "Proper vent routing",
                "System labeling and documentation"
            ),
            equipmentNeeded = listOf(
                "Radon fan",
                "PVC piping system",
                "Sealing materials",
                "System monitoring gauge",
                "Electrical connections"
            ),
            safetyRequirements = listOf(
                "Electrical safety",
                "Proper ventilation routing",
                "System testing protocols"
            ),
            typicalProjects = listOf(
                "Basement mitigation",
                "Crawl space systems",
                "Slab-on-grade systems",
                "System retrofits",
                "Commercial buildings"
            ),
            costFactors = mapOf(
                "basement_system" to 1.0,
                "crawl_space_system" to 1.2,
                "slab_system" to 0.8,
                "complex_routing" to 1.4,
                "multiple_suction_points" to 1.6
            ),
            regionalVariation = 0.15, // Less variation, standardized systems
            seasonalFactors = mapOf(
                "winter" to 1.1, // More indoor time, higher concern
                "spring" to 1.0,
                "summer" to 0.9,
                "fall" to 1.05
            ),
            source = "EPA Radon Mitigation Standards",
            lastUpdated = Date()
        )
    )

    /**
     * Structural and Seismic Specialty Services
     * Engineering-intensive work requiring specialized expertise
     */
    fun getStructuralServices(): Map<String, SpecialtyService> = mapOf(
        
        "seismic_retrofitting" to SpecialtyService(
            id = "seismic_retrofitting",
            name = "Seismic Retrofitting",
            category = "Structural",
            description = "Earthquake resistance improvements to existing structures",
            unit = "square foot",
            baseRate = 18.50,
            laborHours = 0.25,
            minimumCharge = 5000.0,
            certificationRequired = true,
            licenseRequired = true,
            specialRequirements = listOf(
                "Structural engineer design",
                "Seismic design certification",
                "Building code compliance",
                "Permit and inspection requirements",
                "Specialized fasteners and hardware",
                "Connection details per engineer"
            ),
            equipmentNeeded = listOf(
                "Structural bolts and hardware",
                "Steel reinforcement materials",
                "Epoxy anchoring systems",
                "Specialized drilling equipment",
                "Welding equipment"
            ),
            safetyRequirements = listOf(
                "Fall protection systems",
                "Structural safety during work",
                "Temporary shoring if needed",
                "Welding safety procedures"
            ),
            typicalProjects = listOf(
                "Foundation anchor bolting",
                "Cripple wall bracing",
                "Soft story retrofitting",
                "Unreinforced masonry upgrade",
                "Steel moment frame installation"
            ),
            costFactors = mapOf(
                "foundation_anchoring" to 1.0,
                "cripple_wall_bracing" to 0.8,
                "soft_story_retrofit" to 1.8,
                "masonry_reinforcement" to 2.2,
                "steel_frame_work" to 2.5
            ),
            regionalVariation = 0.40, // High variation based on seismic zones
            seasonalFactors = mapOf(
                "winter" to 1.1,
                "spring" to 1.0,
                "summer" to 0.95,
                "fall" to 1.0
            ),
            source = "ASCE 41 Seismic Rehabilitation Standards",
            lastUpdated = Date()
        ),

        "foundation_underpinning" to SpecialtyService(
            id = "foundation_underpinning",
            name = "Foundation Underpinning",
            category = "Structural",
            description = "Foundation strengthening and depth extension",
            unit = "linear foot",
            baseRate = 185.0,
            laborHours = 2.5, // Per linear foot
            minimumCharge = 8000.0,
            certificationRequired = true,
            licenseRequired = true,
            specialRequirements = listOf(
                "Geotechnical engineering report",
                "Structural engineering design",
                "Specialized excavation techniques",
                "Underpinning system installation",
                "Monitoring during construction",
                "Load transfer procedures"
            ),
            equipmentNeeded = listOf(
                "Micro-piles or helical piers",
                "Hydraulic jacks",
                "Concrete pump equipment",
                "Specialized excavation tools",
                "Monitoring equipment"
            ),
            safetyRequirements = listOf(
                "Excavation safety procedures",
                "Structural monitoring",
                "Shoring and support systems",
                "Equipment operator certification"
            ),
            typicalProjects = listOf(
                "Settlement repair",
                "Basement lowering",
                "Load capacity increase",
                "Seismic upgrades",
                "Historic building preservation"
            ),
            costFactors = mapOf(
                "micro_pile_system" to 1.0,
                "mass_concrete_method" to 0.7,
                "helical_pier_system" to 1.3,
                "limited_access" to 1.6,
                "rock_conditions" to 1.4
            ),
            regionalVariation = 0.35, // Soil conditions vary significantly
            seasonalFactors = mapOf(
                "winter" to 1.25, // Excavation challenges
                "spring" to 1.15, // Wet conditions
                "summer" to 0.95, // Optimal conditions
                "fall" to 1.0
            ),
            source = "ICC Building Code, Geotechnical Engineering Standards",
            lastUpdated = Date()
        ),

        "structural_steel_repair" to SpecialtyService(
            id = "structural_steel_repair",
            name = "Structural Steel Repair",
            category = "Structural",
            description = "Repair and reinforcement of steel structural elements",
            unit = "pound",
            baseRate = 4.25, // Per pound of steel
            laborHours = 0.15, // Per pound
            minimumCharge = 3000.0,
            certificationRequired = true,
            licenseRequired = true,
            specialRequirements = listOf(
                "Certified welding procedures",
                "AWS welding certification",
                "Non-destructive testing",
                "Structural engineer approval",
                "Material certifications",
                "Quality control inspections"
            ),
            equipmentNeeded = listOf(
                "Welding equipment and consumables",
                "Cutting and grinding tools",
                "Lifting and rigging equipment",
                "NDT testing equipment",
                "Safety and access equipment"
            ),
            safetyRequirements = listOf(
                "Fall protection systems",
                "Hot work permits",
                "Welding safety procedures",
                "Rigging safety protocols"
            ),
            typicalProjects = listOf(
                "Beam reinforcement",
                "Column repair",
                "Connection retrofitting",
                "Fire damage repair",
                "Corrosion remediation"
            ),
            costFactors = mapOf(
                "field_welding" to 1.0,
                "overhead_work" to 1.4,
                "confined_spaces" to 1.7,
                "occupied_buildings" to 1.3,
                "emergency_repairs" to 1.8
            ),
            regionalVariation = 0.25,
            seasonalFactors = mapOf(
                "winter" to 1.15, // Cold weather welding
                "spring" to 1.0,
                "summer" to 1.05, // Heat stress
                "fall" to 0.98
            ),
            source = "AWS Structural Welding Codes, AISC Standards",
            lastUpdated = Date()
        )
    )

    /**
     * Technology and Smart Building Services
     * Modern building automation and technology systems
     */
    fun getTechnologyServices(): Map<String, SpecialtyService> = mapOf(
        
        "smart_home_automation" to SpecialtyService(
            id = "smart_home_automation",
            name = "Smart Home Automation System",
            category = "Technology",
            description = "Comprehensive home automation and control systems",
            unit = "system",
            baseRate = 2500.0, // Base system
            laborHours = 16.0, // Base installation
            minimumCharge = 2000.0,
            certificationRequired = true,
            licenseRequired = false,
            specialRequirements = listOf(
                "Home automation certification",
                "Network configuration expertise",
                "Integration platform knowledge",
                "Programming and configuration",
                "User training and documentation",
                "Ongoing support options"
            ),
            equipmentNeeded = listOf(
                "Control hub/processor",
                "Smart switches and outlets",
                "Sensors and detectors",
                "Network infrastructure",
                "Programming software",
                "Testing equipment"
            ),
            safetyRequirements = listOf(
                "Electrical safety standards",
                "Network security protocols",
                "Data privacy protection"
            ),
            typicalProjects = listOf(
                "Lighting control systems",
                "Climate control automation", 
                "Security system integration",
                "Entertainment system control",
                "Energy management systems"
            ),
            costFactors = mapOf(
                "basic_lighting" to 0.6,
                "whole_house_system" to 1.0,
                "luxury_integration" to 2.1,
                "commercial_grade" to 1.8,
                "retrofit_installation" to 1.3
            ),
            regionalVariation = 0.20,
            seasonalFactors = mapOf(
                "winter" to 1.0,
                "spring" to 1.0,
                "summer" to 1.05, // Higher demand
                "fall" to 1.0
            ),
            source = "CEDIA Home Technology Standards",
            lastUpdated = Date()
        ),

        "structured_cabling" to SpecialtyService(
            id = "structured_cabling",
            name = "Structured Cabling System",
            category = "Technology",
            description = "Commercial-grade network and telecommunications cabling",
            unit = "drop",
            baseRate = 125.0, // Per network drop
            laborHours = 1.5, // Per drop
            minimumCharge = 1500.0,
            certificationRequired = true,
            licenseRequired = false,
            specialRequirements = listOf(
                "TIA/EIA standards compliance",
                "Cable testing and certification",
                "Proper termination techniques",
                "Pathway design and installation",
                "Documentation and labeling",
                "Performance testing"
            ),
            equipmentNeeded = listOf(
                "Cat6/Cat6A cable",
                "Patch panels and jacks",
                "Cable management systems",
                "Network testing equipment",
                "Termination tools",
                "Labeling systems"
            ),
            safetyRequirements = listOf(
                "Electrical safety in data rooms",
                "Proper grounding techniques",
                "Fire-rated cable requirements"
            ),
            typicalProjects = listOf(
                "Office network installation",
                "Residential structured wiring",
                "Data center cabling",
                "Fiber optic installation",
                "Wireless access point cabling"
            ),
            costFactors = mapOf(
                "cat5e_installation" to 0.8,
                "cat6_installation" to 1.0,
                "cat6a_installation" to 1.3,
                "fiber_optic" to 1.8,
                "outdoor_rated" to 1.4
            ),
            regionalVariation = 0.15,
            seasonalFactors = mapOf(
                "winter" to 1.0,
                "spring" to 1.0,
                "summer" to 1.0,
                "fall" to 1.0
            ),
            source = "TIA-568 Standards, BICSI Guidelines",
            lastUpdated = Date()
        ),

        "audio_visual_systems" to SpecialtyService(
            id = "audio_visual_systems",
            name = "Audio Visual System Installation",
            category = "Technology",
            description = "Professional AV system design and installation",
            unit = "room",
            baseRate = 3500.0, // Per room/zone
            laborHours = 12.0, // Per room
            minimumCharge = 2500.0,
            certificationRequired = true,
            licenseRequired = false,
            specialRequirements = listOf(
                "AV design and engineering",
                "Equipment selection expertise",
                "Calibration and tuning",
                "Control system programming",
                "User training and documentation",
                "Warranty and support services"
            ),
            equipmentNeeded = listOf(
                "Displays and projectors",
                "Audio equipment and speakers",
                "Control processors",
                "Signal distribution equipment",
                "Mounting and rigging hardware",
                "Calibration tools"
            ),
            safetyRequirements = listOf(
                "Electrical safety compliance",
                "Proper equipment mounting",
                "Cable management standards"
            ),
            typicalProjects = listOf(
                "Home theater systems",
                "Conference room AV",
                "Multi-zone audio systems", 
                "Digital signage networks",
                "Presentation systems"
            ),
            costFactors = mapOf(
                "basic_system" to 0.7,
                "standard_system" to 1.0,
                "premium_system" to 1.6,
                "commercial_grade" to 1.4,
                "integrated_control" to 1.3
            ),
            regionalVariation = 0.25,
            seasonalFactors = mapOf(
                "winter" to 1.0,
                "spring" to 1.0,
                "summer" to 1.1, // Higher demand for outdoor systems
                "fall" to 1.0
            ),
            source = "AVIXA AV Standards, CEDIA Guidelines",
            lastUpdated = Date()
        )
    )

    /**
     * Accessibility and ADA Compliance Services
     * Specialized work for handicap accessibility
     */
    fun getAccessibilityServices(): Map<String, SpecialtyService> = mapOf(
        
        "ada_compliance_retrofit" to SpecialtyService(
            id = "ada_compliance_retrofit",
            name = "ADA Compliance Retrofit",
            category = "Accessibility",
            description = "Building modifications for ADA compliance",
            unit = "square foot",
            baseRate = 15.75,
            laborHours = 0.2,
            minimumCharge = 5000.0,
            certificationRequired = true,
            licenseRequired = true,
            specialRequirements = listOf(
                "ADA compliance expertise",
                "Accessibility audit and planning",
                "Code compliance verification",
                "Specialized equipment knowledge",
                "Documentation and certification",
                "Ongoing maintenance planning"
            ),
            equipmentNeeded = listOf(
                "Accessibility hardware",
                "Ramps and rail systems",
                "Door and hardware modifications",
                "Bathroom fixture adaptations",
                "Signage and marking systems",
                "Safety and warning equipment"
            ),
            safetyRequirements = listOf(
                "Safety during modifications",
                "Temporary accessibility provisions",
                "Code compliance verification"
            ),
            typicalProjects = listOf(
                "Ramp installation",
                "Bathroom modifications",
                "Door width modifications",
                "Elevator installation",
                "Parking lot compliance"
            ),
            costFactors = mapOf(
                "ramp_installation" to 1.0,
                "bathroom_retrofit" to 1.4,
                "elevator_addition" to 3.5,
                "door_modifications" to 0.8,
                "signage_and_marking" to 0.6
            ),
            regionalVariation = 0.20,
            seasonalFactors = mapOf(
                "winter" to 1.1,
                "spring" to 1.0,
                "summer" to 0.95,
                "fall" to 1.0
            ),
            source = "ADA Standards for Accessible Design",
            lastUpdated = Date()
        )
    )

    /**
     * Energy Efficiency and Green Building Services
     * Sustainable construction and efficiency improvements
     */
    fun getEnergyServices(): Map<String, SpecialtyService> = mapOf(
        
        "energy_efficiency_retrofit" to SpecialtyService(
            id = "energy_efficiency_retrofit",
            name = "Energy Efficiency Retrofit",
            category = "Energy",
            description = "Comprehensive building energy efficiency improvements",
            unit = "square foot",
            baseRate = 8.50,
            laborHours = 0.08,
            minimumCharge = 2500.0,
            certificationRequired = true,
            licenseRequired = false,
            specialRequirements = listOf(
                "Energy audit and analysis",
                "HERS rating certification",
                "Blower door testing",
                "Thermal imaging assessment",
                "Performance verification",
                "Rebate documentation"
            ),
            equipmentNeeded = listOf(
                "Insulation materials",
                "Air sealing materials",
                "High-efficiency equipment",
                "Testing and diagnostic tools",
                "Installation tools"
            ),
            safetyRequirements = listOf(
                "Insulation safety procedures",
                "Confined space safety",
                "Equipment safety protocols"
            ),
            typicalProjects = listOf(
                "Attic insulation upgrade",
                "Air sealing services",
                "HVAC system upgrade",
                "Window replacement",
                "Duct sealing and insulation"
            ),
            costFactors = mapOf(
                "insulation_upgrade" to 1.0,
                "air_sealing" to 0.7,
                "hvac_upgrade" to 1.8,
                "window_replacement" to 1.4,
                "whole_house_approach" to 1.2
            ),
            regionalVariation = 0.30, // Climate affects needs
            seasonalFactors = mapOf(
                "winter" to 1.15, // High demand for efficiency
                "spring" to 1.0,
                "summer" to 0.95,
                "fall" to 1.05
            ),
            source = "ENERGY STAR Guidelines, RESNET Standards",
            lastUpdated = Date()
        ),

        "solar_panel_installation" to SpecialtyService(
            id = "solar_panel_installation",
            name = "Solar Panel System Installation",
            category = "Energy",
            description = "Residential and commercial solar photovoltaic systems",
            unit = "watt",
            baseRate = 2.85, // Per watt installed
            laborHours = 0.008, // Per watt
            minimumCharge = 8000.0,
            certificationRequired = true,
            licenseRequired = true,
            specialRequirements = listOf(
                "NABCEP certification",
                "Electrical contractor license",
                "Structural engineering review",
                "Utility interconnection process",
                "Permitting and inspection",
                "System commissioning"
            ),
            equipmentNeeded = listOf(
                "Solar panels and racking",
                "Inverters and optimizers",
                "Electrical components",
                "Monitoring systems",
                "Safety equipment",
                "Installation tools"
            ),
            safetyRequirements = listOf(
                "Fall protection systems",
                "Electrical safety protocols",
                "Rooftop safety procedures"
            ),
            typicalProjects = listOf(
                "Residential rooftop systems",
                "Commercial roof installations",
                "Ground-mount systems",
                "Carport solar structures",
                "Agricultural solar projects"
            ),
            costFactors = mapOf(
                "simple_roof" to 1.0,
                "complex_roof" to 1.3,
                "ground_mount" to 0.9,
                "commercial_scale" to 0.8,
                "battery_storage" to 1.4
            ),
            regionalVariation = 0.35, // Sunshine and incentives vary
            seasonalFactors = mapOf(
                "winter" to 1.2, // Weather challenges
                "spring" to 1.0,
                "summer" to 0.9, // Optimal installation weather
                "fall" to 1.0
            ),
            source = "NABCEP Installation Standards, NEC Code",
            lastUpdated = Date()
        )
    )

    /**
     * Safety and Security System Services
     * Life safety and property protection systems
     */
    fun getSafetyServices(): Map<String, SpecialtyService> = mapOf(
        
        "fire_sprinkler_system" to SpecialtyService(
            id = "fire_sprinkler_system",
            name = "Fire Sprinkler System Installation",
            category = "Safety",
            description = "Automatic fire sprinkler system design and installation",
            unit = "square foot",
            baseRate = 3.25,
            laborHours = 0.04,
            minimumCharge = 5000.0,
            certificationRequired = true,
            licenseRequired = true,
            specialRequirements = listOf(
                "Fire sprinkler contractor license",
                "NFPA 13 design compliance",
                "Hydraulic calculations",
                "System testing and commissioning",
                "Inspection and maintenance training",
                "Code compliance certification"
            ),
            equipmentNeeded = listOf(
                "Sprinkler heads and piping",
                "Control valves and alarms",
                "Fire department connections",
                "Backflow prevention devices",
                "Testing and monitoring equipment"
            ),
            safetyRequirements = listOf(
                "Fire safety during installation",
                "System testing protocols",
                "Water damage prevention"
            ),
            typicalProjects = listOf(
                "Commercial building systems",
                "Residential fire sprinklers",
                "Industrial facility protection",
                "High-rise building systems",
                "Special hazard systems"
            ),
            costFactors = mapOf(
                "light_hazard" to 1.0, // Offices, residential
                "ordinary_hazard" to 1.3, // Retail, schools
                "extra_hazard" to 1.8, // Industrial, storage
                "high_rise" to 1.5,
                "retrofit_installation" to 1.4
            ),
            regionalVariation = 0.25,
            seasonalFactors = mapOf(
                "winter" to 1.1, // Freeze protection concerns
                "spring" to 1.0,
                "summer" to 0.95,
                "fall" to 1.0
            ),
            source = "NFPA 13 Installation Standards",
            lastUpdated = Date()
        ),

        "security_system_installation" to SpecialtyService(
            id = "security_system_installation", 
            name = "Security System Installation",
            category = "Safety",
            description = "Comprehensive security and surveillance systems",
            unit = "device",
            baseRate = 225.0, // Per device/zone
            laborHours = 2.0, // Per device
            minimumCharge = 1500.0,
            certificationRequired = true,
            licenseRequired = true, // Some states require
            specialRequirements = listOf(
                "Security system certification",
                "Low voltage license",
                "System design and programming",
                "User training and documentation",
                "Monitoring service setup",
                "Ongoing maintenance contracts"
            ),
            equipmentNeeded = listOf(
                "Control panels and keypads",
                "Sensors and detectors",
                "Cameras and recording equipment",
                "Communication devices",
                "Power and backup systems",
                "Installation and testing tools"
            ),
            safetyRequirements = listOf(
                "Electrical safety standards",
                "Privacy and security protocols",
                "System testing procedures"
            ),
            typicalProjects = listOf(
                "Residential alarm systems",
                "Commercial security systems",
                "Access control systems",
                "Video surveillance networks",
                "Integrated safety systems"
            ),
            costFactors = mapOf(
                "basic_alarm_system" to 0.8,
                "integrated_system" to 1.0,
                "commercial_grade" to 1.4,
                "high_security" to 1.8,
                "wireless_system" to 1.1
            ),
            regionalVariation = 0.20,
            seasonalFactors = mapOf(
                "winter" to 1.1, // Higher security concerns
                "spring" to 1.0,
                "summer" to 0.95, // Vacation season
                "fall" to 1.05
            ),
            source = "ESA Security Standards, UL Listings",
            lastUpdated = Date()
        )
    )

    /**
     * Get service categories for organization
     */
    fun getServiceCategories(): List<ServiceCategory> = listOf(
        ServiceCategory(
            name = "Environmental",
            description = "Hazardous material remediation and environmental services",
            services = getEnvironmentalServices().keys.toList(),
            regulatoryRequirements = "EPA, OSHA, state environmental agencies",
            typicalInsurance = "Environmental liability, pollution legal liability"
        ),
        ServiceCategory(
            name = "Structural", 
            description = "Structural engineering and specialized structural work",
            services = getStructuralServices().keys.toList(),
            regulatoryRequirements = "Professional engineering license, building permits",
            typicalInsurance = "Professional liability, general liability"
        ),
        ServiceCategory(
            name = "Technology",
            description = "Building automation, networking, and technology systems",
            services = getTechnologyServices().keys.toList(),
            regulatoryRequirements = "Industry certifications, some electrical licensing",
            typicalInsurance = "Technology errors and omissions, general liability"
        ),
        ServiceCategory(
            name = "Accessibility",
            description = "ADA compliance and accessibility modifications",
            services = getAccessibilityServices().keys.toList(),
            regulatoryRequirements = "Building codes, ADA compliance expertise",
            typicalInsurance = "General liability, professional liability"
        ),
        ServiceCategory(
            name = "Energy",
            description = "Energy efficiency and renewable energy systems",
            services = getEnergyServices().keys.toList(),
            regulatoryRequirements = "Electrical license, energy certifications",
            typicalInsurance = "Professional liability, product liability"
        ),
        ServiceCategory(
            name = "Safety",
            description = "Life safety and security systems",
            services = getSafetyServices().keys.toList(),
            regulatoryRequirements = "Fire sprinkler license, security system license",
            typicalInsurance = "Professional liability, general liability"
        )
    )
}

/**
 * Supporting data classes for specialty services
 */
data class SpecialtyService(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val unit: String,
    val baseRate: Double,
    val laborHours: Double,
    val minimumCharge: Double,
    val certificationRequired: Boolean,
    val licenseRequired: Boolean,
    val specialRequirements: List<String>,
    val equipmentNeeded: List<String>,
    val safetyRequirements: List<String>,
    val typicalProjects: List<String>,
    val costFactors: Map<String, Double>,
    val regionalVariation: Double, // Percentage variation
    val seasonalFactors: Map<String, Double>,
    val source: String,
    val lastUpdated: Date,
    val warrantyPeriod: String = "1 year standard",
    val maintenanceRequired: Boolean = true
)

data class ServiceCategory(
    val name: String,
    val description: String,
    val services: List<String>,
    val regulatoryRequirements: String,
    val typicalInsurance: String
)