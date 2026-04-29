package com.nextgenbuildpro.hermes.models

data class AgentResult(
    val agentType: String,
    val chunkId: String,
    val output: String,
    val success: Boolean,
    val errorMessage: String? = null
)

data class RequestEnvelope(
    val originalRequest: String,
    val chunks: List<TaskChunk>,
    val results: List<AgentResult> = emptyList(),
    val auditPassed: Boolean = false,
    val finalResponse: String = ""
)
