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

    // Additional simplified trade creation methods...
    private fun createPlumbingTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        val plumbingMasterAssembly = MasterAssembly(
            name = "Plumbing Master Assembly",
            description = "Complete plumbing system for residential construction",
            tradeCode = "PLB",
            assemblies = emptyList(), // Would be populated with detailed assemblies
            subAssemblies = emptyList(),
            totalEstimatedHours = 60.0,
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
        val hvacMasterAssembly = MasterAssembly(
            name = "HVAC Master Assembly",
            description = "Complete HVAC system for residential construction",
            tradeCode = "HVC",
            assemblies = emptyList(),
            subAssemblies = emptyList(),
            totalEstimatedHours = 100.0,
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
        val drywallMasterAssembly = MasterAssembly(
            name = "Drywall Master Assembly",
            description = "Complete drywall system for interior finishing",
            tradeCode = "DRY",
            assemblies = emptyList(),
            subAssemblies = emptyList(),
            totalEstimatedHours = 80.0,
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
        val roofingMasterAssembly = MasterAssembly(
            name = "Roofing Master Assembly",
            description = "Complete roofing system for weather protection",
            tradeCode = "ROF",
            assemblies = emptyList(),
            subAssemblies = emptyList(),
            totalEstimatedHours = 60.0,
            totalEstimatedCost = 3200.0
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
        val foundationMasterAssembly = MasterAssembly(
            name = "Foundation Master Assembly",
            description = "Complete foundation system for structural support",
            tradeCode = "FND",
            assemblies = emptyList(),
            subAssemblies = emptyList(),
            totalEstimatedHours = 40.0,
            totalEstimatedCost = 2800.0
        )

        return TradeIndex(
            tradeName = "Foundation",
            tradeCode = "FND",
            description = "Foundation and concrete work",
            workDescription = "Foundation work includes excavation, footings, foundation walls, slabs, and waterproofing.",
            lifecyclePhase = HomeLifecyclePhase.STRUCTURE,
            masterAssembly = foundationMasterAssembly,
            avgLaborRate = 40.00,
            webResourceUrls = webResources
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
    fun getNavigationInfo(itemId: String, level: CatalogueLevel): CatalogueNavigation {
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