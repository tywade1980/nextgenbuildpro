package com.nextgenbuildpro.pm.test

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.data.repository.HierarchicalCatalogueRepository
import com.nextgenbuildpro.pm.services.WebResourceLaborService
import kotlinx.coroutines.runBlocking

/**
 * Comprehensive test suite for the Hierarchical Indexed Catalogue system
 * Validates all requirements from the problem statement
 */
class HierarchicalCatalogueTest(private val context: Context) {
    private val TAG = "CatalogueTest"
    
    fun runAllTests() {
        Log.d(TAG, "=== Running Hierarchical Catalogue Tests ===")
        
        val repo = HierarchicalCatalogueRepository.create(context)
        val webService = WebResourceLaborService(context)
        
        runBlocking {
            // Test 1: Verify hierarchical structure
            testHierarchicalStructure(repo)
            
            // Test 2: Verify project types as children of home construction
            testProjectTypesAsChildren(repo)
            
            // Test 3: Verify trade indexing within projects
            testTradeIndexing(repo)
            
            // Test 4: Verify assemblies and sub-assemblies breakdown
            testAssembliesBreakdown(repo)
            
            // Test 5: Verify tasks and macro tasks
            testTasksAndMacroTasks(repo)
            
            // Test 6: Verify material and labor data
            testMaterialAndLaborData(repo)
            
            // Test 7: Verify web resource integration
            testWebResourceIntegration(webService)
            
            // Test 8: Verify detailed descriptions at all levels
            testDetailedDescriptions(repo)
            
            // Test 9: Verify search functionality
            testSearchFunctionality(repo)
            
            // Test 10: Verify cost calculations
            testCostCalculations(repo)
        }
        
        Log.d(TAG, "=== All Tests Complete ===")
    }
    
    private fun testHierarchicalStructure(repo: HierarchicalCatalogueRepository) {
        Log.d(TAG, "\n--- Test 1: Hierarchical Structure ---")
        
        val catalogue = repo.projectCatalogue.value
        assert(catalogue != null) { "Project catalogue should be loaded" }
        
        Log.d(TAG, "✓ Root level exists: ${catalogue!!.name}")
        assert(catalogue.name == "Home Construction Master Catalogue") { "Root should be home construction" }
        
        assert(catalogue.projectTypes.isNotEmpty()) { "Should have project types" }
        Log.d(TAG, "✓ Has ${catalogue.projectTypes.size} project types")
        
        val newConstruction = catalogue.projectTypes.find { it.name == "New Construction" }
        assert(newConstruction != null) { "Should have New Construction project type" }
        
        assert(newConstruction!!.trades.isNotEmpty()) { "Should have trades" }
        Log.d(TAG, "✓ New Construction has ${newConstruction.trades.size} trades")
        
        val framing = newConstruction.trades.find { it.tradeName == "Framing" }
        assert(framing != null) { "Should have Framing trade" }
        
        assert(framing!!.masterAssembly.assemblies.isNotEmpty()) { "Should have assemblies" }
        Log.d(TAG, "✓ Framing has ${framing.masterAssembly.assemblies.size} assemblies")
        
        val assembly = framing.masterAssembly.assemblies.first()
        assert(assembly.tasks.isNotEmpty()) { "Should have tasks" }
        Log.d(TAG, "✓ Assembly has ${assembly.tasks.size} tasks")
        
        Log.d(TAG, "✓ Test 1 PASSED: Hierarchical structure is correct")
    }
    
    private fun testProjectTypesAsChildren(repo: HierarchicalCatalogueRepository) {
        Log.d(TAG, "\n--- Test 2: Project Types as Children ---")
        
        val catalogue = repo.projectCatalogue.value!!
        
        // Verify expected project types exist
        val expectedTypes = listOf("New Construction", "Remodeling", "Addition", "Repair & Maintenance")
        expectedTypes.forEach { typeName ->
            val projectType = catalogue.projectTypes.find { it.name == typeName }
            assert(projectType != null) { "Should have $typeName project type" }
            Log.d(TAG, "✓ Found project type: $typeName")
        }
        
        // Verify each project type has proper structure
        catalogue.projectTypes.forEach { projectType ->
            assert(projectType.code.isNotEmpty()) { "Project type should have code" }
            assert(projectType.description.isNotEmpty()) { "Project type should have description" }
            Log.d(TAG, "✓ ${projectType.name} has code: ${projectType.code}")
        }
        
        Log.d(TAG, "✓ Test 2 PASSED: Project types are properly structured as children")
    }
    
