package com.nextgenbuildpro.features.projects

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Project Detail Screen - Shows detailed information about a specific project
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(navController: NavController, projectId: String) {
    val project = remember { getProjectById(projectId) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Tasks", "Photos", "Documents")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share project */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* Edit project */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add new item based on selected tab */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Project Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (project.status) {
                        "Active" -> MaterialTheme.colorScheme.primaryContainer
                        "Completed" -> MaterialTheme.colorScheme.tertiaryContainer
                        "On Hold" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = project.clientName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = project.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        AssistChip(
                            onClick = { },
                            label = { Text(project.status) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (project.status) {
                                        "Active" -> Icons.Default.PlayArrow
                                        "Completed" -> Icons.Default.CheckCircle
                                        "On Hold" -> Icons.Default.Pause
                                        else -> Icons.Default.Build
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Progress indicator
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "${project.progressPercentage}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        LinearProgressIndicator(
                            progress = project.progressPercentage / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Key metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProjectMetric(
                            label = "Budget",
                            value = "$${String.format("%,.0f", project.budget)}"
                        )
                        ProjectMetric(
                            label = "Start Date",
                            value = project.startDate
                        )
                        ProjectMetric(
                            label = "Due Date",
                            value = project.dueDate
                        )
                    }
                }
            }
            
            // Tabs
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
                0 -> ProjectOverviewTab(project)
                1 -> ProjectTasksTab(project.tasks)
                2 -> ProjectPhotosTab(project.photos)
                3 -> ProjectDocumentsTab(project.documents)
            }
        }
    }
}

@Composable
fun ProjectMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProjectOverviewTab(project: ProjectDetail) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Project Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = project.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Team Members",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    project.teamMembers.forEach { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = member.role,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectTasksTab(tasks: List<ProjectTask>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tasks) { task ->
            TaskCard(task = task)
        }
    }
}

@Composable
fun ProjectPhotosTab(photos: List<ProjectPhoto>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photos) { photo ->
            PhotoCard(photo = photo)
        }
    }
}

@Composable
fun ProjectDocumentsTab(documents: List<ProjectDocument>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(documents) { document ->
            DocumentCard(document = document)
        }
    }
}

@Composable
fun TaskCard(task: ProjectTask) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant
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
                imageVector = if (task.isCompleted) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.RadioButtonUnchecked
                },
                contentDescription = if (task.isCompleted) "Completed" else "Pending",
                tint = if (task.isCompleted) {
                    Color.Green
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Due: ${task.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AssistChip(
                onClick = { },
                label = { Text(task.priority) }
            )
        }
    }
}

@Composable
fun PhotoCard(photo: ProjectPhoto) {
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
                    text = photo.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = photo.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Placeholder for photo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Photo",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = photo.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DocumentCard(document: ProjectDocument) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (document.type) {
                    "PDF" -> Icons.Default.PictureAsPdf
                    "DOC" -> Icons.Default.Description
                    "IMG" -> Icons.Default.Image
                    else -> Icons.Default.AttachFile
                },
                contentDescription = document.type,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${document.type} • ${document.size} • ${document.uploadDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = { /* Download/View document */ }) {
                Icon(Icons.Default.Download, contentDescription = "Download")
            }
        }
    }
}

// Data classes
data class ProjectDetail(
    val id: String,
    val name: String,
    val clientName: String,
    val address: String,
    val description: String,
    val status: String,
    val progressPercentage: Int,
    val budget: Double,
    val startDate: String,
    val dueDate: String,
    val teamMembers: List<TeamMember>,
    val tasks: List<ProjectTask>,
    val photos: List<ProjectPhoto>,
    val documents: List<ProjectDocument>
)

data class TeamMember(
    val name: String,
    val role: String
)

data class ProjectTask(
    val title: String,
    val dueDate: String,
    val priority: String,
    val isCompleted: Boolean
)

data class ProjectPhoto(
    val title: String,
    val description: String,
    val date: String
)

data class ProjectDocument(
    val name: String,
    val type: String,
    val size: String,
    val uploadDate: String
)

private fun getProjectById(projectId: String): ProjectDetail {
    return ProjectDetail(
        id = projectId,
        name = "Kitchen Renovation",
        clientName = "Johnson Residence",
        address = "123 Main Street, Springfield",
        description = "Complete kitchen renovation including new cabinets, countertops, appliances, and flooring. The project includes electrical updates for modern appliances and plumbing modifications for a new kitchen island.",
        status = "Active",
        progressPercentage = 65,
        budget = 45000.0,
        startDate = "Nov 15, 2024",
        dueDate = "Dec 30, 2024",
        teamMembers = listOf(
            TeamMember("John Smith", "Project Manager"),
            TeamMember("Mike Johnson", "Lead Carpenter"),
            TeamMember("Sarah Davis", "Electrician"),
            TeamMember("Tom Wilson", "Plumber")
        ),
        tasks = listOf(
            ProjectTask("Demolition", "Nov 20, 2024", "High", true),
            ProjectTask("Electrical Rough-In", "Dec 5, 2024", "High", true),
            ProjectTask("Plumbing Rough-In", "Dec 8, 2024", "High", true),
            ProjectTask("Drywall Installation", "Dec 15, 2024", "Medium", false),
            ProjectTask("Cabinet Installation", "Dec 22, 2024", "High", false),
            ProjectTask("Countertop Installation", "Dec 28, 2024", "Medium", false)
        ),
        photos = listOf(
            ProjectPhoto(
                "Before Demolition",
                "Original kitchen layout before renovation started",
                "Nov 14, 2024"
            ),
            ProjectPhoto(
                "Demolition Complete",
                "Kitchen stripped down to studs, ready for electrical and plumbing",
                "Nov 21, 2024"
            ),
            ProjectPhoto(
                "Electrical Progress",
                "New electrical circuits installed for modern appliances",
                "Dec 6, 2024"
            )
        ),
        documents = listOf(
            ProjectDocument("Original Plans.pdf", "PDF", "2.3 MB", "Nov 10, 2024"),
            ProjectDocument("Permit Application.doc", "DOC", "156 KB", "Nov 12, 2024"),
            ProjectDocument("Material Invoice.pdf", "PDF", "445 KB", "Nov 18, 2024"),
            ProjectDocument("Progress Report.doc", "DOC", "89 KB", "Dec 1, 2024")
        )
    )
}