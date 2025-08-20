package com.nextgenbuildpro.receptionist.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.nextgenbuildpro.core.Repository
import com.nextgenbuildpro.core.firestore.BaseFirestoreRepository
import com.nextgenbuildpro.core.firestore.FirestoreCollectionNames
import com.nextgenbuildpro.features.calendar.models.CalendarEvent
import com.nextgenbuildpro.features.calendar.models.EventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

/**
 * Repository for managing calendar events with Firestore persistence
 */
class CalendarEventRepository : Repository<CalendarEvent> {

    companion object {
        private const val TAG = "CalendarEventRepo"
    }

    // In-memory cache for calendar events
    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    // Firestore repository for persistence
    private val firestoreRepo = FirestoreCalendarEventRepository()

    // Coroutine scope for background operations
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        // Load initial data from Firestore
        coroutineScope.launch {
            try {
                val firestoreEvents = firestoreRepo.getAll().first<List<CalendarEvent>>()
                _events.value = firestoreEvents
                Log.d(TAG, "Loaded ${firestoreEvents.size} events from Firestore")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading events from Firestore", e)
            }
        }
    }

    /**
     * Get all calendar events
     */
    override suspend fun getAll(): List<CalendarEvent> {
        return withContext(Dispatchers.IO) {
            try {
                // Try to refresh from Firestore first
                val firestoreEvents = firestoreRepo.getAll().first<List<CalendarEvent>>()
                if (firestoreEvents.isNotEmpty()) {
                    _events.value = firestoreEvents
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing events from Firestore", e)
            }

            _events.value
        }
    }

    /**
     * Get a calendar event by ID
     */
    override suspend fun getById(id: String): CalendarEvent? {
        return withContext(Dispatchers.IO) {
            try {
                // Try to get from Firestore first
                val firestoreEvent = firestoreRepo.getById(id).first<CalendarEvent?>()
                if (firestoreEvent != null) {
                    // Update the in-memory cache
                    _events.value = _events.value.map { 
                        if (it.id == id) firestoreEvent else it 
                    }
                    return@withContext firestoreEvent
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting event from Firestore", e)
            }

            // Fall back to in-memory cache
            _events.value.find { it.id == id }
        }
    }

    /**
     * Save a new calendar event
     */
    override suspend fun save(item: CalendarEvent): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Save to Firestore first
                firestoreRepo.add(item)

                // Update in-memory cache
                _events.value = _events.value + item

                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving event to Firestore", e)
                false
            }
        }
    }

    /**
     * Update an existing calendar event
     */
    override suspend fun update(item: CalendarEvent): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Update in Firestore first
                firestoreRepo.update(item.id, item)

                // Update in-memory cache
                _events.value = _events.value.map { 
                    if (it.id == item.id) item else it 
                }

                true
            } catch (e: Exception) {
                Log.e(TAG, "Error updating event in Firestore", e)
                false
            }
        }
    }

    /**
     * Delete a calendar event by ID
     */
    override suspend fun delete(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Delete from Firestore first
                firestoreRepo.delete(id)

                // Update in-memory cache
                _events.value = _events.value.filter { it.id != id }

                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting event from Firestore", e)
                false
            }
        }
    }

    /**
     * Save a calendar event (non-suspend version for use from AIReceptionist)
     */
    fun saveEvent(event: CalendarEvent): Boolean {
        // Launch a coroutine to save the event
        coroutineScope.launch {
            save(event)
        }

        // Update in-memory cache immediately
        _events.value = _events.value + event

        return true
    }

    /**
     * Get events for a specific date
     */
    suspend fun getEventsForDate(date: Date): List<CalendarEvent> {
        return _events.value.filter { 
            isSameDay(it.startTime, date)
        }
    }

    /**
     * Get events for a specific lead
     */
    suspend fun getEventsForLead(leadId: String): List<CalendarEvent> {
        return _events.value.filter { it.leadId == leadId }
    }

    /**
     * Get events for a specific project
     */
    suspend fun getEventsForProject(projectId: String): List<CalendarEvent> {
        return _events.value.filter { it.projectId == projectId }
    }

    /**
     * Get upcoming events
     */
    suspend fun getUpcomingEvents(limit: Int = 10): List<CalendarEvent> {
        val now = Date()
        return _events.value
            .filter { it.startTime.after(now) }
            .sortedBy { it.startTime }
            .take(limit)
    }

    /**
     * Check if a time slot is available
     */
    suspend fun isTimeSlotAvailable(startTime: Date, endTime: Date): Boolean {
        return _events.value.none { event ->
            (startTime.before(event.endTime) && endTime.after(event.startTime))
        }
    }

    /**
     * Find available time slots for a given date
     */
    suspend fun findAvailableTimeSlots(date: Date, durationMinutes: Int): List<Date> {
        // This is a simplified implementation
        // In a real app, this would be more complex

        val availableSlots = mutableListOf<Date>()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 9) // Start at 9 AM
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)

        val endOfDay = java.util.Calendar.getInstance()
        endOfDay.time = date
        endOfDay.set(java.util.Calendar.HOUR_OF_DAY, 17) // End at 5 PM
        endOfDay.set(java.util.Calendar.MINUTE, 0)
        endOfDay.set(java.util.Calendar.SECOND, 0)
        endOfDay.set(java.util.Calendar.MILLISECOND, 0)

        while (calendar.time.before(endOfDay.time)) {
            val slotStart = calendar.time

            // Calculate slot end
            calendar.add(java.util.Calendar.MINUTE, durationMinutes)
            val slotEnd = calendar.time

            // Check if this slot is available
            if (isTimeSlotAvailable(slotStart, slotEnd)) {
                availableSlots.add(slotStart)
            }

            // Move to next slot (30-minute increments)
            calendar.time = slotStart
            calendar.add(java.util.Calendar.MINUTE, 30)
        }

        return availableSlots
    }

    /**
     * Helper function to check if two dates are on the same day
     */
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance()
        cal1.time = date1

        val cal2 = java.util.Calendar.getInstance()
        cal2.time = date2

        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH) &&
               cal1.get(java.util.Calendar.DAY_OF_MONTH) == cal2.get(java.util.Calendar.DAY_OF_MONTH)
    }
}
