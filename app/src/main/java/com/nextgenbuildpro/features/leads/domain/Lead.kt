package com.nextgenbuildpro.features.leads.domain

import java.util.Date

/**
 * Data class representing a lead
 */
data class Lead(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val status: LeadStatus = LeadStatus.NEW,
    val createdAt: Date = Date(),
    val updatedAt: Date? = null,
    val photoUrls: List<String> = emptyList(),
    val source: String = "",
    val assignedTo: String = ""
)