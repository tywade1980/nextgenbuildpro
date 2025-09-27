package com.nextgenbuildpro.orchestrators

import com.nextgenbuildpro.shared.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.content.Context
import android.util.Log
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.pow

/**
 * Analytics Orchestrator
 * 
 * Provides comprehensive data analysis, reporting, performance metrics,
 * predictive insights, cost tracking, and executive dashboards.
 * Includes advanced AI-powered analytics for construction management.
 */
class AnalyticsOrchestrator(
    private val context: Context
) : DepartmentalOrchestrator {
    
    companion object {
        private const val TAG = "AnalyticsOrchestrator"
    }
    
    override val agentType: AgentType = AgentType.ANALYTICS_ORCHESTRATOR
    override val departmentName: String = "Analytics & Reporting"
    
    private val _status = MutableStateFlow(SystemStatus.INITIALIZING)
    override val status: StateFlow<SystemStatus> = _status.asStateFlow()
    
    private val _sharedContext = MutableStateFlow(SharedContext(
        currentProjects = emptyList(),
        activeClients = emptyList(),
        systemMetrics = PerformanceMetrics(0.0, 0.0, 0.0, 0.0, 0.0)
    ))
    override val sharedContext: StateFlow<SharedContext> = _sharedContext.asStateFlow()
    
    private val mutex = Mutex()
    private val knowledgeBase = mutableMapOf<String, Any>()
    private val metricsDatabase = mutableMapOf<String, AnalyticsMetric>()
    private val reportTemplates = mutableMapOf<String, ReportTemplate>()
    private val dashboards = mutableMapOf<String, Dashboard>()
    private val predictiveModels = mutableMapOf<String, PredictiveModel>()
    private val kpiDefinitions = mutableMapOf<String, KPIDefinition>()
    private val dataProcessors = mutableListOf<DataProcessor>()
    
    override val toolsets = listOf(
        OrchestratorTool(
            name = "Executive Dashboard",
            description = "High-level KPIs and project health visualization",
            toolType = ToolType.REPORTING_TOOL,
            permissions = listOf(Permission.READ_CALENDAR, Permission.INTERNET_ACCESS)
        ),
        OrchestratorTool(
            name = "Financial Analytics",
            description = "Profit/loss, cash flow, budget variance analysis",
            toolType = ToolType.DATA_ANALYSIS,
            permissions = listOf(Permission.INTERNET_ACCESS)
        ),
        OrchestratorTool(
            name = "Performance Analytics",
            description = "Crew productivity, project efficiency, timeline analysis",
            toolType = ToolType.DATA_ANALYSIS,
            permissions = listOf(Permission.ACCESS_LOCATION, Permission.READ_CALENDAR)
        ),
        OrchestratorTool(
            name = "Predictive Analytics",
            description = "Cost prediction, schedule optimization, risk assessment",
            toolType = ToolType.AI_SERVICE,
            permissions = listOf(Permission.INTERNET_ACCESS)
        ),
        OrchestratorTool(
            name = "Compliance Reporting",
            description = "Safety, quality, regulatory compliance reports",
            toolType = ToolType.REPORTING_TOOL,
            permissions = listOf(Permission.ACCESS_STORAGE, Permission.INTERNET_ACCESS)
        ),
        OrchestratorTool(
            name = "Custom Report Builder",
            description = "Drag-and-drop report creation interface",
            toolType = ToolType.REPORTING_TOOL,
            permissions = listOf(Permission.ACCESS_STORAGE)
        ),
        OrchestratorTool(
            name = "Real-Time Monitoring",
            description = "Live project metrics and performance tracking",
            toolType = ToolType.SYSTEM_INTEGRATION,
            permissions = listOf(Permission.ACCESS_LOCATION, Permission.INTERNET_ACCESS)
        ),
        OrchestratorTool(
            name = "Data Visualization",
            description = "Advanced charts, graphs, and interactive visualizations",
            toolType = ToolType.REPORTING_TOOL,
            permissions = listOf(Permission.ACCESS_STORAGE)
        )
    )
    
    override val capabilities = listOf(
        AgentCapability(
            name = "Advanced Data Analysis",
            description = "Complex statistical analysis and data mining",
            inputTypes = listOf("ProjectData", "FinancialData", "PerformanceMetrics"),
            outputTypes = listOf("AnalyticsReport", "DataInsights", "TrendAnalysis"),
            skillLevel = SkillLevel.EXPERT
        ),
        AgentCapability(
            name = "Predictive Modeling",
            description = "Machine learning-based prediction and forecasting",
            inputTypes = listOf("HistoricalData", "ProjectParameters", "MarketConditions"),
            outputTypes = listOf("CostPrediction", "ScheduleForecast", "RiskAssessment"),
            skillLevel = SkillLevel.EXPERT
        ),
        AgentCapability(
            name = "Financial Intelligence",
            description = "Comprehensive financial analysis and reporting",
            inputTypes = listOf("CostData", "RevenueData", "CashFlowData"),
            outputTypes = listOf("FinancialReport", "ProfitAnalysis", "BudgetVariance"),
            skillLevel = SkillLevel.ADVANCED
        ),
        AgentCapability(
            name = "Performance Optimization",
            description = "Identify optimization opportunities and efficiency gains",
            inputTypes = listOf("ProductivityData", "ResourceUtilization", "TimelineData"),
            outputTypes = listOf("OptimizationPlan", "EfficiencyReport", "ResourceRecommendations"),
            skillLevel = SkillLevel.EXPERT
        ),
        AgentCapability(
            name = "Real-Time Dashboard Creation",
            description = "Dynamic dashboard generation with live data",
            inputTypes = listOf("DataSources", "UserRequirements", "KPIDefinitions"),
            outputTypes = listOf("InteractiveDashboard", "LiveMetrics", "AlertSystem"),
            skillLevel = SkillLevel.ADVANCED
        )
    )
    
    override suspend fun initialize(): Result<Unit> = try {
        mutex.withLock {
            Log.i(TAG, "Initializing Analytics Orchestrator...")
            
            _status.value = SystemStatus.INITIALIZING
            
            // Initialize analytics engine
            initializeAnalyticsEngine()
            
            // Set up KPI definitions
            setupKPIDefinitions()
            
            // Initialize report templates
            initializeReportTemplates()
            
            // Set up executive dashboards
            setupExecutiveDashboards()
            
            // Initialize predictive models
            initializePredictiveModels()
            
            // Start data processors
            startDataProcessors()
            
            _status.value = SystemStatus.ACTIVE
            Log.i(TAG, "Analytics Orchestrator initialized successfully")
            
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Analytics Orchestrator", e)
        _status.value = SystemStatus.ERROR
        Result.failure(e)
    }
    
    override suspend fun processVoiceCommand(command: String): Result<String> = try {
        Log.d(TAG, "Processing Analytics voice command: $command")
        
        val response = when {
            command.contains("show dashboard", ignoreCase = true) -> {
                handleShowDashboard(command)
            }
            command.contains("generate report", ignoreCase = true) -> {
                handleGenerateReport(command)
            }
            command.contains("project performance", ignoreCase = true) -> {
                handleProjectPerformance(command)
            }
            command.contains("cost analysis", ignoreCase = true) -> {
                handleCostAnalysis(command)
            }
            command.contains("predict", ignoreCase = true) -> {
                handlePredictiveAnalysis(command)
            }
            command.contains("kpi", ignoreCase = true) || command.contains("metrics", ignoreCase = true) -> {
                handleKPIQuery(command)
            }
            else -> "Analytics command not recognized. Available commands: show dashboard, generate report, project performance, cost analysis, predict, kpi/metrics"
        }
        
        Result.success(response)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to process Analytics voice command: $command", e)
        Result.failure(e)
    }
    
    override suspend fun executeTask(task: NextGenTask): Result<NextGenTask> = try {
        Log.d(TAG, "Executing Analytics task: ${task.title}")
        
        val updatedTask = when (task.title.lowercase()) {
            "generate executive dashboard" -> handleGenerateExecutiveDashboard(task)
            "analyze project profitability" -> handleAnalyzeProjectProfitability(task)
            "create performance report" -> handleCreatePerformanceReport(task)
            "predict project costs" -> handlePredictProjectCosts(task)
            "track kpi metrics" -> handleTrackKPIMetrics(task)
            "analyze crew productivity" -> handleAnalyzeCrewProductivity(task)
            "generate compliance report" -> handleGenerateComplianceReport(task)
            else -> handleGenericAnalyticsTask(task)
        }
        
        Result.success(updatedTask)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to execute Analytics task: ${task.title}", e)
        Result.failure(e)
    }
    
    // Core Analytics Methods
    
    suspend fun generateExecutiveDashboard(): Result<ExecutiveDashboard> = try {
        Log.d(TAG, "Generating executive dashboard...")
        
        val kpis = calculateExecutiveKPIs()
        val projectHealth = analyzeProjectHealth()
        val financialSummary = generateFinancialSummary()
        val performanceMetrics = calculatePerformanceMetrics()
        
        val dashboard = ExecutiveDashboard(
            id = UUID.randomUUID().toString(),
            generatedAt = LocalDateTime.now(),
            kpis = kpis,
            projectHealth = projectHealth,
            financialSummary = financialSummary,
            performanceMetrics = performanceMetrics,
            alerts = generateAlerts()
        )
        
        dashboards["executive"] = Dashboard(
            id = "executive",
            name = "Executive Dashboard",
            type = DashboardType.EXECUTIVE,
            components = listOf("KPIs", "Project Health", "Financial Summary", "Performance Metrics")
        )
        
        Log.i(TAG, "Executive dashboard generated successfully")
        Result.success(dashboard)
        
    } catch (e: Exception) {
        Log.e(TAG, "Failed to generate executive dashboard", e)
        Result.failure(e)
    }
    
    suspend fun predictProjectCosts(
        projectData: Map<String, Any>
    ): Result<CostPrediction> = try {
        
        val model = predictiveModels["cost_prediction"]
            ?: return Result.failure(Exception("Cost prediction model not found"))
        
        val squareFootage = projectData["square_footage"] as? Double ?: 2000.0
        val projectType = projectData["project_type"] as? String ?: "residential"
        val complexity = projectData["complexity"] as? String ?: "medium"
        
        // Simplified ML prediction - in real implementation would use trained model
        val baseCost = squareFootage * 150.0
        val complexityMultiplier = when (complexity.lowercase()) {
            "low" -> 0.85
            "medium" -> 1.0
            "high" -> 1.25
            "very_high" -> 1.5
            else -> 1.0
        }
        
        val typeMultiplier = when (projectType.lowercase()) {
            "residential" -> 1.0
            "commercial" -> 1.3
            "industrial" -> 1.5
            else -> 1.0
        }
        
        val predictedCost = baseCost * complexityMultiplier * typeMultiplier
        val confidence = calculatePredictionConfidence(projectData)
        val variance = predictedCost * 0.15 // ±15% variance
        
        val prediction = CostPrediction(
            projectId = projectData["project_id"] as? String ?: "unknown",
            predictedCost = predictedCost,
            confidenceLevel = confidence,
            variance = variance,
            factors = listOf(
                PredictionFactor("Square Footage", squareFootage, 0.6),
                PredictionFactor("Project Type", projectType, 0.25),
                PredictionFactor("Complexity", complexity, 0.15)
            ),
            modelVersion = model.version,
            createdAt = LocalDateTime.now()
        )
        
        Log.i(TAG, "Cost prediction generated: $${predictedCost} (±${variance})")
        Result.success(prediction)
        
    } catch (e: Exception) {
        Log.e(TAG, "Failed to predict project costs", e)
        Result.failure(e)
    }
    
    suspend fun analyzeCrewProductivity(
        timeframe: String = "last_30_days"
    ): Result<ProductivityAnalysis> = try {
        
        Log.d(TAG, "Analyzing crew productivity for timeframe: $timeframe")
        
        // Simulate productivity data
        val crewData = mapOf(
            "Framing Crew A" to ProductivityMetrics(85.0, 120.0, 95.0, 32),
            "Electrical Team B" to ProductivityMetrics(92.0, 95.0, 88.0, 28),
            "Plumbing Squad C" to ProductivityMetrics(78.0, 110.0, 92.0, 35)
        )
        
        val analysis = ProductivityAnalysis(
            timeframe = timeframe,
            crewMetrics = crewData,
            averageEfficiency = crewData.values.map { it.efficiency }.average(),
            topPerformers = crewData.filter { it.value.efficiency > 90.0 }.keys.toList(),
            improvementAreas = crewData.filter { it.value.efficiency < 85.0 }.keys.toList(),
            recommendations = generateProductivityRecommendations(crewData),
            generatedAt = LocalDateTime.now()
        )
        
        Log.i(TAG, "Crew productivity analysis completed - Average efficiency: ${analysis.averageEfficiency}%")
        Result.success(analysis)
        
    } catch (e: Exception) {
        Log.e(TAG, "Failed to analyze crew productivity", e)
        Result.failure(e)
    }
    
    // Implementation methods
    
    private fun initializeAnalyticsEngine() {
        Log.d(TAG, "Initializing analytics engine...")
        knowledgeBase["analytics_engine_active"] = true
        knowledgeBase["data_sources_connected"] = 5
    }
    
    private fun setupKPIDefinitions() {
        Log.d(TAG, "Setting up KPI definitions...")
        
        kpiDefinitions.putAll(mapOf(
            "project_completion_rate" to KPIDefinition(
                "Project Completion Rate",
                "Percentage of projects completed on time",
                "percentage",
                90.0 // target
            ),
            "budget_variance" to KPIDefinition(
                "Budget Variance",
                "Average percentage variance from project budgets",
                "percentage",
                5.0 // target ±5%
            ),
            "client_satisfaction" to KPIDefinition(
                "Client Satisfaction",
                "Average client satisfaction score",
                "score",
                4.5 // target out of 5
            ),
            "safety_incidents" to KPIDefinition(
                "Safety Incidents",
                "Number of safety incidents per month",
                "count",
                0.0 // target zero incidents
            ),
            "profit_margin" to KPIDefinition(
                "Profit Margin",
                "Average profit margin on projects",
                "percentage",
                15.0 // target 15%
            )
        ))
        
        knowledgeBase["kpi_definitions_loaded"] = kpiDefinitions.size
    }
    
    private fun initializeReportTemplates() {
        Log.d(TAG, "Initializing report templates...")
        
        reportTemplates.putAll(mapOf(
            "executive_summary" to ReportTemplate(
                "Executive Summary",
                "High-level project and financial overview",
                ReportType.EXECUTIVE,
                listOf("KPIs", "Financial Summary", "Project Status")
            ),
            "financial_report" to ReportTemplate(
                "Financial Report",
                "Detailed financial analysis and projections",
                ReportType.FINANCIAL,
                listOf("P&L", "Cash Flow", "Budget Analysis")
            ),
            "performance_report" to ReportTemplate(
                "Performance Report", 
                "Crew and project performance analysis",
                ReportType.PERFORMANCE,
                listOf("Productivity", "Timeline Analysis", "Quality Metrics")
            )
        ))
        
        knowledgeBase["report_templates_loaded"] = reportTemplates.size
    }
    
    private fun setupExecutiveDashboards() {
        Log.d(TAG, "Setting up executive dashboards...")
        knowledgeBase["executive_dashboards_configured"] = true
    }
    
    private fun initializePredictiveModels() {
        Log.d(TAG, "Initializing predictive models...")
        
        predictiveModels.putAll(mapOf(
            "cost_prediction" to PredictiveModel(
                "Cost Prediction Model",
                "Predicts project costs based on historical data",
                "1.2.0",
                0.87 // accuracy
            ),
            "schedule_optimization" to PredictiveModel(
                "Schedule Optimization Model",
                "Optimizes project schedules based on constraints",
                "1.1.0",
                0.82
            ),
            "risk_assessment" to PredictiveModel(
                "Risk Assessment Model",
                "Identifies potential project risks",
                "1.0.1",
                0.79
            )
        ))
        
        knowledgeBase["predictive_models_loaded"] = predictiveModels.size
    }
    
    private fun startDataProcessors() {
        Log.d(TAG, "Starting data processors...")
        
        dataProcessors.addAll(listOf(
            DataProcessor("financial_processor", "Processes financial data"),
            DataProcessor("performance_processor", "Processes performance metrics"),
            DataProcessor("compliance_processor", "Processes compliance data")
        ))
        
        knowledgeBase["data_processors_active"] = dataProcessors.size
    }
    
    private fun calculateExecutiveKPIs(): Map<String, KPIValue> {
        return mapOf(
            "project_completion_rate" to KPIValue(88.5, 90.0, "percentage"),
            "budget_variance" to KPIValue(3.2, 5.0, "percentage"),
            "client_satisfaction" to KPIValue(4.6, 4.5, "score"),
            "safety_incidents" to KPIValue(1.0, 0.0, "count"),
            "profit_margin" to KPIValue(16.8, 15.0, "percentage")
        )
    }
    
    private fun analyzeProjectHealth(): ProjectHealthSummary {
        return ProjectHealthSummary(
            totalProjects = 15,
            onTrackProjects = 12,
            delayedProjects = 2,
            atRiskProjects = 1,
            overallHealth = 85.0
        )
    }
    
    private fun generateFinancialSummary(): FinancialSummary {
        return FinancialSummary(
            totalRevenue = 2850000.0,
            totalCosts = 2280000.0,
            grossProfit = 570000.0,
            profitMargin = 20.0,
            cashFlow = 485000.0
        )
    }
    
    private fun calculatePerformanceMetrics(): Map<String, Double> {
        return mapOf(
            "average_project_duration" to 32.5, // weeks
            "crew_utilization" to 87.2, // percentage
            "quality_score" to 94.1, // percentage
            "change_order_rate" to 8.5 // percentage
        )
    }
    
    private fun generateAlerts(): List<Alert> {
        return listOf(
            Alert("Project Alpha behind schedule", AlertSeverity.MEDIUM),
            Alert("Material costs increased 5%", AlertSeverity.LOW),
            Alert("Crew productivity down 3%", AlertSeverity.MEDIUM)
        )
    }
    
    private fun calculatePredictionConfidence(projectData: Map<String, Any>): Double {
        // Simplified confidence calculation
        val dataCompleteness = projectData.size / 10.0 // Assume 10 ideal data points
        return (dataCompleteness * 0.95).coerceAtMost(0.95)
    }
    
    private fun generateProductivityRecommendations(
        crewData: Map<String, ProductivityMetrics>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        crewData.forEach { (crew, metrics) ->
            when {
                metrics.efficiency < 80 -> recommendations.add("$crew needs productivity training")
                metrics.hoursPerTask > 100 -> recommendations.add("$crew may need better tools or processes")
                metrics.qualityScore < 90 -> recommendations.add("$crew needs quality improvement focus")
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("All crews performing well - consider bonus incentives")
        }
        
        return recommendations
    }
    
    // Voice command handlers
    private fun handleShowDashboard(command: String): String {
        return "Displaying executive dashboard with current KPIs and project health metrics."
    }
    
    private fun handleGenerateReport(command: String): String {
        return "Which report would you like to generate? Available: executive summary, financial, performance, compliance."
    }
    
    private fun handleProjectPerformance(command: String): String {
        return "Analyzing project performance metrics. Current average efficiency: 87.2%"
    }
    
    private fun handleCostAnalysis(command: String): String {
        return "Running cost analysis. Current profit margin: 16.8%, within target range."
    }
    
    private fun handlePredictiveAnalysis(command: String): String {
        return "What would you like me to predict? Available: project costs, schedule completion, risk assessment."
    }
    
    private fun handleKPIQuery(command: String): String {
        return "Current KPIs - Project completion: 88.5%, Budget variance: 3.2%, Client satisfaction: 4.6/5"
    }
    
    // Task handlers (simplified)
    private fun handleGenerateExecutiveDashboard(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.COMPLETED, progress = 1.0f, updatedAt = LocalDateTime.now())
    }
    
    private fun handleAnalyzeProjectProfitability(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.COMPLETED, progress = 1.0f, updatedAt = LocalDateTime.now())
    }
    
    private fun handleCreatePerformanceReport(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.COMPLETED, progress = 1.0f, updatedAt = LocalDateTime.now())
    }
    
    private fun handlePredictProjectCosts(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.COMPLETED, progress = 1.0f, updatedAt = LocalDateTime.now())
    }
    
    private fun handleTrackKPIMetrics(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.COMPLETED, progress = 1.0f, updatedAt = LocalDateTime.now())
    }
    
    private fun handleAnalyzeCrewProductivity(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.COMPLETED, progress = 1.0f, updatedAt = LocalDateTime.now())
    }
    
    private fun handleGenerateComplianceReport(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.COMPLETED, progress = 1.0f, updatedAt = LocalDateTime.now())
    }
    
    private fun handleGenericAnalyticsTask(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.IN_PROGRESS, progress = 0.5f, updatedAt = LocalDateTime.now())
    }
    
    // Standard interface implementations
    override suspend fun processMessage(message: AgentMessage): Result<AgentMessage?> = Result.success(null)
    override suspend fun getSpecializedCapabilities(): List<AgentCapability> = capabilities
    override suspend fun coordinateWithOtherDepartments(request: InterDepartmentalRequest): Result<InterDepartmentalResponse> {
        return Result.success(InterDepartmentalResponse(true, mapOf("analytics_data" to "Shared with ${request.targetDepartment}")))
    }
    override suspend fun learn(data: LearningData): Result<Unit> = Result.success(Unit)
    override suspend fun getKnowledgeBase(): Map<String, Any> = knowledgeBase.toMap()
    override suspend fun updateModel(parameters: Map<String, Any>): Result<Unit> = Result.success(Unit)
    override suspend fun getStatus(): SystemStatus = _status.value
    override suspend fun shutdown(): Result<Unit> = try {
        _status.value = SystemStatus.SHUTDOWN
        Log.i(TAG, "Analytics Orchestrator shutdown complete")
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Supporting data classes for Analytics
data class AnalyticsMetric(
    val name: String,
    val value: Double,
    val unit: String,
    val timestamp: LocalDateTime
)

data class ReportTemplate(
    val name: String,
    val description: String,
    val type: ReportType,
    val sections: List<String>
)

enum class ReportType {
    EXECUTIVE, FINANCIAL, PERFORMANCE, COMPLIANCE, CUSTOM
}

data class Dashboard(
    val id: String,
    val name: String,
    val type: DashboardType,
    val components: List<String>
)

enum class DashboardType {
    EXECUTIVE, OPERATIONAL, FINANCIAL, PROJECT_SPECIFIC
}

data class PredictiveModel(
    val name: String,
    val description: String,
    val version: String,
    val accuracy: Double
)

data class KPIDefinition(
    val name: String,
    val description: String,
    val unit: String,
    val target: Double
)

data class KPIValue(
    val current: Double,
    val target: Double,
    val unit: String
)

data class DataProcessor(
    val name: String,
    val description: String,
    val isActive: Boolean = true
)

data class ExecutiveDashboard(
    val id: String,
    val generatedAt: LocalDateTime,
    val kpis: Map<String, KPIValue>,
    val projectHealth: ProjectHealthSummary,
    val financialSummary: FinancialSummary,
    val performanceMetrics: Map<String, Double>,
    val alerts: List<Alert>
)

data class ProjectHealthSummary(
    val totalProjects: Int,
    val onTrackProjects: Int,
    val delayedProjects: Int,
    val atRiskProjects: Int,
    val overallHealth: Double
)

data class FinancialSummary(
    val totalRevenue: Double,
    val totalCosts: Double,
    val grossProfit: Double,
    val profitMargin: Double,
    val cashFlow: Double
)

data class Alert(
    val message: String,
    val severity: AlertSeverity,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class AlertSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class CostPrediction(
    val projectId: String,
    val predictedCost: Double,
    val confidenceLevel: Double,
    val variance: Double,
    val factors: List<PredictionFactor>,
    val modelVersion: String,
    val createdAt: LocalDateTime
)

data class PredictionFactor(
    val name: String,
    val value: Any,
    val weight: Double
)

data class ProductivityAnalysis(
    val timeframe: String,
    val crewMetrics: Map<String, ProductivityMetrics>,
    val averageEfficiency: Double,
    val topPerformers: List<String>,
    val improvementAreas: List<String>,
    val recommendations: List<String>,
    val generatedAt: LocalDateTime
)

data class ProductivityMetrics(
    val efficiency: Double, // percentage
    val hoursPerTask: Double,
    val qualityScore: Double, // percentage
    val projectsCompleted: Int
)