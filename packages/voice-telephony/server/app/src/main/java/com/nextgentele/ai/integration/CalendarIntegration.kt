package com.nextgentele.ai.integration

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class CalendarIntegration(private val context: Context) {
    
    companion object {
        private const val TAG = "CalendarIntegration"
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }
    
    data class CalendarEvent(
        val id: String,
        val title: String,
        val startTime: Long,
        val endTime: Long,
        val description: String?,
        val location: String?
    )
    
    fun getEvents(date: String?): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        
        try {
            val calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = dateFormat.parse(date) ?: Date()
            }
            
            // Set to beginning of day
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val dayStart = calendar.timeInMillis
            
            // Set to end of day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MILLISECOND, -1)
            val dayEnd = calendar.timeInMillis
            
            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf(
                    CalendarContract.Events._ID,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND,
                    CalendarContract.Events.DESCRIPTION,
                    CalendarContract.Events.EVENT_LOCATION
                ),
                "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?",
                arrayOf(dayStart.toString(), dayEnd.toString()),
                CalendarContract.Events.DTSTART + " ASC"
            )
            
            cursor?.use {
                val idColumn = it.getColumnIndex(CalendarContract.Events._ID)
                val titleColumn = it.getColumnIndex(CalendarContract.Events.TITLE)
                val startColumn = it.getColumnIndex(CalendarContract.Events.DTSTART)
                val endColumn = it.getColumnIndex(CalendarContract.Events.DTEND)
                val descColumn = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
                val locationColumn = it.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)
                
                while (it.moveToNext()) {
                    val event = CalendarEvent(
                        id = it.getString(idColumn) ?: "",
                        title = it.getString(titleColumn) ?: "",
                        startTime = it.getLong(startColumn),
                        endTime = it.getLong(endColumn),
                        description = if (descColumn >= 0) it.getString(descColumn) else null,
                        location = if (locationColumn >= 0) it.getString(locationColumn) else null
                    )
                    events.add(event)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving calendar events", e)
        }
        
        return events
    }
    
    fun addEvent(title: String, datetime: String, durationMinutes: Int): Boolean {
        try {
            val startTime = dateFormat.parse(datetime)?.time ?: return false
            val endTime = startTime + (durationMinutes * 60 * 1000)
            
            val values = ContentValues().apply {
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.CALENDAR_ID, getDefaultCalendarId())
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.DESCRIPTION, "Created by NextGenTele AI")
            }
            
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            return uri != null
            
        } catch (e: Exception) {
            Log.e(TAG, "Error adding calendar event", e)
            return false
        }
    }
    
    fun scheduleCallback(phoneNumber: String, datetime: String, notes: String): Boolean {
        try {
            val title = "Callback: $phoneNumber"
            val description = "AI scheduled callback\nPhone: $phoneNumber\nNotes: $notes"
            
            val startTime = dateFormat.parse(datetime)?.time ?: return false
            val endTime = startTime + (15 * 60 * 1000) // 15 minute callback
            
            val values = ContentValues().apply {
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.CALENDAR_ID, getDefaultCalendarId())
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.DESCRIPTION, description)
            }
            
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            
            if (uri != null) {
                Log.d(TAG, "Callback scheduled for $phoneNumber at $datetime")
                return true
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling callback", e)
        }
        
        return false
    }
    
    fun checkAvailability(datetime: String, durationMinutes: Int): Boolean {
        try {
            val startTime = dateFormat.parse(datetime)?.time ?: return false
            val endTime = startTime + (durationMinutes * 60 * 1000)
            
            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf(CalendarContract.Events._ID),
                "${CalendarContract.Events.DTSTART} < ? AND ${CalendarContract.Events.DTEND} > ?",
                arrayOf(endTime.toString(), startTime.toString()),
                null
            )
            
            cursor?.use {
                val hasConflict = it.count > 0
                return !hasConflict
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking availability", e)
        }
        
        return false
    }
    
    fun getUpcomingEvents(hoursAhead: Int = 24): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        
        try {
            val now = System.currentTimeMillis()
            val future = now + (hoursAhead * 60 * 60 * 1000)
            
            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf(
                    CalendarContract.Events._ID,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND,
                    CalendarContract.Events.DESCRIPTION,
                    CalendarContract.Events.EVENT_LOCATION
                ),
                "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?",
                arrayOf(now.toString(), future.toString()),
                CalendarContract.Events.DTSTART + " ASC"
            )
            
            cursor?.use {
                val idColumn = it.getColumnIndex(CalendarContract.Events._ID)
                val titleColumn = it.getColumnIndex(CalendarContract.Events.TITLE)
                val startColumn = it.getColumnIndex(CalendarContract.Events.DTSTART)
                val endColumn = it.getColumnIndex(CalendarContract.Events.DTEND)
                val descColumn = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
                val locationColumn = it.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)
                
                while (it.moveToNext()) {
                    val event = CalendarEvent(
                        id = it.getString(idColumn) ?: "",
                        title = it.getString(titleColumn) ?: "",
                        startTime = it.getLong(startColumn),
                        endTime = it.getLong(endColumn),
                        description = if (descColumn >= 0) it.getString(descColumn) else null,
                        location = if (locationColumn >= 0) it.getString(locationColumn) else null
                    )
                    events.add(event)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving upcoming events", e)
        }
        
        return events
    }
    
    private fun getDefaultCalendarId(): Long {
        try {
            val cursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                arrayOf(CalendarContract.Calendars._ID),
                "${CalendarContract.Calendars.ACCOUNT_NAME} IS NOT NULL",
                null,
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    val idColumn = it.getColumnIndex(CalendarContract.Calendars._ID)
                    return it.getLong(idColumn)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting default calendar ID", e)
        }
        
        return 1L // Default fallback
    }
    
    fun findNextAvailableSlot(durationMinutes: Int, daysAhead: Int = 7): String? {
        try {
            val calendar = Calendar.getInstance()
            
            for (day in 0 until daysAhead) {
                calendar.add(Calendar.DAY_OF_MONTH, if (day == 0) 0 else 1)
                
                // Check business hours (9 AM to 5 PM)
                for (hour in 9..16) {
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    
                    val timeString = dateFormat.format(calendar.time)
                    if (checkAvailability(timeString, durationMinutes)) {
                        return timeString
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error finding next available slot", e)
        }
        
        return null
    }
}