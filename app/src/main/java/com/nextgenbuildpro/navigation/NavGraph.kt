package com.nextgenbuildpro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nextgenbuildpro.features.home.HomeScreen
import com.nextgenbuildpro.features.leads.LeadsScreen
import com.nextgenbuildpro.features.leads.LeadDetailScreen
import com.nextgenbuildpro.features.estimates.EstimatesScreen
import com.nextgenbuildpro.features.estimates.EstimateDetailScreen
import com.nextgenbuildpro.features.tasks.TasksScreen
import com.nextgenbuildpro.features.calendar.CalendarScreen
import com.nextgenbuildpro.features.calendar.CalendarEventEditorScreen
import com.nextgenbuildpro.features.estimates.EstimateItemEditorScreen
//import com.nextgenbuildpro.fieldtools.ui.ArVisualizationScreen
//import com.nextgenbuildpro.fieldtools.ui.VoiceToTextScreen
//import com.nextgenbuildpro.fieldtools.ui.OfflineModeScreen
import com.nextgenbuildpro.clientengagement.ui.ClientPortalScreen
import com.nextgenbuildpro.clientengagement.ui.ProgressUpdatesScreen
import com.nextgenbuildpro.clientengagement.ui.DigitalSignatureScreen
import com.nextgenbuildpro.receptionist.ui.AIReceptionistSettingsScreen
import com.nextgenbuildpro.timeclock.ui.TimeClockScreen
//import com.nextgenbuildpro.ui.PlaceholderScreen
import com.nextgenbuildpro.crm.ui.MessagesScreen
import androidx.compose.runtime.remember
import com.nextgenbuildpro.crm.rememberCrmComponents

