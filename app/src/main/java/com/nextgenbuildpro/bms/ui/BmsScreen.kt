package com.nextgenbuildpro.bms.ui

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.bms.data.model.*
import com.nextgenbuildpro.bms.data.repository.BmsRepository

/**
 * Building Management System Main Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmsScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Buildings", "Materials", "Inspections", "Performance")
    val repository = remember { BmsRepository(navController.context) }
    
    val buildings by repository.buildings.collectAsState()
    val materials by repository.materials.collectAsState()
    val inspections by repository.inspections.collectAsState()
    val performance by repository.performance.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Building Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Add new building */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Building")
                    }
                }
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> FloatingActionButton(onClick = { /* Add building */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Building")
                }
                1 -> FloatingActionButton(onClick = { /* Add material */ }) {
                    Icon(Icons.Default.Inventory, contentDescription = "Add Material")
                }
                2 -> FloatingActionButton(onClick = { /* Schedule inspection */ }) {
                    Icon(Icons.Default.Schedule, contentDescription = "Schedule Inspection")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                0 -> BuildingsTab(buildings = buildings, onBuildingClick = { /* Navigate to building detail */ })
                1 -> MaterialsTab(materials = materials)
                2 -> InspectionsTab(inspections = inspections)
                3 -> PerformanceTab(performance = performance)
            }
        }
    }
}

@Composable
private fun BuildingsTab(
    buildings: List<Building>,
    onBuildingClick: (Building) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Active Projects",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${buildings.size} buildings in progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Buildings
        items(buildings) { building ->
            BuildingCard(
                building = building,
                onClick = { onBuildingClick(building) }
            )
        }
    }
}

@Composable
private fun MaterialsTab(materials: List<Material>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Material Inventory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Track materials across all projects",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Material Status Overview
        item {
            MaterialStatusOverview()
        }

        // Materials by Type
        val materialsByType = materials.groupBy { it.type }
        materialsByType.forEach { (type, typeMaterials) ->
            item {
                Text(
                    text = type.name.replace("_", " "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(typeMaterials) { material ->
                MaterialCard(material = material)
            }
        }
    }
}

@Composable
private fun InspectionsTab(inspections: List<Inspection>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Inspections & Compliance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${inspections.size} inspections tracked",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Inspection Overview
        item {
            InspectionOverview(inspections = inspections)
        }

        // Recent Inspections
        item {
            Text(
                text = "Recent Inspections",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        items(inspections.sortedByDescending { it.scheduledDate }) { inspection ->
            InspectionCard(inspection = inspection)
        }
    }
}

@Composable
private fun PerformanceTab(performance: List<BuildingPerformance>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Building Performance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Performance Overview
        item {
            PerformanceOverview(performance = performance)
        }

        // Individual Building Performance
        items(performance) { buildingPerformance ->
            BuildingPerformanceCard(performance = buildingPerformance)
        }
    }
}

@Composable
private fun BuildingCard(
    building: Building,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (building.type) {
                        BuildingType.RESIDENTIAL -> Icons.Default.Home
                        BuildingType.COMMERCIAL -> Icons.Default.Business
                        BuildingType.INDUSTRIAL -> Icons.Default.Factory
                        BuildingType.INSTITUTIONAL -> Icons.Default.School
                        BuildingType.MIXED_USE -> Icons.Default.Apartment
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = building.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = building.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "PM: ${building.projectManager}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                StatusChip(status = building.status.name)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Floors",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = building.floors.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column {
                    Text(
                        text = "Area",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${building.totalArea.toInt()} sq ft",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column {
                    Text(
                        text = "Completion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = building.estimatedCompletion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialCard(material: Material) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = material.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${material.quantity} ${material.unit} - ${material.supplier}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            StatusChip(status = material.status.name)
        }
    }
}

@Composable
private fun InspectionCard(inspection: Inspection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = inspection.type.name.replace("_", " "),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Inspector: ${inspection.inspector}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Scheduled: ${inspection.scheduledDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                StatusChip(status = inspection.status.name)
            }
            
            if (inspection.results != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Score: ${inspection.results.score?.toInt() ?: "N/A"}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Violations: ${inspection.results.violations.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun BuildingPerformanceCard(performance: BuildingPerformance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Building Performance Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Energy",
                    value = "${performance.energyEfficiency.efficiency.toInt()}%"
                )
                MetricItem(
                    label = "Safety",
                    value = "${performance.safetyMetrics.safetyScore.toInt()}%"
                )
                MetricItem(
                    label = "Quality",
                    value = "${performance.qualityMetrics.qualityScore.toInt()}%"
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    AssistChip(
        onClick = { },
        label = { 
            Text(
                text = status.replace("_", " "),
                style = MaterialTheme.typography.bodySmall
            ) 
        }
    )
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MaterialStatusOverview() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Material Status Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(label = "Ordered", value = "25")
                MetricItem(label = "Delivered", value = "18")
                MetricItem(label = "Installed", value = "12")
            }
        }
    }
}

@Composable
private fun InspectionOverview(inspections: List<Inspection>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Inspection Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val passed = inspections.count { it.status == InspectionStatus.PASSED }
            val scheduled = inspections.count { it.status == InspectionStatus.SCHEDULED }
            val failed = inspections.count { it.status == InspectionStatus.FAILED }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(label = "Passed", value = passed.toString())
                MetricItem(label = "Scheduled", value = scheduled.toString())
                MetricItem(label = "Failed", value = failed.toString())
            }
        }
    }
}

@Composable
private fun PerformanceOverview(performance: List<BuildingPerformance>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Overall Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val avgEnergy = performance.map { it.energyEfficiency.efficiency }.average()
            val avgSafety = performance.map { it.safetyMetrics.safetyScore }.average()
            val avgQuality = performance.map { it.qualityMetrics.qualityScore }.average()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(label = "Avg Energy", value = "${avgEnergy.toInt()}%")
                MetricItem(label = "Avg Safety", value = "${avgSafety.toInt()}%")
                MetricItem(label = "Avg Quality", value = "${avgQuality.toInt()}%")
            }
        }
    }
}