package com.nextgenbuildpro.crm.data.repository

import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.core.DateUtils
import com.nextgenbuildpro.crm.data.model.Lead
import com.nextgenbuildpro.crm.data.model.LeadPhase
import com.nextgenbuildpro.crm.data.model.LeadSource
import com.nextgenbuildpro.crm.data.model.MediaAttachment
import com.nextgenbuildpro.features.leads.di.LeadRepositoryModule
import com.nextgenbuildpro.features.leads.domain.Lead as DomainLead
import com.nextgenbuildpro.features.leads.domain.LeadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Repository for managing leads in the CRM module
 */
class LeadRepository : Repository<Lead> {
    private val _leads = MutableStateFlow<List<Lead>>(emptyList())
    val leads: StateFlow<List<Lead>> = _leads.asStateFlow()

    init {
        // Try to load data from Firestore first
        loadFromFirestore()

        // If no data is loaded, fall back to sample data
        if (_leads.value.isEmpty()) {
            loadSampleData()
        }
    }

    /**
     * Load leads from Firestore
     */
    private fun loadFromFirestore() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get the Firestore repository
                val firestoreRepo = LeadRepositoryModule.provideLeadRepository()

                // Get leads from Firestore
                val domainLeads = firestoreRepo.getLeads().first()

                // Convert domain leads to CRM leads
                val crmLeads = domainLeads.map { domainLead -> convertDomainToCrm(domainLead) }

