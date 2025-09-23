package com.nextgenbuildpro.core.services

import android.util.Log
import android.util.Patterns
import java.util.regex.Pattern

/**
 * Data validation and conflict resolution service for auto-fill system
 */
class DataValidationService {
    
    /**
     * Validate auto-fill suggestions and resolve conflicts
     */
    fun validateAndResolveSuggestions(
        fieldType: FormFieldType,
        suggestions: List<AutoFillSuggestion>
    ): List<AutoFillSuggestion> {
        
        // Filter out invalid suggestions
        val validSuggestions = suggestions.filter { suggestion ->
            isValidForFieldType(fieldType, suggestion.value)
        }
        
        // Resolve conflicts between suggestions
        val resolvedSuggestions = resolveConflicts(fieldType, validSuggestions)
        
        // Sort by confidence and relevance, then limit results
        return resolvedSuggestions
            .sortedByDescending { it.confidence * it.relevance }
            .take(MAX_SUGGESTIONS)
    }
    
    /**
     * Validate if a value is appropriate for a specific field type
     */
    fun isValidForFieldType(fieldType: FormFieldType, value: Any?): Boolean {
        if (value == null) return false
        
        val stringValue = value.toString().trim()
        if (stringValue.isEmpty()) return false
        
        return when (fieldType) {
            FormFieldType.EMAIL, FormFieldType.CLIENT_EMAIL, FormFieldType.CONTRACTOR_EMAIL -> {
                isValidEmail(stringValue)
            }
            FormFieldType.PHONE, FormFieldType.CLIENT_PHONE, FormFieldType.CONTRACTOR_PHONE -> {
                isValidPhone(stringValue)
            }
            FormFieldType.NAME, FormFieldType.CLIENT_NAME, FormFieldType.CONTRACTOR_NAME -> {
                isValidName(stringValue)
            }
            FormFieldType.ADDRESS, FormFieldType.CLIENT_ADDRESS -> {
                isValidAddress(stringValue)
            }
            FormFieldType.BUDGET -> {
                isValidBudget(stringValue)
            }
            FormFieldType.DATE -> {
                isValidDate(stringValue)
            }
            FormFieldType.TIME -> {
                isValidTime(stringValue)
            }
            FormFieldType.PRIORITY -> {
                isValidPriority(stringValue)
            }
            FormFieldType.URGENCY -> {
                isValidUrgency(stringValue)
            }
            else -> {
                // For other field types, basic non-empty validation
                stringValue.length >= MIN_FIELD_LENGTH && stringValue.length <= MAX_FIELD_LENGTH
            }
        }
    }
    
    /**
     * Resolve conflicts between multiple suggestions
     */
    private fun resolveConflicts(
        fieldType: FormFieldType,
        suggestions: List<AutoFillSuggestion>
    ): List<AutoFillSuggestion> {
        
        if (suggestions.size <= 1) return suggestions
        
        // Group suggestions by similar values
        val groupedSuggestions = groupSimilarSuggestions(fieldType, suggestions)
        
        // Merge similar suggestions
        val mergedSuggestions = groupedSuggestions.map { group ->
            if (group.size == 1) {
                group.first()
            } else {
                mergeSimilarSuggestions(fieldType, group)
            }
        }
        
        // Remove duplicates based on normalized values
        return removeDuplicates(fieldType, mergedSuggestions)
    }
    
    /**
     * Group suggestions that are similar enough to be considered duplicates
     */
    private fun groupSimilarSuggestions(
        fieldType: FormFieldType,
        suggestions: List<AutoFillSuggestion>
    ): List<List<AutoFillSuggestion>> {
        
        val groups = mutableListOf<MutableList<AutoFillSuggestion>>()
        
        for (suggestion in suggestions) {
            var foundGroup = false
            
            for (group in groups) {
                if (areSimilar(fieldType, suggestion, group.first())) {
                    group.add(suggestion)
                    foundGroup = true
                    break
                }
            }
            
            if (!foundGroup) {
                groups.add(mutableListOf(suggestion))
            }
        }
        
        return groups
    }
    
