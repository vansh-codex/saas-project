package com.friday.assistant.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Snapshot of the user-configurable settings. Only non-secret projections of the encrypted
 * store are exposed to the UI.
 */
data class SettingsSnapshot(
    val geminiApiKey: String? = null,
) {
    val hasGeminiApiKey: Boolean get() = !geminiApiKey.isNullOrBlank()
}

/**
 * Single source of truth for settings, backed by [SecureKeyStore].
 */
class SettingsRepository(private val secureKeyStore: SecureKeyStore) {

    private val _settings = MutableStateFlow(
        SettingsSnapshot(geminiApiKey = secureKeyStore.readGeminiApiKey()),
    )

    val settings: StateFlow<SettingsSnapshot> = _settings.asStateFlow()

    val geminiApiKey: String? get() = _settings.value.geminiApiKey

    fun saveGeminiApiKey(apiKey: String) {
        secureKeyStore.writeGeminiApiKey(apiKey)
        _settings.update { it.copy(geminiApiKey = secureKeyStore.readGeminiApiKey()) }
    }

    fun clearGeminiApiKey() {
        secureKeyStore.writeGeminiApiKey(null)
        _settings.update { it.copy(geminiApiKey = null) }
    }
}
