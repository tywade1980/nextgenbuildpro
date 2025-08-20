package com.nextgenbuildpro.features.leads.domain

import android.net.Uri
import kotlinx.coroutines.flow.Flow

/**
 * Interface for lead repository
 * Defines operations that can be performed on leads
 */
interface LeadRepository {
    
    /**
     * Get all leads
     * @return Flow of list of leads
     */
    fun getLeads(): Flow<List<Lead>>
    
    /**
     * Get a lead by ID
     * @param id The ID of the lead
     * @return Flow of the lead or null if not found
     */
    fun getLead(id: String): Flow<Lead?>
    
    /**
     * Get leads by status
     * @param status The status to filter by
     * @return Flow of list of leads with the specified status
     */
    fun getLeadsByStatus(status: LeadStatus): Flow<List<Lead>>
    
    /**
     * Save a lead
     * @param lead The lead to save
     * @return The ID of the saved lead
     */
    suspend fun saveLead(lead: Lead): String
    
    /**
     * Delete a lead
     * @param id The ID of the lead to delete
     */
    suspend fun deleteLead(id: String)
    
    /**
     * Upload a photo for a lead
     * @param leadId The ID of the lead
     * @param photoUri The URI of the photo to upload
     * @return The download URL of the uploaded photo
     */
    suspend fun uploadLeadPhoto(leadId: String, photoUri: Uri): String
    
    /**
     * Update lead status
     * @param id The ID of the lead
     * @param status The new status
     */
    suspend fun updateLeadStatus(id: String, status: LeadStatus)
}