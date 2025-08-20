package com.nextgenbuildpro.receptionist.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.nextgenbuildpro.core.firestore.BaseFirestoreRepository
import com.nextgenbuildpro.core.firestore.FirestoreCollectionNames
import com.nextgenbuildpro.features.calendar.models.CalendarEvent
import com.nextgenbuildpro.features.calendar.models.EventType
import java.util.Date

/**
 * Firestore implementation of repository for calendar events
 */
class FirestoreCalendarEventRepository : BaseFirestoreRepository<CalendarEvent, String>() {
    
    companion object {
        private const val TAG = "FirestoreCalendarRepo"
        
        // Field names in Firestore documents
        private const val FIELD_ID = "id"
        private const val FIELD_TITLE = "title"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_START_TIME = "startTime"
        private const val FIELD_END_TIME = "endTime"
        private const val FIELD_LOCATION = "location"
        private const val FIELD_TYPE = "type"
        private const val FIELD_PROJECT_ID = "projectId"
        private const val FIELD_LEAD_ID = "leadId"
    }
    
    /**
     * Get the collection reference for calendar events
     */
    override fun getCollectionReference(): CollectionReference {
        return firestore.collection(FirestoreCollectionNames.CALENDAR_EVENTS)
    }
    
    /**
     * Convert a Firestore document to a CalendarEvent
     */
    override fun fromDocument(document: DocumentSnapshot): CalendarEvent {
        val id = document.getString(FIELD_ID) ?: document.id
        val title = document.getString(FIELD_TITLE) ?: ""
        val description = document.getString(FIELD_DESCRIPTION) ?: ""
        val startTime = (document.getTimestamp(FIELD_START_TIME)?.toDate()) ?: Date()
        val endTime = (document.getTimestamp(FIELD_END_TIME)?.toDate()) ?: Date()
        val location = document.getString(FIELD_LOCATION) ?: ""
        val typeString = document.getString(FIELD_TYPE) ?: EventType.MEETING.name
        val type = try {
            EventType.valueOf(typeString)
        } catch (e: Exception) {
            EventType.MEETING
        }
        val projectId = document.getString(FIELD_PROJECT_ID)
        val leadId = document.getString(FIELD_LEAD_ID)
        
        return CalendarEvent(
            id = id,
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            location = location,
            type = type,
            projectId = projectId,
            leadId = leadId
        )
    }
    
    /**
     * Convert a CalendarEvent to a map for Firestore
     */
    override fun toMap(item: CalendarEvent): Map<String, Any?> {
        return mapOf(
            FIELD_ID to item.id,
            FIELD_TITLE to item.title,
            FIELD_DESCRIPTION to item.description,
            FIELD_START_TIME to Timestamp(item.startTime),
            FIELD_END_TIME to Timestamp(item.endTime),
            FIELD_LOCATION to item.location,
            FIELD_TYPE to item.type.name,
            FIELD_PROJECT_ID to item.projectId,
            FIELD_LEAD_ID to item.leadId
        )
    }
    
    /**
     * Convert a string to an ID
     */
    override fun stringToId(idString: String): String {
        return idString
    }
}