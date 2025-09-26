package com.nextgenbuildpro.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
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
import com.nextgenbuildpro.features.estimates.EstimateItemEditorScreen
import com.nextgenbuildpro.features.tasks.TasksScreen
import com.nextgenbuildpro.features.calendar.CalendarScreen
import com.nextgenbuildpro.features.calendar.CalendarEventEditorScreen
import com.nextgenbuildpro.features.estimates.AssemblySearchAndSelectionScreen
import com.nextgenbuildpro.features.estimates.EnhancedEstimateEditorScreen
import com.nextgenbuildpro.features.estimates.AssemblyIntegrationDemoScreen
import com.nextgenbuildpro.features.estimates.EnhancedCatalogueDemoScreen
//import com.nextgenbuildpro.fieldtools.ui.ArVisualizationScreen
//import com.nextgenbuildpro.fieldtools.ui.VoiceToTextScreen
//import com.nextgenbuildpro.fieldtools.ui.OfflineModeScreen

import com.nextgenbuildpro.clientengagement.ui.ClientPortalScreen
import com.nextgenbuildpro.clientengagement.ui.ProgressUpdatesScreen
import com.nextgenbuildpro.receptionist.ui.AIReceptionistSettingsScreen
import com.nextgenbuildpro.timeclock.ui.TimeClockScreen
import com.nextgenbuildpro.crm.ui.MessagesScreen
import com.nextgenbuildpro.features.automation.WorkflowAutomationScreen
import com.nextgenbuildpro.crm.rememberCrmComponents
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavDestinations.HOME
    ) {
        composable(NavDestinations.HOME) { HomeScreen(navController) }

        composable(NavDestinations.LEADS) { LeadsScreen(navController) }

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

        composable(NavDestinations.ESTIMATES) { EstimatesScreen(navController) }

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

        // Enhanced Estimate Editor with Assembly Integration
        composable(
            route = "${NavDestinations.ENHANCED_ESTIMATE_EDITOR}?estimateId={estimateId}&projectId={projectId}",
            arguments = listOf(
                navArgument("estimateId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("projectId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val estimateId = backStackEntry.arguments?.getString("estimateId")
            val projectId = backStackEntry.arguments?.getString("projectId")
            EnhancedEstimateEditorScreen(
                navController = navController,
                estimateId = estimateId,
                projectId = projectId
            )
        }

        // Assembly Search and Selection
        composable(NavDestinations.ASSEMBLY_SEARCH) {
            AssemblySearchAndSelectionScreen(navController)
        }

        // Assembly Integration Demo
        composable(NavDestinations.ASSEMBLY_INTEGRATION_DEMO) {
            AssemblyIntegrationDemoScreen(navController)
        }

        // Enhanced Catalogue Demo
        composable(NavDestinations.ENHANCED_CATALOGUE_DEMO) {
            EnhancedCatalogueDemoScreen(navController)
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

        composable(NavDestinations.PROJECTS) {
            com.nextgenbuildpro.features.projects.AssembliesScreen(navController)
        }

        composable(
            route = "${NavDestinations.PROJECT_DETAIL}/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("projectId") ?: ""
        }

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
        }

        composable(NavDestinations.CAMERA) {
            com.nextgenbuildpro.features.camera.CameraScreen(navController)
        }

        composable(NavDestinations.ROOM_SCAN) {
            com.nextgenbuildpro.features.roomscan.RoomScanScreen(navController)
        }

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
        }

        composable(NavDestinations.FILE_UPLOAD) {
            com.nextgenbuildpro.features.files.FileUploadScreen(navController)
        }

        composable(
            route = "${NavDestinations.NOTE_EDITOR}/{leadId}",
            arguments = listOf(navArgument("leadId") { type = NavType.StringType })
        ) { backStackEntry ->
            val leadId = backStackEntry.arguments?.getString("leadId") ?: ""
            com.nextgenbuildpro.features.leads.NoteEditorScreen(navController, leadId)
        }

        composable(NavDestinations.ACCOUNT_SETTINGS) {
            com.nextgenbuildpro.features.settings.AccountSettingsScreen(navController)
        }

        composable(NavDestinations.PERMISSIONS) {
            com.nextgenbuildpro.features.settings.PermissionsScreen(navController)
        }

        composable(NavDestinations.NOTIFICATIONS) {
            com.nextgenbuildpro.features.settings.NotificationsScreen(navController)
        }

        composable("tasks") { TasksScreen(navController) }

        composable(NavDestinations.CALENDAR) { CalendarScreen(navController) }

        composable(NavDestinations.CALENDAR_EVENT_EDITOR) {
            CalendarEventEditorScreen(navController)
        }

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

        composable(NavDestinations.CALENDAR_TIMELINE) { }

        composable(NavDestinations.TIME_CLOCK) { TimeClockScreen(navController) }

        composable(NavDestinations.AR_VISUALIZATION) { }

        composable(NavDestinations.VOICE_TO_TEXT) { }

        composable(NavDestinations.OFFLINE_MODE) { }

        composable(NavDestinations.CLIENT_PORTAL) { ClientPortalScreen(navController) }

        composable(NavDestinations.PROGRESS_UPDATES) { ProgressUpdatesScreen(navController) }

        composable(NavDestinations.DIGITAL_SIGNATURE) {
            com.nextgenbuildpro.clientengagement.ui.DigitalSignatureScreen(navController)
        }

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

        composable(NavDestinations.MORE) {
            com.nextgenbuildpro.features.more.MoreScreen(navController)
        }

        composable(NavDestinations.AI_RECEPTIONIST_SETTINGS) {
            AIReceptionistSettingsScreen(navController)
        }

        composable(NavDestinations.BMS) {
            com.nextgenbuildpro.bms.ui.BmsScreen(navController)
        }

        composable(NavDestinations.WORKFLOW_AUTOMATION) {
            WorkflowAutomationScreen(navController)
        }

        composable(NavDestinations.CALL_SCREEN) {
            // Call handling features removed - placeholder screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Call Screen - Features Removed")
            }
        }
    }
}

