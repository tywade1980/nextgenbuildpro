package com.nextgenbuildpro.timeclock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.pm.data.model.Project
import com.nextgenbuildpro.pm.rememberPmComponents
import com.nextgenbuildpro.service.rememberServiceComponents
import com.nextgenbuildpro.timeclock.data.model.TimeClockSession
import com.nextgenbuildpro.timeclock.data.model.TimeClockStatus
import com.nextgenbuildpro.timeclock.data.model.WorkLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for time clock functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeClockScreen(navController: NavController) {
    LocalContext.current
    val serviceComponents = rememberServiceComponents()
    val pmComponents = rememberPmComponents()
    val timeClockService = serviceComponents.timeClockService
    val projectRepository = pmComponents.projectRepository
    val coroutineScope = rememberCoroutineScope()

    // State
    val timeClockStatus by timeClockService.timeClockStatus
    val lastKnownLocation by timeClockService.lastKnownLocation

    var workLocations by remember { mutableStateOf<List<WorkLocation>>(emptyList()) }
    var timeClockSessions by remember { mutableStateOf<List<TimeClockSession>>(emptyList()) }
    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var showWorkLocationDialog by remember { mutableStateOf(false) }
    var selectedProjectId by remember { mutableStateOf<String?>(null) }

    // Load data
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            workLocations = timeClockService.getAllWorkLocations()
            timeClockSessions = timeClockService.getTimeClockSessions()
            projects = projectRepository.getAll()
        }
    }

    // Periodically refresh time clock sessions
    LaunchedEffect(Unit) {
        while(true) {
            delay(10000) // Refresh every 10 seconds
            coroutineScope.launch {
                timeClockSessions = timeClockService.getTimeClockSessions()
            }
        }
    }

    // Date formatter
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }
    val durationFormatter = remember { 
        { millis: Long? -> 
            if (millis == null) {
                "In progress"
            } else {
                val hours = millis / (1000 * 60 * 60)
                val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
                String.format("%d hrs %d mins", hours, minutes)
            }
        } 
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Time Clock") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current status card
            TimeClockStatusCard(
                timeClockStatus = timeClockStatus,
                dateFormatter = dateFormatter,
                projects = projects,
                onClockIn = { showWorkLocationDialog = true },
                onClockOut = {
                    coroutineScope.launch {
                        timeClockService.clockOut()
                        timeClockSessions = timeClockService.getTimeClockSessions()
                    }
                }
            )

            // Work locations card
            WorkLocationsCard(
                workLocations = workLocations,
                currentWorkLocationId = timeClockStatus.currentWorkLocationId,
                lastKnownLocation = lastKnownLocation
            )

            // Time clock history card
            TimeClockHistoryCard(
                sessions = timeClockSessions,
                dateFormatter = dateFormatter,
                durationFormatter = durationFormatter,
                projects = projects
            )
        }

        // Work location selection dialog
        if (showWorkLocationDialog) {
            WorkLocationSelectionDialog(
                workLocations = workLocations,
                projects = projects,
                onDismiss = { showWorkLocationDialog = false },
                onWorkLocationAndProjectSelected = { workLocationId, projectId ->
                    coroutineScope.launch {
                        timeClockService.clockIn(workLocationId, projectId)
                        timeClockSessions = timeClockService.getTimeClockSessions()
                        selectedProjectId = projectId
                        showWorkLocationDialog = false
                    }
                }
            )
        }
    }
}

/**
 * Real-time clock display component
 */
