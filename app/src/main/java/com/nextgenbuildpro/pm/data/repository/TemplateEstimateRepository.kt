package com.nextgenbuildpro.pm.data.repository

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.pm.data.model.TemplateEstimate
import com.nextgenbuildpro.pm.data.model.TemplateAssembly
import com.nextgenbuildpro.pm.data.model.ContextMode
import com.nextgenbuildpro.pm.data.model.UnitType
import com.nextgenbuildpro.pm.data.model.ResolvedTask
import com.nextgenbuildpro.pm.data.model.AssemblyTemplate
import com.nextgenbuildpro.pm.data.model.TaskTemplate
import com.nextgenbuildpro.pm.data.model.TradeTemplate
import com.nextgenbuildpro.pm.data.model.TemplateLibrary
import com.nextgenbuildpro.pm.data.model.EstimateStatus
import com.nextgenbuildpro.pm.data.model.HomeLifecyclePhase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

/**
 * Repository for managing template-based estimates in the PM module
 */
class TemplateEstimateRepository(private val context: Context) : Repository<TemplateEstimate> {
    private val TAG = "TemplateEstimateRepo"
    private val _estimates = MutableStateFlow<List<TemplateEstimate>>(emptyList())
    val estimates: StateFlow<List<TemplateEstimate>> = _estimates.asStateFlow()

    // Template library data
    private val _templateLibrary = MutableStateFlow<TemplateLibrary?>(null)
    val templateLibrary: StateFlow<TemplateLibrary?> = _templateLibrary.asStateFlow()

    init {
        // Load sample template data
        loadSampleTemplateData()
    }

    /**
     * Get all estimates
     */
    override suspend fun getAll(): List<TemplateEstimate> {
        return _estimates.value
    }

    /**
     * Get an estimate by ID
     */
    override suspend fun getById(id: String): TemplateEstimate? {
        return _estimates.value.find { it.id == id }
    }

    /**
     * Save a new estimate
     */
    override suspend fun save(item: TemplateEstimate): Boolean {
        try {
            _estimates.value = _estimates.value + item
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving estimate: ${e.message}")
            return false
        }
    }

