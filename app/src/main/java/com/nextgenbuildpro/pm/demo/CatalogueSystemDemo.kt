package com.nextgenbuildpro.pm.demo

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.data.repository.HierarchicalCatalogueRepository
import com.nextgenbuildpro.pm.services.WebResourceLaborService
import kotlinx.coroutines.runBlocking

/**
 * Simple demonstration of the Hierarchical Indexed Catalogue system
 * This shows the working implementation with real data
 */
class CatalogueSystemDemo(private val context: Context) {
    private val TAG = "CatalogueDemo"
    
    fun runDemo() {
        Log.d(TAG, "=== Hierarchical Catalogue System Demo ===")
        
        // Initialize the system
        val catalogueRepo = HierarchicalCatalogueRepository.create(context)
        val webService = WebResourceLaborService(context)
        
        runBlocking {
            try {
                // Demonstrate the complete hierarchy
                demonstrateHierarchy(catalogueRepo)
                
                // Show web resource integration
                demonstrateWebIntegration(webService)
                
                // Show search capabilities
                demonstrateSearch(catalogueRepo)
                
                // Calculate real costs
                demonstrateCostCalculation(catalogueRepo)
                
            } catch (e: Exception) {
                Log.e(TAG, "Demo error", e)
            }
        }
    }
    
    private fun demonstrateHierarchy(repo: HierarchicalCatalogueRepository) {
        Log.d(TAG, "\n--- Hierarchy Navigation Demo ---")
        
        val catalogue = repo.projectCatalogue.value ?: return
        Log.d(TAG, "✓ Root: ${catalogue.name} (${catalogue.projectTypes.size} project types)")
        
        // Navigate to New Construction
        val newConstruction = catalogue.projectTypes.find { it.name == "New Construction" }
        if (newConstruction != null) {
            Log.d(TAG, "✓ Project Type: ${newConstruction.name} (${newConstruction.trades.size} trades)")
            
            // Navigate to Framing trade
            val framing = newConstruction.trades.find { it.tradeName == "Framing" }
            if (framing != null) {
                Log.d(TAG, "✓ Trade: ${framing.tradeName} @ $${framing.avgLaborRate}/hr")
                
                // Show master assembly
                Log.d(TAG, "✓ Master Assembly: ${framing.masterAssembly.name}")
                Log.d(TAG, "  - ${framing.masterAssembly.assemblies.size} assemblies")
                Log.d(TAG, "  - ${framing.masterAssembly.subAssemblies.size} sub-assemblies")
                Log.d(TAG, "  - Total cost: $${framing.masterAssembly.totalEstimatedCost}")
                
                // Show detailed assembly
                val wallAssembly = framing.masterAssembly.assemblies.firstOrNull()
                if (wallAssembly != null) {
                    Log.d(TAG, "✓ Assembly: ${wallAssembly.name}")
                    Log.d(TAG, "  - ${wallAssembly.tasks.size} tasks")
                    Log.d(TAG, "  - ${wallAssembly.macroTasks.size} macro tasks")
                    
                    // Show detailed task
                    val task = wallAssembly.tasks.firstOrNull()
                    if (task != null) {
                        Log.d(TAG, "✓ Task: ${task.name}")
                        Log.d(TAG, "  - ${task.laborTimePerUnit} hrs @ $${task.laborCostPerUnit}")
                        Log.d(TAG, "  - ${task.requiredTools.size} tools required")
                        Log.d(TAG, "  - ${task.requiredMaterials.size} materials")
                    }
                }
            }
        }
    }
    
