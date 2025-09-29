package com.nextgenbuildpro.features.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Calendar Timeline Screen - Shows project timeline with Gantt chart-like view
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTimelineScreen(navController: NavController) {
    var selectedProject by remember { mutableStateOf<TimelineProject?>(null) }
    var viewMode by remember { mutableStateOf(TimelineViewMode.WEEK) }
    val projects = remember { getTimelineProjects() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Timeline") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // View mode toggle
                    IconButton(onClick = { 
                        viewMode = when (viewMode) {
                            TimelineViewMode.WEEK -> TimelineViewMode.MONTH
                            TimelineViewMode.MONTH -> TimelineViewMode.QUARTER
                            TimelineViewMode.QUARTER -> TimelineViewMode.WEEK
                        }
                    }) {
                        Icon(
                            imageVector = when (viewMode) {
                                TimelineViewMode.WEEK -> Icons.Default.ViewWeek
                                TimelineViewMode.MONTH -> Icons.Default.CalendarMonth
                                TimelineViewMode.QUARTER -> Icons.Default.CalendarViewDay
                            },
                            contentDescription = "View Mode"
                        )
                    }
                    
                    IconButton(onClick = { /* Filter projects */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // View mode selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimelineViewMode.values().forEach { mode ->
                    FilterChip(
                        selected = viewMode == mode,
                        onClick = { viewMode = mode },
                        label = { Text(mode.displayName) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Timeline view
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Timeline header
                    TimelineHeader(viewMode = viewMode)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Project timeline bars
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(projects) { project ->
                            ProjectTimelineRow(
                                project = project,
                                viewMode = viewMode,
                                onProjectClick = { selectedProject = project }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Project details
            selectedProject?.let { project ->
                ProjectTimelineDetails(
                    project = project,
                    onDismiss = { selectedProject = null }
                )
            }
        }
    }
}

@Composable
fun TimelineHeader(viewMode: TimelineViewMode) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Project name column
        Text(
            text = "Project",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(120.dp)
        )
        
        // Timeline columns
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawTimelineHeader(this, viewMode)
            }
        }
    }
}

@Composable
fun ProjectTimelineRow(
    project: TimelineProject,
    viewMode: TimelineViewMode,
    onProjectClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Project info
        Card(
            modifier = Modifier
                .width(120.dp)
                .fillMaxHeight(),
            onClick = onProjectClick,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = project.client,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Timeline bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(30.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawProjectTimeline(this, project, viewMode)
            }
        }
    }
}

