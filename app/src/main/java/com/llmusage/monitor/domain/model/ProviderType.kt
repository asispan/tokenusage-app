package com.llmusage.monitor.domain.model

/**
 * Identifier for each supported provider module.
 *
 * Use the [id] string as a stable persistence key (do not persist the enum
 * ordinal — adding/removing entries would shift values).
 */
enum class ProviderType(val id: String, val displayName: String) {
    OPENAI("openai", "OpenAI"),
    ANTHROPIC("anthropic", "Anthropic"),
    OPENROUTER("openrouter", "OpenRouter"),
    GEMINI("gemini", "Google Gemini"),
    MANUAL("manual", "Manual / Other");

    companion object {
        fun fromId(id: String): ProviderType =
            values().firstOrNull { it.id == id } ?: MANUAL
    }
}
