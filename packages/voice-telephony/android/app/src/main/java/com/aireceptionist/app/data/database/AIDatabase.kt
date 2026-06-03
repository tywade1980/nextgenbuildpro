package com.aireceptionist.app.data.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aireceptionist.app.data.dao.*
import com.aireceptionist.app.data.models.*

/**
 * Main Room database class for the AI Receptionist app
 */
@Database(
    entities = [
        CallContext::class,
        CallRecord::class,
        CallInteraction::class,
        CallerHistoryEntity::class,
        AgentMetrics::class,
        KnowledgeBaseEntry::class,
        FAQEntry::class,
        Appointment::class,
        TrainingData::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AIDatabase : RoomDatabase() {
    
    // DAOs
    abstract fun callDao(): CallDao
    abstract fun callerHistoryDao(): CallerHistoryDao
    abstract fun agentMetricsDao(): AgentMetricsDao
    abstract fun knowledgeBaseDao(): KnowledgeBaseDao
    abstract fun faqDao(): FAQDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun trainingDataDao(): TrainingDataDao
    
    companion object {
        const val DATABASE_NAME = "ai_receptionist_db"
        
        // Migration from version 1 to 2 (example for future use)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns or tables here when needed
                // database.execSQL("ALTER TABLE call_records ADD COLUMN new_field TEXT")
            }
        }
    }
}

/**
 * Type converters for Room database
 */
class Converters {
    
    @TypeConverter
    fun fromCallType(value: CallType): String = value.name
    
    @TypeConverter
    fun toCallType(value: String): CallType = CallType.valueOf(value)
    
    @TypeConverter
    fun fromCallResult(value: CallResult): String = value.name
    
    @TypeConverter
    fun toCallResult(value: String): CallResult = CallResult.valueOf(value)
    
    @TypeConverter
    fun fromSpeakerType(value: SpeakerType): String = value.name
    
    @TypeConverter
    fun toSpeakerType(value: String): SpeakerType = SpeakerType.valueOf(value)
    
    @TypeConverter
    fun fromAppointmentStatus(value: AppointmentStatus): String = value.name
    
    @TypeConverter
    fun toAppointmentStatus(value: String): AppointmentStatus = AppointmentStatus.valueOf(value)
}