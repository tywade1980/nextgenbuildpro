package com.nextgenbuildpro.orchestrator

import android.util.Log
import com.nextgenbuildpro.hermes.HermesRouter
import com.nextgenbuildpro.hermes.WgsRepository
import com.nextgenbuildpro.hermes.models.RequestEnvelope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Converted from wade-global-state/orchestrator.py.
 * Sits above HermesRouter and adds:
 *   - Conversation history tracking (mirrors orchestrator.py's history list)
 *   - Voice-friendly response formatting (mirrors format_for_voice())
 *   - WadeGlobalState context injection
 */
@Singleton
class CarolineOrchestrator @Inject constructor(
    private val hermesRouter: HermesRouter,
    private val wgsRepository: WgsRepository
) {
    private val TAG = "CarolineOrchestrator"

    /** Primary entry point — call from CarolineVoiceViewModel or any UI layer. */
    suspend fun handleRequest(userMessage: String): String {
        Log.d(TAG, "handleRequest: $userMessage")

        // Record user turn
        wgsRepository.update {
            conversationHistory.add(ConversationTurn(role = "user", content = userMessage))
            // Keep history bounded (mirrors Python's 20-turn window)
            if (conversationHistory.size > 40) {
                conversationHistory.removeAt(0)
            }
        }

        // Route through Hermes
        val envelope: RequestEnvelope = hermesRouter.route(userMessage)

        // Format response for voice / UI
        val response = formatForVoice(envelope)

        // Record assistant turn
        wgsRepository.update {
            conversationHistory.add(ConversationTurn(role = "assistant", content = response))
            lastUpdated = System.currentTimeMillis()
        }

        return response
    }

    /**
     * Converts a RequestEnvelope into a clean, voice-friendly string.
     * Mirrors format_for_voice() in orchestrator.py.
     */
    private fun formatForVoice(envelope: RequestEnvelope): String {
        if (!envelope.auditPassed && envelope.results.none { it.success }) {
            return "I'm sorry, I ran into a problem processing that. Could you try again?"
        }

        val successOutputs = envelope.results
            .filter { it.success && it.output.isNotBlank() }
            .map { it.output }

        if (successOutputs.isEmpty()) {
            return "Got it. I'll take care of that for you."
        }

        // Single agent — return directly
        if (successOutputs.size == 1) {
            return cleanForVoice(successOutputs.first())
        }

        // Multiple agents — join naturally
        return successOutputs.joinToString(" Also, ") { cleanForVoice(it) }
    }

    /** Strip technical prefixes like "[Calendar]" for cleaner TTS output. */
    private fun cleanForVoice(text: String): String {
        return text
            .replace(Regex("^\\[\\w+\\]\\s*"), "")
            .trim()
            .replaceFirstChar { it.uppercase() }
    }
}
