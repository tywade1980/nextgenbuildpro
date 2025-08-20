package com.nextgenbuildpro.clientengagement.data.repository

import android.content.Context
import com.nextgenbuildpro.clientengagement.data.model.ClientPortal
import com.nextgenbuildpro.clientengagement.data.model.ClientPortalActivity
import com.nextgenbuildpro.clientengagement.data.model.ClientPortalActivityType
import com.nextgenbuildpro.clientengagement.data.model.ClientPortalSettings
import com.nextgenbuildpro.core.Repository
import java.util.UUID

/**
 * Repository for managing client portals
 */
class ClientPortalRepository(private val context: Context) : Repository<ClientPortal> {
    
    // In-memory storage for demo purposes
    private val portals = mutableListOf<ClientPortal>()
    private val portalSettings = mutableListOf<ClientPortalSettings>()
    private val portalActivities = mutableListOf<ClientPortalActivity>()
    
    /**
     * Get all client portals
     */
    override suspend fun getAll(): List<ClientPortal> {
        return portals
    }
    
    /**
     * Get a client portal by ID
     */
    override suspend fun getById(id: String): ClientPortal? {
        return portals.find { it.id == id }
    }
    
    /**
     * Save a new client portal
     */
    override suspend fun save(item: ClientPortal): Boolean {
        return try {
            portals.add(item)
            
            // Create default settings for the portal
            val settings = ClientPortalSettings(
                portalId = item.id
            )
            portalSettings.add(settings)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Update an existing client portal
     */
    override suspend fun update(item: ClientPortal): Boolean {
        return try {
            val index = portals.indexOfFirst { it.id == item.id }
            if (index != -1) {
                portals[index] = item
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Delete a client portal by ID
     */
    override suspend fun delete(id: String): Boolean {
        return try {
            val removed = portals.removeIf { it.id == id }
            
            // Also remove related settings and activities
            if (removed) {
                portalSettings.removeIf { it.portalId == id }
                portalActivities.removeIf { it.portalId == id }
            }
            
            removed
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get client portal by client ID
     */
    suspend fun getPortalByClientId(clientId: String): ClientPortal? {
        return portals.find { it.clientId == clientId }
    }
    
    /**
     * Get client portal by project ID
     */
    suspend fun getPortalByProjectId(projectId: String): ClientPortal? {
        return portals.find { it.projectId == projectId }
    }
    
    /**
     * Get client portal settings
     */
    suspend fun getPortalSettings(portalId: String): ClientPortalSettings? {
        return portalSettings.find { it.portalId == portalId }
    }
    
    /**
     * Update client portal settings
     */
    suspend fun updatePortalSettings(settings: ClientPortalSettings): Boolean {
        return try {
            val index = portalSettings.indexOfFirst { it.portalId == settings.portalId }
            if (index != -1) {
                portalSettings[index] = settings
                true
            } else {
                portalSettings.add(settings)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Log client portal activity
     */
    suspend fun logActivity(
        portalId: String,
        clientId: String,
        activityType: ClientPortalActivityType,
        description: String,
        relatedItemId: String? = null,
        relatedItemType: String? = null
    ): Boolean {
        return try {
            val activity = ClientPortalActivity(
                id = UUID.randomUUID().toString(),
                portalId = portalId,
                clientId = clientId,
                activityType = activityType,
                description = description,
                timestamp = System.currentTimeMillis(),
                relatedItemId = relatedItemId,
                relatedItemType = relatedItemType
            )
            
            portalActivities.add(activity)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get client portal activities
     */
    suspend fun getActivities(portalId: String): List<ClientPortalActivity> {
        return portalActivities.filter { it.portalId == portalId }
    }
    
    /**
     * Get client portal activities by client
     */
    suspend fun getActivitiesByClient(clientId: String): List<ClientPortalActivity> {
        return portalActivities.filter { it.clientId == clientId }
    }
    
    /**
     * Update portal access token
     */
    suspend fun updateAccessToken(portalId: String): String? {
        val portal = getById(portalId) ?: return null
        
        val newToken = UUID.randomUUID().toString()
        val updatedPortal = portal.copy(
            accessToken = newToken,
            updatedAt = System.currentTimeMillis()
        )
        
        return if (update(updatedPortal)) {
            newToken
        } else {
            null
        }
    }
    
    /**
     * Record portal access
     */
    suspend fun recordAccess(portalId: String): Boolean {
        val portal = getById(portalId) ?: return false
        
        val updatedPortal = portal.copy(
            lastAccessDate = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        return update(updatedPortal)
    }
}