package com.nextgenbuildpro.crm.data.repository

import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.crm.data.model.Lead
import com.nextgenbuildpro.crm.data.model.LeadPhase
import com.nextgenbuildpro.crm.data.model.MediaAttachment
import com.nextgenbuildpro.features.leads.domain.LeadRepository as DomainLeadRepository
import com.nextgenbuildpro.features.leads.domain.Lead as DomainLead
import com.nextgenbuildpro.features.leads.domain.LeadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Adapter that converts between the domain Lead model and the CRM Lead model
 * This allows the CRM module to use the Firestore implementation of LeadRepository
 */
class LeadRepositoryAdapter(
    private val domainRepository: DomainLeadRepository
) : Repository<Lead> {
    private val _leads = MutableStateFlow<List<Lead>>(emptyList())
    val leads: StateFlow<List<Lead>> = _leads.asStateFlow()

    init {
        // Load initial data
        refreshLeads()
    }

    /**
     * Refresh leads from the domain repository
     */
    private fun refreshLeads() {
        // Launch a coroutine to load leads from the domain repository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val domainLeads = domainRepository.getLeads().first()
                val crmLeads = domainLeads.map { convertDomainToCrm(it) }
                _leads.value = crmLeads
            } catch (e: Exception) {
                // Log error but don't crash
                android.util.Log.e("LeadRepositoryAdapter", "Error refreshing leads", e)
            }
        }
    }

    /**
     * Get all leads
     */
    override suspend fun getAll(): List<Lead> {
        val domainLeads = domainRepository.getLeads().first()
        return domainLeads.map { convertDomainToCrm(it) }
    }

    /**
     * Get a lead by ID
     */
    override suspend fun getById(id: String): Lead? {
        val domainLead = domainRepository.getLead(id).first()
        return domainLead?.let { convertDomainToCrm(it) }
    }

    /**
     * Save a new lead
     */
    override suspend fun save(item: Lead): Boolean {
        try {
            val domainLead = convertCrmToDomain(item)
            domainRepository.saveLead(domainLead)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Update an existing lead
     */
    override suspend fun update(item: Lead): Boolean {
        try {
            val domainLead = convertCrmToDomain(item)
            domainRepository.saveLead(domainLead)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Delete a lead by ID
     */
    override suspend fun delete(id: String): Boolean {
        try {
            domainRepository.deleteLead(id)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Add a note to a lead
     */
    suspend fun addLeadNote(leadId: String, note: String): Boolean {
        try {
            val lead = getById(leadId) ?: return false
            val updatedNotes = lead.notes + "\n" + note
            val updatedLead = lead.copy(notes = updatedNotes.trim())
            return update(updatedLead)
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Update a lead's phase
     */
    suspend fun updateLeadPhase(leadId: String, newPhase: LeadPhase): Boolean {
        try {
            val lead = getById(leadId) ?: return false
            val updatedLead = lead.copy(phase = newPhase)
            return update(updatedLead)
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Convert a domain Lead to a CRM Lead
     */
    private fun convertDomainToCrm(domainLead: DomainLead): Lead {
        return Lead(
            id = domainLead.id,
            name = domainLead.name,
            phone = domainLead.phone,
            email = domainLead.email.takeIf { it.isNotBlank() },
            address = domainLead.address,
            projectType = domainLead.source, // Use source as project type for now
            phase = convertStatusToPhase(domainLead.status),
            notes = domainLead.notes,
            urgency = "Medium", // Default urgency
            source = domainLead.source,
            intakeTimestamp = domainLead.createdAt.time,
            attachments = domainLead.photoUrls.map { 
                MediaAttachment(
                    id = java.util.UUID.randomUUID().toString(),
                    leadId = domainLead.id,
                    name = "Photo",
                    type = "image/jpeg",
                    url = it,
                    thumbnailUrl = null,
                    size = 0,
                    uploadedAt = domainLead.createdAt.time,
                    description = null
                )
            }
        )
    }

    /**
     * Convert a CRM Lead to a domain Lead
     */
    private fun convertCrmToDomain(crmLead: Lead): DomainLead {
        return DomainLead(
            id = crmLead.id,
            name = crmLead.name,
            phone = crmLead.phone,
            email = crmLead.email ?: "",
            address = crmLead.address,
            notes = crmLead.notes,
            status = convertPhaseToStatus(crmLead.phase),
            createdAt = Date(crmLead.intakeTimestamp),
            updatedAt = Date(),
            photoUrls = crmLead.attachments.map { it.url },
            source = crmLead.source,
            assignedTo = ""
        )
    }

    /**
     * Convert a domain LeadStatus to a CRM LeadPhase
     */
    private fun convertStatusToPhase(status: LeadStatus): LeadPhase {
        return when (status) {
            LeadStatus.NEW -> LeadPhase.CONTACTED
            LeadStatus.CONTACTED -> LeadPhase.CONTACTED
            LeadStatus.QUALIFIED -> LeadPhase.QUALIFIED
            LeadStatus.PROPOSAL -> LeadPhase.ESTIMATING
            LeadStatus.NEGOTIATION -> LeadPhase.DELIVERED
            LeadStatus.WON -> LeadPhase.CLOSED_WON
            LeadStatus.LOST -> LeadPhase.CLOSED_LOST
            LeadStatus.INACTIVE -> LeadPhase.DISCOVERY
        }
    }

    /**
     * Convert a CRM LeadPhase to a domain LeadStatus
     */
    private fun convertPhaseToStatus(phase: LeadPhase): LeadStatus {
        return when (phase) {
            LeadPhase.CONTACTED -> LeadStatus.CONTACTED
            LeadPhase.QUALIFIED -> LeadStatus.QUALIFIED
            LeadPhase.DISCOVERY -> LeadStatus.INACTIVE
            LeadPhase.SCOPED -> LeadStatus.QUALIFIED
            LeadPhase.ESTIMATING -> LeadStatus.PROPOSAL
            LeadPhase.DELIVERED -> LeadStatus.NEGOTIATION
            LeadPhase.CLOSED_WON -> LeadStatus.WON
            LeadPhase.CLOSED_LOST -> LeadStatus.LOST
        }
    }
}
