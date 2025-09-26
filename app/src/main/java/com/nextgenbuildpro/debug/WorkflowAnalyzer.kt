package com.nextgenbuildpro.debug

import android.util.Log
import com.nextgenbuildpro.navigation.NavDestinations
import com.nextgenbuildpro.util.ErrorLogger
import java.util.concurrent.ConcurrentHashMap

/**
 * WorkflowAnalyzer - Utility to identify incomplete workflows and dead-end features
 *
 * This class tracks user journeys through the app and identifies:
 * - Incomplete workflows (paths that don't reach completion)
 * - Dead-end features (UI elements that lead to unimplemented or broken features)
 * - User journey interruptions (where users abandon a workflow)
 */
object WorkflowAnalyzer {
    private const val TAG = "WorkflowAnalyzer"

    // Define known workflow paths and their completion states
    private val knownWorkflows = mapOf(
        "lead_creation" to listOf(
            NavDestinations.LEADS,
            NavDestinations.LEAD_EDITOR,
            NavDestinations.LEAD_DETAIL
        ),
        "estimate_creation" to listOf(
            NavDestinations.ESTIMATES,
            NavDestinations.ESTIMATE_EDITOR,
            NavDestinations.ESTIMATE_DETAIL
        ),
        "calendar_event" to listOf(
            NavDestinations.CALENDAR,
            NavDestinations.CALENDAR_EVENT_EDITOR,
            NavDestinations.CALENDAR
        )
        // Add more workflows as needed
    )

    // Track active user journeys (userId -> JourneyInfo)
    private val activeJourneys = ConcurrentHashMap<String, JourneyInfo>()

    // Track dead-end encounters (destination -> count)
    private val deadEndEncounters = ConcurrentHashMap<String, Int>()

    // Track incomplete workflows (workflowType -> count)
    private val incompleteWorkflows = ConcurrentHashMap<String, Int>()

    // Track UI elements leading to dead ends (elementId -> DeadEndInfo)
    private val deadEndElements = ConcurrentHashMap<String, DeadEndInfo>()

    /**
     * Track a screen visit
     *
     * @param userId Identifier for the current user
     * @param destination The screen/destination being visited
     * @param sourceElementId Optional ID of UI element that triggered navigation
     */
    fun trackScreenVisit(userId: String, destination: String, sourceElementId: String? = null) {
        try {
            Log.d(TAG, "User $userId visited screen: $destination")

            // Get or create journey for this user
            val journey = activeJourneys.getOrPut(userId) { JourneyInfo(userId) }

            // Add this destination to the user's path
            journey.path.add(destination)

            // Check if this could be part of a known workflow
            updateWorkflowProgress(journey, destination)

            // Check if this is a potential dead end
            checkForDeadEnd(destination, sourceElementId)

        } catch (e: Exception) {
            ErrorLogger.logError(TAG, "Error tracking screen visit", e, mapOf(
                "userId" to userId,
                "destination" to destination
            ))
        }
    }

    /**
     * Mark a workflow as abandoned
     *
     * @param userId Identifier for the current user
     * @param reason Optional reason for abandonment
     */
    fun markWorkflowAbandoned(userId: String, reason: String? = null) {
        val journey = activeJourneys[userId] ?: return

        // Identify which workflow was potentially abandoned
        val workflowType = journey.activeWorkflow
        if (workflowType != null) {
            Log.d(TAG, "User $userId abandoned $workflowType workflow, reason: $reason")

            // Increment count for this incomplete workflow
            incompleteWorkflows.merge(workflowType, 1) { old, _ -> old + 1 }

            // Reset the active workflow
            journey.activeWorkflow = null
            journey.workflowProgress = 0
        }
    }

    /**
     * Register a dead-end UI element
     *
     * @param elementId Identifier for the UI element
     * @param screenName Name of the screen containing the element
     * @param intendedDestination The intended destination that's not implemented
     */
    fun registerDeadEndElement(elementId: String, screenName: String, intendedDestination: String) {
        val info = deadEndElements.getOrPut(elementId) {
            DeadEndInfo(elementId, screenName, intendedDestination)
        }

        // Increment the encounter count
        info.encounterCount++

        Log.d(TAG, "Dead-end element encountered: $elementId on $screenName (count: ${info.encounterCount})")
    }

    /**
     * Update workflow progress based on the current destination
     */
    private fun updateWorkflowProgress(journey: JourneyInfo, currentDestination: String) {
        // If there's no active workflow, try to find one that starts with this destination
        if (journey.activeWorkflow == null) {
            for ((workflowType, path) in knownWorkflows) {
                if (path.firstOrNull() == currentDestination) {
                    journey.activeWorkflow = workflowType
                    journey.workflowProgress = 1
                    Log.d(TAG, "Started workflow: $workflowType for user ${journey.userId}")
                    break
                }
            }
            return
        }

        // If there is an active workflow, check if we're making progress
        val workflowType = journey.activeWorkflow ?: return
        val workflowPath = knownWorkflows[workflowType] ?: return

        // Find where we should be in the workflow
        val expectedNextIndex = journey.workflowProgress
        if (expectedNextIndex >= workflowPath.size) {
            // Workflow is already complete
            return
        }

        if (workflowPath[expectedNextIndex] == currentDestination) {
            // We're making progress through the workflow
            journey.workflowProgress++
            Log.d(TAG, "Progress in workflow $workflowType: ${journey.workflowProgress}/${workflowPath.size}")

            // Check if workflow is complete
            if (journey.workflowProgress >= workflowPath.size) {
                Log.d(TAG, "Workflow completed: $workflowType for user ${journey.userId}")
                journey.completedWorkflows.add(workflowType)
                journey.activeWorkflow = null
                journey.workflowProgress = 0
            }
        } else if (!workflowPath.contains(currentDestination)) {
            // We've deviated from the workflow
            Log.d(TAG, "Workflow deviation: expected ${workflowPath[expectedNextIndex]}, got $currentDestination")
            markWorkflowAbandoned(journey.userId, "navigation deviation")
        }
    }

