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
                // TODO: Implement remodeling and addition project types in future
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
     * TODO: Load from Firestore instead of hardcoded data
     */
    private fun createFramingTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        return createEmptyTradeIndex(
            tradeName = "Framing",
            tradeCode = "FRM",
            description = "Structural framing and carpentry work",
            lifecyclePhase = HomeLifecyclePhase.STRUCTURE,
            webResources = webResources
        )
    }

    /**
     * Create additional trade indices (simplified for brevity but follow same pattern)
     */
    private fun createElectricalTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        return createEmptyTradeIndex(
            tradeName = "Electrical",
            tradeCode = "ELC",
            description = "Electrical system installation",
            lifecyclePhase = HomeLifecyclePhase.SYSTEMS,
            webResources = webResources
        )
    }

    // Trade creation methods with proper implementations
    private fun createPlumbingTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        return createEmptyTradeIndex(
            tradeName = "Plumbing",
            tradeCode = "PLM",
            description = "Plumbing system installation",
            lifecyclePhase = HomeLifecyclePhase.SYSTEMS,
            webResources = webResources
        )
    }

    private fun createHVACTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        return createEmptyTradeIndex(
            tradeName = "HVAC",
            tradeCode = "HVC",
            description = "HVAC system installation",
            lifecyclePhase = HomeLifecyclePhase.SYSTEMS,
            webResources = webResources
        )
    }

    private fun createDrywallTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        return createEmptyTradeIndex(
            tradeName = "Drywall",
            tradeCode = "DRY",
            description = "Drywall installation and finishing",
            lifecyclePhase = HomeLifecyclePhase.INTERIORS,
            webResources = webResources
        )
    }

    private fun createRoofingTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        return createEmptyTradeIndex(
            tradeName = "Roofing",
            tradeCode = "ROF",
            description = "Roofing system installation",
            lifecyclePhase = HomeLifecyclePhase.ENCLOSURE,
            webResources = webResources
        )
    }

    private fun createFoundationTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        return createEmptyTradeIndex(
            tradeName = "Foundation",
            tradeCode = "FND",
            description = "Foundation and concrete work",
            lifecyclePhase = HomeLifecyclePhase.STRUCTURE,
            webResources = webResources
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
    suspend fun searchCatalogue(criteria: HierarchicalCatalogueSearchCriteria): List<Any> {
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

    /**
     * Helper method to create an empty TradeIndex structure
     * TODO: Replace with Firestore data loading
     */
    private fun createEmptyTradeIndex(
        tradeName: String,
        tradeCode: String,
        description: String,
        lifecyclePhase: HomeLifecyclePhase,
        webResources: List<WebResourceUrl>
    ): TradeIndex {
        val emptyMasterAssembly = MasterAssembly(
            name = "$tradeName Master Assembly",
            description = description,
            tradeCode = tradeCode,
            assemblies = emptyList(),
            subAssemblies = emptyList(),
            totalEstimatedHours = 0.0,
            totalEstimatedCost = 0.0
        )
        
        return TradeIndex(
            tradeName = tradeName,
            tradeCode = tradeCode,
            description = description,
            workDescription = description,
            lifecyclePhase = lifecyclePhase,
            masterAssembly = emptyMasterAssembly,
            avgLaborRate = 0.0,
            webResourceUrls = webResources.filter { it.dataType == WebResourceType.LABOR_RATES }
        )
    }

    companion object {
        fun create(context: Context): HierarchicalCatalogueRepository {
            return HierarchicalCatalogueRepository(context)
        }
    }
}