package com.nextgenbuildpro.features.estimates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.data.repository.HierarchicalCatalogueRepository
import com.nextgenbuildpro.pm.data.repository.AssemblyRepository
import com.nextgenbuildpro.pm.data.repository.TemplateEstimateRepository
import com.nextgenbuildpro.pm.service.*
import com.nextgenbuildpro.pm.test.AssemblyCatalogueIntegrationTest
import kotlinx.coroutines.launch

/**
 * Comprehensive demo screen showcasing assembly catalogue integration
 * Demonstrates all features and provides testing capabilities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssemblyIntegrationDemoScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize all services
    val hierarchicalCatalogueRepository = remember { HierarchicalCatalogueRepository(context) }
    val assemblyRepository = remember { AssemblyRepository() }
    val templateEstimateRepository = remember { TemplateEstimateRepository(context) }
    val assemblyCatalogueService = remember { 
        AssemblyCatalogueService(context, hierarchicalCatalogueRepository, assemblyRepository)
    }
    val calculationEngine = remember { CalculationEngineService() }
    val templateSystemService = remember {
        TemplateSystemService(context, templateEstimateRepository, hierarchicalCatalogueRepository)
    }
    val performanceOptimizer = remember { CataloguePerformanceOptimizer(context) }
    remember { CalculationValidationService() }
    val integrationTest = remember { AssemblyCatalogueIntegrationTest(context) }
    
    // Demo state
    var demoResults by remember { mutableStateOf<String>("") }
    var isLoading by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<AssemblySearchResult>>(emptyList()) }
    var testResults by remember { mutableStateOf<String>("") }
    var performanceStats by remember { mutableStateOf<String>("") }
    
    // Demo functions
    fun runSearchDemo() {
        coroutineScope.launch {
            isLoading = true
            try {
                demoResults = "Running assembly search demo...\n"
                
                // Test various search scenarios
                val basicSearch = assemblyCatalogueService.searchAssemblies(keyword = "wall")
                demoResults += "Basic search for 'wall': ${basicSearch.size} results\n"
                
                val tradeSearch = assemblyCatalogueService.searchAssemblies(tradeType = "Framing")
                demoResults += "Trade search for 'Framing': ${tradeSearch.size} results\n"
                
                val contextSearch = assemblyCatalogueService.searchAssemblies(contextMode = ContextMode.REMODELING)
                demoResults += "Context search for 'Remodeling': ${contextSearch.size} results\n"
                
                searchResults = basicSearch
                demoResults += "\nSearch demo completed successfully!"
            } catch (e: Exception) {
                demoResults += "\nError: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun runCalculationDemo() {
        coroutineScope.launch {
            isLoading = true
            try {
                demoResults = "Running calculation engine demo...\n"
                
                // Test different pricing models
                val fixedPriceCalc = calculationEngine.calculateLineItemTotals(
                    quantity = 10.0,
                    unitPrice = 25.50,
                    pricingModel = PricingModel.FIXED_UNIT_PRICE,
                    overhead = 15.0,
                    profit = 20.0
                )
                demoResults += "Fixed Unit Price: Base: $${String.format("%.2f", fixedPriceCalc.baseTotal)}, Total: $${String.format("%.2f", fixedPriceCalc.total)}\n"
                
                val timeMaterialsCalc = calculationEngine.calculateLineItemTotals(
                    quantity = 5.0,
                    unitPrice = 0.0,
                    pricingModel = PricingModel.TIME_AND_MATERIALS,
                    laborHours = 3.0,
                    laborRate = 45.0,
                    materialCosts = listOf(MaterialCost("Lumber", 15.0, 2.0)),
                    overhead = 10.0,
                    profit = 15.0
                )
                demoResults += "Time & Materials: Labor: $${String.format("%.2f", timeMaterialsCalc.laborCost)}, Material: $${String.format("%.2f", timeMaterialsCalc.materialCost)}, Total: $${String.format("%.2f", timeMaterialsCalc.total)}\n"
                
                // Test tax and markup calculations
                val taxCalc = calculationEngine.applyTaxCalculations(
                    1000.0,
                    TaxSettings(TaxType.PERCENTAGE, rate = 8.25)
                )
                demoResults += "Tax Calculation: Subtotal: $${String.format("%.2f", taxCalc.subtotal)}, Tax: $${String.format("%.2f", taxCalc.taxAmount)}, Total: $${String.format("%.2f", taxCalc.totalWithTax)}\n"
                
                demoResults += "\nCalculation demo completed successfully!"
            } catch (e: Exception) {
                demoResults += "\nError: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun runEstimateDemo() {
        coroutineScope.launch {
            isLoading = true
            try {
                demoResults = "Running estimate creation demo...\n"
                
                // Create new estimate
                val estimate = templateEstimateRepository.createEstimate("demo-project", ContextMode.REMODELING)
                demoResults += "Created estimate: ${estimate.id}\n"
                
                // Get assembly templates
                val assemblyTemplates = templateEstimateRepository.getAssemblyTemplatesByContextMode(ContextMode.REMODELING)
                demoResults += "Found ${assemblyTemplates.size} assembly templates\n"
                
                if (assemblyTemplates.isNotEmpty()) {
                    // Add assembly to estimate
                    val assembly = assemblyTemplates.first()
                    val success = templateEstimateRepository.addAssemblyToEstimate(estimate.id, assembly, 2.0)
                    
                    if (success) {
                        val updatedEstimate = templateEstimateRepository.getById(estimate.id)
                        demoResults += "Added assembly '${assembly.name}' with quantity 2.0\n"
                        demoResults += "Updated estimate total: $${String.format("%.2f", updatedEstimate?.grandTotal ?: 0.0)}\n"
                        
                        // Apply tax and markup
                        val taxMarkupSuccess = templateEstimateRepository.applyTaxAndMarkup(
                            estimate.id,
                            TaxSettings(TaxType.PERCENTAGE, rate = 8.25),
                            MarkupSettings(MarkupType.PERCENTAGE, value = 20.0)
                        )
                        
                        if (taxMarkupSuccess) {
                            val finalEstimate = templateEstimateRepository.getById(estimate.id)
                            demoResults += "Applied 8.25% tax and 20% markup\n"
                            demoResults += "Final estimate total: $${String.format("%.2f", finalEstimate?.grandTotal ?: 0.0)}\n"
                        }
                    }
                }
                
                demoResults += "\nEstimate demo completed successfully!"
            } catch (e: Exception) {
                demoResults += "\nError: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun runTemplateDemo() {
        coroutineScope.launch {
            isLoading = true
            try {
                demoResults = "Running template system demo...\n"
                
                // Create a test estimate first
                val estimate = templateEstimateRepository.createEstimate("template-demo", ContextMode.REMODELING)
                val assemblyTemplates = templateEstimateRepository.getAssemblyTemplatesByContextMode(ContextMode.REMODELING)
                
                if (assemblyTemplates.isNotEmpty()) {
                    templateEstimateRepository.addAssemblyToEstimate(estimate.id, assemblyTemplates.first(), 1.0)
                    val populatedEstimate = templateEstimateRepository.getById(estimate.id)!!
                    
                    // Create template from estimate
                    val template = templateSystemService.createTemplateFromEstimate(
                        populatedEstimate,
                        "Demo Template ${System.currentTimeMillis()}",
                        "Template created for demonstration",
                        listOf(ContextMode.REMODELING),
                        HomeLifecyclePhase.STRUCTURE
                    )
                    
                    if (template != null) {
                        demoResults += "Created template: '${template.name}'\n"
                        demoResults += "Template has ${template.assemblies.size} assemblies\n"
                        
                        // Convert template back to estimate
                        val newEstimate = templateSystemService.convertTemplateToEstimate(
                            template,
                            "template-demo-2",
                            ContextMode.REMODELING
                        )
                        
                        if (newEstimate != null) {
                            demoResults += "Converted template to new estimate: ${newEstimate.id}\n"
                            demoResults += "New estimate total: $${String.format("%.2f", newEstimate.grandTotal)}\n"
                        }
                        
                        // Get template suggestions
                        val suggestions = templateSystemService.generateTemplateSuggestions(
                            ContextMode.REMODELING,
                            HomeLifecyclePhase.STRUCTURE
                        )
                        demoResults += "Found ${suggestions.size} template suggestions\n"
                        
                        // Clean up
                        templateSystemService.deleteTemplate(template.name)
                        demoResults += "Cleaned up demo template\n"
                    }
                }
                
                demoResults += "\nTemplate demo completed successfully!"
            } catch (e: Exception) {
                demoResults += "\nError: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun runPerformanceDemo() {
        coroutineScope.launch {
            isLoading = true
            try {
                performanceStats = "Running performance optimization demo...\n"
                
                val startTime = System.currentTimeMillis()
                
                // Test optimized search
                performanceOptimizer.optimizedSearch(keyword = "wall")
                performanceOptimizer.optimizedSearch(keyword = "wall") // Should hit cache
                performanceOptimizer.optimizedSearch(tradeType = "Framing")
                
                val searchTime = System.currentTimeMillis() - startTime
                performanceStats += "Optimized search completed in ${searchTime}ms\n"
                
                // Get cache statistics
                val cacheStats = performanceOptimizer.getCacheStats()
                performanceStats += "Cache Stats:\n"
                performanceStats += "  Search cache size: ${cacheStats.searchCacheSize}\n"
                performanceStats += "  Assembly cache size: ${cacheStats.assemblyCacheSize}\n"
                performanceStats += "  Hit rate: ${String.format("%.2f", cacheStats.searchHitRate * 100)}%\n"
                performanceStats += "  Memory usage: ${cacheStats.memoryUsageKB} KB\n"
                
                // Test preloading
                performanceOptimizer.preloadFrequentAssemblies(ContextMode.REMODELING)
                performanceStats += "Preloaded frequent assemblies for remodeling context\n"
                
                performanceStats += "\nPerformance demo completed successfully!"
            } catch (e: Exception) {
                performanceStats += "\nError: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun runIntegrationTests() {
        coroutineScope.launch {
            isLoading = true
            try {
                testResults = "Running comprehensive integration tests...\n\n"
                
                val results = integrationTest.runAllTests()
                testResults += results.getDetailedResults()
                testResults += "\n\nTest Summary: ${results.getSummary()}"
                
                if (results.getAllPassed()) {
                    testResults += "\n\n✅ ALL TESTS PASSED! Assembly catalogue integration is working correctly."
                } else {
                    testResults += "\n\n❌ Some tests failed. Please review the results above."
                }
            } catch (e: Exception) {
                testResults += "\nTest execution failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assembly Integration Demo") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Assembly Catalogue Integration Demo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "This demo showcases the comprehensive integration between assembly catalogues and estimate components.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Demo buttons
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Feature Demonstrations",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { runSearchDemo() },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Search")
                            }
                            
                            Button(
                                onClick = { runCalculationDemo() },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calculations")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { runEstimateDemo() },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            ) {
                                Icon(Icons.Default.Assignment, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Estimates")
                            }
                            
                            Button(
                                onClick = { runTemplateDemo() },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            ) {
                                Icon(Icons.Default.FileCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Templates")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { runPerformanceDemo() },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Performance")
                            }
                            
                            Button(
                                onClick = { runIntegrationTests() },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            ) {
                                Icon(Icons.Default.BugReport, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Run Tests")
                            }
                        }
                    }
                }
            }
            
            // Results display
            if (demoResults.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Demo Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = demoResults,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }
            
            if (performanceStats.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Performance Statistics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = performanceStats,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            if (testResults.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Integration Test Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = testResults,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }
            
            // Search results display
            if (searchResults.isNotEmpty()) {
                item {
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            items(searchResults) { result ->
                AssemblySearchResultCard(
                    assembly = result,
                    onSelect = { /* Demo only - no action needed */ }
                )
            }
            
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

enum class DemoSection {
    SEARCH,
    CALCULATIONS,
    ESTIMATES,
    TEMPLATES,
    PERFORMANCE,
    TESTS
}