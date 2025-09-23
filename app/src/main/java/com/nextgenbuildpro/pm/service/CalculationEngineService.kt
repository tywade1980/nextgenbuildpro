package com.nextgenbuildpro.pm.service

import android.util.Log
import com.nextgenbuildpro.pm.data.model.*
import kotlin.math.roundToInt

/**
 * Calculation Engine for estimates
 * Handles line item calculations, aggregations, tax and markup calculations
 */
class CalculationEngineService {
    private val TAG = "CalculationEngineService"

    /**
     * Calculate line item totals for various pricing models
     */
    fun calculateLineItemTotals(
        quantity: Double,
        unitPrice: Double,
        pricingModel: PricingModel = PricingModel.FIXED_UNIT_PRICE,
        laborHours: Double = 0.0,
        laborRate: Double = 0.0,
        materialCosts: List<MaterialCost> = emptyList(),
        overhead: Double = 0.0,
        profit: Double = 0.0
    ): LineItemCalculation {
        try {
            val baseTotal = when (pricingModel) {
                PricingModel.FIXED_UNIT_PRICE -> quantity * unitPrice
                PricingModel.TIME_AND_MATERIALS -> calculateTimeAndMaterials(quantity, laborHours, laborRate, materialCosts)
                PricingModel.COST_PLUS -> calculateCostPlus(quantity, unitPrice, overhead, profit)
                PricingModel.SQUARE_FOOTAGE -> calculateSquareFootage(quantity, unitPrice)
                PricingModel.LINEAR_FOOTAGE -> calculateLinearFootage(quantity, unitPrice)
                PricingModel.ASSEMBLY_BASED -> calculateAssemblyBased(quantity, unitPrice, laborHours, laborRate, materialCosts)
            }

            val overheadAmount = baseTotal * (overhead / 100.0)
            val subtotalWithOverhead = baseTotal + overheadAmount
            val profitAmount = subtotalWithOverhead * (profit / 100.0)
            val totalWithProfit = subtotalWithOverhead + profitAmount

            return LineItemCalculation(
                baseTotal = baseTotal,
                overheadAmount = overheadAmount,
                profitAmount = profitAmount,
                subtotal = subtotalWithOverhead,
                total = totalWithProfit,
                laborCost = if (pricingModel == PricingModel.TIME_AND_MATERIALS || pricingModel == PricingModel.ASSEMBLY_BASED) 
                    laborHours * laborRate * quantity else 0.0,
                materialCost = materialCosts.sumOf { it.totalCost * quantity },
                pricingModel = pricingModel
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating line item totals: ${e.message}")
            return LineItemCalculation()
        }
    }

    /**
     * Calculate section totals (aggregation of line items)
     */
    fun calculateSectionTotals(lineItems: List<LineItemCalculation>): SectionCalculation {
        try {
            val totalLabor = lineItems.sumOf { it.laborCost }
            val totalMaterial = lineItems.sumOf { it.materialCost }
            val totalOverhead = lineItems.sumOf { it.overheadAmount }
            val totalProfit = lineItems.sumOf { it.profitAmount }
            val subtotal = lineItems.sumOf { it.subtotal }
            val total = lineItems.sumOf { it.total }

            return SectionCalculation(
                laborTotal = totalLabor,
                materialTotal = totalMaterial,
                overheadTotal = totalOverhead,
                profitTotal = totalProfit,
                subtotal = subtotal,
                total = total,
                itemCount = lineItems.size
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating section totals: ${e.message}")
            return SectionCalculation()
        }
    }

    /**
     * Calculate estimate totals with tax and markup
     */
    fun calculateEstimateTotals(
        sections: List<SectionCalculation>,
        taxSettings: TaxSettings,
        markupSettings: MarkupSettings,
        discountSettings: DiscountSettings? = null
    ): EstimateCalculation {
        try {
            val subtotal = sections.sumOf { it.total }
            
            // Apply discount if any
            val discountAmount = discountSettings?.let { 
                when (it.type) {
                    DiscountType.PERCENTAGE -> subtotal * (it.value / 100.0)
                    DiscountType.FIXED_AMOUNT -> it.value
                }
            } ?: 0.0
            
            val subtotalAfterDiscount = subtotal - discountAmount

            // Calculate markup
            val markupAmount = when (markupSettings.type) {
                MarkupType.PERCENTAGE -> subtotalAfterDiscount * (markupSettings.value / 100.0)
                MarkupType.FIXED_AMOUNT -> markupSettings.value
            }

            val subtotalWithMarkup = subtotalAfterDiscount + markupAmount

            // Calculate tax
            val taxAmount = when (taxSettings.type) {
                TaxType.PERCENTAGE -> subtotalWithMarkup * (taxSettings.rate / 100.0)
                TaxType.FIXED_AMOUNT -> taxSettings.amount
                TaxType.NO_TAX -> 0.0
            }

            val grandTotal = subtotalWithMarkup + taxAmount

            return EstimateCalculation(
                laborTotal = sections.sumOf { it.laborTotal },
                materialTotal = sections.sumOf { it.materialTotal },
                overheadTotal = sections.sumOf { it.overheadTotal },
                profitTotal = sections.sumOf { it.profitTotal },
                subtotal = subtotal,
                discountAmount = discountAmount,
                subtotalAfterDiscount = subtotalAfterDiscount,
                markupAmount = markupAmount,
                subtotalWithMarkup = subtotalWithMarkup,
                taxAmount = taxAmount,
                grandTotal = grandTotal,
                sectionCount = sections.size
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating estimate totals: ${e.message}")
            return EstimateCalculation()
        }
    }

    /**
     * Calculate template assembly totals
     */
    fun calculateTemplateAssemblyTotals(assembly: TemplateAssembly): TemplateAssemblyCalculation {
        try {
            val laborTotal = assembly.tasks.sumOf { it.laborCost }
            val materialTotal = assembly.tasks.sumOf { it.materialCost }
            val markupTotal = assembly.tasks.sumOf { it.markupCost }
            val total = laborTotal + materialTotal + markupTotal

            return TemplateAssemblyCalculation(
                assemblyId = assembly.id,
                assemblyName = assembly.name,
                quantity = assembly.quantity,
                laborTotal = laborTotal,
                materialTotal = materialTotal,
                markupTotal = markupTotal,
                total = total,
                unitCost = if (assembly.quantity > 0) total / assembly.quantity else 0.0,
                taskCount = assembly.tasks.size
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating template assembly totals: ${e.message}")
            return TemplateAssemblyCalculation()
        }
    }

    /**
     * Apply tax calculations
     */
    fun applyTaxCalculations(
        subtotal: Double,
        taxSettings: TaxSettings
    ): TaxCalculation {
        try {
            val taxAmount = when (taxSettings.type) {
                TaxType.PERCENTAGE -> subtotal * (taxSettings.rate / 100.0)
                TaxType.FIXED_AMOUNT -> taxSettings.amount
                TaxType.NO_TAX -> 0.0
            }

            val totalWithTax = subtotal + taxAmount

            return TaxCalculation(
                subtotal = subtotal,
                taxRate = if (taxSettings.type == TaxType.PERCENTAGE) taxSettings.rate else 0.0,
                taxAmount = taxAmount,
                totalWithTax = totalWithTax,
                taxType = taxSettings.type
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error applying tax calculations: ${e.message}")
            return TaxCalculation()
        }
    }

    /**
     * Apply markup calculations
     */
    fun applyMarkupCalculations(
        subtotal: Double,
        markupSettings: MarkupSettings
    ): MarkupCalculation {
        try {
            val markupAmount = when (markupSettings.type) {
                MarkupType.PERCENTAGE -> subtotal * (markupSettings.value / 100.0)
                MarkupType.FIXED_AMOUNT -> markupSettings.value
            }

            val totalWithMarkup = subtotal + markupAmount

            return MarkupCalculation(
                subtotal = subtotal,
                markupPercentage = if (markupSettings.type == MarkupType.PERCENTAGE) markupSettings.value else 0.0,
                markupAmount = markupAmount,
                totalWithMarkup = totalWithMarkup,
                markupType = markupSettings.type
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error applying markup calculations: ${e.message}")
            return MarkupCalculation()
        }
    }

    // Private calculation methods for different pricing models

    private fun calculateTimeAndMaterials(
        quantity: Double,
        laborHours: Double,
        laborRate: Double,
        materialCosts: List<MaterialCost>
    ): Double {
        val laborCost = quantity * laborHours * laborRate
        val materialCost = materialCosts.sumOf { it.totalCost * quantity }
        return laborCost + materialCost
    }

    private fun calculateCostPlus(
        quantity: Double,
        unitPrice: Double,
        overhead: Double,
        profit: Double
    ): Double {
        val baseCost = quantity * unitPrice
        val overheadAmount = baseCost * (overhead / 100.0)
        val profitAmount = (baseCost + overheadAmount) * (profit / 100.0)
        return baseCost + overheadAmount + profitAmount
    }

    private fun calculateSquareFootage(quantity: Double, pricePerSqFt: Double): Double {
        return quantity * pricePerSqFt
    }

    private fun calculateLinearFootage(quantity: Double, pricePerLinearFt: Double): Double {
        return quantity * pricePerLinearFt
    }

    private fun calculateAssemblyBased(
        quantity: Double,
        unitPrice: Double,
        laborHours: Double,
        laborRate: Double,
        materialCosts: List<MaterialCost>
    ): Double {
        val baseCost = quantity * unitPrice
        val laborCost = quantity * laborHours * laborRate
        val materialCost = materialCosts.sumOf { it.totalCost * quantity }
        return baseCost + laborCost + materialCost
    }

    /**
     * Round currency values to 2 decimal places
     */
    fun roundCurrency(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }

    /**
     * Format currency for display
     */
    fun formatCurrency(value: Double): String {
        return "$${String.format("%.2f", value)}"
    }
}

// Enums and data classes for calculations

enum class PricingModel {
    FIXED_UNIT_PRICE,
    TIME_AND_MATERIALS,
    COST_PLUS,
    SQUARE_FOOTAGE,
    LINEAR_FOOTAGE,
    ASSEMBLY_BASED
}

enum class TaxType {
    PERCENTAGE,
    FIXED_AMOUNT,
    NO_TAX
}

enum class MarkupType {
    PERCENTAGE,
    FIXED_AMOUNT
}

enum class DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT
}

data class MaterialCost(
    val name: String,
    val unitCost: Double,
    val quantity: Double,
    val totalCost: Double = unitCost * quantity
)

data class TaxSettings(
    val type: TaxType,
    val rate: Double = 0.0,
    val amount: Double = 0.0,
    val description: String = ""
)

data class MarkupSettings(
    val type: MarkupType,
    val value: Double,
    val description: String = ""
)

data class DiscountSettings(
    val type: DiscountType,
    val value: Double,
    val description: String = ""
)

data class LineItemCalculation(
    val baseTotal: Double = 0.0,
    val overheadAmount: Double = 0.0,
    val profitAmount: Double = 0.0,
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val laborCost: Double = 0.0,
    val materialCost: Double = 0.0,
    val pricingModel: PricingModel = PricingModel.FIXED_UNIT_PRICE
)

data class SectionCalculation(
    val laborTotal: Double = 0.0,
    val materialTotal: Double = 0.0,
    val overheadTotal: Double = 0.0,
    val profitTotal: Double = 0.0,
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val itemCount: Int = 0
)

data class EstimateCalculation(
    val laborTotal: Double = 0.0,
    val materialTotal: Double = 0.0,
    val overheadTotal: Double = 0.0,
    val profitTotal: Double = 0.0,
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val subtotalAfterDiscount: Double = 0.0,
    val markupAmount: Double = 0.0,
    val subtotalWithMarkup: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val sectionCount: Int = 0
)

data class TemplateAssemblyCalculation(
    val assemblyId: String = "",
    val assemblyName: String = "",
    val quantity: Double = 0.0,
    val laborTotal: Double = 0.0,
    val materialTotal: Double = 0.0,
    val markupTotal: Double = 0.0,
    val total: Double = 0.0,
    val unitCost: Double = 0.0,
    val taskCount: Int = 0
)

data class TaxCalculation(
    val subtotal: Double = 0.0,
    val taxRate: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalWithTax: Double = 0.0,
    val taxType: TaxType = TaxType.NO_TAX
)

data class MarkupCalculation(
    val subtotal: Double = 0.0,
    val markupPercentage: Double = 0.0,
    val markupAmount: Double = 0.0,
    val totalWithMarkup: Double = 0.0,
    val markupType: MarkupType = MarkupType.PERCENTAGE
)