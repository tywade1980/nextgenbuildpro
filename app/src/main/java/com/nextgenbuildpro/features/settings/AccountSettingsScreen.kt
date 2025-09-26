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
 * Account Settings Screen - Allows users to manage their account information and preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(navController: NavController) {
    var userName by remember { mutableStateOf("Tyler Wade") }
    var userEmail by remember { mutableStateOf("tyler.wade@nextgenbuildpro.com") }
    var companyName by remember { mutableStateOf("NextGen BuildPro") }
    var showUserDialog by remember { mutableStateOf(false) }
    var showCompanyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings") },
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
            // Profile Section
            item {
                Text(
                    text = "Profile Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(getProfileSettings(userName, userEmail, companyName)) { setting ->
                SettingItem(
                    setting = setting,
                    onClick = {
                        when (setting.key) {
                            "user_info" -> showUserDialog = true
                            "company_info" -> showCompanyDialog = true
                        }
                    }
                )
            }

            // Account Actions Section
            item {
                Text(
                    text = "Account Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(getAccountActions()) { setting ->
                SettingItem(
                    setting = setting,
                    onClick = {
                        when (setting.key) {
                            "change_password" -> {
                                // Handle password change
                            }
                            "sync_data" -> {
                                // Handle data sync
                            }
                            "export_data" -> {
                                // Handle data export
                            }
                            "delete_account" -> {
                                // Handle account deletion
                            }
                        }
                    }
                )
            }

            // Privacy & Security Section
            item {
                Text(
                    text = "Privacy & Security",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(getPrivacySettings()) { setting ->
                SettingItem(
                    setting = setting,
                    onClick = {
                        when (setting.key) {
                            "two_factor" -> {
                                // Handle 2FA setup
                            }
                            "data_privacy" -> {
                                // Handle privacy settings
                            }
                            "backup_settings" -> {
                                // Handle backup configuration
                            }
                        }
                    }
                )
            }
        }
    }

    // User Info Dialog
    if (showUserDialog) {
        UserInfoDialog(
            userName = userName,
            userEmail = userEmail,
            onDismiss = { showUserDialog = false },
            onSave = { name, email ->
                userName = name
                userEmail = email
                showUserDialog = false
            }
        )
    }

    // Company Info Dialog
    if (showCompanyDialog) {
        CompanyInfoDialog(
            companyName = companyName,
            onDismiss = { showCompanyDialog = false },
            onSave = { company ->
                companyName = company
                showCompanyDialog = false
            }
        )
    }
}

/**
 * Data class for settings items
 */
data class SettingItem(
    val key: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

/**
 * Get profile settings for display
 */
private fun getProfileSettings(userName: String, userEmail: String, companyName: String): List<SettingItem> {
    return listOf(
        SettingItem(
            key = "user_info",
            title = userName,
            description = userEmail,
            icon = Icons.Default.Person
        ),
        SettingItem(
            key = "company_info",
            title = companyName,
            description = "Company Information",
            icon = Icons.Default.Business
        )
    )
}

/**
 * Get account action settings for display
 */
private fun getAccountActions(): List<SettingItem> {
    return listOf(
        SettingItem(
            key = "change_password",
            title = "Change Password",
            description = "Update your security credentials",
            icon = Icons.Default.Lock
        ),
        SettingItem(
            key = "sync_data",
            title = "Sync Data",
            description = "Synchronize with cloud storage",
            icon = Icons.Default.Sync
        ),
        SettingItem(
            key = "export_data",
            title = "Export Data",
            description = "Download your data as CSV",
            icon = Icons.Default.Download
        ),
        SettingItem(
            key = "delete_account",
            title = "Delete Account",
            description = "Remove your account and data",
            icon = Icons.Default.Delete
        )
    )
}

/**
 * Get privacy and security settings for display
 */
private fun getPrivacySettings(): List<SettingItem> {
    return listOf(
        SettingItem(
            key = "two_factor",
            title = "Two-Factor Authentication",
            description = "Add an extra layer of security",
            icon = Icons.Default.Security
        ),
        SettingItem(
            key = "data_privacy",
            title = "Data Privacy",
            description = "Manage your data sharing preferences",
            icon = Icons.Default.PrivacyTip
        ),
        SettingItem(
            key = "backup_settings",
            title = "Backup Settings",
            description = "Configure automatic backups",
            icon = Icons.Default.Backup
        )
    )
}

@Composable
fun SettingItem(setting: SettingItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = setting.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = setting.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = setting.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

// Dialog for editing user information
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoDialog(
    userName: String,
    userEmail: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var nameInput by remember { mutableStateOf(userName) }
    var emailInput by remember { mutableStateOf(userEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(nameInput, emailInput)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog for editing company information
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyInfoDialog(
    companyName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var companyNameInput by remember { mutableStateOf(companyName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Company") },
        text = {
            OutlinedTextField(
                value = companyNameInput,
                onValueChange = { companyNameInput = it },
                label = { Text("Company Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(companyNameInput)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
