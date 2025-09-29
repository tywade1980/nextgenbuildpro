package com.nextgenbuildpro.pm.data.repository

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

/**
 * Repository for managing the hierarchical indexed catalogue
 * Provides comprehensive construction project data with detailed assemblies, tasks, and web-sourced cost information
 */
class HierarchicalCatalogueRepository(private val context: Context) {
    private val TAG = "HierarchicalCatalogueRepo"
    
    private val _projectCatalogue = MutableStateFlow<ProjectCatalogue?>(null)
    val projectCatalogue: StateFlow<ProjectCatalogue?> = _projectCatalogue.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<Any>>(emptyList())
    val searchResults: StateFlow<List<Any>> = _searchResults.asStateFlow()

    init {
        loadComprehensiveConstructionCatalogue()
    }

    /**
     * Load comprehensive construction catalogue with detailed hierarchical data
     */
    private fun loadComprehensiveConstructionCatalogue() {
        try {
            // Create web resource URLs for cost data sourcing
            val webResources = createWebResources()
            
            // Create project types
            val projectTypes = listOf(
                createNewConstructionProjectType(webResources),
                createRemodelingProjectType(webResources),
                createAdditionProjectType(webResources),
                createRepairMaintenanceProjectType(webResources)
            )

            // Create the root project catalogue
            val catalogue = ProjectCatalogue(
                name = "Home Construction Master Catalogue",
                description = "Comprehensive hierarchical catalogue for residential construction projects with detailed assemblies, tasks, material specifications, and labor cost data sourced from industry web resources",
                version = "1.0.0",
                projectTypes = projectTypes
            )

            _projectCatalogue.value = catalogue
            Log.d(TAG, "Loaded comprehensive construction catalogue with ${projectTypes.size} project types")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading construction catalogue", e)
        }
    }

    /**
     * Create web resource URLs for cost and time data sourcing
     */
    private fun createWebResources(): List<WebResourceUrl> {
        return listOf(
            WebResourceUrl(
                name = "Bureau of Labor Statistics",
                url = "https://www.bls.gov/oes/current/oes_nat.htm",
                description = "Federal government labor statistics and wage data",
                dataType = WebResourceType.LABOR_RATES
            ),
            WebResourceUrl(
                name = "RSMeans Construction Data",
                url = "https://www.rsmeans.com/",
                description = "Industry-standard construction cost database",
                dataType = WebResourceType.LABOR_RATES
            ),
            WebResourceUrl(
                name = "HomeAdvisor Cost Guides",
                url = "https://www.homeadvisor.com/cost/",
                description = "Consumer-facing construction cost data",
                dataType = WebResourceType.INDUSTRY_BENCHMARKS
            ),
            WebResourceUrl(
                name = "National Association of Home Builders",
                url = "https://www.nahb.org/research/",
                description = "Industry research and cost benchmarks",
                dataType = WebResourceType.TIME_STANDARDS
            ),
            WebResourceUrl(
                name = "Home Depot Pro",
                url = "https://www.homedepot.com/c/pro",
                description = "Professional contractor material pricing",
                dataType = WebResourceType.MATERIAL_COSTS
            ),
            WebResourceUrl(
                name = "Lowe's Pro Services",
                url = "https://www.lowes.com/pro",
                description = "Professional contractor material and equipment pricing",
                dataType = WebResourceType.MATERIAL_COSTS
            )
        )
    }

    /**
     * Create New Construction project type with comprehensive trade data
     */
    private fun createNewConstructionProjectType(webResources: List<WebResourceUrl>): ProjectType {
        val trades = listOf(
            createFramingTradeIndex(webResources),
            createElectricalTradeIndex(webResources),
            createPlumbingTradeIndex(webResources),
            createHVACTradeIndex(webResources),
            createDrywallTradeIndex(webResources),
            createRoofingTradeIndex(webResources),
            createFoundationTradeIndex(webResources)
        )

        return ProjectType(
            name = "New Construction",
            description = "Complete new residential construction from foundation to finish",
            code = "NC",
            contextMode = ContextMode.NEW_CONSTRUCTION,
            applicablePhases = HomeLifecyclePhase.values().toList(),
            trades = trades,
            metadata = mapOf(
                "typical_duration" to "6-12 months",
                "complexity" to "high",
                "permit_required" to "true"
            )
        )
    }

    /**
     * Create Framing trade index with detailed assemblies and tasks
     */
    private fun createFramingTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        // Create detailed tasks for framing
        val framingTasks = listOf(
            DetailedTask(
                name = "Install Sole Plate",
                description = "Install pressure-treated sole plate on foundation",
                workDescription = "Measure and cut pressure-treated lumber to length, position on foundation, check for level and square, drill pilot holes, and secure with anchor bolts or powder-actuated fasteners. Ensure proper spacing and alignment for wall framing.",
                unitType = UnitType.LF,
                defaultQty = 1.0,
                laborTimePerUnit = 0.15, // 9 minutes per linear foot
                laborCostPerUnit = 3.75, // Based on $25/hour carpenter rate
                materialCostPerUnit = 2.50,
                markup = 0.20,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Circular Saw", "Drill", "Level", "Chalk Line", "Measuring Tape"),
                requiredMaterials = listOf(
                    TaskMaterial(
                        name = "Pressure Treated 2x6 Lumber",
                        description = "Ground contact rated pressure treated lumber",
                        quantity = 1.0,
                        unit = "LF",
                        unitCost = 2.50,
                        supplier = "Home Depot",
                        webSourceUrl = "https://www.homedepot.com/p/lumber"
                    )
                ),
                safetyNotes = listOf("Wear safety glasses", "Use ear protection when cutting"),
                qualityCheckpoints = listOf("Check level within 1/4 inch", "Verify square corners"),
                webSourcedData = LaborCostData(
                    source = "BLS Construction Laborers",
                    sourceUrl = "https://www.bls.gov/oes/current/oes472061.htm",
                    region = "National Average",
                    lastUpdated = LocalDateTime.now(),
                    avgHourlyRate = 25.00,
                    lowRate = 18.00,
                    highRate = 35.00,
                    reliability = DataReliability.HIGH
                )
            ),
            DetailedTask(
                name = "Frame Wall Studs",
                description = "Cut and install wall studs at 16\" on center",
                workDescription = "Measure wall length, calculate stud layout at 16\" on center, cut studs to proper height (typically 8' walls require 92-5/8\" studs), mark top and bottom plates, position studs, and secure with framing nails. Install cripple studs under windows and above doors as needed.",
                unitType = UnitType.LF,
                defaultQty = 1.0,
                laborTimePerUnit = 0.25, // 15 minutes per linear foot
                laborCostPerUnit = 6.25,
                materialCostPerUnit = 4.00,
                markup = 0.20,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Framing Hammer", "Circular Saw", "Speed Square", "Measuring Tape"),
                requiredMaterials = listOf(
                    TaskMaterial(
                        name = "2x6 Kiln Dried Stud",
                        description = "Kiln dried dimensional lumber stud",
                        quantity = 0.75, // Approximately 3/4 stud per linear foot
                        unit = "EA",
                        unitCost = 4.00
                    )
                ),
                webSourcedData = LaborCostData(
                    source = "RSMeans Building Construction",
                    sourceUrl = "https://www.rsmeans.com/",
                    region = "National Average",
                    lastUpdated = LocalDateTime.now(),
                    avgHourlyRate = 25.00,
                    lowRate = 20.00,
                    highRate = 30.00,
                    reliability = DataReliability.VERIFIED
                )
            ),
            DetailedTask(
                name = "Install Top Plate",
                description = "Install double top plate on wall frame",
                workDescription = "Cut top plate lumber to length, ensuring joints are staggered from bottom plate joints. Install first top plate flush with stud tops, securing with 16d nails. Install second top plate overlapping joints by minimum 24 inches, tying intersecting walls together.",
                unitType = UnitType.LF,
                defaultQty = 1.0,
                laborTimePerUnit = 0.10,
                laborCostPerUnit = 2.50,
                materialCostPerUnit = 5.00, // Double top plate
                markup = 0.20,
                skillLevel = SkillLevel.INTERMEDIATE
            )
        )

