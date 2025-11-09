package com.nextgenbuildpro.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.pm.data.model.LaborRate
import com.nextgenbuildpro.pm.data.model.MaterialCost
import com.nextgenbuildpro.pm.data.model.RegionalAdjustment
import com.nextgenbuildpro.pm.data.repository.CostDatabaseRepository
import com.nextgenbuildpro.pm.service.CostDataService
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

/**
 * Screen for managing cost database settings and baseline data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostDatabaseSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Services
    val costDataService = remember { CostDataService.create(context) }
    val costRepository = remember { CostDatabaseRepository.getInstance() }
    
    // State
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Labor Rates", "Materials", "Regional", "Updates")
    
    var isUpdating by remember { mutableStateOf(false) }
    var updateStatus by remember { mutableStateOf("") }
    
    // Data state
    val laborRates by costRepository.laborRates.collectAsState()
    val materialCosts by costRepository.materialCosts.collectAsState()
    val isInitialized by costRepository.isInitialized.collectAsState()
    
    // Initialize database if needed
    LaunchedEffect(Unit) {
        if (!isInitialized) {
            try {
                costRepository.initializeDatabase(costDataService)
            } catch (e: Exception) {
                updateStatus = "Error initializing database: ${e.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cost Database Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isUpdating = true
                                updateStatus = "Updating from external sources..."
                                try {
                                    costDataService.updateFromExternalSources()
                                    updateStatus = "Update completed successfully"
                                } catch (e: Exception) {
                                    updateStatus = "Update failed: ${e.message}"
                                } finally {
                                    isUpdating = false
                                }
                            }
                        }
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Update Data")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status message
            if (updateStatus.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (updateStatus.contains("Error") || updateStatus.contains("failed")) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = updateStatus,
                        modifier = Modifier.padding(16.dp),
                        color = if (updateStatus.contains("Error") || updateStatus.contains("failed"))
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Tab Content
            when (selectedTab) {
                0 -> LaborRatesTab(laborRates, costRepository, coroutineScope)
                1 -> MaterialCostsTab(materialCosts, costRepository, coroutineScope)
                2 -> RegionalAdjustmentsTab(costDataService)
                3 -> UpdatesTab(costDataService, isUpdating, updateStatus) { status ->
                    updateStatus = status
                }
            }
        }
    }
}

@Composable
private fun LaborRatesTab(
    laborRates: Map<String, LaborRate>,
    repository: CostDatabaseRepository,
    coroutineScope: CoroutineScope
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Industry Labor Rates",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        items(laborRates.toList()) { (key, rate) ->
            LaborRateCard(
                tradeKey = key,
                laborRate = rate,
                onUpdate = { updatedRate ->
                    coroutineScope.launch {
                        repository.updateLaborRate(key, updatedRate)
                    }
                }
            )
        }
    }
}

@Composable
private fun LaborRateCard(
    tradeKey: String,
    laborRate: LaborRate,
    onUpdate: (LaborRate) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = laborRate.trade,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Journeyman: ${NumberFormat.getCurrencyInstance().format(laborRate.journeymanRate)}/hr",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = laborRate.region,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Rate")
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LaborRateDetail("Base Rate", laborRate.baseHourlyRate)
                    LaborRateDetail("Skilled Rate", laborRate.skilledRate)
                    LaborRateDetail("Journeyman Rate", laborRate.journeymanRate)
                    LaborRateDetail("Foreman Rate", laborRate.foremanRate)
                    LaborRateDetail("Benefits", laborRate.benefits)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Source: ${laborRate.source}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (laborRate.notes.isNotEmpty()) {
                        Text(
                            text = "Notes: ${laborRate.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (isEditing) {
                LaborRateEditor(
                    laborRate = laborRate,
                    onSave = { updatedRate ->
                        onUpdate(updatedRate)
                        isEditing = false
                    },
                    onCancel = { isEditing = false }
                )
            }
        }
    }
}

@Composable
private fun LaborRateDetail(label: String, value: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = NumberFormat.getCurrencyInstance().format(value),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LaborRateEditor(
    laborRate: LaborRate,
    onSave: (LaborRate) -> Unit,
    onCancel: () -> Unit
) {
    var journeymanRate by remember { mutableStateOf(laborRate.journeymanRate.toString()) }
    var benefits by remember { mutableStateOf(laborRate.benefits.toString()) }
    
    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = journeymanRate,
            onValueChange = { journeymanRate = it },
            label = { Text("Journeyman Rate") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = benefits,
            onValueChange = { benefits = it },
            label = { Text("Benefits") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = {
                    val updatedRate = laborRate.copy(
                        journeymanRate = journeymanRate.toDoubleOrNull() ?: laborRate.journeymanRate,
                        benefits = benefits.toDoubleOrNull() ?: laborRate.benefits
                    )
                    onSave(updatedRate)
                }
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun MaterialCostsTab(
    materialCosts: Map<String, MaterialCost>,
    repository: CostDatabaseRepository,
    coroutineScope: CoroutineScope
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Material Costs Database",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        items(materialCosts.toList()) { (key, cost) ->
            MaterialCostCard(
                materialKey = key,
                materialCost = cost,
                onUpdate = { updatedCost ->
                    coroutineScope.launch {
                        repository.updateMaterialCost(key, updatedCost)
                    }
                }
            )
        }
    }
}

@Composable
private fun MaterialCostCard(
    materialKey: String,
    materialCost: MaterialCost,
    onUpdate: (MaterialCost) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = materialCost.item,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${NumberFormat.getCurrencyInstance().format(materialCost.cost)} per ${materialCost.unit}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = materialCost.supplier,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Region: ${materialCost.region}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Last Updated: ${materialCost.lastUpdated}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    if (materialCost.notes.isNotEmpty()) {
                        Text(
                            text = "Notes: ${materialCost.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegionalAdjustmentsTab(costDataService: CostDataService) {
    val regionalAdjustments by costDataService.regionalAdjustments.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Regional Cost Adjustments",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Based on RSMeans City Cost Index 2024",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        items(regionalAdjustments.toList()) { (key, adjustment) ->
            RegionalAdjustmentCard(adjustment = adjustment)
        }
    }
}

@Composable
private fun RegionalAdjustmentCard(adjustment: RegionalAdjustment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = adjustment.region,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AdjustmentFactor("Labor", adjustment.laborMultiplier)
                AdjustmentFactor("Materials", adjustment.materialMultiplier)
                AdjustmentFactor("Equipment", adjustment.equipmentMultiplier)
            }
            
            if (adjustment.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = adjustment.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AdjustmentFactor(label: String, multiplier: Double) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "${(multiplier * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = when {
                multiplier > 1.1 -> MaterialTheme.colorScheme.error
                multiplier < 0.9 -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun UpdatesTab(
    costDataService: CostDataService,
    isUpdating: Boolean,
    updateStatus: String,
    onUpdateStatus: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Data Source Updates",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Automatic Updates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Cost data is automatically updated from:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                val sources = listOf(
                    "• Bureau of Labor Statistics (BLS)",
                    "• RSMeans Cost Database",
                    "• Building News Intelligence (BNI)",
                    "• Trade Union Rate Sheets",
                    "• Regional Construction Boards"
                )
                
                sources.forEach { source ->
                    Text(
                        text = source,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Manual Update",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Force update all cost data from external sources",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            onUpdateStatus("Updating cost database...")
                            try {
                                costDataService.updateFromExternalSources()
                                onUpdateStatus("Update completed successfully at ${Date()}")
                            } catch (e: Exception) {
                                onUpdateStatus("Update failed: ${e.message}")
                            }
                        }
                    },
                    enabled = !isUpdating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isUpdating) "Updating..." else "Update Now")
                }
            }
        }
        
        if (updateStatus.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = updateStatus,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}