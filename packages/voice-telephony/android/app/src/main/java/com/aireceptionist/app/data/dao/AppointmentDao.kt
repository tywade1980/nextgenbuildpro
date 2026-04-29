package com.aireceptionist.app.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.aireceptionist.app.data.models.Appointment
import com.aireceptionist.app.data.models.AppointmentStatus

@Dao
interface AppointmentDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment)
    
    @Update
    suspend fun updateAppointment(appointment: Appointment)
    
    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getAppointment(id: String): Appointment?
    
    @Query("SELECT * FROM appointments WHERE customerPhone = :phoneNumber ORDER BY appointmentDate ASC")
    suspend fun getAppointmentsByPhone(phoneNumber: String): List<Appointment>
    
    @Query("SELECT * FROM appointments WHERE status = :status ORDER BY appointmentDate ASC")
    suspend fun getAppointmentsByStatus(status: AppointmentStatus): List<Appointment>
    
    @Query("SELECT * FROM appointments WHERE appointmentDate >= :startDate AND appointmentDate <= :endDate ORDER BY appointmentDate ASC")
    suspend fun getAppointmentsByDateRange(startDate: Long, endDate: Long): List<Appointment>
    
    @Query("SELECT * FROM appointments WHERE DATE(appointmentDate/1000, 'unixepoch') = DATE('now') ORDER BY appointmentTime ASC")
    suspend fun getTodaysAppointments(): List<Appointment>
    
    @Query("SELECT * FROM appointments WHERE appointmentDate >= :currentTime AND status IN (:statuses) ORDER BY appointmentDate ASC LIMIT :limit")
    suspend fun getUpcomingAppointments(
        currentTime: Long,
        statuses: List<AppointmentStatus> = listOf(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED),
        limit: Int = 50
    ): List<Appointment>
    
    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointment(id: String)
    
    @Query("SELECT * FROM appointments ORDER BY appointmentDate DESC")
    fun getAllAppointmentsLiveData(): LiveData<List<Appointment>>
    
    @Query("SELECT * FROM appointments WHERE DATE(appointmentDate/1000, 'unixepoch') = DATE('now') ORDER BY appointmentTime ASC")
    fun getTodaysAppointmentsLiveData(): LiveData<List<Appointment>>
    
    // Search functionality
    @Query("""
        SELECT * FROM appointments 
        WHERE customerName LIKE '%' || :query || '%' 
        OR customerPhone LIKE '%' || :query || '%'
        OR serviceType LIKE '%' || :query || '%'
        ORDER BY appointmentDate DESC
        LIMIT :limit
    """)
    suspend fun searchAppointments(query: String, limit: Int = 50): List<Appointment>
}