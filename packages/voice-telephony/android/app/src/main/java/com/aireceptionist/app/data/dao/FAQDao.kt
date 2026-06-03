package com.aireceptionist.app.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.aireceptionist.app.data.models.FAQEntry

@Dao
interface FAQDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFAQEntry(entry: FAQEntry)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFAQEntries(entries: List<FAQEntry>)
    
    @Update
    suspend fun updateFAQEntry(entry: FAQEntry)
    
    @Query("SELECT * FROM faq_entries WHERE id = :id")
    suspend fun getFAQEntry(id: String): FAQEntry?
    
    @Query("SELECT * FROM faq_entries WHERE isActive = 1 ORDER BY priority DESC, viewCount DESC")
    suspend fun getActiveFAQEntries(): List<FAQEntry>
    
    @Query("SELECT * FROM faq_entries WHERE category = :category AND isActive = 1 ORDER BY priority DESC")
    suspend fun getFAQEntriesByCategory(category: String): List<FAQEntry>
    
    @Query("""
        SELECT * FROM faq_entries 
        WHERE isActive = 1 AND (
            question LIKE '%' || :query || '%' OR 
            answer LIKE '%' || :query || '%' OR
            keywords LIKE '%' || :query || '%'
        )
        ORDER BY priority DESC, viewCount DESC
        LIMIT :limit
    """)
    suspend fun searchFAQ(query: String, limit: Int = 10): List<FAQEntry>
    
    @Query("SELECT DISTINCT category FROM faq_entries WHERE isActive = 1")
    suspend fun getFAQCategories(): List<String>
    
    @Query("SELECT * FROM faq_entries WHERE isActive = 1 ORDER BY viewCount DESC LIMIT :limit")
    suspend fun getPopularFAQs(limit: Int = 10): List<FAQEntry>
    
    @Query("DELETE FROM faq_entries WHERE id = :id")
    suspend fun deleteFAQEntry(id: String)
    
    @Transaction
    suspend fun incrementViewCount(id: String) {
        val entry = getFAQEntry(id)
        entry?.let {
            updateFAQEntry(it.copy(
                viewCount = it.viewCount + 1,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }
    
    @Transaction
    suspend fun recordFeedback(id: String, isHelpful: Boolean) {
        val entry = getFAQEntry(id)
        entry?.let {
            val updated = if (isHelpful) {
                it.copy(
                    helpfulVotes = it.helpfulVotes + 1,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                it.copy(
                    unhelpfulVotes = it.unhelpfulVotes + 1,
                    updatedAt = System.currentTimeMillis()
                )
            }
            updateFAQEntry(updated)
        }
    }
}