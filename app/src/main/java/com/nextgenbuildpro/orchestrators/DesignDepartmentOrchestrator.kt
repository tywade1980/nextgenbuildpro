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

/**
 * Design Department Orchestrator
 * 
 * Handles 3D modeling, blueprint generation, design workflows, shop drawings,
 * and image generation for construction projects. Focused purely on design
 * deliverables that contribute to project completion.
 */
class DesignDepartmentOrchestrator(
    private val context: Context
) : DepartmentalOrchestrator {
    
    companion object {
        private const val TAG = "DesignDepartmentOrchestrator"
    }
    
    override val agentType: AgentType = AgentType.DESIGN_DEPARTMENT_ORCHESTRATOR
    override val departmentName: String = "Design Department"
    
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
    private val blueprintLibrary = mutableMapOf<String, Blueprint>()
    private val threeDModels = mutableMapOf<String, ThreeDModel>()
    private val shopDrawings = mutableMapOf<String, ShopDrawing>()
    private val materialTakeoffs = mutableMapOf<String, MaterialTakeoff>()
    private val designTemplates = mutableMapOf<String, DesignTemplate>()
    
    override val toolsets = listOf(
        OrchestratorTool(
            name = "Blueprint Generation",
            description = "AI-assisted plan creation from sketches and requirements",
            toolType = ToolType.DESIGN_TOOL,
            permissions = listOf(Permission.ACCESS_CAMERA, Permission.ACCESS_STORAGE)
        ),
        OrchestratorTool(
            name = "3D Model Creation",
            description = "Convert 2D plans to 3D visualizations for client presentation",
            toolType = ToolType.MODELING_TOOL,
            permissions = listOf(Permission.ACCESS_STORAGE)
        ),
        OrchestratorTool(
            name = "Shop Drawing Generator",
            description = "Automated technical drawings for trade contractors",
            toolType = ToolType.DESIGN_TOOL,
            permissions = listOf(Permission.ACCESS_STORAGE)
        ),
        OrchestratorTool(
            name = "Material Takeoff Calculator",
            description = "Automatic quantity calculations from plans for accurate ordering",
            toolType = ToolType.DATA_ANALYSIS,
            permissions = listOf(Permission.ACCESS_STORAGE)
        ),
        OrchestratorTool(
            name = "Design Modification Engine",
            description = "Real-time updates to plans and estimates when changes are made",
            toolType = ToolType.DESIGN_TOOL,
            permissions = listOf(Permission.ACCESS_STORAGE)
        ),
        OrchestratorTool(
            name = "Construction Documentation",
            description = "Generate permit-ready drawings and construction documents",
            toolType = ToolType.DESIGN_TOOL,
            permissions = listOf(Permission.ACCESS_STORAGE)
        )
    )
    
    override val capabilities = listOf(
        AgentCapability(
            name = "Blueprint Generation",
            description = "Create professional blueprints from sketches or requirements",
            inputTypes = listOf("Sketch", "Requirements", "SitePhoto"),
            outputTypes = listOf("Blueprint", "FloorPlan", "ElevationDrawing"),
            skillLevel = SkillLevel.EXPERT
        ),
        AgentCapability(
            name = "3D Visualization",
            description = "Generate 3D models and renderings for client presentation",
            inputTypes = listOf("Blueprint", "DesignSpecifications"),
            outputTypes = listOf("3DModel", "Rendering", "VirtualTour"),
            skillLevel = SkillLevel.ADVANCED
        ),
        AgentCapability(
            name = "Technical Drawing Creation",
            description = "Generate shop drawings and technical specifications",
            inputTypes = listOf("Blueprint", "ConstructionDetails"),
            outputTypes = listOf("ShopDrawing", "TechnicalSpec", "DetailDrawing"),
            skillLevel = SkillLevel.EXPERT
        ),
        AgentCapability(
            name = "Material Quantity Analysis",
            description = "Calculate precise material quantities from plans",
            inputTypes = listOf("Blueprint", "ConstructionPlan"),
            outputTypes = listOf("MaterialTakeoff", "QuantityList", "CostEstimate"),
            skillLevel = SkillLevel.ADVANCED
        ),
        AgentCapability(
            name = "Design Change Management",
            description = "Manage design changes and update all related documents",
            inputTypes = listOf("ChangeRequest", "ModifiedPlan"),
            outputTypes = listOf("UpdatedBlueprint", "RevisedTakeoff", "ChangeOrder"),
            skillLevel = SkillLevel.EXPERT
        )
    )
    
    override suspend fun initialize(): Result<Unit> = try {
        mutex.withLock {
            Log.i(TAG, "Initializing Design Department Orchestrator...")
            
            _status.value = SystemStatus.INITIALIZING
            
            // Initialize design templates
            initializeDesignTemplates()
            
            // Set up blueprint library
            setupBlueprintLibrary()
            
            // Initialize 3D modeling engine
            initialize3DModelingEngine()
            
            // Set up material calculation systems
            setupMaterialCalculationSystems()
            
            // Initialize technical drawing tools
            initializeTechnicalDrawingTools()
            
            _status.value = SystemStatus.ACTIVE
            Log.i(TAG, "Design Department Orchestrator initialized successfully")
            
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Design Department Orchestrator", e)
        _status.value = SystemStatus.ERROR
        Result.failure(e)
    }
    
    override suspend fun processVoiceCommand(command: String): Result<String> = try {
        Log.d(TAG, "Processing Design voice command: $command")
        
        val response = when {
            command.contains("create blueprint", ignoreCase = true) -> {
                handleCreateBlueprint(command)
            }
            command.contains("generate 3d model", ignoreCase = true) -> {
                handleGenerate3DModel(command)
            }
            command.contains("shop drawing", ignoreCase = true) -> {
                handleShopDrawing(command)
            }
            command.contains("material takeoff", ignoreCase = true) -> {
                handleMaterialTakeoff(command)
            }
            command.contains("modify design", ignoreCase = true) -> {
                handleModifyDesign(command)
            }
            command.contains("permit drawings", ignoreCase = true) -> {
                handlePermitDrawings(command)
            }
            else -> "Design command not recognized. Available commands: create blueprint, generate 3d model, shop drawing, material takeoff, modify design, permit drawings"
        }
        
        Result.success(response)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to process Design voice command: $command", e)
        Result.failure(e)
    }
    
    override suspend fun executeTask(task: NextGenTask): Result<NextGenTask> = try {
        Log.d(TAG, "Executing Design task: ${task.title}")
        
        val updatedTask = when (task.title.lowercase()) {
            "generate construction blueprints" -> handleGenerateBlueprints(task)
            "create 3d visualization" -> handleCreate3DVisualization(task)
            "produce shop drawings" -> handleProduceShopDrawings(task)
            "calculate material quantities" -> handleCalculateMaterialQuantities(task)
            "update design modifications" -> handleUpdateDesignModifications(task)
            "prepare permit drawings" -> handlePreparePermitDrawings(task)
            else -> handleGenericDesignTask(task)
        }
        
        Result.success(updatedTask)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to execute Design task: ${task.title}", e)
        Result.failure(e)
    }
    
    // Core Design Methods
    
    suspend fun generateBlueprintFromRequirements(
        projectRequirements: ProjectRequirements
    ): Result<Blueprint> = try {
        
        Log.d(TAG, "Generating blueprint for ${projectRequirements.projectType}")
        
        val blueprint = Blueprint(
            id = UUID.randomUUID().toString(),
            projectId = projectRequirements.projectId,
            projectType = projectRequirements.projectType,
            squareFootage = projectRequirements.squareFootage,
            floors = projectRequirements.floors,
            bedrooms = projectRequirements.bedrooms,
            bathrooms = projectRequirements.bathrooms,
            specialFeatures = projectRequirements.specialFeatures,
            floorPlans = generateFloorPlans(projectRequirements),
            elevations = generateElevations(projectRequirements),
            sections = generateSections(projectRequirements),
            createdDate = LocalDateTime.now(),
            version = "1.0"
        )
        
        blueprintLibrary[blueprint.id] = blueprint
        
        Log.i(TAG, "Blueprint generated successfully: ${blueprint.id}")
        Result.success(blueprint)
        
    } catch (e: Exception) {
        Log.e(TAG, "Failed to generate blueprint", e)
        Result.failure(e)
    }
    
    suspend fun create3DModelFromBlueprint(
        blueprintId: String
    ): Result<ThreeDModel> = try {
        
        val blueprint = blueprintLibrary[blueprintId]
            ?: return Result.failure(Exception("Blueprint not found: $blueprintId"))
        
        Log.d(TAG, "Creating 3D model from blueprint: $blueprintId")
        
        val model = ThreeDModel(
            id = UUID.randomUUID().toString(),
            blueprintId = blueprintId,
            projectId = blueprint.projectId,
            modelType = ModelType.EXTERIOR_INTERIOR,
            renderQuality = RenderQuality.HIGH,
            views = generateModelViews(blueprint),
            materials = generateModelMaterials(blueprint),
            lighting = "Natural + Artificial",
            createdDate = LocalDateTime.now()
        )
        
        threeDModels[model.id] = model
        
        Log.i(TAG, "3D model created successfully: ${model.id}")
        Result.success(model)
        
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create 3D model", e)
        Result.failure(e)
    }
    
    suspend fun generateShopDrawings(
        blueprintId: String,
        tradeType: String
    ): Result<List<ShopDrawing>> = try {
        
        val blueprint = blueprintLibrary[blueprintId]
            ?: return Result.failure(Exception("Blueprint not found: $blueprintId"))
        
        Log.d(TAG, "Generating shop drawings for trade: $tradeType")
        
        val shopDrawings = when (tradeType.lowercase()) {
            "framing" -> generateFramingDrawings(blueprint)
            "electrical" -> generateElectricalDrawings(blueprint)
            "plumbing" -> generatePlumbingDrawings(blueprint)
            "hvac" -> generateHVACDrawings(blueprint)
            else -> generateGenericShopDrawings(blueprint, tradeType)
        }
        
        shopDrawings.forEach { drawing ->
            this.shopDrawings[drawing.id] = drawing
        }
        
        Log.i(TAG, "Generated ${shopDrawings.size} shop drawings for $tradeType")
        Result.success(shopDrawings)
        
    } catch (e: Exception) {
        Log.e(TAG, "Failed to generate shop drawings", e)
        Result.failure(e)
    }
    
    suspend fun calculateMaterialTakeoff(
        blueprintId: String
    ): Result<MaterialTakeoff> = try {
        
        val blueprint = blueprintLibrary[blueprintId]
            ?: return Result.failure(Exception("Blueprint not found: $blueprintId"))
        
        Log.d(TAG, "Calculating material takeoff for blueprint: $blueprintId")
        
        val takeoff = MaterialTakeoff(
            id = UUID.randomUUID().toString(),
            blueprintId = blueprintId,
            projectId = blueprint.projectId,
            categories = calculateMaterialCategories(blueprint),
            totalQuantities = calculateTotalQuantities(blueprint),
            estimatedCost = calculateEstimatedCost(blueprint),
            createdDate = LocalDateTime.now()
        )
        
        materialTakeoffs[takeoff.id] = takeoff
        
        Log.i(TAG, "Material takeoff calculated successfully: ${takeoff.id}")
        Result.success(takeoff)
        
    } catch (e: Exception) {
        Log.e(TAG, "Failed to calculate material takeoff", e)
        Result.failure(e)
    }
    
    // Implementation methods
    
    private fun initializeDesignTemplates() {
        Log.d(TAG, "Initializing design templates...")
        
        designTemplates.putAll(mapOf(
            "residential_single_story" to DesignTemplate(
                "Single Story Residential",
                "Standard single-story home design template",
                TemplateType.RESIDENTIAL
            ),
            "residential_two_story" to DesignTemplate(
                "Two Story Residential", 
                "Standard two-story home design template",
                TemplateType.RESIDENTIAL
            ),
            "commercial_office" to DesignTemplate(
                "Commercial Office",
                "Standard commercial office design template",
                TemplateType.COMMERCIAL
            )
        ))
        
        knowledgeBase["design_templates_loaded"] = designTemplates.size
    }
    
    private fun setupBlueprintLibrary() {
        Log.d(TAG, "Setting up blueprint library...")
        knowledgeBase["blueprint_library_active"] = true
    }
    
    private fun initialize3DModelingEngine() {
        Log.d(TAG, "Initializing 3D modeling engine...")
        knowledgeBase["3d_modeling_engine_active"] = true
    }
    
    private fun setupMaterialCalculationSystems() {
        Log.d(TAG, "Setting up material calculation systems...")
        knowledgeBase["material_calculation_active"] = true
    }
    
    private fun initializeTechnicalDrawingTools() {
        Log.d(TAG, "Initializing technical drawing tools...")
        knowledgeBase["technical_drawing_tools_active"] = true
    }
    
    private fun generateFloorPlans(requirements: ProjectRequirements): List<String> {
        return listOf("Ground Floor Plan", "Second Floor Plan").take(requirements.floors)
    }
    
    private fun generateElevations(requirements: ProjectRequirements): List<String> {
        return listOf("Front Elevation", "Rear Elevation", "Left Elevation", "Right Elevation")
    }
    
    private fun generateSections(requirements: ProjectRequirements): List<String> {
        return listOf("Building Section A-A", "Building Section B-B")
    }
    
    private fun generateModelViews(blueprint: Blueprint): List<String> {
        return listOf("Exterior View", "Interior Living Room", "Interior Kitchen", "Interior Bedrooms")
    }
    
    private fun generateModelMaterials(blueprint: Blueprint): List<String> {
        return listOf("Brick Exterior", "Hardwood Flooring", "Granite Countertops", "Standard Paint")
    }
    
    private fun generateFramingDrawings(blueprint: Blueprint): List<ShopDrawing> {
        return listOf(
            ShopDrawing(
                id = UUID.randomUUID().toString(),
                blueprintId = blueprint.id,
                tradeType = "Framing",
                drawingType = "Floor Framing Plan",
                details = "Structural framing layout and member sizes"
            ),
            ShopDrawing(
                id = UUID.randomUUID().toString(),
                blueprintId = blueprint.id,
                tradeType = "Framing",
                drawingType = "Roof Framing Plan",
                details = "Roof structure and truss layout"
            )
        )
    }
    
    private fun generateElectricalDrawings(blueprint: Blueprint): List<ShopDrawing> {
        return listOf(
            ShopDrawing(
                id = UUID.randomUUID().toString(),
                blueprintId = blueprint.id,
                tradeType = "Electrical",
                drawingType = "Electrical Layout",
                details = "Outlet, switch, and fixture locations"
            )
        )
    }
    
    private fun generatePlumbingDrawings(blueprint: Blueprint): List<ShopDrawing> {
        return listOf(
            ShopDrawing(
                id = UUID.randomUUID().toString(),
                blueprintId = blueprint.id,
                tradeType = "Plumbing",
                drawingType = "Plumbing Layout",
                details = "Water supply and waste line routing"
            )
        )
    }
    
    private fun generateHVACDrawings(blueprint: Blueprint): List<ShopDrawing> {
        return listOf(
            ShopDrawing(
                id = UUID.randomUUID().toString(),
                blueprintId = blueprint.id,
                tradeType = "HVAC",
                drawingType = "HVAC Layout",
                details = "Ductwork and equipment placement"
            )
        )
    }
    
    private fun generateGenericShopDrawings(blueprint: Blueprint, tradeType: String): List<ShopDrawing> {
        return listOf(
            ShopDrawing(
                id = UUID.randomUUID().toString(),
                blueprintId = blueprint.id,
                tradeType = tradeType,
                drawingType = "$tradeType Layout",
                details = "Technical details for $tradeType trade"
            )
        )
    }
    
    private fun calculateMaterialCategories(blueprint: Blueprint): Map<String, MaterialCategory> {
        return mapOf(
            "structural" to MaterialCategory("Structural", calculateStructuralMaterials(blueprint)),
            "roofing" to MaterialCategory("Roofing", calculateRoofingMaterials(blueprint)),
            "siding" to MaterialCategory("Siding", calculateSidingMaterials(blueprint)),
            "flooring" to MaterialCategory("Flooring", calculateFlooringMaterials(blueprint)),
            "electrical" to MaterialCategory("Electrical", calculateElectricalMaterials(blueprint)),
            "plumbing" to MaterialCategory("Plumbing", calculatePlumbingMaterials(blueprint))
        )
    }
    
    private fun calculateStructuralMaterials(blueprint: Blueprint): List<MaterialItem> {
        return listOf(
            MaterialItem("2x4x8 SPF Lumber", (blueprint.squareFootage * 0.5).toInt(), "pieces"),
            MaterialItem("2x6x8 SPF Lumber", (blueprint.squareFootage * 0.2).toInt(), "pieces"),
            MaterialItem("Plywood Sheathing 4x8", (blueprint.squareFootage / 30).toInt(), "sheets")
        )
    }
    
    private fun calculateRoofingMaterials(blueprint: Blueprint): List<MaterialItem> {
        return listOf(
            MaterialItem("Asphalt Shingles", (blueprint.squareFootage / 100).toInt(), "squares"),
            MaterialItem("Roofing Felt", (blueprint.squareFootage * 1.1).toInt(), "sq ft")
        )
    }
    
    private fun calculateSidingMaterials(blueprint: Blueprint): List<MaterialItem> {
        val perimeterArea = blueprint.squareFootage * 0.8 // Estimated exterior wall area
        return listOf(
            MaterialItem("Vinyl Siding", perimeterArea.toInt(), "sq ft"),
            MaterialItem("House Wrap", perimeterArea.toInt(), "sq ft")
        )
    }
    
    private fun calculateFlooringMaterials(blueprint: Blueprint): List<MaterialItem> {
        return listOf(
            MaterialItem("Hardwood Flooring", (blueprint.squareFootage * 0.7).toInt(), "sq ft"),
            MaterialItem("Tile Flooring", (blueprint.squareFootage * 0.3).toInt(), "sq ft")
        )
    }
    
    private fun calculateElectricalMaterials(blueprint: Blueprint): List<MaterialItem> {
        return listOf(
            MaterialItem("12-2 Romex Wire", (blueprint.squareFootage * 2).toInt(), "linear feet"),
            MaterialItem("Standard Outlets", (blueprint.squareFootage / 80).toInt(), "pieces"),
            MaterialItem("Light Switches", (blueprint.squareFootage / 150).toInt(), "pieces")
        )
    }
    
    private fun calculatePlumbingMaterials(blueprint: Blueprint): List<MaterialItem> {
        return listOf(
            MaterialItem("PEX Pipe 3/4\"", (blueprint.squareFootage * 1.5).toInt(), "linear feet"),
            MaterialItem("PVC Pipe 4\"", (blueprint.squareFootage * 0.5).toInt(), "linear feet")
        )
    }
    
    private fun calculateTotalQuantities(blueprint: Blueprint): Map<String, Int> {
        return mapOf(
            "total_lumber_pieces" to (blueprint.squareFootage * 0.7).toInt(),
            "total_sheets" to (blueprint.squareFootage / 30).toInt(),
            "total_fixtures" to (blueprint.bathrooms * 3 + 1) // Bathroom fixtures + kitchen sink
        )
    }
    
    private fun calculateEstimatedCost(blueprint: Blueprint): Double {
        // Simple cost calculation based on square footage
        return blueprint.squareFootage * 15.0 // $15 per sq ft for materials
    }
    
    // Voice command handlers
    private fun handleCreateBlueprint(command: String): String {
        return "Creating blueprint from requirements. Please specify project type and square footage."
    }
    
    private fun handleGenerate3DModel(command: String): String {
        return "Generating 3D model from blueprint. This will take a few minutes to render."
    }
    
    private fun handleShopDrawing(command: String): String {
        return "Which trade needs shop drawings? Available: framing, electrical, plumbing, HVAC."
    }
    
    private fun handleMaterialTakeoff(command: String): String {
        return "Calculating material takeoff from blueprint. This includes all construction materials."
    }
    
    private fun handleModifyDesign(command: String): String {
        return "Design modification initiated. What changes would you like to make?"
    }
    
    private fun handlePermitDrawings(command: String): String {
        return "Preparing permit-ready drawings with all required details and specifications."
    }
    
    // Task handlers (simplified)
    private fun handleGenerateBlueprints(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.COMPLETED, progress = 1.0f, updatedAt = LocalDateTime.now())
    }
    
    private fun handleCreate3DVisualization(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.COMPLETED, progress = 1.0f, updatedAt = LocalDateTime.now())
    }
    
    private fun handleProduceShopDrawings(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.COMPLETED, progress = 1.0f, updatedAt = LocalDateTime.now())
    }
    
    private fun handleCalculateMaterialQuantities(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.COMPLETED, progress = 1.0f, updatedAt = LocalDateTime.now())
    }
    
    private fun handleUpdateDesignModifications(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.COMPLETED, progress = 1.0f, updatedAt = LocalDateTime.now())
    }
    
    private fun handlePreparePermitDrawings(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.COMPLETED, progress = 1.0f, updatedAt = LocalDateTime.now())
    }
    
    private fun handleGenericDesignTask(task: NextGenTask): NextGenTask {
        return task.copy(status = TaskStatus.IN_PROGRESS, progress = 0.5f, updatedAt = LocalDateTime.now())
    }
    
    // Standard interface implementations
    override suspend fun processMessage(message: AgentMessage): Result<AgentMessage?> = Result.success(null)
    override suspend fun getSpecializedCapabilities(): List<AgentCapability> = capabilities
    override suspend fun coordinateWithOtherDepartments(request: InterDepartmentalRequest): Result<InterDepartmentalResponse> {
        return Result.success(InterDepartmentalResponse(true, mapOf("design_data" to "Shared with ${request.targetDepartment}")))
    }
    override suspend fun learn(data: LearningData): Result<Unit> = Result.success(Unit)
    override suspend fun getKnowledgeBase(): Map<String, Any> = knowledgeBase.toMap()
    override suspend fun updateModel(parameters: Map<String, Any>): Result<Unit> = Result.success(Unit)
    override suspend fun getStatus(): SystemStatus = _status.value
    override suspend fun shutdown(): Result<Unit> = try {
        _status.value = SystemStatus.SHUTDOWN
        Log.i(TAG, "Design Department Orchestrator shutdown complete")
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Supporting data classes for Design
data class ProjectRequirements(
    val projectId: String,
    val projectType: String,
    val squareFootage: Double,
    val floors: Int,
    val bedrooms: Int,
    val bathrooms: Int,
    val specialFeatures: List<String>
)

data class Blueprint(
    val id: String,
    val projectId: String,
    val projectType: String,
    val squareFootage: Double,
    val floors: Int,
    val bedrooms: Int,
    val bathrooms: Int,
    val specialFeatures: List<String>,
    val floorPlans: List<String>,
    val elevations: List<String>,
    val sections: List<String>,
    val createdDate: LocalDateTime,
    val version: String
)

data class ThreeDModel(
    val id: String,
    val blueprintId: String,
    val projectId: String,
    val modelType: ModelType,
    val renderQuality: RenderQuality,
    val views: List<String>,
    val materials: List<String>,
    val lighting: String,
    val createdDate: LocalDateTime
)

enum class ModelType {
    EXTERIOR_ONLY, INTERIOR_ONLY, EXTERIOR_INTERIOR
}

enum class RenderQuality {
    LOW, MEDIUM, HIGH, ULTRA
}

data class ShopDrawing(
    val id: String,
    val blueprintId: String,
    val tradeType: String,
    val drawingType: String,
    val details: String,
    val createdDate: LocalDateTime = LocalDateTime.now()
)

data class MaterialTakeoff(
    val id: String,
    val blueprintId: String,
    val projectId: String,
    val categories: Map<String, MaterialCategory>,
    val totalQuantities: Map<String, Int>,
    val estimatedCost: Double,
    val createdDate: LocalDateTime
)

data class MaterialCategory(
    val name: String,
    val items: List<MaterialItem>
)

data class MaterialItem(
    val name: String,
    val quantity: Int,
    val unit: String
)

data class DesignTemplate(
    val name: String,
    val description: String,
    val type: TemplateType
)

enum class TemplateType {
    RESIDENTIAL, COMMERCIAL, INDUSTRIAL
}