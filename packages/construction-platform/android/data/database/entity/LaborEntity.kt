package com.constructionmanager.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.constructionmanager.domain.model.TradeType
import com.constructionmanager.domain.model.SkillLevel
import com.constructionmanager.domain.model.LaborEntryStatus
import com.constructionmanager.domain.model.ConstructionPhase

@Entity(tableName = "labor_categories")
data class LaborCategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val tradeType: TradeType,
    val skillLevel: SkillLevel,
    val hourlyRate: String, // BigDecimal as string
    val overtimeRate: String, // BigDecimal as string
    val regionalRates: String, // JSON string of Map<String, BigDecimal>
    val certifications: String, // JSON string of List<String>
    val unionRate: Boolean = false,
    val description: String,
    val isActive: Boolean = true
)

@Entity(tableName = "workers")
data class WorkerEntity(
    @PrimaryKey
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val tradeTypes: String, // JSON string of List<TradeType>
    val skillLevel: SkillLevel,
    val certifications: String, // JSON string of List<Certification>
    val hourlyRate: String, // BigDecimal as string
    val isActive: Boolean = true,
    val hireDate: String, // ISO date string
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val emergencyContactRelationship: String? = null,
    val notes: String = ""
)

@Entity(tableName = "labor_entries")
data class LaborEntryEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val workerId: String,
    val workerName: String,
    val laborCategoryId: String,
    val date: String, // ISO date string
    val startTime: String, // ISO time string
    val endTime: String, // ISO time string
    val breakDuration: Double = 0.0, // hours
    val regularHours: Double,
    val overtimeHours: Double = 0.0,
    val hourlyRate: String, // BigDecimal as string
    val overtimeRate: String, // BigDecimal as string
    val totalCost: String, // BigDecimal as string
    val taskDescription: String,
    val phase: ConstructionPhase,
    val notes: String = "",
    val approvedBy: String? = null,
    val status: LaborEntryStatus = LaborEntryStatus.PENDING
)