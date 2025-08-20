package com.nextgenbuildpro.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nextgenbuildpro.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.nextgenbuildpro.crm.data.model.Lead
import com.nextgenbuildpro.crm.rememberCrmComponents
import com.nextgenbuildpro.navigation.navigateSafely
import com.nextgenbuildpro.navigation.navigateSafelyWithArgs
import com.nextgenbuildpro.navigation.rememberNavigationHelper
import com.nextgenbuildpro.navigation.NavDestinations
import com.nextgenbuildpro.pm.data.model.Estimate
import com.nextgenbuildpro.pm.rememberPmComponents
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            HomeTopBar(navController)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            CrmPathSection(navController)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            ProjectManagementPathSection(navController)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            LeadsSection(navController)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            EstimatesSection(navController)
        }
    }
}

@Composable
fun HomeTopBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "11:28",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        Row {
            // Weather icon placeholder
            Text(
                text = "☁️ ☕",
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Battery, signal icons placeholder
            Text(
                text = "🔋 📶",
                fontSize = 18.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Search functionality
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                showSearchResults = it.isNotEmpty()
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_leads)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                Row {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { 
                                searchQuery = ""
                                showSearchResults = false
                            }
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { navController.navigateSafely(NavDestinations.NOTIFICATIONS) }
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = { navController.navigate("messages") }
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Messages",
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        // Search results dropdown
        if (showSearchResults) {
            val crmComponents = rememberCrmComponents()
            val leadsViewModel = crmComponents.leadsViewModel
            val leads = leadsViewModel.items.value

            // Filter leads based on search query
            val filteredLeads = leads.filter { lead ->
                lead.name.contains(searchQuery, ignoreCase = true) ||
                (lead.email?.contains(searchQuery, ignoreCase = true) ?: false) ||
                lead.phone.contains(searchQuery, ignoreCase = true)
            }.take(5) // Limit to 5 results

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (filteredLeads.isEmpty()) {
                        Text(
                            text = "No results found",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        // Display filtered leads
                        filteredLeads.forEach { lead ->
                            SearchResultItem(
                                title = lead.name,
                                subtitle = "Lead",
                                onClick = {
                                    navController.navigateSafelyWithArgs(NavDestinations.LEAD_DETAIL, lead.id)
                                    showSearchResults = false
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CrmPathSection(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CRM",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = ">",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigateSafely("leads") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionItem(
                    icon = Icons.Default.Person,
                    label = "New Lead",
                    onClick = { navController.navigateSafely("lead_editor") }
                )

                ActionItem(
                    icon = Icons.Default.Email,
                    label = "Message",
                    onClick = { navController.navigateSafely("messages") }
                )

                ActionItem(
                    icon = Icons.Default.Call,
                    label = "Call",
                    onClick = { navController.navigateSafely(NavDestinations.LEAD_DETAIL + "/1") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionItem(
                    icon = Icons.Default.Schedule,
                    label = "Follow-up",
                    onClick = { navController.navigateSafely(NavDestinations.LEAD_DETAIL + "/2") }
                )

                ActionItem(
                    icon = Icons.Default.Note,
                    label = "Note",
                    onClick = { navController.navigateSafely("note_editor") }
                )

                ActionItem(
                    icon = Icons.Default.Upload,
                    label = "File",
                    onClick = { navController.navigateSafely("file_upload") }
                )
            }
        }
    }
}

@Composable
fun ProjectManagementPathSection(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Project Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = ">",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigateSafely("projects") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionItem(
                    icon = Icons.Default.AttachMoney,
                    label = "Estimate",
                    onClick = { navController.navigateSafely("estimate_editor") }
                )

                ActionItem(
                    icon = Icons.Default.Build,
                    label = "Project",
                    onClick = { navController.navigateSafely("projects") }
                )

                ActionItem(
                    icon = Icons.Default.PhotoCamera,
                    label = "Photo",
                    onClick = { navController.navigateSafely("camera") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionItem(
                    icon = Icons.Default.CameraAlt,
                    label = "Room Scan",
                    onClick = { navController.navigateSafely("room_scan") }
                )

                ActionItem(
                    icon = Icons.Default.CalendarToday,
                    label = "Schedule",
                    onClick = { navController.navigateSafely(NavDestinations.CALENDAR) }
                )

                ActionItem(
                    icon = Icons.Default.Assignment,
                    label = "Tasks",
                    onClick = { navController.navigateSafely("tasks") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                ActionItem(
                    icon = Icons.Default.Timer,
                    label = "Time Clock",
                    onClick = { navController.navigateSafely(NavDestinations.TIME_CLOCK) }
                )
            }
        }
    }
}

@Composable
fun CreateNewSection(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.create_new_section),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = ">",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionItem(
                    icon = Icons.Default.PhotoCamera,
                    label = stringResource(R.string.take_photo),
                    onClick = { 
                        // Open camera to take a photo
                        navController.navigate("camera")
                    }
                )

                ActionItem(
                    icon = Icons.Default.CameraAlt, // Using CameraAlt instead of ViewInAr
                    label = stringResource(R.string.room_scan),
                    onClick = { 
                        // Open room scanning feature
                        navController.navigate("room_scan")
                    }
                )

                ActionItem(
                    icon = Icons.Default.Email,
                    label = stringResource(R.string.message),
                    onClick = { 
                        // Open messaging feature
                        navController.navigate("messages")
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionItem(
                    icon = Icons.Default.AttachMoney, // Using AttachMoney instead of Calculate
                    label = stringResource(R.string.estimate),
                    onClick = { 
                        // Navigate to create new estimate screen
                        navController.navigate("estimate_editor")
                    }
                )

                ActionItem(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.lead),
                    onClick = { navController.navigate("leads") }
                )

                ActionItem(
                    icon = Icons.Default.Upload,
                    label = stringResource(R.string.file),
                    onClick = { 
                        // Open file upload dialog
                        navController.navigate("file_upload")
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                ActionItem(
                    icon = Icons.Default.Note,
                    label = stringResource(R.string.note),
                    onClick = { 
                        // Navigate to note editor
                        navController.navigate("note_editor")
                    }
                )
            }
        }
    }
}

@Composable
fun ActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier
                .size(40.dp)
                .padding(bottom = 4.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LeadsSection(navController: NavController) {
    // Get the CRM components to access the LeadsViewModel
    val crmComponents = rememberCrmComponents()
    val leadsViewModel = crmComponents.leadsViewModel

    // Collect the leads from the ViewModel
    val leads = leadsViewModel.items.value

    // Refresh leads when the component is first displayed
    LaunchedEffect(Unit) {
        leadsViewModel.refresh()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.leads_section),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = ">",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate("leads") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (leads.isEmpty()) {
                Text(
                    text = "No leads available. Create a new lead to get started.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                // Display up to 2 leads for the dashboard
                leads.take(2).forEach { lead ->
                    // Get initials from name
                    val initials = lead.name.split(" ")
                        .filter { it.isNotEmpty() }
                        .take(2)
                        .joinToString("") { it.first().toString() }
                        .uppercase()

                    // Determine color based on lead name hash
                    val colorResId = when (Math.abs(lead.name.hashCode()) % 3) {
                        0 -> R.color.avatar_pink
                        1 -> R.color.avatar_brown
                        else -> R.color.avatar_yellow
                    }

                    LeadItem(
                        initials = initials,
                        color = colorResource(colorResId),
                        title = if (lead.projectType.isNotEmpty()) "${lead.name} ${lead.projectType}" else lead.name,
                        subtitle = lead.name,
                        onClick = { navController.navigateSafelyWithArgs(NavDestinations.LEAD_DETAIL, lead.id) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun LeadItem(
    initials: String,
    color: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EstimatesSection(navController: NavController) {
    // Get the PM components to access the EstimatesViewModel
    val pmComponents = rememberPmComponents()
    val estimatesViewModel = pmComponents.estimatesViewModel

    // Collect the estimates from the ViewModel
    val estimates = estimatesViewModel.items.value

    // Refresh estimates when the component is first displayed
    LaunchedEffect(Unit) {
        estimatesViewModel.refresh()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.estimates_section),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = ">",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate("estimates") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (estimates.isEmpty()) {
                Text(
                    text = "No estimates available. Create a new estimate to get started.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                // Display up to 2 estimates for the dashboard
                estimates.take(2).forEach { estimate ->
                    // Format the amount with currency symbol
                    val formattedAmount = "$${String.format("%,.2f", estimate.amount)}"

                    EstimateItem(
                        title = estimate.title,
                        amount = formattedAmount,
                        status = estimate.status,
                        onClick = { navController.navigateSafelyWithArgs(NavDestinations.ESTIMATE_DETAIL, estimate.id) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun EstimateItem(
    title: String,
    amount: String,
    status: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = when(status) {
                        "Draft" -> Color(0xFFE0E0E0)
                        "Sent" -> Color(0xFFD1E3FF)
                        "Approved" -> Color(0xFFD1FFDA)
                        else -> Color(0xFFE0E0E0)
                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "View Estimate"
        )
    }
}
