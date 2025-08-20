package com.nextgenbuildpro.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nextgenbuildpro.CrmAgent
import com.nextgenbuildpro.LocationService
import com.nextgenbuildpro.PermissionManager
import com.nextgenbuildpro.navigation.NavDestinations
import com.nextgenbuildpro.navigation.NavGraph
import com.nextgenbuildpro.navigation.navigateSafely
import com.nextgenbuildpro.navigation.rememberNavigationHelper

/**
 * Main composable for the NextGenBuildPro application.
 * This is the entry point for the UI and sets up the navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextGenBuildProApp(
    locationService: LocationService,
    permissionManager: PermissionManager,
    crmAgent: CrmAgent
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Remember navigation helper
    rememberNavigationHelper()

    // Define bottom navigation items
    val bottomNavItems = listOf(
        BottomNavItem(
            title = "Home",
            icon = Icons.Default.Home,
            route = NavDestinations.HOME
        ),
        BottomNavItem(
            title = "Leads",
            icon = Icons.Default.Person,
            route = NavDestinations.LEADS
        ),
        BottomNavItem(
            title = "Create",
            icon = Icons.Default.Add,
            route = "create" // This is a special case, handled in onBottomNavItemClick
        ),
        BottomNavItem(
            title = "Projects",
            icon = Icons.Default.Build,
            route = NavDestinations.PROJECTS
        ),
        BottomNavItem(
            title = "More",
            icon = Icons.Default.Menu,
            route = NavDestinations.MORE
        )
    )

    // Show bottom navigation on main screens only
    val showBottomNav = remember(currentDestination) {
        currentDestination?.route in listOf(
            NavDestinations.HOME,
            NavDestinations.LEADS,
            NavDestinations.PROJECTS,
            NavDestinations.MORE
        )
    }

    // Create dialog state for the "Create" menu
    var showCreateMenu by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { 
                            it.route == item.route 
                        } ?: false

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                if (item.route == "create") {
                                    // Show create menu dialog
                                    showCreateMenu = true
                                } else {
                                    // Use safe navigation
                                    val success = navController.navigateSafely(item.route)

                                    if (success) {
                                        // If navigation was successful, configure the back stack
                                        navController.popBackStack(
                                            navController.graph.findStartDestination().id,
                                            false
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main navigation host
            NavGraph(navController = navController)

            // Create menu dialog
            if (showCreateMenu) {
                CreateMenuDialog(
                    onDismiss = { showCreateMenu = false },
                    onOptionSelected = { route ->
                        navController.navigateSafely(route)
                        showCreateMenu = false
                    }
                )
            }
        }
    }
}

/**
 * Dialog for the "Create" menu options.
 */
@Composable
fun CreateMenuDialog(
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New") },
        text = {
            Column {
                // Lead
                ListItem(
                    headlineContent = { Text("Lead") },
                    leadingContent = { Icon(Icons.Default.Person, contentDescription = "Lead") },
                    modifier = Modifier.clickable { onOptionSelected(NavDestinations.LEAD_EDITOR) }
                )

                // Estimate
                ListItem(
                    headlineContent = { Text("Estimate") },
                    leadingContent = { Icon(Icons.Default.Description, contentDescription = "Estimate") },
                    modifier = Modifier.clickable { onOptionSelected(NavDestinations.ESTIMATE_EDITOR) }
                )

                // Photo
                ListItem(
                    headlineContent = { Text("Photo") },
                    leadingContent = { Icon(Icons.Default.PhotoCamera, contentDescription = "Photo") },
                    modifier = Modifier.clickable { onOptionSelected(NavDestinations.CAMERA) }
                )

                // Room Scan
                ListItem(
                    headlineContent = { Text("Room Scan") },
                    leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = "Room Scan") },
                    modifier = Modifier.clickable { onOptionSelected(NavDestinations.ROOM_SCAN) }
                )

                // AR Visualization
                ListItem(
                    headlineContent = { Text("AR Visualization") },
                    leadingContent = { Icon(Icons.Default.ViewInAr, contentDescription = "AR Visualization") },
                    modifier = Modifier.clickable { onOptionSelected(NavDestinations.AR_VISUALIZATION) }
                )

                // Voice to Text
                ListItem(
                    headlineContent = { Text("Voice to Text") },
                    leadingContent = { Icon(Icons.Default.Mic, contentDescription = "Voice to Text") },
                    modifier = Modifier.clickable { onOptionSelected(NavDestinations.VOICE_TO_TEXT) }
                )

                // Message
                ListItem(
                    headlineContent = { Text("Message") },
                    leadingContent = { Icon(Icons.Default.Message, contentDescription = "Message") },
                    modifier = Modifier.clickable { onOptionSelected(NavDestinations.MESSAGES) }
                )

                // Note
                ListItem(
                    headlineContent = { Text("Note") },
                    leadingContent = { Icon(Icons.Default.Note, contentDescription = "Note") },
                    modifier = Modifier.clickable { onOptionSelected(NavDestinations.NOTE_EDITOR) }
                )

                // Client Portal
                ListItem(
                    headlineContent = { Text("Client Portal") },
                    leadingContent = { Icon(Icons.Default.Person, contentDescription = "Client Portal") },
                    modifier = Modifier.clickable { onOptionSelected(NavDestinations.CLIENT_PORTAL) }
                )

                // Progress Updates
                ListItem(
                    headlineContent = { Text("Progress Updates") },
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = "Progress Updates") },
                    modifier = Modifier.clickable { onOptionSelected(NavDestinations.PROGRESS_UPDATES) }
                )

                // Digital Signature
                ListItem(
                    headlineContent = { Text("Digital Signature") },
                    leadingContent = { Icon(Icons.Default.Draw, contentDescription = "Digital Signature") },
                    modifier = Modifier.clickable { onOptionSelected(NavDestinations.DIGITAL_SIGNATURE) }
                )

                // File
                ListItem(
                    headlineContent = { Text("File") },
                    leadingContent = { Icon(Icons.Default.Upload, contentDescription = "File") },
                    modifier = Modifier.clickable { onOptionSelected(NavDestinations.FILE_UPLOAD) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Removed CreateMenuItem function as we're using ListItem directly in the dialog

/**
 * Data class for bottom navigation items.
 */
data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)
