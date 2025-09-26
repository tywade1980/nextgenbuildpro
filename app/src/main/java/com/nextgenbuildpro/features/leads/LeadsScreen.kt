package com.nextgenbuildpro.features.leads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.R
import com.nextgenbuildpro.crm.data.model.Lead
import com.nextgenbuildpro.crm.data.model.LeadPhase
import com.nextgenbuildpro.crm.rememberCrmComponents
import com.nextgenbuildpro.crm.viewmodel.LeadsViewModel
import com.nextgenbuildpro.debug.WorkflowAnalyzer
import com.nextgenbuildpro.navigation.NavDestinations
import com.nextgenbuildpro.navigation.navigateSafely
import com.nextgenbuildpro.ui.ButtonNavigationValidator
import com.nextgenbuildpro.ui.FeatureCompletionTracker
import com.nextgenbuildpro.ui.components.completeFeature
import com.nextgenbuildpro.ui.components.trackFeature
import com.nextgenbuildpro.ui.components.trackNavigation

/**
 * Screen for displaying and managing leads
 */
@Composable
fun LeadsScreen(navController: NavController) {
    // Session ID for tracking this user's journey
    val sessionId = remember { "user_${System.currentTimeMillis()}" }

    // Register this screen visit with the WorkflowAnalyzer
    LaunchedEffect(Unit) {
        WorkflowAnalyzer.trackScreenVisit(
            userId = sessionId,
            destination = NavDestinations.LEADS
        )
    }

    // Get the LeadsViewModel from the CRM module
    val crmComponents = rememberCrmComponents()
    val viewModel = crmComponents.leadsViewModel

    // Refresh leads when the screen is displayed
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    // Collect state from the ViewModel
    val leads by viewModel.filteredLeads.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Create status options from LeadPhase enum
    val statusOptions = listOf("All") + LeadPhase.values().map { it.name }

    Scaffold(
        topBar = {
            LeadsTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                navController = navController,
                onRefresh = { viewModel.refresh() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                modifier = Modifier.trackFeature(
                    buttonId = "add_lead_fab",
                    screenName = "LeadsScreen",
                    featureName = "create_lead"
                ) { featureSessionId ->
                    // Track this navigation with the ButtonNavigationValidator
                    ButtonNavigationValidator.validateAndNavigate(
                        navController = navController,
                        destination = NavDestinations.LEAD_EDITOR,
                        buttonId = "add_lead_fab",
                        screenName = "LeadsScreen"
                    )
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Lead")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search leads...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                // Status filter chip group
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(statusOptions) { status ->
                        FilterChip(
                            selected = statusFilter == status,
                            onClick = { viewModel.updateStatusFilter(status) },
                            label = { Text(status) }
                        )
                    }
                }

                // Leads list
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (leads.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isEmpty() && statusFilter == "All") {
                                "No leads available. Create your first lead!"
                            } else {
                                "No leads match your filters."
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(leads) { lead ->
                            LeadListItem(
                                lead = lead,
                                onClick = {
                                    // Track this as part of a workflow for viewing lead details
                                    val featureId = FeatureCompletionTracker.trackFeatureStart(
                                        elementId = "lead_item_${lead.id}",
                                        screenName = "LeadsScreen",
                                        featureName = "view_lead_details",
                                        expectedDestination = NavDestinations.LEAD_DETAIL
                                    )

                                    // Navigate to lead details screen
                                    val success = ButtonNavigationValidator.validateAndNavigate(
                                        navController = navController,
                                        destination = "${NavDestinations.LEAD_DETAIL}/${lead.id}",
                                        buttonId = "lead_item_${lead.id}",
                                        screenName = "LeadsScreen"
                                    )

                                    // Mark the feature as complete or incomplete
                                    if (success) {
                                        completeFeature(featureId, true, "Successfully navigated to lead details")
                                    } else {
                                        completeFeature(featureId, false, "Failed to navigate to lead details")
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeadListItem(lead: Lead, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar or initials circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(getColorForLead(lead), shape = androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = lead.name.take(1).uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Lead information
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = lead.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = lead.phone ?: "No phone",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // Lead status
        AssistChip(
            onClick = {},
            label = { Text(text = lead.phase.name, style = MaterialTheme.typography.bodySmall) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = getStatusColor(lead.phase.name)
            )
        )

        // Navigate chevron
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "View Lead Details",
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * Card for displaying a lead
 */
@Composable
fun LeadCard(
    lead: Lead,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name and project type
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lead.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = lead.projectType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                // Status chip
                Surface(
                    color = getColorForLeadPhase(lead.phase).copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = lead.phase.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = getColorForLeadPhase(lead.phase),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contact info and urgency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Urgency: ${lead.urgency}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Text(
                        text = "Intake: ${java.text.SimpleDateFormat("MM/dd/yyyy").format(java.util.Date(lead.intakeTimestamp))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Action buttons
                Row {
                    IconButton(onClick = { /* Call lead */ }) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { /* Message lead */ }) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Scrollable filter chips for selecting status
 */
@Composable
fun ScrollableFilterChips(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            Surface(
                color = if (selectedOption == option) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .height(32.dp)
                    .clickable { onOptionSelected(option) }
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedOption == option)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Error message with retry button
 */
@Composable
fun ErrorMessage(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Red
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Error",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Retry")
        }
    }
}

/**
 * Top bar for leads screen
 */
@Composable
fun LeadsTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    navController: NavController,
    onRefresh: () -> Unit
) {
    Column {
        // Custom app bar
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Leads",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { navController.navigateSafely(NavDestinations.NOTIFICATIONS) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search leads...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
    }
}

/**
 * Empty leads view
 */
@Composable
fun EmptyLeadsView(isFiltered: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isFiltered) Icons.Default.FilterList else Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isFiltered) "No leads match your search" else "No leads yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isFiltered) 
                "Try adjusting your search or filters" 
            else 
                "Create your first lead by clicking the + button",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Get color for lead phase
 */
fun getColorForLeadPhase(phase: LeadPhase): Color {
    return when (phase) {
        LeadPhase.CONTACTED -> Color(0xFF607D8B)    // Blue Grey
        LeadPhase.QUALIFIED -> Color(0xFF2196F3)     // Blue
        LeadPhase.DISCOVERY -> Color(0xFFFF9800)     // Orange
        LeadPhase.SCOPED -> Color(0xFF9C27B0)        // Purple
        LeadPhase.ESTIMATING -> Color(0xFFFFEB3B)    // Yellow
        LeadPhase.DELIVERED -> Color(0xFF4CAF50)     // Green
        LeadPhase.CLOSED_WON -> Color(0xFF4CAF50)    // Green
        LeadPhase.CLOSED_LOST -> Color(0xFFF44336)   // Red
    }
}

private fun getColorForLead(lead: Lead): Color {
    // Generate a consistent color based on the lead's name
    val hash = lead.name.hashCode()
    return when (Math.abs(hash % 5)) {
        0 -> Color(0xFF2196F3) // Blue
        1 -> Color(0xFF4CAF50) // Green
        2 -> Color(0xFFFFC107) // Amber
        3 -> Color(0xFFE91E63) // Pink
        else -> Color(0xFF9C27B0) // Purple
    }
}

private fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "new" -> Color(0xFF2196F3) // Blue
        "contacted" -> Color(0xFFFF9800) // Orange
        "qualified" -> Color(0xFF4CAF50) // Green
        "proposal" -> Color(0xFF9C27B0) // Purple
        "negotiation" -> Color(0xFFFFC107) // Amber
        "won" -> Color(0xFF00E676) // Green A400
        "lost" -> Color(0xFFF44336) // Red
        else -> Color.Gray
    }
}
