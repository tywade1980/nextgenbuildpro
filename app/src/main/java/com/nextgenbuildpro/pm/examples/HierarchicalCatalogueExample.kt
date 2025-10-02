package com.nextgenbuildpro.pm.examples

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.data.repository.HierarchicalCatalogueRepository
import com.nextgenbuildpro.pm.services.WebResourceLaborService
import kotlinx.coroutines.runBlocking

/**
 * Example usage of the Hierarchical Indexed Catalogue system
 * Demonstrates navigation through the hierarchy and accessing detailed data
 */
class HierarchicalCatalogueExample(private val context: Context) {
    private val TAG = "CatalogueExample"
    
    private val catalogueRepository = HierarchicalCatalogueRepository.create(context)
    private val webService = WebResourceLaborService(context)

    /**
     * Demonstrate the complete hierarchy navigation from top to bottom
     */
    fun demonstrateHierarchyNavigation() {
        runBlocking {
            try {
                Log.d(TAG, "=== Hierarchical Catalogue Navigation Example ===")
                
                // 1. Access the root project catalogue
                val projectCatalogue = catalogueRepository.projectCatalogue.value
                if (projectCatalogue == null) {
                    Log.e(TAG, "Project catalogue not loaded")
                    return@runBlocking
                }
                
                Log.d(TAG, "Root Level - Project Catalogue:")
                Log.d(TAG, "  Name: ${projectCatalogue.name}")
                Log.d(TAG, "  Description: ${projectCatalogue.description}")
                Log.d(TAG, "  Version: ${projectCatalogue.version}")
                Log.d(TAG, "  Project Types: ${projectCatalogue.projectTypes.size}")
                
                // 2. Navigate to a specific project type (New Construction)
                val newConstruction = projectCatalogue.projectTypes.find { it.name == "New Construction" }
                if (newConstruction == null) {
                    Log.e(TAG, "New Construction project type not found")
                    return@runBlocking
                }
                
                Log.d(TAG, "\nLevel 2 - Project Type: ${newConstruction.name}")
                Log.d(TAG, "  Code: ${newConstruction.code}")
                Log.d(TAG, "  Description: ${newConstruction.description}")
                Log.d(TAG, "  Context Mode: ${newConstruction.contextMode}")
                Log.d(TAG, "  Applicable Phases: ${newConstruction.applicablePhases.size}")
                Log.d(TAG, "  Available Trades: ${newConstruction.trades.size}")
                
                // 3. Navigate to a specific trade (Framing)
                val framingTrade = newConstruction.trades.find { it.tradeName == "Framing" }
                if (framingTrade == null) {
                    Log.e(TAG, "Framing trade not found")
                    return@runBlocking
                }
                
                Log.d(TAG, "\nLevel 3 - Trade Index: ${framingTrade.tradeName}")
                Log.d(TAG, "  Trade Code: ${framingTrade.tradeCode}")
                Log.d(TAG, "  Description: ${framingTrade.description}")
                Log.d(TAG, "  Work Description: ${framingTrade.workDescription}")
                Log.d(TAG, "  Lifecycle Phase: ${framingTrade.lifecyclePhase}")
                Log.d(TAG, "  Average Labor Rate: $${framingTrade.avgLaborRate}/hour")
                Log.d(TAG, "  Web Resources: ${framingTrade.webResourceUrls.size}")
                
                // 4. Access the master assembly
                val masterAssembly = framingTrade.masterAssembly
                Log.d(TAG, "\nLevel 4 - Master Assembly: ${masterAssembly.name}")
                Log.d(TAG, "  Description: ${masterAssembly.description}")
                Log.d(TAG, "  Assemblies: ${masterAssembly.assemblies.size}")
                Log.d(TAG, "  Sub-Assemblies: ${masterAssembly.subAssemblies.size}")
                Log.d(TAG, "  Total Estimated Hours: ${masterAssembly.totalEstimatedHours}")
                Log.d(TAG, "  Total Estimated Cost: $${masterAssembly.totalEstimatedCost}")
                
                // 5. Navigate to a specific assembly
                val wallFramingAssembly = masterAssembly.assemblies.firstOrNull()
                if (wallFramingAssembly == null) {
                    Log.e(TAG, "No assemblies found in master assembly")
                    return@runBlocking
                }
                
                Log.d(TAG, "\nLevel 5 - Assembly: ${wallFramingAssembly.name}")
                Log.d(TAG, "  Category: ${wallFramingAssembly.category}")
                Log.d(TAG, "  Description: ${wallFramingAssembly.description}")
                Log.d(TAG, "  Work Description: ${wallFramingAssembly.workDescription}")
                Log.d(TAG, "  Valid Modes: ${wallFramingAssembly.validModes}")
                Log.d(TAG, "  Base Quantity: ${wallFramingAssembly.baseQuantity} ${wallFramingAssembly.defaultQuantityUnit}")
                Log.d(TAG, "  Tasks: ${wallFramingAssembly.tasks.size}")
                Log.d(TAG, "  Macro Tasks: ${wallFramingAssembly.macroTasks.size}")
                Log.d(TAG, "  Prerequisites: ${wallFramingAssembly.prerequisites}")
                Log.d(TAG, "  Quality Standards: ${wallFramingAssembly.qualityStandards}")
                
                // 6. Access sub-assemblies
                val subAssembly = masterAssembly.subAssemblies.firstOrNull()
                if (subAssembly != null) {
                    Log.d(TAG, "\nLevel 6 - Sub-Assembly: ${subAssembly.name}")
                    Log.d(TAG, "  Description: ${subAssembly.description}")
                    Log.d(TAG, "  Work Description: ${subAssembly.workDescription}")
                    Log.d(TAG, "  Skill Level: ${subAssembly.skillLevel}")
                    Log.d(TAG, "  Estimated Hours: ${subAssembly.estimatedHours}")
                    Log.d(TAG, "  Estimated Cost: $${subAssembly.estimatedCost}")
                }
                
                // 7. Navigate to specific tasks
                val task = wallFramingAssembly.tasks.firstOrNull()
                if (task != null) {
                    Log.d(TAG, "\nLevel 7 - Task: ${task.name}")
                    Log.d(TAG, "  Description: ${task.description}")
                    Log.d(TAG, "  Work Description: ${task.workDescription}")
                    Log.d(TAG, "  Unit Type: ${task.unitType}")
                    Log.d(TAG, "  Default Quantity: ${task.defaultQty}")
                    Log.d(TAG, "  Labor Time Per Unit: ${task.laborTimePerUnit} hours")
                    Log.d(TAG, "  Labor Cost Per Unit: $${task.laborCostPerUnit}")
                    Log.d(TAG, "  Material Cost Per Unit: $${task.materialCostPerUnit}")
                    Log.d(TAG, "  Skill Level: ${task.skillLevel}")
                    Log.d(TAG, "  Required Tools: ${task.requiredTools}")
                    Log.d(TAG, "  Required Materials: ${task.requiredMaterials.size}")
                    Log.d(TAG, "  Safety Notes: ${task.safetyNotes}")
                    Log.d(TAG, "  Quality Checkpoints: ${task.qualityCheckpoints}")
                    
                    // Display web-sourced labor data
                    task.webSourcedData?.let { laborData ->
                        Log.d(TAG, "  Web-Sourced Labor Data:")
                        Log.d(TAG, "    Source: ${laborData.source}")
                        Log.d(TAG, "    URL: ${laborData.sourceUrl}")
                        Log.d(TAG, "    Region: ${laborData.region}")
                        Log.d(TAG, "    Avg Rate: $${laborData.avgHourlyRate}/hour")
                        Log.d(TAG, "    Range: $${laborData.lowRate} - $${laborData.highRate}")
                        Log.d(TAG, "    Reliability: ${laborData.reliability}")
                        Log.d(TAG, "    Last Updated: ${laborData.lastUpdated}")
                    }
                }
                
                // 8. Navigate to macro tasks
                val macroTask = wallFramingAssembly.macroTasks.firstOrNull()
                if (macroTask != null) {
                    Log.d(TAG, "\nLevel 8 - Macro Task: ${macroTask.name}")
                    Log.d(TAG, "  Description: ${macroTask.description}")
                    Log.d(TAG, "  Work Description: ${macroTask.workDescription}")
                    Log.d(TAG, "  Sequence Order: ${macroTask.sequenceOrder}")
                    Log.d(TAG, "  Tasks Included: ${macroTask.tasks.size}")
                    Log.d(TAG, "  Total Hours: ${macroTask.totalEstimatedHours}")
                    Log.d(TAG, "  Total Cost: $${macroTask.totalEstimatedCost}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error demonstrating hierarchy navigation", e)
            }
        }
    }

    /**
     * Demonstrate searching the catalogue
     */
    fun demonstrateSearchFunctionality() {
        runBlocking {
            try {
                Log.d(TAG, "\n=== Catalogue Search Examples ===")
                
                // Search by keyword
                val keywordResults = catalogueRepository.searchCatalogue(
                    HierarchicalCatalogueSearchCriteria(keyword = "framing")
                )
                Log.d(TAG, "Search results for 'framing': ${keywordResults.size} items")
                
                // Search by trade type
                val tradeResults = catalogueRepository.searchCatalogue(
                    HierarchicalCatalogueSearchCriteria(tradeType = "Electrical")
                )
                Log.d(TAG, "Search results for 'Electrical' trade: ${tradeResults.size} items")
                
                // Search by project type
                val projectResults = catalogueRepository.searchCatalogue(
                    HierarchicalCatalogueSearchCriteria(projectType = "New Construction")
                )
                Log.d(TAG, "Search results for 'New Construction': ${projectResults.size} items")
                
                // Search by lifecycle phase
                val phaseResults = catalogueRepository.searchCatalogue(
                    HierarchicalCatalogueSearchCriteria(lifecyclePhase = HomeLifecyclePhase.STRUCTURE)
                )
                Log.d(TAG, "Search results for STRUCTURE phase: ${phaseResults.size} items")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error demonstrating search functionality", e)
            }
        }
    }

    /**
     * Demonstrate web resource integration
     */
    fun demonstrateWebResourceIntegration() {
        runBlocking {
            try {
                Log.d(TAG, "\n=== Web Resource Integration Examples ===")
                
                // Update labor cost data for framing trade
                val laborData = webService.updateLaborCostData("FRM", "California")
                laborData?.let {
                    Log.d(TAG, "Updated framing labor data:")
                    Log.d(TAG, "  Source: ${it.source}")
                    Log.d(TAG, "  Average Rate: $${it.avgHourlyRate}/hour")
                    Log.d(TAG, "  Range: $${it.lowRate} - $${it.highRate}")
                    Log.d(TAG, "  Region: ${it.region}")
                    Log.d(TAG, "  Reliability: ${it.reliability}")
                }
                
                // Fetch material costs
                val materialData = webService.fetchMaterialCosts("2x6 kiln dried stud", "HomeDepot")
                materialData?.let {
                    Log.d(TAG, "Material cost data:")
                    Log.d(TAG, "  Material: ${it.materialName}")
                    Log.d(TAG, "  Supplier: ${it.supplier}")
                    Log.d(TAG, "  Price: $${it.price} per ${it.unit}")
                    Log.d(TAG, "  Availability: ${it.availability}")
                }
                
                // Fetch productivity data
                val productivityData = webService.fetchProductivityData("frame wall stud")
                productivityData?.let {
                    Log.d(TAG, "Productivity data:")
                    Log.d(TAG, "  Task: ${it.taskType}")
                    Log.d(TAG, "  Productivity: ${it.unitsPerHour} ${it.unit}/hour")
                    Log.d(TAG, "  Crew Size: ${it.crewSize}")
                    Log.d(TAG, "  Skill Level: ${it.skillLevel}")
                    Log.d(TAG, "  Source: ${it.source}")
                }
                
                // Update all trade data
                webService.updateAllTradeData("ELE", "Texas")
                Log.d(TAG, "Updated all electrical trade data for Texas region")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error demonstrating web resource integration", e)
            }
        }
    }

    /**
     * Demonstrate cost breakdown calculation
     */
    fun demonstrateCostCalculation() {
        runBlocking {
            try {
                Log.d(TAG, "\n=== Cost Calculation Examples ===")
                
                val projectCatalogue = catalogueRepository.projectCatalogue.value
                val newConstruction = projectCatalogue?.projectTypes?.find { it.name == "New Construction" }
                val framingTrade = newConstruction?.trades?.find { it.tradeName == "Framing" }
                val wallAssembly = framingTrade?.masterAssembly?.assemblies?.firstOrNull()
                
                if (wallAssembly != null) {
                    // Calculate costs for a specific assembly
                    var totalLaborHours = 0.0
                    var totalLaborCost = 0.0
                    var totalMaterialCost = 0.0
                    var totalEquipmentCost = 0.0
                    
                    wallAssembly.tasks.forEach { task ->
                        val quantity = task.defaultQty * wallAssembly.baseQuantity
                        totalLaborHours += task.laborTimePerUnit * quantity
                        totalLaborCost += task.laborCostPerUnit * quantity
                        totalMaterialCost += task.materialCostPerUnit * quantity
                        totalEquipmentCost += task.equipmentCostPerUnit * quantity
                    }
                    
                    val markup = (totalLaborCost + totalMaterialCost + totalEquipmentCost) * 0.20
                    val grandTotal = totalLaborCost + totalMaterialCost + totalEquipmentCost + markup
                    
                    Log.d(TAG, "Cost breakdown for ${wallAssembly.name} (${wallAssembly.baseQuantity} ${wallAssembly.defaultQuantityUnit}):")
                    Log.d(TAG, "  Total Labor Hours: ${String.format("%.2f", totalLaborHours)}")
                    Log.d(TAG, "  Labor Cost: $${String.format("%.2f", totalLaborCost)}")
                    Log.d(TAG, "  Material Cost: $${String.format("%.2f", totalMaterialCost)}")
                    Log.d(TAG, "  Equipment Cost: $${String.format("%.2f", totalEquipmentCost)}")
                    Log.d(TAG, "  Markup (20%): $${String.format("%.2f", markup)}")
                    Log.d(TAG, "  Grand Total: $${String.format("%.2f", grandTotal)}")
                } else {
                    Log.d(TAG, "Wall assembly not found for cost calculation")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error demonstrating cost calculation", e)
            }
        }
    }

    /**
     * Run all examples
     */
    fun runAllExamples() {
        Log.d(TAG, "Starting Hierarchical Catalogue Examples...")
        
        demonstrateHierarchyNavigation()
        demonstrateSearchFunctionality()
        demonstrateWebResourceIntegration()
        demonstrateCostCalculation()
        
        Log.d(TAG, "Completed all Hierarchical Catalogue Examples")
    }
}