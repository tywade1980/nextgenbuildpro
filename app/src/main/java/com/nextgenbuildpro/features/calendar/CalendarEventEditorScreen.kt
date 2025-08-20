package com.nextgenbuildpro.features.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.crm.data.repository.LeadRepository
import com.nextgenbuildpro.crm.rememberCrmComponents
import com.nextgenbuildpro.features.calendar.models.CalendarEvent
import com.nextgenbuildpro.features.calendar.models.EventType
import com.nextgenbuildpro.receptionist.repository.CalendarEventRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for creating or editing calendar events
 * @param navController Navigation controller
 * @param leadId Optional lead ID. If provided, the screen will pre-populate lead information.
 * @param eventId Optional event ID. If provided, the screen will load and edit an existing event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarEventEditorScreen(
    navController: NavController,
    leadId: String? = null,
    eventId: String? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val crmComponents = rememberCrmComponents()
    val leadsViewModel = crmComponents.leadsViewModel
    val calendarEventRepository = CalendarEventRepository()
    val notificationService = crmComponents.notificationService

    // State for lead information
    var leadName by remember { mutableStateOf("") }
    var leadPhone by remember { mutableStateOf("") }
    var leadEmail by remember { mutableStateOf("") }

    // State for form fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Office") }
    var eventType by remember { mutableStateOf(EventType.MEETING) }

    // State for date and time
    var selectedDate by remember { mutableStateOf(Date()) }
    var startHour by remember { mutableStateOf(9) }
    var startMinute by remember { mutableStateOf(0) }
    var durationHours by remember { mutableStateOf(1) }
    var durationMinutes by remember { mutableStateOf(0) }

    // State for available time slots
    var availableTimeSlots by remember { mutableStateOf<List<Date>>(emptyList()) }
    var selectedTimeSlot by remember { mutableStateOf<Date?>(null) }

    // State for validation
    var titleError by remember { mutableStateOf<String?>(null) }

    // State for saving
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    // State for notification
    // This tracks whether a notification has been sent to the lead
    var notificationSent by remember { mutableStateOf(false) }

    // State for editing
    var isEditing by remember { mutableStateOf(false) }
    var existingEvent by remember { mutableStateOf<CalendarEvent?>(null) }

    // Reset notification state when component is first rendered
    LaunchedEffect(Unit) {
        notificationSent = false
    }

    // Load lead data if leadId is provided
    LaunchedEffect(leadId) {
        if (leadId != null) {
            // Get lead from repository
            val lead = leadsViewModel.items.value.find { it.id == leadId }
            lead?.let {
                leadName = it.name
                leadPhone = it.phone
                leadEmail = it.email ?: ""

                // Pre-populate title with lead name
                title = "Meeting with ${it.name}"
                description = "Initial consultation for ${it.name}"
            }
        }
    }

    // Load existing event if eventId is provided
    LaunchedEffect(eventId) {
        if (eventId != null) {
            calendarEventRepository.getById(eventId)?.let { event ->
                existingEvent = event
                isEditing = true

                // Populate form fields with event data
                title = event.title
                description = event.description
                location = event.location
                eventType = event.type

                // Set date and time
                selectedDate = event.startTime
                val calendar = Calendar.getInstance()
                calendar.time = event.startTime
                startHour = calendar.get(Calendar.HOUR_OF_DAY)
                startMinute = calendar.get(Calendar.MINUTE)

                // Calculate duration
                val durationMillis = event.endTime.time - event.startTime.time
                val durationMinutesTotal = (durationMillis / (1000 * 60)).toInt()
                durationHours = durationMinutesTotal / 60
                durationMinutes = durationMinutesTotal % 60

                // If this event is associated with a lead, load lead data
                event.leadId?.let { leadId ->
                    val lead = leadsViewModel.items.value.find { it.id == leadId }
                    lead?.let {
                        leadName = it.name
                        leadPhone = it.phone
                        leadEmail = it.email ?: ""
                    }
                }
            }
        }
    }

    // Load available time slots when date or duration changes
    LaunchedEffect(selectedDate, durationHours, durationMinutes) {
        // Only load available time slots if we're not editing an existing event
        if (!isEditing) {
            val durationInMinutes = (durationHours * 60) + durationMinutes
            availableTimeSlots = calendarEventRepository.findAvailableTimeSlots(selectedDate, durationInMinutes)

            // If we have available slots and no time slot is selected, select the first one
            if (availableTimeSlots.isNotEmpty() && selectedTimeSlot == null) {
                selectedTimeSlot = availableTimeSlots.first()
                val calendar = Calendar.getInstance()
                calendar.time = selectedTimeSlot!!
                startHour = calendar.get(Calendar.HOUR_OF_DAY)
                startMinute = calendar.get(Calendar.MINUTE)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (isEditing) "Edit Meeting" else "Schedule Meeting") },
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

                            if (title.isBlank()) {
                                titleError = "Title is required"
                                isValid = false
                            } else {
                                titleError = null
                            }

                            if (isValid) {
                                // Save event
                                coroutineScope.launch {
                                    isSaving = true

                                    // Calculate start and end times
                                    val calendar = Calendar.getInstance()
                                    calendar.time = selectedDate
                                    calendar.set(Calendar.HOUR_OF_DAY, startHour)
                                    calendar.set(Calendar.MINUTE, startMinute)
                                    calendar.set(Calendar.SECOND, 0)
                                    calendar.set(Calendar.MILLISECOND, 0)
                                    val startTime = calendar.time

                                    calendar.add(Calendar.HOUR_OF_DAY, durationHours)
                                    calendar.add(Calendar.MINUTE, durationMinutes)
                                    val endTime = calendar.time

                                    // Create or update event
                                    val event = if (isEditing && existingEvent != null) {
                                        // Update existing event
                                        existingEvent!!.copy(
                                            title = title,
                                            description = description,
                                            startTime = startTime,
                                            endTime = endTime,
                                            location = location,
                                            type = eventType
                                        )
                                    } else {
                                        // Create new event
                                        CalendarEvent(
                                            id = UUID.randomUUID().toString(),
                                            title = title,
                                            description = description,
                                            startTime = startTime,
                                            endTime = endTime,
                                            location = location,
                                            type = eventType,
                                            leadId = leadId,
                                            projectId = null
                                        )
                                    }

                                    // Save or update event
                                    val success = if (isEditing) {
                                        calendarEventRepository.update(event)
                                    } else {
                                        calendarEventRepository.save(event)
                                    }

                                    isSaving = false

                                    if (success) {
                                        // Send notification if this is a lead-related event
                                        // Only send notifications if we have a lead ID and contact information
                                        if (leadId != null && leadEmail.isNotEmpty() && leadPhone.isNotEmpty()) {
                                            // Use the NotificationService to send an event confirmation
                                            // This will send an email and/or SMS to the lead with the event details
                                            notificationService.sendEventConfirmation(
                                                email = leadEmail,
                                                phone = leadPhone,
                                                event = event
                                            )

                                            // Track that a notification was sent so we can show it in the UI
                                            notificationSent = true
                                        }

                                        showSuccessMessage = true
                                        // Navigate back after a short delay
                                        kotlinx.coroutines.delay(1500)
                                        navController.navigateUp()
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
                // Lead information section (if leadId is provided)
                if (leadId != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Lead Information",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = "Name: $leadName")
                            Text(text = "Phone: $leadPhone")
                            if (leadEmail.isNotEmpty()) {
                                Text(text = "Email: $leadEmail")
                            }
                        }
                    }
                }

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = titleError != null,
                    supportingText = { titleError?.let { Text(it) } }
                )

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    minLines = 3
                )

                // Location field
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Event type dropdown
                Text(
                    text = "Event Type",
                    style = MaterialTheme.typography.bodyMedium
                )
                var eventTypeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = eventTypeExpanded,
                    onExpandedChange = { eventTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = eventType.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = eventTypeExpanded,
                        onDismissRequest = { eventTypeExpanded = false }
                    ) {
                        EventType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    eventType = type
                                    eventTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date picker
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                var showDatePicker by remember { mutableStateOf(false) }

                Text(
                    text = "Date",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = dateFormat.format(selectedDate),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            showDatePicker = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Select Date"
                            )
                        }
                    }
                )

                // Date picker dialog
                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = selectedDate.time
                    )

                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    // Keep the time part of the original date
                                    val originalCalendar = Calendar.getInstance()
                                    originalCalendar.time = selectedDate
                                    val originalHour = originalCalendar.get(Calendar.HOUR_OF_DAY)
                                    val originalMinute = originalCalendar.get(Calendar.MINUTE)

                                    // Set the new date but keep the original time
                                    val newCalendar = Calendar.getInstance()
                                    newCalendar.timeInMillis = millis
                                    newCalendar.set(Calendar.HOUR_OF_DAY, originalHour)
                                    newCalendar.set(Calendar.MINUTE, originalMinute)

                                    selectedDate = newCalendar.time
                                }
                                showDatePicker = false
                            }) {
                                Text("Confirm")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState
                        )
                    }
                }

                // Time picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start Time",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hours
                            OutlinedTextField(
                                value = startHour.toString(),
                                onValueChange = { 
                                    val hour = it.toIntOrNull() ?: 0
                                    if (hour in 0..23) {
                                        startHour = hour
                                    }
                                },
                                modifier = Modifier.width(60.dp),
                                label = { Text("Hr") }
                            )

                            Text(
                                text = ":",
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            // Minutes
                            OutlinedTextField(
                                value = startMinute.toString(),
                                onValueChange = { 
                                    val minute = it.toIntOrNull() ?: 0
                                    if (minute in 0..59) {
                                        startMinute = minute
                                    }
                                },
                                modifier = Modifier.width(60.dp),
                                label = { Text("Min") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Duration",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hours
                            OutlinedTextField(
                                value = durationHours.toString(),
                                onValueChange = { 
                                    val hours = it.toIntOrNull() ?: 0
                                    if (hours in 0..23) {
                                        durationHours = hours
                                    }
                                },
                                modifier = Modifier.width(60.dp),
                                label = { Text("Hr") }
                            )

                            Text(
                                text = ":",
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            // Minutes
                            OutlinedTextField(
                                value = durationMinutes.toString(),
                                onValueChange = { 
                                    val minutes = it.toIntOrNull() ?: 0
                                    if (minutes in 0..59) {
                                        durationMinutes = minutes
                                    }
                                },
                                modifier = Modifier.width(60.dp),
                                label = { Text("Min") }
                            )
                        }
                    }
                }

                // Available time slots
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Available Time Slots",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (availableTimeSlots.isEmpty()) {
                            Text(
                                text = "No available time slots found for the selected date and duration. Try a different date or duration.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            // Format for displaying time
                            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

                            // Display available time slots
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                availableTimeSlots.forEach { slot ->
                                    val isSelected = selectedTimeSlot == slot
                                    val backgroundColor = if (isSelected) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surface

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                selectedTimeSlot = slot
                                                val calendar = Calendar.getInstance()
                                                calendar.time = slot
                                                startHour = calendar.get(Calendar.HOUR_OF_DAY)
                                                startMinute = calendar.get(Calendar.MINUTE)
                                            },
                                        color = backgroundColor,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = timeFormat.format(slot),
                                            modifier = Modifier.padding(12.dp),
                                            color = if (isSelected) 
                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isEditing) "Meeting updated successfully" else "Meeting scheduled successfully",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        // Show additional message if a notification was sent to the lead
                        // This provides feedback to the user that the lead has been notified
                        if (notificationSent) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Notification sent to $leadName",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
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
