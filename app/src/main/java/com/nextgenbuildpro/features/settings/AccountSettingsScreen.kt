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

@Composable
private fun SettingItem(
    setting: AccountSetting,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
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
                    text = setting.subtitle,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserInfoDialog(
    userName: String,
    userEmail: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(userName) }
    var email by remember { mutableStateOf(userEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, email) }) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompanyInfoDialog(
    companyName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var company by remember { mutableStateOf(companyName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Company Info") },
        text = {
            OutlinedTextField(
                value = company,
                onValueChange = { company = it },
                label = { Text("Company Name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(company) }) {
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

/**
 * Data class for account settings
 */
data class AccountSetting(
    val key: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

/**
 * Get profile settings
 */
private fun getProfileSettings(userName: String, userEmail: String, companyName: String): List<AccountSetting> {
    return listOf(
        AccountSetting(
            key = "user_info",
            icon = Icons.Default.Person,
            title = userName,
            subtitle = userEmail
        ),
        AccountSetting(
            key = "company_info",
            icon = Icons.Default.Business,
            title = companyName,
            subtitle = "Company information"
        )
    )
}

/**
 * Get account actions
 */
private fun getAccountActions(): List<AccountSetting> {
    return listOf(
        AccountSetting(
            key = "change_password",
            icon = Icons.Default.Lock,
            title = "Change Password",
            subtitle = "Update your password"
        ),
        AccountSetting(
            key = "sync_data",
            icon = Icons.Default.Sync,
            title = "Sync Data",
            subtitle = "Synchronize with cloud storage"
        ),
        AccountSetting(
            key = "export_data",
            icon = Icons.Default.Download,
            title = "Export Data",
            subtitle = "Download your data"
        ),
        AccountSetting(
            key = "delete_account",
            icon = Icons.Default.DeleteForever,
            title = "Delete Account",
            subtitle = "Permanently delete your account"
        )
    )
}

/**
 * Get privacy settings
 */
private fun getPrivacySettings(): List<AccountSetting> {
    return listOf(
        AccountSetting(
            key = "two_factor",
            icon = Icons.Default.Security,
            title = "Two-Factor Authentication",
            subtitle = "Add extra security to your account"
        ),
        AccountSetting(
            key = "data_privacy",
            icon = Icons.Default.PrivacyTip,
            title = "Data Privacy",
            subtitle = "Manage your privacy preferences"
        ),
        AccountSetting(
            key = "backup_settings",
            icon = Icons.Default.Backup,
            title = "Backup Settings",
            subtitle = "Configure automatic backups"
        )
    )
}