    /**
     * Check if two suggestions are similar enough to be considered duplicates
     */
    private fun areSimilar(
        fieldType: FormFieldType,
        suggestion1: AutoFillSuggestion,
        suggestion2: AutoFillSuggestion
    ): Boolean {
        
        val value1 = normalizeValue(fieldType, suggestion1.value.toString())
        val value2 = normalizeValue(fieldType, suggestion2.value.toString())
        
        return when (fieldType) {
            FormFieldType.EMAIL, FormFieldType.CLIENT_EMAIL, FormFieldType.CONTRACTOR_EMAIL -> {
                value1.equals(value2, ignoreCase = true)
            }
            FormFieldType.PHONE, FormFieldType.CLIENT_PHONE, FormFieldType.CONTRACTOR_PHONE -> {
                extractPhoneDigits(value1) == extractPhoneDigits(value2)
            }
            FormFieldType.NAME, FormFieldType.CLIENT_NAME, FormFieldType.CONTRACTOR_NAME -> {
                calculateNameSimilarity(value1, value2) > NAME_SIMILARITY_THRESHOLD
            }
            FormFieldType.ADDRESS, FormFieldType.CLIENT_ADDRESS -> {
                calculateAddressSimilarity(value1, value2) > ADDRESS_SIMILARITY_THRESHOLD
            }
            else -> {
                value1.equals(value2, ignoreCase = true)
            }
        }
    }
    
    /**
     * Merge similar suggestions into a single suggestion with combined confidence
     */
    private fun mergeSimilarSuggestions(
        fieldType: FormFieldType,
        suggestions: List<AutoFillSuggestion>
    ): AutoFillSuggestion {
        
        // Use the highest quality suggestion as the base
        val baseSuggestion = suggestions.maxByOrNull { it.confidence } ?: suggestions.first()
        
        // Calculate combined confidence and relevance
        val combinedConfidence = suggestions.map { it.confidence }.average()
        val combinedRelevance = suggestions.map { it.relevance }.average()
        
        // Combine sources
        val sources = suggestions.map { it.source }.distinct().joinToString(", ")
        
        // Merge metadata
        val combinedMetadata = mutableMapOf<String, Any>()
        suggestions.forEach { suggestion ->
            combinedMetadata.putAll(suggestion.metadata)
        }
        combinedMetadata["merged_from"] = suggestions.size
        combinedMetadata["original_sources"] = suggestions.map { it.source }
        
        return AutoFillSuggestion(
            value = baseSuggestion.value,
            displayText = baseSuggestion.displayText,
            confidence = combinedConfidence,
            relevance = combinedRelevance,
            source = sources,
            metadata = combinedMetadata
        )
    }
    
    /**
     * Remove duplicate suggestions based on normalized values
     */
    private fun removeDuplicates(
        fieldType: FormFieldType,
        suggestions: List<AutoFillSuggestion>
    ): List<AutoFillSuggestion> {
        
        val seen = mutableSetOf<String>()
        return suggestions.filter { suggestion ->
            val normalizedValue = normalizeValue(fieldType, suggestion.value.toString())
            seen.add(normalizedValue)
        }
    }
    
    /**
     * Normalize a value for comparison purposes
     */
    private fun normalizeValue(fieldType: FormFieldType, value: String): String {
        return when (fieldType) {
            FormFieldType.EMAIL, FormFieldType.CLIENT_EMAIL, FormFieldType.CONTRACTOR_EMAIL -> {
                value.lowercase().trim()
            }
            FormFieldType.PHONE, FormFieldType.CLIENT_PHONE, FormFieldType.CONTRACTOR_PHONE -> {
                extractPhoneDigits(value)
            }
            FormFieldType.NAME, FormFieldType.CLIENT_NAME, FormFieldType.CONTRACTOR_NAME -> {
                value.lowercase().replace(Regex("\\s+"), " ").trim()
            }
            FormFieldType.ADDRESS, FormFieldType.CLIENT_ADDRESS -> {
                value.lowercase()
                    .replace(Regex("\\s+"), " ")
                    .replace(Regex("[,.]"), "")
                    .trim()
            }
            else -> {
                value.lowercase().trim()
            }
        }
    }
    
    // Validation methods for specific field types
    
