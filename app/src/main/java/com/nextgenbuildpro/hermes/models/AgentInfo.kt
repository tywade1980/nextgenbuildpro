package com.nextgenbuildpro.hermes.models

data class AgentInfo(
    val name: String,
    val type: String,
    val endpoint: String? = null,
    val capabilities: List<String> = emptyList()
)
