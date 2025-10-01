package com.nextgenbuildpro.bms.data.repository

import android.content.Context
import com.nextgenbuildpro.bms.data.model.*
import com.nextgenbuildpro.core.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for Building Management System data
 */
class BmsRepository(private val context: Context) : Repository<Building> {
    private val _buildings = MutableStateFlow<List<Building>>(emptyList())
    val buildings: StateFlow<List<Building>> = _buildings.asStateFlow()

    private val _components = MutableStateFlow<List<BuildingComponent>>(emptyList())
    val components: StateFlow<List<BuildingComponent>> = _components.asStateFlow()

    private val _materials = MutableStateFlow<List<Material>>(emptyList())
    val materials: StateFlow<List<Material>> = _materials.asStateFlow()

    private val _inspections = MutableStateFlow<List<Inspection>>(emptyList())
    val inspections: StateFlow<List<Inspection>> = _inspections.asStateFlow()

    private val _performance = MutableStateFlow<List<BuildingPerformance>>(emptyList())
    val performance: StateFlow<List<BuildingPerformance>> = _performance.asStateFlow()

    init {
        loadSampleData()
    }

    override suspend fun getAll(): List<Building> {
        return _buildings.value
    }

    override suspend fun getById(id: String): Building? {
        return _buildings.value.find { it.id == id }
    }

    override suspend fun save(item: Building): Boolean {
        val updatedList = _buildings.value + item
        _buildings.value = updatedList
        return true
    }

    override suspend fun update(item: Building): Boolean {
        val updatedList = _buildings.value.map { 
            if (it.id == item.id) item else it 
        }
        _buildings.value = updatedList
        return true
    }

    override suspend fun delete(id: String): Boolean {
        val updatedList = _buildings.value.filter { it.id != id }
        _buildings.value = updatedList
        return true
    }

    // Create method for convenience (delegates to save)
    suspend fun create(item: Building): Result<Building> {
        return if (save(item)) {
            Result.success(item)
        } else {
            Result.failure(Exception("Failed to save building"))
        }
    }

    // Building Components
    suspend fun getComponentsByBuilding(buildingId: String): Result<List<BuildingComponent>> {
        val buildingComponents = _components.value.filter { it.buildingId == buildingId }
        return Result.success(buildingComponents)
    }

    suspend fun createComponent(component: BuildingComponent): Result<BuildingComponent> {
        val updatedList = _components.value + component
        _components.value = updatedList
        return Result.success(component)
    }

    suspend fun updateComponent(component: BuildingComponent): Result<BuildingComponent> {
        val updatedList = _components.value.map { 
            if (it.id == component.id) component else it 
        }
        _components.value = updatedList
        return Result.success(component)
    }

    // Materials
    suspend fun getMaterialsByComponent(componentId: String): Result<List<Material>> {
        val component = _components.value.find { it.id == componentId }
        return Result.success(component?.materials ?: emptyList())
    }

    suspend fun createMaterial(material: Material): Result<Material> {
        val updatedList = _materials.value + material
        _materials.value = updatedList
        return Result.success(material)
    }

    suspend fun updateMaterial(material: Material): Result<Material> {
        val updatedList = _materials.value.map { 
            if (it.id == material.id) material else it 
        }
        _materials.value = updatedList
        return Result.success(material)
    }

    // Inspections
    suspend fun getInspectionsByBuilding(buildingId: String): Result<List<Inspection>> {
        val buildingInspections = _inspections.value.filter { it.buildingId == buildingId }
        return Result.success(buildingInspections)
    }

    suspend fun createInspection(inspection: Inspection): Result<Inspection> {
        val updatedList = _inspections.value + inspection
        _inspections.value = updatedList
        return Result.success(inspection)
    }

    suspend fun updateInspection(inspection: Inspection): Result<Inspection> {
        val updatedList = _inspections.value.map { 
            if (it.id == inspection.id) inspection else it 
        }
        _inspections.value = updatedList
        return Result.success(inspection)
    }

    // Performance
    suspend fun getPerformanceByBuilding(buildingId: String): Result<BuildingPerformance?> {
        val performance = _performance.value.find { it.buildingId == buildingId }
        return Result.success(performance)
    }

