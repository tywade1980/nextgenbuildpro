package com.nextgenbuildpro.brain

/**
 * Converted from unified-brain/voice_profiles.py.
 * Each VoiceProfile maps to a Fish Speech / xAI TTS configuration.
 */
data class VoiceProfile(
    val name: String,
    val voiceId: String,
    val speed: Float = 1.0f,
    val emotion: String = "neutral",
    val pitch: Float = 0f,
    val language: String = "en-US"
)

object VoiceProfiles {

    val CAROLINE = VoiceProfile(
        name = "caroline",
        voiceId = "caroline_v2",
        speed = 1.05f,
        emotion = "warm",
        pitch = -0.5f
    )

    val RECEPTIONIST = VoiceProfile(
        name = "receptionist",
        voiceId = "professional_female_1",
        speed = 1.0f,
        emotion = "professional",
        pitch = 0f
    )

    val CONSTRUCTION = VoiceProfile(
        name = "construction",
        voiceId = "professional_male_1",
        speed = 1.0f,
        emotion = "neutral",
        pitch = 0.2f
    )

    val CRM = VoiceProfile(
        name = "crm",
        voiceId = "friendly_female_1",
        speed = 1.05f,
        emotion = "friendly",
        pitch = 0f
    )

    val GENERAL = VoiceProfile(
        name = "general",
        voiceId = "neutral_1",
        speed = 1.0f,
        emotion = "neutral",
        pitch = 0f
    )

    fun forPersona(persona: String): VoiceProfile = when (persona.lowercase()) {
        "caroline"     -> CAROLINE
        "receptionist" -> RECEPTIONIST
        "construction" -> CONSTRUCTION
        "crm"          -> CRM
        else           -> GENERAL
    }

    fun all(): List<VoiceProfile> = listOf(CAROLINE, RECEPTIONIST, CONSTRUCTION, CRM, GENERAL)
}
