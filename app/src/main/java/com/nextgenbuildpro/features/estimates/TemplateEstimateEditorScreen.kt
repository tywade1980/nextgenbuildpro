package com.nextgenbuildpro.features.estimates

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.data.repository.TemplateEstimateRepository
import kotlinx.coroutines.launch
import java.util.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Screen for creating and editing template-based estimates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEstimateEditorScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Repository
    val templateEstimateRepository = remember { TemplateEstimateRepository(context) }

    // State for the form
    var selectedTrade by remember { mutableStateOf<String?>(null) }
    var assemblyName by remember { mutableStateOf("") }
    var selectedPhase by remember { mutableStateOf<HomeLifecyclePhase?>(null) }
    var description by remember { mutableStateOf("") }
    var defaultUnit by remember { mutableStateOf<UnitType?>(null) }
    var baseQuantity by remember { mutableStateOf("") }

    // State for tasks
    var tasks by remember { mutableStateOf(listOf(TaskState())) }

    // State for voice input
    var voiceInputEnabled by remember { mutableStateOf(false) }

    // State for project type suggestion
    var suggestBasedOnProjectType by remember { mutableStateOf(false) }

    // State for selected tags
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }

    // State for showing dialogs
    var showAddTradeDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Function to validate the form
    fun validateForm(): Boolean {
        if (selectedTrade == null) {
            errorMessage = "Please select a trade"
            showErrorDialog = true
            return false
        }

        if (assemblyName.isBlank()) {
            errorMessage = "Please enter an assembly name"
            showErrorDialog = true
            return false
        }

        if (selectedPhase == null) {
            errorMessage = "Please select a phase"
            showErrorDialog = true
            return false
        }

        if (defaultUnit == null) {
            errorMessage = "Please select a default unit"
            showErrorDialog = true
            return false
        }

        if (baseQuantity.isBlank() || baseQuantity.toDoubleOrNull() == null) {
            errorMessage = "Please enter a valid base quantity"
            showErrorDialog = true
            return false
        }

        if (tasks.isEmpty()) {
            errorMessage = "Please add at least one task"
            showErrorDialog = true
            return false
        }

        for ((index, task) in tasks.withIndex()) {
            if (task.description.isBlank()) {
                errorMessage = "Please enter a description for task #${index + 1}"
                showErrorDialog = true
                return false
            }

            if (task.unitType == null) {
                errorMessage = "Please select a unit type for task #${index + 1}"
                showErrorDialog = true
                return false
            }

            if (task.defaultQuantity.isBlank() || task.defaultQuantity.toDoubleOrNull() == null) {
                errorMessage = "Please enter a valid default quantity for task #${index + 1}"
                showErrorDialog = true
                return false
            }

            if (task.laborRate.isBlank() || task.laborRate.toDoubleOrNull() == null) {
                errorMessage = "Please enter a valid labor rate for task #${index + 1}"
                showErrorDialog = true
                return false
            }

            if (task.materialRate.isBlank() || task.materialRate.toDoubleOrNull() == null) {
                errorMessage = "Please enter a valid material rate for task #${index + 1}"
                showErrorDialog = true
                return false
            }

            if (task.markup.isBlank() || task.markup.toDoubleOrNull() == null) {
                errorMessage = "Please enter a valid markup for task #${index + 1}"
                showErrorDialog = true
                return false
            }
        }

        return true
    }

    // Function to save the assembly template
    fun saveAssemblyTemplate() {
        coroutineScope.launch {
            try {
                // Convert tasks to TaskTemplate objects
                val taskTemplates = tasks.map { task ->
                    TaskTemplate(
                        description = task.description,
                        unitType = task.unitType!!,
                        defaultQty = task.defaultQuantity.toDouble(),
                        laborPerUnit = task.laborRate.toDouble(),
                        materialPerUnit = task.materialRate.toDouble(),
                        markup = task.markup.toDouble(),
                        requiredTools = emptyList(),
                        flags = selectedTags
                    )
                }

                // Create AssemblyTemplate object
                val assemblyTemplate = AssemblyTemplate(
                    name = assemblyName,
                    category = selectedTrade ?: "",
                    validModes = listOf(ContextMode.REMODELING, ContextMode.NEW_CONSTRUCTION),
                    description = description,
                    defaultQuantityUnit = defaultUnit!!,
                    baseQuantity = baseQuantity.toDouble(),
                    lifecyclePhase = selectedPhase!!,
                    tasks = taskTemplates
                )

                // Get the current template library
                val templateLibrary = templateEstimateRepository.templateLibrary.value
                if (templateLibrary != null) {
                    // Find the trade
                    val trade = templateLibrary.trades.find { it.tradeName == selectedTrade }
                    if (trade != null) {
                        // Add the assembly to the trade
                        val updatedTrade = trade.copy(
                            assemblies = trade.assemblies + assemblyTemplate
                        )

                        // Update the template library
                        templateLibrary.trades.map {
                            if (it.tradeName == selectedTrade) updatedTrade else it 
                        }

                        // Save the updated template library
                        // Note: In a real app, this would update a database or file
                        // For this example, we'll just show a success dialog
                        showSuccessDialog = true
                    } else {
                        errorMessage = "Trade not found"
                        showErrorDialog = true
                    }
                } else {
                    errorMessage = "Template library not found"
                    showErrorDialog = true
                }
            } catch (e: Exception) {
                errorMessage = "Error saving assembly template: ${e.message}"
                showErrorDialog = true
            }
        }
    }

    // Function to export the template as JSON
    fun exportToJson() {
        try {
            // Validate form first
            if (!validateForm()) {
                return
            }

            // Convert tasks to JSON
            val tasksJson = JSONArray()
            tasks.forEach { task ->
                val taskJson = JSONObject()
                taskJson.put("description", task.description)
                taskJson.put("unit", task.unitType?.name)
                taskJson.put("quantity", task.defaultQuantity.toDouble())
                taskJson.put("laborRate", task.laborRate.toDouble())
                taskJson.put("materialRate", task.materialRate.toDouble())
                taskJson.put("markup", task.markup.toDouble())
                tasksJson.put(taskJson)
            }

            // Create assembly JSON
            val assemblyJson = JSONObject()
            assemblyJson.put("name", assemblyName)
            assemblyJson.put("phase", selectedPhase?.name)
            assemblyJson.put("defaultUnit", defaultUnit?.name)
            assemblyJson.put("description", description)
            assemblyJson.put("baseQuantity", baseQuantity.toDouble())
            assemblyJson.put("tasks", tasksJson)
            assemblyJson.put("tags", JSONArray(selectedTags))

            // Create trade JSON
            val tradeJson = JSONObject()
            tradeJson.put("name", selectedTrade)
            tradeJson.put("assemblies", JSONArray().put(assemblyJson))

            // Show the JSON in a dialog or share it
            // For this example, we'll just log it
            Log.d("TemplateEstimateEditor", "Exported JSON: ${tradeJson.toString(2)}")

            // In a real app, you would share this JSON or save it to a file
            // For this example, we'll just show a success dialog
            showSuccessDialog = true
        } catch (e: Exception) {
            errorMessage = "Error exporting JSON: ${e.message}"
            showErrorDialog = true
        }
    }

    // Available trades (loaded from repository)
    val templateLibraryState = templateEstimateRepository.templateLibrary.collectAsState()
    val availableTrades = remember(templateLibraryState.value) {
        templateLibraryState.value?.trades?.map { it.tradeName } ?: emptyList()
    }

    // Available phases
    val availablePhases = remember { HomeLifecyclePhase.values().toList() }

    // Available units
    val availableUnits = remember { UnitType.values().toList() }

    // Available tags
    val availableTags = remember {
        listOf(
            "Structural",
            "Finish",
            "Interior",
            "Exterior",
            "Load-Bearing",
            "Non-Load-Bearing"
        )
    }

    // Add Trade Dialog
    if (showAddTradeDialog) {
        var newTradeName by remember { mutableStateOf("") }
        var newTradeCode by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddTradeDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Add New Trade",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newTradeName,
                        onValueChange = { newTradeName = it },
                        label = { Text("Trade Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newTradeCode,
                        onValueChange = { newTradeCode = it },
                        label = { Text("Trade Code") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddTradeDialog = false }) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                // Add new trade
                                if (newTradeName.isNotBlank() && newTradeCode.isNotBlank()) {
                                    // In a real app, this would add the trade to the repository
                                    // For this example, we'll just close the dialog
                                    selectedTrade = newTradeName
                                    showAddTradeDialog = false
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        Dialog(onDismissRequest = { showSuccessDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Success",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "The operation was completed successfully.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showSuccessDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }

    // Error Dialog
    if (showErrorDialog) {
        Dialog(onDismissRequest = { showErrorDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showErrorDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Assembly Template") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
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
                .verticalScroll(rememberScrollState())
        ) {
            // Trade selection
            Text(
                text = "Trade",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            var tradeMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = tradeMenuExpanded,
                onExpandedChange = { tradeMenuExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTrade ?: "",
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tradeMenuExpanded) },
                    placeholder = { Text("Select a trade (e.g., Framing, Drywall, Tile...)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = tradeMenuExpanded,
                    onDismissRequest = { tradeMenuExpanded = false }
                ) {
                    availableTrades.forEach { trade ->
                        DropdownMenuItem(
                            text = { Text(trade) },
                            onClick = { 
                                selectedTrade = trade
                                tradeMenuExpanded = false
                            }
                        )
                    }

                    Divider()

                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add New Trade",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add New Trade")
                            }
                        },
                        onClick = { 
                            showAddTradeDialog = true
                            tradeMenuExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Assembly details
            Text(
                text = "Assembly Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = assemblyName,
                onValueChange = { assemblyName = it },
                label = { Text("Assembly Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Phase dropdown
            var phaseMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = phaseMenuExpanded,
                onExpandedChange = { phaseMenuExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedPhase?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = phaseMenuExpanded) },
                    label = { Text("Phase") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = phaseMenuExpanded,
                    onDismissRequest = { phaseMenuExpanded = false }
                ) {
                    availablePhases.forEach { phase ->
                        DropdownMenuItem(
                            text = { Text(phase.name) },
                            onClick = { 
                                selectedPhase = phase
                                phaseMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Default unit dropdown
            var unitMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = unitMenuExpanded,
                onExpandedChange = { unitMenuExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = defaultUnit?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded) },
                    label = { Text("Default Unit") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = unitMenuExpanded,
                    onDismissRequest = { unitMenuExpanded = false }
                ) {
                    availableUnits.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.name) },
                            onClick = { 
                                defaultUnit = unit
                                unitMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Base quantity
            OutlinedTextField(
                value = baseQuantity,
                onValueChange = { baseQuantity = it },
                label = { Text("Base Quantity") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tasks
            Text(
                text = "Tasks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            tasks.forEachIndexed { index, task ->
                TaskEditor(
                    task = task,
                    index = index,
                    onTaskChanged = { updatedTask ->
                        tasks = tasks.toMutableList().apply {
                            this[index] = updatedTask
                        }
                    },
                    onDeleteTask = {
                        if (tasks.size > 1) {
                            tasks = tasks.toMutableList().apply {
                                removeAt(index)
                            }
                        }
                    },
                    onMoveUp = {
                        if (index > 0) {
                            tasks = tasks.toMutableList().apply {
                                val temp = this[index]
                                this[index] = this[index - 1]
                                this[index - 1] = temp
                            }
                        }
                    },
                    onMoveDown = {
                        if (index < tasks.size - 1) {
                            tasks = tasks.toMutableList().apply {
                                val temp = this[index]
                                this[index] = this[index + 1]
                                this[index + 1] = temp
                            }
                        }
                    },
                    availableUnits = availableUnits
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Add task button
            Button(
                onClick = {
                    tasks = tasks + TaskState()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Task")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Voice input toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enable Voice Input",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.weight(1f))

                Switch(
                    checked = voiceInputEnabled,
                    onCheckedChange = { voiceInputEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Voice input button
            Button(
                onClick = { /* Start voice input */ },
                enabled = voiceInputEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Start Listening"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Listening")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Project type suggestion
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = suggestBasedOnProjectType,
                    onCheckedChange = { suggestBasedOnProjectType = it }
                )

                Text(
                    text = "Suggest based on project type",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tags dropdown
            var tagsMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = tagsMenuExpanded,
                onExpandedChange = { tagsMenuExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTags.joinToString(", "),
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagsMenuExpanded) },
                    label = { Text("Assembly Tags") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = tagsMenuExpanded,
                    onDismissRequest = { tagsMenuExpanded = false }
                ) {
                    availableTags.forEach { tag ->
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedTags.contains(tag),
                                        onCheckedChange = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(tag)
                                }
                            },
                            onClick = { 
                                selectedTags = if (selectedTags.contains(tag)) {
                                    selectedTags - tag
                                } else {
                                    selectedTags + tag
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        // Validate form
                        if (validateForm()) {
                            // Save to library
                            saveAssemblyTemplate()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to Library")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { exportToJson() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export JSON")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    // Reset form
                    selectedTrade = null
                    assemblyName = ""
                    selectedPhase = null
                    description = ""
                    defaultUnit = null
                    tasks = listOf(TaskState())
                    voiceInputEnabled = false
                    suggestBasedOnProjectType = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Form")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * State for a task in the form
 */
data class TaskState(
    val description: String = "",
    val unitType: UnitType? = null,
    val defaultQuantity: String = "",
    val laborRate: String = "",
    val materialRate: String = "",
    val markup: String = ""
)

/**
 * Task editor component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditor(
    task: TaskState,
    index: Int,
    onTaskChanged: (TaskState) -> Unit,
    onDeleteTask: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    availableUnits: List<UnitType>
) {
    var unitMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Task header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Task #${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Task actions
                IconButton(onClick = onDeleteTask) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(onClick = onMoveDown) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Move Down"
                    )
                }

                IconButton(onClick = onMoveUp) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Move Up"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task description
            OutlinedTextField(
                value = task.description,
                onValueChange = { onTaskChanged(task.copy(description = it)) },
                label = { Text("Task Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Unit type dropdown
            ExposedDropdownMenuBox(
                expanded = unitMenuExpanded,
                onExpandedChange = { unitMenuExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = task.unitType?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded) },
                    label = { Text("Unit Type") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = unitMenuExpanded,
                    onDismissRequest = { unitMenuExpanded = false }
                ) {
                    availableUnits.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.name) },
                            onClick = {
                                onTaskChanged(task.copy(unitType = unit))
                                unitMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task details in a grid
            Row(modifier = Modifier.fillMaxWidth()) {
                // Default quantity
                OutlinedTextField(
                    value = task.defaultQuantity,
                    onValueChange = { onTaskChanged(task.copy(defaultQuantity = it)) },
                    label = { Text("Default Quantity") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Labor rate
                OutlinedTextField(
                    value = task.laborRate,
                    onValueChange = { onTaskChanged(task.copy(laborRate = it)) },
                    label = { Text("Labor Rate / unit") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Material rate
                OutlinedTextField(
                    value = task.materialRate,
                    onValueChange = { onTaskChanged(task.copy(materialRate = it)) },
                    label = { Text("Material Rate / unit") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Markup
                OutlinedTextField(
                    value = task.markup,
                    onValueChange = { onTaskChanged(task.copy(markup = it)) },
                    label = { Text("Markup (decimal)") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
