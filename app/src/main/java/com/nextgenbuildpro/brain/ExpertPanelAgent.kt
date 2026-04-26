package com.nextgenbuildpro.brain

import android.util.Log
import com.nextgenbuildpro.hermes.HermesRouter
import com.nextgenbuildpro.hermes.WgsRepository
import com.nextgenbuildpro.skills.SkillRegistry
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Converted from expert-panel/config/SOUL.md.
 * The Expert Panel is a multi-agent intelligence combining Manus, Claude Code,
 * OpenClaw, Permissions, and Hermes. It routes every task to the best specialist
 * using the Assess -> Route -> Gate -> Remember -> Synthesize loop.
 */
@Singleton
class ExpertPanelAgent @Inject constructor(
    private val hermesRouter: HermesRouter,
    private val wgsRepository: WgsRepository,
    private val unifiedBrainService: UnifiedBrainService
) {
    private val TAG = "ExpertPanel"

    private val ROUTING_PROMPT = """
        You are the Expert Panel routing agent. Given a task, reply with ONE word:
        voice, construction, crm, phone, memory, search, or general.
        Pick the specialist best suited for the task.
    """.trimIndent()

    suspend fun handle(request: String): String {
        Log.d(TAG, "Expert Panel: $request")
        val envelope = hermesRouter.route(request)
        if (envelope.chunks.size > 1 && envelope.auditPassed) {
            wgsRepository.update { memory["last_complex_task"] = request.take(200) }
            Log.d(TAG, "Multi-step task learned. Skill candidate: ${SkillRegistry.find("skill-creator")?.name}")
        }
        return envelope.finalResponse.ifBlank { "Done, Tyler." }
    }
}