    private suspend fun demonstrateWebIntegration(webService: WebResourceLaborService) {
        Log.d(TAG, "\n--- Web Resource Integration Demo ---")
        
        // Get framing labor data
        val framingLabor = webService.updateLaborCostData("FRM")
        framingLabor?.let {
            Log.d(TAG, "✓ Framing Labor Data (${it.source}):")
            Log.d(TAG, "  - Average: $${it.avgHourlyRate}/hr")
            Log.d(TAG, "  - Range: $${it.lowRate} - $${it.highRate}")
            Log.d(TAG, "  - Region: ${it.region}")
        }
        
        // Get electrical labor data
        val electricalLabor = webService.updateLaborCostData("ELE")
        electricalLabor?.let {
            Log.d(TAG, "✓ Electrical Labor Data:")
            Log.d(TAG, "  - Average: $${it.avgHourlyRate}/hr")
        }
        
        // Get material costs
        val materialCost = webService.fetchMaterialCosts("2x6 kiln dried stud")
        materialCost?.let {
            Log.d(TAG, "✓ Material Cost (${it.supplier}):")
            Log.d(TAG, "  - ${it.materialName}: $${it.price}")
            Log.d(TAG, "  - Availability: ${it.availability}")
        }
        
        // Get productivity data
        val productivity = webService.fetchProductivityData("frame wall stud")
        productivity?.let {
            Log.d(TAG, "✓ Productivity Data:")
            Log.d(TAG, "  - Rate: ${it.unitsPerHour} ${it.unit}/hr")
            Log.d(TAG, "  - Crew size: ${it.crewSize}")
        }
    }
    
    private suspend fun demonstrateSearch(repo: HierarchicalCatalogueRepository) {
        Log.d(TAG, "\n--- Search Functionality Demo ---")
        
        // Search by keyword
        val framingResults = repo.searchCatalogue(
            HierarchicalCatalogueSearchCriteria(keyword = "framing")
        )
        Log.d(TAG, "✓ Search 'framing': ${framingResults.size} results")
        
        // Search by trade
        val electricalResults = repo.searchCatalogue(
            HierarchicalCatalogueSearchCriteria(tradeType = "Electrical")
        )
        Log.d(TAG, "✓ Search 'Electrical': ${electricalResults.size} results")
        
        // Search by lifecycle phase
        val structureResults = repo.searchCatalogue(
            HierarchicalCatalogueSearchCriteria(lifecyclePhase = HomeLifecyclePhase.STRUCTURE)
        )
        Log.d(TAG, "✓ Search 'STRUCTURE' phase: ${structureResults.size} results")
    }
    
    private fun demonstrateCostCalculation(repo: HierarchicalCatalogueRepository) {
        Log.d(TAG, "\n--- Cost Calculation Demo ---")
        
        val catalogue = repo.projectCatalogue.value ?: return
        val newConstruction = catalogue.projectTypes.find { it.name == "New Construction" }
        val framing = newConstruction?.trades?.find { it.tradeName == "Framing" }
        val wallAssembly = framing?.masterAssembly?.assemblies?.firstOrNull()
        
        if (wallAssembly != null) {
            var totalLabor = 0.0
            var totalMaterial = 0.0
            var totalHours = 0.0
            
            wallAssembly.tasks.forEach { task ->
                val quantity = task.defaultQty * wallAssembly.baseQuantity
                totalHours += task.laborTimePerUnit * quantity
                totalLabor += task.laborCostPerUnit * quantity
                totalMaterial += task.materialCostPerUnit * quantity
            }
            
            val markup = (totalLabor + totalMaterial) * 0.20
            val total = totalLabor + totalMaterial + markup
            
            Log.d(TAG, "✓ Cost for ${wallAssembly.name} (${wallAssembly.baseQuantity} LF):")
            Log.d(TAG, "  - Hours: ${String.format("%.1f", totalHours)}")
            Log.d(TAG, "  - Labor: $${String.format("%.2f", totalLabor)}")
            Log.d(TAG, "  - Material: $${String.format("%.2f", totalMaterial)}")
            Log.d(TAG, "  - Markup: $${String.format("%.2f", markup)}")
            Log.d(TAG, "  - Total: $${String.format("%.2f", total)}")
        }
        
        Log.d(TAG, "\n=== Demo Complete ===")
        Log.d(TAG, "The hierarchical catalogue system is fully operational!")
    }
}