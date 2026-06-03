package com.aireceptionist.app.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.aireceptionist.app.data.models.KnowledgeBaseEntry

@Dao
interface KnowledgeBaseDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledgeEntry(entry: KnowledgeBaseEntry)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledgeEntries(entries: List<KnowledgeBaseEntry>)
    
    @Update
    suspend fun updateKnowledgeEntry(entry: KnowledgeBaseEntry)
    
    @Query("SELECT * FROM knowledge_base WHERE id = :id")
    suspend fun getKnowledgeEntry(id: String): KnowledgeBaseEntry?
    
    @Query("SELECT * FROM knowledge_base WHERE isActive = 1 ORDER BY priority DESC, usageCount DESC")
    suspend fun getActiveKnowledgeEntries(): List<KnowledgeBaseEntry>
    
    @Query("SELECT * FROM knowledge_base WHERE category = :category AND isActive = 1 ORDER BY priority DESC")
    suspend fun getKnowledgeEntriesByCategory(category: String): List<KnowledgeBaseEntry>
    
    @Query("SELECT * FROM knowledge_base WHERE intent = :intent AND isActive = 1 ORDER BY priority DESC")
    suspend fun getKnowledgeEntriesByIntent(intent: String): List<KnowledgeBaseEntry>
    
    @Query("""
        SELECT * FROM knowledge_base 
        WHERE isActive = 1 AND (
            title LIKE '%' || :query || '%' OR 
            content LIKE '%' || :query || '%' OR
            tags LIKE '%' || :query || '%'
        )
        ORDER BY priority DESC, usageCount DESC
        LIMIT :limit
    """)
    suspend fun searchKnowledgeBase(query: String, limit: Int = 10): List<KnowledgeBaseEntry>
    
    @Query("SELECT DISTINCT category FROM knowledge_base WHERE isActive = 1")
    suspend fun getCategories(): List<String>
    
    @Query("DELETE FROM knowledge_base WHERE id = :id")
    suspend fun deleteKnowledgeEntry(id: String)
    
    @Transaction
    suspend fun incrementUsage(id: String) {
        val entry = getKnowledgeEntry(id)
        entry?.let {
            updateKnowledgeEntry(it.copy(
                usageCount = it.usageCount + 1,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }
}