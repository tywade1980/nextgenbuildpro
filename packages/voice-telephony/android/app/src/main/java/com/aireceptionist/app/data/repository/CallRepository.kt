package com.aireceptionist.app.data.repository

import com.aireceptionist.app.ai.agents.impl.CallerHistory
import com.aireceptionist.app.data.dao.CallDao
import com.aireceptionist.app.data.dao.CallerHistoryDao
import com.aireceptionist.app.data.models.*
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for call-related data operations
 */
@Singleton
class CallRepository @Inject constructor(
    private val callDao: CallDao,
    private val callerHistoryDao: CallerHistoryDao
) {
    
    // CallContext operations
    suspend fun insertCallContext(callContext: CallContext) {
        try {
            callDao.insertCallContext(callContext)
            Logger.d(TAG, "Call context inserted: ${callContext.callId}")
        } catch (e: Exception) {
            Logger.e(TAG, "Error inserting call context", e)
            throw e
        }
    }
    
    suspend fun updateCallContext(callContext: CallContext) {
        try {
            callDao.updateCallContext(callContext)
            Logger.d(TAG, "Call context updated: ${callContext.callId}")
        } catch (e: Exception) {
            Logger.e(TAG, "Error updating call context", e)
            throw e
        }
    }
    
    suspend fun getCallContext(callId: String): CallContext? {
        return try {
            callDao.getCallContext(callId)
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting call context", e)
            null
        }
    }
    
    // CallRecord operations
    suspend fun insertCallRecord(callRecord: CallRecord) {
        try {
            callDao.insertCallRecord(callRecord)
            
            // Update caller history
            callRecord.callerNumber?.let { phoneNumber ->
                callerHistoryDao.updateCallerStats(
                    phoneNumber = phoneNumber,
                    newCallDate = callRecord.startTime,
                    satisfactionScore = callRecord.satisfactionScore
                )
            }
            
            Logger.d(TAG, "Call record inserted: ${callRecord.id}")
        } catch (e: Exception) {
            Logger.e(TAG, "Error inserting call record", e)
            throw e
        }
    }
    
    suspend fun updateCallRecord(callRecord: CallRecord) {
        try {
            callDao.updateCallRecord(callRecord)
            Logger.d(TAG, "Call record updated: ${callRecord.id}")
        } catch (e: Exception) {
            Logger.e(TAG, "Error updating call record", e)
            throw e
        }
    }
    
    suspend fun getCallRecord(id: String): CallRecord? {
        return try {
            callDao.getCallRecord(id)
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting call record", e)
            null
        }
    }
    
    suspend fun getRecentCallRecords(limit: Int = 100): List<CallRecord> {
        return try {
            callDao.getRecentCallRecords(limit)
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting recent call records", e)
            emptyList()
        }
    }
    
    suspend fun getCallRecordsByPhoneNumber(phoneNumber: String): List<CallRecord> {
        return try {
            callDao.getCallRecordsByPhoneNumber(phoneNumber)
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting call records by phone number", e)
            emptyList()
        }
    }
    
    // CallInteraction operations
    suspend fun insertCallInteraction(interaction: CallInteraction) {
        try {
            callDao.insertCallInteraction(interaction)
            Logger.d(TAG, "Call interaction inserted: ${interaction.id}")
        } catch (e: Exception) {
            Logger.e(TAG, "Error inserting call interaction", e)
            throw e
        }
    }
    
    suspend fun insertCallInteractions(interactions: List<CallInteraction>) {
        try {
            callDao.insertCallInteractions(interactions)
            Logger.d(TAG, "Call interactions inserted: ${interactions.size}")
        } catch (e: Exception) {
            Logger.e(TAG, "Error inserting call interactions", e)
            throw e
        }
    }
    
    suspend fun getCallInteractions(callRecordId: String): List<CallInteraction> {
        return try {
            callDao.getCallInteractions(callRecordId)
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting call interactions", e)
            emptyList()
        }
    }
    
    fun getCallInteractionsFlow(callRecordId: String): Flow<List<CallInteraction>> {
        return callDao.getCallInteractionsFlow(callRecordId)
    }
    
    // Caller history operations
    suspend fun getCallerHistory(phoneNumber: String): CallerHistory? {
        return try {
            val entity = callerHistoryDao.getCallerHistory(phoneNumber)
            entity?.let {
                CallerHistory(
                    phoneNumber = it.phoneNumber,
                    callCount = it.totalCalls,
                    lastCallDate = it.lastCallDate,
                    satisfactionScore = it.averageSatisfaction,
                    isVip = it.isVip,
                    preferredLanguage = it.preferredLanguage,
                    commonIssues = emptyList() // Would parse from commonIntents JSON
                )
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting caller history", e)
            null
        }
    }
    
    suspend fun markCallerAsVip(phoneNumber: String, isVip: Boolean = true) {
        try {
            val existing = callerHistoryDao.getCallerHistory(phoneNumber)
            if (existing != null) {
                callerHistoryDao.updateCallerHistory(
                    existing.copy(
                        isVip = isVip,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                Logger.d(TAG, "Caller VIP status updated: $phoneNumber -> $isVip")
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error updating VIP status", e)
            throw e
        }
    }
    
    // Analytics operations
    suspend fun getTodaysCallStats(): CallStats {
        return try {
            val totalCalls = callDao.getTodaysCallCount()
            val averageDuration = callDao.getTodaysAverageCallDuration() ?: 0.0
            val humanTransfers = callDao.getTodaysHumanTransferCount()
            val averageSatisfaction = callDao.getTodaysAverageSatisfaction() ?: 0.0
            val callTypeBreakdown = callDao.getTodaysCallTypeBreakdown()
            val callResultBreakdown = callDao.getTodaysCallResultBreakdown()
            
            CallStats(
                totalCalls = totalCalls,
                averageDuration = averageDuration,
                humanTransfers = humanTransfers,
                averageSatisfaction = averageSatisfaction,
                callTypeBreakdown = callTypeBreakdown,
                callResultBreakdown = callResultBreakdown
            )
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting today's call stats", e)
            CallStats()
        }
    }
    
    // Search operations
    suspend fun searchCallRecords(query: String, limit: Int = 50): List<CallRecord> {
        return try {
            callDao.searchCallRecords(query, limit)
        } catch (e: Exception) {
            Logger.e(TAG, "Error searching call records", e)
            emptyList()
        }
    }
    
    suspend fun searchCallInteractions(query: String, limit: Int = 50): List<CallInteraction> {
        return try {
            callDao.searchCallInteractions(query, limit)
        } catch (e: Exception) {
            Logger.e(TAG, "Error searching call interactions", e)
            emptyList()
        }
    }
    
    companion object {
        private const val TAG = "CallRepository"
    }
}

/**
 * Data class for call statistics
 */
data class CallStats(
    val totalCalls: Int = 0,
    val averageDuration: Double = 0.0,
    val humanTransfers: Int = 0,
    val averageSatisfaction: Double = 0.0,
    val callTypeBreakdown: List<CallTypeCount> = emptyList(),
    val callResultBreakdown: List<CallResultCount> = emptyList()
)