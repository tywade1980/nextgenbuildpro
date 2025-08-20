package com.nextgenbuildpro.core

import android.content.Context
import com.nextgenbuildpro.ai.AIModule

import com.nextgenbuildpro.clientengagement.ClientEngagementModule
import com.nextgenbuildpro.crm.CrmModule
import com.nextgenbuildpro.pm.PmModule
import com.nextgenbuildpro.receptionist.ReceptionistModule
import com.nextgenbuildpro.service.ServiceModule

/**
 * ModuleManager for NextGenBuildPro
 * 
 * This class manages all modules in the application and provides a central point
 * for module initialization and access.
 */
object ModuleManager {
    private var initialized = false

    // Module references
    private lateinit var coreModule: CoreModule
    private lateinit var crmModule: CrmModule
    private lateinit var pmModule: PmModule
    private lateinit var serviceModule: ServiceModule
    // private lateinit var bmsModule: BmsModule
  //  private lateinit var fieldToolsModule: FieldToolsModule
    private lateinit var clientEngagementModule: ClientEngagementModule
    private lateinit var aiModule: AIModule
    private lateinit var receptionistModule: ReceptionistModule

    /**
     * Initialize all modules
     */
    fun initialize(context: Context) {
        if (initialized) return

        // Initialize core module first
        CoreModule.initialize()
        coreModule = CoreModule

        // Initialize service module next as other modules might depend on it
        ServiceModule.initialize(context)
        serviceModule = ServiceModule

        // Initialize other modules
        CrmModule.initialize(context)
        crmModule = CrmModule

        PmModule.initialize(context)
        pmModule = PmModule

        // Initialize BMS module
        // BmsModule.initialize(context)
        // bmsModule = BmsModule

        // Initialize Field Tools module
        //FieldToolsModule.initialize(context)
        //FIeldToolsModule = FieldToolsModule

        // Initialize Client Engagement module
        ClientEngagementModule.initialize(context)
        clientEngagementModule = ClientEngagementModule

        // Initialize AI module
        AIModule.initialize(context)
        aiModule = AIModule

        // Initialize Receptionist module
        ReceptionistModule.initialize(context)
        receptionistModule = ReceptionistModule

        initialized = true
    }

    /**
     * Get the core module
     */
    fun getCoreModule(): CoreModule {
        checkInitialized()
        return coreModule
    }

    /**
     * Get the service module
     */
    fun getServiceModule(): ServiceModule {
        checkInitialized()
        return serviceModule
    }

    /**
     * Get the CRM module
     */
    fun getCrmModule(): CrmModule {
        checkInitialized()
        return crmModule
    }

    /**
     * Get the PM module
     */
    fun getPmModule(): PmModule {
        checkInitialized()
        return pmModule
    }

    /**
     * Get the BMS module
     */
    // fun getBmsModule(): BmsModule {
    //     checkInitialized()
    //     return bmsModule
    // }

    /**
     * Get the Field Tools module
     */
    //fun getFieldToolsModule(): FieldToolsModule {
    //    checkInitialized()
    //    return fieldToolsModule
    //}

    /**
     * Get the Client Engagement module
     */
    fun getClientEngagementModule(): ClientEngagementModule {
        checkInitialized()
        return clientEngagementModule
    }

    /**
     * Get the AI module
     */
    fun getAIModule(): AIModule {
        checkInitialized()
        return aiModule
    }

    /**
     * Get the Receptionist module
     */
    fun getReceptionistModule(): ReceptionistModule {
        checkInitialized()
        return receptionistModule
    }

    /**
     * Check if the module manager is initialized
     */
    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("ModuleManager is not initialized. Call initialize() first.")
        }
    }
}
