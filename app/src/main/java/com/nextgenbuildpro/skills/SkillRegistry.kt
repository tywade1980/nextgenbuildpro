package com.nextgenbuildpro.skills

/**
 * Converted from manus-master-archive/skills/ and manus-drs-skills/skills/.
 * Models every Manus skill as a typed entry. AgentDispatcher uses the
 * agentType field to route chunks to the correct handler.
 */
data class Skill(
    val name: String,
    val description: String,
    val agentType: String,
    val tags: List<String> = emptyList()
)

object SkillRegistry {

    val ALL: List<Skill> = listOf(
        Skill("caroline-ai",          "Caroline AI companion: voice TTS, WGS sync, RunPod status, personality evolution.",              "voice",        listOf("caroline", "tts", "wgs", "runpod", "elevenlabs")),
        Skill("construct-ai",         "Construction business intelligence: project dashboard, analytics, estimation, client management.", "construction", listOf("construction", "dashboard", "analytics", "estimation", "crm")),
        Skill("wade-custom-carpentry","Project planning, material sourcing, and proposals for Wade Custom Carpentry.",                   "construction", listOf("carpentry", "materials", "proposals")),
        Skill("rsmeans-cost-estimator","RSMeans labor hours, unit costs, Columbus OH CCI adjustments.",                                 "construction", listOf("rsmeans", "cost", "estimate", "labor")),
        Skill("wade-telephony",       "AI receptionist, call handling, SMS, and InCallService management.",                            "phone",        listOf("calls", "sms", "receptionist", "incallservice")),
        Skill("runpod-connector",     "Manage RunPod GPU pods: list, start, stop, terminate via GraphQL API.",                        "general",      listOf("runpod", "gpu", "cloud")),
        Skill("neurorank",            "Decision quality scoring using on-device HRM inference.",                                       "general",      listOf("ai", "inference", "scoring")),
        Skill("centauri-connectors",  "Cross-platform connectors for the Centauri AI OS.",                                            "general",      listOf("centauri", "integration")),
        Skill("centauri-os",          "Core Centauri OS runtime and command routing.",                                                 "general",      listOf("centauri", "os", "routing")),
        Skill("honcho-caroline",      "Honcho user session management layer for Caroline.",                                            "memory",       listOf("sessions", "memory", "caroline")),
        Skill("wade-ecosystem",       "Full Wade ecosystem health check: all services, states, and integrations.",                     "general",      listOf("health", "ecosystem", "monitor")),
        Skill("bgm-prompter",         "Background music prompt generation for the work environment.",                                  "general",      listOf("music", "audio", "ambient")),
        Skill("excel-generator",      "Generate Excel spreadsheets from structured data.",                                             "general",      listOf("excel", "reports", "spreadsheet")),
        Skill("skill-share",          "Share and install skills across Manus instances.",                                              "memory",       listOf("skills", "sync", "share")),
        Skill("internet-skill-finder","Discover and install new skills from the internet.",                                            "search",       listOf("skills", "discovery", "web")),
        Skill("github-gem-seeker",    "Find high-value GitHub repositories and libraries.",                                            "search",       listOf("github", "libraries", "discovery")),
        Skill("centauri-interlock",   "Centauri OS interlock: command approval and safety gating.",                                    "general",      listOf("centauri", "safety", "gate")),
        Skill("skill-creator",        "Create new Manus skills from task patterns.",                                                   "memory",       listOf("skills", "creation", "hermes"))
    )

    fun byTag(tag: String): List<Skill>        = ALL.filter { tag in it.tags }
    fun byAgent(agentType: String): List<Skill> = ALL.filter { it.agentType == agentType }
    fun find(name: String): Skill?              = ALL.firstOrNull { it.name == name }
}