    /**
     * Check if the current destination might be a dead end
     */
    private fun checkForDeadEnd(destination: String, sourceElementId: String?) {
        // Check if this destination has limited onward navigation options
        val isDeadEnd = when {
            // Define criteria for dead ends, for example:
            destination.endsWith("_detail") && destination != NavDestinations.LEAD_DETAIL &&
                    destination != NavDestinations.ESTIMATE_DETAIL -> true
            destination == NavDestinations.ROOM_SCAN -> true
            // Add more criteria as needed
            else -> false
        }

        if (isDeadEnd) {
            // Increment counter for this dead end
            deadEndEncounters.merge(destination, 1) { old, _ -> old + 1 }

            // Record the UI element if provided
            if (sourceElementId != null) {
                val info = deadEndElements.getOrPut(sourceElementId) {
                    DeadEndInfo(sourceElementId, "unknown", destination)
                }
                info.encounterCount++
            }

            Log.d(TAG, "Potential dead end encountered: $destination")
        }
    }

    /**
     * Generate a report of incomplete workflows and dead ends
     *
     * @return A formatted report string
     */
    fun generateReport(): String {
        val sb = StringBuilder()
        sb.appendLine("===== WORKFLOW & DEAD END ANALYSIS =====")

        // Report on dead ends
        sb.appendLine("\n🚧 DEAD END DESTINATIONS:")
        if (deadEndEncounters.isEmpty()) {
            sb.appendLine("No dead ends detected")
        } else {
            deadEndEncounters.entries
                .sortedByDescending { it.value }
                .forEach { (destination, count) ->
                    sb.appendLine("- $destination: encountered $count times")
                }
        }

        // Report on dead end UI elements
        sb.appendLine("\n🚫 DEAD END UI ELEMENTS:")
        if (deadEndElements.isEmpty()) {
            sb.appendLine("No dead end UI elements detected")
        } else {
            deadEndElements.values
                .sortedByDescending { it.encounterCount }
                .forEach { info ->
                    sb.appendLine("- ${info.elementId} on ${info.screenName}: ${info.encounterCount} encounters (intended: ${info.intendedDestination})")
                }
        }

        // Report on incomplete workflows
        sb.appendLine("\n⚠️ INCOMPLETE WORKFLOWS:")
        if (incompleteWorkflows.isEmpty()) {
            sb.appendLine("No incomplete workflows detected")
        } else {
            incompleteWorkflows.entries
                .sortedByDescending { it.value }
                .forEach { (workflow, count) ->
                    sb.appendLine("- $workflow: abandoned $count times")
                }
        }

        // Report on active journeys
        sb.appendLine("\n👤 ACTIVE USER JOURNEYS:")
        if (activeJourneys.isEmpty()) {
            sb.appendLine("No active journeys")
        } else {
            activeJourneys.values.forEach { journey ->
                sb.appendLine("- User ${journey.userId}: ${journey.path.size} steps")
                if (journey.activeWorkflow != null) {
                    sb.appendLine("  Current workflow: ${journey.activeWorkflow} (progress: ${journey.workflowProgress})")
                }
                sb.appendLine("  Completed workflows: ${journey.completedWorkflows.joinToString(", ") { it } ?: "none"}")
            }
        }

        return sb.toString()
    }

    /**
     * Log the analysis report
     */
    fun logReport() {
        val report = generateReport()

        // Split the report into chunks if necessary
        val maxLength = 4000
        var i = 0
        while (i < report.length) {
            val end = (i + maxLength).coerceAtMost(report.length)
            Log.d(TAG, report.substring(i, end))
            i = end
        }
    }

    /**
     * Reset all tracking data
     */
    fun reset() {
        activeJourneys.clear()
        deadEndEncounters.clear()
        incompleteWorkflows.clear()
        deadEndElements.clear()
        Log.d(TAG, "Workflow analysis data reset")
    }

    /**
     * Data class representing a user's journey through the app
     */
    data class JourneyInfo(
        val userId: String,
        val path: MutableList<String> = mutableListOf(),
        var activeWorkflow: String? = null,
        var workflowProgress: Int = 0,
        val completedWorkflows: MutableList<String> = mutableListOf()
    )

    /**
     * Data class representing information about a dead-end UI element
     */
    data class DeadEndInfo(
        val elementId: String,
        val screenName: String,
        val intendedDestination: String,
        var encounterCount: Int = 0
    )
}
