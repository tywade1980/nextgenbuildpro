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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Permissions Management Screen - Allows users to manage app permissions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(navController: NavController) {
    var permissions by remember { mutableStateOf(getInitialPermissions()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Permissions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // Header
            item {
                Column {
                    Text(
                        text = "Manage Permissions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Control which features can access your device's capabilities",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Essential Permissions
            item {
                Text(
                    text = "Essential Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(permissions.filter { it.category == PermissionCategory.ESSENTIAL }) { permission ->
                PermissionItem(
                    permission = permission,
                    onToggle = { isEnabled ->
                        permissions = permissions.map {
                            if (it.key == permission.key) it.copy(isEnabled = isEnabled) else it
                        }
                    }
                )
            }

            // Optional Permissions
            item {
                Text(
                    text = "Optional Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(permissions.filter { it.category == PermissionCategory.OPTIONAL }) { permission ->
                PermissionItem(
                    permission = permission,
                    onToggle = { isEnabled ->
                        permissions = permissions.map {
                            if (it.key == permission.key) it.copy(isEnabled = isEnabled) else it
                        }
                    }
                )
            }

            // Advanced Permissions
            item {
                Text(
                    text = "Advanced Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(permissions.filter { it.category == PermissionCategory.ADVANCED }) { permission ->
                PermissionItem(
                    permission = permission,
                    onToggle = { isEnabled ->
                        permissions = permissions.map {
                            if (it.key == permission.key) it.copy(isEnabled = isEnabled) else it
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PermissionItem(
    permission: Permission,
    onToggle: (Boolean) -> Unit
) {
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
                imageVector = permission.icon,
                contentDescription = null,
                tint = if (permission.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = permission.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = permission.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (permission.category == PermissionCategory.ESSENTIAL) {
                    Text(
                        text = "Required for core functionality",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Switch(
                checked = permission.isEnabled,
                onCheckedChange = onToggle,
                enabled = permission.category != PermissionCategory.ESSENTIAL
            )
        }
    }
}

/**
 * Data classes for permissions
 */
data class Permission(
    val key: String,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val category: PermissionCategory,
    val isEnabled: Boolean
)

enum class PermissionCategory {
    ESSENTIAL,
    OPTIONAL,
    ADVANCED
}

/**
 * Get initial permissions state
 */
private fun getInitialPermissions(): List<Permission> {
    return listOf(
        // Essential Permissions
        Permission(
            key = "storage",
            icon = Icons.Default.Storage,
            title = "Storage Access",
            description = "Required to save and access project files",
            category = PermissionCategory.ESSENTIAL,
            isEnabled = true
        ),
        Permission(
            key = "network",
            icon = Icons.Default.Wifi,
            title = "Network Access",
            description = "Required for syncing data and cloud services",
            category = PermissionCategory.ESSENTIAL,
            isEnabled = true
        ),
        
        // Optional Permissions
        Permission(
            key = "camera",
            icon = Icons.Default.PhotoCamera,
            title = "Camera Access",
            description = "Take photos for project documentation",
            category = PermissionCategory.OPTIONAL,
            isEnabled = true
        ),
        Permission(
            key = "location",
            icon = Icons.Default.LocationOn,
            title = "Location Access",
            description = "Tag photos and projects with location data",
            category = PermissionCategory.OPTIONAL,
            isEnabled = false
        ),
        Permission(
            key = "microphone",
            icon = Icons.Default.Mic,
            title = "Microphone Access",
            description = "Voice notes and call recording features",
            category = PermissionCategory.OPTIONAL,
            isEnabled = false
        ),
        Permission(
            key = "contacts",
            icon = Icons.Default.Contacts,
            title = "Contacts Access",
            description = "Import client contacts for easier communication",
            category = PermissionCategory.OPTIONAL,
            isEnabled = true
        ),
        Permission(
            key = "calendar",
            icon = Icons.Default.CalendarToday,
            title = "Calendar Access",
            description = "Sync project schedules with your calendar",
            category = PermissionCategory.OPTIONAL,
            isEnabled = true
        ),
        
        // Advanced Permissions
        Permission(
            key = "phone",
            icon = Icons.Default.Phone,
            title = "Phone Access",
            description = "Make calls directly from the app",
            category = PermissionCategory.ADVANCED,
            isEnabled = false
        ),
        Permission(
            key = "sms",
            icon = Icons.Default.Sms,
            title = "SMS Access",
            description = "Send project updates via SMS",
            category = PermissionCategory.ADVANCED,
            isEnabled = false
        ),
        Permission(
            key = "bluetooth",
            icon = Icons.Default.Bluetooth,
            title = "Bluetooth Access",
            description = "Connect to measurement tools and devices",
            category = PermissionCategory.ADVANCED,
            isEnabled = false
        ),
        Permission(
            key = "device_admin",
            icon = Icons.Default.AdminPanelSettings,
            title = "Device Admin",
            description = "Advanced security and management features",
            category = PermissionCategory.ADVANCED,
            isEnabled = false
        )
    )
}