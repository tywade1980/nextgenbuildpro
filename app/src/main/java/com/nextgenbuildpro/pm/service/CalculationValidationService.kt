package com.nextgenbuildpro.pm.service

import android.util.Log
import com.nextgenbuildpro.pm.data.model.*
import kotlin.math.abs

/**
 * Validation service for ensuring calculation accuracy
 * Provides comprehensive validation for estimates, assemblies, and calculations
 */
class CalculationValidationService {
    private val TAG = "CalculationValidationService"
    
    // Validation thresholds
    private val maxTolerancePercentage = 0.01 // 1% tolerance for floating point calculations
    private val maxReasonableLaborRate = 200.0 // $/hour
    private val maxReasonableMaterialCost = 10000.0 // $ per unit
    private val maxReasonableAssemblyCost = 100000.0 // $ per assembly
    
    /**
     * Validate complete estimate for accuracy and consistency
     */
    fun validateEstimate(estimate: TemplateEstimate): EstimateValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<ValidationWarning>()
        
        try {
            // Validate basic estimate structure
            validateEstimateStructure(estimate, issues)
            
            // Validate assemblies
            estimate.assemblies.forEach { assembly ->
                validateAssembly(assembly, issues, warnings)
            }
            
            // Validate estimate totals
            validateEstimateTotals(estimate, issues)
            
            // Cross-validate assemblies
            validateAssemblyConsistency(estimate.assemblies, issues, warnings)
            
            // Validate calculation accuracy
            validateCalculationAccuracy(estimate, issues)
            
            val severity = when {
                issues.any { it.severity == ValidationSeverity.CRITICAL } -> ValidationSeverity.CRITICAL
                issues.any { it.severity == ValidationSeverity.ERROR } -> ValidationSeverity.ERROR
                issues.any { it.severity == ValidationSeverity.WARNING } || warnings.isNotEmpty() -> ValidationSeverity.WARNING
                else -> ValidationSeverity.INFO
            }
            
            return EstimateValidationResult(
                isValid = issues.none { it.severity == ValidationSeverity.CRITICAL || it.severity == ValidationSeverity.ERROR },
                severity = severity,
                issues = issues,
                warnings = warnings,
                summary = generateValidationSummary(issues, warnings)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Validation failed: ${e.message}")
            return EstimateValidationResult(
                isValid = false,
                severity = ValidationSeverity.CRITICAL,
                issues = listOf(ValidationIssue(
                    ValidationSeverity.CRITICAL,
                    "VALIDATION_ERROR",
                    "Validation process failed: ${e.message}"
                )),
                warnings = emptyList(),
                summary = "Validation process encountered an error"
            )
        }
    }
    
    /**
     * Validate assembly calculations and structure
     */
    fun validateAssembly(assembly: TemplateAssembly): AssemblyValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<ValidationWarning>()
        
        validateAssembly(assembly, issues, warnings)
        
