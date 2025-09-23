package com.nextgenbuildpro.pm.test

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.data.repository.HierarchicalCatalogueRepository
import com.nextgenbuildpro.pm.data.repository.AssemblyRepository
import com.nextgenbuildpro.pm.data.repository.TemplateEstimateRepository
import com.nextgenbuildpro.pm.service.AssemblyCatalogueService
import com.nextgenbuildpro.pm.service.CalculationEngineService
import com.nextgenbuildpro.pm.service.TemplateSystemService
import com.nextgenbuildpro.pm.service.*
import kotlinx.coroutines.runBlocking

/**
 * Comprehensive test suite for assembly catalogue integration
 * Tests various assembly types, calculations, and integration points
 */
class AssemblyCatalogueIntegrationTest(private val context: Context) {
    private val TAG = "AssemblyCatalogueTest"
    
    private val hierarchicalCatalogueRepository = HierarchicalCatalogueRepository(context)
    private val assemblyRepository = AssemblyRepository()
    private val templateEstimateRepository = TemplateEstimateRepository(context)
    private val assemblyCatalogueService = AssemblyCatalogueService(
        context, 
        hierarchicalCatalogueRepository, 
        assemblyRepository
    )
    private val calculationEngine = CalculationEngineService()
    private val templateSystemService = TemplateSystemService(
        context,
        templateEstimateRepository,
        hierarchicalCatalogueRepository
    )
    
    /**
     * Run comprehensive integration tests
     */
    suspend fun runAllTests(): TestResults {
        val results = TestResults()
        
        try {
            Log.i(TAG, "Starting Assembly Catalogue Integration Tests...")
            
            // Test 1: Assembly Search Functionality
            results.addTest("Assembly Search", testAssemblySearch())
            
            // Test 2: Assembly to Line Items Conversion
            results.addTest("Assembly to Line Items", testAssemblyToLineItemsConversion())
            
            // Test 3: Calculation Engine Accuracy
            results.addTest("Calculation Engine", testCalculationEngineAccuracy())
            
            // Test 4: Template System Operations
            results.addTest("Template System", testTemplateSystemOperations())
            
            // Test 5: Estimate Creation and Calculations
            results.addTest("Estimate Creation", testEstimateCreationAndCalculations())
            
            // Test 6: Performance with Large Catalogues
            results.addTest("Performance Test", testPerformanceWithLargeCatalogues())
            
            // Test 7: Tax and Markup Calculations
            results.addTest("Tax and Markup", testTaxAndMarkupCalculations())
            
            Log.i(TAG, "Integration Tests Completed. Results: ${results.getSummary()}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Integration tests failed: ${e.message}")
            results.addTest("Test Suite Error", TestResult(false, "Test suite failed: ${e.message}"))
        }
        
        return results
    }
    
    /**
     * Test assembly search functionality
     */
    private suspend fun testAssemblySearch(): TestResult {
        return try {
            // Test basic search
            val basicResults = assemblyCatalogueService.searchAssemblies(keyword = "wall")
            if (basicResults.isEmpty()) {
                return TestResult(false, "Basic search returned no results")
            }
            
            // Test trade-specific search
            val framingResults = assemblyCatalogueService.searchAssemblies(tradeType = "Framing")
            if (framingResults.isEmpty()) {
                return TestResult(false, "Trade-specific search returned no results")
            }
            
            // Test context mode search
            val remodelingResults = assemblyCatalogueService.searchAssemblies(contextMode = ContextMode.REMODELING)
            if (remodelingResults.isEmpty()) {
                return TestResult(false, "Context mode search returned no results")
            }
            
            TestResult(true, "Assembly search working correctly. Found ${basicResults.size} basic, ${framingResults.size} framing, ${remodelingResults.size} remodeling assemblies")
        } catch (e: Exception) {
            TestResult(false, "Assembly search failed: ${e.message}")
        }
    }
    
