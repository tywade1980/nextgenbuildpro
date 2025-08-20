package com.nextgenbuildpro.pm.data.repository

import android.content.Context
import com.nextgenbuildpro.pm.data.model.Building
import com.nextgenbuildpro.pm.data.model.FloorPlan
import com.nextgenbuildpro.pm.data.model.StructuralComponent
import com.nextgenbuildpro.core.Repository

/**
 * Repository for managing building information
 */
class BuildingRepository(private val context: Context) : Repository<Building> {
    
    // In-memory storage for demo purposes
    private val buildings = mutableListOf<Building>()
    private val floorPlans = mutableListOf<FloorPlan>()
    private val structuralComponents = mutableListOf<StructuralComponent>()
    
    /**
     * Get all buildings
     */
    override suspend fun getAll(): List<Building> {
        return buildings
    }
    
    /**
     * Get a building by ID
     */
    override suspend fun getById(id: String): Building? {
        return buildings.find { it.id == id }
    }
    
    /**
     * Save a new building
     */
    override suspend fun save(item: Building): Boolean {
        return try {
            buildings.add(item)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Update an existing building
     */
    override suspend fun update(item: Building): Boolean {
        return try {
            val index = buildings.indexOfFirst { it.id == item.id }
            if (index != -1) {
                buildings[index] = item
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Delete a building by ID
     */
    override suspend fun delete(id: String): Boolean {
        return try {
            val removed = buildings.removeIf { it.id == id }
            // Also remove related floor plans and structural components
            if (removed) {
                floorPlans.removeIf { it.buildingId == id }
                structuralComponents.removeIf { it.buildingId == id }
            }
            removed
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get all floor plans for a building
     */
    suspend fun getFloorPlans(buildingId: String): List<FloorPlan> {
        return floorPlans.filter { it.buildingId == buildingId }
    }
    
    /**
     * Get a floor plan by ID
     */
    suspend fun getFloorPlanById(id: String): FloorPlan? {
        return floorPlans.find { it.id == id }
    }
    
    /**
     * Save a new floor plan
     */
    suspend fun saveFloorPlan(floorPlan: FloorPlan): Boolean {
        return try {
            floorPlans.add(floorPlan)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Update an existing floor plan
     */
    suspend fun updateFloorPlan(floorPlan: FloorPlan): Boolean {
        return try {
            val index = floorPlans.indexOfFirst { it.id == floorPlan.id }
            if (index != -1) {
                floorPlans[index] = floorPlan
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Delete a floor plan by ID
     */
    suspend fun deleteFloorPlan(id: String): Boolean {
        return try {
            val removed = floorPlans.removeIf { it.id == id }
            // Also remove related structural components
            if (removed) {
                structuralComponents.removeIf { it.floorPlanId == id }
            }
            removed
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get all structural components for a building
     */
    suspend fun getStructuralComponents(buildingId: String): List<StructuralComponent> {
        return structuralComponents.filter { it.buildingId == buildingId }
    }
    
    /**
     * Get structural components for a specific floor plan
     */
    suspend fun getStructuralComponentsByFloorPlan(floorPlanId: String): List<StructuralComponent> {
        return structuralComponents.filter { it.floorPlanId == floorPlanId }
    }
    
    /**
     * Get a structural component by ID
     */
    suspend fun getStructuralComponentById(id: String): StructuralComponent? {
        return structuralComponents.find { it.id == id }
    }
    
    /**
     * Save a new structural component
     */
    suspend fun saveStructuralComponent(component: StructuralComponent): Boolean {
        return try {
            structuralComponents.add(component)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Update an existing structural component
     */
    suspend fun updateStructuralComponent(component: StructuralComponent): Boolean {
        return try {
            val index = structuralComponents.indexOfFirst { it.id == component.id }
            if (index != -1) {
                structuralComponents[index] = component
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Delete a structural component by ID
     */
    suspend fun deleteStructuralComponent(id: String): Boolean {
        return try {
            structuralComponents.removeIf { it.id == id }
        } catch (e: Exception) {
            false
        }
    }
}