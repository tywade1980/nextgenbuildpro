package com.nextgenbuildpro.construction

import android.util.Log
import com.nextgenbuildpro.brain.UnifiedBrainService
import com.nextgenbuildpro.hermes.WgsRepository
import com.nextgenbuildpro.orchestrator.CrmContact
import com.nextgenbuildpro.orchestrator.JobRecord
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class ProjectMetrics(
    val jobId: String,
    val clientName: String,
    val description: String,
    val status: String
)

data class ProjectDashboard(
    val activeProjects: List<ProjectMetrics>,
    val pendingInvoices: Int,
    val monthlyRevenueForecast: Double,
    val summary: String
)

/**
 * Converted from manus-master-archive/skills/construct-ai/SKILL.md.
 * Orchestrates construction business intelligence for Wade Custom Carpentry.
 */
@Singleton
class ConstructAiManager @Inject constructor(
    private val wgsRepository: WgsRepository,
    private val unifiedBrainService: UnifiedBrainService
) {
    private val TAG = "ConstructAI"

    fun getDashboard(): ProjectDashboard {
        val wgs = wgsRepository.load()
        val active = wgs.scheduledJobs
            .filter { it.status != "completed" && it.status != "cancelled" }
            .map { ProjectMetrics(it.jobId, it.clientName, it.description, it.status) }
        val forecast = active.size * 3500.0
        return ProjectDashboard(
            activeProjects = active,
            pendingInvoices = wgs.crmContacts.size / 3,
            monthlyRevenueForecast = forecast,
            summary = "${active.size} active project(s). Monthly forecast: $${"%.0f".format(forecast)}."
        )
    }

    suspend fun generateEstimate(description: String): String =
        unifiedBrainService.generate(
            messages = listOf(mapOf(
                "role" to "user",
                "content" to "Estimate cost for this Columbus OH construction project, applying RSMeans CCI 0.96. Include labor and materials. Project: $description"
            )),
            persona = "construction", maxTokens = 512
        )

    fun scheduleJob(clientName: String, description: String, scheduledTime: Long): JobRecord {
        val job = JobRecord(UUID.randomUUID().toString(), clientName, description, scheduledTime)
        wgsRepository.update { scheduledJobs.add(job) }
        Log.d(TAG, "Scheduled job for $clientName")
        return job
    }

    fun upsertContact(name: String, phone: String?, email: String?, notes: String = ""): CrmContact {
        val wgs = wgsRepository.load()
        val existing = wgs.crmContacts.find { it.name == name }
        return if (existing != null) {
            wgsRepository.update {
                crmContacts.replaceAll { c ->
                    if (c.contactId == existing.contactId) c.copy(phone = phone ?: c.phone, email = email ?: c.email, notes = notes.ifBlank { c.notes }) else c
                }
            }
            existing
        } else {
            val c = CrmContact(UUID.randomUUID().toString(), name, phone, email, notes)
            wgsRepository.update { crmContacts.add(c) }
            c
        }
    }

    suspend fun voiceBriefing(): String {
        val d = getDashboard()
        return unifiedBrainService.generate(
            messages = listOf(mapOf("role" to "user", "content" to "Give Tyler a 30-second voice briefing on his construction business. ${d.summary}")),
            persona = "caroline", maxTokens = 150
        )
    }
}