@Composable
fun CurrentTimeDisplay() {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())

    LaunchedEffect(Unit) {
        while(true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    Text(
        text = "Current Time: ${dateFormatter.format(Date(currentTime))}",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold
    )
}

/**
 * Card displaying current time clock status
 */
@Composable
fun TimeClockStatusCard(
    timeClockStatus: TimeClockStatus,
    dateFormatter: SimpleDateFormat,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit,
    projects: List<Project> = emptyList()
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Current Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Real-time clock display
            CurrentTimeDisplay()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = if (timeClockStatus.isClockedIn) Color.Green else Color.Red,
                            shape = RoundedCornerShape(8.dp)
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (timeClockStatus.isClockedIn) "Clocked In" else "Clocked Out",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (timeClockStatus.isClockedIn) {
                Spacer(modifier = Modifier.height(4.dp))

                // Work location information
                Text(
                    text = "Location: ${timeClockStatus.currentWorkLocationName ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Clock in time and duration
                timeClockStatus.lastClockInTime?.let { clockInTime ->
                    Text(
                        text = "Clocked in at: ${dateFormatter.format(clockInTime)}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Calculate and display duration
                    val currentTime = System.currentTimeMillis()
                    val duration = currentTime - clockInTime.time
                    val hours = duration / (1000 * 60 * 60)
                    val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)
                    val seconds = (duration % (1000 * 60)) / 1000

                    Text(
                        text = "Duration: ${String.format("%02d:%02d:%02d", hours, minutes, seconds)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Project information (enhanced)
                timeClockStatus.currentProjectId?.let { projectId ->
                    // Find the project details
                    val project = projects.find { it.id == projectId }

                    // Project card with details
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Current Project",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            if (project != null) {
                                Text(
                                    text = project.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Status: ${project.status}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "Progress: ${project.progress}%",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                // Progress bar
                                LinearProgressIndicator(
                                    progress = project.progress / 100f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )
                            } else {
                                Text(
                                    text = "Project ID: $projectId",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Project details not available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = if (timeClockStatus.isClockedIn) onClockOut else onClockIn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (timeClockStatus.isClockedIn) Icons.Default.ExitToApp else Icons.Default.Login,
                    contentDescription = if (timeClockStatus.isClockedIn) "Clock Out" else "Clock In"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (timeClockStatus.isClockedIn) "Clock Out" else "Clock In")
            }
        }
    }
}

/**
 * Card displaying work locations
 */
@Composable
fun WorkLocationsCard(
    workLocations: List<WorkLocation>,
    currentWorkLocationId: String?,
    lastKnownLocation: android.location.Location?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Work Locations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (workLocations.isEmpty()) {
                Text(
                    text = "No work locations defined",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(workLocations) { workLocation ->
                        WorkLocationItem(
                            workLocation = workLocation,
                            isCurrentLocation = workLocation.id == currentWorkLocationId,
                            lastKnownLocation = lastKnownLocation
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual work location item
 */
@Composable
fun WorkLocationItem(
    workLocation: WorkLocation,
    isCurrentLocation: Boolean,
    lastKnownLocation: android.location.Location?
) {
    val isNearby = remember(lastKnownLocation) {
        if (lastKnownLocation == null) false
        else {
            val location = android.location.Location("LastKnown")
            location.latitude = lastKnownLocation.latitude
            location.longitude = lastKnownLocation.longitude
            workLocation.isLocationWithinRadius(location)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = when {
            isCurrentLocation -> MaterialTheme.colorScheme.primaryContainer
            isNearby -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    isCurrentLocation -> Icons.Default.LocationOn
                    isNearby -> Icons.Default.NearMe
                    else -> Icons.Default.LocationCity
                },
                contentDescription = null,
                tint = when {
                    isCurrentLocation -> MaterialTheme.colorScheme.primary
                    isNearby -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = workLocation.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = workLocation.address,
                    style = MaterialTheme.typography.bodySmall
                )

                if (isNearby && !isCurrentLocation) {
                    Text(
                        text = "You are near this location",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/**
 * Card displaying time clock history
 */
@Composable
fun TimeClockHistoryCard(
    sessions: List<TimeClockSession>,
    dateFormatter: SimpleDateFormat,
    durationFormatter: (Long?) -> String,
    projects: List<Project> = emptyList()
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Time Clock History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (sessions.isEmpty()) {
                Text(
                    text = "No time clock history",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions.sortedByDescending { it.date }) { session ->
                        TimeClockSessionItem(
                            session = session,
                            dateFormatter = dateFormatter,
                            durationFormatter = durationFormatter,
                            project = session.projectId?.let { projectId ->
                                projects.find { it.id == projectId }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual time clock session item
 */
@Composable
fun TimeClockSessionItem(
    session: TimeClockSession,
    dateFormatter: SimpleDateFormat,
    durationFormatter: (Long?) -> String,
    project: Project? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = session.workLocationName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = dateFormatter.format(session.date),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Clock in time
            Text(
                text = "Clock in: ${dateFormatter.format(session.date)}",
                style = MaterialTheme.typography.bodySmall
            )

            // Clock out time (if available)
            session.duration?.let { duration ->
                val clockOutTime = Date(session.date.time + duration)
                Text(
                    text = "Clock out: ${dateFormatter.format(clockOutTime)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Duration
            Text(
                text = "Duration: ${durationFormatter(session.duration)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            // Project information (enhanced)
            if (project != null) {
                // Project details
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = project.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Status: ${project.status} - Progress: ${project.progress}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else if (session.projectId != null) {
                Text(
                    text = "Project ID: ${session.projectId}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (session.notes.isNotEmpty()) {
                Text(
                    text = "Notes: ${session.notes}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Dialog for selecting a work location and project to clock in
 */
@Composable
fun WorkLocationSelectionDialog(
    workLocations: List<WorkLocation>,
    projects: List<Project>,
    onDismiss: () -> Unit,
    onWorkLocationAndProjectSelected: (workLocationId: String, projectId: String?) -> Unit
) {
    var selectedWorkLocationId by remember { mutableStateOf<String?>(null) }
    var selectedProjectId by remember { mutableStateOf<String?>(null) }
    var showProjectSelection by remember { mutableStateOf(false) }

    if (!showProjectSelection) {
        // Step 1: Select work location
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Work Location") },
            text = {
                LazyColumn {
                    items(workLocations) { workLocation ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    selectedWorkLocationId = workLocation.id
                                    showProjectSelection = true
                                }
                                .padding(vertical = 8.dp),
                            color = Color.Transparent
                        ) {
                            Column {
                                Text(
                                    text = workLocation.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = workLocation.address,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (workLocation != workLocations.last()) {
                            Divider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    } else {
        // Step 2: Select project
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Project") },
            text = {
                Column {
                    Text(
                        text = "Select the project you're working on:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        selectedProjectId = null
                                        selectedWorkLocationId?.let { workLocationId ->
                                            onWorkLocationAndProjectSelected(workLocationId, null)
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                color = Color.Transparent
                            ) {
                                Text(
                                    text = "No specific project (General work)",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Divider()
                        }

                        items(projects) { project ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        selectedProjectId = project.id
                                        selectedWorkLocationId?.let { workLocationId ->
                                            onWorkLocationAndProjectSelected(workLocationId, project.id)
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                color = Color.Transparent
                            ) {
                                Column {
                                    Text(
                                        text = project.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "Status: ${project.status} - Progress: ${project.progress}%",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            if (project != projects.last()) {
                                Divider()
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = { 
                        showProjectSelection = false 
                    }) {
                        Text("Back")
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}
