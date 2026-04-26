package com.nextgenbuildpro.hermes

import android.util.Log
import com.nextgenbuildpro.hermes.models.RequestEnvelope
import com.nextgenbuildpro.orchestrator.WadeGlobalState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Converted from wade-global-state/hermes.py.
 * Entry point for all agent routing:
 *   1. OpenClawChunker splits the request into typed TaskChunks.
 *   2. AgentDispatcher executes all chunks in parallel (coroutines mirror asyncio.gather).
 *   3. AuditAgent validates results before returning the RequestEnvelope.
 */
@Singleton
class HermesRouter @Inject constructor(
    private val wgsRepository: WgsRepository
) {
    private val TAG = "HermesRouter"

    suspend fun route(request: String): RequestEnvelope = coroutineScope {
        val wgs: WadeGlobalState = wgsRepository.load()

        // Step 1 — chunk
        val chunks = OpenClawChunker.chunk(request, wgs)
        Log.d(TAG, "Chunked into ${chunks.size} task(s): ${chunks.map { it.agentType }}")

        // Step 2 — parallel dispatch (mirrors asyncio.gather)
        val deferreds = chunks.map { chunk ->
            async { AgentDispatcher.executeChunk(chunk, wgs) }
        }
        val results = deferreds.map { it.await() }

        // Step 3 — audit
        val audit = AuditAgent.audit(chunks, results, request)
        if (!audit.passed) {
            Log.w(TAG, "Audit issues: ${audit.issues}")
        }

        // Persist any state mutations made during dispatch
        wgsRepository.save(wgs)

        RequestEnvelope(
            originalRequest = request,
            chunks = chunks,
            results = results,
            auditPassed = audit.passed,
            finalResponse = results.filter { it.success }.joinToString("\n") { it.output }
        )
    }
}
