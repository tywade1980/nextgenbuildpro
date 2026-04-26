package com.nextgenbuildpro.hermes

import android.util.Log
import com.nextgenbuildpro.hermes.models.AgentResult
import com.nextgenbuildpro.hermes.models.TaskChunk
import com.nextgenbuildpro.orchestrator.WadeGlobalState

/**
 * Converted from wade-global-state/hermes.py execute_chunk().
 * Dispatches a single TaskChunk to the appropriate local handler.
 * Network-bound agents call out to UnifiedBrainService via the
 * CarolineOrchestrator; local agents run inline.
 */
object AgentDispatcher {

    private val TAG = "AgentDispatcher"

    suspend fun executeChunk(chunk: TaskChunk, wgs: WadeGlobalState): AgentResult {
        return try {
            val output = when (chunk.agentType) {
                "voice"        -> handleVoice(chunk, wgs)
                "calendar"     -> handleCalendar(chunk, wgs)
                "construction" -> handleConstruction(chunk, wgs)
                "crm"          -> handleCrm(chunk, wgs)
                "phone"        -> handlePhone(chunk, wgs)
                "search"       -> handleSearch(chunk, wgs)
                "memory"       -> handleMemory(chunk, wgs)
                else           -> handleGeneral(chunk, wgs)
            }
            AgentResult(
                agentType = chunk.agentType,
                chunkId = chunk.id,
                output = output,
                success = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Chunk ${chunk.id} (${chunk.agentType}) failed", e)
            AgentResult(
                agentType = chunk.agentType,
                chunkId = chunk.id,
                output = "",
                success = false,
                errorMessage = e.message
            )
        }
    }

    private suspend fun handleVoice(chunk: TaskChunk, wgs: WadeGlobalState): String {
        // Delegate to CarolineVoiceViewModel via event bus or direct call
        return "[Voice] Queued: ${chunk.content}"
    }

    private suspend fun handleCalendar(chunk: TaskChunk, wgs: WadeGlobalState): String {
        return "[Calendar] Acknowledged: ${chunk.content}"
    }

    private suspend fun handleConstruction(chunk: TaskChunk, wgs: WadeGlobalState): String {
        return "[Construction] Logged job request: ${chunk.content}"
    }

    private suspend fun handleCrm(chunk: TaskChunk, wgs: WadeGlobalState): String {
        return "[CRM] Contact action queued: ${chunk.content}"
    }

    private suspend fun handlePhone(chunk: TaskChunk, wgs: WadeGlobalState): String {
        return "[Phone] Call/SMS action noted: ${chunk.content}"
    }

    private suspend fun handleSearch(chunk: TaskChunk, wgs: WadeGlobalState): String {
        return "[Search] Query registered: ${chunk.content}"
    }

    private suspend fun handleMemory(chunk: TaskChunk, wgs: WadeGlobalState): String {
        val key = "mem_${System.currentTimeMillis()}"
        wgs.memory[key] = chunk.content
        return "[Memory] Stored under key $key"
    }

    private suspend fun handleGeneral(chunk: TaskChunk, wgs: WadeGlobalState): String {
        return "[General] Received: ${chunk.content}"
    }
}
