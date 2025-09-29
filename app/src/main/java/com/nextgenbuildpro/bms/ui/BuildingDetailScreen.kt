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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Building Detail Screen - Shows detailed information about a specific building in the BMS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingDetailScreen(navController: NavController, buildingId: String) {
    val building = remember { getBuildingById(buildingId) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Systems", "Sensors", "Maintenance")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(building.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share building info */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* Edit building */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add new system or sensor */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Building Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (building.status) {
                        "Online" -> MaterialTheme.colorScheme.primaryContainer
                        "Maintenance" -> MaterialTheme.colorScheme.tertiaryContainer
                        "Offline" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = building.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = building.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        AssistChip(
                            onClick = { },
                            label = { Text(building.status) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (building.status) {
                                        "Online" -> Icons.Default.CheckCircle
                                        "Maintenance" -> Icons.Default.Build
                                        "Offline" -> Icons.Default.ErrorOutline
                                        else -> Icons.Default.Help
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Key metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BuildingMetric(
                            label = "Systems",
                            value = building.systemsCount.toString(),
                            icon = Icons.Default.Settings
                        )
                        BuildingMetric(
                            label = "Sensors",
                            value = building.sensorsCount.toString(),
                            icon = Icons.Default.Sensors
                        )
                        BuildingMetric(
                            label = "Temperature",
                            value = "${building.currentTemp}°F",
                            icon = Icons.Default.Thermostat
                        )
                        BuildingMetric(
                            label = "Energy",
                            value = "${building.energyUsage} kWh",
                            icon = Icons.Default.ElectricBolt
                        )
                    }
                }
            }
            
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Tab content
            when (selectedTab) {
                0 -> BuildingOverviewTab(building)
                1 -> BuildingSystemsTab(building.systems)
                2 -> BuildingSensorsTab(building.sensors)
                3 -> BuildingMaintenanceTab(building.maintenanceRecords)
            }
        }
    }
}

@Composable
fun BuildingMetric(
    label: String, 
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
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
fun BuildingOverviewTab(building: BuildingDetail) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Building Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    InfoRow("Type", building.buildingType)
                    InfoRow("Size", "${building.squareFootage} sq ft")
                    InfoRow("Floors", building.floors.toString())
                    InfoRow("Built", building.yearBuilt.toString())
                    InfoRow("Owner", building.owner)
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Current Conditions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    InfoRow("Temperature", "${building.currentTemp}°F")
                    InfoRow("Humidity", "${building.currentHumidity}%")
                    InfoRow("Air Quality", building.airQuality)
                    InfoRow("Occupancy", "${building.currentOccupancy}%")
                }
            }
        }
    }
}

@Composable
fun BuildingSystemsTab(systems: List<BuildingSystem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(systems) { system ->
            SystemCard(system = system)
        }
    }
}

@Composable
fun BuildingSensorsTab(sensors: List<BuildingSensor>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sensors) { sensor ->
            SensorCard(sensor = sensor)
        }
    }
}

