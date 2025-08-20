package com.nextgenbuildpro.pm.data.repository

import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.core.DateUtils
import com.nextgenbuildpro.pm.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date
import java.util.UUID

/**
 * Repository for managing assemblies and job templates in the PM module
 */
class AssemblyRepository {
    private val _tradeCategories = MutableStateFlow<List<TradeCategory>>(emptyList())
    val tradeCategories: StateFlow<List<TradeCategory>> = _tradeCategories.asStateFlow()

    private val _assemblies = MutableStateFlow<List<Assembly>>(emptyList())
    val assemblies: StateFlow<List<Assembly>> = _assemblies.asStateFlow()

    private val _jobTemplates = MutableStateFlow<List<JobTemplate>>(emptyList())
    val jobTemplates: StateFlow<List<JobTemplate>> = _jobTemplates.asStateFlow()

    init {
        // Load sample data for demonstration
        loadSampleData()
    }

    /**
     * Get all trade categories
     */
    suspend fun getAllTradeCategories(): List<TradeCategory> {
        return _tradeCategories.value
    }

    /**
     * Get a trade category by ID
     */
    suspend fun getTradeCategoryById(id: String): TradeCategory? {
        return _tradeCategories.value.find { it.id == id }
    }

    /**
     * Get all assemblies
     */
    suspend fun getAllAssemblies(): List<Assembly> {
        return _assemblies.value
    }

    /**
     * Get assemblies by trade category
     */
    suspend fun getAssembliesByTrade(tradeId: String): List<Assembly> {
        return _assemblies.value.filter { it.tradeId == tradeId }
    }

    /**
     * Get an assembly by ID
     */
    suspend fun getAssemblyById(id: String): Assembly? {
        return _assemblies.value.find { it.id == id }
    }

