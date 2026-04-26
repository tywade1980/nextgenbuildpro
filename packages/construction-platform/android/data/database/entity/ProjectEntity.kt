package com.constructionmanager.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.constructionmanager.domain.model.ConstructionPhase
import com.constructionmanager.domain.model.ProjectStatus
import com.constructionmanager.domain.model.ProjectType

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
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
    val startDate: String, // ISO date string
    val estimatedEndDate: String,
    val actualEndDate: String? = null,
    val totalBudget: String, // BigDecimal as string
    val currentCost: String = "0",
    val status: ProjectStatus,
    val notes: String = "",
    val createdAt: String, // ISO datetime string
    val updatedAt: String
)