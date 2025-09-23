package com.nextgenbuildpro.core

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.core.services.AutoFillService
import com.nextgenbuildpro.core.services.WorkflowAutomationEngine

/**
 * Core module for NextGenBuildPro
 * 
 * This module contains shared functionality used by both CRM and PM modules.
 * It includes:
 * - Common data models
 * - Utility functions
 * - Shared UI components
 * - Base classes for repositories and view models
 * - Auto-fill and workflow automation services
 */
object CoreModule {
    private var initialized = false
    private lateinit var context: Context
    private lateinit var autoFillService: AutoFillService
    private lateinit var workflowAutomationEngine: WorkflowAutomationEngine
    
    /**
     * Initialize the core module with automation services
     */
    fun initialize(appContext: Context? = null) {
        if (initialized && appContext == null) return
        
        appContext?.let { 
            context = it
            
            // Initialize automation services
            autoFillService = AutoFillService(context)
            workflowAutomationEngine = WorkflowAutomationEngine(context)
            
            Log.d("CoreModule", "Core module initialized with automation services")
        }
        
        initialized = true
    }
    
    /**
     * Get the auto-fill service instance
     */
    fun getAutoFillService(): AutoFillService {
        checkInitialized()
        return autoFillService
    }
    
    /**
     * Get the workflow automation engine instance
     */
    fun getWorkflowAutomationEngine(): WorkflowAutomationEngine {
        checkInitialized()
        return workflowAutomationEngine
    }
    
    /**
     * Check if the module is initialized
     */
    fun isInitialized(): Boolean {
        return initialized
    }
    
    private fun checkInitialized() {
        if (!initialized || !::context.isInitialized) {
            throw IllegalStateException("CoreModule is not initialized. Call initialize(context) first.")
        }
    }
}

// Common interfaces
interface Repository<T> {
    suspend fun getAll(): List<T>
    suspend fun getById(id: String): T?
    suspend fun save(item: T): Boolean
    suspend fun update(item: T): Boolean
    suspend fun delete(id: String): Boolean
}

interface ViewModel<T> {
    val items: androidx.compose.runtime.State<List<T>>
    fun refresh()
}

// Common utility functions
object StringUtils {
    fun formatCurrency(amount: Double): String {
        return java.text.NumberFormat.getCurrencyInstance().format(amount)
    }

    fun formatPhoneNumber(phone: String): String {
        // Simple formatting for US phone numbers
        if (phone.length == 10) {
            return "(${phone.substring(0, 3)}) ${phone.substring(3, 6)}-${phone.substring(6)}"
        }
        return phone
    }
}

// Common data models
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String
)

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String = "USA"
)

data class Note(
    val id: String,
    val content: String,
    val createdAt: String,
    val createdBy: String,
    val relatedItemId: String,
    val relatedItemType: String
)

data class Attachment(
    val id: String,
    val name: String,
    val type: String,
    val url: String,
    val size: Long,
    val uploadedAt: String,
    val uploadedBy: String,
    val relatedItemId: String,
    val relatedItemType: String
)

// Base AI Assistant interface
interface AIAssistant {
    fun processCommand(command: String): String
    fun generateSuggestions(context: String): List<String>
    fun learnFromInteraction(interaction: String, outcome: String)
}
