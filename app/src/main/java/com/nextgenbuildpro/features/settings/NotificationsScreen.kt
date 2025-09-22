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
 * Notifications Management Screen - Allows users to configure notification preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    var notificationSettings by remember { mutableStateOf(getInitialNotificationSettings()) }
    var globalNotificationsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
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
                        text = "Notification Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Configure when and how you receive notifications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Global notifications toggle
            item {
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
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Notifications",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Turn all notifications on or off",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = globalNotificationsEnabled,
                            onCheckedChange = { globalNotificationsEnabled = it }
                        )
                    }
                }
            }

            // Project Notifications
            item {
                Text(
                    text = "Project Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(notificationSettings.filter { it.category == NotificationCategory.PROJECT }) { setting ->
                NotificationItem(
                    setting = setting,
                    isGlobalEnabled = globalNotificationsEnabled,
                    onToggle = { isEnabled ->
                        notificationSettings = notificationSettings.map {
                            if (it.key == setting.key) it.copy(isEnabled = isEnabled) else it
                        }
                    }
                )
            }

            // Communication Notifications
            item {
                Text(
                    text = "Communication",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(notificationSettings.filter { it.category == NotificationCategory.COMMUNICATION }) { setting ->
                NotificationItem(
                    setting = setting,
                    isGlobalEnabled = globalNotificationsEnabled,
                    onToggle = { isEnabled ->
                        notificationSettings = notificationSettings.map {
                            if (it.key == setting.key) it.copy(isEnabled = isEnabled) else it
                        }
                    }
                )
            }

            // System Notifications
            item {
                Text(
                    text = "System",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(notificationSettings.filter { it.category == NotificationCategory.SYSTEM }) { setting ->
                NotificationItem(
                    setting = setting,
                    isGlobalEnabled = globalNotificationsEnabled,
                    onToggle = { isEnabled ->
                        notificationSettings = notificationSettings.map {
                            if (it.key == setting.key) it.copy(isEnabled = isEnabled) else it
                        }
                    }
                )
            }

            // Notification Schedule
            item {
                Text(
                    text = "Notification Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                NotificationScheduleCard()
            }
        }
    }
}

@Composable
private fun NotificationItem(
    setting: NotificationSetting,
    isGlobalEnabled: Boolean,
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
                imageVector = setting.icon,
                contentDescription = null,
                tint = if (setting.isEnabled && isGlobalEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                onCheckedChange = onToggle,
                enabled = isGlobalEnabled
            )
        }
    }
}

@Composable
private fun NotificationScheduleCard() {
    var quietHoursEnabled by remember { mutableStateOf(true) }
    var startTime by remember { mutableStateOf("22:00") }
    var endTime by remember { mutableStateOf("08:00") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Quiet Hours",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Silence notifications during specified hours",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = quietHoursEnabled,
                    onCheckedChange = { quietHoursEnabled = it }
                )
            }

            if (quietHoursEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start Time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = startTime,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "End Time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = endTime,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Data classes for notification settings
 */
data class NotificationSetting(
    val key: String,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val category: NotificationCategory,
    val isEnabled: Boolean
)

enum class NotificationCategory {
    PROJECT,
    COMMUNICATION,
    SYSTEM
}

/**
 * Get initial notification settings
 */
private fun getInitialNotificationSettings(): List<NotificationSetting> {
    return listOf(
        // Project Notifications
        NotificationSetting(
            key = "project_updates",
            icon = Icons.Default.Assignment,
            title = "Project Updates",
            description = "Status changes, milestone completions",
            category = NotificationCategory.PROJECT,
            isEnabled = true
        ),
        NotificationSetting(
            key = "deadlines",
            icon = Icons.Default.Schedule,
            title = "Deadlines",
            description = "Upcoming project deadlines and reminders",
            category = NotificationCategory.PROJECT,
            isEnabled = true
        ),
        NotificationSetting(
            key = "estimates",
            icon = Icons.Default.AttachMoney,
            title = "Estimate Updates",
            description = "New estimates, approvals, and changes",
            category = NotificationCategory.PROJECT,
            isEnabled = true
        ),
        NotificationSetting(
            key = "inspections",
            icon = Icons.Default.CheckCircle,
            title = "Inspections",
            description = "Scheduled inspections and results",
            category = NotificationCategory.PROJECT,
            isEnabled = true
        ),

        // Communication Notifications
        NotificationSetting(
            key = "messages",
            icon = Icons.Default.Message,
            title = "Messages",
            description = "New messages from clients and team members",
            category = NotificationCategory.COMMUNICATION,
            isEnabled = true
        ),
        NotificationSetting(
            key = "calls",
            icon = Icons.Default.Phone,
            title = "Missed Calls",
            description = "Notifications for missed calls",
            category = NotificationCategory.COMMUNICATION,
            isEnabled = true
        ),
        NotificationSetting(
            key = "appointments",
            icon = Icons.Default.CalendarToday,
            title = "Appointments",
            description = "Meeting reminders and calendar events",
            category = NotificationCategory.COMMUNICATION,
            isEnabled = true
        ),

        // System Notifications
        NotificationSetting(
            key = "data_sync",
            icon = Icons.Default.Sync,
            title = "Data Sync",
            description = "Cloud synchronization status",
            category = NotificationCategory.SYSTEM,
            isEnabled = false
        ),
        NotificationSetting(
            key = "backups",
            icon = Icons.Default.Backup,
            title = "Backups",
            description = "Backup completion and failures",
            category = NotificationCategory.SYSTEM,
            isEnabled = true
        ),
        NotificationSetting(
            key = "updates",
            icon = Icons.Default.SystemUpdate,
            title = "App Updates",
            description = "Available app updates and features",
            category = NotificationCategory.SYSTEM,
            isEnabled = true
        ),
        NotificationSetting(
            key = "security",
            icon = Icons.Default.Security,
            title = "Security Alerts",
            description = "Security-related notifications",
            category = NotificationCategory.SYSTEM,
            isEnabled = true
        )
    )
}