    /**
     * Test assembly to line items conversion
     */
    private suspend fun testAssemblyToLineItemsConversion(): TestResult {
        return try {
            // Get a test assembly
            val searchResults = assemblyCatalogueService.searchAssemblies(keyword = "wall")
            if (searchResults.isEmpty()) {
                return TestResult(false, "No assemblies found for conversion test")
            }
            
            val testAssembly = Assembly(
                id = "test-assembly",
                name = "Test Wall Assembly",
                description = "Test assembly for conversion",
                tradeId = "framing",
                tradeName = "Framing",
                materials = listOf(
                    AssemblyMaterial(
                        id = "mat1",
                        name = "2x4 Lumber",
                        description = "Framing lumber",
                        quantity = 10.0,
                        unit = "EA",
                        unitCost = 5.50,
                        totalCost = 55.0
                    ),
                    AssemblyMaterial(
                        id = "mat2",
                        name = "Nails",
                        description = "Framing nails",
                        quantity = 2.0,
                        unit = "LB",
                        unitCost = 3.25,
                        totalCost = 6.50
                    )
                ),
                laborHours = 4.0,
                estimatedCost = 241.50,
                tags = listOf("framing", "interior"),
                createdAt = "2024-01-01",
                updatedAt = "2024-01-01"
            )
            
            // Convert to line items
            val lineItems = assemblyCatalogueService.convertAssemblyToLineItems(
                testAssembly, 
                2.0, // quantity
                ContextMode.REMODELING
            )
            
            if (lineItems.isEmpty()) {
                return TestResult(false, "Assembly conversion produced no line items")
            }
            
            // Validate line items
            val laborItems = lineItems.filter { it.category == "Labor" }
            val materialItems = lineItems.filter { it.category == "Material" }
            
            if (laborItems.isEmpty()) {
                return TestResult(false, "No labor line items generated")
            }
            
            if (materialItems.size != testAssembly.materials.size) {
                return TestResult(false, "Material line items count mismatch")
            }
            
            val totalCost = lineItems.sumOf { it.totalPrice }
            
            TestResult(true, "Assembly conversion successful. Generated ${lineItems.size} line items, total cost: $${String.format("%.2f", totalCost)}")
        } catch (e: Exception) {
            TestResult(false, "Assembly conversion failed: ${e.message}")
        }
    }
    
    /**
     * Test calculation engine accuracy
     */
    private suspend fun testCalculationEngineAccuracy(): TestResult {
        return try {
            val testCases = listOf(
                // Test case 1: Fixed unit price
                CalculationTestCase(
                    pricingModel = PricingModel.FIXED_UNIT_PRICE,
                    quantity = 10.0,
                    unitPrice = 15.50,
                    expectedBase = 155.0
                ),
                // Test case 2: Time and materials
                CalculationTestCase(
                    pricingModel = PricingModel.TIME_AND_MATERIALS,
                    quantity = 5.0,
                    laborHours = 2.0,
                    laborRate = 45.0,
                    materialCosts = listOf(MaterialCost("Test Material", 10.0, 1.0)),
                    expectedBase = 500.0 // (5 * 2 * 45) + (5 * 10)
                )
            )
            
            val failedTests = mutableListOf<String>()
            
            testCases.forEach { testCase ->
                val result = calculationEngine.calculateLineItemTotals(
                    quantity = testCase.quantity,
                    unitPrice = testCase.unitPrice,
                    pricingModel = testCase.pricingModel,
                    laborHours = testCase.laborHours,
                    laborRate = testCase.laborRate,
                    materialCosts = testCase.materialCosts,
                    overhead = testCase.overhead,
                    profit = testCase.profit
                )
                
                if (Math.abs(result.baseTotal - testCase.expectedBase) > 0.01) {
                    failedTests.add("${testCase.pricingModel} - Expected: ${testCase.expectedBase}, Got: ${result.baseTotal}")
                }
            }
            
            if (failedTests.isNotEmpty()) {
                TestResult(false, "Calculation accuracy failed: ${failedTests.joinToString(", ")}")
            } else {
                TestResult(true, "All calculation tests passed with accurate results")
            }
        } catch (e: Exception) {
            TestResult(false, "Calculation engine test failed: ${e.message}")
        }
    }
    
    /**
     * Test template system operations
     */
    private suspend fun testTemplateSystemOperations(): TestResult {
        return try {
            // Create a test estimate
            val estimate = templateEstimateRepository.createEstimate("test-project", ContextMode.REMODELING)
            
            // Create template from estimate
            val template = templateSystemService.createTemplateFromEstimate(
                estimate,
                "Test Template",
                "Template for testing",
                listOf(ContextMode.REMODELING),
                HomeLifecyclePhase.STRUCTURE
            )
            
            if (template == null) {
                return TestResult(false, "Template creation failed")
            }
            
            // Test template loading
            val loadedTemplate = templateSystemService.loadEstimateTemplate(template.name)
            if (loadedTemplate == null) {
                return TestResult(false, "Template loading failed")
            }
            
            // Test template conversion back to estimate
            val newEstimate = templateSystemService.convertTemplateToEstimate(
                loadedTemplate,
                "test-project-2",
                ContextMode.REMODELING
            )
            
            if (newEstimate == null) {
                return TestResult(false, "Template to estimate conversion failed")
            }
            
            // Test template suggestions
            val suggestions = templateSystemService.generateTemplateSuggestions(
                ContextMode.REMODELING,
                HomeLifecyclePhase.STRUCTURE
            )
            
            // Clean up test template
            templateSystemService.deleteTemplate(template.name)
            
            TestResult(true, "Template system operations successful. Found ${suggestions.size} suggestions")
        } catch (e: Exception) {
            TestResult(false, "Template system test failed: ${e.message}")
        }
    }
    
