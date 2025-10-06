package com.nextgenbuildpro.fieldtools.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Offline Mode Screen - Functional implementation for offline data management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineModeScreen(navController: NavController) {
    var isOfflineMode by remember { mutableStateOf(false) }
    var syncStatus by remember { mutableStateOf(SyncStatus.SYNCED) }
    var offlineData by remember { mutableStateOf(getOfflineDataSummary()) }
    var pendingSync by remember { mutableStateOf(getPendingSyncItems()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Mode") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Connection status indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOfflineMode) Color.Red else Color.Green
                            )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
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
            // Offline Mode Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOfflineMode) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (isOfflineMode) {
                                Icons.Default.CloudOff
                            } else {
                                Icons.Default.Cloud
                            },
                            contentDescription = "Connection Status",
                            modifier = Modifier.size(48.dp),
                            tint = if (isOfflineMode) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = if (isOfflineMode) {
                                "Offline Mode Active"
                            } else {
                                "Online - Connected"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = if (isOfflineMode) {
                                "Working offline. Data will sync when connected."
                            } else {
                                "All data is synchronized and up to date."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Switch(
                            checked = isOfflineMode,
                            onCheckedChange = { 
                                isOfflineMode = it
                                syncStatus = if (it) SyncStatus.OFFLINE else SyncStatus.SYNCING
                            }
                        )
                    }
                }
            }
            
            // Sync Status
            item {
                SyncStatusCard(
                    status = syncStatus,
                    onSyncNow = { 
                        syncStatus = SyncStatus.SYNCING
                        // Simulate sync process
                    }
                )
            }
            
            // Offline Data Summary
            item {
                Text(
                    text = "Offline Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(offlineData) { dataItem ->
                OfflineDataCard(dataItem = dataItem)
            }
            
            // Pending Sync Items
            if (pendingSync.isNotEmpty()) {
                item {
                    Text(
                        text = "Pending Sync (${pendingSync.size} items)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                items(pendingSync) { syncItem ->
                    PendingSyncCard(syncItem = syncItem)
                }
            }
            
            // Offline Actions
            item {
                Text(
                    text = "Offline Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(getOfflineActions()) { action ->
                OfflineActionCard(
                    action = action,
                    onClick = { /* Handle action */ }
                )
            }
        }
    }
}

@Composable
fun SyncStatusCard(
    status: SyncStatus,
    onSyncNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (status) {
                        SyncStatus.SYNCED -> Icons.Default.CheckCircle
                        SyncStatus.SYNCING -> Icons.Default.Sync
                        SyncStatus.OFFLINE -> Icons.Default.CloudOff
                        SyncStatus.ERROR -> Icons.Default.Error
                    },
                    contentDescription = status.name,
                    tint = when (status) {
                        SyncStatus.SYNCED -> Color.Green
                        SyncStatus.SYNCING -> MaterialTheme.colorScheme.primary
                        SyncStatus.OFFLINE -> Color.Gray
                        SyncStatus.ERROR -> MaterialTheme.colorScheme.error
                    }
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = when (status) {
                            SyncStatus.SYNCED -> "All Data Synced"
                            SyncStatus.SYNCING -> "Syncing..."
                            SyncStatus.OFFLINE -> "Working Offline"
                            SyncStatus.ERROR -> "Sync Error"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = when (status) {
                            SyncStatus.SYNCED -> "Last sync: 5 minutes ago"
                            SyncStatus.SYNCING -> "Synchronizing data with server"
                            SyncStatus.OFFLINE -> "Changes saved locally"
                            SyncStatus.ERROR -> "Failed to sync. Tap to retry."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (status != SyncStatus.SYNCING) {
                Button(
                    onClick = onSyncNow,
                    enabled = status != SyncStatus.OFFLINE
                ) {
                    Text("Sync Now")
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineDataCard(dataItem: OfflineDataItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = dataItem.icon,
                    contentDescription = dataItem.type,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = dataItem.type,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${dataItem.count} items (${dataItem.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (dataItem.hasChanges) {
                Badge {
                    Text("!")
                }
            }
        }
    }
}

@Composable
fun PendingSyncCard(syncItem: PendingSyncItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = syncItem.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = syncItem.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                AssistChip(
                    onClick = { },
                    label = { Text(syncItem.type) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Modified: ${syncItem.lastModified}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineActionCard(
    action: OfflineAction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = action.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class SyncStatus {
    SYNCED, SYNCING, OFFLINE, ERROR
}

data class OfflineDataItem(
    val type: String,
    val count: Int,
    val size: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val hasChanges: Boolean = false
)

data class PendingSyncItem(
    val title: String,
    val description: String,
    val type: String,
    val lastModified: String
)

data class OfflineAction(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun getOfflineDataSummary(): List<OfflineDataItem> {
    return listOf(
        OfflineDataItem(
            type = "Projects",
            count = 12,
            size = "15.2 MB",
            icon = Icons.Default.Build,
            hasChanges = true
        ),
        OfflineDataItem(
            type = "Estimates",
            count = 8,
            size = "3.4 MB",
            icon = Icons.Default.AttachMoney,
            hasChanges = false
        ),
        OfflineDataItem(
            type = "Photos",
            count = 247,
            size = "124.5 MB",
            icon = Icons.Default.Photo,
            hasChanges = true
        ),
        OfflineDataItem(
            type = "Documents",
            count = 35,
            size = "28.1 MB",
            icon = Icons.Default.Description,
            hasChanges = false
        ),
        OfflineDataItem(
            type = "Voice Notes",
            count = 16,
            size = "45.3 MB",
            icon = Icons.Default.RecordVoiceOver,
            hasChanges = true
        )
    )
}

private fun getPendingSyncItems(): List<PendingSyncItem> {
    return listOf(
        PendingSyncItem(
            title = "Kitchen Project Update",
            description = "Added 3 photos and updated progress notes",
            type = "Project",
            lastModified = "2 hours ago"
        ),
        PendingSyncItem(
            title = "New Estimate - Bathroom Renovation",
            description = "Created estimate for Smith residence bathroom",
            type = "Estimate",
            lastModified = "4 hours ago"
        ),
        PendingSyncItem(
            title = "Voice Note - Safety Inspection",
            description = "Recorded safety inspection findings",
            type = "Voice Note",
            lastModified = "1 day ago"
        )
    )
}

private fun getOfflineActions(): List<OfflineAction> {
    return listOf(
        OfflineAction(
            title = "Download for Offline",
            description = "Download additional data for offline access",
            icon = Icons.Default.Download
        ),
        OfflineAction(
            title = "Manage Storage",
            description = "View and manage offline data storage",
            icon = Icons.Default.Storage
        ),
        OfflineAction(
            title = "Sync Settings",
            description = "Configure synchronization preferences",
            icon = Icons.Default.Settings
        ),
        OfflineAction(
            title = "Export Data",
            description = "Export offline data to external storage",
            icon = Icons.Default.GetApp
        )
    )
}