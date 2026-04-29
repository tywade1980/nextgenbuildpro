package com.aireceptionist.app.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.aireceptionist.app.data.models.AgentMetrics

@Dao
interface AgentMetricsDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgentMetrics(metrics: AgentMetrics)
    
    @Update
    suspend fun updateAgentMetrics(metrics: AgentMetrics)
    
    @Query("SELECT * FROM agent_metrics WHERE agentId = :agentId AND date = :date")
    suspend fun getAgentMetrics(agentId: String, date: String): AgentMetrics?
    
    @Query("SELECT * FROM agent_metrics WHERE agentId = :agentId ORDER BY date DESC")
    suspend fun getAgentMetricsHistory(agentId: String): List<AgentMetrics>
    
    @Query("SELECT * FROM agent_metrics WHERE date = :date")
    suspend fun getAllAgentMetricsForDate(date: String): List<AgentMetrics>
    
    @Query("SELECT * FROM agent_metrics ORDER BY date DESC, agentId ASC")
    fun getAllAgentMetricsLiveData(): LiveData<List<AgentMetrics>>
    
    @Query("DELETE FROM agent_metrics WHERE date < :cutoffDate")
    suspend fun deleteOldMetrics(cutoffDate: String)
}