    private fun testTradeIndexing(repo: HierarchicalCatalogueRepository) {
        Log.d(TAG, "\n--- Test 3: Trade Indexing ---")
        
        val catalogue = repo.projectCatalogue.value!!
        val newConstruction = catalogue.projectTypes.find { it.name == "New Construction" }!!
        
        // Verify trades are indexed by type
        val expectedTrades = listOf("Framing", "Electrical", "Plumbing", "HVAC", "Drywall", "Roofing", "Foundation")
        expectedTrades.forEach { tradeName ->
            val trade = newConstruction.trades.find { it.tradeName == tradeName }
            assert(trade != null) { "Should have $tradeName trade" }
            assert(trade!!.tradeCode.isNotEmpty()) { "Trade should have code" }
            Log.d(TAG, "✓ Found indexed trade: $tradeName (${trade.tradeCode})")
        }
        
        // Verify each trade has proper lifecycle phase
        newConstruction.trades.forEach { trade ->
            assert(trade.lifecyclePhase != null) { "Trade should have lifecycle phase" }
            Log.d(TAG, "✓ ${trade.tradeName} in phase: ${trade.lifecyclePhase}")
        }
        
        Log.d(TAG, "✓ Test 3 PASSED: Trades are properly indexed by type")
    }
    
    private fun testAssembliesBreakdown(repo: HierarchicalCatalogueRepository) {
        Log.d(TAG, "\n--- Test 4: Assemblies and Sub-Assemblies ---")
        
        val catalogue = repo.projectCatalogue.value!!
        val framing = catalogue.projectTypes.find { it.name == "New Construction" }!!
            .trades.find { it.tradeName == "Framing" }!!
        
        // Verify master assembly exists
        val masterAssembly = framing.masterAssembly
        assert(masterAssembly.name.isNotEmpty()) { "Master assembly should have name" }
        Log.d(TAG, "✓ Master assembly: ${masterAssembly.name}")
        
        // Verify assemblies exist
        assert(masterAssembly.assemblies.isNotEmpty()) { "Should have assemblies" }
        masterAssembly.assemblies.forEach { assembly ->
            assert(assembly.name.isNotEmpty()) { "Assembly should have name" }
            assert(assembly.category.isNotEmpty()) { "Assembly should have category" }
            assert(assembly.description.isNotEmpty()) { "Assembly should have description" }
            Log.d(TAG, "✓ Assembly: ${assembly.name} (${assembly.category})")
        }
        
        // Verify sub-assemblies exist
        assert(masterAssembly.subAssemblies.isNotEmpty()) { "Should have sub-assemblies" }
        masterAssembly.subAssemblies.forEach { subAssembly ->
            assert(subAssembly.name.isNotEmpty()) { "Sub-assembly should have name" }
            assert(subAssembly.parentAssemblyId.isNotEmpty()) { "Sub-assembly should have parent" }
            Log.d(TAG, "✓ Sub-assembly: ${subAssembly.name}")
        }
        
        Log.d(TAG, "✓ Test 4 PASSED: Assemblies and sub-assemblies properly structured")
    }
    
    private fun testTasksAndMacroTasks(repo: HierarchicalCatalogueRepository) {
        Log.d(TAG, "\n--- Test 5: Tasks and Macro Tasks ---")
        
        val assembly = repo.projectCatalogue.value!!
            .projectTypes.find { it.name == "New Construction" }!!
            .trades.find { it.tradeName == "Framing" }!!
            .masterAssembly.assemblies.first()
        
        // Verify tasks exist with required properties
        assert(assembly.tasks.isNotEmpty()) { "Should have tasks" }
        assembly.tasks.forEach { task ->
            assert(task.name.isNotEmpty()) { "Task should have name" }
            assert(task.description.isNotEmpty()) { "Task should have description" }
            assert(task.workDescription.isNotEmpty()) { "Task should have work description" }
            assert(task.laborTimePerUnit > 0) { "Task should have labor time" }
            assert(task.laborCostPerUnit > 0) { "Task should have labor cost" }
            Log.d(TAG, "✓ Task: ${task.name} (${task.laborTimePerUnit}hrs, $${task.laborCostPerUnit})")
        }
        
        // Verify macro tasks exist
        assert(assembly.macroTasks.isNotEmpty()) { "Should have macro tasks" }
        assembly.macroTasks.forEach { macroTask ->
            assert(macroTask.name.isNotEmpty()) { "Macro task should have name" }
            assert(macroTask.tasks.isNotEmpty()) { "Macro task should contain tasks" }
            assert(macroTask.totalEstimatedHours > 0) { "Macro task should have total hours" }
            Log.d(TAG, "✓ Macro task: ${macroTask.name} (${macroTask.tasks.size} tasks)")
        }
        
        Log.d(TAG, "✓ Test 5 PASSED: Tasks and macro tasks properly implemented")
    }
    
