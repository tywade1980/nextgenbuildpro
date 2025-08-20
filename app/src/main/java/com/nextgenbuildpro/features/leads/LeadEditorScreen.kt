package com.nextgenbuildpro.features.leads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.crm.data.model.Lead
import com.nextgenbuildpro.crm.data.model.LeadPhase
import com.nextgenbuildpro.crm.data.model.LeadSource
import com.nextgenbuildpro.crm.data.model.MediaAttachment
import com.nextgenbuildpro.crm.rememberCrmComponents
import com.nextgenbuildpro.navigation.NavDestinations
import com.nextgenbuildpro.navigation.navigateSafely
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Screen for creating or editing a lead
 * @param navController Navigation controller
 * @param leadId Optional lead ID. If provided, the screen will load the lead for editing.
 *               If null, a new lead will be created.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadEditorScreen(navController: NavController, leadId: String? = null) {
    val crmComponents = rememberCrmComponents()
    val leadsViewModel = crmComponents.leadsViewModel
    val coroutineScope = rememberCoroutineScope()

    // State for form fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var projectType by remember { mutableStateOf("") }
    var phase by remember { mutableStateOf(LeadPhase.CONTACTED) }
    var notes by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf("Medium") }
    var source by remember { mutableStateOf(LeadSource.WEBSITE.displayName) }

    // State for validation
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    // State for saving
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    // Load lead data if editing an existing lead
    LaunchedEffect(leadId) {
        if (leadId != null) {
            // Collect the selected lead from the ViewModel
            leadsViewModel.selectLead(Lead(id = leadId, name = "", phone = "", email = null, 
                address = "", projectType = "", phase = LeadPhase.CONTACTED, notes = "", 
                urgency = "", source = "", intakeTimestamp = 0, attachments = emptyList()))

            leadsViewModel.selectedLead.collect { lead ->
                lead?.let {
                    name = it.name
                    phone = it.phone
                    email = it.email ?: ""
                    address = it.address
                    projectType = it.projectType
                    phase = it.phase
                    notes = it.notes
                    urgency = it.urgency
                    source = it.source
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (leadId == null) "Create Lead" else "Edit Lead") },
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

                            if (name.isBlank()) {
                                nameError = "Name is required"
                                isValid = false
                            } else {
                                nameError = null
                            }

                            if (phone.isBlank()) {
                                phoneError = "Phone is required"
                                isValid = false
                            } else {
                                phoneError = null
                            }

                            if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Invalid email format"
                                isValid = false
                            } else {
                                emailError = null
                            }

                            if (isValid) {
                                // Save lead
                                coroutineScope.launch {
                                    isSaving = true

                                    val lead = Lead(
                                        id = leadId ?: UUID.randomUUID().toString(),
                                        name = name,
                                        phone = phone,
                                        email = if (email.isBlank()) null else email,
                                        address = address,
                                        projectType = projectType,
                                        phase = phase,
                                        notes = notes,
                                        urgency = urgency,
                                        source = source,
                                        intakeTimestamp = System.currentTimeMillis(),
                                        attachments = emptyList()
                                    )

                                    if (leadId == null) {
                                        leadsViewModel.createLead(lead)
                                    } else {
                                        leadsViewModel.updateLead(lead)
                                    }

                                    isSaving = false

                                    // Check if there was an error during save
                                    val error = leadsViewModel.error.value
                                    if (error == null) {
                                        // No error means success
                                        showSuccessMessage = true
                                        // Navigate to leads screen after a short delay
                                        kotlinx.coroutines.delay(1500)
                                        navController.navigate(NavDestinations.LEADS) {
                                            // Clear the back stack so user can't go back to the editor
                                            popUpTo(NavDestinations.LEADS) { inclusive = true }
                                        }
                                    } else {
                                        // Show error message
                                        showSuccessMessage = false
                                    }
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
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } }
                )

                // Phone field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone*") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError != null,
                    supportingText = { phoneError?.let { Text(it) } }
                )

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it) } }
                )

                // Address field
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Project Type field
                OutlinedTextField(
                    value = projectType,
                    onValueChange = { projectType = it },
                    label = { Text("Project Type") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Lead Phase dropdown
                Text(
                    text = "Lead Phase",
                    style = MaterialTheme.typography.bodyMedium
                )
                var phaseExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = phaseExpanded,
                    onExpandedChange = { phaseExpanded = it }
                ) {
                    OutlinedTextField(
                        value = phase.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = phaseExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = phaseExpanded,
                        onDismissRequest = { phaseExpanded = false }
                    ) {
                        LeadPhase.values().forEach { phaseOption ->
                            DropdownMenuItem(
                                text = { Text(phaseOption.name) },
                                onClick = {
                                    phase = phaseOption
                                    phaseExpanded = false
                                }
                            )
                        }
                    }
                }

                // Urgency dropdown
                Text(
                    text = "Urgency",
                    style = MaterialTheme.typography.bodyMedium
                )
                var urgencyExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = urgencyExpanded,
                    onExpandedChange = { urgencyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = urgency,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = urgencyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = urgencyExpanded,
                        onDismissRequest = { urgencyExpanded = false }
                    ) {
                        listOf("Low", "Medium", "High").forEach { urgencyOption ->
                            DropdownMenuItem(
                                text = { Text(urgencyOption) },
                                onClick = {
                                    urgency = urgencyOption
                                    urgencyExpanded = false
                                }
                            )
                        }
                    }
                }

                // Source dropdown
                Text(
                    text = "Lead Source",
                    style = MaterialTheme.typography.bodyMedium
                )
                var sourceExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = sourceExpanded,
                    onExpandedChange = { sourceExpanded = it }
                ) {
                    OutlinedTextField(
                        value = source,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = sourceExpanded,
                        onDismissRequest = { sourceExpanded = false }
                    ) {
                        LeadSource.values().forEach { sourceOption ->
                            DropdownMenuItem(
                                text = { Text(sourceOption.displayName) },
                                onClick = {
                                    source = sourceOption.displayName
                                    sourceExpanded = false
                                }
                            )
                        }
                    }
                }

                // Notes field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    minLines = 3
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
                        text = if (leadId == null) "Lead created successfully" else "Lead updated successfully",
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
