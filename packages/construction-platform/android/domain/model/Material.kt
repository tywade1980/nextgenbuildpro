package com.constructionmanager.domain.model

import java.math.BigDecimal

data class Material(
    val id: String,
    val name: String,
    val category: MaterialCategory,
    val subcategory: String,
    val unitOfMeasurement: String,
    val currentPrice: BigDecimal,
    val supplier: String,
    val supplierSku: String? = null,
    val description: String,
    val specifications: Map<String, String> = emptyMap(),
    val isActive: Boolean = true,
    val lastPriceUpdate: kotlinx.datetime.LocalDateTime,
    val regionalPricing: Map<String, BigDecimal> = emptyMap() // Region to price mapping
)

enum class MaterialCategory {
    LUMBER,
    CONCRETE,
    STEEL,
    ROOFING,
    INSULATION,
    DRYWALL,
    FLOORING,
    PLUMBING,
    ELECTRICAL,
    HVAC,
    WINDOWS,
    DOORS,
    HARDWARE,
    FIXTURES,
    FINISHES,
    TOOLS,
    SAFETY_EQUIPMENT,
    LANDSCAPING,
    OTHER
}

data class MaterialOrder(
    val id: String,
    val projectId: String,
    val materialId: String,
    val material: Material,
    val quantityOrdered: Double,
    val quantityReceived: Double = 0.0,
    val unitPrice: BigDecimal,
    val totalCost: BigDecimal,
    val supplier: String,
    val orderDate: kotlinx.datetime.LocalDate,
    val expectedDeliveryDate: kotlinx.datetime.LocalDate,
    val actualDeliveryDate: kotlinx.datetime.LocalDate? = null,
    val status: OrderStatus,
    val notes: String = ""
)

enum class OrderStatus {
    PENDING,
    ORDERED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    BACKORDERED
}