    /**
     * Test estimate creation and calculations
     */
    private suspend fun testEstimateCreationAndCalculations(): TestResult {
        return try {
            // Create estimate
            val estimate = templateEstimateRepository.createEstimate("test-project", ContextMode.REMODELING)
            
            // Get assembly templates
            val assemblyTemplates = templateEstimateRepository.getAssemblyTemplatesByContextMode(ContextMode.REMODELING)
            if (assemblyTemplates.isEmpty()) {
                return TestResult(false, "No assembly templates available for testing")
            }
            
            // Add assembly to estimate
            val assemblyTemplate = assemblyTemplates.first()
            val success = templateEstimateRepository.addAssemblyToEstimate(estimate.id, assemblyTemplate, 1.0)
            
            if (!success) {
                return TestResult(false, "Failed to add assembly to estimate")
            }
            
            // Get updated estimate
            val updatedEstimate = templateEstimateRepository.getById(estimate.id)
            if (updatedEstimate == null) {
                return TestResult(false, "Updated estimate not found")
            }
            
            // Validate calculations
            if (updatedEstimate.assemblies.isEmpty()) {
                return TestResult(false, "No assemblies in updated estimate")
            }
            
            if (updatedEstimate.grandTotal <= 0) {
                return TestResult(false, "Invalid grand total calculation")
            }
            
            TestResult(true, "Estimate creation and calculations successful. Grand total: $${String.format("%.2f", updatedEstimate.grandTotal)}")
        } catch (e: Exception) {
            TestResult(false, "Estimate creation test failed: ${e.message}")
        }
    }
    
    /**
     * Test performance with large catalogues
     */
    private suspend fun testPerformanceWithLargeCatalogues(): TestResult {
        return try {
            val startTime = System.currentTimeMillis()
            
            // Perform multiple search operations
            repeat(10) {
                assemblyCatalogueService.searchAssemblies(keyword = "test")
                assemblyCatalogueService.searchAssemblies(tradeType = "Framing")
                assemblyCatalogueService.searchAssemblies(contextMode = ContextMode.REMODELING)
            }
            
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            // Performance should be under 2 seconds for 30 searches
            if (duration > 2000) {
                TestResult(false, "Performance test failed. Duration: ${duration}ms (expected < 2000ms)")
            } else {
                TestResult(true, "Performance test passed. 30 searches completed in ${duration}ms")
            }
        } catch (e: Exception) {
            TestResult(false, "Performance test failed: ${e.message}")
        }
    }
    
    /**
     * Test tax and markup calculations
     */
    private suspend fun testTaxAndMarkupCalculations(): TestResult {
        return try {
            val subtotal = 1000.0
            
            // Test percentage tax
            val taxSettings = TaxSettings(TaxType.PERCENTAGE, rate = 8.25)
            val taxCalculation = calculationEngine.applyTaxCalculations(subtotal, taxSettings)
            
            val expectedTax = subtotal * 0.0825
            if (Math.abs(taxCalculation.taxAmount - expectedTax) > 0.01) {
                return TestResult(false, "Tax calculation incorrect. Expected: $expectedTax, Got: ${taxCalculation.taxAmount}")
            }
            
            // Test percentage markup
            val markupSettings = MarkupSettings(MarkupType.PERCENTAGE, value = 20.0)
            val markupCalculation = calculationEngine.applyMarkupCalculations(subtotal, markupSettings)
            
            val expectedMarkup = subtotal * 0.20
            if (Math.abs(markupCalculation.markupAmount - expectedMarkup) > 0.01) {
                return TestResult(false, "Markup calculation incorrect. Expected: $expectedMarkup, Got: ${markupCalculation.markupAmount}")
            }
            
            TestResult(true, "Tax and markup calculations accurate. Tax: $${String.format("%.2f", taxCalculation.taxAmount)}, Markup: $${String.format("%.2f", markupCalculation.markupAmount)}")
        } catch (e: Exception) {
            TestResult(false, "Tax and markup test failed: ${e.message}")
        }
    }
}

// Test data classes

data class TestResults(
    private val tests: MutableMap<String, TestResult> = mutableMapOf()
) {
    fun addTest(name: String, result: TestResult) {
        tests[name] = result
    }
    
    fun getSummary(): String {
        val passed = tests.values.count { it.passed }
        val total = tests.size
        return "$passed/$total tests passed"
    }
    
    fun getDetailedResults(): String {
        return tests.entries.joinToString("\n") { (name, result) ->
            val status = if (result.passed) "✅ PASS" else "❌ FAIL"
            "$status - $name: ${result.message}"
        }
    }
    
    fun getAllPassed(): Boolean = tests.values.all { it.passed }
}

data class TestResult(
    val passed: Boolean,
    val message: String
)

data class CalculationTestCase(
    val pricingModel: PricingModel,
    val quantity: Double,
    val unitPrice: Double = 0.0,
    val laborHours: Double = 0.0,
    val laborRate: Double = 0.0,
    val materialCosts: List<MaterialCost> = emptyList(),
    val overhead: Double = 0.0,
    val profit: Double = 0.0,
    val expectedBase: Double
)