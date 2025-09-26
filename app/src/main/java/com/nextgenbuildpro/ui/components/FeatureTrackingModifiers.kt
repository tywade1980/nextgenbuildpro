package com.nextgenbuildpro.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.navigation.NavController
import com.nextgenbuildpro.debug.WorkflowAnalyzer
import com.nextgenbuildpro.ui.ButtonNavigationValidator
import com.nextgenbuildpro.ui.FeatureCompletionTracker

/**
 * Extension functions to track button clicks and feature completions
 *
 * These functions can be applied to any clickable Compose element to track
 * when buttons lead to dead-end features or incomplete workflows.
 */

/**
 * Track a button click that should navigate to a destination
 *
 * @param buttonId Unique identifier for the button
 * @param screenName The name of the current screen
 * @param destination The navigation destination
 * @param navController The navigation controller
 * @return A Modifier with the tracking logic
 */
fun Modifier.trackNavigation(
    buttonId: String,
    screenName: String,
    destination: String,
    navController: NavController
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }

    this.clickable(
        interactionSource = interactionSource,
        indication = null
    ) {
        // Track with ButtonNavigationValidator
        ButtonNavigationValidator.validateAndNavigate(
            navController = navController,
            destination = destination,
            buttonId = buttonId,
            screenName = screenName
        )

        // Also track as part of workflow analysis
        WorkflowAnalyzer.trackScreenVisit(
            userId = buttonId, // Using buttonId as a simple user identifier
            destination = destination,
            sourceElementId = buttonId
        )
    }
}

/**
 * Track a feature initiation
 *
 * @param buttonId Unique identifier for the button
 * @param screenName The name of the current screen
 * @param featureName The name of the feature being initiated
 * @param action The action to perform when clicked
 * @return A Modifier with the tracking logic
 */
fun Modifier.trackFeature(
    buttonId: String,
    screenName: String,
    featureName: String,
    action: (String) -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }

    this.clickable(
        interactionSource = interactionSource,
        indication = null
    ) {
        // Track with FeatureCompletionTracker
        val sessionId = FeatureCompletionTracker.trackFeatureStart(
            elementId = buttonId,
            screenName = screenName,
            featureName = featureName
        )

        // Execute the action with the session ID
        action(sessionId)
    }
}

/**
 * Mark a feature as complete or incomplete
 *
 * @param sessionId The session ID from trackFeature
 * @param successful Whether the feature completed successfully
 * @param reason Reason for completion status
 */
fun completeFeature(sessionId: String, successful: Boolean, reason: String) {
    if (successful) {
        FeatureCompletionTracker.markFeatureComplete(sessionId, true, reason)
    } else {
        FeatureCompletionTracker.markFeatureIncomplete(sessionId, reason)
    }
}