        return AssemblyValidationResult(
            assemblyId = assembly.id,
            assemblyName = assembly.name,
            isValid = issues.none { it.severity == ValidationSeverity.CRITICAL || it.severity == ValidationSeverity.ERROR },
            issues = issues,
            warnings = warnings
        )
    }
    
    /**
     * Validate calculation engine results
     */
    fun validateCalculationResults(
        input: CalculationInput,
        result: LineItemCalculation
    ): CalculationValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        
        try {
            // Validate calculation consistency
            validateCalculationConsistency(input, result, issues)
            
            // Validate result reasonableness
            validateResultReasonableness(result, issues)
            
            // Validate mathematical accuracy
            validateMathematicalAccuracy(input, result, issues)
            
            return CalculationValidationResult(
                isValid = issues.none { it.severity == ValidationSeverity.CRITICAL || it.severity == ValidationSeverity.ERROR },
                issues = issues,
                calculationCorrect = issues.none { it.code.startsWith("CALC_") }
            )
        } catch (e: Exception) {
            return CalculationValidationResult(
                isValid = false,
                issues = listOf(ValidationIssue(
                    ValidationSeverity.CRITICAL,
                    "CALC_VALIDATION_ERROR",
                    "Calculation validation failed: ${e.message}"
                )),
                calculationCorrect = false
            )
        }
    }
    
    /**
     * Validate pricing model application
     */
    fun validatePricingModel(
        pricingModel: PricingModel,
        quantity: Double,
        unitPrice: Double,
        laborHours: Double,
        laborRate: Double,
        materialCosts: List<MaterialCost>
    ): PricingValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        
        when (pricingModel) {
            PricingModel.FIXED_UNIT_PRICE -> {
                if (unitPrice <= 0) {
                    issues.add(ValidationIssue(
                        ValidationSeverity.ERROR,
                        "PRICING_INVALID_UNIT_PRICE",
                        "Unit price must be positive for fixed unit pricing"
                    ))
                }
                if (unitPrice > maxReasonableMaterialCost) {
                    issues.add(ValidationIssue(
                        ValidationSeverity.WARNING,
                        "PRICING_HIGH_UNIT_PRICE",
                        "Unit price seems unusually high: $${String.format("%.2f", unitPrice)}"
                    ))
                }
            }
            
            PricingModel.TIME_AND_MATERIALS -> {
                if (laborHours <= 0 && materialCosts.isEmpty()) {
                    issues.add(ValidationIssue(
                        ValidationSeverity.ERROR,
                        "PRICING_INVALID_TIME_MATERIALS",
                        "Time and materials pricing requires either labor hours or material costs"
                    ))
                }
                if (laborRate > maxReasonableLaborRate) {
                    issues.add(ValidationIssue(
                        ValidationSeverity.WARNING,
                        "PRICING_HIGH_LABOR_RATE",
                        "Labor rate seems unusually high: $${String.format("%.2f", laborRate)}/hour"
                    ))
                }
            }
            
            PricingModel.COST_PLUS -> {
                if (unitPrice <= 0) {
                    issues.add(ValidationIssue(
                        ValidationSeverity.ERROR,
                        "PRICING_INVALID_BASE_COST",
                        "Base cost must be positive for cost-plus pricing"
                    ))
                }
            }
            
            else -> {
                // Validate other pricing models
            }
        }
        
        // Validate quantity
        if (quantity <= 0) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "PRICING_INVALID_QUANTITY",
                "Quantity must be positive"
            ))
        }
        
        return PricingValidationResult(
            pricingModel = pricingModel,
            isValid = issues.none { it.severity == ValidationSeverity.CRITICAL || it.severity == ValidationSeverity.ERROR },
            issues = issues
        )
    }
    
    // Private validation methods
    
    private fun validateEstimateStructure(estimate: TemplateEstimate, issues: MutableList<ValidationIssue>) {
        if (estimate.projectId.isBlank()) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "ESTIMATE_NO_PROJECT",
                "Estimate must be associated with a project"
            ))
        }
        
        if (estimate.assemblies.isEmpty()) {
            issues.add(ValidationIssue(
                ValidationSeverity.WARNING,
                "ESTIMATE_NO_ASSEMBLIES",
                "Estimate contains no assemblies"
            ))
        }
        
        if (estimate.grandTotal < 0) {
            issues.add(ValidationIssue(
                ValidationSeverity.CRITICAL,
                "ESTIMATE_NEGATIVE_TOTAL",
                "Estimate total cannot be negative"
            ))
        }
    }
    
    private fun validateAssembly(
        assembly: TemplateAssembly,
        issues: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationWarning>
    ) {
        // Validate assembly structure
        if (assembly.name.isBlank()) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "ASSEMBLY_NO_NAME",
                "Assembly must have a name"
            ))
        }
        
        if (assembly.quantity <= 0) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "ASSEMBLY_INVALID_QUANTITY",
                "Assembly quantity must be positive: ${assembly.name}"
            ))
        }
        
        if (assembly.tasks.isEmpty()) {
            warnings.add(ValidationWarning(
                "ASSEMBLY_NO_TASKS",
                "Assembly has no tasks: ${assembly.name}"
            ))
        }
        
        // Validate assembly totals
        val calculatedLaborTotal = assembly.tasks.sumOf { it.laborCost }
        val calculatedMaterialTotal = assembly.tasks.sumOf { it.materialCost }
        val calculatedMarkupTotal = assembly.tasks.sumOf { it.markupCost }
        val calculatedTotal = calculatedLaborTotal + calculatedMaterialTotal + calculatedMarkupTotal
        
        if (abs(assembly.subtotalLabor - calculatedLaborTotal) > maxTolerancePercentage * calculatedLaborTotal) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "ASSEMBLY_LABOR_MISMATCH",
                "Labor total mismatch in assembly: ${assembly.name}. Expected: ${calculatedLaborTotal}, Got: ${assembly.subtotalLabor}"
            ))
        }
        
        if (abs(assembly.subtotalMaterial - calculatedMaterialTotal) > maxTolerancePercentage * calculatedMaterialTotal) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "ASSEMBLY_MATERIAL_MISMATCH",
                "Material total mismatch in assembly: ${assembly.name}. Expected: ${calculatedMaterialTotal}, Got: ${assembly.subtotalMaterial}"
            ))
        }
        
        if (abs(assembly.total - calculatedTotal) > maxTolerancePercentage * calculatedTotal) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "ASSEMBLY_TOTAL_MISMATCH",
                "Total mismatch in assembly: ${assembly.name}. Expected: ${calculatedTotal}, Got: ${assembly.total}"
            ))
        }
        
        // Validate reasonableness
        if (assembly.total > maxReasonableAssemblyCost) {
            warnings.add(ValidationWarning(
                "ASSEMBLY_HIGH_COST",
                "Assembly cost seems unusually high: ${assembly.name} - $${String.format("%.2f", assembly.total)}"
            ))
        }
        
        // Validate tasks
        assembly.tasks.forEach { task ->
            validateResolvedTask(task, assembly.name, issues, warnings)
        }
    }
    
    private fun validateResolvedTask(
        task: ResolvedTask,
        assemblyName: String,
        issues: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationWarning>
    ) {
        if (task.quantity <= 0) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "TASK_INVALID_QUANTITY",
                "Task quantity must be positive: ${task.task.description} in ${assemblyName}"
            ))
        }
        
        if (task.laborCost < 0 || task.materialCost < 0 || task.markupCost < 0) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "TASK_NEGATIVE_COST",
                "Task costs cannot be negative: ${task.task.description} in ${assemblyName}"
            ))
        }
        
        // Validate task calculation
        val expectedLaborCost = task.quantity * task.task.laborPerUnit
        val expectedMaterialCost = task.quantity * task.task.materialPerUnit
        (expectedLaborCost + expectedMaterialCost) * task.task.markup
        
        if (abs(task.laborCost - expectedLaborCost) > maxTolerancePercentage * expectedLaborCost) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "TASK_LABOR_CALC_ERROR",
                "Task labor calculation error: ${task.task.description} in ${assemblyName}"
            ))
        }
    }
    
    private fun validateEstimateTotals(estimate: TemplateEstimate, issues: MutableList<ValidationIssue>) {
        val calculatedLaborTotal = estimate.assemblies.sumOf { it.subtotalLabor }
        val calculatedMaterialTotal = estimate.assemblies.sumOf { it.subtotalMaterial }
        val calculatedMarkupTotal = estimate.assemblies.sumOf { it.subtotalMarkup }
        val calculatedGrandTotal = calculatedLaborTotal + calculatedMaterialTotal + calculatedMarkupTotal
        
        if (abs(estimate.subtotalLabor - calculatedLaborTotal) > maxTolerancePercentage * calculatedLaborTotal) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "ESTIMATE_LABOR_TOTAL_MISMATCH",
                "Estimate labor total mismatch. Expected: ${calculatedLaborTotal}, Got: ${estimate.subtotalLabor}"
            ))
        }
        
        if (abs(estimate.grandTotal - calculatedGrandTotal) > maxTolerancePercentage * calculatedGrandTotal) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "ESTIMATE_GRAND_TOTAL_MISMATCH",
                "Estimate grand total mismatch. Expected: ${calculatedGrandTotal}, Got: ${estimate.grandTotal}"
            ))
        }
    }
    
    private fun validateAssemblyConsistency(
        assemblies: List<TemplateAssembly>,
        issues: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationWarning>
    ) {
        // Check for duplicate assemblies
        val assemblyNames = assemblies.map { it.name }
        val duplicates = assemblyNames.groupBy { it }.filter { it.value.size > 1 }.keys
        
        duplicates.forEach { name ->
            warnings.add(ValidationWarning(
                "ASSEMBLY_DUPLICATE_NAME",
                "Duplicate assembly name found: $name"
            ))
        }
        
        // Check for inconsistent unit pricing within same trade
        val tradeGroups = assemblies.groupBy { it.category }
        tradeGroups.forEach { (trade, tradeAssemblies) ->
            validateTradeConsistency(trade, tradeAssemblies, warnings)
        }
    }
    
    private fun validateTradeConsistency(
        trade: String,
        assemblies: List<TemplateAssembly>,
        warnings: MutableList<ValidationWarning>
    ) {
        if (assemblies.size < 2) return
        
        // Check for large cost variations within same trade
        val costsPerUnit = assemblies.map { 
            if (it.quantity > 0) it.total / it.quantity else 0.0 
        }
        
        val avgCost = costsPerUnit.average()
        val highVariation = costsPerUnit.any { abs(it - avgCost) > avgCost * 0.5 }
        
        if (highVariation) {
            warnings.add(ValidationWarning(
                "TRADE_HIGH_COST_VARIATION",
                "High cost variation detected in trade: $trade"
            ))
        }
    }
    
    private fun validateCalculationAccuracy(estimate: TemplateEstimate, issues: MutableList<ValidationIssue>) {
        // Perform independent calculation verification
        val calculationEngine = CalculationEngineService()
        
        estimate.assemblies.forEach { assembly ->
            val independentCalculation = calculationEngine.calculateTemplateAssemblyTotals(assembly)
            
            if (abs(assembly.total - independentCalculation.total) > maxTolerancePercentage * independentCalculation.total) {
                issues.add(ValidationIssue(
                    ValidationSeverity.CRITICAL,
                    "CALC_ACCURACY_ERROR",
                    "Calculation accuracy error in assembly: ${assembly.name}"
                ))
            }
        }
    }
    
    private fun validateCalculationConsistency(
        input: CalculationInput,
        result: LineItemCalculation,
        issues: MutableList<ValidationIssue>
    ) {
        // Validate that output is consistent with input parameters
        val expectedBase = when (input.pricingModel) {
            PricingModel.FIXED_UNIT_PRICE -> input.quantity * input.unitPrice
            PricingModel.TIME_AND_MATERIALS -> {
                val laborCost = input.quantity * input.laborHours * input.laborRate
                val materialCost = input.materialCosts.sumOf { it.totalCost * input.quantity }
                laborCost + materialCost
            }
            else -> result.baseTotal // Accept calculated value for other models
        }
        
        if (abs(result.baseTotal - expectedBase) > maxTolerancePercentage * expectedBase) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "CALC_BASE_INCONSISTENT",
                "Base calculation inconsistent with input parameters"
            ))
        }
    }
    
    private fun validateResultReasonableness(result: LineItemCalculation, issues: MutableList<ValidationIssue>) {
        if (result.total < result.baseTotal) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "CALC_TOTAL_LESS_THAN_BASE",
                "Total cannot be less than base amount"
            ))
        }
        
        if (result.total <= 0) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "CALC_NON_POSITIVE_TOTAL",
                "Calculation result must be positive"
            ))
        }
    }
    
    private fun validateMathematicalAccuracy(
        input: CalculationInput,
        result: LineItemCalculation,
        issues: MutableList<ValidationIssue>
    ) {
        // Verify mathematical relationships
        val expectedTotal = result.baseTotal + result.overheadAmount + result.profitAmount
        
        if (abs(result.total - expectedTotal) > 0.01) {
            issues.add(ValidationIssue(
                ValidationSeverity.ERROR,
                "CALC_MATH_ERROR",
                "Mathematical calculation error detected"
            ))
        }
    }
    
    private fun generateValidationSummary(
        issues: List<ValidationIssue>,
        warnings: List<ValidationWarning>
    ): String {
        val criticalCount = issues.count { it.severity == ValidationSeverity.CRITICAL }
        val errorCount = issues.count { it.severity == ValidationSeverity.ERROR }
        val warningCount = issues.count { it.severity == ValidationSeverity.WARNING } + warnings.size
        
        return when {
            criticalCount > 0 -> "CRITICAL: $criticalCount critical issues found"
            errorCount > 0 -> "ERRORS: $errorCount errors found"
            warningCount > 0 -> "WARNINGS: $warningCount warnings found"
            else -> "VALID: No issues found"
        }
    }
}

// Data classes for validation

data class EstimateValidationResult(
    val isValid: Boolean,
    val severity: ValidationSeverity,
    val issues: List<ValidationIssue>,
    val warnings: List<ValidationWarning>,
    val summary: String
)

data class AssemblyValidationResult(
    val assemblyId: String,
    val assemblyName: String,
    val isValid: Boolean,
    val issues: List<ValidationIssue>,
    val warnings: List<ValidationWarning>
)

data class CalculationValidationResult(
    val isValid: Boolean,
    val issues: List<ValidationIssue>,
    val calculationCorrect: Boolean
)

data class PricingValidationResult(
    val pricingModel: PricingModel,
    val isValid: Boolean,
    val issues: List<ValidationIssue>
)

data class ValidationIssue(
    val severity: ValidationSeverity,
    val code: String,
    val message: String
)

data class ValidationWarning(
    val code: String,
    val message: String
)

data class CalculationInput(
    val pricingModel: PricingModel,
    val quantity: Double,
    val unitPrice: Double,
    val laborHours: Double,
    val laborRate: Double,
    val materialCosts: List<MaterialCost>
)

enum class ValidationSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}