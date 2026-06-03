package com.aireceptionist.app.data.repository

import com.aireceptionist.app.ai.agents.impl.BookingResult
import com.aireceptionist.app.ai.agents.impl.CancellationResult
import com.aireceptionist.app.data.dao.AppointmentDao
import com.aireceptionist.app.data.models.Appointment
import com.aireceptionist.app.data.models.AppointmentStatus
import com.aireceptionist.app.utils.Logger
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for appointment scheduling operations
 */
@Singleton
class AppointmentRepository @Inject constructor(
    private val appointmentDao: AppointmentDao
) {
    
    private var isInitialized = false
    
    suspend fun initialize() {
        if (isInitialized) return
        
        try {
            Logger.i(TAG, "Initializing Appointment Repository")
            isInitialized = true
            Logger.i(TAG, "Appointment Repository initialized")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize Appointment Repository", e)
            throw e
        }
    }
    
    suspend fun bookAppointment(details: com.aireceptionist.app.ai.agents.impl.AppointmentDetails): BookingResult {
        return try {
            // Check if the requested time slot is available
            val requestedDate = details.date?.time ?: return BookingResult(
                isSuccess = false,
                reason = "Invalid date provided"
            )
            
            val isSlotAvailable = checkAvailability(requestedDate, details.time)
            
            if (isSlotAvailable) {
                // Create and insert appointment
                val appointment = Appointment(
                    id = generateAppointmentId(),
                    customerName = details.customerName ?: "Unknown",
                    customerPhone = details.customerPhone ?: "",
                    appointmentDate = requestedDate,
                    appointmentTime = details.time ?: "",
                    serviceType = details.serviceType ?: "General",
                    status = AppointmentStatus.SCHEDULED,
                    notes = details.notes,
                    createdBy = "ai"
                )
                
                appointmentDao.insertAppointment(appointment)
                Logger.i(TAG, "Appointment booked successfully: ${appointment.id}")
                
                BookingResult(
                    isSuccess = true,
                    appointmentId = appointment.id
                )
            } else {
                // Get alternative time slots
                val alternatives = getAlternativeSlots(requestedDate)
                
                BookingResult(
                    isSuccess = false,
                    reason = "Requested time slot is not available",
                    alternativeSlots = alternatives
                )
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error booking appointment", e)
            BookingResult(
                isSuccess = false,
                reason = "System error: ${e.message}"
            )
        }
    }
    
    private suspend fun checkAvailability(date: Long, time: String?): Boolean {
        // In a real implementation, this would check against existing appointments
        // and business rules (working hours, holidays, etc.)
        
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        
        // Check if it's a weekday
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return false
        }
        
        // Check business hours (9 AM to 5 PM)
        time?.let { timeStr ->
            val hour = parseTimeHour(timeStr)
            if (hour < 9 || hour >= 17) {
                return false
            }
        }
        
        // Check for existing appointments at the same time
        val startOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val endOfDay = calendar.apply {
            add(Calendar.DAY_OF_YEAR, 1)
            add(Calendar.MILLISECOND, -1)
        }.timeInMillis
        
        val existingAppointments = appointmentDao.getAppointmentsByDateRange(startOfDay, endOfDay)
        
        // Check for time conflicts (simple implementation)
        return !existingAppointments.any { it.appointmentTime == time }
    }
    
    private fun parseTimeHour(timeStr: String): Int {
        return try {
            val timeParts = timeStr.split(":")
            var hour = timeParts[0].toInt()
            
            // Handle AM/PM
            if (timeStr.toLowerCase().contains("pm") && hour != 12) {
                hour += 12
            } else if (timeStr.toLowerCase().contains("am") && hour == 12) {
                hour = 0
            }
            
            hour
        } catch (e: Exception) {
            9 // Default to 9 AM if parsing fails
        }
    }
    
    private suspend fun getAlternativeSlots(requestedDate: Long): List<String> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = requestedDate
        
        val alternatives = mutableListOf<String>()
        
        // Generate next 5 business days with available slots
        var daysAdded = 0
        while (alternatives.size < 5 && daysAdded < 10) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            
            // Skip weekends
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                val dateStr = "${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}"
                
                // Add available time slots for this day
                val availableTimes = listOf("9:00 AM", "10:00 AM", "2:00 PM", "3:00 PM", "4:00 PM")
                for (time in availableTimes) {
                    if (checkAvailability(calendar.timeInMillis, time)) {
                        alternatives.add("$dateStr at $time")
                        if (alternatives.size >= 5) break
                    }
                }
            }
            daysAdded++
        }
        
        return alternatives
    }
    
    suspend fun getAppointments(phoneNumber: String): List<com.aireceptionist.app.ai.agents.impl.AppointmentDetails> {
        return try {
            val appointments = appointmentDao.getAppointmentsByPhone(phoneNumber)
            appointments.map { appointment ->
                com.aireceptionist.app.ai.agents.impl.AppointmentDetails(
                    date = Date(appointment.appointmentDate),
                    time = appointment.appointmentTime,
                    serviceType = appointment.serviceType,
                    customerName = appointment.customerName,
                    customerPhone = appointment.customerPhone,
                    notes = appointment.notes
                )
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting appointments for $phoneNumber", e)
            emptyList()
        }
    }
    
    suspend fun cancelAppointment(phoneNumber: String): CancellationResult {
        return try {
            val upcomingAppointments = appointmentDao.getAppointmentsByPhone(phoneNumber)
                .filter { it.status in listOf(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED) }
                .filter { it.appointmentDate > System.currentTimeMillis() }
            
            if (upcomingAppointments.isNotEmpty()) {
                val appointmentToCancel = upcomingAppointments.first()
                val cancelledAppointment = appointmentToCancel.copy(
                    status = AppointmentStatus.CANCELLED,
                    updatedAt = System.currentTimeMillis()
                )
                
                appointmentDao.updateAppointment(cancelledAppointment)
                Logger.i(TAG, "Appointment cancelled: ${appointmentToCancel.id}")
                
                CancellationResult(isSuccess = true)
            } else {
                CancellationResult(
                    isSuccess = false,
                    reason = "No upcoming appointments found"
                )
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error cancelling appointment", e)
            CancellationResult(
                isSuccess = false,
                reason = "System error: ${e.message}"
            )
        }
    }
    
    suspend fun getTodaysAppointments(): List<Appointment> {
        return try {
            appointmentDao.getTodaysAppointments()
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting today's appointments", e)
            emptyList()
        }
    }
    
    suspend fun getUpcomingAppointments(limit: Int = 50): List<Appointment> {
        return try {
            appointmentDao.getUpcomingAppointments(System.currentTimeMillis(), limit = limit)
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting upcoming appointments", e)
            emptyList()
        }
    }
    
    private fun generateAppointmentId(): String {
        return "apt_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    fun isHealthy(): Boolean {
        return isInitialized
    }
    
    suspend fun shutdown() {
        isInitialized = false
        Logger.i(TAG, "Appointment Repository shutdown")
    }
    
    companion object {
        private const val TAG = "AppointmentRepository"
    }
}