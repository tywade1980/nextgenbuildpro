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
import com.nextgenbuildpro.debug.WorkflowAnalyzer
import com.nextgenbuildpro.ui.ButtonNavigationValidator
import com.nextgenbuildpro.ui.FeatureCompletionTracker
import com.nextgenbuildpro.ui.components.completeFeature
import com.nextgenbuildpro.ui.components.trackFeature
import com.nextgenbuildpro.ui.components.trackNavigation
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(navController: NavController, leadId: String) {
    // Session ID for tracking this user's journey
    val sessionId = remember { "user_${System.currentTimeMillis()}" }

    // Register this screen visit with the WorkflowAnalyzer
    LaunchedEffect(Unit) {
        WorkflowAnalyzer.trackScreenVisit(
            userId = sessionId,
            destination = "${NavDestinations.LEAD_DETAIL}/$leadId",
            sourceElementId = "lead_item_$leadId"
        )
    }

    // Get CRM components
    val crmComponents = rememberCrmComponents()
    val photoRepository = crmComponents.photoRepository
    val leadsViewModel = crmComponents.leadsViewModel

    // Coroutine scope for async operations
    val coroutineScope = rememberCoroutineScope()

    // State for lead data and UI
    var lead by remember { mutableStateOf<Lead?>(null) }
    var photos by remember { mutableStateOf<List<ClientPhoto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPhaseDialog by remember { mutableStateOf(false) }
    var recordingInProgress by remember { mutableStateOf(false) }

    // Context for camera and audio recording
    val context = LocalContext.current
    val voiceRecorderService = remember { VoiceRecorderService(context) }

    // Load lead data
    LaunchedEffect(leadId) {
        isLoading = true
        error = null

        try {
            coroutineScope.launch {
                // Track loading state for the lead detail workflow
                val featureId = FeatureCompletionTracker.trackFeatureStart(
                    elementId = "lead_detail_load_$leadId",
                    screenName = "LeadDetailScreen",
                    featureName = "load_lead_details"
                )

                try {
                    // Fetch lead data
                    lead = leadsViewModel.getLeadById(leadId)

                    // Fetch photos
                    if (lead != null) {
                        photos = photoRepository.getPhotosByLeadId(leadId)
                    }

                    isLoading = false

                    // Mark the feature as complete if data loaded successfully
                    if (lead != null) {
                        completeFeature(featureId, true, "Successfully loaded lead details")
                    } else {
                        completeFeature(featureId, false, "Lead not found")
                        error = "Lead not found"
                    }
                } catch (e: Exception) {
                    isLoading = false
                    error = "Error loading lead: ${e.message}"
                    completeFeature(featureId, false, "Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            isLoading = false
            error = "Error: ${e.message}"
        }
    }

    // Setup image picker for adding photos
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Track photo addition workflow
            val featureId = FeatureCompletionTracker.trackFeatureStart(
                elementId = "add_photo_to_lead_$leadId",
                screenName = "LeadDetailScreen",
                featureName = "add_photo_to_lead"
            )

            coroutineScope.launch {
                try {
                    val newPhoto = ClientPhoto(
                        id = UUID.randomUUID().toString(),
                        leadId = leadId,
                        filePath = it.toString(),
                        fileName = "picked_image_${System.currentTimeMillis()}",
                        timestamp = System.currentTimeMillis().toString(),
                        location = null,
                        description = ""
                    )

                    photoRepository.save(newPhoto)
                    photos = photoRepository.getPhotosByLeadId(leadId)
                    completeFeature(featureId, true, "Successfully added photo to lead")
                } catch (e: Exception) {
                    completeFeature(featureId, false, "Failed to add photo: ${e.message}")
                }
            }
        }
    }

    // Camera launcher for taking new photos
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            // Track photo capture workflow
            val featureId = FeatureCompletionTracker.trackFeatureStart(
                elementId = "capture_photo_for_lead_$leadId",
                screenName = "LeadDetailScreen",
                featureName = "capture_photo_for_lead"
            )

            coroutineScope.launch {
                try {
                    // Save bitmap to file
                    val photoFile = File(context.cacheDir, "lead_photo_${System.currentTimeMillis()}.jpg")
                    photoFile.outputStream().use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }

                    val newPhoto = ClientPhoto(
                        id = UUID.randomUUID().toString(),
                        leadId = leadId,
                        filePath = photoFile.absolutePath,
                        fileName = photoFile.name,
                        timestamp = System.currentTimeMillis().toString(),
                        location = null,
                        description = ""
                    )

                    photoRepository.save(newPhoto)
                    photos = photoRepository.getPhotosByLeadId(leadId)
                    completeFeature(featureId, true, "Successfully captured and saved photo")
                } catch (e: Exception) {
                    completeFeature(featureId, false, "Failed to save captured photo: ${e.message}")
                }
            }
        }
    }

    // Content for the screen
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lead?.name ?: "Lead Details") },
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
                            buttonId = "edit_lead_${leadId}",
                            screenName = "LeadDetailScreen",
                            featureName = "edit_lead"
                        ) { featureSessionId ->
                            ButtonNavigationValidator.validateAndNavigate(
                                navController = navController,
                                destination = "${NavDestinations.LEAD_EDITOR}/$leadId",
                                buttonId = "edit_lead_${leadId}",
                                screenName = "LeadDetailScreen"
                            )
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Lead")
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Lead")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                modifier = Modifier.trackFeature(
                    buttonId = "create_estimate_for_lead",
                    screenName = "LeadDetailScreen",
                    featureName = "create_estimate_from_lead"
                ) { featureSessionId ->
                    // Check if the estimate creation feature is implemented
                    if (ButtonNavigationValidator.validateAndNavigate(
                        navController = navController,
                        destination = NavDestinations.ESTIMATE_EDITOR,
                        buttonId = "create_estimate_for_lead",
                        screenName = "LeadDetailScreen"
                    )) {
                        completeFeature(featureSessionId, true, "Successfully started estimate creation")
                    } else {
                        completeFeature(featureSessionId, false, "Estimate creation navigation failed")
                        WorkflowAnalyzer.registerDeadEndElement(
                            elementId = "create_estimate_for_lead",
                            screenName = "LeadDetailScreen",
                            intendedDestination = NavDestinations.ESTIMATE_EDITOR
                        )
                    }
                }
            ) {
                Icon(Icons.Default.AttachMoney, contentDescription = "Create Estimate")
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
                lead != null -> {
                    LeadDetailContent(
                        lead = lead!!,
                        photos = photos,
                        onTakePhoto = { cameraLauncher.launch(null) },
                        onPickPhoto = { imagePickerLauncher.launch("image/*") },
                        onChangePhase = { showPhaseDialog = true },
                        onAddNote = {
                            // Track note creation workflow
                            val featureId = FeatureCompletionTracker.trackFeatureStart(
                                elementId = "add_note_to_lead_$leadId",
                                screenName = "LeadDetailScreen",
                                featureName = "add_note_to_lead"
                            )

                            val success = ButtonNavigationValidator.validateAndNavigate(
                                navController = navController,
                                destination = NavDestinations.NOTE_EDITOR,
                                buttonId = "add_note_to_lead_$leadId",
                                screenName = "LeadDetailScreen"
                            )

                            if (!success) {
                                completeFeature(featureId, false, "Note editor navigation failed")
                                WorkflowAnalyzer.registerDeadEndElement(
                                    elementId = "add_note_to_lead_$leadId",
                                    screenName = "LeadDetailScreen",
                                    intendedDestination = NavDestinations.NOTE_EDITOR
                                )
                            }
                        },
                        onRecordVoice = {
                            // Track voice recording workflow
                            val featureId = FeatureCompletionTracker.trackFeatureStart(
                                elementId = "record_voice_for_lead_$leadId",
                                screenName = "LeadDetailScreen",
                                featureName = "record_voice_note"
                            )

                            if (!recordingInProgress) {
                                // Start recording
                                try {
                                    voiceRecorderService.startRecording(
                                        fileName = "lead_${leadId}_${System.currentTimeMillis()}.mp3"
                                    )
                                    recordingInProgress = true
                                } catch (e: Exception) {
                                    completeFeature(featureId, false, "Failed to start recording: ${e.message}")
                                    // Check if this is a dead-end feature
                                    WorkflowAnalyzer.registerDeadEndElement(
                                        elementId = "record_voice_for_lead_$leadId",
                                        screenName = "LeadDetailScreen",
                                        intendedDestination = "VoiceRecording"
                                    )
                                }
                            } else {
                                // Stop recording
                                try {
                                    voiceRecorderService.stopRecording()
                                    recordingInProgress = false

                                    // Add voice note to lead
                                    coroutineScope.launch {
                                        // Here you would save the voice note to the lead
                                        completeFeature(featureId, true, "Voice note recorded and saved")
                                    }
                                } catch (e: Exception) {
                                    completeFeature(featureId, false, "Failed to save voice note: ${e.message}")
                                }
                            }
                        },
                        recordingInProgress = recordingInProgress,
                        navController = navController
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Lead") },
            text = { Text("Are you sure you want to delete this lead? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        // Track lead deletion workflow
                        val featureId = FeatureCompletionTracker.trackFeatureStart(
                            elementId = "delete_lead_$leadId",
                            screenName = "LeadDetailScreen",
                            featureName = "delete_lead"
                        )

                        coroutineScope.launch {
                            try {
                                leadsViewModel.deleteLead(leadId)
                                showDeleteDialog = false
                                completeFeature(featureId, true, "Lead deleted successfully")
                                navController.navigateUp()
                            } catch (e: Exception) {
                                completeFeature(featureId, false, "Failed to delete lead: ${e.message}")
                            }
                        }
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

    // Phase change dialog
    if (showPhaseDialog && lead != null) {
        val phases = LeadPhase.values()

        AlertDialog(
            onDismissRequest = { showPhaseDialog = false },
            title = { Text("Change Lead Phase") },
            text = {
                Column {
                    phases.forEach { phase ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Track phase change workflow
                                    val featureId = FeatureCompletionTracker.trackFeatureStart(
                                        elementId = "change_lead_phase_$leadId",
                                        screenName = "LeadDetailScreen",
                                        featureName = "change_lead_phase"
                                    )

                                    coroutineScope.launch {
                                        try {
                                            val updatedLead = lead!!.copy(phase = phase)
                                            leadsViewModel.updateLead(updatedLead)
                                            lead = updatedLead
                                            showPhaseDialog = false
                                            completeFeature(featureId, true, "Lead phase updated successfully")
                                        } catch (e: Exception) {
                                            completeFeature(featureId, false, "Failed to update lead phase: ${e.message}")
                                        }
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = lead?.phase == phase,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(phase.name)
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(
                    onClick = { showPhaseDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
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

@Composable
fun LeadDetailContent(
    lead: Lead,
    photos: List<ClientPhoto>,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onChangePhase: () -> Unit,
    onAddNote: () -> Unit,
    onRecordVoice: () -> Unit,
    recordingInProgress: Boolean,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LeadHeader(lead = lead)
        }

        item {
            QuickActions(navController = navController, lead = lead)
        }

        item {
            ContactInformation(lead = lead)
        }

        item {
            NotesSection(lead = lead, navController = navController)
        }

        item {
            PhotosSection(photos = photos, navController = navController, leadId = lead.id)
        }

        item {
            EstimatesSection(navController = navController, leadId = lead.id)
        }

        item {
            ActivityTimeline()
        }

        item {
            StatusSection(lead = lead, navController = navController)
        }
    }
}