@Composable
fun ProjectTimelineDetails(
    project: TimelineProject,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimelineDetailItem(
                    icon = Icons.Default.Person,
                    label = "Client",
                    value = project.client
                )
                
                TimelineDetailItem(
                    icon = Icons.Default.CalendarToday,
                    label = "Start",
                    value = project.startDate.format(DateTimeFormatter.ofPattern("MMM dd"))
                )
                
                TimelineDetailItem(
                    icon = Icons.Default.Event,
                    label = "End",
                    value = project.endDate.format(DateTimeFormatter.ofPattern("MMM dd"))
                )
                
                TimelineDetailItem(
                    icon = Icons.Default.Percent,
                    label = "Progress",
                    value = "${project.progress}%"
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Milestones
            if (project.milestones.isNotEmpty()) {
                Text(
                    text = "Upcoming Milestones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                project.milestones.take(3).forEach { milestone ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (milestone.isCompleted) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (milestone.isCompleted) {
                                Color.Green
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = milestone.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = milestone.date.format(DateTimeFormatter.ofPattern("MMM dd")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun drawTimelineHeader(drawScope: DrawScope, viewMode: TimelineViewMode) {
    with(drawScope) {
        val width = size.width
        val height = size.height
        
        // Draw time markers based on view mode
        val divisions = when (viewMode) {
            TimelineViewMode.WEEK -> 7
            TimelineViewMode.MONTH -> 4
            TimelineViewMode.QUARTER -> 3
        }
        
        val divisionWidth = width / divisions
        
        for (i in 0..divisions) {
            val x = i * divisionWidth
            
            // Draw vertical lines
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1.dp.toPx()
            )
            
            // Add time labels (simplified)
            if (i < divisions) {
                val label = when (viewMode) {
                    TimelineViewMode.WEEK -> "Day ${i + 1}"
                    TimelineViewMode.MONTH -> "Week ${i + 1}"
                    TimelineViewMode.QUARTER -> "Month ${i + 1}"
                }
                
                // Note: In a real implementation, you'd use proper text drawing
                // For now, this is a placeholder for the timeline header
            }
        }
    }
}

private fun drawProjectTimeline(
    drawScope: DrawScope, 
    project: TimelineProject, 
    viewMode: TimelineViewMode
) {
    with(drawScope) {
        val width = size.width
        val height = size.height
        
        // Calculate project timeline position (simplified)
        val startPercent = 0.2f // Would be calculated based on actual dates
        val durationPercent = 0.6f // Would be calculated based on project duration
        
        val startX = width * startPercent
        val barWidth = width * durationPercent
        
        // Draw project bar
        drawRoundRect(
            color = when (project.status) {
                ProjectStatus.ACTIVE -> Color(0xFF4CAF50)
                ProjectStatus.PENDING -> Color(0xFFFFC107)
                ProjectStatus.COMPLETED -> Color(0xFF2196F3)
                ProjectStatus.DELAYED -> Color(0xFFF44336)
            },
            topLeft = Offset(startX, height * 0.25f),
            size = androidx.compose.ui.geometry.Size(barWidth, height * 0.5f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
        )
        
        // Draw progress overlay
        val progressWidth = barWidth * (project.progress / 100f)
        drawRoundRect(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = Offset(startX, height * 0.25f),
            size = androidx.compose.ui.geometry.Size(progressWidth, height * 0.5f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
        )
        
        // Draw milestones
        project.milestones.forEach { milestone ->
            val milestoneX = startX + (barWidth * 0.5f) // Simplified positioning
            drawCircle(
                color = if (milestone.isCompleted) Color.Green else Color.Orange,
                radius = 4.dp.toPx(),
                center = Offset(milestoneX, height * 0.5f)
            )
        }
    }
}

enum class TimelineViewMode(val displayName: String) {
    WEEK("Week"),
    MONTH("Month"),
    QUARTER("Quarter")
}

enum class ProjectStatus {
    ACTIVE, PENDING, COMPLETED, DELAYED
}

data class TimelineProject(
    val id: String,
    val name: String,
    val client: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val progress: Int,
    val status: ProjectStatus,
    val milestones: List<ProjectMilestone>
)

data class ProjectMilestone(
    val name: String,
    val date: LocalDate,
    val isCompleted: Boolean
)

private fun getTimelineProjects(): List<TimelineProject> {
    return listOf(
        TimelineProject(
            id = "1",
            name = "Kitchen Renovation",
            client = "Johnson Family",
            startDate = LocalDate.now().minusDays(10),
            endDate = LocalDate.now().plusDays(20),
            progress = 65,
            status = ProjectStatus.ACTIVE,
            milestones = listOf(
                ProjectMilestone("Demolition", LocalDate.now().minusDays(8), true),
                ProjectMilestone("Electrical", LocalDate.now().minusDays(2), true),
                ProjectMilestone("Cabinets", LocalDate.now().plusDays(5), false),
                ProjectMilestone("Countertops", LocalDate.now().plusDays(15), false)
            )
        ),
        TimelineProject(
            id = "2",
            name = "Bathroom Remodel",
            client = "Smith Residence",
            startDate = LocalDate.now().plusDays(5),
            endDate = LocalDate.now().plusDays(25),
            progress = 0,
            status = ProjectStatus.PENDING,
            milestones = listOf(
                ProjectMilestone("Design Approval", LocalDate.now().plusDays(3), false),
                ProjectMilestone("Permit", LocalDate.now().plusDays(7), false),
                ProjectMilestone("Demo", LocalDate.now().plusDays(10), false)
            )
        ),
        TimelineProject(
            id = "3",
            name = "Deck Construction",
            client = "Davis Home",
            startDate = LocalDate.now().minusDays(30),
            endDate = LocalDate.now().minusDays(5),
            progress = 100,
            status = ProjectStatus.COMPLETED,
            milestones = listOf(
                ProjectMilestone("Foundation", LocalDate.now().minusDays(25), true),
                ProjectMilestone("Framing", LocalDate.now().minusDays(20), true),
                ProjectMilestone("Decking", LocalDate.now().minusDays(10), true),
                ProjectMilestone("Finishing", LocalDate.now().minusDays(5), true)
            )
        ),
        TimelineProject(
            id = "4",
            name = "Living Room Update",
            client = "Wilson Family",
            startDate = LocalDate.now().minusDays(15),
            endDate = LocalDate.now().plusDays(5),
            progress = 85,
            status = ProjectStatus.DELAYED,
            milestones = listOf(
                ProjectMilestone("Planning", LocalDate.now().minusDays(12), true),
                ProjectMilestone("Flooring", LocalDate.now().minusDays(5), true),
                ProjectMilestone("Painting", LocalDate.now().plusDays(2), false)
            )
        )
    )
}