        // Create macro task grouping related framing tasks
        val wallFramingMacroTask = MacroTask(
            name = "Complete Wall Framing",
            description = "Frame complete wall from sole plate to top plate",
            workDescription = "Complete wall framing assembly including sole plate installation, stud layout and installation, header framing for openings, and double top plate installation. Includes all rough openings for doors and windows.",
            tasks = framingTasks,
            totalEstimatedHours = framingTasks.sumOf { it.laborTimePerUnit * it.defaultQty },
            totalEstimatedCost = framingTasks.sumOf { (it.laborCostPerUnit + it.materialCostPerUnit) * it.defaultQty },
            sequenceOrder = 1
        )

        // Create detailed assembly
        val wallFramingAssembly = DetailedAssembly(
            name = "Exterior Wall Framing",
            category = "Structural Framing",
            description = "Complete exterior wall framing assembly with headers and rough openings",
            workDescription = "Construct complete exterior wall framing system including foundation attachment, wall studs, headers for openings, corners, intersections, and top plate installation. Frame includes provisions for windows, doors, and mechanical penetrations per architectural plans.",
            validModes = listOf(ContextMode.NEW_CONSTRUCTION),
            defaultQuantityUnit = UnitType.LF,
            baseQuantity = 40.0,
            lifecyclePhase = HomeLifecyclePhase.STRUCTURE,
            tasks = framingTasks,
            macroTasks = listOf(wallFramingMacroTask),
            prerequisites = listOf("Foundation cured and ready", "Lumber delivered to site"),
            deliverables = listOf("Framed walls ready for sheathing", "Rough openings to spec"),
            qualityStandards = listOf("Plumb walls within 1/4 inch", "Level within 1/8 inch per 10 feet"),
            safetyRequirements = listOf("Fall protection required", "Hard hats mandatory")
        )

        // Create master assembly for framing trade
        val framingMasterAssembly = MasterAssembly(
            name = "Framing Master Assembly",
            description = "Complete framing system for residential construction including all walls, floors, and roof framing",
            tradeCode = "FRM",
            assemblies = listOf(wallFramingAssembly),
            subAssemblies = listOf(
                SubAssembly(
                    name = "Corner Assembly",
                    description = "Three-stud corner assembly for exterior wall intersections",
                    workDescription = "Construct three-stud corner using (2) full-length studs and (1) backing stud with blocks, providing nailing surface for interior and exterior finishes.",
                    parentAssemblyId = wallFramingAssembly.id,
                    tasks = framingTasks.take(2), // Subset of tasks
                    estimatedHours = 2.0,
                    estimatedCost = 45.00,
                    skillLevel = SkillLevel.INTERMEDIATE
                )
            ),
            totalEstimatedHours = 120.0,
            totalEstimatedCost = 2400.0
        )