    /**
     * Save a new assembly
     */
    suspend fun saveAssembly(assembly: Assembly): Boolean {
        try {
            _assemblies.value = _assemblies.value + assembly
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Update an existing assembly
     */
    suspend fun updateAssembly(assembly: Assembly): Boolean {
        try {
            _assemblies.value = _assemblies.value.map { 
                if (it.id == assembly.id) assembly else it 
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Delete an assembly by ID
     */
    suspend fun deleteAssembly(id: String): Boolean {
        try {
            _assemblies.value = _assemblies.value.filter { it.id != id }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Get all job templates
     */
    suspend fun getAllJobTemplates(): List<JobTemplate> {
        return _jobTemplates.value
    }

    /**
     * Get job templates by trade category
     */
    suspend fun getJobTemplatesByTrade(tradeId: String): List<JobTemplate> {
        return _jobTemplates.value.filter { it.tradeId == tradeId }
    }

    /**
     * Get a job template by ID
     */
    suspend fun getJobTemplateById(id: String): JobTemplate? {
        return _jobTemplates.value.find { it.id == id }
    }

    /**
     * Save a new job template
     */
    suspend fun saveJobTemplate(jobTemplate: JobTemplate): Boolean {
        try {
            _jobTemplates.value = _jobTemplates.value + jobTemplate
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Update an existing job template
     */
    suspend fun updateJobTemplate(jobTemplate: JobTemplate): Boolean {
        try {
            _jobTemplates.value = _jobTemplates.value.map { 
                if (it.id == jobTemplate.id) jobTemplate else it 
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Delete a job template by ID
     */
    suspend fun deleteJobTemplate(id: String): Boolean {
        try {
            _jobTemplates.value = _jobTemplates.value.filter { it.id != id }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Search assemblies by name or description
     */
    suspend fun searchAssemblies(query: String): List<Assembly> {
        if (query.isBlank()) return _assemblies.value

        val lowercaseQuery = query.lowercase()
        return _assemblies.value.filter { 
            it.name.lowercase().contains(lowercaseQuery) || 
            it.description.lowercase().contains(lowercaseQuery) 
        }
    }

    /**
     * Search job templates by name or description
     */
    suspend fun searchJobTemplates(query: String): List<JobTemplate> {
        if (query.isBlank()) return _jobTemplates.value

        val lowercaseQuery = query.lowercase()
        return _jobTemplates.value.filter { 
            it.name.lowercase().contains(lowercaseQuery) || 
            it.description.lowercase().contains(lowercaseQuery) 
        }
    }

    /**
     * Create a project from a job template
     */
    suspend fun createProjectFromTemplate(
        templateId: String, 
        projectName: String, 
        clientId: String, 
        clientName: String,
        leadId: String,
        address: com.nextgenbuildpro.core.Address,
        customFieldValues: Map<String, String>
    ): Project? {
        val template = getJobTemplateById(templateId) ?: return null

        // Create a new project based on the template
        val project = Project(
            id = UUID.randomUUID().toString(),
            name = projectName,
            clientId = clientId,
            clientName = clientName,
            leadId = leadId,
            address = address,
            status = ProjectStatus.PLANNING.displayName,
            startDate = DateUtils.formatDate(Date()),
            endDate = calculateEndDate(template.estimatedDuration),
            budget = template.estimatedCost,
            actualCost = 0.0,
            progress = 0,
            description = template.description,
            notes = "Created from template: ${template.name}",
            lastActivityDate = DateUtils.getCurrentTimestamp(),
            createdAt = DateUtils.getCurrentTimestamp(),
            updatedAt = DateUtils.getCurrentTimestamp()
        )

        return project
    }

    /**
     * Calculate end date by adding days to current date
     */
    private fun calculateEndDate(daysToAdd: Int): String {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
        return DateUtils.formatDate(calendar.time)
    }

    /**
     * Load sample data for demonstration
     */
    private fun loadSampleData() {
        // Sample trade categories
        val tradeCategories = listOf(
            TradeCategory(
                id = "trade_1",
                name = TradeType.CARPENTRY.displayName,
                description = "Carpentry work including framing, trim, and cabinetry",
                iconName = "build"
            ),
            TradeCategory(
                id = "trade_2",
                name = TradeType.ELECTRICAL.displayName,
                description = "Electrical work including wiring, fixtures, and panels",
                iconName = "electrical_services"
            ),
            TradeCategory(
                id = "trade_3",
                name = TradeType.PLUMBING.displayName,
                description = "Plumbing work including pipes, fixtures, and water heaters",
                iconName = "plumbing"
            ),
            TradeCategory(
                id = "trade_4",
                name = TradeType.HVAC.displayName,
                description = "HVAC work including ductwork, units, and thermostats",
                iconName = "air"
            ),
            TradeCategory(
                id = "trade_5",
                name = TradeType.PAINTING.displayName,
                description = "Painting work including interior and exterior surfaces",
                iconName = "format_paint"
            )
        )

        // Sample assembly materials
        val cabinetMaterials = listOf(
            AssemblyMaterial(
                id = "material_1",
                name = "Base Cabinet",
                description = "36\" base cabinet with 2 doors and 1 drawer",
                quantity = 1.0,
                unit = "each",
                unitCost = 250.0,
                totalCost = 250.0
            ),
            AssemblyMaterial(
                id = "material_2",
                name = "Cabinet Hardware",
                description = "Handles and hinges for cabinet",
                quantity = 1.0,
                unit = "set",
                unitCost = 25.0,
                totalCost = 25.0
            ),
            AssemblyMaterial(
                id = "material_3",
                name = "Mounting Screws",
                description = "Screws for mounting cabinet to wall",
                quantity = 1.0,
                unit = "box",
                unitCost = 5.0,
                totalCost = 5.0
            )
        )

        // Sample assemblies
        val assemblies = listOf(
            Assembly(
                id = "assembly_1",
                name = "Base Cabinet Installation",
                description = "Installation of a standard 36\" base cabinet",
                tradeId = "trade_1",
                tradeName = TradeType.CARPENTRY.displayName,
                materials = cabinetMaterials,
                laborHours = 2.0,
                estimatedCost = 350.0,
                tags = listOf("cabinet", "kitchen"),
                createdAt = DateUtils.getCurrentTimestamp(),
                updatedAt = DateUtils.getCurrentTimestamp()
            ),
            Assembly(
                id = "assembly_2",
                name = "Wall Cabinet Installation",
                description = "Installation of a standard 30\" wall cabinet",
                tradeId = "trade_1",
                tradeName = TradeType.CARPENTRY.displayName,
                materials = listOf(
                    AssemblyMaterial(
                        id = "material_4",
                        name = "Wall Cabinet",
                        description = "30\" wall cabinet with 2 doors",
                        quantity = 1.0,
                        unit = "each",
                        unitCost = 200.0,
                        totalCost = 200.0
                    ),
                    AssemblyMaterial(
                        id = "material_5",
                        name = "Cabinet Hardware",
                        description = "Handles and hinges for cabinet",
                        quantity = 1.0,
                        unit = "set",
                        unitCost = 25.0,
                        totalCost = 25.0
                    ),
                    AssemblyMaterial(
                        id = "material_6",
                        name = "Mounting Screws",
                        description = "Screws for mounting cabinet to wall",
                        quantity = 1.0,
                        unit = "box",
                        unitCost = 5.0,
                        totalCost = 5.0
                    )
                ),
                laborHours = 1.5,
                estimatedCost = 280.0,
                tags = listOf("cabinet", "kitchen"),
                createdAt = DateUtils.getCurrentTimestamp(),
                updatedAt = DateUtils.getCurrentTimestamp()
            )
        )

        // Sample editable data fields
        val kitchenTemplateDataFields = listOf(
            EditableDataField(
                id = "field_1",
                name = "Kitchen Size",
                description = "Size of the kitchen in square feet",
                type = DataFieldType.NUMBER,
                required = true,
                defaultValue = "150",
                min = 50.0,
                max = 500.0,
                placeholder = "Enter kitchen size in sq ft"
            ),
            EditableDataField(
                id = "field_2",
                name = "Cabinet Style",
                description = "Style of cabinets to be installed",
                type = DataFieldType.SELECT,
                required = true,
                options = listOf("Shaker", "Flat Panel", "Raised Panel", "Beadboard", "Custom"),
                defaultValue = "Shaker",
                placeholder = "Select cabinet style"
            ),
            EditableDataField(
                id = "field_3",
                name = "Countertop Material",
                description = "Material for the countertops",
                type = DataFieldType.SELECT,
                required = true,
                options = listOf("Granite", "Quartz", "Marble", "Butcher Block", "Laminate"),
                defaultValue = "Quartz",
                placeholder = "Select countertop material"
            ),
            EditableDataField(
                id = "field_4",
                name = "Include Island",
                description = "Whether to include a kitchen island",
                type = DataFieldType.BOOLEAN,
                required = false,
                defaultValue = "true"
            ),
            EditableDataField(
                id = "field_5",
                name = "Special Instructions",
                description = "Any special instructions for the kitchen renovation",
                type = DataFieldType.TEXTAREA,
                required = false,
                placeholder = "Enter any special instructions or notes"
            )
        )

        // Sample job templates
        val jobTemplates = listOf(
            JobTemplate(
                id = "template_1",
                name = "Kitchen Renovation",
                description = "Complete kitchen renovation including cabinets, countertops, and appliances",
                tradeId = "trade_1",
                tradeName = TradeType.CARPENTRY.displayName,
                assemblies = assemblies,
                estimatedDuration = 30, // 30 days
                estimatedCost = 25000.0,
                phases = listOf(
                    TemplatePhase(
                        id = "phase_1",
                        name = "Demo",
                        description = "Remove existing cabinets, countertops, and appliances",
                        order = 1,
                        estimatedDuration = 3,
                        tasks = listOf(
                            TemplateTask(
                                id = "task_1",
                                name = "Remove cabinets",
                                description = "Remove all existing cabinets",
                                estimatedHours = 8.0,
                                assignedToRole = "Carpenter"
                            ),
                            TemplateTask(
                                id = "task_2",
                                name = "Remove countertops",
                                description = "Remove all existing countertops",
                                estimatedHours = 4.0,
                                assignedToRole = "Carpenter"
                            ),
                            TemplateTask(
                                id = "task_3",
                                name = "Remove appliances",
                                description = "Disconnect and remove all appliances",
                                estimatedHours = 4.0,
                                assignedToRole = "Plumber/Electrician"
                            )
                        )
                    ),
                    TemplatePhase(
                        id = "phase_2",
                        name = "Rough-in",
                        description = "Rough-in plumbing and electrical",
                        order = 2,
                        estimatedDuration = 5,
                        tasks = listOf(
                            TemplateTask(
                                id = "task_4",
                                name = "Plumbing rough-in",
                                description = "Rough-in plumbing for sink and dishwasher",
                                estimatedHours = 8.0,
                                assignedToRole = "Plumber"
                            ),
                            TemplateTask(
                                id = "task_5",
                                name = "Electrical rough-in",
                                description = "Rough-in electrical for appliances and lighting",
                                estimatedHours = 8.0,
                                assignedToRole = "Electrician"
                            )
                        ),
                        dependencies = listOf("phase_1")
                    ),
                    TemplatePhase(
                        id = "phase_3",
                        name = "Cabinet Installation",
                        description = "Install new cabinets",
                        order = 3,
                        estimatedDuration = 7,
                        tasks = listOf(
                            TemplateTask(
                                id = "task_6",
                                name = "Install base cabinets",
                                description = "Install all base cabinets",
                                estimatedHours = 16.0,
                                assignedToRole = "Carpenter"
                            ),
                            TemplateTask(
                                id = "task_7",
                                name = "Install wall cabinets",
                                description = "Install all wall cabinets",
                                estimatedHours = 12.0,
                                assignedToRole = "Carpenter"
                            )
                        ),
                        dependencies = listOf("phase_2")
                    )
                ),
                dataFields = kitchenTemplateDataFields,
                tags = listOf("kitchen", "renovation", "cabinets"),
                createdAt = DateUtils.getCurrentTimestamp(),
                updatedAt = DateUtils.getCurrentTimestamp()
            )
        )

        _tradeCategories.value = tradeCategories
        _assemblies.value = assemblies
        _jobTemplates.value = jobTemplates
    }
}
