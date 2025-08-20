package com.nextgenbuildpro.features.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.nextgenbuildpro.navigation.NavDestinations
import com.nextgenbuildpro.navigation.navigateSafely

/**
 * More screen that provides access to additional features and settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More Options") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Settings items
            items(getSettingsItems()) { item ->
                SettingsItem(
                    icon = item.icon,
                    title = item.title,
                    description = item.description,
                    onClick = {
                        navController.navigateSafely(item.destination)
                    }
                )
            }
            
            // Header
            item {
                Text(
                    text = "Tools",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            
            // Tools items
            items(getToolsItems()) { item ->
                SettingsItem(
                    icon = item.icon,
                    title = item.title,
                    description = item.description,
                    onClick = {
                        navController.navigateSafely(item.destination)
                    }
                )
            }
            
            // Header
            item {
                Text(
                    text = "AI Features",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            
            // AI features items
            items(getAIItems()) { item ->
                SettingsItem(
                    icon = item.icon,
                    title = item.title,
                    description = item.description,
                    onClick = {
                        navController.navigateSafely(item.destination)
                    }
                )
            }
        }
    }
}

/**
 * Settings item component
 */
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
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

/**
 * Data class for settings items
 */
data class SettingsItemData(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val destination: String
)

/**
 * Get settings items
 */
fun getSettingsItems(): List<SettingsItemData> {
    return listOf(
        SettingsItemData(
            icon = Icons.Default.AccountCircle,
            title = "Account Settings",
            description = "Manage your account information",
            destination = NavDestinations.ACCOUNT_SETTINGS
        ),
        SettingsItemData(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            description = "Configure notification preferences",
            destination = NavDestinations.NOTIFICATIONS
        ),
        SettingsItemData(
            icon = Icons.Default.Security,
            title = "Permissions",
            description = "Manage app permissions",
            destination = NavDestinations.PERMISSIONS
        )
    )
}

/**
 * Get tools items
 */
fun getToolsItems(): List<SettingsItemData> {
    return listOf(
        SettingsItemData(
            icon = Icons.Default.CloudUpload,
            title = "File Upload",
            description = "Upload and manage files",
            destination = NavDestinations.FILE_UPLOAD
        ),
        SettingsItemData(
            icon = Icons.Default.AccessTime,
            title = "Time Clock",
            description = "Track work hours",
            destination = NavDestinations.TIME_CLOCK
        ),
        SettingsItemData(
            icon = Icons.Default.Mic,
            title = "Voice to Text",
            description = "Convert speech to text",
            destination = NavDestinations.VOICE_TO_TEXT
        ),
        SettingsItemData(
            icon = Icons.Default.ViewInAr,
            title = "AR Visualization",
            description = "Visualize projects in augmented reality",
            destination = NavDestinations.AR_VISUALIZATION
        )
    )
}

/**
 * Get AI features items
 */
fun getAIItems(): List<SettingsItemData> {
    return listOf(
        SettingsItemData(
            icon = Icons.Default.Phone,
            title = "AI Receptionist",
            description = "Configure call handling and voice settings",
            destination = NavDestinations.AI_RECEPTIONIST_SETTINGS
        ),
        SettingsItemData(
            icon = Icons.Default.Forum,
            title = "Client Portal",
            description = "Manage client communication portal",
            destination = NavDestinations.CLIENT_PORTAL
        ),
        SettingsItemData(
            icon = Icons.Default.Assessment,
            title = "Progress Updates",
            description = "Configure automated progress updates",
            destination = NavDestinations.PROGRESS_UPDATES
        )
    )
}