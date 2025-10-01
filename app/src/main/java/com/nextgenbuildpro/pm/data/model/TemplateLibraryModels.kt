package com.nextgenbuildpro.pm.data.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Data models for the Template Library
 * These models are used for creating estimates from templates
 */

/**
 * Represents the natural phases of a home/job lifecycle
 * Used for organizing trades and tasks in a logical sequence
 */
enum class HomeLifecyclePhase {
    DISCOVERY,           // Lead intake, homeowner interest
    SITE_ANALYSIS,       // Measure, inspect, survey
    DESIGN_DEVELOPMENT,  // Concepts, drafts, layout
    ESTIMATING,          // Task + cost breakdowns
    PERMITTING,          // Code checks, legal steps
    DEMOLITION,          // Tear-outs, clearouts
    STRUCTURE,           // Framing, concrete, sheathing
    ENCLOSURE,           // Roofing, windows, doors
    SYSTEMS,             // HVAC, Plumbing, Electrical
    INTERIORS,           // Drywall, Paint, Trim, Cabinets
    FINAL_PUNCHOUT,      // Walkthrough, touchups
    COMPLETION           // Closeout, handoff, invoicing
}

/**
 * Maps each home lifecycle phase to a list of trades that are typically involved
 * This provides a logical order of trades for each phase of construction
 */
val tradeStackByPhase = mapOf(
    HomeLifecyclePhase.STRUCTURE to listOf("Excavation", "Concrete", "Framing"),
    HomeLifecyclePhase.ENCLOSURE to listOf("Roofing", "Windows", "Siding"),
    HomeLifecyclePhase.SYSTEMS to listOf("Plumbing", "Electrical", "HVAC"),
    HomeLifecyclePhase.INTERIORS to listOf("Drywall", "Painting", "Trim", "Cabinets", "Flooring"),
    HomeLifecyclePhase.FINAL_PUNCHOUT to listOf("Cleaning", "Touchups", "Inspection"),
    HomeLifecyclePhase.COMPLETION to listOf("Final Walkthrough", "Handoff", "Invoicing")
)

/**
 * Enum for context modes
 * Defines the type of construction project
 */
enum class ContextMode {
    REMODELING,
    NEW_CONSTRUCTION,
    REPAIR,
    MAINTENANCE,
    ADDITION,
    RENOVATION,
    FLIP
}

/**
 * Enum for unit types
 * Defines the units of measurement for tasks and materials
 */
enum class UnitType {
    LF, // Linear Feet
    SF, // Square Feet
    SY, // Square Yards
    CF, // Cubic Feet
    CY, // Cubic Yards
    EA, // Each
    HR, // Hour
    DAY, // Day
    BOX, // Box
    ROLL, // Roll
    SHEET, // Sheet
    GALLON, // Gallon
    POUND, // Pound
    SET // Set
}

/**
 * Represents a template library containing trade templates
 */
data class TemplateLibrary(
    val trades: List<TradeTemplate>
)

/**
 * Represents a trade template containing assemblies
 */
data class TradeTemplate(
    val tradeName: String,                         // e.g., "Framing"
    val tradeCode: String,                         // e.g., "FRM"
    val contextModes: List<ContextMode>,           // ["REMODELING", "NEW_CONSTRUCTION"]
    val assemblies: List<AssemblyTemplate>
)

/**
 * Represents an assembly template
 */
data class AssemblyTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,                              // e.g., "Frame Interior Wall"
    val category: String,                          // e.g., "Interior Walls"
    val validModes: List<ContextMode>,             // Limits use to certain project types
    val description: String,
    val defaultQuantityUnit: UnitType,             // LF, SF, EA, etc.
    val baseQuantity: Double = 0.0,
    val lifecyclePhase: HomeLifecyclePhase = HomeLifecyclePhase.STRUCTURE, // Default to STRUCTURE phase
    val tasks: List<TaskTemplate>
)

/**
 * Represents a task template within an assembly
 */
data class TaskTemplate(
    val id: String = UUID.randomUUID().toString(),
    val description: String,                       // e.g., "Install top and bottom plate"
    val unitType: UnitType,                        // LF, SF, EA, HR
    val defaultQty: Double,
    val laborPerUnit: Double,                      // Labor hours per unit
    val materialPerUnit: Double,                   // Material cost per unit
    val markup: Double,                            // Default markup %
    val requiredTools: List<String> = listOf(),
    val flags: List<String> = listOf()             // ["Requires Inspection", "Optional"]
)

/**
 * Represents a resolved task with calculated costs
 */
data class ResolvedTask(
    val task: TaskTemplate,
    val quantity: Double,
    val laborCost: Double,
    val materialCost: Double,
    val markupCost: Double
)

/**
 * Represents the context of a project for estimate generation
 */
data class ProjectContext(
    val lifecyclePhase: HomeLifecyclePhase,
    val contextMode: ContextMode
)

/**
 * Template Library System Logic
 * Class for generating estimates from templates
 */
class TemplateEstimateGenerator {
    companion object {
        /**
         * Generate a template estimate based on project context
         */
        fun generateEstimate(projectContext: ProjectContext): TemplateEstimate {
            val applicableTrades = getTradesByPhase(projectContext.lifecyclePhase)
            val assemblies = loadAssembliesForTrades(applicableTrades)
            val tasks = flattenTasks(assemblies)
            return calculateEstimate(tasks, projectContext)
        }

        /**
         * Get trades applicable to a specific lifecycle phase
         */
        fun getTradesByPhase(phase: HomeLifecyclePhase): List<String> {
            return tradeStackByPhase[phase] ?: emptyList()
        }

        /**
         * Load assembly templates for the specified trades
         */
        fun loadAssembliesForTrades(trades: List<String>): List<AssemblyTemplate> {
            // This is a placeholder implementation
            // In a real implementation, this would load from a database or repository
            val templateLibrary = TemplateLibrary(emptyList())
            return trades.flatMap { trade ->
                templateLibrary.trades.find { it.tradeName == trade }?.assemblies ?: listOf()
            }
        }

        /**
         * Flatten assemblies into a list of tasks
         */
        fun flattenTasks(assemblies: List<AssemblyTemplate>): List<TaskTemplate> {
            return assemblies.flatMap { it.tasks }
        }

        /**
         * Calculate estimate based on tasks
         */
        fun calculateEstimate(tasks: List<TaskTemplate>, projectContext: ProjectContext): TemplateEstimate {
            // Create resolved tasks
            val resolvedTasks = tasks.map { task ->
                ResolvedTask(
                    task = task,
                    quantity = task.defaultQty,
                    laborCost = task.laborPerUnit * task.defaultQty,
                    materialCost = task.materialPerUnit * task.defaultQty,
                    markupCost = (task.laborPerUnit + task.materialPerUnit) * task.defaultQty * task.markup
                )
            }

            // Calculate subtotals
            val subtotalLabor = resolvedTasks.sumOf { it.laborCost }
            val subtotalMaterial = resolvedTasks.sumOf { it.materialCost }
            val markupTotal = resolvedTasks.sumOf { it.markupCost }
            val grandTotal = subtotalLabor + subtotalMaterial + markupTotal

            // Create template assemblies
            val templateAssemblies = mutableListOf<TemplateAssembly>()

            // Create and return template estimate
            return TemplateEstimate(
                projectId = UUID.randomUUID().toString(), // This would be replaced with actual project ID
                contextMode = projectContext.contextMode,
                assemblies = templateAssemblies,
                subtotalLabor = subtotalLabor,
                subtotalMaterial = subtotalMaterial,
                markupTotal = markupTotal,
                grandTotal = grandTotal
            )
        }
    }
}
