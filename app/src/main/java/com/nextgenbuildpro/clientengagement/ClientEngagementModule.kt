package com.nextgenbuildpro.clientengagement

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nextgenbuildpro.clientengagement.data.repository.ClientPortalRepository
import com.nextgenbuildpro.clientengagement.data.repository.ProgressUpdateRepository
import com.nextgenbuildpro.clientengagement.data.repository.DigitalSignatureRepository

/**
 * Client Engagement Module for NextGenBuildPro
 * 
 * This module handles all client engagement functionality:
 * - Client portal for viewing project progress
 * - Automated progress updates
 * - Digital signature capture for approvals and contracts
 */
object ClientEngagementModule {
    private var initialized = false
    private lateinit var clientPortalRepository: ClientPortalRepository
    private lateinit var progressUpdateRepository: ProgressUpdateRepository
    private lateinit var digitalSignatureRepository: DigitalSignatureRepository

    /**
     * Check if the module is already initialized
     */
    fun isInitialized(): Boolean {
        return initialized
    }

    /**
     * Initialize the Client Engagement module
     */
    fun initialize(context: Context) {
        if (initialized) return

        // Initialize repositories
        clientPortalRepository = ClientPortalRepository(context)
        progressUpdateRepository = ProgressUpdateRepository(context)
        digitalSignatureRepository = DigitalSignatureRepository(context)

        initialized = true
    }

    /**
     * Get the client portal repository
     */
    fun getClientPortalRepository(): ClientPortalRepository {
        checkInitialized()
        return clientPortalRepository
    }

    /**
     * Get the progress update repository
     */
    fun getProgressUpdateRepository(): ProgressUpdateRepository {
        checkInitialized()
        return progressUpdateRepository
    }

    /**
     * Get the digital signature repository
     */
    fun getDigitalSignatureRepository(): DigitalSignatureRepository {
        checkInitialized()
        return digitalSignatureRepository
    }

    /**
     * Check if the module is initialized
     */
    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("Client Engagement Module is not initialized. Call initialize() first.")
        }
    }
}

/**
 * Composable function to remember Client Engagement module components
 */
@Composable
fun rememberClientEngagementComponents(): ClientEngagementComponents {
    val context = LocalContext.current

    // Initialize module if needed
    if (!ClientEngagementModule.isInitialized()) {
        ClientEngagementModule.initialize(context)
    }

    // Create repositories
    val clientPortalRepository = remember { ClientEngagementModule.getClientPortalRepository() }
    val progressUpdateRepository = remember { ClientEngagementModule.getProgressUpdateRepository() }
    val digitalSignatureRepository = remember { ClientEngagementModule.getDigitalSignatureRepository() }

    return ClientEngagementComponents(
        clientPortalRepository = clientPortalRepository,
        progressUpdateRepository = progressUpdateRepository,
        digitalSignatureRepository = digitalSignatureRepository
    )
}

/**
 * Data class to hold Client Engagement components
 */
data class ClientEngagementComponents(
    val clientPortalRepository: ClientPortalRepository,
    val progressUpdateRepository: ProgressUpdateRepository,
    val digitalSignatureRepository: DigitalSignatureRepository
)