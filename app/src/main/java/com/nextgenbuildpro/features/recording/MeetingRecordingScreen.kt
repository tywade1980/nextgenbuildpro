package com.nextgenbuildpro.features.recording

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextgenbuildpro.crm.service.*
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

/**
 * MeetingRecordingScreen - UI for recording meetings and extracting actionable items
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingRecordingScreen(
    meetingRecordingManager: MeetingRecordingManager,
    onNavigateBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    
    // Observe recording state
    val recordingState by meetingRecordingManager.getRecordingState().collectAsState()
    val currentRecording by meetingRecordingManager.getCurrentRecordingInfo().collectAsState()
    val processingState by meetingRecordingManager.processingState.collectAsState()
    val autoRecordCalls by meetingRecordingManager.getAutoRecordCalls().collectAsState()
    val recentMeetings by meetingRecordingManager.recentMeetings.collectAsState()
    
    // Local state
    var meetingTitle by remember { mutableStateOf("") }
    var showMeetingDialog by remember { mutableStateOf(false) }
    var selectedMeetingType by remember { mutableStateOf(MeetingType.IN_PERSON) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meeting Recording") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, "Settings")
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
            // Recording Controls Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Meeting Recording",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Auto-record calls toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Auto-record phone calls")
                        Switch(
                            checked = autoRecordCalls,
                            onCheckedChange = { meetingRecordingManager.setAutoRecordCalls(it) }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Recording status
                    when (recordingState) {
                        RecordingState.IDLE -> {
                            Button(
                                onClick = { showMeetingDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.FiberManualRecord, "Start Recording")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Recording")
                            }
                        }
                        RecordingState.RECORDING -> {
                            // Show recording info
                            currentRecording?.let { recording ->
                                Text(
                                    text = "Recording: ${recording.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                meetingRecordingManager.pauseRecording()
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Pause, "Pause")
                                    }
                                    
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val result = meetingRecordingManager.stopAndProcessMeeting()
                                                if (result.isFailure) {
                                                    errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                                                    showError = true
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Default.Stop, "Stop")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Stop")
                                    }
                                }
                            }
                        }
                        RecordingState.PAUSED -> {
                            Button(
                                onClick = {
                                    scope.launch {
                                        meetingRecordingManager.resumeRecording()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, "Resume")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Resume Recording")
                            }
                        }
                        RecordingState.PROCESSING -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(16.dp)
                            )
                        }
                    }
                    
                    // Processing status
                    if (processingState != ProcessingState.IDLE) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when (processingState) {
                                    ProcessingState.TRANSCRIBING -> "Transcribing audio..."
                                    ProcessingState.EXTRACTING -> "Extracting actionable items..."
                                    ProcessingState.CREATING_TASKS -> "Creating tasks..."
                                    ProcessingState.COMPLETED -> "Processing completed!"
                                    ProcessingState.ERROR -> "Processing error"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Recent Meetings
            Text(
                text = "Recent Meetings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (recentMeetings.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "No meetings recorded yet",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentMeetings) { meeting ->
                        MeetingCard(meeting = meeting)
                    }
                }
            }
        }
        
        // Start Meeting Dialog
        if (showMeetingDialog) {
            AlertDialog(
                onDismissRequest = { showMeetingDialog = false },
                title = { Text("Start Meeting Recording") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = meetingTitle,
                            onValueChange = { meetingTitle = it },
                            label = { Text("Meeting Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Meeting Type:")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedMeetingType == MeetingType.IN_PERSON,
                                onClick = { selectedMeetingType = MeetingType.IN_PERSON },
                                label = { Text("In-Person") }
                            )
                            FilterChip(
                                selected = selectedMeetingType == MeetingType.PHONE_CALL,
                                onClick = { selectedMeetingType = MeetingType.PHONE_CALL },
                                label = { Text("Phone Call") }
                            )
                            FilterChip(
                                selected = selectedMeetingType == MeetingType.VIDEO_CALL,
                                onClick = { selectedMeetingType = MeetingType.VIDEO_CALL },
                                label = { Text("Video Call") }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val result = meetingRecordingManager.startMeetingRecording(
                                    meetingType = selectedMeetingType,
                                    meetingTitle = meetingTitle.ifBlank { null }
                                )
                                if (result.isFailure) {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                                    showError = true
                                }
                                showMeetingDialog = false
                            }
                        }
                    ) {
                        Text("Start")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMeetingDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Error Dialog
        if (showError) {
            AlertDialog(
                onDismissRequest = { showError = false },
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    Button(onClick = { showError = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun MeetingCard(meeting: ProcessedMeeting) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = meeting.recording.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = meeting.recording.startTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (meeting.extractedTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${meeting.extractedTasks.size} actionable items extracted",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                meeting.extractedTasks.take(3).forEach { task ->
                    Text(
                        text = "• ${task.title}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
                
                if (meeting.extractedTasks.size > 3) {
                    Text(
                        text = "  ... and ${meeting.extractedTasks.size - 3} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}
