package com.nextgenbuildpro.features.leads

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nextgenbuildpro.R
import com.nextgenbuildpro.navigation.NavDestinations
import com.nextgenbuildpro.navigation.navigateSafely
import com.nextgenbuildpro.crm.data.model.ClientPhoto
import com.nextgenbuildpro.crm.data.model.Lead
import com.nextgenbuildpro.crm.data.model.LeadPhase
import com.nextgenbuildpro.crm.data.repository.LeadRepository
import com.nextgenbuildpro.crm.rememberCrmComponents
import com.nextgenbuildpro.crm.service.VoiceRecorderService
import com.nextgenbuildpro.crm.ui.getColorForLeadPhase
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
fun LeadDetailScreen(navController: NavController, leadId: String) {
    // Get CRM components
    val crmComponents = rememberCrmComponents()
    val photoRepository = crmComponents.photoRepository
    val leadsViewModel = crmComponents.leadsViewModel

    // Coroutine scope for async operations
    val coroutineScope = rememberCoroutineScope()

    // State for photos
    val photos = remember { mutableStateListOf<ClientPhoto>() }

    // Create a default lead as a fallback
    val defaultLead = Lead(
        id = leadId,
        name = "Loading Lead...",
        email = "",
        phone = "",
        address = "",
        projectType = "",
        phase = LeadPhase.CONTACTED,
        notes = "",
        urgency = "Low",
        source = "Other",
        intakeTimestamp = System.currentTimeMillis()
    )

    // State for lead (non-nullable with default value)
    var lead by remember { mutableStateOf(defaultLead) }

    // State to track if we're loading
    var isLoading by remember { mutableStateOf(true) }

    // Try to get lead from the ViewModel
    LaunchedEffect(leadId) {
        isLoading = true

        // Check if the lead is already in the ViewModel's items
        val existingLead = leadsViewModel.items.value.find { it.id == leadId }

        if (existingLead != null) {
            // If found, use it
            lead = existingLead
            isLoading = false
        } else {
            // If not found, try to fetch it from the repository
            coroutineScope.launch {
                try {
                    // This will trigger a repository fetch
                    leadsViewModel.refresh()

                    // Check again after refresh
                    val refreshedLead = leadsViewModel.items.value.find { it.id == leadId }

                    if (refreshedLead != null) {
                        lead = refreshedLead
                    } else {
                        // If still not found, use a fallback
                        lead = Lead(
                            id = leadId,
                            name = "Lead Not Found",
                            email = "unknown@example.com",
                            phone = "(555) 000-0000",
                            address = "Unknown Address",
                            projectType = "Unknown",
                            phase = LeadPhase.CONTACTED,
                            notes = "This lead could not be found in the database.",
                            urgency = "Low",
                            source = "Other",
                            intakeTimestamp = System.currentTimeMillis()
                        )
                    }
                } catch (e: Exception) {
                    // In case of error, use a fallback
                    lead = Lead(
                        id = leadId,
                        name = "Error Loading Lead",
                        email = "unknown@example.com",
                        phone = "(555) 000-0000",
                        address = "Unknown Address",
                        projectType = "Unknown",
                        phase = LeadPhase.CONTACTED,
                        notes = "Error loading lead: ${e.message}",
                        urgency = "Low",
                        source = "Other",
                        intakeTimestamp = System.currentTimeMillis()
                    )
                } finally {
                    isLoading = false
                }
            }
        }
    }

    // Show loading state while lead is being fetched
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Load photos for this lead
    LaunchedEffect(leadId) {
        coroutineScope.launch {
            photos.clear()
            photos.addAll(photoRepository.getPhotosByLeadId(leadId))
        }
    }

    Scaffold(
        topBar = {
            LeadDetailTopBar(lead = lead, navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Open actions menu */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Actions"
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
            // Lead header
            item {
                LeadHeader(lead = lead)
            }

            // Quick actions
            item {
                QuickActions(navController = navController, lead = lead)
            }

            // Contact information
            item {
                ContactInformation(lead = lead)
            }

            // Notes
            item {
                NotesSection(lead = lead, navController = navController)
            }

            // Estimates
            item {
                EstimatesSection(navController = navController, leadId = leadId)
            }

            // Photos
            item {
                PhotosSection(photos = photos, navController = navController, leadId = leadId)
            }

            // Status section
            item {
                StatusSection(lead = lead, navController = navController)
            }

            // Activity timeline
            item {
                ActivityTimeline()
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun LeadDetailTopBar(lead: Lead, navController: NavController) {
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
                    text = lead.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Status: ${lead.phase.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }

            IconButton(
                onClick = { /* Open menu */ },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun LeadHeader(lead: Lead) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(getColorForLeadPhase(lead.phase)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lead.name.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = lead.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = lead.email ?: "No email",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = lead.phone,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = getColorForLeadPhase(lead.phase).copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = lead.phase.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = getColorForLeadPhase(lead.phase),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActions(navController: NavController, lead: Lead) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                icon = Icons.Default.Call,
                text = "Call",
                onClick = { /* Make call */ }
            )
            ActionButton(
                icon = Icons.Default.Email,
                text = "Message",
                onClick = { navController.navigateSafely(NavDestinations.MESSAGES) }
            )
            ActionButton(
                icon = Icons.Default.Email,
                text = "Email",
                onClick = { /* Send email */ }
            )
            ActionButton(
                icon = Icons.Default.Schedule,
                text = "Schedule",
                onClick = { 
                    // Navigate directly to calendar event editor with lead ID
                    navController.navigateSafely("${NavDestinations.CALENDAR_EVENT_EDITOR}?leadId=${lead.id}") 
                }
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .wrapContentWidth()
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ContactInformation(lead: Lead) {
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
                text = "Contact Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = lead.phone,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = lead.email ?: "No email",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Address",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = lead.address,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun NotesSection(lead: Lead, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val leadsViewModel = rememberCrmComponents().leadsViewModel

    // State for voice recording
    var isRecording by remember { mutableStateOf(false) }
    var recordedFilePath by remember { mutableStateOf<String?>(null) }
    var transcribedText by remember { mutableStateOf<String?>(null) }

    // Voice recorder service
    val voiceRecorderService = remember { VoiceRecorderService(context) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
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
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    // Voice recording button
                    TextButton(
                        onClick = {
                            if (isRecording) {
                                // Stop recording
                                coroutineScope.launch {
                                    recordedFilePath = voiceRecorderService.stopRecording()
                                    isRecording = false

                                    // Transcribe the recording
                                    recordedFilePath?.let { filePath ->
                                        transcribedText = voiceRecorderService.transcribeAudio(filePath)

                                        // Add transcription to lead's notes
                                        transcribedText?.let { text ->
                                            leadsViewModel.addLeadNote(lead.id, "Voice Note: $text")
                                        }
                                    }
                                }
                            } else {
                                // Start recording
                                coroutineScope.launch {
                                    recordedFilePath = voiceRecorderService.startRecording()
                                    isRecording = true
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                            tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isRecording) "Stop" else "Record")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Add note button
                    TextButton(onClick = { navController.navigateSafely(NavDestinations.NOTE_EDITOR + "/" + lead.id) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Note"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Note")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Recording status
            if (isRecording) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recording in progress...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                }
            }

            if (lead.notes.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = lead.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Text(
                    text = "No notes yet. Click 'Add Note' to create one or use 'Record' for voice notes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EstimatesSection(navController: NavController, leadId: String = "") {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
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
                    text = "Estimates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { 
                    // Pass the lead ID to the estimate editor
                    navController.navigateSafely("${NavDestinations.ESTIMATE_EDITOR}/${leadId}") 
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Estimate"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sample estimate
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigateSafely(NavDestinations.ESTIMATE_DETAIL + "/1") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Built-in Office Estimate",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$7,500.00",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Surface(
                        color = Color(0xFFE0E0E0),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Draft",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityTimeline() {
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
                text = "Activity Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sample timeline items
            TimelineItem(
                icon = Icons.Default.Person,
                title = "Lead Created",
                description = "Lead was added to the system",
                time = "May 15, 2023 - 10:30 AM"
            )

            TimelineItem(
                icon = Icons.Default.Call,
                title = "Call Made",
                description = "Discussed project requirements",
                time = "May 16, 2023 - 2:15 PM"
            )

            TimelineItem(
                icon = Icons.Default.Email,
                title = "Message Sent",
                description = "Sent follow-up message about estimate",
                time = "May 17, 2023 - 9:45 AM"
            )
        }
    }
}

@Composable
fun TimelineItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    time: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

/**
 * Utility function to save a bitmap to a URI
 */
fun saveBitmapToUri(context: android.content.Context, bitmap: Bitmap): Uri {
    val fileName = "photo_${System.currentTimeMillis()}.jpg"
    val outputDir = context.cacheDir
    val outputFile = File(outputDir, fileName)

    outputFile.outputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        out.flush()
    }

    return Uri.fromFile(outputFile)
}

/**
 * Section to display photos associated with a lead
 */
@Composable
fun PhotosSection(photos: List<ClientPhoto>, navController: NavController, leadId: String = "") {
    val context = LocalContext.current
    val photoRepository = rememberCrmComponents().photoRepository
    val coroutineScope = rememberCoroutineScope()
    val mutablePhotos = remember { mutableStateListOf<ClientPhoto>() }

    // Initialize mutablePhotos with the provided photos
    LaunchedEffect(photos) {
        mutablePhotos.clear()
        mutablePhotos.addAll(photos)
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            // Convert bitmap to URI
            val uri = saveBitmapToUri(context, bitmap)

            // Upload photo
            coroutineScope.launch {
                val downloadUrl = photoRepository.uploadLeadPhoto(leadId, uri)

                // Create ClientPhoto object
                val photo = ClientPhoto(
                    id = UUID.randomUUID().toString(),
                    leadId = leadId,
                    filePath = downloadUrl,
                    fileName = "photo_${System.currentTimeMillis()}.jpg",
                    timestamp = com.nextgenbuildpro.core.DateUtils.getCurrentTimestamp(),
                    location = null,
                    description = ""
                )

                // Add photo to repository
                photoRepository.save(photo)

                // Refresh photos
                mutablePhotos.add(photo)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
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
                    text = "Photos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { 
                    // Launch camera or gallery
                    cameraLauncher.launch(null)
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Photo"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Photo")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (mutablePhotos.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mutablePhotos) { photo ->
                        PhotoItem(photo = photo, onClick = {
                            // Navigate to photo detail screen
                            navController.navigate("photo_detail/${photo.id}")
                        })
                    }
                }
            } else {
                Text(
                    text = "No photos yet. Add photos to document your project.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Individual photo item
 */
@Composable
fun PhotoItem(photo: ClientPhoto, onClick: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .size(width = 160.dp, height = 120.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photo.filePath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Project photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Location indicator if available
            if (photo.location != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Section for lead status workflow
 */
@Composable
fun StatusSection(lead: Lead, navController: NavController) {
    val crmComponents = rememberCrmComponents()
    val leadsViewModel = crmComponents.leadsViewModel
    val coroutineScope = rememberCoroutineScope()

    // State for status
    var selectedPhase by remember { mutableStateOf(lead.phase) }
    var phaseExpanded by remember { mutableStateOf(false) }

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
                text = "Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Status:",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Status chip that opens dropdown when clicked
                Surface(
                    color = getColorForLeadPhase(selectedPhase).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .clickable { phaseExpanded = true }
                ) {
                    Text(
                        text = selectedPhase.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = getColorForLeadPhase(selectedPhase),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Dropdown menu for status selection
                DropdownMenu(
                    expanded = phaseExpanded,
                    onDismissRequest = { phaseExpanded = false }
                ) {
                    LeadPhase.values().forEach { phase ->
                        DropdownMenuItem(
                            text = { Text(phase.name) },
                            onClick = {
                                // Show confirmation dialog for status change
                                // For now, just update directly
                                selectedPhase = phase
                                phaseExpanded = false

                                // Update lead phase
                                coroutineScope.launch {
                                    leadsViewModel.updateLeadPhase(lead.id, phase)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
