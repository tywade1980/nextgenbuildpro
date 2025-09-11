package com.nextgenbuildpro.apps

import com.nextgenbuildpro.shared.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * ConstructionPlatform
 * 
 * Comprehensive construction management platform integrated with NextGen AI OS.
 * Handles project management, resource allocation, safety monitoring, and intelligent
 * construction workflow optimization.
 */
class ConstructionPlatform(private val context: Context) : NextGenService {
    
    override val serviceName: String = "ConstructionPlatform"
    
    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val _projects = MutableStateFlow<List<ConstructionProject>>(emptyList())
    val projects: StateFlow<List<ConstructionProject>> = _projects.asStateFlow()
    
    private val _activeProject = MutableStateFlow<ConstructionProject?>(null)
    val activeProject: StateFlow<ConstructionProject?> = _activeProject.asStateFlow()
    
    private val _tasks = MutableStateFlow<List<ConstructionTask>>(emptyList())
    val tasks: StateFlow<List<ConstructionTask>> = _tasks.asStateFlow()
    
    private val _resources = MutableStateFlow<List<Resource>>(emptyList())
    val resources: StateFlow<List<Resource>> = _resources.asStateFlow()
    
    private val _safetyAlerts = MutableStateFlow<List<SafetyAlert>>(emptyList())
    val safetyAlerts: StateFlow<List<SafetyAlert>> = _safetyAlerts.asStateFlow()
    
    private val mutex = Mutex()
    private val projectManager = ProjectManager()
    private val resourceManager = ResourceManager()
    private val safetyManager = SafetyManager()
    private val qualityControl = QualityControl()
    private val aiOptimizer = ConstructionAIOptimizer()
    
    // Configuration
    private val config = ConstructionPlatformConfig(
        enableAIOptimization = true,
        enableSafetyMonitoring = true,
        enableQualityControl = true,
        enableResourceTracking = true,
        enableRealtimeUpdates = true,
        enablePredictiveAnalytics = true
    )
    
