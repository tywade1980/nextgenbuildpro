package com.nextgenbuildpro.debug

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.core.CoreModule
import com.nextgenbuildpro.core.services.*
import com.nextgenbuildpro.pm.test.FoundationCatalogueValidation
import kotlinx.coroutines.runBlocking

/**
 * Debug utility to test auto-fill and workflow automation functionality
 */
class AutomationDebugger(private val context: Context) {
    
    // Get services through methods instead of properties to ensure we always get the latest instance
    private val autoFillService get() = CoreModule.getAutoFillService()
    private val workflowEngine get() = CoreModule.getWorkflowAutomationEngine()

    companion object {
        private const val TAG = "AutomationDebugger"
    }

    /**
     * Initialize the automation debugging system
     */
    fun initialize() {
        Log.i(TAG, "Initializing automation debugging system...")
        try {
            Log.i(TAG, "Starting auto-fill service...")
            runBlocking {
                autoFillService.start()
            }

            Log.i(TAG, "Starting workflow engine...")
            runBlocking {
                workflowEngine.start()
            }

            Log.i(TAG, "Automation debugging system initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize automation debugging system", e)
        }
    }

    /**
     * Run comprehensive tests of the automation system
     */
    fun runAutomationTests() {
        Log.i(TAG, "Starting automation system tests...")
        
        runBlocking {
            try {
                // Test 1: Start services
                testServiceInitialization()
                
                // Test 2: Auto-fill functionality
                testAutoFillFunctionality()
                
                // Test 3: Data validation
                testDataValidation()
                
                // Test 4: Workflow execution
                testWorkflowExecution()
                
                // Test 5: Integration test
                testEndToEndIntegration()
                
                // Test 6: Foundation catalogue validation
                testFoundationCatalogueValidation()
                
                Log.i(TAG, "All automation tests completed successfully!")
                
            } catch (e: Exception) {
                Log.e(TAG, "Automation tests failed", e)
            }
        }
    }
    
    private suspend fun testServiceInitialization() {
        Log.d(TAG, "Testing service initialization...")
        
        // Start auto-fill service
        val autoFillResult = autoFillService.start()
        if (autoFillResult.isFailure) {
            throw Exception("Failed to start AutoFillService: ${autoFillResult.exceptionOrNull()?.message}")
        }
        
        // Start workflow engine
        val workflowResult = workflowEngine.start()
        if (workflowResult.isFailure) {
            throw Exception("Failed to start WorkflowAutomationEngine: ${workflowResult.exceptionOrNull()?.message}")
        }
        
        // Check service health
        val autoFillHealth = autoFillService.getHealthStatus()
        val workflowHealth = workflowEngine.getHealthStatus()
        
        Log.d(TAG, "AutoFill Service Health: ${autoFillHealth.isHealthy}")
        Log.d(TAG, "Workflow Engine Health: ${workflowHealth.isHealthy}")
        
        if (!autoFillHealth.isHealthy || !workflowHealth.isHealthy) {
            throw Exception("One or more services are not healthy")
        }
        
        Log.i(TAG, "✅ Service initialization test passed")
    }
    
    private suspend fun testAutoFillFunctionality() {
        Log.d(TAG, "Testing auto-fill functionality...")
        
        val context = AutoFillContext(
            contextId = "test_context",
            formType = "test_form",
            userId = "test_user"
        )
        
        // Test different field types
        val testCases = mapOf(
            FormFieldType.NAME to "Expected: Client names from CRM",
            FormFieldType.PHONE to "Expected: Phone numbers from various sources",
            FormFieldType.EMAIL to "Expected: Email addresses from contacts",
            FormFieldType.PROJECT_TYPE to "Expected: Common project types",
            FormFieldType.ADDRESS to "Expected: Recent addresses"
        )
        
        for ((fieldType, description) in testCases) {
            Log.d(TAG, "Testing $fieldType - $description")
            
            val suggestions = autoFillService.getAutoFillSuggestions(fieldType, context)
            Log.d(TAG, "Got ${suggestions.size} suggestions for $fieldType")
            
            suggestions.forEach { suggestion ->
                Log.d(TAG, "  - ${suggestion.displayText} (confidence: ${suggestion.confidence}, source: ${suggestion.source})")
            }
        }
        
        Log.i(TAG, "✅ Auto-fill functionality test passed")
    }
    
