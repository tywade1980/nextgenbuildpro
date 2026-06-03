package com.nextgenbuildpro.construction

/**
 * Construction Platform — Unified Integration Layer
 *
 * Consolidates features from:
 *   - nextgenbuildpro (root): Firestore-backed PM, BMS, estimates, AI orchestrators
 *   - ngbp-v2-0: Room DB, clean architecture, 14-phase workflow, 2025 material pricing seed
 *   - nextgen_apk/apps/ConstructionPlatform.kt: Blueprint management, quality checks,
 *       defect tracking, LEED compliance, supplier ratings, AI-event integration
 *
 * Architecture:
 *   ConstructionPlatformManager
 *       ├── ProjectManager           (Firestore + Room mirror)
 *       ├── MaterialCatalogueManager (ngbp-v2-0 seed data + Firestore pricing)
 *       ├── BlueprintManager         (nextgen_apk domain)
 *       ├── QualityManager           (nextgen_apk quality checks + defect tracking)
 *       ├── LaborManager             (ngbp-v2-0 labor entities + time tracking)
 *       ├── EstimateManager          (root orchestrators: CEOAssistant → CFOFinancial)
 *       └── ComplianceManager        (LEED, ADA, safety requirements)
 */

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.util.UUID

// ─── Unified Project Model ────────────────────────────────────────────────────
// Merges: ngbp-v2-0 ProjectEntity + nextgen_apk ConstructionProject

data class UnifiedProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientEmail: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",

    // Project classification (ngbp-v2-0)
    val type: ProjectType = ProjectType.RESIDENTIAL,
    val status: ProjectStatus = ProjectStatus.PLANNING,
    val phase: ConstructionPhase = ConstructionPhase.PRE_CONSTRUCTION,

    // Financial (root app)
    val budgetTotal: Double = 0.0,
    val budgetSpent: Double = 0.0,
    val estimatedRevenue: Double = 0.0,
    val contractAmount: Double = 0.0,

    // Timeline
    val startDate: LocalDateTime? = null,
    val estimatedEndDate: LocalDateTime? = null,
    val actualEndDate: LocalDateTime? = null,

    // AI/agent features (nextgen_apk + root app)
    val blueprints: List<Blueprint> = emptyList(),
    val qualityChecks: List<QualityCheck> = emptyList(),
    val defects: List<Defect> = emptyList(),
    val workOrders: List<WorkOrder> = emptyList(),
    val safetyRequirements: List<String> = emptyList(),

    // Compliance (nextgen_apk)
    val isLeedCertified: Boolean = false,
    val isAdaCompliant: Boolean = false,
    val permitNumbers: List<String> = emptyList(),

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

// ─── Enums (ngbp-v2-0 + root app merged) ─────────────────────────────────────

enum class ProjectType {
    RESIDENTIAL, COMMERCIAL, INDUSTRIAL, RENOVATION, INFRASTRUCTURE, MIXED_USE
}

enum class ProjectStatus {
    PLANNING, ACTIVE, ON_HOLD, COMPLETED, CANCELLED, BIDDING
}

// Full 14-phase workflow from ngbp-v2-0
enum class ConstructionPhase {
    PRE_CONSTRUCTION,
    FOUNDATION,
    FRAMING,
    MEP_ROUGH_IN,        // Mechanical/Electrical/Plumbing rough-in
    INSULATION,
    DRYWALL,
    FLOORING,
    INTERIOR_FINISH,
    EXTERIOR_FINISH,
    LANDSCAPING,
    FINAL_INSPECTION,
    PUNCH_LIST,
    HANDOVER,
    COMPLETED
}

// ─── Blueprint Model (nextgen_apk) ────────────────────────────────────────────

