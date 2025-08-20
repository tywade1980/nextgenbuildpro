package com.nextgenbuildpro.pm.data.model

/**
 * Data models for the Daily Log feature
 */

/**
 * Represents a daily log entry for a project
 */
data class DailyLog(
    val id: String,
    val projectId: String,
    val leadId: String,
    val date: String,
    val completedTasks: String,
    val hoursWorked: Int,
    val materialsNeeded: String,
    val issues: String,
    val notes: String
)

/**
 * Enum for daily log status
 */
enum class DailyLogStatus(val displayName: String) {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    REVIEWED("Reviewed")
}