    override suspend fun start(): Result<Unit> = try {
        mutex.withLock {
            Log.i("ConstructionPlatform", "Starting Construction Platform...")
            
            // Initialize managers
            projectManager.initialize()
            resourceManager.initialize()
            safetyManager.initialize()
            qualityControl.initialize()
            aiOptimizer.initialize()
            
            // Load data
            loadProjects()
            loadResources()
            loadSafetyAlerts()
            
            _isRunning.value = true
            Log.i("ConstructionPlatform", "Construction Platform started successfully")
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("ConstructionPlatform", "Failed to start Construction Platform", e)
        Result.failure(e)
    }
    
    override suspend fun stop(): Result<Unit> = try {
        mutex.withLock {
            Log.i("ConstructionPlatform", "Stopping Construction Platform...")
            
            // Save current state
            saveProjectData()
            
            // Shutdown managers
            projectManager.shutdown()
            resourceManager.shutdown()
            safetyManager.shutdown()
            qualityControl.shutdown()
            aiOptimizer.shutdown()
            
            _isRunning.value = false
            Log.i("ConstructionPlatform", "Construction Platform stopped successfully")
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("ConstructionPlatform", "Error stopping Construction Platform", e)
        Result.failure(e)
    }
    
    override suspend fun restart(): Result<Unit> {
        stop()
        return start()
    }
    
    override suspend fun getHealthStatus(): ServiceHealth {
        return ServiceHealth(
            isHealthy = _isRunning.value,
            lastCheckTime = LocalDateTime.now(),
            issues = if (_isRunning.value) emptyList() else listOf("Service not running"),
            metrics = mapOf(
                "active_projects" to _projects.value.count { it.status == ProjectStatus.IN_PROGRESS }.toDouble(),
                "pending_tasks" to _tasks.value.count { it.status == TaskStatus.PENDING }.toDouble(),
                "safety_alerts" to _safetyAlerts.value.count { it.severity == "HIGH" }.toDouble(),
                "resource_utilization" to calculateResourceUtilization()
            )
        )
    }
    
    // === PROJECT MANAGEMENT ===
    
    suspend fun createProject(projectData: ProjectCreationData): Result<ConstructionProject> = try {
        Log.d("ConstructionPlatform", "Creating new project: ${projectData.name}")
        
        val project = ConstructionProject(
            id = UUID.randomUUID().toString(),
            name = projectData.name,
            description = projectData.description,
            address = projectData.address,
            clientId = projectData.clientId,
            projectManager = projectData.projectManagerId,
            status = ProjectStatus.PLANNING,
            startDate = projectData.startDate,
            estimatedEndDate = projectData.estimatedEndDate,
            budget = projectData.budget,
            currentCost = 0.0,
            phases = projectData.phases.map { createProjectPhase(it) },
            tasks = emptyList(),
            documents = emptyList()
        )
        
        val updatedProjects = _projects.value + project
        _projects.value = updatedProjects
        
        // Initialize project resources and tasks
        initializeProjectResources(project)
        initializeProjectTasks(project)
        
        Log.i("ConstructionPlatform", "Project created successfully: ${project.id}")
        Result.success(project)
    } catch (e: Exception) {
        Log.e("ConstructionPlatform", "Error creating project", e)
        Result.failure(e)
    }
    
    suspend fun selectProject(projectId: String): Result<Unit> = try {
        val project = _projects.value.find { it.id == projectId }
        if (project != null) {
            _activeProject.value = project
            loadProjectTasks(project)
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Project not found: $projectId"))
        }
    } catch (e: Exception) {
        Log.e("ConstructionPlatform", "Error selecting project", e)
        Result.failure(e)
    }
    
    suspend fun updateProjectStatus(projectId: String, status: ProjectStatus): Result<Unit> = try {
        val projects = _projects.value.toMutableList()
        val index = projects.indexOfFirst { it.id == projectId }
        
        if (index != -1) {
            projects[index] = projects[index].copy(status = status)
            _projects.value = projects
            
            // Update active project if it's the same
            if (_activeProject.value?.id == projectId) {
                _activeProject.value = projects[index]
            }
            
            Log.i("ConstructionPlatform", "Project status updated: $projectId -> $status")
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Project not found: $projectId"))
        }
    } catch (e: Exception) {
        Log.e("ConstructionPlatform", "Error updating project status", e)
        Result.failure(e)
    }
    
    // === TASK MANAGEMENT ===
    
    suspend fun createTask(taskData: TaskCreationData): Result<ConstructionTask> = try {
        Log.d("ConstructionPlatform", "Creating new task: ${taskData.title}")
        
        val task = ConstructionTask(
            id = UUID.randomUUID().toString(),
            title = taskData.title,
            description = taskData.description,
            projectId = taskData.projectId,
            assignedTo = taskData.assignedTo,
            priority = taskData.priority,
            status = TaskStatus.PENDING,
            category = taskData.category,
            estimatedHours = taskData.estimatedHours,
            actualHours = 0.0,
            dependencies = taskData.dependencies,
            materials = taskData.materials,
            equipment = taskData.equipment,
            safetyRequirements = taskData.safetyRequirements,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            dueDate = taskData.dueDate
        )
        
        val updatedTasks = _tasks.value + task
        _tasks.value = updatedTasks
        
        // Update project with new task
        updateProjectTasks(task.projectId, updatedTasks.filter { it.projectId == task.projectId })
        
        // Optimize resource allocation
        if (config.enableAIOptimization) {
            aiOptimizer.optimizeTaskAssignment(task)
        }
        
        Log.i("ConstructionPlatform", "Task created successfully: ${task.id}")
        Result.success(task)
    } catch (e: Exception) {
        Log.e("ConstructionPlatform", "Error creating task", e)
        Result.failure(e)
    }
    
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> = try {
        val tasks = _tasks.value.toMutableList()
        val index = tasks.indexOfFirst { it.id == taskId }
        
        if (index != -1) {
            val updatedTask = tasks[index].copy(
                status = status,
                updatedAt = LocalDateTime.now()
            )
            tasks[index] = updatedTask
            _tasks.value = tasks
            
            // Update project progress
            updateProjectProgress(updatedTask.projectId)
            
            // Trigger safety check if task completed
            if (status == TaskStatus.COMPLETED && config.enableSafetyMonitoring) {
                safetyManager.performPostTaskSafetyCheck(updatedTask)
            }
            
            Log.i("ConstructionPlatform", "Task status updated: $taskId -> $status")
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Task not found: $taskId"))
        }
    } catch (e: Exception) {
        Log.e("ConstructionPlatform", "Error updating task status", e)
        Result.failure(e)
    }
    
    // === RESOURCE MANAGEMENT ===
    
    suspend fun allocateResource(resourceId: String, taskId: String, quantity: Double): Result<Unit> = try {
        Log.d("ConstructionPlatform", "Allocating resource $resourceId to task $taskId")
        
        val allocation = resourceManager.allocateResource(resourceId, taskId, quantity)
        if (allocation.isSuccess) {
            // Update resource availability
            updateResourceAvailability()
            
            // Optimize resource distribution
            if (config.enableAIOptimization) {
                aiOptimizer.optimizeResourceDistribution()
            }
            
            Result.success(Unit)
        } else {
            Result.failure(allocation.exceptionOrNull() ?: RuntimeException("Resource allocation failed"))
        }
    } catch (e: Exception) {
        Log.e("ConstructionPlatform", "Error allocating resource", e)
        Result.failure(e)
    }
    
    // === SAFETY MANAGEMENT ===
    
    suspend fun reportSafetyIncident(incidentData: SafetyIncidentData): Result<SafetyAlert> = try {
        Log.w("ConstructionPlatform", "Safety incident reported: ${incidentData.type}")
        
        val alert = SafetyAlert(
            id = UUID.randomUUID().toString(),
            type = incidentData.type,
            severity = incidentData.severity,
            description = incidentData.description,
            location = incidentData.location,
            reportedBy = incidentData.reportedBy,
            timestamp = LocalDateTime.now(),
            status = "OPEN",
            actionsTaken = emptyList(),
            relatedTaskId = incidentData.relatedTaskId
        )
        
        val updatedAlerts = _safetyAlerts.value + alert
        _safetyAlerts.value = updatedAlerts
        
        // Trigger immediate safety response
        safetyManager.respondToIncident(alert)
        
        // Stop related tasks if critical
        if (incidentData.severity == "CRITICAL") {
            stopRelatedTasks(incidentData.relatedTaskId)
        }
        
        Log.i("ConstructionPlatform", "Safety alert created: ${alert.id}")
        Result.success(alert)
    } catch (e: Exception) {
        Log.e("ConstructionPlatform", "Error reporting safety incident", e)
        Result.failure(e)
    }
    
    // === AI OPTIMIZATION ===
    
    suspend fun optimizeProject(projectId: String): Result<OptimizationResult> = try {
        Log.d("ConstructionPlatform", "Optimizing project: $projectId")
        
        val project = _projects.value.find { it.id == projectId }
        if (project == null) {
            return Result.failure(IllegalArgumentException("Project not found: $projectId"))
        }
        
        val optimization = aiOptimizer.optimizeProject(project, _tasks.value, _resources.value)
        
        // Apply optimizations
        applyOptimizations(optimization)
        
        Log.i("ConstructionPlatform", "Project optimization completed: $projectId")
        Result.success(optimization)
    } catch (e: Exception) {
        Log.e("ConstructionPlatform", "Error optimizing project", e)
        Result.failure(e)
    }
    
    // === PRIVATE METHODS ===
    
    private suspend fun loadProjects() {
        // Load projects from storage - simplified for demo
        val demoProjects = listOf(
            ConstructionProject(
                id = "proj_001",
                name = "Downtown Office Complex",
                description = "15-story office building with retail ground floor",
                address = "123 Downtown Ave",
                clientId = "client_001",
                projectManager = "pm_001",
                status = ProjectStatus.IN_PROGRESS,
                startDate = LocalDateTime.now().minusDays(30),
                estimatedEndDate = LocalDateTime.now().plusDays(300),
                budget = 5000000.0,
                currentCost = 1200000.0,
                phases = listOf(
                    ProjectPhase("phase_1", "Foundation", "Foundation and basement work", 
                        LocalDateTime.now().minusDays(30), LocalDateTime.now().plusDays(30), TaskStatus.IN_PROGRESS, listOf("Excavation", "Concrete pour"))
                ),
                tasks = emptyList(),
                documents = emptyList()
            )
        )
        
        _projects.value = demoProjects
    }
    
    private suspend fun loadResources() {
        val demoResources = listOf(
            Resource("res_001", "Concrete Mixer", "EQUIPMENT", "AVAILABLE", 2.0, "units"),
            Resource("res_002", "Steel Rebar", "MATERIAL", "LOW_STOCK", 500.0, "tons"),
            Resource("res_003", "Construction Workers", "LABOR", "AVAILABLE", 20.0, "workers"),
            Resource("res_004", "Tower Crane", "EQUIPMENT", "IN_USE", 1.0, "units")
        )
        
        _resources.value = demoResources
    }
    
    private suspend fun loadSafetyAlerts() {
        // Load recent safety alerts
        _safetyAlerts.value = emptyList()
    }
    
    private fun createProjectPhase(phaseData: PhaseCreationData): ProjectPhase {
        return ProjectPhase(
            id = UUID.randomUUID().toString(),
            name = phaseData.name,
            description = phaseData.description,
            startDate = phaseData.startDate,
            endDate = phaseData.endDate,
            status = TaskStatus.PENDING,
            milestones = phaseData.milestones
        )
    }
    
    private suspend fun initializeProjectResources(project: ConstructionProject) {
        // Initialize default resources for new project
        Log.d("ConstructionPlatform", "Initializing resources for project: ${project.id}")
    }
    
    private suspend fun initializeProjectTasks(project: ConstructionProject) {
        // Create initial tasks based on project phases
        Log.d("ConstructionPlatform", "Initializing tasks for project: ${project.id}")
    }
    
    private suspend fun loadProjectTasks(project: ConstructionProject) {
        val projectTasks = _tasks.value.filter { it.projectId == project.id }
        // Tasks are already loaded in the global tasks state
        Log.d("ConstructionPlatform", "Loaded ${projectTasks.size} tasks for project: ${project.id}")
    }
    
    private suspend fun updateProjectTasks(projectId: String, tasks: List<ConstructionTask>) {
        val projects = _projects.value.toMutableList()
        val index = projects.indexOfFirst { it.id == projectId }
        
        if (index != -1) {
            val nextGenTasks = tasks.map { constructionTask ->
                NextGenTask(
                    id = constructionTask.id,
                    title = constructionTask.title,
                    description = constructionTask.description,
                    assignedAgent = AgentType.MRM, // Default assignment
                    priority = Priority.MEDIUM,
                    status = constructionTask.status,
                    createdAt = constructionTask.createdAt,
                    updatedAt = constructionTask.updatedAt,
                    dueDate = constructionTask.dueDate,
                    dependencies = constructionTask.dependencies,
                    metadata = mapOf(
                        "category" to constructionTask.category,
                        "estimated_hours" to constructionTask.estimatedHours,
                        "actual_hours" to constructionTask.actualHours
                    )
                )
            }
            
            projects[index] = projects[index].copy(tasks = nextGenTasks)
            _projects.value = projects
        }
    }
    
    private suspend fun updateProjectProgress(projectId: String) {
        val projectTasks = _tasks.value.filter { it.projectId == projectId }
        val completedTasks = projectTasks.count { it.status == TaskStatus.COMPLETED }
        val progress = if (projectTasks.isNotEmpty()) completedTasks.toFloat() / projectTasks.size else 0f
        
        Log.d("ConstructionPlatform", "Project $projectId progress: ${(progress * 100).toInt()}%")
    }
    
    private suspend fun updateResourceAvailability() {
        // Recalculate resource availability based on current allocations
        resourceManager.updateAvailability()
    }
    
    private suspend fun stopRelatedTasks(taskId: String?) {
        if (taskId != null) {
            updateTaskStatus(taskId, TaskStatus.PAUSED)
        }
    }
    
    private suspend fun applyOptimizations(optimization: OptimizationResult) {
        // Apply the optimization recommendations
        Log.d("ConstructionPlatform", "Applying ${optimization.recommendations.size} optimizations")
    }
    
    private fun calculateResourceUtilization(): Double {
        val totalResources = _resources.value.size
        val inUseResources = _resources.value.count { it.status == "IN_USE" }
        return if (totalResources > 0) inUseResources.toDouble() / totalResources else 0.0
    }
    
    private fun saveProjectData() {
        Log.d("ConstructionPlatform", "Saving project data...")
    }
    
    // Helper classes
    
    private inner class ProjectManager {
        fun initialize() {
            Log.d("ProjectManager", "Initializing project manager")
        }
        
        fun shutdown() {
            Log.d("ProjectManager", "Shutting down project manager")
        }
    }
    
    private inner class ResourceManager {
        fun initialize() {
            Log.d("ResourceManager", "Initializing resource manager")
        }
        
        fun shutdown() {
            Log.d("ResourceManager", "Shutting down resource manager")
        }
        
        suspend fun allocateResource(resourceId: String, taskId: String, quantity: Double): Result<Unit> {
            return try {
                Log.d("ResourceManager", "Allocating $quantity of resource $resourceId to task $taskId")
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        
        fun updateAvailability() {
            Log.d("ResourceManager", "Updating resource availability")
        }
    }
    
    private inner class SafetyManager {
        fun initialize() {
            Log.d("SafetyManager", "Initializing safety manager")
        }
        
        fun shutdown() {
            Log.d("SafetyManager", "Shutting down safety manager")
        }
        
        suspend fun respondToIncident(alert: SafetyAlert) {
            Log.w("SafetyManager", "Responding to safety incident: ${alert.type}")
        }
        
        suspend fun performPostTaskSafetyCheck(task: ConstructionTask) {
            Log.d("SafetyManager", "Performing post-task safety check for: ${task.title}")
        }
    }
    
    private inner class QualityControl {
        fun initialize() {
            Log.d("QualityControl", "Initializing quality control")
        }
        
        fun shutdown() {
            Log.d("QualityControl", "Shutting down quality control")
        }
    }
    
    private inner class ConstructionAIOptimizer {
        fun initialize() {
            Log.d("ConstructionAIOptimizer", "Initializing AI optimizer")
        }
        
        fun shutdown() {
            Log.d("ConstructionAIOptimizer", "Shutting down AI optimizer")
        }
        
        suspend fun optimizeTaskAssignment(task: ConstructionTask) {
            Log.d("ConstructionAIOptimizer", "Optimizing assignment for task: ${task.title}")
        }
        
        suspend fun optimizeResourceDistribution() {
            Log.d("ConstructionAIOptimizer", "Optimizing resource distribution")
        }
        
        suspend fun optimizeProject(
            project: ConstructionProject,
            tasks: List<ConstructionTask>,
            resources: List<Resource>
        ): OptimizationResult {
            return OptimizationResult(
                projectId = project.id,
                optimizationType = "FULL_PROJECT",
                recommendations = listOf(
                    "Parallel execution of foundation tasks",
                    "Reallocate workers from Phase 1 to Phase 2",
                    "Optimize material delivery schedule"
                ),
                expectedTimeReduction = 15, // days
                expectedCostSavings = 75000.0,
                confidenceScore = 0.85,
                timestamp = LocalDateTime.now()
            )
        }
    }
    
    // Data classes
    
    data class ConstructionTask(
        val id: String,
        val title: String,
        val description: String,
        val projectId: String,
        val assignedTo: String,
        val priority: Priority,
        val status: TaskStatus,
        val category: String,
        val estimatedHours: Double,
        val actualHours: Double,
        val dependencies: List<String>,
        val materials: List<String>,
        val equipment: List<String>,
        val safetyRequirements: List<String>,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val dueDate: LocalDateTime?
    )
    
    data class Resource(
        val id: String,
        val name: String,
        val type: String, // EQUIPMENT, MATERIAL, LABOR
        val status: String, // AVAILABLE, IN_USE, MAINTENANCE, LOW_STOCK
        val quantity: Double,
        val unit: String
    )
    
    data class SafetyAlert(
        val id: String,
        val type: String,
        val severity: String,
        val description: String,
        val location: String,
        val reportedBy: String,
        val timestamp: LocalDateTime,
        val status: String,
        val actionsTaken: List<String>,
        val relatedTaskId: String?
    )
    
    data class ProjectCreationData(
        val name: String,
        val description: String,
        val address: String,
        val clientId: String,
        val projectManagerId: String,
        val startDate: LocalDateTime,
        val estimatedEndDate: LocalDateTime,
        val budget: Double,
        val phases: List<PhaseCreationData>
    )
    
    data class PhaseCreationData(
        val name: String,
        val description: String,
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
        val milestones: List<String>
    )
    
    data class TaskCreationData(
        val title: String,
        val description: String,
        val projectId: String,
        val assignedTo: String,
        val priority: Priority,
        val category: String,
        val estimatedHours: Double,
        val dependencies: List<String>,
        val materials: List<String>,
        val equipment: List<String>,
        val safetyRequirements: List<String>,
        val dueDate: LocalDateTime?
    )
    
    data class SafetyIncidentData(
        val type: String,
        val severity: String,
        val description: String,
        val location: String,
        val reportedBy: String,
        val relatedTaskId: String?
    )
    
    data class OptimizationResult(
        val projectId: String,
        val optimizationType: String,
        val recommendations: List<String>,
        val expectedTimeReduction: Int, // days
        val expectedCostSavings: Double,
        val confidenceScore: Double,
        val timestamp: LocalDateTime
    )
    
    data class ConstructionPlatformConfig(
        val enableAIOptimization: Boolean,
        val enableSafetyMonitoring: Boolean,
        val enableQualityControl: Boolean,
        val enableResourceTracking: Boolean,
        val enableRealtimeUpdates: Boolean,
        val enablePredictiveAnalytics: Boolean
    )
}

/**
 * Composable UI for the Construction Platform
 */
@Composable
fun ConstructionPlatformUI(
    platform: ConstructionPlatform,
    modifier: Modifier = Modifier
) {
    val projects by platform.projects.collectAsState()
    val activeProject by platform.activeProject.collectAsState()
    val tasks by platform.tasks.collectAsState()
    val resources by platform.resources.collectAsState()
    val safetyAlerts by platform.safetyAlerts.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Projects", "Tasks", "Resources", "Safety")
    
    Column(modifier = modifier.fillMaxSize()) {
        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Tab Content
        when (selectedTab) {
            0 -> DashboardTab(projects, tasks, resources, safetyAlerts)
            1 -> ProjectsTab(projects, activeProject) { platform.selectProject(it) }
            2 -> TasksTab(tasks, activeProject) { platform.updateTaskStatus(it, TaskStatus.COMPLETED) }
            3 -> ResourcesTab(resources)
            4 -> SafetyTab(safetyAlerts)
        }
    }
}

@Composable
private fun DashboardTab(
    projects: List<ConstructionProject>,
    tasks: List<ConstructionTask>,
    resources: List<Resource>,
    safetyAlerts: List<SafetyAlert>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Construction Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                item {
                    DashboardCard(
                        title = "Active Projects",
                        value = projects.count { it.status == ProjectStatus.IN_PROGRESS }.toString(),
                        icon = Icons.Default.Business,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    DashboardCard(
                        title = "Pending Tasks",
                        value = tasks.count { it.status == TaskStatus.PENDING }.toString(),
                        icon = Icons.Default.Assignment,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                item {
                    DashboardCard(
                        title = "Safety Alerts",
                        value = safetyAlerts.count { it.status == "OPEN" }.toString(),
                        icon = Icons.Default.Warning,
                        color = if (safetyAlerts.any { it.severity == "CRITICAL" }) Color.Red else MaterialTheme.colorScheme.tertiary
                    )
                }
                item {
                    DashboardCard(
                        title = "Resources",
                        value = resources.count { it.status == "AVAILABLE" }.toString(),
                        icon = Icons.Default.Build,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        
        if (safetyAlerts.isNotEmpty()) {
            item {
                SafetyAlertsSection(safetyAlerts.filter { it.status == "OPEN" })
            }
        }
        
        item {
            RecentActivitySection(tasks.filter { it.status == TaskStatus.COMPLETED }.take(5))
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SafetyAlertsSection(alerts: List<SafetyAlert>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Safety Alerts",
                    tint = Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Safety Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            alerts.take(3).forEach { alert ->
                SafetyAlertItem(alert)
            }
        }
    }
}

@Composable
private fun SafetyAlertItem(alert: SafetyAlert) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Badge(
            containerColor = when (alert.severity) {
                "CRITICAL" -> Color.Red
                "HIGH" -> Color(0xFFFF9800)
                else -> Color(0xFFFFC107)
            }
        ) {
            Text(
                text = alert.severity,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.type,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = alert.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RecentActivitySection(recentTasks: List<ConstructionTask>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (recentTasks.isEmpty()) {
                Text(
                    text = "No recent activity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            } else {
                recentTasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = task.updatedAt.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectsTab(
    projects: List<ConstructionProject>,
    activeProject: ConstructionProject?,
    onProjectSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Construction Projects",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(projects) { project ->
            ProjectItem(
                project = project,
                isActive = project.id == activeProject?.id,
                onSelected = { onProjectSelected(project.id) }
            )
        }
    }
}

@Composable
private fun ProjectItem(
    project: ConstructionProject,
    isActive: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelected,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer 
                          else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = project.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Badge(
                    containerColor = when (project.status) {
                        ProjectStatus.PLANNING -> Color(0xFFFFC107)
                        ProjectStatus.IN_PROGRESS -> Color(0xFF2196F3)
                        ProjectStatus.COMPLETED -> Color.Green
                        ProjectStatus.ON_HOLD -> Color(0xFFFF9800)
                        ProjectStatus.CANCELLED -> Color.Red
                        else -> MaterialTheme.colorScheme.outline
                    }
                ) {
                    Text(project.status.name)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Budget: $${(project.budget / 1000000).toInt()}M",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Progress: ${((project.currentCost / project.budget) * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun TasksTab(
    tasks: List<ConstructionTask>,
    activeProject: ConstructionProject?,
    onTaskCompleted: (String) -> Unit
) {
    val filteredTasks = if (activeProject != null) {
        tasks.filter { it.projectId == activeProject.id }
    } else {
        tasks
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = if (activeProject != null) "Tasks - ${activeProject.name}" else "All Tasks",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(filteredTasks) { task ->
            TaskItem(task, onTaskCompleted)
        }
    }
}

@Composable
private fun TaskItem(
    task: ConstructionTask,
    onTaskCompleted: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = task.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Assigned to: ${task.assignedTo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Badge(
                    containerColor = when (task.status) {
                        TaskStatus.PENDING -> Color(0xFFFFC107)
                        TaskStatus.IN_PROGRESS -> Color(0xFF2196F3)
                        TaskStatus.COMPLETED -> Color.Green
                        TaskStatus.PAUSED -> Color(0xFFFF9800)
                        TaskStatus.FAILED -> Color.Red
                        TaskStatus.CANCELLED -> Color.Gray
                    }
                ) {
                    Text(task.status.name)
                }
                
                if (task.status == TaskStatus.IN_PROGRESS) {
                    IconButton(onClick = { onTaskCompleted(task.id) }) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Complete Task")
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourcesTab(resources: List<Resource>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Resources",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(resources) { resource ->
            ResourceItem(resource)
        }
    }
}

@Composable
private fun ResourceItem(resource: Resource) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (resource.type) {
                    "EQUIPMENT" -> Icons.Default.Build
                    "MATERIAL" -> Icons.Default.Inventory
                    "LABOR" -> Icons.Default.People
                    else -> Icons.Default.Category
                },
                contentDescription = resource.type,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${resource.quantity} ${resource.unit}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = resource.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Badge(
                containerColor = when (resource.status) {
                    "AVAILABLE" -> Color.Green
                    "IN_USE" -> Color(0xFF2196F3)
                    "MAINTENANCE" -> Color(0xFFFF9800)
                    "LOW_STOCK" -> Color.Red
                    else -> MaterialTheme.colorScheme.outline
                }
            ) {
                Text(resource.status)
            }
        }
    }
}

@Composable
private fun SafetyTab(safetyAlerts: List<SafetyAlert>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Safety Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (safetyAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "All Clear",
                            tint = Color.Green
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "No active safety alerts - All clear!",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        } else {
            items(safetyAlerts) { alert ->
                SafetyAlertDetailItem(alert)
            }
        }
    }
}

@Composable
private fun SafetyAlertDetailItem(alert: SafetyAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.severity) {
                "CRITICAL" -> Color.Red.copy(alpha = 0.1f)
                "HIGH" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> Color(0xFFFFC107).copy(alpha = 0.1f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.type,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = alert.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Badge(
                    containerColor = when (alert.severity) {
                        "CRITICAL" -> Color.Red
                        "HIGH" -> Color(0xFFFF9800)
                        else -> Color(0xFFFFC107)
                    }
                ) {
                    Text(alert.severity)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Location: ${alert.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = alert.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}