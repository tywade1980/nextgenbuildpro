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
import com.nextgenbuildpro.pm.data.model.Estimate
import com.nextgenbuildpro.pm.data.model.EstimateItem
import com.nextgenbuildpro.pm.data.model.EstimateStatus
import com.nextgenbuildpro.pm.rememberPmComponents
import java.text.NumberFormat
import java.util.*

@Composable
fun EstimateDetailScreen(navController: NavController, estimateId: String) {
    // Get the EstimatesViewModel from the PM module
    val pmComponents = rememberPmComponents()
    val viewModel = pmComponents.estimatesViewModel

    // Fetch the estimate data from the repository
    LaunchedEffect(estimateId) {
        viewModel.clearSelectedEstimate() // Clear any previously selected estimate
        viewModel.refresh() // Refresh the list of estimates
    }

    // Get all estimates and find the one with the matching ID
    val allEstimates by viewModel.filteredEstimates.collectAsState()
    val selectedEstimate by viewModel.selectedEstimate.collectAsState()

    // Find the estimate with the matching ID
    val foundEstimate = allEstimates.find { it.id == estimateId }

    // If found, select it
    LaunchedEffect(foundEstimate) {
        if (foundEstimate != null) {
            viewModel.selectEstimate(foundEstimate)
        }
    }

    // Use the selected estimate or a placeholder if not found
    val estimate = selectedEstimate ?: remember {
        Estimate(
            id = estimateId,
            projectId = null,
            title = "Loading...",
            clientName = "",
            amount = 0.0,
            status = EstimateStatus.DRAFT.name,
            createdAt = "",
            updatedAt = "",
            items = emptyList()
        )
    }

    var showActionsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EstimateDetailTopBar(
                estimate = estimate,
                navController = navController,
                onActionsClick = { showActionsDialog = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("estimate_item_editor/${estimate.id}/null") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estimate header
            item {
                EstimateHeader(estimate = estimate)
            }

            // Client information
            item {
                ClientInformation(clientName = estimate.clientName)
            }

            // Items section
            item {
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            // List of estimate items
            items(estimate.items) { item ->
                EstimateItemCard(
                    item = item,
                    onClick = { navController.navigate("estimate_item_editor/${estimate.id}/${item.id}") }
                )
            }

            // Summary section
            item {
                EstimateSummary(estimate = estimate)
            }

            // Action buttons
            item {
                ActionButtons(
                    onPreviewClick = { /* Handle preview */ },
                    onSendClick = { /* Handle send */ }
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Actions dialog
        if (showActionsDialog) {
            EstimateActionsDialog(
                onDismiss = { showActionsDialog = false },
                onCollectSignature = { /* Handle collect signature */ },
                onMarkApproved = { /* Handle mark approved */ },
                onMarkDeclined = { /* Handle mark declined */ },
                onDuplicate = { /* Handle duplicate */ },
                onViewActivity = { /* Handle view activity */ },
                onArchive = { /* Handle archive */ },
                onDownloadPdf = { /* Handle download PDF */ }
            )
        }
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