/**
 * Main navigation graph for the application.
 * This defines all the routes and connects the different feature modules.
 */
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavDestinations.HOME
    ) {
        // Home
        composable(NavDestinations.HOME) {
            HomeScreen(navController)
        }

        // Leads
        composable(NavDestinations.LEADS) {
            LeadsScreen(navController)
        }

        composable(
            route = "${NavDestinations.LEAD_DETAIL}/{leadId}",
            arguments = listOf(navArgument("leadId") { type = NavType.StringType })
        ) { backStackEntry ->
            val leadId = backStackEntry.arguments?.getString("leadId") ?: ""
            LeadDetailScreen(navController, leadId)
        }

        composable(NavDestinations.LEAD_EDITOR) {
            com.nextgenbuildpro.features.leads.LeadEditorScreen(navController)
        }

        composable(
            route = "${NavDestinations.LEAD_EDITOR}/{leadId}",
            arguments = listOf(navArgument("leadId") { type = NavType.StringType })
        ) { backStackEntry ->
            val leadId = backStackEntry.arguments?.getString("leadId") ?: ""
            com.nextgenbuildpro.features.leads.LeadEditorScreen(navController, leadId)
        }

        // Estimates
        composable(NavDestinations.ESTIMATES) {
            EstimatesScreen(navController)
        }

        composable(
            route = "${NavDestinations.ESTIMATE_DETAIL}/{estimateId}",
            arguments = listOf(navArgument("estimateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val estimateId = backStackEntry.arguments?.getString("estimateId") ?: ""
            EstimateDetailScreen(navController, estimateId)
        }

        composable(NavDestinations.ESTIMATE_EDITOR) {
            com.nextgenbuildpro.features.estimates.TemplateEstimateEditorScreen(navController)
        }

        composable(
            route = "${NavDestinations.ESTIMATE_ITEM_EDITOR}/{estimateId}/{itemId}",
            arguments = listOf(
                navArgument("estimateId") { type = NavType.StringType },
                navArgument("itemId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val estimateId = backStackEntry.arguments?.getString("estimateId") ?: ""
            val itemId = backStackEntry.arguments?.getString("itemId")
            EstimateItemEditorScreen(
                navController = navController,
                estimateId = estimateId,
                itemId = itemId
            )
        }

        // Projects
        composable(NavDestinations.PROJECTS) {
            com.nextgenbuildpro.features.projects.AssembliesScreen(navController)
        }

        composable(
            route = "${NavDestinations.PROJECT_DETAIL}/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("projectId") ?: ""
            //PlaceholderScreen(
            //    navController = navController,
            //    title = "Project Details",
            //    message = "The project details feature is coming soon. You'll be able to view and manage all aspects of your projects."
            //)
        }

        // Assembly and Template details
        composable(
            route = "assembly_detail/{assemblyId}",
            arguments = listOf(navArgument("assemblyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val assemblyId = backStackEntry.arguments?.getString("assemblyId") ?: ""
            com.nextgenbuildpro.features.projects.AssemblyDetailScreen(
                navController = navController,
                assemblyId = assemblyId
            )
        }

        composable(
            route = "template_detail/{templateId}",
            arguments = listOf(navArgument("templateId") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("templateId") ?: ""
            //PlaceholderScreen(
            //    navController = navController,
            //    title = "Template Details",
            //    message = "The template details feature is coming soon. You'll be able to view and edit template details, and create projects from templates."
            //)
        }

        // Camera & Room Scan
        composable(NavDestinations.CAMERA) {
            //PlaceholderScreen(
            //    navController = navController,
            //    title = "Camera",
            //    message = "The camera feature is coming soon. You'll be able to take photos directly within the app."
            //)
        }

        composable(NavDestinations.ROOM_SCAN) {
            //PlaceholderScreen(
            //    navController = navController,
            //    title = "Room Scan",
            //    message = "The room scanning feature is coming soon. You'll be able to create 3D models of rooms for better project planning."
            //)
        }

        // Messages
        composable(NavDestinations.MESSAGES) {
            val crmComponents = rememberCrmComponents()
            MessagesScreen(
                navController = navController,
                viewModel = crmComponents.messagesViewModel
            )
        }

        composable(
            route = "${NavDestinations.MESSAGE_DETAIL}/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("conversationId") ?: ""
            //PlaceholderScreen(
            //    navController = navController,
            //    title = "Conversation",
            //    message = "The conversation view is coming soon. You'll be able to see your message history with clients."
            //)
        }

        // File Upload
        composable(NavDestinations.FILE_UPLOAD) {
            //PlaceholderScreen(
            //    navController = navController,
            //    title = "File Upload",
            //    message = "The file upload feature is coming soon. You'll be able to upload and manage documents related to your projects."
            //)
        }

        // Notes
        composable(
            route = "${NavDestinations.NOTE_EDITOR}/{leadId}",
            arguments = listOf(navArgument("leadId") { type = NavType.StringType })
        ) { backStackEntry ->
            val leadId = backStackEntry.arguments?.getString("leadId") ?: ""
            com.nextgenbuildpro.features.leads.NoteEditorScreen(navController, leadId)
        }

        // Settings
        composable(NavDestinations.ACCOUNT_SETTINGS) {
            //PlaceholderScreen(
            //    navController = navController,
            //    title = "Account Settings",
            //    message = "The account settings feature is coming soon. You'll be able to manage your account preferences."
            //)
        }

        composable(NavDestinations.PERMISSIONS) {
            //PlaceholderScreen(
            //    navController = navController,
            //    title = "Permissions",
            //    message = "The permissions management feature is coming soon. You'll be able to control app permissions."
            //)
        }

        // Notifications
        composable(NavDestinations.NOTIFICATIONS) {
            //PlaceholderScreen(
            //    navController = navController,
            //    title = "Notifications",
            //    message = "The notifications feature is coming soon. You'll be able to view and manage your notifications."
            //)
        }

        // Tasks
        composable("tasks") {
            TasksScreen(navController)
        }

        // Calendar
        composable(NavDestinations.CALENDAR) {
            CalendarScreen(navController)
        }

        composable(NavDestinations.CALENDAR_EVENT_EDITOR) {
            CalendarEventEditorScreen(navController)
        }

        // Add a new route that accepts a leadId parameter
        composable(
            route = "${NavDestinations.CALENDAR_EVENT_EDITOR}?leadId={leadId}",
            arguments = listOf(
                navArgument("leadId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val leadId = backStackEntry.arguments?.getString("leadId")
            CalendarEventEditorScreen(navController, leadId)
        }

        // Add a route for editing an existing event
        composable(
            route = "${NavDestinations.CALENDAR_EVENT_EDITOR}/{eventId}",
            arguments = listOf(
                navArgument("eventId") { 
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            CalendarEventEditorScreen(navController, eventId = eventId)
        }

        composable(NavDestinations.CALENDAR_TIMELINE) {
            //PlaceholderScreen(
            //    navController = navController,
            //    title = "Project Timeline",
            //    message = "The detailed project timeline view is coming soon. You'll be able to see a Gantt chart of your project schedule."
            //)
        }

        // Time Clock
        composable(NavDestinations.TIME_CLOCK) {
            TimeClockScreen(navController)
        }

        // Field Tools - AR Visualization
        composable(NavDestinations.AR_VISUALIZATION) {
            //ArVisualizationScreen(navController)
        }

        // Field Tools - Voice to Text
        composable(NavDestinations.VOICE_TO_TEXT) {
            //VoiceToTextScreen(navController)
        }

        // Field Tools - Offline Mode
        composable(NavDestinations.OFFLINE_MODE) {
            //OfflineModeScreen(navController)
        }

        // Client Engagement - Client Portal
        composable(NavDestinations.CLIENT_PORTAL) {
            ClientPortalScreen(navController)
        }

        // Client Engagement - Progress Updates
        composable(NavDestinations.PROGRESS_UPDATES) {
            ProgressUpdatesScreen(navController)
        }

        // Client Engagement - Digital Signature
        composable(NavDestinations.DIGITAL_SIGNATURE) {
            com.nextgenbuildpro.clientengagement.ui.DigitalSignatureScreen(navController)
        }

        // Digital Signature with lead ID
        composable(
            route = "${NavDestinations.DIGITAL_SIGNATURE}/{leadId}",
            arguments = listOf(navArgument("leadId") { type = NavType.StringType })
        ) { backStackEntry ->
            val leadId = backStackEntry.arguments?.getString("leadId") ?: ""
            com.nextgenbuildpro.clientengagement.ui.DigitalSignatureScreen(
                navController = navController,
                leadId = leadId
            )
        }

        // Digital Signature with document ID
        composable(
            route = "${NavDestinations.DIGITAL_SIGNATURE}/document/{documentId}",
            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId") ?: ""
            com.nextgenbuildpro.clientengagement.ui.DigitalSignatureScreen(
                navController = navController,
                documentId = documentId
            )
        }

        // More
        composable(NavDestinations.MORE) {
            com.nextgenbuildpro.features.more.MoreScreen(navController)
        }

        // AI Receptionist Settings
        composable(NavDestinations.AI_RECEPTIONIST_SETTINGS) {
            AIReceptionistSettingsScreen(navController)
        }
    }
}

/**
 * Object containing all navigation destinations as constants.
 */
object NavDestinations {
    // Main sections
    const val HOME = "home"
    const val LEADS = "leads"
    const val ESTIMATES = "estimates"
    const val PROJECTS = "projects"
    const val MORE = "more"

    // Leads
    const val LEAD_DETAIL = "lead_detail"
    const val LEAD_EDITOR = "lead_editor"

    // Estimates
    const val ESTIMATE_DETAIL = "estimate_detail"
    const val ESTIMATE_EDITOR = "estimate_editor"
    const val ESTIMATE_ITEM_EDITOR = "estimate_item_editor"

    // Projects
    const val PROJECT_DETAIL = "project_detail"

    // Camera & Room Scan
    const val CAMERA = "camera"
    const val ROOM_SCAN = "room_scan"

    // Messages
    const val MESSAGES = "messages"
    const val MESSAGE_DETAIL = "message_detail"

    // File Upload
    const val FILE_UPLOAD = "file_upload"

    // Notes
    const val NOTE_EDITOR = "note_editor"

    // Settings
    const val ACCOUNT_SETTINGS = "account_settings"
    const val PERMISSIONS = "permissions"

    // Notifications
    const val NOTIFICATIONS = "notifications"

    // Calendar
    const val CALENDAR = "calendar"
    const val CALENDAR_EVENT_EDITOR = "calendar_event_editor"
    const val CALENDAR_TIMELINE = "calendar_timeline"

    // Time Clock
    const val TIME_CLOCK = "time_clock"

    // Field Tools
    const val AR_VISUALIZATION = "ar_visualization"
    const val VOICE_TO_TEXT = "voice_to_text"
    const val OFFLINE_MODE = "offline_mode"

    // Client Engagement
    const val CLIENT_PORTAL = "client_portal"
    const val PROGRESS_UPDATES = "progress_updates"
    const val DIGITAL_SIGNATURE = "digital_signature"

    // AI Receptionist
    const val AI_RECEPTIONIST_SETTINGS = "ai_receptionist_settings"
}
