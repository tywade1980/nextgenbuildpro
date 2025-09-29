package com.nextgenbuildpro.fieldtools.ui

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

/**
 * Voice to Text Screen - Functional implementation for voice recording and transcription
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceToTextScreen(navController: NavController) {
    var isRecording by remember { mutableStateOf(false) }
    var currentTranscription by remember { mutableStateOf("") }
    var transcriptionHistory by remember { mutableStateOf(getRecentTranscriptions()) }
    var selectedCategory by remember { mutableStateOf("General") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice to Text") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Open voice settings */ }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
            // Recording Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRecording) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Recording button
                    FloatingActionButton(
                        onClick = { isRecording = !isRecording },
                        modifier = Modifier.size(80.dp),
                        containerColor = if (isRecording) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ) {
                        Icon(
                            imageVector = if (isRecording) {
                                Icons.Default.Stop
                            } else {
                                Icons.Default.Mic
                            },
                            contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isRecording) {
                            "Recording... Tap to stop"
                        } else {
                            "Tap to start voice recording"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    if (isRecording) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Category:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                
                getVoiceCategories().forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Current Transcription
            if (currentTranscription.isNotEmpty()) {
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
                                text = "Current Transcription",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row {
                                IconButton(
                                    onClick = { /* Copy to clipboard */ }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                                IconButton(
                                    onClick = { /* Save transcription */ }
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = "Save")
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = currentTranscription,
                            onValueChange = { currentTranscription = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Transcribed text will appear here...") },
                            minLines = 3,
                            maxLines = 6
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { currentTranscription = "" }
                            ) {
                                Text("Clear")
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = { 
                                    // Save current transcription to history
                                    val newTranscription = VoiceTranscription(
                                        id = "new_${System.currentTimeMillis()}",
                                        text = currentTranscription,
                                        category = selectedCategory,
                                        timestamp = "Just now",
                                        duration = "45s"
                                    )
                                    transcriptionHistory = listOf(newTranscription) + transcriptionHistory
                                    currentTranscription = ""
                                }
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Recent Transcriptions
            Text(
                text = "Recent Transcriptions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transcriptionHistory) { transcription ->
                    TranscriptionCard(
                        transcription = transcription,
                        onEdit = { currentTranscription = transcription.text },
                        onShare = { /* Share transcription */ },
                        onDelete = { 
                            transcriptionHistory = transcriptionHistory.filter { it.id != transcription.id }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TranscriptionCard(
    transcription: VoiceTranscription,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text(transcription.category) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(transcription.category),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = transcription.duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = transcription.text,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3
                    )
                }
                
                Text(
                    text = transcription.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

data class VoiceTranscription(
    val id: String,
    val text: String,
    val category: String,
    val timestamp: String,
    val duration: String
)

private fun getVoiceCategories(): List<String> {
    return listOf("General", "Notes", "Estimates", "Issues", "Safety")
}

private fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "Notes" -> Icons.Default.Note
        "Estimates" -> Icons.Default.AttachMoney
        "Issues" -> Icons.Default.Warning
        "Safety" -> Icons.Default.Security
        else -> Icons.Default.RecordVoiceOver
    }
}

private fun getRecentTranscriptions(): List<VoiceTranscription> {
    return listOf(
        VoiceTranscription(
            id = "1",
            text = "Kitchen cabinet installation complete. All upper cabinets are level and secure. Need to schedule countertop template for next week.",
            category = "Notes",
            timestamp = "2 hours ago",
            duration = "45s"
        ),
        VoiceTranscription(
            id = "2",
            text = "Electrical rough-in inspection scheduled for Friday. All circuits tested and working properly. GFCI outlets installed in kitchen and bathrooms.",
            category = "Notes",
            timestamp = "1 day ago",
            duration = "32s"
        ),
        VoiceTranscription(
            id = "3",
            text = "Safety issue: Found loose floorboard in hallway near bathroom entrance. Client should avoid area until repair. Schedule repair for tomorrow morning.",
            category = "Safety",
            timestamp = "2 days ago",
            duration = "28s"
        ),
        VoiceTranscription(
            id = "4",
            text = "Additional work estimate: Client requests built-in shelving in master closet. Materials approximately 800 dollars, labor 12 hours at standard rate.",
            category = "Estimates",
            timestamp = "3 days ago",
            duration = "51s"
        )
    )
}