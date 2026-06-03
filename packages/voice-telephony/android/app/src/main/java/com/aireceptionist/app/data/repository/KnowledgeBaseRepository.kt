package com.aireceptionist.app.data.repository

import com.aireceptionist.app.ai.agents.impl.KnowledgeResult
import com.aireceptionist.app.data.dao.KnowledgeBaseDao
import com.aireceptionist.app.data.models.KnowledgeBaseEntry
import com.aireceptionist.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for knowledge base operations
 */
@Singleton
class KnowledgeBaseRepository @Inject constructor(
    private val knowledgeBaseDao: KnowledgeBaseDao
) {
    
    private var isInitialized = false
    
    suspend fun initialize() {
        if (isInitialized) return
        
        try {
            Logger.i(TAG, "Initializing Knowledge Base Repository")
            
            // Load default knowledge base entries if database is empty
            val existingEntries = knowledgeBaseDao.getActiveKnowledgeEntries()
            if (existingEntries.isEmpty()) {
                loadDefaultKnowledgeBase()
            }
            
            isInitialized = true
            Logger.i(TAG, "Knowledge Base Repository initialized")
            
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize Knowledge Base Repository", e)
            throw e
        }
    }
    
    suspend fun search(query: String, intent: String? = null): List<KnowledgeResult> = withContext(Dispatchers.IO) {
        try {
            val entries = if (intent != null) {
                // First try to find by intent
                val intentEntries = knowledgeBaseDao.getKnowledgeEntriesByIntent(intent)
                if (intentEntries.isNotEmpty()) {
                    intentEntries
                } else {
                    // Fallback to text search
                    knowledgeBaseDao.searchKnowledgeBase(query)
                }
            } else {
                knowledgeBaseDao.searchKnowledgeBase(query)
            }
            
            // Convert to KnowledgeResult and update usage counts
            entries.map { entry ->
                // Increment usage count asynchronously
                knowledgeBaseDao.incrementUsage(entry.id)
                
                KnowledgeResult(
                    id = entry.id,
                    title = entry.title,
                    content = entry.content,
                    category = entry.category,
                    confidence = calculateRelevanceScore(query, entry),
                    tags = parseJsonArray(entry.tags)
                )
            }.sortedByDescending { it.confidence }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error searching knowledge base", e)
            emptyList()
        }
    }
    
    private fun calculateRelevanceScore(query: String, entry: KnowledgeBaseEntry): Float {
        val queryWords = query.toLowerCase().split("\\s+".toRegex())
        val contentWords = (entry.title + " " + entry.content).toLowerCase().split("\\s+".toRegex())
        val tags = parseJsonArray(entry.tags)
        
        var score = 0.0f
        var matches = 0
        
        for (queryWord in queryWords) {
            // Title matches get higher weight
            if (entry.title.toLowerCase().contains(queryWord)) {
                score += 3.0f
                matches++
            }
            
            // Content matches
            if (entry.content.toLowerCase().contains(queryWord)) {
                score += 1.0f
                matches++
            }
            
            // Tag matches get high weight
            if (tags.any { it.toLowerCase().contains(queryWord) }) {
                score += 2.0f
                matches++
            }
        }
        
        // Normalize by query length and apply usage bonus
        val normalizedScore = if (queryWords.isNotEmpty()) score / queryWords.size else 0.0f
        val usageBonus = minOf(entry.usageCount * 0.1f, 1.0f)
        val priorityBonus = entry.priority * 0.05f
        
        return minOf(normalizedScore + usageBonus + priorityBonus, 1.0f)
    }
    
    suspend fun addKnowledgeEntry(entry: KnowledgeBaseEntry) {
        try {
            knowledgeBaseDao.insertKnowledgeEntry(entry)
            Logger.d(TAG, "Knowledge entry added: ${entry.title}")
        } catch (e: Exception) {
            Logger.e(TAG, "Error adding knowledge entry", e)
            throw e
        }
    }
    
    suspend fun updateKnowledgeEntry(entry: KnowledgeBaseEntry) {
        try {
            knowledgeBaseDao.updateKnowledgeEntry(entry)
            Logger.d(TAG, "Knowledge entry updated: ${entry.title}")
        } catch (e: Exception) {
            Logger.e(TAG, "Error updating knowledge entry", e)
            throw e
        }
    }
    
    suspend fun getKnowledgeEntriesByCategory(category: String): List<KnowledgeBaseEntry> {
        return try {
            knowledgeBaseDao.getKnowledgeEntriesByCategory(category)
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting knowledge entries by category", e)
            emptyList()
        }
    }
    
    suspend fun getCategories(): List<String> {
        return try {
            knowledgeBaseDao.getCategories()
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting categories", e)
            emptyList()
        }
    }
    
    private suspend fun loadDefaultKnowledgeBase() {
        try {
            Logger.i(TAG, "Loading default knowledge base entries")
            
            val defaultEntries = listOf(
                KnowledgeBaseEntry(
                    id = "kb_hours",
                    title = "Business Hours",
                    content = "Our business hours are Monday through Friday, 9 AM to 5 PM EST. We are closed on weekends and major holidays.",
                    category = "general",
                    tags = """["hours", "schedule", "open", "closed", "time"]""",
                    intent = "hours_inquiry",
                    priority = 5
                ),
                KnowledgeBaseEntry(
                    id = "kb_location",
                    title = "Location and Address",
                    content = "We are located at 123 Main Street, City, State 12345. Our building is the blue one with the large sign out front.",
                    category = "general",
                    tags = """["location", "address", "where", "directions"]""",
                    intent = "location_inquiry",
                    priority = 5
                ),
                KnowledgeBaseEntry(
                    id = "kb_contact",
                    title = "Contact Information",
                    content = "You can reach us by phone at (555) 123-4567, by email at contact@company.com, or through our website contact form.",
                    category = "general",
                    tags = """["contact", "phone", "email", "reach"]""",
                    intent = "information_request",
                    priority = 4
                ),
                KnowledgeBaseEntry(
                    id = "kb_services",
                    title = "Our Services",
                    content = "We offer consultation services, project management, and technical support. Each service can be customized to meet your specific needs.",
                    category = "services",
                    tags = """["services", "what", "offer", "do"]""",
                    intent = "service_inquiry",
                    priority = 4
                ),
                KnowledgeBaseEntry(
                    id = "kb_appointments",
                    title = "Scheduling Appointments",
                    content = "To schedule an appointment, you can call us during business hours, use our online booking system, or speak with me now. We typically have availability within 1-2 weeks.",
                    category = "appointments",
                    tags = """["appointment", "schedule", "book", "meeting"]""",
                    intent = "appointment_booking",
                    priority = 6
                ),
                KnowledgeBaseEntry(
                    id = "kb_emergency",
                    title = "Emergency Procedures",
                    content = "For urgent matters outside business hours, please call our emergency line at (555) 999-HELP. For life-threatening emergencies, always call 911 first.",
                    category = "emergency",
                    tags = """["emergency", "urgent", "help", "crisis"]""",
                    intent = "emergency",
                    priority = 10
                )
            )
            
            knowledgeBaseDao.insertKnowledgeEntries(defaultEntries)
            Logger.i(TAG, "Default knowledge base entries loaded: ${defaultEntries.size}")
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error loading default knowledge base", e)
        }
    }
    
    private fun parseJsonArray(jsonString: String): List<String> {
        return try {
            // Simple JSON array parsing - in production, use proper JSON library
            jsonString
                .removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun isHealthy(): Boolean {
        return isInitialized
    }
    
    suspend fun shutdown() {
        isInitialized = false
        Logger.i(TAG, "Knowledge Base Repository shutdown")
    }
    
    companion object {
        private const val TAG = "KnowledgeBaseRepository"
    }
}