                // Update the in-memory list
                _leads.value = crmLeads
            } catch (e: Exception) {
                // Log error but don't crash
                android.util.Log.e("LeadRepository", "Error loading leads from Firestore", e)
            }
        }
    }

    /**
     * Convert a Domain Lead to a CRM Lead
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
            attachments = emptyList() // We don't have a way to convert photo URLs to MediaAttachment objects yet
        )
    }

    /**
     * Convert a Domain LeadStatus to a CRM LeadPhase
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
     * Get all leads
     */
    override suspend fun getAll(): List<Lead> {
        return _leads.value
    }

    /**
     * Get a lead by ID
     */
    override suspend fun getById(id: String): Lead? {
        return _leads.value.find { it.id == id }
    }

    /**
     * Save a new lead
     */
    override suspend fun save(item: Lead): Boolean {
        try {
            // Save to Firestore first
            val firestoreSuccess = saveToFirestore(item)

            if (firestoreSuccess) {
                // If Firestore save was successful, update in-memory list
                _leads.value = _leads.value + item
                return true
            } else {
                // If Firestore save failed, don't update in-memory list
                return false
            }
        } catch (e: Exception) {
            android.util.Log.e("LeadRepository", "Error saving lead", e)
            return false
        }
    }

    /**
     * Update an existing lead
     */
    override suspend fun update(item: Lead): Boolean {
        try {
            // Save to Firestore first
            val firestoreSuccess = saveToFirestore(item)

            if (firestoreSuccess) {
                // If Firestore save was successful, update in-memory list
                _leads.value = _leads.value.map { 
                    if (it.id == item.id) item else it 
                }
                return true
            } else {
                // If Firestore save failed, don't update in-memory list
                return false
            }
        } catch (e: Exception) {
            android.util.Log.e("LeadRepository", "Error updating lead", e)
            return false
        }
    }

    /**
     * Save a lead to Firestore
     * @return true if the save was successful, false otherwise
     */
    private suspend fun saveToFirestore(item: Lead): Boolean {
        return try {
            // Get the Firestore repository
            val firestoreRepo = LeadRepositoryModule.provideLeadRepository()

            // Convert CRM Lead to Domain Lead
            val domainLead = convertCrmToDomain(item)

            // Save to Firestore and get the result
            val savedId = firestoreRepo.saveLead(domainLead)

            // If the saved ID is not empty, the save was successful
            savedId.isNotEmpty()
        } catch (e: Exception) {
            // Log error and return false
            android.util.Log.e("LeadRepository", "Error saving lead to Firestore", e)
            false
        }
    }

    /**
     * Convert a CRM Lead to a Domain Lead
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
     * Convert a CRM LeadPhase to a Domain LeadStatus
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

    /**
     * Delete a lead by ID
     */
    override suspend fun delete(id: String): Boolean {
        try {
            // Delete from Firestore first
            val firestoreSuccess = deleteFromFirestore(id)

            if (firestoreSuccess) {
                // If Firestore delete was successful, update in-memory list
                _leads.value = _leads.value.filter { it.id != id }
                return true
            } else {
                // If Firestore delete failed, don't update in-memory list
                return false
            }
        } catch (e: Exception) {
            android.util.Log.e("LeadRepository", "Error deleting lead", e)
            return false
        }
    }

    /**
     * Delete a lead from Firestore
     * @return true if the delete was successful, false otherwise
     */
    private suspend fun deleteFromFirestore(id: String): Boolean {
        return try {
            // Get the Firestore repository
            val firestoreRepo = LeadRepositoryModule.provideLeadRepository()

            // Delete from Firestore
            firestoreRepo.deleteLead(id)
            true
        } catch (e: Exception) {
            // Log error and return false
            android.util.Log.e("LeadRepository", "Error deleting lead from Firestore", e)
            false
        }
    }

    /**
     * Get leads filtered by phase
     */
    suspend fun getLeadsByPhase(phase: LeadPhase): List<Lead> {
        return _leads.value.filter { it.phase == phase }
    }

    /**
     * Get leads filtered by source
     */
    suspend fun getLeadsBySource(source: String): List<Lead> {
        return _leads.value.filter { it.source == source }
    }

    /**
     * Search leads by name or email
     */
    suspend fun searchLeads(query: String): List<Lead> {
        if (query.isBlank()) return _leads.value

        val lowercaseQuery = query.lowercase()
        return _leads.value.filter { 
            it.name.lowercase().contains(lowercaseQuery) || 
            (it.email?.lowercase()?.contains(lowercaseQuery) ?: false)
        }
    }

    /**
     * Find a lead by phone number
     */
    fun findLeadByPhone(phoneNumber: String): Lead? {
        // Normalize the phone number by removing non-digit characters
        val normalizedPhone = phoneNumber.replace("[^0-9]".toRegex(), "")

        // Find the lead with the matching phone number
        return _leads.value.find { lead ->
            val leadPhone = lead.phone.replace("[^0-9]".toRegex(), "")
            leadPhone == normalizedPhone
        }
    }

    /**
     * Add a note to a lead
     */
    suspend fun addLeadNote(leadId: String, note: String): Boolean {
        try {
            _leads.value = _leads.value.map { lead ->
                if (lead.id == leadId) {
                    val timestamp = DateUtils.getCurrentTimestamp()
                    val updatedNotes = lead.notes + "\n" + timestamp + ": " + note
                    lead.copy(notes = updatedNotes.trim())
                } else {
                    lead
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Update a lead's phase
     */
    suspend fun updateLeadPhase(leadId: String, newPhase: LeadPhase): Boolean {
        try {
            _leads.value = _leads.value.map { lead ->
                if (lead.id == leadId) {
                    lead.copy(phase = newPhase)
                } else {
                    lead
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Load sample data for demonstration
     */
    private fun loadSampleData() {
        val currentTime = System.currentTimeMillis()

        val sampleLeads = listOf(
            Lead(
                id = "lead_1",
                name = "John Smith",
                phone = "555-123-4567",
                email = "john.smith@example.com",
                address = "123 Main St, Anytown, CA 12345",
                projectType = "Kitchen Renovation",
                phase = LeadPhase.CONTACTED,
                notes = "Initial consultation scheduled for 06/15/2023",
                urgency = "Medium",
                source = LeadSource.WEBSITE.displayName,
                intakeTimestamp = currentTime - (7 * 24 * 60 * 60 * 1000), // 7 days ago
                attachments = emptyList()
            ),
            Lead(
                id = "lead_2",
                name = "Sarah Johnson",
                phone = "555-234-5678",
                email = "sarah.johnson@example.com",
                address = "456 Oak Ave, Somewhere, NY 67890",
                projectType = "Bathroom Remodel",
                phase = LeadPhase.QUALIFIED,
                notes = "Interested in premium fixtures, budget around $10k",
                urgency = "High",
                source = LeadSource.REFERRAL.displayName,
                intakeTimestamp = currentTime - (5 * 24 * 60 * 60 * 1000), // 5 days ago
                attachments = emptyList()
            ),
            Lead(
                id = "lead_3",
                name = "Michael Brown",
                phone = "555-345-6789",
                email = "michael.brown@example.com",
                address = "789 Pine Rd, Nowhere, TX 54321",
                projectType = "Deck Construction",
                phase = LeadPhase.DISCOVERY,
                notes = "Site visit scheduled for next week",
                urgency = "Low",
                source = LeadSource.GOOGLE_ADS.displayName,
                intakeTimestamp = currentTime - (3 * 24 * 60 * 60 * 1000), // 3 days ago
                attachments = emptyList()
            ),
            Lead(
                id = "lead_4",
                name = "Emily Davis",
                phone = "555-456-7890",
                email = "emily.davis@example.com",
                address = "321 Elm St, Somewhere Else, FL 13579",
                projectType = "Full Home Renovation",
                phase = LeadPhase.SCOPED,
                notes = "Project scope defined, ready for estimate",
                urgency = "Medium",
                source = LeadSource.SOCIAL_MEDIA.displayName,
                intakeTimestamp = currentTime - (2 * 24 * 60 * 60 * 1000), // 2 days ago
                attachments = emptyList()
            ),
            Lead(
                id = "lead_5",
                name = "David Wilson",
                phone = "555-567-8901",
                email = "david.wilson@example.com",
                address = "654 Maple Dr, Anyplace, WA 97531",
                projectType = "Kitchen and Bath Remodel",
                phase = LeadPhase.ESTIMATING,
                notes = "Working on detailed estimate",
                urgency = "High",
                source = LeadSource.WEBSITE.displayName,
                intakeTimestamp = currentTime - (1 * 24 * 60 * 60 * 1000), // 1 day ago
                attachments = emptyList()
            )
        )

        _leads.value = sampleLeads
    }
}
