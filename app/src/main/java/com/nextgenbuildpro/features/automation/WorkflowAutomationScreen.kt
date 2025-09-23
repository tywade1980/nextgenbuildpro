package com.nextgenbuildpro.features.automation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.core.CoreModule
import com.nextgenbuildpro.core.services.AutoFillContext
import com.nextgenbuildpro.core.services.AutomatedWorkflowTemplate
import com.nextgenbuildpro.core.services.WorkflowStats
import kotlinx.coroutines.launch

/**
 * Workflow Automation Dashboard Screen
 * Shows available workflow templates and allows users to execute automated workflows
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowAutomationScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    
    // Get workflow automation engine
    val workflowEngine = remember { 
        try {
            CoreModule.getWorkflowAutomationEngine()
        } catch (e: Exception) {
            null
        }
    }
    
    // State
    var workflowTemplates by remember { mutableStateOf<List<AutomatedWorkflowTemplate>>(emptyList()) }
    var workflowStats by remember { mutableStateOf(WorkflowStats()) }
    var isExecutingWorkflow by remember { mutableStateOf(false) }
    var executionResult by remember { mutableStateOf<String?>(null) }
    var showExecutionDialog by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf<AutomatedWorkflowTemplate?>(null) }
    
    // Load workflow templates and stats
    LaunchedEffect(workflowEngine) {
        workflowEngine?.let { engine ->
            try {
                // Start the service if not running
                if (!engine.isRunning.value) {
                    engine.start()
                }
                
                workflowTemplates = engine.getWorkflowTemplates()
                
                // Collect stats
                launch {
                    engine.workflowStats.collect { stats ->
                        workflowStats = stats
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workflow Automation") },
                actions = {
                    IconButton(
                        onClick = { /* Navigate to settings */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Create custom workflow */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create workflow"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Workflow Statistics Card
            WorkflowStatsCard(stats = workflowStats)
            
            // Workflow Templates Section
            Text(
                text = "Available Workflows",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (workflowEngine == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Workflow Automation Unavailable",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "The workflow automation engine is not available. Please check your configuration.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(workflowTemplates) { template ->
                        WorkflowTemplateCard(
                            template = template,
                            onExecute = {
                                selectedTemplate = template
                                showExecutionDialog = true
                            },
                            isExecuting = isExecutingWorkflow
                        )
                    }
                }
            }
        }
    }
    
    // Workflow Execution Dialog
    if (showExecutionDialog && selectedTemplate != null) {
        WorkflowExecutionDialog(
            template = selectedTemplate!!,
            onExecute = { triggerData ->
                showExecutionDialog = false
                coroutineScope.launch {
                    isExecutingWorkflow = true
                    executionResult = null
                    
                    try {
                        val context = AutoFillContext(
                            contextId = "manual_trigger",
                            formType = "workflow_execution",
                            userId = "current_user",
                            metadata = triggerData
                        )
                        
                        val result = workflowEngine!!.executeAutomatedWorkflow(
                            templateId = selectedTemplate!!.id,
                            triggerData = triggerData,
                            autoFillContext = context
                        )
                        
                        if (result.isSuccess) {
                            executionResult = "Workflow executed successfully!"
                        } else {
                            executionResult = "Workflow execution failed: ${result.exceptionOrNull()?.message}"
                        }
                    } catch (e: Exception) {
                        executionResult = "Error executing workflow: ${e.message}"
                    } finally {
                        isExecutingWorkflow = false
                    }
                }
            },
            onDismiss = { showExecutionDialog = false }
        )
    }
    
    // Execution Result Snackbar
    executionResult?.let { result ->
        LaunchedEffect(result) {
            kotlinx.coroutines.delay(3000)
            executionResult = null
        }
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar {
                Text(result)
            }
        }
    }
}

@Composable
fun WorkflowStatsCard(stats: WorkflowStats) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Workflow Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total",
                    value = stats.totalCount.toString()
                )
                StatItem(
                    label = "Completed",
                    value = stats.completedCount.toString()
                )
                StatItem(
                    label = "Failed",
                    value = stats.failedCount.toString()
                )
                StatItem(
                    label = "Success Rate",
                    value = "${(stats.successRate * 100).toInt()}%"
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WorkflowTemplateCard(
    template: AutomatedWorkflowTemplate,
    onExecute: () -> Unit,
    isExecuting: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = onExecute,
                    enabled = !isExecuting && template.enabled
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Execute"
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Execute")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Steps",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${template.steps.size} steps",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Text(
                    text = "~${template.estimatedDuration} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = template.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun WorkflowExecutionDialog(
    template: AutomatedWorkflowTemplate,
    onExecute: (Map<String, Any>) -> Unit,
    onDismiss: () -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var projectType by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Execute ${template.name}")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Client Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = projectType,
                    onValueChange = { projectType = it },
                    label = { Text("Project Type") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Budget") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val triggerData = mapOf(
                        "client_name" to clientName,
                        "project_type" to projectType,
                        "budget" to (budget.toDoubleOrNull() ?: 0.0),
                        "trigger_source" to "manual"
                    )
                    onExecute(triggerData)
                }
            ) {
                Text("Execute Workflow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}