    /**
     * Update an existing estimate
     */
    override suspend fun update(item: TemplateEstimate): Boolean {
        try {
            _estimates.value = _estimates.value.map { 
                if (it.id == item.id) item else it 
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating estimate: ${e.message}")
            return false
        }
    }

    /**
     * Delete an estimate by ID
     */
    override suspend fun delete(id: String): Boolean {
        try {
            _estimates.value = _estimates.value.filter { it.id != id }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting estimate: ${e.message}")
            return false
        }
    }

    /**
     * Create a new estimate for a project with the specified context mode
     */
    suspend fun createEstimate(projectId: String, contextMode: ContextMode): TemplateEstimate {
        val estimate = TemplateEstimate(
            projectId = projectId,
            contextMode = contextMode,
            assemblies = mutableListOf()
        )

        save(estimate)
        return estimate
    }

    /**
     * Add an assembly to an estimate
     */
    suspend fun addAssemblyToEstimate(estimateId: String, assemblyTemplate: AssemblyTemplate, quantity: Double): Boolean {
        try {
            val estimate = getById(estimateId) ?: return false

            // Resolve tasks for the assembly
            val resolvedTasks = resolveTasksForAssembly(assemblyTemplate, quantity)

            // Calculate subtotals
            val subtotalLabor = resolvedTasks.sumOf { it.laborCost as Double }
            val subtotalMaterial = resolvedTasks.sumOf { it.materialCost as Double }
            val subtotalMarkup = resolvedTasks.sumOf { it.markupCost as Double }
            val total = subtotalLabor + subtotalMaterial + subtotalMarkup

            // Create assembly
            val assembly = TemplateAssembly(
                id = UUID.randomUUID().toString(),
                templateId = assemblyTemplate.id,
                name = assemblyTemplate.name,
                category = assemblyTemplate.category,
                description = assemblyTemplate.description,
                quantityUnit = assemblyTemplate.defaultQuantityUnit,
                quantity = quantity,
                tasks = resolvedTasks,
                subtotalLabor = subtotalLabor,
                subtotalMaterial = subtotalMaterial,
                subtotalMarkup = subtotalMarkup,
                total = total
            )

            // Add assembly to estimate
            val updatedAssemblies = estimate.assemblies.toMutableList()
            updatedAssemblies.add(assembly)

            // Recalculate estimate totals
            val updatedEstimate = recalculateEstimateTotals(estimate.copy(assemblies = updatedAssemblies))

            // Update estimate
            return update(updatedEstimate)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding assembly to estimate: ${e.message}")
            return false
        }
    }

    /**
     * Remove an assembly from an estimate
     */
    suspend fun removeAssemblyFromEstimate(estimateId: String, assemblyId: String): Boolean {
        try {
            val estimate = getById(estimateId) ?: return false

            // Remove assembly from estimate
            val updatedAssemblies = estimate.assemblies.toMutableList()
            updatedAssemblies.removeIf { it.id == assemblyId }

            // Recalculate estimate totals
            val updatedEstimate = recalculateEstimateTotals(estimate.copy(assemblies = updatedAssemblies))

            // Update estimate
            return update(updatedEstimate)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing assembly from estimate: ${e.message}")
            return false
        }
    }

    /**
     * Update an assembly in an estimate
     */
    suspend fun updateAssemblyInEstimate(estimateId: String, assembly: TemplateAssembly): Boolean {
        try {
            val estimate = getById(estimateId) ?: return false

            // Update assembly in estimate
            val updatedAssemblies = estimate.assemblies.toMutableList()
            val index = updatedAssemblies.indexOfFirst { it.id == assembly.id }
            if (index == -1) return false

            updatedAssemblies[index] = assembly

            // Recalculate estimate totals
            val updatedEstimate = recalculateEstimateTotals(estimate.copy(assemblies = updatedAssemblies))

            // Update estimate
            return update(updatedEstimate)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating assembly in estimate: ${e.message}")
            return false
        }
    }

    /**
     * Resolve tasks for an assembly
     */
    private fun resolveTasksForAssembly(assemblyTemplate: AssemblyTemplate, quantity: Double): List<ResolvedTask> {
        return assemblyTemplate.tasks.map { task ->
            val taskQuantity = task.defaultQty * quantity / assemblyTemplate.baseQuantity
            val laborCost = task.laborPerUnit * taskQuantity
            val materialCost = task.materialPerUnit * taskQuantity
            val markupCost = (laborCost + materialCost) * task.markup

            ResolvedTask(
                task = task,
                quantity = taskQuantity,
                laborCost = laborCost,
                materialCost = materialCost,
                markupCost = markupCost
            )
        }
    }

    /**
     * Recalculate estimate totals
     */
    private fun recalculateEstimateTotals(estimate: TemplateEstimate): TemplateEstimate {
        val subtotalLabor = estimate.assemblies.sumOf { it.subtotalLabor as Double }
        val subtotalMaterial = estimate.assemblies.sumOf { it.subtotalMaterial as Double }
        val markupTotal = estimate.assemblies.sumOf { it.subtotalMarkup as Double }
        val grandTotal = subtotalLabor + subtotalMaterial + markupTotal

        return estimate.copy(
            subtotalLabor = subtotalLabor,
            subtotalMaterial = subtotalMaterial,
            markupTotal = markupTotal,
            grandTotal = grandTotal
        )
    }

    /**
     * Get assembly templates filtered by context mode
     */
    suspend fun getAssemblyTemplatesByContextMode(contextMode: ContextMode): List<AssemblyTemplate> {
        val library = _templateLibrary.value ?: return emptyList()

        val templates = mutableListOf<AssemblyTemplate>()
        for (trade in library.trades) {
            if (trade.contextModes.contains(contextMode)) {
                for (assembly in trade.assemblies) {
                    if (assembly.validModes.contains(contextMode)) {
                        templates.add(assembly)
                    }
                }
            }
        }

        return templates
    }

    /**
     * Get assembly templates by trade
     */
    suspend fun getAssemblyTemplatesByTrade(tradeCode: String): List<AssemblyTemplate> {
        val library = _templateLibrary.value ?: return emptyList()

        val trade = library.trades.find { it.tradeCode == tradeCode } ?: return emptyList()
        return trade.assemblies
    }

    /**
     * Load sample template data
     */
    private fun loadSampleTemplateData() {
        // Create Frame Interior Wall tasks
        val frameInteriorWallTasks = listOf(
            TaskTemplate(
                description = "Layout top and bottom plates",
                unitType = UnitType.LF,
                defaultQty = 40.0,
                laborPerUnit = 0.25,
                materialPerUnit = 1.20,
                markup = 0.30,
                requiredTools = listOf("Chalk Line", "Hammer Drill")
            ),
            TaskTemplate(
                description = "Cut and install studs",
                unitType = UnitType.EA,
                defaultQty = 20.0,
                laborPerUnit = 0.15,
                materialPerUnit = 3.50,
                markup = 0.25,
                requiredTools = listOf("Circular Saw", "Nail Gun")
            ),
            TaskTemplate(
                description = "Nail off framing",
                unitType = UnitType.LF,
                defaultQty = 40.0,
                laborPerUnit = 0.10,
                materialPerUnit = 0.50,
                markup = 0.20,
                requiredTools = listOf("Nail Gun", "Hammer")
            )
        )

        // Create Frame Exterior Wall tasks
        val frameExteriorWallTasks = listOf(
            TaskTemplate(
                description = "Sheathing install",
                unitType = UnitType.SF,
                defaultQty = 160.0,
                laborPerUnit = 0.20,
                materialPerUnit = 1.80,
                markup = 0.30,
                requiredTools = listOf("Circular Saw", "Nail Gun")
            ),
            TaskTemplate(
                description = "Install header and opening blocking",
                unitType = UnitType.EA,
                defaultQty = 3.0,
                laborPerUnit = 0.50,
                materialPerUnit = 6.50,
                markup = 0.25,
                requiredTools = listOf("Circular Saw", "Nail Gun")
            ),
            TaskTemplate(
                description = "Anchor wall to slab",
                unitType = UnitType.LF,
                defaultQty = 60.0,
                laborPerUnit = 0.30,
                materialPerUnit = 1.00,
                markup = 0.20,
                requiredTools = listOf("Hammer Drill", "Impact Driver")
            )
        )

        // Create sample assembly templates
        val framingAssemblies = listOf(
            AssemblyTemplate(
                name = "Frame Interior Wall",
                category = "Interior Walls",
                validModes = listOf(ContextMode.REMODELING, ContextMode.NEW_CONSTRUCTION),
                description = "Basic stud wall on existing floor",
                defaultQuantityUnit = UnitType.LF,
                baseQuantity = 40.0,
                lifecyclePhase = HomeLifecyclePhase.STRUCTURE,
                tasks = frameInteriorWallTasks
            ),
            AssemblyTemplate(
                name = "Frame Exterior Wall",
                category = "Exterior Walls",
                validModes = listOf(ContextMode.NEW_CONSTRUCTION),
                description = "Exterior stud wall with sheathing",
                defaultQuantityUnit = UnitType.LF,
                baseQuantity = 60.0,
                lifecyclePhase = HomeLifecyclePhase.STRUCTURE,
                tasks = frameExteriorWallTasks
            )
        )

        // Create sample trade templates
        val trades = listOf(
            TradeTemplate(
                tradeName = "Framing",
                tradeCode = "FRM",
                contextModes = listOf(ContextMode.REMODELING, ContextMode.NEW_CONSTRUCTION),
                assemblies = framingAssemblies
            ),
            TradeTemplate(
                tradeName = "Drywall",
                tradeCode = "DRY",
                contextModes = listOf(ContextMode.REMODELING),
                assemblies = emptyList()
            ),
            TradeTemplate(
                tradeName = "Finish Carpentry",
                tradeCode = "FIN",
                contextModes = listOf(ContextMode.REMODELING),
                assemblies = emptyList()
            ),
            TradeTemplate(
                tradeName = "Tile & Flooring",
                tradeCode = "FLR",
                contextModes = listOf(ContextMode.REMODELING, ContextMode.FLIP),
                assemblies = emptyList()
            ),
            TradeTemplate(
                tradeName = "Painting",
                tradeCode = "PNT",
                contextModes = listOf(ContextMode.REMODELING, ContextMode.FLIP),
                assemblies = emptyList()
            )
        )

        // Create template library
        _templateLibrary.value = TemplateLibrary(trades = trades)
    }

    companion object {
        /**
         * Create a TemplateEstimateRepository instance
         */
        fun create(context: Context): TemplateEstimateRepository {
            return TemplateEstimateRepository(context)
        }
    }
}
