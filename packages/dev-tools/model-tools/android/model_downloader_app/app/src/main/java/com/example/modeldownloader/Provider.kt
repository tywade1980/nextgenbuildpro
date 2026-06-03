package com.example.modeldownloader

/**
 * Enumeration of supported model providers.  Each provider encapsulates
 * information about its display name and whether or not it requires
 * authentication.  Additional provider‑specific metadata can be stored
 * here in the future if needed.
 */
enum class Provider(val displayName: String, val requiresAuthentication: Boolean) {
    /**
     * Ollama runs locally and therefore does not require authentication.
     */
    OLLAMA("Ollama", false),
    /**
     * Hugging Face offers a hosted model hub.  Public models can be
     * downloaded without a token. We don't require authentication
     * for public models to make it easier to access open source models.
     */
    HUGGING_FACE("Hugging Face", false),
    /**
     * GitHub can host model files. Public repositories don't require
     * authentication for access. Private repositories would need a token.
     */
    GITHUB("GitHub", false);
}