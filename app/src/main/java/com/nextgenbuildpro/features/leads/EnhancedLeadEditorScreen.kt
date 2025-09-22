package com.nextgenbuildpro.features.leads

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.core.CoreModule
import com.nextgenbuildpro.core.services.AutoFillContext
import com.nextgenbuildpro.core.services.AutoFillSuggestion
import com.nextgenbuildpro.core.services.FormFieldType
import com.nextgenbuildpro.crm.data.model.Lead
import com.nextgenbuildpro.crm.data.model.LeadPhase
import com.nextgenbuildpro.crm.data.model.LeadSource
import com.nextgenbuildpro.crm.rememberCrmComponents
import com.nextgenbuildpro.navigation.NavDestinations
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Enhanced Lead Editor Screen with Auto-Fill Capabilities
 * @param navController Navigation controller
 * @param leadId Optional lead ID. If provided, the screen will load the lead for editing.
 *               If null, a new lead will be created.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedLeadEditorScreen(navController: NavController, leadId: String? = null) {
    val crmComponents = rememberCrmComponents()
    val leadsViewModel = crmComponents.leadsViewModel
    val coroutineScope = rememberCoroutineScope()

    // Auto-fill service
    val autoFillService = remember { 
        try {
            CoreModule.getAutoFillService()
        } catch (e: Exception) {
            null
        }
    }

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

    // Auto-fill states
    var showAutoFillSuggestions by remember { mutableStateOf(false) }
    var currentField by remember { mutableStateOf<FormFieldType?>(null) }
    var autoFillSuggestions by remember { mutableStateOf<List<AutoFillSuggestion>>(emptyList()) }
    var isAutoFilling by remember { mutableStateOf(false) }

    // Error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    // Loading and saving states
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    // Auto-fill context
    val autoFillContext = remember {
        AutoFillContext(
            contextId = leadId ?: "new_lead",
            formType = "lead_editor",
            userId = "current_user",
            metadata = mapOf("module" to "crm", "screen" to "lead_editor")
        )
    }

    // Load existing lead if editing
    LaunchedEffect(leadId) {
        if (leadId != null) {
            isLoading = true
            // In a real implementation, this would load the lead data
            isLoading = false
        }
    }

    // Auto-fill function
    fun performAutoFill(fieldType: FormFieldType) {
        if (autoFillService == null) return
        
        coroutineScope.launch {
            isAutoFilling = true
            currentField = fieldType
            
            try {
                val suggestions = autoFillService.getAutoFillSuggestions(fieldType, autoFillContext)
                autoFillSuggestions = suggestions
                showAutoFillSuggestions = suggestions.isNotEmpty()
            } catch (e: Exception) {
                // Handle error
                showAutoFillSuggestions = false
            } finally {
                isAutoFilling = false
            }
        }
    }

    // Apply auto-fill suggestion
    fun applySuggestion(suggestion: AutoFillSuggestion) {
        when (currentField) {
            FormFieldType.NAME -> name = suggestion.value.toString()
            FormFieldType.PHONE -> phone = suggestion.value.toString()
            FormFieldType.EMAIL -> email = suggestion.value.toString()
            FormFieldType.ADDRESS -> address = suggestion.value.toString()
            FormFieldType.PROJECT_TYPE -> projectType = suggestion.value.toString()
            FormFieldType.NOTES -> notes = suggestion.value.toString()
            FormFieldType.SOURCE -> source = suggestion.value.toString()
            else -> {}
        }
        
        // Learn from user selection
        coroutineScope.launch {
            currentField?.let { fieldType ->
                autoFillService?.learnFromUserSelection(fieldType, autoFillContext, suggestion.value)
            }
        }
        
        showAutoFillSuggestions = false
        currentField = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (leadId == null) "New Lead" else "Edit Lead") 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Auto-fill all button
                    if (autoFillService != null) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    isAutoFilling = true
                                    try {
                                        // Auto-fill common fields
                                        val namesuggestions = autoFillService.getAutoFillSuggestions(FormFieldType.NAME, autoFillContext)
                                        namesuggestions.firstOrNull()?.let { name = it.value.toString() }
                                        
                                        val phonesuggestions = autoFillService.getAutoFillSuggestions(FormFieldType.PHONE, autoFillContext)
                                        phonesuggestions.firstOrNull()?.let { phone = it.value.toString() }
                                        
                                        val emailsuggestions = autoFillService.getAutoFillSuggestions(FormFieldType.EMAIL, autoFillContext)
                                        emailsuggestions.firstOrNull()?.let { email = it.value.toString() }
                                        
                                        val projectsuggestions = autoFillService.getAutoFillSuggestions(FormFieldType.PROJECT_TYPE, autoFillContext)
                                        projectsuggestions.firstOrNull()?.let { projectType = it.value.toString() }
                                    } finally {
                                        isAutoFilling = false
                                    }
                                }
                            },
                            enabled = !isAutoFilling
                        ) {
                            if (isAutoFilling) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = "Auto Fill"
                                )
                            }
                        }
                    }
                    
                    // Save button
                    IconButton(
                        onClick = {
                            // Validation logic
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
                                        showSuccessMessage = true
                                        kotlinx.coroutines.delay(1500)
                                        navController.navigate(NavDestinations.LEADS) {
                                            popUpTo(NavDestinations.LEADS) { inclusive = true }
                                        }
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
                // Auto-fill suggestions card
                if (showAutoFillSuggestions && autoFillSuggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                    text = "Auto-fill Suggestions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { showAutoFillSuggestions = false }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Hide suggestions"
                                    )
                                }
                            }
                            
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(autoFillSuggestions) { suggestion ->
                                    SuggestionItem(
                                        suggestion = suggestion,
                                        onClick = { applySuggestion(suggestion) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Name field with auto-fill
                AutoFillTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name*",
                    isError = nameError != null,
                    errorText = nameError,
                    onAutoFillClick = { performAutoFill(FormFieldType.NAME) },
                    autoFillEnabled = autoFillService != null
                )

                // Phone field with auto-fill
                AutoFillTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone*",
                    isError = phoneError != null,
                    errorText = phoneError,
                    keyboardType = KeyboardType.Phone,
                    onAutoFillClick = { performAutoFill(FormFieldType.PHONE) },
                    autoFillEnabled = autoFillService != null
                )

                // Email field with auto-fill
                AutoFillTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    isError = emailError != null,
                    errorText = emailError,
                    keyboardType = KeyboardType.Email,
                    onAutoFillClick = { performAutoFill(FormFieldType.EMAIL) },
                    autoFillEnabled = autoFillService != null
                )

                // Address field with auto-fill
                AutoFillTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Address",
                    onAutoFillClick = { performAutoFill(FormFieldType.ADDRESS) },
                    autoFillEnabled = autoFillService != null
                )

                // Project Type field with auto-fill
                AutoFillTextField(
                    value = projectType,
                    onValueChange = { projectType = it },
                    label = "Project Type",
                    onAutoFillClick = { performAutoFill(FormFieldType.PROJECT_TYPE) },
                    autoFillEnabled = autoFillService != null
                )

                // Phase dropdown
                var phaseExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = phaseExpanded,
                    onExpandedChange = { phaseExpanded = !phaseExpanded }
                ) {
                    OutlinedTextField(
                        value = phase.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Phase") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = phaseExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = phaseExpanded,
                        onDismissRequest = { phaseExpanded = false }
                    ) {
                        LeadPhase.values().forEach { leadPhase ->
                            DropdownMenuItem(
                                text = { Text(leadPhase.displayName) },
                                onClick = {
                                    phase = leadPhase
                                    phaseExpanded = false
                                }
                            )
                        }
                    }
                }

                // Notes field with auto-fill
                AutoFillTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes",
                    minLines = 3,
                    onAutoFillClick = { performAutoFill(FormFieldType.NOTES) },
                    autoFillEnabled = autoFillService != null
                )

                // Urgency dropdown
                var urgencyExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = urgencyExpanded,
                    onExpandedChange = { urgencyExpanded = !urgencyExpanded }
                ) {
                    OutlinedTextField(
                        value = urgency,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Urgency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = urgencyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = urgencyExpanded,
                        onDismissRequest = { urgencyExpanded = false }
                    ) {
                        listOf("Low", "Medium", "High", "Urgent").forEach { urgencyLevel ->
                            DropdownMenuItem(
                                text = { Text(urgencyLevel) },
                                onClick = {
                                    urgency = urgencyLevel
                                    urgencyExpanded = false
                                }
                            )
                        }
                    }
                }

                // Source dropdown with auto-fill
                var sourceExpanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = sourceExpanded,
                        onExpandedChange = { sourceExpanded = !sourceExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = source,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Source") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = sourceExpanded,
                            onDismissRequest = { sourceExpanded = false }
                        ) {
                            LeadSource.values().forEach { leadSource ->
                                DropdownMenuItem(
                                    text = { Text(leadSource.displayName) },
                                    onClick = {
                                        source = leadSource.displayName
                                        sourceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    if (autoFillService != null) {
                        IconButton(
                            onClick = { performAutoFill(FormFieldType.SOURCE) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Auto-fill source"
                            )
                        }
                    }
                }
            }

            // Loading indicator
            if (isLoading || isSaving) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Success message
            if (showSuccessMessage) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text("Lead saved successfully!")
                }
            }
        }
    }
}

@Composable
fun AutoFillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    onAutoFillClick: () -> Unit = {},
    autoFillEnabled: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            isError = isError,
            supportingText = { errorText?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            minLines = minLines
        )
        
        if (autoFillEnabled) {
            IconButton(
                onClick = onAutoFillClick
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = "Auto-fill $label"
                )
            }
        }
    }
}

@Composable
fun SuggestionItem(
    suggestion: AutoFillSuggestion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = suggestion.displayText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "From: ${suggestion.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Confidence: ${(suggestion.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}