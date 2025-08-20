package com.nextgenbuildpro.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.features.calendar.models.*
import com.nextgenbuildpro.features.calendar.repository.CalendarRepository
import com.nextgenbuildpro.navigation.NavDestinations
import com.nextgenbuildpro.pm.data.repository.ProjectRepository
import com.nextgenbuildpro.pm.rememberPmComponents
import com.nextgenbuildpro.receptionist.repository.CalendarEventRepository
import com.nextgenbuildpro.crm.rememberCrmComponents
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main screen for the Calendar feature
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController) {
    // Get repositories
    val pmComponents = rememberPmComponents()
    rememberCrmComponents()
    val calendarEventRepository = CalendarEventRepository()
    pmComponents.projectRepository

    // Get leadId from navigation arguments
    val navBackStackEntry = navController.currentBackStackEntry
    val leadId = navBackStackEntry?.arguments?.getString("leadId")

    // State for calendar events
    val events = remember { mutableStateListOf<CalendarEvent>() }

    // If leadId is provided, navigate directly to the event editor
    LaunchedEffect(leadId) {
        if (leadId != null) {
            navController.navigate("${NavDestinations.CALENDAR_EVENT_EDITOR}?leadId=$leadId")
        }
    }

    // State for selected date
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }

    // State for selected view mode
    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTH) }

    // State for timelines
    val timelines = remember { mutableStateListOf<ProjectTimeline>() }

    // Load timelines
    LaunchedEffect(Unit) {
        // In a real app, this would come from a repository
        // For now, we'll use sample data
        timelines.clear()
        timelines.addAll(getSampleTimelines())
    }

    // Load calendar events for the selected date
    LaunchedEffect(selectedDate) {
        events.clear()
        events.addAll(calendarEventRepository.getEventsForDate(selectedDate))
    }

    Scaffold(
        topBar = {
            CalendarTopBar(
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                navController = navController
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(NavDestinations.CALENDAR_EVENT_EDITOR) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Event"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Calendar view
            CalendarView(
                selectedDate = selectedDate,
                onDateSelect = { selectedDate = it },
                viewMode = viewMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
            )

            // Calendar events for selected date
            CalendarEvents(
                events = events,
                navController = navController,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
            )

            // Project timelines
            ProjectTimelines(
                timelines = timelines,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
            )
        }
    }
}

/**
 * Top bar for the Calendar screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTopBar(
    selectedDate: Date,
    onDateChange: (Date) -> Unit,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    navController: NavController
) {
    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    CenterAlignedTopAppBar(
        title = { Text(dateFormat.format(selectedDate)) },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            // Month navigation
            IconButton(
                onClick = { 
                    // Navigate to previous month
                    val calendar = Calendar.getInstance()
                    calendar.time = selectedDate
                    calendar.add(Calendar.MONTH, -1)
                    onDateChange(calendar.time)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Month"
                )
            }

            IconButton(
                onClick = { 
                    // Navigate to next month
                    val calendar = Calendar.getInstance()
                    calendar.time = selectedDate
                    calendar.add(Calendar.MONTH, 1)
                    onDateChange(calendar.time)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Month"
                )
            }

            // View mode selector
            IconButton(onClick = { onViewModeChange(CalendarViewMode.DAY) }) {
                Icon(
                    imageVector = Icons.Default.ViewDay,
                    contentDescription = "Day View",
                    tint = if (viewMode == CalendarViewMode.DAY) 
                        MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            IconButton(onClick = { onViewModeChange(CalendarViewMode.WEEK) }) {
                Icon(
                    imageVector = Icons.Default.ViewWeek,
                    contentDescription = "Week View",
                    tint = if (viewMode == CalendarViewMode.WEEK) 
                        MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            IconButton(onClick = { onViewModeChange(CalendarViewMode.MONTH) }) {
                Icon(
                    imageVector = Icons.Default.CalendarViewMonth,
                    contentDescription = "Month View",
                    tint = if (viewMode == CalendarViewMode.MONTH) 
                        MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    )
}

/**
 * Calendar view component
 */
@Composable
fun CalendarView(
    selectedDate: Date,
    onDateSelect: (Date) -> Unit,
    viewMode: CalendarViewMode,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()
    calendar.time = selectedDate

    Card(
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Calendar header (days of week)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            when (viewMode) {
                CalendarViewMode.MONTH -> MonthCalendarGrid(
                    selectedDate = selectedDate,
                    onDateSelect = onDateSelect
                )
                CalendarViewMode.WEEK -> WeekCalendarView(
                    selectedDate = selectedDate,
                    onDateSelect = onDateSelect
                )
                CalendarViewMode.DAY -> DayCalendarView(
                    selectedDate = selectedDate
                )
            }
        }
    }
}

/**
 * Month calendar grid
 */
