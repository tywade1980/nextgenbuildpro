package com.nextgenbuildpro.crm.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgenbuildpro.core.ViewModel as CoreViewModel
import com.nextgenbuildpro.crm.data.model.Lead
import com.nextgenbuildpro.crm.data.model.LeadPhase
import com.nextgenbuildpro.crm.data.repository.LeadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for managing leads in the CRM module
 */
class LeadsViewModel(
    private val repository: LeadRepository
) : ViewModel(), CoreViewModel<Lead> {

    // All leads
    private val _leads = mutableStateOf<List<Lead>>(emptyList())
    override val items: State<List<Lead>> = _leads

    // Filtered leads
    private val _filteredLeads = MutableStateFlow<List<Lead>>(emptyList())
    val filteredLeads: StateFlow<List<Lead>> = _filteredLeads.asStateFlow()

    // Selected lead
    private val _selectedLead = MutableStateFlow<Lead?>(null)
    val selectedLead: StateFlow<Lead?> = _selectedLead.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Status filter
    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Collect leads from repository
        viewModelScope.launch {
            repository.leads.collectLatest { leads ->
                _leads.value = leads
                updateFilteredLeads()
            }
        }
    }

    /**
     * Refresh leads from repository
     */
    override fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _leads.value = repository.getAll()
                updateFilteredLeads()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to refresh leads: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredLeads()
    }

    /**
     * Update status filter
     */
    fun updateStatusFilter(status: String) {
        _statusFilter.value = status
        updateFilteredLeads()
    }

    /**
     * Select a lead
     */
    fun selectLead(lead: Lead) {
        _selectedLead.value = lead
    }

    /**
     * Clear selected lead
     */
    fun clearSelectedLead() {
        _selectedLead.value = null
    }

    /**
     * Create a new lead
     */
    fun createLead(lead: Lead) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.save(lead)
                if (!success) {
                    _error.value = "Failed to create lead"
                } else {
                    _error.value = null
                }
            } catch (e: Exception) {
                _error.value = "Failed to create lead: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing lead
     */
    fun updateLead(lead: Lead) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.update(lead)
                if (!success) {
                    _error.value = "Failed to update lead"
                } else {
                    _error.value = null
                    if (_selectedLead.value?.id == lead.id) {
                        _selectedLead.value = lead
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to update lead: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a lead
     */
    fun deleteLead(leadId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.delete(leadId)
                if (!success) {
                    _error.value = "Failed to delete lead"
                } else {
                    _error.value = null
                    if (_selectedLead.value?.id == leadId) {
                        _selectedLead.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete lead: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a note to a lead
     */
    fun addLeadNote(leadId: String, note: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.addLeadNote(leadId, note)
                if (!success) {
                    _error.value = "Failed to add note to lead"
                } else {
                    _error.value = null
                    // Refresh selected lead if it's the one we added a note to
                    if (_selectedLead.value?.id == leadId) {
                        _selectedLead.value = repository.getById(leadId)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to add note to lead: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update a lead's phase
     */
    fun updateLeadPhase(leadId: String, newPhase: LeadPhase) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.updateLeadPhase(leadId, newPhase)
                if (!success) {
                    _error.value = "Failed to update lead phase"
                } else {
                    _error.value = null
                    // Refresh selected lead if it's the one we updated
                    if (_selectedLead.value?.id == leadId) {
                        _selectedLead.value = repository.getById(leadId)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to update lead phase: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update a lead's status (for backward compatibility)
     */
    fun updateLeadStatus(leadId: String, newStatus: String) {
        try {
            // Convert string status to LeadPhase enum
            val phase = LeadPhase.valueOf(newStatus)
            updateLeadPhase(leadId, phase)
        } catch (e: Exception) {
            _error.value = "Invalid lead phase: $newStatus"
        }
    }

    /**
     * Update filtered leads based on search query and status filter
     */
    private fun updateFilteredLeads() {
        val query = _searchQuery.value
        val status = _statusFilter.value

        val filtered = if (query.isBlank() && status == "All") {
            _leads.value
        } else {
            _leads.value.filter { lead ->
                val matchesQuery = query.isBlank() || 
                    lead.name.contains(query, ignoreCase = true) || 
                    (lead.email?.contains(query, ignoreCase = true) ?: false)

                val matchesStatus = status == "All" || lead.phase.name == status

                matchesQuery && matchesStatus
            }
        }

        _filteredLeads.value = filtered
    }
}
