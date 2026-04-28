package com.aireceptionist.app.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.aireceptionist.app.data.models.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for call-related operations
 */
@Dao
interface CallDao {
    
    // CallContext operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallContext(callContext: CallContext)
    
    @Update
    suspend fun updateCallContext(callContext: CallContext)
    
    @Query("SELECT * FROM call_contexts WHERE callId = :callId")
    suspend fun getCallContext(callId: String): CallContext?
    
    @Query("SELECT * FROM call_contexts WHERE callerNumber = :phoneNumber ORDER BY callStartTime DESC")
    suspend fun getCallContextsByPhoneNumber(phoneNumber: String): List<CallContext>
    
    @Query("DELETE FROM call_contexts WHERE callId = :callId")
    suspend fun deleteCallContext(callId: String)
    
    // CallRecord operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallRecord(callRecord: CallRecord)
    
    @Update
    suspend fun updateCallRecord(callRecord: CallRecord)
    
    @Query("SELECT * FROM call_records WHERE id = :id")
    suspend fun getCallRecord(id: String): CallRecord?
    
    @Query("SELECT * FROM call_records WHERE callId = :callId")
    suspend fun getCallRecordByCallId(callId: String): CallRecord?
    
    @Query("SELECT * FROM call_records ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentCallRecords(limit: Int = 100): List<CallRecord>
    
    @Query("SELECT * FROM call_records WHERE callerNumber = :phoneNumber ORDER BY startTime DESC")
    suspend fun getCallRecordsByPhoneNumber(phoneNumber: String): List<CallRecord>
    
    @Query("SELECT * FROM call_records WHERE DATE(startTime/1000, 'unixepoch') = DATE('now') ORDER BY startTime DESC")
    suspend fun getTodaysCallRecords(): List<CallRecord>
    
    @Query("SELECT * FROM call_records WHERE startTime >= :startTime AND startTime <= :endTime ORDER BY startTime DESC")
    suspend fun getCallRecordsByDateRange(startTime: Long, endTime: Long): List<CallRecord>
    
    @Query("DELETE FROM call_records WHERE id = :id")
    suspend fun deleteCallRecord(id: String)
    
    // CallInteraction operations  
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallInteraction(interaction: CallInteraction)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallInteractions(interactions: List<CallInteraction>)
    
    @Query("SELECT * FROM call_interactions WHERE callRecordId = :callRecordId ORDER BY timestamp ASC")
    suspend fun getCallInteractions(callRecordId: String): List<CallInteraction>
    
    @Query("SELECT * FROM call_interactions WHERE callRecordId = :callRecordId AND speakerType = :speakerType ORDER BY timestamp ASC")
    suspend fun getCallInteractionsBySpeaker(callRecordId: String, speakerType: SpeakerType): List<CallInteraction>
    
    @Query("DELETE FROM call_interactions WHERE callRecordId = :callRecordId")
    suspend fun deleteCallInteractions(callRecordId: String)
    
    // Aggregate queries
    @Query("SELECT COUNT(*) FROM call_records WHERE DATE(startTime/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodaysCallCount(): Int
    
    @Query("SELECT AVG(duration) FROM call_records WHERE DATE(startTime/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodaysAverageCallDuration(): Double?
    
    @Query("SELECT COUNT(*) FROM call_records WHERE transferredToHuman = 1 AND DATE(startTime/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodaysHumanTransferCount(): Int
    
    @Query("SELECT AVG(satisfactionScore) FROM call_records WHERE satisfactionScore IS NOT NULL AND DATE(startTime/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodaysAverageSatisfaction(): Double?
    
    @Query("""
        SELECT callType, COUNT(*) as count 
        FROM call_records 
        WHERE DATE(startTime/1000, 'unixepoch') = DATE('now') 
        GROUP BY callType
    """)
    suspend fun getTodaysCallTypeBreakdown(): List<CallTypeCount>
    
    @Query("""
        SELECT callResult, COUNT(*) as count 
        FROM call_records 
        WHERE DATE(startTime/1000, 'unixepoch') = DATE('now') 
        GROUP BY callResult
    """)
    suspend fun getTodaysCallResultBreakdown(): List<CallResultCount>
    
    // Live data for UI
    @Query("SELECT * FROM call_records ORDER BY startTime DESC LIMIT 50")
    fun getRecentCallRecordsLiveData(): LiveData<List<CallRecord>>
    
    @Query("SELECT COUNT(*) FROM call_records WHERE DATE(startTime/1000, 'unixepoch') = DATE('now')")
    fun getTodaysCallCountLiveData(): LiveData<Int>
    
    @Query("SELECT * FROM call_interactions WHERE callRecordId = :callRecordId ORDER BY timestamp ASC")
    fun getCallInteractionsFlow(callRecordId: String): Flow<List<CallInteraction>>
    
    // Search operations
    @Query("""
        SELECT * FROM call_records 
        WHERE callerNumber LIKE '%' || :query || '%' 
        OR callerName LIKE '%' || :query || '%'
        OR notes LIKE '%' || :query || '%'
        ORDER BY startTime DESC
        LIMIT :limit
    """)
    suspend fun searchCallRecords(query: String, limit: Int = 50): List<CallRecord>
    
    @Query("""
        SELECT * FROM call_interactions 
        WHERE content LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun searchCallInteractions(query: String, limit: Int = 50): List<CallInteraction>
}

/**
 * Data classes for aggregate query results
 */
data class CallTypeCount(
    val callType: CallType,
    val count: Int
)

data class CallResultCount(
    val callResult: CallResult,
    val count: Int
)