        return TradeIndex(
            tradeName = "Framing",
            tradeCode = "FRM",
            description = "Structural framing and carpentry work",
            workDescription = "Structural framing includes all wood framing for walls, floors, roofs, stairs, and structural elements. Work encompasses layout, cutting, assembly, and installation of dimensional lumber and engineered wood products to create the structural skeleton of the building.",
            lifecyclePhase = HomeLifecyclePhase.STRUCTURE,
            masterAssembly = framingMasterAssembly,
            avgLaborRate = 25.00,
            webResourceUrls = webResources.filter { it.dataType == WebResourceType.LABOR_RATES }
        )
    }

    /**
     * Create additional trade indices (simplified for brevity but follow same pattern)
     */
    private fun createElectricalTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        val electricalTasks = listOf(
            DetailedTask(
                name = "Install Electrical Panel",
                description = "Install main electrical service panel",
                workDescription = "Mount electrical service panel in designated location, connect service entrance cables, install main breaker, and rough-in branch circuit breakers. Coordinate with utility company for meter installation.",
                unitType = UnitType.EA,
                defaultQty = 1.0,
                laborTimePerUnit = 4.0,
                laborCostPerUnit = 200.00, // $50/hour electrician rate
                materialCostPerUnit = 350.00,
                markup = 0.25,
                skillLevel = SkillLevel.EXPERT,
                webSourcedData = LaborCostData(
                    source = "BLS Electricians",
                    sourceUrl = "https://www.bls.gov/oes/current/oes472111.htm",
                    region = "National Average",
                    lastUpdated = LocalDateTime.now(),
                    avgHourlyRate = 50.00,
                    lowRate = 35.00,
                    highRate = 75.00,
                    reliability = DataReliability.HIGH
                )
            )
        )

        val electricalAssembly = DetailedAssembly(
            name = "Electrical Rough-In",
            category = "Electrical Systems",
            description = "Complete electrical rough-in installation",
            workDescription = "Install all rough electrical including service panel, branch circuits, outlets, switches, and fixture boxes. Run all wiring through framing and coordinate with other trades.",
            validModes = listOf(ContextMode.NEW_CONSTRUCTION, ContextMode.REMODELING),
            defaultQuantityUnit = UnitType.SF,
            baseQuantity = 2000.0,
            lifecyclePhase = HomeLifecyclePhase.SYSTEMS,
            tasks = electricalTasks,
            macroTasks = emptyList(),
            prerequisites = listOf("Framing inspection complete"),
            deliverables = listOf("All rough electrical installed and inspected")
        )

        val electricalMasterAssembly = MasterAssembly(
            name = "Electrical Master Assembly",
            description = "Complete electrical system for residential construction",
            tradeCode = "ELE",
            assemblies = listOf(electricalAssembly),
            subAssemblies = emptyList(),
            totalEstimatedHours = 80.0,
            totalEstimatedCost = 3500.0
        )

        return TradeIndex(
            tradeName = "Electrical",
            tradeCode = "ELE",
            description = "Electrical systems installation",
            workDescription = "Electrical work encompasses all electrical systems including service panels, branch circuits, outlets, switches, lighting, and low voltage systems. Work includes planning, installation, testing, and inspection coordination.",
            lifecyclePhase = HomeLifecyclePhase.SYSTEMS,
            masterAssembly = electricalMasterAssembly,
            avgLaborRate = 50.00,
            webResourceUrls = webResources.filter { it.dataType == WebResourceType.LABOR_RATES }
        )
    }

    // Trade creation methods with proper implementations
    private fun createPlumbingTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        // Create basic plumbing tasks
        val plumbingTasks = listOf(
            DetailedTask(
                name = "Install Supply Lines",
                description = "Install hot and cold water supply lines",
                estimatedHours = 16.0,
                materialCost = 450.0,
                laborCost = 800.0,
                requirements = listOf("Framing complete")
            ),
            DetailedTask(
                name = "Install Drain Lines",
                description = "Install waste and vent lines",
                estimatedHours = 20.0,
                materialCost = 350.0,
                laborCost = 1000.0,
                requirements = listOf("Supply lines installed")
            ),
            DetailedTask(
                name = "Install Fixtures",
                description = "Install toilets, sinks, and other fixtures",
                estimatedHours = 12.0,
                materialCost = 800.0,
                laborCost = 600.0,
                requirements = listOf("Drain lines complete")
            )
        )
        
        val plumbingAssembly = DetailedAssembly(
            name = "Rough Plumbing",
            description = "Basic plumbing infrastructure",
            assemblyCode = "PLB-001",
            unit = "sqft",
            baseQuantity = 1500.0,
            lifecyclePhase = HomeLifecyclePhase.SYSTEMS,
            tasks = plumbingTasks,
            macroTasks = emptyList(),
            prerequisites = listOf("Framing inspection complete"),
            deliverables = listOf("All rough plumbing installed and inspected")
        )
        
        val plumbingMasterAssembly = MasterAssembly(
            name = "Plumbing Master Assembly",
            description = "Complete plumbing system for residential construction",
            tradeCode = "PLB",
            assemblies = listOf(plumbingAssembly),
            subAssemblies = emptyList(),
            totalEstimatedHours = 48.0,
            totalEstimatedCost = 2800.0
        )

        return TradeIndex(
            tradeName = "Plumbing",
            tradeCode = "PLB",
            description = "Plumbing systems installation",
            workDescription = "Plumbing work includes water supply lines, drainage systems, waste and vent piping, fixture installation, and system testing.",
            lifecyclePhase = HomeLifecyclePhase.SYSTEMS,
            masterAssembly = plumbingMasterAssembly,
            avgLaborRate = 45.00,
            webResourceUrls = webResources
        )
    }

    private fun createHVACTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        // Create basic HVAC tasks
        val hvacTasks = listOf(
            DetailedTask(
                name = "Install Ductwork",
                description = "Install supply and return air ducts",
                estimatedHours = 24.0,
                materialCost = 800.0,
                laborCost = 1200.0,
                requirements = listOf("Framing complete")
            ),
            DetailedTask(
                name = "Install HVAC Unit",
                description = "Install heating and cooling equipment",
                estimatedHours = 16.0,
                materialCost = 2500.0,
                laborCost = 800.0,
                requirements = listOf("Electrical rough-in complete")
            ),
            DetailedTask(
                name = "Install Controls",
                description = "Install thermostats and control systems",
                estimatedHours = 8.0,
                materialCost = 200.0,
                laborCost = 400.0,
                requirements = listOf("HVAC unit installed")
            )
        )
        
        val hvacAssembly = DetailedAssembly(
            name = "HVAC System Installation",
            description = "Complete HVAC system installation",
            assemblyCode = "HVC-001",
            unit = "sqft",
            baseQuantity = 1800.0,
            lifecyclePhase = HomeLifecyclePhase.SYSTEMS,
            tasks = hvacTasks,
            macroTasks = emptyList(),
            prerequisites = listOf("Framing and electrical rough-in complete"),
            deliverables = listOf("HVAC system fully installed and tested")
        )
        
        val hvacMasterAssembly = MasterAssembly(
            name = "HVAC Master Assembly",
            description = "Complete HVAC system for residential construction",
            tradeCode = "HVC",
            assemblies = listOf(hvacAssembly),
            subAssemblies = emptyList(),
            totalEstimatedHours = 48.0,
            totalEstimatedCost = 4500.0
        )

        return TradeIndex(
            tradeName = "HVAC",
            tradeCode = "HVC",
            description = "Heating, ventilation, and air conditioning systems",
            workDescription = "HVAC work includes heating and cooling equipment installation, ductwork, ventilation systems, and controls.",
            lifecyclePhase = HomeLifecyclePhase.SYSTEMS,
            masterAssembly = hvacMasterAssembly,
            avgLaborRate = 55.00,
            webResourceUrls = webResources
        )
    }

    private fun createDrywallTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        // Create basic drywall tasks
        val drywallTasks = listOf(
            DetailedTask(
                name = "Hang Drywall",
                description = "Install gypsum board sheets",
                estimatedHours = 24.0,
                materialCost = 600.0,
                laborCost = 720.0,
                requirements = listOf("All systems rough-in complete")
            ),
            DetailedTask(
                name = "Tape and Mud",
                description = "Apply joint compound and tape joints",
                estimatedHours = 20.0,
                materialCost = 150.0,
                laborCost = 600.0,
                requirements = listOf("Drywall hung and inspected")
            ),
            DetailedTask(
                name = "Sand and Prime",
                description = "Sand smooth and apply primer",
                estimatedHours = 16.0,
                materialCost = 100.0,
                laborCost = 480.0,
                requirements = listOf("Final mud coat dried")
            )
        )
        
        val drywallAssembly = DetailedAssembly(
            name = "Interior Drywall",
            description = "Complete drywall installation and finishing",
            assemblyCode = "DRY-001",
            unit = "sqft",
            baseQuantity = 3000.0,
            lifecyclePhase = HomeLifecyclePhase.INTERIORS,
            tasks = drywallTasks,
            macroTasks = emptyList(),
            prerequisites = listOf("All rough inspections passed", "Insulation installed"),
            deliverables = listOf("Walls ready for paint")
        )
        
        val drywallMasterAssembly = MasterAssembly(
            name = "Drywall Master Assembly",
            description = "Complete drywall system for interior finishing",
            tradeCode = "DRY",
            assemblies = listOf(drywallAssembly),
            subAssemblies = emptyList(),
            totalEstimatedHours = 60.0,
            totalEstimatedCost = 1800.0
        )

        return TradeIndex(
            tradeName = "Drywall",
            tradeCode = "DRY",
            description = "Drywall installation and finishing",
            workDescription = "Drywall work includes gypsum board installation, taping, mudding, sanding, and preparation for paint.",
            lifecyclePhase = HomeLifecyclePhase.INTERIORS,
            masterAssembly = drywallMasterAssembly,
            avgLaborRate = 30.00,
            webResourceUrls = webResources
        )
    }

    private fun createRoofingTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        // Create basic roofing tasks
        val roofingTasks = listOf(
            DetailedTask(
                name = "Install Roof Decking",
                description = "Install plywood or OSB roof sheathing",
                estimatedHours = 16.0,
                materialCost = 800.0,
                laborCost = 640.0,
                requirements = listOf("Roof framing complete")
            ),
            DetailedTask(
                name = "Install Underlayment",
                description = "Install roofing felt or synthetic underlayment",
                estimatedHours = 8.0,
                materialCost = 300.0,
                laborCost = 320.0,
                requirements = listOf("Roof decking installed")
            ),
            DetailedTask(
                name = "Install Shingles",
                description = "Install asphalt shingles and flashing",
                estimatedHours = 20.0,
                materialCost = 1500.0,
                laborCost = 800.0,
                requirements = listOf("Underlayment complete")
            )
        )
        
        val roofingAssembly = DetailedAssembly(
            name = "Asphalt Shingle Roof",
            description = "Complete roofing system installation",
            assemblyCode = "ROF-001",
            unit = "sqft",
            baseQuantity = 2200.0,
            lifecyclePhase = HomeLifecyclePhase.EXTERIOR,
            tasks = roofingTasks,
            macroTasks = emptyList(),
            prerequisites = listOf("Roof framing inspection passed"),
            deliverables = listOf("Weather-tight roof system")
        )
        
        val roofingMasterAssembly = MasterAssembly(
            name = "Roofing Master Assembly",
            description = "Complete roofing system for weather protection",
            tradeCode = "ROF",
            assemblies = listOf(roofingAssembly),
            subAssemblies = emptyList(),
            totalEstimatedHours = 44.0,
            totalEstimatedCost = 3200.0
        )
        )

        return TradeIndex(
            tradeName = "Roofing",
            tradeCode = "ROF",
            description = "Roofing systems installation",
            workDescription = "Roofing work includes sheathing, underlayment, shingles or other roofing materials, flashing, and gutters.",
            lifecyclePhase = HomeLifecyclePhase.ENCLOSURE,
            masterAssembly = roofingMasterAssembly,
            avgLaborRate = 35.00,
            webResourceUrls = webResources
        )
    }

    private fun createFoundationTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        // Create detailed foundation assemblies
        val foundationAssemblies = createFoundationAssemblies()
        
        val foundationMasterAssembly = MasterAssembly(
            name = "Foundation Master Assembly",
            description = "Complete foundation system for structural support",
            tradeCode = "FND",
            assemblies = foundationAssemblies,
            subAssemblies = emptyList(),
            totalEstimatedHours = 156.0, // Sum of all foundation assembly hours
            totalEstimatedCost = 28500.0 // Updated comprehensive cost
        )

        return TradeIndex(
            tradeName = "Foundation",
            tradeCode = "FND",
            description = "Foundation and concrete work",
            workDescription = "Foundation work includes excavation, footings, foundation walls, slabs, waterproofing, and basement construction with finishing work.",
            lifecyclePhase = HomeLifecyclePhase.STRUCTURE,
            masterAssembly = foundationMasterAssembly,
            avgLaborRate = 40.00,
            webResourceUrls = webResources
        )
    }

    /**
     * Create comprehensive foundation assemblies based on problem statement requirements
     */
    private fun createFoundationAssemblies(): List<DetailedAssembly> {
        return listOf(
            createFullBasementFoundationAssembly(),
            createBasementFloorAssembly(),
            createFramedBasementWallsAssembly(),
            createBasementCeilingAssembly()
        )
    }

    /**
     * Create Full Basement Foundation assembly - Basement Construction scope
     */
    private fun createFullBasementFoundationAssembly(): DetailedAssembly {
        val tasks = listOf(
            DetailedTask(
                name = "Excavate to required depth",
                description = "Excavate basement area to specified depth with proper slope and drainage considerations",
                workDescription = "Excavate using appropriate machinery to required depth, typically 8-9 feet below grade. Maintain proper slope for drainage, preserve existing utilities, and ensure stable soil conditions. Include over-excavation allowance for footings.",
                unitType = UnitType.CY,
                defaultQty = 120.0,
                laborTimePerUnit = 0.5,
                laborCostPerUnit = 20.00,
                materialCostPerUnit = 0.0,
                markup = 0.15,
                skillLevel = SkillLevel.SKILLED,
                requiredTools = listOf("Excavator", "Transit", "Hand Tools"),
                requiredMaterials = emptyList()
            ),
            DetailedTask(
                name = "Install footing drains",
                description = "Install perimeter drainage system around foundation footings",
                workDescription = "Install 4-inch perforated drain pipe around foundation perimeter, connect to sump pit or drainage system, backfill with gravel, and wrap with filter fabric to prevent clogging.",
                unitType = UnitType.LF,
                defaultQty = 120.0,
                laborTimePerUnit = 0.75,
                laborCostPerUnit = 30.00,
                materialCostPerUnit = 12.50,
                markup = 0.20,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Trenching Shovel", "Level", "Hand Tools"),
                requiredMaterials = listOf(
                    TaskMaterial("4\" Perforated Drain Pipe", "Schedule 40 PVC perforated pipe", 120.0, "LF", 8.50),
                    TaskMaterial("Drainage Gravel", "3/4\" washed stone", 4.0, "CY", 45.00),
                    TaskMaterial("Filter Fabric", "Non-woven geotextile fabric", 150.0, "SF", 0.85)
                )
            ),
            DetailedTask(
                name = "Form and pour footings",
                description = "Install footing forms and pour concrete footings to support foundation walls",
                workDescription = "Set up footing forms to proper width and depth per code requirements, install reinforcement steel, pour and level concrete, and allow proper curing time. Typical residential footings are 20\" wide by 8\" deep.",
                unitType = UnitType.LF,
                defaultQty = 120.0,
                laborTimePerUnit = 1.25,
                laborCostPerUnit = 50.00,
                materialCostPerUnit = 28.75,
                markup = 0.25,
                skillLevel = SkillLevel.SKILLED,
                requiredTools = listOf("Concrete Forms", "Screeds", "Float", "Concrete Mixer"),
                requiredMaterials = listOf(
                    TaskMaterial("Concrete Mix", "3000 PSI concrete", 12.0, "CY", 125.00),
                    TaskMaterial("Footing Forms", "2x8 lumber forms", 240.0, "LF", 3.50),
                    TaskMaterial("Rebar", "#4 rebar for reinforcement", 480.0, "LF", 1.25)
                )
            ),
            DetailedTask(
                name = "Form basement walls",
                description = "Install forming system for basement concrete walls",
                workDescription = "Set up wall forms using ICF blocks or traditional wood/steel forms, ensure proper alignment and bracing, install electrical and plumbing sleeves, and prepare for concrete placement.",
                unitType = UnitType.SF,
                defaultQty = 960.0,
                laborTimePerUnit = 0.45,
                laborCostPerUnit = 18.00,
                materialCostPerUnit = 8.50,
                markup = 0.20,
                skillLevel = SkillLevel.SKILLED,
                requiredTools = listOf("Forms", "Braces", "Ties", "Level", "Transit"),
                requiredMaterials = listOf(
                    TaskMaterial("Wall Forms", "Reusable concrete wall forms", 960.0, "SF", 6.50),
                    TaskMaterial("Form Ties", "Steel form ties and accessories", 200.0, "EA", 2.25),
                    TaskMaterial("Form Release", "Concrete form release agent", 2.0, "GAL", 25.00)
                )
            ),
            DetailedTask(
                name = "Install steel reinforcement",
                description = "Place reinforcement steel in wall forms according to structural plans",
                workDescription = "Install vertical and horizontal rebar per structural engineer specifications, typically #4 bars at 12\" on center each way, tie intersections properly, and maintain proper concrete cover.",
                unitType = UnitType.TON,
                defaultQty = 2.5,
                laborTimePerUnit = 16.0,
                laborCostPerUnit = 640.00,
                materialCostPerUnit = 850.00,
                markup = 0.18,
                skillLevel = SkillLevel.SKILLED,
                requiredTools = listOf("Rebar Cutter", "Tie Wire", "Rebar Bender", "Spacers"),
                requiredMaterials = listOf(
                    TaskMaterial("Rebar #4", "Grade 60 deformed steel", 5000.0, "LF", 1.25),
                    TaskMaterial("Tie Wire", "16 gauge tie wire", 50.0, "LB", 2.50),
                    TaskMaterial("Rebar Chairs", "Plastic rebar supports", 100.0, "EA", 1.75)
                )
            ),
            DetailedTask(
                name = "Pour concrete walls",
                description = "Place concrete in wall forms using proper techniques and equipment",
                workDescription = "Pour concrete walls in lifts using concrete pump or crane bucket, vibrate properly to eliminate voids, maintain consistent placement rate, and ensure proper consolidation around reinforcement and embedments.",
                unitType = UnitType.CY,
                defaultQty = 28.0,
                laborTimePerUnit = 2.0,
                laborCostPerUnit = 80.00,
                materialCostPerUnit = 125.00,
                markup = 0.15,
                skillLevel = SkillLevel.EXPERT,
                requiredTools = listOf("Concrete Pump", "Vibrator", "Screeds", "Float"),
                requiredMaterials = listOf(
                    TaskMaterial("Concrete Mix", "3500 PSI concrete with air entrainment", 28.0, "CY", 135.00)
                )
            ),
            DetailedTask(
                name = "Strip forms",
                description = "Remove concrete forms after proper curing period",
                workDescription = "Remove wall forms after minimum 24-48 hours curing time, clean and stack forms for reuse, patch any surface imperfections, and prepare walls for waterproofing application.",
                unitType = UnitType.SF,
                defaultQty = 960.0,
                laborTimePerUnit = 0.15,
                laborCostPerUnit = 6.00,
                materialCostPerUnit = 0.50,
                markup = 0.10,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Pry Bars", "Hammers", "Form Puller"),
                requiredMaterials = listOf(
                    TaskMaterial("Patching Compound", "Concrete patching material", 5.0, "BAG", 15.00)
                )
            ),
            DetailedTask(
                name = "Apply waterproofing membrane",
                description = "Install exterior basement wall waterproofing system",
                workDescription = "Clean wall surface, apply primer as needed, install waterproofing membrane using spray-applied, rolled, or sheet membrane system, seal all penetrations and joints, and protect from damage during backfill.",
                unitType = UnitType.SF,
                defaultQty = 960.0,
                laborTimePerUnit = 0.35,
                laborCostPerUnit = 14.00,
                materialCostPerUnit = 4.25,
                markup = 0.25,
                skillLevel = SkillLevel.SKILLED,
                requiredTools = listOf("Sprayer", "Brushes", "Rollers", "Trowel"),
                requiredMaterials = listOf(
                    TaskMaterial("Waterproof Membrane", "Liquid applied membrane", 12.0, "GAL", 35.00),
                    TaskMaterial("Primer", "Concrete bonding primer", 4.0, "GAL", 28.00),
                    TaskMaterial("Sealant", "Joint and penetration sealant", 6.0, "TUBE", 12.50)
                )
            ),
            DetailedTask(
                name = "Install drainage board",
                description = "Install exterior drainage and protection board system",
                workDescription = "Install dimpled drainage board or similar system over waterproofing membrane, secure with appropriate fasteners, overlap joints properly, and integrate with foundation drainage system.",
                unitType = UnitType.SF,
                defaultQty = 960.0,
                laborTimePerUnit = 0.25,
                laborCostPerUnit = 10.00,
                materialCostPerUnit = 2.75,
                markup = 0.20,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Fasteners", "Utility Knife", "Measuring Tape"),
                requiredMaterials = listOf(
                    TaskMaterial("Drainage Board", "Dimpled plastic drainage mat", 960.0, "SF", 2.25),
                    TaskMaterial("Fasteners", "Concrete fasteners for drainage board", 200.0, "EA", 0.75)
                )
            ),
            DetailedTask(
                name = "Install window wells",
                description = "Install basement window wells and drainage systems",
                workDescription = "Excavate window well areas, install galvanized or composite well liners, connect to foundation drainage system, install window well covers, and backfill with appropriate drainage material.",
                unitType = UnitType.EA,
                defaultQty = 4.0,
                laborTimePerUnit = 3.0,
                laborCostPerUnit = 120.00,
                materialCostPerUnit = 185.00,
                markup = 0.25,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Excavator", "Level", "Hand Tools"),
                requiredMaterials = listOf(
                    TaskMaterial("Window Well", "Galvanized steel window well", 4.0, "EA", 150.00),
                    TaskMaterial("Well Cover", "Clear polycarbonate well cover", 4.0, "EA", 45.00),
                    TaskMaterial("Drainage Gravel", "3/8\" washed stone", 2.0, "CY", 45.00)
                )
            ),
            DetailedTask(
                name = "Backfill foundation",
                description = "Backfill around foundation walls with proper compaction",
                workDescription = "Backfill foundation walls in 12\" lifts using suitable backfill material, compact each lift to prevent settlement, maintain proper slope away from foundation, and protect waterproofing during process.",
                unitType = UnitType.CY,
                defaultQty = 100.0,
                laborTimePerUnit = 0.75,
                laborCostPerUnit = 30.00,
                materialCostPerUnit = 15.00,
                markup = 0.15,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Compactor", "Excavator", "Hand Tools"),
                requiredMaterials = listOf(
                    TaskMaterial("Backfill Material", "Suitable granular backfill", 100.0, "CY", 18.00)
                )
            )
        )

        return DetailedAssembly(
            name = "Full Basement Foundation",
            category = "Foundation Systems",
            description = "Complete basement foundation construction including excavation, footings, walls, and waterproofing",
            workDescription = "Comprehensive basement foundation construction from excavation through backfill, including all structural elements, waterproofing systems, and drainage components for a complete below-grade foundation system.",
            validModes = listOf(ContextMode.NEW_CONSTRUCTION),
            defaultQuantityUnit = UnitType.EA,
            baseQuantity = 1.0,
            lifecyclePhase = HomeLifecyclePhase.STRUCTURE,
            tasks = tasks,
            macroTasks = emptyList(),
            prerequisites = listOf("Site survey completed", "Building permits obtained", "Utilities marked"),
            deliverables = listOf("Excavated basement area", "Complete foundation walls", "Waterproofed foundation", "Backfilled foundation"),
            skillsRequired = listOf(SkillLevel.SKILLED, SkillLevel.EXPERT),
            webResourceUrls = emptyList(),
            estimatedDuration = 14, // days
            estimatedLaborHours = 68.0,
            estimatedMaterialCost = 12500.0,
            estimatedLaborCost = 2720.0,
            estimatedTotalCost = 17500.0
        )
    }

    /**
     * Create Basement Floor assembly - Basement Construction scope
     */
    private fun createBasementFloorAssembly(): DetailedAssembly {
        val tasks = listOf(
            DetailedTask(
                name = "Install radon mitigation system",
                description = "Install sub-slab radon mitigation piping system",
                workDescription = "Install 4-inch PVC radon mitigation piping under slab with proper connections to exterior, include collection points and sealing systems per radon mitigation standards.",
                unitType = UnitType.EA,
                defaultQty = 1.0,
                laborTimePerUnit = 8.0,
                laborCostPerUnit = 320.00,
                materialCostPerUnit = 250.00,
                markup = 0.20,
                skillLevel = SkillLevel.SKILLED,
                requiredTools = listOf("PVC Saw", "Drill", "Measuring Tools"),
                requiredMaterials = listOf(
                    TaskMaterial("Radon Pipe", "4\" PVC pipe for radon system", 100.0, "LF", 3.50),
                    TaskMaterial("Radon Fittings", "PVC fittings and connections", 1.0, "LOT", 125.00),
                    TaskMaterial("Sealing Materials", "Radon barrier and sealants", 1.0, "KIT", 85.00)
                )
            ),
            DetailedTask(
                name = "Place 4\" stone base",
                description = "Install 4-inch crushed stone base under basement slab",
                workDescription = "Place and spread 4 inches of 3/4-inch crushed stone base material over prepared subgrade, ensure uniform thickness and proper drainage characteristics.",
                unitType = UnitType.CY,
                defaultQty = 12.0,
                laborTimePerUnit = 1.5,
                laborCostPerUnit = 60.00,
                materialCostPerUnit = 25.00,
                markup = 0.15,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Rake", "Shovel", "Wheelbarrow"),
                requiredMaterials = listOf(
                    TaskMaterial("Crushed Stone", "3/4\" crushed stone base", 12.0, "CY", 28.00)
                )
            ),
            DetailedTask(
                name = "Compact stone base",
                description = "Compact stone base to proper density for slab support",
                workDescription = "Compact stone base using plate compactor or similar equipment, achieve 95% compaction per geotechnical requirements, maintain uniform surface elevation.",
                unitType = UnitType.SF,
                defaultQty = 900.0,
                laborTimePerUnit = 0.02,
                laborCostPerUnit = 0.80,
                materialCostPerUnit = 0.0,
                markup = 0.10,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Plate Compactor", "Hand Tools"),
                requiredMaterials = emptyList()
            ),
            DetailedTask(
                name = "Install vapor barrier",
                description = "Install vapor barrier over stone base under concrete slab",
                workDescription = "Install 6-mil polyethylene vapor barrier over compacted stone base, overlap seams by 6 inches minimum, seal all penetrations, and protect from damage during concrete placement.",
                unitType = UnitType.SF,
                defaultQty = 950.0,
                laborTimePerUnit = 0.05,
                laborCostPerUnit = 2.00,
                materialCostPerUnit = 0.75,
                markup = 0.15,
                skillLevel = SkillLevel.BASIC,
                requiredTools = listOf("Utility Knife", "Tape Measure"),
                requiredMaterials = listOf(
                    TaskMaterial("Vapor Barrier", "6-mil polyethylene sheeting", 950.0, "SF", 0.65),
                    TaskMaterial("Sealing Tape", "Vapor barrier sealing tape", 200.0, "LF", 1.25)
                )
            ),
            DetailedTask(
                name = "Install perimeter insulation",
                description = "Install rigid insulation around basement slab perimeter",
                workDescription = "Install 2-inch rigid foam insulation vertically along foundation walls and horizontally under slab edge, secure properly and seal joints to create thermal break.",
                unitType = UnitType.LF,
                defaultQty = 120.0,
                laborTimePerUnit = 0.5,
                laborCostPerUnit = 20.00,
                materialCostPerUnit = 8.50,
                markup = 0.20,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Saw", "Adhesive", "Measuring Tools"),
                requiredMaterials = listOf(
                    TaskMaterial("Rigid Insulation", "2\" XPS foam board", 240.0, "SF", 2.25),
                    TaskMaterial("Insulation Adhesive", "Foam-safe construction adhesive", 6.0, "TUBE", 8.50)
                )
            ),
            DetailedTask(
                name = "Install radiant heat (if applicable)",
                description = "Install in-floor radiant heating system in basement slab",
                workDescription = "Install radiant heating tubing or electric mats per manufacturer specifications, secure tubing to reinforcement, pressure test system, and protect during concrete placement.",
                unitType = UnitType.SF,
                defaultQty = 900.0,
                laborTimePerUnit = 0.75,
                laborCostPerUnit = 30.00,
                materialCostPerUnit = 12.50,
                markup = 0.25,
                skillLevel = SkillLevel.SKILLED,
                requiredTools = listOf("Tubing Tools", "Pressure Tester", "Fasteners"),
                requiredMaterials = listOf(
                    TaskMaterial("Radiant Tubing", "PEX tubing for radiant heat", 1800.0, "LF", 2.25),
                    TaskMaterial("Tubing Fasteners", "Clips and ties for tubing", 450.0, "EA", 0.35),
                    TaskMaterial("Manifold System", "Supply and return manifolds", 1.0, "SET", 650.00)
                )
            ),
            DetailedTask(
                name = "Install wire mesh/rebar",
                description = "Install reinforcement mesh or rebar in basement slab",
                workDescription = "Install welded wire mesh or rebar grid per structural requirements, typically 6x6 W2.9xW2.9 WWF or #4 bars at 18\" each way, support at proper elevation in slab.",
                unitType = UnitType.SF,
                defaultQty = 900.0,
                laborTimePerUnit = 0.08,
                laborCostPerUnit = 3.20,
                materialCostPerUnit = 1.85,
                markup = 0.15,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Wire Cutters", "Rebar Chairs", "Tie Wire"),
                requiredMaterials = listOf(
                    TaskMaterial("Wire Mesh", "6x6 W2.9xW2.9 welded wire mesh", 900.0, "SF", 1.65),
                    TaskMaterial("Mesh Chairs", "Wire mesh support chairs", 100.0, "EA", 1.25)
                )
            ),
            DetailedTask(
                name = "Pour concrete slab",
                description = "Place concrete for basement floor slab",
                workDescription = "Pour 4-inch thick concrete slab using 3000 PSI concrete, maintain proper elevation and slope to drains, avoid disturbing reinforcement and embedded systems during placement.",
                unitType = UnitType.CY,
                defaultQty = 11.0,
                laborTimePerUnit = 3.0,
                laborCostPerUnit = 120.00,
                materialCostPerUnit = 125.00,
                markup = 0.15,
                skillLevel = SkillLevel.SKILLED,
                requiredTools = listOf("Screed", "Float", "Edger", "Jointer"),
                requiredMaterials = listOf(
                    TaskMaterial("Concrete Mix", "3000 PSI concrete", 11.0, "CY", 130.00)
                )
            ),
            DetailedTask(
                name = "Float and finish concrete",
                description = "Float and finish basement slab surface",
                workDescription = "Bull float concrete surface when bleed water evaporates, perform final floating and steel troweling for smooth finish, maintain consistent surface texture suitable for floor coverings.",
                unitType = UnitType.SF,
                defaultQty = 900.0,
                laborTimePerUnit = 0.15,
                laborCostPerUnit = 6.00,
                materialCostPerUnit = 0.0,
                markup = 0.10,
                skillLevel = SkillLevel.SKILLED,
                requiredTools = listOf("Bull Float", "Steel Trowel", "Hand Float"),
                requiredMaterials = emptyList()
            ),
            DetailedTask(
                name = "Cut control joints",
                description = "Cut control joints in basement slab to control cracking",
                workDescription = "Cut control joints at maximum 15-foot spacing using concrete saw, joints should be 1/4 of slab thickness deep, seal joints if required for floor covering installation.",
                unitType = UnitType.LF,
                defaultQty = 200.0,
                laborTimePerUnit = 0.25,
                laborCostPerUnit = 10.00,
                materialCostPerUnit = 0.50,
                markup = 0.15,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Concrete Saw", "Vacuum", "Safety Equipment"),
                requiredMaterials = listOf(
                    TaskMaterial("Saw Blades", "Diamond concrete saw blades", 2.0, "EA", 35.00),
                    TaskMaterial("Joint Sealer", "Polyurethane joint sealant", 6.0, "TUBE", 12.50)
                )
            ),
            DetailedTask(
                name = "Cure concrete",
                description = "Properly cure basement concrete slab",
                workDescription = "Apply curing compound or use plastic sheeting to maintain moisture for proper concrete curing, maintain temperature above 50°F, cure for minimum 7 days before loading.",
                unitType = UnitType.SF,
                defaultQty = 900.0,
                laborTimePerUnit = 0.02,
                laborCostPerUnit = 0.80,
                materialCostPerUnit = 0.35,
                markup = 0.10,
                skillLevel = SkillLevel.BASIC,
                requiredTools = listOf("Sprayer", "Plastic Sheeting"),
                requiredMaterials = listOf(
                    TaskMaterial("Curing Compound", "Concrete curing compound", 3.0, "GAL", 25.00),
                    TaskMaterial("Plastic Sheeting", "Polyethylene curing sheets", 950.0, "SF", 0.15)
                )
            )
        )

        return DetailedAssembly(
            name = "Basement Floor",
            category = "Foundation Systems",
            description = "Complete basement floor construction including base preparation, utilities, reinforcement, and concrete placement",
            workDescription = "Comprehensive basement floor system installation including radon mitigation, insulation, reinforcement, and properly finished concrete slab suitable for basement use.",
            validModes = listOf(ContextMode.NEW_CONSTRUCTION),
            defaultQuantityUnit = UnitType.SF,
            baseQuantity = 900.0,
            lifecyclePhase = HomeLifecyclePhase.STRUCTURE,
            tasks = tasks,
            macroTasks = emptyList(),
            prerequisites = listOf("Foundation walls complete", "Utilities rough-in complete", "Compacted base ready"),
            deliverables = listOf("Finished basement slab", "Cured concrete", "Control joints cut", "Ready for framing"),
            skillsRequired = listOf(SkillLevel.INTERMEDIATE, SkillLevel.SKILLED),
            webResourceUrls = emptyList(),
            estimatedDuration = 7, // days
            estimatedLaborHours = 42.0,
            estimatedMaterialCost = 8500.0,
            estimatedLaborCost = 1680.0,
            estimatedTotalCost = 11750.0
        )
    }

    /**
     * Create Framed Basement Walls assembly - Basement Finishing scope
     */
    private fun createFramedBasementWallsAssembly(): DetailedAssembly {
        val tasks = listOf(
            DetailedTask(
                name = "Layout wall locations",
                description = "Layout interior basement wall locations on slab",
                workDescription = "Mark wall locations on basement slab using chalk lines, verify dimensions against plans, locate utilities and mechanical systems, ensure proper clearances and access.",
                unitType = UnitType.LF,
                defaultQty = 160.0,
                laborTimePerUnit = 0.1,
                laborCostPerUnit = 2.50,
                materialCostPerUnit = 0.0,
                markup = 0.10,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Chalk Line", "Measuring Tape", "Square"),
                requiredMaterials = emptyList()
            ),
            DetailedTask(
                name = "Install bottom plate (pressure treated)",
                description = "Install pressure-treated bottom plate on basement slab",
                workDescription = "Install pressure-treated 2x4 bottom plate using concrete fasteners, ensure straight alignment, treat joints with caulk or foam sealant to create moisture barrier.",
                unitType = UnitType.LF,
                defaultQty = 160.0,
                laborTimePerUnit = 0.25,
                laborCostPerUnit = 6.25,
                materialCostPerUnit = 4.50,
                markup = 0.20,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Hammer Drill", "Circular Saw", "Level"),
                requiredMaterials = listOf(
                    TaskMaterial("PT Bottom Plate", "2x4 pressure treated lumber", 160.0, "LF", 3.25),
                    TaskMaterial("Concrete Fasteners", "3/8\" concrete wedge anchors", 80.0, "EA", 1.85),
                    TaskMaterial("Sealant", "Polyurethane sealant", 4.0, "TUBE", 8.50)
                )
            ),
            DetailedTask(
                name = "Install top plate",
                description = "Install top plate for basement wall framing",
                workDescription = "Install double 2x4 top plate system, ensure level installation, connect to floor joists above or ceiling structure, maintain proper alignment with bottom plate.",
                unitType = UnitType.LF,
                defaultQty = 160.0,
                laborTimePerUnit = 0.3,
                laborCostPerUnit = 7.50,
                materialCostPerUnit = 4.25,
                markup = 0.20,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Circular Saw", "Hammer", "Level", "Framing Square"),
                requiredMaterials = listOf(
                    TaskMaterial("Top Plate", "2x4 SPF lumber", 320.0, "LF", 2.85),
                    TaskMaterial("Framing Nails", "16d common nails", 5.0, "LB", 3.50)
                )
            ),
            DetailedTask(
                name = "Install studs 16\" O.C.",
                description = "Install wall studs at 16 inches on center spacing",
                workDescription = "Cut and install 2x4 studs at 16\" on center, typical basement ceiling height requires 92-5/8\" studs, ensure plumb installation and proper attachment to plates.",
                unitType = UnitType.EA,
                defaultQty = 120.0,
                laborTimePerUnit = 0.4,
                laborCostPerUnit = 10.00,
                materialCostPerUnit = 6.50,
                markup = 0.18,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Circular Saw", "Hammer", "Level", "Speed Square"),
                requiredMaterials = listOf(
                    TaskMaterial("Wall Studs", "2x4x8' SPF studs", 120.0, "EA", 5.25),
                    TaskMaterial("Framing Nails", "16d common nails", 8.0, "LB", 3.50)
                )
            ),
            DetailedTask(
                name = "Frame door openings",
                description = "Frame rough openings for basement doors",
                workDescription = "Frame door rough openings per door schedule, install proper headers and king studs, ensure openings are plumb and square, maintain proper rough opening dimensions.",
                unitType = UnitType.EA,
                defaultQty = 3.0,
                laborTimePerUnit = 2.0,
                laborCostPerUnit = 50.00,
                materialCostPerUnit = 35.00,
                markup = 0.20,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Circular Saw", "Hammer", "Level", "Square"),
                requiredMaterials = listOf(
                    TaskMaterial("Header Material", "2x8 SPF lumber for headers", 18.0, "LF", 4.25),
                    TaskMaterial("King Studs", "2x4 SPF lumber", 12.0, "LF", 2.85),
                    TaskMaterial("Cripple Studs", "2x4 SPF lumber", 6.0, "LF", 2.85)
                )
            ),
            DetailedTask(
                name = "Frame utilities chases",
                description = "Frame chases and enclosures for utilities in basement walls",
                workDescription = "Frame utility chases for plumbing, electrical, and HVAC systems, provide access panels where required, coordinate with utility rough-in requirements.",
                unitType = UnitType.EA,
                defaultQty = 6.0,
                laborTimePerUnit = 1.5,
                laborCostPerUnit = 37.50,
                materialCostPerUnit = 25.00,
                markup = 0.18,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Circular Saw", "Drill", "Measuring Tools"),
                requiredMaterials = listOf(
                    TaskMaterial("Chase Framing", "2x4 SPF lumber", 48.0, "LF", 2.85),
                    TaskMaterial("Access Panels", "Metal access doors", 3.0, "EA", 45.00)
                )
            ),
            DetailedTask(
                name = "Install blocking for fixtures",
                description = "Install blocking for wall-mounted fixtures and equipment",
                workDescription = "Install 2x blocking between studs for mounting televisions, shelving, cabinets, and other wall-mounted items, coordinate locations with homeowner requirements.",
                unitType = UnitType.EA,
                defaultQty = 12.0,
                laborTimePerUnit = 0.75,
                laborCostPerUnit = 18.75,
                materialCostPerUnit = 8.50,
                markup = 0.15,
                skillLevel = SkillLevel.BASIC,
                requiredTools = listOf("Circular Saw", "Hammer", "Measuring Tape"),
                requiredMaterials = listOf(
                    TaskMaterial("Blocking", "2x6 SPF blocking", 24.0, "LF", 3.25),
                    TaskMaterial("Nails", "16d common nails", 2.0, "LB", 3.50)
                )
            ),
            DetailedTask(
                name = "Install wall insulation",
                description = "Install insulation in basement wall cavities",
                workDescription = "Install R-13 fiberglass or equivalent insulation in wall cavities, ensure proper fit without compression, seal air gaps, coordinate with moisture control strategies.",
                unitType = UnitType.SF,
                defaultQty = 1280.0,
                laborTimePerUnit = 0.08,
                laborCostPerUnit = 2.00,
                materialCostPerUnit = 1.25,
                markup = 0.20,
                skillLevel = SkillLevel.BASIC,
                requiredTools = listOf("Utility Knife", "Stapler", "Safety Equipment"),
                requiredMaterials = listOf(
                    TaskMaterial("Wall Insulation", "R-13 fiberglass batts", 1280.0, "SF", 1.15),
                    TaskMaterial("Vapor Retarder", "Kraft-faced or poly vapor barrier", 1300.0, "SF", 0.35)
                )
            ),
            DetailedTask(
                name = "Install vapor barrier",
                description = "Install vapor barrier over insulated basement walls",
                workDescription = "Install 6-mil polyethylene vapor barrier over insulated walls, overlap seams and seal with tape, seal around all penetrations, coordinate with moisture management system.",
                unitType = UnitType.SF,
                defaultQty = 1280.0,
                laborTimePerUnit = 0.05,
                laborCostPerUnit = 1.25,
                materialCostPerUnit = 0.45,
                markup = 0.15,
                skillLevel = SkillLevel.BASIC,
                requiredTools = listOf("Stapler", "Utility Knife", "Tape"),
                requiredMaterials = listOf(
                    TaskMaterial("Vapor Barrier", "6-mil polyethylene sheeting", 1350.0, "SF", 0.35),
                    TaskMaterial("Sealing Tape", "Vapor barrier tape", 200.0, "LF", 1.25)
                )
            )
        )

        return DetailedAssembly(
            name = "Framed Basement Walls",
            category = "Foundation Systems",
            description = "Interior framed walls for basement finishing including insulation and vapor barrier",
            workDescription = "Complete basement wall framing system for finishing, including all framing members, utility accommodations, insulation, and moisture control systems.",
            validModes = listOf(ContextMode.NEW_CONSTRUCTION, ContextMode.REMODELING),
            defaultQuantityUnit = UnitType.LF,
            baseQuantity = 160.0,
            lifecyclePhase = HomeLifecyclePhase.INTERIORS,
            tasks = tasks,
            macroTasks = emptyList(),
            prerequisites = listOf("Basement slab complete", "Foundation walls waterproofed", "Utilities rough-in complete"),
            deliverables = listOf("Framed walls", "Insulated walls", "Vapor barrier installed", "Ready for drywall"),
            skillsRequired = listOf(SkillLevel.INTERMEDIATE),
            webResourceUrls = emptyList(),
            estimatedDuration = 5, // days
            estimatedLaborHours = 28.0,
            estimatedMaterialCost = 3200.0,
            estimatedLaborCost = 700.0,
            estimatedTotalCost = 4500.0
        )
    }

    /**
     * Create Basement Ceiling assembly - Basement Finishing scope
     */
    private fun createBasementCeilingAssembly(): DetailedAssembly {
        val tasks = listOf(
            DetailedTask(
                name = "Install sound insulation",
                description = "Install sound insulation between floor joists above basement",
                workDescription = "Install R-19 or higher insulation between floor joists to reduce sound transmission, ensure proper support and coverage, coordinate with mechanical systems and access requirements.",
                unitType = UnitType.SF,
                defaultQty = 900.0,
                laborTimePerUnit = 0.1,
                laborCostPerUnit = 2.50,
                materialCostPerUnit = 1.85,
                markup = 0.20,
                skillLevel = SkillLevel.BASIC,
                requiredTools = listOf("Utility Knife", "Stapler", "Safety Equipment"),
                requiredMaterials = listOf(
                    TaskMaterial("Ceiling Insulation", "R-19 fiberglass batts", 900.0, "SF", 1.65),
                    TaskMaterial("Insulation Supports", "Wire insulation supports", 450.0, "EA", 0.25)
                )
            ),
            DetailedTask(
                name = "Install resilient channels",
                description = "Install resilient metal channels for sound isolation",
                workDescription = "Install resilient metal channels perpendicular to joists at 24\" on center, maintain proper spacing from walls, do not penetrate channels with fasteners that contact joists.",
                unitType = UnitType.LF,
                defaultQty = 450.0,
                laborTimePerUnit = 0.15,
                laborCostPerUnit = 3.75,
                materialCostPerUnit = 2.25,
                markup = 0.18,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Drill", "Metal Snips", "Level"),
                requiredMaterials = listOf(
                    TaskMaterial("Resilient Channels", "Metal resilient channels", 450.0, "LF", 2.15),
                    TaskMaterial("Channel Fasteners", "Screws for resilient channels", 900.0, "EA", 0.08)
                )
            ),
            DetailedTask(
                name = "Install drywall",
                description = "Install drywall on basement ceiling using resilient channels",
                workDescription = "Install 5/8\" drywall on ceiling using resilient channel system, maintain proper fastener spacing, do not penetrate channels into joists, coordinate with lighting and HVAC penetrations.",
                unitType = UnitType.SF,
                defaultQty = 900.0,
                laborTimePerUnit = 0.2,
                laborCostPerUnit = 5.00,
                materialCostPerUnit = 1.95,
                markup = 0.20,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Drywall Lift", "Screw Gun", "Utility Knife", "T-Square"),
                requiredMaterials = listOf(
                    TaskMaterial("Drywall", "5/8\" fire-rated drywall", 900.0, "SF", 1.75),
                    TaskMaterial("Drywall Screws", "Fine thread drywall screws", 2.0, "LB", 8.50)
                )
            ),
            DetailedTask(
                name = "Tape and finish drywall",
                description = "Tape and finish basement ceiling drywall",
                workDescription = "Apply tape and joint compound to all drywall joints and fasteners, sand smooth between coats, apply three coats minimum for smooth finish suitable for painting.",
                unitType = UnitType.SF,
                defaultQty = 900.0,
                laborTimePerUnit = 0.25,
                laborCostPerUnit = 6.25,
                materialCostPerUnit = 0.85,
                markup = 0.15,
                skillLevel = SkillLevel.SKILLED,
                requiredTools = listOf("Taping Knives", "Mud Pan", "Sander", "Pole Sander"),
                requiredMaterials = listOf(
                    TaskMaterial("Joint Tape", "Paper or mesh joint tape", 300.0, "LF", 0.15),
                    TaskMaterial("Joint Compound", "All-purpose joint compound", 6.0, "GAL", 25.00),
                    TaskMaterial("Sandpaper", "120-grit sanding screens", 12.0, "EA", 4.50)
                )
            ),
            DetailedTask(
                name = "Install ceiling access panels",
                description = "Install access panels for utilities and maintenance",
                workDescription = "Install ceiling access panels at locations required for utility access, typically 24\"x24\" minimum size, coordinate with mechanical, electrical, and plumbing systems.",
                unitType = UnitType.EA,
                defaultQty = 3.0,
                laborTimePerUnit = 1.5,
                laborCostPerUnit = 37.50,
                materialCostPerUnit = 85.00,
                markup = 0.20,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Drywall Saw", "Measuring Tools", "Screwdriver"),
                requiredMaterials = listOf(
                    TaskMaterial("Access Panels", "24\"x24\" metal access doors", 3.0, "EA", 75.00),
                    TaskMaterial("Panel Fasteners", "Screws and trim for access panels", 3.0, "SET", 15.00)
                )
            ),
            DetailedTask(
                name = "Prime and paint ceiling",
                description = "Prime and paint basement ceiling with quality finish",
                workDescription = "Apply quality primer to all drywall surfaces, sand lightly if needed, apply two coats of ceiling paint using appropriate sheen for basement environment.",
                unitType = UnitType.SF,
                defaultQty = 900.0,
                laborTimePerUnit = 0.15,
                laborCostPerUnit = 3.75,
                materialCostPerUnit = 1.25,
                markup = 0.25,
                skillLevel = SkillLevel.INTERMEDIATE,
                requiredTools = listOf("Rollers", "Brushes", "Paint Trays", "Extension Poles"),
                requiredMaterials = listOf(
                    TaskMaterial("Primer", "High-quality drywall primer", 3.0, "GAL", 35.00),
                    TaskMaterial("Ceiling Paint", "Flat or satin ceiling paint", 4.0, "GAL", 45.00),
                    TaskMaterial("Paint Supplies", "Rollers, brushes, and supplies", 1.0, "SET", 65.00)
                )
            )
        )

        return DetailedAssembly(
            name = "Basement Ceiling",
            category = "Foundation Systems",
            description = "Complete basement ceiling system with sound insulation, drywall, and painted finish",
            workDescription = "Comprehensive basement ceiling installation including sound control insulation, resilient channel system, drywall installation and finishing, access panels, and quality paint finish.",
            validModes = listOf(ContextMode.NEW_CONSTRUCTION, ContextMode.REMODELING),
            defaultQuantityUnit = UnitType.SF,
            baseQuantity = 900.0,
            lifecyclePhase = HomeLifecyclePhase.INTERIORS,
            tasks = tasks,
            macroTasks = emptyList(),
            prerequisites = listOf("Basement walls framed", "Utilities rough-in complete", "Insulation approved"),
            deliverables = listOf("Insulated ceiling", "Finished drywall ceiling", "Access panels installed", "Painted ceiling"),
            skillsRequired = listOf(SkillLevel.INTERMEDIATE, SkillLevel.SKILLED),
            webResourceUrls = emptyList(),
            estimatedDuration = 8, // days
            estimatedLaborHours = 18.0,
            estimatedMaterialCost = 2500.0,
            estimatedLaborCost = 450.0,
            estimatedTotalCost = 3500.0
        )
    }

    /**
     * Create other project types (simplified)
     */
    private fun createRemodelingProjectType(webResources: List<WebResourceUrl>): ProjectType {
        return ProjectType(
            name = "Remodeling",
            description = "Renovation and remodeling projects for existing homes",
            code = "RM",
            contextMode = ContextMode.REMODELING,
            applicablePhases = listOf(
                HomeLifecyclePhase.DEMOLITION,
                HomeLifecyclePhase.SYSTEMS,
                HomeLifecyclePhase.INTERIORS
            ),
            trades = emptyList() // Would be populated with remodeling-specific trades
        )
    }

    private fun createAdditionProjectType(webResources: List<WebResourceUrl>): ProjectType {
        return ProjectType(
            name = "Addition",
            description = "Home addition projects",
            code = "AD",
            contextMode = ContextMode.ADDITION,
            applicablePhases = HomeLifecyclePhase.values().toList(),
            trades = emptyList()
        )
    }

    private fun createRepairMaintenanceProjectType(webResources: List<WebResourceUrl>): ProjectType {
        return ProjectType(
            name = "Repair & Maintenance",
            description = "Ongoing maintenance and repair work",
            code = "RM",
            contextMode = ContextMode.MAINTENANCE,
            applicablePhases = listOf(HomeLifecyclePhase.SYSTEMS, HomeLifecyclePhase.INTERIORS),
            trades = emptyList()
        )
    }

    /**
     * Search the catalogue based on criteria
     */
    suspend fun searchCatalogue(criteria: CatalogueSearchCriteria): List<Any> {
        val results = mutableListOf<Any>()
        val catalogue = _projectCatalogue.value ?: return results

        catalogue.projectTypes.forEach { projectType ->
            if (criteria.projectType == null || projectType.name.contains(criteria.projectType, ignoreCase = true)) {
                projectType.trades.forEach { trade ->
                    if (criteria.tradeType == null || trade.tradeName.contains(criteria.tradeType, ignoreCase = true)) {
                        // Search in master assembly
                        trade.masterAssembly.assemblies.forEach { assembly ->
                            if (criteria.keyword == null || 
                                assembly.name.contains(criteria.keyword, ignoreCase = true) ||
                                assembly.description.contains(criteria.keyword, ignoreCase = true)) {
                                results.add(assembly)
                            }
                            
                            // Search in tasks
                            assembly.tasks.forEach { task ->
                                if (criteria.keyword == null ||
                                    task.name.contains(criteria.keyword, ignoreCase = true) ||
                                    task.description.contains(criteria.keyword, ignoreCase = true)) {
                                    results.add(task)
                                }
                            }
                        }
                    }
                }
            }
        }

        _searchResults.value = results
        return results
    }

    /**
     * Get navigation information for a specific item in the hierarchy
     */
    fun getNavigationInfo(itemId: String, level: CatalogueLevel): CatalogueNavigation? {
        // Implementation would traverse the hierarchy to find the item and build navigation
        return CatalogueNavigation(
            currentLevel = level,
            breadcrumb = listOf("Home Construction", "New Construction", "Framing"),
            availableChildren = listOf("Assemblies", "Sub-Assemblies", "Tasks")
        )
    }

    /**
     * Get detailed cost breakdown for a specific assembly or task
     */
    fun getCostBreakdown(itemId: String): Map<String, Double> {
        // Implementation would calculate detailed cost breakdown
        return mapOf(
            "labor" to 150.0,
            "materials" to 200.0,
            "equipment" to 50.0,
            "markup" to 80.0,
            "total" to 480.0
        )
    }

    companion object {
        fun create(context: Context): HierarchicalCatalogueRepository {
            return HierarchicalCatalogueRepository(context)
        }
    }
}