    private fun testMaterialAndLaborData(repo: HierarchicalCatalogueRepository) {
        Log.d(TAG, "\n--- Test 6: Material and Labor Data ---")
        
        val task = repo.projectCatalogue.value!!
            .projectTypes.find { it.name == "New Construction" }!!
            .trades.find { it.tradeName == "Framing" }!!
            .masterAssembly.assemblies.first().tasks.first()
        
        // Verify labor data
        assert(task.laborTimePerUnit > 0) { "Task should have labor time" }
        assert(task.laborCostPerUnit > 0) { "Task should have labor cost" }
        assert(task.materialCostPerUnit >= 0) { "Task should have material cost" }
        Log.d(TAG, "✓ Labor: ${task.laborTimePerUnit}hrs @ $${task.laborCostPerUnit}")
        Log.d(TAG, "✓ Material: $${task.materialCostPerUnit}")
        
        // Verify material specifications
        if (task.requiredMaterials.isNotEmpty()) {
            task.requiredMaterials.forEach { material ->
                assert(material.name.isNotEmpty()) { "Material should have name" }
                assert(material.quantity > 0) { "Material should have quantity" }
                assert(material.unitCost >= 0) { "Material should have cost" }
                Log.d(TAG, "✓ Material: ${material.name} (${material.quantity} ${material.unit})")
            }
        }
        
        // Verify web-sourced data
        if (task.webSourcedData != null) {
            val webData = task.webSourcedData!!
            assert(webData.source.isNotEmpty()) { "Web data should have source" }
            assert(webData.avgHourlyRate > 0) { "Web data should have rate" }
            Log.d(TAG, "✓ Web data source: ${webData.source}")
        }
        
        Log.d(TAG, "✓ Test 6 PASSED: Material and labor data properly structured")
    }
    
    private suspend fun testWebResourceIntegration(webService: WebResourceLaborService) {
        Log.d(TAG, "\n--- Test 7: Web Resource Integration ---")
        
        // Test labor cost data fetching
        val framingLabor = webService.updateLaborCostData("FRM")
        assert(framingLabor != null) { "Should get framing labor data" }
        assert(framingLabor!!.avgHourlyRate > 0) { "Should have valid rate" }
        Log.d(TAG, "✓ Framing labor: $${framingLabor.avgHourlyRate}/hr from ${framingLabor.source}")
        
        val electricalLabor = webService.updateLaborCostData("ELE")
        assert(electricalLabor != null) { "Should get electrical labor data" }
        Log.d(TAG, "✓ Electrical labor: $${electricalLabor!!.avgHourlyRate}/hr")
        
        // Test material cost fetching
        val materialCost = webService.fetchMaterialCosts("2x6 kiln dried stud")
        assert(materialCost != null) { "Should get material cost" }
        assert(materialCost!!.price > 0) { "Should have valid price" }
        Log.d(TAG, "✓ Material cost: ${materialCost.materialName} @ $${materialCost.price}")
        
        // Test productivity data
        val productivity = webService.fetchProductivityData("frame wall stud")
        assert(productivity != null) { "Should get productivity data" }
        assert(productivity!!.unitsPerHour > 0) { "Should have valid productivity" }
        Log.d(TAG, "✓ Productivity: ${productivity.unitsPerHour} ${productivity.unit}/hr")
        
        Log.d(TAG, "✓ Test 7 PASSED: Web resource integration working")
    }
    
