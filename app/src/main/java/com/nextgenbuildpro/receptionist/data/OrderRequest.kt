package com.nextgenbuildpro.receptionist.data

import java.util.Date

/**
 * Data class representing an order request made through the AI receptionist
 */
data class OrderRequest(
    /**
     * Unique identifier for the order
     */
    val id: String,
    
    /**
     * List of items to order
     */
    val items: List<String>,
    
    /**
     * Supplier to order from
     */
    val supplier: String,
    
    /**
     * Urgency of the order (standard, expedited, rush)
     */
    val urgency: String,
    
    /**
     * Timestamp when the order was placed
     */
    val timestamp: Date,
    
    /**
     * Current status of the order
     */
    val status: String,
    
    /**
     * ID of the project associated with this order (optional)
     */
    val projectId: String? = null,
    
    /**
     * Additional notes for the order
     */
    val notes: String? = null,
    
    /**
     * Estimated delivery date (optional)
     */
    val estimatedDelivery: Date? = null,
    
    /**
     * Total cost of the order (optional)
     */
    val totalCost: Double? = null
)

/**
 * Enum representing the status of an order
 */
enum class OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    ON_HOLD
}

/**
 * Enum representing the urgency of an order
 */
enum class OrderUrgency {
    STANDARD,
    EXPEDITED,
    RUSH
}