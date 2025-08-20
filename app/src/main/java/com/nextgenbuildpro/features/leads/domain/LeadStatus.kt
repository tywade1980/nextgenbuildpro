package com.nextgenbuildpro.features.leads.domain

/**
 * Enum representing the status of a lead
 */
enum class LeadStatus {
    NEW,
    CONTACTED,
    QUALIFIED,
    PROPOSAL,
    NEGOTIATION,
    WON,
    LOST,
    INACTIVE
}