package com.nextgenbuildpro.clientengagement.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Client Portal functional screen - replaced placeholder
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientPortalScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Client Portal") },
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
            item {
                Text(
                    text = "Welcome to Your Project Portal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Text(
                    text = "Access all your project information, estimates, and communications in one place.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(getClientPortalQuickActions()) { action ->
                ClientPortalActionCard(
                    action = action,
                    onClick = { navController.navigate(action.route) }
                )
            }

            // Recent Activity
            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(getRecentActivities()) { activity ->
                ActivityCard(activity = activity)
            }
        }
    }
}

@Composable
fun ClientPortalActionCard(
    action: ClientPortalAction,
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
                imageVector = action.icon,
                contentDescription = action.title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = action.description,
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

@Composable
fun ActivityCard(activity: ClientActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
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
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = activity.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

data class ClientPortalAction(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

data class ClientActivity(
    val title: String,
    val description: String,
    val date: String
)

private fun getClientPortalQuickActions(): List<ClientPortalAction> {
    return listOf(
        ClientPortalAction(
            title = "View Estimates",
            description = "Review and approve project estimates",
            icon = Icons.Default.AttachMoney,
            route = "client_estimates"
        ),
        ClientPortalAction(
            title = "Project Progress",
            description = "Track your project's current status",
            icon = Icons.Default.Timeline,
            route = "progress_updates"
        ),
        ClientPortalAction(
            title = "Messages",
            description = "Chat with your project team",
            icon = Icons.Default.Message,
            route = "client_messages"
        ),
        ClientPortalAction(
            title = "Documents",
            description = "Access contracts and project documents",
            icon = Icons.Default.Folder,
            route = "client_documents"
        ),
        ClientPortalAction(
            title = "Digital Signatures",
            description = "Sign documents electronically",
            icon = Icons.Default.Draw,
            route = "digital_signature"
        )
    )
}

private fun getRecentActivities(): List<ClientActivity> {
    return listOf(
        ClientActivity(
            title = "Estimate Updated",
            description = "Kitchen renovation estimate has been updated with new materials",
            date = "2 hours ago"
        ),
        ClientActivity(
            title = "Progress Photo",
            description = "New photos added showing completion of electrical work",
            date = "1 day ago"
        ),
        ClientActivity(
            title = "Message Received",
            description = "Your project manager sent you an update about scheduling",
            date = "2 days ago"
        )
    )
}

/**
 * Progress Updates functional screen - replaced placeholder
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressUpdatesScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Timeline", "Photos", "Milestones")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Updates") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab bar
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Tab content
            when (selectedTab) {
                0 -> ProgressTimelineTab()
                1 -> ProgressPhotosTab()
                2 -> MilestonesTab()
            }
        }
    }
}

@Composable
fun ProgressTimelineTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(getProgressUpdates()) { update ->
            ProgressUpdateCard(update = update)
        }
    }
}

@Composable
fun ProgressPhotosTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(getProgressPhotos()) { photoUpdate ->
            ProgressPhotoCard(photoUpdate = photoUpdate)
        }
    }
}

@Composable
fun MilestonesTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(getProjectMilestones()) { milestone ->
            MilestoneCard(milestone = milestone)
        }
    }
}

@Composable
fun ProgressUpdateCard(update: ProgressUpdate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = update.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = update.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    text = update.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (update.status.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = { },
                    label = { Text(update.status) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ProgressPhotoCard(photoUpdate: ProgressPhotoUpdate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
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
                    text = photoUpdate.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = photoUpdate.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Placeholder for photo gallery
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Photos",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${photoUpdate.photoCount} Photos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = photoUpdate.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun MilestoneCard(milestone: ProjectMilestone) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (milestone.isCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (milestone.isCompleted) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.RadioButtonUnchecked
                },
                contentDescription = if (milestone.isCompleted) "Completed" else "Pending",
                tint = if (milestone.isCompleted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (milestone.isCompleted) {
                        "Completed on ${milestone.completedDate}"
                    } else {
                        "Target: ${milestone.targetDate}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (milestone.progress > 0 && !milestone.isCompleted) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${milestone.progress}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = milestone.progress / 100f,
                        modifier = Modifier.width(60.dp)
                    )
                }
            }
        }
    }
}

data class ProgressUpdate(
    val title: String,
    val description: String,
    val date: String,
    val status: String = ""
)

data class ProgressPhotoUpdate(
    val title: String,
    val description: String,
    val date: String,
    val photoCount: Int
)

data class ProjectMilestone(
    val title: String,
    val targetDate: String,
    val completedDate: String = "",
    val isCompleted: Boolean = false,
    val progress: Int = 0
)

private fun getProgressUpdates(): List<ProgressUpdate> {
    return listOf(
        ProgressUpdate(
            title = "Electrical Work Completed",
            description = "All electrical outlets and fixtures have been installed and tested",
            date = "Dec 10, 2024",
            status = "Completed"
        ),
        ProgressUpdate(
            title = "Plumbing Rough-In",
            description = "Rough plumbing installed, ready for inspection",
            date = "Dec 8, 2024",
            status = "Pending Inspection"
        ),
        ProgressUpdate(
            title = "Framing Complete",
            description = "All wall framing and structural work completed",
            date = "Dec 5, 2024",
            status = "Completed"
        )
    )
}

private fun getProgressPhotos(): List<ProgressPhotoUpdate> {
    return listOf(
        ProgressPhotoUpdate(
            title = "Kitchen Electrical Installation",
            description = "Photos showing completed electrical work including outlets and lighting",
            date = "Dec 10, 2024",
            photoCount = 8
        ),
        ProgressPhotoUpdate(
            title = "Plumbing Progress",
            description = "Rough plumbing installation photos",
            date = "Dec 8, 2024",
            photoCount = 5
        )
    )
}

private fun getProjectMilestones(): List<ProjectMilestone> {
    return listOf(
        ProjectMilestone(
            title = "Demolition",
            targetDate = "Dec 1, 2024",
            completedDate = "Dec 1, 2024",
            isCompleted = true
        ),
        ProjectMilestone(
            title = "Framing",
            targetDate = "Dec 5, 2024",
            completedDate = "Dec 5, 2024",
            isCompleted = true
        ),
        ProjectMilestone(
            title = "Electrical & Plumbing",
            targetDate = "Dec 12, 2024",
            progress = 85
        ),
        ProjectMilestone(
            title = "Drywall",
            targetDate = "Dec 18, 2024",
            progress = 0
        ),
        ProjectMilestone(
            title = "Final Finishes",
            targetDate = "Dec 28, 2024",
            progress = 0
        )
    )
}

/**
 * Digital Signature functional screen - replaced placeholder
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalSignatureScreen(navController: NavController) {
    var signaturePadVisible by remember { mutableStateOf(false) }
    var documentSelected by remember { mutableStateOf<String?>(null) }
    var isSigningMode by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digital Signatures") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSigningMode) {
                        TextButton(
                            onClick = { isSigningMode = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isSigningMode && documentSelected != null) {
            SignaturePadScreen(
                documentName = documentSelected!!,
                onSignatureComplete = { signature ->
                    // Handle signature completion
                    isSigningMode = false
                    // Navigate back or show success message
                },
                onCancel = { isSigningMode = false }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Document Signatures",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Text(
                        text = "Select a document to sign digitally",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Documents awaiting signature
                item {
                    Text(
                        text = "Pending Signatures",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(getPendingDocuments()) { document ->
                    DocumentSignatureCard(
                        document = document,
                        onSignClick = {
                            documentSelected = document.title
                            isSigningMode = true
                        }
                    )
                }

                // Completed signatures
                item {
                    Text(
                        text = "Completed Signatures",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(getCompletedDocuments()) { document ->
                    CompletedSignatureCard(document = document)
                }
            }
        }
    }
}

@Composable
fun DocumentSignatureCard(
    document: SignatureDocument,
    onSignClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = document.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Due: ${document.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Button(
                    onClick = onSignClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Draw,
                        contentDescription = "Sign",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign")
                }
            }
        }
    }
}

@Composable
fun CompletedSignatureCard(document: SignatureDocument) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Signed on ${document.signedDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SignaturePadScreen(
    documentName: String,
    onSignatureComplete: (String) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Sign: $documentName",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Signature pad placeholder - in a real implementation, this would be a custom Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Signature Pad\n(Touch to sign)",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = { onSignatureComplete("signature_data") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Complete")
            }
        }
    }
}

data class SignatureDocument(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val signedDate: String? = null,
    val isCompleted: Boolean = false
)

private fun getPendingDocuments(): List<SignatureDocument> {
    return listOf(
        SignatureDocument(
            id = "1",
            title = "Project Contract",
            description = "Main construction contract for kitchen renovation",
            dueDate = "Dec 15, 2024"
        ),
        SignatureDocument(
            id = "2",
            title = "Change Order #1",
            description = "Additional electrical work approval",
            dueDate = "Dec 20, 2024"
        )
    )
}

private fun getCompletedDocuments(): List<SignatureDocument> {
    return listOf(
        SignatureDocument(
            id = "3",
            title = "Initial Estimate",
            description = "Kitchen renovation estimate approval",
            dueDate = "Dec 1, 2024",
            signedDate = "Dec 1, 2024",
            isCompleted = true
        )
    )
}
