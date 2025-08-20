package com.nextgenbuildpro.features.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.pm.data.model.Task
import com.nextgenbuildpro.pm.data.model.TaskPriority
import com.nextgenbuildpro.pm.data.model.TaskStatus
import com.nextgenbuildpro.pm.PmModule
import com.nextgenbuildpro.pm.rememberPmComponents
import androidx.compose.ui.platform.LocalContext

@Composable
fun TasksScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "To Do", "In Progress", "Blocked", "Completed")

    // Get the PM components
    val pmComponents = rememberPmComponents()
    val taskRepository = pmComponents.taskRepository

    // Use LaunchedEffect to load tasks when the screen is first displayed
    val tasks = remember { mutableStateListOf<Task>() }
    LaunchedEffect(Unit) {
        // In a real app, this would be a suspend function call
        // For now, we'll use sample data
        tasks.clear()
        tasks.addAll(taskRepository.getAll())
    }

    // Filter tasks based on selected filter and search query
    val filteredTasks = if (selectedFilter == "All") {
        tasks.filter { it.title.contains(searchQuery, ignoreCase = true) }
    } else {
        tasks.filter { 
            it.title.contains(searchQuery, ignoreCase = true) && 
            it.status == selectedFilter 
        }
    }

    Scaffold(
        topBar = {
            TasksTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                navController = navController
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("task_editor") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    Surface(
                        color = if (selectedFilter == filter) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .height(32.dp)
                            .clickable { selectedFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedFilter == filter)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Tasks list
            if (filteredTasks.isEmpty()) {
                EmptyTasksView(searchQuery.isNotEmpty())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTasks) { task ->
                        TaskCard(
                            task = task,
                            onClick = { navController.navigate("task_detail/${task.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TasksTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    navController: NavController
) {
    Column {
        // Custom app bar to avoid experimental API
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

                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { navController.navigate("notifications") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { navController.navigate("account_settings") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Account",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search tasks...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
    }
}

@Composable
fun TaskCard(task: Task, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(getPriorityColor(task.priority))
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Task title and status
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = getStatusColor(task.status).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = task.status,
                                style = MaterialTheme.typography.bodySmall,
                                color = getStatusColor(task.status),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Show linked entity type if available
                        task.linkedEntityType?.let { entityType ->
                            Surface(
                                color = Color.LightGray.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Text(
                                    text = entityType.capitalize(),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Due date
                if (task.dueDate.isNotEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Due",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = task.dueDate,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task description
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Task details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Assigned to
                if (task.assignedTo.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Assigned To",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.assignedTo,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // Estimated hours
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Estimated Hours",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${task.estimatedHours}h",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyTasksView(isFiltered: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isFiltered) Icons.Default.FilterList else Icons.Default.Assignment,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isFiltered) "No tasks match your search" else "No tasks yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isFiltered) 
                "Try adjusting your search or filters" 
            else 
                "Create your first task by clicking the + button",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun getPriorityColor(priority: String): Color {
    return when (priority) {
        TaskPriority.LOW.displayName -> Color(0xFF4CAF50) // Green
        TaskPriority.MEDIUM.displayName -> Color(0xFFFFC107) // Amber
        TaskPriority.HIGH.displayName -> Color(0xFFFF9800) // Orange
        TaskPriority.URGENT.displayName -> Color(0xFFF44336) // Red
        else -> Color.Gray
    }
}

@Composable
fun getStatusColor(status: String): Color {
    return when (status) {
        TaskStatus.TODO.displayName -> Color(0xFF2196F3) // Blue
        TaskStatus.IN_PROGRESS.displayName -> Color(0xFFFF9800) // Orange
        TaskStatus.BLOCKED.displayName -> Color(0xFFF44336) // Red
        TaskStatus.COMPLETED.displayName -> Color(0xFF4CAF50) // Green
        "Split" -> Color(0xFF9C27B0) // Purple
        else -> Color.Gray
    }
}

// Extension function to capitalize the first letter of a string
fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
