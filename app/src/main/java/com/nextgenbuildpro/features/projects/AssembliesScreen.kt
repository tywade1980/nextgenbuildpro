package com.nextgenbuildpro.features.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nextgenbuildpro.pm.data.model.Assembly
import com.nextgenbuildpro.pm.data.model.JobTemplate
import com.nextgenbuildpro.pm.data.model.TradeCategory
import com.nextgenbuildpro.pm.data.repository.AssemblyRepository
import kotlinx.coroutines.launch

/**
 * Screen for browsing assemblies and job templates organized by trade
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssembliesScreen(navController: NavController) {
    val assemblyRepository = remember { AssemblyRepository() }
    val coroutineScope = rememberCoroutineScope()

    // State for trade categories, assemblies, and job templates
    var tradeCategories by remember { mutableStateOf<List<TradeCategory>>(emptyList()) }
    var selectedTradeId by remember { mutableStateOf<String?>(null) }
    var assemblies by remember { mutableStateOf<List<Assembly>>(emptyList()) }
    var jobTemplates by remember { mutableStateOf<List<JobTemplate>>(emptyList()) }

    // Load data
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            tradeCategories = assemblyRepository.getAllTradeCategories()
            if (tradeCategories.isNotEmpty()) {
                selectedTradeId = tradeCategories.first().id
                loadTradeData(selectedTradeId, assemblyRepository) { loadedAssemblies, loadedTemplates ->
                    assemblies = loadedAssemblies
                    jobTemplates = loadedTemplates
                }
            }
        }
    }

    // Effect to load data when selected trade changes
    LaunchedEffect(selectedTradeId) {
        if (selectedTradeId != null) {
            loadTradeData(selectedTradeId, assemblyRepository) { loadedAssemblies, loadedTemplates ->
                assemblies = loadedAssemblies
                jobTemplates = loadedTemplates
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Assemblies & Templates") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implement search */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
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
        ) {
            // Trade categories horizontal list
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(tradeCategories) { trade ->
                    TradeCategoryChip(
                        trade = trade,
                        selected = trade.id == selectedTradeId,
                        onClick = { selectedTradeId = trade.id }
                    )
                }
            }

            // Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Job Templates section
                item {
                    Text(
                        text = "Job Templates",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(jobTemplates) { template ->
                    JobTemplateCard(
                        template = template,
                        onClick = {
                            // Navigate to template detail screen
                            navController.navigate("template_detail/${template.id}")
                        }
                    )
                }

                // Assemblies section
                item {
                    Text(
                        text = "Assemblies",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(assemblies) { assembly ->
                    AssemblyCard(
                        assembly = assembly,
                        onClick = {
                            // Navigate to assembly detail screen
                            navController.navigate("assembly_detail/${assembly.id}")
                        }
                    )
                }
            }
        }
    }
}

/**
 * Load assemblies and job templates for a specific trade
 */
private suspend fun loadTradeData(
    tradeId: String?,
    repository: AssemblyRepository,
    onDataLoaded: (List<Assembly>, List<JobTemplate>) -> Unit
) {
    if (tradeId == null) {
        onDataLoaded(emptyList(), emptyList())
        return
    }

    val assemblies = repository.getAssembliesByTrade(tradeId)
    val templates = repository.getJobTemplatesByTrade(tradeId)
    onDataLoaded(assemblies, templates)
}

/**
 * Trade category chip component
 */
@Composable
fun TradeCategoryChip(
    trade: TradeCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on trade category
            val icon = when (trade.iconName) {
                "build" -> Icons.Default.Build
                "electrical_services" -> Icons.Default.Bolt
                "plumbing" -> Icons.Default.WaterDrop
                "air" -> Icons.Default.AcUnit
                "format_paint" -> Icons.Default.Brush
                else -> Icons.Default.Construction
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = trade.name,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Job template card component
 */
@Composable
fun JobTemplateCard(
    template: JobTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = template.tradeName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$${template.estimatedCost}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = template.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${template.estimatedDuration} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${template.phases.size} phases",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Assembly card component
 */
@Composable
fun AssemblyCard(
    assembly: Assembly,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assembly.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = assembly.tradeName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$${assembly.estimatedCost}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = assembly.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${assembly.laborHours} hours",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${assembly.materials.size} materials",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
