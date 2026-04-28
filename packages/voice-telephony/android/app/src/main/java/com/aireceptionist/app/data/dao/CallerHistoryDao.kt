package com.aireceptionist.app.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.aireceptionist.app.data.models.CallerHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for caller history operations
 */
@Dao
interface CallerHistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallerHistory(callerHistory: CallerHistoryEntity)
    
    @Update
    suspend fun updateCallerHistory(callerHistory: CallerHistoryEntity)
    
    @Query("SELECT * FROM caller_history WHERE phoneNumber = :phoneNumber")
    suspend fun getCallerHistory(phoneNumber: String): CallerHistoryEntity?
    
    @Query("SELECT * FROM caller_history ORDER BY lastCallDate DESC")
    suspend fun getAllCallerHistory(): List<CallerHistoryEntity>
    
    @Query("SELECT * FROM caller_history WHERE isVip = 1 ORDER BY lastCallDate DESC")
    suspend fun getVipCallers(): List<CallerHistoryEntity>
    
    @Query("SELECT * FROM caller_history WHERE totalCalls > :minCalls ORDER BY totalCalls DESC")
    suspend fun getFrequentCallers(minCalls: Int = 5): List<CallerHistoryEntity>
    
    @Query("SELECT * FROM caller_history WHERE averageSatisfaction < :threshold ORDER BY averageSatisfaction ASC")
    suspend fun getLowSatisfactionCallers(threshold: Float = 3.0f): List<CallerHistoryEntity>
    
    @Query("DELETE FROM caller_history WHERE phoneNumber = :phoneNumber")
    suspend fun deleteCallerHistory(phoneNumber: String)
    
    @Query("DELETE FROM caller_history WHERE lastCallDate < :cutoffTime")
    suspend fun deleteOldCallerHistory(cutoffTime: Long)
    
    // Aggregate queries
    @Query("SELECT COUNT(*) FROM caller_history")
    suspend fun getTotalUniqueCallers(): Int
    
    @Query("SELECT COUNT(*) FROM caller_history WHERE isVip = 1")
    suspend fun getVipCallerCount(): Int
    
    @Query("SELECT AVG(averageSatisfaction) FROM caller_history WHERE averageSatisfaction > 0")
    suspend fun getOverallAverageSatisfaction(): Double?
    
    @Query("SELECT AVG(totalCalls) FROM caller_history")
    suspend fun getAverageCallsPerCaller(): Double?
    
    // Live data for UI
    @Query("SELECT * FROM caller_history ORDER BY lastCallDate DESC LIMIT 20")
    fun getRecentCallersLiveData(): LiveData<List<CallerHistoryEntity>>
    
    @Query("SELECT * FROM caller_history WHERE isVip = 1 ORDER BY lastCallDate DESC")
    fun getVipCallersLiveData(): LiveData<List<CallerHistoryEntity>>
    
    @Query("SELECT COUNT(*) FROM caller_history")
    fun getTotalUniqueCallersLiveData(): LiveData<Int>
    
    // Search operations
    @Query("""
        SELECT * FROM caller_history 
        WHERE phoneNumber LIKE '%' || :query || '%' 
        OR notes LIKE '%' || :query || '%'
        ORDER BY lastCallDate DESC
        LIMIT :limit
    """)
    suspend fun searchCallerHistory(query: String, limit: Int = 50): List<CallerHistoryEntity>
    
    // Batch operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallerHistories(callerHistories: List<CallerHistoryEntity>)
    
    @Transaction
    suspend fun updateCallerStats(
        phoneNumber: String,
        newCallDate: Long,
        satisfactionScore: Float? = null
    ) {
        val existing = getCallerHistory(phoneNumber)
        if (existing != null) {
            val updatedHistory = existing.copy(
                totalCalls = existing.totalCalls + 1,
                lastCallDate = newCallDate,
                averageSatisfaction = if (satisfactionScore != null) {
                    ((existing.averageSatisfaction * existing.totalCalls) + satisfactionScore) / (existing.totalCalls + 1)
                } else {
                    existing.averageSatisfaction
                },
                updatedAt = System.currentTimeMillis()
            )
            updateCallerHistory(updatedHistory)
        } else {
            val newHistory = CallerHistoryEntity(
                phoneNumber = phoneNumber,
                totalCalls = 1,
                lastCallDate = newCallDate,
                averageSatisfaction = satisfactionScore ?: 0.0f
            )
            insertCallerHistory(newHistory)
        }
    }
}