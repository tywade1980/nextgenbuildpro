package com.nextgenbuildpro.features.leads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.crm.rememberCrmComponents
import kotlinx.coroutines.launch

/**
 * Screen for creating or editing a note for a lead
 * @param navController Navigation controller
 * @param leadId The ID of the lead to add a note to
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(navController: NavController, leadId: String) {
    val crmComponents = rememberCrmComponents()
    val leadsViewModel = crmComponents.leadsViewModel
    val coroutineScope = rememberCoroutineScope()
    
    // State for form fields
    var noteText by remember { mutableStateOf("") }
    
    // State for validation
    var noteError by remember { mutableStateOf<String?>(null) }
    
    // State for saving
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    // Lead name for display
    var leadName by remember { mutableStateOf("") }
    
    // Load lead data to get the name
    LaunchedEffect(leadId) {
        leadsViewModel.selectLead(com.nextgenbuildpro.crm.data.model.Lead(
            id = leadId,
            name = "",
            phone = "",
            email = null,
            address = "",
            projectType = "",
            phase = com.nextgenbuildpro.crm.data.model.LeadPhase.CONTACTED,
            notes = "",
            urgency = "",
            source = "",
            intakeTimestamp = 0,
            attachments = emptyList()
        ))
        
        leadsViewModel.selectedLead.collect { lead ->
            lead?.let {
                leadName = it.name
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Add Note for ${if (leadName.isNotBlank()) leadName else "Lead"}") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Validate form
                            var isValid = true
                            
                            if (noteText.isBlank()) {
                                noteError = "Note text is required"
                                isValid = false
                            } else {
                                noteError = null
                            }
                            
                            if (isValid) {
                                // Save note
                                coroutineScope.launch {
                                    isSaving = true
                                    
                                    leadsViewModel.addLeadNote(leadId, noteText)
                                    
                                    isSaving = false
                                    showSuccessMessage = true
                                    
                                    // Navigate back after a short delay
                                    kotlinx.coroutines.delay(1500)
                                    navController.navigateUp()
                                }
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Note text field
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    isError = noteError != null,
                    supportingText = { noteError?.let { Text(it) } },
                    minLines = 8
                )
                
                // Hint text
                Text(
                    text = "Add details about your interaction with this lead, including any important information discussed or action items.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Spacer at the bottom for better scrolling
                Spacer(modifier = Modifier.height(80.dp))
            }
            
            // Success message
            if (showSuccessMessage) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Note added successfully",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Loading indicator
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}