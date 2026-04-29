package com.constructionmanager.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal

data class Project(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val clientName: String,
    val clientEmail: String,
    val clientPhone: String,
    val projectType: ProjectType,
    val currentPhase: ConstructionPhase,
    val startDate: LocalDate,
    val estimatedEndDate: LocalDate,
    val actualEndDate: LocalDate? = null,
    val totalBudget: BigDecimal,
    val currentCost: BigDecimal = BigDecimal.ZERO,
    val status: ProjectStatus,
    val notes: String = "",
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class ProjectType {
    NEW_CONSTRUCTION,
    RENOVATION,
    ADDITION,
    REPAIR,
    COMMERCIAL,
    RESIDENTIAL
}

enum class ProjectStatus {
    PLANNING,
    ACTIVE,
    ON_HOLD,
    COMPLETED,
    CANCELLED
}

enum class ConstructionPhase {
    PRE_CONSTRUCTION,
    SITE_PREPARATION,
    FOUNDATION,
    FRAMING,
    ROOFING,
    SIDING_EXTERIOR,
    MEP_ROUGH_IN,
    INSULATION,
    DRYWALL,
    FLOORING,
    INTERIOR_FINISH,
    FINAL_INSPECTION,
    HANDOVER,
    COMPLETED
}