    private suspend fun testDataValidation() {
        Log.d(TAG, "Testing data validation...")
        
        val testCases = mapOf(
            FormFieldType.EMAIL to listOf(
                "valid@email.com" to true,
                "invalid-email" to false,
                "test@domain.co.uk" to true,
                "@invalid.com" to false
            ),
            FormFieldType.PHONE to listOf(
                "(555) 123-4567" to true,
                "555-123-4567" to true,
                "123" to false,
                "abc-def-ghij" to false
            ),
            FormFieldType.NAME to listOf(
                "John Doe" to true,
                "Mary Jane Smith" to true,
                "123 Invalid" to false,
                "A" to false
            )
        )
        
        for ((fieldType, cases) in testCases) {
            Log.d(TAG, "Testing validation for $fieldType")
            
            for ((value, expectedValid) in cases) {
                val result = autoFillService.validateFieldValue(fieldType, value)
                if (result.isValid != expectedValid) {
                    throw Exception("Validation failed for $fieldType with value '$value'. Expected: $expectedValid, Got: ${result.isValid}")
                }
                Log.d(TAG, "  ✓ '$value' -> ${if (result.isValid) "VALID" else "INVALID"}")
            }
        }
        
        Log.i(TAG, "✅ Data validation test passed")
    }
    
    private suspend fun testWorkflowExecution() {
        Log.d(TAG, "Testing workflow execution...")
        
        val templates = workflowEngine.getWorkflowTemplates()
        Log.d(TAG, "Found ${templates.size} workflow templates")
        
        if (templates.isNotEmpty()) {
            val testTemplate = templates.first()
            Log.d(TAG, "Testing workflow: ${testTemplate.name}")
            
            val triggerData = mapOf(
                "client_name" to "Test Client",
                "project_type" to "Test Project",
                "budget" to 50000.0,
                "test_mode" to true
            )
            
            val context = AutoFillContext(
                contextId = "workflow_test",
                formType = "workflow_execution",
                userId = "test_user",
                metadata = triggerData
            )
            
            val result = workflowEngine.executeAutomatedWorkflow(
                templateId = testTemplate.id,
                triggerData = triggerData,
                autoFillContext = context
            )
            
            if (result.isFailure) {
                throw Exception("Workflow execution failed: ${result.exceptionOrNull()?.message}")
            }
            
            val execution = result.getOrThrow()
            Log.d(TAG, "Workflow execution completed with status: ${execution.status}")
            Log.d(TAG, "Executed ${execution.executedSteps.size} steps")
            
            execution.executedSteps.forEach { step ->
                Log.d(TAG, "  - ${step.stepName}: ${if (step.success) "SUCCESS" else "FAILED"}")
            }
        }
        
        Log.i(TAG, "✅ Workflow execution test passed")
    }
    
