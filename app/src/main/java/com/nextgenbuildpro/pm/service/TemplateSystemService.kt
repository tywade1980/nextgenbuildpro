package com.nextgenbuildpro.pm.service

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.model.*
import com.nextgenbuildpro.pm.data.repository.TemplateEstimateRepository
import com.nextgenbuildpro.pm.data.repository.HierarchicalCatalogueRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime

/**
 * Template System Service
 * Manages template creation, storage, and conversion functionality
 */
class TemplateSystemService(
    private val context: Context,
    private val templateEstimateRepository: TemplateEstimateRepository,
    private val hierarchicalCatalogueRepository: HierarchicalCatalogueRepository
) {
    private val TAG = "TemplateSystemService"
    
    /**
     * Create estimate template from existing estimate
     */
    suspend fun createTemplateFromEstimate(
        estimate: TemplateEstimate,
        templateName: String,
        templateDescription: String,
        applicableContextModes: List<ContextMode>,
        lifecyclePhase: HomeLifecyclePhase
    ): EstimateTemplate? {
        try {
            val template = EstimateTemplate(
                name = templateName,
                description = templateDescription,
                contextModes = applicableContextModes,
                lifecyclePhase = lifecyclePhase,
                assemblies = estimate.assemblies.map { assembly ->
                    AssemblyTemplate(
                        name = assembly.name,
                        category = assembly.category,
                        validModes = applicableContextModes,
                        description = assembly.description,
                        defaultQuantityUnit = assembly.quantityUnit,
                        baseQuantity = assembly.quantity,
                        lifecyclePhase = lifecyclePhase,
                        tasks = assembly.tasks.map { task ->
                            TaskTemplate(
                                description = task.task.description,
                                unitType = task.task.unitType,
                                defaultQty = task.quantity,
                                laborPerUnit = if (task.quantity > 0) task.laborCost / task.quantity else 0.0,
                                materialPerUnit = if (task.quantity > 0) task.materialCost / task.quantity else 0.0,
                                markup = if (task.laborCost + task.materialCost > 0) 
                                    task.markupCost / (task.laborCost + task.materialCost) else 0.0,
                                requiredTools = task.task.requiredTools,
                                flags = task.task.flags
                            )
                        }
                    )
                },
                createdAt = LocalDateTime.now(),
                lastModified = LocalDateTime.now()
            )
            
            // Save template to file system
            saveTemplateToFile(template)
            
            return template
        } catch (e: Exception) {
            Log.e(TAG, "Error creating template from estimate: ${e.message}")
            return null
        }
    }
    
    /**
     * Load estimate template by name
     */
    suspend fun loadEstimateTemplate(templateName: String): EstimateTemplate? {
        try {
            return loadTemplateFromFile(templateName)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading template: ${e.message}")
            return null
        }
    }
    
    /**
     * Convert template to estimate
     */
    suspend fun convertTemplateToEstimate(
        template: EstimateTemplate,
        projectId: String,
        contextMode: ContextMode,
        assemblyQuantityOverrides: Map<String, Double> = emptyMap()
    ): TemplateEstimate? {
        try {
            // Create base estimate
            val estimate = templateEstimateRepository.createEstimate(projectId, contextMode)
            
            // Convert template assemblies to estimate assemblies
            val assemblyPairs = template.assemblies.map { assemblyTemplate ->
                val quantity = assemblyQuantityOverrides[assemblyTemplate.name] ?: assemblyTemplate.baseQuantity
                assemblyTemplate to quantity
            }
            
            // Bulk add assemblies to estimate
            val success = templateEstimateRepository.bulkAddAssembliesToEstimate(estimate.id, assemblyPairs)
            
            return if (success) {
                templateEstimateRepository.getById(estimate.id)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting template to estimate: ${e.message}")
            return null
        }
    }
    
    /**
     * Create assembly template from catalogue assembly
     */
    suspend fun createAssemblyTemplateFromCatalogue(
        catalogueAssembly: Assembly,
        contextModes: List<ContextMode>,
        lifecyclePhase: HomeLifecyclePhase
    ): AssemblyTemplate? {
        try {
            // Convert assembly materials to tasks
            val tasks = catalogueAssembly.materials.map { material ->
                TaskTemplate(
                    description = "Install ${material.name}",
                    unitType = UnitType.valueOf(material.unit.uppercase()),
                    defaultQty = material.quantity,
                    laborPerUnit = calculateLaborRate(catalogueAssembly.tradeName),
                    materialPerUnit = material.unitCost,
                    markup = 0.25, // Standard 25% markup
                    requiredTools = getStandardTools(catalogueAssembly.tradeName),
                    flags = emptyList()
                )
            }
            
            return AssemblyTemplate(
                name = catalogueAssembly.name,
                category = catalogueAssembly.tradeName,
                validModes = contextModes,
                description = catalogueAssembly.description,
                defaultQuantityUnit = UnitType.EA, // Default unit
                baseQuantity = 1.0,
                lifecyclePhase = lifecyclePhase,
                tasks = tasks
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating assembly template from catalogue: ${e.message}")
            return null
        }
    }
    
    /**
     * Get all available estimate templates
     */
    suspend fun getAvailableTemplates(): List<EstimateTemplateInfo> {
        try {
            val templatesDir = File(context.filesDir, "estimate_templates")
            if (!templatesDir.exists()) return emptyList()
            
            return templatesDir.listFiles()?.mapNotNull { file ->
                if (file.extension == "json") {
                    try {
                        val content = file.readText()
                        val json = JSONObject(content)
                        EstimateTemplateInfo(
                            name = json.getString("name"),
                            description = json.getString("description"),
                            contextModes = json.getJSONArray("contextModes").let { array ->
                                (0 until array.length()).map { i ->
                                    ContextMode.valueOf(array.getString(i))
                                }
                            },
                            lifecyclePhase = HomeLifecyclePhase.valueOf(json.getString("lifecyclePhase")),
                            assemblyCount = json.getJSONArray("assemblies").length(),
                            createdAt = LocalDateTime.parse(json.getString("createdAt")),
                            fileName = file.name
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing template file ${file.name}: ${e.message}")
                        null
                    }
                } else null
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting available templates: ${e.message}")
            return emptyList()
        }
    }
    
    /**
     * Delete estimate template
     */
    suspend fun deleteTemplate(templateName: String): Boolean {
        try {
            val templatesDir = File(context.filesDir, "estimate_templates")
            val templateFile = File(templatesDir, "${templateName.replace(" ", "_")}.json")
            
            return if (templateFile.exists()) {
                templateFile.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting template: ${e.message}")
            return false
        }
    }
    
    /**
     * Update existing template
     */
    suspend fun updateTemplate(template: EstimateTemplate): Boolean {
        try {
            return saveTemplateToFile(template.copy(lastModified = LocalDateTime.now()))
        } catch (e: Exception) {
            Log.e(TAG, "Error updating template: ${e.message}")
            return false
        }
    }
    
    /**
     * Generate template suggestions based on project context
     */
    suspend fun generateTemplateSuggestions(
        contextMode: ContextMode,
        lifecyclePhase: HomeLifecyclePhase,
        projectDescription: String = ""
    ): List<EstimateTemplateInfo> {
        try {
            val allTemplates = getAvailableTemplates()
            
            return allTemplates.filter { template ->
                template.contextModes.contains(contextMode) &&
                template.lifecyclePhase == lifecyclePhase &&
                (projectDescription.isBlank() || 
                 template.description.contains(projectDescription, ignoreCase = true) ||
                 template.name.contains(projectDescription, ignoreCase = true))
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating template suggestions: ${e.message}")
            return emptyList()
        }
    }
    
    // Private helper methods
    
    private suspend fun saveTemplateToFile(template: EstimateTemplate): Boolean {
        try {
            val templatesDir = File(context.filesDir, "estimate_templates")
            if (!templatesDir.exists()) {
                templatesDir.mkdirs()
            }
            
            val fileName = "${template.name.replace(" ", "_")}.json"
            val templateFile = File(templatesDir, fileName)
            
            val json = JSONObject().apply {
                put("name", template.name)
                put("description", template.description)
                put("contextModes", JSONArray(template.contextModes.map { it.name }))
                put("lifecyclePhase", template.lifecyclePhase.name)
                put("createdAt", template.createdAt.toString())
                put("lastModified", template.lastModified.toString())
                
                val assembliesArray = JSONArray()
                template.assemblies.forEach { assembly ->
                    val assemblyJson = JSONObject().apply {
                        put("name", assembly.name)
                        put("category", assembly.category)
                        put("description", assembly.description)
                        put("defaultQuantityUnit", assembly.defaultQuantityUnit.name)
                        put("baseQuantity", assembly.baseQuantity)
                        put("lifecyclePhase", assembly.lifecyclePhase.name)
                        
                        val tasksArray = JSONArray()
                        assembly.tasks.forEach { task ->
                            val taskJson = JSONObject().apply {
                                put("description", task.description)
                                put("unitType", task.unitType?.name)
                                put("defaultQty", task.defaultQty)
                                put("laborPerUnit", task.laborPerUnit)
                                put("materialPerUnit", task.materialPerUnit)
                                put("markup", task.markup)
                                put("requiredTools", JSONArray(task.requiredTools))
                                put("flags", JSONArray(task.flags))
                            }
                            tasksArray.put(taskJson)
                        }
                        put("tasks", tasksArray)
                    }
                    assembliesArray.put(assemblyJson)
                }
                put("assemblies", assembliesArray)
            }
            
            templateFile.writeText(json.toString(2))
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving template to file: ${e.message}")
            return false
        }
    }
    
    private suspend fun loadTemplateFromFile(templateName: String): EstimateTemplate? {
        try {
            val templatesDir = File(context.filesDir, "estimate_templates")
            val templateFile = File(templatesDir, "${templateName.replace(" ", "_")}.json")
            
            if (!templateFile.exists()) return null
            
            val content = templateFile.readText()
            val json = JSONObject(content)
            
            val assemblies = json.getJSONArray("assemblies").let { array ->
                (0 until array.length()).map { i ->
                    val assemblyJson = array.getJSONObject(i)
                    val tasks = assemblyJson.getJSONArray("tasks").let { tasksArray ->
                        (0 until tasksArray.length()).map { j ->
                            val taskJson = tasksArray.getJSONObject(j)
                            TaskTemplate(
                                description = taskJson.getString("description"),
                                unitType = UnitType.valueOf(taskJson.getString("unitType")),
                                defaultQty = taskJson.getDouble("defaultQty"),
                                laborPerUnit = taskJson.getDouble("laborPerUnit"),
                                materialPerUnit = taskJson.getDouble("materialPerUnit"),
                                markup = taskJson.getDouble("markup"),
                                requiredTools = taskJson.getJSONArray("requiredTools").let { toolsArray ->
                                    (0 until toolsArray.length()).map { k -> toolsArray.getString(k) }
                                },
                                flags = taskJson.getJSONArray("flags").let { flagsArray ->
                                    (0 until flagsArray.length()).map { k -> flagsArray.getString(k) }
                                }
                            )
                        }
                    }
                    
                    AssemblyTemplate(
                        name = assemblyJson.getString("name"),
                        category = assemblyJson.getString("category"),
                        validModes = json.getJSONArray("contextModes").let { modesArray ->
                            (0 until modesArray.length()).map { j -> ContextMode.valueOf(modesArray.getString(j)) }
                        },
                        description = assemblyJson.getString("description"),
                        defaultQuantityUnit = UnitType.valueOf(assemblyJson.getString("defaultQuantityUnit")),
                        baseQuantity = assemblyJson.getDouble("baseQuantity"),
                        lifecyclePhase = HomeLifecyclePhase.valueOf(assemblyJson.getString("lifecyclePhase")),
                        tasks = tasks
                    )
                }
            }
            
            return EstimateTemplate(
                name = json.getString("name"),
                description = json.getString("description"),
                contextModes = json.getJSONArray("contextModes").let { array ->
                    (0 until array.length()).map { i -> ContextMode.valueOf(array.getString(i)) }
                },
                lifecyclePhase = HomeLifecyclePhase.valueOf(json.getString("lifecyclePhase")),
                assemblies = assemblies,
                createdAt = LocalDateTime.parse(json.getString("createdAt")),
                lastModified = LocalDateTime.parse(json.getString("lastModified"))
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading template from file: ${e.message}")
            return null
        }
    }
    
    private fun calculateLaborRate(tradeName: String): Double {
        return when (tradeName.lowercase()) {
            "framing" -> 45.0
            "drywall" -> 40.0
            "electrical" -> 65.0
            "plumbing" -> 55.0
            "hvac" -> 60.0
            "roofing" -> 50.0
            "flooring" -> 35.0
            "painting" -> 30.0
            "tile" -> 40.0
            "cabinets" -> 55.0
            else -> 45.0
        }
    }
    
    private fun getStandardTools(tradeName: String): List<String> {
        return when (tradeName.lowercase()) {
            "framing" -> listOf("Circular Saw", "Nail Gun", "Hammer", "Level")
            "drywall" -> listOf("Drywall Saw", "Screw Gun", "Taping Knife", "Sander")
            "electrical" -> listOf("Wire Strippers", "Multimeter", "Drill", "Fish Tape")
            "plumbing" -> listOf("Pipe Wrench", "Torch", "Pipe Cutter", "Level")
            "hvac" -> listOf("Manifold Gauges", "Vacuum Pump", "Torch", "Ductwork Tools")
            "roofing" -> listOf("Nail Gun", "Hammer", "Chalk Line", "Safety Equipment")
            "flooring" -> listOf("Saw", "Nailer", "Tapping Block", "Level")
            "painting" -> listOf("Brushes", "Rollers", "Sprayer", "Drop Cloths")
            "tile" -> listOf("Tile Saw", "Trowel", "Level", "Spacers")
            "cabinets" -> listOf("Circular Saw", "Router", "Clamps", "Drill")
            else -> listOf("Basic Hand Tools")
        }
    }
}

// Data classes for template system

data class EstimateTemplate(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val contextModes: List<ContextMode>,
    val lifecyclePhase: HomeLifecyclePhase,
    val assemblies: List<AssemblyTemplate>,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now()
)

data class EstimateTemplateInfo(
    val name: String,
    val description: String,
    val contextModes: List<ContextMode>,
    val lifecyclePhase: HomeLifecyclePhase,
    val assemblyCount: Int,
    val createdAt: LocalDateTime,
    val fileName: String
)