package com.nextgenbuildpro.receptionist.repository

import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.receptionist.data.OrderRequest
import com.nextgenbuildpro.receptionist.data.OrderStatus
import com.nextgenbuildpro.receptionist.data.OrderUrgency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.UUID

/**
 * Repository for managing order requests made through the AI receptionist
 */
class OrderRepository : Repository<OrderRequest> {
    
    // In-memory storage for orders
    private val _orders = MutableStateFlow<List<OrderRequest>>(emptyList())
    val orders: StateFlow<List<OrderRequest>> = _orders.asStateFlow()
    
    /**
     * Get all orders
     */
    override suspend fun getAll(): List<OrderRequest> {
        return _orders.value
    }
    
    /**
     * Get an order by ID
     */
    override suspend fun getById(id: String): OrderRequest? {
        return _orders.value.find { it.id == id }
    }
    
    /**
     * Save a new order
     */
    override suspend fun save(item: OrderRequest): Boolean {
        try {
            _orders.value = _orders.value + item
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Update an existing order
     */
    override suspend fun update(item: OrderRequest): Boolean {
        try {
            _orders.value = _orders.value.map { 
                if (it.id == item.id) item else it 
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Delete an order by ID
     */
    override suspend fun delete(id: String): Boolean {
        try {
            _orders.value = _orders.value.filter { it.id != id }
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Save an order (non-suspend version for use from AIReceptionist)
     */
    fun saveOrder(order: OrderRequest): Boolean {
        try {
            _orders.value = _orders.value + order
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Update order status
     */
    suspend fun updateOrderStatus(id: String, status: String): Boolean {
        try {
            val order = getById(id) ?: return false
            val updatedOrder = order.copy(status = status)
            return update(updatedOrder)
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Get orders for a specific project
     */
    suspend fun getOrdersForProject(projectId: String): List<OrderRequest> {
        return _orders.value.filter { it.projectId == projectId }
    }
    
    /**
     * Get orders from a specific supplier
     */
    suspend fun getOrdersFromSupplier(supplier: String): List<OrderRequest> {
        return _orders.value.filter { 
            it.supplier.contains(supplier, ignoreCase = true)
        }
    }
    
    /**
     * Get orders by status
     */
    suspend fun getOrdersByStatus(status: String): List<OrderRequest> {
        return _orders.value.filter { it.status == status }
    }
    
    /**
     * Get orders by urgency
     */
    suspend fun getOrdersByUrgency(urgency: String): List<OrderRequest> {
        return _orders.value.filter { it.urgency == urgency }
    }
    
    /**
     * Get orders containing specific items
     */
    suspend fun getOrdersContainingItem(item: String): List<OrderRequest> {
        return _orders.value.filter { orderRequest ->
            orderRequest.items.any { it.contains(item, ignoreCase = true) }
        }
    }
    
    /**
     * Get orders from a specific date range
     */
    suspend fun getOrdersInDateRange(startDate: Date, endDate: Date): List<OrderRequest> {
        return _orders.value.filter { 
            it.timestamp.after(startDate) && it.timestamp.before(endDate)
        }
    }
    
    /**
     * Get recent orders
     */
    suspend fun getRecentOrders(limit: Int = 10): List<OrderRequest> {
        return _orders.value
            .sortedByDescending { it.timestamp }
            .take(limit)
    }
    
    /**
     * Get pending orders
     */
    suspend fun getPendingOrders(): List<OrderRequest> {
        return _orders.value.filter { it.status == "Pending" }
    }
    
    /**
     * Get rush orders
     */
    suspend fun getRushOrders(): List<OrderRequest> {
        return _orders.value.filter { it.urgency == "RUSH" }
    }
}