    private suspend fun testEndToEndIntegration() {
        Log.d(TAG, "Testing end-to-end integration...")
        
        // Simulate a complete lead-to-project workflow
        val leadData = mapOf(
            "name" to "Integration Test Client",
            "phone" to "(555) 999-8888",
            "email" to "integration@test.com",
            "project_type" to "Kitchen Renovation",
            "budget" to 75000.0
        )
        
        // Step 1: Auto-fill a form with lead data
        val formSchema = FormSchema(
            id = "integration_test_form",
            name = "Integration Test Form",
            fields = listOf(
                FormFieldSchema("client_name", FormFieldType.CLIENT_NAME, true),
                FormFieldSchema("client_phone", FormFieldType.CLIENT_PHONE, true),
                FormFieldSchema("client_email", FormFieldType.CLIENT_EMAIL, true),
                FormFieldSchema("project_type", FormFieldType.PROJECT_TYPE, true)
            )
        )
        
        val autoFillContext = AutoFillContext(
            contextId = "integration_test",
            formType = "lead_form",
            userId = "test_user",
            metadata = leadData
        )
        
        val autoFillResult = autoFillService.autoFillForm(formSchema, autoFillContext)
        Log.d(TAG, "Auto-fill result confidence: ${autoFillResult.confidence}")
        Log.d(TAG, "Filled fields: ${autoFillResult.filledFields.keys}")
        
        // Step 2: Execute lead-to-project workflow
        val workflowTemplates = workflowEngine.getWorkflowTemplates()
        val leadToProjectTemplate = workflowTemplates.find { it.id == "lead_to_project" }
        
        if (leadToProjectTemplate != null) {
            Log.d(TAG, "Executing lead-to-project workflow...")
            
            val workflowResult = workflowEngine.executeAutomatedWorkflow(
                templateId = leadToProjectTemplate.id,
                triggerData = leadData,
                autoFillContext = autoFillContext
            )
            
            if (workflowResult.isSuccess) {
                val execution = workflowResult.getOrThrow()
                Log.d(TAG, "End-to-end workflow completed: ${execution.status}")
                Log.d(TAG, "Total steps executed: ${execution.executedSteps.size}")
            } else {
                Log.w(TAG, "Workflow execution failed: ${workflowResult.exceptionOrNull()?.message}")
            }
        } else {
            Log.w(TAG, "Lead-to-project workflow template not found")
        }
        
        Log.i(TAG, "✅ End-to-end integration test completed")
    }
    
    /**
     * Generate a comprehensive test report
     */
    fun generateTestReport(): String {
        return buildString {
            appendLine("=== NextGen Build Pro Automation Test Report ===")
            appendLine()
            
            try {
                // Service status
                val autoFillHealth = runBlocking { autoFillService.getHealthStatus() }
                val workflowHealth = runBlocking { workflowEngine.getHealthStatus() }
                
                appendLine("Service Status:")
                appendLine("  AutoFill Service: ${if (autoFillHealth.isHealthy) "✅ HEALTHY" else "❌ UNHEALTHY"}")
                appendLine("  Workflow Engine: ${if (workflowHealth.isHealthy) "✅ HEALTHY" else "❌ UNHEALTHY"}")
                appendLine()
                
                // Auto-fill metrics
                appendLine("Auto-Fill Metrics:")
                autoFillHealth.metrics.forEach { (key, value) ->
                    appendLine("  $key: $value")
                }
                appendLine()
                
                // Workflow metrics
                appendLine("Workflow Metrics:")
                workflowHealth.metrics.forEach { (key, value) ->
                    appendLine("  $key: $value")
                }
                appendLine()
                
                // Available workflows
                val templates = workflowEngine.getWorkflowTemplates()
                appendLine("Available Workflows: ${templates.size}")
                templates.forEach { template ->
                    appendLine("  - ${template.name} (${template.steps.size} steps, ~${template.estimatedDuration}min)")
                }
                
            } catch (e: Exception) {
                appendLine("Error generating report: ${e.message}")
            }
        }
    }

=======
    
    private suspend fun testFoundationCatalogueValidation() {
        Log.d(TAG, "Testing Foundation & Basement catalogue enhancements...")
        
        try {
            val foundationValidator = FoundationCatalogueValidation(context)
            foundationValidator.validateFoundationEnhancements()
            Log.d(TAG, "✅ Foundation catalogue validation passed")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Foundation catalogue validation failed: ${e.message}", e)
            throw e
        }
    }
    
    companion object {
        private const val TAG = "AutomationDebugger"
    }
}