data class Blueprint(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val type: BlueprintType,
    val version: String = "1.0",
    val fileUrl: String = "",
    val approvedBy: String = "",
    val isApproved: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class BlueprintType {
    ARCHITECTURAL, STRUCTURAL, ELECTRICAL, MECHANICAL, PLUMBING, SITE_PLAN
}

// ─── Quality Check Model (nextgen_apk) ────────────────────────────────────────

data class QualityCheck(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val phase: ConstructionPhase,
    val checkType: String,
    val criteria: List<QualityCriterion>,
    val inspector: String = "",
    val scheduledDate: LocalDateTime? = null,
    val completedDate: LocalDateTime? = null,
    val status: QualityStatus = QualityStatus.PENDING,
    val overallScore: Double = 0.0
)

data class QualityCriterion(
    val name: String,
    val description: String,
    val weight: Double = 1.0,
    val result: CriterionResult = CriterionResult.PENDING,
    val notes: String = ""
)

enum class QualityStatus { PENDING, IN_PROGRESS, PASSED, FAILED, WAIVED }
enum class CriterionResult { PENDING, PASS, FAIL, NA }

// ─── Defect Model (nextgen_apk) ───────────────────────────────────────────────

data class Defect(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val phase: ConstructionPhase,
    val severity: DefectSeverity,
    val description: String,
    val location: String = "",
    val photoUrls: List<String> = emptyList(),
    val reportedBy: String = "",
    val assignedTo: String = "",
    val status: DefectStatus = DefectStatus.OPEN,
    val reportedAt: LocalDateTime = LocalDateTime.now(),
    val resolvedAt: LocalDateTime? = null
)

enum class DefectSeverity { CRITICAL, MAJOR, MINOR, COSMETIC }
enum class DefectStatus { OPEN, IN_PROGRESS, RESOLVED, CLOSED, WAIVED }

// ─── Work Order (nextgen_apk) ─────────────────────────────────────────────────

data class WorkOrder(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val description: String,
    val phase: ConstructionPhase,
    val assignedTo: String = "",
    val priority: WorkOrderPriority = WorkOrderPriority.MEDIUM,
    val isCriticalPath: Boolean = false,
    val estimatedHours: Double = 0.0,
    val actualHours: Double = 0.0,
    val status: WorkOrderStatus = WorkOrderStatus.OPEN,
    val dependencies: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class WorkOrderPriority { LOW, MEDIUM, HIGH, CRITICAL }
enum class WorkOrderStatus { OPEN, IN_PROGRESS, BLOCKED, COMPLETED, CANCELLED }

// ─── Unified Material (ngbp-v2-0 + root app merged) ─────────────────────────

data class UnifiedMaterial(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val subcategory: String = "",
    val unitOfMeasure: String,
    val basePrice: Double,

    // Regional 2025 pricing from ngbp-v2-0 SeedData
    val regionalPricing: Map<String, Double> = emptyMap(),

    // Supplier info
    val supplierName: String = "",
    val supplierSku: String = "",
    val supplierUrl: String = "",
    val supplierReliabilityScore: Double = 1.0,   // 0–1 (nextgen_apk)
    val avgDeliveryDays: Int = 7,

    // Specs
    val specifications: Map<String, String> = emptyMap(),
    val isInStock: Boolean = true,
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun getPriceForRegion(region: String): Double =
        regionalPricing[region] ?: basePrice
}

// ─── Platform Manager ─────────────────────────────────────────────────────────

class ConstructionPlatformManager(private val context: Context) {

    companion object {
        private const val TAG = "ConstructionPlatform"
    }

    private val _projects = MutableStateFlow<List<UnifiedProject>>(emptyList())
    val projects: StateFlow<List<UnifiedProject>> = _projects.asStateFlow()

    private val _activeProject = MutableStateFlow<UnifiedProject?>(null)
    val activeProject: StateFlow<UnifiedProject?> = _activeProject.asStateFlow()

    // ─── Project Operations ───────────────────────────────────────────────────

    fun createProject(project: UnifiedProject): UnifiedProject {
        val current = _projects.value.toMutableList()
        current.add(project)
        _projects.value = current
        Log.i(TAG, "Project created: ${project.name}")
        return project
    }

    fun setActiveProject(projectId: String) {
        _activeProject.value = _projects.value.find { it.id == projectId }
    }

    fun updateProjectPhase(projectId: String, phase: ConstructionPhase): Boolean {
        val projects = _projects.value.toMutableList()
        val idx = projects.indexOfFirst { it.id == projectId }
        if (idx < 0) return false
        projects[idx] = projects[idx].copy(phase = phase, updatedAt = LocalDateTime.now())
        _projects.value = projects
        Log.i(TAG, "Project ${projects[idx].name} advanced to phase: $phase")
        return true
    }

    fun addDefect(projectId: String, defect: Defect): Boolean {
        val projects = _projects.value.toMutableList()
        val idx = projects.indexOfFirst { it.id == projectId }
        if (idx < 0) return false
        val defects = projects[idx].defects.toMutableList()
        defects.add(defect)
        projects[idx] = projects[idx].copy(defects = defects, updatedAt = LocalDateTime.now())
        _projects.value = projects
        Log.w(TAG, "Defect logged on ${projects[idx].name}: ${defect.severity} — ${defect.description}")
        return true
    }

    fun addQualityCheck(projectId: String, check: QualityCheck): Boolean {
        val projects = _projects.value.toMutableList()
        val idx = projects.indexOfFirst { it.id == projectId }
        if (idx < 0) return false
        val checks = projects[idx].qualityChecks.toMutableList()
        checks.add(check)
        projects[idx] = projects[idx].copy(qualityChecks = checks, updatedAt = LocalDateTime.now())
        _projects.value = projects
        return true
    }

    // ─── Dashboard Metrics ────────────────────────────────────────────────────

    fun getDashboardStats(): DashboardStats {
        val all = _projects.value
        return DashboardStats(
            totalProjects = all.size,
            activeProjects = all.count { it.status == ProjectStatus.ACTIVE },
            completedProjects = all.count { it.status == ProjectStatus.COMPLETED },
            totalBudget = all.sumOf { it.budgetTotal },
            totalSpent = all.sumOf { it.budgetSpent },
            openDefects = all.flatMap { it.defects }.count { it.status == DefectStatus.OPEN },
            pendingQualityChecks = all.flatMap { it.qualityChecks }.count { it.status == QualityStatus.PENDING },
            onSchedulePercentage = calculateOnSchedulePercentage(all)
        )
    }

    private fun calculateOnSchedulePercentage(projects: List<UnifiedProject>): Double {
        val active = projects.filter { it.status == ProjectStatus.ACTIVE }
        if (active.isEmpty()) return 100.0
        val onSchedule = active.count { project ->
            project.estimatedEndDate?.isAfter(LocalDateTime.now()) != false
        }
        return (onSchedule.toDouble() / active.size) * 100.0
    }
}

data class DashboardStats(
    val totalProjects: Int,
    val activeProjects: Int,
    val completedProjects: Int,
    val totalBudget: Double,
    val totalSpent: Double,
    val openDefects: Int,
    val pendingQualityChecks: Int,
    val onSchedulePercentage: Double
)
