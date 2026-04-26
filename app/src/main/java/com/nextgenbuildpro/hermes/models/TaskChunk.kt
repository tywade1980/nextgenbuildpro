package com.nextgenbuildpro.hermes.models

data class TaskChunk(
    val id: String,
    val agentType: String,
    val content: String,
    val priority: Int = 0,
    val metadata: Map<String, String> = emptyMap()
)
