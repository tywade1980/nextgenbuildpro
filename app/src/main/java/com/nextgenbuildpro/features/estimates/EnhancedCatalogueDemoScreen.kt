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
import com.nextgenbuildpro.pm.data.repository.EnhancedCatalogueDataService
import com.nextgenbuildpro.pm.data.repository.HierarchicalCatalogueRepository
import com.nextgenbuildpro.pm.data.repository.AssemblyRepository
import com.nextgenbuildpro.pm.service.EnhancedAssemblyCatalogueService
import com.nextgenbuildpro.pm.service.AssemblyCreationData
import kotlinx.coroutines.launch

/**
 * Enhanced Catalogue Demo Screen
 * 
 * Demonstrates the new hierarchical catalogue structure with
 * Category -> Trade -> Scope -> Assembly -> Task/Material organization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCatalogueDemoScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize services
    val enhancedCatalogueDataService = remember { EnhancedCatalogueDataService(context) }
    val hierarchicalCatalogueRepository = remember { HierarchicalCatalogueRepository(context) }
    val assemblyRepository = remember { AssemblyRepository() }
    val enhancedService = remember { 
        EnhancedAssemblyCatalogueService(
            context,
            enhancedCatalogueDataService,
            hierarchicalCatalogueRepository,
            assemblyRepository
        )
    }
    
    // State
    var catalogue by remember { mutableStateOf<CompleteCatalogue?>(null) }
    var searchResults by remember { mutableStateOf<List<AssemblySearchResultWithContext>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedTrade by remember { mutableStateOf<Trade?>(null) }
    var selectedScope by remember { mutableStateOf<Scope?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var demoResults by remember { mutableStateOf("") }
    
    // Demo functions
    fun loadCatalogue() {
        coroutineScope.launch {
            isLoading = true
            try {
                demoResults = "Loading enhanced catalogue...\n"
                catalogue = enhancedService.getCompleteCatalogue()
                demoResults += "Catalogue loaded successfully!\n"
                demoResults += "Categories: ${catalogue?.categories?.size ?: 0}\n"
                catalogue?.categories?.forEach { categoryWithChildren ->
                    demoResults += "  - ${categoryWithChildren.category.name}: ${categoryWithChildren.trades.size} trades\n"
                    categoryWithChildren.trades.forEach { tradeWithChildren ->
                        demoResults += "    - ${tradeWithChildren.trade.name}: ${tradeWithChildren.scopes.size} scopes\n"
                        tradeWithChildren.scopes.forEach { scopeWithChildren ->
                            demoResults += "      - ${scopeWithChildren.scope.name}: ${scopeWithChildren.assemblies.size} assemblies\n"
                        }
                    }
                }
            } catch (e: Exception) {
                demoResults += "Error: ${e.message}\n"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun searchAssemblies() {
        coroutineScope.launch {
            isLoading = true
            try {
                demoResults = "Searching assemblies...\n"
                searchResults = enhancedService.searchAssembliesEnhanced(
                    keyword = searchQuery.takeIf { it.isNotBlank() },
                    categoryId = selectedCategory?.id,
                    tradeId = selectedTrade?.id,
                    scopeId = selectedScope?.id
                )
                demoResults += "Found ${searchResults.size} assemblies\n"
                searchResults.forEach { result ->
                    demoResults += "  - ${result.assembly.name} (${result.category.name} > ${result.trade.name} > ${result.scope.name})\n"
                    demoResults += "    Labor: ${result.assembly.laborHours}h, Cost: $${String.format("%.2f", result.assembly.totalCost)}\n"
                }
            } catch (e: Exception) {
                demoResults += "Search error: ${e.message}\n"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun createSampleAssembly() {
        coroutineScope.launch {
            isLoading = true
            try {
                demoResults = "Creating sample assembly...\n"
                
                // First create necessary hierarchy if it doesn't exist
                val categoryResult = enhancedCatalogueDataService.createCategory(
                    name = "Sample Category",
                    description = "Demo category for testing",
                    sequence = 10
                )
                
                val category = categoryResult.getOrThrow()
                demoResults += "Created category: ${category.name}\n"
                
                val tradeResult = enhancedCatalogueDataService.createTrade(
                    categoryId = category.id,
                    name = "Sample Trade",
                    description = "Demo trade for testing",
                    sequence = 1
                )
                
                val trade = tradeResult.getOrThrow()
                demoResults += "Created trade: ${trade.name}\n"
                
                val scopeResult = enhancedCatalogueDataService.createScope(
                    tradeId = trade.id,
                    name = "Sample Scope",
                    description = "Demo scope for testing",
                    sequence = 1
                )
                
                val scope = scopeResult.getOrThrow()
                demoResults += "Created scope: ${scope.name}\n"
                
                // Create complete assembly with tasks and materials
                val assemblyData = AssemblyCreationData(
                    name = "Sample Assembly",
                    description = "Demo assembly created from enhanced catalogue demo",
                    sequence = 1,
                    unit = "EA",
                    laborHours = 8.0,
                    materialCost = 150.0,
                    laborCost = 360.0, // 8 hours * $45/hour
                    equipmentCost = 25.0,
                    markupPercentage = 0.2,
                    tags = listOf("demo", "sample", "test")
                )
                
                val tasks = listOf(
                    TaskCreationData(
                        name = "Preparation",
                        description = "Prepare work area and materials",
                        sequence = 1,
                        laborHours = 2.0,
                        materialCost = 25.0,
                        laborCost = 90.0
                    ),
                    TaskCreationData(
                        name = "Installation",
                        description = "Install assembly components",
                        sequence = 2,
                        laborHours = 5.0,
                        materialCost = 100.0,
                        laborCost = 225.0
                    ),
                    TaskCreationData(
                        name = "Finishing",
                        description = "Complete and clean up",
                        sequence = 3,
                        laborHours = 1.0,
                        materialCost = 25.0,
                        laborCost = 45.0
                    )
                )
                
                val materials = listOf(
                    MaterialCreationData(
                        name = "Primary Material",
                        description = "Main component material",
                        quantity = 10.0,
                        unit = "LF",
                        unitCost = 12.50,
                        waste = 0.1
                    ),
                    MaterialCreationData(
                        name = "Fasteners",
                        description = "Screws and bolts",
                        quantity = 25.0,
                        unit = "EA",
                        unitCost = 1.25
                    ),
                    MaterialCreationData(
                        name = "Sealant",
                        description = "Weatherproofing sealant",
                        quantity = 2.0,
                        unit = "TUBE",
                        unitCost = 8.75
                    )
                )
                
                val completeAssembly = enhancedService.createCompleteAssembly(
                    scopeId = scope.id,
                    assemblyData = assemblyData,
                    tasks = tasks,
                    materials = materials
                )
                
                if (completeAssembly != null) {
                    demoResults += "Created complete assembly: ${completeAssembly.assembly.name}\n"
                    demoResults += "  Tasks: ${completeAssembly.tasks.size}\n"
                    demoResults += "  Materials: ${completeAssembly.materials.size}\n"
                    demoResults += "  Total Cost: $${String.format("%.2f", completeAssembly.assembly.totalCost)}\n"
                } else {
                    demoResults += "Failed to create complete assembly\n"
                }
                
            } catch (e: Exception) {
                demoResults += "Creation error: ${e.message}\n"
            } finally {
                isLoading = false
            }
        }
    }
    
    // Load catalogue on first composition
    LaunchedEffect(Unit) {
        loadCatalogue()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enhanced Catalogue Demo") },
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
                    text = "Enhanced Catalogue Structure Demo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "This demo showcases the enhanced hierarchical catalogue structure with Category → Trade → Scope → Assembly → Task/Material organization, as suggested in the comments.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Demo controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Demo Actions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { loadCatalogue() },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Load Catalogue")
                            }
                            
                            Button(
                                onClick = { createSampleAssembly() },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Create Sample")
                            }
                        }
                    }
                }
            }
            
            // Search controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Enhanced Search",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search assemblies...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Category selection
                        catalogue?.categories?.let { categories ->
                            var categoryMenuExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = categoryMenuExpanded,
                                onExpandedChange = { categoryMenuExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedCategory?.name ?: "All Categories",
                                    onValueChange = { },
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                                    label = { Text("Category") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = categoryMenuExpanded,
                                    onDismissRequest = { categoryMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Categories") },
                                        onClick = {
                                            selectedCategory = null
                                            selectedTrade = null
                                            selectedScope = null
                                            categoryMenuExpanded = false
                                        }
                                    )
                                    categories.forEach { categoryWithChildren ->
                                        DropdownMenuItem(
                                            text = { Text(categoryWithChildren.category.name) },
                                            onClick = {
                                                selectedCategory = categoryWithChildren.category
                                                selectedTrade = null
                                                selectedScope = null
                                                categoryMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { searchAssemblies() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Search Enhanced Catalogue")
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
                                    .heightIn(max = 300.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }
            
            // Search results
            if (searchResults.isNotEmpty()) {
                item {
                    Text(
                        text = "Enhanced Search Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(searchResults) { result ->
                    EnhancedAssemblyCard(result = result)
                }
            }
        }
    }
}

@Composable
fun EnhancedAssemblyCard(result: AssemblySearchResultWithContext) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with hierarchy
            Text(
                text = "${result.category.name} → ${result.trade.name} → ${result.scope.name}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Assembly name and description
            Text(
                text = result.assembly.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (result.assembly.description.isNotEmpty()) {
                Text(
                    text = result.assembly.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Cost breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Labor: ${result.assembly.laborHours}h",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Unit: ${result.assembly.unit}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("$%.2f", result.assembly.totalCost),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${result.assembly.markupPercentage * 100}% markup",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Tasks and materials count
            if (result.tasks.isNotEmpty() || result.materials.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    if (result.tasks.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "${result.tasks.size} tasks",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    if (result.materials.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "${result.materials.size} materials",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Tags
            if (result.assembly.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    result.assembly.tags.take(3).forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    if (result.assembly.tags.size > 3) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "+${result.assembly.tags.size - 3}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}