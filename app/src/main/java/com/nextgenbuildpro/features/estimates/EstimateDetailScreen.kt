package com.nextgenbuildpro.features.estimates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.nextgenbuildpro.core.DateUtils
import com.nextgenbuildpro.debug.WorkflowAnalyzer
import com.nextgenbuildpro.navigation.NavDestinations
import com.nextgenbuildpro.pm.data.model.Estimate
import com.nextgenbuildpro.pm.data.model.EstimateItem
import com.nextgenbuildpro.pm.data.model.EstimateStatus
import com.nextgenbuildpro.pm.rememberPmComponents
import com.nextgenbuildpro.ui.ButtonNavigationValidator
import com.nextgenbuildpro.ui.FeatureCompletionTracker
import com.nextgenbuildpro.ui.components.completeFeature
import com.nextgenbuildpro.ui.components.trackFeature
import com.nextgenbuildpro.ui.components.trackNavigation
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstimateDetailScreen(navController: NavController, estimateId: String) {
    // Session ID for tracking this user's journey
    val sessionId = remember { "user_${System.currentTimeMillis()}" }

    // Register this screen visit with the WorkflowAnalyzer
    LaunchedEffect(Unit) {
        WorkflowAnalyzer.trackScreenVisit(
            userId = sessionId,
            destination = "${NavDestinations.ESTIMATE_DETAIL}/$estimateId",
            sourceElementId = "estimate_item_$estimateId"
        )
    }

    // Get the EstimatesViewModel from the PM module
    val pmComponents = rememberPmComponents()
    val viewModel = pmComponents.estimatesViewModel

    // Fetch the estimate data from the repository
    LaunchedEffect(estimateId) {
        // Track the data loading process
        val featureId = FeatureCompletionTracker.trackFeatureStart(
            elementId = "load_estimate_details_$estimateId",
            screenName = "EstimateDetailScreen",
            featureName = "load_estimate_details"
        )

        try {
            viewModel.clearSelectedEstimate() // Clear any previously selected estimate
            viewModel.refresh() // Refresh the list of estimates

            // Mark the loading feature as complete once data is refreshed
            completeFeature(featureId, true, "Successfully initiated estimate data loading")
        } catch (e: Exception) {
            // Mark the loading feature as incomplete if there was an error
            completeFeature(featureId, false, "Error loading estimate data: ${e.message}")

            // Register this as a dead-end element
            WorkflowAnalyzer.registerDeadEndElement(
                elementId = "load_estimate_details_$estimateId",
                screenName = "EstimateDetailScreen",
                intendedDestination = "${NavDestinations.ESTIMATE_DETAIL}/$estimateId"
            )
        }
    }

    // Get all estimates and find the one with the matching ID
    val allEstimates by viewModel.filteredEstimates.collectAsState()
    val selectedEstimate by viewModel.selectedEstimate.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Find the estimate with the matching ID
    val foundEstimate = allEstimates.find { it.id == estimateId }

    // If found, select it
    LaunchedEffect(foundEstimate) {
        if (foundEstimate != null) {
            viewModel.selectEstimate(foundEstimate)
            isLoading = false

            // Track successful data loading
            val featureId = FeatureCompletionTracker.trackFeatureStart(
                elementId = "display_estimate_details_$estimateId",
                screenName = "EstimateDetailScreen",
                featureName = "display_estimate_details"
            )
            completeFeature(featureId, true, "Successfully loaded and displayed estimate data")
        } else if (allEstimates.isNotEmpty()) {
            // If we have estimates but couldn't find the requested one
            isLoading = false
            error = "Estimate not found"

            // Track failed data loading
            val featureId = FeatureCompletionTracker.trackFeatureStart(
                elementId = "display_estimate_details_$estimateId",
                screenName = "EstimateDetailScreen",
                featureName = "display_estimate_details"
            )
            completeFeature(featureId, false, "Failed to find estimate with ID: $estimateId")

            // Register this as a dead-end element
            WorkflowAnalyzer.registerDeadEndElement(
                elementId = "display_estimate_details_$estimateId",
                screenName = "EstimateDetailScreen",
                intendedDestination = "${NavDestinations.ESTIMATE_DETAIL}/$estimateId"
            )
        }
    }

    // UI states for dialog visibility
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedEstimate?.title ?: "Estimate Details") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigateUp()
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.trackFeature(
                            buttonId = "edit_estimate_$estimateId",
                            screenName = "EstimateDetailScreen",
                            featureName = "edit_estimate"
                        ) { featureSessionId ->
                            val success = ButtonNavigationValidator.validateAndNavigate(
                                navController = navController,
                                destination = "${NavDestinations.ESTIMATE_EDITOR}/$estimateId",
                                buttonId = "edit_estimate_$estimateId",
                                screenName = "EstimateDetailScreen"
                            )

                            if (!success) {
                                completeFeature(featureSessionId, false, "Failed to navigate to estimate editor")
                                WorkflowAnalyzer.registerDeadEndElement(
                                    elementId = "edit_estimate_$estimateId",
                                    screenName = "EstimateDetailScreen",
                                    intendedDestination = "${NavDestinations.ESTIMATE_EDITOR}/$estimateId"
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Estimate")
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Estimate")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                modifier = Modifier.trackFeature(
                    buttonId = "add_item_to_estimate_$estimateId",
                    screenName = "EstimateDetailScreen",
                    featureName = "add_estimate_item"
                ) { featureSessionId ->
                    val success = ButtonNavigationValidator.validateAndNavigate(
                        navController = navController,
                        destination = "${NavDestinations.ESTIMATE_ITEM_EDITOR}/$estimateId",
                        buttonId = "add_item_to_estimate_$estimateId",
                        screenName = "EstimateDetailScreen"
                    )

                    if (!success) {
                        completeFeature(featureSessionId, false, "Failed to navigate to item editor")
                        WorkflowAnalyzer.registerDeadEndElement(
                            elementId = "add_item_to_estimate_$estimateId",
                            screenName = "EstimateDetailScreen",
                            intendedDestination = "${NavDestinations.ESTIMATE_ITEM_EDITOR}/$estimateId"
                        )
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = error ?: "Unknown error",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigateUp() }
                            ) {
                                Text("Go Back")
                            }
                        }
                    }
                }
                selectedEstimate != null -> {
                    val estimate = selectedEstimate!!
                    EstimateDetailContent(
                        estimate = estimate,
                        onItemClick = { itemId ->
                            // Track item editing as part of workflow
                            val featureId = FeatureCompletionTracker.trackFeatureStart(
                                elementId = "edit_estimate_item_${itemId}",
                                screenName = "EstimateDetailScreen",
                                featureName = "edit_estimate_item"
                            )

                            val success = ButtonNavigationValidator.validateAndNavigate(
                                navController = navController,
                                destination = "${NavDestinations.ESTIMATE_ITEM_EDITOR}/$estimateId/$itemId",
                                buttonId = "edit_item_${itemId}",
                                screenName = "EstimateDetailScreen"
                            )

                            if (!success) {
                                completeFeature(featureId, false, "Failed to navigate to item editor")
                                WorkflowAnalyzer.registerDeadEndElement(
                                    elementId = "edit_item_${itemId}",
                                    screenName = "EstimateDetailScreen",
                                    intendedDestination = "${NavDestinations.ESTIMATE_ITEM_EDITOR}/$estimateId/$itemId"
                                )
                            }
                        },
                        onChangeStatus = { showStatusDialog = true },
                        onSendEstimate = {
                            // Track estimate sending as part of workflow
                            val featureId = FeatureCompletionTracker.trackFeatureStart(
                                elementId = "send_estimate_$estimateId",
                                screenName = "EstimateDetailScreen",
                                featureName = "send_estimate"
                            )

                            // Here you would implement the actual sending logic
                            // For now, we'll just track it as a potentially incomplete feature
                            WorkflowAnalyzer.registerDeadEndElement(
                                elementId = "send_estimate_$estimateId",
                                screenName = "EstimateDetailScreen",
                                intendedDestination = "SendEstimateEmail"
                            )
                            completeFeature(featureId, false, "Send estimate feature not fully implemented")
                        }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Estimate") },
            text = { Text("Are you sure you want to delete this estimate? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        // Track estimate deletion as part of workflow
                        val featureId = FeatureCompletionTracker.trackFeatureStart(
                            elementId = "delete_estimate_$estimateId",
                            screenName = "EstimateDetailScreen",
                            featureName = "delete_estimate"
                        )

                        // Perform deletion
                        viewModel.deleteEstimate(estimateId)
                        showDeleteDialog = false
                        completeFeature(featureId, true, "Estimate deleted successfully")

                        // Navigate back
                        navController.navigateUp()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Status change dialog
    if (showStatusDialog && selectedEstimate != null) {
        val statusOptions = EstimateStatus.values()

        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Change Estimate Status") },
            text = {
                Column {
                    statusOptions.forEach { status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Track status change as part of workflow
                                    val featureId = FeatureCompletionTracker.trackFeatureStart(
                                        elementId = "change_estimate_status_$estimateId",
                                        screenName = "EstimateDetailScreen",
                                        featureName = "change_estimate_status"
                                    )

                                    // Perform status update
                                    viewModel.updateEstimateStatus(estimateId, status)
                                    showStatusDialog = false
                                    completeFeature(featureId, true, "Estimate status updated successfully")
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedEstimate?.status == status.name,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(status.name)
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(
                    onClick = { showStatusDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EstimateDetailTopBar(
    estimate: Estimate,
    navController: NavController,
    onActionsClick: () -> Unit
) {
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = estimate.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Status: ${estimate.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }

            IconButton(
                onClick = onActionsClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Actions",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun EstimateHeader(estimate: Estimate) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = estimate.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currencyFormat.format(estimate.amount),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                val statusColor = when (estimate.status) {
                    EstimateStatus.DRAFT.name -> Color(0xFF607D8B)    // Blue Grey
                    EstimateStatus.SENT.name -> Color(0xFF2196F3)     // Blue
                    EstimateStatus.APPROVED.name -> Color(0xFF4CAF50) // Green
                    EstimateStatus.DECLINED.name -> Color(0xFFF44336) // Red
                    EstimateStatus.EXPIRED.name -> Color(0xFF9E9E9E)  // Grey
                    else -> Color.Gray
                }

                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = estimate.status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Created: ${estimate.createdAt}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Text(
                text = "Last updated: ${estimate.updatedAt}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ClientInformation(clientName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Client Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = clientName.take(2).uppercase(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = clientName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Client",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun EstimateItemCard(item: EstimateItem, onClick: () -> Unit) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val totalPrice = remember { item.quantity * item.unitPrice }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = when (item.type) {
                        "Labor" -> Color(0xFFE1F5FE)
                        "Material" -> Color(0xFFE8F5E9)
                        else -> Color(0xFFF5F5F5)
                    },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = item.type,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.quantity} ${item.unit} × ${currencyFormat.format(item.unitPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = currencyFormat.format(totalPrice),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EstimateSummary(estimate: Estimate) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    // Calculate subtotals by type
    val laborItems = estimate.items.filter { item -> item.type == "Labor" }
    val materialItems = estimate.items.filter { item -> item.type == "Material" }

    val laborTotal = laborItems.sumOf { item -> item.quantity * item.unitPrice }
    val materialTotal = materialItems.sumOf { item -> item.quantity * item.unitPrice }
    val total = estimate.amount

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Labor subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Labor",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = currencyFormat.format(laborTotal),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Material subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Materials",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = currencyFormat.format(materialTotal),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currencyFormat.format(total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ActionButtons(onPreviewClick: () -> Unit, onSendClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onPreviewClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "Preview",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Preview")
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = onSendClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send")
        }
    }
}

@Composable
fun EstimateActionsDialog(
    onDismiss: () -> Unit,
    onCollectSignature: () -> Unit,
    onMarkApproved: () -> Unit,
    onMarkDeclined: () -> Unit,
    onDuplicate: () -> Unit,
    onViewActivity: () -> Unit,
    onArchive: () -> Unit,
    onDownloadPdf: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Estimate Actions") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Document Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                ActionItem(
                    icon = Icons.Default.Draw,
                    text = "Collect Signature",
                    onClick = {
                        onDismiss()
                        onCollectSignature()
                    }
                )

                ActionItem(
                    icon = Icons.Default.CheckCircle,
                    text = "Mark as Approved",
                    onClick = {
                        onDismiss()
                        onMarkApproved()
                    }
                )

                ActionItem(
                    icon = Icons.Default.Cancel,
                    text = "Mark as Declined",
                    onClick = {
                        onDismiss()
                        onMarkDeclined()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Document Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                ActionItem(
                    icon = Icons.Default.ContentCopy,
                    text = "Duplicate",
                    onClick = {
                        onDismiss()
                        onDuplicate()
                    }
                )

                ActionItem(
                    icon = Icons.Default.History,
                    text = "View Activity Stream",
                    onClick = {
                        onDismiss()
                        onViewActivity()
                    }
                )

                ActionItem(
                    icon = Icons.Default.Archive,
                    text = "Archive",
                    onClick = {
                        onDismiss()
                        onArchive()
                    }
                )

                ActionItem(
                    icon = Icons.Default.PictureAsPdf,
                    text = "Download as PDF",
                    onClick = {
                        onDismiss()
                        onDownloadPdf()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun EstimateDetailContent(
    estimate: Estimate,
    onItemClick: (String) -> Unit,
    onChangeStatus: () -> Unit,
    onSendEstimate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header
        EstimateHeader(estimate = estimate)

        Spacer(modifier = Modifier.height(16.dp))

        // Client information
        ClientInformation(clientName = estimate.clientName)

        Spacer(modifier = Modifier.height(16.dp))

        // Items section
        Text(
            text = "Items",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        // List of estimate items
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(estimate.items) { item ->
                EstimateItemCard(
                    item = item,
                    onClick = { onItemClick(item.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary section
        EstimateSummary(estimate = estimate)

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        ActionButtons(
            onPreviewClick = { /* TODO: Implement preview action */ },
            onSendClick = onSendEstimate
        )
    }
}
