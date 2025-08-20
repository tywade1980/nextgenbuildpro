package com.nextgenbuildpro.pm.service

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.pm.data.model.ContextMode
import com.nextgenbuildpro.pm.data.model.TemplateEstimate
import com.nextgenbuildpro.pm.data.model.AssemblyTemplate
import com.nextgenbuildpro.pm.data.repository.TemplateEstimateRepositoryImpl
import com.nextgenbuildpro.pm.data.repository.ProjectRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Service for creating and managing estimates
 * Handles triggers from voice commands, client detail screen, and job lead pipeline
 */
class EstimateService(
    private val context: Context,
    private val templateEstimateRepository: TemplateEstimateRepositoryImpl,
    private val projectRepository: ProjectRepository
) {
    private val TAG = "EstimateService"
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    /**
     * Process a voice command to create an estimate
     * Example: "Create estimate for bathroom remodel"
     */
    fun processVoiceCommand(command: String): Boolean {
        val lowerCommand = command.lowercase().trim()

        if (lowerCommand.contains("create estimate") || lowerCommand.contains("generate estimate")) {
            // Extract project type from command
            val contextMode = when {
                lowerCommand.contains("remodel") -> ContextMode.REMODELING
                lowerCommand.contains("new construction") -> ContextMode.NEW_CONSTRUCTION
                lowerCommand.contains("repair") -> ContextMode.REPAIR
                lowerCommand.contains("maintenance") -> ContextMode.MAINTENANCE
                lowerCommand.contains("addition") -> ContextMode.ADDITION
                lowerCommand.contains("renovation") -> ContextMode.RENOVATION
                else -> ContextMode.REMODELING // Default
            }

            // Extract project name or type
            val projectType = extractProjectType(lowerCommand)

            // Find matching project or use current project
            serviceScope.launch {
                val project = findProjectByType(projectType)
                if (project != null) {
                    createEstimateForProject(project.id, contextMode)
                    Log.d(TAG, "Created estimate for project: ${project.name} with context mode: $contextMode")
                } else {
                    Log.e(TAG, "No matching project found for: $projectType")
                }
            }

            return true
        }

        return false
    }

    /**
     * Create an estimate from the client detail screen
     */
    suspend fun createEstimateFromClientScreen(projectId: String, contextMode: ContextMode): TemplateEstimate? {
        return withContext(Dispatchers.IO) {
            try {
                val project = projectRepository.getById(projectId)
                if (project != null) {
                    val estimate = createEstimateForProject(projectId, contextMode)
                    Log.d(TAG, "Created estimate from client screen for project: ${project.name}")
                    estimate
                } else {
                    Log.e(TAG, "Project not found with ID: $projectId")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating estimate from client screen: ${e.message}")
                null
            }
        }
    }

    /**
     * Create an estimate for a hot lead in the pipeline
     */
    suspend fun createEstimateForHotLead(leadId: String, projectId: String, contextMode: ContextMode): TemplateEstimate? {
        return withContext(Dispatchers.IO) {
            try {
                val estimate = createEstimateForProject(projectId, contextMode)
                Log.d(TAG, "Created estimate for hot lead: $leadId, project: $projectId")
                estimate
            } catch (e: Exception) {
                Log.e(TAG, "Error creating estimate for hot lead: ${e.message}")
                null
            }
        }
    }

    /**
     * Create an estimate for a project
     */
    private suspend fun createEstimateForProject(projectId: String, contextMode: ContextMode): TemplateEstimate {
        // Create the estimate
        val estimate = templateEstimateRepository.createEstimate(projectId, contextMode)

        // Get relevant assembly templates for the context mode
        val templates = templateEstimateRepository.getAssemblyTemplatesByContextMode(contextMode)

        // Auto-select relevant assemblies based on context mode
        // In a real app, this would use AI to select the most relevant assemblies
        // For this implementation, we'll just add the first assembly if available
        if (templates.isNotEmpty()) {
            val template = templates.first()
            templateEstimateRepository.addAssemblyToEstimate(estimate.id, template, template.baseQuantity)
        }

        return estimate
    }

    /**
     * Extract project type from voice command
     */
    private fun extractProjectType(command: String): String {
        // Extract project type using simple keyword matching
        // In a real app, this would use NLP to extract the project type
        val keywords = listOf("bathroom", "kitchen", "basement", "roof", "deck", "addition", "garage")
        for (keyword in keywords) {
            if (command.contains(keyword)) {
                return keyword
            }
        }

        return "remodel" // Default
    }

    /**
     * Find a project by type
     */
    private suspend fun findProjectByType(projectType: String): com.nextgenbuildpro.pm.data.model.Project? {
        val projects = projectRepository.getAll()

        // First try to find a project with the type in the name
        val projectByName = projects.find { 
            it.name.lowercase().contains(projectType.lowercase()) 
        }

        if (projectByName != null) {
            return projectByName
        }

        // If no match by name, try to find a project with the type in the description
        val projectByDescription = projects.find { 
            it.description.lowercase().contains(projectType.lowercase()) 
        }

        if (projectByDescription != null) {
            return projectByDescription
        }

        // If still no match, return the first active project if available
        return projects.find { it.status == "In Progress" }
    }

    companion object {
        /**
         * Create an EstimateService instance
         */
        fun create(
            context: Context,
            templateEstimateRepository: TemplateEstimateRepositoryImpl,
            projectRepository: ProjectRepository
        ): EstimateService {
            return EstimateService(context, templateEstimateRepository, projectRepository)
        }
    }
}