@Composable
fun BuildingMaintenanceTab(records: List<MaintenanceRecord>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(records) { record ->
            MaintenanceCard(record = record)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SystemCard(system: BuildingSystem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (system.type) {
                    "HVAC" -> Icons.Default.Air
                    "Lighting" -> Icons.Default.Lightbulb
                    "Security" -> Icons.Default.Security
                    "Fire Safety" -> Icons.Default.LocalFireDepartment
                    else -> Icons.Default.Settings
                },
                contentDescription = system.type,
                modifier = Modifier.size(32.dp),
                tint = if (system.status == "Online") {
                    Color.Green
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = system.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = system.type,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                AssistChip(
                    onClick = { },
                    label = { Text(system.status) }
                )
                Text(
                    text = "Last Updated: ${system.lastUpdate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SensorCard(sensor: BuildingSensor) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sensor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${sensor.type} • ${sensor.location}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = sensor.currentValue,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = sensor.unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Range: ${sensor.minValue} - ${sensor.maxValue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Updated: ${sensor.lastUpdate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MaintenanceCard(record: MaintenanceRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (record.priority) {
                "High" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                "Medium" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                AssistChip(
                    onClick = { },
                    label = { Text(record.status) },
                    leadingIcon = {
                        Icon(
                            imageVector = when (record.status) {
                                "Completed" -> Icons.Default.CheckCircle
                                "In Progress" -> Icons.Default.Build
                                "Scheduled" -> Icons.Default.Schedule
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = record.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Priority: ${record.priority}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Due: ${record.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Data classes
data class BuildingDetail(
    val id: String,
    val name: String,
    val address: String,
    val buildingType: String,
    val squareFootage: Int,
    val floors: Int,
    val yearBuilt: Int,
    val owner: String,
    val status: String,
    val systemsCount: Int,
    val sensorsCount: Int,
    val currentTemp: Int,
    val currentHumidity: Int,
    val airQuality: String,
    val currentOccupancy: Int,
    val energyUsage: Double,
    val systems: List<BuildingSystem>,
    val sensors: List<BuildingSensor>,
    val maintenanceRecords: List<MaintenanceRecord>
)

data class BuildingSystem(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val lastUpdate: String
)

data class BuildingSensor(
    val id: String,
    val name: String,
    val type: String,
    val location: String,
    val currentValue: String,
    val unit: String,
    val minValue: String,
    val maxValue: String,
    val lastUpdate: String
)

data class MaintenanceRecord(
    val id: String,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val dueDate: String
)

private fun getBuildingById(buildingId: String): BuildingDetail {
    return BuildingDetail(
        id = buildingId,
        name = "Corporate Headquarters",
        address = "456 Business Ave, Downtown",
        buildingType = "Office Building",
        squareFootage = 125000,
        floors = 15,
        yearBuilt = 2018,
        owner = "NextGen BuildPro",
        status = "Online",
        systemsCount = 12,
        sensorsCount = 47,
        currentTemp = 72,
        currentHumidity = 45,
        airQuality = "Good",
        currentOccupancy = 85,
        energyUsage = 2450.5,
        systems = listOf(
            BuildingSystem("1", "Main HVAC System", "HVAC", "Online", "5 min ago"),
            BuildingSystem("2", "LED Lighting Control", "Lighting", "Online", "2 min ago"),
            BuildingSystem("3", "Access Control System", "Security", "Online", "1 min ago"),
            BuildingSystem("4", "Fire Detection System", "Fire Safety", "Online", "10 min ago"),
            BuildingSystem("5", "Elevator Control", "Transportation", "Maintenance", "2 hours ago")
        ),
        sensors = listOf(
            BuildingSensor("1", "Lobby Temperature", "Temperature", "Floor 1 - Lobby", "72", "°F", "65", "78", "2 min ago"),
            BuildingSensor("2", "Office Humidity", "Humidity", "Floor 5 - Open Office", "45", "%", "30", "60", "5 min ago"),
            BuildingSensor("3", "Conference Room CO2", "Air Quality", "Floor 10 - Conference A", "450", "ppm", "400", "1000", "1 min ago"),
            BuildingSensor("4", "Outdoor Air Quality", "Air Quality", "Rooftop", "Good", "AQI", "0", "300", "10 min ago")
        ),
        maintenanceRecords = listOf(
            MaintenanceRecord("1", "HVAC Filter Replacement", "Replace air filters in main HVAC system", "Medium", "Scheduled", "Dec 15, 2024"),
            MaintenanceRecord("2", "Elevator Inspection", "Annual safety inspection required", "High", "In Progress", "Dec 12, 2024"),
            MaintenanceRecord("3", "LED Bulb Replacement", "Replace failed LED bulbs in parking garage", "Low", "Completed", "Dec 10, 2024"),
            MaintenanceRecord("4", "Fire System Test", "Monthly fire detection system test", "High", "Scheduled", "Dec 20, 2024")
        )
    )
}