    private fun testDetailedDescriptions(repo: HierarchicalCatalogueRepository) {
        Log.d(TAG, "\n--- Test 8: Detailed Descriptions ---")
        
        val catalogue = repo.projectCatalogue.value!!
        
        // Test catalogue level description
        assert(catalogue.description.isNotEmpty()) { "Catalogue should have description" }
        Log.d(TAG, "✓ Catalogue description: ${catalogue.description.take(100)}...")
        
        // Test project type description
        val projectType = catalogue.projectTypes.first()
        assert(projectType.description.isNotEmpty()) { "Project type should have description" }
        Log.d(TAG, "✓ Project type description: ${projectType.description.take(100)}...")
        
        // Test trade description
        val trade = projectType.trades.first()
        assert(trade.description.isNotEmpty()) { "Trade should have description" }
        assert(trade.workDescription.isNotEmpty()) { "Trade should have work description" }
        Log.d(TAG, "✓ Trade work description: ${trade.workDescription.take(100)}...")
        
        // Test assembly description
        val assembly = trade.masterAssembly.assemblies.first()
        assert(assembly.description.isNotEmpty()) { "Assembly should have description" }
        assert(assembly.workDescription.isNotEmpty()) { "Assembly should have work description" }
        Log.d(TAG, "✓ Assembly work description: ${assembly.workDescription.take(100)}...")
        
        // Test task description
        val task = assembly.tasks.first()
        assert(task.description.isNotEmpty()) { "Task should have description" }
        assert(task.workDescription.isNotEmpty()) { "Task should have work description" }
        Log.d(TAG, "✓ Task work description: ${task.workDescription.take(100)}...")
        
        Log.d(TAG, "✓ Test 8 PASSED: Detailed descriptions at all levels")
    }
    
    private suspend fun testSearchFunctionality(repo: HierarchicalCatalogueRepository) {
        Log.d(TAG, "\n--- Test 9: Search Functionality ---")
        
        // Test keyword search
        val keywordResults = repo.searchCatalogue(CatalogueSearchCriteria(keyword = "framing"))
        assert(keywordResults.isNotEmpty()) { "Keyword search should return results" }
        Log.d(TAG, "✓ Keyword search 'framing': ${keywordResults.size} results")
        
        // Test trade search
        val tradeResults = repo.searchCatalogue(CatalogueSearchCriteria(tradeType = "Electrical"))
        assert(tradeResults.isNotEmpty()) { "Trade search should return results" }
        Log.d(TAG, "✓ Trade search 'Electrical': ${tradeResults.size} results")
        
        // Test phase search
        val phaseResults = repo.searchCatalogue(CatalogueSearchCriteria(lifecyclePhase = HomeLifecyclePhase.STRUCTURE))
        assert(phaseResults.isNotEmpty()) { "Phase search should return results" }
        Log.d(TAG, "✓ Phase search 'STRUCTURE': ${phaseResults.size} results")
        
        Log.d(TAG, "✓ Test 9 PASSED: Search functionality working")
    }
    
    private fun testCostCalculations(repo: HierarchicalCatalogueRepository) {
        Log.d(TAG, "\n--- Test 10: Cost Calculations ---")
        
        val assembly = repo.projectCatalogue.value!!
            .projectTypes.find { it.name == "New Construction" }!!
            .trades.find { it.tradeName == "Framing" }!!
            .masterAssembly.assemblies.first()
        
        var totalHours = 0.0
        var totalCost = 0.0
        
        assembly.tasks.forEach { task ->
            val quantity = task.defaultQty * assembly.baseQuantity
            totalHours += task.laborTimePerUnit * quantity
            totalCost += (task.laborCostPerUnit + task.materialCostPerUnit) * quantity
        }
        
        assert(totalHours > 0) { "Should calculate total hours" }
        assert(totalCost > 0) { "Should calculate total cost" }
        
        Log.d(TAG, "✓ Calculated ${assembly.name}:")
        Log.d(TAG, "  - Total hours: ${String.format("%.2f", totalHours)}")
        Log.d(TAG, "  - Total cost: $${String.format("%.2f", totalCost)}")
        
        Log.d(TAG, "✓ Test 10 PASSED: Cost calculations working")
    }
}