@Composable
fun MonthCalendarGrid(
    selectedDate: Date,
    onDateSelect: (Date) -> Unit
) {
    // Get the calendar for the selected date
    val calendar = Calendar.getInstance()
    calendar.time = selectedDate

    // Move to the first day of the month
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    // Get the day of week for the first day of the month
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    // Get the number of days in the month
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Create a grid of days
    Column {
        // Calculate the number of rows needed
        val rows = (firstDayOfWeek - 1 + daysInMonth + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0 until 7) {
                    val day = row * 7 + col - (firstDayOfWeek - 1) + 1

                    if (day in 1..daysInMonth) {
                        // Create a new calendar for this day
                        val dayCalendar = Calendar.getInstance()
                        dayCalendar.time = selectedDate
                        dayCalendar.set(Calendar.DAY_OF_MONTH, day)

                        // Create a calendar for the selected date for comparison
                        val selectedCalendar = Calendar.getInstance()
                        selectedCalendar.time = selectedDate

                        // Check if this is the selected day
                        val isSelected = dayCalendar.get(Calendar.DAY_OF_MONTH) == selectedCalendar.get(Calendar.DAY_OF_MONTH) &&
                                dayCalendar.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                                dayCalendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR)

                        // Day cell
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable {
                                    // Create a new date object for the selected day
                                    val newDate = Calendar.getInstance()
                                    newDate.time = selectedDate
                                    newDate.set(Calendar.DAY_OF_MONTH, day)
                                    onDateSelect(newDate.time)
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                color = if (isSelected) Color.White else Color.Unspecified,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        // Empty cell
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Week calendar view
 */
@Composable
fun WeekCalendarView(
    selectedDate: Date,
    onDateSelect: (Date) -> Unit
) {
    // Placeholder for week view
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Week View - Coming Soon")
    }
}

/**
 * Day calendar view
 */
@Composable
fun DayCalendarView(
    selectedDate: Date
) {
    // Placeholder for day view
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Day View - Coming Soon")
    }
}

/**
 * Project timelines component
 */
@Composable
fun ProjectTimelines(
    timelines: List<ProjectTimeline>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Project Timelines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (timelines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No project timelines available")
                }
            } else {
                LazyColumn {
                    items(timelines) { timeline ->
                        ProjectTimelineItem(timeline = timeline)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * Project timeline item
 */
@Composable
fun ProjectTimelineItem(
    timeline: ProjectTimeline
) {
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Project name and dates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeline.projectName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${dateFormat.format(timeline.startDate)} - ${dateFormat.format(timeline.endDate)}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = timeline.progress / 100f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Progress percentage
        Text(
            text = "${timeline.progress}% Complete",
            style = MaterialTheme.typography.bodySmall
        )

        // Gantt chart placeholder
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("Gantt Chart - Coming Soon")
        }
    }
}

/**
 * Get sample timelines for testing
 */
private fun getSampleTimelines(): List<ProjectTimeline> {
    val currentCalendar = Calendar.getInstance()
    val currentYear = currentCalendar.get(Calendar.YEAR)
    val currentMonth = currentCalendar.get(Calendar.MONTH)

    // Project 1 timeline - starts this month, ends in 3 months
    val project1StartDate = Calendar.getInstance().apply {
        set(currentYear, currentMonth, 1)
    }.time

    val project1EndDate = Calendar.getInstance().apply {
        set(currentYear, currentMonth + 3, 1)
        add(Calendar.DATE, -1) // Last day of the month + 2
    }.time

    val project1Timeline = ProjectTimeline(
        id = "timeline_1",
        projectId = "project_1",
        projectName = "Kitchen Renovation - Smith",
        startDate = project1StartDate,
        endDate = project1EndDate,
        progress = 0
    )

    // Project 2 timeline - started last month, ends next month
    val project2StartDate = Calendar.getInstance().apply {
        set(currentYear, currentMonth - 1, 15)
    }.time

    val project2EndDate = Calendar.getInstance().apply {
        set(currentYear, currentMonth + 1, 15)
    }.time

    val project2Timeline = ProjectTimeline(
        id = "timeline_2",
        projectId = "project_2",
        projectName = "Bathroom Remodel - Johnson",
        startDate = project2StartDate,
        endDate = project2EndDate,
        progress = 40
    )

    return listOf(project1Timeline, project2Timeline)
}

/**
 * Calendar events component
 */
@Composable
fun CalendarEvents(
    events: List<CalendarEvent>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Events",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (events.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No events for this date")
                }
            } else {
                LazyColumn {
                    items(events) { event ->
                        CalendarEventItem(
                            event = event,
                            onClick = {
                                // Navigate to event editor with event ID
                                navController.navigate("${NavDestinations.CALENDAR_EVENT_EDITOR}/${event.id}")
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * Calendar event item
 */
@Composable
fun CalendarEventItem(
    event: CalendarEvent,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${timeFormat.format(event.startTime)} - ${timeFormat.format(event.endTime)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (event.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * Enum for calendar view modes
 */
enum class CalendarViewMode {
    DAY,
    WEEK,
    MONTH
}
