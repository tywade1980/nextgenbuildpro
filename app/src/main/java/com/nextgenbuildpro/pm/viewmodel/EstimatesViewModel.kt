package com.nextgenbuildpro.pm.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgenbuildpro.core.ViewModel as CoreViewModel
import com.nextgenbuildpro.pm.data.model.Estimate
import com.nextgenbuildpro.pm.data.model.EstimateStatus
import com.nextgenbuildpro.pm.data.repository.EstimateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing estimates in the PM module
 */
class EstimatesViewModel(
    private val repository: EstimateRepository
) : ViewModel(), CoreViewModel<Estimate> {

    // All estimates
    private val _estimates = mutableStateOf<List<Estimate>>(emptyList())
    override val items: State<List<Estimate>> = _estimates

    // Filtered estimates
    private val _filteredEstimates = MutableStateFlow<List<Estimate>>(emptyList())
    val filteredEstimates: StateFlow<List<Estimate>> = _filteredEstimates.asStateFlow()

    // Selected estimate
    private val _selectedEstimate = MutableStateFlow<Estimate?>(null)
    val selectedEstimate: StateFlow<Estimate?> = _selectedEstimate.asStateFlow()

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
        refresh()
    }

    /**
     * Refresh estimates from repository
     */
    override fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _estimates.value = repository.getAll()
                updateFilteredEstimates()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to refresh estimates: ${e.message}"
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
        updateFilteredEstimates()
    }

    /**
     * Update status filter
     */
    fun updateStatusFilter(status: String) {
        _statusFilter.value = status
        updateFilteredEstimates()
    }

    /**
     * Select an estimate
     */
    fun selectEstimate(estimate: Estimate) {
        _selectedEstimate.value = estimate
    }

    /**
     * Clear selected estimate
     */
    fun clearSelectedEstimate() {
        _selectedEstimate.value = null
    }

    /**
     * Create a new estimate
     */
    fun createEstimate(estimate: Estimate) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.save(estimate)
                if (!success) {
                    _error.value = "Failed to create estimate"
                } else {
                    _error.value = null
                    refresh()
                }
            } catch (e: Exception) {
                _error.value = "Failed to create estimate: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing estimate
     */
    fun updateEstimate(estimate: Estimate) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.update(estimate)
                if (!success) {
                    _error.value = "Failed to update estimate"
                } else {
                    _error.value = null
                    refresh()
                    if (_selectedEstimate.value?.id == estimate.id) {
                        _selectedEstimate.value = estimate
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to update estimate: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete an estimate
     */
    fun deleteEstimate(estimateId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.delete(estimateId)
                if (!success) {
                    _error.value = "Failed to delete estimate"
                } else {
                    _error.value = null
                    refresh()
                    if (_selectedEstimate.value?.id == estimateId) {
                        _selectedEstimate.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete estimate: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an estimate's status
     */
    fun updateEstimateStatus(estimateId: String, newStatus: EstimateStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.updateEstimateStatus(estimateId, newStatus.name)
                if (!success) {
                    _error.value = "Failed to update estimate status"
                } else {
                    _error.value = null
                    refresh()
                    // Refresh selected estimate if it's the one we updated
                    if (_selectedEstimate.value?.id == estimateId) {
                        _selectedEstimate.value = repository.getById(estimateId)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to update estimate status: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update filtered estimates based on search query and status filter
     */
    private fun updateFilteredEstimates() {
        val query = _searchQuery.value
        val status = _statusFilter.value

        val filtered = if (query.isBlank() && status == "All") {
            _estimates.value
        } else {
            _estimates.value.filter { estimate ->
                val matchesQuery = query.isBlank() || 
                    estimate.title.contains(query, ignoreCase = true) || 
                    estimate.clientName.contains(query, ignoreCase = true)

                val matchesStatus = status == "All" || estimate.status == status

                matchesQuery && matchesStatus
            }
        }

        _filteredEstimates.value = filtered
    }
}