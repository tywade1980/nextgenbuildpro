package com.aireceptionist.app.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aireceptionist.app.ai.agents.AgentManager
import com.aireceptionist.app.data.models.CallRecord
import com.aireceptionist.app.data.repository.CallRepository
import com.aireceptionist.app.data.repository.CallStats
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

/**
 * ViewModel for MainActivity
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val agentManager: AgentManager
) : ViewModel() {
    
    private val _callHistory = MutableLiveData<List<CallRecord>>()
    val callHistory: LiveData<List<CallRecord>> = _callHistory
    
    private val _callStats = MutableLiveData<CallStats>()
    val callStats: LiveData<CallStats> = _callStats
    
    private val _agentStatus = MutableLiveData<Map<String, Boolean>>()
    val agentStatus: LiveData<Map<String, Boolean>> = _agentStatus
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private var isInitialized = false
    
    fun initialize() {
        if (isInitialized) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Logger.i(TAG, "Initializing MainViewModel")
                
                // Initialize agent manager if not already done
                agentManager.initialize()
                
                isInitialized = true
                Logger.i(TAG, "MainViewModel initialized successfully")
                
            } catch (e: Exception) {
                Logger.e(TAG, "Error initializing MainViewModel", e)
                _errorMessage.value = "Failed to initialize: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadCallHistory() {
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Loading call history")
                val recentCalls = callRepository.getRecentCallRecords(50)
                _callHistory.value = recentCalls
                Logger.d(TAG, "Loaded ${recentCalls.size} call records")
            } catch (e: Exception) {
                Logger.e(TAG, "Error loading call history", e)
                _errorMessage.value = "Failed to load call history"
                _callHistory.value = emptyList()
            }
        }
    }
    
    fun loadCallStats() {
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Loading call statistics")
                val stats = callRepository.getTodaysCallStats()
                _callStats.value = stats
                Logger.d(TAG, "Loaded call stats: ${stats.totalCalls} calls")
            } catch (e: Exception) {
                Logger.e(TAG, "Error loading call stats", e)
                _errorMessage.value = "Failed to load statistics"
                _callStats.value = CallStats()
            }
        }
    }
    
    fun loadAgentStatus() {
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Loading agent status")
                val agentHealth = agentManager.getSystemHealth()
                _agentStatus.value = agentHealth
                
                val healthyCount = agentHealth.count { it.value }
                Logger.d(TAG, "Agent status: $healthyCount/${agentHealth.size} healthy")
            } catch (e: Exception) {
                Logger.e(TAG, "Error loading agent status", e)
                _errorMessage.value = "Failed to load agent status"
                _agentStatus.value = emptyMap()
            }
        }
    }
    
    fun refreshData() {
        loadCallHistory()
        loadCallStats()
        loadAgentStatus()
    }
    
    fun searchCallHistory(query: String) {
        if (query.isBlank()) {
            loadCallHistory()
            return
        }
        
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Searching call history: $query")
                val searchResults = callRepository.searchCallRecords(query, 50)
                _callHistory.value = searchResults
                Logger.d(TAG, "Found ${searchResults.size} matching calls")
            } catch (e: Exception) {
                Logger.e(TAG, "Error searching call history", e)
                _errorMessage.value = "Search failed"
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
    
    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "MainViewModel cleared")
    }
    
    companion object {
        private const val TAG = "MainViewModel"
    }
}