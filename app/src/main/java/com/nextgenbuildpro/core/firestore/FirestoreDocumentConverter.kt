package com.nextgenbuildpro.core.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

/**
 * Utility class for converting between Firestore documents and app models
 */
object FirestoreDocumentConverter {
    
    /**
     * Get a string value from a document
     * @param document The document to get the value from
     * @param field The field name
     * @param defaultValue The default value to return if the field is not found or is null
     * @return The string value or the default value
     */
    fun getString(document: DocumentSnapshot, field: String, defaultValue: String = ""): String {
        return document.getString(field) ?: defaultValue
    }
    
    /**
     * Get a long value from a document
     * @param document The document to get the value from
     * @param field The field name
     * @param defaultValue The default value to return if the field is not found or is null
     * @return The long value or the default value
     */
    fun getLong(document: DocumentSnapshot, field: String, defaultValue: Long = 0): Long {
        return document.getLong(field) ?: defaultValue
    }
    
    /**
     * Get an int value from a document
     * @param document The document to get the value from
     * @param field The field name
     * @param defaultValue The default value to return if the field is not found or is null
     * @return The int value or the default value
     */
    fun getInt(document: DocumentSnapshot, field: String, defaultValue: Int = 0): Int {
        return document.getLong(field)?.toInt() ?: defaultValue
    }
    
    /**
     * Get a double value from a document
     * @param document The document to get the value from
     * @param field The field name
     * @param defaultValue The default value to return if the field is not found or is null
     * @return The double value or the default value
     */
    fun getDouble(document: DocumentSnapshot, field: String, defaultValue: Double = 0.0): Double {
        return document.getDouble(field) ?: defaultValue
    }
    
    /**
     * Get a boolean value from a document
     * @param document The document to get the value from
     * @param field The field name
     * @param defaultValue The default value to return if the field is not found or is null
     * @return The boolean value or the default value
     */
    fun getBoolean(document: DocumentSnapshot, field: String, defaultValue: Boolean = false): Boolean {
        return document.getBoolean(field) ?: defaultValue
    }
    
    /**
     * Get a date value from a document
     * @param document The document to get the value from
     * @param field The field name
     * @param defaultValue The default value to return if the field is not found or is null
     * @return The date value or the default value
     */
    fun getDate(document: DocumentSnapshot, field: String, defaultValue: Date = Date()): Date {
        val timestamp = document.getTimestamp(field)
        return timestamp?.toDate() ?: defaultValue
    }
    
    /**
     * Get a list of strings from a document
     * @param document The document to get the value from
     * @param field The field name
     * @param defaultValue The default value to return if the field is not found or is null
     * @return The list of strings or the default value
     */
    fun getStringList(document: DocumentSnapshot, field: String, defaultValue: List<String> = emptyList()): List<String> {
        @Suppress("UNCHECKED_CAST")
        return document.get(field) as? List<String> ?: defaultValue
    }
    
    /**
     * Get a map from a document
     * @param document The document to get the value from
     * @param field The field name
     * @param defaultValue The default value to return if the field is not found or is null
     * @return The map or the default value
     */
    fun getMap(document: DocumentSnapshot, field: String, defaultValue: Map<String, Any> = emptyMap()): Map<String, Any> {
        @Suppress("UNCHECKED_CAST")
        return document.get(field) as? Map<String, Any> ?: defaultValue
    }
    
    /**
     * Convert a Date to a Firestore Timestamp
     * @param date The date to convert
     * @return The Firestore Timestamp
     */
    fun dateToTimestamp(date: Date): Timestamp {
        return Timestamp(date)
    }
    
    /**
     * Convert a Firestore Timestamp to a Date
     * @param timestamp The timestamp to convert
     * @return The Date
     */
    fun timestampToDate(timestamp: Timestamp): Date {
        return timestamp.toDate()
    }
}