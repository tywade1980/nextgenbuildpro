package com.constructionmanager.domain.model

import java.math.BigDecimal

data class LaborCategory(
    val id: String,
    val name: String,
    val tradeType: TradeType,
    val skillLevel: SkillLevel,
    val hourlyRate: BigDecimal,
    val overtimeRate: BigDecimal,
    val regionalRates: Map<String, BigDecimal> = emptyMap(),
    val certifications: List<String> = emptyList(),
    val unionRate: Boolean = false,
    val description: String,
    val isActive: Boolean = true
)

enum class TradeType {
    GENERAL_LABOR,
    CARPENTER,
    ELECTRICIAN,
    PLUMBER,
    HVAC_TECHNICIAN,
    ROOFER,
    CONCRETE_FINISHER,
    DRYWALL_INSTALLER,
    PAINTER,
    FLOORING_INSTALLER,
    TILE_SETTER,
    GLAZIER,
    INSULATION_INSTALLER,
    LANDSCAPER,
    EXCAVATOR_OPERATOR,
    CRANE_OPERATOR,
    WELDER,
    MASON,
    PROJECT_MANAGER,
    FOREMAN,
    SAFETY_INSPECTOR,
    ARCHITECT,
    ENGINEER
}

enum class SkillLevel {
    APPRENTICE,
    JOURNEYMAN,
    MASTER,
    SPECIALIST,
    SUPERVISOR
}

data class LaborEntry(
    val id: String,
    val projectId: String,
    val workerId: String,
    val workerName: String,
    val laborCategoryId: String,
    val laborCategory: LaborCategory,
    val date: kotlinx.datetime.LocalDate,
    val startTime: kotlinx.datetime.LocalTime,
    val endTime: kotlinx.datetime.LocalTime,
    val breakDuration: Double = 0.0, // hours
    val regularHours: Double,
    val overtimeHours: Double = 0.0,
    val hourlyRate: BigDecimal,
    val overtimeRate: BigDecimal,
    val totalCost: BigDecimal,
    val taskDescription: String,
    val phase: ConstructionPhase,
    val notes: String = "",
    val approvedBy: String? = null,
    val status: LaborEntryStatus = LaborEntryStatus.PENDING
)

enum class LaborEntryStatus {
    PENDING,
    APPROVED,
    REJECTED,
    PAID
}

data class Worker(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val tradeTypes: List<TradeType>,
    val skillLevel: SkillLevel,
    val certifications: List<Certification>,
    val hourlyRate: BigDecimal,
    val isActive: Boolean = true,
    val hireDate: kotlinx.datetime.LocalDate,
    val emergencyContact: EmergencyContact? = null,
    val notes: String = ""
)

data class Certification(
    val name: String,
    val issuingOrganization: String,
    val issueDate: kotlinx.datetime.LocalDate,
    val expirationDate: kotlinx.datetime.LocalDate?,
    val certificationNumber: String
)

data class EmergencyContact(
    val name: String,
    val relationship: String,
    val phone: String,
    val email: String? = null
)