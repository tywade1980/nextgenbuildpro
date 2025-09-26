package com.nextgenbuildpro.ui

import android.util.Log
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.nextgenbuildpro.debug.WorkflowAnalyzer
import com.nextgenbuildpro.navigation.NavigationHelper
import com.nextgenbuildpro.util.ErrorLogger
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * FeatureCompletionTracker - Track UI interactions and identify incomplete features
 *
 * This utility tracks user interactions with UI elements and helps identify:
 * - Buttons that lead to unimplemented features
 * - Navigation paths that don't complete as expected
 * - Features that don't fulfill their intended purpose
 */
object FeatureCompletionTracker {
    private const val TAG = "FeatureTracker"

    // Track clicked elements and their intended destinations
    private val clickedElements = ConcurrentHashMap<String, ElementInfo>()

    // Track sessions for user journeys
    private val activeSessions = ConcurrentHashMap<String, SessionInfo>()

    // Map of expected completion states for features
    private val featureCompletionStates = mapOf(
        "create_lead" to "lead_detail",
        "edit_estimate" to "estimate_detail",
        "schedule_appointment" to "calendar",
        "upload_photos" to "project_detail",
        "scan_room" to "ar_visualization"
        // Add more feature completion mappings
    )

    /**
     * Create or get a session ID for the current user journey
     */
    fun getOrCreateSessionId(): String {
        // In a real app, you'd use a persistent user ID
        val sessionId = "session_${UUID.randomUUID()}"
        activeSessions[sessionId] = SessionInfo(sessionId)
        return sessionId
    }

    /**
     * Track a button click that initiates a feature
     *
     * @param elementId Unique identifier for the UI element
     * @param screenName Name of the current screen
     * @param featureName Name of the feature being initiated
     * @param expectedDestination Expected final destination for this feature
     */
    fun trackFeatureStart(
        elementId: String,
        screenName: String,
        featureName: String,
        expectedDestination: String? = null
    ): String {
        val sessionId = getOrCreateSessionId()
        val session = activeSessions[sessionId]!!

        // Create element info
        val info = ElementInfo(
            id = elementId,
            screenName = screenName,
            featureName = featureName,
            expectedDestination = expectedDestination ?: featureCompletionStates[featureName],
            timestamp = System.currentTimeMillis()
        )

        // Store element info
        clickedElements[elementId] = info

        // Update session
        session.currentFeature = featureName
        session.featureStartTime = System.currentTimeMillis()
        session.featureStartElement = elementId

        Log.d(TAG, "Feature started: $featureName from $elementId on $screenName (session: $sessionId)")

        return sessionId
    }

    /**
     * Track navigation to a screen after button click
     *
     * @param sessionId Current session ID
     * @param destination The destination screen reached
     * @param success Whether navigation was successful
     */
    fun trackNavigation(
        sessionId: String,
        destination: String,
        success: Boolean
    ) {
        val session = activeSessions[sessionId] ?: return

        // Record this destination in the path
        session.navigationPath.add(destination)

        // Check if this completes the expected feature
        val currentFeature = session.currentFeature
        if (currentFeature != null) {
            val expectedDestination = featureCompletionStates[currentFeature]

            if (expectedDestination == destination) {
                // Feature completed successfully
                markFeatureComplete(sessionId, true, "Reached expected destination")
            } else if (!success) {
                // Navigation failed
                markFeatureIncomplete(sessionId, "Navigation failed to: $destination")
            }
        }

        Log.d(TAG, "Navigation to $destination (success=$success) for session: $sessionId")

        // Track in the WorkflowAnalyzer too
        WorkflowAnalyzer.trackScreenVisit(
            userId = sessionId,
            destination = destination,
            sourceElementId = session.featureStartElement
        )
    }

    /**
     * Mark a feature as complete
     *
     * @param sessionId Current session ID
     * @param successful Whether the feature completed successfully
     * @param reason Reason for completion status
     */
    fun markFeatureComplete(
        sessionId: String,
        successful: Boolean,
        reason: String
    ) {
        val session = activeSessions[sessionId] ?: return
        val featureName = session.currentFeature ?: return
        val elementId = session.featureStartElement ?: return

        // Update element info
        val elementInfo = clickedElements[elementId]
        if (elementInfo != null) {
            elementInfo.featureCompleted = successful
            elementInfo.completionReason = reason
            elementInfo.completionTime = System.currentTimeMillis()
        }

        // Log the feature completion
        if (successful) {
            Log.d(TAG, "Feature completed successfully: $featureName, reason: $reason")
        } else {
            Log.w(TAG, "Feature failed to complete: $featureName, reason: $reason")

            // Record as a dead end if unsuccessful
            elementInfo?.expectedDestination?.let {
                WorkflowAnalyzer.registerDeadEndElement(elementId, elementInfo.screenName, it)
            }
        }

        // Reset the current feature
        session.currentFeature = null
        session.featureStartElement = null
    }

