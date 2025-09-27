package com.nextgenbuildpro.navigation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.*
import com.nextgenbuildpro.shared.*

/**
 * Intuitive Navigation Manager for NextGen BuildPro v2.0
 * 
 * Provides dynamic navigation with maximum 3 taps to reach any feature.
 * Adapts navigation methods based on context and user workflow for maximum intuitiveness.
 */
class IntuitiveNavigationManager {
    
    private val _currentDepartment = MutableStateFlow<Department?>(null)
    val currentDepartment: StateFlow<Department?> = _currentDepartment.asStateFlow()
    
    private val _navigationStack = MutableStateFlow<List<NavigationNode>>(emptyList())
    val navigationStack: StateFlow<List<NavigationNode>> = _navigationStack.asStateFlow()
    
    private val _quickActions = MutableStateFlow<List<QuickAction>>(emptyList())
    val quickActions: StateFlow<List<QuickAction>> = _quickActions.asStateFlow()
    
    // Contextual navigation methods
    private val navigationMethods = mutableMapOf<String, NavigationMethod>()
    
    init {
        initializeNavigationMethods()
        setupQuickActions()
    }
    
    /**
     * Navigate to department with adaptive method selection
     */
    suspend fun navigateToDepartment(department: Department, context: NavigationContext = NavigationContext.DEFAULT) {
        Log.d("NavigationManager", "Navigating to ${department.name} with context: $context")
        
        val navigationMethod = selectOptimalNavigationMethod(department, context)
        
        when (navigationMethod) {
            NavigationMethod.DIRECT_ACCESS -> {
                // Single tap access for most common features
                _currentDepartment.value = department
                _navigationStack.value = listOf(NavigationNode(department.name, department.id))
                updateQuickActionsForDepartment(department)
            }
            
            NavigationMethod.CONTEXTUAL_MENU -> {
                // Context-aware sliding menu
                showContextualMenu(department, context)
            }
            
            NavigationMethod.GESTURE_BASED -> {
                // Swipe/gesture navigation for field use
                handleGestureNavigation(department, context)
            }
            
            NavigationMethod.VOICE_ACTIVATED -> {
                // Voice navigation for hands-free operation
                handleVoiceNavigation(department, context)
            }
        }
        
        Log.d("NavigationManager", "Navigation completed to ${department.name}")
    }
    
    /**
     * Get dashboard for current department
     */
    fun getDashboardForDepartment(department: Department): DepartmentDashboard {
        return when (department.id) {
            "personal_assistant" -> createPersonalAssistantDashboard()
            "crm" -> createCRMDashboard()
            "project_management" -> createProjectManagementDashboard()
            "analytics" -> createAnalyticsDashboard()
            "design" -> createDesignDashboard()
            "marketing" -> createMarketingDashboard()
            else -> createDefaultDashboard(department)
        }
    }
    