    private fun loadSampleData() {
        // Sample buildings
        val sampleBuildings = listOf(
            Building(
                id = "building_1",
                name = "Johnson Residence",
                address = "123 Oak Street, Springfield, IL",
                type = BuildingType.RESIDENTIAL,
                floors = 2,
                totalArea = 2500.0,
                constructionStart = "2024-01-15",
                estimatedCompletion = "2024-08-30",
                status = BuildingStatus.CONSTRUCTION,
                projectManager = "John Smith",
                description = "Custom family home with modern amenities"
            ),
            Building(
                id = "building_2",
                name = "Downtown Office Complex",
                address = "456 Business Ave, Metro City, CA",
                type = BuildingType.COMMERCIAL,
                floors = 12,
                totalArea = 45000.0,
                constructionStart = "2023-06-01",
                estimatedCompletion = "2025-03-15",
                status = BuildingStatus.CONSTRUCTION,
                projectManager = "Sarah Johnson",
                description = "Modern office building with sustainable features"
            ),
            Building(
                id = "building_3",
                name = "Riverside Apartments",
                address = "789 River Road, Riverside, NY",
                type = BuildingType.RESIDENTIAL,
                floors = 8,
                totalArea = 32000.0,
                constructionStart = "2024-03-01",
                estimatedCompletion = "2025-01-30",
                status = BuildingStatus.DESIGN,
                projectManager = "Mike Wilson",
                description = "Luxury apartment complex with river views"
            )
        )

        // Sample components
        val sampleComponents = listOf(
            BuildingComponent(
                id = "comp_1",
                buildingId = "building_1",
                name = "Foundation",
                type = ComponentType.FOUNDATION,
                floor = 0,
                materials = listOf(
                    Material(
                        id = "mat_1",
                        name = "Concrete Mix",
                        type = MaterialType.CONCRETE,
                        quantity = 50.0,
                        unit = "cubic yards",
                        unitCost = 120.0,
                        supplier = "Metro Concrete",
                        specifications = "4000 PSI concrete mix",
                        deliveryDate = "2024-01-20",
                        status = MaterialStatus.DELIVERED
                    )
                ),
                dimensions = Dimensions(length = 50.0, width = 30.0, height = 3.0),
                status = ComponentStatus.COMPLETED,
                installDate = "2024-01-25",
                inspectionDate = "2024-02-01",
                notes = "Foundation completed on schedule"
            ),
            BuildingComponent(
                id = "comp_2",
                buildingId = "building_1",
                name = "Framing",
                type = ComponentType.WALLS,
                floor = 1,
                materials = listOf(
                    Material(
                        id = "mat_2",
                        name = "2x4 Lumber",
                        type = MaterialType.LUMBER,
                        quantity = 200.0,
                        unit = "linear feet",
                        unitCost = 3.50,
                        supplier = "Forest Products Inc",
                        specifications = "Pressure treated lumber",
                        deliveryDate = "2024-02-10",
                        status = MaterialStatus.INSTALLED
                    )
                ),
                dimensions = Dimensions(length = 50.0, width = 30.0, height = 9.0),
                status = ComponentStatus.INSPECTED,
                installDate = "2024-02-15",
                inspectionDate = "2024-03-01",
                notes = "Framing inspection passed"
            )
        )

        // Sample inspections
        val sampleInspections = listOf(
            Inspection(
                id = "insp_1",
                buildingId = "building_1",
                componentId = "comp_1",
                type = InspectionType.FOUNDATION,
                inspector = "County Inspector #123",
                scheduledDate = "2024-02-01",
                completedDate = "2024-02-01",
                status = InspectionStatus.PASSED,
                results = InspectionResults(
                    passed = true,
                    score = 95.0,
                    violations = emptyList(),
                    recommendations = listOf("Excellent foundation work"),
                    nextInspectionDate = null
                ),
                notes = "Foundation meets all code requirements"
            ),
            Inspection(
                id = "insp_2",
                buildingId = "building_1",
                componentId = "comp_2",
                type = InspectionType.FRAMING,
                inspector = "County Inspector #456",
                scheduledDate = "2024-03-01",
                completedDate = "2024-03-01",
                status = InspectionStatus.PASSED,
                results = InspectionResults(
                    passed = true,
                    score = 92.0,
                    violations = emptyList(),
                    recommendations = listOf("Good framing quality"),
                    nextInspectionDate = null
                ),
                notes = "Framing inspection completed successfully"
            )
        )

        // Sample performance data
        val samplePerformance = listOf(
            BuildingPerformance(
                buildingId = "building_1",
                energyEfficiency = EnergyMetrics(
                    estimatedConsumption = 12000.0,
                    efficiency = 85.0,
                    certificationLevel = "Energy Star"
                ),
                sustainability = SustainabilityMetrics(
                    leedRating = "Gold",
                    wasteReduction = 25.0,
                    recycledMaterials = 15.0,
                    carbonFootprint = 8.5
                ),
                safetyMetrics = SafetyMetrics(
                    incidentCount = 0,
                    safetyScore = 98.0,
                    lastSafetyInspection = "2024-03-15"
                ),
                qualityMetrics = QualityMetrics(
                    defectRate = 2.1,
                    reworkRate = 1.5,
                    customerSatisfaction = 4.8,
                    qualityScore = 94.0
                ),
                lastUpdated = "2024-03-20"
            )
        )

        _buildings.value = sampleBuildings
        _components.value = sampleComponents
        _inspections.value = sampleInspections
        _performance.value = samplePerformance
    }
}