object NavDestinations {
    const val HOME = "home"
    const val LEADS = "leads"
    const val ESTIMATES = "estimates"
    const val PROJECTS = "projects"
    const val MORE = "more"

    const val LEAD_DETAIL = "lead_detail"
    const val LEAD_EDITOR = "lead_editor"

    const val ESTIMATE_DETAIL = "estimate_detail"
    const val ESTIMATE_EDITOR = "estimate_editor"
    const val ESTIMATE_ITEM_EDITOR = "estimate_item_editor"
    const val ASSEMBLY_SEARCH = "assembly_search"
    const val ENHANCED_ESTIMATE_EDITOR = "enhanced_estimate_editor"
    const val ASSEMBLY_INTEGRATION_DEMO = "assembly_integration_demo"
    const val ENHANCED_CATALOGUE_DEMO = "enhanced_catalogue_demo"

    const val PROJECT_DETAIL = "project_detail"

    const val CAMERA = "camera"
    const val ROOM_SCAN = "room_scan"

    const val MESSAGES = "messages"
    const val MESSAGE_DETAIL = "message_detail"

    const val FILE_UPLOAD = "file_upload"

    const val NOTE_EDITOR = "note_editor"

    const val ACCOUNT_SETTINGS = "account_settings"
    const val PERMISSIONS = "permissions"

    const val NOTIFICATIONS = "notifications"

    const val CALENDAR = "calendar"
    const val CALENDAR_EVENT_EDITOR = "calendar_event_editor"
    const val CALENDAR_TIMELINE = "calendar_timeline"

    const val TIME_CLOCK = "time_clock"

    const val AR_VISUALIZATION = "ar_visualization"
    const val VOICE_TO_TEXT = "voice_to_text"
    const val OFFLINE_MODE = "offline_mode"

    const val CLIENT_PORTAL = "client_portal"
    const val PROGRESS_UPDATES = "progress_updates"
    const val DIGITAL_SIGNATURE = "digital_signature"

    const val AI_RECEPTIONIST_SETTINGS = "ai_receptionist_settings"

    const val BMS = "bms"
    const val BUILDING_DETAIL = "building_detail"

    const val WORKFLOW_AUTOMATION = "workflow_automation"
    const val CALL_SCREEN = "call_screen"
}