    private fun isValidEmail(email: String): Boolean {
        return email.length <= MAX_EMAIL_LENGTH && 
               Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidPhone(phone: String): Boolean {
        val digits = extractPhoneDigits(phone)
        return digits.length >= MIN_PHONE_DIGITS && digits.length <= MAX_PHONE_DIGITS
    }
    
    private fun isValidName(name: String): Boolean {
        return name.length >= MIN_NAME_LENGTH && 
               name.length <= MAX_NAME_LENGTH &&
               name.matches(Regex("^[a-zA-Z\\s'-]+$"))
    }
    
    private fun isValidAddress(address: String): Boolean {
        return address.length >= MIN_ADDRESS_LENGTH && 
               address.length <= MAX_ADDRESS_LENGTH
    }
    
    private fun isValidBudget(budget: String): Boolean {
        return try {
            val numericValue = budget.replace(Regex("[^\\d.]"), "").toDoubleOrNull()
            numericValue != null && numericValue >= 0 && numericValue <= MAX_BUDGET
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isValidDate(date: String): Boolean {
        return try {
            // Support various date formats
            val dateFormats = listOf(
                "yyyy-MM-dd", "MM/dd/yyyy", "dd/MM/yyyy", 
                "yyyy/MM/dd", "MMM dd, yyyy", "dd MMM yyyy"
            )
            
            dateFormats.any { format ->
                try {
                    java.time.LocalDate.parse(date, java.time.format.DateTimeFormatter.ofPattern(format))
                    true
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isValidTime(time: String): Boolean {
        return try {
            val timeFormats = listOf("HH:mm", "h:mm a", "HH:mm:ss")
            
            timeFormats.any { format ->
                try {
                    java.time.LocalTime.parse(time, java.time.format.DateTimeFormatter.ofPattern(format))
                    true
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isValidPriority(priority: String): Boolean {
        val validPriorities = setOf("low", "medium", "high", "urgent", "critical")
        return validPriorities.contains(priority.lowercase())
    }
    
    private fun isValidUrgency(urgency: String): Boolean {
        val validUrgencies = setOf("low", "medium", "high", "urgent")
        return validUrgencies.contains(urgency.lowercase())
    }
    
    // Helper methods
    
    private fun extractPhoneDigits(phone: String): String {
        return phone.replace(Regex("[^0-9]"), "")
    }
    
    private fun calculateNameSimilarity(name1: String, name2: String): Double {
        // Simple similarity calculation based on common words
        val words1 = name1.split(Regex("\\s+")).toSet()
        val words2 = name2.split(Regex("\\s+")).toSet()
        
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        
        return if (union == 0) 0.0 else intersection.toDouble() / union
    }
    
    private fun calculateAddressSimilarity(address1: String, address2: String): Double {
        // Simple address similarity based on common tokens
        val tokens1 = address1.split(Regex("[\\s,]+")).filter { it.isNotEmpty() }.toSet()
        val tokens2 = address2.split(Regex("[\\s,]+")).filter { it.isNotEmpty() }.toSet()
        
        val intersection = tokens1.intersect(tokens2).size
        val union = tokens1.union(tokens2).size
        
        return if (union == 0) 0.0 else intersection.toDouble() / union
    }
    
    companion object {
        private const val TAG = "DataValidationService"
        
        // Validation constants
        private const val MIN_FIELD_LENGTH = 1
        private const val MAX_FIELD_LENGTH = 500
        private const val MAX_EMAIL_LENGTH = 254
        private const val MIN_PHONE_DIGITS = 7
        private const val MAX_PHONE_DIGITS = 15
        private const val MIN_NAME_LENGTH = 2
        private const val MAX_NAME_LENGTH = 100
        private const val MIN_ADDRESS_LENGTH = 5
        private const val MAX_ADDRESS_LENGTH = 300
        private const val MAX_BUDGET = 10_000_000.0
        
        // Similarity thresholds
        private const val NAME_SIMILARITY_THRESHOLD = 0.7
        private const val ADDRESS_SIMILARITY_THRESHOLD = 0.6
        
        // Other constants
        private const val MAX_SUGGESTIONS = 5
    }
}

/**
 * Conflict resolution strategies for different types of data conflicts
 */
enum class ConflictResolutionStrategy {
    /**
     * Keep the suggestion with the highest confidence score
     */
    HIGHEST_CONFIDENCE,
    
    /**
     * Keep the suggestion from the most relevant data source
     */
    HIGHEST_RELEVANCE,
    
    /**
     * Merge similar suggestions into a single suggestion
     */
    MERGE_SIMILAR,
    
    /**
     * Keep the most recent suggestion
     */
    MOST_RECENT,
    
    /**
     * Keep all suggestions and let user choose
     */
    USER_CHOICE
}

/**
 * Result of data validation operation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val suggestedCorrection: String? = null,
    val validationDetails: Map<String, Any> = emptyMap()
)

/**
 * Conflict detection and resolution result
 */
data class ConflictResolutionResult(
    val hasConflicts: Boolean,
    val resolvedSuggestions: List<AutoFillSuggestion>,
    val conflictDetails: List<ConflictDetail> = emptyList()
)

/**
 * Details about a detected conflict
 */
data class ConflictDetail(
    val conflictType: ConflictType,
    val conflictingSuggestions: List<AutoFillSuggestion>,
    val resolutionStrategy: ConflictResolutionStrategy,
    val resolvedSuggestion: AutoFillSuggestion
)

/**
 * Types of conflicts that can occur in auto-fill suggestions
 */
enum class ConflictType {
    DUPLICATE_VALUES,
    SIMILAR_VALUES,
    CONTRADICTORY_VALUES,
    FORMAT_MISMATCH,
    SOURCE_RELIABILITY_CONFLICT
}