    /**
     * Mark a feature as incomplete
     *
     * @param sessionId Current session ID
     * @param reason Reason why the feature is incomplete
     */
    fun markFeatureIncomplete(
        sessionId: String,
        reason: String
    ) {
        markFeatureComplete(sessionId, false, reason)
    }

    /**
     * Track a button that triggers a feature
     *
     * This is a composable wrapper that can be used with any clickable UI element
     * to track its behavior and detect dead-end features.
     *
     * @param elementId Unique identifier for this UI element
     * @param screenName Name of the current screen
     * @param featureName Name of the feature being triggered
     * @param navController Navigation controller for tracking navigation
     * @param expectedDestination Expected final destination for this feature
     * @return A session ID that can be used to track this interaction
     */
    @Composable
    fun TrackFeature(
        elementId: String,
        screenName: String,
        featureName: String,
        navController: NavController? = null,
        expectedDestination: String? = null
    ): String {
        val context = LocalContext.current
        val sessionId = remember { getOrCreateSessionId() }
        val navigationHelper = remember { NavigationHelper.create(context) }

        // Effect to track the feature start
        DisposableEffect(elementId) {
            trackFeatureStart(elementId, screenName, featureName, expectedDestination)

            // If we have a NavController, set up a destination change listener
            navController?.let { nav ->
                val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                    val destRoute = destination.route ?: destination.id.toString()
                    val implemented = navigationHelper.isDestinationImplemented(destRoute)
                    trackNavigation(sessionId, destRoute, implemented)
                }

                nav.addOnDestinationChangedListener(listener)

                onDispose {
                    nav.removeOnDestinationChangedListener(listener)
                }
            }

            onDispose {
                // Check if the feature was left incomplete
                val session = activeSessions[sessionId]
                if (session?.currentFeature != null) {
                    markFeatureIncomplete(sessionId, "UI component disposed before feature completed")
                }
            }
        }

        return sessionId
    }

    /**
     * Generate a report of feature completions and dead ends
     */
    fun generateReport(): String {
        val sb = StringBuilder()
        sb.appendLine("===== FEATURE COMPLETION ANALYSIS =====")

        // Count complete vs incomplete features
        val completed = clickedElements.values.count { it.featureCompleted }
        val incomplete = clickedElements.values.count { !it.featureCompleted }

        sb.appendLine("\nSUMMARY:")
        sb.appendLine("- Total features tracked: ${clickedElements.size}")
        sb.appendLine("- Features completed: $completed")
        sb.appendLine("- Features incomplete: $incomplete")
        sb.appendLine("- Completion rate: ${if (clickedElements.isNotEmpty()) (completed * 100 / clickedElements.size) else 0}%")

        // List incomplete features
        sb.appendLine("\n⚠️ INCOMPLETE FEATURES:")
        if (incomplete == 0) {
            sb.appendLine("No incomplete features detected")
        } else {
            clickedElements.values
                .filter { !it.featureCompleted }
                .sortedBy { it.screenName }
                .forEach { info ->
                    val duration = if (info.completionTime > 0) {
                        (info.completionTime - info.timestamp) / 1000
                    } else "unknown"

                    sb.appendLine("- ${info.featureName} (${info.id} on ${info.screenName})")
                    sb.appendLine("  Expected: ${info.expectedDestination ?: "unknown"}")
                    sb.appendLine("  Reason: ${info.completionReason ?: "unknown"}")
                    sb.appendLine("  Duration: ${duration}s")
                }
        }

        // Active sessions
        sb.appendLine("\n👤 ACTIVE SESSIONS:")
        if (activeSessions.isEmpty()) {
            sb.appendLine("No active sessions")
        } else {
            activeSessions.values.forEach { session ->
                sb.appendLine("- Session ${session.id}: ${session.navigationPath.size} navigation steps")
                if (session.currentFeature != null) {
                    sb.appendLine("  Current feature: ${session.currentFeature}")
                    sb.appendLine("  Started: ${(System.currentTimeMillis() - session.featureStartTime) / 1000}s ago")
                }
                sb.appendLine("  Path: ${session.navigationPath.joinToString(" → ")}")
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
        clickedElements.clear()
        activeSessions.clear()
        Log.d(TAG, "Feature completion tracking data reset")
    }

    /**
     * Data class representing information about a UI element
     */
    data class ElementInfo(
        val id: String,
        val screenName: String,
        val featureName: String,
        val expectedDestination: String? = null,
        val timestamp: Long,
        var featureCompleted: Boolean = false,
        var completionReason: String? = null,
        var completionTime: Long = 0
    )

    /**
     * Data class representing a user session
     */
    data class SessionInfo(
        val id: String,
        var currentFeature: String? = null,
        var featureStartElement: String? = null,
        var featureStartTime: Long = 0,
        val navigationPath: MutableList<String> = mutableListOf()
    )
}
