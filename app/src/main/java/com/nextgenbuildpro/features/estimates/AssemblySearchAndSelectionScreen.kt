package com.nextgenbuildpro.features.estimates

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.data.repository.HierarchicalCatalogueRepository
import com.nextgenbuildpro.pm.data.repository.AssemblyRepository
import com.nextgenbuildpro.pm.service.AssemblyCatalogueService
import com.nextgenbuildpro.pm.service.AssemblySearchResult
import kotlinx.coroutines.launch

/**
 * Assembly Search and Selection Component
 * Provides comprehensive search and selection interface for assemblies
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssemblySearchAndSelectionScreen(
    navController: NavController,
    onAssemblySelected: (AssemblySearchResult, Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize services
    val hierarchicalCatalogueRepository = remember { HierarchicalCatalogueRepository(context) }
    val assemblyRepository = remember { AssemblyRepository() }
    val assemblyCatalogueService = remember { 
        AssemblyCatalogueService(context, hierarchicalCatalogueRepository, assemblyRepository) 
    }
    
    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var selectedTrade by remember { mutableStateOf<String?>(null) }
    var selectedContextMode by remember { mutableStateOf<ContextMode?>(null) }
    var searchResults by remember { mutableStateOf<List<AssemblySearchResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    
    // Selection state
    var selectedAssembly by remember { mutableStateOf<AssemblySearchResult?>(null) }
    var quantity by remember { mutableStateOf("1.0") }
    var showQuantityDialog by remember { mutableStateOf(false) }
    
    // Available trades and context modes
    val availableTrades = listOf(
        "Framing", "Drywall", "Electrical", "Plumbing", "HVAC", 
        "Roofing", "Flooring", "Painting", "Tile", "Cabinets"
    )
    
    val availableContextModes = ContextMode.values().toList()
    
    // Search function
    fun performSearch() {
        coroutineScope.launch {
            isLoading = true
            try {
                searchResults = assemblyCatalogueService.searchAssemblies(
                    keyword = searchQuery.takeIf { it.isNotBlank() },
                    tradeType = selectedTrade,
                    contextMode = selectedContextMode
                )
            } catch (e: Exception) {
                // Handle error
                searchResults = emptyList()
            } finally {
                isLoading = false
            }
        }
    }
    
    // Load initial results
    LaunchedEffect(Unit) {
        performSearch()
    }
    
    // Quantity selection dialog
    if (showQuantityDialog && selectedAssembly != null) {
        Dialog(onDismissRequest = { showQuantityDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Quantity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = selectedAssembly!!.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showQuantityDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        
                        Button(
                            onClick = {
                                val qty = quantity.toDoubleOrNull() ?: 1.0
                                onAssemblySelected(selectedAssembly!!, qty)
                                showQuantityDialog = false
                                selectedAssembly = null
                                navController.navigateUp()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add to Estimate")
                        }
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assembly Search") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    // Trigger search on text change with debounce would be ideal
                },
                label = { Text("Search assemblies...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = ""
                            performSearch()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Search button
            Button(
                onClick = { performSearch() },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Search")
            }
            
            // Filters section
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Trade filter
                        var tradeMenuExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = tradeMenuExpanded,
                            onExpandedChange = { tradeMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedTrade ?: "All Trades",
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tradeMenuExpanded) },
                                label = { Text("Trade Category") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = tradeMenuExpanded,
                                onDismissRequest = { tradeMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Trades") },
                                    onClick = {
                                        selectedTrade = null
                                        tradeMenuExpanded = false
                                        performSearch()
                                    }
                                )
                                availableTrades.forEach { trade ->
                                    DropdownMenuItem(
                                        text = { Text(trade) },
                                        onClick = {
                                            selectedTrade = trade
                                            tradeMenuExpanded = false
                                            performSearch()
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Context mode filter
                        var contextMenuExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = contextMenuExpanded,
                            onExpandedChange = { contextMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedContextMode?.name ?: "All Context Modes",
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contextMenuExpanded) },
                                label = { Text("Context Mode") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = contextMenuExpanded,
                                onDismissRequest = { contextMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Context Modes") },
                                    onClick = {
                                        selectedContextMode = null
                                        contextMenuExpanded = false
                                        performSearch()
                                    }
                                )
                                availableContextModes.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode.name) },
                                        onClick = {
                                            selectedContextMode = mode
                                            contextMenuExpanded = false
                                            performSearch()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Results section
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No assemblies found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { assembly ->
                        AssemblySearchResultCard(
                            assembly = assembly,
                            onSelect = {
                                selectedAssembly = assembly
                                showQuantityDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssemblySearchResultCard(
    assembly: AssemblySearchResult,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = assembly.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = assembly.tradeCategory,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = String.format("$%.2f", assembly.estimatedCost),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "${assembly.laborHours} hrs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (assembly.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = assembly.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (assembly.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    assembly.tags.take(3).forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    
                    if (assembly.tags.size > 3) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "+${assembly.tags.size - 3}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}