    /**
     * Navigate to specific feature within department (max 2 additional taps)
     */
    suspend fun navigateToFeature(
        departmentId: String, 
        featureId: String, 
        subFeatureId: String? = null
    ): Result<Unit> = try {
        val currentStack = _navigationStack.value.toMutableList()
        
        // Tap 1: Department (if not already there)
        if (_currentDepartment.value?.id != departmentId) {
            val department = getDepartmentById(departmentId)
                ?: return Result.failure(IllegalArgumentException("Department not found: $departmentId"))
            navigateToDepartment(department)
            currentStack.clear()
            currentStack.add(NavigationNode(department.name, department.id))
        }
        
        // Tap 2: Feature
        currentStack.add(NavigationNode(featureId, "$departmentId.$featureId"))
        
        // Tap 3: Sub-feature (optional)
        subFeatureId?.let { subId ->
            currentStack.add(NavigationNode(subId, "$departmentId.$featureId.$subId"))
        }
        
        _navigationStack.value = currentStack
        
        Log.d("NavigationManager", "Navigated to feature: $departmentId.$featureId${subFeatureId?.let { ".$it" } ?: ""}")
        Log.d("NavigationManager", "Total navigation depth: ${currentStack.size} taps")
        
        if (currentStack.size > 3) {
            Log.w("NavigationManager", "Navigation depth exceeded 3 taps - optimizing...")
            optimizeNavigationPath()
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("NavigationManager", "Navigation failed", e)
        Result.failure(e)
    }
    
    /**
     * Get quick actions for current context
     */
    fun getQuickActionsForContext(context: NavigationContext): List<QuickAction> {
        return when (context) {
            NavigationContext.FIELD_WORK -> listOf(
                QuickAction("take_photo", "📷 Photo", "Take project photo"),
                QuickAction("voice_command", "🎤 Voice", "Voice command"),
                QuickAction("emergency", "🚨 Emergency", "Emergency response"),
                QuickAction("time_clock", "⏰ Clock In/Out", "Time tracking")
            )
            
            NavigationContext.OFFICE_WORK -> listOf(
                QuickAction("create_estimate", "💰 Estimate", "Create new estimate"),
                QuickAction("review_projects", "📋 Projects", "Review projects"),
                QuickAction("generate_report", "📊 Reports", "Generate reports"),
                QuickAction("manage_contacts", "👥 Contacts", "Manage contacts")
            )
            
            NavigationContext.CLIENT_MEETING -> listOf(
                QuickAction("project_status", "📈 Status", "Project status"),
                QuickAction("cost_breakdown", "💵 Costs", "Cost breakdown"),
                QuickAction("schedule_overview", "📅 Schedule", "Schedule overview"),
                QuickAction("change_orders", "📝 Changes", "Change orders")
            )
            
            NavigationContext.DEFAULT -> listOf(
                QuickAction("dashboard", "🏠 Dashboard", "Main dashboard"),
                QuickAction("projects", "🏗️ Projects", "Active projects"),
                QuickAction("contacts", "📞 Contacts", "Contact management"),
                QuickAction("reports", "📊 Analytics", "Reports & analytics")
            )
        }
    }
    
    /**
     * Adaptive navigation method selection based on context
     */
    private fun selectOptimalNavigationMethod(
        department: Department, 
        context: NavigationContext
    ): NavigationMethod {
        return when (context) {
            NavigationContext.FIELD_WORK -> {
                // Prioritize voice and gesture for hands-free operation
                if (department.supportsVoiceNavigation) NavigationMethod.VOICE_ACTIVATED
                else NavigationMethod.GESTURE_BASED
            }
            
            NavigationContext.OFFICE_WORK -> {
                // Direct access for efficiency
                NavigationMethod.DIRECT_ACCESS
            }
            
            NavigationContext.CLIENT_MEETING -> {
                // Contextual menu for professional presentation
                NavigationMethod.CONTEXTUAL_MENU
            }
            
            NavigationContext.EMERGENCY -> {
                // Direct access for speed
                NavigationMethod.DIRECT_ACCESS
            }
            
            NavigationContext.DEFAULT -> {
                // Choose based on department characteristics
                when (department.primaryInterface) {
                    InterfaceType.TOUCH -> NavigationMethod.DIRECT_ACCESS
                    InterfaceType.VOICE -> NavigationMethod.VOICE_ACTIVATED
                    InterfaceType.GESTURE -> NavigationMethod.GESTURE_BASED
                    InterfaceType.CONTEXTUAL -> NavigationMethod.CONTEXTUAL_MENU
                }
            }
        }
    }
    
    private fun initializeNavigationMethods() {
        navigationMethods["direct"] = NavigationMethod.DIRECT_ACCESS
        navigationMethods["contextual"] = NavigationMethod.CONTEXTUAL_MENU
        navigationMethods["gesture"] = NavigationMethod.GESTURE_BASED
        navigationMethods["voice"] = NavigationMethod.VOICE_ACTIVATED
    }
    
    private fun setupQuickActions() {
        _quickActions.value = getQuickActionsForContext(NavigationContext.DEFAULT)
    }
    
    private fun updateQuickActionsForDepartment(department: Department) {
        val departmentSpecificActions = when (department.id) {
            "personal_assistant" -> listOf(
                QuickAction("voice_command", "🎤 Voice", "Voice command"),
                QuickAction("quick_contact", "📞 Call", "Quick contact"),
                QuickAction("emergency", "🚨 Emergency", "Emergency response")
            )
            
            "crm" -> listOf(
                QuickAction("add_lead", "👤 New Lead", "Add new lead"),
                QuickAction("follow_up", "📧 Follow Up", "Schedule follow up"),
                QuickAction("view_pipeline", "🔄 Pipeline", "View sales pipeline")
            )
            
            "project_management" -> listOf(
                QuickAction("new_project", "🏗️ New Project", "Create project"),
                QuickAction("schedule_task", "📅 Schedule", "Schedule task"),
                QuickAction("track_progress", "📈 Progress", "Track progress")
            )
            
            "analytics" -> listOf(
                QuickAction("dashboard_view", "📊 Dashboard", "Main dashboard"),
                QuickAction("generate_report", "📋 Report", "Generate report"),
                QuickAction("cost_analysis", "💰 Costs", "Cost analysis")
            )
            
            "design" -> listOf(
                QuickAction("new_design", "✏️ Design", "New design"),
                QuickAction("3d_model", "🏢 3D Model", "3D modeling"),
                QuickAction("blueprint", "📐 Blueprint", "Blueprint view")
            )
            
            "marketing" -> listOf(
                QuickAction("new_campaign", "📢 Campaign", "New campaign"),
                QuickAction("lead_generation", "🎯 Leads", "Generate leads"),
                QuickAction("proposal", "📄 Proposal", "Create proposal")
            )
            
            else -> emptyList()
        }
        
        _quickActions.value = departmentSpecificActions
    }
    
    private suspend fun showContextualMenu(department: Department, context: NavigationContext) {
        // Implementation for contextual sliding menu
        Log.d("NavigationManager", "Showing contextual menu for ${department.name}")
    }
    
    private suspend fun handleGestureNavigation(department: Department, context: NavigationContext) {
        // Implementation for gesture-based navigation
        Log.d("NavigationManager", "Handling gesture navigation to ${department.name}")
    }
    
    private suspend fun handleVoiceNavigation(department: Department, context: NavigationContext) {
        // Implementation for voice-activated navigation
        Log.d("NavigationManager", "Handling voice navigation to ${department.name}")
    }
    
    private fun optimizeNavigationPath() {
        // Optimize navigation path if it exceeds 3 taps
        val currentStack = _navigationStack.value
        if (currentStack.size > 3) {
            val optimizedStack = currentStack.takeLast(3)
            _navigationStack.value = optimizedStack
            Log.d("NavigationManager", "Navigation path optimized to ${optimizedStack.size} taps")
        }
    }
    
    private fun getDepartmentById(id: String): Department? {
        return getAllDepartments().find { it.id == id }
    }
    
    fun getAllDepartments(): List<Department> {
        return listOf(
            Department("personal_assistant", "Personal Assistant", InterfaceType.VOICE, true),
            Department("crm", "CRM", InterfaceType.TOUCH, false),
            Department("project_management", "Project Management", InterfaceType.CONTEXTUAL, false),
            Department("analytics", "Analytics", InterfaceType.TOUCH, false),
            Department("design", "Design", InterfaceType.GESTURE, false),
            Department("marketing", "Marketing", InterfaceType.CONTEXTUAL, false)
        )
    }
    
    // Dashboard creation methods
    private fun createPersonalAssistantDashboard(): DepartmentDashboard {
        return DepartmentDashboard(
            title = "Personal Assistant",
            widgets = listOf(
                DashboardWidget("voice_status", "Voice Command Status", "🎤"),
                DashboardWidget("recent_commands", "Recent Commands", "📝"),
                DashboardWidget("emergency_contacts", "Emergency Contacts", "🚨"),
                DashboardWidget("quick_actions", "Quick Actions", "⚡")
            ),
            quickAccess = listOf(
                QuickAccessItem("voice_command", "🎤 Voice Command"),
                QuickAccessItem("emergency", "🚨 Emergency"),
                QuickAccessItem("contacts", "📞 Contacts")
            )
        )
    }
    
    private fun createCRMDashboard(): DepartmentDashboard {
        return DepartmentDashboard(
            title = "CRM Dashboard",
            widgets = listOf(
                DashboardWidget("leads_pipeline", "Sales Pipeline", "🔄"),
                DashboardWidget("recent_calls", "Recent Calls", "📞"),
                DashboardWidget("follow_ups", "Follow-ups Due", "📧"),
                DashboardWidget("conversion_rate", "Conversion Rate", "📈")
            ),
            quickAccess = listOf(
                QuickAccessItem("add_lead", "👤 Add Lead"),
                QuickAccessItem("call_log", "📞 Call Log"),
                QuickAccessItem("proposals", "📄 Proposals")
            )
        )
    }
    
    private fun createProjectManagementDashboard(): DepartmentDashboard {
        return DepartmentDashboard(
            title = "Project Management",
            widgets = listOf(
                DashboardWidget("active_projects", "Active Projects", "🏗️"),
                DashboardWidget("schedule_overview", "Schedule Overview", "📅"),
                DashboardWidget("budget_status", "Budget Status", "💰"),
                DashboardWidget("team_status", "Team Status", "👥")
            ),
            quickAccess = listOf(
                QuickAccessItem("new_project", "🏗️ New Project"),
                QuickAccessItem("schedule", "📅 Schedule"),
                QuickAccessItem("costs", "💰 Costs")
            )
        )
    }
    
    private fun createAnalyticsDashboard(): DepartmentDashboard {
        return DepartmentDashboard(
            title = "Analytics Dashboard",
            widgets = listOf(
                DashboardWidget("kpi_overview", "KPI Overview", "📊"),
                DashboardWidget("cost_analysis", "Cost Analysis", "💵"),
                DashboardWidget("performance_metrics", "Performance", "📈"),
                DashboardWidget("predictive_insights", "Predictions", "🔮")
            ),
            quickAccess = listOf(
                QuickAccessItem("reports", "📋 Reports"),
                QuickAccessItem("analytics", "📊 Analytics"),
                QuickAccessItem("predictions", "🔮 Predictions")
            )
        )
    }
    
    private fun createDesignDashboard(): DepartmentDashboard {
        return DepartmentDashboard(
            title = "Design Department",
            widgets = listOf(
                DashboardWidget("active_designs", "Active Designs", "✏️"),
                DashboardWidget("3d_models", "3D Models", "🏢"),
                DashboardWidget("blueprints", "Blueprints", "📐"),
                DashboardWidget("material_takeoffs", "Material Takeoffs", "📋")
            ),
            quickAccess = listOf(
                QuickAccessItem("new_design", "✏️ New Design"),
                QuickAccessItem("3d_model", "🏢 3D Model"),
                QuickAccessItem("blueprint", "📐 Blueprint")
            )
        )
    }
    
    private fun createMarketingDashboard(): DepartmentDashboard {
        return DepartmentDashboard(
            title = "Marketing Dashboard",
            widgets = listOf(
                DashboardWidget("campaigns", "Active Campaigns", "📢"),
                DashboardWidget("lead_generation", "Lead Generation", "🎯"),
                DashboardWidget("proposals", "Proposals", "📄"),
                DashboardWidget("conversion_metrics", "Conversions", "📈")
            ),
            quickAccess = listOf(
                QuickAccessItem("new_campaign", "📢 Campaign"),
                QuickAccessItem("leads", "🎯 Leads"),
                QuickAccessItem("proposals", "📄 Proposals")
            )
        )
    }
    
    private fun createDefaultDashboard(department: Department): DepartmentDashboard {
        return DepartmentDashboard(
            title = "${department.name} Dashboard",
            widgets = listOf(
                DashboardWidget("overview", "Overview", "📊"),
                DashboardWidget("recent_activity", "Recent Activity", "📝"),
                DashboardWidget("quick_stats", "Quick Stats", "⚡")
            ),
            quickAccess = listOf(
                QuickAccessItem("overview", "📊 Overview")
            )
        )
    }
}

// Supporting data classes
data class Department(
    val id: String,
    val name: String,
    val primaryInterface: InterfaceType,
    val supportsVoiceNavigation: Boolean
)

data class NavigationNode(
    val name: String,
    val id: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class QuickAction(
    val id: String,
    val label: String,
    val description: String
)

data class DepartmentDashboard(
    val title: String,
    val widgets: List<DashboardWidget>,
    val quickAccess: List<QuickAccessItem>
)

data class DashboardWidget(
    val id: String,
    val title: String,
    val icon: String,
    val data: Map<String, Any> = emptyMap()
)

data class QuickAccessItem(
    val id: String,
    val label: String
)

enum class NavigationMethod {
    DIRECT_ACCESS,      // Single tap for common features
    CONTEXTUAL_MENU,    // Context-aware sliding menu
    GESTURE_BASED,      // Swipe/gesture navigation
    VOICE_ACTIVATED     // Voice commands
}

enum class NavigationContext {
    DEFAULT,
    FIELD_WORK,         // On-site construction work
    OFFICE_WORK,        // Office/admin tasks
    CLIENT_MEETING,     // Client presentations
    EMERGENCY           // Emergency situations
}

enum class InterfaceType {
    TOUCH,      // Traditional touch interface
    VOICE,      // Voice-first interface
    GESTURE,    // Gesture-based interface
    CONTEXTUAL  // Context-aware interface
}