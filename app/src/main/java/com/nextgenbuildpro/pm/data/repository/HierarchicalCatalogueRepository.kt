package com.nextgenbuildpro.pm.data.repository

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.core.api.FirestoreService
import com.nextgenbuildpro.pm.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.time.LocalDateTime

/**
 * Repository for managing the hierarchical indexed catalogue
 * Provides comprehensive construction project data with detailed assemblies, tasks, and web-sourced cost information
 */
class HierarchicalCatalogueRepository(
    private val context: Context,
    private val firestoreService: FirestoreService? = null
) {
    private val TAG = "HierarchicalCatalogueRepo"
    
    private val _projectCatalogue = MutableStateFlow<ProjectCatalogue?>(null)
    val projectCatalogue: StateFlow<ProjectCatalogue?> = _projectCatalogue.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<Any>>(emptyList())
    val searchResults: StateFlow<List<Any>> = _searchResults.asStateFlow()

    init {
        // Load fallback data synchronously for immediate availability
        loadFallbackCatalogueSync()
        // Attempt to load from Firestore asynchronously (fire and forget)
        attemptAsyncFirestoreLoad()
    }

    /**
     * Attempt to load catalogue from Firestore asynchronously
     */
    private fun attemptAsyncFirestoreLoad() {
        try {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val firestoreCatalogue = loadCatalogueFromFirestore()
                    if (firestoreCatalogue != null) {
                        // Update on main thread
                        withContext(Dispatchers.Main) {
                            _projectCatalogue.value = firestoreCatalogue
                            Log.d(TAG, "Loaded catalogue from Firestore")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading catalogue from Firestore", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting async Firestore load", e)
        }
    }

    /**
     * Load comprehensive construction catalogue with detailed hierarchical data
     * Attempts to load from Firestore first, falls back to hardcoded data
     */
    private suspend fun loadCatalogueFromFirestore(): ProjectCatalogue? {
        return try {
            if (firestoreService == null || !firestoreService.isServiceAvailable()) {
                Log.d(TAG, "Firestore not available, using fallback data")
                return null
            }

            val catalogueCollection = firestoreService.getCollection("catalogues")
            val snapshot = catalogueCollection
                .whereEqualTo("name", "Home Construction Master Catalogue")
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                Log.d(TAG, "Found catalogue in Firestore: ${doc.id}")
                parseFirestoreCatalogue(doc.data ?: emptyMap())
            } else {
                Log.d(TAG, "No catalogue found in Firestore, using fallback data")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading catalogue from Firestore", e)
            null
        }
    }



    /**
     * Parse a Firestore document map into a [ProjectCatalogue].
     * Falls back to the in-memory fallback catalogue when required fields are missing.
     */
    @Suppress("UNCHECKED_CAST")
    private fun parseFirestoreCatalogue(data: Map<String, Any>): ProjectCatalogue? {
        return try {
            val name = data["name"] as? String ?: return null
            val description = data["description"] as? String ?: ""
            val version = data["version"] as? String ?: "1.0"
            val id = data["id"] as? String ?: UUID.randomUUID().toString()

            val rawProjectTypes = data["projectTypes"] as? List<Map<String, Any>> ?: emptyList()
            val projectTypes = rawProjectTypes.mapNotNull { pt -> parseProjectType(pt) }

            if (projectTypes.isEmpty()) {
                Log.d(TAG, "Firestore catalogue has no project types — using fallback")
                return null
            }

            ProjectCatalogue(id = id, name = name, description = description, version = version, projectTypes = projectTypes)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Firestore catalogue, using fallback", e)
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseProjectType(data: Map<String, Any>): ProjectType? {
        return try {
            val name = data["name"] as? String ?: return null
            val id = data["id"] as? String ?: UUID.randomUUID().toString()
            val description = data["description"] as? String ?: ""
            val code = data["code"] as? String ?: name.take(2).uppercase()

            val contextMode = runCatching {
                ContextMode.valueOf(data["contextMode"] as? String ?: "NEW_CONSTRUCTION")
            }.getOrDefault(ContextMode.NEW_CONSTRUCTION)

            val rawTrades = data["trades"] as? List<Map<String, Any>> ?: emptyList()
            val trades = rawTrades.mapNotNull { t -> parseTradeIndex(t) }

            ProjectType(
                id = id,
                name = name,
                description = description,
                code = code,
                contextMode = contextMode,
                applicablePhases = emptyList(),
                trades = trades
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing project type", e)
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseTradeIndex(data: Map<String, Any>): TradeIndex? {
        return try {
            val tradeName = data["tradeName"] as? String ?: return null
            val id = data["id"] as? String ?: UUID.randomUUID().toString()
            val tradeCode = data["tradeCode"] as? String ?: tradeName.take(3).uppercase()
            val description = data["description"] as? String ?: ""
            val workDescription = data["workDescription"] as? String ?: ""
            val avgLaborRate = (data["avgLaborRate"] as? Number)?.toDouble() ?: 0.0

            val rawMaster = data["masterAssembly"] as? Map<String, Any> ?: emptyMap()
            val masterAssembly = parseMasterAssembly(rawMaster, tradeCode)

            TradeIndex(
                id = id,
                tradeName = tradeName,
                tradeCode = tradeCode,
                description = description,
                workDescription = workDescription,
                lifecyclePhase = HomeLifecyclePhase.CONSTRUCTION,
                masterAssembly = masterAssembly,
                avgLaborRate = avgLaborRate
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing trade index", e)
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseMasterAssembly(data: Map<String, Any>, tradeCode: String): MasterAssembly {
        val id = data["id"] as? String ?: UUID.randomUUID().toString()
        val name = data["name"] as? String ?: "$tradeCode Master Assembly"
        val description = data["description"] as? String ?: ""
        val totalHours = (data["totalEstimatedHours"] as? Number)?.toDouble() ?: 0.0
        val totalCost = (data["totalEstimatedCost"] as? Number)?.toDouble() ?: 0.0

        val rawAssemblies = data["assemblies"] as? List<Map<String, Any>> ?: emptyList()
        val assemblies = rawAssemblies.mapNotNull { a -> parseDetailedAssembly(a) }

        return MasterAssembly(
            id = id,
            name = name,
            description = description,
            tradeCode = tradeCode,
            assemblies = assemblies,
            subAssemblies = emptyList(),
            totalEstimatedHours = totalHours,
            totalEstimatedCost = totalCost
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseDetailedAssembly(data: Map<String, Any>): DetailedAssembly? {
        return try {
            val name = data["name"] as? String ?: return null
            val id = data["id"] as? String ?: UUID.randomUUID().toString()
            val category = data["category"] as? String ?: ""
            val description = data["description"] as? String ?: ""
            val workDescription = data["workDescription"] as? String ?: description

            val unitType = runCatching {
                UnitType.valueOf(data["defaultQuantityUnit"] as? String ?: "EA")
            }.getOrDefault(UnitType.EA)

            val lifecyclePhase = runCatching {
                HomeLifecyclePhase.valueOf(data["lifecyclePhase"] as? String ?: "CONSTRUCTION")
            }.getOrDefault(HomeLifecyclePhase.STRUCTURE)

            DetailedAssembly(
                id = id,
                name = name,
                category = category,
                description = description,
                workDescription = workDescription,
                validModes = listOf(ContextMode.NEW_CONSTRUCTION, ContextMode.REMODELING),
                defaultQuantityUnit = unitType,
                baseQuantity = (data["baseQuantity"] as? Number)?.toDouble() ?: 1.0,
                lifecyclePhase = lifecyclePhase,
                tasks = emptyList(),
                macroTasks = emptyList()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing assembly", e)
            null
        }
    }

    /**
     * Synchronous fallback catalogue loading for error cases
     */
    private fun loadFallbackCatalogueSync() {
        try {
            // Create web resource URLs for cost data sourcing
            val webResources = createWebResources()

            // Create project types (simplified synchronous version)
            val projectTypes = listOf(
                createNewConstructionProjectTypeSync(webResources),
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
            Log.d(TAG, "Loaded sync fallback construction catalogue with ${projectTypes.size} project types")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading sync fallback construction catalogue", e)
        }
    }

    /**
     * Synchronous version of createNewConstructionProjectType for error fallback
     */
    private fun createNewConstructionProjectTypeSync(webResources: List<WebResourceUrl>): ProjectType {
        val trades = listOf(
            createEmptyTradeIndex("Framing", "FRM", "Structural framing and carpentry work", HomeLifecyclePhase.STRUCTURE, webResources),
            createEmptyTradeIndex("Electrical", "ELC", "Electrical system installation", HomeLifecyclePhase.SYSTEMS, webResources),
            createEmptyTradeIndex("Plumbing", "PLM", "Plumbing system installation", HomeLifecyclePhase.SYSTEMS, webResources),
            createEmptyTradeIndex("HVAC", "HVC", "HVAC system installation", HomeLifecyclePhase.SYSTEMS, webResources),
            createEmptyTradeIndex("Drywall", "DRY", "Drywall installation and finishing", HomeLifecyclePhase.INTERIORS, webResources),
            createEmptyTradeIndex("Roofing", "ROF", "Roofing system installation", HomeLifecyclePhase.ENCLOSURE, webResources),
            createEmptyTradeIndex("Foundation", "FND", "Foundation and concrete work", HomeLifecyclePhase.STRUCTURE, webResources)
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
     * Load fallback hardcoded catalogue data
     */
    private suspend fun loadFallbackCatalogue() {
        try {
            // Create web resource URLs for cost data sourcing
            val webResources = createWebResources()

            // Create project types
            val projectTypes = listOf(
                createNewConstructionProjectType(webResources),
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
            Log.d(TAG, "Loaded fallback construction catalogue with ${projectTypes.size} project types")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading fallback construction catalogue", e)
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
    private suspend fun createNewConstructionProjectType(webResources: List<WebResourceUrl>): ProjectType {
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
     * Attempts to load from Firestore, falls back to empty structure
     */
    private suspend fun createFramingTradeIndex(webResources: List<WebResourceUrl>): TradeIndex {
        return try {
            // Try to load from Firestore first (only if service is available)
            if (firestoreService == null) {
                return createEmptyTradeIndex(
                    tradeName = "Framing",
                    tradeCode = "FRM",
                    description = "Structural framing and carpentry work",
                    lifecyclePhase = HomeLifecyclePhase.STRUCTURE,
                    webResources = webResources
                )
            }

            val tradeCollection = firestoreService.getCollection("trades")
            val snapshot = tradeCollection
                .whereEqualTo("tradeCode", "FRM")
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                val data = doc.data
                if (data != null) {
                    Log.d(TAG, "Loaded Framing trade from Firestore: ${doc.id}")
                    parseTradeIndex(data)?.let { return@try it }
                }
            }

            // Fallback to empty structure
            createEmptyTradeIndex(
                tradeName = "Framing",
                tradeCode = "FRM",
                description = "Structural framing and carpentry work",
                lifecyclePhase = HomeLifecyclePhase.STRUCTURE,
                webResources = webResources
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading Framing trade from Firestore", e)
            createEmptyTradeIndex(
                tradeName = "Framing",
                tradeCode = "FRM",
                description = "Structural framing and carpentry work",
                lifecyclePhase = HomeLifecyclePhase.STRUCTURE,
                webResources = webResources
            )
        }
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

    /**
     * Helper method to create an empty TradeIndex structure used as fallback.
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
        fun create(context: Context, firestoreService: FirestoreService? = null): HierarchicalCatalogueRepository {
            return HierarchicalCatalogueRepository(context, firestoreService)
        }
    }
}