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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.data.repository.TemplateEstimateRepository
import com.nextgenbuildpro.pm.service.AssemblyCatalogueService
import com.nextgenbuildpro.pm.service.CalculationEngineService
import com.nextgenbuildpro.pm.service.AssemblySearchResult
import com.nextgenbuildpro.pm.service.TaxSettings
import com.nextgenbuildpro.pm.service.MarkupSettings
import com.nextgenbuildpro.pm.service.TaxType
import com.nextgenbuildpro.pm.service.MarkupType
import kotlinx.coroutines.launch

/**
 * Enhanced Estimate Editor Screen with Assembly Catalogue Integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedEstimateEditorScreen(
    navController: NavController,
    estimateId: String? = null,
    projectId: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize services
    val templateEstimateRepository = remember { TemplateEstimateRepository(context) }
    val calculationEngine = remember { CalculationEngineService() }
    
    // State for estimate
    var estimate by remember { mutableStateOf<TemplateEstimate?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // State for adding assemblies
    var showAssemblySearch by remember { mutableStateOf(false) }
    var showTaxMarkupDialog by remember { mutableStateOf(false) }
    
    // Tax and markup settings
    var taxRate by remember { mutableStateOf("8.25") }
    var markupPercentage by remember { mutableStateOf("20.0") }
    
    // Load estimate if editing existing one
    LaunchedEffect(estimateId) {
        if (estimateId != null) {
            estimate = templateEstimateRepository.getById(estimateId)
        } else if (projectId != null) {
            // Create new estimate for project
            estimate = templateEstimateRepository.createEstimate(projectId, ContextMode.REMODELING)
        }
    }
    
    // Function to add assembly to estimate
    fun addAssemblyToEstimate(assembly: AssemblySearchResult, quantity: Double) {
        coroutineScope.launch {
            isLoading = true
            try {
                estimate?.let { currentEstimate ->
                    // Find assembly template
                    val assemblyTemplates = templateEstimateRepository.getAssemblyTemplatesByContextMode(currentEstimate.contextMode)
                    val assemblyTemplate = assemblyTemplates.find { it.name == assembly.name }
                    
                    if (assemblyTemplate != null) {
                        val success = templateEstimateRepository.addAssemblyToEstimate(
                            currentEstimate.id, 
                            assemblyTemplate, 
                            quantity
                        )
                        
                        if (success) {
                            // Reload estimate
                            estimate = templateEstimateRepository.getById(currentEstimate.id)
                        } else {
                            errorMessage = "Failed to add assembly to estimate"
                            showError = true
                        }
                    } else {
                        errorMessage = "Assembly template not found"
                        showError = true
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error adding assembly: ${e.message}"
                showError = true
            } finally {
                isLoading = false
            }
        }
    }
    
    // Function to apply tax and markup
    fun applyTaxAndMarkup() {
        coroutineScope.launch {
            estimate?.let { currentEstimate ->
                val taxSettings = TaxSettings(
                    type = TaxType.PERCENTAGE,
                    rate = taxRate.toDoubleOrNull() ?: 0.0
                )
                
                val markupSettings = MarkupSettings(
                    type = MarkupType.PERCENTAGE,
                    value = markupPercentage.toDoubleOrNull() ?: 0.0
                )
                
                val success = templateEstimateRepository.applyTaxAndMarkup(
                    currentEstimate.id,
                    taxSettings,
                    markupSettings
                )
                
                if (success) {
                    estimate = templateEstimateRepository.getById(currentEstimate.id)
                    showTaxMarkupDialog = false
                } else {
                    errorMessage = "Failed to apply tax and markup"
                    showError = true
                }
            }
        }
    }
    
    // Error dialog
    if (showError) {
        Dialog(onDismissRequest = { showError = false }) {
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
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showError = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
    
    // Tax and markup dialog
    if (showTaxMarkupDialog) {
        Dialog(onDismissRequest = { showTaxMarkupDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Tax & Markup Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = taxRate,
                        onValueChange = { taxRate = it },
                        label = { Text("Tax Rate (%)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = markupPercentage,
                        onValueChange = { markupPercentage = it },
                        label = { Text("Markup (%)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showTaxMarkupDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        
                        Button(
                            onClick = { applyTaxAndMarkup() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estimate Editor") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showTaxMarkupDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Tax & Markup"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAssemblySearch = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Assembly"
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            estimate?.let { currentEstimate ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // Estimate summary card
                    EstimateSummaryCard(estimate = currentEstimate)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Assemblies list
                    Text(
                        text = "Assemblies",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (currentEstimate.assemblies.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "No assemblies added yet",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                Text(
                                    text = "Tap the + button to add assemblies",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(currentEstimate.assemblies) { assembly ->
                                AssemblyItemCard(
                                    assembly = assembly,
                                    onEdit = { /* Handle edit */ },
                                    onDelete = {
                                        coroutineScope.launch {
                                            val success = templateEstimateRepository.removeAssemblyFromEstimate(
                                                currentEstimate.id,
                                                assembly.id
                                            )
                                            if (success) {
                                                estimate = templateEstimateRepository.getById(currentEstimate.id)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Estimate not found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
    
    // Assembly search screen
    if (showAssemblySearch) {
        AssemblySearchAndSelectionScreen(
            navController = navController,
            onAssemblySelected = { assembly, quantity ->
                addAssemblyToEstimate(assembly, quantity)
                showAssemblySearch = false
            }
        )
    }
}

@Composable
fun EstimateSummaryCard(estimate: TemplateEstimate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Estimate Total",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = String.format("$%.2f", estimate.grandTotal),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Assemblies: ${estimate.assemblies.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = estimate.contextMode.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Labor: ${String.format("$%.2f", estimate.subtotalLabor)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Material: ${String.format("$%.2f", estimate.subtotalMaterial)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Markup: ${String.format("$%.2f", estimate.markupTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun AssemblyItemCard(
    assembly: TemplateAssembly,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = assembly.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Qty: ${assembly.quantity} ${assembly.quantityUnit.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = String.format("$%.2f", assembly.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            if (assembly.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = assembly.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}