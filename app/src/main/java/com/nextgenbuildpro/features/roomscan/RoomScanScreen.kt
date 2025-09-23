package com.nextgenbuildpro.features.roomscan

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

/**
 * Room Scan Screen - 3D room scanning for construction projects
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScanScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Scan", "Models", "Analysis")
    var scans by remember { mutableStateOf(getSampleScans()) }
    var isScanning by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Scanner") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        if (!isScanning) {
                            isScanning = true
                            // Simulate scanning process
                            // In a real app, this would start the 3D scanning
                        }
                    }
                ) {
                    Icon(
                        if (isScanning) Icons.Default.Stop else Icons.Default.CameraAlt,
                        contentDescription = if (isScanning) "Stop Scan" else "Start Scan"
                    )
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
                0 -> ScanTab(
                    isScanning = isScanning,
                    onScanComplete = { scanData ->
                        scans = listOf(scanData) + scans
                        isScanning = false
                    }
                )
                1 -> ModelsTab(
                    scans = scans,
                    onDeleteScan = { scanId ->
                        scans = scans.filter { it.id != scanId }
                    }
                )
                2 -> AnalysisTab(scans = scans)
            }
        }
    }
}

@Composable
private fun ScanTab(
    isScanning: Boolean,
    onScanComplete: (RoomScan) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scanning Status
        item {
            if (isScanning) {
                ScanningStatusCard(onScanComplete = onScanComplete)
            } else {
                ScanInstructionsCard()
            }
        }

        // Scan Settings
        item {
            Text(
                text = "Scan Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        items(getScanSettings()) { setting ->
            ScanSettingCard(setting = setting)
        }

        // Quick Tips
        item {
            Text(
                text = "Scanning Tips",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        items(getScanningTips()) { tip ->
            TipCard(tip = tip)
        }
    }
}

@Composable
private fun ModelsTab(
    scans: List<RoomScan>,
    onDeleteScan: (String) -> Unit
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
                    text = "3D Models",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${scans.size} scanned models available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Scan Models
        items(scans) { scan ->
            ScanModelCard(
                scan = scan,
                onDelete = { onDeleteScan(scan.id) },
                onShare = { /* Handle sharing */ },
                onExport = { /* Handle export */ }
            )
        }
    }
}

@Composable
private fun AnalysisTab(scans: List<RoomScan>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Room Analysis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Analysis Overview
        item {
            AnalysisOverviewCard(scans = scans)
        }

        // Individual Analyses
        items(scans) { scan ->
            ScanAnalysisCard(scan = scan)
        }
    }
}

@Composable
private fun ScanningStatusCard(onScanComplete: (RoomScan) -> Unit) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Simulate scanning progress
        while (progress < 1f) {
            kotlinx.coroutines.delay(100)
            progress += 0.01f
        }
        // Complete scan
        onScanComplete(
            RoomScan(
                id = "scan_${System.currentTimeMillis()}",
                name = "Room Scan ${System.currentTimeMillis() % 1000}",
                roomType = "Living Room",
                dimensions = "12' x 15' x 9'",
                area = "180 sq ft",
                volume = "1,620 cu ft",
                scanDate = "Just now",
                accuracy = "98.5%",
                pointCount = "2.3M points"
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Scanning in Progress...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Move your device slowly around the room",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(progress * 100).toInt()}% Complete",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ScanInstructionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ViewInAr,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Ready to Scan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tap the camera button to start 3D scanning",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScanSettingCard(setting: ScanSetting) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = setting.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = setting.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = setting.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = setting.isEnabled,
                onCheckedChange = { /* Handle setting change */ }
            )
        }
    }
}

@Composable
private fun TipCard(tip: ScanTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = tip.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ScanModelCard(
    scan: RoomScan,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit
) {
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
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = scan.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = scan.roomType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = scan.scanDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Dimensions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = scan.dimensions,
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
                        text = scan.area,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column {
                    Text(
                        text = "Accuracy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = scan.accuracy,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onShare) {
                    Text("Share")
                }
                TextButton(onClick = onExport) {
                    Text("Export")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun AnalysisOverviewCard(scans: List<RoomScan>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Analysis Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Scans",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = scans.size.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Avg Accuracy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "97.8%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Total Area",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "2,450 sq ft",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanAnalysisCard(scan: RoomScan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = scan.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Measurements",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Volume: ${scan.volume}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Points: ${scan.pointCount}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Data classes
 */
data class RoomScan(
    val id: String,
    val name: String,
    val roomType: String,
    val dimensions: String,
    val area: String,
    val volume: String,
    val scanDate: String,
    val accuracy: String,
    val pointCount: String
)

data class ScanSetting(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val isEnabled: Boolean
)

data class ScanTip(
    val icon: ImageVector,
    val title: String,
    val description: String
)

/**
 * Sample data functions
 */
private fun getSampleScans(): List<RoomScan> {
    return listOf(
        RoomScan(
            id = "1",
            name = "Main Living Room",
            roomType = "Living Room",
            dimensions = "18' x 22' x 10'",
            area = "396 sq ft",
            volume = "3,960 cu ft",
            scanDate = "2 days ago",
            accuracy = "98.2%",
            pointCount = "3.1M points"
        ),
        RoomScan(
            id = "2",
            name = "Master Bedroom",
            roomType = "Bedroom",
            dimensions = "14' x 16' x 9'",
            area = "224 sq ft",
            volume = "2,016 cu ft",
            scanDate = "1 week ago",
            accuracy = "97.8%",
            pointCount = "2.5M points"
        ),
        RoomScan(
            id = "3",
            name = "Kitchen Area",
            roomType = "Kitchen",
            dimensions = "12' x 15' x 9'",
            area = "180 sq ft",
            volume = "1,620 cu ft",
            scanDate = "2 weeks ago",
            accuracy = "98.9%",
            pointCount = "2.8M points"
        )
    )
}

private fun getScanSettings(): List<ScanSetting> {
    return listOf(
        ScanSetting(
            icon = Icons.Default.HighQuality,
            title = "High Quality",
            description = "Higher resolution scanning for detailed models",
            isEnabled = true
        ),
        ScanSetting(
            icon = Icons.Default.FlashAuto,
            title = "Auto Flash",
            description = "Automatic flash control for better scanning",
            isEnabled = false
        ),
        ScanSetting(
            icon = Icons.Default.GridOn,
            title = "Guide Grid",
            description = "Show grid overlay to help with scanning",
            isEnabled = true
        ),
        ScanSetting(
            icon = Icons.Default.VolumeUp,
            title = "Audio Feedback",
            description = "Audio cues during scanning process",
            isEnabled = true
        )
    )
}

private fun getScanningTips(): List<ScanTip> {
    return listOf(
        ScanTip(
            icon = Icons.Default.Speed,
            title = "Move Slowly",
            description = "Keep a steady, slow movement for better accuracy"
        ),
        ScanTip(
            icon = Icons.Default.Lightbulb,
            title = "Good Lighting",
            description = "Ensure adequate lighting for optimal scanning"
        ),
        ScanTip(
            icon = Icons.Default.CenterFocusStrong,
            title = "Maintain Distance",
            description = "Keep 3-6 feet from walls and objects"
        ),
        ScanTip(
            icon = Icons.Default.Refresh,
            title = "Multiple Passes",
            description = "Scan the room from different angles for completeness"
        )
    )
}